/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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

import org.apache.tools.ant.*;
import java.io.*;
import java.util.*;

/**
 * Will set a Project property. Used to be a hack in ProjectHelper
 *
 * @author costin@dnt.ro
 */
public class Property extends Task {

    private String name;
    private String value;
    private String file;
    private String resource;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void init() throws BuildException {
        try {
            if ((name != null) && (value != null)) {
                String v = ProjectHelper.replaceProperties(value, project.getProperties());
                project.setProperty(name, v);
            }

            if (file != null) loadFile(file);

            if (resource != null) loadResource(resource);

        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private void loadFile (String name) {
        Properties props = new Properties();
        project.log("Loading " + name, project.MSG_VERBOSE);
        try {
            if (new File(name).exists()) {
                props.load(new FileInputStream(name));
                addProperties(props);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadResource( String name ) {
        Properties props = new Properties();
        project.log("Resource Loading " + name, project.MSG_VERBOSE);
        try {
            InputStream is = this.getClass().getResourceAsStream(name);
            if (is != null) {
                props.load(is);
                addProperties(props);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addProperties(Properties props) {
        Enumeration e = props.keys();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            String value = (String) props.getProperty(name);
            String v = ProjectHelper.replaceProperties(value, project.getProperties());
            project.setProperty(name, v);
        }
    }
}
