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

import org.openqa.selenium.*;

import java.util.List;

/**
 * @author Michael Tamm
 */
public class WebPageBackedByWebDriver extends AbstractWebPage {

    private final WebDriver _driver;

    public WebPageBackedByWebDriver(WebDriver driver) {
        _driver = driver;
    }

    public List<WebElement> findElements(By by) {
        return _driver.findElements(by);
    }

    public Object executeJavaScript(String javaScript, Object...  arguments) {
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

    protected byte[] takeScreenshotAsBytes() {
        if (_driver instanceof TakesScreenshot) {
            return ((TakesScreenshot) _driver).getScreenshotAs(OutputType.BYTES);
        } else {
            throw new UnsupportedOperationException(_driver.getClass().getName() + " does not support taking screenshots.");
        }
    }

    protected void injectJQuery() {
        executeJavaScript(
            "if (typeof jQuery == 'undefined') {\n" +
            "    document.body.appendChild(document.createElement('script')).src = 'http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js';\n" +
            "}"
        );
    }
}
