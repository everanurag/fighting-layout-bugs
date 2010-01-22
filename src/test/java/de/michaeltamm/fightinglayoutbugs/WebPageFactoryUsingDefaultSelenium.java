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

import com.thoughtworks.selenium.DefaultSelenium;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;

/**
 * @author Michael Tamm
 */
public class WebPageFactoryUsingDefaultSelenium extends WebPageFactory {

    private final SeleniumServer _seleniumServer;
    private final DefaultSelenium _defaultSelenium;

    public WebPageFactoryUsingDefaultSelenium(String webserverBaseUrl) {
        super(webserverBaseUrl);
        System.out.println("Creating SeleniumServer ...");
        RemoteControlConfiguration config = new RemoteControlConfiguration();
        int port = SocketHelper.findFreePort();
        config.setPort(port);
        try {
            _seleniumServer = new SeleniumServer(config);
        } catch (Exception e) {
            throw new RuntimeException("Could not create SeleniumServer.", e);
        }
        try {
            _seleniumServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start SeleniumServer.", e);
        }
        System.out.println("Creating DefaultSelenium ...");
        _defaultSelenium = new DefaultSelenium("localhost", port, "*firefox", webserverBaseUrl);
        String options = "executablePath=" + FirefoxHelper.findFirefoxExecutable().getAbsolutePath();
        _defaultSelenium.start(options);
    }

    public WebPage createWebPageFor(String url) {
        String absoluteUrl = makeAbsolute(url);
        _defaultSelenium.open(absoluteUrl);
        return new WebPageBackedBySelenium(_defaultSelenium);
    }

    public void dispose() {
        try {
            System.out.println("Destroying DefaultSelenium ...");
            _defaultSelenium.stop();
        } finally {
            System.out.println("Destroying SeleniumServer ...");
            _seleniumServer.stop();
        }
    }
}
