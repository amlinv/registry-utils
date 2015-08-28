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

import java.util.Iterator;

/**
 * Created by art on 5/5/15.
 */
public interface NotificationExecutor<K, V> {
    /**
     * Fire notification of a new entry added to the registry.
     *
     * @param putKey key identifying the entry in the registry.
     * @param putValue value of the entry in the registry.
     */
    void firePutNotification(Iterator<RegistryListener<K, V>> listeners, K putKey, V putValue);

    /**
     * Fire notification of an entry that was just removed from the registry.
     *
     * @param removeKey key identifying the entry removed from the registry.
     * @param removeValue value of the entry in the registry.
     */
    void fireRemoveNotification (Iterator<RegistryListener<K, V>> listeners, K removeKey,
                                 V removeValue);

    /**
     * Fire notification of an entry for which the value was just replaced in the registry.
     * @param replaceKey key identifying the entry in the registry for which the value was replaced.
     * @param oldValue old value of the entry in the registry.
     * @param newValue new value of the entry in the registry.
     */
    void fireReplaceNotification (Iterator<RegistryListener<K, V>> listeners, K replaceKey,
                                  V oldValue, V newValue);
}
