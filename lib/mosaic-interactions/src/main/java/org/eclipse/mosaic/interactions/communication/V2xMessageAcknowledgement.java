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

package org.eclipse.mosaic.interactions.communication;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.eclipse.mosaic.lib.enums.NegativeAckReason;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * This extension of {@link Interaction} is used by a communication simulator to inform about success or failure
 * of a packet transmission. Typically, the application simulator should subscribe to this interaction.
 */
public class V2xMessageAcknowledgement extends Interaction {

    private static final long serialVersionUID = 1L;

    /**
     * String identifying the type of this interaction.
     */
    public final static String TYPE_ID = createTypeIdentifier(V2xMessageAcknowledgement.class);

    /**
     * The initially sent V2XMessage.
     */
    private final int messageId;

    private final String sourceName;

    /**
     * The additional information about transmission problems for the sender.
     * Usually, only negative acknowledgements have reasons (otherwise everything was ok).
     */
    private final List<NegativeAckReason> reasons = new ArrayList<>();

    public V2xMessageAcknowledgement(long time, int messageId, String sourceName) {
        super(time);
        this.messageId = messageId;
        this.sourceName = sourceName;
    }

    public V2xMessageAcknowledgement(long time, V2xMessage originatingMessage) {
        this(time, originatingMessage.getId(), originatingMessage.getRouting().getSource().getSourceName());
    }

    public V2xMessageAcknowledgement(long time, V2xMessage originatingMessage, List<NegativeAckReason> reasons) {
        this(time, originatingMessage);
        this.reasons.addAll(reasons);
    }

    public boolean isAcknowledged() {
        return reasons.isEmpty();
    }

    public List<NegativeAckReason> getNegativeReasons() {
        return reasons;
    }

    public int getOriginatingMessageId() {
        return messageId;
    }

    @Nonnull
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5, 79)
                .append(sourceName)
                .append(messageId)
                .append(reasons)
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

        V2xMessageAcknowledgement other = (V2xMessageAcknowledgement) obj;
        return new EqualsBuilder()
                .append(this.messageId, other.messageId)
                .append(this.sourceName, other.sourceName)
                .append(this.reasons, other.reasons)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append("messageId", messageId)
                .append("sourceName", sourceName)
                .append("reasons", reasons)
                .toString();
    }
}
