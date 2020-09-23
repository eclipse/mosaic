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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;

public class MultiThreadedEventScheduler extends DefaultEventScheduler {

    private final ExecutorService executorService;

    public MultiThreadedEventScheduler(int threads) {
        executorService = Executors.newFixedThreadPool(threads);
    }

    @Override
    public synchronized void addEvent(@Nonnull final Event event) {
        super.addEvent(event);
    }

    @Override
    public int scheduleEvents(long time) {
        if (isEmpty()) {
            return 0;
        }

        final List<Callable<Integer>> executables = new ArrayList<>();
        long nice = getNextEventNice();
        int processedEvents = 0;
        scheduledTime = time;

        while (true) {
            final Event nextEvent = super.eventQueue.peek();
            if (nextEvent == null) {
                processedEvents += executeEvents(executables);
                return processedEvents;
            }

            if (nextEvent.getTime() < time) {
                throw new RuntimeException("Scheduled event lies in the past.");
            } else if (nextEvent.getTime() == time && nextEvent.getNice() == nice) {
                super.eventQueue.remove(); // remove the head of the queue
                executables.add(nextEvent::execute);
            } else if (nextEvent.getTime() == time) {
                super.eventQueue.remove(); // remove the head of the queue
                // same time, but different nice value
                processedEvents += executeEvents(executables);
                executables.clear();

                nice = nextEvent.getNice();
                executables.add(nextEvent::execute);
            } else {
                // else case: nextEvent.getTime() > time
                // do not schedule this event, push it back to the queue
                processedEvents += executeEvents(executables);
                return processedEvents;
            }
        }
    }

    private synchronized long getNextEventNice() {
        if (isEmpty()) {
            throw new IllegalStateException("No event in the queue.");
        }
        return eventQueue.peek().getNice();
    }

    /**
     * Executes a list of EventExecutors and wait for its response.
     *
     * @param executables to process
     * @return the total of processed events by all threads.
     */
    private int executeEvents(List<Callable<Integer>> executables) {
        int processedEvents = 0;

        try {
            List<Future<Integer>> futures = executorService.invokeAll(executables);
            for (Future<Integer> future : futures) {
                processedEvents += future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return processedEvents;
    }
}
