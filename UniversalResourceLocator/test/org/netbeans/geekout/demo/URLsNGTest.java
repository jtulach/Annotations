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
import java.io.InputStream;
import java.net.URL;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Jaroslav Tulach
 */
public class URLsNGTest {
    public URLsNGTest() {
    }
    
    @BeforeClass
    public void setUpClass() {
        URLs.initialize();
    }
    
    @Test
    public void testGeekProtocol() throws Exception {
        URL u = new URL("geekout://Jarda");
        InputStream is = u.openStream();
        String res = readFully(is);
        assertEquals(res, "Hello Jarda!", "Greetings are OK");
    }

    private String readFully(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return sb.toString();
    }
}
