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

package org.eclipse.mosaic.fed.mapping.ambassador.weighting;

import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Helper class allowing the random and pseudo-random selection of multiple
 * objects. The distribution can be defined by weights.
 *
 * @param <T> The type of the object to be returned
 */
public class StochasticSelector<T extends Weighted> implements WeightedSelector<T> {
    private final List<T> objects;
    private final RandomNumberGenerator randGenerator;

    private List<Double> normalizedWeights;

    /**
     * Constructor for {@link StochasticSelector}.
     *
     * @param objects       a list of objects to select from
     * @param randGenerator the {@link RandomNumberGenerator} for the selector
     */
    public StochasticSelector(List<T> objects, @Nonnull RandomNumberGenerator randGenerator) {
        Validate.notNull(objects, "Illegal constructor call for WeightedSelector: objects is null.");
        Validate.isTrue(!objects.isEmpty(), "Illegal constructor call for WeightedSelector: objects is empty!");

        this.randGenerator = randGenerator;
        this.objects = new ArrayList<>(objects);
    }

    /**
     * This method actually initializes the Selector.
     */
    private void init() {
        // Init with base values...
        this.normalizedWeights = new ArrayList<>(objects.size());
        double sumWeights = objects.stream().mapToDouble(Weighted::getWeight).sum();

        // ...and add all weights
        for (T object : objects) {
            if (sumWeights == 0) {
                // If all weights are zero then we assume all weights to be equal
                this.normalizedWeights.add(1.);
            } else {
                this.normalizedWeights.add(object.getWeight() / sumWeights);
            }
        }
    }

    /**
     * Request the next item.
     *
     * @return A random- or pseudo-randomly selected item from the list
     */
    @Override
    public T nextItem() {
        if (normalizedWeights == null) {
            init();
        }
        double number = this.randGenerator.nextDouble();
        // this will more often select objects with larger weights, but still gives lower weight objects a chance
        int i = 0;
        for (double weight : normalizedWeights) {
            number -= weight;
            if (number < 0) {
                return objects.get(i);
            }
            i++;
        }
        throw new RuntimeException("WeightedSelector.nextItem(): left with " + number + " after all subtractions!");
    }
}
