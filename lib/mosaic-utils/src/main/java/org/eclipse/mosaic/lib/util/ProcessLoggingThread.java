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
 *
 * Contact: mosaic@fokus.fraunhofer.de
 */

package org.eclipse.mosaic.lib.util;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ProcessLoggingThread extends Thread {

    private final String processName;
    private final Consumer<String> lineConsumer;
    private final InputStream stream;
    private boolean running = true;

    public ProcessLoggingThread(String processName, InputStream stream, Consumer<String> lineConsumer) {
        this.processName = processName;
        this.stream = stream;
        this.lineConsumer = lineConsumer;
    }

    public void close() {
        running = false;
    }

    @Override
    public void run() {
        flushLog(this.stream);
    }

    /**
     * Flushes the stream to the given logback logger.
     *
     * @param stream stream to flush
     */
    @SuppressWarnings(value = "REC_CATCH_EXCEPTION",
            justification = "Read exception will always occur if something goes wrong and the process we monitor is dead.")
    private void flushLog(InputStream stream) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            String line;
            while (running) {
                if ((line = bufferedReader.readLine()) != null) {
                    lineConsumer.accept("Process " + processName + ": " + line);
                }
            }

        } catch (Exception ex) {
            /* Read exception will always occur, if something goes wrong and the process we monitor is dead.
             * Therefore it is a normal behavior and it is safe to ignore them.
             */
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // quiet
            }

        }
    }
}
