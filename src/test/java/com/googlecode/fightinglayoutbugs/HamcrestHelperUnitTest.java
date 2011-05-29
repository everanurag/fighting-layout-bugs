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

package com.googlecode.fightinglayoutbugs;

import org.hamcrest.Matcher;
import org.junit.Test;

import static com.googlecode.fightinglayoutbugs.TestHelper.asList;
import static com.googlecode.fightinglayoutbugs.TestHelper.asSet;
import static org.junit.Assert.*;

/**
 * @author Michael Tamm
 */
public class HamcrestHelperUnitTest {

    @Test
    public void testIsNull() {
        final Matcher<Object> matcher = HamcrestHelper.is(null);
        assertTrue(matcher.matches(null));
        assertFalse(matcher.matches(1));
    }

    @Test
    public void testIsWithLongAndInteger() {
        final Matcher<Object> matcher1 = HamcrestHelper.is(1);
        assertFalse(matcher1.matches(null));
        assertFalse(matcher1.matches(0));
        assertTrue(matcher1.matches(1));
        assertFalse(matcher1.matches(2));
        assertFalse(matcher1.matches(0L));
        assertTrue(matcher1.matches(1L));
        assertFalse(matcher1.matches(2L));
        assertFalse(matcher1.matches((1L << 32) + 1L));
        final Matcher<Object> matcher2 = HamcrestHelper.is(1L);
        assertFalse(matcher2.matches(null));
        assertFalse(matcher2.matches(0));
        assertTrue(matcher2.matches(1));
        assertFalse(matcher2.matches(2));
        assertFalse(matcher2.matches(0L));
        assertTrue(matcher2.matches(1L));
        assertFalse(matcher2.matches(2L));
        assertFalse(matcher2.matches((1L << 32) + 1L));
    }

    @Test
    public void testWithCollections() {
        final Matcher<Object> matcher1 = HamcrestHelper.is("foo");
        assertTrue(matcher1.matches(asList("foo")));
        assertFalse(matcher1.matches(asList("bar")));
        assertFalse(matcher1.matches(asList("foo", "bar")));
        assertFalse(matcher1.matches(asList("bar", "foo")));
        assertTrue(matcher1.matches(asSet("foo")));
        assertFalse(matcher1.matches(asSet("bar")));
        assertFalse(matcher1.matches(asSet("foo", "bar")));
        assertFalse(matcher1.matches(asSet("bar", "foo")));
        final Matcher<Object> matcher2 = HamcrestHelper.is("foo", "bar");
        assertFalse(matcher2.matches(asList("foo")));
        assertFalse(matcher2.matches(asList("bar")));
        assertTrue(matcher2.matches(asList("foo", "bar")));
        assertFalse(matcher2.matches(asList("bar", "foo")));
        assertFalse(matcher2.matches(asSet("foo")));
        assertFalse(matcher2.matches(asSet("bar")));
        assertTrue(matcher2.matches(asSet("foo", "bar")));
        assertTrue(matcher2.matches(asSet("bar", "foo")));
    }

    @Test
    public void testWithArrays() {
        final Matcher<Object> matcher1 = HamcrestHelper.is("foo", "bar");
        final Matcher<Object> matcher2 = HamcrestHelper.is(new String[]{ "foo", "bar" });
        assertTrue(matcher1.matches(new String[]{ "foo", "bar" }));
        assertTrue(matcher1.matches(new String[]{ "foo", "bar" }));
        assertFalse(matcher1.matches(new String[]{ "bar", "foo" }));
        assertFalse(matcher2.matches(new String[]{ "bar", "foo" }));
    }
}
