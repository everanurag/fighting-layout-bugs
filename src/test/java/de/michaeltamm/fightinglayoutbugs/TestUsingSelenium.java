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

import org.testng.annotations.AfterSuite;

/**
 * Base class for tests using <a href="http://selenium.googlecode.com">Selenium</a>,
 * which also takes care of starting and stopping a {@link Webserver} to serve the HTML pages
 * located under the <code>src/test/webapp</code> directory.
 *
 * @author Michael Tamm
 */
public class TestUsingSelenium {

    private static Webserver webserver;
    private static WebPageFactory webPageFactoryUsingChromeDriver;
    private static WebPageFactory webPageFactoryUsingDefaultSelenium;
    private static WebPageFactory webPageFactoryUsingFirefoxDriver;

    /**
     * Helper class for fluent API - see {@link TestUsingSelenium#getWebPageFor}.
     */
    protected static class SpecifyDriver {
        private final String _pathToHtmlPageOrCompleteUrl;

        private SpecifyDriver(String pathToHtmlPageOrCompleteUrl) {
            _pathToHtmlPageOrCompleteUrl = pathToHtmlPageOrCompleteUrl;
        }

/*
        public WebPage usingChromeDriver() {
            if (webPageFactoryUsingChromeDriver == null) {
                startWebserver();
                webPageFactoryUsingChromeDriver = new WebPageFactoryUsingChromeDriver(getBaseUrl());
            }
            return webPageFactoryUsingChromeDriver.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }
*/
        public WebPage usingDefaultSelenium() {
            if (webPageFactoryUsingDefaultSelenium == null) {
                startWebserver();
                webPageFactoryUsingDefaultSelenium = new WebPageFactoryUsingDefaultSelenium(getBaseUrl());
            }
            return webPageFactoryUsingDefaultSelenium.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }

        public WebPage usingFirefoxDriver() {
            if (webPageFactoryUsingFirefoxDriver == null) {
                startWebserver();
                webPageFactoryUsingFirefoxDriver = new WebPageFactoryUsingFirefoxDriver(getBaseUrl());
            }
            return webPageFactoryUsingFirefoxDriver.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }
    }

    /**
     * To get a {@link WebPage} in your test, you can either write<pre>
     * getWebPageFor("...").usingChromeDriver();</pre>
     * or <pre>
     * getWebPageFor("...").usingDefaultSelenium();</pre>
     * or <pre>
     * getWebPageFor("...").usingFirefoxDriver();</pre>
     *
     * @param pathToHtmlPageOrCompleteUrl either the path to a HTML page relative to the <code>src/test/webapp</code> directory or a complete URL
     */
    protected SpecifyDriver getWebPageFor(String pathToHtmlPageOrCompleteUrl) {
        return new SpecifyDriver(pathToHtmlPageOrCompleteUrl);
    }

    private static void startWebserver() {
        if (webserver == null) {
            System.out.println("Starting Webserver ...");
            webserver = new Webserver();
            webserver.start();
        }
    }

    private static void stopWebserver() {
        if (webserver != null) {
            System.out.println("Stopping Webserver ...");
            webserver.stop();
            webserver = null;
        }
    }

    private static String getBaseUrl() {
        return "http://localhost:" + webserver.getPort() + "/";
    }

    @AfterSuite
    public void tearDown() {
        if (webPageFactoryUsingChromeDriver != null) {
            webPageFactoryUsingChromeDriver.dispose();
            webPageFactoryUsingChromeDriver = null;
        }
        if (webPageFactoryUsingDefaultSelenium != null) {
            webPageFactoryUsingDefaultSelenium.dispose();
            webPageFactoryUsingDefaultSelenium = null;
        }
        if (webPageFactoryUsingFirefoxDriver != null) {
            webPageFactoryUsingFirefoxDriver.dispose();
            webPageFactoryUsingFirefoxDriver = null;
        }
        stopWebserver();
    }

}
