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
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import static java.lang.Character.isWhitespace;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Michael Tamm
 */
public class DetectInvalidImageUrls extends AbstractLayoutBugDetector {

    private static class Css {
        public String charset;
        public String text;
    }

    static String stripCommentsFrom(String css) {
        final int n = css.length();
        int j = css.indexOf("/*");
        final String result;
        if (j == -1) {
            result = css;
        } else {
            final StringBuilder sb = new StringBuilder(n);
            int i = 0;
            do {
                sb.append(css, i, j);
                i = css.indexOf("*/", i) + 2;
                if (i == 1) {
                    i = n;
                }
                j = css.indexOf("/*", i);
            } while (j != -1);
            sb.append(css, i, n);
            result = sb.toString();
        }
        return result;
    }

    /**
     * <code>""</code> as value means the URL is valid, otherwise
     * the value contains the error message for the URL.
     */
    private final Map<URL, String> _checkedUrls = new HashMap<URL, String>();

    private URL _baseUrl;
    private boolean _screenshotTaken;
    private String _documentCharset;
    private HttpClient _httpClient;
    private Set<URL> _visitedCssUrls;
    /** Initialized in by {@link #findLayoutBugsIn}, might be overwritten by {@link #checkLinkedCss}. */
    private String _faviconUrl;

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) {
        // Determine base URL for completion of relative URLs ...
        final String url = webPage.getUrl();
        try {
            _baseUrl = new URL(url);
        } catch (MalformedURLException e) {
            // Should never happen.
            throw new RuntimeException("Could not convert " + url + " into an URL.", e);
        }
        _documentCharset = (String) webPage.executeJavaScript("return document.characterSet");
        try {
            _httpClient = new HttpClient();
            HttpState state = getHttpStateFor(webPage);
            _httpClient.setState(state);
            _visitedCssUrls = new HashSet<URL>();
            _faviconUrl = "/favicon.ico";
            try {
                final List<LayoutBug> layoutBugs = new ArrayList<LayoutBug>();
                // 1. Check the src attribute of all <img> elements ...
                checkImgElements(webPage, layoutBugs);
                // 2. Check the style attribute of all elements ...
                checkStyleAttributes(webPage, layoutBugs);
                // 3. Check all <style> elements ...
                checkStyleElements(webPage, layoutBugs);
                // 4. Check all linked CSS resources ...
                checkLinkedCss(webPage, layoutBugs);
                // 5. Check favicon ...
                checkFavicon(webPage, layoutBugs);
                return layoutBugs;
            } finally {
                _visitedCssUrls = null;
                _httpClient = null;
            }
        } finally {
            _baseUrl = null;
        }
    }

    private HttpState getHttpStateFor(WebPage webPage) {
        HttpState state = new HttpState();
        if (webPage instanceof WebPageBackedByWebDriver) {
            WebDriver driver = ((WebPageBackedByWebDriver) webPage).getDriver();
            for (org.openqa.selenium.Cookie cookie : driver.manage().getCookies()) {
                state.addCookie(new Cookie(cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getExpiry(), cookie.isSecure()));
            }
        } else {
            Selenium selenium = ((WebPageBackedBySelenium) webPage).getSelenium();
            // TODO: copy cookies from Selenium
        }
        return state;
    }

    boolean saveScreenshot() {
        if (_screenshotTaken) {
            return false;
        } else {
            _screenshotTaken = true;
            return true;
        }
    }

    private void checkImgElements(WebPage webPage, List<LayoutBug> layoutBugs) {
        int numImgElementsWithoutSrcAttribute = 0;
        int numImgElementsWithEmptySrcAttribute = 0;
        final Set<String> seen = new HashSet<String>();
        for (WebElement img : webPage.findElements(By.tagName("img"))) {
            final String src = img.getAttribute("src");
            if (src == null) {
                ++numImgElementsWithoutSrcAttribute;
            } else if ("".equals(src)) {
                ++numImgElementsWithEmptySrcAttribute;
            } else {
                if (!seen.contains(src)) {
                    try {
                        final URL imageUrl = getCompleteUrlFor(src);
                        final String error = checkImageUrl(imageUrl);
                        if (error.length() > 0) {
                            layoutBugs.add(createLayoutBug("Detected <img> element with invalid src attribute \"" + src + "\" - " + error, webPage, saveScreenshot()));
                        }
                    } catch (MalformedURLException e) {
                        layoutBugs.add(createLayoutBug("Detected <img> element with invalid src attribute \"" + src + "\" - " + e.getMessage(), webPage, saveScreenshot()));
                    }
                    seen.add(src);
                }
            }
        }
        if (numImgElementsWithEmptySrcAttribute > 0) {
            if (numImgElementsWithEmptySrcAttribute == 1) {
                layoutBugs.add(createLayoutBug("Detected <img> element with empty src attribute.", webPage, saveScreenshot()));
            } else {
                layoutBugs.add(createLayoutBug("Detected " + numImgElementsWithEmptySrcAttribute + " <img> elements with empty src attribute.", webPage, saveScreenshot()));
            }
        }
        if (numImgElementsWithoutSrcAttribute > 0) {
            if (numImgElementsWithEmptySrcAttribute == 1) {
                layoutBugs.add(createLayoutBug("Detected <img> without src attribute.", webPage, saveScreenshot()));
            } else {
                layoutBugs.add(createLayoutBug("Detected " + numImgElementsWithoutSrcAttribute + " <img> elements without src attribute.", webPage, saveScreenshot()));
            }
        }
    }

    private void checkStyleAttributes(WebPage webPage, List<LayoutBug> layoutBugs) {
        for (WebElement element : webPage.findElements(By.xpath("//*[@style]"))) {
            final String css = element.getAttribute("style");
            for (String importUrl : getImportUrlsFrom(css)) {
                checkCssResource(importUrl + " (imported in style attribute of <" + element.getTagName() + "> element)", importUrl, _baseUrl, _documentCharset, webPage, layoutBugs);
            }
            for (String imageUrl : extractUrlsFrom(css).keySet()) {
                try {
                    final String error = checkImageUrl(getCompleteUrlFor(imageUrl));
                    if (error.length() > 0) {
                        layoutBugs.add(createLayoutBug("Detected <" + element.getTagName() + "> element with invalid image URL \"" + imageUrl + "\" in its style attribute - " + error, webPage, saveScreenshot()));
                    }
                } catch (MalformedURLException e) {
                    layoutBugs.add(createLayoutBug("Detected <" + element.getTagName() + "> element with invalid image URL \"" + imageUrl + "\" in its style attribute - " + e.getMessage(), webPage, saveScreenshot()));
                }
            }
        }
    }

    private void checkStyleElements(WebPage webPage, List<LayoutBug> layoutBugs) {
        for (WebElement styleElement : webPage.findElements(By.tagName("style"))) {
            final String css = (String) webPage.executeJavaScript("return arguments[0].innerHTML", styleElement);
            for (String importUrl : getImportUrlsFrom(css)) {
                checkCssResource(importUrl + " (imported in <style> element)", importUrl, _baseUrl, _documentCharset, webPage, layoutBugs);
            }
            for (String imageUrl : extractUrlsFrom(css).keySet()) {
                try {
                    final String error = checkImageUrl(getCompleteUrlFor(imageUrl));
                    if (error.length() > 0) {
                        layoutBugs.add(createLayoutBug("Detected <style> element with invalid image URL \"" + imageUrl + "\" - " + error, webPage, saveScreenshot()));
                    }
                } catch (MalformedURLException e) {
                    layoutBugs.add(createLayoutBug("Detected <style> element with invalid image URL \"" + imageUrl + "\" - " + e.getMessage(), webPage, saveScreenshot()));
                }
            }
        }
    }

    private void checkLinkedCss(WebPage webPage, List<LayoutBug> layoutBugs) {
        for (WebElement link : webPage.findElements(By.tagName("link"))) {
            String rel = link.getAttribute("rel");
            if (rel != null) {
                rel = rel.toLowerCase(Locale.ENGLISH);
            }
            final String type = link.getAttribute("type");
            final String href = link.getAttribute("href");
            if ((rel != null && rel.contains("stylesheet")) || (type != null && type.startsWith("text/css"))) {
                if (href != null) {
                    String charset = link.getAttribute("charset");
                    if (!isValidCharset(charset)) {
                        charset = _documentCharset;
                    }
                    checkCssResource(href, href, _baseUrl, charset, webPage, layoutBugs);
                }
            }
            // prepare checkFavicon ...
            if (rel != null && ("icon".equals(rel) || "shortcut icon".equals(rel))) {
                if (href != null) {
                    _faviconUrl = href;
                }
            }
        }
    }

    private boolean isValidCharset(String charset) {
        boolean result = false;
        if (charset != null && charset.length() > 0) {
            try {
                result = (Charset.forName(charset) != null);
            } catch (Exception ignored) {}
        }
        return result;
    }

    private void checkCssResource(String pathToCssResource, String url, URL baseUrl, String fallBackCharset, WebPage webPage, List<LayoutBug> layoutBugs) {
        URL cssUrl = null;
        try {
            cssUrl = getCompleteUrlFor(baseUrl, url);
        } catch (MalformedURLException e) {
            System.err.print("Could not get CSS from " + pathToCssResource + " - " + e.getMessage());
        }
        if (cssUrl != null && !_visitedCssUrls.contains(cssUrl)) {
            _visitedCssUrls.add(cssUrl);
            final Css css = getCssFrom(cssUrl, fallBackCharset);
            if (css.text != null) {
                for (String importUrl : getImportUrlsFrom(css.text)) {
                    checkCssResource(importUrl + " (imported from " + pathToCssResource + ")", importUrl, cssUrl, css.charset, webPage, layoutBugs);
                }
                for (String imageUrl : extractUrlsFrom(css.text).keySet()) {
                    try {
                        final String error = checkImageUrl(getCompleteUrlFor(cssUrl, imageUrl));
                        if (error.length() > 0) {
                            layoutBugs.add(createLayoutBug("Detected invalid image URL \"" + imageUrl + "\" in " + pathToCssResource + " - " + error, webPage, saveScreenshot()));
                        }
                    } catch (MalformedURLException e) {
                        layoutBugs.add(createLayoutBug("Detected invalid image URL \"" + imageUrl + "\" in " + pathToCssResource + " - " + e.getMessage(), webPage, saveScreenshot()));
                    }
                }
            }
        }
    }

    /**
     * Extract the import URLs from CSS.
     * See <a href="http://www.w3.org/TR/CSS2/cascade.html#at-import">http://www.w3.org/TR/CSS2/cascade.html#at-import</a>
     */
    private Set<String> getImportUrlsFrom(String css) {
        css = stripCommentsFrom(css).trim();
        // Skip @charset rule if present ...
        if (css.startsWith("@charset")) {
            int i = css.indexOf(";");
            if (i == -1) {
                css = "";
            } else {
                css = css.substring(i + 1).trim();
            }
        }
        // Only parse @import rules at the beginning of the CSS ...
        final Set<String> result = new HashSet<String>();
        while (css.startsWith("@import")) {
            int i = css.indexOf(";");
            if (i == -1) {
                // Ignore incomplete @import rule ...
                css = "";
            } else {
                String temp = css.substring("@import".length(), i).trim();
                if (!temp.startsWith("url(")) {
                    temp = "url(" + temp + ")";
                }
                String url = extractUrlsFrom(temp).keySet().iterator().next();
                result.add(url);
                css = css.substring(i + 1).trim();
            }
        }
        return result;
    }

    /**
     * Extracts URLs from CSS.
     * See <a href="http://www.w3.org/TR/CSS2/syndata.html#value-def-uri">http://www.w3.org/TR/CSS2/syndata.html#value-def-uri</a>
     */
    private Map<String, Integer> extractUrlsFrom(String css) {
        final ConcurrentMap<String, Integer> imageUrls = new ConcurrentHashMap<String, Integer>();
        css = stripCommentsFrom(css);
        final int n = css.length();
        // 1.) Skip at-rules ...
        int i = 0;
        do {
            while (i < n && isWhitespace(css.charAt(i))) {
                ++i;
            }
            if (i < n && css.charAt(i) == '@') {
                i = css.indexOf(';', i) + 1;
                if (i == 0) {
                    i = n;
                }
            }
        } while (i < n && (isWhitespace(css.charAt(i)) || css.charAt(i) == '@'));
        // 2. Extract all remaining URLs ...
        i = css.indexOf("url(", i);
        while (i != -1) {
            int j = i + 4;
            while (j < n && isWhitespace(css.charAt(j))) {
                ++j;
            }
            int k;
            if (j < n && css.charAt(j) == '"') {
                ++j;
                k = css.indexOf('"', j);
                if (k == -1) {
                    k = n;
                }
            } else if (j < n && css.charAt(j) == '\'') {
                ++j;
                k = css.indexOf('\'', j);
                if (k == -1) {
                    k = n;
                }
            } else if (j < n) {
                k = css.indexOf(')', j);
                while (k != -1 && css.charAt(k - 1) == '\\') {
                    k = css.indexOf(')', k + 1);
                }
                if (k == -1) {
                    k = n;
                }
                while (k - 1 > j && isWhitespace(css.charAt(k - 1))) {
                    --k;
                }
            } else {
                j = k = n;
            }
            final String url = css.substring(j, k);
            // Put if absent, so the returned map contains the position of the *first* occurrence for each URL ...
            imageUrls.putIfAbsent(url, i);
            i = css.indexOf("url(", k);
        }
        return imageUrls;
    }

    /**
     * @param externallySpecifiedCharset the charset from the charset attribute of a &lt;link&gt; attribute if present,
     *                                   otherwise the charset of the refering style sheet or document.
     */
    private Css getCssFrom(URL url, String externallySpecifiedCharset) {
        final Css result = new Css();
        final GetMethod getMethod = new GetMethod(url.toExternalForm());
        getMethod.setFollowRedirects(true);
        try {
            _httpClient.executeMethod(getMethod);
            if (getMethod.getStatusCode() >= 400) {
                System.err.println("Could not get CSS from " + url + " - server responded with: " + getMethod.getStatusCode() + " " + getMethod.getStatusText());
            } else {
                final InputStream in = getMethod.getResponseBodyAsStream();
                try {
                    final Utf8BomAwareByteArrayOutputStream out = new Utf8BomAwareByteArrayOutputStream();
                    IOUtils.copy(in, out);
                    // Determine charset (see http://www.w3.org/TR/CSS2/syndata.html#charset) ...
                    // 1. Check charset parameter of Content-Type response header ...
                    final Header contentTypeHeader = getMethod.getResponseHeader("Content-Type");
                    if (contentTypeHeader != null) {
                        final HeaderElement[] a = contentTypeHeader.getElements();
                        if (a.length > 0) {
                            final NameValuePair charsetParam = a[0].getParameterByName("charset");
                            if (charsetParam != null) {
                                result.charset = charsetParam.getValue();
                            }
                        }
                    }
                    // 2. Check for BOM ...
                    if (!isValidCharset(result.charset) && out.hasUtf8Bom()) {
                        result.charset= "UTF-8";
                    }
                    // 3. Check for @charset rule ...
                    if (!isValidCharset(result.charset)) {
                        String temp = out.toString("US-ASCII");
                        if (temp.startsWith("@charset \"")) {
                            int i = temp.indexOf("\";");
                            if (i == -1) {
                                result.text = "";
                            } else {
                                result.charset = temp.substring("@charset \"".length(), i);
                            }
                        }
                    }
                    // 4. Fall back to the externally specified charset parameter ...
                    if (!isValidCharset(result.charset) && result.text == null) {
                        result.charset = externallySpecifiedCharset;
                    }
                    // 5. If the charset is not determined by now, assume UTF-8 ...
                    if (!isValidCharset(result.charset) && result.text == null) {
                        result.charset = "UTF-8";
                    }
                    if (result.text == null) {
                        try {
                            result.text = out.toString(result.charset);
                        } catch (UnsupportedEncodingException e) {
                            result.text = "";
                        }
                    }
                } finally {
                    in.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not get CSS from " + url + " - " + e.getMessage());
        } finally {
            getMethod.releaseConnection();
        }
        return result;
    }

    private void checkFavicon(WebPage webPage, List<LayoutBug> layoutBugs) {
        URL faviconUrl = null;
        try {
            faviconUrl = getCompleteUrlFor(_faviconUrl);
        } catch (MalformedURLException e) {
            layoutBugs.add(createLayoutBug("Detected invalid favicon URL \"" + _faviconUrl + "\" - " + e.getMessage(), webPage, saveScreenshot()));
        }
        if (faviconUrl != null) {
            final String error = checkImageUrl(faviconUrl);
            if (error.length() > 0) {
                layoutBugs.add(createLayoutBug("Detected invalid favicon URL \"" + _faviconUrl + "\" - " + error, webPage, saveScreenshot()));
            }
        }
    }

    /**
     * Returns <code>""</code> if the given URL is a valid image URL,
     * otherwise an error message is returned.
     */
    private String checkImageUrl(URL url) {
        String error = _checkedUrls.get(url);
        if (error == null) {
            final GetMethod getMethod;
            try {
                getMethod = new GetMethod(url.toURI().toString());
            } catch (URISyntaxException e) {
                // TODO: how can we check the url?
                System.out.println("Ignoring image URL " + url + " -- it can not be checked with Apache HttpClient.");
                return "";
            }
            getMethod.setFollowRedirects(true);
            try {
                _httpClient.executeMethod(getMethod);
                if (getMethod.getStatusCode() >= 400) {
                    if (getMethod.getStatusCode() == 401) {
                        System.out.println("Ignoring HTTP response status code 401 (" + getMethod.getStatusText() + ") for image URL " + url);
                        error = "";
                    } else {
                        error = "HTTP GET responded with: " + getMethod.getStatusCode() + " " + getMethod.getStatusText();
                    }
                } else {
                    final Header contentTypeHeader = getMethod.getResponseHeader("Content-Type");
                    if (contentTypeHeader == null) {
                        error = "HTTP response did not contain Content-Type header.";
                    } else {
                        final String contentType = contentTypeHeader.getValue();
                        if (!contentType.startsWith("image/")) {
                            error = "Content-Type HTTP response header \"" + contentType + "\" does not start with \"image/\".";
                        } else {
                            // The given URL seems to be a valid image URL.
                            error = "";
                        }
                    }
                }
            } catch (IOException e) {
                error = "HTTP GET failed: " + e.getMessage();
            } finally {
                getMethod.releaseConnection();
            }
            _checkedUrls.put(url, error);
        }
        return error;
    }

    private URL getCompleteUrlFor(String url) throws MalformedURLException {
        return getCompleteUrlFor(_baseUrl, url);
    }

    private URL getCompleteUrlFor(URL baseUrl, String url) throws MalformedURLException {
        final URL completeUrl;
        if (hasProtocol(url)) {
            completeUrl = new URL(url);
        } else {
            completeUrl = new URL(baseUrl, url);
        }
        return completeUrl;
    }

    private boolean hasProtocol(String url) {
        boolean result = false;
        if (url != null) {
            final int i = url.indexOf(':');
            final int j = url.indexOf('?');
            result = (i > 0 && (j == -1 || i < j));
        }
        return result;
    }
}
