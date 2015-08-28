/*
 *   Copyright 2015 AML Innovation & Consulting LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.amlinv.registry.util;

import com.amlinv.registry.util.listener.NotificationExecutor;

import com.amlinv.registry.util.listener.SimpleSynchronousNotificationExecutor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by art on 8/27/15.
 */
public class ConcurrentRegistryTest {

    private ConcurrentRegistry<String, String> registry;
    private NotificationExecutor<String, String> mockNotificationExecutor;
    private List<RegistryListener<String, String>> mockRegistryListeners;

    @Before
    public void setupTest() throws Exception {
        this.mockNotificationExecutor = Mockito.mock(NotificationExecutor.class);
        this.registry = new ConcurrentRegistry<>(this.mockNotificationExecutor);
        this.mockRegistryListeners = new LinkedList<>();

        this.mockRegistryListeners.add(Mockito.mock(RegistryListener.class));
        this.mockRegistryListeners.add(Mockito.mock(RegistryListener.class));
        this.mockRegistryListeners.add(Mockito.mock(RegistryListener.class));
    }

    @Test
    public void testConstructWithNotificationExecutor() throws Exception {
        assertSame(this.mockNotificationExecutor, this.registry.getNotificationExecutor());
    }

    @Test
    public void testConstructWithoutNotificationExecutor() throws Exception {
        this.registry = new ConcurrentRegistry<>();

        assertTrue(this.registry.getNotificationExecutor() instanceof SimpleSynchronousNotificationExecutor);
    }

    @Test
    public void testGetSetListeners() throws Exception {
        assertEquals(0, this.registry.getListeners().size());

        this.registry.setListeners(this.mockRegistryListeners);
        assertEquals(this.mockRegistryListeners, this.registry.getListeners());
    }

    @Test
    public void testAddListener() throws Exception {
        assertEquals(0, this.registry.getListeners().size());

        this.registry.addListener(this.mockRegistryListeners.get(0));
        assertEquals(this.mockRegistryListeners.subList(0, 1), this.registry.getListeners());

        this.registry.addListener(this.mockRegistryListeners.get(1));
        assertEquals(this.mockRegistryListeners.subList(0, 2), this.registry.getListeners());

        this.registry.addListener(this.mockRegistryListeners.get(2));
        assertEquals(this.mockRegistryListeners.subList(0, 3), this.registry.getListeners());
    }

    @Test
    public void testRemoveListener() throws Exception {
        assertEquals(0, this.registry.getListeners().size());

        this.registry.setListeners(this.mockRegistryListeners);
        this.registry.removeListener(this.mockRegistryListeners.get(2));
        assertEquals(this.mockRegistryListeners.subList(0, 2), this.registry.getListeners());
    }

    @Test
    public void testGetPut() throws Exception {
        assertNull(this.registry.get("x-key1-x"));

        this.registry.put("x-key1-x", "x-value1-x");
        assertEquals("x-value1-x", this.registry.get("x-key1-x"));
    }

    @Test
    public void testContainsKey() throws Exception {
        assertFalse(this.registry.containsKey("x-key1-x"));

        this.registry.put("x-key1-x", "x-value1-x");
        assertTrue(this.registry.containsKey("x-key1-x"));
    }

    @Test
    public void testPutIfAbsent() throws Exception {
        this.registry.setListeners(this.mockRegistryListeners);

        this.registry.putIfAbsent("x-key1-x", "x-value1-x");
        this.registry.putIfAbsent("x-key1-x", "x-value2-x");
        assertEquals("x-value1-x", this.registry.get("x-key1-x"));

        //
        // Need to use an argument captor here instead of an argument matcher as the latter attempts to evaluate more
        //  than once, leading to the problem that iterators cannot be rewound.
        //
        ArgumentCaptor<Iterator> captureIterator = ArgumentCaptor.forClass(Iterator.class);
        Mockito.verify(this.mockNotificationExecutor)
                .firePutNotification(captureIterator.capture(),
                        Mockito.eq("x-key1-x"),
                        Mockito.eq("x-value1-x"));

        this.validateIterator(captureIterator.getValue(), this.mockRegistryListeners.toArray());

        Mockito.verifyNoMoreInteractions(this.mockNotificationExecutor);
    }

    @Test
    public void testReplace() throws Exception {
        this.registry.setListeners(this.mockRegistryListeners);

        this.registry.put("x-key1-x", "x-value1-x");
        assertEquals("x-value1-x", this.registry.get("x-key1-x"));
        this.registry.put("x-key1-x", "x-value1B-x");
        assertEquals("x-value1B-x", this.registry.get("x-key1-x"));

        //
        // Need to use an argument captor here instead of an argument matcher as the latter attempts to evaluate more
        //  than once, leading to the problem that iterators cannot be rewound.
        //
        ArgumentCaptor<Iterator> captureIterator = ArgumentCaptor.forClass(Iterator.class);
        Mockito.verify(this.mockNotificationExecutor, Mockito.times(1))
                .fireReplaceNotification(captureIterator.capture(),
                        Mockito.eq("x-key1-x"),
                        Mockito.eq("x-value1-x"),
                        Mockito.eq("x-value1B-x"));

        this.validateIterator(captureIterator.getValue(), this.mockRegistryListeners.toArray());
    }

