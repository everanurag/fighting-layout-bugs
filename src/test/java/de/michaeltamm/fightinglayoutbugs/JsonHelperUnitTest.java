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

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.*;
import static de.michaeltamm.fightinglayoutbugs.TestHelper.asList;
import static de.michaeltamm.fightinglayoutbugs.TestHelper.asMap;

/**
 * @author Michael Tamm
 */
public class JsonHelperUnitTest {

    @Test
    public void testNullLiteral() {
        assertThat(JsonHelper.parse("null"), isNull());
    }

    @Test
    public void testFalseLiteral() {
        assertThat(JsonHelper.parse("false"), both(isInstanceOf(Boolean.class)).and(is(false)));
    }

    @Test
    public void testTrueLiteral() {
        assertThat(JsonHelper.parse("true"), both(isInstanceOf(Boolean.class)).and(is(true)));
    }

    @Test
    public void testString() {
        assertThat(JsonHelper.parse("\"foo\""), both(isInstanceOf(String.class)).and(isEqualTo("foo")));
        assertThat(JsonHelper.parse("\"\\\"\""), both(isInstanceOf(String.class)).and(isEqualTo("\"")));
        assertThat(JsonHelper.parse("\"foo\\nbar\""), both(isInstanceOf(String.class)).and(isEqualTo("foo\nbar")));
        assertThat(JsonHelper.parse("\"foo\\u000abar\""), both(isInstanceOf(String.class)).and(isEqualTo("foo\nbar")));
    }

    @Test
    public void testNumber() {
        assertThat(JsonHelper.parse("1"), both(isInstanceOf(Integer.class)).and(isEqualTo(1)));
        assertThat(JsonHelper.parse("-2"), both(isInstanceOf(Integer.class)).and(isEqualTo(-2)));
        assertThat(JsonHelper.parse("3.4"), both(isInstanceOf(Double.class)).and(isEqualTo(3.4)));
        assertThat(JsonHelper.parse("5e6"), both(isInstanceOf(Double.class)).and(isEqualTo(5e6)));
        assertThat(JsonHelper.parse("-7E8"), both(isInstanceOf(Double.class)).and(isEqualTo(-7E8)));
        assertThat(JsonHelper.parse(Long.toString(Long.MAX_VALUE)), both(isInstanceOf(Long.class)).and(isEqualTo(Long.MAX_VALUE)));
    }

    @Test
    public void testArray() {
        assertThat(JsonHelper.parse("[1,\"foo\",null,true]"), both(isInstanceOf(List.class)).and(isEqualTo(asList(1, "foo", null, true))));
    }

    @Test
    public void testObject() {
        assertThat(JsonHelper.parse("{\"x\":1,\"y\":2}"), both(isInstanceOf(Map.class)).and(isEqualTo(asMap("x", 1, "y", 2))));
    }

    @Test
    @SuppressWarnings("RawUseOfParameterizedType")
    public void testComplexJsonString() {
        List list1 = (List) JsonHelper.parse("[\"start\",{\"foo\":true,\"bar\":{\"x\":123,\"y\":45.6,\"z\":[]}}]");
        assertThat(list1.size(), is(2));
        assertThat((String) list1.get(0), is("start"));
        Map map1 = (Map) list1.get(1);
        assertThat(map1.size(), is(2));
        assertThat((Boolean) map1.get("foo"), is(true));
        Map map2 = (Map) map1.get("bar");
        assertThat(map2.size(), is(3));
        assertThat((Integer) map2.get("x"), is(123));
        assertThat((Double) map2.get("y"), is(45.6));
        List list2 = (List) map2.get("z");
        assertThat(list2.isEmpty());
    }

}
