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

package com.googlecode.fightinglayoutbugs.helpers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.*;

import static com.googlecode.fightinglayoutbugs.helpers.StringHelper.asString;

/**
 * Helper class for easy use of <a href="http://code.google.com/p/hamcrest/">hamcrest</a>.
 *
 * @author Michael Tamm
 */
public class HamcrestHelper {

    public static void assertThat(boolean shouldBeTrue) {
        if (!shouldBeTrue) {
            fail();
        }
    }

    public static <T> void assertThat(T actual, Matcher<T> matcher) {
        if (!matcher.matches(actual)) {
            fail();
        }
    }

    public static class AndMatcher<T> extends BaseMatcher<T> {

        private final List<Matcher<? extends T>> _matchers;

        public AndMatcher(Matcher<T> matcher) {
            _matchers = new LinkedList<Matcher<? extends T>>();
            _matchers.add(matcher);
        }

        @SuppressWarnings({"ReturnOfThis"})
        public AndMatcher<T> and(Matcher<? extends T> matcher) {
            _matchers.add(matcher);
            return this;
        }

        @SuppressWarnings({"MethodWithMultipleReturnPoints"})
        public boolean matches(Object item) {
            for (Matcher<? extends T> m : _matchers) {
                if (!m.matches(item)) {
                    return false;
                }
            }
            return true;
        }

        public void describeTo(Description description) {
            for (Iterator<Matcher<? extends T>> i = _matchers.iterator(); i.hasNext(); ) {
                final Matcher<? extends T> m = i.next();
                m.describeTo(description);
                if (i.hasNext()) {
                    description.appendText(" and ");
                }
            }
        }
    }

    public static <T> AndMatcher<T> both(Matcher<T> matcher) {
        return new AndMatcher<T>(matcher);
    }

    public static class OrMatcher<T> extends BaseMatcher<T> {

        private final List<Matcher<? extends T>> _matchers;

        public OrMatcher(Matcher<T> matcher) {
            _matchers = new LinkedList<Matcher<? extends T>>();
            _matchers.add(matcher);
        }

        @SuppressWarnings({"ReturnOfThis"})
        public OrMatcher<T> or(T value) {
            _matchers.add(isEqualTo(value));
            return this;
        }

        @SuppressWarnings({"ReturnOfThis"})
        public OrMatcher<T> or(Matcher<T> matcher) {
            _matchers.add(matcher);
            return this;
        }

        @SuppressWarnings({"MethodWithMultipleReturnPoints"})
        public boolean matches(Object item) {
            for (Matcher<? extends T> m : _matchers) {
                if (m.matches(item)) {
                    return true;
                }
            }
            return false;
        }

        public void describeTo(Description description) {
            for (Iterator<Matcher<? extends T>> i = _matchers.iterator(); i.hasNext(); ) {
                final Matcher<? extends T> m = i.next();
                m.describeTo(description);
                if (i.hasNext()) {
                    description.appendText(" or ");
                }
            }
        }
    }

    public static <T> OrMatcher<T> either(Matcher<T> matcher) {
        return new OrMatcher<T>(matcher);
    }

    public static <T> OrMatcher<T> isEither(T value) {
        return new OrMatcher<T>(isEqualTo(value));
    }

    public static class NorMatcher<T> extends BaseMatcher<T> {

        private final List<Matcher<? extends T>> _matchers;

        public NorMatcher(Matcher<? extends T> matcher) {
            _matchers = new LinkedList<Matcher<? extends T>>();
            _matchers.add(matcher);
        }

        @SuppressWarnings({"ReturnOfThis"})
        public NorMatcher<T> nor(T value) {
            _matchers.add(isEqualTo(value));
            return this;
        }

        @SuppressWarnings({"ReturnOfThis"})
        public NorMatcher<T> nor(Matcher<? extends T> matcher) {
            _matchers.add(matcher);
            return this;
        }

        @SuppressWarnings({"MethodWithMultipleReturnPoints"})
        public boolean matches(Object item) {
            for (Matcher<? extends T> m : _matchers) {
                if (m.matches(item)) {
                    return false;
                }
            }
            return true;
        }

