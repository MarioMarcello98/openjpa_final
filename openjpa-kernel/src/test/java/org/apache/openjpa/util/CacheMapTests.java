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

        public CacheMapPutTest(String keyType, String valueType, boolean existingKey, boolean maxSize, boolean pinnedMap, Object output) {            this.keyType = keyType;
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
            //     keyType, valueType, inSoftMap, maxsize, pinnedMap, expectedOutput
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
                    {VALID, INVALID, true, false, false, null},
                    {VALID, INVALID, false, false, false, null},
                    {VALID, INVALID, true, false, false, 0},

                    {INVALID, NULL, true, false, false, null},
                    {INVALID, NULL, false, false, false, null},
                    {INVALID, VALID, true, false, false, 0},
                    {INVALID, VALID, false, false, false, null},
                    {INVALID, INVALID, true, false, false, null},
                    {INVALID, INVALID, false, false, false, null},

                    {VALID, NULL, false, false, true, null},
                    {VALID, VALID, false, false, true, 0},
                    {VALID, VALID, true, true, false, null},
                    {VALID, INVALID, false, false, true, null},
                    {VALID, INVALID, true, true, false, null},

            });
        }

        @Before
        public void setUp() {
            cacheMap = spy(new CacheMap());

            if (!Objects.equals(keyType, NULL) && !Objects.equals(valueType, NULL)) {
                setParam(keyType);
                setParam(valueType);
            }

            if (existingKey)
                cacheMap.put(key, value);

            if(pinnedMap)
                cacheMap.put(cacheMap.pinnedMap, key, value);

            if (maxSize)
                cacheMap.cacheMap.setMaxSize(0);

        }

        @Test
        public void test() {
            if (!Objects.equals(valueType, NULL) && !Objects.equals(keyType, NULL))
                if (Objects.equals(valueType, VALID))
                    value = ValueNew;
                else
                    value = new InvalidKeyValue();

                // res è l'output da controllare
            Object res = cacheMap.put(key, value);
            Object checkGet = cacheMap.get(key);

            if (output != null && Objects.equals(keyType, VALID)) {
                if (valueType.equals(VALID)) {
                    Assert.assertEquals(output, res);
                    Assert.assertEquals(ValueNew, checkGet);
                }
            } else if (output != null && Objects.equals(keyType, INVALID)) {
                Assert.assertEquals(output, res);
                Assert.assertEquals(ValueNew, checkGet);
            } else {

                Assert.assertNull(res);
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
                    value = null;
                    break;
                case VALID:
                    key = new Object();
                    value = ValueOld;
                    break;
                case INVALID:
                    key = new InvalidKeyValue();
                    value = new InvalidKeyValue();
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
                    {INVALID, false, false, null}



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