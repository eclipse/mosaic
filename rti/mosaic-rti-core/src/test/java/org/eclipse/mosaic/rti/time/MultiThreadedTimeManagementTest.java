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

import org.eclipse.mosaic.rti.MosaicComponentParameters;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.rti.api.ComponentProvider;
import org.eclipse.mosaic.rti.api.TimeManagement;

public class MultiThreadedTimeManagementTest extends SequentialTimeManagementTest {

    @Override
    protected TimeManagement createTimeManagement(ComponentProvider componentProvider) {
        return new MultiThreadedTimeManagement(componentProvider, new MosaicComponentParameters().setNumberOfThreads(2).setEndTime(20 * TIME.SECOND));
    }
}