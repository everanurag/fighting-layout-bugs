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

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;

/**
 * @author Michael Tamm
 */
public class TestUsingFirefoxDriver {

    protected FirefoxDriver _driver;

    @Before
    public void createFirefoxDriver() {
        File firefoxExe = null;
        if (System.getProperty("webdriver.firefox.bin") == null) {
            // Try to find the Firefox executable at places, which are not checked by WebDriver ...
            final File firefoxExeOnWindows = new File("C:\\Program Files (x86)\\Mozilla\\Firefox3\\firefox.exe");
            if (firefoxExeOnWindows.exists()) {
                firefoxExe = firefoxExeOnWindows;
            } else {
                final File firefoxExeOnLinux = new File("/usr/bin/firefox");
                if (firefoxExe.exists()) {
                    firefoxExe = firefoxExeOnLinux;
                }
            }
        }
        _driver = new FirefoxDriver(new FirefoxBinary(firefoxExe), null);
    }

    @After
    public void destroyFirefoxDriver() {
        _driver.quit();
    }

}
