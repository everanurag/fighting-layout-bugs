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

import java.util.*;

/**
 * @author Michael Tamm
 */
public class StringHelper {

    /**
     * Null-safe string comparison.
     */
    public static boolean equals(String s1, String s2) {
        return (s1 == null ? s2 == null : s1.equals(s2));
    }

    /**
     * Removes leading and trailing whitespaces from <code>s</code> and replaces
     * all sequences of whitespaces inside <code>s</code> with a single space character.
     */
    public static String normalizeSpace(String s) {
        final String result;
        if (s == null) {
            result = null;
        } else {
            final int n = s.length();
            final StringBuilder sb = new StringBuilder(n);
            boolean lastCharacterWasWhitespace = true;
            for (int i = 0; i < n; ++i) {
                final char c = s.charAt(i);
                if (Character.isWhitespace(c)) {
                    lastCharacterWasWhitespace = true;
                } else {
                    if (lastCharacterWasWhitespace && sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(c);
                    lastCharacterWasWhitespace = false;
                }
            }
            result = sb.toString();
        }
        return result;
    }

    /**
     * Returns <code>"1 " + noun</code>, if <code>amount == 1</code>,
     * or <code>amount + " " + {@link #englishPluralOf englishPluralOf}(noun)</code> otherwise.
     */
    public static String amountString(long amount, String noun) {
        return (amount == 1 ? "1 " + noun : amount + " " + englishPluralOf(noun));
    }

    public static String englishPluralOf(String noun) {
        final String result;
        if (noun.endsWith("is")) {
            // E.g. "thesis" => "theses"
            result = noun.substring(0, noun.length() - 2) + "es";
        } else if (noun.endsWith("fe")) {
            // E.g. "knife" => "knifes"
            result = noun.substring(0, noun.length() - 2) + "ves";
        } else if (noun.endsWith("ay") || noun.endsWith("ey") || noun.endsWith("iy") || noun.endsWith("oy") || noun.endsWith("uy")) {
            // E.g. "key" => "keys"
            result = noun + "s";
        } else if (noun.endsWith("ch") || noun.endsWith("s") || noun.endsWith("sh") || noun.endsWith("x") || noun.endsWith("z")) {
            // E.g. "box" => "boxes"
            result = noun + "es";
        } else if (noun.endsWith("y")) {
            // E.g. "entry" => "entries"
            result = noun.substring(0, noun.length() - 1) + "ies";
        } else if (noun.endsWith("f")) {
            // E.g. "self" => "selves"
            result = noun.substring(0, noun.length() - 1) + "ves";
        } else {
            result = noun + "s";
        }
        return result;
    }

    /**
     * Converts the given object to a string useful for log messages
     * with special handling of {@link String}s, arrays, {@link Collection}s,
     * {@link Map}s, {@link Class}es and {@link Calendar}s.
     */
    public static String asString(Object o) {
        final StringBuilder sb = new StringBuilder();
        objectAsString(o, sb);
        return sb.toString();
    }

    private static void objectAsString(Object o, StringBuilder sb) {
        if (o == null) {
            sb.append("null");
        } else if (o instanceof String) {
            stringAsString((String) o, sb);
        } else if (o.getClass().isArray()) {
            arrayAsString(o, sb);
        } else if (o instanceof Collection) {
            collectionAsString((Collection) o, sb);
        } else if (o instanceof Map) {
            mapAsString((Map) o, sb);
        } else if (o instanceof Class){
            sb.append(((Class) o).getName());
        } else if (o instanceof Calendar) {
            sb.append(((Calendar) o).getTime());
        } else {
            sb.append(o);
        }
    }

    private static void stringAsString(String s, StringBuilder sb) {
        if (s == null) {
            sb.append("null");
        } else {
            sb.append('"').append(s.replace("\\", "\\\\").replace("\f", "\\f").replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n").replace("\0", "\\0")).append('"');
        }
    }

    private static void arrayAsString(Object array, StringBuilder sb) {
        if (array instanceof boolean[]) {
            sb.append(Arrays.toString((boolean[]) array));
        } else if (array instanceof byte[]) {
            sb.append(Arrays.toString((byte[]) array));
        } else if (array instanceof char[]) {
            sb.append(Arrays.toString((char[]) array));
        } else if (array instanceof short[]) {
            sb.append(Arrays.toString((short[]) array));
        } else if (array instanceof int[]) {
            sb.append(Arrays.toString((int[]) array));
        } else if (array instanceof long[]) {
            sb.append(Arrays.toString((long[]) array));
        } else if (array instanceof float[]) {
            sb.append(Arrays.toString((float[]) array));
        } else if (array instanceof double[]) {
            sb.append(Arrays.toString((double []) array));
        } else {
            objectArrayAsString((Object[]) array, sb);
        }
    }

    private static void objectArrayAsString(Object[] array, StringBuilder sb) {
        sb.append('[');
        if (array.length > 0) {
            sb.append(asString(array[0]));
            for (int i = 1; i < array.length; ++i) {
                sb.append(", ");
                objectAsString(array[i], sb);
            }
        }
        sb.append(']');
    }

    private static void collectionAsString(Collection<?> collection, StringBuilder sb) {
        sb.append('{');
        final Iterator<?> i = collection.iterator();
        if (i.hasNext()) {
            objectAsString(i.next(), sb);
            while (i.hasNext()) {
                sb.append(", ");
                objectAsString(i.next(), sb);
            }
        }
        sb.append('}');
    }

    private static void mapAsString(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        final Iterator<?> i = map.entrySet().iterator();
        if (i.hasNext()) {
            Map.Entry<?, ?> mapEntry = (Map.Entry) i.next();
            objectAsString(mapEntry.getKey(), sb);
            sb.append(" => ");
            objectAsString(mapEntry.getValue(), sb);
            while (i.hasNext()) {
                sb.append(", ");
                mapEntry = (Map.Entry) i.next();
                objectAsString(mapEntry.getKey(), sb);
                sb.append(" => ");
                objectAsString(mapEntry.getValue(), sb);
            }
        }
        sb.append('}');
    }

    protected StringHelper() {}
}
