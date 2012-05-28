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

package com.googlecode.fightinglayoutbugs.helpers;

import org.openqa.selenium.browserlaunchers.locators.BrowserInstallation;
import org.openqa.selenium.browserlaunchers.locators.CombinedFirefoxLocator;
import org.openqa.selenium.firefox.internal.Executable;

import java.io.File;

public class FirefoxHelper {

    private static final String[] FIREFOX_EXECUTABLE_PATH_CANDIDATES = {
        "C:\\Program Files (x86)\\Mozilla\\Firefox3\\firefox.exe",
        "C:\\Program Files\\Mozilla Firefox\\firefox.exe",
        "/usr/bin/firefox"
    };

    public static File findFirefoxExecutable() {
        // Let's see if WebDriver can find it ...
        try {
            Executable executable = new Executable(null);
            return executable.getFile();
        } catch (Exception ignored) {}
        // Let's see if Selenium can find it ...
        try {
            final BrowserInstallation bi = new CombinedFirefoxLocator().findBrowserLocationOrFail();
            if (bi != null) {
                return new File(bi.launcherFilePath());
            }
        } catch (Exception ignored) {}
        // Let's see if we can find it ...
        for (String path : FIREFOX_EXECUTABLE_PATH_CANDIDATES) {
            File firefoxExecutable = new File(path);
            if (firefoxExecutable.exists()) {
                return firefoxExecutable;
            }
        }
        // That's bad ...
        throw new RuntimeException("Could not find Firefox executable.");
    }

    protected FirefoxHelper() {}
}
