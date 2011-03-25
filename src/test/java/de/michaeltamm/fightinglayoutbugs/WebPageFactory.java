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

import static de.michaeltamm.fightinglayoutbugs.StringHelper.asString;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Tamm
 */
public abstract class WebPageFactory {

    /**
     * @param pathToHtmlPageOrCompleteUrl either the path to a HTML page relative to the <code>src/test/webapp</code> directory or a complete URL
     */
    public abstract WebPage createWebPageFor(String pathToHtmlPageOrCompleteUrl);

    public abstract void dispose();

    /**
     * @param pathToHtmlPageOrCompleteUrl either the path to a HTML page relative to the <code>src/test/webapp</code> directory or a complete URL
     */
    protected String makeAbsolute(String pathToHtmlPageOrCompleteUrl) {
        String absoluteUrl;
        if (isAbsolute(pathToHtmlPageOrCompleteUrl)) {
            absoluteUrl = pathToHtmlPageOrCompleteUrl;
        } else {
            final URL baseUrl = TestUsingSelenium.getBaseUrlForTestWebServer();
            try {
                absoluteUrl = new URL(baseUrl, pathToHtmlPageOrCompleteUrl).toString();
            } catch (MalformedURLException ignored) {
                throw new IllegalArgumentException("Invalid URL: " + asString(pathToHtmlPageOrCompleteUrl));
            }
        }
        return absoluteUrl;
    }

    /**
     * Returns <code>true</code> if the given <code>url</code> specifies a host.
     */
    protected boolean isAbsolute(String url) {
        try {
            String host = new URL(url).getHost();
            return (host != null);
        } catch(MalformedURLException ignored) {
            return false;
        }
    }
}
