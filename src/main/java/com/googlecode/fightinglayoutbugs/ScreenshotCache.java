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

import com.googlecode.fightinglayoutbugs.helpers.ImageHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.primitives.Bytes.asList;

public class ScreenshotCache {

    private static final Log LOG = LogFactory.getLog(ScreenshotCache.class);

    public enum Condition {
        UNMODIFIED(null, false),
        WITH_NO_IMAGES(null, true),
        WITH_ALL_TEXT_WHITE("#ffffff", false),
        WITH_ALL_TEXT_BLACK("#000000", false),
        WITH_NO_IMAGES_AND_ALL_TEXT_WHITE("#ffffff", true),
        WITH_NO_IMAGES_AND_ALL_TEXT_BLACK("#000000", true),
        WITH_ALL_TEXT_TRANSPARENT ("transparent", false);

        final String textColor;
        final boolean hideImages;

        private Condition(String textColor, boolean hideImages) {
            this.textColor = textColor;
            this.hideImages = hideImages;
        }
    }

    private static List<Byte> PNG_SIGNATURE = asList(new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

    private final WebPage _webPage;
    private final Map<Condition, SoftReference<Screenshot>> _cache = new HashMap<Condition, SoftReference<Screenshot>>();
    private boolean _textColorsBackedUp;
    private boolean _imageUrlsBackedUp;
    private String _currentTextColor;
    private boolean _imagesAreCurrentlyHidden;

    public ScreenshotCache(WebPage webPage) {
        _webPage = webPage;
    }

    @Nonnull
    public Screenshot getScreenshot(Condition condition) {
        SoftReference<Screenshot> softReference = _cache.get(condition);
        Screenshot screenshot;
        if (softReference == null) {
            screenshot = takeScreenshot(condition);
            _cache.put(condition, new SoftReference<Screenshot>(screenshot));
        } else {
            screenshot = softReference.get();
            if (screenshot == null) {
                LOG.warn("Cached screenshot " + condition.name().toLowerCase().replace('_', ' ') + " was garbage collected, taking it again -- give the JVM more heap memory to speed up layout bug detection.");
                _cache.remove(condition);
                return getScreenshot(condition);
            }
        }
        return screenshot;
    }

    /**
     * Bypasses the cache and always takes a screenshot.
     */
    public Screenshot takeScreenshot(Condition condition) {
        // Handle text color ...
        if (condition.textColor != null && !condition.textColor.equals(_currentTextColor)) {
            colorAllText(condition.textColor);
            _currentTextColor = condition.textColor;
        } else if (condition.textColor == null && _currentTextColor != null) {
            restoreTextColors();
            _currentTextColor = null;
        }
        // Handle images ...
        if (condition.hideImages && !_imagesAreCurrentlyHidden) {
            hideImages();
            _imagesAreCurrentlyHidden = true;
        } else if (!condition.hideImages && _imagesAreCurrentlyHidden) {
            restoreImages();
            _imagesAreCurrentlyHidden = false;
        }
        return takeScreenshot();
    }

    void hideImages() {
        if (!_imageUrlsBackedUp) {
            _webPage.injectJQueryIfNotPresent();
            _webPage.executeJavaScript(
                "jQuery('*').each(function() {\n" +
                "    var $x = jQuery(this);\n" +
                "    var b = $x.css('background-image');\n" +
                "    if (b && b != 'none') {\n" +
                "        $x.data('flb_background-image_backup', b)\n" +
                "          .css('background-image', 'url(\"data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==\")');\n" +
                "    }\n" +
                "});\n" +
                "jQuery('img').each(function() {\n" +
                "    var $img = jQuery(this);\n" +
                "    var w = $img.width();\n" +
                "    var h = $img.height();\n" +
                "    $img.data('flb_src_backup', $img.attr('src'))\n" +
                "        .css('width', w + 'px')\n" +
                "        .css('height', h + 'px')\n" +
                "        .attr('src', 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==')\n" +
                "}).size();" // ... the trailing ".size()" will reduce the size of the response
            );
            _imageUrlsBackedUp = true;
        } else {
            _webPage.executeJavaScript(
                "jQuery('*').each(function() {\n" +
                "    var $x = jQuery(this);\n" +
                "    if ($x.data('flb_background-image_backup')) $x.css('background-image', 'url(\"data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==\")');\n" +
                "});\n" +
                "jQuery('img').each(function() {\n" +
                "    jQuery(this).attr('src', 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==')\n" +
                "}).size();" // ... the trailing ".size()" will reduce the size of the response
            );
        }
    }

    void restoreImages() {
        _webPage.executeJavaScript(
            "jQuery('*').each(function() {\n" +
            "    var $x = jQuery(this);\n" +
            "    var b = $x.data('flb_background-image_backup');\n" +
            "    if (b) $x.css('background-image', b);\n" +
            "});\n" +
            "\n" +
            "jQuery('img').each(function() {\n" +
            "    var $img = jQuery(this);\n" +
            "    $img.attr('src', $img.data('flb_src_backup'));\n" +
            "}).size();" // ... the trailing ".size()" will reduce the size of the response
        );
    }

    void colorAllText(@Nonnull String color) {
        if (!_textColorsBackedUp) {
            _webPage.injectJQueryIfNotPresent();
            _webPage.executeJavaScript("jQuery('*').each(function() { var $x = jQuery(this); $x.data('flb_color_backup', $x.css('color')); }).size();"); // ... the trailing ".size()" will reduce the size of the response
            _textColorsBackedUp = true;
        }
        _webPage.executeJavaScript("jQuery('*').css('color', '" + color + "').size();"); // ... the trailing ".size()" will reduce the size of the response
    }

    void restoreTextColors() {
        _webPage.executeJavaScript("jQuery('*').each(function() { var $x = jQuery(this); $x.css('color', $x.data('flb_color_backup')); }).size();"); // ... the trailing ".size()" will reduce the size of the response
    }

    protected Screenshot takeScreenshot() {
        WebDriver driver = _webPage.getDriver();
        if (driver instanceof TakesScreenshot) {
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            if (bytes == null) {
                throw new RuntimeException(driver.getClass().getName() + ".getScreenshotAs(OutputType.BYTES) returned null.");
            }
            if (bytes.length < 8) {
                throw new RuntimeException(driver.getClass().getName() + ".getScreenshotAs(OutputType.BYTES) did not return a PNG image.");
            } else {
                // Workaround for http://code.google.com/p/selenium/issues/detail?id=1686 ...
                if (!asList(bytes).subList(0, 8).equals(PNG_SIGNATURE)) {
                    bytes = Base64.decodeBase64(bytes);
                }
                if (!asList(bytes).subList(0, 8).equals(PNG_SIGNATURE)) {
                    throw new RuntimeException(driver.getClass().getName() + ".getScreenshotAs(OutputType.BYTES) did not return a PNG image.");
                }
            }
            int[][] pixels = ImageHelper.pngToPixels(bytes);
            return new Screenshot(pixels);
        } else {
            throw new UnsupportedOperationException(driver.getClass().getName() + " does not support taking screenshots.");
        }
    }
}
