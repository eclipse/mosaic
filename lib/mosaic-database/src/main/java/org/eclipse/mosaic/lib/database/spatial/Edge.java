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

package org.eclipse.mosaic.lib.database.spatial;

import org.eclipse.mosaic.lib.database.road.Connection;
import org.eclipse.mosaic.lib.database.road.Node;

import java.util.Objects;
import javax.annotation.Nonnull;

public class Edge {

    private final Connection connection;
    private final Node previousNode;
    private final Node nextNode;

    Edge(@Nonnull Connection connection, @Nonnull String id, @Nonnull Node from, @Nonnull Node to) {
        this.connection = Objects.requireNonNull(connection);
        this.previousNode = Objects.requireNonNull(from);
        this.nextNode = Objects.requireNonNull(to);
    }


    /**
     * The {@link Connection} this edge is a part from. To determine which part please have a look
     * at {@link #getPreviousNode()} and {@link #getNextNode()};
     *
     * @return Connection of the edge.
     */
    @Nonnull
    public Connection getConnection() {
        return connection;
    }

    /**
     * The {@link Node} the edge starts on the {@link Connection} (see {@link #getConnection()}).
     *
     * @return Node from the connection starts.
     */
    @Nonnull
    public Node getPreviousNode() {
        return previousNode;
    }

    /**
     * The {@link Node} the edge ends on the {@link Connection} (see {@link #getConnection()}).
     *
     * @return End node of the connection.
     */
    @Nonnull
    public Node getNextNode() {
        return nextNode;
    }

}
