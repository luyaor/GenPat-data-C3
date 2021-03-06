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

package edu.rice.cs.drjava.ui;


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;

import javax.swing.tree.*;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.config.*;

import edu.rice.cs.util.swing.FileSelectorComponent;

/**
 * The frame for setting Project Preferences
 */
public class ProjectPropertiesFrame extends JFrame {

  private static final int FRAME_WIDTH = 400;
  private static final int FRAME_HEIGHT = 300;
  private JButton _okButton;
  private JButton _applyButton;
  private JButton _cancelButton;
//  private JButton _saveSettingsButton;
  private JPanel _mainPanel;
  
  private MainFrame _mainFrame;
  
  private FileSelectorComponent _builtDirSelector;
  
  /**
   * Sets up the frame and displays it.
   */
  public ProjectPropertiesFrame(MainFrame mf) {
    super("Project Properties");

    _mainFrame = mf;

    _mainPanel= new JPanel();
    _setupPanel(_mainPanel);
       
//    JScrollPane scroll = new JScrollPane(_mainPanel,
//                                         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
//                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//    
//    // Fix increment on scrollbar
//    JScrollBar bar = scroll.getVerticalScrollBar();
//    bar.setUnitIncrement(25);
//    bar.setBlockIncrement(400);
//    
    
    
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    
    cp.add(_mainPanel, BorderLayout.NORTH);
   
    Action okAction = new AbstractAction("OK") {
      public void actionPerformed(ActionEvent e) {
        // Always apply and save settings
        boolean successful = true;
        successful = saveSettings();
        if (successful) {
          ProjectPropertiesFrame.this.setVisible(false);
        }
      }
    };
    _okButton = new JButton(okAction);

    Action applyAction = new AbstractAction("Apply") {
      public void actionPerformed(ActionEvent e) {
        // Always save settings
        saveSettings();
      }
    };
    _applyButton = new JButton(applyAction);

    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    };
    _cancelButton = new JButton(cancelAction);

    // Add buttons
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5,5,5,5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_applyButton);
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    cp.add(bottom, BorderLayout.SOUTH);



    // Set all dimensions ----
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();

    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }

    this.setSize(frameSize);
    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);
    
    
    addWindowListener(new WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent e) {
        cancel();
      }
    });
    
    reset();
  }

  /**
   * Resets the frame and hides it.
   */
  public void cancel() {
    reset();
    ProjectPropertiesFrame.this.setVisible(false);
  }
  
  private void reset() {
    File f = _mainFrame.getModel().getBuildDirectory();
    JTextField textField = _builtDirSelector.getFileField();
    if(f == null)
      textField.setText("");
    else
      _builtDirSelector.setFileField(f);
  }

  /**
   * Write the settings to the project file
   */
  public boolean saveSettings() {//throws IOException {
    _mainFrame.getModel().setBuildDirectory(_builtDirSelector.getFileFromField());
    _mainFrame.saveProject();
    return true;
  }
  
  /**
   * Returns the current working directory, or the user's current directory
   * if none is set. 20040213 Changed default value to user's current directory.
   */
  private File _getWorkDir() {
    File workDir = DrJava.getConfig().getSetting(OptionConstants.WORKING_DIRECTORY);
    if (workDir == FileOption.NULL_FILE) {
      workDir = new File(System.getProperty("user.dir"));
    }
    if (workDir.isFile() && workDir.getParent() != null) {
      workDir = workDir.getParentFile();
    }
    return workDir;
  }
  
  private void _setupPanel(JPanel panel) {
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    panel.setLayout(gridbag);
    c.fill = GridBagConstraints.HORIZONTAL;
    Insets labelInsets = new Insets(0, 10, 0, 10);
    Insets compInsets  = new Insets(0, 0, 0, 0);
    c.weightx = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    
    JLabel label = new JLabel("Build Directory");
    gridbag.setConstraints(label, c);
    panel.add(label);
    c.weightx = 1.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
     
     JPanel dirPanel = _builtDirectoryPanel();
     gridbag.setConstraints(dirPanel, c);
     panel.add(dirPanel);
    
  }
  
  public JPanel _builtDirectoryPanel() {
//    JPanel toReturn = new JPanel();
//    toReturn.setLayout(new BorderLayout());
//   
//    toReturn.add(new JLabel("Build Directory"),BorderLayout.WEST);
//    
    JFileChooser dirChooser = new JFileChooser(_getWorkDir());
    dirChooser.setDialogTitle("Select");
    dirChooser.setApproveButtonText("Select");
    dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    dirChooser.setMultiSelectionEnabled(false);
    _builtDirSelector = new FileSelectorComponent(this,dirChooser,20,12f);
    _builtDirSelector.setFileFilter(new DirectoryFilter());
    //toReturn.add(_builtDirSelector, BorderLayout.EAST);
    return _builtDirSelector;
  }
}
