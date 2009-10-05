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

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.assertThat;
import org.junit.Test;

import java.util.Collection;

public class DetectInvalidImageUrlsTest extends TestUsingFirefoxDriver {

    @Test
    public void shouldFindInvalidImageUrls() throws Exception {
        _driver.get("http://localhost:8080/page_with_invalid_image_urls.html");
        final LayoutBugDetector detector = new DetectInvalidImageUrls();
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugs(_driver);
        for (LayoutBug bug : layoutBugs) {
            System.out.println(bug);
        }
        assertThat(layoutBugs.size() == 7);
    }

}
