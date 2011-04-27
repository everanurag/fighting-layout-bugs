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

package com.googlecode.fightinglayoutbugs;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import org.apache.commons.codec.binary.Base64;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.internal.FindsById;
import org.openqa.selenium.internal.FindsByTagName;
import org.openqa.selenium.internal.FindsByXPath;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.fightinglayoutbugs.StringHelper.asString;

/**
 * @author Sascha Schwarze, Michael Tamm
 */
public class WebPageBackedBySelenium extends WebPage {

    private final Selenium _selenium;

    private boolean _jsonInjected;

    public WebPageBackedBySelenium(Selenium selenium) {
        _selenium = selenium;
    }

    public Selenium getSelenium() {
        return _selenium;
    }

    public List<WebElement> findElements(By by) {
        return by.findElements(new SeleniumSearchContext());
    }

    protected Object executeJavaScript(String javaScript, Object... arguments) {
        StringBuilder sb = new StringBuilder();
        if (arguments.length > 0) {
            // We have to create an arguments array and make it visible in the current window ...
            sb.append("var arguments = new Array();\n");
            sb.append("selenium.browserbot.getCurrentWindow().arguments = arguments;\n");
            for (int i = 0; i < arguments.length; ++i) {
                Object argument = arguments[i];
                if (argument instanceof SeleniumWebElement) {
                    String locator = ((SeleniumWebElement) argument).getLocator().replaceAll("\"", "\\\\\"");
                    sb.append("arguments[").append(i).append("] = selenium.browserbot.findElement(\"").append(locator).append("\");\n");
                } else {
                    throw new IllegalArgumentException("The argument with the index " + i + " (" + asString(argument) + ") has an unsupported class (" + argument.getClass() + ").");
                }
            }
        }
        if (javaScript.startsWith("return")) {
            // Selenium.getEval(...) always returns a String, therefore we convert the return value into a JSON string first.
            // If the Prototype JavaScript framework is present just calling JSON.stringify(...) for an array leads to an
            // invalid JSON string, therefore we use Array.prototype.toJSON(...) if present ...
            injectJsonIfNeeded();
            javaScript = "var result = " + javaScript.substring("return".length()).trim() + "; result instanceof Array && Array.prototype.toJSON ? result.toJSON() : JSON.stringify(result)";
        }
        sb.append("with (selenium.browserbot.getCurrentWindow()) { ").append(javaScript).append(" }");
        String resultAsJsonString = _selenium.getEval(sb.toString());
        Object result = JsonHelper.parse(resultAsJsonString);
        return result;
    }

    private void injectJsonIfNeeded() {
        if (!_jsonInjected) {
            if (!_selenium.getEval("window.JSON").startsWith("[object")) {
                String json2 = readResource("json2.js");
                executeJavaScript(json2);
                if (!_selenium.getEval("window.JSON").startsWith("[object")) {
                    throw new RuntimeException("Failed to inject JSON.");
                }
            }
            _jsonInjected = true;
        }
    }

    protected String retrieveUrl() {
        return _selenium.getLocation();
    }

    protected String retrieveHtml() {
        return _selenium.getHtmlSource();
    }

    protected byte[] takeScreenshotAsPng() {
        String base64EncodedScreenshot = _selenium.captureEntirePageScreenshotToString("");
        byte[] base64EncodedBytes;
        try {
            base64EncodedBytes = base64EncodedScreenshot.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
        return Base64.decodeBase64(base64EncodedBytes);
    }

    private class SeleniumSearchContext implements FindsById, FindsByTagName, FindsByXPath, SearchContext {

        public WebElement findElement(By by) {
            String toString = by.toString();
            if (toString.startsWith("By.xpath")) {
                return findElementByXPath(toString.substring(10));
            } else if (toString.startsWith("By.id")) {
                return findElementById(toString.substring(7));
            } else if (toString.startsWith("By.tagName")) {
                return findElementByXPath("//" + toString.substring(12));
            } else {
                throw new UnsupportedOperationException("Not implemented (" + toString + ")");
            }
        }

        public WebElement findElementById(String using) {
            return new SeleniumWebElement(_selenium, "id=" + using);
        }

        public WebElement findElementByTagName(String using) {
            return new SeleniumWebElement(_selenium, "dom=document.getElementsByTagName(\"" + using + "\")[0]");
        }

        public WebElement findElementByXPath(String using) {
            return new SeleniumWebElement(_selenium, "xpath=" + using);
        }

        public List<WebElement> findElements(By by) {
            String toString = by.toString();
            if (toString.startsWith("By.xpath")) {
                return findElementsByXPath(toString.substring(10));
            } else if (toString.startsWith("By.id")) {
                List<WebElement> elements = new ArrayList<WebElement>(1);
                elements.add(findElementById(toString.substring(7)));
                return elements;
            } else if (toString.startsWith("By.tagName")) {
                return findElementsByXPath("//" + toString.substring(12));
            } else {
                throw new UnsupportedOperationException("Not implemented (" + toString + ")");
            }
        }

        public List<WebElement> findElementsById(String using) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public List<WebElement> findElementsByTagName(String using) {
            int count = _selenium.getXpathCount("//" + using).intValue();
            List<WebElement> elements = new ArrayList<WebElement>(count);
            for(int i = 0; i < count; i ++) {
                elements.add(new SeleniumWebElement(_selenium, "dom=document.getElementsByTagName(\"" + using + "\")[" + i + "]"));
            }
            return elements;
        }

        public List<WebElement> findElementsByXPath(String using) {
            int count = _selenium.getXpathCount(using).intValue();
            List<WebElement> elements = new ArrayList<WebElement>(count);
            for(int i = 1; i <= count; i ++) {
                if(i == 1) {
                    elements.add(new SeleniumWebElement(_selenium, using));
                } else {
                    elements.add(new SeleniumWebElement(_selenium, using + "[" + i + "]"));
                }
            }

            return elements;
        }
    }

    private class SeleniumWebElement implements WebElement {
        private final Selenium selenium;
        private final String locator;

        private SeleniumWebElement(Selenium selenium, String locator) {
            this.selenium = selenium;
            this.locator = locator;
        }

        public void clear() {
            selenium.type(locator, "");
        }

        public void click() {
            selenium.click(locator);
        }

        public WebElement findElement(By by) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public List<WebElement> findElements(By by) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public String getAttribute(String name) {
            try {
                return selenium.getAttribute(locator + "@" + name);
            } catch (SeleniumException e) {
                // SeleniumException thrown if attribute value is ""
                return "";
            }
        }

        private String getLocator() {
            return locator;
        }

        public String getTagName() {
            return selenium.getEval("selenium.browserbot.findElement(\"" + getLocator().replaceAll("\"", "\\\\\"") + "\").tagName;");
        }

        public String getText() {
            return selenium.getText(locator);
        }

        public String getValue() {
            return selenium.getValue(locator);
        }

        public boolean isEnabled() {
            return selenium.isEditable(locator);
        }

        public boolean isSelected() {
            return selenium.isChecked(locator);
        }

        public void sendKeys(CharSequence... keysToSend) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void setSelected() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void submit() {
            selenium.submit(locator);
        }

        public boolean toggle() {
            if(selenium.isChecked(locator)) {
                selenium.uncheck(locator);
                return false;
            } else {
                selenium.check(locator);
                return true;
            }
        }
    }
}
