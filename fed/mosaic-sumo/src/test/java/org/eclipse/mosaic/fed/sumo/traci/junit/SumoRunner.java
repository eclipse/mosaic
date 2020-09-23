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

package org.eclipse.mosaic.fed.sumo.traci.junit;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;

/**
 * SUMO Traci-Tests sometimes fail without any reason (probably due to blocked ports). This
 * runner retries a test at most three times and only fails if all three attempts have failed.
 * Also annotated tests are skipped if SUMO is not installed or Maven is called with {@code -DskipSumoTests}.
 */
public class SumoRunner extends BlockJUnit4ClassRunner {

    private static final int MAX_ATTEMPTS = 3;

    private int failedAttempts = 0;

    public SumoRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    public void run(final RunNotifier notifier) {
        EachTestNotifier testNotifier = new EachTestNotifier(notifier, getDescription());
        Statement statement = classBlock(notifier);

        try {
            // skip if called with `mvn test -DskipSumoTests` for faster builds
            Assume.assumeTrue("Skipping SUMO Test", System.getProperty("skipSumoTests") == null);
            Assume.assumeTrue("SUMO is not installed. Skipping.", isSumoInstalled());

            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            testNotifier.fireTestIgnored();
        } catch (StoppedByUserException e) {
            throw e;
        } catch (Throwable e) {
            retry(testNotifier, statement, e);
        }
    }

    private boolean isSumoInstalled() throws InterruptedException {
        String executable = "sumo";
        String sumoHome = System.getenv("SUMO_HOME");
        if (StringUtils.isNotBlank(sumoHome)) {
            executable = sumoHome + File.separator + "bin" + File.separator + executable;
        }

        try {
            return Runtime.getRuntime().exec(executable).waitFor() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            runTestUnit(methodBlock(method), description, notifier);
        }
    }

    /**
     * Runs a {@link Statement} that represents a leaf (aka atomic) test.
     */
    private void runTestUnit(Statement statement, Description description,
                             RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            retry(eachNotifier, statement, e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }

    private void retry(EachTestNotifier notifier, Statement statement, Throwable currentThrowable) {
        Throwable caughtThrowable = currentThrowable;
        while (MAX_ATTEMPTS > failedAttempts) {
            try {
                statement.evaluate();
                return;
            } catch (AssumptionViolatedException e) {
                notifier.addFailedAssumption(e);
                return;
            } catch (Throwable t) {
                failedAttempts++;
                caughtThrowable = t;
            }
        }
        notifier.addFailure(caughtThrowable);
    }
}