    @Test
    public void testRemove() throws Exception {
        this.registry.setListeners(this.mockRegistryListeners);

        this.registry.remove("x-key1-x");
        Mockito.verifyZeroInteractions(this.mockNotificationExecutor);

        this.registry.put("x-key1-x", "x-value1-x");
        this.registry.remove("x-key1-x");


        //
        // Need to use an argument captor here instead of an argument matcher as the latter attempts to evaluate more
        //  than once, leading to the problem that iterators cannot be rewound.
        //
        ArgumentCaptor<Iterator> captureIterator = ArgumentCaptor.forClass(Iterator.class);
        Mockito.verify(this.mockNotificationExecutor)
                .fireRemoveNotification(captureIterator.capture(),
                        Mockito.eq("x-key1-x"),
                        Mockito.eq("x-value1-x"));

        this.validateIterator(captureIterator.getValue(), this.mockRegistryListeners.toArray());
    }

    @Test
    public void testRemoveByKeyAndValue() throws Exception {
        this.registry.setListeners(this.mockRegistryListeners);

        this.registry.remove("x-key1-x");
        Mockito.verifyZeroInteractions(this.mockNotificationExecutor);

        this.registry.put("x-key1-x", "x-value1-x");
        this.registry.remove("x-key1-x", "x-wrong-value-x");
        Mockito.verify(this.mockNotificationExecutor, Mockito.times(0))
                .fireRemoveNotification(Mockito.any(Iterator.class), Mockito.anyString(), Mockito.anyString());

        this.registry.remove("x-key1-x", "x-value1-x");


        //
        // Need to use an argument captor here instead of an argument matcher as the latter attempts to evaluate more
        //  than once, leading to the problem that iterators cannot be rewound.
        //
        ArgumentCaptor<Iterator> captureIterator = ArgumentCaptor.forClass(Iterator.class);
        Mockito.verify(this.mockNotificationExecutor)
                .fireRemoveNotification(captureIterator.capture(),
                        Mockito.eq("x-key1-x"),
                        Mockito.eq("x-value1-x"));

        this.validateIterator(captureIterator.getValue(), this.mockRegistryListeners.toArray());

    }

    @Test
    public void testKeys() throws Exception {
        assertEquals(0, this.registry.keys().size());

        this.registry.put("x-key1-x", "x-value1-x");
        assertEquals(new HashSet<>(Arrays.asList("x-key1-x")), this.registry.keys());

        this.registry.put("x-key2-x", "x-value2-x");
        assertEquals(new HashSet<>(Arrays.asList("x-key1-x", "x-key2-x")), this.registry.keys());

        this.registry.put("x-key3-x", "x-value3-x");
        assertEquals(new HashSet<>(Arrays.asList("x-key1-x", "x-key2-x", "x-key3-x")), this.registry.keys());
    }

    @Test
    public void testValues() throws Exception {
        assertEquals(0, this.registry.values().size());

        this.registry.put("x-key1-x", "x-value1-x");
        validateIterator(this.registry.values().iterator(), "x-value1-x");
        assertEquals(new HashSet<>(Arrays.asList("x-value1-x")), new HashSet<>(this.registry.values()));

        this.registry.put("x-key2-x", "x-value2-x");
        assertEquals(new HashSet<>(Arrays.asList("x-value1-x", "x-value2-x")), new HashSet<>(this.registry.values()));

        this.registry.put("x-key3-x", "x-value3-x");
        assertEquals(new HashSet<>(Arrays.asList("x-value1-x", "x-value2-x", "x-value3-x")),
                new HashSet<>(this.registry.values()));
    }

    @Test
    public void testAsMap() throws Exception {
        Map<String, String> expected = new HashMap<>();
        assertEquals(expected, this.registry.asMap());

        this.registry.put("x-key1-x", "x-value1-x");
        expected.put("x-key1-x", "x-value1-x");
        assertEquals(expected, this.registry.asMap());

        this.registry.put("x-key2-x", "x-value2-x");
        expected.put("x-key2-x", "x-value2-x");
        assertEquals(expected, this.registry.asMap());

        this.registry.put("x-key3-x", "x-value3-x");
        expected.put("x-key3-x", "x-value3-x");
        assertEquals(expected, this.registry.asMap());
    }



                                                 ////             ////
                                                 ////  INTERNALS  ////
                                                 ////             ////

    protected void validateIterator(Iterator actual, Object... expectedObjs) throws Exception {
        int cur = 0;
        while (cur < expectedObjs.length) {
            if (! actual.hasNext()) {
                throw new AssertionError("actual number of objects expected not received: actual=" + cur +
                        "expected=" + expectedObjs.length);
            }

            Object actualObj = actual.next();
            if (! expectedObjs[cur].equals(actualObj) ) {
                throw new AssertionError("iterator contents do not match the expected element: pos=" + cur);
            }

            cur++;
        }

        if (actual.hasNext()) {
            throw new AssertionError("actual number of objects expected exceeds the number expected: expected-count=" +
                    expectedObjs.length);
        };
    }
}