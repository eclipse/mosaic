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

package org.eclipse.mosaic.lib.util.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * This is a test for the {@link Event}.
 */
public class EventTest {

    private final static long time = 100L;

    private final static long nice = 100L;

    private final static EventProcessor processor = new EventProcessor() {
        @Override
        public void processEvent(final Event event) {
            // do nothing
        }

        @Override
        public boolean canProcessEvent() {
            return true;
        }
    };

    private final static ArrayList<EventProcessor> processorList = new ArrayList<>(
            Arrays.asList(processor)
    );

    private final static ArrayList<EventProcessor> processorListEmpty = new ArrayList<>();

    private final static ArrayList<EventProcessor> processorListContainsNull = new ArrayList<>(
            Arrays.asList(processor, null)
    );

    /**
     * The PriorityQueue sorts the events in their natural order.
     */
    private final PriorityQueue<Event> queue = new PriorityQueue<>();

    /**
     * The event manager to add events to the queue.
     */
    private final EventManager eventManager = queue::add;

    private final static EventProcessor interceptProcessor = new EventProcessor() {
        @Override
        public void processEvent(Event event) {
            // do nothing
        }

        @Override
        public boolean canProcessEvent() {
            return true;
        }
    };

    private final static ArrayList<EventProcessor> interceptProcessorList = new ArrayList<>(
            Arrays.asList(interceptProcessor)
    );

    private final static String resource = "resource";

    public EventTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test a constructor.
     */
    @Test
    public void testConstructor0CorrectValues() {
        // this constructor use a default value for the resource and nice value
        final Event event = new Event(time, processor);
        // the default nice value
        assertEquals(0, event.getNice());
        // the default resource value
        assertNull(event.getResource());
        // the return value for the class name if no resource was given
        assertNull(event.getResourceClassSimpleName());
    }

    /**
     * Test a constructor.
     */
    @Test
    public void testConstructor1CorrectValues() {
        // this constructor requires a resource value
        final Event event = new Event(time, processor, resource);
        // the given resource value
        assertEquals(resource, event.getResource());
        // the given resource value was a String
        assertEquals("String", event.getResourceClassSimpleName());
    }

    /**
     * Test a constructor.
     */
    @Test
    public void testConstructor2CorrectValues() {
        // this constructor requires a processor list instead a single processor
        final Event event = new Event(time, processorList, resource, Event.NICE_DEFAULT_PRIORITY);
        // the given processor list
        assertEquals(processorList, event.getProcessors());
    }

    /**
     * Test a constructor.
     */
    @Test
    public void testConstructor3CorrectValues() {
        // this constructor require all arguments and require only one processor
        final Event event = new Event(time, processor, resource, nice);
        // the given processor in a list
        assertEquals(processorList, event.getProcessors());
        // additional checks, safety first =)
        assertEquals(time, event.getTime());
        assertEquals(nice, event.getNice());
        assertEquals(resource, event.getResource());
        assertEquals("String", event.getResourceClassSimpleName());
    }

    /**
     * Test a constructor.
     */
    @Test
    public void testConstructor4CorrectValues() {
        // this constructor require all arguments and require processor list instead a single processor
        final Event event = new Event(time, processorList, resource, nice);
        // the given processor list
        assertEquals(processorList, event.getProcessors());
        // additional checks, safety first =)
        assertEquals(time, event.getTime());
        assertEquals(nice, event.getNice());
        assertEquals(resource, event.getResource());
        assertEquals("String", event.getResourceClassSimpleName());
    }

    /**
     * Test a constructor.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor4NonProcessor() {
        // failsafe
        assertTrue(processorListEmpty.isEmpty());
        // the constructor should throw an exception because the given processor list is empty
        new Event(time, processorListEmpty, resource, nice);
    }

    /**
     * Test a constructor.
     */
    @Test(expected = NullPointerException.class)
    public void testConstructor4NullProcessor() {
        // failsafe
        assertTrue(processorListContainsNull.contains(null));
        // the constructor should throw an exception because a given processor is null
        new Event(time, processorListContainsNull, resource, nice);
    }

    @Test
    public void testCompareTime() {
        // create events with different times
        final Event eventA = new Event(-1, processorList, resource, nice);
        final Event eventB = new Event(0, processorList, resource, nice);
        final Event eventC = new Event(1, processorList, resource, nice);

        // the PriorityQueue sorts the events in their natural order
        final PriorityQueue<Event> queue = new PriorityQueue<>();

        // add the events in random order
        queue.add(eventC);
        queue.add(eventA);
        queue.add(eventB);

        // check the natural order of the events
        assertEquals(eventA, queue.poll());
        assertEquals(eventB, queue.poll());
        assertEquals(eventC, queue.poll());
    }

