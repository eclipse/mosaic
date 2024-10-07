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

package org.eclipse.mosaic.fed.cell.utility;

import org.eclipse.mosaic.fed.cell.config.CRegion;
import org.eclipse.mosaic.fed.cell.config.model.CMobileNetworkProperties;
import org.eclipse.mosaic.fed.cell.config.model.CNetworkProperties;
import org.eclipse.mosaic.lib.geo.Area;
import org.eclipse.mosaic.lib.geo.Bounds;
import org.eclipse.mosaic.lib.geo.CartesianPoint;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.spatial.KdTree;
import org.eclipse.mosaic.lib.spatial.SpatialItemAdapter;
import org.eclipse.mosaic.lib.spatial.SpatialTree;
import org.eclipse.mosaic.lib.spatial.SpatialTreeTraverser.Nearest;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import java.util.Collection;

/**
 * Provides a spatial index for regions. This enables a fast lookup of all regions which
 * cover a given point (see {@link CRegion}.
 */
public class RegionsIndex {

    private final KdTree<MobileNetworkPropertiesWrapper> regionIndex;
    private final InArea inArea = new InArea();

    /**
     * Creates a new {@link RegionsIndex} object.
     *
     * @param regions Collection of the regions.
     */
    public RegionsIndex(final Collection<CMobileNetworkProperties> regions) {
        this.regionIndex = new KdTree<>(
                new SpatialItemAdapter.AreaAdapter<>(),
                regions.stream()
                        .filter(region -> region.getCapoArea() != null)
                        .map(MobileNetworkPropertiesWrapper::new)
                        .toList()
        );
    }

    public CNetworkProperties getRegion(CartesianPoint cartesianPoint) {

        inArea.setup(cartesianPoint);
        inArea.traverse(regionIndex);

        return inArea.getNearest() != null ? inArea.getNearest().areaRegion : null;
    }


    private static class MobileNetworkPropertiesWrapper implements Area<CartesianPoint> {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings(value = "SE_BAD_FIELD", justification = "That's fine, we do not serialize this class.")
        private final CMobileNetworkProperties areaRegion;

        private MobileNetworkPropertiesWrapper(CMobileNetworkProperties region) {
            this.areaRegion = region;
        }

        @Override
        public Bounds<CartesianPoint> getBounds() {
            return areaRegion.getCapoArea().getBounds();
        }

        @Override
        public double getArea() {
            return areaRegion.getCapoArea().getArea();
        }

        @Override
        public boolean contains(CartesianPoint cartesianPoint) {
            return areaRegion.getCapoArea().contains(cartesianPoint);
        }
    }

    private static class InArea extends Nearest<MobileNetworkPropertiesWrapper> {

        private CartesianPoint search;

        public void setup(CartesianPoint searchPoint) {
            setup(new Vector3d(searchPoint.getX(), searchPoint.getY(), 0));
            search = searchPoint;
        }

        @Override
        protected void traverseLeaf(SpatialTree<MobileNetworkPropertiesWrapper>.Node node,
                                    SpatialTree<MobileNetworkPropertiesWrapper> tree) {
            for (MobileNetworkPropertiesWrapper item : node.getItems()) {
                double squareDistance = getCenterDistanceSqr(item, tree);
                if (item.contains(search) && squareDistance <= distanceSqr) {
                    nearest = item;
                    distanceSqr = squareDistance;
                }
            }
        }
    }
}
