package org.apache.openjpa.util;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.*;
import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;



@RunWith(Enclosed.class)
public class ProxyManagerImplTests {

    @RunWith(Parameterized.class)
    public static class CopyCustomTests {
        private ProxyManagerImpl proxyManager;
        private Object obj;
        private final ObjectType objectInstance;
        private MockedStatic<ImplHelper> mockImpl;

        public CopyCustomTests(ObjectType objectType) throws Exception {
            this.obj = generateObject(objectType);
            this.objectInstance = objectType;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // objectType
                    {ObjectType.NULL},
                    {ObjectType.PROXYABLE},
                    {ObjectType.NON_PROXYABLE},
                    {ObjectType.PROXY},
                    {ObjectType.COLLECTION},
                    {ObjectType.MAP},
                    {ObjectType.DATE},
                    {ObjectType.CALENDAR},
                    {ObjectType.MANAGEABLE_TYPE}
            });
        }

        @Before
        public void setUp() {
            proxyManager = spy(new ProxyManagerImpl());

            if (objectInstance.equals(ObjectType.NON_PROXYABLE))
                proxyManager.setUnproxyable(NonProxyableIstance.class.getName());

            if (objectInstance.equals(ObjectType.PROXY))
                obj = proxyManager.newCalendarProxy(Calendar.class, TimeZone.getTimeZone("GMT"));

            if (objectInstance.equals(ObjectType.MANAGEABLE_TYPE)) {
                mockImpl = mockStatic(ImplHelper.class);
                when(ImplHelper.isManageable(any())).thenReturn(true);
            }
        }


        @Test
        public void test() {
            Object output = proxyManager.copyCustom(obj);
            checkCopy(output);
        }

        private Object generateObject(ObjectType objectType) throws Exception {
            switch (objectType) {
                case NULL:
                case PROXY:
                    return null;
                case PROXYABLE:
                    return new ProxyableInstance();
                case NON_PROXYABLE:
                    return new NonProxyableIstance("0001", "NonProxyable");
                case COLLECTION:
                    Collection<Integer> collection = new ArrayList<>();
                    collection.add(1);
                    collection.add(2);
                    return collection;
                case MAP:
                    Map<String, Integer> map = new HashMap<>();
                    map.put("A", 0);
                    map.put("B", 1);
                    return map;
                case DATE:
                    Date date = new Date();
                    date.setTime(date.getTime());
                    return date;
                case CALENDAR:
                    Calendar calendar = Calendar.getInstance();
                    calendar.setMinimalDaysInFirstWeek(2);
                    return calendar;
                case MANAGEABLE_TYPE:
                    return 1;
                default:
                    throw new Exception("Invalid argument");
            }
        }

        private void checkCopy(Object output) {
            switch (objectInstance) {
                case NULL:
                case NON_PROXYABLE:
                case MANAGEABLE_TYPE:
                    Assert.assertNull(output);
                    break;
                case PROXYABLE:
                    assert output != null;
                    Assert.assertEquals(((ProxyableInstance) output).getState(), ((ProxyableInstance) obj).getState());
                    break;
                case COLLECTION:
                case MAP:
                    Assert.assertEquals(output, obj);
                    break;
                case DATE:
                    Assert.assertEquals(((Date) output).getTime(), ((Date) obj).getTime());
                    break;
                case CALENDAR:
                    Assert.assertEquals(((Calendar) output).getMinimalDaysInFirstWeek(), ((Calendar) obj).getMinimalDaysInFirstWeek());
                    break;
                case PROXY:
                    Assert.assertEquals(((Calendar) output).getTimeZone(), ((Calendar) obj).getTimeZone());
                    break;
            }
        }

        @After
        public void tearDown() {
            proxyManager = null;

            if (mockImpl != null)
                mockImpl.close();
        }
    }

    @RunWith(Parameterized.class)
    public static class NewCustomProxyTests {
        private ProxyManagerImpl proxyManager;
        private Object obj;
        private final ObjectType objectInstance;
        private final boolean autoOff;
        private MockedStatic<ImplHelper> mockImpl;


        public NewCustomProxyTests(ObjectType objectType, boolean autoOff) throws Exception {
            this.obj = generateObject(objectType);
            this.objectInstance = objectType;
            this.autoOff = autoOff;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    //  objType, autoOff
                    {ObjectType.NULL, true},
                    {ObjectType.PROXYABLE, true},
                    {ObjectType.PROXYABLE, false},
                    {ObjectType.NON_PROXYABLE, false},
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
            });
        }

        @Before
        public void setUp() {
            proxyManager = spy(new ProxyManagerImpl());

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
            checkNewProxy(ret);
        }

        private Object generateObject(ObjectType objectType) throws Exception {
            switch (objectType) {
                case NULL:
                case PROXY:
                    return null;
                case PROXYABLE:
                    return new ProxyableInstance();
                case NON_PROXYABLE:
                    return new NonProxyableIstance("0001", "NonProxyable");
                case MAP:
                    Map<String, Integer> map = new HashMap<>();
                    return map;
                case COLLECTION:
                    Collection<Integer> collection = new ArrayList<>();
                    return collection;
                case DATE:
                    return new Date();
                case CALENDAR:
                    return Calendar.getInstance();
                case MANAGEABLE_TYPE:
                    return 1;
                case SORTED_MAP:
                    SortedMap<Integer, Integer> sortedMap = new TreeMap<>();
                    return sortedMap;
                case SORTED_SET:
                    SortedSet<Integer> sortedSet = new TreeSet<>();
                    return sortedSet;
                case TIMESTAMP:
                    return new Timestamp(System.currentTimeMillis());
                default:
                    throw new Exception("Invalid argument");
            }
        }

        private void checkNewProxy(Proxy output) {
            if (!objectInstance.equals(ObjectType.NON_PROXYABLE) &&
                    !objectInstance.equals(ObjectType.NULL) &&
                    !objectInstance.equals(ObjectType.MANAGEABLE_TYPE))
                Assert.assertThat(output, instanceOf(Proxy.class));
            else
                Assert.assertNull(output);
        }

        @After
        public void tearDown() {
            proxyManager = null;

            if (mockImpl != null)
                mockImpl.close();
        }
    }
}
