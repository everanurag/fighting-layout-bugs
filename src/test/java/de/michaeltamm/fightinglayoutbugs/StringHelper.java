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

import java.util.*;

/**
 * @author Michael Tamm
 */
public class StringHelper {

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
            final String s  = (String) o;
            sb.append('"').append(s.replace("\t", "\\t").replace("\r", "\\r").replace("\n", "\\n")).append('"');
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

    private StringHelper() {}
}
