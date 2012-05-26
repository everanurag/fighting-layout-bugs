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

package com.googlecode.fightinglayoutbugs.helpers;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Michael Tamm
 */
public class TestHelper {

    private static final long[] WAIT_FOR_DELAYS = {10, 20, 30, 40, 50, 50, 50, 50, 100, 100, 200, 200, 300, 300, 500};

    /**
     * Repetitively runs the given <code>runnableAssert</code> until it
     * succeeds without throwing an exception or error or until the
     * given <code>timeout</code> is reached.
     *
     * @throws TimeoutException if the given <code>runnableAssert</code> could not be executed successfully in the given time
     */
    public static void waitFor(long timeout, TimeUnit timeoutUnit, RunnableAssert runnableAssert) {
        boolean success = false;
        final long timeoutReached = System.currentTimeMillis() + timeoutUnit.toMillis(timeout);
        int i = 0;
        do {
            try {
                runnableAssert.run();
                success = true;
            } catch (Exception e) {
                if (System.currentTimeMillis() > timeoutReached) {
                    AssertionError assertionError = new AssertionError("Timeout while waiting for: " + runnableAssert + ".");
                    assertionError.initCause(e);
                    throw assertionError;
                }
                try {
                    Thread.sleep(WAIT_FOR_DELAYS[i]);
                } catch (InterruptedException e2) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Got interrupted while waiting for: " + runnableAssert + ".", e2);
                }
                if (i < WAIT_FOR_DELAYS.length - 1) {
                    ++i;
                }
            }
        } while (!success);
    }

    /**
     * Returns a {@link List} containing the given <code>objects</code>.
     */
    public static <T> List<T> asList(T... items) {
        return new ArrayWrapper<T>(items);
    }

    /**
     * Returns a {@link Set} containing the given <code>objects</code>.
     */
    public static <T> Set<T> asSet(T... objects) {
        final Set<T> result;
        result = new LinkedHashSet<T>();
        result.addAll(new ArrayWrapper<T>(objects));
        return result;
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

    /**
     * This class is similar to the class which is returned by {@link Arrays#asList}
     * but does not clone the wrapped array if {@link #toArray} is called.
     */
    private static class ArrayWrapper<E> extends AbstractList<E> {
        private final E[] _wrappedArray;

        private ArrayWrapper(E[] arrayToWrap) {
            _wrappedArray = arrayToWrap;
        }

        @Override
        public E get(int index) {
            return _wrappedArray[index];
        }

        @Override
        public int size() {
            return _wrappedArray.length;
        }

        /**
         * Returns the wrapped array.
         */
        @Override
        public Object[] toArray() {
            return _wrappedArray;
        }
    }

    private TestHelper() {}
}
