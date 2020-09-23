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

package org.eclipse.mosaic.lib.junit;

import org.eclipse.mosaic.lib.objects.addressing.IpResolver;
import org.eclipse.mosaic.lib.util.junit.TestUtils;
import org.eclipse.mosaic.rti.config.CIpResolver;

import com.google.gson.Gson;
import org.junit.rules.ExternalResource;

public class IpResolverRule extends ExternalResource {

    private final String resolverConfiguration;

    public IpResolverRule() {
        resolverConfiguration = null;
    }

    public IpResolverRule(String resolverConfiguration) {
        this.resolverConfiguration = resolverConfiguration;
    }

    @Override
    protected void before() {
        final CIpResolver cIPResolver;
        if (resolverConfiguration != null) {
            cIPResolver = new Gson().fromJson(resolverConfiguration, CIpResolver.class);
        } else {
            cIPResolver = new CIpResolver();
        }
        IpResolver.setSingleton(new IpResolver(cIPResolver));
    }

    @Override
    protected void after() {
        TestUtils.setPrivateField(IpResolver.class, "singleton", null);
    }

}
