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

package org.eclipse.mosaic.lib.enums;

/**
 * Channel numbers based on IEEE 1604 WAVE standard for US spectrum allocation.
 * <p>
 * 5.9 GHz Band (802.11p)
 * Channel Name   |SCH1|SCH2|SCH3|CCH |SCH4|SCH5|SCH6|
 * ---------------------------------------------------
 * Channel Number |  0 |  1 |  2 |  3 |  4 |  5 |  6 |
 * ---------------------------------------------------
 * Center GHz     |5.86|5.87|5.88|5.89|5.90|5.91|5.92|
 * </p>
 */
public enum AdHocChannel {
    SCH1(0),
    SCH2(1),
    SCH3(2),
    CCH(3),
    SCH4(4),
    SCH5(5),
    SCH6(6);

    int channelNr;

    AdHocChannel(int channelNr) {
        this.channelNr = channelNr;
    }

    public int getChannelNr() {
        return this.channelNr;
    }

}