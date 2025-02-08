package org.apache.openjpa.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class CacheMapPuttingTest {

    private CacheMap cacheMap;
    private Object key;
    private Object value;
    private final STATE_OF_KEY stateOfKey;
    private final STATE_OF_VALUE stateOfValue;
    private final boolean isKeyPinned;
    private final Object existingValue = new Object();

    private enum STATE_OF_KEY { NULL, EXISTENT, NOT_EXISTENT, INVALID }
    private enum STATE_OF_VALUE { NULL, VALID, INVALID }

    @Parameterized.Parameters(name = "{index}: key={0}, pinned={1}, value={2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {STATE_OF_KEY.NULL, false, STATE_OF_VALUE.VALID},
                {STATE_OF_KEY.NULL, false, STATE_OF_VALUE.NULL},
                {STATE_OF_KEY.NULL, false, STATE_OF_VALUE.INVALID},
                {STATE_OF_KEY.EXISTENT, false, STATE_OF_VALUE.VALID},
                {STATE_OF_KEY.EXISTENT, false, STATE_OF_VALUE.NULL},
                {STATE_OF_KEY.EXISTENT, false, STATE_OF_VALUE.INVALID},
                {STATE_OF_KEY.NOT_EXISTENT, false, STATE_OF_VALUE.VALID},
                {STATE_OF_KEY.NOT_EXISTENT, false, STATE_OF_VALUE.NULL},
                {STATE_OF_KEY.NOT_EXISTENT, false, STATE_OF_VALUE.INVALID},
                {STATE_OF_KEY.INVALID, false, STATE_OF_VALUE.VALID},
                {STATE_OF_KEY.INVALID, false, STATE_OF_VALUE.NULL},
                {STATE_OF_KEY.INVALID, false, STATE_OF_VALUE.INVALID},
                {STATE_OF_KEY.EXISTENT, true, STATE_OF_VALUE.VALID},
                {STATE_OF_KEY.EXISTENT, true, STATE_OF_VALUE.NULL},
                {STATE_OF_KEY.EXISTENT, true, STATE_OF_VALUE.INVALID},
                {STATE_OF_KEY.NOT_EXISTENT, true, STATE_OF_VALUE.VALID},
                {STATE_OF_KEY.NOT_EXISTENT, true, STATE_OF_VALUE.NULL},
                {STATE_OF_KEY.NOT_EXISTENT, true, STATE_OF_VALUE.INVALID}
        });
    }

    public CacheMapPuttingTest(STATE_OF_KEY stateOfKey, boolean isKeyPinned, STATE_OF_VALUE stateOfValue) {
        this.stateOfKey = stateOfKey;
        this.isKeyPinned = isKeyPinned;
        this.stateOfValue = stateOfValue;
    }

    @Before
    public void setUp() {
        this.cacheMap = spy(new CacheMap());

        switch (stateOfKey) {
            case NULL:
                this.key = null;
                break;
            case INVALID:
                this.key = new MyInvalidObject();
                break;
            case EXISTENT:
                this.key = new Object();
                this.cacheMap.put(this.key, this.existingValue);
                if (this.isKeyPinned) {
                    this.cacheMap.pin(this.key);
                }
                break;
            case NOT_EXISTENT:
                this.key = new Object();
                if (this.isKeyPinned) {
                    this.cacheMap.pin(this.key);
                }
                break;
        }

        switch (stateOfValue) {
            case NULL:
                this.value = null;
                break;
            case INVALID:
                this.value = new MyInvalidObject();
                break;
            case VALID:
                this.value = new Object();
                break;
        }
    }

    @Test
    public void testPut() {
        Object retVal = this.cacheMap.put(this.key, this.value);


        if (this.stateOfKey == STATE_OF_KEY.NOT_EXISTENT) {
            verify(this.cacheMap).entryAdded(this.key, this.value);
        }

        else if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
            verify(this.cacheMap).entryRemoved(this.key, this.existingValue, false);
            verify(this.cacheMap).entryAdded(this.key, this.value);
        }


        int expectedLockCalls = (this.stateOfKey == STATE_OF_KEY.EXISTENT) ? 2 : 1;
        if (this.isKeyPinned) expectedLockCalls++;

        verify(this.cacheMap, times(expectedLockCalls)).writeLock();
        verify(this.cacheMap, times(expectedLockCalls)).writeUnlock();


        Assert.assertEquals(this.value, this.cacheMap.get(this.key));
        if (this.stateOfKey == STATE_OF_KEY.EXISTENT) {
            Assert.assertEquals(this.existingValue, retVal);
        } else {
            Assert.assertNull(retVal);
        }
    }

    @After
    public void tearDown() {
        this.cacheMap.clear();
    }
}
