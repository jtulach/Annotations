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
package org.netbeans.geekout.demo.processor;

import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.netbeans.geekout.demo.URLProtocolRegistration;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("org.netbeans.geekout.demo.URLProtocolRegistration")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class URLProtocolRegistrationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeMirror factoryType = processingEnv.getElementUtils().getTypeElement("java.net.URLStreamHandlerFactory").asType();
        for (Element e : roundEnv.getElementsAnnotatedWith(URLProtocolRegistration.class)) {
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Class has to be public", e
                );
            }
            if (!processingEnv.getTypeUtils().isAssignable(e.asType(), factoryType)) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Has to implement URLStreamHandlerFactory", e
                );
            }
            
            try {
                URLProtocolRegistration upr = e.getAnnotation(URLProtocolRegistration.class);
                for (String p : upr.protocol()) {
                    FileObject res = processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT, "",
                        "META-INF/urls/" + p, e
                    );
                    String binaryName = processingEnv.getElementUtils().getBinaryName((TypeElement)e).toString();
                    res.openWriter().append(binaryName).append("\n").close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), e);
            }
        }
        return true;
    }
    
}
