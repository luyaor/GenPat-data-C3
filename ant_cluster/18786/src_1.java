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

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.zip.*;

import java.io.*;

/**
 * Creates a JAR archive.
 * 
 * @author James Davidson <a href="mailto:duncan@x180.com">duncan@x180.com</a>
 */
public class Jar extends Zip {

    private Manifest manifest;
    private Manifest execManifest;    

    public Jar() {
        super();
        archiveType = "jar";
        emptyBehavior = "create";
        setEncoding("UTF8");
    }

    public void setJarfile(File jarFile) {
        super.setZipfile(jarFile);
    }

    public void setManifest(File manifestFile) {
        if (!manifestFile.exists()) {
            throw new BuildException("Manifest file: " + manifestFile + " does not exist.", 
                                     getLocation());
        }
        
        InputStream is = null;
        try {
            is = new FileInputStream(manifestFile);
            Manifest newManifest = new Manifest(is);
            if (manifest == null) {
                manifest = getDefaultManifest();
            }
            manifest.merge(newManifest);
        }
        catch (IOException e) {
            throw new BuildException("Unable to read manifest file: " + manifestFile, e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    public void addMetainf(ZipFileSet fs) {
        // We just set the prefix for this fileset, and pass it up.
        fs.setPrefix("META-INF/");
        super.addFileset(fs);
    }

    protected void initZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException
    {
        // If no manifest is specified, add the default one.
        if (manifest == null) {
            execManifest = null;
        }
        else {
            execManifest = new Manifest();
            execManifest.merge(manifest);
        }
        zipDir(null, zOut, "META-INF/");
        super.initZipOutputStream(zOut);
    }

    private Manifest getDefaultManifest() throws IOException {
        String s = "/org/apache/tools/ant/defaultManifest.mf";
        InputStream in = this.getClass().getResourceAsStream(s);
        if (in == null) {
            throw new BuildException("Could not find: " + s);
        }
        return new Manifest(in);
    }   
    
    protected void finalizeZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException {

        if (execManifest == null) {
            execManifest = getDefaultManifest();
        }
        
        // time to write the manifest
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos);
        execManifest.write(writer);
        writer.flush();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        super.zipFile(bais, zOut, "META-INF/MANIFEST.MF", System.currentTimeMillis());
        super.finalizeZipOutputStream(zOut);

    }

    /**
     * Handle situation when we encounter a manifest file
     *
     * If we haven't been given one, we use this one.
     *
     * If we have, we merge the manifest in, provided it is a new file
     * and not the old one from the JAR we are updating
     */
    private void zipManifestEntry(InputStream is) throws IOException {
        if (execManifest == null) {
            execManifest = new Manifest(is);
        }
        else if (isAddingNewFiles()) {
            execManifest.merge(new Manifest(is));
        }
    }
    
    protected void zipFile(File file, ZipOutputStream zOut, String vPath)
        throws IOException
    {
        // If the file being added is META-INF/MANIFEST.MF, we merge it with the
        // current manifest 
        if (vPath.equalsIgnoreCase("META-INF/MANIFEST.MF"))  {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                zipManifestEntry(is);
            }
            catch (IOException e) {
                throw new BuildException("Unable to read manifest file: " + file, e);
            }
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        // do nothing
                    }
                }
            }
        } else {
            super.zipFile(file, zOut, vPath);
        }
    }

    protected void zipFile(InputStream is, ZipOutputStream zOut, String vPath, long lastModified)
        throws IOException
    {
        // If the file being added is META-INF/MANIFEST.MF, we merge it with the
        // current manifest 
        if (vPath.equalsIgnoreCase("META-INF/MANIFEST.MF"))  {
            try {
                zipManifestEntry(is);
            }
            catch (IOException e) {
                throw new BuildException("Unable to read manifest file: ", e);
            }
        } else {
            super.zipFile(is, zOut, vPath, lastModified);
        }
    }

    /**
     * Make sure we don't think we already have a MANIFEST next time this task
     * gets executed.
     */
    protected void cleanUp() {
        super.cleanUp();
    }
}