    @Test
    public void testCompareNice() {
        // create events with different priorities
        final Event eventA = new Event(0, processorList, resource, Event.NICE_MAX_PRIORITY);
        final Event eventB = new Event(0, processorList, resource, Event.NICE_DEFAULT_PRIORITY);
        final Event eventC = new Event(0, processorList, resource, Event.NICE_MIN_PRIORITY);

        // the PriorityQueue sorts the events in their natural order
        final PriorityQueue<Event> queue = new PriorityQueue<>();

        // add the events in random order
        queue.add(eventC);
        queue.add(eventA);
        queue.add(eventB);

        // check the natural order of the events
        assertEquals(eventA, queue.poll());
        assertEquals(eventB, queue.poll());
        assertEquals(eventC, queue.poll());
    }

    @Test
    public void testCompareTimeAndNice() {
        // create events with different times and priorities
        final Event eventA = new Event(-10, processorList, resource, 10);
        final Event eventB = new Event(-10, processorList, resource, 25);
        final Event eventC = new Event(0, processorList, resource, 0);
        final Event eventD = new Event(0, processorList, resource, 120);
        final Event eventE = new Event(1, processorList, resource, 0);
        final Event eventF = new Event(1, processorList, resource, 40);

        // the PriorityQueue sorts the events in their natural order
        final PriorityQueue<Event> queue = new PriorityQueue<>();

        // add the events in random order
        queue.add(eventE);
        queue.add(eventD);
        queue.add(eventB);
        queue.add(eventA);
        queue.add(eventF);
        queue.add(eventC);

        // check the natural order of the events
        assertEquals(eventA, queue.poll());
        assertEquals(eventB, queue.poll());
        assertEquals(eventC, queue.poll());
        assertEquals(eventD, queue.poll());
        assertEquals(eventE, queue.poll());
        assertEquals(eventF, queue.poll());
    }

    @Test
    public void testToString() {
        // create an event with default values
        final Event event = new Event(time, processorList, resource, nice);
        assertNotNull(event.toString());
    }

    @Test
    public void testConstructInterceptedEvent0() {
        // this constructor requires all arguments and require only one processor
        final Event eventToIntercept = new Event(time, processor, resource, nice);
        final InterceptedEvent interceptedEvent = new InterceptedEvent(eventToIntercept, interceptProcessorList);
        testInterceptedEvent(interceptedEvent, eventToIntercept);
    }

    private void testInterceptedEvent(final InterceptedEvent interceptedEvent, final Event eventToIntercept) {
        // the resource should be an event
        Event originalEvent = (Event) interceptedEvent.getResource();
        assertNotNull(originalEvent);

        // the resource of the intercepted event should be the event
        assertEquals(eventToIntercept, originalEvent);

        // the processors shouldn't be the same
        assertNotEquals(interceptedEvent.getProcessors(), originalEvent.getProcessors());

        // the time and nice value should be the same
        assertEquals(originalEvent.getTime(), interceptedEvent.getTime());
        assertEquals(originalEvent.getNice(), interceptedEvent.getNice());
    }

    @Test
    public void testEventInterceptorConstructor0CorrectValues() {
        final EventInterceptor eventInterceptor = new EventInterceptor(eventManager, processor);
        testEventInterceptor(eventInterceptor);
    }

    @Test
    public void testEventInterceptorConstructor1CorrectValues() {
        final EventInterceptor eventInterceptor = new EventInterceptor(eventManager, processorList);
        testEventInterceptor(eventInterceptor);
    }

    private void testEventInterceptor(final EventInterceptor eventInterceptor) {
        // add two events
        eventInterceptor.addEvent(new Event(0, processor));
        eventInterceptor.addEvent(new Event(0, processor));
        // the queue should contain two events
        assertEquals(2, queue.size());
        // clear the queue
        queue.clear();
    }

    /**
     * Test a constructor.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEventInterceptorConstructor4NonProcessor() {
        // failsafe
        assertTrue(processorListEmpty.isEmpty());
        // the constructor should throw an exception because the given processor list is empty
        new EventInterceptor(eventManager, processorListEmpty);
    }

    /**
     * Test a constructor.
     */
    @Test(expected = NullPointerException.class)
    public void testEventInterceptorConstructor4NullProcessor() {
        // failsafe
        assertTrue(processorListContainsNull.contains(null));
        // the constructor should throw an exception because a given processor is null
        new EventInterceptor(eventManager, processorListContainsNull);
    }

}
