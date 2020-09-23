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

package org.eclipse.mosaic.lib.spatial;

import org.eclipse.mosaic.lib.math.MathUtils;
import org.eclipse.mosaic.lib.math.Vector3d;
import org.eclipse.mosaic.lib.math.VectorUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class LineString<T extends Vector3d> extends ArrayList<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient Vector3d tmpVec = new Vector3d();

    public interface Walker<T> {
        void walk(T point, double pos);
    }

    public static class Vecs extends LineString<Vector3d> {
        public Vecs() { }

        public Vecs(List<? extends Vector3d> copyFrom) {
            super(copyFrom);
        }

        public Vecs(Stream<? extends Vector3d> vecs) {
            super(vecs);
        }

        @Override
        protected Vector3d newPoint() {
            return new Vector3d();
        }
    }

    public LineString() { }

    public LineString(List<? extends T> copyFrom) {
        super(copyFrom);
    }

    public LineString(Stream<? extends T> points) {
        points.forEach(this::addPoint);
    }

    protected abstract T newPoint();

    public T first() {
        return get(0);
    }

    public T last() {
        return get(size() - 1);
    }

    public boolean isSingular() {
        return size() <= 1;
    }

    private void checkNotSingular() {
        if (isSingular()) {
            throw new IllegalStateException("Path is singular (points.size = " + size() + " )");
        }
    }

    public void addPoint(T point) {
        add(point);
    }

    public void addPoint(T pt, double minDist) {
        if (size() == 0) {
            add(pt);
        } else if (pt.distanceTo(last()) > minDist) {
            add(pt);
        }
    }

    public void addFront(T point) {
        add(0, point);
    }

    public void addLineString(LineString<? extends T> other) {
        addAll(other);
    }

    public void joinLineStrings(LineString<? extends T> other) {
        if (this.size() > 0 && other.size() > 0 && last() != null && last().isFuzzyEqual(other.first())) {
            remove(size() - 1);
        }
        addLineString(other);
    }

    public void addLineStringReversed(LineString<? extends T> other) {
        for (int i = other.size() - 1; i >= 0; i--) {
            addPoint(other.get(i));
        }
    }

    public LineString<? super T> getReversed(LineString<? super T> result) {
        result.clear();
        result.addLineStringReversed(this);
        return result;
    }

    public double getLength() {
        double len = 0;
        for (int i = 1; i < size(); i++) {
            len += get(i).distanceTo(get(i - 1));
        }
        return len;
    }

    /**
     * Moves the first point along {@link #getStartDirection(Vector3d)} by the given distance.
     * @param len Distance to move the first point by.
     */
    public void extendStart(double len) {
        first().add(getStartDirection(tmpVec).multiply(-len));
    }

    /**
     * Moves the last point along {@link #getEndDirection(Vector3d)} by the given distance.
     * @param len Distance to move the last point by.
     */
    public void extendEnd(double len) {
        last().add(getEndDirection(tmpVec).multiply(len));
    }

    public Vector3d getStartDirection(Vector3d result) {
        checkNotSingular();
        return result.set(get(1)).subtract(get(0)).norm();
    }

    public Vector3d getEndDirection(Vector3d result) {
        checkNotSingular();
        return result.set(get(size() - 1)).subtract(get(size() - 2)).norm();
    }

    public Vector3d getDirectionAt(Vector3d queryPt, Vector3d result) {
        int idx = getLowerIndex(queryPt);
        return result.set(get(idx + 1)).subtract(get(idx)).norm();
    }

    public Vector3d getOrthoDirectionAt(Vector3d queryPt, Vector3d result) {
        return getDirectionAt(queryPt, result).rotate(Math.PI / 2, 0, -1, 0);
    }

    public Vector3d getClosestPointOnPath(Vector3d queryPt, Vector3d result) {
        int idx = getLowerIndex(queryPt);
        return new Edge<>(get(idx), get(idx + 1)).getNearestPointOnEdge(queryPt, result);
    }

    public double getDistance(Vector3d pt) {
        return pt.distanceTo(getClosestPointOnPath(pt, tmpVec));
    }

    public double getPosition(Vector3d queryPt) {
        int idx = getLowerIndex(queryPt);
        new Edge<>(get(idx), get(idx + 1)).getNearestPointOnEdge(queryPt, tmpVec);

        double dist = 0;
        for (int i = 1; i <= idx; i++) {
            dist += get(i).distanceTo(get(i - 1));
        }
        dist += get(idx).distanceTo(tmpVec);
        return dist;
    }

    public Vector3d getPointAtPosition(double position, Vector3d result) {
        if (isSingular()) {
            return first();
        }
        checkNotSingular();

        if (position < 0) {
            return result.set(first());
        }
        result.set(last());

        for (int i = 0; i < size() - 1; i++) {
            double l = get(i).distanceTo(get(i + 1));
            if (l < position) {
                position -= l;
            } else {
                get(i + 1).subtract(get(i), result).norm();
                result.multiply(position).add(get(i));
                break;
            }
        }
        return result;
    }

    public <R extends LineString<T>> R translateLateral(double translation, R result) {
        return translateLateral(p -> translation, result);
    }

    public <R extends LineString<T>> R translateLateral(Function<Double, Double> translationFun, R result) {
        checkNotSingular();
        result.clear();

        double len = getLength();
        double pos = 0;

        Vector3d ortho = new Vector3d(get(1)).subtract(get(0)).norm().multiply(translationFun.apply(0.0));
        ortho.rotate(Math.PI / 2, 0, -1, 0);
        for (int i = 0; i < size() - 1; i++) {
            if (i > 0) {
                pos += get(i).distanceTo(get(i - 1));
                ortho.set(get(i + 1)).subtract(get(i - 1)).norm().multiply(translationFun.apply(pos / len));
                ortho.rotate(Math.PI / 2, 0, -1, 0);
            }
            T point = newPoint();
            point.set(get(i)).add(ortho);
            result.addPoint(point);
        }
        ortho.set(get(size() - 1)).subtract(get(size() - 2)).norm().multiply(translationFun.apply(1.0));
        ortho.rotate(Math.PI / 2, 0, -1, 0);
        T point = newPoint();
        point.set(get(size() - 1)).add(ortho);
        result.addPoint(point);
        return result;
    }

    public <R extends LineString<T>> R translateLateralByPoints(Function<T, Double> translationFun, R result) {
        checkNotSingular();
        result.clear();

        Vector3d ortho = new Vector3d(get(1)).subtract(get(0)).norm().multiply(translationFun.apply(first()));
        ortho.rotate(Math.PI / 2, 0, -1, 0);
        for (int i = 0; i < size() - 1; i++) {
            if (i > 0) {
                ortho.set(get(i + 1)).subtract(get(i - 1)).norm().multiply(translationFun.apply(get(i)));
                ortho.rotate(Math.PI / 2, 0, -1, 0);
            }
            T point = newPoint();
            point.set(get(i)).add(ortho);
            result.addPoint(point);
        }
        ortho.set(get(size() - 1)).subtract(get(size() - 2)).norm().multiply(translationFun.apply(last()));
        ortho.rotate(Math.PI / 2, 0, -1, 0);
        T point = newPoint();
        point.set(get(size() - 1)).add(ortho);
        result.addPoint(point);
        return result;
    }

    public <R extends LineString<T>> R getSubPath(double startPos, double endPos, R result) {
        checkNotSingular();
        result.clear();
        if ((startPos - endPos) == 0) {
            return result;
        }
        T start = newPoint();
        T end = newPoint();
        getPointAtPosition(startPos, start);
        getPointAtPosition(endPos, end);

        result.addPoint(start);
        if (startPos > endPos) {
            // reverse order
            double pos = getLength();
            for (int i = size() - 2; i >= 0; i--) {
                pos -= get(i).distanceTo(get(i + 1));
                if (pos < endPos) {
                    if (!result.containsFuzzy(end)) {
                        result.addPoint(end);
                    }
                    break;
                } else if (pos < startPos) {
                    result.addPoint(get(i));
                }
            }
        } else {
            // normal order
            double pos = 0;
            for (int i = 1; i < size(); i++) {
                pos += get(i).distanceTo(get(i - 1));
                if (pos > endPos) {
                    if (!result.containsFuzzy(end)) {
                        result.addPoint(end);
                    }
                    break;
                } else if (pos > startPos) {
                    result.addPoint(get(i));
                }
            }
        }
        return result;
    }

    public boolean isFuzzyEqual(LineString<?> other) {
        if (size() != other.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).isFuzzyEqual(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean containsFuzzy(Vector3d pt) {
        for (int i = 0; i < size(); i++) {
            if (get(i).isFuzzyEqual(pt)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIntersectingXZ(LineString<?> other) {
        return getIntersectionPointXZ(other, tmpVec);
    }

    public boolean getIntersectionPointXZ(LineString<?> other, Vector3d result) {
        for (int i = 0; i < size() - 1; i++) {
            for (int j = 0; j < other.size() - 1; j++) {
                if (VectorUtils.computeXZEdgeIntersectionPoint(get(i), get(i + 1),
                        other.get(j), other.get(j + 1), result)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getLowerIndex(double position) {
        checkNotSingular();
        for (int i = 0; i < size() - 1; i++) {
            position -= get(i).distanceTo(get(i + 1));
            if (position < 0) {
                return i;
            }
        }
        return size() - 1;
    }

    public int getLowerIndex(Vector3d queryPt) {
        checkNotSingular();
        Edge<T> ed = new Edge<>(get(0), get(1));

        double minD = Double.MAX_VALUE;
        int minI = 0;
        for (int i = 0; i < size() - 1; i++) {
            ed.a = get(i);
            ed.b = get(i + 1);
            ed.getNearestPointOnEdge(queryPt, tmpVec);
            double d = queryPt.distanceTo(tmpVec);
            if (d < minD) {
                minD = d;
                minI = i;
                if (MathUtils.isFuzzyZero(d)) {
                    // that's as close as we can get
                    break;
                }
            }
        }
        return minI;
    }

    public T getLowerPoint(Vector3d queryPt) {
        return get(getLowerIndex(queryPt));
    }

    public void walk(Walker<T> walker) {
        double pos = 0;
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                pos += get(i).distanceTo(get(i - 1));
            }
            walker.walk(get(i), pos);
        }
    }
}