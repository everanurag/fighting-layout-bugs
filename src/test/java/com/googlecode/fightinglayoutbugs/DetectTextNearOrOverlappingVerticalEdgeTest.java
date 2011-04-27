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

import org.junit.Test;

import java.util.Collection;

import static com.googlecode.fightinglayoutbugs.HamcrestHelper.assertThat;

/**
 * @author Michael Tamm
 */
public class DetectTextNearOrOverlappingVerticalEdgeTest extends TestUsingSelenium {

    @Test
    public void shouldFindLayoutBugInYahooProfileUpdatesPage() throws Exception {
        WebPage testPage = getWebPageFor("/Yahoo!_Profile_Updates.html");
        testPage.executeJavaScript("window.resizeTo(1008, 706)");
        final long startTime = System.currentTimeMillis();
        final LayoutBugDetector detector = new DetectTextNearOrOverlappingVerticalEdge();
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(testPage);
        assertThat(layoutBugs.size() == 1);
        final LayoutBug layoutBug = layoutBugs.iterator().next();
        assertThat(layoutBug.getScreenshot(), HamcrestHelper.isNotNull());
        System.out.println(layoutBug);
        assertThat(layoutBug.getScreenshot().isFile());
        assertThat(layoutBug.getScreenshot().lastModified() > startTime);
    }

    @Test
    public void shouldFindLayoutBugInMicrosoftNewsletterPage() throws Exception {
        WebPage testPage = getWebPageFor("/Microsoft_Newsletter.html");
        final long startTime = System.currentTimeMillis();
        final LayoutBugDetector detector = new DetectTextNearOrOverlappingVerticalEdge();
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(testPage);
        assertThat(layoutBugs.size() == 1);
        final LayoutBug layoutBug = layoutBugs.iterator().next();
        assertThat(layoutBug.getScreenshot(), HamcrestHelper.isNotNull());
        System.out.println(layoutBug);
        assertThat(layoutBug.getScreenshot().isFile());
        assertThat(layoutBug.getScreenshot().lastModified() > startTime);
    }
}
