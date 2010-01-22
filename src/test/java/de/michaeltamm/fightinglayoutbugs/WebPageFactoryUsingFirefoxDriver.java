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

import java.io.File;

/**
 * @author Michael Tamm
 */
public class WebPageFactoryUsingFirefoxDriver extends WebPageFactory {

    private final FirefoxDriver _driver;

    public WebPageFactoryUsingFirefoxDriver(String webserverBaseUrl) {
        super(webserverBaseUrl);
        System.out.println("Creating FirefoxDriver ...");
        File firefoxExecutable = FirefoxHelper.findFirefoxExecutable();
        _driver = new FirefoxDriver(new FirefoxBinary(firefoxExecutable), null);
    }

    public WebPage createWebPageFor(String pathToHtmlPageOrCompleteUrl) {
        String absoluteUrl = makeAbsolute(pathToHtmlPageOrCompleteUrl);
        _driver.get(absoluteUrl);
        return new WebPageBackedByWebDriver(_driver);
    }

    public void dispose() {
        System.out.println("Destroying FirefoxDriver ...");
        _driver.quit();
    }
}
