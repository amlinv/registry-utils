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

package com.amlinv.registry.util.listener;

import com.amlinv.registry.util.RegistryListener;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by art on 8/27/15.
 */
public class SimpleSynchronousNotificationExecutorTest {

  public SimpleSynchronousNotificationExecutor<String, String> executor;
  public Iterator<RegistryListener<String, String>> listnerIterator;
  public List<RegistryListener<String, String>> mockListeners;

  @Before
  public void setupTest() throws Exception {
    this.executor = new SimpleSynchronousNotificationExecutor<>();

    this.mockListeners = new LinkedList<>();
    this.mockListeners.add(Mockito.mock(RegistryListener.class));
    this.mockListeners.add(Mockito.mock(RegistryListener.class));
    this.mockListeners.add(Mockito.mock(RegistryListener.class));
  }

  @Test
  public void testFirePutNotification() throws Exception {
    this.executor.firePutNotification(this.mockListeners.iterator(), "x-key1-x", "x-value1-x");

    Mockito.verify(this.mockListeners.get(0)).onPutEntry("x-key1-x", "x-value1-x");
    Mockito.verify(this.mockListeners.get(1)).onPutEntry("x-key1-x", "x-value1-x");
    Mockito.verify(this.mockListeners.get(2)).onPutEntry("x-key1-x", "x-value1-x");
  }

  @Test
  public void testFireRemoveNotification() throws Exception {
    this.executor.fireRemoveNotification(this.mockListeners.iterator(), "x-key1-x", "x-value1-x");

    Mockito.verify(this.mockListeners.get(0)).onRemoveEntry("x-key1-x", "x-value1-x");
    Mockito.verify(this.mockListeners.get(1)).onRemoveEntry("x-key1-x", "x-value1-x");
    Mockito.verify(this.mockListeners.get(2)).onRemoveEntry("x-key1-x", "x-value1-x");
  }

  @Test
  public void testFireReplaceNotification() throws Exception {
    this.executor.fireReplaceNotification(this.mockListeners.iterator(), "x-key1-x", "x-value1-x",
                                          "x-value2-x");

    Mockito.verify(this.mockListeners.get(0))
        .onReplaceEntry("x-key1-x", "x-value1-x", "x-value2-x");
    Mockito.verify(this.mockListeners.get(1))
        .onReplaceEntry("x-key1-x", "x-value1-x", "x-value2-x");
    Mockito.verify(this.mockListeners.get(2))
        .onReplaceEntry("x-key1-x", "x-value1-x", "x-value2-x");

  }
}