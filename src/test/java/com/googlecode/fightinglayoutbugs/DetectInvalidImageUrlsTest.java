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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collection;

import static com.googlecode.fightinglayoutbugs.helpers.TestHelper.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class DetectInvalidImageUrlsTest extends TestUsingSelenium {

    @Test
    public void testStripCommentsFrom() {
        assertThat(DetectInvalidImageUrls.stripCommentsFrom(""), is(""));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("foo"), is("foo"));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("foo /* ..."), is("foo "));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("foo /* ... */"), is("foo "));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("foo /* ... */ bar"), is("foo  bar"));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("/**/"), is(""));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("/**//**/"), is(""));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("foo/**//**/"), is("foo"));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("/**/foo/**/"), is("foo"));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("/**//**/foo"), is("foo"));
        assertThat(DetectInvalidImageUrls.stripCommentsFrom("f/**/o/**/o"), is("foo"));
    }

    @Test
    public void shouldNotFindInvalidImageUrlsInYahooSportsPage() throws Exception {
        WebPage yahooSportsPage = getWebPageFor("/Yahoo!_Sports.html");
        LayoutBugDetector detector = new DetectInvalidImageUrls();
        Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(yahooSportsPage);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
        assertThat(layoutBugs, isEmpty());
    }

    @Test
    public void shouldFindInvalidImageUrls() throws Exception {
        WebPage pageWithInvalidImageUrls = getWebPageFor("/page_with_invalid_image_urls.html");
        LayoutBugDetector detector = new DetectInvalidImageUrls();
        Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(pageWithInvalidImageUrls);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/1.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/2.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/3.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/4.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/5.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/6.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/7.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/8.png"));
        assertThat(layoutBugs, containsLayoutBug("Detected visible <img> without src attribute."));
        assertThat(layoutBugs, containsLayoutBug("Detected visible <img> element with empty src attribute."));
    }

    @Test
    public void shouldNotComplainAboutValidImageUrls() throws Exception {
        WebPage pageWithValidImageUrls = getWebPageFor("/page_with_valid_image_urls.html");
        LayoutBugDetector detector = new DetectInvalidImageUrls();
        Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(pageWithValidImageUrls);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
        assertThat(layoutBugs, isEmpty());
    }

    /**
     * Test for <a href="http://code.google.com/p/fighting-layout-bugs/issues/detail?id=9">issue 9</a>.
     */
    @Test
    public void shouldNotReportFontFaceUrlsAsInvalidImageUrls() throws Exception {
        WebPage pageWithValidImageUrls = getWebPageFor("/odesk.html");
        LayoutBugDetector detector = new DetectInvalidImageUrls();
        Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(pageWithValidImageUrls);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
            assertThat(bug.getDescription(), not(containsString("data:font/opentype")));
        }
    }

    private Matcher<Collection<LayoutBug>> containsLayoutBug(final String descriptionSubstring) {
        return new TypeSafeMatcher<Collection<LayoutBug>>() {
            public boolean matchesSafely(Collection<LayoutBug> layoutBugs) {
                for (LayoutBug bug : layoutBugs) {
                    if (bug.getDescription().contains(descriptionSubstring)) {
                        return true;
                    }
                }
                return false;
            }
            public void describeTo(Description description) {
                description.appendText("contains LayoutBug \"" + descriptionSubstring + "\"");
            }
        };
    }

}
