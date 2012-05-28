/*
 * Copyright 2009-2012 Michael Tamm
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

import com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition;
import com.googlecode.fightinglayoutbugs.helpers.RectangularRegion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition.UNMODIFIED;
import static com.googlecode.fightinglayoutbugs.helpers.StringHelper.asString;

/**
 * Represents a web page. This class was created to improve
 * performance when several {@link LayoutBugDetector}s
 * analyze the same page -- it caches as much information
 * as possible. Furthermore it stops all JavaScript
 * animations to reduce the possibility of false positives.
 */
public class WebPage {

    private static final Log LOG = LogFactory.getLog(WebPage.class);

    private final WebDriver _driver;
    private final ScreenshotCache _screenshotCache;

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;

    private URL _url;
    private SoftReference<String> _html;
    private SoftReference<boolean[][]> _textPixels;
    private SoftReference<boolean[][]> _horizontalEdges;
    private SoftReference<boolean[][]> _verticalEdges;

    private boolean _jqueryInjected;

    public WebPage(WebDriver driver) {
        _driver = driver;
        _screenshotCache = new ScreenshotCache(this);
        stopJavaScriptAnimations();
    }

    private void stopJavaScriptAnimations() {
        executeJavaScript(
            "var noop = function() {};\n" +
            "var i;\n" +
            "var n = window.setTimeout(noop, 1);\n" +
            "for (i = 0; i <= n; ++i) window.clearTimeout(i);\n" +
            "window.setTimeout = noop;\n" +
            "n = window.setInterval(noop, 1);\n" +
            "for (i = 0; i <= n; ++i) window.clearInterval(i);\n" +
            "window.setInterval = noop;\n"
        );
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
    @Nonnull
    public String getHtml() {
        String html = (_html == null ? null : _html.get());
        if (html == null) {
            html = _driver.getPageSource();
            _html = new SoftReference<String>(html);
        }
        return html;
    }

    public Screenshot getScreenshot() {
        return getScreenshot(UNMODIFIED);
    }

    public Screenshot getScreenshot(Condition condition) {
        return _screenshotCache.getScreenshot(condition);
    }

    /**
     * Bypasses the cache and always takes a screenshot.
     */
    public Screenshot takeScreenshot() {
        return takeScreenshot(UNMODIFIED);
    }

    /**
     * Bypasses the cache and always takes a screenshot.
     */
    public Screenshot takeScreenshot(Condition condition) {
        return _screenshotCache.takeScreenshot(condition);
    }

    /**
     * Returns a two dimensional array <tt>a</tt>, whereby <tt>a[x][y]</tt> is <tt>true</tt>
     * if the pixel with the coordinates x,y in a {@link #getScreenshot screenshot} of this web page
     * belongs to displayed text, otherwise <tt>a[x][y]</tt> is <tt>false</tt>.
     */
    public boolean[][] getTextPixels() {
        boolean[][] textPixels;
        if (_textPixels == null) {
            if (_textDetector == null) {
                _textDetector = new AnimationAwareTextDetector();
            }
            textPixels = _textDetector.detectTextPixelsIn(this);
            _textPixels = new SoftReference<boolean[][]>(textPixels);
        } else {
            textPixels = _textPixels.get();
            if (textPixels == null) {
                LOG.warn("Cached result of text detection was garbage collected, running text detection again -- give the JVM more heap memory to speed up layout bug detection.");
                _textPixels = null;
                return getTextPixels();
            }
        }
        return textPixels;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SBSC")
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
        boolean[][] horizontalEdges;
        if (_horizontalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            horizontalEdges = _edgeDetector.detectHorizontalEdgesIn(this);
            _horizontalEdges = new SoftReference<boolean[][]>(horizontalEdges);
        } else {
            horizontalEdges = _textPixels.get();
            if (horizontalEdges == null) {
                LOG.warn("Cached result of horizontal edge detection was garbage collected, running horizontal edge detection again -- give the JVM more heap memory to speed up layout bug detection.");
                _horizontalEdges = null;
                return getTextPixels();
            }
        }
        return horizontalEdges;
    }

    /**
     * Returns a two dimensional array <tt>a</tt>, whereby <tt>a[x][y]</tt> is <tt>true</tt>
     * if the pixel with the coordinates x,y in a {@link #getScreenshot screenshot} of this web page
     * belongs to a vertical edge, otherwise <tt>a[x][y]</tt> is <tt>false</tt>.
     */
    public boolean[][] getVerticalEdges() {
        boolean[][] verticalEdges;
        if (_verticalEdges == null) {
            if (_edgeDetector == null) {
                _edgeDetector = new SimpleEdgeDetector();
            }
            verticalEdges = _edgeDetector.detectVerticalEdgesIn(this);
            _verticalEdges = new SoftReference<boolean[][]>(verticalEdges);
        } else {
            verticalEdges = _textPixels.get();
            if (verticalEdges == null) {
                LOG.warn("Cached result of vertical edge detection was garbage collected, running vertical edge detection again -- give the JVM more heap memory to speed up layout bug detection.");
                _verticalEdges = null;
                return getTextPixels();
            }
        }
        return verticalEdges;
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

    void injectJQueryIfNotPresent() {
        if (!_jqueryInjected) {
            // Check if jQuery is present ...
            if ("undefined".equals(executeJavaScript("return typeof jQuery"))) {
                String jquery = readResource("jquery-1.7.2.min.js");
                executeJavaScript(jquery);
                // Check if jQuery was successfully injected ...
                if (!"1.7.2".equals(executeJavaScript("return jQuery.fn.jquery"))) {
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
}
