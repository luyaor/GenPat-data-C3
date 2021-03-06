/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

import java.io.File;
import java.util.Vector;

/**
 * Task to import another build file into the current project.
 * <p>
 * It must be 'top level'. On execution it will read another Ant file
 * into the same Project.
 * </p>
 * <p>
 * <b>Important</b>: Trying to understand how relative file references
 * resolved in deep/complex build hierarchies - such as what happens
 * when an imported file imports another file can be difficult. Use absolute references for
 * enhanced build file stability, especially in the imported files.
 * </p>
 * <p>Examples:</p>
 * <pre>
 * &lt;import file="../common-targets.xml"/&gt;
 * </pre>
 * <p>Import targets from a file in a parent directory.</p>
 * <pre>
 * &lt;import file="${deploy-platform}.xml"/&gt;
 * </pre>
 * <p>Import the project defined by the property <code>deploy-platform</code>.</p>
 *
 * @since Ant1.6
 * @ant.task category="control"
 */
public class ImportTask extends Task {
    private String file;
    private boolean optional;
    private String targetPrefix;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    /**
     * sets the optional attribute
     *
     * @param optional if true ignore files that are not present,
     *                 default is false
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * the name of the file to import. How relative paths are resolved is still
     * in flux: use absolute paths for safety.
     * @param file the name of the file
     */
    public void setFile(String file) {
        // I don't think we can use File - different rules
        // for relative paths.
        this.file = file;
    }

    /**
     * The prefix to use when prefixing the imported target names.
     *
     * @since Ant 1.8.0
     */
    public void setAs(String prefix) {
        targetPrefix = prefix;
    }

    /**
     *  This relies on the task order model.
     *
     */
    public void execute() {
        if (file == null) {
            throw new BuildException("import requires file attribute");
        }
        if (getOwningTarget() == null
            || !"".equals(getOwningTarget().getName())) {
            throw new BuildException("import only allowed as a top-level task");
        }

        ProjectHelper helper =
                (ProjectHelper) getProject().
                    getReference(ProjectHelper.PROJECTHELPER_REFERENCE);

        if (helper == null) {
            // this happens if the projecthelper was not registered with the project.
            throw new BuildException("import requires support in ProjectHelper");
        }

        Vector importStack = helper.getImportStack();

        if (importStack.size() == 0) {
            // this happens if ant is used with a project
            // helper that doesn't set the import.
            throw new BuildException("import requires support in ProjectHelper");
        }

        if (getLocation() == null || getLocation().getFileName() == null) {
            throw new BuildException("Unable to get location of import task");
        }

        File buildFile = new File(getLocation().getFileName()).getAbsoluteFile();

        // Paths are relative to the build file they're imported from,
        // *not* the current directory (same as entity includes).

        File buildFileParent = new File(buildFile.getParent());
        File importedFile = FILE_UTILS.resolveFile(buildFileParent, file);

        getProject().log("Importing file " + importedFile + " from "
                         + buildFile.getAbsolutePath(), Project.MSG_VERBOSE);

        if (!importedFile.exists()) {
            String message =
                "Cannot find " + file + " imported from "
                + buildFile.getAbsolutePath();
            if (optional) {
                getProject().log(message, Project.MSG_VERBOSE);
                return;
            } else {
                throw new BuildException(message);
            }
        }

        if (!isInIncludeMode() && importStack.contains(importedFile)) {
            getProject().log(
                "Skipped already imported file:\n   "
                + importedFile + "\n", Project.MSG_VERBOSE);
            return;
        }

        // nested invokations are possible like an imported file
        // importing another one
        String oldPrefix = ProjectHelper.getCurrentTargetPrefix();
        boolean oldIncludeMode = ProjectHelper.isInIncludeMode();
        try {
            ProjectHelper.setCurrentTargetPrefix(targetPrefix);
            ProjectHelper.setInIncludeMode(isInIncludeMode());
            helper.parse(getProject(), importedFile);
        } catch (BuildException ex) {
            throw ProjectHelper.addLocationToBuildException(
                ex, getLocation());
        } finally {
            ProjectHelper.setCurrentTargetPrefix(oldPrefix);
            ProjectHelper.setInIncludeMode(oldIncludeMode);
        }
    }

    /**
     * Whether the task is in include (as opposed to import) mode.
     *
     * <p>In include mode included targets are only known by their
     * prefixed names and their depends lists get rewritten so that
     * all dependencies get the prefix as well.</p>
     *
     * <p>In import mode imported targets are known by an adorned as
     * well as a prefixed name and the unadorned target may be
     * overwritten in the importing build file.  The depends list of
     * the imported targets is not modified at all.</p>
     *
     * @since Ant 1.8.0
     */
    protected final boolean isInIncludeMode() {
        return "include".equals(getTaskType());
    }

}
