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

import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.WITH_ALL_TEXT_BLACK;
import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.WITH_ALL_TEXT_WHITE;

/**
 * Detects text pixels by comparing screenshots after colorizing all text to black
 * and then to white via JavaScript. Ignores Java Applets, embedded objects like
 * Flash movies, and iframes. Might return too many text pixels if there are
 * animated GIF images or JavaScript animation. You should use the
 * {@link AnimationAwareTextDetector} for such cases.
 *
 * @author Michael Tamm
 */
public class SimpleTextDetector extends AbstractTextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) {
        // 1.) Take first screenshot with all text colored black ...
        Screenshot screenshotWithAllTextColoredBlack = webPage.getScreenshot(WITH_ALL_TEXT_BLACK);
        Visualization.algorithmStepFinished("1.) Take first screenshot with all text colored black.", webPage, screenshotWithAllTextColoredBlack);
        // 2.) Take second screenshot with all text colored white ...
        Screenshot screenshotWithAllTextColoredWhite = webPage.getScreenshot(WITH_ALL_TEXT_WHITE);
        Visualization.algorithmStepFinished("2.) Take second screenshot with all text colored white.", webPage, screenshotWithAllTextColoredWhite);
        // 3.) Determine potential text pixels by comparing the last two screenshots ...
        CompareScreenshots diff = new CompareScreenshots(screenshotWithAllTextColoredBlack, screenshotWithAllTextColoredWhite);
        Visualization.algorithmStepFinished("3.) Determine potential text pixels by comparing the last two screenshots.", webPage, diff);
        // 4.) Determine regions of Java Applets, embedded objects like Flash movies, iframes, and other ignored elements ...
        Collection<RectangularRegion> ignoredRegions = getIgnoredRegions(webPage);
        Visualization.algorithmStepFinished("4.) Determine regions of Java Applets, embedded objects like Flash movies, iframes, and other ignored elements.", webPage, ignoredRegions);
        // 5.) Remove potential text pixels inside ignored regions ...
        boolean[][] textPixels = diff.ignore(ignoredRegions).differentPixels;
        Visualization.algorithmFinished("5.) Remove potential text pixels inside ignored regions.", webPage, textPixels);
        return textPixels;
    }
}
