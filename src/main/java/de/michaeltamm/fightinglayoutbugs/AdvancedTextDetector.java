/*
 * Copyright 2009 Michael Tamm
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

package de.michaeltamm.fightinglayoutbugs;

import static java.lang.Math.abs;

/**
 * Detects text pixels by comparing several screenshots
 * of a web page whereby all text is either colored
 * black, dark gray, light gray, or white via JavaScript -
 * runs significantly slower than the {@link SimpleTextDetector},
 * but is able to detect and ignore animation on the web page
 * (e.g. a animated GIF image, a Flash movie or a JavaScript
 * animation).
 *
 * @author Michael Tamm
 */
public class AdvancedTextDetector implements TextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) {
        int[][] screenshot = webPage.getScreenshot();
        int w = screenshot.length;
        int h = screenshot[0].length;
        boolean[][] animatedPixels = new boolean[w][h];
        int[][] screenshotWithBlackText = null;
        int[][] screenshotWithDarkGrayText = null;
        int[][] screenshotWithLightGrayText = null;
        int[][] screenshotWithWhiteText = null;
        webPage.injectJQueryIfNotPresent();
        webPage.backupTextColors();
        try {
            long t0 = System.currentTimeMillis();
            screenshotWithBlackText = takeScreenshotWithTextColor(webPage, "#000000");
            Visualization.algorithmStepFinished("1.) Take first screenshot with all text colored black", screenshotWithBlackText);
            waitAtLeastUntil(t0 + 250);
            screenshotWithDarkGrayText = takeScreenshotWithTextColor(webPage, "#555555");
            Visualization.algorithmStepFinished("2.) Take second screenshot with all text colored dark gray", screenshotWithDarkGrayText);
            waitAtLeastUntil(t0 + 500);
            screenshotWithLightGrayText = takeScreenshotWithTextColor(webPage, "#AAAAAA");
            Visualization.algorithmStepFinished("3.) Take third screenshot with all text colored light gray", screenshotWithLightGrayText);
            waitAtLeastUntil(t0 + 750);
            screenshotWithWhiteText = takeScreenshotWithTextColor(webPage, "#FFFFFF");
            Visualization.algorithmStepFinished("4.) Take fourth screenshot with all text colored white", screenshotWithWhiteText);
            // Try to find animated pixels ...
            update(animatedPixels).byComparing(screenshotWithLightGrayText, takeScreenshotWithTextColor(webPage, "#AAAAAA"));
            Visualization.algorithmStepFinished("5.) Detect animated pixels by taking another screenshot with all text colored light gray and comparing it with the one previously taken with the same text color", animatedPixels);
            update(animatedPixels).byComparing(screenshotWithDarkGrayText, takeScreenshotWithTextColor(webPage, "#555555"));
            Visualization.algorithmStepFinished("6.) Update animated pixels by taking another screenshot with all text colored dark gray and comparing it with the one previously taken with the same text color", animatedPixels);
            update(animatedPixels).byComparing(screenshotWithBlackText, takeScreenshotWithTextColor(webPage, "#000000"));
            Visualization.algorithmStepFinished("7.) Update animated pixels by taking another screenshot with all text colored black and comparing it with the one previously taken with the same text color", animatedPixels);
        } finally {
            webPage.restoreTextColors();
        }
        update(animatedPixels).byComparing(screenshot, webPage.takeScreenshot());
        Visualization.algorithmStepFinished("8.) Update animated pixels by taking another screenshot with text colored restored and comparing it with the first screenshot taken", animatedPixels);
        boolean[][] result = new boolean[w][h];
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (!animatedPixels[x][y] && screenshotWithBlackText[x][y] != screenshotWithWhiteText[x][y]) {
                    Distance d1 = new Distance(screenshotWithBlackText[x][y], screenshotWithDarkGrayText[x][y]);
                    Distance d2 = new Distance(screenshotWithDarkGrayText[x][y], screenshotWithLightGrayText[x][y]);
                    if (d1.roughlyEquals(d2)) {
                        Distance d3 = new Distance(screenshotWithLightGrayText[x][y], screenshotWithWhiteText[x][y]);
                        if (d2.roughlyEquals(d3)) {
                            result[x][y] = true;
                        }
                    }
                }
            }
        }
        Visualization.algorithmFinished("9.) Detect text pixels based on screenshot series with different text colors and ignoring animated pixels", result);
        return result;
    }

    private void waitAtLeastUntil(long t) {
        long now = System.currentTimeMillis();
        if (now < t) {
            try {
                Thread.sleep(t - now);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Got interrupted.", e);
            }
        }
    }

    private int[][] takeScreenshotWithTextColor(WebPage webPage, String textColor) {
        webPage.executeJavaScript("jQuery('*').css('color', '" + textColor + "');");
        return webPage.takeScreenshot();
    }

    private AnimatedPixelsUpdater update(boolean[][] animatedPixels) {
        return new AnimatedPixelsUpdater(animatedPixels);
    }

    private static class AnimatedPixelsUpdater {
        private final boolean[][] _animatedPixels;

        private AnimatedPixelsUpdater(boolean[][] animatedPixels) {
            _animatedPixels = animatedPixels;
        }

        private void byComparing(int[][] screenshot1, int[][] screenshot2) {
            int w = _animatedPixels.length;
            int h = _animatedPixels[0].length;
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (screenshot1[x][y] != screenshot2[x][y]) {
                        _animatedPixels[x][y] = true;
                    }
                }
            }
        }
    }

    private static class Distance {
        private final int dr;
        private final int dg;
        private final int db;

        private Distance(int pixel1, int pixel2) {
            int r1 = (pixel1 & 0xFF0000) >> 16;
            int g1 = (pixel1 & 0xFF00) >> 8;
            int b1 = (pixel1 & 0xFF);
            int r2 = (pixel2 & 0xFF0000) >> 16;
            int g2 = (pixel2 & 0xFF00) >> 8;
            int b2 = (pixel2 & 0xFF);
            dr = r2 - r1;
            dg = g2 - g1;
            db = b2 - b1;
        }

        private boolean roughlyEquals(Distance other) {
            return (abs(this.dr - other.dr) <= 1) && (abs(this.dg - other.dg) <= 1) && (abs(this.db - other.db) <= 1);
        }
    }
}
