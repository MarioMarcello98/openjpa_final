package org.apache.openjpa.util;

import org.junit.*;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;

@RunWith(Enclosed.class)
public class ProxyManagerImplTests {

    @RunWith(Parameterized.class)
    public static class CopyCustomTests {
        private ProxyManagerImpl proxyManager;
        private final Object obj;
        private final ObjectType objectInstance;

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
                    {ObjectType.NON_PROXYABLE}
            });
        }

        @Before
        public void setUp() {
            proxyManager = spy(new ProxyManagerImpl());
            proxyManager.setUnproxyable(NonProxyableIstance.class.getName());
        }

        @Test
        public void testCopyCustom() {
            Object output = proxyManager.copyCustom(obj);
            switch (objectInstance) {
                case NULL:
                case NON_PROXYABLE:
                    Assert.assertNull(output);
                    break;
                case PROXYABLE:
                    Assert.assertNotNull(output);
                    Assert.assertEquals(
                            ((ProxyableInstance) obj).getDummy(),
                            ((ProxyableInstance) output).getDummy()
                    );
                    break;
            }
        }

        @After
        public void tearDown() {
            proxyManager = null;
        }

        private Object generateObject(ObjectType type) throws Exception {
            switch (type) {
                case NULL:
                    return null;
                case PROXYABLE:
                    return new ProxyableInstance();
                case NON_PROXYABLE:
                    return new NonProxyableIstance("Fiat", "Punto");
                default:
                    throw new Exception("Invalid object type");
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class NewCustomProxyTests {
        private ProxyManagerImpl proxyManager;
        private final Object obj;
        private final ObjectType objectInstance;
        private final boolean autoOff;

        public NewCustomProxyTests(ObjectType objectType, boolean autoOff) throws Exception {
            this.obj = generateObject(objectType);
            this.objectInstance = objectType;
            this.autoOff = autoOff;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
             //      objType, autoOff
                    {ObjectType.NULL, true},
                    {ObjectType.PROXYABLE, true},
                    {ObjectType.PROXYABLE, false},
                    {ObjectType.NON_PROXYABLE, false}
            });
        }

        @Before
        public void setUp() {
            proxyManager = spy(new ProxyManagerImpl());
            proxyManager.setUnproxyable(NonProxyableIstance.class.getName());
        }

        @Test
        public void testNewCustomProxy() {
            Proxy ret = proxyManager.newCustomProxy(obj, autoOff);
            switch (objectInstance) {
                case NULL:
                case NON_PROXYABLE:
                    Assert.assertNull(ret);
                    break;
                case PROXYABLE:
                    assertThat(ret, instanceOf(Proxy.class));
                    break;
            }
        }

        @After
        public void tearDown() {
            proxyManager = null;
        }

        private Object generateObject(ObjectType type) throws Exception {
            switch (type) {
                case NULL:
                    return null;
                case PROXYABLE:
                    return new ProxyableInstance();
                case NON_PROXYABLE:
                    return new NonProxyableIstance("Fiat", "Punto");
                default:
                    throw new Exception("Invalid object type");
            }
        }
    }
}
