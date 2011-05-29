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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

/**
 * Helper class for dealing with <a href="http://www.json.org/">JSON</a>.
 *
 * @author Michael Tamm
 */
public class JsonHelper {

    /**
     * Converts the given {@code json} string into an Java object
     * using the following mapping:<ul>
     *     <li><tt>"null" => null</tt></li>
     *     <li><tt>"false" => false</tt></li>
     *     <li><tt>"true" => true</tt></li>
     *     <li><tt>"</tt><i>string</i><tt>" => </tt>{@link String}</li>
     *     <li><tt>"</tt><i>number</i><tt>" => </tt>{@link Number}</li>
     *     <li><tt>"</tt><i>array</i><tt>" => </tt>{@link List}</li>
     *     <li><tt>"</tt><i>object</i><tt>" => </tt>{@link Map}</li>
     * </ul>
     */
    public static Object parse(String json) {
        if (json == null || "null".equals(json)) {
            return null;
        } else if ("false".equals(json)) {
            return false;
        } else if ("true".equals(json)) {
            return true;
        } else {
            try {
                if (json.startsWith("\"")) {
                    JSONTokener tokener = new JSONTokener(json);
                    tokener.next();
                    return tokener.nextString('"');
                } else if (json.startsWith("[")) {
                    JSONTokener tokener = new JSONTokener(json);
                    return convert(tokener.nextValue());
                } else if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    return convert(jsonObject);
                } else {
                    JSONTokener tokener = new JSONTokener(json);
                    return tokener.nextValue();
                }
            } catch (JSONException e) {
                throw new RuntimeException("Could not parse " + StringHelper.asString(json) + ".", e);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    private static Object convert(Object object) {
        if (object == JSONObject.NULL) {
            return null;
        } else if (object instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) object;
            Map map = new HashMap();
            Iterator<String> i = (Iterator<String>) jsonObject.keys();
            while (i.hasNext()) {
                String key = i.next();
                Object value = jsonObject.opt(key);
                Object convertedValue = convert(value);
                map.put(key, convertedValue);
            }
            return map;
        } else if (object instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) object;
            int n = jsonArray.length();
            List list = new ArrayList(n);
            for (int i = 0; i < n; ++i) {
                Object value = jsonArray.opt(i);
                Object convertedValue = convert(value);
                list.add(convertedValue);
            }
            return list;
        } else {
            return object;
        }
    }
}
