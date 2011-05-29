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

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.Password;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;

import static com.googlecode.fightinglayoutbugs.HamcrestHelper.assertThat;
import static com.googlecode.fightinglayoutbugs.TestHelper.waitFor;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A simple HTTP server, which serves all files located under the <code>src/test/webapp</code> directory.
 *
 * @author Michael Tamm
 */
public class TestWebServer {

    private static final Logger LOG = Logger.getLogger(TestWebServer.class);

    private static int port;
    private static Server server;

    public static void main(String[] args) {
        TestWebServer.start();
    }

    public static synchronized void start() {
        LOG.info("Starting TestWebServer ...");
        port = SocketHelper.findFreePort();
        server = new Server();
        HashUserRealm realm = new HashUserRealm("Fighting Layout Bugs Realm");
        realm.put("admin", new Password("secret"));
        realm.addUserToRole("admin", "admin");
        server.addUserRealm(realm);
        File webappDir = new File("src/test/webapp");
        File webXml = new File(webappDir, "WEB-INF/web.xml");
        assertThat(webXml.exists());
        WebAppContext webAppContext = new WebAppContext(webappDir.getAbsolutePath(), "/");
        server.addHandler(webAppContext);
        Connector connector = new SocketConnector();
        connector.setPort(port);
        server.addConnector(connector);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start " + server + ".", e);
        }
        waitFor(3, SECONDS, new RunnableAssert("TestWebServer is started") { @Override public void run() {
            assertThat(server.isStarted());
            assertThat(SocketHelper.isBound(port));
        }});
    }

    public static synchronized int getPort() {
        if (server == null) {
            start();
        }
        return port;
    }
}
