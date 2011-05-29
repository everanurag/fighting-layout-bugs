/*
 * Copyright 2009-2011 Michael Tamm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.fightinglayoutbugs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class DebugHelper implements Visualization.Listener {

    private static final Log LOG = LogFactory.getLog(DebugHelper.class);

    public static class AlgorithmStep {
        public final String algorithm;
        public final String stepDescription;
        public final File screenshot;

        public AlgorithmStep(String algorithm, String stepDescription, File screenshot) {
            this.algorithm = algorithm;
            this.stepDescription = stepDescription;
            this.screenshot = screenshot;
        }

        @Override
        public String toString() {
            return algorithm + " " + stepDescription + " -- " + screenshot.getAbsolutePath();
        }
    }

    private static ThreadLocal<DebugHelper> INSTANCES = new ThreadLocal<DebugHelper>();

    public static void start() {
        DebugHelper instance = INSTANCES.get();
        if (instance != null) {
            throw new IllegalStateException("DebugHelper already started in current thread.");
        }
        instance = new DebugHelper();
        Visualization.registerListener(instance);
        INSTANCES.set(instance);
    }

    public static List<AlgorithmStep> stop() {
        DebugHelper instance = INSTANCES.get();
        if (instance == null) {
            throw new IllegalStateException("DebugHelper has not been started in current thread.");
        }
        Visualization.unregisterListener(instance);
        INSTANCES.remove();
        return instance.algorithmSteps;
    }

    private final File screenshotDir;
    private final List<AlgorithmStep> algorithmSteps;
    private final NumberFormat nf = new DecimalFormat("00");
    private int i;

    private DebugHelper() {
        try {
            screenshotDir = FileHelper.createTempDir();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        algorithmSteps = new ArrayList<AlgorithmStep>();
    }


    @Override
    public void algorithmStepFinished(String algorithm, String stepDescription, int[][] tempResult) {
        File screenshotFile = new File(screenshotDir, algorithm + "." + nf.format(++i) + ".png");
        ImageHelper.pixelsToPngFile(tempResult, screenshotFile);
        AlgorithmStep algorithmStep = new AlgorithmStep(algorithm, stepDescription, screenshotFile);
        LOG.debug(algorithmStep);
        algorithmSteps.add(algorithmStep);
    }

    @Override
    public void algorithmFinished(String algorithm, String stepDescription, int[][] result) {
        File screenshotFile = new File(screenshotDir, algorithm + "." + nf.format(++i) + ".png");
        ImageHelper.pixelsToPngFile(result, screenshotFile);
        AlgorithmStep algorithmStep = new AlgorithmStep(algorithm, stepDescription, screenshotFile);
        LOG.debug(algorithmStep);
        algorithmSteps.add(algorithmStep);
    }
}
