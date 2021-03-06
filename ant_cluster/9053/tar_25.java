/*
 * Copyright  2000-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.tools.ant.taskdefs;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;

/**
 * @version $Revision$
 */
public class GUnzipTest extends BuildFileTest {

    /** Utilities used for file operations */
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    public GUnzipTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/gunzip.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void test1() {
        expectBuildException("test1", "required argument missing");
    }

    public void test2() {
        expectBuildException("test2", "attribute src invalid");
    }

    public void testRealTest() throws java.io.IOException {
        executeTarget("realTest");
        assertTrue(FILE_UTILS.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }

    public void testTestGzipTask() throws java.io.IOException {
        executeTarget("testGzipTask");
        assertTrue(FILE_UTILS.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }

}
