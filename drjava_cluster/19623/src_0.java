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

package edu.rice.cs.util.swing;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * A JPanel with a text box and a "..." button used to select
 * a file or directory.  The file name is editable in the text
 * box, and a JFileChooser is displayed if the user clicks the
 * "..." button.
 *
 * @version $Id$
 */
public class DirectorySelectorComponent extends JPanel {
  /**
   * The default number of columns for the text box.
   */
  public static final int DEFAULT_NUM_COLS = 30;

  /**
   * The default font size for the text box.
   */
  public static final float DEFAULT_FONT_SIZE = 10f;


  /**
   * The parent frame of this component.
   */
  protected final Frame _parent;

  /**
   * Text field with the name of the selected file.
   */
  protected final JTextField _fileField;

  /**
   * "..." button to open the file chooser.
   */
  protected final JButton _chooserButton;

  /**
   * File chooser to open when clicking the "..." button.
   */
  protected final DirectoryChooser _chooser;

  /**
   * Creates a new DirectorySelectorComponent with default dimensions.
   *
   * @param parent  Parent of this component.
   * @param chooser File chooser to display from the "..." button.
   */
  public DirectorySelectorComponent(Frame parent, DirectoryChooser chooser) {
    this(parent, chooser, DEFAULT_NUM_COLS, DEFAULT_FONT_SIZE);
  }

  /**
   * Creates a new DirectorySelectorComponent.
   *
   * @param parent   Parent of this component.
   * @param chooser  File chooser to display from the "..." button.
   * @param numCols  Number of columns to display in the text field
   * @param fontSize Font size for the text field
   */
  public DirectorySelectorComponent(Frame parent, DirectoryChooser chooser,
                               int numCols, float fontSize) {
    _parent = parent;
    _chooser = chooser;
    
    _fileField = new JTextField(numCols) {
      public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
      }
    };
    _fileField.setFont(_fileField.getFont().deriveFont(fontSize));
    _fileField.setPreferredSize(new Dimension(22,22));
    
    _chooserButton = new JButton("...");
    _chooserButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _chooseFile();
      }
    });
    _chooserButton.setMaximumSize(new Dimension(22, 22));
    _chooserButton.setMargin(new Insets(0,5,0,5));
    // Add components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(_fileField);
    this.add(_chooserButton);
  }

  public void setEnabled(boolean enabled) {
    _fileField.setEnabled(enabled);
    _chooserButton.setEnabled(enabled);
    super.setEnabled(enabled);
  }
  
  /**
   * Returns the file text field.
   */
  public JTextField getFileField() {
    return _fileField;
  }

  /**
   * Returns the file chooser.
   */
  public DirectoryChooser getFileChooser() {
    return _chooser;
  }

  /**
   * Returns the file currently typed into the file field.
   */
  public File getFileFromField() {
    return new File(_fileField.getText());
  }

  /**
   * Sets the text of the file field to be the given file.
   *
   * @param file File to display in the file field.
   */
  public void setFileField(File file) {
    try {
      _fileField.setText(file.getCanonicalPath());
    }
    catch(IOException e) {
      //handle it gracefully
      _fileField.setText(file.getAbsolutePath());      
    }
    _fileField.setCaretPosition(_fileField.getText().length());
    if (file.exists()) {
      _chooser.setSelectedDirectory(file);
    }
  }


  /**
   * adds a filter to decide if a directory can be chosen
   */
  public void addChoosableFileFilter(FileFilter filter) {
    _chooser.addChoosableFileFilter(filter);
  }
  
  /**
   * removes the given filefilter from the chooser
   */
  public void removeChoosableFileFilter(FileFilter filter) {
    _chooser.removeChoosableFileFilter(filter);
  }
  
  public void clearChoosableFileFilters() {
    _chooser.clearChoosableFileFilters();
  }
  
  /**
   * Opens the file chooser to select a file, putting the result
   * in the file field.
   */
  private void _chooseFile() {
    File file = getFileFromField();

    // Get the file from the chooser
    int returnValue = _chooser.showDialog(file);
    if (returnValue == DirectoryChooser.APPROVE_OPTION) {
      File chosen = _chooser.getSelectedDirectory();
      if (chosen != null) {
        setFileField(chosen);
      }
    }
  }


}