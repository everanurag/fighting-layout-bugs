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

import com.googlecode.fightinglayoutbugs.helpers.JsonHelper;
import com.googlecode.fightinglayoutbugs.helpers.RectangularRegion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * <p>
 * Detects if there are elements on the analyzed web page, which
 * take the focus (when the user presses the TAB key several times)
 * but do not change their visual appearance when they got the focus.
 * </p><p>
 * This is actually a usability problem, because the user does not
 * see, which element is currently focused.
 * </p><p>
 * Attention: This detector is very slow, because it needs to take
 * a screenshot after each simulated press on the TAB key.
 * </p>
 */
public class DetectElementsWithInvisibleFocus extends AbstractLayoutBugDetector {

    private static final Log LOG = LogFactory.getLog(DetectElementsWithInvisibleFocus.class);
    private static final RectangularRegion NOT_DISPLAYED = new RectangularRegion(0, 0, 0, 0);

    @Override
    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) {
        Collection<LayoutBug> result = new ArrayList<LayoutBug>();
        // 1.) Focus first focusable element ...
        FocusedElement focusedElement1 = focusFirstElement(webPage);
        if (focusedElement1 != null) {
            Set<WebElement> visitedElements = new HashSet<WebElement>();
            visitedElements.add(focusedElement1.element);
            Screenshot screenshot1 = webPage.takeScreenshot();
            List<RectangularRegion> focusOrder = new ArrayList<RectangularRegion>();
            focusOrder.add(focusedElement1.region);
            // 2.) Focus next elements and compare screenshots (restrict detection to first 99 focusable elements) ...
            for (int i = 2; i <= 99 ; ++i) {
                FocusedElement focusedElement2 = focusNextElement(focusedElement1, webPage, visitedElements);
                if (focusedElement2 == null) {
                    break;
                }
                focusOrder.add(focusedElement2.region);
                Screenshot screenshot2 = webPage.takeScreenshot();
                if (i == 2 && focusedElement1.hasInvisibleFocus(screenshot1, screenshot2)) {
                    result.add(createLayoutBug(focusedElement1, focusOrder, webPage, screenshot1));
                }
                if (focusedElement2.hasInvisibleFocus(screenshot2, screenshot1)) {
                    result.add(createLayoutBug(focusedElement2, focusOrder, webPage, screenshot2));
                }
                screenshot1 = screenshot2;
                focusedElement1 = focusedElement2;
            }
        }
        return result;
    }

    @Nullable
    private FocusedElement focusFirstElement(WebPage webPage) {
        WebElement firstFocusedWebElement = getFocusedWebElement(webPage);
        if (firstFocusedWebElement == null) {
            // Try to focus first element ...
            try {
                WebDriver driver = webPage.getDriver();
                WebElement bodyElement = driver.findElement(By.tagName("body"));
                bodyElement.sendKeys(Keys.TAB);
            } catch (Exception e) {
                LOG.warn("Failed to focus first element.", e);
            }
            firstFocusedWebElement = getFocusedWebElement(webPage);
        } else if ("body".equals(firstFocusedWebElement.getTagName().toLowerCase())) {
            firstFocusedWebElement.sendKeys(Keys.TAB);
            firstFocusedWebElement = getFocusedWebElement(webPage);
        }
        if (firstFocusedWebElement != null && !"body".equals(firstFocusedWebElement.getTagName().toLowerCase())) {
            webPage.injectJQueryIfNotPresent();
            return toFocusedElement(firstFocusedWebElement, webPage);
        } else {
            return null;
        }
    }

    private WebElement getFocusedWebElement(WebPage webPage) {
        return (WebElement) webPage.executeJavaScript("return document.activeElement;");
    }

    private FocusedElement toFocusedElement(@Nonnull WebElement activeElement, WebPage webPage) {
        // I don't trust WebDriver, that's why I determine the offset, width and height with jQuery too ...
        @SuppressWarnings("unchecked")
        Map<String, Object> temp = (Map<String, Object>) JsonHelper.parse((String) webPage.executeJavaScript(
            "var $element = jQuery(arguments[0]);\n" +
            "var offset = $element.offset();\n" +
            "var $temp = $element.clone(false).wrap('<div></div>').parent();\n" +
            "try {\n" +
            "    return JSON.stringify({\n" +
            "        x: offset.left,\n" +
            "        y: offset.top,\n" +
            "        w: $element.width(),\n" +
            "        h: $element.height(),\n" +
            "        html: $temp.html()\n" +
            "    });\n" +
            "} finally {\n" +
            "    $temp.remove();\n" +
            "}", activeElement
        ));
        int x = ((Number) temp.get("x")).intValue();
        int y = ((Number) temp.get("y")).intValue();
        int w = ((Number) temp.get("w")).intValue();
        int h = ((Number) temp.get("h")).intValue();
        String html = (String) temp.get("html");
        if (activeElement.isDisplayed()) {
            Point location = activeElement.getLocation();
            Dimension size = activeElement.getSize();
            int x1 = Math.min(location.getX(), x);
            int y1 = Math.min(location.getY(), y);
            int x2 = Math.max(location.getX() + size.getWidth(), x + w) - 1;
            int y2 = Math.max(location.getY() + size.getHeight(), y + h) - 1;
            return new FocusedElement(activeElement, new RectangularRegion(x1, y1, x2, y2), html);
        } else {
            return new FocusedElement(activeElement, NOT_DISPLAYED, html);
        }
    }

    @Nullable
    private FocusedElement focusNextElement(FocusedElement focusedElement, WebPage webPage, Collection<WebElement> visitedElements) {
        focusedElement.element.sendKeys(Keys.TAB);
        final WebElement focusedWebElement = getFocusedWebElement(webPage);
        if (focusedWebElement != null && !visitedElements.contains(focusedWebElement) && !"body".equals(focusedWebElement.getTagName().toLowerCase())) {
            visitedElements.add(focusedWebElement);
            return toFocusedElement(focusedWebElement, webPage);
        } else {
            return null;
        }
    }

    private LayoutBug createLayoutBug(FocusedElement focusedElement, List<RectangularRegion> focusOrder, WebPage webPage, Screenshot screenshotWithFocus) {
        return createLayoutBug(
                "Detected element with invisible focus -- i.e. the element does not change its appearance when it gets the focus.\n" +
                        "- Element: " + focusedElement.html.replace("\n", "\n           ") + "\n" +
                        "- Region: " + focusedElement.region,
                webPage,
                screenshotWithFocus,
                new InvisibleFocusMarker(focusedElement, focusOrder)
        );
    }

    private static class FocusedElement {
        private final WebElement element;
        private final RectangularRegion region;
        private final String html;

        private FocusedElement(WebElement element, RectangularRegion region, String html) {
            this.element = element;
            this.region = region;
            this.html = html;
        }

        private boolean hasInvisibleFocus(Screenshot screenshotWithFocus, Screenshot screenshotWithoutFocus) {
            // Ignore text input fields, they should have a blinking cursor ...
            if (isTextInputField(element)) {
                return false;
            }
            // Ignore elements, which are not displayed ...
            if (!isDisplayed()) {
                return false;
            }
            // To prevent false alarms we extend the region to analyze by 4 pixels in each direction ...
            RectangularRegion regionToAnalyze = addBorder(region, 4, screenshotWithFocus);
            ScreenshotRegion screenshotRegionWithFocus = new ScreenshotRegion(screenshotWithFocus, regionToAnalyze);
            ScreenshotRegion screenshotRegionWithoutFocus = new ScreenshotRegion(screenshotWithoutFocus, regionToAnalyze);
            return screenshotRegionWithFocus.equals(screenshotRegionWithoutFocus);
        }

        private boolean isDisplayed() {
            return region != NOT_DISPLAYED;
        }

        private boolean isTextInputField(WebElement webElement) {
            String tagName = webElement.getTagName().toLowerCase();
            if ("input".equals(tagName)) {
                String typeAttribute = webElement.getAttribute("type");
                return (typeAttribute == null || typeAttribute.isEmpty() || "text".equals(typeAttribute) || "password".equals(typeAttribute));
            } else {
                return "textarea".equals(tagName);
            }
        }

        private RectangularRegion addBorder(RectangularRegion region, int border, Screenshot screenshot) {
            final int x1 = Math.max(0, region.x1 - border);
            final int y1 = Math.max(0, region.y1 - border);
            final int x2 = Math.max(screenshot.width - 1, region.x2 + border);
            final int y2 = Math.max(screenshot.height - 1, region.y2 + border);
            return new RectangularRegion(x1, y1, x2, y2);
        }
    }

    private static class InvisibleFocusMarker implements Marker {
        private final FocusedElement focusedElement;
        private final List<RectangularRegion> focusOrder;

        public InvisibleFocusMarker(FocusedElement data, List<RectangularRegion> focusOrder) {
            this.focusedElement = data;
            this.focusOrder = focusOrder;
        }

        @Override
        public void mark(int[][] screenshot) {
            final int w = screenshot.length;
            final int h = screenshot[0].length;
            // TODO: add numbers to indicate focus order
            for (int x = focusedElement.region.x1; x <= focusedElement.region.x2 && x < w; ++x) {
                for (int y = focusedElement.region.y1; y <= focusedElement.region.y2 && y < h; ++y) {
                    if ((x + y) % 2 == 0) {
                        screenshot[x][y] = 0xFF0000;
                    }
                }
            }
            // TODO: fade out unimportant areas
        }
    }
}
