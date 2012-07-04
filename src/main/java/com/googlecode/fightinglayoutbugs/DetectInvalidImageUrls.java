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

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Character.isWhitespace;

/**
 * Detects invalid image URLs in the HTML source of the analyzed web page as well
 * as all directly or indirectly referenced CSS resources.
 */
public class DetectInvalidImageUrls extends AbstractLayoutBugDetector {

    private static final Log LOG = LogFactory.getLog(DetectInvalidImageUrls.class);

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

    private static boolean isValidCharset(String charset) {
        boolean result = false;
        if (charset != null && charset.length() > 0) {
            try {
                result = (Charset.forName(charset) != null);
            } catch (Exception ignored) {}
        }
        return result;
    }

    private static boolean hasProtocol(String url) {
        boolean result = false;
        if (url != null) {
            final int i = url.indexOf(':');
            final int j = url.indexOf('?');
            result = (i > 0 && (j == -1 || i < j));
        }
        return result;
    }

    /**
     * <code>""</code> as value means the image URL is either currently being checked or valid,
     * all other values are the error message for the image URL.
     */
    private final ConcurrentMap<String, String> _checkedImageUrls = new ConcurrentHashMap<String, String>();

    private WebPage _webPage;
    private URL _baseUrl;
    private String _documentCharset;
    private boolean _screenshotTaken;
    private Set<String> _checkedCssUrls;
    private String _faviconUrl;
    private List<LayoutBug> _layoutBugs;
    private HttpClient _httpClient;
    private MockBrowser _mockBrowser;

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) {
        try {
            _webPage = webPage;
            _baseUrl = _webPage.getUrl();
            _documentCharset = (String) _webPage.executeJavaScript("return document.characterSet");
            _screenshotTaken = false;
            _checkedCssUrls = new ConcurrentSkipListSet<String>();
            _faviconUrl = "/favicon.ico";
            _layoutBugs = new ArrayList<LayoutBug>();
            _mockBrowser = new MockBrowser(_httpClient == null ? new HttpClient(new MultiThreadedHttpConnectionManager()) : _httpClient);
            try {
                // 1. Check the src attribute of all visible <img> elements ...
                checkVisibleImgElements();
                // 2. Check the style attribute of all elements ...
                checkStyleAttributes();
                // 3. Check all <style> elements ...
                checkStyleElements();
                // 4. Check all linked CSS resources ...
                checkLinkedCss();
                // 5. Check favicon ...
                checkFavicon();
                // 6. Wait until all asynchronous checks are finished ...
                _mockBrowser.waitUntilAllDownloadsAreFinished();
                return _layoutBugs;
            } finally {
                _mockBrowser.dispose();
            }
        } finally {
            // Free resources for garbage collection ...
            _mockBrowser = null;
            _layoutBugs = null;
            _faviconUrl = null;
            _checkedCssUrls = null;
            _documentCharset = null;
            _baseUrl = null;
            _webPage = null;
        }
    }

    /**
     * Sets the {@link HttpClient} used for downloading CSS files and checking image URLs.
     */
    public void setHttpClient(HttpClient httpClient) {
        _httpClient = httpClient;
    }

    private void checkVisibleImgElements() {
        int numImgElementsWithoutSrcAttribute = 0;
        int numImgElementsWithEmptySrcAttribute = 0;
        final Set<String> seen = new HashSet<String>();
        for (WebElement img : _webPage.findElements(By.tagName("img"))) {
            if (img.isDisplayed()) {
                final String src = img.getAttribute("src");
                if (src == null) {
                    ++numImgElementsWithoutSrcAttribute;
                } else if ("".equals(src)) {
                    ++numImgElementsWithEmptySrcAttribute;
                } else {
                    if (seen.add(src)) {
                        try {
                            checkImageUrl(src, "Detected visible <img> element with invalid src attribute \"" + src + "\"");
                        } catch (MalformedURLException e) {
                            addLayoutBugIfNotPresent("Detected visible <img> element with invalid src attribute \"" + src + "\" -- " + e.getMessage());
                        }
                    }
                }
            }
        }
        if (numImgElementsWithEmptySrcAttribute > 0) {
            if (numImgElementsWithEmptySrcAttribute == 1) {
                addLayoutBugIfNotPresent("Detected visible <img> element with empty src attribute.");
            } else {
                addLayoutBugIfNotPresent("Detected " + numImgElementsWithEmptySrcAttribute + " visible <img> elements with empty src attribute.");
            }
        }
        if (numImgElementsWithoutSrcAttribute > 0) {
            if (numImgElementsWithEmptySrcAttribute == 1) {
                addLayoutBugIfNotPresent("Detected visible <img> without src attribute.");
            } else {
                addLayoutBugIfNotPresent("Detected " + numImgElementsWithoutSrcAttribute + " visible <img> elements without src attribute.");
            }
        }
    }

    private void checkStyleAttributes() {
        for (WebElement element : _webPage.findElements(By.xpath("//*[@style]"))) {
            final String css = element.getAttribute("style");
            for (String importUrl : getImportUrlsFrom(css)) {
                checkCssResourceAsync(importUrl + " (imported in style attribute of <" + element.getTagName() + "> element)", importUrl, _baseUrl, _documentCharset);
            }
            for (String url : extractUrlsFrom(css)) {
                try {
                    checkImageUrl(url, "Detected <" + element.getTagName() + "> element with invalid image URL \"" + url + "\" in its style attribute");
                } catch (MalformedURLException e) {
                    addLayoutBugIfNotPresent("Detected <" + element.getTagName() + "> element with invalid image URL \"" + url + "\" in its style attribute -- " + e.getMessage());
                }
            }
        }
    }

    private void checkStyleElements() {
        for (WebElement styleElement : _webPage.findElements(By.tagName("style"))) {
            final String css = (String) _webPage.executeJavaScript("return arguments[0].innerHTML", styleElement);
            for (String importUrl : getImportUrlsFrom(css)) {
                checkCssResourceAsync(importUrl + " (imported in <style> element)", importUrl, _baseUrl, _documentCharset);
            }
            for (String url : extractUrlsFrom(css)) {
                try {
                    checkImageUrl(url, "Detected <style> element with invalid image URL \"" + url + "\"");
                } catch (MalformedURLException e) {
                    addLayoutBugIfNotPresent("Detected <style> element with invalid image URL \"" + url + "\" -- " + e.getMessage());
                }
            }
        }
    }

    private void checkLinkedCss() {
        for (WebElement link : _webPage.findElements(By.tagName("link"))) {
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
                    checkCssResourceAsync(href, href, _baseUrl, charset);
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

    private void checkFavicon() {
        try {
            checkImageUrl(_faviconUrl, "Detected invalid favicon URL \"" + _faviconUrl + "\"");
        } catch (MalformedURLException e) {
            addLayoutBugIfNotPresent("Detected invalid favicon URL \"" + _faviconUrl + "\" -- " + e.getMessage());
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
                String url = extractUrlsFrom(temp).iterator().next();
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
    private Set<String> extractUrlsFrom(String css) {
        final Set<String> urls = new HashSet<String>();
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
            // If it is a @font-face src:url (see http://code.google.com/p/fighting-layout-bugs/issues/detail?id=9) ...
            try {
                j = css.lastIndexOf("{", i);
                while (j > 0 && isWhitespace(css.charAt(j - 1))) {
                    --j;
                }
                if (j >= 10 && "@font-face".equals(css.substring(j - 10, j))) {
                    // ... ignore it, otherwise ...
                } else {
                    urls.add(url);
                }
            } catch (StringIndexOutOfBoundsException e) {
                System.out.println("j: " + css.lastIndexOf("{", i) + ", css.length(): " + css.length());
            }
            i = css.indexOf("url(", k);
        }
        return urls;
    }

    private void checkImageUrl(String url, final String errorDescriptionPrefix) throws MalformedURLException {
        if (url.startsWith("data:")) {
            checkDataUrl(url);
        } else {
            URL completeUrl = getCompleteUrlFor(url);
            checkImageUrlAsync(completeUrl, errorDescriptionPrefix);
        }
    }

    private void checkImageUrl(URL baseUrl, String url, final String errorDescriptionPrefix) throws MalformedURLException {
        if (url.startsWith("data:")) {
            checkDataUrl(url);
        } else {
            URL completeUrl = getCompleteUrlFor(baseUrl, url);
            checkImageUrlAsync(completeUrl, errorDescriptionPrefix);
        }
    }

    private void checkDataUrl(String url) throws MalformedURLException {
        if (!url.startsWith("data:image/")) {
            throw new MalformedURLException("Data URL does not contain image data.");
        }
        // TODO: check if the data URL contains a valid image.
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

    private void checkImageUrlAsync(URL completeUrl, final String errorDescriptionPrefix) throws MalformedURLException {
        final String completeUrlAsString = completeUrl.toExternalForm();
        String error = _checkedImageUrls.putIfAbsent(completeUrlAsString, "");
        if (error == null) {
            _mockBrowser.downloadAsync(completeUrl, new DownloadCallback() {
                @Override
                public void onSuccess(GetMethod getMethod) {
                    if (getMethod.getStatusCode() >= 400) {
                        if (getMethod.getStatusCode() == 401) {
                            LOG.info("Ignoring HTTP response status code 401 (" + getMethod.getStatusText() + ") for image URL " + completeUrlAsString);
                        } else {
                            handleError("HTTP server responded with: " + getMethod.getStatusCode() + " " + getMethod.getStatusText());
                        }
                    } else {
                        final Header contentTypeHeader = getMethod.getResponseHeader("Content-Type");
                        if (contentTypeHeader == null) {
                            handleError("HTTP response did not contain Content-Type header.");
                        } else {
                            final String contentType = contentTypeHeader.getValue();
                            if (!contentType.startsWith("image/")) {
                                handleError("Content-Type HTTP response header \"" + contentType + "\" does not start with \"image/\".");
                            } else {
                                // TODO: check if the response body is a valid image
                            }
                        }
                    }
                }

                @Override
                public void onFailure(IOException e) {
                    handleError("HTTP GET failed: " + e.getMessage());
                }

                private void handleError(String error) {
                    _checkedImageUrls.put(completeUrlAsString, error);
                    addLayoutBugIfNotPresent(errorDescriptionPrefix + " -- " + error);
                }
            });
        } else if (error.length() > 0) {
            addLayoutBugIfNotPresent(errorDescriptionPrefix + " -- " + error);
        }
    }

    private void checkCssResourceAsync(final String pathToCssResource, String url, URL baseUrl, final String fallBackCharset) {
        try {
            final URL cssUrl = getCompleteUrlFor(baseUrl, url);
            if (_checkedCssUrls.add(cssUrl.toExternalForm())) {
                _mockBrowser.downloadAsync(cssUrl, new DownloadCallback() {
                    @Override
                    public void onSuccess(GetMethod getMethod) {
                        final Css css = getCssFrom(getMethod, cssUrl, fallBackCharset);
                        if (css.text != null) {
                            for (String importUrl : getImportUrlsFrom(css.text)) {
                                checkCssResourceAsync(importUrl + " (imported from " + pathToCssResource + ")", importUrl, cssUrl, css.charset);
                            }
                            for (String url : extractUrlsFrom(css.text)) {
                                try {
                                    checkImageUrl(cssUrl, url, "Detected invalid image URL \"" + url + "\" in " + pathToCssResource);
                                } catch (MalformedURLException e) {
                                    addLayoutBugIfNotPresent("Detected invalid image URL \"" + url + "\" in " + pathToCssResource);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(IOException e) {
                        LOG.error("Could not get CSS from " + pathToCssResource + ".", e);
                    }
                });
            }
        } catch (MalformedURLException e) {
            LOG.error("Could not get CSS from " + pathToCssResource + ".", e);
        }
    }

    /**
     * @param externallySpecifiedCharset the charset from the charset attribute of a &lt;link&gt; attribute if present,
     *                                   otherwise the charset of the refering style sheet or document.
     */
    private Css getCssFrom(GetMethod getMethod, URL cssUrl, String externallySpecifiedCharset) {
        final Css result = new Css();
        if (getMethod.getStatusCode() >= 400) {
            LOG.error("Could not get CSS from " + cssUrl + " -- HTTP server responded with: " + getMethod.getStatusCode() + " " + getMethod.getStatusText());
        } else {
            InputStream in = null;
            try {
                in = getMethod.getResponseBodyAsStream();
                Utf8BomAwareByteArrayOutputStream out = new Utf8BomAwareByteArrayOutputStream();
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
                        LOG.error("Could not get CSS from " + cssUrl, e);
                    }
                }
            } catch (IOException e) {
                LOG.error("Could not get CSS from " + cssUrl, e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        return result;
    }

    private void addLayoutBugIfNotPresent(String description) {
        // noinspection SynchronizeOnNonFinalField
        synchronized (_layoutBugs) {
            for (LayoutBug layoutBug : _layoutBugs) {
                if (description.equals(layoutBug.getDescription())) {
                    return;
                }
            }
            boolean saveScreenshot;
            if (_screenshotTaken) {
                saveScreenshot = false;
            } else {
                _screenshotTaken = (saveScreenshot = true);
            }
            _layoutBugs.add(createLayoutBug(description, _webPage, saveScreenshot));
        }
    }

    private interface DownloadCallback {
        void onSuccess(GetMethod getMethod);
        void onFailure(IOException e);
    }

    private class MockBrowser {
        private final HttpClient _httpClient;
        private final ExecutorService _threadPool;
        private final AtomicInteger _downloads = new AtomicInteger(0);

        public MockBrowser(HttpClient httpClient) {
            _httpClient = httpClient;
            HttpConnectionManager connectionManager = httpClient.getHttpConnectionManager();
            if (connectionManager instanceof MultiThreadedHttpConnectionManager) {
                _threadPool = Executors.newFixedThreadPool(10);
            } else {
                LOG.warn("The configured HttpClient does not use a MultiThreadedHttpConnectionManager, will only use 1 thread (instead of 10) for downloading CSS files and checking image URLs ...");
                _threadPool = Executors.newFixedThreadPool(1);
            }
            HttpState httpState = new HttpState();
            WebDriver driver = _webPage.getDriver();
            for (org.openqa.selenium.Cookie cookie : driver.manage().getCookies()) {
                httpState.addCookie(new Cookie(cookie.getDomain(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getExpiry(), cookie.isSecure()));
            }
            _httpClient.setState(httpState);
        }

        public void downloadAsync(URL url, final DownloadCallback callBack) {
            try {
                final GetMethod getMethod = new GetMethod(url.toURI().toString());
                getMethod.setFollowRedirects(true);
                _downloads.incrementAndGet();
                boolean downloadSubmitted = false;
                try {
                    _threadPool.submit(new Runnable() { @Override public void run() {
                        try {
                            _httpClient.executeMethod(getMethod);
                            try {
                                callBack.onSuccess(getMethod);
                            } catch (Throwable t) {
                                LOG.error("Unexpected exception while handling HTTP response for " + getMethod, t);
                            }
                        } catch (IOException e) {
                            try {
                                callBack.onFailure(e);
                            } catch (Throwable t) {
                                LOG.error("Unexpected exception while handling IOException for " + getMethod, t);
                            }
                        } finally {
                            try {
                                getMethod.releaseConnection();
                            } catch (Throwable t) {
                                LOG.error("Failed to release connection of " + getMethod, t);
                            } finally {
                                _downloads.decrementAndGet();
                            }
                        }
                    }});
                    downloadSubmitted = true;
                } finally {
                    if (!downloadSubmitted) {
                        _downloads.decrementAndGet();
                    }
                }
            } catch (URISyntaxException e) {
                // TODO: how can we check the url?
                LOG.info("Ignoring URL " + url + " -- it can not be checked with Apache HttpClient.");
            }
        }

        public void waitUntilAllDownloadsAreFinished() {
            while (_downloads.get() > 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Got interrupted while waiting for all downloads to finish.", e);
                }
            }
        }

        public void dispose() {
            _threadPool.shutdown();
        }
    }

    private static class Css {
        public String charset;
        public String text;
    }
}
