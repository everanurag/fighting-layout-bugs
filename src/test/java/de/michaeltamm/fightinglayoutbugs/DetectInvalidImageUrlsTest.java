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

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.*;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Description;

import java.util.Collection;

public class DetectInvalidImageUrlsTest extends TestUsingFirefoxDriver {

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
    public void shouldFindInvalidImageUrls() throws Exception {
        _driver.get("http://localhost:8080/page_with_invalid_image_urls.html");
        final LayoutBugDetector detector = new DetectInvalidImageUrls();
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(_driver);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
            System.out.println();
        }
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/1.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/2.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/3.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/4.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/5.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/6.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/7.png"));
        assertThat(layoutBugs, containsLayoutBug("/invalid/url/8.png"));
        assertThat(layoutBugs, containsLayoutBug("Detected <img> without src attribute."));
        assertThat(layoutBugs, containsLayoutBug("Detected <img> element with empty src attribute."));
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
