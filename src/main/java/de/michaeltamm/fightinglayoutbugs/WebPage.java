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
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Represents a web page. This class was created
 * to improve performance: It caches all information
 * when it is retrieved the first time. So if several
 * layout bug detectors request the same information,
 * it is only retrieved once.
 *
 * @author Michael Tamm
 */
public class WebPage {

    private final FirefoxDriver _driver;

    private String _url;
    private String _html;
    private int[][] _screenshot;
    private int[][] _screenshotWithoutText;
    private boolean[][] _textPixels;
    private boolean[][] _horizontalEdges;
    private boolean[][] _verticalEdges;
    private boolean _jqueryInjected;
    private boolean _textColorsBackedUp;

    public WebPage(FirefoxDriver driver) {
        _driver = driver;
    }

    /** Returns the URL of this web page. */
    public String getUrl() {
        if (_url == null) {
            _url = _driver.getCurrentUrl();
        }
        return _url;
    }

    /** Returns the source HTML of this web page. */
    public String getHtml() {
        if (_html == null) {
            _html = _driver.getPageSource();
        }
        return _html;
    }

    /** Returns a screenshot of this web page. */
    public int[][] getScreenshot() throws Exception {
        if (_screenshot == null) {
            _screenshot = takeScreenshot();
        }
        return _screenshot;
    }

    /** Returns a screenshot of this web page where all text is transparent. */
    public int[][] getScreenshotWithoutText() throws Exception {
        if (_screenshotWithoutText == null) {
            backupTextColors();
            try {
                injectJQueryIfNotPresent();
                // Hide all text ...
                executeJavaScript("jQuery('*').css('color', 'transparent');");
                // take screenshot ...
                _screenshotWithoutText = takeScreenshot();
            } finally {
                restoreTextColors();
            }
        }
        return _screenshotWithoutText;
    }
    
    /** Injects <a href="http://jquery.com/">jQuery</a> into this web page, if not already present. */
    public void injectJQueryIfNotPresent() {
        if (!_jqueryInjected) {
            executeJavaScript(
                "if (typeof jQuery == 'undefined') {\n" +
                "    document.body.appendChild(document.createElement('script')).src = 'http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js';\n" +
                "}"
            );
            _jqueryInjected = true;
        }
    }

    /** Takes a screenshot of what is currently displayed. */
    public int[][] takeScreenshot() throws IOException {
        final File tempFile = File.createTempFile("screenshot-", ".png");
        try {
            _driver.saveScreenshot(tempFile);
            return ImageHelper.fileToPixels(tempFile);
        } finally {
            if (tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
        }
    }

    /** Saves a {@link #getScreenshot() screenshot} of this web page to the given PNG file. */
    public void saveScreenshotTo(File pngFile) throws IOException {
        if (_screenshot == null) {
            _driver.saveScreenshot(pngFile);
        } else {
            ImageHelper.pixelsToFile(_screenshot, pngFile);
        }
    }

    public boolean[][] getTextPixels() throws Exception {
        if (_textPixels == null) {
            final TextDetector textDetector = new SimpleTextDetector();
            _textPixels = textDetector.detectTextPixelsIn(this);
        }
        return _textPixels;
    }

    public boolean[][] getHorizontalEdges() throws Exception {
        if (_horizontalEdges == null) {
            final EdgeDetector edgeDetector = new SimpleEdgeDetector();
            _horizontalEdges = edgeDetector.detectHorizontalEdgesIn(this, 16);
        }
        return _horizontalEdges;
    }

    public boolean[][] getVerticalEdges() throws Exception {
        if (_verticalEdges == null) {
            final EdgeDetector edgeDetector = new SimpleEdgeDetector();
            _verticalEdges = edgeDetector.detectVerticalEdgesIn(this, 16);
        }
        return _verticalEdges;
    }

    public void backupTextColors() {
        if (!_textColorsBackedUp) {
            injectJQueryIfNotPresent();
            executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.attr('flb_color_backup', j.css('color')); });");
            _textColorsBackedUp = true;
        }
    }

    public void restoreTextColors() {
        if (!_textColorsBackedUp) {
            throw new IllegalStateException("You must call backupTextColors() before you can call restoreTextColors()");
        }
        executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.css('color', j.attr('flb_color_backup')); });");
    }

    public List<WebElement> findElements(By by) {
        return _driver.findElements(by);
    }

    public Object executeJavaScript(String javaScript, Object...  arguments) {
        return _driver.executeScript(javaScript, arguments);
    }
}
