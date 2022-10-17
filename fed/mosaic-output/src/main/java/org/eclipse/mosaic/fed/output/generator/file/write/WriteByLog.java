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

package org.eclipse.mosaic.fed.output.generator.file.write;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.slf4j.LoggerFactory;

import java.io.File;

@SuppressWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class WriteByLog implements Write {

    private Logger logger;

    private final int appenderIdx;
    private static int cnt = 0;

    public WriteByLog(File outputFile, boolean append) {
        appenderIdx = cnt++;

        init(outputFile.getPath(), append);
    }

    private void init(String outputFile, boolean append) {
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        fileAppender.stop();

        PatternLayout pl = new PatternLayout();
        pl.setPattern("%m");
        pl.setContext(lc);
        pl.start();

        LayoutWrappingEncoder<ILoggingEvent> patternEncoder = new LayoutWrappingEncoder<>();
        patternEncoder.setContext(lc);
        patternEncoder.setLayout(pl);

        fileAppender.setEncoder(patternEncoder);
        fileAppender.setContext(lc);
        fileAppender.setFile(outputFile);
        fileAppender.setAppend(append);
        fileAppender.start();

        logger = (Logger) LoggerFactory.getLogger("FileOutput" + appenderIdx);

        // don't call ancestor logger
        logger.setAdditive(false);
        logger.addAppender(fileAppender);
    }

    @Override
    public void close() {
        this.logger.detachAndStopAllAppenders();
    }

    @Override
    public void write(String content) {
        this.logger.info(content);
    }
}
