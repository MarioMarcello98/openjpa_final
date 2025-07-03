package org.apache.openjpa.util;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class CacheMapTests {

    @RunWith(Parameterized.class)
    public static class CacheMapPutTest {
        private Object key;
        private Object value;
        private final boolean existingKey;
        private CacheMap cacheMap;
        private final Object output;
        private Integer ValueOld = 0;
        private Integer ValueNew = 1;
        public String keyType;
        public String valueType;
        private boolean maxSize;
        private boolean pinnedMap;


        private static final String NULL = "null";
        private static final String VALID = "valid";
        private static final String INVALID = "invalid";

        public CacheMapPutTest(String keyType, String valueType, boolean existingKey, boolean maxSize, boolean pinnedMap, Object output) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.existingKey = existingKey;
            this.output = output;
            this.maxSize = maxSize;
            this.pinnedMap = pinnedMap;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
            //     keyType, valueType, existingKey, maxsize, pinnedMap, expectedOutput
                    {NULL, NULL, false, false, false, null},
                    {NULL, NULL, true, false, false, null},
                    {NULL, VALID, false, false, false, null},
                    {NULL, VALID, true, false, false, 0},
                    {NULL, INVALID, false, false, false, null},
                    {NULL, INVALID, true, false, false, null},

                    {VALID, NULL, true, false, false, null},
                    {VALID, NULL, false, false, false, null},
                    {VALID, VALID, true, false, false, 0},
                    {VALID, VALID, false, false, false, null},
                    {VALID, INVALID, true, false, false, new InvalidKeyValue()},

                    {INVALID, NULL, true, false, false, null},
                    {INVALID, NULL, false, false, false, null},
                    {INVALID, VALID, true, false, false, null},
                    {INVALID, VALID, false, false, false, null},
                    {INVALID, INVALID, true, false, false, null},
                    {INVALID, INVALID, false, false, false, null},

                    {VALID, NULL, false, false, true, null},
                    {VALID, VALID, false, false, true, 0},
                    {VALID, VALID, true, true, false, null},
                    {VALID, INVALID, false, false, true, new InvalidKeyValue()},
                    {VALID, INVALID, true, true, false, null},
                    {VALID, VALID, false, true, false, null},
                    {VALID, INVALID, false, true, false, null}



            });
        }

        @Before
        public void setUp() {
            cacheMap = spy(new CacheMap());

            setKey(keyType);
            setValue(valueType);

            if (maxSize) {
                cacheMap.cacheMap.setMaxSize(0);
            } else if (existingKey) {
                cacheMap.put(key, value);
            } else if (pinnedMap) {
                cacheMap.put(cacheMap.pinnedMap, key, value);
            }

        }

        @Test
        public void test() {
            Object newValue = null;
            if (!Objects.equals(valueType, NULL))
                if (Objects.equals(valueType, VALID))
                    newValue = ValueNew;
                else
                    newValue = new InvalidKeyValue();

            if (pinnedMap) {
                cacheMap.cacheMap.clear();
            }

            // res è l'output da controllare
            Object res = cacheMap.put(key, newValue);
            Object checkGet = cacheMap.get(key);

            if (output != null) {
                Assert.assertEquals(output, res);
            } else {
                Assert.assertNull(res);
            }

            if (!valueType.equals(NULL) && !maxSize && !keyType.equals(INVALID))
                Assert.assertNotNull(checkGet);
            else
                Assert.assertNull(checkGet);

            if (pinnedMap) {
                if (valueType.equals(NULL)) {
                    verify(cacheMap).entryAdded(key, newValue);
                } else {
                    verify(cacheMap).entryRemoved(key, value, false);
                    verify(cacheMap).entryAdded(key, newValue);
                }
                verify(cacheMap).writeUnlock();
            } else if (!maxSize && existingKey && valueType.equals(VALID) && keyType.equals(VALID)) {
                verify(cacheMap).entryRemoved(key, value, false);
                verify(cacheMap).entryAdded(key, newValue);
                verify(cacheMap, times(2)).writeUnlock();
            } else if (existingKey && valueType.equals(NULL)) {
                verify(cacheMap, times(2)).entryAdded(key, newValue);
            }
        }

        @After
        public void tearDown() {
            cacheMap.clear();
        }

        private void setKey(String keyType) {
            switch (keyType) {
                case NULL:
                    key = null;
                    break;
                case VALID:
                    key = new Object();
                    break;
                case INVALID:
                    key = new InvalidKeyValue();
                    break;
            }
        }

        private void setValue(String valueType) {
            switch (valueType) {
                case NULL:
                    value = null;
                    break;
                case VALID:
                    value = ValueOld;
                    break;
                case INVALID:
                    value = this.output;
                    break;
            }
        }
    }


    @RunWith(Parameterized.class)
    public static class CacheMapGetTest {
        private Object key;
        private String keyType;
        private boolean existingKey;
        private CacheMap cacheMap;
        private Object output;
        private Integer Value = 5;
        private boolean inSoftMap;
        private int softMapSize = 512;

        private static final String NULL = "null";
        private static final String VALID = "valid";
        private static final String INVALID = "invalid";

        public CacheMapGetTest(String keyType, boolean existingKey, boolean inSoftMap, Object output) {
            this.keyType = keyType;
            this.existingKey = existingKey;
            this.output = output;
            this.inSoftMap = inSoftMap;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
              //    keyType, existingKey, inSoftMap, expectedOutput
                    {NULL, false, false, null},
                    {NULL, true, false, 5},
                    {VALID, false, false, null},
                    {VALID, true, false, 5},
                    {INVALID, true, false, null},
                    {INVALID, false, false, null},

                    {NULL, false, true, 5},
                    {VALID, true, true, 5},
                    {VALID, false, false, null},
                    {INVALID, true, true, null},
                    {INVALID, false, false, null},

                    {VALID, false, true, 5},
                    {INVALID, false, true, null},
            });
        }

        @Before
        public void setUp() {
            cacheMap = spy(new CacheMap());

            setParam(keyType);

            if (existingKey)
                cacheMap.put(key, Value);

            if (inSoftMap) {
                cacheMap.setSoftReferenceSize(softMapSize);
                cacheMap.put(cacheMap.softMap, key, Value);
            }

        }

        @Test
        public void test() {
            Object res = cacheMap.get(key);

            if (output != null && keyType.equals(VALID) || keyType.equals(NULL)) {
                Assert.assertEquals(output, res);
            } else {
                Assert.assertNull(res);
            }


            if (existingKey && inSoftMap && !keyType.equals(INVALID)) {
                verify(cacheMap, times(2)).put(key, Value);
            }
        }

        @After
        public void tearDown() {
            cacheMap.clear();
        }

        private void setParam(String param) {
            switch (param) {
                case NULL:
                    key = null;
                    break;
                case VALID:
                    key = new Object();
                    break;
                case INVALID:
                    key = new InvalidKeyValue();
                    break;
            }
        }
    }
}