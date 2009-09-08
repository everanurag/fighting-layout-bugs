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

import java.io.File;
import java.io.IOException;

/**
 * Abstract base class for detectors, which provides common helper methods.
 *
 * @author michael.tamm
 * @version 1.0 24.08.2009
 */
public abstract class AbstractDetector {

    protected void injectJQueryInto(FirefoxDriver driver) {
        driver.executeScript(
            "if (typeof jQuery == 'undefined') {\n" +
            "    document.body.appendChild(document.createElement('script')).src = 'http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js';\n" +
            "}"
        );
    }

    protected int[][] takeScreenshot(FirefoxDriver driver) throws IOException {
        final File tempFile = File.createTempFile(getClass().getSimpleName(), ".png");
        try {
            driver.saveScreenshot(tempFile);
            return ImageHelper.fileToPixels(tempFile);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }
}
