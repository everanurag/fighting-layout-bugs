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

import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

import static de.michaeltamm.fightinglayoutbugs.StringHelper.asString;

/**
 * @author Michael Tamm
 */
public class TestUsingFirefoxDriver extends TestAccessingWebserver {

    private final static String[] FIREFOX_PATH_CANDIDATES = {
        "C:\\Program Files (x86)\\Mozilla\\Firefox3\\firefox.exe",
        "C:\\Program Files\\Mozilla Firefox\\firefox.exe",
        "/usr/bin/firefox"
    };

    protected static FirefoxDriver _driver;

    @BeforeSuite
    public void createFirefoxDriver() {
        System.out.println("Creating FirefoxDriver ...");
        File firefoxExe = null;
        if (System.getProperty("webdriver.firefox.bin") == null) {
            // Try to find the Firefox executable at places,
            // which are not checked by WebDriver ...
            for (String path : FIREFOX_PATH_CANDIDATES) {
                File temp = new File(path);
                if (temp.exists()) {
                    firefoxExe = temp;
                    break;
                }
            }
        }
        _driver = new FirefoxDriver(new FirefoxBinary(firefoxExe), null);
    }


    @AfterSuite
    public void destroyFirefoxDriver() {
        System.out.println("Destroying FirefoxDriver ...");
        _driver.quit();
    }

    protected String makeUrlAbsolute(String url) {
        String absoluteUrl;
        if (isAbsolute(url)) {
            absoluteUrl = url;
        } else {
            String baseUrl = getBaseUrl();
            if (url == null) {
                absoluteUrl = baseUrl;
            } else {
                URL context;
                try {
                    context = new URL(baseUrl);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid baseUrl: " + asString(baseUrl));
                }
                try {
                    absoluteUrl = new URL(context, url).toString();
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("Invalid url: " + asString(url));
                }
            }
        }
        return absoluteUrl;
    }

    /**
     * Returns <code>true</code> if the given <code>url</code> specifies a host.
     */
    private boolean isAbsolute(String url) {
        try {
            String host = new URL(url).getHost();
            return (host != null);
        } catch(MalformedURLException e) {
            return false;
        }
    }
}
