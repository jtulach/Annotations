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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class GenericHandler extends URLStreamHandler {
    private final Class<? extends URLConnection> implClass;

    public GenericHandler(Class<? extends URLConnection> implClass) {
        this.implClass = implClass;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        try {
            return implClass.getConstructor(URL.class).newInstance(u);
        } catch (Exception ex) {
            throw new IllegalStateException("Needs to have constructor with URL parameter: " + implClass, ex);
        }
    }
    
}
