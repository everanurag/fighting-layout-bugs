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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.WITH_NO_IMAGES;
import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.WITH_NO_IMAGES_AND_ALL_TEXT_BLACK;
import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.WITH_NO_IMAGES_AND_ALL_TEXT_WHITE;

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
 *     <li>all iframes, videos, Java Applets, and embedded objects like Flash movies are ignored.</li>
 * </ol>
 * </p><p>
 * If for any unknown reason there is still animation detected, a loop is entered,
 * which takes a series of screenshots and compares them until no more animated
 * pixels are found or {@link #setMaxTime max time} has been reached.
 * </p><p>
 * All animated pixels found are not considered to be text pixels.
 * </p>
 */
public class AnimationAwareTextDetector extends AbstractTextDetector {

    private static final Log LOG = LogFactory.getLog(AnimationAwareTextDetector.class);

    private static final int[] SOME_PRIME_NUMBERS = { 83, 107, 137, 167 };

    private long _maxMillis = 5000;

    /**
     * Sets the maximum time for the loop in {@link #detectTextPixelsIn} which
     * will take screenshots after screenshot as long as new animated pixels are found,
     * default is 5 seconds.
     */
    public void setMaxTime(long time, TimeUnit timeUnit) {
        if (time <= 0) {
            throw new IllegalArgumentException("Method parameter time must be greater than 0.");
        }
        if (timeUnit == null) {
            throw new IllegalArgumentException("Method parameter timeUnit must not be null.");
        }
        _maxMillis = timeUnit.toMillis(time);
    }

    public boolean[][] detectTextPixelsIn(WebPage webPage) {
        long startTime = System.currentTimeMillis();
        // 1.) Take initial screenshot of web page with no images ...
        Screenshot screenshot1 = webPage.takeScreenshot(WITH_NO_IMAGES);
        Visualization.algorithmStepFinished("1.) Take initial screenshot with no images of web page.", webPage, screenshot1);
        // 2.) Take a screenshot with all text colored black ...
        Screenshot screenshotWithAllTextColoredBlack = webPage.getScreenshot(WITH_NO_IMAGES_AND_ALL_TEXT_BLACK);
        Visualization.algorithmStepFinished("2.) Take a screenshot with no images and all text colored black.", webPage, screenshotWithAllTextColoredBlack);
        // 3.) Take another screenshot with all text colored white ...
        Screenshot screenshotWithAllTextColoredWhite = webPage.getScreenshot(WITH_NO_IMAGES_AND_ALL_TEXT_WHITE);
        Visualization.algorithmStepFinished("3.) Take another screenshot with no images and all text colored white.", webPage, screenshotWithAllTextColoredWhite);
        // 4.) Determine potential text pixels by comparing the last two screenshots ...
        CompareScreenshots diff1 = new CompareScreenshots(screenshotWithAllTextColoredBlack, screenshotWithAllTextColoredWhite);
        Visualization.algorithmStepFinished("4.) Determine potential text pixels by comparing the last two screenshots.", webPage, diff1);
        // 5.) Determine regions of Java Applets, embedded objects like Flash movies, videos, iframes, and other ignored elements ...
        Collection<RectangularRegion> ignoredRegions = getIgnoredRegions(webPage);
        Visualization.algorithmStepFinished("5.) Determine regions of iframes, videos, Java Applets, embedded objects like Flash movies, and other ignored elements.", webPage, ignoredRegions);
        // 6.) Take another screenshot of the web page (with text colors restored and at least 283 milliseconds later) ...
        sleepUntil(startTime + 283);
        Screenshot screenshot2 = webPage.takeScreenshot(WITH_NO_IMAGES);
        Visualization.algorithmStepFinished("6.) Take another screenshot of the web page (with text colors restored).", webPage, screenshot2);
        // 7.) Compare the last screenshot with the initial screenshot (ignoring ignored regions) to find more animated pixels ...
        CompareScreenshots diff2 = new CompareScreenshots(screenshot1, screenshot2).ignore(ignoredRegions);
        Visualization.algorithmStepFinished("7.) Compare the last screenshot with the initial screenshot (ignoring ignored regions) to find more animated pixels.", webPage, diff2);
        boolean[][] textPixels;
        if (diff2.noDifferencesFound()) {
            // 8.) No more animated pixels found, remove potential text pixels inside ignored regions ...
            textPixels = diff1.ignore(ignoredRegions).differentPixels;
            Visualization.algorithmFinished("8.) No more animated pixels found, remove potential text pixels inside ignored regions.", webPage, textPixels);
        } else {
            LOG.warn(
                "This is strange: Found animated pixels, although\n" +
                "(1) all JavaScript animations have been stopped,\n" +
                "(2) all animated GIF images have been hidden, and\n" +
                "(3) all elements potentially containing animation (like Java Applets, Flash Movies, videos, and iframes) are ignored.\n" +
                "Please send an email to fighting-layout-bugs@googlegroups.com with the URL " + webPage.getUrl() + ", so that we can have a look at it.");
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
            Visualization.algorithmStepFinished("8.) Found more animated pixels, consider all ignored regions as animated pixels too.", webPage, animatedPixels);
            boolean moreAnimatedPixelsFound;
            boolean timedOut;
            // 9.) Take a series of screenshots to determine all animated pixels ...
            int w = screenshot1.width;
            int h = screenshot2.height;
            Random random = new Random();
            do {
                moreAnimatedPixelsFound = false;
                screenshot1 = screenshot2;
                // Sleep some random time to give the animation CPU time ...
                // The sleep time is a prime number to increase the probability
                // that we don't miss an animation step ...
                sleep(SOME_PRIME_NUMBERS[random.nextInt(SOME_PRIME_NUMBERS.length)]);
                screenshot2 = webPage.takeScreenshot(WITH_NO_IMAGES);
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
                timedOut = (System.currentTimeMillis() - startTime) > _maxMillis;
            } while (moreAnimatedPixelsFound && !timedOut);
            if (timedOut) {
                LOG.warn("detectTextPixelsIn(...) timed out. This might lead to false positives. You can increase the maximum time for detecting animated pixels by calling setMaxTime(...).");
            }
            Visualization.algorithmStepFinished("9.) Take a series of screenshots to determine all animated pixels.", webPage, animatedPixels);
            // 10.) Ignore all animated pixels ...
            textPixels = diff1.differentPixels;
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (animatedPixels[x][y]) {
                        textPixels[x][y] = false;
                    }
                }
            }
            Visualization.algorithmFinished("10.) Ignore all animated pixels.", webPage, textPixels);
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
