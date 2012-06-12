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

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ServiceLoader;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class GlobalProxyFactory implements URLStreamHandlerFactory {

    public GlobalProxyFactory() {
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
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
    
}
