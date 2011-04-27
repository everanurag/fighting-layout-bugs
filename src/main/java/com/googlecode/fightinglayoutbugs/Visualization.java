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

package com.googlecode.fightinglayoutbugs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Visualization {

    public interface Listener {
        void algorithmStepFinished(String stepDescription, Object tempResult);
        void algorithmFinished(String stepDescription, Object result);
    }

    private static final List<Listener> LISTENERS = new ArrayList<Listener>();

    public static void registerListener(Listener listener) {
        synchronized (Visualization.class) {
            LISTENERS.add(listener);
        }
    }

    public static void unregisterListener(Listener listener) {
        synchronized (Visualization.class) {
            Iterator<Listener> i = LISTENERS.iterator();
            while (i.hasNext()) {
                if (i.next() == listener) {
                    i.remove();
                }
            }
        }
    }

    /**
     * Calls {@link Listener#algorithmStepFinished} for all registered listeners.
     */
    public static void algorithmStepFinished(String stepDescription, Object tempResult) {
        List<Listener> listeners;
        synchronized (Visualization.class) {
            listeners = new ArrayList<Listener>(LISTENERS);
        }
        for (Listener listener : listeners) {
            listener.algorithmStepFinished(stepDescription, tempResult);
        }
    }

    /**
     * Calls {@link Listener#algorithmFinished} for all registered listeners.
     */
    public static void algorithmFinished(String stepDescription, Object result) {
        List<Listener> listeners;
        synchronized (Visualization.class) {
            listeners = new ArrayList<Listener>(LISTENERS);
        }
        for (Listener listener : listeners) {
            listener.algorithmFinished(stepDescription, result);
        }
    }

    private Visualization() {}
}
