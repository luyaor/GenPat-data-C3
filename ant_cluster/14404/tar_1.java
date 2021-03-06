/*
 * Copyright  2001-2005 The Apache Software Foundation
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

package org.apache.tools.ant.types;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Comparator;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.resources.ZipResource;
import org.apache.tools.ant.types.resources.FileResourceIterator;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

/**
 * ZipScanner accesses the pattern matching algorithm in DirectoryScanner,
 * which are protected methods that can only be accessed by subclassing.
 *
 * This implementation of FileScanner defines getIncludedFiles to return
 * the matching Zip entries.
 *
 */
public class ZipScanner extends DirectoryScanner {

    /**
     * The zip file which should be scanned.
     */
    protected File srcFile;

    /**
     * to record the last scanned zip file with its modification date
     */
    private Resource lastScannedResource;

    /**
     * record list of all file zip entries
     */
    private TreeMap fileEntries = new TreeMap();

    /**
     * record list of all directory zip entries
     */
    private TreeMap dirEntries = new TreeMap();

    /**
     * record list of matching file zip entries
     */
    private TreeMap matchFileEntries = new TreeMap();

    /**
     * record list of matching directory zip entries
     */
    private TreeMap matchDirEntries = new TreeMap();

    /**
     * encoding of file names.
     *
     * @since Ant 1.6
     */
    private String encoding;

    /**
     * Don't scan when we have no zipfile.
     * @since Ant 1.7
     */
    public void scan() {
        if (srcFile == null) {
            return;
        }
        super.scan();
    }

    /**
     * Sets the srcFile for scanning. This is the jar or zip file that
     * is scanned for matching entries.
     *
     * @param srcFile the (non-null) zip file name for scanning
     */
    public void setSrc(File srcFile) {
        this.srcFile = srcFile;
    }

    /**
     * Sets encoding of file names.
     * @param encoding the encoding format
     * @since Ant 1.6
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Returns the names of the files which matched at least one of the
     * include patterns and none of the exclude patterns.
     * The names are relative to the base directory.
     *
     * @return the names of the files which matched at least one of the
     *         include patterns and none of the exclude patterns.
     */
    public String[] getIncludedFiles() {
        if (srcFile == null) {
            return super.getIncludedFiles();
        }
        scanme();
        Set s = matchFileEntries.keySet();
        return (String[]) (s.toArray(new String[s.size()]));
    }

    /**
     * Override parent implementation.
     * @return count of included files.
     * @since Ant 1.7
     */
    public int getIncludedFilesCount() {
        if (srcFile == null) {
            return super.getIncludedFilesCount();
        }
        scanme();
        return matchFileEntries.size();
    }

    /**
     * Returns the names of the directories which matched at least one of the
     * include patterns and none of the exclude patterns.
     * The names are relative to the base directory.
     *
     * @return the names of the directories which matched at least one of the
     * include patterns and none of the exclude patterns.
     */
    public String[] getIncludedDirectories() {
        if (srcFile == null) {
            return super.getIncludedDirectories();
        }
        scanme();
        Set s = matchDirEntries.keySet();
        return (String[]) (s.toArray(new String[s.size()]));
    }

    /**
     * Override parent implementation.
     * @return count of included directories.
     * @since Ant 1.7
     */
    public int getIncludedDirsCount() {
        if (srcFile == null) {
            return super.getIncludedDirsCount();
        }
        scanme();
        return matchDirEntries.size();
    }

    /**
     * Get the set of Resources that represent files.
     * @return an Iterator of Resources.
     * @since Ant 1.7
     */
    /* package-private for now */ Iterator getResourceFiles() {
        if (srcFile == null) {
            return new FileResourceIterator(getBasedir(), getIncludedFiles());
        }
        scanme();
        return matchFileEntries.values().iterator();
    }

    /**
     * Get the set of Resources that represent directories.
     * @return an Iterator of Resources.
     * @since Ant 1.7
     */
    /* package-private for now */  Iterator getResourceDirectories() {
        if (srcFile == null) {
            return new FileResourceIterator(getBasedir(), getIncludedDirectories());
        }
        scanme();
        return matchDirEntries.values().iterator();
    }

    /**
     * Initialize DirectoryScanner data structures.
     */
    public void init() {
        if (includes == null) {
            // No includes supplied, so set it to 'matches all'
            includes = new String[1];
            includes[0] = "**";
        }
        if (excludes == null) {
            excludes = new String[0];
        }
    }

    /**
     * Matches a jar entry against the includes/excludes list,
     * normalizing the path separator.
     *
     * @param path the (non-null) path name to test for inclusion
     *
     * @return <code>true</code> if the path should be included
     *         <code>false</code> otherwise.
     */
    public boolean match(String path) {
        String vpath = path.replace('/', File.separatorChar).
            replace('\\', File.separatorChar);
        return isIncluded(vpath) && !isExcluded(vpath);
    }

    /**
     * Get the named Resource.
     * @param name path name of the file sought in the archive
     * @return the resource
     * @since Ant 1.5.2
     */
    public Resource getResource(String name) {
        if (srcFile == null) {
            return super.getResource(name);
        }
        if (name.equals("")) {
            // special case in ZIPs, we do not want this thing included
            return new Resource("", true, Long.MAX_VALUE, true);
        }
        // first check if the archive needs to be scanned again
        scanme();
        if (fileEntries.containsKey(name)) {
            return (Resource) fileEntries.get(name);
        }
        name = trimSeparator(name);

        if (dirEntries.containsKey(name)) {
            return (Resource) dirEntries.get(name);
        }
        return new Resource(name);
    }

    /**
     * if the datetime of the archive did not change since
     * lastScannedResource was initialized returns immediately else if
     * the archive has not been scanned yet, then all the zip entries
     * are put into the appropriate tables.
     */
    private void scanme() {
        //do not use a FileResource b/c it pulls File info from the filesystem:
        Resource thisresource = new Resource(srcFile.getAbsolutePath(),
                                             srcFile.exists(),
                                             srcFile.lastModified());
        // spare scanning again and again
        if (lastScannedResource != null
            && lastScannedResource.getName().equals(thisresource.getName())
            && lastScannedResource.getLastModified()
            == thisresource.getLastModified()) {
            return;
        }
        init();
        ZipEntry entry = null;
        ZipFile zf = null;

        fileEntries.clear();
        dirEntries.clear();
        matchFileEntries.clear();
        matchDirEntries.clear();

        try {
            try {
                zf = new ZipFile(srcFile, encoding);
            } catch (ZipException ex) {
                throw new BuildException("problem reading " + srcFile, ex);
            } catch (IOException ex) {
                throw new BuildException("problem opening " + srcFile, ex);
            }
            Enumeration e = zf.getEntries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                Resource r = new ZipResource(srcFile, encoding, entry);
                String name = entry.getName();
                if (entry.isDirectory()) {
                    name = trimSeparator(name);
                    dirEntries.put(name, r);
                    if (match(name)) {
                        matchDirEntries.put(name, r);
                    }
                } else {
                    fileEntries.put(name, r);
                    if (match(name)) {
                        matchFileEntries.put(name, r);
                    }
                }
            }
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException ex) {
                    // swallow
                }
            }
        }
        // record data about the last scanned resource
        lastScannedResource = thisresource;
    }

    private static String trimSeparator(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

}
