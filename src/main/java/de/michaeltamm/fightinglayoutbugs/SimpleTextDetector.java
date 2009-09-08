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

import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * @author Michael Tamm
 */
public class SimpleTextDetector extends AbstractDetector implements TextDetector {

    public boolean[][] detectTextPixelsIn(FirefoxDriver driver) throws Exception {
        injectJQueryInto(driver);
        // Backup colors of all elements ...
        driver.executeScript("jQuery('*').each(function() { var j = jQuery(this); j.attr('color_backup', j.css('color')); });");
        try {
            // Colorize all text black ...
            driver.executeScript("jQuery('*').css('color', '#000000');");
            // Take first screenshot ...
            final int[][] pixels1 = takeScreenshot(driver);
            final int w = pixels1.length;
            final int h = pixels1[0].length;
            // Colorize all text white ...
            driver.executeScript("jQuery('*').css('color', '#ffffff');");
            // Take second screenshot ...
            final int[][] pixels2 = takeScreenshot(driver);
            assert pixels2.length == w;
            assert pixels2[0].length == h;
            // Detect text pixels ...
            final boolean[][] result = new boolean[w][h];
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    result[x][y] = (pixels1[x][y] != pixels2[x][y]);
                }
            }
            return result;
        } finally {
            // Restore colors ...
            driver.executeScript("jQuery('*').each(function() { var j = jQuery(this); j.css('color', j.attr('color_backup')); });");
        }
    }
}
