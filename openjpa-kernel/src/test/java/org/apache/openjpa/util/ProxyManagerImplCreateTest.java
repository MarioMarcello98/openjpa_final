package org.apache.openjpa.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ProxyManagerImplCreateTest {
    private ProxyManagerImpl proxyManager;
    private final Object obj;
    private final ObjectType objectInstance;
    private final boolean autoOff;

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
                {ObjectType.NON_PROXYABLE, false}
        });
    }

    @Before
    public void setUp() {
        proxyManager = spy(new ProxyManagerImpl());
        proxyManager.setUnproxyable(NonProxyableIstance.class.getName());   // set this type of class as not proxyable
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
                return null;
            case PROXYABLE:
                return new ProxyableInstance();
            case NON_PROXYABLE:
                return new NonProxyableIstance("Apple", "iPhone12");
            default:
                throw new Exception("Invalid argument");
        }
    }

    private void checkNewProxy(Proxy output) {
        switch (objectInstance) {
            case NULL:
            case NON_PROXYABLE:
                Assert.assertNull(output);
                break;
            case PROXYABLE:
                Assert.assertThat(output, instanceOf(Proxy.class));    // check that is effectively a Proxy instance
                break;
        }
    }

    @After
    public void tearDown() {
        proxyManager = null;
    }
}