        public void describeTo(Description description) {
            String separator = "neither ";
            for (Matcher<? extends T> m : _matchers) {
                description.appendText(separator);
                m.describeTo(description);
                separator = " nor ";
            }
        }
    }

    public static <T> NorMatcher<T> neither(Matcher<T> matcher) {
        return new NorMatcher<T>(matcher);
    }

    /**
     * Handles {@code null}, numbers, collections, and arrays.
     */
    public static Matcher<Object> is(final Object expected, final Object... moreExpectedValues) {
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                boolean matches;
                if (o == null) {
                    matches = (moreExpectedValues.length == 0 && expected == null);
                } else if (o instanceof Number) {
                    if (moreExpectedValues.length > 0) {
                        matches = false;
                    } else {
                        if (expected instanceof Number) {
                            if (expected instanceof Long && o instanceof Integer) {
                                matches = (((Long) expected) == ((Integer) o).longValue());
                            } else if (expected instanceof Integer && o instanceof Long) {
                                matches = (((Integer) expected).longValue() == ((Long) o));
                            } else {
                                matches = o.equals(expected);
                            }
                        } else {
                            matches = false;
                        }
                    }
                } else if (o instanceof Collection) {
                    if (moreExpectedValues.length > 0) {
                        if (o instanceof Set) {
                            Set<?> expectedSet = Sets.union(TestHelper.asSet(expected), TestHelper.asSet(moreExpectedValues));
                            matches = expectedSet.equals(o);
                        } else if (o instanceof List) {
                            List<?> expectedList = Lists.asList(expected, moreExpectedValues);
                            matches = expectedList.equals(o);
                        } else {
                            throw new RuntimeException("Don't know how to match a " + o.getClass().getName() + " instance.");
                        }
                    } else if (expected == null) {
                        throw new RuntimeException("Ambigious matcher: Use either isNull() or isEqualTo(Collections.singleton(null))");
                    } else if (expected instanceof Collection) {
                        matches = expected.equals(o);
                    } else {
                        matches = (((Collection) o).size() == 1 && expected.equals(((Collection) o).iterator().next()));
                    }
                } else if (o.getClass().isArray()) {
                    final Object expectedArray;
                    if (moreExpectedValues.length > 0) {
                        final Object[] a = new Object[moreExpectedValues.length + 1];
                        a[0] = expected;
                        System.arraycopy(moreExpectedValues, 0, a, 1, moreExpectedValues.length);
                        expectedArray = a;
                    } else if (expected != null && !expected.getClass().isArray()) {
                        expectedArray = new Object[]{ expected };
                    } else {
                        expectedArray = expected;
                    }
                    matches = new IsEqual<Object>(expectedArray).matches(o);
                } else {
                    matches = (moreExpectedValues.length == 0 && o.equals(expected));
                }
                return matches;
            }
            @Override
            public void describeTo(Description description) {
                if (moreExpectedValues.length == 0) {
                    if (expected instanceof SelfDescribing) {
                        ((SelfDescribing) expected).describeTo(description);
                    } else {
                        description.appendText(StringHelper.asString(expected));
                    }
                } else {
                    final List<Object> temp = new ArrayList<Object>(moreExpectedValues.length + 1);
                    temp.add(expected);
                    temp.addAll(Arrays.asList(moreExpectedValues));
                    description.appendText(Joiner.on(", ").join(temp));
                }
            }
        };
    }

    /**
     * Is the value equal to another value, as tested by the
     * {@link Object#equals(Object)} method with special handling for arrays.
     */
    public static <T> Matcher<T> isEqualTo(T operand) {
        return IsEqual.equalTo(operand);
    }

    /**
     * Is the value an instance of a particular class?
     */
    public static Matcher<Object> isInstanceOf(Class<?> clazz) {
        return IsInstanceOf.instanceOf(clazz);
    }

    /**
     * Evaluates to {@code true} only if the matched value is the same instance as the given object.
     */
    public static <T> Matcher<T> isSameInstanceAs(T object) {
        return IsSame.sameInstance(object);
    }

    /**
     * Matches if the matched value is {@code null}.
     */
    public static Matcher<Object> isNull() {
        return IsNull.nullValue();
    }

    /**
     * Matches if the matched value is not {@code null}.
     */
    public static Matcher<Object> isNotNull() {
        return IsNull.notNullValue();
    }

    /**
     * This matcher always evaluates to {@code true}, use it like this:<pre>
     *     assertThat(foo(), doesNotThrowAnException());
     * </pre>
     */
    public static Matcher<Object> doesNotThrowAnException() {
        return new BaseMatcher<Object>() {
            public boolean matches(Object item) {
                return true;
            }
            public void describeTo(Description description) {
                description.appendText("does not throw an exception");
            }
        };
    }

    /**
     * Handles CharSequences like String or StringBuilder and Collections.
     */
    public static Matcher<Object> contains(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("Parameter s must not be null.");
        }
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                final boolean result;
                if (o instanceof CharSequence || o instanceof StringWriter) {
                    result = "".equals(s) || o.toString().contains(s);
                } else if (o instanceof Collection) {
                    result = ((Collection) o).contains(s);
                } else if (o == null) {
                    result = false;
                } else {
                    throw new RuntimeException("Don't know how to handle object of type " + o.getClass().getName());
                }
                return result;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("contains ").appendText(asString(s));
            }
        };
    }

    /**
     * Works for strings, arrays, collections, and maps.
     */
    public static Matcher<Object> isEmpty() {
        return new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object o) {
                final boolean result;
                if (o == null) {
                    result = false;
                } else if (o instanceof CharSequence || o instanceof StringWriter) {
                    final String s = o.toString();
                    result = (s.length() == 0);
                } else if (o instanceof Collection) {
                    result = ((Collection) o).isEmpty();
                } else if (o instanceof Map) {
                    result = ((Map) o).isEmpty();
                } else if (o.getClass().isArray()) {
                    result = (Array.getLength(o) == 0);
                } else {
                    throw new RuntimeException("Don't know how to handle object of type " + o.getClass().getName());
                }
                return result;
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("empty");
            }
        };
    }

    private static void fail() {
        String assertionErrorMessage = "";
        // Try to extract assertion error message from the source file, from which assertThat has been called ...
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int i = 0;
        while (!"assertThat".equals(stackTrace[i].getMethodName())) {
            ++i;
        }
        ++i;
        final File javaFile = findSourceFileFor(stackTrace[i]);
        if (javaFile != null) {
            final int lineNumber = stackTrace[i].getLineNumber();
            try {
                @SuppressWarnings({"unchecked"})
                final List<String> lines = (List<String>) FileUtils.readLines(javaFile, "UTF-8");
                final String line = lines.get(lineNumber - 1).trim();
                if (line.startsWith("assertThat(")) {
                    assertionErrorMessage = line.substring("assertThat(".length());
                    if (assertionErrorMessage.endsWith(");")) {
                        assertionErrorMessage = assertionErrorMessage.substring(0, assertionErrorMessage.length() - ");".length());
                    } else {
                        assertionErrorMessage += " ...";
                    }
                }
            } catch (IOException ignored) {}
        }
        AssertionError e = new AssertionError(assertionErrorMessage);
        e.setStackTrace(Arrays.copyOfRange(stackTrace, i, stackTrace.length));
        throw e;
    }

    private static File findSourceFileFor(StackTraceElement stackTraceElement) {
        final String className = stackTraceElement.getClassName();
        final int i = className.lastIndexOf('.');
        final String packageDir = (i == -1 ? "" : "/" + className.substring(0, i).replace('.', '/'));
        final File workDir = new File(new File("dummy").getAbsolutePath()).getParentFile();
        // Maven 2 directory layout ...
        final String testSourcesDir = "src/test/java";
        final String sourceFileName = stackTraceElement.getFileName();
        File sourceFile = new File(testSourcesDir + packageDir, sourceFileName);
        if (!sourceFile.exists()) {
            System.err.println("Could not find " + sourceFile.getAbsolutePath() + " (current work dir: " + workDir.getAbsolutePath() + ").");
            sourceFile = null;
        }
        return sourceFile;
    }

    private HamcrestHelper() {}
}
