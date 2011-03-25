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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base class for tests using <a href="http://selenium.googlecode.com">Selenium</a>,
 * which also takes care of starting and stopping a {@link TestWebServer} to serve
 * the HTML pages located under the <code>src/test/webapp</code> directory.
 *
 * @author Michael Tamm
 */
public class TestUsingSelenium {

    private static TestWebServer testWebServer;
    private static WebPageFactory webPageFactory;

    @AfterSuite
    public void tearDown() {
        disposeWebPageFactoryIfNeeded();
        stopWebserverIfNeeded();
    }

    /**
     * Helper class for fluent API - see {@link TestUsingSelenium#getWebPageFor}.
     */
    protected static class GetWebPage {
        private final String _pathToHtmlPageOrCompleteUrl;

        private GetWebPage(String pathToHtmlPageOrCompleteUrl) {
            _pathToHtmlPageOrCompleteUrl = pathToHtmlPageOrCompleteUrl;
        }

        public WebPage usingChromeDriver() {
            if (!(webPageFactory instanceof WebPageFactoryUsingChromeDriver)) {
                disposeWebPageFactoryIfNeeded();
                webPageFactory = new WebPageFactoryUsingChromeDriver();
            }
            return webPageFactory.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }

        public WebPage usingDefaultSelenium() {
            if (!(webPageFactory instanceof WebPageFactoryUsingDefaultSelenium)) {
                disposeWebPageFactoryIfNeeded();
                webPageFactory = new WebPageFactoryUsingDefaultSelenium();
            }
            return webPageFactory.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }

        public WebPage usingFirefoxDriver() {
            if (!(webPageFactory instanceof WebPageFactoryUsingFirefoxDriver)) {
                disposeWebPageFactoryIfNeeded();
                webPageFactory = new WebPageFactoryUsingFirefoxDriver();
            }
            return webPageFactory.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }

        public WebPage usingInternetExplorerDriver() {
            if (!(webPageFactory instanceof WebPageFactoryUsingInternetExplorerDriver)) {
                disposeWebPageFactoryIfNeeded();
                webPageFactory = new WebPageFactoryUsingInternetExplorerDriver();
            }
            return webPageFactory.createWebPageFor(_pathToHtmlPageOrCompleteUrl);
        }
    }

    /**
     * To get a {@link WebPage} in your test, you can either write<pre>
     * getWebPageFor("...").usingChromeDriver();</pre>
     * or <pre>
     * getWebPageFor("...").usingDefaultSelenium();</pre>
     * or <pre>
     * getWebPageFor("...").usingFirefoxDriver();</pre>
     * or <pre>
     * getWebPageFor("...").usingInternetExplorer();</pre>
     *
     * @param pathToHtmlPageOrCompleteUrl either the path to a HTML page relative to the <code>src/test/webapp</code> directory or a complete URL
     */
    protected GetWebPage getWebPageFor(String pathToHtmlPageOrCompleteUrl) {
        return new GetWebPage(pathToHtmlPageOrCompleteUrl);
    }

    static URL getBaseUrlForTestWebServer() {
        startWebserverIfNeeded();
        try {
            return new URL("http://localhost:" + testWebServer.getPort() + "/");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Should never happen.", e);
        }
    }


    private static void startWebserverIfNeeded() {
        if (testWebServer == null) {
            System.out.println("Starting TestWebServer ...");
            testWebServer = new TestWebServer();
            testWebServer.start();
        }
    }

    private static void disposeWebPageFactoryIfNeeded() {
        if (webPageFactory != null) {
            try {
                webPageFactory.dispose();
            } finally {
                webPageFactory = null;
            }
        }
    }

    private static void stopWebserverIfNeeded() {
        if (testWebServer != null) {
            try {
                System.out.println("Stopping TestWebServer ...");
                testWebServer.stop();
            } finally {
                testWebServer = null;
            }
        }
    }
}
