/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
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

package org.apache.tools.ant.taskdefs;

import java.io.File;

import junit.framework.AssertionFailedError;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

/**
 * @author Nico Seessle <nico@seessle.de> 
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a> 
 * @version $Revision$
 */
public class AntTest extends TaskdefsTest { 
    
    private static final String TESTCASES_DIR = "src/etc/testcases";
    private static final String TASKDEFS_DIR = TESTCASES_DIR + "/taskdefs";

    public AntTest(String name) { 
        super(name);
    }    
    
    public void setUp() { 
        configureProject(TASKDEFS_DIR + "/ant.xml");
    }
    
    public void test1() { 
        expectBuildException("test1", "recursive call");
    }

    // target must be specified
    public void test2() { 
        expectBuildException("test2", "required argument not specified");
    }

    // Should fail since a recursion will occur...
    public void test3() { 
        expectBuildException("test1", "recursive call");
    }

    public void test4() { 
        expectBuildException("test4", "target doesn't exist");
    }

    public void test5() { 
        executeTarget("test5");
    }

    public void test6() { 
        executeTarget("test6");
    }

    public void testExplicitBasedir1() {
        File dir1 = getProjectDir();
        File dir2 = new File(TESTCASES_DIR);
        testBaseDirs("explicitBasedir1", 
                     new String[] {dir1.getAbsolutePath(), 
                                   dir2.getAbsolutePath()
                     });
    }

    public void testExplicitBasedir2() {
        File dir1 = getProjectDir();
        File dir2 = new File(TESTCASES_DIR);
        testBaseDirs("explicitBasedir2",
                     new String[] {dir1.getAbsolutePath(), 
                                   dir2.getAbsolutePath()
                     });
    }

    public void testInheritBasedir() {
        String basedir = getProjectDir().getAbsolutePath();
        testBaseDirs("inheritBasedir", new String[] {basedir, basedir});
    }

    public void testDoNotInheritBasedir() {
        File dir1 = getProjectDir();
        File dir2 = new File(TASKDEFS_DIR+"/ant");
        String basedir = getProjectDir().getAbsolutePath();
        testBaseDirs("doNotInheritBasedir",
                     new String[] {dir1.getAbsolutePath(), 
                                   dir2.getAbsolutePath()
                     });
    }

    public void testBasedirTripleCall() {
        File dir1 = getProjectDir();
        File dir2 = new File(TASKDEFS_DIR+"/ant");
        testBaseDirs("tripleCall", 
                     new String[] {dir1.getAbsolutePath(), 
                                   dir2.getAbsolutePath(),
                                   dir1.getAbsolutePath()
                     });
    }

    protected void testBaseDirs(String target, String[] dirs) {
        BasedirChecker bc = new BasedirChecker(dirs);
        project.addBuildListener(bc);
        executeTarget(target);
        AssertionFailedError ae = bc.getError();
        if (ae != null) {
            throw ae;
        }
    }

    private class BasedirChecker implements BuildListener {
        private String[] expectedBasedirs;
        private int calls = 0;
        private AssertionFailedError error;

        BasedirChecker(String[] dirs) {
            expectedBasedirs = dirs;
        }

        public void buildStarted(BuildEvent event) {}
        public void buildFinished(BuildEvent event) {}
        public void targetFinished(BuildEvent event){}
        public void taskStarted(BuildEvent event) {}
        public void taskFinished(BuildEvent event) {}
        public void messageLogged(BuildEvent event) {}

        public void targetStarted(BuildEvent event) {
            if (error == null) {
                try {
                    assertEquals(expectedBasedirs[calls++],
                                 event.getProject().getBaseDir().getAbsolutePath());
                } catch (AssertionFailedError e) {
                    error = e;
                }
            }
        }

        AssertionFailedError getError() {
            return error;
        }

    }

}
