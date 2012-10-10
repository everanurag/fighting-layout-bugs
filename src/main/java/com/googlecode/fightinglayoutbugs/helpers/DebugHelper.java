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

import com.google.common.base.Joiner;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.internal.BuildInfo;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class DebugHelper {

    public static String getDiagnosticInfo(WebDriver driver) {
        StringWriter sw = new StringWriter();
        try {
            appendFlbVersion(sw);
            sw.append('\n');
            appendSeleniumVersion(sw);
            sw.append('\n');
            appendWebDriverInfo(sw, driver);
            sw.append('\n');
            appendBrowserVersion(sw, driver);
            sw.append('\n');
            appendJavaVersion(sw);
            sw.append('\n');
            appendOperatingSystemVersion(sw);
            sw.append('\n');
            appendClassLoaderHierarchy(sw);
        } catch (Throwable t) {
            sw.append("getDiagnosticInfo(...) failed: ");
            t.printStackTrace(new PrintWriter(sw));
        }
        return sw.toString();
    }

    private static void appendFlbVersion(StringWriter sw) {
        sw.append("FLB Version: ");
        try {
            URL url = DebugHelper.class.getProtectionDomain().getCodeSource().getLocation();
            File file = new File(url.toURI());
            JarFile jar = new JarFile(file);
            Manifest manifest = jar.getManifest();
            Attributes buildInfo = manifest.getAttributes("Build-Info");
            String flbVersion = buildInfo.getValue("FLB-Version");
            sw.append(flbVersion);
        } catch (Throwable t) {
            t.printStackTrace(new PrintWriter(sw));
        }
    }

    private static void appendSeleniumVersion(StringWriter sw) {
        sw.append("Selenium Version: ");
        try {
            final BuildInfo buildInfo = new BuildInfo();
            sw.append(buildInfo.getReleaseLabel());
        } catch (Throwable t) {
            t.printStackTrace(new PrintWriter(sw));
        }
    }

    private static void appendWebDriverInfo(StringWriter sw, WebDriver driver) {
        sw.append("WebDriver: ");
        sw.append(driver == null ? "null" : driver.getClass().getName());
    }

    private static void appendBrowserVersion(StringWriter sw, WebDriver driver) {
        if (driver != null) {
            sw.append("Browser: ");
            if (driver instanceof JavascriptExecutor) {
                try {
                    Object userAgent = ((JavascriptExecutor) driver).executeScript("return window.navigator.userAgent");
                    sw.append(userAgent == null ? "null" : userAgent.toString());
                } catch (Throwable t) {
                    t.printStackTrace(new PrintWriter(sw));
                }
            } else {
                sw.append("???");
            }
        }
    }

    private static void appendJavaVersion(StringWriter sw) {
        sw.append("Java: ");
        try {
            String javaVersion = System.getProperty("java.version", "???");
            sw.append(javaVersion);
            String javaVendor = System.getProperty("java.vendor");
            if (javaVendor != null) {
                sw.append(" (").append(javaVendor).append(")");
            }
        } catch (Throwable t) {
            t.printStackTrace(new PrintWriter(sw));
        }
    }

    private static void appendOperatingSystemVersion(StringWriter sw) {
        sw.append("OS: ");
        try {
            String osName = System.getProperty("os.name", "???");
            sw.append(osName).append(" ");
            String osVersion = System.getProperty("os.version", "???");
            sw.append(osVersion).append(" ");
            String osArch = System.getProperty("os.arch", "???");
            sw.append(osArch);
        } catch (Throwable t) {
            t.printStackTrace(new PrintWriter(sw));
        }
    }

    private static void appendClassLoaderHierarchy(StringWriter sw) {
        sw.append("Classpath:");
        try {
            ClassPath classPath = new ClassPath();
            if (classPath.isEmpty()) {
                sw.append(" ???");
            } else {
                sw.append("\n\t").append(Joiner.on("\n\t").join(classPath));
            }
        } catch (Throwable t) {
            sw.append(" ");
            t.printStackTrace(new PrintWriter(sw));
        }
    }
}
