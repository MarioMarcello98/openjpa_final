package org.apache.openjpa.util;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.MockedStatic;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class ProxyManagerImplCopyTest {
    private ProxyManagerImpl proxyManager;
    private Object obj;
    private final ObjectType objectInstance;
    private MockedStatic<ImplHelper> mockImpl;

    public ProxyManagerImplCopyTest(ObjectType objectType) throws Exception {
        this.obj = generateObj(objectType);
        this.objectInstance = objectType;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {ObjectType.NULL},
                {ObjectType.PROXYABLE},
                {ObjectType.NON_PROXYABLE},
                // Test cases added after jacoco
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
            proxyManager.setUnproxyable(NonProxyableInstanceFinal.class.getName());   // set this type of class as not proxyable

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

        /* Check integrity of copy */
        checkCopy(output);
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
                collection.add(1);
                collection.add(2);
                return collection;
            case MAP:
                Map<String, Integer> map = new HashMap<>();
                map.put("A", 1);
                map.put("B", 2);
                return map;
            case DATE:
                Date date = new Date();
                date.setTime(date.getTime() + 1000);
                return date;
            case CALENDAR:
                Calendar calendar = Calendar.getInstance();
                calendar.setMinimalDaysInFirstWeek(2);
                return calendar;
            case MANAGEABLE_TYPE:
                /* Any type of data is valid, we put a simple int */
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
                /* check that the state of the proxied object (output) is the same of the original obj */
                Assert.assertEquals(((ProxyableInstance) output).getDummy(), ((ProxyableInstance) obj).getDummy());
                break;
            case COLLECTION:
            case MAP:
                /* check that the state of the proxied object (output) is the same of the original obj */
                Assert.assertEquals(output, obj);
                break;
            case DATE:
                /* check that the state of the proxied object (output) is the same of the original obj */
                Assert.assertEquals(((Date) output).getTime(), ((Date) obj).getTime());
                break;
            case CALENDAR:
                /* check that the state of the proxied object (output) is the same of the original obj */
                Assert.assertEquals(((Calendar) output).getMinimalDaysInFirstWeek(), ((Calendar) obj).getMinimalDaysInFirstWeek());
                break;
            case PROXY:
                /* check that the state of the proxied object (output) is the same of the original obj */
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