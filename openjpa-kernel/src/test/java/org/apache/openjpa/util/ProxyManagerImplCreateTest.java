package org.apache.openjpa.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.*;

@RunWith(Parameterized.class)
public class ProxyManagerImplCreateTest {
    private ProxyManagerImpl proxyManager;
    private Object obj;
    private final ObjectType objectInstance;
    private final boolean autoOff;
    private MockedStatic<ImplHelper> mockImpl;

    public ProxyManagerImplCreateTest(ObjectType objectType, boolean autoOff) throws Exception {
        this.obj = generateObj(objectType);
        this.objectInstance = objectType;
        this.autoOff = autoOff;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {ObjectType.NULL, true},
                {ObjectType.PROXYABLE, true},
                {ObjectType.PROXYABLE, false},
                {ObjectType.NON_PROXYABLE, false},
                // Test cases added after jacoco
                {ObjectType.PROXY, true},
                {ObjectType.PROXY, false},
                {ObjectType.COLLECTION, true},
                {ObjectType.COLLECTION, false},
                {ObjectType.MAP, true},
                {ObjectType.MAP, false},
                {ObjectType.DATE, true},
                {ObjectType.DATE, false},
                {ObjectType.CALENDAR, true},
                {ObjectType.CALENDAR, false},
                {ObjectType.MANAGEABLE_TYPE, true},
                {ObjectType.SORTED_MAP, true},
                {ObjectType.SORTED_MAP, false},
                {ObjectType.SORTED_SET, true},
                {ObjectType.SORTED_SET, false},
                {ObjectType.TIMESTAMP, true},
                {ObjectType.TIMESTAMP, false},
                {ObjectType.HIDE_NON_PROXYABLE, true}
        });
    }

    @Before
    public void setUp() {
        proxyManager = spy(new ProxyManagerImpl());

        if (objectInstance.equals(ObjectType.HIDE_NON_PROXYABLE))
            proxyManager.setUnproxyable(NonProxyableInstanceNotFinal.class.getName());   // set this type of class as not proxyable

        if (objectInstance.equals(ObjectType.PROXY))
            obj = proxyManager.newDateProxy(Date.class);

        if (objectInstance.equals(ObjectType.MANAGEABLE_TYPE)) {
            mockImpl = mockStatic(ImplHelper.class);
            when(ImplHelper.isManageable(any())).thenReturn(true);
        }
    }

    @Test
    public void test() {
        Proxy ret = proxyManager.newCustomProxy(obj, autoOff);

        /* Check new Proxy */
        checkNewProxy(ret);
    }

    private Object generateObj(ObjectType objectType) throws Exception {
        switch (objectType) {
            case NULL:
            case PROXY:
                return null;
            case PROXYABLE:
                return new ProxyableInstance();
            case NON_PROXYABLE:
                return new NonProxyableInstanceFinal("Apple", "iPhone12");
            case COLLECTION:
                Collection<Integer> collection = new ArrayList<>();
                return collection;
            case MAP:
                Map<String, Integer> map = new HashMap<>();
                map.put("A", 1);
                return map;
            case DATE:
                Date date = new Date();
                date.setTime(date.getTime() + 1000);
                return date;
            case CALENDAR:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(500);
                return calendar;
            case MANAGEABLE_TYPE:
                /* Any type of data is valid, we put a simple int */
                return 1;
            case TIMESTAMP:
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                timestamp.setNanos(1000);
                return timestamp;
            case SORTED_MAP:
                SortedMap<Integer, Integer> sortedMap = new TreeMap<>();
                return sortedMap;
            case SORTED_SET:
                SortedSet<Integer> sortedSet = new TreeSet<>();
                return sortedSet;
            case HIDE_NON_PROXYABLE:
                return new NonProxyableInstanceNotFinal("Hello World");
            default:
                throw new Exception("Invalid argument");
        }
    }

    private void checkNewProxy(Proxy output) {
        if (!objectInstance.equals(ObjectType.NON_PROXYABLE) &&
                !objectInstance.equals(ObjectType.NULL) &&
                !objectInstance.equals(ObjectType.MANAGEABLE_TYPE) &&
                !objectInstance.equals(ObjectType.HIDE_NON_PROXYABLE))
            Assert.assertThat(output, instanceOf(Proxy.class));    // check that is effectively a Proxy instance
        else
            Assert.assertNull(output);

        /* Killed all remaining mutations */
        if (objectInstance.equals(ObjectType.MAP)) {
            Assert.assertEquals(((Map<?, ?>) output).get("A"), ((Map<?, ?>) obj).get("A"));
        } else if (objectInstance.equals(ObjectType.DATE)) {
            Assert.assertEquals(((Date) output).getTime(), ((Date) obj).getTime());
        } else if (objectInstance.equals(ObjectType.TIMESTAMP)) {
            Assert.assertEquals(((Timestamp) output).getNanos(), ((Timestamp) obj).getNanos());
        } else if (objectInstance.equals(ObjectType.CALENDAR)) {
            Assert.assertEquals(((Calendar) output).getTimeInMillis(), ((Calendar) obj).getTimeInMillis());
        }
    }

    @After
    public void tearDown() {
        proxyManager = null;

        if (mockImpl != null)
            mockImpl.close();
    }
}