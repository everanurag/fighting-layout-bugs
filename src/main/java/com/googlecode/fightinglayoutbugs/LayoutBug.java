/*
 * Copyright 2009-2011 Michael Tamm
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

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Represents a layout bug found by a {@link LayoutBugDetector}.
 *
 * @author Michael Tamm
 */
public class LayoutBug {

    private final String _description;
    private final URL _url;
    private final String _html;
    private File _screenshotFile;

    public LayoutBug(String description, WebPage webPage, File screenshotFile) {
        _description = description;
        _url = webPage.getUrl();
        _html = webPage.getHtml();
        _screenshotFile = screenshotFile;
    }

    /**
     * Returns a description of this layout bug.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Returns the URL of the page with this layout bug.
     */
    public URL getUrl() {
        return _url;
    }

    /**
     * Returns the HTML of the page with this layout bug.
     */
    public String getHtml() {
        return _html;
    }

    /**
     * Returns a screenshot of this layout bug, might return <code>null</code>.
     */
    public File getScreenshot() {
        return _screenshotFile;
    }

    @Override
    public String toString() {
        if (_screenshotFile == null) {
            return _description + "\n- URL: " + _url;
        } else {
            try {
                return _description + "\n- URL: " + _url + "\n- Screenshot: " + _screenshotFile.getCanonicalPath();
            } catch (IOException ignored) {
                return _description + "\n- URL: " + _url + "\n- Screenshot: " + _screenshotFile;
            }
        }
    }

}
