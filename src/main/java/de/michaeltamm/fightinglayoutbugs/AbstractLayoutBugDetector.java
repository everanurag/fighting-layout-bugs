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

import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @author Michael Tamm
 */
public abstract class AbstractLayoutBugDetector implements LayoutBugDetector {

    /** The directory where screenshots of erroneous pages will be saved. */
    File _screenshotDir;

    public void setScreenshotDir(File screenshotDir) {
        _screenshotDir = screenshotDir;
    }

    protected LayoutBug createLayoutBug(String message, WebPage webPage, boolean saveScreenshot) {
        return createLayoutBug(message, webPage, saveScreenshot, null);
    }

    protected LayoutBug createLayoutBug(String message, WebPage webPage, Marker marker) {
        return createLayoutBug(message, webPage, true, marker);
    }

    private LayoutBug createLayoutBug(String message, WebPage webPage, boolean saveScreenshot, Marker marker) {
        File screenshotFile = null;
        boolean screenshotSaved = false;
        if (saveScreenshot && _screenshotDir != null) {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String prefix = getClass().getSimpleName();
            if (prefix.startsWith("Detect")) {
                prefix = prefix.substring("Detect".length());
            }
            try {
                screenshotFile = File.createTempFile(prefix + "_" + df.format(new Date()) + ".", ".png", _screenshotDir);
                webPage.saveScreenshotTo(screenshotFile);
                screenshotSaved = true;
            } catch (Exception e) {
                System.err.print("Could not save screenshot: ");
                e.printStackTrace(System.err);
            }
        }
        final LayoutBug layoutBug = new LayoutBug(message, webPage, screenshotFile);
        if (screenshotSaved && marker != null) {
            try {
                layoutBug.markScreenshotUsing(marker);
            } catch (Exception e) {
                System.err.print("Could not mark screenshot: ");
                e.printStackTrace(System.err);
            }
        }
        return layoutBug;
    }

    public final Collection<LayoutBug> findLayoutBugsIn(WebDriver driver) throws Exception {
        WebPage webPage = new WebPageBackedByWebDriver(driver);
        return findLayoutBugsIn(webPage);
    }

    public final Collection<LayoutBug> findLayoutBugsIn(Selenium selenium) throws Exception {
        WebPage webPage = new WebPageBackedBySelenium(selenium);
        return findLayoutBugsIn(webPage);
    }
}
