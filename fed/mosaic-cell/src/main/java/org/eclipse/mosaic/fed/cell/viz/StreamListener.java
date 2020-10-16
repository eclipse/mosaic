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

package org.eclipse.mosaic.fed.cell.viz;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public interface StreamListener {

    /**
     * This method allows to exchange messages between a sender and a receiver as stream
     * and it also includes properties(bandwidth, application class ) of the stream.
     *
     * @param sender     The sender of the message.
     * @param receiver   The receiver of the message.
     * @param properties Includes properties of the stream.
     */
    void messageSent(StreamParticipant sender, StreamParticipant receiver, StreamProperties properties);

    /**
     * The stream listener finishes the listening of the channel.
     */
    void finish();

    /**
     * Helper class which describes a stream participant includes :
     * - Region of the participant.
     * - Message time.
     */
    class StreamParticipant {

        private final String region;
        private final long messageTime;

        public StreamParticipant(String region, long messageTime) {
            this.region = region;
            this.messageTime = messageTime;
        }

        public String getRegion() {
            return region;
        }

        public long getMessageTime() {
            return messageTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StreamParticipant that = (StreamParticipant) o;
            return new EqualsBuilder()
                    .append(messageTime, that.messageTime)
                    .append(region, that.region)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(region)
                    .append(messageTime)
                    .toHashCode();
        }
    }

    /**
     * Helper class describes a stream that includes the properties :
     * - application class.
     * - used bandwidth.
     */
    class StreamProperties {

        private final String applicationClass;
        private final long bandwidth;

        public StreamProperties(String applicationClass, Long bandwidth) {
            this.applicationClass = applicationClass;
            this.bandwidth = bandwidth;
        }

        public String getApplicationClass() {
            return applicationClass;
        }

        public long getBandwidth() {
            return bandwidth;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            StreamProperties that = (StreamProperties) o;
            return new EqualsBuilder()
                    .append(bandwidth, that.bandwidth)
                    .append(applicationClass, that.applicationClass)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(applicationClass)
                    .append(bandwidth)
                    .toHashCode();
        }
    }
}
