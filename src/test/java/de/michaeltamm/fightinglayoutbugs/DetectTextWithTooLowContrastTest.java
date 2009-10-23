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

import java.io.File;
import java.util.Collection;

public class DetectTextWithTooLowContrastTest extends TestUsingFirefoxDriver {

    @Test
    public void shouldFindLayoutBugInEspiritNewsletterPage() throws Exception {
        _driver.get("http://localhost:8080/ESPRIT_newsletter.html");
        final long startTime = System.currentTimeMillis();
        final LayoutBugDetector detector = new DetectTextWithTooLowContrast();
        detector.setScreenshotDir(new File("target"));
        final Collection<LayoutBug> layoutBugs = detector.findLayoutBugsIn(_driver);
        assertThat(layoutBugs.size() == 1);
        final LayoutBug layoutBug = layoutBugs.iterator().next();
        assertThat(layoutBug.getScreenshot(), HamcrestHelper.isNotNull());
        System.out.println(layoutBug);
        assertThat(layoutBug.getScreenshot().isFile());
        assertThat(layoutBug.getScreenshot().lastModified() > startTime);
    }

}
