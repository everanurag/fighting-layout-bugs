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
 * runs slower than the {@link SimpleTextDetector},
 * but is able to detect and ignore animation on the web page.
 *
 * @author Michael Tamm
 */
public class AdvancedTextDetector implements TextDetector {

    public boolean[][] detectTextPixelsIn(WebPage webPage) throws Exception {
        int[][] screenshotWithBlackText;
        int[][] screenshotWithDarkGrayText;
        int[][] screenshotWithLightGrayText;
        int[][] screenshotWithWhiteText;
        webPage.injectJQueryIfNotPresent();
        webPage.backupTextColors();
        try {
            webPage.executeJavaScript("jQuery('*').css('color', '#000000');");
            screenshotWithBlackText = webPage.takeScreenshot();
            webPage.executeJavaScript("jQuery('*').css('color', '#555555');");
            screenshotWithDarkGrayText = webPage.takeScreenshot();
            webPage.executeJavaScript("jQuery('*').css('color', '#AAAAAA');");
            screenshotWithLightGrayText = webPage.takeScreenshot();
            webPage.executeJavaScript("jQuery('*').css('color', '#FFFFFF');");
            screenshotWithWhiteText = webPage.takeScreenshot();
        } finally {
            webPage.restoreTextColors();
        }
        int w = screenshotWithBlackText.length;
        int h = screenshotWithBlackText[0].length;
        boolean[][] result = new boolean[w][h];
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (screenshotWithBlackText[x][y] != screenshotWithWhiteText[x][y]) {
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
        return result;
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
