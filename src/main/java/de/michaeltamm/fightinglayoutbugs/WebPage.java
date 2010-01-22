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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.List;

/**
 * Represents a web page. This interface was created
 * to improve performance when several {@link LayoutBugDetector}s
 * analyze the same page. Implementations are supposed to cache
 * all information when they are retrieved the first time.
 *
 * @author Michael Tamm
 */
public interface WebPage {

    /** Returns the URL of this web page. */
    public String getUrl();

    /** Returns the source HTML of this web page. */
    public String getHtml();

    /** Returns a screenshot of this web page. */
    public int[][] getScreenshot() throws Exception;

    /** Saves the {@link #getScreenshot() screenshot} of this web page to the given file in PNG format. */
    public void saveScreenshotTo(File pngFile) throws Exception;

    /** Takes a screenshot of what is currently displayed. */
    public int[][] takeScreenshot() throws Exception;

    /** Returns all elements on this web page for the given find criteria. */
    public List<WebElement> findElements(By by);

    /** Executes the given JavaScript in the context of this web page. */
    public Object executeJavaScript(String javaScript, Object... arguments);

    /** Injects <a href="http://jquery.com/">jQuery</a> into this web page, if not already present. */
    public void injectJQueryIfNotPresent() throws Exception;

    public void backupTextColors() throws Exception;

    public void restoreTextColors() throws Exception;

    /** Returns a screenshot of this web page where all text is transparent. */
    public int[][] getScreenshotWithoutText() throws Exception;

    public boolean[][] getTextPixels() throws Exception;

    public boolean[][] getHorizontalEdges() throws Exception;

    public boolean[][] getVerticalEdges() throws Exception;
}
