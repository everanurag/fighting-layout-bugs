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

import java.util.Collection;

import static de.michaeltamm.fightinglayoutbugs.Screenshot.takenAtLeast;
import static de.michaeltamm.fightinglayoutbugs.Screenshot.withAllTextColored;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Works similar to the {@link SimpleTextDetector}, but detects
 * and ignores animated GIF images and JavaScript animation too
 * by taking a series of screenshots and comparing them until
 * no more animated pixels are found.
 *
 * @author Michael Tamm
 */
public class AnimationAwareTextDetector extends AbstractTextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) {
        // 1.) Take initial screenshot of web page ...
        Screenshot screenshot1 = webPage.getScreenshot();
        Visualization.algorithmStepFinished("1.) Take initial screenshot of web page.", screenshot1);
        // 2.) Take a screenshot with all text colored black ...
        Screenshot screenshotWithAllTextColoredBlack = webPage.getScreenshot(withAllTextColored("#000000"));
        Visualization.algorithmStepFinished("2.) Take a screenshot with all text colored black.", screenshotWithAllTextColoredBlack);
        // 3.) Take another screenshot with all text colored white ...
        Screenshot screenshotWithAllTextColoredWhite = webPage.getScreenshot(withAllTextColored("#ffffff"));
        Visualization.algorithmStepFinished("3.) Take another screenshot with all text colored white.", screenshotWithAllTextColoredWhite);
        // 4.) Determine potential text pixels by comparing the last two screenshots ...
        CompareScreenshots diff1 = new CompareScreenshots(screenshotWithAllTextColoredBlack, screenshotWithAllTextColoredWhite);
        Visualization.algorithmStepFinished("4.) Determine potential text pixels by comparing the last two screenshots.", diff1);
        // 5.) Determine regions of Java Applets, embedded objects like Flash movies, iframes, and other ignored elements ...
        Collection<RectangularRegion> ignoredRegions = getIgnoredRegions(webPage);
        Visualization.algorithmStepFinished("5.) Determine regions of Java Applets, embedded objects like Flash movies, iframes, and other ignored elements.", ignoredRegions);
        // 6.) Take another screenshot of the web page (with text colors restored) ...
        Screenshot screenshot2 = webPage.getScreenshot(takenAtLeast(500, MILLISECONDS).laterThan(screenshot1));
        Visualization.algorithmStepFinished("6.) Take another screenshot of the web page (with text colors restored).", screenshot2);
        // 7.) Compare the last screenshot with the initial screenshot (ignoring ignored regions) to find more animated pixels ...
        CompareScreenshots diff2 = new CompareScreenshots(screenshot1, screenshot2).ignore(ignoredRegions);
        Visualization.algorithmStepFinished("7.) Compare the last screenshot with the initial screenshot (ignoring ignored regions) to find more animated pixels.", diff2);
        boolean[][] textPixels;
        if (diff2.noDifferencesFound) {
            // 8.) No more animated pixels found, remove potential text pixels inside ignored regions ...
            textPixels = diff1.ignore(ignoredRegions).differentPixels;
            Visualization.algorithmFinished("8.) No more animated pixels, remove potential text pixels inside ignored regions.", textPixels);
        } else {
            // 8.) Found more animated pixels, consider all ignored regions as animated pixels too ...
            boolean[][] animatedPixels = diff2.differentPixels;
            for (RectangularRegion ignoredRegion : ignoredRegions) {
                int x2 = Math.min(diff2.width - 1, ignoredRegion.x2);
                int y2 = Math.min(diff2.height - 1, ignoredRegion.y2);
                for (int x = ignoredRegion.x1; x <= x2; ++x) {
                    for (int y = ignoredRegion.y1; y <= y2; ++y) {
                        animatedPixels[x][y] = true;
                    }
                }
            }
            Visualization.algorithmStepFinished("8.) Found more animated pixels, consider all ignored regions as animated pixels too.", animatedPixels);
            boolean moreAnimatedPixelsFound;
            // 9.) Take a series of screenshots to determine all animated pixels ...
            int w = screenshot1.width;
            int h = screenshot2.height;
            do {
                moreAnimatedPixelsFound = false;
                screenshot1 = screenshot2;
                screenshot2 = webPage.getScreenshot(takenAtLeast(137, MILLISECONDS).laterThan(screenshot1));
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
            Visualization.algorithmStepFinished("9.) Take a series of screenshots to determine all animated pixels.", animatedPixels);
            // 10.) Ignore all animated pixels ...
            textPixels = diff1.differentPixels;
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (animatedPixels[x][y]) {
                        textPixels[x][y] = false;
                    }
                }
            }
            Visualization.algorithmFinished("10.) Ignore all animated pixels.", textPixels);
        }
        return textPixels;
    }
}
