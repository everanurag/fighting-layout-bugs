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

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.List;

/**
 * Represents a web page. This class was created
 * to improve performance when several {@link LayoutBugDetector}s
 * analyze the same page. It caches all information
 * when retrieved for the first time.
 *
 * @author Michael Tamm
 */
public abstract class WebPage {

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;

    private String _url;
    private String _html;
    private int[][] _screenshot;

    private boolean _jqueryInjected;
    private boolean _textColorsBackedUp;
    private int[][] _screenshotWithoutText;
    private boolean[][] _textPixels;
    private boolean[][] _horizontalEdges;
    private boolean[][] _verticalEdges;

    /**
     * Returns the URL of this web page.
     */
    public String getUrl() {
        if (_url == null) {
            _url = retrieveUrl();
        }
        return _url;
    }

    /**
     * Returns the source HTML of this web page.
     */
    public String getHtml() {
        if (_html == null) {
            _html = retrieveHtml();
        }
        return _html;
    }

    /**
     * Returns a screenshot of this web page.
     */
    public int[][] getScreenshot() throws Exception {
        if (_screenshot == null) {
            _screenshot = takeScreenshot();
        }
        return _screenshot;
    }

    /**
     * Saves the {@link #getScreenshot() screenshot} of this web page to the given file in PNG format.
     */
    public void saveScreenshotTo(File pngFile) throws Exception {
        if (_screenshot != null) {
            ImageHelper.pixelsToFile(_screenshot, pngFile);
        } else {
            byte[] bytes = takeScreenshotAsBytes();
            FileUtils.writeByteArrayToFile(pngFile, bytes);
        }
    }

    /**
     * Takes a screenshot of what is currently displayed.
     */
    public int[][] takeScreenshot() throws Exception {
        byte[] bytes = takeScreenshotAsBytes();
        return ImageHelper.bytesToPixels(bytes);
    }

    /**
     * Returns all elements on this web page for the given find criteria.
     */
    public abstract List<WebElement> findElements(By by);

    /**
     * Executes the given JavaScript in the context of this web page.
     */
    public abstract Object executeJavaScript(String javaScript, Object... arguments);

    /**
     * Injects <a href="http://jquery.com/">jQuery</a> into this web page, if not already present.
     */
    public void injectJQueryIfNotPresent() {
        if (!_jqueryInjected) {
            injectJQuery();
            _jqueryInjected = true;
        }
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

    /**
     * Returns a screenshot of this web page where all text is transparent.
     */
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


    /**
     * Sets the detector for {@link #getTextPixels()}, default is the {@link AdvancedTextDetector}.
     */
    public void setTextDetector(TextDetector textDetector) {
        if (_textPixels != null) {
            throw new IllegalStateException("getTextPixels() was already called.");
        }
        _textDetector = textDetector;
    }

    public boolean[][] getTextPixels() throws Exception {
        if (_textPixels == null) {
            if (_textDetector == null) {
                _textDetector = new AdvancedTextDetector();
            }
            _textPixels = _textDetector.detectTextPixelsIn(this);
        }
        return _textPixels;
    }

    /**
     * Sets the detector for {@link #getHorizontalEdges()} and {@link #getVerticalEdges()},
     * default is the {@link SimpleEdgeDetector}.
     */
    public void setEdgeDetector(EdgeDetector edgeDetector) {
        if (_horizontalEdges != null) {
            throw new IllegalStateException("getHorizontalEdges() was already called.");
        }
        if (_verticalEdges != null) {
            throw new IllegalStateException("getVerticalEdges() was already called.");
        }
        _edgeDetector = edgeDetector;
    }

    public boolean[][] getHorizontalEdges() throws Exception {
        if (_horizontalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            _horizontalEdges = _edgeDetector.detectHorizontalEdgesIn(this);
        }
        return _horizontalEdges;
    }

    public boolean[][] getVerticalEdges() throws Exception {
        if (_verticalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            _verticalEdges = _edgeDetector.detectVerticalEdgesIn(this);
        }
        return _verticalEdges;
    }

    /**
     * Returns the URL of this web page.
     */
    protected abstract String retrieveUrl();

    /**
     * Returns the HTML source code of this web page.
     */
    protected abstract String retrieveHtml();

    /**
     * Returns the bytes of a PNG image.
     */
    protected abstract byte[] takeScreenshotAsBytes();

    protected abstract void injectJQuery();

}
