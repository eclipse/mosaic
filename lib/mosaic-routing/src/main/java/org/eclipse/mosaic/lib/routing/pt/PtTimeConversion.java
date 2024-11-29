/*
 * Copyright (c) 2024 Fraunhofer FOKUS and others. All rights reserved.
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

package org.eclipse.mosaic.lib.routing.pt;

import org.eclipse.mosaic.rti.TIME;

import java.time.LocalTime;
import java.util.Date;

/**
 * Contains functions to calculate between different time formats, since "time" is usually always referring to
 * simulation time, but a real-world time is needed for the calculation of public transportation routes
 */
public final class PtTimeConversion {

    private static final LocalTime realWorldTime = LocalTime.of(0, 0, 0);
    private static final long realWorldTimeNanoS =
            TIME.HOUR * realWorldTime.getHour() +
                    TIME.MINUTE * realWorldTime.getMinute() +
                    TIME.SECOND * realWorldTime.getSecond();

    private PtTimeConversion() {
        //just need functions to calculate bewtween time formats
    }

    public static LocalTime toLocalTime(long simTime) {
        return LocalTime.ofNanoOfDay(simTime + realWorldTimeNanoS);
    }

    public static long toSimTime(LocalTime time) {
        int hourDiff = time.getHour() - realWorldTime.getHour();
        int minuteDiff = time.getMinute() - realWorldTime.getMinute();
        int secondDiff = time.getSecond() - realWorldTime.getSecond();

        long simTime = TIME.HOUR * hourDiff + TIME.MINUTE * minuteDiff + TIME.SECOND * secondDiff;

        if (simTime < 0) {
            System.out.println("Warning, calculated simtime is before sim actually starts");
        }

        return simTime;
    }

    public static long toSimTime(Date date) {
        return toSimTime(dateToLocalTime(date));
    }

    public static LocalTime dateToLocalTime(Date date) {
        if (date == null) {
            return null;
        }
        String[] splitString = date.toString().split(" ");
        String[] timeStrings = splitString[3].split(":");
        return LocalTime.of(Integer.parseInt(timeStrings[0]), Integer.parseInt(timeStrings[1]), Integer.parseInt(timeStrings[2]));
    }

}
