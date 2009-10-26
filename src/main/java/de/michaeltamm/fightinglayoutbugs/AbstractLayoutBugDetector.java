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
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collection;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverBackedSelenium;



/**
 * @author Michael Tamm
 */
public abstract class AbstractLayoutBugDetector implements LayoutBugDetector {

    /** The directory where screenshots of erroneous pages will be saved. */
    File _screenshotDir;

    public void setScreenshotDir(File screenshotDir) {
        _screenshotDir = screenshotDir;
    }

    protected LayoutBug createLayoutBug(String message, WebPage webPage) {
        return createLayoutBug(message, webPage, null);
    }

    protected LayoutBug createLayoutBug(String message, WebPage webPage, boolean[][] buggyPixels) {
        File screenshot = null;
        if (_screenshotDir != null) {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String prefix = getClass().getSimpleName();
            if (prefix.startsWith("Detect")) {
                prefix = prefix.substring("Detect".length());
            }
            try {
                screenshot = File.createTempFile(prefix + "_" + df.format(new Date()) + ".", ".png", _screenshotDir);
                webPage.saveScreenshotTo(screenshot);
            } catch (IOException e) {
                System.err.print("Could not save screenshot: ");
                e.printStackTrace(System.err);
            }
        }
        final LayoutBug layoutBug = new LayoutBug(message, webPage, screenshot);
        if (buggyPixels != null) {
            try {
                layoutBug.markBuggyPixels(buggyPixels);
            } catch (Exception e) {
                System.err.print("Could not mark buggy pixels in screenshot: ");
                e.printStackTrace(System.err);
            }
        }
        return layoutBug;
    }

    public final Collection<LayoutBug> findLayoutBugsIn(WebDriver driver) throws Exception {
        if (driver instanceof FirefoxDriver) {
            final WebPage webPage = new WebPage((FirefoxDriver) driver);
            return findLayoutBugsIn(webPage);
        }
        throw new IllegalArgumentException("Currently only FirefoxDriver is supported.");
    }

    public Collection<LayoutBug> findLayoutBugsIn(Selenium selenium) throws Exception {
        if (selenium instanceof WebDriverBackedSelenium) {
            WebDriver driver = ((WebDriverBackedSelenium) selenium).getUnderlyingWebDriver();
            if (driver instanceof FirefoxDriver) {
                final WebPage webPage = new WebPage((FirefoxDriver) driver);
                return findLayoutBugsIn(webPage);
            }
        }
        throw new IllegalArgumentException("Currently on WebDriverBackedSelenium backed by a FirefoxDriver is supported.");
    }
}
