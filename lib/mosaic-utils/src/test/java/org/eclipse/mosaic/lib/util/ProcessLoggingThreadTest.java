/*
 * Copyright (c) 2026 Fraunhofer FOKUS and others. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ProcessLoggingThreadTest {

    private static final int REPETITIONS = 500;
    private static final int LINES_AFTER_START = 20;

    /**
     * Reads the first line of a process output through a tee and closes the tee, as the SUMO ambassador does to read
     * SUMO's TraCI port, and checks that the logging thread keeps logging the following lines. Closing the tee makes its
     * thread remove it from the logging thread's list of tees while the logging thread may iterate over that list, so
     * the scenario is repeated to hit this race. The test mimics:
     * <ul>
     *   <li>fed/mosaic-sumo/src/main/java/org/eclipse/mosaic/fed/sumo/ambassador/SumoAmbassador.java: startSumoLocal(),
     *   which creates the tees, and getPortFromStdOut(), which reads the first lines</li>
     *   <li>lib/mosaic-utils/src/main/java/org/eclipse/mosaic/lib/util/ProcessLoggingThread.java: flushLog(), which
     *   iterates over the tees, and the finally block in TeeInputStream, which removes a tee</li>
     * </ul>
     */
    @Test
    public void closeTeeWhileLogging_loggingContinues() throws Exception {
        for (int i = 0; i < REPETITIONS; i++) {
            // a pipe instead of a process: the test writes the process's output into processOutput, and loggedStream stands
            // for the stream the logging thread reads, e.g. Process.getInputStream() of SUMO in startSumoLocal() in
            // fed/mosaic-sumo/src/main/java/org/eclipse/mosaic/fed/sumo/ambassador/SumoAmbassador.java
            final PipedOutputStream processOutput = new PipedOutputStream();
            final PipedInputStream loggedStream = new PipedInputStream(processOutput, 64 * 1024);
            final List<String> loggedLines = new CopyOnWriteArrayList<>();
            final ProcessLoggingThread loggingThread = new ProcessLoggingThread("test", loggedStream, loggedLines::add);

            // like startSumoLocal() in SumoAmbassador.java (see above): create the tee, start logging, read a line through
            // the tee (an example line based on the one getPortFromStdOut() in the same file parses for SUMO's TraCI port),
            // and close the tee at the end of the try-with-resources block
            try (InputStream tee = loggingThread.teeInputStream()) {
                loggingThread.start();
                writeLine(processOutput, "Starting server on port 4711", i);
                assertEquals("Starting server on port 4711",
                        new BufferedReader(new InputStreamReader(tee, StandardCharsets.UTF_8)).readLine());
            }

            // the process's further output, e.g. SUMO's warnings during the simulation; the first of these lines wakes the
            // closed tee's thread, which then removes the tee (TeeInputStream in ProcessLoggingThread.java)
            for (int line = 0; line < LINES_AFTER_START; line++) {
                writeLine(processOutput, "line " + line, i);
            }
            final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (loggedLines.size() < LINES_AFTER_START + 1 && loggingThread.isAlive() && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            assertEquals("Lines logged in repetition " + i, LINES_AFTER_START + 1, loggedLines.size());

            loggingThread.close();
            processOutput.close();
            loggingThread.join(TimeUnit.SECONDS.toMillis(5));
        }
    }

    private static void writeLine(PipedOutputStream processOutput, String line, int repetition) {
        try {
            processOutput.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            processOutput.flush();
        } catch (IOException e) {
            // a real process gets SIGPIPE here: the logging thread has closed its end of the stream
            fail("Logging thread stopped reading the process output in repetition " + repetition + ": " + e.getMessage());
        }
    }
}
