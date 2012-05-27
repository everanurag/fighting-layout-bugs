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

import com.googlecode.fightinglayoutbugs.ScreenshotCache.Condition;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(Theories.class)
public class ScreenshotCacheTest {

    @DataPoints
    public static final Condition[] ALL_CONDITIONS = Condition.values();

    private int largeScreenshotSize;

    @Test
    public void testThatScreenshotsAreCached() {
        final AtomicInteger i = new AtomicInteger(0);
        ScreenshotCache cache = new ScreenshotCache(null) {
            @Override void hideImages() {}
            @Override void restoreImages() {}
            @Override void colorAllText(@Nonnull String color) {}
            @Override void restoreTextColors() {}
            @Override protected Screenshot takeScreenshot() { return new Screenshot(new int[][] { new int[] { i.getAndIncrement() } }); }
        };
        for (Condition condition : ALL_CONDITIONS) {
            cache.getScreenshot(condition);
        }
        ScreenshotCache spy = spy(cache);
        for (Condition condition : ALL_CONDITIONS) {
            Screenshot screenshot = spy.getScreenshot(condition);
            assertThat(screenshot.pixels[0][0], is(condition.ordinal()));
        }
        verify(spy, never()).takeScreenshot();
    }

    /**
     * Test for <a href="http://code.google.com/p/fighting-layout-bugs/issues/detail?id=7">issue 7</a>.
     */
    @Test
    public void testThatCacheDoesNotLeadToOutOfMemoryError() {
        assertThatTwoLargeScreenshotsDoNotFitIntoMemory();
        assertThat(ALL_CONDITIONS.length, is(greaterThan(2)));
        ScreenshotCache cache = new ScreenshotCache(null) {
            @Override void hideImages() {}
            @Override void restoreImages() {}
            @Override void colorAllText(@Nonnull String color) {}
            @Override void restoreTextColors() {}
            @Override protected Screenshot takeScreenshot() { return newLargeScreenshot(); }
        };
        try {
            for (Condition condition : ALL_CONDITIONS) {
                cache.getScreenshot(condition);
            }
        } catch (OutOfMemoryError e) {
            fail("Caught OutOfMemoryError.");
        }
    }

    @Theory
    public void testTakeScreenshot(Condition condition1, Condition condition2) {
        ScreenshotCache cache = new ScreenshotCache(null) {
            @Override void hideImages() {}
            @Override void restoreImages() {}
            @Override void colorAllText(@Nonnull String color) {}
            @Override void restoreTextColors() {}
            @Override protected Screenshot takeScreenshot() { return null; }
        };
        cache.takeScreenshot(condition1);
        ScreenshotCache spy = spy(cache);
        spy.takeScreenshot(condition2);
        if (condition1.textColor == null && condition2.textColor != null) {
            verify(spy).colorAllText(condition2.textColor);
            verify(spy, never()).restoreTextColors();
        }
        if (condition1.textColor != null && condition2.textColor == null) {
            verify(spy).restoreTextColors();
            verify(spy, never()).colorAllText(anyString());
        }
        if (!condition1.hideImages && condition2.hideImages) {
            verify(spy).hideImages();
            verify(spy, never()).restoreImages();
        }
        if (condition1.hideImages && !condition2.hideImages) {
            verify(spy).restoreImages();
            verify(spy, never()).hideImages();
        }
    }

    private void assertThatTwoLargeScreenshotsDoNotFitIntoMemory() {
        Screenshot screenshot1 = newLargeScreenshot();
        try {
            Screenshot screenshot2 = newLargeScreenshot();
            fail("OutOfMemoryError expected.");
        } catch (OutOfMemoryError expected) {}
    }

    private Screenshot newLargeScreenshot() {
        if (largeScreenshotSize == 0) {
            largeScreenshotSize = determineLargeScreenshotSize();
        }
        int[][] pixels = new int[1][];
        pixels[0] = new int[largeScreenshotSize];
        return new Screenshot(pixels);
    }

    private int determineLargeScreenshotSize() {
        int n = 1000000;
        for (;;) {
            int[] a1 = new int[n];
            try {
                int[] a2 = new int[n];
            } catch (OutOfMemoryError e) {
                return n;
            }
            n = (n * 4) / 3;
        }
    }
}
