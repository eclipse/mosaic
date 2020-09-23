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

package org.eclipse.mosaic.fed.sumo.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

/**
 * This class creates a new image which contains a given
 * background image and centered text on it.
 */
class TrafficSignImageCreator {

    private final static Logger LOG = LoggerFactory.getLogger(TrafficSignImageCreator.class);
    public static final String SIGN_FONT = "Helvetica";

    private final Path sumoWorkingDir;
    private final BufferedImage backgroundImage;
    private final Color textColor;
    private final int minPadding;

    private TrafficSignImageCreator(Path sumoWorkingDir, String backgroundImageFileName, Color textColor, int minPadding) throws IOException {
        this.sumoWorkingDir = sumoWorkingDir;
        this.backgroundImage = ImageIO.read(Validate.notNull(
                getClass().getResourceAsStream("/trafficsigns/" + backgroundImageFileName),
                "Could not find image file " + backgroundImageFileName)
        );
        this.textColor = textColor;
        this.minPadding = minPadding;
    }

    static TrafficSignImageCreator forLaneAssignments(Path sumoWorkingDir) throws IOException {
        return new TrafficSignImageCreator(sumoWorkingDir, "lane-assignment-background.png", Color.WHITE, 50);
    }

    static TrafficSignImageCreator forSpeedLimits(Path sumoWorkingDir) throws IOException {
        return new TrafficSignImageCreator(sumoWorkingDir, "speed-sign-background.png", Color.BLACK, 130);
    }

    public final Path getOrCreateImage(String text, @Nullable Integer lane) {
        String laneSuffix = lane != null ? "-lane" + lane : "";
        Path imageFilesDir = sumoWorkingDir.resolve("trafficsigns");
        Path imageFilePath = imageFilesDir.resolve(StringUtils.defaultIfEmpty(text, "EMPTY") + laneSuffix + ".png");
        if (Files.isReadable(imageFilePath)) {
            // Return cached image file
            return imageFilePath;
        }

        try {
            Files.createDirectories(imageFilesDir);
        } catch (IOException e) {
            LOG.warn("Could not create directories", e);
        }

        try {
            // Create new image
            BufferedImage resultImage = new BufferedImage(
                    backgroundImage.getWidth(),
                    backgroundImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            // Add background imageC
            Graphics g = resultImage.getGraphics();
            g.drawImage(backgroundImage, 0, 0, null);

            // Text
            if (!StringUtils.isEmpty(text)) {
                g.setColor(textColor);
                int maxWidth = backgroundImage.getWidth() - 2 * minPadding;
                Dimension textSize = makeTextFitDimension(g, new Dimension(maxWidth, maxWidth), text, true);
                int left = (backgroundImage.getWidth() - textSize.width) / 2;
                int top = backgroundImage.getHeight() / 2 + textSize.height / 2;
                g.drawString(text, left, top);
            }

            // Lane index box
            if (lane != null) {
                // Draw border box
                int width = 170;
                int left = backgroundImage.getWidth() - width;
                int top = backgroundImage.getHeight() - width;
                g.setColor(Color.BLACK);
                g.fillRect(left, top, width, width);

                // Draw inner box
                int borderWidth = 10;
                int innerWidth = width - 2 * borderWidth;
                g.setColor(Color.WHITE);
                g.fillRect(left + borderWidth, top + borderWidth, innerWidth, innerWidth);

                // Add text (lane index)
                text = "" + lane;
                int maxWidth = width - 6 * borderWidth;
                Dimension textSize = makeTextFitDimension(g, new Dimension(maxWidth, maxWidth), text, false);
                left += (innerWidth - textSize.width) / 2;
                top += borderWidth + innerWidth / 2 + textSize.height / 2;
                g.setColor(Color.BLACK);
                g.drawString(text, left, top);
            }

            // Save as new image
            g.dispose();
            ImageIO.write(resultImage, "PNG", imageFilePath.toFile());
            return imageFilePath;
        } catch (IOException e) {
            LOG.error("Could not create image file for traffic sign in " + imageFilePath.toAbsolutePath(), e);
            return imageFilePath;
        }
    }

    private Dimension makeTextFitDimension(Graphics graphics, Dimension maxDimension, String text, boolean isBold) {
        int fontSize = 300;
        graphics.setFont(new Font(SIGN_FONT, isBold ? Font.BOLD : Font.PLAIN, fontSize));
        Dimension dimension = getDimension(graphics, text);
        while (dimension.width > maxDimension.width || dimension.height > maxDimension.height) {
            fontSize -= 5;
            graphics.setFont(new Font(SIGN_FONT, isBold ? Font.BOLD : Font.PLAIN, fontSize));
            dimension = getDimension(graphics, text);
        }
        return dimension;
    }

    private Dimension getDimension(Graphics graphics, String text) {
        TextLayout textLayout = new TextLayout(text, graphics.getFont(), graphics.getFontMetrics().getFontRenderContext());
        return new Dimension((int) textLayout.getBounds().getWidth(), (int) textLayout.getBounds().getHeight());
    }
}
