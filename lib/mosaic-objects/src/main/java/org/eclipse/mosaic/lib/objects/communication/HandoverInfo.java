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

package org.eclipse.mosaic.lib.objects.communication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class HandoverInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nodeId;
    private final String previousRegion;
    private final String currentRegion;

    public String getNodeId() {
        return nodeId;
    }

    public String getCurrentRegion() {
        return currentRegion;
    }

    public String getPreviousRegion() {
        return previousRegion;
    }

    public HandoverInfo(String nodeId, String currentRegion, String previousRegion) {
        this.nodeId = nodeId;
        this.currentRegion = currentRegion;
        this.previousRegion = previousRegion;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .append(nodeId)
                .append(currentRegion)
                .append(previousRegion)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        HandoverInfo other = (HandoverInfo) obj;
        return new EqualsBuilder()
                .append(this.getNodeId(), other.getNodeId())
                .append(this.getCurrentRegion(), other.getCurrentRegion())
                .append(this.getPreviousRegion(), other.getPreviousRegion())
                .isEquals();
    }

    @Override
    public String toString() {
        return "HandoverInfo{" + "nodeId=" + nodeId + ", currentRegion=" + currentRegion + ", previousRegion=" + previousRegion + "}";
    }
}

