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

package org.eclipse.mosaic.fed.output.generator.file;

import org.eclipse.mosaic.fed.output.ambassador.AbstractOutputGenerator;
import org.eclipse.mosaic.fed.output.ambassador.Handle;
import org.eclipse.mosaic.fed.output.generator.file.format.ExtendedMethodSet;
import org.eclipse.mosaic.fed.output.generator.file.format.InteractionFormatter;
import org.eclipse.mosaic.fed.output.generator.file.write.Write;
import org.eclipse.mosaic.interactions.communication.V2xMessageRemoval;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.rti.api.Interaction;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FileOutput extends AbstractOutputGenerator {

    private final static Logger log = LoggerFactory.getLogger(FileOutput.class);

    /**
     * Writer is used to write the visualization result into the file.
     */
    private final Write writer;

    /**
     * To get message in a specified format.
     */
    private final InteractionFormatter interactionFormatter;

    /**
     * Construct FileVisualizer with a writer and a message formatter.
     *
     * @param writer               the writer visualizes the formatted message into file
     * @param interactionFormatter the message formatter transfers the messages in a format defined in the configuration file
     */
    public FileOutput(Write writer, InteractionFormatter interactionFormatter) {
        this.interactionFormatter = interactionFormatter;
        this.writer = writer;
    }

    @Handle
    public void visualizeInteraction(V2xMessageTransmission interaction) {
        ExtendedMethodSet.putV2xMessage(interaction.getMessage());
        visualize(interaction);
    }

    @Handle
    public void visualizeInteraction(V2xMessageRemoval interaction) {
        interaction.getRemovedMessageIds().forEach(ExtendedMethodSet::deleteV2xMessage);
        visualize(interaction);
    }

    @Override
    public void handleUnregisteredInteraction(Interaction interaction) {
        visualize(interaction);
    }

    private void visualize(Interaction interaction) {
        try {
            String content = this.interactionFormatter.format(interaction);
            if (StringUtils.isNotBlank(content)) {
                this.writer.write(content);
            }
        } catch (Exception e) {
            log.warn("Could not visualize message '{}'", interaction.getTypeId());
            log.debug("", e);
        }
    }

    @Override
    public void finish() {
        try {
            this.writer.close();
        } catch (IOException e) {
            log.error("Could not close file visualizer");
        }
    }
}
