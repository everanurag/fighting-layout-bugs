/*
 * Copyright 2009-2012 Michael Tamm
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

import com.googlecode.fightinglayoutbugs.helpers.RectangularRegion;

import java.util.Collection;

import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.*;

/**
 * <p>
 * Works similar to the {@link SimpleTextDetector}, but additionally
 * hides all images to prevent animated GIF images to influence
 * text detection and tries to detect if there is still some animation
 * by comparing to screenshots (with no images) taken at different times.
 * </p><p>
 * Actually I expected that no animation is found on the analyzed web page,
 * when the text detection is performed, because:<ol>
 *     <li>all JavaScript animations have been stopped</li>
 *     <li>all animated GIF images have been hidden</li>
 *     <li>all iframes, videos, Java Applets, and embedded objects like Flash movies are ignored,</li>
 *     <li>all CSS animations have been paused, and</li>
 *     <li>all CSS transitions have been disabled</li>
 * </ol>
 * </p><p>
 * If for any unknown reason there is still animation detected,
 * a {@link AnimationDetectedException} is thrown to prevent false alarm.
 * </p>
 */
public class AnimationAwareTextDetector extends AbstractTextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) throws AnimationDetectedException {
        long startTime = System.currentTimeMillis();
        // 1.) Take initial screenshot of web page with no images ...
        Screenshot screenshot1 = webPage.takeScreenshot(WITH_NO_IMAGES);
        Visualization.algorithmStepFinished("1.) Took initial screenshot with no images of web page.", webPage, screenshot1);
        // 2.) Take a screenshot with all text colored black ...
        Screenshot screenshotWithAllTextColoredBlack = webPage.getScreenshot(WITH_NO_IMAGES_AND_ALL_TEXT_BLACK);
        Visualization.algorithmStepFinished("2.) Took a screenshot with no images and all text colored black.", webPage, screenshotWithAllTextColoredBlack);
        // 3.) Take another screenshot with all text colored white ...
        Screenshot screenshotWithAllTextColoredWhite = webPage.getScreenshot(WITH_NO_IMAGES_AND_ALL_TEXT_WHITE);
        Visualization.algorithmStepFinished("3.) Took another screenshot with no images and all text colored white.", webPage, screenshotWithAllTextColoredWhite);
        // 4.) Determine potential text pixels by comparing the last two screenshots ...
        CompareScreenshots diff1 = new CompareScreenshots(screenshotWithAllTextColoredBlack, screenshotWithAllTextColoredWhite);
        Visualization.algorithmStepFinished("4.) Determined potential text pixels by comparing the last two screenshots.", webPage, diff1);
        // 5.) Determine regions of Java Applets, embedded objects like Flash movies, videos, iframes, and other ignored elements ...
        Collection<RectangularRegion> ignoredRegions = getIgnoredRegions(webPage);
        Visualization.algorithmStepFinished("5.) Determined regions of iframes, videos, Java Applets, embedded objects like Flash movies, and other ignored elements.", webPage, ignoredRegions);
        // 6.) Take another screenshot of the web page (with text colors restored and at least 283 milliseconds later) ...
        sleepUntil(startTime + 283);
        Screenshot screenshot2 = webPage.takeScreenshot(WITH_NO_IMAGES);
        Visualization.algorithmStepFinished("6.) Took another screenshot of the web page (with text colors restored).", webPage, screenshot2);
        // 7.) Compare the last screenshot with the initial screenshot (ignoring ignored regions) to find animated pixels ...
        CompareScreenshots diff2 = new CompareScreenshots(screenshot1, screenshot2).ignore(ignoredRegions);
        Visualization.algorithmStepFinished("7.) Compared the last screenshot with the initial screenshot (ignoring ignored regions) to find more animated pixels.", webPage, diff2);
        boolean[][] textPixels;
        if (diff2.noDifferencesFound()) {
            // 8.) No animated pixels found, remove potential text pixels inside ignored regions ...
            textPixels = diff1.ignore(ignoredRegions).differentPixels;
            Visualization.algorithmFinished("8.) Done: No animated pixels found, removed potential text pixels inside ignored regions.", webPage, textPixels);
        } else {
            throw new AnimationDetectedException(
                "This is strange: Found animated pixels, although\n" +
                "- all JavaScript animations have been stopped,\n" +
                "- all animated GIF images have been hidden,\n" +
                "- all elements potentially containing animation (like Java Applets, Flash Movies, videos, and iframes) are ignored.\n" +
                "- all CSS animations have been paused, and\n" +
                "- all CSS transitions have been disabled.\n" +
                "Analysis is stopped, so you don't get false alarms.\n" +
                "If you want support (or want to support FLB) please\n" +
                "1.) add calls to FightingLayoutBugs.setScreenshotDir(...) and FightingLayoutBugs.enableDebugMode() to your code\n," +
                "2.) run it again, and if you get this exception again\n" +
                "3.) send an email to fighting-layout-bugs@googlegroups.com with the following information:\n" +
                "    - Your code.\n" +
                "    - All logged output.\n" +
                "    - All screenshot files (you might want to pack those into an zip archive).\n" +
                "    - Which version of FLB do you use?\n" +
                "    - Which version of Selenium do you use?\n" +
                "    - Which browser (type and version) do you use?\n" +
                "    - Which Java version do you use?\n" +
                "    - Which OS (type and version)do you use?\n"
            );
        }
        return textPixels;
    }

    private void sleepUntil(long t) {
        long delay = t - System.currentTimeMillis();
        if (delay > 0) {
            sleep(delay);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Got interrupted.", e);
        }
    }
}
