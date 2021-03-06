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

package org.apache.tools.ant.taskdefs.optional.ejb;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

import javax.xml.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class GenericDeploymentTool implements EJBDeploymentTool {
    /** Private constants that are used when constructing the standard jarfile */
    protected static final String META_DIR  = "META-INF/";
    protected static final String EJB_DD    = "ejb-jar.xml";

    /** Stores a handle to the directory to put the Jar files in */
    private File destDir = null;
    
    /**
     * Instance variable that determines whether to use a package structure
     * of a flat directory as the destination for the jar files.
     */
    private boolean flatDestDir = false;
    
    /** Instance variable that marks the end of the 'basename' */
    private String basenameTerminator = "-";

    /** Instance variable that stores the suffix for the generated jarfile. */
    private String genericjarsuffix = "-generic.jar";

    /**
     * The task to which this tool belongs.
     */
    private Task task;
    
    /**
     * Setter used to store the value of destination directory prior to execute()
     * being called.
     * @param inDir the destination directory.
     */
    public void setDestdir(File inDir) {
        this.destDir = inDir;
    }

    /**
     * Get the desitination directory.
     */
    protected File getDestDir() {
        return destDir;
    }
    

    /**
     * Set the task which owns this tool
     */
    public void setTask(Task task) {
        this.task = task;
    }
       
    /**
     * Get the task for this tool.
     */
    protected Task getTask() {
        return task;
    }

    /**
     * Get the basename terminator.
     */
    protected String getBasenameTerminator() {
        return basenameTerminator;
    }
    
    /**
     * Setter used to store the suffix for the generated jar file.
     * @param inString the string to use as the suffix.
     */
    public void setGenericjarsuffix(String inString) {
        this.genericjarsuffix = inString;
    }

    /**
     * Configure this tool for use in the ejbjar task.
     */
    public void configure(String basenameTerminator, boolean flatDestDir) {
        this.basenameTerminator = basenameTerminator;
        this.flatDestDir = flatDestDir;
    }

    /**
     * Utility method that encapsulates the logic of adding a file entry to
     * a .jar file.  Used by execute() to add entries to the jar file as it is
     * constructed.
     * @param jStream A JarOutputStream into which to write the
     *        jar entry.
     * @param iStream A FileInputStream from which to read the
     *        contents the file being added.
     * @param filename A String representing the name, including
     *        all relevant path information, that should be stored for the entry
     *        being added.
     */
    protected void addFileToJar(JarOutputStream jStream,
                                FileInputStream iStream,
                                String          filename)
        throws BuildException {
        try {
            // Create the zip entry and add it to the jar file
            ZipEntry zipEntry = new ZipEntry(filename);
            jStream.putNextEntry(zipEntry);
            
            // Create the file input stream, and buffer everything over
            // to the jar output stream
            byte[] byteBuffer = new byte[2 * 1024];
            int count = 0;
            do {
                jStream.write(byteBuffer, 0, count);
                count = iStream.read(byteBuffer, 0, byteBuffer.length);
            } while (count != -1);
            
            // Close up the file input stream for the class file
            iStream.close();
        }
        catch (IOException ioe) {
            String msg = "IOException while adding entry "
                         + filename + "to jarfile."
                         + ioe.getMessage();
            throw new BuildException(msg, ioe);
        }
    }

    protected DescriptorHandler getDescriptorHandler(File srcDir) {
        return new DescriptorHandler(srcDir);
    }
    
    public void processDescriptor(File srcDir, String descriptorFilename, SAXParser saxParser) {
        try {
            DescriptorHandler handler = getDescriptorHandler(srcDir);
            
            /* Parse the ejb deployment descriptor.  While it may not
             * look like much, we use a SAXParser and an inner class to
             * get hold of all the classfile names for the descriptor.
             */
            saxParser.parse(new InputSource
                            (new FileInputStream
                             (new File(srcDir, descriptorFilename))),
                            handler);
                            
            Hashtable ejbFiles = handler.getFiles();
            
            String baseName = "";
            
            // Work out what the base name is
            int lastSeparatorIndex = descriptorFilename.lastIndexOf(File.separator);
            int endBaseName = -1;
            if (lastSeparatorIndex != -1) {
                endBaseName = descriptorFilename.indexOf(basenameTerminator, 
                                                         lastSeparatorIndex);
            }
            else {
                endBaseName = descriptorFilename.indexOf(basenameTerminator);
            }
            
            if (endBaseName != -1) {
                baseName = descriptorFilename.substring(0, endBaseName);
            }

            // First the regular deployment descriptor
            ejbFiles.put(META_DIR + EJB_DD,
                         new File(srcDir, descriptorFilename));
                         
            addVendorFiles(ejbFiles, srcDir, baseName);

            // Lastly create File object for the Jar files. If we are using
            // a flat destination dir, then we need to redefine baseName!
            if (flatDestDir && baseName.length() != 0) {
                int startName = baseName.lastIndexOf(File.separator);
                int endName   = baseName.length();
                baseName = baseName.substring(startName, endName);
            }
            
            File jarFile = getVendorOutputJarFile(baseName);
            
            // By default we assume we need to build.
            boolean needBuild = true;

            if (jarFile.exists()) {
                long    lastBuild = jarFile.lastModified();
                Iterator fileIter = ejbFiles.values().iterator();
                // Set the need build to false until we find out otherwise.
                needBuild = false;

                // Loop through the files seeing if any has been touched
                // more recently than the destination jar.
                while( (needBuild == false) && (fileIter.hasNext()) ) {
                    File currentFile = (File) fileIter.next();
                    needBuild = ( lastBuild < currentFile.lastModified() );
                }
            }
            
            // Check to see if we need a build and start
            // doing the work!
            if (needBuild) {
                // Log that we are going to build...
                getTask().log( "building "
                              + jarFile.getName()
                              + " with "
                              + String.valueOf(ejbFiles.size())
                              + " files",
                              Project.MSG_INFO);
    
                // Use helper method to write the jarfile
                writeJar(baseName, jarFile, ejbFiles);

            }
            else {
                // Log that the file is up to date...
                getTask().log(jarFile.toString() + " is up to date.",
                              Project.MSG_INFO);
            }

        }
        catch (SAXException se) {
            String msg = "SAXException while parsing '"
                + descriptorFilename.toString()
                + "'. This probably indicates badly-formed XML."
                + "  Details: "
                + se.getMessage();
            throw new BuildException(msg, se);
        }
        catch (IOException ioe) {
            String msg = "IOException while parsing'"
                + descriptorFilename.toString()
                + "'.  This probably indicates that the descriptor"
                + " doesn't exist. Details:"
                + ioe.getMessage();
            throw new BuildException(msg, ioe);
        }
    }
    
    /**
     * Add any vendor specific files which should be included in the 
     * EJB Jar.
     */
    protected void addVendorFiles(Hashtable ejbFiles, File srcDir, String baseName) {
    }


    /**
     * Get the vendor specific name of the Jar that will be output. The modification date
     * of this jar will be checked against the dependent bean classes.
     */
    File getVendorOutputJarFile(String baseName) {
        return new File(destDir, baseName + genericjarsuffix);
    }

    /**
     * Method used to encapsulate the writing of the JAR file. Iterates over the
     * filenames/java.io.Files in the Hashtable stored on the instance variable
     * ejbFiles.
     */
    protected void writeJar(String baseName, File jarfile, Hashtable files) throws BuildException{
        JarOutputStream jarStream = null;
        Iterator entryIterator = null;
        String entryName = null;
        File entryFile = null;

        try {
            /* If the jarfile already exists then whack it and recreate it.
             * Should probably think of a more elegant way to handle this
             * so that in case of errors we don't leave people worse off
             * than when we started =)
             */
            if (jarfile.exists()) {
                jarfile.delete();
            }
            jarfile.getParentFile().mkdirs();
            jarfile.createNewFile();
            
            // Create the streams necessary to write the jarfile
            jarStream = new JarOutputStream(new FileOutputStream(jarfile));
            jarStream.setMethod(JarOutputStream.DEFLATED);
            
            // Loop through all the class files found and add them to the jar
            entryIterator = files.keySet().iterator();
            while (entryIterator.hasNext()) {
                entryName = (String) entryIterator.next();
                entryFile = (File) files.get(entryName);
                
                getTask().log("adding file '" + entryName + "'",
                              Project.MSG_VERBOSE);

                addFileToJar(jarStream,
                             new FileInputStream(entryFile),
                             entryName);
            }
            // All done.  Close the jar stream.
            jarStream.close();
        }
        catch(IOException ioe) {
            String msg = "IOException while processing ejb-jar file '"
                + jarfile.toString()
                + "'. Details: "
                + ioe.getMessage();
            throw new BuildException(msg, ioe);
        }
    } // end of writeJar
    

    /**
     * Called to validate that the tool parameters have been configured.
     *
     */
    public void validateConfigured() throws BuildException {
        if (destDir == null) {
            throw new BuildException("The destdir attribute must be specified");
        }
    }
}
