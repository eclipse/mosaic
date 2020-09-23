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

package org.eclipse.mosaic.lib.util.junit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used to copy test resources from the resource-directory to a temporary folder.
 * For example if you have a file in <code>src/test/resources/path/to/myfile.txt</code>, you can access this
 * file easily in your test with:
 *
 * <pre>{@code
 *  class MyTest {
 *
 *      @Rule
 *      public TestFileRule rule = new TestFileRule()
 *          .with("myfile.txt", "/path/to/myfile.txt");
 *
 *      @Test
 *      public void test() {
 *          File myFile = rule.get("myfile.txt");
 *          // ...
 *      }
 *  }
 * }</pre>
 */
public class TestFileRule extends ExternalResource {

    private final TemporaryFolder temporaryFolder;
    private final boolean ownTemporaryFolder;

    private final Map<String, Resource> resources = new HashMap<>();
    private String basedir = null;

    public TestFileRule() {
        this.temporaryFolder = new TemporaryFolder();
        this.ownTemporaryFolder = true;
    }

    public TestFileRule(TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
        this.ownTemporaryFolder = false;
    }

    public TestFileRule basedir(String basedir) {
        this.basedir = basedir;
        return this;
    }

    public TestFileRule with(String resource) {
        resources.put(StringUtils.substringAfterLast(resource, "/"), new Resource(resource, null));
        return this;
    }

    public TestFileRule with(String targetFile, String resource) {
        resources.put(targetFile, new Resource(resource, null));
        return this;
    }

    public File get(String fileName) {
        return Validate.notNull(resources.get(fileName), "Could not find file " + fileName).copyOfResource;
    }

    @Override
    protected void before() throws Throwable {
        if (ownTemporaryFolder) {
            temporaryFolder.create();
        }

        File base;
        if (basedir == null) {
            base = temporaryFolder.getRoot();
        } else {
            base = temporaryFolder.newFolder(basedir);
        }

        for (Map.Entry<String, Resource> resource : resources.entrySet()) {
            File newFile = new File(base, resource.getKey());
            Validate.isTrue(newFile.createNewFile(), "Could not create temporary file");
            resource.getValue().copyOfResource = newFile;

            try (OutputStream out = new FileOutputStream(resource.getValue().copyOfResource);
                 InputStream in = getClass().getResourceAsStream(resource.getValue().resource)) {

                Validate.notNull(in, "Could not find resource: " + resource.getValue().resource);

                IOUtils.copy(in, out);
            }
        }
    }

    public File getRoot() {
        return temporaryFolder.getRoot();
    }

    @Override
    protected void after() {
        if (ownTemporaryFolder) {
            temporaryFolder.delete();
        }
    }

    private static class Resource {
        String resource;
        File copyOfResource;

        Resource(String resource, File copyOfResource) {
            this.resource = resource;
            this.copyOfResource = copyOfResource;
        }
    }
}
