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

import org.testng.annotations.Test;

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.*;

/**
 * @author Michael Tamm
 */
public class AdvancedTextDetectorTest extends TestUsingSelenium {

    @Test
    public void shouldBehaveLikeSimpleTextDetectorWhenThereAreNoAnimations() throws Exception {
        // We can not test with ChromeDriver yet, because it compresses screenshots :( ...
        WebPage testPage = getWebPageFor("/Yahoo!_Profile_Updates.html").usingFirefoxDriver();
        boolean[][] expected = new SimpleTextDetector().detectTextPixelsIn(testPage);
        boolean[][] actual = new AdvancedTextDetector().detectTextPixelsIn(testPage);
        assertThat(actual, is(expected));
        // Test with DefaultSelenium ...
        testPage = getWebPageFor("/Microsoft_Newsletter.html").usingDefaultSelenium();
        expected = new SimpleTextDetector().detectTextPixelsIn(testPage);
        actual = new AdvancedTextDetector().detectTextPixelsIn(testPage);
        assertThat(actual, is(expected));
        // Test with FirefoxDriver ...
        testPage = getWebPageFor("/ESPRIT_newsletter.html").usingFirefoxDriver();
        expected = new SimpleTextDetector().detectTextPixelsIn(testPage);
        actual = new AdvancedTextDetector().detectTextPixelsIn(testPage);
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldIgnoreAnimations() throws Exception {
        WebPage testPage = getWebPageFor("/page_with_animated_gif.html").usingFirefoxDriver();
        final TextDetector detector = new AdvancedTextDetector();
        final boolean[][] textPixels = detector.detectTextPixelsIn(testPage);
        for (boolean[] column : textPixels) {
            for (boolean isTextPixel : column) {
                assertThat(isTextPixel, is(false));
            }
        }
    }

}
