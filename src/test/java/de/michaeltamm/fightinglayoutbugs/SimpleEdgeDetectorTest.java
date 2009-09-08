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

/**
 * @author Michael Tamm
 */
public class SimpleEdgeDetectorTest extends TestUsingFirefoxDriver {

    @Test
    public void shouldDetectHorizontalAndVerticalEdgesInYahooProfileUpdatesPage() throws Exception {
        _driver.get("http://localhost:8080/Yahoo!_Profile_Updates.html");
        _driver.executeScript("window.resizeTo(1008, 706)");
        final EdgeDetector edgeDetector = new SimpleEdgeDetector();
        final boolean[][] horizontalEdges = edgeDetector.detectHorizontalEdgesIn(_driver, 16);
        // TODO: add assertion
        final int w = horizontalEdges.length;
        final int h = horizontalEdges[0].length;
        final boolean[][] verticalEdges = edgeDetector.detectVerticalEdgesIn(_driver, 16);
        assertThat(verticalEdges.length == w);
        assertThat(verticalEdges[0].length == h);
        // TODO: add assertion
    }

}
