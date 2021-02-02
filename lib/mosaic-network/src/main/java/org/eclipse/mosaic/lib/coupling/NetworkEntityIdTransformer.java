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

package org.eclipse.mosaic.lib.coupling;

import org.eclipse.mosaic.lib.util.objects.IdTransformer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkEntityIdTransformer implements IdTransformer<Integer, String> {

    private final static Logger log = LoggerFactory.getLogger(NetworkEntityIdTransformer.class);
    private BiMap<String, Integer> idMap = HashBiMap.create();
    private AtomicInteger nextId = new AtomicInteger();

    boolean containsInternalId(String internalId) {
        return idMap.containsKey(internalId);
    }

    boolean containsExternalId(Integer externalId) {
        return idMap.inverse().containsKey(externalId);
    }

    Integer removeUsingInternalId(String internalId) {
        if (containsInternalId(internalId)) {
            log.debug("Removing internal id {} with corresponding external id {} from id-map", internalId, toExternalId(internalId));
        } else {
            log.debug("Cannot remove {}, internal id doesn't exist", internalId);
        }
        return idMap.remove(internalId);
    }

    /**
     * Returns the corresponding external ID of the federate for a given internal ID.
     * If that mapping doesn't exist, create a new mapping with the next external ID.
     *
     * @param nodeId an internal ID of type String
     * @return the corresponding external ID of the federate
     */
    @Override
    public Integer toExternalId(String nodeId) {
        Integer externalId = idMap.get(nodeId);
        if (externalId == null) {
            int id = nextId.getAndIncrement();
            idMap.put(nodeId, id);
            log.debug("Assigned internal id {} to external id {}", nodeId, id);
        }
        return idMap.get(nodeId);
    }

    /**
     * Returns the corresponding internal ID for a given external ID.
     * If that mapping doesn't exist throw an Exception.
     *
     * @param externalId an external ID of the federate
     * @return the corresponding internal ID
     * @throws IllegalStateException if mapping doesn't exist
     */
    @Override
    public String fromExternalId(Integer externalId) throws IllegalStateException {
        String nodeId = idMap.inverse().get(externalId);
        if (nodeId == null) {
            throw new IllegalStateException(String.format("No element with the external ID %s", externalId));
        }
        return nodeId;
    }

    @Override
    public void reset() {
        idMap.clear();
        nextId.set(0);
    }
}
