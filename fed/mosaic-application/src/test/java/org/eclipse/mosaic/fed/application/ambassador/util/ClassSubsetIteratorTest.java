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

package org.eclipse.mosaic.fed.application.ambassador.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ClassSubsetIteratorTest {
    
    private List<A> testList;
    
    @Before
    public void setup() {
        testList = new LinkedList<>();
        testList.add(new A());
        testList.add(new B());
        testList.add(new B());
        testList.add(new A());
        testList.add(new C());
        testList.add(new B());
        testList.add(new C());
        testList.add(new D());
        testList.add(new A());
    }
    
    /**
     * Iterates only over substitutes of class A, which is true for every object in testlist.
     */
    @Test
    public void iterateOverA_returnObjectsOfAllTypes() {
        Iterator<A> iteratorOverA = new ClassSubsetIterator<>(testList.iterator(), A.class);

        String result = StringUtils.join(iteratorOverA, ",");

        Assert.assertEquals("A,B,B,A,C,B,C,D,A", result);
    }
    
    /**
     * Iterates only over substitutes of class B, which is only true for objects of type B and C.
     */
    @Test
    public void iterateOverB_returnObjectsOfTypesBC() {
        Iterator<B> iteratorOverB = new ClassSubsetIterator<>(testList.iterator(), B.class);
        
        String result = StringUtils.join(iteratorOverB, ",");
        
        Assert.assertEquals("B,B,C,B,C", result);
    }
    
    /**
     * Iterates only over substitutes of class C, which is only true for objects of type C itself.
     */
    @Test
    public void iterateOverC_returnObjectsOfTypeC() {
        Iterator<C> iteratorOverC = new ClassSubsetIterator<>(testList.iterator(), C.class); 
        
        String result = StringUtils.join(iteratorOverC, ",");
        
        Assert.assertEquals("C,C", result);
    }
    
    /**
     * Iterates only over substitutes of class D, which is only true for objects of type D itself.
     */
    @Test
    public void iterateOverD_returnObjectsOfTypeD() {
        Iterator<D> iteratorOverD = new ClassSubsetIterator<>(testList.iterator(), D.class); 
        
        String result = StringUtils.join(iteratorOverD, ",");
        
        Assert.assertEquals("D", result);
    }
        

    private static class A {
        public String toString() {
            return "A";
        }
    }
    
    private static class B extends A {
        public String toString() {
            return "B";
        }
    }
    
    private static class C extends B {
        public String toString() {
            return "C";
        }
    }
    
    private static class D extends A {
        public String toString() {
            return "D";
        }
    }

}
