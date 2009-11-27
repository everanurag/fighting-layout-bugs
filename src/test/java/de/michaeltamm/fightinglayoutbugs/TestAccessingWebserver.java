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

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

/**
 * @author Michael Tamm
 */
public class TestAccessingWebserver {

    static Webserver _webserver;

    @BeforeSuite
    public void startWebserver() {
        System.out.println("Starting Webserver ...");
        _webserver = new Webserver();
        _webserver.start();
    }

    @AfterSuite
    public void stopWebserver() {
        System.out.println("Stopping Webserver ...");
        _webserver.stop();
    }

    protected String getBaseUrl() {
        return "http://localhost:" + _webserver.getPort() + "/";
    }

}
