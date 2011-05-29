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

import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.*;

import java.util.List;

import static com.google.common.primitives.Bytes.asList;

/**
 * @author Michael Tamm
 */
public class WebPageBackedByWebDriver extends WebPage {

    private static List<Byte> PNG_SIGNATURE = asList(new byte[]{ (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

    private final WebDriver _driver;

    public WebPageBackedByWebDriver(WebDriver driver) {
        _driver = driver;
    }

    public WebDriver getDriver() {
        return _driver;
    }

    public List<WebElement> findElements(By by) {
        return _driver.findElements(by);
    }

    protected Object executeJavaScript(String javaScript, Object...  arguments) {
        if (_driver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) _driver).executeScript(javaScript, arguments);
        } else {
            throw new UnsupportedOperationException("Can't execute JavaScript via " + _driver.getClass().getSimpleName());
        }
    }

    protected String retrieveUrl() {
        return _driver.getCurrentUrl();
    }

    protected String retrieveHtml() {
        return _driver.getPageSource();
    }

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
}
