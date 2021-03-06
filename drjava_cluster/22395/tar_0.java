/*BEGIN_COPYRIGHT_BLOCK
 *
 * Copyright (c) 2001-2007, JavaPLT group at Rice University (javaplt@rice.edu)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the names of DrJava, the JavaPLT group, Rice University, nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software is Open Source Initiative approved Open Source Software.
 * Open Source Initative Approved is a trademark of the Open Source Initiative.
 * 
 * This file is part of DrJava.  Download the current version of this project
 * from http://www.drjava.org/ or http://sourceforge.net/projects/drjava/
 * 
 * END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import edu.rice.cs.util.ProcessCreator;
import edu.rice.cs.util.JVMProcessCreator;
import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.OptionConstants;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.util.swing.Utilities;
import edu.rice.cs.util.CompletionMonitor;
import edu.rice.cs.util.swing.DirectoryChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.FontMetrics;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import edu.rice.cs.plt.tuple.Pair;

public class ExecuteExternalDialog extends JFrame implements OptionConstants {
  private static final int FRAME_WIDTH = 750;
  private static final int FRAME_HEIGHT = 500;
  
  /** Class to save the frame state, i.e. location. */
  public static class FrameState {
    private Point _loc;
    public FrameState(Point l) {
      _loc = l;
    }
    public FrameState(String s) {
      StringTokenizer tok = new StringTokenizer(s);
      try {
        int x = Integer.valueOf(tok.nextToken());
        int y = Integer.valueOf(tok.nextToken());
        _loc = new Point(x, y);
      }
      catch(NoSuchElementException nsee) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nsee);
      }
      catch(NumberFormatException nfe) {
        throw new IllegalArgumentException("Wrong FrameState string: " + nfe);
      }
    }
    public FrameState(ExecuteExternalDialog comp) {
      _loc = comp.getLocation();
    }
    public String toString() {
      final StringBuilder sb = new StringBuilder();
      sb.append(_loc.x);
      sb.append(' ');
      sb.append(_loc.y);
      return sb.toString();
    }
    public Point getLocation() { return _loc; }
  }
  
  /** Run Command button. */
  private JButton _runCommandButton;
  /** Save Command button. */
  private JButton _saveCommandButton;
  /** Insert Command button. */
  private JButton _insertCommandButton;
  /** Cancel Command button. */
  private JButton _cancelCommandButton;
  /** Entered command line. */
  private JTextPane _commandLine;
  /** Command line preview. */
  private JTextPane _commandLinePreview;
  /** Java command line preview document. */
  private StyledDocument _commandLineDoc;
  /** Entered command line working directory. */
  private JTextPane _commandWorkDirLine;
  /** Command line working directory preview. */
  private JTextPane _commandWorkDirLinePreview;
  /** Command line working directory preview document. */
  private StyledDocument _commandWorkDirLineDoc;
  /** Command working directory button. */
  private JButton _commandWorkDirBtn;
  /** Last of the two text panes to have focus. */
  private JTextPane _lastCommandFocus;

  /** Run Java button. */
  private JButton _runJavaButton;
  /** Save Java button. */
  private JButton _saveJavaButton;
  /** Insert Command button. */
  private JButton _insertJavaButton;
  /** Cancel Java button. */
  private JButton _cancelJavaButton;
  /** Entered JVM args line document. */
  StyledDocument _jvmLineDoc;
  /** Entered JVM args line. */
  private JTextPane _jvmLine;
  /** Entered command line. */
  private JTextPane _javaCommandLine;
  /** Java command line preview. */
  private JTextPane _javaCommandLinePreview;
  /** Last of the three text panes to have focus. */
  private JTextPane _lastJavaFocus;
  /** Java command line preview document. */
  StyledDocument _javaCommandLineDoc;
  /** Entered command line working directory. */
  private JTextPane _javaCommandWorkDirLine;
  /** Command line working directory preview. */
  private JTextPane _javaCommandWorkDirLinePreview;
  /** Java command line working directory preview document. */
  private StyledDocument _javaCommandWorkDirLineDoc;
  /** Java working directory button. */
  private JButton _javaCommandWorkDirBtn;

  /** Style for variable the executable part. */
  SimpleAttributeSet _varCommandLineCmdStyle;
  /** Style for erroneous variable the command args part. */
  SimpleAttributeSet _varErrorCommandLineCmdStyle;
  /** Style for normal text */
  SimpleAttributeSet _commandLineCmdAS;  
  
  /** Style for the executable part. */
  Style _javaCommandLineExecutableStyle;
  /** Style for the JVM args part. */
  Style _javaCommandLineJVMStyle;
  
  /** Style for normal text */
  SimpleAttributeSet _javaCommandLineJVMAS;
  /** Style for normal text */
  SimpleAttributeSet _javaCommandLineCmdAS;

  /** Style for variable the executable part. */
  SimpleAttributeSet _javaVarCommandLineCmdStyle;
  /** Style for variable the JVM args part. */
  SimpleAttributeSet _javaVarCommandLineJVMStyle;
  
  /** Style for erroneous variable the JVM args part. */
  SimpleAttributeSet _javaVarErrorCommandLineJVMStyle;
  /** Style for erroneous variable the command args part. */
  SimpleAttributeSet _javaVarErrorCommandLineCmdStyle;
  
  /** Tab pane. */
  JTabbedPane _tabbedPane;
  /** Command line document listener. */
  DocumentListener _documentListener;
  /** Java document listener. */
  DocumentListener _javaDocumentListener;
  /** Command line work directory document listener. */
  DocumentListener _workDirDocumentListener;
  /** Java work directory document listener. */
  DocumentListener _javaWorkDirDocumentListener;

  /** File chooser to open when clicking the "..." button. */
  protected DirectoryChooser _dirChooser;

  /** Dialog to insert variables. */
  protected InsertVariableDialog _insertVarDialog;
  
  /** Completion monitor to simulate modal behavior. */
  protected CompletionMonitor _insertVarDialogMonitor = new CompletionMonitor();
  
  /** Main frame. */
  protected MainFrame _mainFrame;
  /** Last frame state. It can be stored and restored. */
  protected FrameState _lastState = null;
  
  /** Map of properties. */
  protected Map<String, Properties> _props; 
  
  /** Returns the last state of the frame, i.e. the location and dimension.
   *  @return frame state
   */
  public FrameState getFrameState() { return _lastState; }
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param ds State to update to, or {@code null} to reset
   */
  public void setFrameState(FrameState ds) {
    _lastState = ds;
    if (_lastState != null) {
      setLocation(_lastState.getLocation());
      validate();
    }
  }  
  
  /** Sets state of the frame, i.e. the location and dimension of the frame for the next use.
   *  @param s  State to update to, or {@code null} to reset
   */
  public void setFrameState(String s) {
    try { _lastState = new FrameState(s); }
    catch(IllegalArgumentException e) { _lastState = null; }
    if (_lastState != null) setLocation(_lastState.getLocation());
    else MainFrame.setPopupLoc(this, _mainFrame);
    validate();
  }
  
  /** Create a dialog.
   *  @param mf the instance of mainframe to query into the project
   */
  public ExecuteExternalDialog(MainFrame mf) {
    super("Execute External Process");
    _mainFrame = mf;
    initComponents();
  }

  /** Build the dialog. */
  private void initComponents() {
    _dirChooser = new DirectoryChooser(this);
    _dirChooser.setSelectedFile(new File(System.getProperty("user.dir")));
    _dirChooser.setDialogTitle("Select Work Directory");
    _dirChooser.setApproveButtonText("Select");

    super.getContentPane().setLayout(new GridLayout(1,1));

     _tabbedPane = new JTabbedPane();
 
    Action runCommandAction = new AbstractAction("Run Command Line") {
      public void actionPerformed(ActionEvent e) {
        _runCommand();
      }
    };
    _runCommandButton = new JButton(runCommandAction);
    Action runJavaAction = new AbstractAction("Run Java Class") {
      public void actionPerformed(ActionEvent e) {
        _runJava();
      }
    };
    _runJavaButton = new JButton(runJavaAction);

    Action saveCommandAction = new AbstractAction("Save to Menu...") {
      public void actionPerformed(ActionEvent e) {
        _saveCommand();
      }
    };
    _saveCommandButton = new JButton(saveCommandAction);
    Action saveJavaAction = new AbstractAction("Save to Menu...") {
      public void actionPerformed(ActionEvent e) {
        _saveJava();
      }
    };
    _saveJavaButton = new JButton(saveJavaAction);

    updateProperties();
    
    _insertVarDialog = new InsertVariableDialog(_mainFrame, _props, _insertVarDialogMonitor);
    Action insertCommandAction = new AbstractAction("Insert Variable...") {
      public void actionPerformed(ActionEvent e) {
        _insertVariableCommand();
      }
    };
    _insertCommandButton = new JButton(insertCommandAction);
    _insertCommandButton.setEnabled(false);
    Action insertJavaAction = new AbstractAction("Insert Variable...") {
      public void actionPerformed(ActionEvent e) {
        _insertVariableJava();
      }
    };
    _insertJavaButton = new JButton(insertJavaAction);
    _insertJavaButton.setEnabled(false);
    
    Action cancelAction = new AbstractAction("Cancel") {
      public void actionPerformed(ActionEvent e) {
        _cancel();
      }
    };
    _cancelCommandButton = new JButton(cancelAction);
    _cancelJavaButton = new JButton(cancelAction);
        
    // set up "Command Line" panel
    JPanel panel1 = makeCommandPane();
    _tabbedPane.addTab("Command Line", null, panel1, "Execute command line process");
    
    // set up "Java" panel
    JPanel panel2 = makeJavaPane();
    _tabbedPane.addTab("Java Class", null, panel2, "Execute Java class");
        
    //The following line enables to use scrolling tabs.
    _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    
    super.getContentPane().add(_tabbedPane);
    super.setResizable(true);
    
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    MainFrame.setPopupLoc(this, _mainFrame);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        _commandLine.requestFocus();
      }
    });
  }
  
  private JPanel makeCommandPane() {
    JPanel panel = new JPanel(new BorderLayout());
    GridBagLayout gridbag = new GridBagLayout();
    JPanel main = new JPanel(gridbag);
    GridBagConstraints c = new GridBagConstraints();
    main.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.insets = labelInsets;
    JLabel commandLineLabel = new JLabel("Command line:");
    gridbag.setConstraints(commandLineLabel, c);
    main.add(commandLineLabel);

    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _commandLine = new JTextPane();
    // do not allow a newline
    _commandLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
           e.consume();
           if (e.isShiftDown()) {
             _insertCommandButton.setEnabled(false);
             _tabbedPane.requestFocus();
           }
           else {
             _commandWorkDirLine.requestFocus();
           }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane commandLineSP = new JScrollPane(_commandLine);
    commandLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandLineSP, c);
    main.add(commandLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel commandLinePreviewLabel = new JLabel("Command line preview:");
    gridbag.setConstraints(commandLinePreviewLabel, c);
    main.add(commandLinePreviewLabel);

    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    _commandLinePreview = new JTextPane();
    _commandLineDoc = (StyledDocument)_commandLinePreview.getDocument();

    // Create a style object and then set the style attributes
    _varCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    
    _commandLineCmdAS = new SimpleAttributeSet();
    StyleConstants.setForeground(_commandLineCmdAS, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
    _varCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    _varErrorCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varErrorCommandLineCmdStyle, DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
    _varCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_varCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));

    _commandLinePreview.setEditable(false);
    _commandLinePreview.setBackground(Color.LIGHT_GRAY);
    _commandLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane commandLinePreviewSP = new JScrollPane(_commandLinePreview);
    commandLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandLinePreviewSP, c);
    main.add(commandLinePreviewSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel workDirLabel = new JLabel("Work directory:");
    gridbag.setConstraints(workDirLabel, c);
    main.add(workDirLabel);

    c.weightx = 1.0;
    c.weighty = 8.0;
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = compInsets;
    
    _commandWorkDirLine = new JTextPane();
    // do not allow a newline
    _commandWorkDirLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
           e.consume();
           if (e.isShiftDown()) {
             _commandLine.requestFocus();
           }
           else {
             _insertCommandButton.setEnabled(false);
             _runCommandButton.requestFocus();
           }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane commandWorkDirLineSP = new JScrollPane(_commandWorkDirLine);
    commandWorkDirLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandWorkDirLineSP, c);
    main.add(commandWorkDirLineSP);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _commandWorkDirBtn = new JButton("...");
    _commandWorkDirBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { chooseFile(_commandWorkDirLine); }
    });
    gridbag.setConstraints(_commandWorkDirBtn, c);
    main.add(_commandWorkDirBtn);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel commandWorkDirLinePreviewLabel = new JLabel("Work directory preview:");
    gridbag.setConstraints(commandWorkDirLinePreviewLabel, c);
    main.add(commandWorkDirLinePreviewLabel);

    c.weightx = 1.0;
    c.weighty = 8.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    _commandWorkDirLinePreview = new JTextPane();
    _commandWorkDirLineDoc = (StyledDocument)_commandWorkDirLinePreview.getDocument();

    _commandWorkDirLinePreview.setEditable(false);
    _commandWorkDirLinePreview.setBackground(Color.LIGHT_GRAY);
    _commandWorkDirLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane commandWorkDirLinePreviewSP = new JScrollPane(_commandWorkDirLinePreview);
    commandWorkDirLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(commandWorkDirLinePreviewSP, c);
    main.add(commandWorkDirLinePreviewSP);

    panel.add(main, BorderLayout.CENTER);
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_runCommandButton);
    bottom.add(_saveCommandButton);
    bottom.add(_insertCommandButton);
    bottom.add(_cancelCommandButton);
    bottom.add(Box.createHorizontalGlue());
    panel.add(bottom, BorderLayout.SOUTH);
    
    // update the preview of the actual command line post substitution
    _documentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _commandLineDoc.remove(0,_commandLineDoc.getLength());
          StringBuilder sb = new StringBuilder();
          String text = replaceVariables(_commandLine.getText(), _props);
          List<String> cmds = commandLineToList(text);
          for(String s: cmds) {
            sb.append(s);
            sb.append(' ');
          }
          _commandLineDoc.insertString(_commandLineDoc.getLength(), sb.toString(), null);
          
          // command line
          colorVariables(_commandLine,
                         this,
                         _commandLineCmdAS,
                         _varCommandLineCmdStyle,
                         _varErrorCommandLineCmdStyle);

        }
        catch(BadLocationException ble) {
          _commandLinePreview.setText("Error.");
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _commandLine.getDocument().addDocumentListener(_documentListener);
    _documentListener.changedUpdate(null);

    // update the preview of the actual command line post substitution
    _workDirDocumentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _commandWorkDirLineDoc.remove(0,_commandWorkDirLineDoc.getLength());
          LOG.log("_commandWorkDirLine.getText() = '"+_commandWorkDirLine.getText()+"'");
          String text = replaceVariables(_commandWorkDirLine.getText(), _props);
          LOG.log("text = '"+text+"'");
          _commandWorkDirLineDoc.insertString(0, text, null);
          
          // command line
          colorVariables(_commandWorkDirLine,
                         this,
                         _commandLineCmdAS,
                         _varCommandLineCmdStyle,
                         _varErrorCommandLineCmdStyle);
          LOG.log("colorVariables");
        }
        catch(BadLocationException ble) {
          _commandLinePreview.setText("Error: "+ble);
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _commandWorkDirLine.getDocument().addDocumentListener(_workDirDocumentListener);
    _commandWorkDirLine.setText("${user.dir}");
    _workDirDocumentListener.changedUpdate(null);

    DrJava.getConfig().addOptionListener(DEFINITIONS_COMMENT_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineExecutableStyle, oce.value);
        _documentListener.changedUpdate(null);
        _workDirDocumentListener.changedUpdate(null);
      }
    });
    DrJava.getConfig().addOptionListener(DEFINITIONS_KEYWORD_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineJVMStyle, oce.value);
        _documentListener.changedUpdate(null);
        _workDirDocumentListener.changedUpdate(null);
      }
    });

    _lastCommandFocus = _commandLine;
    // do not allow preview to have focus
    _commandLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastCommandFocus = (JTextPane)e.getComponent();
        _insertCommandButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _commandLinePreview) || 
            (e.getOppositeComponent() == _commandWorkDirLinePreview)) {
          _commandLine.requestFocus();
        }
      }
    });
    _commandWorkDirLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastCommandFocus = (JTextPane)e.getComponent();
        _insertCommandButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _commandLinePreview) || 
            (e.getOppositeComponent() == _commandWorkDirLinePreview)) {
          _commandWorkDirLine.requestFocus();
        }
      }
    });
    
    return panel;
  }
  
  private JPanel makeJavaPane() {
    JPanel panel = new JPanel(new BorderLayout());
    GridBagLayout gridbag = new GridBagLayout();
    JPanel main = new JPanel(gridbag);
    GridBagConstraints c = new GridBagConstraints();
    main.setLayout(gridbag);
    c.fill = GridBagConstraints.BOTH;
    Insets labelInsets = new Insets(5, 10, 0, 0);
    Insets compInsets  = new Insets(5, 5, 0, 10);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel jvmArgsLabel = new JLabel("JVM arguments:");
    gridbag.setConstraints(jvmArgsLabel, c);
    main.add(jvmArgsLabel);

    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _jvmLine = new JTextPane();
    _jvmLineDoc = (StyledDocument)_jvmLine.getDocument();
    // do not allow a newline
    _jvmLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
           e.consume();
           if (e.isShiftDown()) {
             _insertJavaButton.setEnabled(false);
             _tabbedPane.requestFocus();
           }
           else {
             _javaCommandLine.requestFocus();
           }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane jvmLineSP = new JScrollPane(_jvmLine);
    jvmLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(jvmLineSP, c);
    main.add(jvmLineSP);

    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaCommandLineLabel = new JLabel("Java command line:");
    gridbag.setConstraints(javaCommandLineLabel, c);
    main.add(javaCommandLineLabel);

    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    _javaCommandLine = new JTextPane();
    // do not allow a newline
    _javaCommandLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
           e.consume();
           if (e.isShiftDown()) {
             _jvmLine.requestFocus();
           }
           else {
             _javaCommandWorkDirLine.requestFocus();
           }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane javaCommandLineSP = new JScrollPane(_javaCommandLine);
    javaCommandLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandLineSP, c);
    main.add(javaCommandLineSP);

    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaCommandLinePreviewLabel = new JLabel("Command line preview:");
    gridbag.setConstraints(javaCommandLinePreviewLabel, c);
    main.add(javaCommandLinePreviewLabel);

    c.weightx = 1.0;
    c.weighty = 32.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    _javaCommandLinePreview = new JTextPane();
    _javaCommandLineDoc = (StyledDocument)_javaCommandLinePreview.getDocument();

    // Create a style object and then set the style attributes
    _javaCommandLineExecutableStyle = _javaCommandLineDoc.addStyle("ExecutableStyle", null);
    StyleConstants.setItalic(_javaCommandLineExecutableStyle, true);
    StyleConstants.setForeground(_javaCommandLineExecutableStyle, DrJava.getConfig().getSetting(DEFINITIONS_COMMENT_COLOR));
    _javaCommandLineJVMStyle = _javaCommandLineDoc.addStyle("JVMStyle", null);
    StyleConstants.setForeground(_javaCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR));
    _javaCommandLineJVMAS = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaCommandLineJVMAS, DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR));
    
    _javaVarCommandLineJVMStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaVarCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_KEYWORD_COLOR));
    StyleConstants.setBackground(_javaVarCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    _javaVarErrorCommandLineJVMStyle = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaVarErrorCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
    StyleConstants.setBackground(_javaVarErrorCommandLineJVMStyle, DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
    _javaVarCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarCommandLineJVMStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    
    _javaCommandLineCmdAS = new SimpleAttributeSet();
    StyleConstants.setForeground(_javaCommandLineCmdAS, DrJava.getConfig().getSetting(DEFINITIONS_NORMAL_COLOR));
    _javaVarCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));
    _javaVarErrorCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarErrorCommandLineCmdStyle, DrJava.getConfig().getSetting(DEBUG_BREAKPOINT_COLOR));
    _javaVarCommandLineCmdStyle = new SimpleAttributeSet();
    StyleConstants.setBackground(_javaVarCommandLineCmdStyle, DrJava.getConfig().getSetting(DEFINITIONS_MATCH_COLOR));

    _javaCommandLinePreview.setEditable(false);
    _javaCommandLinePreview.setBackground(Color.LIGHT_GRAY);
    _javaCommandLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane javaCommandLinePreviewSP = new JScrollPane(_javaCommandLinePreview);
    javaCommandLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandLinePreviewSP, c);
    main.add(javaCommandLinePreviewSP);

    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaWorkDirLabel = new JLabel("Work directory:");
    gridbag.setConstraints(javaWorkDirLabel, c);
    main.add(javaWorkDirLabel);

    c.weightx = 1.0;
    c.weighty = 12.0;
    c.gridwidth = GridBagConstraints.RELATIVE;
    c.insets = compInsets;
    
    _javaCommandWorkDirLine = new JTextPane();
    // do not allow a newline
    _javaCommandWorkDirLine.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) {
           e.consume();
           if (e.isShiftDown()) {
             _javaCommandLine.requestFocus();
           }
           else {
             _insertJavaButton.setEnabled(false);
             _runJavaButton.requestFocus();
           }
        }
      }
      public void  keyReleased(KeyEvent e) { }
      public void  keyTyped(KeyEvent e) { }
    });
    JScrollPane javaCommandWorkDirLineSP = new JScrollPane(_javaCommandWorkDirLine);
    javaCommandWorkDirLineSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandWorkDirLineSP, c);
    main.add(javaCommandWorkDirLineSP);

    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;
    
    _javaCommandWorkDirBtn = new JButton("...");
    _javaCommandWorkDirBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) { chooseFile(_javaCommandWorkDirLine); }
    });
    gridbag.setConstraints(_javaCommandWorkDirBtn, c);
    main.add(_javaCommandWorkDirBtn);
    
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.insets = labelInsets;
    JLabel javaCommandWorkDirLinePreviewLabel = new JLabel("Work directory preview:");
    gridbag.setConstraints(javaCommandWorkDirLinePreviewLabel, c);
    main.add(javaCommandWorkDirLinePreviewLabel);

    c.weightx = 1.0;
    c.weighty = 12.0;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.insets = compInsets;

    _javaCommandWorkDirLinePreview = new JTextPane();
    _javaCommandWorkDirLineDoc = (StyledDocument)_javaCommandWorkDirLinePreview.getDocument();

    _javaCommandWorkDirLinePreview.setEditable(false);
    _javaCommandWorkDirLinePreview.setBackground(Color.LIGHT_GRAY);
    _javaCommandWorkDirLinePreview.setSelectedTextColor(Color.LIGHT_GRAY);
    JScrollPane javaCommandWorkDirLinePreviewSP = new JScrollPane(_javaCommandWorkDirLinePreview);
    javaCommandWorkDirLinePreviewSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    gridbag.setConstraints(javaCommandWorkDirLinePreviewSP, c);
    main.add(javaCommandWorkDirLinePreviewSP);

    panel.add(main, BorderLayout.CENTER);
    JPanel bottom = new JPanel();
    bottom.setBorder(new EmptyBorder(5, 5, 5, 5));
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_runJavaButton);
    bottom.add(_saveJavaButton);
    bottom.add(_insertJavaButton);
    bottom.add(_cancelJavaButton);
    bottom.add(Box.createHorizontalGlue());
    panel.add(bottom, BorderLayout.SOUTH);
    
    // update the preview of the actual command line post substitution
    _javaDocumentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _javaCommandLineDoc.remove(0,_javaCommandLineDoc.getLength());
          StringBuilder sb = new StringBuilder();
          sb.append(edu.rice.cs.util.newjvm.ExecJVM.getExecutable());
          sb.append(' ');
          _javaCommandLineDoc.insertString(_javaCommandLineDoc.getLength(), sb.toString(), _javaCommandLineExecutableStyle);
          
          sb = new StringBuilder();
          String text = replaceVariables(_jvmLine.getText(), _props);
          List<String> cmds = commandLineToList(text);
          for(String s: cmds) {
            sb.append(s);
            sb.append(' ');
          }
          _javaCommandLineDoc.insertString(_javaCommandLineDoc.getLength(), sb.toString(), _javaCommandLineJVMStyle);
          
          sb = new StringBuilder();
          text = replaceVariables(_javaCommandLine.getText(), _props);
          cmds = commandLineToList(text);
          for(String s: cmds) {
            sb.append(s);
            sb.append(' ');
          }
          _javaCommandLineDoc.insertString(_javaCommandLineDoc.getLength(), sb.toString(), null);
          
          // JVM line
          colorVariables(_jvmLine,
                         this,
                         _javaCommandLineJVMAS,
                         _javaVarCommandLineJVMStyle,
                         _javaVarErrorCommandLineJVMStyle);

          // Java Command line
          colorVariables(_javaCommandLine,
                         this,
                         _javaCommandLineCmdAS,
                         _javaVarCommandLineCmdStyle,
                         _javaVarErrorCommandLineCmdStyle);

        }
        catch(BadLocationException ble) {
          _javaCommandLinePreview.setText("Error.");
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _javaCommandLine.getDocument().addDocumentListener(_javaDocumentListener);
    _jvmLine.getDocument().addDocumentListener(_javaDocumentListener);
    _javaDocumentListener.changedUpdate(null);

    // update the preview of the actual work dir post substitution
    _javaWorkDirDocumentListener = new DocumentListener() {
      public void update(DocumentEvent e) {
        try {
          // preview
          _javaCommandWorkDirLineDoc.remove(0,_javaCommandWorkDirLineDoc.getLength());
          String text = replaceVariables(_javaCommandWorkDirLine.getText(), _props);
          _javaCommandWorkDirLineDoc.insertString(0, text, null);
          
          // work dir
          colorVariables(_javaCommandWorkDirLine,
                         this,
                         _javaCommandLineCmdAS,
                         _javaVarCommandLineCmdStyle,
                         _javaVarErrorCommandLineCmdStyle);

        }
        catch(BadLocationException ble) {
          _javaCommandWorkDirLinePreview.setText("Error: "+ble);
        }
      }
      public void changedUpdate(DocumentEvent e) { update(e); }
      public void insertUpdate(DocumentEvent e) { update(e); }
      public void removeUpdate(DocumentEvent e)  { update(e); }
    };
    _javaCommandWorkDirLine.getDocument().addDocumentListener(_javaWorkDirDocumentListener);
    _javaCommandWorkDirLine.setText("${user.dir}");
    _javaWorkDirDocumentListener.changedUpdate(null);
    
    DrJava.getConfig().addOptionListener(DEFINITIONS_COMMENT_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineExecutableStyle, oce.value);
        _javaDocumentListener.changedUpdate(null);
      }
    });
    DrJava.getConfig().addOptionListener(DEFINITIONS_KEYWORD_COLOR, new OptionListener<Color>() {
      public void optionChanged(OptionEvent<Color> oce) {
        StyleConstants.setForeground(_javaCommandLineJVMStyle, oce.value);
        _javaDocumentListener.changedUpdate(null);
      }
    });
    
    _lastJavaFocus = _javaCommandLine;
    // do not allow preview to have focus
    _javaCommandLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastJavaFocus = (JTextPane)e.getComponent();
        _insertJavaButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _javaCommandLinePreview) ||
            (e.getOppositeComponent() == _javaCommandWorkDirLinePreview)) {
          _javaCommandLine.requestFocus();
        }
      }
    });
    _jvmLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastJavaFocus = (JTextPane)e.getComponent();
        _insertJavaButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _javaCommandLinePreview) ||
            (e.getOppositeComponent() == _javaCommandWorkDirLinePreview)) {
          _jvmLine.requestFocus();
        }
      }
    });
    _javaCommandWorkDirLine.addFocusListener(new FocusAdapter() {
      @SuppressWarnings("unchecked")
      public void focusGained(FocusEvent e) {
        _lastJavaFocus = (JTextPane)e.getComponent();
        _insertJavaButton.setEnabled(true);
      }
      public void focusLost(FocusEvent e) {
        if ((e.getOppositeComponent() == _javaCommandLinePreview) ||
            (e.getOppositeComponent() == _javaCommandWorkDirLinePreview)) {
          _javaCommandWorkDirLine.requestFocus();
        }
      }
    });
    
    return panel;
  }
  
  /** Color properties as variables.
    * @param pane the pane that contains the text
    * @param props the properties to color */
  protected void colorVariables(final JTextPane pane,
                                final DocumentListener dl,
                                final SimpleAttributeSet normal,
                                final SimpleAttributeSet variable,
                                final SimpleAttributeSet error) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        StyledDocument doc = (StyledDocument)pane.getDocument();
        doc.removeDocumentListener(dl);
        String str = pane.getText();
        doc.setCharacterAttributes(0,str.length(),normal,true);
        try {
//          int pos = str.indexOf("${");
//          int bsPos = str.indexOf("\\\\");
//          if ((bsPos!=-1) && (bsPos<pos)) { pos = bsPos; }
          int pos = 0;
          SimpleAttributeSet sas = variable;
          // LOG.log(str);
          while(pos>=0) {
            // LOG.log("pos = "+pos); 
            // see if this is an escaped \ (\\)
            if ((str.charAt(pos)=='\\') &&
                (pos<str.length()-1) &&
                (str.charAt(pos+1)=='\\')) {
              doc.setCharacterAttributes(pos,pos+1,normal,true);
              pos += 2;
            }
            else if ((str.charAt(pos)=='\\') &&
                     (pos<str.length()-1) &&
                     (str.charAt(pos+1)=='$')) {
              // escaped $ (\$)
              doc.setCharacterAttributes(pos,pos+1,normal,true);
              pos += 2;
            }
            else if ((str.charAt(pos)=='$') &&
                     (pos<str.length()-1) &&
                     (str.charAt(pos+1)=='{')) {
              // beginning of what should be a ${variable}
              boolean found = false;
              for(Map.Entry<String, Properties> table: _props.entrySet()) {
                Enumeration<?> e = table.getValue().propertyNames();
                while(e.hasMoreElements()) {
                  String key = (String)e.nextElement();
                  int endPos = pos + key.length() + 3;
                  if (str.substring(pos, Math.min(str.length(), endPos)).equals("${"+key+"}")) {
                    // found property name
                    found = true;
                    doc.setCharacterAttributes(pos,endPos-pos,variable,true);
                    pos = endPos;
                    break;
                  }
                }
              }
              if (!found) {
                int closePos = str.indexOf('}', pos);
                if (closePos!=-1) {
                  doc.setCharacterAttributes(pos,closePos-pos+1,error,true);
                  pos = closePos+1;
                }
                else {
                  doc.setCharacterAttributes(pos,1,error,true);
                  ++pos;
                }
              }
            }
            else {
              doc.setCharacterAttributes(pos,1,normal,true);
              ++pos;
            }
          }
        }
        finally {
          doc.addDocumentListener(dl);
        }
      }
    });
  }
  
  /** Method that handels the Cancel button */
  private void _cancel() {
    _lastState = new FrameState(this);
    this.setVisible(false);
  }

  public static edu.rice.cs.util.Log LOG = new edu.rice.cs.util.Log("process.txt", false);

  /** Convert a command line into a list of individual arguments. */
  private List<String> commandLineToList(String cmdline) {
    StreamTokenizer tok = new StreamTokenizer(new StringReader(cmdline));
    tok.resetSyntax();
    tok.wordChars(0,255);
    tok.whitespaceChars(0,32);
    tok.quoteChar('\'');
    tok.quoteChar('"');
    tok.quoteChar('`');
    tok.slashSlashComments(false);
    tok.slashStarComments(false);
    ArrayList<String> cmds = new ArrayList<String>();
    
    int next;
    try {
      while(((next=tok.nextToken())!=StreamTokenizer.TT_EOF) &&
            (next!=StreamTokenizer.TT_EOL)) {
        switch(next) {
          case '\'':
          case '"':
          case '`':
            cmds.add(""+((char)next)+tok.sval+((char)next));
            break;
          case StreamTokenizer.TT_WORD:
            cmds.add(tok.sval);
            break;
          case StreamTokenizer.TT_NUMBER:
            cmds.add(""+tok.nval);
            break;
          default:
            return new ArrayList<String>();
        }
      }
    }
    catch(IOException ioe) {
      return new ArrayList<String>();
    }
    return cmds;
  }
  
  /** Execute the command line. */
  private void _runCommand() {
    _mainFrame.updateStatusField("Executing external process...");

    List<String> cmds = commandLineToList(replaceVariables(_commandLine.getText(), _props));
    
    if (cmds.size()>0) {
      ProcessCreator pc = new ProcessCreator(cmds.toArray(new String[cmds.size()]));
      File wd = new File(replaceVariables(_commandWorkDirLine.getText().trim(), _props));
      // LOG.log("ok. wd = "+wd);
      if ((!_commandWorkDirLine.getText().equals("")) &&
          ((!wd.exists()) ||
           (!wd.isDirectory()))) {
        JOptionPane.showMessageDialog(this,
                                      "The working directory '"+ wd + "'\n"+
                                      "is invalid because it does not exist.",
                                      "Invalid Work Directory", JOptionPane.ERROR_MESSAGE);
 
        // Always apply and save settings
        _saveSettings();
        this.setVisible(false);
        return;
      }
      else {
        // LOG.log("setting!");
        pc.setDir(wd);
      }
      String name = "External";
      if (cmds.size()>0) { name += ": "+cmds.get(0); }
      final ExternalProcessPanel panel = new ExternalProcessPanel(_mainFrame, name, pc);
      _mainFrame._tabs.addLast(panel);
      panel.getMainPanel().addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) { _mainFrame._lastFocusOwner = panel; }
      });
      panel.setVisible(true);
      _mainFrame.showTab(panel);
      _mainFrame._tabbedPane.setSelectedComponent(panel);
      // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the findResultsPanel has been selected
      EventQueue.invokeLater(new Runnable() { public void run() { panel.requestFocusInWindow(); } });
    }
    else {
      if (_commandLinePreview.getText().length()>0) {
        JOptionPane.showMessageDialog(this,
                                      "Could not separate command line into individual parts.",
                                      "Invalid Command Line",
                                      JOptionPane.ERROR_MESSAGE);
      }
      else {
        JOptionPane.showMessageDialog(this,
                                      "Empty command line.",
                                      "Invalid Command Line",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }

    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
  }

  /** Execute the Java class. */
  private void _runJava() {
    _mainFrame.updateStatusField("Executing external Java class...");

    List<String> jvms = new ArrayList<String>();
    List<String> cmds = new ArrayList<String>();
    if (_jvmLine.getText().trim().length()>0) {
      jvms = commandLineToList(replaceVariables(_jvmLine.getText().trim(), _props));
    }
    if (_javaCommandLine.getText().trim().length()>0) {
      cmds = commandLineToList(replaceVariables(_javaCommandLine.getText().trim(), _props));
    }
    
    if (jvms.size()+cmds.size()>0) {
      ProcessCreator pc = new JVMProcessCreator(jvms, cmds);
      
      File wd = new File(replaceVariables(_javaCommandWorkDirLine.getText().trim(), _props));
      // LOG.log("ok. wd = "+wd);
      if ((!_javaCommandWorkDirLine.getText().equals("")) &&
          ((!wd.exists()) ||
           (!wd.isDirectory()))) {
        JOptionPane.showMessageDialog(this,
                                      "The working directory '"+ wd + "'\n"+
                                      "is invalid because it does not exist.",
                                      "Invalid Work Directory", JOptionPane.ERROR_MESSAGE);
 
        // Always apply and save settings
        _saveSettings();
        this.setVisible(false);
        return;
      }
      else {
        // LOG.log("setting!");
        pc.setDir(wd);
      }
      String name = "External Java";
      if (cmds.size()>0) { name += ": "+cmds.get(0); }
      final ExternalProcessPanel panel = new ExternalProcessPanel(_mainFrame, name, pc);
      _mainFrame._tabs.addLast(panel);
      panel.getMainPanel().addFocusListener(new FocusAdapter() {
        public void focusGained(FocusEvent e) { _mainFrame._lastFocusOwner = panel; }
      });
      panel.setVisible(true);
      _mainFrame.showTab(panel);
      _mainFrame._tabbedPane.setSelectedComponent(panel);
      // Use SwingUtilties.invokeLater to ensure that focus is set AFTER the findResultsPanel has been selected
      EventQueue.invokeLater(new Runnable() { public void run() { panel.requestFocusInWindow(); } });
    }
    else {
      if ((_javaCommandLinePreview.getText().length()>0) &&
          (_jvmLine.getText().length()>0)) {
        JOptionPane.showMessageDialog(this,
                                      "Could not separate command line into individual parts.",
                                      "Invalid Command Line",
                                      JOptionPane.ERROR_MESSAGE);
      }
      else {
        JOptionPane.showMessageDialog(this,
                                      "Empty command line.",
                                      "Invalid Command Line",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
    
    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
  }
  
  /** Save the command line to the menu. */
  private void _saveCommand() {
    // TODO

    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
  }

  /** Save the Java class to the menu. */
  private void _saveJava() {
    // TODO
    
    // Always apply and save settings
    _saveSettings();
    this.setVisible(false);
  }

  /** Save the settings for this dialog. */
  private boolean _saveSettings() {
    _lastState = new FrameState(this);
    return true;
  }
  
  /** Insert a variable into the command line. */
  private void _insertVariableCommand() {
    _windowListenerActive = false;
    _insertVarDialogMonitor.reset();
    _insertVarDialog.setVisible(true);
    // start a new thread to wait for the dialog to finish
    // this waiting cannot happen in the event thread, as that would block the other dialog
    new Thread(new Runnable() {
      public void run() {
        _insertVarDialogMonitor.waitOne();
        // dialog has finished, figure out the results in the event thread
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                ExecuteExternalDialog.this.toFront();
              }
            });
            _windowListenerActive = true;
            edu.rice.cs.plt.tuple.Pair<String,String> selected = _insertVarDialog.getSelected();
            if (selected!=null) {
              String text = _lastCommandFocus.getText();
              Caret caret = _lastCommandFocus.getCaret();
              int min = Math.min(caret.getDot(), caret.getMark());
              int max = Math.max(caret.getDot(), caret.getMark());
              if (min!=max) {
                text = text.substring(0, min) + text.substring(max);
              }
              text = text.substring(0,min) + "${" + selected.first() + "}" + text.substring(min);
              _lastCommandFocus.setText(text);
              caret.setDot(min+selected.first().length()+2);
              _lastCommandFocus.setCaret(caret);
            }
          }
        });
      }
    }).start();
  }
  
  /** insert a variable Java class command line. */
  private void _insertVariableJava() {
    _windowListenerActive = false;
    _insertVarDialogMonitor.reset();
    _insertVarDialog.setVisible(true);
    // start a new thread to wait for the dialog to finish
    // this waiting cannot happen in the event thread, as that would block the other dialog
    new Thread(new Runnable() {
      public void run() {
        _insertVarDialogMonitor.waitOne();
        // dialog has finished, figure out the results in the event thread
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                ExecuteExternalDialog.this.toFront();
              }
            });
            _windowListenerActive = true;
            edu.rice.cs.plt.tuple.Pair<String,String> selected = _insertVarDialog.getSelected();
            if (selected!=null) {
              String text = _lastJavaFocus.getText();
              Caret caret = _lastJavaFocus.getCaret();
              int min = Math.min(caret.getDot(), caret.getMark());
              int max = Math.max(caret.getDot(), caret.getMark());
              if (min!=max) {
                text = text.substring(0, min) + text.substring(max);
              }
              text = text.substring(0,min) + "${" + selected.first() + "}" + text.substring(min);
              _lastJavaFocus.setText(text);
              caret.setDot(min+selected.first().length()+2);
              _lastJavaFocus.setCaret(caret);
            }
          }
        });
      }
    }).start();
  }
  
  /** Update the properties. */
  public void updateProperties() {
    _props = new HashMap<String, Properties>();
    _props.put("Java", System.getProperties());
    
    Properties drJavaProps = new Properties();
    OptionMap om = DrJava.getConfig().getOptionMap();
    Iterator<OptionParser<?>> it = om.keys();
    while(it.hasNext()) {
      OptionParser<?> op = it.next();
      String key = op.getName();
      String value = om.getString(op);
      drJavaProps.setProperty("drjava."+key,value);
    }
    _props.put("DrJava", drJavaProps);
  }

  /**
   * Replace variables of the form "${variable}" with the value associated with the string "variable" in the
   * provided hash table.
   * To give the "$" character its literal meaning, it needs to be escaped as "\$" (backslash dollar).
   * To make the "\" character not escaping, escape it as "\\"(double backslash).
   * @param str input string
   * @param props hash map of hash tables with variable-value pairs
   * @return string with variables replaced by values
   */
  public static String replaceVariables(String str, Map<String,Properties> props) {
    int pos = str.indexOf("${");
    int bsPos = str.indexOf('\\');
    if ((bsPos!=-1) && (bsPos<pos)) { pos = bsPos; }
    // find every ${
    // LOG.log("========================");
    while(pos>=0) {
      // LOG.log("str = '"+str+"', pos = "+pos);
      // see if this is an escaped \ (\\)
      if ((str.charAt(pos)=='\\') &&
          (pos<str.length()-1) &&
          (str.charAt(pos+1)=='\\')) {
        // change the \\ into a single \
        // LOG.log("\t\\\\");
        str = str.substring(0, pos) + str.substring(pos+1);
      }
      // see if this is an escaped $ (\$)
      else if ((str.charAt(pos)=='\\') &&
               (pos<str.length()-1) &&
               (str.charAt(pos+1)=='$')) {
        // change the \$ into a single $
        // LOG.log("\t\\$");
        str = str.substring(0, pos) + str.substring(pos+1);
        // and skip
        ++pos;
      }
      else if (str.charAt(pos)=='$') {
        // LOG.log("\t$");
        // look if this is str property name enclosed by ${...}, e.g. "${user.home}"
        for(Map.Entry<String, Properties> table: props.entrySet()) {
          Enumeration<?> e = table.getValue().propertyNames();
          while(e.hasMoreElements()) {
            String key = (String)e.nextElement();
            int endPos = pos + key.length() + 3;
            if (str.substring(pos, Math.min(str.length(), endPos)).equals("${"+key+"}")) {
              // found property name
              // replace "${property.name}" with the value of the property, e.g. /home/user
              String value = table.getValue().getProperty(key);
              str = str.substring(0, pos) + value + str.substring(endPos);
              // advance to the last character of the value
              pos = pos + value.length() - 1;
              break;
            }
          }
        }
      }
      pos = str.indexOf("${", pos+1);
      bsPos = str.indexOf("\\\\", pos+1);
      if ((bsPos!=-1) && (bsPos<pos)) { pos = bsPos; }
    }
    // LOG.log("end str = '"+str+"'");
    return str;
  }
  
  protected volatile boolean _windowListenerActive = false;
  protected WindowAdapter _windowListener = new WindowAdapter() {
    public void windowDeactivated(WindowEvent we) {
      if (_windowListenerActive) { ExecuteExternalDialog.this.toFront(); }
    }
  };
  
  /** Toggle visibility of this frame. Warning, it behaves like a modal dialog. */
  public void setVisible(boolean vis) {
    assert EventQueue.isDispatchThread();
    validate();
    if (vis) {
      updateProperties();
      _mainFrame.hourglassOn();
      addWindowListener(_windowListener);
      _windowListenerActive = true;
    }
    else {
      _windowListenerActive = false;
      removeWindowFocusListener(_windowListener);
      _mainFrame.hourglassOff();
      _mainFrame.toFront();
    }
    super.setVisible(vis);
  }
  
  /** Opens the file chooser to select a file, putting the result in the file field. */
  protected void chooseFile(JTextPane pane) {
    // Get the file from the chooser
    File wd = new File(replaceVariables(pane.getText().trim(), _props));
    if ((pane.getText().equals("")) ||
        (!wd.exists()) &&
        (!wd.isDirectory())) {
      wd = new File(System.getProperty("user.dir"));
    }

    _dirChooser.setSelectedFile(wd);
    int returnValue = _dirChooser.showDialog(wd);
    if (returnValue == DirectoryChooser.APPROVE_OPTION) {
      File chosen = _dirChooser.getSelectedDirectory();
      if (chosen != null) { pane.setText(chosen.toString()); };
    }
  }
}
