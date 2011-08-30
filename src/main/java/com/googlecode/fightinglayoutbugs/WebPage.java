/*
 * Copyright 2009-2011 Michael Tamm
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

package com.googlecode.fightinglayoutbugs;

import com.googlecode.fightinglayoutbugs.Screenshot.Condition;
import com.googlecode.fightinglayoutbugs.helpers.Dimension;
import com.googlecode.fightinglayoutbugs.helpers.ImageHelper;
import com.googlecode.fightinglayoutbugs.helpers.RectangularRegion;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.*;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.google.common.primitives.Bytes.asList;
import static com.googlecode.fightinglayoutbugs.helpers.StringHelper.asString;

/**
 * Represents a web page. This class was created to improve
 * performance when several {@link LayoutBugDetector}s
 * analyze the same page. It caches as much information
 * as possible.
 *
 * @author Michael Tamm
 */
public class WebPage {

    private static List<Byte> PNG_SIGNATURE = asList(new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

    private final WebDriver _driver;

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;

    private URL _url;
    private String _html;
    private final List<Screenshot> _screenshots = new ArrayList<Screenshot>();
    private boolean[][] _textPixels;
    private boolean[][] _horizontalEdges;
    private boolean[][] _verticalEdges;

    private boolean _jqueryInjected;
    private boolean _textColorsBackedUp;
    private String _currentTextColor;

    public WebPage(WebDriver driver) {
        _driver = driver;
    }

    public WebDriver getDriver() {
        return _driver;
    }

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
        Number currentBrowserWindowWidth = (Number) executeJavaScript("return window.outerWidth");
        if (currentBrowserWindowWidth == null || currentBrowserWindowWidth.intValue() != newBrowserWindowSize.width || ((Number) executeJavaScript("return window.outerHeight")).intValue() != newBrowserWindowSize.height) {
            executeJavaScript("window.resizeTo(" + newBrowserWindowSize.width + ", " + newBrowserWindowSize.height + ")");
            if (currentBrowserWindowWidth != null) {
                // Check if resizing succeeded ...
                int browserWindowWidth = ((Number) executeJavaScript("return window.outerWidth")).intValue();
                int browserWindowHeight = ((Number) executeJavaScript("return window.outerHeight")).intValue();
                if (browserWindowWidth != newBrowserWindowSize.width || browserWindowHeight != newBrowserWindowSize.height) {
                    throw new RuntimeException("Failed to resize browser window. If you are using Chrome, see http://code.google.com/p/chromium/issues/detail?id=2091");
                }
            }
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
    public URL getUrl() {
        if (_url == null) {
            String urlAsString = _driver.getCurrentUrl();
            try {
                _url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Could not convert " + asString(urlAsString) + " into an URL.", e);
            }
        }
        return _url;
    }

    /**
     * Returns the source HTML of this web page.
     */
    public String getHtml() {
        if (_html == null) {
            _html = _driver.getPageSource();
        }
        return _html;
    }

    /**
     * Takes back any changes which might have been applied to the web page.
     */
    public void restore() {
        restoreTextColors();
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

    /**
     * Returns a two dimensional array <tt>a</tt>, whereby <tt>a[x][y]</tt> is <tt>true</tt>
     * if the pixel with the coordinates x,y in a {@link #getScreenshot screenshot} of this web page
     * belongs to displayed text, otherwise <tt>a[x][y]</tt> is <tt>false</tt>.
     */
    public boolean[][] getTextPixels() {
        if (_textPixels == null) {
            if (_textDetector == null) {
                _textDetector = new AnimationAwareTextDetector();
            }
            _textPixels = _textDetector.detectTextPixelsIn(this);
        }
        return _textPixels;
    }

    public Collection<RectangularRegion> getRectangularRegionsCoveredBy(Collection<String> jQuerySelectors) {
        if (jQuerySelectors.isEmpty()) {
            return Collections.emptySet();
        }
        injectJQueryIfNotPresent();
        // 1.) Assemble JavaScript to select elements ...
        Iterator<String> i = jQuerySelectors.iterator();
        String js = "jQuery('" + i.next().replace("'", "\\'");
        while (i.hasNext()) {
            js += "').add('" + i.next().replace("'", "\\'");
        }
        js += "').filter(':visible')";
        // 2.) Assemble JavaScript function to fill an array with rectangular region of each selected element ...
        js = "function() { " +
                 "var a = new Array(); " +
                  js + ".each(function(i, e) { " +
                           "var j = jQuery(e); " +
                           "var o = j.offset(); " +
                           "a.push({ top: o.top, left: o.left, width: j.width(), height: j.height() }); " +
                       "}); " +
                 "return a; " +
             "}";
        // 3.) Execute JavaScript function ...
        @SuppressWarnings("unchecked")
        List<Map<String, Number>> list = (List<Map<String, Number>>) executeJavaScript("return (" + js + ")()");
        // 4.) Convert JavaScript return value to Java return value ...
        if (list.isEmpty()) {
            return Collections.emptySet();
        }
        Collection<RectangularRegion> result = new ArrayList<RectangularRegion>(list.size());
        for (Map<String, Number> map : list) {
            double left = map.get("left").doubleValue();
            double width = map.get("width").doubleValue();
            double top = map.get("top").doubleValue();
            double height = map.get("height").doubleValue();
            if (height > 0 && width > 0) {
                int x1 = (int) left;
                int y1 = (int) top;
                int x2 = (int) Math.round(left + width - 0.5000001);
                int y2 = (int) Math.round(top + height - 0.5000001);
                if (x2 >= 0 && y2 >= 0) {
                    if (x1 < 0) {
                        x1 = 0;
                    }
                    if (y1 < 0) {
                        y1 = 0;
                    }
                    if (x1 <= x2 && y1 <= y2) {
                        result.add(new RectangularRegion(x1, y1, x2, y2));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a two dimensional array <tt>a</tt>, whereby <tt>a[x][y]</tt> is <tt>true</tt>
     * if the pixel with the coordinates x,y in a {@link #getScreenshot screenshot} of this web page
     * belongs to a horizontal edge, otherwise <tt>a[x][y]</tt> is <tt>false</tt>.
     */
    public boolean[][] getHorizontalEdges() {
        if (_horizontalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            _horizontalEdges = _edgeDetector.detectHorizontalEdgesIn(this);
        }
        return _horizontalEdges;
    }

    /**
     * Returns a two dimensional array <tt>a</tt>, whereby <tt>a[x][y]</tt> is <tt>true</tt>
     * if the pixel with the coordinates x,y in a {@link #getScreenshot screenshot} of this web page
     * belongs to a vertical edge, otherwise <tt>a[x][y]</tt> is <tt>false</tt>.
     */
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
    public List<WebElement> findElements(By by) {
        return _driver.findElements(by);
    }

    /**
     * Executes the given JavaScript in the context of this web page.
     */
    protected Object executeJavaScript(String javaScript, Object... arguments) {
        if (_driver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) _driver).executeScript(javaScript, arguments);
        } else {
            throw new UnsupportedOperationException("Can't execute JavaScript via " + _driver.getClass().getName());
        }
    }

    /**
     * Returns the bytes of a PNG image.
     */
    protected byte[] takeScreenshotAsPng() {
        if (_driver instanceof TakesScreenshot) {
            byte[] bytes = ((TakesScreenshot) _driver).getScreenshotAs(OutputType.BYTES);
            if (bytes == null) {
                throw new RuntimeException(_driver.getClass().getName() + ".getScreenshotAs(OutputType.BYTES) returned null.");
            }
            if (bytes.length < 8) {
                throw new RuntimeException(_driver.getClass().getName() + ".getScreenshotAs(OutputType.BYTES) did not return a PNG image.");
            } else {
                // Workaround for http://code.google.com/p/selenium/issues/detail?id=1686 ...
                if (!asList(bytes).subList(0, 8).equals(PNG_SIGNATURE)) {
                    bytes = Base64.decodeBase64(bytes);
                }
                if (!asList(bytes).subList(0, 8).equals(PNG_SIGNATURE)) {
                    throw new RuntimeException(_driver.getClass().getName() + ".getScreenshotAs(OutputType.BYTES) did not return a PNG image.");
                }
            }
            return bytes;
        } else {
            throw new UnsupportedOperationException(_driver.getClass().getName() + " does not support taking screenshots.");
        }
    }

    void colorAllText(@Nonnull String color) {
        if (!color.equals(_currentTextColor)) {
            if (!_textColorsBackedUp) {
                injectJQueryIfNotPresent();
                executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.attr('flb_color_backup', j.css('color')); }).size();"); // ... the trailing ".size()" will reduce the size of the response
                _textColorsBackedUp = true;
            }
            executeJavaScript("jQuery('*').css('color', '" + color + "').size();"); // ... the trailing ".size()" will reduce the size of the response
            _currentTextColor = color;
        }
    }

    void restoreTextColors() {
        if (_currentTextColor != null) {
            if (!_textColorsBackedUp) {
                throw new IllegalStateException("text colors have not been backed up.");
            }
            executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.css('color', j.attr('flb_color_backup')); }).size();"); // ... the trailing ".size()" will reduce the size of the response
            _currentTextColor = null;
        }
    }

    private void injectJQueryIfNotPresent() {
        if (!_jqueryInjected) {
            // Check if jQuery is present ...
            if ("undefined".equals(executeJavaScript("return typeof jQuery"))) {
                String jquery = readResource("jquery-1.6.2.min.js");
                executeJavaScript(jquery);
                // Check if jQuery was successfully injected ...
                if (!"1.6.2".equals(executeJavaScript("return jQuery.fn.jquery"))) {
                    throw new RuntimeException("Failed to inject jQuery.");
                }
            }
            _jqueryInjected = true;
        }
    }

    protected String readResource(String resourceFileName) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            InputStream in = getClass().getResourceAsStream(resourceFileName);
            try {
                try {
                    IOUtils.copy(in, buf);
                } catch (IOException e) {
                    throw new RuntimeException("Could not read " + resourceFileName, e);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        } finally {
            IOUtils.closeQuietly(buf);
        }
        try {
            return new String(buf.toByteArray(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new RuntimeException(e);
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
