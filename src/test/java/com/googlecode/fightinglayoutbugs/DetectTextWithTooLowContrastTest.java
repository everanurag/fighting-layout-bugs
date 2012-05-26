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

package com.googlecode.fightinglayoutbugs;

import com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper;
import org.junit.Test;

import java.util.Collection;

import static com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper.assertThat;
import static com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper.is;

/**
 * @author Michael Tamm
 */
public class DetectTextWithTooLowContrastTest extends TestUsingSelenium {

    @Test
    public void shouldFindLayoutBugInEspiritNewsletterPage() throws Exception {
        WebPage testPage = getWebPageFor("/ESPRIT_newsletter.html");
        final long startTime = System.currentTimeMillis();
        final LayoutBugDetector detector = new DetectTextWithTooLowContrast();
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(testPage);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
        assertThat(layoutBugs.size(), is(1));
        final LayoutBug layoutBug = layoutBugs.iterator().next();
        assertThat(layoutBug.getScreenshot(), HamcrestHelper.isNotNull());
        assertThat(layoutBug.getScreenshot().isFile());
        assertThat(layoutBug.getScreenshot().lastModified() > startTime);
    }

    @Test
    public void shouldNotFindBugsOnHardToReadPage() throws Exception {
        WebPage testPage = getWebPageFor("/hard_to_read.html");
        final LayoutBugDetector detector = new DetectTextWithTooLowContrast();
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(testPage);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
        assertThat(layoutBugs.isEmpty());
    }

}
