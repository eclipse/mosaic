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

package org. eclipse.mosaic.fed.output.generator.file.write;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class WriteByFile implements Write {

    private final OutputStreamWriter out;

    public WriteByFile(File file, boolean append) throws FileNotFoundException {
        this(new FileOutputStream(file, append));
    }

    public WriteByFile(OutputStream outputStream) {
        out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        out.flush();
        out.close();
    }

    @Override
    public void write(String content) throws IOException {
        out.write(content);
    }
}
