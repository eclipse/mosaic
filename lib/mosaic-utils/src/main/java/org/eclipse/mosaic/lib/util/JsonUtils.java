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

package org.eclipse.mosaic.lib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonUtils {

    private JsonUtils() {
        // static methods only
    }

    /**
     * Removes user custom comments from a JSON string.
     *
     * @return the JSON string without any comments
     */
    public static String removeComments(String jsonString) {
        final String tokenizer = "\"|(/\\*)|(\\*/)|(//)|\\n|\\r";
        final String magic = "(\\\\)*$";
        final Pattern pattern = Pattern.compile(tokenizer);
        final Pattern magicPattern = Pattern.compile(magic);

        final Matcher matcher = pattern.matcher(jsonString);
        if (!matcher.find()) {
            return jsonString;
        } else {
            matcher.reset();
        }

        final List<String> newLines = new ArrayList<>();

        boolean inString = false;
        boolean inMultilineComment = false;
        boolean inSinglelineComment = false;
        boolean foundMagic = false;

        String tmp;
        String tmp2;
        int from = 0;
        String lc;
        String rc = "";
        while (matcher.find()) {
            lc = jsonString.substring(0, matcher.start());
            rc = jsonString.substring(matcher.end(), jsonString.length());
            tmp = jsonString.substring(matcher.start(), matcher.end());

            if (!inMultilineComment && !inSinglelineComment) {
                tmp2 = lc.substring(from);
                if (!inString) {
                    tmp2 = tmp2.replaceAll("(\\n|\\r|\\s)*", "");
                }

                newLines.add(tmp2);
            }
            from = matcher.end();

            if (tmp.charAt(0) == '\"' && !inMultilineComment && !inSinglelineComment) {
                final Matcher magicMatcher = magicPattern.matcher(lc);
                foundMagic = magicMatcher.find();
                if (!inString || !foundMagic || (magicMatcher.end() - magicMatcher.start()) % 2 == 0) {
                    inString = !inString;
                }
                from--;
                rc = jsonString.substring(from);
            } else if (tmp.startsWith("/*") && !inString && !inMultilineComment && !inSinglelineComment) {
                inMultilineComment = true;
            } else if (tmp.startsWith("*/") && !inString && inMultilineComment) {
                inMultilineComment = false;
            } else if (tmp.startsWith("//") && !inString && !inMultilineComment && !inSinglelineComment) {
                inSinglelineComment = true;
            } else if ((tmp.startsWith("\n") || tmp.startsWith("\r")) && !inString && !inMultilineComment && inSinglelineComment) {
                inSinglelineComment = false;
            } else if (!inMultilineComment && !inSinglelineComment && !tmp.substring(0, 1).matches("\\n|\\r|\\s")) {
                newLines.add(tmp);
            }
        }

        newLines.add(rc);
        return newLines.stream().collect(Collectors.joining());
    }

}
