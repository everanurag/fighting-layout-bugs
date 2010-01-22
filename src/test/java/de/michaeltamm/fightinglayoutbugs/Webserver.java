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

import static de.michaeltamm.fightinglayoutbugs.HamcrestHelper.assertThat;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import java.io.File;

/**
 * A simple HTTP server, which serves all files located under the <code>src/test/webapp</code> directory.
 *
 * @author Michael Tamm
 */
public class Webserver {

    private Server _server;
    private int _port;

    public Webserver() {
        _server = new Server();
        File webappDir = new File("src/test/webapp");
        File webXml = new File(webappDir, "WEB-INF/web.xml");
        assertThat(webXml.exists());
        WebAppContext webAppContext = new WebAppContext(webappDir.getAbsolutePath(), "/");
        _server.addHandler(webAppContext);
    }

    public void start() {
        _port = SocketHelper.findFreePort();
        Connector connector = new SocketConnector();
        connector.setPort(_port);
        _server.addConnector(connector);
        try {
            _server.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start " + _server, e);
        }
    }

    public void stop() {
        try {
            _server.stop();
        } catch (Exception e) {
            System.err.print("Could not stop " + _server + ": ");
            e.printStackTrace(System.err);
        }
    }

    public int getPort() {
        return _port;
    }
}
