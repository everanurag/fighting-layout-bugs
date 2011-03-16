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

import static de.michaeltamm.fightinglayoutbugs.Screenshot.withAllTextColored;

/**
 * Detects text pixels by comparing screenshots after colorizing all text to black
 * and then to white via JavaScript.
 * Might return too many text pixels if there is animation on the web page
 * (like animated GIF images, Flash movies, or JavaScript  * animation). You should use the {@link AnimationAwareTextDetector} if you have
 * animation on your web page.
 *
 * @author Michael Tamm
 */
public class SimpleTextDetector implements TextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) {
        // 1.) Take first screenshot with all text colored black ...
        Screenshot screenshotWithAllTextColoredBlack = webPage.getScreenshot(withAllTextColored("#000000"));
        Visualization.algorithmStepFinished("1.) Take first screenshot with all text colored black.", screenshotWithAllTextColoredBlack);
        // 2.) Take second screenshot with all text colored white ...
        Screenshot screenshotWithAllTextColoredWhite = webPage.getScreenshot(withAllTextColored("#ffffff"));
        Visualization.algorithmStepFinished("2.) Take second screenshot with all text colored white.", screenshotWithAllTextColoredWhite);
        // 3.) Determine text pixels by comparing the last two screenshots ...
        boolean[][] textPixels = new CompareScreenshots(screenshotWithAllTextColoredBlack, screenshotWithAllTextColoredWhite).differentPixels;
        Visualization.algorithmFinished("3.) Determine text pixels by comparing the last two screenshots.", textPixels);
        return textPixels;
    }
}
