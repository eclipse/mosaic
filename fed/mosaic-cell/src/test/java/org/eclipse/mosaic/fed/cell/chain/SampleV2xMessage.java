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

package org.eclipse.mosaic.fed.cell.chain;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.rti.DATA;

import javax.annotation.Nonnull;

public class SampleV2xMessage extends V2xMessage {

    private static final long serialVersionUID = 1L;
    
    private EncodedPayload payload;

    public SampleV2xMessage(MessageRouting routing, long messageLengthInBits) {
        super(routing);
        this.payload = new EncodedPayload((int)(messageLengthInBits / DATA.BYTE));
    }

    @Nonnull
    @Override
    public EncodedPayload getPayload() {
        return payload;
    }
}
