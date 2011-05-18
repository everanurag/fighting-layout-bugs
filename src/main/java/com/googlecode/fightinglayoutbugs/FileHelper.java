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

import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.fightinglayoutbugs.StringHelper.amountString;

/**
 * @author Michael Tamm
 */
public class FileHelper {

    public static void createParentDirectoryIfNeeded(@Nonnull File file) {
        final File dir = checkNotNull(file, "Method parameter file must not be null.").getParentFile();
        try {
            if (dir != null && !dir.exists()) {
                FileUtils.forceMkdir(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + dir, e);
        }
    }

    public static void bytesToFile(@Nonnull byte[] bytes, @Nonnull File file) {
        checkNotNull(bytes, "Method parameter bytes must not be null.");
        FileHelper.createParentDirectoryIfNeeded(checkNotNull(file, "Method parameter file must not be null."));
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write " + amountString(bytes.length, "byte") + " to file: " + file, e);
        }

    }

    public static File createTempDir() throws IOException {
        final File baseDir = new File(System.getProperty("java.io.tmpdir"));
        if (!baseDir.isDirectory()) {
            throw new IOException("java.io.tmpdir (" + baseDir + ") is not a directory.");
        }
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int i = 0;
        while (!stackTrace[i].getClassName().equals(FileHelper.class.getName())) { ++i; }
        while (stackTrace[i].getClassName().equals(FileHelper.class.getName())) { ++i; }
        String prefix = stackTrace[i].getClassName();
        prefix = prefix.substring(prefix.lastIndexOf('.') + 1);
        File tempDir;
        boolean tempDirSuccessfullyCreated;
        do {
            final long randomLong = LazyInitRandom.nextLong();
            final String dirName = prefix + (randomLong < 0 ? randomLong : "-" + randomLong);
            tempDir = new File(baseDir, dirName);
            tempDirSuccessfullyCreated = !tempDir.exists() && tempDir.mkdir();
        } while (!tempDirSuccessfullyCreated);
        return tempDir;
    }

    protected FileHelper() {}

    private static class LazyInitRandom {

        private static Random c_random;
        private static RuntimeException c_initException;

        static {
            try {
                c_random = new Random();
            } catch (RuntimeException e) {
                c_initException = e;
            }
        }

        public static long nextLong() {
            if (c_random == null) {
                throw c_initException;
            }
            return c_random.nextLong();
        }

        private LazyInitRandom() {}
    }
}
