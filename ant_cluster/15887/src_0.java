/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

package org.apache.tools.ant.types.selectors;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.BuildException;

/**
 * Selector that filters files based on whether they are newer than
 * a matching file in another directory tree. It can contain a mapper
 * element, so isn't available as an ExtendSelector (since those
 * parameters can't hold other elements).
 *
 * @author <a href="mailto:bruce@callenish.com">Bruce Atherton</a>
 * @since 1.5
 */
public class DependSelector extends BaseSelector {

    private String targetdir = null;
    private File targetbase = null;
    private Mapper mapperElement = null;
    private FileNameMapper map = null;
    private int granularity = 0;

    public DependSelector() {
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("{dependselector targetdir: ");
        buf.append(targetdir);
        buf.append(" granularity: ");
        buf.append(granularity);
        if (map != null) {
            buf.append(" mapper: ");
            buf.append(map.toString());
        }
        else if (mapperElement != null) {
            buf.append(" mapper: ");
            buf.append(mapperElement.toString());
        }
        buf.append("}");
        return buf.toString();
    }

    /**
     * The name of the file or directory which is checked for out-of-date
     * files.
     *
     * @param targetdir the directory to scan looking for files.
     */
    public void setTargetdir(String targetdir) {
        this.targetdir = SelectorUtils.fixPath(targetdir);
        targetbase = new File(this.targetdir);
    }

    /**
     * Sets the number of milliseconds leeway we will give before we consider
     * a file out of date.
     */
    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }

    /**
     * Defines the FileNameMapper to use (nested mapper element).
     */
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper");
        }
        mapperElement = new Mapper(project);
        return mapperElement;
    }


    /**
     * Checks to make sure all settings are kosher. In this case, it
     * means that the dest attribute has been set and we have a mapper.
     */
    public void verifySettings() {
        if (targetdir == null) {
            setError("The targetdir attribute is required.");
        }
        if (mapperElement == null) {
            map = new IdentityMapper();
        }
        else {
            map = mapperElement.getImplementation();
        }
        if (map == null) {
            setError("Could not set <mapper> element.");
        }
    }

    /**
     * The heart of the matter. This is where the selector gets to decide
     * on the inclusion of a file in a particular fileset.
     *
     * @param basedir the base directory the scan is being done from
     * @param filename is the name of the file to check
     * @param file is a java.io.File object the selector can use
     * @return whether the file should be selected or not
     */
    public boolean isSelected(File basedir, String filename, File file) {

        // throw BuildException on error
        validate();

        // Get File object for the target directory
        File target = targetbase;
        if (target == null) {
            target = new File(basedir,targetdir);
        }

        // Determine file whose out-of-dateness is to be checked
        String[] destfiles = map.mapFileName(filename);
        // Sanity check
        if (destfiles.length != 1 || destfiles[0] == null) {
            throw new BuildException("Invalid destination file results for "
                + targetdir + " with filename " + filename);
        }
        String destname = destfiles[0];
        File destfile = new File(target,destname);

        return SelectorUtils.isOutOfDate(file, destfile, granularity);
    }

}

