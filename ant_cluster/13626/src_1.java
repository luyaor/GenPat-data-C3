/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.tools.ant.taskdefs.optional.junit.formatter;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Not exactly rocket science test.. dooh !
 *
 * @author <a href="mailto:sbailliez@apache.org">Stephane Bailliez</a>
 */
public class FilterStackFormatterTest extends TestCase {

    public FilterStackFormatterTest(String s) {
        super(s);
    }

    protected String trace = "";
    protected String expected = "";
    protected NullFormatter wrapped;

    protected void setUp() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        pw.println("org.apache.test.ClassName.method(ClassName.java:125)");
        pw.println("\tat org.apache.test.C1.m1(C1.java:125)");
        pw.println("\tat junit.framework.TestCase.m0(TestCase.java:999)");
        pw.println("\tat org.apache.test.C2.m1(C2.java:125)");
        pw.println("\tat junit.framework.TestResult.m1(TestResult.java:999)");
        pw.println("\tat org.apache.test.C3.m1(C3.java:125)");
        pw.println("\tat junit.framework.TestSuite.m2(TestSuite.java:999)");
        pw.println("\tat org.apache.test.C4.m1(C4.java:125)");
        pw.println("\tat junit.framework.Assert.m3(Assert.java:999)");
        pw.println("\tat junit.swingui.TestRunner.m3(TestRunner.java:999)");
        pw.println("\tat junit.awtui.TestRunner.m3(TestRunner.java:999)");
        pw.println("\tat org.apache.test.C5.m1(C5.java:125)");
        pw.println("\tat junit.textui.TestRunner.m3(TestRunner.java:999)");
        pw.println("\tat java.lang.reflect.Method.invoke(Method.java:999)");
        pw.println("\tat org.apache.tools.ant.C.m(C.java:999)");
        pw.println("\tat org.apache.test.C6.m1(C6.java:125)");
        trace = sw.toString();
        sw.getBuffer().setLength(0);

        pw.println("org.apache.test.ClassName.method(ClassName.java:125)");
        pw.println("\tat org.apache.test.C1.m1(C1.java:125)");
        pw.println("\tat org.apache.test.C2.m1(C2.java:125)");
        pw.println("\tat org.apache.test.C3.m1(C3.java:125)");
        pw.println("\tat org.apache.test.C4.m1(C4.java:125)");
        pw.println("\tat org.apache.test.C5.m1(C5.java:125)");
        pw.println("\tat org.apache.test.C6.m1(C6.java:125)");
        expected = sw.toString();

        wrapped = new NullFormatter();
    }

    public void testFiltering() {
        FilterStackFormatter wrapper = new FilterStackFormatter(wrapped);
        wrapper.testFailed(0, "", trace);
        assertEquals(expected, wrapped.trace);
    }

    public class NullFormatter implements Formatter {
        protected String trace;

        public void setOutput(OutputStream out) {
        }

        public void testStarted(String testname) {
        }

        public void setSystemOutput(String out) {
        }

        public void testEnded(String testname) {
        }

        public void setSystemError(String err) {
        }

        public void testFailed(int status, String testname, String trace) {
            this.trace = trace;
        }

        public void testStdOutLine(String testname, String line) {
        }

        public void testStdErrLine(String testname, String line) {
        }

        public void testRunSystemProperties(Properties props) {
        }

        public void testRunStarted(int testcount) {
        }

        public void testRunEnded(long elapsedtime) {
        }

        public void testRunStopped(long elapsedtime) {
        }
    }
}
