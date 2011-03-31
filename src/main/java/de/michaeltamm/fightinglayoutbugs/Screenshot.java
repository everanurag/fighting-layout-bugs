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

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a screenshot of an entire {@link WebPage}.
 */
public class Screenshot {

    public interface Condition {
        boolean isSatisfiedBy(Screenshot screenshot);
        boolean satisfyWillModifyWebPage();
        void satisfyFor(WebPage webPage);
    }

    private static class WithAllTextColoredCondition implements Condition {
        private final String color;

        private WithAllTextColoredCondition(String color) {
            this.color = color;
        }

        public boolean isSatisfiedBy(Screenshot screenshot) {
            return color.equals(screenshot.textColor);
        }

        public boolean satisfyWillModifyWebPage() {
            return true;
        }

        public void satisfyFor(WebPage webPage) {
            webPage.colorAllText(color);
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof WithAllTextColoredCondition) && StringUtils.equals(color, ((WithAllTextColoredCondition) o).color);
        }
    }

    public static Condition withAllTextColored(@Nonnull String color) {
        if (color == null) {
            throw new IllegalArgumentException("Method parameter color must not be null.");
        }
        return new WithAllTextColoredCondition(color);
    }

    public static Condition withNoText() {
        return withAllTextColored("transparent");
    }

    /**
     * Helper interface for fluent API to build conditions like:
     * <code>takenAtLeast(500, MILLISECONDS)&#46;laterThan(screenshot1)</code>.
     */
    public interface TakenAtLeast {
        public Condition laterThan(Screenshot screenshot);
    }

    public static TakenAtLeast takenAtLeast(final long time, final TimeUnit timeUnit) {
        return new TakenAtLeast() {
            public Condition laterThan(Screenshot screenshot) {
                final Date notTakenBefore = new Date(screenshot.creationDate.getTime() + timeUnit.toMillis(time));
                return new Condition() {
                    public boolean isSatisfiedBy(Screenshot screenshot) {
                        return !screenshot.creationDate.before(notTakenBefore);
                    }
                    public boolean satisfyWillModifyWebPage() {
                        return false;
                    }
                    public void satisfyFor(WebPage webPage) {
                        Date now = new Date();
                        while (now.before(notTakenBefore)) {
                            long delay = notTakenBefore.getTime() - now.getTime();
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException("Got interrupted.", e);
                            }
                            now = new Date();
                        }
                    }
                };
            }
        };
    }

    public final Date creationDate;
    public int[][] pixels;
    public final String textColor;
    public final int width;
    public final int height;
    public final Dimension dimension;

    public Screenshot(int[][] pixels, String textColor) {
        creationDate = new Date();
        this.pixels = pixels;
        this.textColor = textColor;
        width = pixels.length;
        height = pixels[0].length;
        dimension = new Dimension(width, height);
    }
}
