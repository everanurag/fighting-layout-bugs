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

import static de.michaeltamm.fightinglayoutbugs.StringHelper.asString;
import org.apache.commons.io.FileUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.collection.IsIn;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.*;
import org.hamcrest.number.IsCloseTo;
import org.hamcrest.number.OrderingComparisons;
import org.hamcrest.text.StringEndsWith;
import org.hamcrest.text.StringStartsWith;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Helper class for easy use of <a href="http://code.google.com/p/hamcrest/">hamcrest</a>.
 *
 * @author Michael Tamm
 */
@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion", "ClassWithTooManyMethods"})
public class HamcrestHelper {

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

    public static class OrMatcher<T> extends BaseMatcher<T> {

        private final List<Matcher<? extends T>> _matchers;

        public OrMatcher(Matcher<T> matcher) {
            _matchers = new LinkedList<Matcher<? extends T>>();
            _matchers.add(matcher);
        }

        @SuppressWarnings({"ReturnOfThis"})
        public OrMatcher<T> or(T value) {
            _matchers.add(is(value));
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

    public static class NorMatcher<T> extends BaseMatcher<T> {

        private final List<Matcher<? extends T>> _matchers;

        public NorMatcher(Matcher<? extends T> matcher) {
            _matchers = new LinkedList<Matcher<? extends T>>();
            _matchers.add(matcher);
        }

        @SuppressWarnings({"ReturnOfThis"})
        public NorMatcher<T> nor(T value) {
            _matchers.add(is(value));
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

    /**
     * <p>Decorates another Matcher, retaining the behavior
     * but allowing tests to be slightly more expressive.</p>
     * <p>eg. <code>assertThat(cheese, is(equalTo(smelly)));</code><br />
     * vs <code>assertThat(cheese, equalTo(smelly));</code></p>
     */
    public static <T> Matcher<T> is(Matcher<T> matcher) {
        return Is.is(matcher);
    }

    /**
     * Same as {@link #equalTo}, but slightly more readable.
     */
    public static <T> Matcher<T> is(T expected) {
        return IsEqual.equalTo(expected);
    }

    /**
     * Inverts the rule.
     */
    public static <T> Matcher<T> not(Matcher<T> matcher) {
        return IsNot.not(matcher);
    }

    /**
     * <p>This is a shortcut to the frequently used <code>not(equalTo(x))</code>.</p>
     * <p>eg. assertThat(cheese, is(not(smelly)));<br />
     * vs assertThat(cheese, is(not(equalTo(smelly))));</p>
     */
    public static <T> Matcher<T> not(T value) {
        return IsNot.not(value);
    }

    /**
     * Inverts the rule.
     */
    public static <T> Matcher<T> isNot(Matcher<T> matcher) {
        return IsNot.not(matcher);
    }

    /**
     * <p>This is a shortcut to the frequently used <code>not(equalTo(x))</code>.</p>
     * <p>eg. assertThat(cheese, is(not(smelly)));<br />
     * vs assertThat(cheese, is(not(equalTo(smelly))));</p>
     */
    public static <T> Matcher<T> isNot(T value) {
        return IsNot.not(value);
    }

    /**
     * Is the value equal to another value, as tested by the
     * {@link Object#equals} method (with special handling for arrays)?
     */
    public static <T> Matcher<T> equalTo(T operand) {
        return IsEqual.equalTo(operand);
    }

    /**
     * Is the value equal to another value, as tested by the
     * {@link Object#equals} method (with special handling for arrays)?
     */
    public static <T> Matcher<T> isEqualTo(T operand) {
        return IsEqual.equalTo(operand);
    }

    /**
     * Is the value an instance of a particular type?
     */
    public static Matcher<Object> instanceOf(Class<?> type) {
        return IsInstanceOf.instanceOf(type);
    }

    /**
     * Is the value an instance of a particular type?
     */
    public static Matcher<Object> isInstanceOf(Class<?> type) {
        return IsInstanceOf.instanceOf(type);
    }

    /**
     * Evaluates to true only if matched value is the same instance as the given object.
     */
    public static <T> Matcher<T> sameInstanceAs(T object) {
        return IsSame.sameInstance(object);
    }

    /**
     * Evaluates to true only if matched value is the same instance as the given object.
     */
    public static <T> Matcher<T> isSameInstanceAs(T object) {
        return IsSame.sameInstance(object);
    }

    /**
     * This matcher always evaluates to true.
     */
    public static <T> Matcher<T> anything() {
        return IsAnything.anything();
    }

    /**
     * This matcher always evaluates to true.
     *
     * @param description A meaningful string used when describing itself.
     */
    public static <T> Matcher<T> anything(String description) {
        return IsAnything.anything(description);
    }

    /**
     * This matcher always evaluates to true. With type inference.
     */
    public static <T> Matcher<T> any(Class<T> type) {
        return IsAnything.any(type);
    }

    /**
     * Matches if matched value is null.
     */
    public static <T> Matcher<T> isNull() {
        return IsNull.nullValue();
    }

    /**
     * Matches if matched value is null. With type inference.
     */
    public static <T> Matcher<T> isNull(Class<T> type) {
        return IsNull.nullValue(type);
    }

    /**
     * Matches if matched value is not null.
     */
    public static <T> Matcher<T> isNotNull() {
        return IsNull.notNullValue();
    }

    /**
     * Matches if matched value is not null. With type inference.
     */
    public static <T> Matcher<T> isNotNull(Class<T> type) {
        return IsNull.notNullValue(type);
    }

    /**
     * <p>This matcher always evaluates to true.</p>
     * <p>Use like this: <code>assertThat(foo(), doesNotThrowAnException());</code></p>
     */
    public static <T> Matcher<T> doesNotThrowAnException() {
        return new BaseMatcher<T>() {
            public boolean matches(Object item) {
                return true;
            }
            public void describeTo(Description description) {
                description.appendText("does not throw an exception");
            }
        };
    }

    public static <T> Matcher<T> oneOf(T... elements) {
        return IsIn.isOneOf(elements);
    }

    public static <T> Matcher<T> isOneOf(T... elements) {
        return IsIn.isOneOf(elements);
    }

    public static <T> AndMatcher<T> both(Matcher<T> matcher) {
        return new AndMatcher<T>(matcher);
    }

    public static <T> OrMatcher<T> either(Matcher<T> matcher) {
        return new OrMatcher<T>(matcher);
    }

    public static <T> OrMatcher<T> either(T value) {
        return new OrMatcher<T>(is(value));
    }

    public static <T> NorMatcher<T> neither(Matcher<T> matcher) {
        return new NorMatcher<T>(matcher);
    }

    public static <T> Matcher<Iterable<T>> containsItem(T element) {
        return IsCollectionContaining.hasItem(element);
    }

    public static <T> Matcher<Iterable<T>> containsItem(Matcher<? extends T> elementMatcher) {
        return IsCollectionContaining.hasItem(elementMatcher);
    }

    public static <K, V> Matcher<Map<K, V>> containsEntry(K key, V value) {
        return IsMapContaining.hasEntry(key, value);
    }

    public static <K, V> Matcher<Map<K, V>> containsEntry(Matcher<K> keyMatcher, Matcher<V> valueMatcher) {
        return IsMapContaining.hasEntry(keyMatcher, valueMatcher);
    }

    public static <K, V> Matcher<Map<K, V>> containsKey(K key) {
        return IsMapContaining.hasKey(key);
    }

    public static <K, V> Matcher<Map<K, V>> containsKey(Matcher<K> keyMatcher) {
        return IsMapContaining.hasKey(keyMatcher);
    }

    public static <K, V> Matcher<Map<K, V>> containsValue(Matcher<V> valueMatcher) {
        return IsMapContaining.hasValue(valueMatcher);
    }

    public static <K, V> Matcher<Map<K, V>> containsValue(V value) {
        return IsMapContaining.hasValue(value);
    }

    public static Matcher<Double> closeTo(double operand, double error) {
        return IsCloseTo.closeTo(operand, error);
    }

    public static Matcher<Double> isCloseTo(double operand, double error) {
        return IsCloseTo.closeTo(operand, error);
    }

    public static <T extends Comparable<T>> Matcher<T> greaterThan(T value) {
        return OrderingComparisons.greaterThan(value);
    }

    public static <T extends Comparable<T>> Matcher<T> isGreaterThan(T value) {
        return OrderingComparisons.greaterThan(value);
    }

    public static <T extends Comparable<T>> Matcher<T> greaterThanOrEqualTo(T value) {
        return OrderingComparisons.greaterThanOrEqualTo(value);
    }

    public static <T extends Comparable<T>> Matcher<T> isGreaterThanOrEqualTo(T value) {
        return OrderingComparisons.greaterThanOrEqualTo(value);
    }

    public static <T extends Comparable<T>> Matcher<T> lessThan(T value) {
        return OrderingComparisons.lessThan(value);
    }

    public static <T extends Comparable<T>> Matcher<T> isLessThan(T value) {
        return OrderingComparisons.lessThan(value);
    }

    public static <T extends Comparable<T>> Matcher<T> lessThanOrEqualTo(T value) {
        return OrderingComparisons.lessThanOrEqualTo(value);
    }

    public static <T extends Comparable<T>> Matcher<T> isLessThanOrEqualTo(T value) {
        return OrderingComparisons.lessThanOrEqualTo(value);
    }

    public static Matcher<String> endsWith(String substring) {
        return StringEndsWith.endsWith(substring);
    }

    public static Matcher<String> startsWith(String substring) {
        return StringStartsWith.startsWith(substring);
    }

    public static <T> Matcher<T> contains(final String s) {
        if (s == null) {
            throw new IllegalArgumentException("Parameter s must not be null.");
        }
        return new BaseMatcher<T>() {
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
            public void describeTo(Description description) {
                description.appendText("contains ").appendText(asString(s));
            }
        };
    }

    /**
     * Works for strings, collections, and maps.
     */
    public static <T> Matcher<T> empty() {
        return new BaseMatcher<T>() {
            public boolean matches(Object o) {
                final boolean result;
                if (o == null) {
                    result = true;
                } else if (o instanceof CharSequence || o instanceof StringWriter) {
                    final String s = o.toString();
                    result = (s == null || s.length() == 0);
                } else if (o instanceof Collection) {
                    result = ((Collection) o).isEmpty();
                } else if (o instanceof Map) {
                    result = ((Map) o).isEmpty();
                } else {
                    throw new RuntimeException("Don't know how to handle object of type " + o.getClass().getName());
                }
                return result;
            }
            public void describeTo(Description description) {
                description.appendText("empty");
            }
        };
    }

    /**
     * Works for strings, collections, and maps.
     */
    public static <T> Matcher<T> isEmpty() {
        return empty();
    }

    /**
     * Works for strings, collections, and maps.
     */
    @SuppressWarnings("unchecked")
    public static <T> Matcher<T> isNotEmpty() {
        return (Matcher<T>) not(empty());
    }

    public static Matcher<String> blank() {
        return new BaseMatcher<String>() {
            public boolean matches(Object o) {
                return (o == null || "".equals(o.toString().trim()));
            }
            public void describeTo(Description description) {
                description.appendText("blank string");
            }
        };
    }

    public static Matcher<String> isBlank() {
        return blank();
    }

    public static Matcher<String> isNotBlank() {
        return not(blank());
    }

    /**
     * Returns a {@link List} containing the given <code>objects</code>.
     */
    public static <T> List<T> asList(T... items) {
        return Arrays.asList(items);
    }

    /**
     * Returns a {@link Map} with the mappings <code>keysAndValues[0] => keysAndValues[1],
     * keysAndValues[2] => keysAndValues[3], ...</code>.
     */
    public static <K, V> Map<K, V> asMap(Object... keysAndValues) {
        final Map<K, V> result = new LinkedHashMap<K, V>();
        final int n = keysAndValues.length;
        if (n % 2 == 1) {
            throw new IllegalArgumentException("You must provide an even number of arguments.");
        }
        for (int i = 0; i < n; i += 2) {
            result.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return result;
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
