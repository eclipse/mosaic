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

import org.eclipse.mosaic.fed.mapping.ambassador.VehicleFlowGenerator;
import org.eclipse.mosaic.lib.math.RandomNumberGenerator;

import org.apache.commons.lang3.Validate;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Helper class allowing the random and pseudo-random selection of multiple
 * objects. The distribution can be defined by weights that do <b>not</b> have to sum up to 1.
 *
 * @param <T> The type of the object to be returned
 */
public class DeterministicSelector<T extends Weighted> implements WeightedSelector<T> {

    private final RandomNumberGenerator randomNumberGenerator;
    private List<Item<T>> items;

    private static class Item<T extends Weighted> implements Weighted {
        // The actual object to be returned after selection
        private T object;
        // The probability of selecting this item
        private double normalizedWeight;
        // The number of times this item has been selected
        private int selections;

        // Set when item is generated the first time. Used as tie-break when two or more objects could be generated.
        private int priority;

        @Override
        public double getWeight() {
            return normalizedWeight;
        }
    }

    /**
     * Number of objects already generated.
     */
    private int totalSelections = 0;

    /**
     * Number of objects already generated without counting the same object twice
     */
    private int totalDistinctSelections = 0;

    /**
     * Constructor for {@link DeterministicSelector}.
     *
     * @param objects       list of all types that might be selected from
     * @param randGenerator random number generator to select the first item
     */
    public DeterministicSelector(List<T> objects, @Nonnull RandomNumberGenerator randGenerator) {
        Validate.notNull(objects, "Illegal constructor call for WeightedSelector: objects is null.");
        Validate.isTrue(!objects.isEmpty(), "Illegal constructor call for WeightedSelector: objects is empty!");

        // init with base values
        this.items = new ArrayList<>(objects.size());
        this.randomNumberGenerator = randGenerator;

        // map all objects to an item so they can be weighted
        this.items = objects.stream().map((o) -> {
            Item<T> item = new Item<>();
            item.object = o;
            item.selections = 0;
            item.priority = objects.size();
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * This method actually initializes the Selector, called
     * on first selection (first call of {@link #nextItem()}.
     *
     * @return the selector deciding which element is selected first
     */
    private StochasticSelector<Item<T>> init() {
        // initialize weights with base values
        double sumWeights = items.stream().mapToDouble((item) -> item.object.getWeight()).sum();
        // set normalized weights, if no weights have been set, apply same weight to all items
        items.forEach((item) -> item.normalizedWeight = sumWeights > 0 ? (item.object.getWeight() / sumWeights) : (1d / items.size()));

        items = items.stream().filter(item -> item.getWeight() > 0).collect(Collectors.toList());

        return new StochasticSelector<>(items, randomNumberGenerator);
    }

    /**
     * Request the next item, if no item has been selected yet the weights will be
     * initialized and the first item will be randomly selected. Otherwise, the next
     * item will be determined depending on its weight and how often it has been selected.
     *
     * @return the selected object of the item
     */
    @Override
    public T nextItem() {

        Item<T> selectedItem;

        if (totalSelections == 0) {
            StochasticSelector<Item<T>> firstItemSelector = init();
            selectedItem = firstItemSelector.nextItem();
        } else {
            Comparator<Item<T>> compareBySelectionToWeightRatio = Comparator.comparingDouble((item) -> (item.selections / (double) totalSelections) - item.getWeight());
            Comparator<Item<T>> compareByPriority = Comparator.comparingInt((item) -> item.priority);
            // choose item which has been selected the least according to its weight
            selectedItem = items.stream()
                    .min(compareBySelectionToWeightRatio.thenComparing(compareByPriority))
                    .orElse(null);
        }

        if (selectedItem == null) {
            return null;
        }

        // if this was an object we did not see before, set its priority (priority is used as tie-breaker)
        if (selectedItem.priority > this.totalDistinctSelections) {
            selectedItem.priority = this.totalDistinctSelections;
            this.totalDistinctSelections++;
        }

        selectedItem.selections++;
        totalSelections++;
        return selectedItem.object;
    }
}
