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

import java.io.File;

/**
 * @author Michael Tamm
 */
public abstract class AbstractWebPage implements WebPage {

    private String _url;
    private String _html;
    private int[][] _screenshot;

    private boolean _jqueryInjected;
    private boolean _textColorsBackedUp;
    private int[][] _screenshotWithoutText;
    private boolean[][] _textPixels;
    private boolean[][] _horizontalEdges;
    private boolean[][] _verticalEdges;

    public String getUrl() {
        if (_url == null) {
            _url = retrieveUrl();
        }
        return _url;
    }

    public String getHtml() {
        if (_html == null) {
            _html = retrieveHtml();
        }
        return _html;
    }

    public int[][] getScreenshot() throws Exception {
        if (_screenshot == null) {
            _screenshot = takeScreenshot();
        }
        return _screenshot;
    }

    public void saveScreenshotTo(File pngFile) throws Exception {
        if (_screenshot != null) {
            ImageHelper.pixelsToFile(_screenshot, pngFile);
        } else {
            byte[] bytes = takeScreenshotAsBytes();
            FileUtils.writeByteArrayToFile(pngFile, bytes);
        }
    }

    public int[][] takeScreenshot() throws Exception {
        byte[] bytes = takeScreenshotAsBytes();
        return ImageHelper.bytesToPixels(bytes);
    }

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

    public boolean[][] getTextPixels() throws Exception {
        if (_textPixels == null) {
            final TextDetector textDetector = new AdvancedTextDetector();
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
