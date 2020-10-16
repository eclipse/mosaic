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

package org.eclipse.mosaic.fed.cell.message;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base message for internal communication of results between the chained cell modules.
 * It is extended, depending on the specific module.
 */
public class CellModuleMessage {

    private final String emittingModule;

    private final String nextModule;

    /**
     * startTime is the time when the module received the message.
     * (or the event that contains the message as resource)
     */
    private final long startTime;

    private final long endTime;

    private final Object resource;

    /**
     * Creates a new {@link CellModuleMessage}.
     * In order to simplify the object creation, a helper object will be used.
     *
     * @param builder Helper object to create a {@link CellModuleMessage}.
     */
    private CellModuleMessage(Builder builder) {
        this.emittingModule = builder.emittingModule;
        this.nextModule = builder.nextModule;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.resource = builder.resource;
    }

    /**
     * A helper class in order to simplify the CellModuleMessage creation.
     */
    public static class Builder {
        private final String emittingModule;
        private final String nextModule;
        private long startTime;
        private long endTime;
        private Object resource = null;

        public Builder(String emittingModule, String nextModule) {
            this.emittingModule = emittingModule;
            this.nextModule = nextModule;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder resource(Object resource) {
            this.resource = resource;
            return this;
        }

        public CellModuleMessage build() {
            return new CellModuleMessage(this);
        }
    }

    public String getEmittingModule() {
        return emittingModule;
    }

    public String getNextModule() {
        return nextModule;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @SuppressWarnings("unchecked")
    public <T> T getResource() throws IllegalStateException {
        try {
            return (T) resource;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Wrong resource class", e);
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 89)
                .append(emittingModule)
                .append(nextModule)
                .append(startTime)
                .append(endTime)
                .append(resource)
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
        if (getClass() != obj.getClass()) {
            return false;
        }

        CellModuleMessage other = (CellModuleMessage) obj;
        return new EqualsBuilder()
                .append(this.emittingModule, other.emittingModule)
                .append(this.nextModule, other.nextModule)
                .append(this.startTime, other.startTime)
                .append(this.endTime, other.endTime)
                .append(this.resource, other.resource)
                .isEquals();
    }
}
