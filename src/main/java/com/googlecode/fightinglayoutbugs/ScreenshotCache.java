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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.primitives.Bytes.asList;

public class ScreenshotCache {

    public enum Condition {
        UNMODIFIED(null),
        WITH_ALL_TEXT_WHITE("#ffffff"),
        WITH_ALL_TEXT_BLACK("#000000"),
        WITH_ALL_TEXT_TRANSPARENT ("transparent");

        private final String textColor;

        private Condition(String textColor) {
            this.textColor = textColor;
        }
    }

    private static List<Byte> PNG_SIGNATURE = asList(new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

    private final WebPage _webPage;
    private final Map<Condition, WeakReference<Screenshot>> _cache = new HashMap<Condition, WeakReference<Screenshot>>();
    private boolean _textColorsBackedUp;
    private String _currentTextColor;

    public ScreenshotCache(WebPage webPage) {
        _webPage = webPage;
    }

    @Nullable
    public Screenshot getScreenshot(Condition condition) {
        WeakReference<Screenshot> weakReference = _cache.get(condition);
        Screenshot result = (weakReference == null ? null : weakReference.get());
        if (result == null) {
            result = takeScreenshot(condition);
            _cache.put(condition, new WeakReference<Screenshot>(result));
        }
        return result;
    }

    /**
     * Bypasses the cache and always takes a screenshot.
     */
    public Screenshot takeScreenshot(Condition condition) {
        if (condition == Condition.UNMODIFIED) {
            if (_currentTextColor != null) {
                restoreTextColors();
                _currentTextColor = null;
            }
        } else {
            if (!condition.textColor.equals(_currentTextColor)) {
                colorAllText(condition.textColor);
                _currentTextColor = condition.textColor;
            }
        }
        return takeScreenshot();
    }

    void colorAllText(@Nonnull String color) {
        if (!_textColorsBackedUp) {
            _webPage.injectJQueryIfNotPresent();
            _webPage.executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.attr('flb_color_backup', j.css('color')); }).size();"); // ... the trailing ".size()" will reduce the size of the response
            _textColorsBackedUp = true;
        }
        _webPage.executeJavaScript("jQuery('*').css('color', '" + color + "').size();"); // ... the trailing ".size()" will reduce the size of the response
    }

    void restoreTextColors() {
        if (!_textColorsBackedUp) {
            throw new IllegalStateException("text colors have not been backed up.");
        }
        _webPage.executeJavaScript("jQuery('*').each(function() { var j = jQuery(this); j.css('color', j.attr('flb_color_backup')); }).size();"); // ... the trailing ".size()" will reduce the size of the response
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
