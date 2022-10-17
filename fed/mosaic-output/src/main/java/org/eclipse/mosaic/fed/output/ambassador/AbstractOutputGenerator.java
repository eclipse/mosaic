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

package org.eclipse.mosaic.fed.output.ambassador;

import org.eclipse.mosaic.lib.util.InteractionUtils;
import org.eclipse.mosaic.rti.api.Interaction;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class needs to be implemented to add a output generator or visualizer to the{@link OutputAmbassador}.
 * The {@link AbstractOutputGenerator} holds a registry which redirects subclasses of {@link Interaction}
 * to a registered consumer method by its type id (i.e. {@link Interaction#getTypeId()}. All
 * interactions which can not be redirected to such method, due to missing registration, is passed
 * to {@link #handleUnregisteredInteraction(Interaction)}.
 * <br/><br/>
 * Each output generator can register specific methods, which are called whenever an interaction with a suitable
 * type id is passed to this output generator. This can be achieved, by adding new methods with exactly one
 * parameter to the sub class implementation, and annotate this method with @{@link Handle}:
 * <pre>
 * &#64;Handle
 * public void handleVehicleUpdates(VehicleUpdates interaction) {
 *    ...
 * }
 * </pre>
 * This works for all subclasses of message, which have a public static member TYPE_ID. If this
 * member is not present (e.g. for custom messages), the annotation can be extended accordingly:
 * <pre>
 *  &#64;Handle("MyInteraction")
 *  public void handleMyInteraction(MyInteraction interaction) {
 *     ...
 *  }
 *  </pre>
 * As a third method, the {@link #registerInteractionForOutputGeneration(String, java.util.function.Consumer)}  method can be used in the constructor.
 * <pre>
 *  MyOutputGenerator() {
 *      super();
 *      registerInteractionForOutputGeneration("MyInteraction", this::handleMyInteraction);
 *  }
 *
 *  public void handleMyInteraction(MyInteraction interaction) {
 *     ...
 *  }
 *  </pre>
 */
@SuppressWarnings
public class AbstractOutputGenerator {

    protected final static Logger log = LoggerFactory.getLogger(AbstractOutputGenerator.class);

    private final Map<String, Consumer<Interaction>> interactionRegistry = new HashMap<>();

    protected AbstractOutputGenerator() {
        readHandleMethods();
    }

    protected <M extends Interaction> void registerInteractionForOutputGeneration(String type, Consumer<M> handleMethod) {
        interactionRegistry.put(type, (Consumer<Interaction>) handleMethod);
    }

    public final void handleInteraction(Interaction interaction) {
        if (interaction == null) {
            return;
        }

        interactionRegistry
                .getOrDefault(interaction.getTypeId(), this::handleUnregisteredInteraction)
                .accept(interaction);
    }

    public void handleUnregisteredInteraction(Interaction interaction) {
        log.debug("No handle method for interaction of type {}", interaction.getTypeId());
    }

    private void readHandleMethods() {
        for (Method m : this.getClass().getMethods()) {
            if (!m.isAnnotationPresent(Handle.class)) {
                continue;
            }
            Class<?>[] parameterTypes = m.getParameterTypes();
            if (parameterTypes.length != 1 || !Interaction.class.isAssignableFrom(parameterTypes[0])) {
                log.warn("Unsuitable types or number of parameters in method '{}'. Expecting one parameter of type {}", m.getName(), Interaction.class.getName());
                continue;
            }

            String annotatedTypeId = m.getAnnotation(Handle.class).value();
            if (StringUtils.isBlank(annotatedTypeId)) {
                Optional<String> declaredTypeId = InteractionUtils.extractTypeId(parameterTypes[0]);
                if (declaredTypeId.isPresent()) {
                    annotatedTypeId = declaredTypeId.get();
                } else {
                    log.warn("No interaction type identifier specified for handle method '{}'", m.getName());
                    continue;
                }
            }

            registerInteractionForOutputGeneration(annotatedTypeId, (Interaction interaction) -> {
                try {
                    m.invoke(AbstractOutputGenerator.this, interaction);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.warn("Could not invoke handle method for method " + interaction.getTypeId(), e.getCause());
                }
            });
        }
    }

    public void finish() {
        //nop
    }
}
