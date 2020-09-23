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

package org.eclipse.mosaic.rti.time;

import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.FederateAmbassador;
import org.eclipse.mosaic.rti.api.InternalFederateException;
import org.eclipse.mosaic.rti.api.time.FederateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ThreadPool {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadPool.class);


    private final Worker[] workers;
    private final ComponentProvider federation;
    private boolean running = false;

    private ScheduledEvents queue = null;
    private int activeCount = 0;

    private InternalFederateException exceptionInThread = null;

    ThreadPool(ComponentProvider federation, int numberOfThreads) {
        this.workers = new Worker[numberOfThreads];
        this.federation = federation;
    }

    void setEventQueue(ScheduledEvents events) {
        this.queue = events;
    }

    /**
     * Initializes the workers of this thread pool.
     */
    void initialize() {
        this.running = true;
        for (int i = 0; i < this.workers.length; i++) {
            this.workers[i] = new Worker(i + 1);
            this.workers[i].start();
        }
    }

    void shutdown() {
        this.running = false;
        for (Worker worker : workers) {
            if (worker.isAlive()) {
                try {
                    worker.join();
                } catch (Exception e) {
                    // nop
                }
            }
        }
    }

    boolean hasException() {
        return this.exceptionInThread != null;
    }

    InternalFederateException getLastException() {
        return this.exceptionInThread;
    }

    boolean isActive() {
        synchronized (this.queue.isEmptyMutex) {
            return this.activeCount > 0 || !this.queue.isEmpty();
        }
    }

    /**
     * Returns the number of worker threads in this thread pool.
     *
     * @return the number of worker threads in this thread pool.
     */
    int getThreadCount() {
        return workers.length;
    }

    private class Worker extends Thread {

        private Worker(int i) {
            super(String.format("%04d", i));
        }

        @Override
        public void run() {
            FederateEvent ev;
            Task task;

            while (true) {
                synchronized (queue.accessMutex) {
                    while (queue.isEmpty()) {
                        if (!running) {
                            return;
                        }
                    }
                    synchronized (queue.isEmptyMutex) {
                        ev = queue.getNextScheduledEvent();
                        activeCount++;
                    }
                    task = new Task(ev);
                }

                try {
                    task.run();
                } catch (RuntimeException e) {
                    LOG.error("Could not execute task", e);
                }
                synchronized (queue.isEmptyMutex) {
                    queue.setEventProcessed(ev);
                    activeCount--;
                    LOG.debug("active count: {}; isEmpty: {}", activeCount, queue.isEmpty());
                    if (activeCount == 0 && queue.isEmpty()) {
                        queue.isEmptyMutex.notifyAll();
                    }
                }
            }
        }
    }

    private class Task implements Runnable {

        private final FederateEvent ev;

        private Task(FederateEvent ev) {
            this.ev = ev;
        }

        @Override
        public void run() {
            try {
                FederateAmbassador ambassador = federation.getFederationManagement().getAmbassador(ev.getFederateId());
                if (ambassador != null) {
                    federation.getMonitor().onBeginActivity(ev);
                    long startTime = System.currentTimeMillis();
                    ambassador.advanceTime(ev.getRequestedTime());
                    federation.getMonitor().onEndActivity(ev, System.currentTimeMillis() - startTime);
                }
            } catch (InternalFederateException iex) {
                exceptionInThread = iex;
            } catch (Exception ex) {
                exceptionInThread = new InternalFederateException(ex);
            }
        }
    }
}
