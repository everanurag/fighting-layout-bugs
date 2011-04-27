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

import com.thoughtworks.selenium.DefaultSelenium;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Tamm
 */
public enum TestWebPageFactory {

    UsingChromeDriver {
        @Override
        protected WebPageCreator getCreator() {
            LOG.info("Creating ChromeDriver ...");
            WebDriver driver = new ChromeDriver();
            LOG.info("ChromeDriver created.");
            return new WebPageCreatorUsingWebDriver(driver);
        }
    },
    UsingFirefoxDriver {
        @Override
        protected WebPageCreator getCreator() {
            LOG.info("Creating FirefoxDriver ...");
            File firefoxExecutable = FirefoxHelper.findFirefoxExecutable();
            WebDriver driver = new FirefoxDriver(new FirefoxBinary(firefoxExecutable), null);
            LOG.info("FirefoxDriver created.");
            return new WebPageCreatorUsingWebDriver(driver);
        }
    },
    UsingInternetExplorerDriver {
        @Override
        protected WebPageCreator getCreator() {
            LOG.info("Creating InternetExplorerDriver ...");
            WebDriver driver = new InternetExplorerDriver();
            LOG.info("InternetExplorerDriver created.");
            return new WebPageCreatorUsingWebDriver(driver);
        }
    },
    UsingDefaultSeleniumWithFirefox {
        @Override
        protected WebPageCreator getCreator() {
            File firefoxExecutable = FirefoxHelper.findFirefoxExecutable();
            return new WebPageCreatorUsingDefaultSelenium("Firefox", "*firefox", "executablePath=" + firefoxExecutable.getAbsolutePath());
        }
    };

    private static final Logger LOG = Logger.getLogger(TestWebPageFactory.class);

    private interface WebPageCreator {
        WebPage createWebPageFor(URL url);
        void destroy();
    }

    private static class WebPageCreatorUsingWebDriver implements WebPageCreator {
        private WebDriver driver;

        private WebPageCreatorUsingWebDriver(WebDriver driver) {
            this.driver = driver;
        }

        @Override
        public WebPage createWebPageFor(URL url) {
            driver.get(url.toExternalForm());
            return new WebPageBackedByWebDriver(driver);
        }

        @Override
        public void destroy() {
            try {
                String driverName = driver.getClass().getSimpleName();
                LOG.info("Destroying " + driverName + " ...");
                driver.quit();
                LOG.info(driverName + " destroyed.");
            } finally {
                driver = null;
            }
        }
    }

    private static class WebPageCreatorUsingDefaultSelenium implements WebPageCreator {
        private String browser;
        private String browserStartCommand;
        private String optionsString;
        private SeleniumServer seleniumServer;
        private int seleniumServerPort;
        private DefaultSelenium defaultSelenium;
        private URL lastUrl;

        private WebPageCreatorUsingDefaultSelenium(String browser,  String browserStartCommand, String optionsString) {
            this.browser = browser;
            this.browserStartCommand = browserStartCommand;
            this.optionsString = optionsString;
            LOG.info("Starting SeleniumServer ...");
            RemoteControlConfiguration config = new RemoteControlConfiguration();
            seleniumServerPort = SocketHelper.findFreePort();
            config.setPort(seleniumServerPort);
            try {
                seleniumServer = new SeleniumServer(config);
            } catch (Exception e) {
                throw new RuntimeException("Could not create SeleniumServer.", e);
            }
            try {
                seleniumServer.start();
            } catch (Exception e) {
                throw new RuntimeException("Could not start SeleniumServer.", e);
            }
            LOG.info("SeleniumServer started.");
        }

        @Override
        public WebPage createWebPageFor(URL url) {
            if (lastUrl != null && !StringHelper.equals(lastUrl.getHost(), url.getHost())) {
                destroyDefaultSelenium();
            }
            if (defaultSelenium == null) {
                LOG.info("Starting DefaultSelenium with " + browser + " for " + url.getProtocol() + "://" + url.getHost() + " ...");
                defaultSelenium = new DefaultSelenium("localhost", seleniumServerPort, browserStartCommand, url.toExternalForm());
                defaultSelenium.start(optionsString);
                LOG.info("DefaultSelenium started.");
            }
            defaultSelenium.open(url.toExternalForm());
            lastUrl = url;
            return new WebPageBackedBySelenium(defaultSelenium);
        }

        private void destroyDefaultSelenium() {
            if (defaultSelenium != null) {
                try {
                    LOG.info("Stopping DefaultSelenium ...");
                    defaultSelenium.stop();
                    LOG.info("DefaultSelenium stopped.");
                } finally {
                    defaultSelenium = null;
                }
            }
        }

        @Override
        public void destroy() {
            try {
                destroyDefaultSelenium();
            } finally {
                try {
                    LOG.info("Stopping SeleniumServer ...");
                    seleniumServer.stop();
                    LOG.info("SeleniumServer stopped.");
                } finally {
                    seleniumServer = null;
                }
            }
        }
    }

    private static TestWebPageFactory frozenFactory;
    private static TestWebPageFactory lastFactory;
    private static WebPageCreator creatorFromLastFactory;

    public static void disposeLastFactory() {
        if (frozenFactory == null) {
            if (lastFactory != null) {
                try {
                    if (creatorFromLastFactory != null) {
                        try {
                            creatorFromLastFactory.destroy();
                        } finally {
                            creatorFromLastFactory = null;
                        }
                    }
                } finally {
                    lastFactory = null;
                }
            }
        }
    }

    protected abstract WebPageCreator getCreator();

    /**
     * All calls to {@link #createFor(String)} will use this TestWebFactory until {@link #melt()} is called.
     */
    public void freeze() {
        disposeLastFactory();
        frozenFactory = this;
    }

    public void melt() {
        frozenFactory = null;
        disposeLastFactory();
    }

    /**
     * @param pathToHtmlPageOrCompleteUrl either the path to a HTML page relative to the <code>src/test/webapp</code> directory or a complete URL
     */
    public WebPage createFor(String pathToHtmlPageOrCompleteUrl) {
        URL absoluteUrl = makeAbsolute(pathToHtmlPageOrCompleteUrl);
        TestWebPageFactory factory = (frozenFactory == null ? this : frozenFactory);
        if (lastFactory != null && lastFactory != factory) {
            disposeLastFactory();
        }
        if (lastFactory == null) {
            lastFactory = factory;
            creatorFromLastFactory = lastFactory.getCreator();
        }
        return creatorFromLastFactory.createWebPageFor(absoluteUrl);
    }

    private URL makeAbsolute(String pathToHtmlPageOrCompleteUrl) {
        URL absoluteUrl;
        try {
            if (isAbsolute(pathToHtmlPageOrCompleteUrl)) {
                absoluteUrl = new URL(pathToHtmlPageOrCompleteUrl);
            } else {
                final URL baseUrl = new URL("http://localhost:" + TestWebServer.getPort() + "/");
                absoluteUrl = new URL(baseUrl, pathToHtmlPageOrCompleteUrl);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return absoluteUrl;
    }

    private boolean isAbsolute(String url) {
        try {
            String host = new URL(url).getHost();
            return (host != null);
        } catch (MalformedURLException ignored) {
            return false;
        }
    }
}
