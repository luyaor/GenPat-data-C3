/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is part of DrJava.  Download the current version of this project:
 * http://sourceforge.net/projects/drjava/ or http://www.drjava.org/
 *
 * DrJava Open Source License
 *
 * Copyright (C) 2001-2003 JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 *
 * Developed by:   Java Programming Languages Team
 *                 Rice University
 *                 http://www.cs.rice.edu/~javaplt/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal with the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to
 * whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimers.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimers in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the names of DrJava, the JavaPLT, Rice University, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this Software without specific prior written permission.
 *     - Products derived from this software may not be called "DrJava" nor
 *       use the term "DrJava" as part of their names without prior written
 *       permission from the JavaPLT group.  For permission, write to
 *       javaplt@rice.edu.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS WITH THE SOFTWARE.
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model;

import java.io.File;

/**
 * This state pattern is used by the global model to store
 * any information pertaining to the currently open project.
 * A state pattern was used because most project information 
 * is not needed in list view. (Elspeth Rocks)
 * (hint: if you're looking for the instances of this interface,
 *  look in the MainFrame as anonymous inner classes)
 */
public interface FileGroupingState {
  
  /**
   * Returns the directory in which to put the class files
   * after compilation
   * @return null if no build directory is specified
   */
  public File getBuildDirectory();
  
  /**
   * @return true if the model is in project mode, false otherwise
   */
  public boolean isProjectActive();
  
  /**
   * @return true if the document is part of the active project (in the project path), or false
   * if it is not (or the model is not in project mode)
   */
  public boolean isInProjectPath(OpenDefinitionsDocument doc);
  
  /**
   * Returns the current project file
   * @return null if not currently in a project
   */
  public File getProjectFile();
  
  /**
   * Sets the current built directory
   */
  public void setBuildDirectory(File f);
  
  /**
   * Return all files currently saved as source files in the project file
   * If not in project mode, returns null
   */
  public File[] getProjectFiles();
  
  /**
   * Returns true the given file is in the current project file.
   */
  public boolean isProjectFile(File f);
  
  /**
   * Returns true if in project mode and the current project file has changed
   */
  public boolean isProjectChanged();
  
  /**
   * Sets that the project state is no longer a snapshot of the open project.
   */
   public void setProjectChanged(); 
}