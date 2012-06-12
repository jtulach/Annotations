/*
 * The MIT License
 *
 * Copyright 2012 Jaroslav Tulach <jtulach@netbeans.org>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.netbeans.geekout.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class GlobalProxyFactory implements URLStreamHandlerFactory {

    public GlobalProxyFactory() {
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        URLStreamHandler res = seekInProtocols(protocol);
        if (res != null) {
            return res;
        }
        return seekInServices(protocol);
    }

    private URLStreamHandler seekInServices(String protocol) {
        ServiceLoader<URLStreamHandlerFactory> res = ServiceLoader.load(URLStreamHandlerFactory.class);
        for (URLStreamHandlerFactory f : res) {
            URLStreamHandler handler = f.createURLStreamHandler(protocol);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }

    private URLStreamHandler seekInProtocols(String protocol) {
        try {
            Enumeration<URL> urls = GlobalProxyFactory.class.getClassLoader().getResources("META-INF/urls/" + protocol);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                for (String line : lines(url)) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    final Class<?> implClass = Class.forName(line);
                    if (URLStreamHandlerFactory.class.isAssignableFrom(implClass)) {
                        Object instance = implClass.newInstance();
                        URLStreamHandlerFactory f = (URLStreamHandlerFactory)instance;
                        URLStreamHandler handler = f.createURLStreamHandler(protocol);
                        if (handler != null) {
                            return handler;
                        }
                    }
                    if (URLConnection.class.isAssignableFrom(implClass)) {
                        return new GenericHandler(implClass.asSubclass(URLConnection.class));
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(GlobalProxyFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Iterable<String> lines(URL url) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(url.openStream()));
        List<String> arr = new ArrayList<String>();
        for (;;) {
            String l = r.readLine();
            if (l == null) {
                return arr;
            }
            l = l.trim();
            if (!l.isEmpty()) {
                arr.add(l);
            }
        }
    }
    
}
