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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.eclipse.mosaic.fed.application.app.api.OperatingSystemAccess;
import org.eclipse.mosaic.fed.application.app.api.os.OperatingSystem;
import org.eclipse.mosaic.rti.TIME;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.io.File;
import java.util.Arrays;

/**
 * Implementation of the {@link UnitLogger} interface
 * <p></p>
 * http://logback.qos.ch/manual/mdc.html set the unitId for the discriminator
 * (Mapped Diagnostic Context)
 */
public class UnitLoggerImpl implements UnitLogger {

    /**
     * Name of the root logger. Hint:
     * <code>&lt;logger name="ApplicationLogDelegate" additivity="false" level="INFO"&gt;</code>
     */
    private final static String ROOT_LOGGER = "ApplicationLogDelegate";

    private static final String SIMTIME_LOG_POSTFIX = " (at simulation time {})";

    /**
     * The reference to the root logger.
     */
    private final Logger log;

    /**
     * Path for the log file.
     */
    private final String mdcPath;

    /**
     * Create a new unit logger based on an id.
     *
     * @param unitId   the id of the unit.
     * @param loggerId the id of the logger.
     */
    public UnitLoggerImpl(final String unitId, final String loggerId) {
        this.log = LoggerFactory.getLogger(ROOT_LOGGER);
        this.mdcPath = unitId + File.separator + loggerId;
    }

    @Override
    public String getName() {
        return log.getName();
    }

    private void redirect(Runnable logCall) {
        String beforeMdcPath =  MDC.get("path");
        try {
            MDC.put("path", mdcPath);
            logCall.run();
        } finally {
            MDC.put("path", beforeMdcPath);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return log.isTraceEnabled(marker);
    }

    @Override
    public void trace(String msg) {
        redirect(() -> log.trace(msg));
    }

    @Override
    public void trace(String format, Object arg) {
        redirect(() -> log.trace(format, arg));
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        redirect(() -> log.trace(format, arg1, arg2));
    }

    @Override
    public void trace(String format, Object... arguments) {
        redirect(() -> log.trace(format, arguments));
    }

    @Override
    public void trace(String msg, Throwable t) {
        redirect(() -> log.trace(msg, t));
    }

    @Override
    public void trace(Marker marker, String msg) {
        redirect(() -> log.trace(marker, msg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        redirect(() -> log.trace(marker, format, arg));
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        redirect(() -> log.trace(marker, format, arg1, arg2));
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        redirect(() -> log.trace(marker, format, argArray));
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        redirect(() -> log.trace(marker, msg, t));
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return log.isDebugEnabled(marker);
    }

    @Override
    public void debug(String msg) {
        redirect(() -> log.debug(msg));
    }

    @Override
    public void debug(String format, Object arg) {
        redirect(() -> log.debug(format, arg));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        redirect(() -> log.debug(format, arg1, arg2));
    }

    @Override
    public void debug(String format, Object... arguments) {
        redirect(() -> log.debug(format, arguments));
    }

    @Override
    public void debug(String msg, Throwable t) {
        redirect(() -> log.debug(msg, t));
    }

    @Override
    public void debug(Marker marker, String msg) {
        redirect(() -> log.debug(marker, msg));
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        redirect(() -> log.debug(marker, format, arg));
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        redirect(() -> log.debug(marker, format, arg1, arg2));
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        redirect(() -> log.debug(marker, format, argArray));
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        redirect(() -> log.debug(marker, msg, t));
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return log.isInfoEnabled(marker);
    }

    @Override
    public void info(String msg) {
        redirect(() -> log.info(msg));
    }

    @Override
    public void info(String format, Object arg) {
        redirect(() -> log.info(format, arg));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        redirect(() -> log.info(format, arg1, arg2));
    }

    @Override
    public void info(String format, Object... arguments) {
        redirect(() -> log.info(format, arguments));
    }

    @Override
    public void info(String msg, Throwable t) {
        redirect(() -> log.info(msg, t));
    }

    @Override
    public void info(Marker marker, String msg) {
        redirect(() -> log.info(marker, msg));
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        redirect(() -> log.info(marker, format, arg));
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        redirect(() -> log.info(marker, format, arg1, arg2));
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        redirect(() -> log.info(marker, format, argArray));
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        redirect(() -> log.info(marker, msg, t));
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return log.isWarnEnabled(marker);
    }

    @Override
    public void warn(String msg) {
        redirect(() -> log.warn(msg));
    }

    @Override
    public void warn(String format, Object arg) {
        redirect(() -> log.warn(format, arg));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        redirect(() -> log.warn(format, arg1, arg2));
    }

    @Override
    public void warn(String format, Object... arguments) {
        redirect(() -> log.warn(format, arguments));
    }

    @Override
    public void warn(String msg, Throwable t) {
        redirect(() -> log.warn(msg, t));
    }

    @Override
    public void warn(Marker marker, String msg) {
        redirect(() -> log.warn(marker, msg));
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        redirect(() -> log.warn(marker, format, arg));
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        redirect(() -> log.warn(marker, format, arg1, arg2));
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        redirect(() -> log.warn(marker, format, argArray));
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        redirect(() -> log.warn(marker, msg, t));
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return log.isErrorEnabled(marker);
    }

    @Override
    public void error(String msg) {
        redirect(() -> log.error(msg));
    }

    @Override
    public void error(String format, Object arg) {
        redirect(() -> log.error(format, arg));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        redirect(() -> log.error(format, arg1, arg2));
    }

    @Override
    public void error(String format, Object... arguments) {
        redirect(() -> log.error(format, arguments));
    }

    @Override
    public void error(String msg, Throwable t) {
        redirect(() -> log.error(msg, t));
    }

    @Override
    public void error(Marker marker, String msg) {
        redirect(() -> log.error(marker, msg));
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        redirect(() -> log.error(marker, format, arg));
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        redirect(() -> log.error(marker, format, arg1, arg2));
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        redirect(() -> log.error(marker, format, argArray));
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        redirect(() -> log.error(marker, msg, t));
    }

    @Override
    public void infoSimTime(OperatingSystemAccess<? extends OperatingSystem> osAccess, String format, Object... arguments) {
        if (isInfoEnabled()) {
            info(format + SIMTIME_LOG_POSTFIX, extendArgumentsWithTime(osAccess.getOperatingSystem().getSimulationTime(), arguments));
        }
    }

    @Override
    public void debugSimTime(OperatingSystemAccess<? extends OperatingSystem> osAccess, String format, Object... arguments) {
        if (isDebugEnabled()) {
            debug(format + SIMTIME_LOG_POSTFIX, extendArgumentsWithTime(osAccess.getOperatingSystem().getSimulationTime(), arguments));
        }
    }

    @Override
    public void warnSimTime(OperatingSystemAccess<? extends OperatingSystem> osAccess, String format, Object... arguments) {
        if (isWarnEnabled()) {
            warn(format + SIMTIME_LOG_POSTFIX, extendArgumentsWithTime(osAccess.getOperatingSystem().getSimulationTime(), arguments));
        }
    }

    private Object[] extendArgumentsWithTime(long simulationTime, Object... arguments) {
        final Object[] argumentsWithTime = Arrays.copyOf(arguments, arguments.length + 1);
        argumentsWithTime[arguments.length] = TIME.format(simulationTime);
        return argumentsWithTime;
    }
}
