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

import static de.michaeltamm.fightinglayoutbugs.Screenshot.takenAtLeast;
import static de.michaeltamm.fightinglayoutbugs.Screenshot.withAllTextColored;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Works similar to the {@link SimpleTextDetector} but detects
 * and ignores animation (like animated GIF images, Flash movies
 * or JavaScript animation).
 *
 * @author Michael Tamm
 */
public class AnimationAwareTextDetector implements TextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) {
        // 1.) Take initial screenshot of web page ...
        Screenshot screenshot1 = webPage.getScreenshot();
        Visualization.algorithmStepFinished("1.) Take initial screenshot of web page.", screenshot1);
        // 2.) Detect Flash movies ...
        boolean[][] flashMoviePixels = webPage.getFlashMoviePixels();
        Visualization.algorithmStepFinished("2.) Detect Flash movies.", flashMoviePixels);
        // 3.) Take screenshot with all text colored black ...
        Screenshot screenshotWithAllTextColoredBlack = webPage.getScreenshot(withAllTextColored("#000000"));
        Visualization.algorithmStepFinished("3.) Take screenshot with all text colored black.", screenshotWithAllTextColoredBlack);
        // 4.) Take another screenshot with all text colored white ...
        Screenshot screenshotWithAllTextColoredWhite = webPage.getScreenshot(withAllTextColored("#ffffff"));
        Visualization.algorithmStepFinished("4.) Take another screenshot with all text colored white.", screenshotWithAllTextColoredWhite);
        // 5.) Determine potential text pixels by comparing the last two screenshots (ignoring Flash movies) ...
        boolean[][] textPixels = new CompareScreenshots(screenshotWithAllTextColoredBlack, screenshotWithAllTextColoredWhite).differentPixels;
        int w = Math.min(flashMoviePixels.length, textPixels.length);
        int h = Math.min(flashMoviePixels[0].length, textPixels[0].length);
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (flashMoviePixels[x][y]) {
                    textPixels[x][y] = false;
                }
            }
        }
        Visualization.algorithmStepFinished("5.) Determine potential text pixels by comparing the last two screenshots (ignoring Flash movies).", textPixels);
        // 6.) Take another screenshot of the web page (with text colors restored) ...
        Screenshot screenshot2 = webPage.getScreenshot(takenAtLeast(500, MILLISECONDS).laterThan(screenshot1));
        Visualization.algorithmStepFinished("6.) Take another screenshot of the web page (with text colors restored) ...", screenshot2);
        // ... and compare it with the initial screenshot (ignoring Flash movies) to find animated pixels ...
        CompareScreenshots diff = new CompareScreenshots(screenshot1, screenshot2).ignore(flashMoviePixels);
        Visualization.algorithmStepFinished("6.) ... and compare it with the initial screenshot (ignoring Flash movies) to find animated pixels.", diff);
        if (diff.noDifferencesFound) {
            // No animated pixels found, return the potential text pixels ...
            Visualization.algorithmFinished("7.) Done: No more animated pixels detected.", textPixels);
        } else {
            // Found animated pixels ...
            boolean[][] animatedPixels = diff.differentPixels;
            // Consider all pixels, which belong to a Flash movie, as animated ...
            w = Math.min(flashMoviePixels.length, diff.width);
            h = Math.min(flashMoviePixels[0].length, diff.height);
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (flashMoviePixels[x][y]) {
                        animatedPixels[x][y] = true;
                    }
                }
            }
            w = diff.width;
            h = diff.height;
            boolean moreAnimatedPixelsFound;
            // 7.) Take a series of screenshots to determine all animated pixels ...
            do {
                moreAnimatedPixelsFound = false;
                screenshot1 = screenshot2;
                screenshot2 = webPage.getScreenshot(takenAtLeast(100, MILLISECONDS).laterThan(screenshot1));
                int[][] pixels1 = screenshot1.pixels;
                int[][] pixels2 = screenshot2.pixels;
                for (int x = 0; x < w; ++x) {
                    for (int y = 0; y < h; ++y) {
                        if ((!animatedPixels[x][y]) && (pixels1[x][y] != pixels2[x][y])) {
                            animatedPixels[x][y] = true;
                            moreAnimatedPixelsFound = true;
                        }
                    }
                }
            } while (moreAnimatedPixelsFound);
            Visualization.algorithmStepFinished("7.) Take a series of screenshots to determine all animated pixels.", animatedPixels);
            // 8.) Ignore all animated pixels ...
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (animatedPixels[x][y]) {
                        textPixels[x][y] = false;
                    }
                }
            }
            Visualization.algorithmFinished("8.) Done: Ignore all found animated pixels.", textPixels);
        }
        return textPixels;
    }
}
