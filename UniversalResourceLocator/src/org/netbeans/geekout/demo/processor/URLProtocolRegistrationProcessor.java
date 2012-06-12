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
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
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
        TypeMirror connType = processingEnv.getElementUtils().getTypeElement("java.net.URLConnection").asType();
        TypeMirror urlType = processingEnv.getElementUtils().getTypeElement("java.net.URL").asType();
        for (Element e : roundEnv.getElementsAnnotatedWith(URLProtocolRegistration.class)) {
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR, "Class has to be public", e
                );
            }
            if (e.getKind() == ElementKind.METHOD) {
                generateWrapperAroundMethod(e, roundEnv);
                continue;
            }
            
            
            if (!processingEnv.getTypeUtils().isAssignable(e.asType(), factoryType)) {
                if (!processingEnv.getTypeUtils().isAssignable(e.asType(), connType)) {
                    processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR, "Has to implement URLStreamHandlerFactory or URLConnection", e
                    );
                } else {
                    boolean found = false;
                    for (Element c : e.getEnclosedElements()) {
                        if (ElementKind.CONSTRUCTOR != c.getKind()) {
                            continue;
                        }
                        ExecutableElement ee = (ExecutableElement)c;
                        if (ee.getParameters().size() == 1 && ee.getParameters().get(0).asType() == urlType) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.ERROR, "Must have constructor with URL parameter", e
                        );
                    }
                }
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
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), e);
            }
        }
        return true;
    }

    private void generateWrapperAroundMethod(Element e, RoundEnvironment roundEnv) {
        String methodName = e.getSimpleName().toString();
        String binaryName = e.getEnclosingElement().getSimpleName().toString();
        String packageName = processingEnv.getElementUtils().getPackageOf(e).getQualifiedName().toString();
        URLProtocolRegistration upr = e.getAnnotation(URLProtocolRegistration.class);
        for (String p : upr.protocol()) {
            try {
                String clsName = binaryName + "$url$" + p;
                JavaFileObject wraper = processingEnv.getFiler().createSourceFile(packageName + '.' + clsName, e);
                Writer w = wraper.openWriter();
                String code = 
                        "package " + packageName + ";\n"
                        + "\n"
                        + "import java.io.IOException;\n"
                        + "import java.io.InputStream;\n"
                        + "import java.io.UnsupportedEncodingException;\n"
                        + "import java.net.URL;\n"
                        + "import java.net.URLConnection;\n"
                        + "import org.netbeans.geekout.demo.URLProtocolRegistration;\n"
                        + "\n"
                        + "@URLProtocolRegistration(protocol=\"" + p + "\")\n"
                        + "public class " + clsName + " extends URLConnection {\n"
                        + "    private final InputStream is;\n"
                        + "\n"
                        + "    public " + clsName + "(URL url) throws Exception {\n"
                        + "        super(url);\n"
                        + "        is = " + binaryName + "." + methodName + "(url);\n"
                        + "    }\n"
                        + "\n"
                        + "    @Override\n"
                        + "    public void connect() throws IOException {\n"
                        + "    }\n"
                        + "\n"
                        + "    @Override\n"
                        + "    public InputStream getInputStream() throws IOException {\n"
                        + "        return is;\n"
                        + "    }\n"
                        + "}\n"
                        + "";
                w.append(code);
                w.close();
            } catch (IOException ex) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), e);
            }
            
        }
        
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
        Element element, AnnotationMirror annotation, 
        ExecutableElement member, String userText
    ) {
        final String nm = member.getSimpleName().toString();
//        System.err.println("  strn: " + nm);
//        System.err.println("user: " + userText);
        if (nm.equals("protocol")) {
            List<MyCmpl> arr = Arrays.asList(new MyCmpl[] { 
                new MyCmpl("http"),
                new MyCmpl("ftp"),
                new MyCmpl("file")
            });
//            System.err.println("array out: " + arr);
            return arr;
        }
        return super.getCompletions(element, annotation, member, userText);
    }
    
    private static final class MyCmpl implements Completion {
        private final String protocol;

        public MyCmpl(String protocol) {
            this.protocol = protocol;
        }

        @Override
        public String getValue() {
            return '"' + protocol ;
        }

        @Override
        public String getMessage() {
            return "Your favorite protocol: " + protocol;
        }
    
    }
}
