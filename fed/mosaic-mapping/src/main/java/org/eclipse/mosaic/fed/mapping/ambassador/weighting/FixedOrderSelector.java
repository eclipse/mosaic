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

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class allowing the random and pseudo-random selection of multiple
 * objects. The distribution can be defined by weights that do <b>not</b> have to sum up to 1.
 *
 * @param <T> The type of the object to be returned
 */
public class FixedOrderSelector<T extends Weighted> implements WeightedSelector<T> {
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
     * Number of objects already generated without counting the same object twice.
     */
    private int totalDistinctSelections = 0;

    /**
     * Constructor for {@link FixedOrderSelector}.
     *
     * @param objects       list of all types that might be selected from
     */
    public FixedOrderSelector(List<T> objects) {
        Validate.notNull(objects, "Illegal constructor call for WeightedSelector: objects is null.");
        Validate.isTrue(!objects.isEmpty(), "Illegal constructor call for WeightedSelector: objects is empty!");

        // init with base values
        this.items = new ArrayList<>(objects.size());

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
            items = normalizeWeights(items);
            selectedItem = selectFirstItem();
        } else {
            selectedItem = selectNextItem();
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

    /**
     * Normalizes the weights of all items to sum 1.
     */
    private List<Item<T>> normalizeWeights(List<Item<T>> items) {
        // initialize weights with base values
        double sumWeights = items.stream().mapToDouble((item) -> item.object.getWeight()).sum();
        // set normalized weights, if no weights have been set, apply same weight to all items
        items.forEach((item) -> item.normalizedWeight = sumWeights > 0 ? (item.object.getWeight() / sumWeights) : (1d / items.size()));

        return items.stream().filter(item -> item.getWeight() > 0).collect(Collectors.toList());
    }

    private Item<T> selectFirstItem() {
        Comparator<Item<T>> compareByWeight =Comparator.comparingDouble(Item::getWeight);
        Comparator<Item<T>> compareByPriority = Comparator.comparingInt((item) -> item.priority);
        // choose first item according to weight. if two or items have the same weight, the first item in the list will be chosen
        return items.stream().max(compareByWeight.thenComparing(compareByPriority)).orElse(null);
    }

    private Item<T> selectNextItem() {
        Comparator<Item<T>> compareBySelectionToWeightRatio =
                Comparator.comparingDouble((item) -> (item.selections / (double) totalSelections) - item.getWeight());
        Comparator<Item<T>> compareByPriority = Comparator.comparingInt((item) -> item.priority);
        // choose item which has been selected the least according to its weight
        return items.stream()
                .min(compareBySelectionToWeightRatio.thenComparing(compareByPriority))
                .orElse(null);
    }
}
