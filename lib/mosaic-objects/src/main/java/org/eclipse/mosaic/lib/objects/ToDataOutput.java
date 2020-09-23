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

package org.eclipse.mosaic.lib.objects;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Write this object to a {@link DataOutput}.
 */
public interface ToDataOutput {

    /**
     * Write this object to a {@link DataOutput}.
     *
     * @param dataOutput the {@link DataOutput}.
     */
    void toDataOutput(DataOutput dataOutput) throws IOException;
}
