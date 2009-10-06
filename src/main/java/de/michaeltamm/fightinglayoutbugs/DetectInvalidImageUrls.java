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

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import static java.lang.Character.isWhitespace;

/**
 * @author Michael Tamm
 */
public class DetectInvalidImageUrls extends AbstractLayoutBugDetector {

    /**
     * <code>""</code> as value means the URL is valid, otherwise
     * the value contains the error message for the URL.
     */
    private final Map<URL, String> _checkedUrls = new HashMap<URL, String>();

    private URL _baseUrl;
    private HttpClient _httpClient;

    public Collection<LayoutBug> findLayoutBugs(FirefoxDriver driver) throws Exception {
        // Determine base URL for completion of relative URLs ...
        final String currentUrl = driver.getCurrentUrl();
        try {
            _baseUrl = new URL(currentUrl);
        } catch (MalformedURLException e) {
            // Should never happen.
            throw new RuntimeException("Could not convert " + currentUrl + " into an URL.", e);
        }
        try {
            _httpClient = new HttpClient();
            try {
                final List<LayoutBug> layoutBugs = new ArrayList<LayoutBug>();
                // 1. Check the src attribute of all <img> elements ...
                checkImgElements(driver, layoutBugs);
                // 2. Check the style attribute of all elements ...
                checkStyleAttributes(driver, layoutBugs);
                // TODO: 3. Check all <style> elements ...
                // TODO: 4. Check all linked CSS resources ...
                // TODO: 5. Check favicon ...
                return layoutBugs;
            } finally {
                _httpClient = null;
            }
        } finally {
            _baseUrl = null;
        }
    }

    private void checkImgElements(FirefoxDriver driver, List<LayoutBug> layoutBugs) {
        int numImgElementsWithoutSrcAttribute = 0;
        int numImgElementsWithEmptySrcAttribute = 0;
        final Set<String> seen = new HashSet<String>();
        for (WebElement img : driver.findElements(By.tagName("img"))) {
            final String src = img.getAttribute("src");
            if (src == null) {
                ++numImgElementsWithoutSrcAttribute;
            } else if ("".equals(src)) {
                ++numImgElementsWithEmptySrcAttribute;
            } else {
                if (!seen.contains(src)) {
                    try {
                        final URL url = getCompleteUrlFor(src);
                        final String error = checkImageUrl(url);
                        if (error.length() > 0) {
                            layoutBugs.add(createLayoutBug("Detected <img> element with invalid src attribute \"" + src + "\" - " + error, driver));
                        }
                    } catch (MalformedURLException e) {
                        layoutBugs.add(createLayoutBug("Detected <img> element with invalid src attribute \"" + src + "\" - " + e.getMessage(), driver));
                    }
                    seen.add(src);
                }
            }
        }
        if (numImgElementsWithEmptySrcAttribute > 0) {
            if (numImgElementsWithEmptySrcAttribute == 1) {
                layoutBugs.add(createLayoutBug("Detected <img> element with empty src attribute.", driver));
            } else {
                layoutBugs.add(createLayoutBug("Detected " + numImgElementsWithEmptySrcAttribute + " <img> elements with empty src attribute.", driver));
            }
        }
        if (numImgElementsWithoutSrcAttribute > 0) {
            if (numImgElementsWithEmptySrcAttribute == 1) {
                layoutBugs.add(createLayoutBug("Detected <img> without src attribute.", driver));
            } else {
                layoutBugs.add(createLayoutBug("Detected " + numImgElementsWithoutSrcAttribute + " <img> elements without src attribute.", driver));
            }
        }
    }

    private void checkStyleAttributes(FirefoxDriver driver, List<LayoutBug> layoutBugs) {
        for (WebElement element : driver.findElements(By.xpath("//*[@style]"))) {
            final String css = element.getAttribute("style");
            for (String url : extractImageUrlsFrom(css).keySet()) {
                try {
                    final String error = checkImageUrl(getCompleteUrlFor(url));
                    if (error.length() > 0) {
                        layoutBugs.add(createLayoutBug("Detected <" + element.getTagName() + "> element with invalid image URL \"" + url + "\" in its style attribute - " + error, driver));
                    }
                } catch (MalformedURLException e) {
                    layoutBugs.add(createLayoutBug("Detected <" + element.getTagName() + "> element with invalid image URL \"" + url + "\" in its style attribute - " + e.getMessage(), driver));
                }
            }
        }
    }

    private Map<String, Integer> extractImageUrlsFrom(String css) {
        final ConcurrentMap<String, Integer> imageUrls = new ConcurrentHashMap<String, Integer>();
        final int n = css.length();
        int i = css.indexOf("url(");
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
     * Returns <code>""</code> if the given URL is a valid image URL,
     * otherwise an error message is returned.
     */
    protected String checkImageUrl(URL url) {
        String error = _checkedUrls.get(url);
        if (error == null) {
            final GetMethod getMethod = new GetMethod(url.toExternalForm());
            getMethod.setFollowRedirects(true);
            try {
                _httpClient.executeMethod(getMethod);
                if (getMethod.getStatusCode() >= 400) {
                    error = "HTTP GET responded with: " + getMethod.getStatusCode() + " " + getMethod.getStatusText();
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
        final URL completeUrl;
        if (hasProtocol(url)) {
            completeUrl = new URL(url);
        } else {
            completeUrl = new URL(_baseUrl, url);
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
