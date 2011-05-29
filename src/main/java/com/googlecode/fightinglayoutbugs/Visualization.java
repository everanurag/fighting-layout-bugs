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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Visualization {

    public interface Listener {
        void algorithmStepFinished(String algorithm, String stepDescription, int[][] tempResult);
        void algorithmFinished(String algorithm, String stepDescription, int[][] result);
    }

    private static final ThreadLocal<List<Listener>> LISTENERS = new ThreadLocal<List<Listener>>() {
        @Override
        protected List<Listener> initialValue() {
            return new ArrayList<Listener>();
        }
    };

    /**
     * Registers a listener for the current thread.
     */
    public static void registerListener(Listener listener) {
        LISTENERS.get().add(listener);
    }

    /**
     * Unregisters the given listener for the current thread.
     */
    public static void unregisterListener(Listener listener) {
        Iterator<Listener> i = LISTENERS.get().iterator();
        while (i.hasNext()) {
            if (i.next() == listener) {
                i.remove();
            }
        }
    }

    /**
     * Calls {@link Listener#algorithmStepFinished Listener.algorithmStepFinished(...)}
     * for all registered listeners for the current thread.
     */
    public static void algorithmStepFinished(String stepDescription, WebPage webPage, Object tempResult) {
        List<Listener> listeners = LISTENERS.get();
        if (!listeners.isEmpty()) {
            String algorithm = determineNameOfCurrentAlgorithm();
            int[][] pixels = resultToPixels(webPage, tempResult);
            for (Listener listener : listeners) {
                listener.algorithmStepFinished(algorithm, stepDescription, pixels);
            }
        }
    }

    /**
     * Calls {@link Listener#algorithmFinished Listener.algorithmFinished(...)}
     * for all registered listeners for the current thread.
     */
    public static void algorithmFinished(String stepDescription, WebPage webPage, Object result) {
        List<Listener> listeners = LISTENERS.get();
        if (!listeners.isEmpty()) {
            String algorithm = determineNameOfCurrentAlgorithm();
            int[][] pixels = resultToPixels(webPage, result);
            for (Listener listener : listeners) {
                listener.algorithmFinished(algorithm, stepDescription, pixels);
            }
        }
    }

    /**
     * Returns the name of the algorithm which called {@link #algorithmStepFinished algorithmStepFinished(...)}
     * or {@link #algorithmStepFinished algorithmFinished(...)}.
     */
    private static String determineNameOfCurrentAlgorithm() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int i = 0;
        while (!stackTrace[i].getClassName().equals(Visualization.class.getName())) { ++i; }
        while (stackTrace[i].getClassName().equals(Visualization.class.getName())) { ++i; }
        final String className = stackTrace[i].getClassName();
        while (i < stackTrace.length - 1 && stackTrace[i + 1].getClassName().equals(className)) { ++i; }
        return className.substring(className.lastIndexOf('.') + 1) + "." + stackTrace[i].getMethodName();
    }

    /**
     * Converts the object passed as second parameter to either {@link Listener#algorithmStepFinished Listener.algorithmStepFinished(...)}
     * or {@link Listener#algorithmFinished Listener.algorithmFinished(...)} into a PNG image.
     */
    private static int[][] resultToPixels(WebPage webPage, Object result) {
        if ((result instanceof CompareScreenshots) || (result instanceof boolean[][])) {
            boolean[][] b = (result instanceof CompareScreenshots ? ((CompareScreenshots) result).differentPixels : (boolean[][]) result);
            int w1 = b.length;
            int h1 = b[0].length;
            Screenshot screenshot = webPage.getScreenshot();
            int w2 = screenshot.width;
            int h2 = screenshot.height;
            int[][] pixels = ImageHelper.copyOf(screenshot.pixels);
            int[][] alphaMask = new int[w2][h2];
            for (int x = 0; x < w2; ++x) {
                for (int y = 0; y < h2; ++y) {
                    alphaMask[x][y] = 0x33FFFFFF;
                }
            }
            ImageHelper.blend(pixels, alphaMask);
            for (int x = 0; x < w2; ++x) {
                for (int y = 0; y < h2; ++y) {
                    if (x < w1 && y < h1 && b[x][y]) {
                        pixels[x][y] = 0;
                    }
                }
            }
            return pixels;
        } else if ((result instanceof Screenshot) || (result instanceof int[][])) {
            return (result instanceof Screenshot ? ((Screenshot) result).pixels : (int[][]) result);
        } else if (result instanceof Collection) {
            Screenshot screenshot = webPage.getScreenshot();
            int w = screenshot.width;
            int h = screenshot.height;
            int[][] pixels = ImageHelper.copyOf(screenshot.pixels);
            int[][] alphaMask = new int[w][h];
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    alphaMask[x][y] = 0x33FFFFFF;
                }
            }
            ImageHelper.blend(pixels, alphaMask);
            for (RectangularRegion r : (Collection<RectangularRegion>) result) {
                for (int x = r.x1; x <= Math.min(w, r.x2); ++x) {
                    for (int y = r.y1; y < Math.min(h, r.y2); ++y) {
                        pixels[x][y] = 0;
                    }
                }
            }
            return pixels;
        } else {
            throw new RuntimeException("Don't know how to convert an instance of " + result.getClass().getName() + " into an image.");
        }
    }

    private Visualization() {}
}
