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

import de.michaeltamm.fightinglayoutbugs.Screenshot.Condition;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a web page. This class was created to improve
 * performance when several {@link LayoutBugDetector}s
 * analyze the same page. It caches as much information
 * as possible.
 *
 * @author Michael Tamm
 */
public abstract class WebPage {

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;

    private String _url;
    private String _html;
    private final List<Screenshot> _screenshots = new ArrayList<Screenshot>();
    private boolean[][] _textPixels;
    private boolean[][] _horizontalEdges;
    private boolean[][] _verticalEdges;

    private boolean _jqueryInjected;
    private boolean _textColorsBackedUp;
    private String _currentTextColor;

    /**
     * Sets the detector for {@link #getTextPixels()},
     * default is the {@link AnimationAwareTextDetector}.
     */
    public void setTextDetector(TextDetector textDetector) {
        if (_textPixels != null) {
            throw new IllegalStateException("getTextPixels() was already called.");
        }
        _textDetector = textDetector;
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

    public void resizeBrowserWindowTo(Dimension newBrowserWindowSize) {
        int currentBrowserWindowWidth = ((Number) executeJavaScript("return window.outerWidth")).intValue();
        boolean callResize = (currentBrowserWindowWidth != newBrowserWindowSize.width);
        if (!callResize) {
            int currentBrowserWindowHeight = ((Number) executeJavaScript("return window.outerHeight")).intValue();
            callResize = (currentBrowserWindowHeight != newBrowserWindowSize.height);
        }
        if (callResize) {
            executeJavaScript("window.resizeTo(" + newBrowserWindowSize.width + ", " + newBrowserWindowSize.height + ")");
            // Clear all cached screenshots and derived values ...
            _screenshots.clear();
            _textPixels = null;
            _horizontalEdges = null;
            _verticalEdges = null;
        }
    }

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

    private class Unmodified implements Condition {
        public boolean isSatisfiedBy(Screenshot screenshot) {
            return screenshot.textColor == null;
        }
        public boolean satisfyWillModifyWebPage() {
            return false;
        }
        public void satisfyFor(WebPage webPage) {
            restoreTextColors();
        }
    }

    /**
     * Returns a screenshot of this web page.
     *
     * @param conditions conditions the taken screenshot must satisfy.
     */
    public Screenshot getScreenshot(Condition... conditions) {
        // 1.) Check if there is a condition, which modifies the web page ...
        boolean thereIsAConditionWhichModifiesTheWebPage = false;
        for (int i = 0; i < conditions.length && !thereIsAConditionWhichModifiesTheWebPage; ++i) {
            if (conditions[i].satisfyWillModifyWebPage()) {
                thereIsAConditionWhichModifiesTheWebPage = true;
            }
        }
        // 2.) If there is no condition, which modifies the web page, we need to add the Unmodified condition ...
        if (!thereIsAConditionWhichModifiesTheWebPage) {
            Condition[] temp = new Condition[1 + conditions.length];
            temp[0] = new Unmodified();
            System.arraycopy(conditions, 0, temp, 1, conditions.length);
            conditions = temp;
        }
        // 3.) Check if we have already taken a screenshot which satisfies all conditions ...
        for (Screenshot screenshot : _screenshots) {
            boolean satisfiesAllConditions = true;
            for (int i = 0; i < conditions.length && satisfiesAllConditions; ++i) {
                if (!conditions[i].isSatisfiedBy(screenshot)) {
                    satisfiesAllConditions = false;
                }
            }
            if (satisfiesAllConditions) {
                return screenshot;
            }
        }
        // 4.) No screenshot satisfied all conditions, we need to tak a new one ...
        for (Condition condition : conditions) {
            condition.satisfyFor(this);
        }
        return takeScreenshot();
    }

    public boolean[][] getTextPixels() {
        if (_textPixels == null) {
            if (_textDetector == null) {
                _textDetector = new AnimationAwareTextDetector();
            }
            _textPixels = _textDetector.detectTextPixelsIn(this);
        }
        return _textPixels;
    }

    public boolean[][] getHorizontalEdges() {
        if (_horizontalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            _horizontalEdges = _edgeDetector.detectHorizontalEdgesIn(this);
        }
        return _horizontalEdges;
    }

    public boolean[][] getVerticalEdges() {
        if (_verticalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            _verticalEdges = _edgeDetector.detectVerticalEdgesIn(this);
        }
        return _verticalEdges;
    }

    /**
     * Returns all elements on this web page for the given find criteria.
     */
    public abstract List<WebElement> findElements(By by);

    /**
     * Executes the given JavaScript in the context of this web page.
     */
    protected abstract Object executeJavaScript(String javaScript, Object... arguments);

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
    protected abstract byte[] takeScreenshotAsPng();

    protected abstract void injectJQuery();

    void colorAllText(@Nonnull String color) {
        if (!color.equals(_currentTextColor)) {
            if (!_textColorsBackedUp) {
                injectJQueryIfNotPresent();
                executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.attr('flb_color_backup', j.css('color')); });");
                _textColorsBackedUp = true;
            }
            executeJavaScript("jQuery('*').css('color', '" + color + "');");
            _currentTextColor = color;
        }
    }

    void restoreTextColors() {
        if (_currentTextColor != null) {
            if (!_textColorsBackedUp) {
                throw new IllegalStateException("text colors have not been backed up.");
            }
            executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.css('color', j.attr('flb_color_backup')); });");
            _currentTextColor = null;
        }
    }

    private void injectJQueryIfNotPresent() {
        if (!_jqueryInjected) {
            injectJQuery();
            _jqueryInjected = true;
        }
    }

    private Screenshot takeScreenshot() {
        byte[] bytes = takeScreenshotAsPng();
        int[][] pixels = ImageHelper.pngToPixels(bytes);
        Screenshot screenshot = new Screenshot(pixels, _currentTextColor);
        _screenshots.add(screenshot);
        return screenshot;
    }
}
