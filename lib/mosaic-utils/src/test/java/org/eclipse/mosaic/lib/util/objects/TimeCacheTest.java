/*
 * Copyright (c) 2020 Fraunhofer FOKUS and others. All rights reserved.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.mosaic.lib.util.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class TimeCacheTest {

    @Test
    public void testSimpleAddAndGet() {
        TimeCache<Item> cache = new TimeCache<>();
        Item item = new Item();
        cache.putItem(0, item);
        Item itemFromCache = cache.getItem(item.getId());
        assertSame(item, itemFromCache);
    }

    @Test
    public void testGarbageCollection() {
        TimeCache<Item> cache = new TimeCache<>();

        SortedMap<Long, Integer> timeIdMapping = new TreeMap<>();
        // create and add new messages, i is the simulation time
        for (long i = 0; i < 30; i++) {
            Item item = new Item();
            // assume that every message was sent at a different time
            cache.putItem(i, item);
            // add the generated id to the cache
            timeIdMapping.put(i, item.getId());
        }

        // all messages are in the cache?
        for (Map.Entry<Long, Integer> entrySet : timeIdMapping.entrySet()) {
            long time = entrySet.getKey();
            int msgId = entrySet.getValue();
            Item item = cache.getItem(msgId);
            assertNotNull(item);
            assertEquals(msgId, item.getId());
        }

        // try to clean only the first message (sent at time 0)
        Set<Integer> collected = cache.garbageCollection(1);
        // get the first sent message
        Integer firstMessageId = timeIdMapping.get(timeIdMapping.firstKey());
        // collected should contain the first message
        assertTrue(collected.contains(firstMessageId));
        // remove the first sent message
        timeIdMapping.remove(timeIdMapping.firstKey());


        // get the second message and send it at again at later time
        // hint: the second message is now the first message
        Integer secondItemId = timeIdMapping.get(timeIdMapping.firstKey());
        Item secondItem = cache.getItem(secondItemId);
        assertNotNull(secondItem);

        // sent again at later time
        cache.putItem(30, secondItem);
        // add the generated id to the cache
        timeIdMapping.put(30L, secondItem.getId());

        // try to collect all messages without the second message
        collected = cache.garbageCollection(30);
        assertEquals(28, collected.size());

        int lastMessageId = timeIdMapping.get(timeIdMapping.lastKey());
        assertEquals(secondItem.getId(), lastMessageId);

        // the last message shouldn't be collected
        assertFalse(collected.contains(lastMessageId));

        // collect the last message
        collected = cache.garbageCollection(31);
        assertEquals(1, collected.size());
        assertTrue(collected.contains(secondItem.getId()));

        collected = cache.garbageCollection(Long.MAX_VALUE);
        assertEquals(0, collected.size());
    }

    static class Item implements Identifiable {

        private static int idCounter = 0;

        private final int  id = idCounter++;;

        @Override
        public int getId() {
            return id;
        }
    }
}
