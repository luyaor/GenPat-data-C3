/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 *
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
 END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.print.*;
import java.beans.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Arrays;
import java.net.URL;

import gj.util.Vector;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.CodeStatus;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.model.*;
import edu.rice.cs.drjava.model.definitions.DefinitionsDocument;
import edu.rice.cs.drjava.model.debug.DebugManager;
import edu.rice.cs.drjava.model.debug.DebugException;
import edu.rice.cs.drjava.ui.CompilerErrorPanel.ErrorListPane;
import edu.rice.cs.drjava.ui.JUnitPanel.JUnitErrorListPane;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeOptionListener;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.util.ExitingNotAllowedException;
import edu.rice.cs.util.swing.DelegatingAction;

/**
 * DrJava's main window.
 * @version $Id$
 */
public class MainFrame extends JFrame implements OptionConstants {
  
  private static final int INTERACTIONS_TAB = 0;
  private static final int COMPILE_TAB = 1;
  private static final int OUTPUT_TAB = 2;
  private static final int JUNIT_TAB = 3;

  // GUI Dimensions
  private static final int GUI_WIDTH = 800;
  private static final int GUI_HEIGHT = 700;
  private static final int DOC_LIST_WIDTH = 150;

  private static final String ICON_PATH = "/edu/rice/cs/drjava/ui/icons/";

  private final SingleDisplayModel _model;

  private Hashtable _defScrollPanes;
  private DefinitionsPane _currentDefPane;

  // Used to determine whether the file title needs to be updated.
  private String _fileTitle = "";

  // These should be final but can't be, as the code is currently organized,
  // because they are not set in the constructor
  private CompilerErrorPanel _errorPanel;
  private OutputPane _outputPane;
  private InteractionsPane _interactionsPane;
  private DebugPanel _debugPanel;
  private JUnitPanel _junitPanel;
  private JPanel _statusBar;
  private JLabel _fileNameField;
  private JLabel _currLocationField;
  private PositionListener _posListener;
  private JTabbedPane _tabbedPane;
  private JSplitPane _docSplitPane;
  private JSplitPane _mainSplit;
  private JList _docList;
  private JMenuBar _menuBar;
  private JToolBar _toolBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _toolsMenu;
  private JMenu _debugMenu;
  private JMenu _helpMenu;
  private FindReplaceDialog _findReplace;
  private JButton _saveButton;
  private JButton _compileButton;
  private JButton _junitButton;
  private JCheckBoxMenuItem _debuggerEnabledMenuItem;
  private JMenuItem _runDebuggerMenuItem;
  private JMenuItem _resumeDebugMenuItem;
  private JMenuItem _stepDebugMenuItem;
  private JMenuItem _nextDebugMenuItem;
  private JMenuItem _suspendDebugMenuItem;
  private JMenuItem _toggleBreakpointMenuItem;
  private JMenuItem _printBreakpointsMenuItem;
  private JMenuItem _clearAllBreakpointsMenuItem;
  private LinkedList _tabs;
  
  private KeyBindingManager _keyBindingManager;
  
  public SingleDisplayModel getModel() {
    return _model;
  }

  /**
   * For opening files.
   * We have a persistent dialog to keep track of the last directory
   * from which we opened.
   */
  private JFileChooser _openChooser;
  /**
   * For saving files.
   * We have a persistent dialog to keep track of the last directory
   * from which we saved.
   */
  private JFileChooser _saveChooser;

  private FileOpenSelector _openSelector = new FileOpenSelector() {
    public File[] getFiles() throws OperationCanceledException {
      return getOpenFiles();
    }
  };

  private FileSaveSelector _saveSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException {
      return getSaveFile();
    }
    public void warnFileOpen() {
      // If we'd like to change to an error message for this, instead
      // of a warning, change both incidents of WARNING to ERROR.
      JOptionPane.showMessageDialog
        ( MainFrame.this,
         "This file is open in DrJava.  You may not overwrite it.",
         "File Open Warning",
         JOptionPane.WARNING_MESSAGE);
    }


    public boolean verifyOverwrite() {
      Object[] options = {"Yes","No"};
      int n = JOptionPane.showOptionDialog
        (MainFrame.this,
         "This file already exists.  Do you wish to overwrite the file?",
         "Confirm Overwrite",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         options,
         options[1]);
      if (n==JOptionPane.YES_OPTION){ return true;}
      else {return false;}

    }
  };

  /** Resets the document in the definitions pane to a blank one. */
  private Action _newAction = new AbstractAction("New") {
    public void actionPerformed(ActionEvent ae) {
      _new();
    }
  };

  /**
   * Asks user for file name and and reads that file into
   * the definitions pane.
   */
  private Action _openAction = new AbstractAction("Open...") {
    public void actionPerformed(ActionEvent ae) {
      _open();
    }
  };
  
  /**
   * Closes the current active document, prompting to save if necessary.
   */
  private Action _closeAction = new AbstractAction("Close") {
    public void actionPerformed(ActionEvent ae) {
      _close();
    }
  };

  /**
   * Closes all open documents, prompting to save if necessary.
   */
  private Action _closeAllAction = new AbstractAction("Close All") {
    public void actionPerformed(ActionEvent ae) {
      _closeAll();
    }
  };


  /** Saves the current document. */
  private Action _saveAction = new AbstractAction("Save") {
    public void actionPerformed(ActionEvent ae) {
      _save();
    }
  };

  /**
   * Asks the user for a file name and saves the document
   * currently in the definitions pane to that file.
   */
  private Action _saveAsAction = new AbstractAction("Save As...") {
    public void actionPerformed(ActionEvent ae) {
      _saveAs();
    }
  };

  /** Reverts the current document. */
  private Action _revertAction = new AbstractAction("Revert to Saved") {
    public void actionPerformed(ActionEvent ae) {
      String title = "Revert to Saved?";
      
      String message = "Are you sure you want to revert the current " +
        "file to the version on disk?";
      
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             message,
                                             title,
                                             JOptionPane.YES_NO_OPTION);
      if (rc == JOptionPane.YES_OPTION) {
        _revert();
      }
    }
  };

  /**
   * Saves all documents, prompting for file names as necessary
   */
  private Action _saveAllAction = new AbstractAction("Save All") {
    public void actionPerformed(ActionEvent ae) {
      _saveAll();
    }
  };

  /** Prints the current document. */
  private Action _printAction = new AbstractAction("Print...") {
    public void actionPerformed(ActionEvent ae) {
      _print();
    }
  };

  /** Opens the print preview window */
  private Action _printPreviewAction = new AbstractAction("Print Preview...") {
    public void actionPerformed(ActionEvent ae) {
      _printPreview();
    }
  };

  /** Opens the page setup window. */
  private Action _pageSetupAction = new AbstractAction("Page Setup...") {
    public void actionPerformed(ActionEvent ae) {
      _pageSetup();
    }
  };

  /** Compiles the document in the definitions pane. */
  private Action _compileAction = new AbstractAction("Compile Current Document") {
    public void actionPerformed(ActionEvent ae) {
      if (!_errorPanel.isDisplayed()) {
        ErrorListPane elp = _errorPanel.getErrorListPane();
        elp.setSize(_tabbedPane.getMinimumSize());
        showTab(_errorPanel);
      }
      _compile();
      _tabbedPane.setSelectedComponent(_errorPanel);
      _setDividerLocation();
    }
  };

  /** Runs JUnit on the document in the definitions pane. */
  private Action _junitAction = new AbstractAction("Test Using JUnit") {
    public void actionPerformed(ActionEvent ae) {
      // display the compiler output tab since a compilation will occur
      if (!_errorPanel.isDisplayed()) {
        ErrorListPane elp = _errorPanel.getErrorListPane();
        elp.setSize(_tabbedPane.getMinimumSize());
        showTab(_errorPanel);
      }
      // display the test output tab
      if (!_junitPanel.isDisplayed()) {
        JUnitErrorListPane elp = _junitPanel.getJUnitErrorListPane();
        elp.setSize(_tabbedPane.getMinimumSize());
        showTab(_junitPanel);
      }
      _junit();
      // it will not be displayed if a compilation error occured while running JUnit
      if (_junitPanel.isDisplayed())
        _tabbedPane.setSelectedComponent(_junitPanel);
      _setDividerLocation();
    }
  };

  /** Default cut action. */
  private Action _cutAction = new DefaultEditorKit.CutAction();

  /** Default copy action. */
  private Action _copyAction = new DefaultEditorKit.CopyAction();

  /** Default paste action. */
  private Action _pasteAction = new DefaultEditorKit.PasteAction();


  /** Undoes the last change to the active definitions document. */
  private DelegatingAction _undoAction = new DelegatingAction();

  /** Redoes the last undo to the active definitions document. */
  private DelegatingAction _redoAction = new DelegatingAction();

  /** Aborts current interaction. */
  private Action _abortInteractionAction = new AbstractAction("Abort Current Interaction") {  
    public void actionPerformed(ActionEvent ae) {
      String title = "Confirm abort interaction";
      
      String message = "Are you sure you want to abort the " +
        "current interaction?";
      
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             message,
                                             title,
                                             JOptionPane.YES_NO_OPTION);
      if (rc == JOptionPane.YES_OPTION) {
        _model.abortCurrentInteraction();
      }
    }
  };

  /** Closes the program. */
  private Action _quitAction = new AbstractAction("Quit") {
    public void actionPerformed(ActionEvent ae) {
      _model.quit();
    }
  };

  /** Selects all text in window*/
  private Action _selectAllAction = new AbstractAction("Select All") {
    public void actionPerformed(ActionEvent ae) {
      _selectAll();
    }
  };

  /** Opens the find/replace dialog. */
  private Action _findReplaceAction = new AbstractAction("Find/Replace...") {
    public void actionPerformed(ActionEvent ae) {
      if(!_findReplace.isDisplayed()) {
        //_tabbedPane.add("Find/Replace", _findReplace);
        showTab(_findReplace);
        _findReplace.beginListeningTo(_currentDefPane);
      }
      _tabbedPane.setSelectedComponent(_findReplace);  
      _findReplace.requestFocus();
      _setDividerLocation();
    }
  };

  /** Asks the user for a line number and goes there. */
  private Action _gotoLineAction = new AbstractAction("Goto Line...") {
    public void actionPerformed(ActionEvent ae) {
      _gotoLine();
    }
  };

  
  // SET CONFIGS coded here for convenience. None should be enabled
  /** Clears DrJava's output console. */
  private Action _clearOutputAction = new AbstractAction("Clear Console") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetConsole();
      //CONFIG.setSetting(INDENT_LEVEL, new Integer(8));
      //CONFIG.setSetting(FONT_MAIN, DrJava.CONFIG.getSetting(FONT_MAIN).deriveFont(32f));
      //CONFIG.setSetting(FONT_DOCLIST, DrJava.CONFIG.getSetting(FONT_DOCLIST).deriveFont(25f));
      //CONFIG.setSetting(FONT_TOOLBAR, DrJava.CONFIG.getSetting(FONT_TOOLBAR).deriveFont(18f));
      //CONFIG.setSetting(TOOLBAR_ICONS_ENABLED, new Boolean(!DrJava.CONFIG.getSetting(TOOLBAR_ICONS_ENABLED).booleanValue()));
      //CONFIG.setSetting(LINEENUM_ENABLED, new Boolean(!DrJava.CONFIG.getSetting(LINEENUM_ENABLED).booleanValue()));
      //CONFIG.setSetting(DEFINITIONS_COMMENT_COLOR, Color.red.darker());
      //CONFIG.setSetting(DEFINITIONS_MATCH_COLOR, Color.gray.brighter());
      //CONFIG.setSetting(JSR14_LOCATION, "/home/javaplt/packages/jsr14_adding_generics-1_2-ea/javac.jar");
      //CONFIG.setSetting(JAVAC_LOCATION, "/usr/local/bin/javac");
      //Vector<String> v = new Vector<String>();
      //v.addElement("/home/mcgraw/javafiles/");
      //CONFIG.setSetting(EXTRA_CLASSPATH, v);
    }
  };

  /** Clears the interactions console. */
  private Action _resetInteractionsAction =
    new AbstractAction("Reset Interactions")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.resetInteractions();
      //CONFIG.setSetting(JSR14_COLLECTIONSPATH, "/home/javaplt/packages/jsr14_adding_generics-1_2-ea/collect.jar");
      //CONFIG.setSetting(TOOLBAR_TEXT_ENABLED, new Boolean(!DrJava.CONFIG.getSetting(TOOLBAR_TEXT_ENABLED).booleanValue()));
    }
  };

  /** Pops up an info dialog. */
  private Action _aboutAction = new AbstractAction("About") {

    public void actionPerformed(ActionEvent ae) {
      new AboutDialog(MainFrame.this, _model.getAboutText()).show();
    }
  };

  /** Switches to next document. */
  private Action _switchToNextAction =
    new AbstractAction("Next Document")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.setNextActiveDocument();
    }
  };

  /** Switches to previous document. */
  private Action _switchToPrevAction =
    new AbstractAction("Previous Document")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.setPreviousActiveDocument();
    }
  };

  /** Enables the debugger */
  private Action _toggleDebuggerAction =
    new AbstractAction("Debugger Enabled")
  {
    public void actionPerformed(ActionEvent ae) {
      toggleDebugger();
    }
  };

  /** Runs the debugger on the current document */
  private Action _runDebuggerAction =
    new AbstractAction("Run Current Document in Debugger")
  {
    public void actionPerformed(ActionEvent ae) {
      _runDebugger();
    }
  };

  /** Resumes debugging */
  private Action _resumeDebuggerAction =
    new AbstractAction("Resume Debugging")
  {
    public void actionPerformed(ActionEvent ae) {
      _resumeDebugger();
    }
  };

  /** Steps into the next method call */
  private Action _stepDebugAction =
    new AbstractAction("Step Into")
  {
    public void actionPerformed(ActionEvent ae) {
      _debugStep();
    }
  };

  /** Runs the next line, without stepping into methods */
  private Action _nextDebugAction =
    new AbstractAction("Next Line")
  {
    public void actionPerformed(ActionEvent ae) {
      _debugNext();
    }
  };

  private Action _suspendDebugAction =
    new AbstractAction("Suspend Debugging")
  {
    public void actionPerformed(ActionEvent ae) {
      _debugSuspend();
    }
  };

  /** Toggles a breakpoint on the current line */
  private Action _toggleBreakpointAction =
    new AbstractAction("Toggle Breakpoint on Current Line")
  {
    public void actionPerformed(ActionEvent ae) {
      _toggleBreakpoint();
    }
  };

  /** Prints all breakpoints */
  private Action _printBreakpointsAction =
    new AbstractAction("Display All Breakpoints")
  {
    public void actionPerformed(ActionEvent ae) {
      _printBreakpoints();
    }
  };

  /** Clears all breakpoints */
  private Action _clearAllBreakpointsAction =
    new AbstractAction("Clear All Breakpoints")
  {
    public void actionPerformed(ActionEvent ae) {
      _clearAllBreakpoints();
    }
  };
  
  /** Clears all breakpoints */
  private Action _cutLineAction = new AbstractAction("Cut Line")
  {
    public void actionPerformed(ActionEvent ae) {
      if (CodeStatus.DEVELOPMENT) {
        ActionMap _actionMap = _currentDefPane.getActionMap();
        int oldCol = _model.getActiveDocument().getDocument().getCurrentCol();
        _actionMap.get(DefaultEditorKit.selectionEndLineAction).actionPerformed(ae);
        // if oldCol is equal to the current column, then selectionEndLine did
        // nothing, so we're at the end of the line and should remove the newline
        // character
        if (oldCol == _model.getActiveDocument().getDocument().getCurrentCol())
          _actionMap.get(DefaultEditorKit.deleteNextCharAction).actionPerformed(ae);
        else
          _cutAction.actionPerformed(ae);
      }
    }
  };
  
  /** How DrJava responds to window events. */
  private WindowListener _windowCloseListener = new WindowAdapter() {
    public void windowActivated(WindowEvent ev) {}
    public void windowClosed(WindowEvent ev) {}
    public void windowClosing(WindowEvent ev) {
      _model.quit();
    }
    public void windowDeactivated(WindowEvent ev) {}
    public void windowDeiconified(WindowEvent ev) {
      try {
        _model.getActiveDocument().revertIfModifiedOnDisk();
       } catch (IOException e) {
         _showIOError(e);
       }
    }
    public void windowIconified(WindowEvent ev) {
    }
    public void windowOpened(WindowEvent ev) {
      _currentDefPane.requestFocus();
    }
  };

  /** Creates the main window, and shows it. */
  public MainFrame() {
    
    if (CodeStatus.DEVELOPMENT) {
      _keyBindingManager = new KeyBindingManager(this);
    }
    _posListener = new PositionListener();
    _setUpStatusBar();

    _model = new SingleDisplayModel();
    String userdir = DrJava.CONFIG.getSetting(WORKING_DIRECTORY).toString();
    _openChooser = new JFileChooser(userdir);
    _openChooser.setFileFilter(new JavaSourceFilter());
    _openChooser.setMultiSelectionEnabled(true);
    _saveChooser = new JFileChooser(userdir);
    _saveChooser.setFileFilter(new JavaSourceFilter());
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // Set up listeners
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());

    //install
    _setUpTabs();

    // DefinitionsPane
    _defScrollPanes = new Hashtable();
    JScrollPane defScroll = _createDefScrollPane(_model.getActiveDocument());
    _currentDefPane = (DefinitionsPane) defScroll.getViewport().getView();
    
    // set up key-bindings
    if (CodeStatus.DEVELOPMENT) {
      _keyBindingManager.setActionMap(_currentDefPane.getActionMap());
      _setUpKeyBindingMaps();
    }
    
    _posListener.updateLocation();

    // Need to set undo/redo actions to point to the initial def pane
    // on switching documents later these pointers will also switch
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());

    _errorPanel.getErrorListPane().setLastDefPane(_currentDefPane);
    _errorPanel.reset();

    _junitPanel.getJUnitErrorListPane().setLastDefPane(_currentDefPane);
    _junitPanel.reset();

    // set up menu bar and actions
    _setUpActions();
    _setUpMenuBar();
    _setUpToolBar();
    _setUpDocumentSelector();

    setBounds(0, 0, GUI_WIDTH, GUI_HEIGHT);
    setSize(GUI_WIDTH, GUI_HEIGHT);

    // suggested from zaq@nosi.com, to keep the frame on the screen!
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    final int menubarHeight = 24;

    if (frameSize.height > screenSize.height - menubarHeight) {
      //       System.out.println("Too Tall! " +
      //     screenSize.height + " vs. " + frameSize.height);

      frameSize.height = screenSize.height - menubarHeight;

      //       System.out.println("Frame Height: " + frameSize.height);
    }

    if (frameSize.width > screenSize.width) {
      //       System.out.println("Too Wide! " +
      //     screenSize.width + " vs. " + frameSize.width);

      frameSize.width = screenSize.width;

      //       System.out.println("Frame Width: " + frameSize.height);
    }

    this.setSize(frameSize);
    this.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height - menubarHeight) / 2);

    _setUpPanes();
    updateFileTitle();

    // Set the fonts
    _setMainFont();
    
    Font doclistFont;
    if (CodeStatus.DEVELOPMENT) {
      doclistFont = DrJava.CONFIG.getSetting(FONT_DOCLIST);
    }
    else {
      doclistFont = new Font (DrJava.CONFIG.getSetting(FONT_DOCLIST_NAME).toString(),
                                 DrJava.CONFIG.getSetting(FONT_DOCLIST_STYLE).intValue(),
                                 DrJava.CONFIG.getSetting(FONT_DOCLIST_SIZE).intValue());
    }
    _docList.setFont(doclistFont);
    
    //Add option listeners
    
    if (CodeStatus.DEVELOPMENT) {
      DrJava.CONFIG.addOptionListener( OptionConstants.FONT_MAIN, new MainFontOptionListener());
      DrJava.CONFIG.addOptionListener( OptionConstants.FONT_DOCLIST, new DoclistFontOptionListener());
      DrJava.CONFIG.addOptionListener( OptionConstants.FONT_TOOLBAR, new ToolbarFontOptionListener());
      DrJava.CONFIG.addOptionListener( OptionConstants.TOOLBAR_ICONS_ENABLED, new ToolbarOptionListener());
      DrJava.CONFIG.addOptionListener( OptionConstants.TOOLBAR_TEXT_ENABLED, new ToolbarOptionListener());
      DrJava.CONFIG.addOptionListener( OptionConstants.LINEENUM_ENABLED, new LineEnumOptionListener());
    }
    

    // If any errors parsing config file, show them
    _showConfigException();
  }

  /**
   * Make the cursor an hourglass.
   */
  public void hourglassOn() {
    getGlassPane().setVisible(true);
  }

  /**
   * Return the cursor to normal.
   */
  public void hourglassOff() {
    getGlassPane().setVisible(false);
  }

  /**
   * Toggles whether the debugger is enabled or disabled,
   * and updates the display accordingly.
   */
  public void toggleDebugger() {
    // Make sure the debugger is available
    if (_debugPanel == null) return;

    try {
      DebugManager debugger = _model.getDebugManager();
      if (debugger.isReady()) {
        // Turn off debugger
        debugger.endSession();
        hideDebugger();
      }
      else {
        // Turn on debugger
        showDebugger();
      }
    }
    catch (NoClassDefFoundError err) {
      _showError(err, "Debugger Error",
                 "Unable to find the JPDA package for the debugger.\n" +
                 "Please make sure either tools.jar or jpda.jar is\n" +
                 "in your classpath when you start DrJava.");
      _setDebugMenuItemsEnabled(false);
    }
  }

  /**
   * Display the debugger tab and update the Debug menu accordingly.
   */
  public void showDebugger() {
     _model.getDebugManager().init(_debugPanel.getUIAdapter());
    _tabbedPane.add("Debug", _debugPanel);
    _tabbedPane.setSelectedComponent(_debugPanel);
    _setDebugMenuItemsEnabled(true);
  }

  /**
   * Hide the debugger tab and update the Debug menu accordingly.
   */
  public void hideDebugger() {
    _model.getDebugManager().cleanUp();
    _tabbedPane.remove(_debugPanel);
    _debugPanel.reset();
    _setDebugMenuItemsEnabled(false);
  }




  /**
   * Updates the title bar with the name of the active document.
   */
  public void updateFileTitle() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    String filename = _model.getDisplayFilename(doc);
    if (!filename.equals(_fileTitle)) {
      _fileTitle = filename;
      setTitle(filename + " - DrJava");
      _docList.repaint();
    }
    // Always update this field-- two files in different directories
    //  can have the same _fileTitle
    _fileNameField.setText(_model.getDisplayFullPath(doc));
  }

  /**
   * Prompt the user to select a place to open a file from, then load it.
   * Ask the user if they'd like to save previous changes (if the current
   * document has been modified) before opening.
   */
  public File[] getOpenFiles() throws OperationCanceledException {
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    File selection = _openChooser.getSelectedFile();
    if (selection != null) {
      _openChooser.setSelectedFile(selection.getParentFile());
      _openChooser.setSelectedFile(selection);
      _openChooser.setSelectedFile(null);
    }
    int rc = _openChooser.showOpenDialog(this);
    return getChosenFiles(_openChooser, rc);
  }

  /**
   * Prompt the user to select a place to save the current document.
   */
  public File getSaveFile() throws OperationCanceledException {
    // This redundant-looking hack is necessary for JDK 1.3.1 on Mac OS X!
    File selection = _saveChooser.getSelectedFile();
    if (selection != null) {
      _saveChooser.setSelectedFile(selection.getParentFile());
      _saveChooser.setSelectedFile(selection);
      _saveChooser.setSelectedFile(null);
    }
    int rc = _saveChooser.showSaveDialog(this);
    return getChosenFile(_saveChooser, rc);
  }

  /**
   * Returns the current DefinitionsPane.
   */
  public DefinitionsPane getCurrentDefPane() {
    return _currentDefPane;
  }


  /**
   * Makes sure save and compile buttons and menu items
   * are enabled and disabled appropriately after document
   * modifications.
   */
  private void _installNewDocumentListener(Document d) {
    d.addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
      _saveAction.setEnabled(true);
      //_compileAction.setEnabled(false);
      updateFileTitle();
    }
    public void insertUpdate(DocumentEvent e) {
      _saveAction.setEnabled(true);
      //_compileAction.setEnabled(false);
      updateFileTitle();
    }
    public void removeUpdate(DocumentEvent e) {
      _saveAction.setEnabled(true);
      //_compileAction.setEnabled(false);
      updateFileTitle();
    }
    });
  }


  private void _new() {
    _model.newFile();
  }

  private void _open() {
    try {
      _model.openFiles(_openSelector);
    }
    catch (AlreadyOpenException aoe) {
      OpenDefinitionsDocument openDoc = aoe.getOpenDocument();
      String filename = "File";
      try {
        filename = openDoc.getFile().getName();
      }
      catch (IllegalStateException ise) {
        // Can't happen: this open document must have a file
        throw new UnexpectedException(ise);
      }
      // Always switch to doc
      _model.setActiveDocument(openDoc);
      
      // Prompt to revert if modified
      if (openDoc.isModifiedSinceSave()) {
        String title = "Revert to Saved?";
        String message = filename + " is already open and modified.\n" +
          "Would you like to revert to the version on disk?\n";
        int choice = JOptionPane.showConfirmDialog(this,
                                                   message,
                                                   title,
                                                   JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
          _revert();
        }
      }
    }
    catch (OperationCanceledException oce) {
      // Ok, don't open a file
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _close() {
    _model.closeFile(_model.getActiveDocument());
  }

  private void _print() {
    try {
      _model.getActiveDocument().print();
    } catch (PrinterException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    } catch (BadLocationException e) {
      _showError(e, "Print Error", "An error occured while printing.");
    }
  }

  /**
   * Opens a new PrintPreview frame.
   */
  private void _printPreview() {
    try {
      _model.getActiveDocument().preparePrintJob();
      new PreviewFrame(_model, this);
    } catch (BadLocationException e) {
      _showError(e, "Print Error",
                 "An error occured while preparing the print preview.");
    } catch (IllegalStateException e) {
      _showError(e, "Print Error",
                 "An error occured while preparing the print preview.");
    }
  }

  private void _pageSetup() {
    PrinterJob job = PrinterJob.getPrinterJob();
    _model.setPageFormat(job.pageDialog(_model.getPageFormat()));
  }

  private void _closeAll() {
    _model.closeAllFiles();
  }


  private void _save() {
    try {
      _model.getActiveDocument().saveFile(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }


  private void _saveAs() {
    try {
      _model.getActiveDocument().saveFileAs(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _saveAll() {
    try {
      _model.saveAllFiles(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _revert() {
    try {
      _model.getActiveDocument().revertFile();
      _currentDefPane.resetUndo();
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _compile() {
    try {
      _model.getActiveDocument().startCompile();
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _junit() {
    try {
      _model.getActiveDocument().startJUnit();
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
    catch (ClassNotFoundException cnfe) {
      _showClassNotFoundError(cnfe);
    }
    catch (NoClassDefFoundError ncde) {
      _showNoClassDefError(ncde);
    }
    catch (ExitingNotAllowedException enae) {
      JOptionPane.showMessageDialog(this,
                                    "An exception occurred while running JUnit, which could\n" +
                                    "not be caught be DrJava.  Details about the exception should\n" +
                                    "have been printed to your console.\n\n",
                                    "Error Running JUnit",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Runs the debugger on the currently active document
   */
  private void _runDebugger() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    try{
      _model.getDebugManager().start(doc);
    }
    catch (ClassNotFoundException cnfe){
      // catch the "Class Not Found exception"; must be some kind of no-compile
      // issue
      _showClassNotFoundError(cnfe);
    }
  }

  /**
   * Resumes the debugger's current execution
   */
  private void _resumeDebugger() {
    _model.getDebugManager().resume();
  }

  /**
   * Steps the debugger
   */
  private void _debugStep() {
    _model.getDebugManager().step();
  }

  /**
   * Runs the next line through the debugger
   */
  private void _debugNext(){
    _model.getDebugManager().next();
  }

  /**
   * Suspends the current execution of the debugger
   */
  private void _debugSuspend(){
    _model.getDebugManager().suspend();
  }

  /**
   * Toggles a breakpoint on the current line
   */
  private void _toggleBreakpoint() {
    OpenDefinitionsDocument doc = _model.getActiveDocument();
    try {
      _model.getDebugManager().
        toggleBreakpoint(doc, doc.getDocument().getCurrentLine());
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
    catch (ClassNotFoundException cnfe) {
      _showClassNotFoundError(cnfe);
    }
    catch (DebugException de) {
      _showDebugError(de);
    }
  }

  /**
   * Displays all breakpoints currently set in the debugger
   */
  private void _printBreakpoints() {
    _model.getDebugManager().printBreakpoints();
  }

  /**
   * Clears all breakpoints from the debugger
   */
  private void _clearAllBreakpoints() {
    _model.getDebugManager().clearAllBreakpoints(true);
  }


  private void _showIOError(IOException ioe) {
    _showError(ioe, "Input/output error",
               "An I/O exception occurred during the last operation.");
  }

  private void _showClassNotFoundError(ClassNotFoundException cnfe) {
    _showError(cnfe, "Class Not Found",
               "A ClassNotFound exception occurred during the last operation.\n" +
               "Please check that your classpath includes all relevant " +
               "directories.\n\n");
  }

  private void _showNoClassDefError(NoClassDefFoundError ncde) {
    _showError(ncde, "No Class Def",
               "A NoClassDefFoundError occurred during the last operation.\n" +
               "Please check that your classpath includes all relevant paths.\n\n");
  }

  private void _showDebugError(DebugException de) {
    _showError(de, "Debug Error",
               "A JSwat error occurred in the last operation.\n\n");
  }

  private void _showError(Throwable e, String title, String message) {
    JOptionPane.showMessageDialog(this,
                                  message + "\n" + e,
                                  title,
                                  JOptionPane.ERROR_MESSAGE);
  }
  
  /**
   * Check if any errors occurred while parsing the config file,
   * and display a message if necessary.
   */
  private void _showConfigException() {
    if (DrJava.CONFIG.hadStartupException()) {
      Exception e = DrJava.CONFIG.getStartupException();
      _showError(e, "Error in Config File",
                 "Could not read the '.drjava' configuration file\n" +
                 "in your home directory.  Starting with default\n" +
                 "values instead.\n");
    }
  }


  /**
   * Returns the File selected by the JFileChooser.
   * @param fc File chooser presented to the user
   * @param choice return value from fc
   * @return Selected File
   * @throws OperationCanceledException if file choice canceled
   * @throws RuntimeException if fc returns a bad file or choice
   */
  private File getChosenFile(JFileChooser fc, int choice)
    throws OperationCanceledException
  {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        if (chosen != null) {
          //append ".java" if not written by user
          if (fc.getFileFilter() instanceof JavaSourceFilter) {
            if (chosen.getName().indexOf(".") == -1)
              return new File (chosen.getAbsolutePath() + ".java");
          }
          return chosen; 
        }
        else
          throw new RuntimeException("filechooser returned null file");
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + choice);
    }
  }
  /**
   * Returns the File selected by the JFileChooser.
   * @param fc File chooser presented to the user
   * @param choice return value from fc
   * @return Selected File
   * @throws OperationCanceledException if file choice canceled
   * @throws RuntimeException if fc returns a bad file or choice
   */
  private File[] getChosenFiles(JFileChooser fc, int choice)
    throws OperationCanceledException
  {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File[] chosen = fc.getSelectedFiles();
        if (chosen == null)
            throw new RuntimeException("filechooser returned null file");
    
        if (chosen[0] == null)
          chosen[0] = fc.getSelectedFile();
        return chosen;
    
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + choice);
    }
  }

  private void _selectAll() {
    _currentDefPane.selectAll();
  }

  /**
   * Ask the user what line they'd like to jump to, then go there.
   */
  private void _gotoLine() {
    final String msg = "What line would you like to go to?";
    final String title = "Go to Line";
    String lineStr = JOptionPane.showInputDialog(this,
                                                 msg,
                                                 title,
                                                 JOptionPane.QUESTION_MESSAGE);
    try {
      if (lineStr != null) {
        int lineNum = Integer.parseInt(lineStr);
        int pos = _model.getActiveDocument().gotoLine(lineNum);
        _currentDefPane.setCaretPosition(pos);

        // this code was taken from FindReplaceDialog's 
        // _selectFoundItem method
        JScrollPane defScroll = (JScrollPane) 
          _defScrollPanes.get(_model.getActiveDocument());
        int viewHeight = (int)defScroll.getViewport().getSize().getHeight();
        // Scroll to make sure this item is visible
        // Centers the selection in the viewport
        Rectangle startRect = _currentDefPane.modelToView(pos);
        int startRectY = (int)startRect.getY();
        startRect.setLocation(0, startRectY-viewHeight/2);
        //Rectangle endRect = _defPane.modelToView(to - 1);
        Point endPoint = new Point(0, startRectY+viewHeight/2-1);
        startRect.add(endPoint);      
      
        _currentDefPane.scrollRectToVisible(startRect);

        //Commented out this call because it would be impossible to
        //center the viewport on pos without passing in the viewport.
        //Perhaps setPositionAndScroll can be changed in the future to
        //allow this.
        //_currentDefPane.setPositionAndScroll(pos);
        _currentDefPane.requestFocus();
      }
    } 
    catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
    catch (BadLocationException ble) {}
  }

  /**
   * Update all appropriate listeners that the CompilerErrorModels
   * have changed.
   */
  private void _updateErrorListeners() {
    // Loop through each errorListener and tell it to update itself
    ListModel docs = _model.getDefinitionsDocuments();
    for (int i = 0; i < docs.getSize(); i++) {
      OpenDefinitionsDocument doc = (OpenDefinitionsDocument)
        docs.getElementAt(i);
      JScrollPane scroll = (JScrollPane) _defScrollPanes.get(doc);
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        CompilerErrorCaretListener listener = pane.getErrorCaretListener();
        listener.resetErrorModel();

        JUnitErrorCaretListener junitListener = pane.getJUnitErrorCaretListener();
        junitListener.resetErrorModel();
      }
    }
  }

  /**
   * Removes the CompilerErrorCaretListener corresponding to
   * the given document, after that document has been closed.
   * (Allows pane and listener to be garbage collected...)
   */
  private void _removeErrorListener(OpenDefinitionsDocument doc) {
    JScrollPane scroll = (JScrollPane) _defScrollPanes.get(doc);
    if (scroll != null) {
      DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
      pane.removeCaretListener(pane.getErrorCaretListener());
      pane.removeCaretListener(pane.getJUnitErrorCaretListener());
    }
  }

  /**
   * Initializes all action objects.
   * Adds icons and descriptions to several of the actions.
   * Note: this initialization will later be done in the
   * constructor of each action, which will subclass AbstractAction.
   */
  private void _setUpActions() {
    _setUpAction(_newAction, "New", "Create a new document");
    _setUpAction(_openAction, "Open", "Open an existing file");
    _setUpAction(_saveAction, "Save", "Save the current document");
    _setUpAction(_saveAsAction, "SaveAs", "Save the current document with a new name");
    _setUpAction(_revertAction, "Revert", "Revert the current document to saved version");

    _setUpAction(_closeAction, "Close", "Close the current document");
    _setUpAction(_closeAllAction, "CloseAll", "Close all documents");
    _setUpAction(_saveAllAction, "SaveAll", "Save all open documents");

    _setUpAction(_compileAction, "Compile", "Compile the current document");
    _setUpAction(_printAction, "Print", "Print the current document");
    _setUpAction(_pageSetupAction, "PageSetup", "Page Setup");
    _setUpAction(_printPreviewAction, "PrintPreview", "Print Preview");

    _setUpAction(_cutAction, "Cut", "Cut selected text to the clipboard");
    _setUpAction(_copyAction, "Copy", "Copy selected text to the clipboard");
    _setUpAction(_pasteAction, "Paste", "Paste text from the clipboard");
    _setUpAction(_selectAllAction, "Select All", "Select all text");

    _cutAction.putValue(Action.NAME, "Cut");
    _copyAction.putValue(Action.NAME, "Copy");
    _pasteAction.putValue(Action.NAME, "Paste");

    _setUpAction(_switchToPrevAction, "Back", "Previous Document");
    _setUpAction(_switchToNextAction, "Forward", "Next Document");

    _setUpAction(_findReplaceAction, "Find", "Find/Replace");
    _setUpAction(_aboutAction, "About", "About");

    _setUpAction(_undoAction, "Undo", "Undo previous command");
    _setUpAction(_redoAction, "Redo", "Redo last undo");

    _undoAction.putValue(Action.NAME, "Undo Previous Command");
    _redoAction.putValue(Action.NAME, "Redo Last Undo");

    _setUpAction(_abortInteractionAction, "Break", "Abort the current interaction");
    _setUpAction(_resetInteractionsAction, "Reset", "Reset interactions");
  
    _setUpAction(_junitAction, "Test", "Run JUnit over the current document");

  }

  private void _setUpAction(Action a, String icon, String shortDesc) {
    // Check whether to show icons
    //boolean useIcons = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    //if (useIcons) 
    a.putValue(Action.SMALL_ICON, _getIcon(icon + "16.gif"));
    a.putValue(Action.DEFAULT, icon);
    a.putValue(Action.SHORT_DESCRIPTION, shortDesc);
  }


  /**
   * Returns the icon with the given name.
   * All icons are assumed to reside in the /edu/rice/cs/drjava/ui/icons
   * directory.
   * @param name Name of icon image file
   * @return ImageIcon object constructed from the file
   */
  private ImageIcon _getIcon(String name) {
    URL url = this.getClass().getResource(ICON_PATH + name);
    if (url != null) {
      return new ImageIcon(url);
    }
    return null;
  }

  /**
   * Sets up the components of the menu bar and links them to the private
   * fields within MainFrame.  This method serves to make the code
   * more legible on the higher calling level, i.e., the constructor.
   */
  private void _setUpMenuBar() {
    boolean showDebugger = (_debugPanel != null);
    //boolean showDebugger = false;

    // Get proper cross-platform mask.
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    _menuBar = new JMenuBar();
    _fileMenu = _setUpFileMenu(mask);
    _editMenu = _setUpEditMenu(mask);
    _toolsMenu = _setUpToolsMenu(mask);
    if (showDebugger) _debugMenu = _setUpDebugMenu(mask);
    _helpMenu = _setUpHelpMenu(mask);

    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_toolsMenu);
    if (showDebugger) _menuBar.add(_debugMenu);
    _menuBar.add(_helpMenu);
    setJMenuBar(_menuBar);
  }
  
  private void _addMenuItem(JMenu menu, Action a, Option<KeyStroke> opt) {
    JMenuItem tmpItem;
    tmpItem = menu.add(a);
    
    KeyStroke ks = DrJava.CONFIG.getSetting(opt);
    // checks that "a" is the action associated with the keystroke
    // need to check in case two actions were assigned to the same
    // key in the config file
    if (_keyBindingManager.get(ks) == a) { 
      tmpItem.setAccelerator(ks);
      _keyBindingManager.putKeyToMenuItemMap(ks, tmpItem);
    }
    _keyBindingManager.addListener(opt, tmpItem);
    
  }
  
  /**
   * Creates and returns a file menu.  Side effects: sets values for
   * _saveMenuItem and _compileMenuItem.
   */
  private JMenu _setUpFileMenu(int mask) {
    JMenuItem tmpItem;
    JMenu fileMenu = new JMenu("File");

    // New, open 
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_newAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, mask));
    }
    else
      _addMenuItem(fileMenu, _newAction, KEY_NEW_FILE);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_openAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask));
    }
    else
      _addMenuItem(fileMenu, _openAction, KEY_OPEN_FILE);
    fileMenu.addSeparator();

    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_saveAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
    }
    else
      _addMenuItem(fileMenu, _saveAction, KEY_SAVE_FILE);
    _saveAction.setEnabled(false);

    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_saveAsAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                                                    mask | InputEvent.SHIFT_MASK));
    }
    else
      _addMenuItem(fileMenu, _saveAsAction, KEY_SAVE_FILE_AS);

    tmpItem = fileMenu.add(_saveAllAction);

    tmpItem = fileMenu.add(_revertAction);
    _revertAction.setEnabled(false);

    // Close, Close all
    fileMenu.addSeparator();
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_closeAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, mask));
    }
    else
    _addMenuItem(fileMenu, _closeAction, KEY_CLOSE_FILE);
    tmpItem = fileMenu.add(_closeAllAction);

    // Page setup, print preview, print
    fileMenu.addSeparator();

    tmpItem = fileMenu.add(_pageSetupAction);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_printPreviewAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, 
                                                    mask | InputEvent.SHIFT_MASK));
    }
    else
      _addMenuItem(fileMenu, _printPreviewAction, KEY_PRINT_PREVIEW);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_printAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
    }
    else
      _addMenuItem(fileMenu, _printAction, KEY_PRINT);
    // Quit
    fileMenu.addSeparator();
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = fileMenu.add(_quitAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, mask));
    }
    else
      _addMenuItem(fileMenu, _quitAction, KEY_QUIT);
    return fileMenu;
  }

  /**
   * Creates and returns a edit menu.
   */
  private JMenu _setUpEditMenu(int mask) {
    JMenuItem tmpItem;
    JMenu editMenu = new JMenu("Edit");

    // Undo, redo
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_undoAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask));
    }
    else
      _addMenuItem(editMenu, _undoAction, KEY_UNDO);
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_redoAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, mask));
    }
    else
      _addMenuItem(editMenu, _redoAction, KEY_REDO);
    // Cut, copy, paste
    editMenu.addSeparator();
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_cutAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask));
    }
    else
      _addMenuItem(editMenu, _cutAction, KEY_CUT);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_copyAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask));
    }
    else
      _addMenuItem(editMenu, _copyAction, KEY_COPY);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_pasteAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask));
    }
    else
      _addMenuItem(editMenu, _pasteAction, KEY_PASTE);   
    
    // Select All
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_selectAllAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, mask));
    }
    else
      _addMenuItem(editMenu, _selectAllAction, KEY_SELECT_ALL);

    // Find/replace, goto
    editMenu.addSeparator();
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_findReplaceAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, mask));
    }
    else
      _addMenuItem(editMenu, _findReplaceAction, KEY_FIND_REPLACE);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_gotoLineAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, mask));
    }
    else
      _addMenuItem(editMenu, _gotoLineAction, KEY_GOTO_LINE);
    // Next, prev doc
    editMenu.addSeparator();
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_switchToPrevAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, mask));
    }
    else
      _addMenuItem(editMenu, _switchToPrevAction, KEY_PREVIOUS_DOCUMENT);
    
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = editMenu.add(_switchToNextAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, mask));
    }
    else
      _addMenuItem(editMenu, _switchToNextAction, KEY_NEXT_DOCUMENT);
    // access to configurations GUI
    //editMenu.addSeparator();
    //tmpItem = editMenu.add(_preferencesAction);
    //tmpItem.setAccelerator(KeyStrong.getKeyStroke(KeyEvent.VK_
    
    // Add the menus to the menu bar
    return editMenu;
  }

  /**
   * Creates and returns a tools menu.
   */
  private JMenu _setUpToolsMenu(int mask) {
    JMenuItem tmpItem;
    JMenu toolsMenu = new JMenu("Tools");

    // Compile
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = toolsMenu.add(_compileAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    }
    else
      _addMenuItem(toolsMenu, _compileAction, KEY_COMPILE);

    toolsMenu.add(_junitAction);

    // Abort/reset interactions, clear console
    toolsMenu.addSeparator();

    _abortInteractionAction.setEnabled(false);
    if (!CodeStatus.DEVELOPMENT) {
      tmpItem = toolsMenu.add(_abortInteractionAction);
      tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }
    else
      _addMenuItem(toolsMenu, _abortInteractionAction, KEY_ABORT_INTERACTION);

    toolsMenu.add(_resetInteractionsAction);
    toolsMenu.add(_clearOutputAction);

    // Add the menus to the menu bar
    return toolsMenu;
  }

  /**
   * Creates and returns a debug menu.
   */
  private JMenu _setUpDebugMenu(int mask) {
    JMenuItem tempItem;
    JMenu debugMenu = new JMenu("Debug");

    // Enable debugging item
    _debuggerEnabledMenuItem = new JCheckBoxMenuItem(_toggleDebuggerAction);
    _debuggerEnabledMenuItem.setState(false);
    debugMenu.add(_debuggerEnabledMenuItem);

    debugMenu.addSeparator();

    // TO DO: Add accelerators?
    _runDebuggerMenuItem = debugMenu.add(_runDebuggerAction);
    _suspendDebugMenuItem = debugMenu.add(_suspendDebugAction);
    _resumeDebugMenuItem = debugMenu.add(_resumeDebuggerAction);
    _stepDebugMenuItem = debugMenu.add(_stepDebugAction);
    _nextDebugMenuItem = debugMenu.add(_nextDebugAction);

    debugMenu.addSeparator(); // breakpoints section:

    _toggleBreakpointMenuItem = debugMenu.add(_toggleBreakpointAction);
    _printBreakpointsMenuItem = debugMenu.add(_printBreakpointsAction);
    _clearAllBreakpointsMenuItem = debugMenu.add(_clearAllBreakpointsAction);

    // Start off disabled
    _setDebugMenuItemsEnabled(false);

    // Add the menu to the menu bar
    return debugMenu;
  }

  /**
   * Enables and disables the debug menu items.
   */
  private void _setDebugMenuItemsEnabled(boolean enabled) {
    _debuggerEnabledMenuItem.setState(enabled);
    _runDebuggerMenuItem.setEnabled(enabled);
    _resumeDebugMenuItem.setEnabled(enabled);
    _stepDebugMenuItem.setEnabled(enabled);
    _nextDebugMenuItem.setEnabled(enabled);
    _suspendDebugMenuItem.setEnabled(enabled);
    _toggleBreakpointMenuItem.setEnabled(enabled);
    _printBreakpointsMenuItem.setEnabled(enabled);
    _clearAllBreakpointsMenuItem.setEnabled(enabled);
  }

  /**
   * Creates and returns a help menu.
   */
  private JMenu _setUpHelpMenu(int mask) {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(_aboutAction);
    return helpMenu;
  }

  JButton _createManualToolbarButton(Action a) {
    final JButton ret;
    
    Font buttonFont;
    if (CodeStatus.DEVELOPMENT) {
      buttonFont = DrJava.CONFIG.getSetting(FONT_TOOLBAR);
    }
    else {
      buttonFont = new Font (DrJava.CONFIG.getSetting(FONT_TOOLBAR_NAME).toString(),
                             DrJava.CONFIG.getSetting(FONT_TOOLBAR_STYLE).intValue(),
                             DrJava.CONFIG.getSetting(FONT_TOOLBAR_SIZE).intValue());
    }

    // Check whether icons should be shown
    boolean useIcon = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_ICONS_ENABLED).booleanValue();
    boolean useText = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    final Icon icon = (useIcon) ? (Icon) a.getValue(Action.SMALL_ICON) : null;
    if (icon == null) {
      ret = new JButton( (String) a.getValue(Action.DEFAULT));
    }
    else {
      ret = new JButton(icon);
      if (useText) {
        ret.setText((String) a.getValue(Action.DEFAULT));
      }
    }

    ret.setEnabled(false);
    ret.addActionListener(a);
    ret.setToolTipText( (String) a.getValue(Action.SHORT_DESCRIPTION));
    ret.setFont(buttonFont);

    a.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
      if ("enabled".equals(evt.getPropertyName())) {
        Boolean val = (Boolean) evt.getNewValue();
        ret.setEnabled(val.booleanValue());
      }
    }
    });

    return ret;
  }

  /**
   * Sets up all buttons for the toolbar except for undo and redo, which use
   * _createManualToolbarButton.
   */
  public JButton _createToolbarButton(Action a) {
    boolean useText = DrJava.CONFIG.getSetting(OptionConstants.TOOLBAR_TEXT_ENABLED).booleanValue();
    
    Font buttonFont;
    if (CodeStatus.DEVELOPMENT) {
      buttonFont = DrJava.CONFIG.getSetting(FONT_TOOLBAR);
    }
    else {
      buttonFont = new Font (DrJava.CONFIG.getSetting(FONT_TOOLBAR_NAME).toString(),
                             DrJava.CONFIG.getSetting(FONT_TOOLBAR_STYLE).intValue(),
                             DrJava.CONFIG.getSetting(FONT_TOOLBAR_SIZE).intValue());
    }
    
    final JButton result = new JButton(a);
    result.setText((String) a.getValue(Action.DEFAULT));
    result.setFont(buttonFont);
    if (!useText && (result.getIcon() != null)) {
      result.setText("");
    }
    return result;
  }

  /**
   * Sets up the toolbar with several useful buttons.
   * Most buttons are always enabled, but those that are not are
   * maintained in fields to allow enabling and disabling.
   */
  private void _setUpToolBar() {
    _toolBar = new JToolBar();

    _toolBar.setFloatable(false);

    _toolBar.addSeparator();

    // New, open, save, close
    _toolBar.add(_createToolbarButton(_newAction));
    _toolBar.add(_createToolbarButton(_openAction));
    _saveButton = _createToolbarButton(_saveAction);
    _toolBar.add(_saveButton);
    _toolBar.add(_createToolbarButton(_closeAction));
    
    // Cut, copy, paste
    _toolBar.addSeparator();
    _toolBar.add(_createToolbarButton(_cutAction));
    _toolBar.add(_createToolbarButton(_copyAction));
    _toolBar.add(_createToolbarButton(_pasteAction));
    
    // Undo, redo
    // Simple workaround, for now, for bug # 520742:
    // Undo/Redo button text in JDK 1.3
    // We just manually create the JButtons, and we *don't* set up
    // PropertyChangeListeners on the action's name.
    _toolBar.addSeparator();
    _toolBar.add(_createManualToolbarButton(_undoAction));
    _toolBar.add(_createManualToolbarButton(_redoAction));
    
    // Find
    _toolBar.addSeparator();
    _toolBar.add(_createToolbarButton(_findReplaceAction));

    // Compile, reset, abort
    _toolBar.addSeparator();
    _compileButton = _createToolbarButton(_compileAction);
    _toolBar.add(_compileButton);
    _toolBar.add(_createToolbarButton(_resetInteractionsAction));
    _toolBar.add(_createToolbarButton(_abortInteractionAction));

    // Junit
    _toolBar.addSeparator();
    
    _junitButton = _createToolbarButton(_junitAction);
    _toolBar.add(_junitButton);


    getContentPane().add(_toolBar, BorderLayout.NORTH);
  }

  /**
   * Update the toolbar's buttons, following any change to TOOLBAR_ICONS_ENABLED, TOOLBAR_TEXT_ENABLED,
   *  or FONT_TOOLBAR (name, style, text)
   */ 
  private void _updateToolbarButtons() {
    
    if (CodeStatus.DEVELOPMENT) {
      Component[] buttons = _toolBar.getComponents();
      
      for (int i = 0; i< buttons.length; i++) {
        
        if (buttons[i] instanceof JButton) {
          
          JButton b = (JButton) buttons[i];
          Action a = b.getAction();

          // Work-around for strange configuration of undo/redo buttons
          /**if (a == null) {
            ActionListener[] al = b.getActionListeners(); // 1.4 only
            
            for (int j=0; j<al.length; j++) {
              if (al[j] instanceof Action) {
                a = (Action) al[j];
                break;
              }
            }
            
            */
            if (a==null) continue;
          //}
          
          boolean iconsEnabled = DrJava.CONFIG.getSetting(TOOLBAR_ICONS_ENABLED).booleanValue();
          
          if (b.getIcon() == null) {
            if (iconsEnabled) {
              //Icon I = (Icon) b.getAction().getValue(Action.SMALL_ICON);
              //System.out.println("button["+i+"]: " + I);
              b.setIcon( (Icon) a.getValue(Action.SMALL_ICON));
            }
          }
          else {
            if (!iconsEnabled && b.getText() != "") {
              b.setIcon(null);
            }
          }
          
          boolean textEnabled = DrJava.CONFIG.getSetting(TOOLBAR_TEXT_ENABLED).booleanValue();
          
          if (b.getText() == "") {
            if (textEnabled) {
              b.setText( (String) a.getValue(Action.DEFAULT));
            }
          }
          else {
            if (!textEnabled && b.getIcon() != null) {
              b.setText("");
            }
          }
          
          Font toolbarFont = DrJava.CONFIG.getSetting(FONT_TOOLBAR);
          
          b.setFont(toolbarFont);
          
        }
      }
    }   
  }
  
  
  /**
   * Sets up the status bar with the filename field.
   */
  private void _setUpStatusBar() {
    _fileNameField = new JLabel();
    _fileNameField.setFont(_fileNameField.getFont().deriveFont(Font.PLAIN));


    _currLocationField = new JLabel();
    _currLocationField.setFont(_currLocationField.getFont().deriveFont(Font.PLAIN));
    _currLocationField.setVisible(true);

    _statusBar = new JPanel( new BorderLayout() );
    _statusBar.add( _fileNameField, BorderLayout.WEST );
    _statusBar.add( _currLocationField, BorderLayout.EAST );
    _statusBar.setBorder(new
                           CompoundBorder(new EmptyBorder(2,2,2,2),
                                          new CompoundBorder(new BevelBorder(BevelBorder.LOWERED),
                                                             new EmptyBorder(2,2,2,2))));
    getContentPane().add(_statusBar, BorderLayout.SOUTH);
  }

  /**
   * Inner class to handle the updating of current position within the
   * document.  Registered with the definitionspane.
   **/
  private class PositionListener implements CaretListener {

    public void caretUpdate( CaretEvent ce ) {
      _model.getActiveDocument().
        syncCurrentLocationWithDefinitions(ce.getDot());
      updateLocation();
    }

    public void updateLocation() {
      DefinitionsDocument doc = _model.getActiveDocument().getDocument();
      /*
      _currLocationField.setText(doc.getCurrentLine() +
                                 ":" + doc.getCurrentCol() + "\t");
      */
      DefinitionsPane p = _currentDefPane;
      _currLocationField.setText(p.getCurrentLine() +
                                 ":" + p.getCurrentCol() + "\t");
    }
  }

  private void _setUpTabs() {
    _outputPane = new OutputPane(_model);
    _errorPanel = new CompilerErrorPanel(_model, this);
    _interactionsPane = new InteractionsPane(_model);
    _findReplace = new FindReplaceDialog(this, _model);

    // Try to create debug panel (see if JSwat is around)
    if (_model.getDebugManager() != null) {
      try {
        _debugPanel = new DebugPanel(_model, this);
      }
      catch(NoClassDefFoundError e) {
        // Don't use the debugger
        _debugPanel = null;
      }
    } else {
      _debugPanel = null;
    }

    _junitPanel = new JUnitPanel(_model, this);
    _tabbedPane = new JTabbedPane();
    _tabbedPane.add("Interactions", new BorderlessScrollPane(_interactionsPane));
    //_tabbedPane.add("Compiler output", _errorPanel);
    _tabbedPane.add("Console", new JScrollPane(_outputPane));
    //_tabbedPane.add("Test output", _junitPanel);
    
    _tabs = new LinkedList();

    _tabs.addLast(_errorPanel);
    _tabs.addLast(_junitPanel);
    _tabs.addLast(_findReplace);
    
    // Show compiler output pane by default
    showTab(_errorPanel);
    //showTab(_junitPanel);
    
    _tabbedPane.setSelectedIndex(0);
    
    // Select interactions pane when interactions tab is selected
    _tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
      if (_tabbedPane.getSelectedIndex() == INTERACTIONS_TAB) {
        _interactionsPane.requestFocus();
      }
    }
    });
  }

  /**
   * Configures the component used for selecting active documents.
   */
  private void _setUpDocumentSelector() {
    _docList = new JList(_model.getDefinitionsDocuments());
    /* {
     public String getToolTipText(MouseEvent event) {
     Point location = event.getPoint();
     int index = locationToIndex(location);
     String tip = null;
     if (index >= 0) {
     tip = _model.getDisplayFullPath(index);
     }
     return tip;
     }
     };
     _docList.setToolTipText("Document List"); */

    _docList.setSelectionModel(_model.getDocumentSelectionModel());
    _docList.setCellRenderer(new DocCellRenderer());
  }

  /**
   * Create a new DefinitionsPane and JScrollPane for an open
   * definitions document.
   * @param doc The open definitions document to wrap
   * @return JScrollPane containing a DefinitionsPane for the
   *         given document.
   */
  private JScrollPane _createDefScrollPane(OpenDefinitionsDocument doc) {
    DefinitionsPane pane = new DefinitionsPane(this, _model, doc);

    if (CodeStatus.DEVELOPMENT) {
      pane.setKeyBindingManager(_keyBindingManager);
    }
    
    // Add listeners
    _installNewDocumentListener(doc.getDocument());
    CompilerErrorCaretListener caretListener =
      new CompilerErrorCaretListener(doc, _errorPanel.getErrorListPane(), pane);
    pane.addErrorCaretListener(caretListener);

    JUnitErrorCaretListener junitCaretListener =
      new JUnitErrorCaretListener(doc, _junitPanel.getJUnitErrorListPane(), pane);
    pane.addJUnitErrorCaretListener(junitCaretListener);

    // add a listener to update line and column.
    pane.addCaretListener( _posListener );    
    
    // Add to a scroll pane
    JScrollPane scroll = new BorderlessScrollPane(pane,
                                                  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    //scroll.setBorder(null); // removes all default borders (MacOS X installs default borders)
    
    // can be used to make sure line wrapping occurs
    /*scroll.getViewport().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        pane.setSize(scroll.getViewport().getWidth(), pane.getHeight());
      }
    });*/
    
    if (CodeStatus.DEVELOPMENT) {
      
      if (DrJava.CONFIG.getSetting(LINEENUM_ENABLED).booleanValue()) {
        scroll.setRowHeaderView( new Rule(pane));
      }
    }
    
    _defScrollPanes.put(doc, scroll);
    
    return scroll;
  }


  private void _setUpPanes() {
    // Document list pane
    JScrollPane listScroll =
      new BorderlessScrollPane(_docList,
                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                               JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // DefinitionsPane
    JScrollPane defScroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());

    // Overall layout
    _docSplitPane = new BorderlessSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                            true,
                                            listScroll,
                                            defScroll);
    _mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                      true,
                                      _docSplitPane,
                                      _tabbedPane);
    _mainSplit.setResizeWeight(1.0);
    getContentPane().add(_mainSplit, BorderLayout.CENTER);
    // This is annoyingly order-dependent. Since split contains _docSplitPane,
    // we need to get split's divider set up first to give _docSplitPane an
    // overall size. Then we can set _docSplitPane's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
    _mainSplit.setDividerLocation(2*getHeight()/3);
    _mainSplit.setDividerSize(getHeight()/60);
    _mainSplit.setOneTouchExpandable(true);
    _docSplitPane.setDividerLocation(DOC_LIST_WIDTH);
    _docSplitPane.setOneTouchExpandable(true);
  }

  /**
   * Switch to the JScrollPane containing the DefinitionsPane
   * for the current active document.
   */
  private void _switchDefScrollPane() {    
    // Sync caret with location before swtiching
    _currentDefPane.getOpenDocument().
      syncCurrentLocationWithDefinitions( _currentDefPane.getCaretPosition() );

    JScrollPane scroll = 
      (JScrollPane) _defScrollPanes.get(_model.getActiveDocument());
    if (scroll == null) {
      throw new UnexpectedException(new Exception(
                                                  "Current definitions scroll pane not found."));
    }

    int oldLocation = _docSplitPane.getDividerLocation();
    _docSplitPane.setRightComponent(scroll);
    _docSplitPane.setDividerLocation(oldLocation);
    _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();

    // reset the undo/redo menu items
    _undoAction.setDelegatee(_currentDefPane.getUndoAction());
    _redoAction.setDelegatee(_currentDefPane.getRedoAction());    
  }
  
  /**
   * Addresses the Mac OS X bug where the scrollbars are disable in
   * one document after opening another document.
   */
  private void _reenableScrollBar() {
    JScrollPane scroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());
    if (scroll == null) {
      throw new UnexpectedException(new Exception(
                                                  "Current definitions scroll pane not found."));
    }
    
    JScrollBar oldbar = scroll.getVerticalScrollBar();
    JScrollBar newbar = scroll.createVerticalScrollBar();
    newbar.setMinimum(oldbar.getMinimum());
    newbar.setMaximum(oldbar.getMaximum());
    newbar.setValue(oldbar.getValue());
    newbar.setVisibleAmount(oldbar.getVisibleAmount());
    newbar.setEnabled(true);
    newbar.revalidate();
    scroll.setVerticalScrollBar(newbar);
    
    // This needs to be repeated for a horizontal scrollbar!
    
    oldbar = scroll.getHorizontalScrollBar();
    newbar = scroll.createHorizontalScrollBar();
    newbar.setMinimum(oldbar.getMinimum());
    newbar.setMaximum(oldbar.getMaximum());
    newbar.setValue(oldbar.getValue());
    newbar.setVisibleAmount(oldbar.getVisibleAmount());
    newbar.setEnabled(true);
    newbar.revalidate();
    scroll.setHorizontalScrollBar(newbar);
    scroll.revalidate();    
  }
  
  /**
   * Sets the current directory to be that of the given file.
   */
  private void _setCurrentDirectory(OpenDefinitionsDocument doc) {
    try {
      File file = doc.getFile();
      _openChooser.setCurrentDirectory(file);
      _saveChooser.setCurrentDirectory(file);
    }
    catch (IllegalStateException ise) {
      // no file, leave in current directory
    }
  }

  /**
   * Sets the font of all panes and panels to the main font
   * @param f is a Font object
   */
  private void _setMainFont() {
    
    Font f;
    if (CodeStatus.DEVELOPMENT) {
      f = DrJava.CONFIG.getSetting(FONT_MAIN);
    }
    else {
      f = new Font (DrJava.CONFIG.getSetting(FONT_MAIN_NAME).toString(),
                         DrJava.CONFIG.getSetting(FONT_MAIN_STYLE).intValue(),
                         DrJava.CONFIG.getSetting(FONT_MAIN_SIZE).intValue());
    }
    
    Iterator scrollPanes = _defScrollPanes.values().iterator();
    while (scrollPanes.hasNext()) {  
      JScrollPane scroll = (JScrollPane) scrollPanes.next();
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        pane.setFont(f);
        if (CodeStatus.DEVELOPMENT) {
          if (DrJava.CONFIG.getSetting(LINEENUM_ENABLED).booleanValue()) {
            scroll.setRowHeaderView( new Rule( pane) );
          }
        }
      }
    }
    _interactionsPane.setFont(f);
    _outputPane.setFont(f);
    if (_debugPanel != null) _debugPanel.setFonts(f);
    _errorPanel.setListFont(f);
    _junitPanel.setListFont(f);
  }
  
  
  /**
   *  Update the row header (line number enumeration) for the definitions scroll pane
   */
  private void _updateDefScrollRowHeader() {
    
    boolean ruleEnabled = DrJava.CONFIG.getSetting(LINEENUM_ENABLED).booleanValue();
    
    Iterator scrollPanes = _defScrollPanes.values().iterator();
    while (scrollPanes.hasNext()) {  
      JScrollPane scroll = (JScrollPane) scrollPanes.next();
      if (scroll != null) {
        DefinitionsPane pane = (DefinitionsPane) scroll.getViewport().getView();
        if (scroll.getRowHeader().getView() == null) {
          if (ruleEnabled) {
            scroll.setRowHeaderView(new Rule(pane));
          }
        }
        else {
          if (!ruleEnabled) {
            scroll.setRowHeaderView(null);
          }
        }
      }
    }
    
  }
  
  
  /**
   * put your documentation comment here
   */
  private class GlassPane extends JComponent {

    /**
     * put your documentation comment here
     */
    public GlassPane() {
      addKeyListener(new KeyAdapter() {});
      addMouseListener(new MouseAdapter() {});
      super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
  }

  private class ModelListener implements SingleDisplayModelListener{
    public void newFileCreated(OpenDefinitionsDocument doc) {
      _createDefScrollPane(doc);
    }

    public void fileSaved(OpenDefinitionsDocument doc) {
      _saveAction.setEnabled(false);
      _revertAction.setEnabled(true);
      //_compileAction.setEnabled(true);
      updateFileTitle();
      _currentDefPane.requestFocus();
    }

    public void fileOpened(OpenDefinitionsDocument doc) {     
      // Fix OS X scrollbar bug before switching
      _reenableScrollBar();   
      _createDefScrollPane(doc);
    }

    public void fileClosed(OpenDefinitionsDocument doc) {
      _removeErrorListener(doc);
      _defScrollPanes.remove(doc);
    }
    public void fileReverted(OpenDefinitionsDocument doc) {
      updateFileTitle();
      _currentDefPane.setPositionAndScroll(0);
    }
    public void activeDocumentChanged(OpenDefinitionsDocument active) {
      _switchDefScrollPane();

      boolean isModified = active.isModifiedSinceSave();
      boolean canCompile = (!isModified && !active.isUntitled());
      _saveAction.setEnabled(isModified);
      _revertAction.setEnabled(!active.isUntitled());
      //_compileAction.setEnabled(canCompile);

      // Update error highlights
      _errorPanel.getErrorListPane().selectNothing();
      _junitPanel.getJUnitErrorListPane().selectNothing();
      //_junitPanel.reset();
      
      int pos = _currentDefPane.getCaretPosition();
      _currentDefPane.getErrorCaretListener().updateHighlight(pos);
      _currentDefPane.getJUnitErrorCaretListener().updateHighlight(pos);
     
      _setCurrentDirectory(active);

      updateFileTitle();
      //_posListener.updateLocation();
      _currentDefPane.requestFocus();
      _posListener.updateLocation();
      
      try {
        active.revertIfModifiedOnDisk();
      } catch (IOException e) {
        _showIOError(e);
      }
      if(_findReplace.isDisplayed()) {
        _findReplace.stopListening();
        _findReplace.beginListeningTo(_currentDefPane);
        //uninstallFindReplaceDialog(_findReplace);
        //installFindReplaceDialog(_findReplace);
      }
      
    }
    
    public void interactionStarted() {
      _interactionsPane.setEditable(false);
      _interactionsPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      _abortInteractionAction.setEnabled(true);
    }

    public void interactionEnded() {
      _abortInteractionAction.setEnabled(false);
      _interactionsPane.setCursor(null);
      _interactionsPane.setEditable(true);
      int pos = _interactionsPane.getDocument().getLength();
      _interactionsPane.setCaretPosition(pos);
    }

    public void compileStarted() {
      if (!_errorPanel.isDisplayed())
        showTab(_errorPanel);
      //_tabbedPane.setSelectedIndex(COMPILE_TAB);
      _saveAction.setEnabled(false);
      //_compileAction.setEnabled(false);
      hourglassOn();
    }

    public void compileEnded() {
      hourglassOff();
      _updateErrorListeners();
      _errorPanel.reset();
      //_compileAction.setEnabled(true);
    }
    
    public void compileErrorDuringJUnit() {
      System.err.println("Called");
      removeTab(_junitPanel);
      _tabbedPane.setSelectedComponent(_errorPanel);
    }

    public void junitStarted() {
      //_tabbedPane.setSelectedIndex(JUNIT_TAB);
      _saveAction.setEnabled(false);
      hourglassOn();
    }

    public void junitEnded() {
      hourglassOff();
      _updateErrorListeners();
      _errorPanel.reset();
      _junitPanel.reset();
      //_tabbedPane.setSelectedComponent(_junitPanel);
    }

    public void interactionsExited(int status) {
      String msg = "The interactions window was terminated by a call " +
        "to System.exit(" + status + ").\n" +
        "The interactions window will now be restarted.";

      String title = "Interactions terminated by System.exit(" + status + ")";

      JOptionPane.showMessageDialog(MainFrame.this,
                                    msg,
                                    title,
                                    JOptionPane.INFORMATION_MESSAGE);

      // we don't restore the interactions pane to life, since
      // the interactionsReset event will do it.
    }

    public void interactionsReset() {
      interactionEnded();
    }

    public void consoleReset() {
    }

    public void saveAllBeforeProceeding(GlobalModelListener.SaveReason reason) {
      String message;
      if (reason == COMPILE_REASON) {
        message =
          "To compile, you must first save ALL modified files.\n" +
          "Would you like to save and then compile?";
      }
      else if (reason == JUNIT_REASON) {
        message =
          "To run JUnit, you must first save and compile ALL modified\n" +
          "files. Would like to save and then compile?";
      }
      else if (reason == DEBUG_REASON) {
        message =
          "To use debugging commands, you must first save and compile\n" +
          "ALL modified files. Would like to save and then compile?";
      }
      else {
        throw new RuntimeException("Invalid reason for forcing a save.");
      }
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, message,
                                             "Must save all files to continue",
                                             JOptionPane.YES_NO_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _saveAll();
          break;
        case JOptionPane.NO_OPTION:
        case JOptionPane.CANCEL_OPTION:
        case JOptionPane.CLOSED_OPTION:
          // do nothing
          break;
        default:
          throw new RuntimeException("Invalid rc from showConfirmDialog: " + rc);
      }
    }

    public void nonTestCase() {

      String message =
        "The  Test  button  (and menu item) in  DrJava  invoke the JUnit\n"  +
        "test  harness  over  the currently open document.  In order for\n" +
        "that  to  work,  the  currently  open  document  must be a valid\n" +
        "JUnit TestCase,  i.e., a subclass of junit.framework.TestCase.\n\n" +

        "For information on how to write JUnit TestCases, visit:\n\n" +

        "  http://www.junit.org/\n\n";

      JOptionPane.showMessageDialog(MainFrame.this, message,
                                    "Test Works Only On JUnit TestCases",
                                    JOptionPane.ERROR_MESSAGE);


    }

    /**
     * Check if the current document has been modified. If it has, ask the user
     * if he would like to save or not, and save the document if yes. Also
     * give the user a "cancel" option to cancel doing the operation that got
     * us here in the first place.
     *
     * @return A boolean, if true means the user is OK with the file being saved
     *         or not as they chose. If false, the user wishes to cancel.
     */
    public boolean canAbandonFile(OpenDefinitionsDocument doc) {
      String fname;

      _model.setActiveDocument(doc);

      try {
        File file = doc.getFile();
        fname = file.getName();
      }
      catch (IllegalStateException ise) {
        // No file exists
        fname = "Untitled file";
      }

      String text = fname + " has been modified. Would you like to save it?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             "Save " + fname + "?",
                                             text,
                                             JOptionPane.YES_NO_CANCEL_OPTION);

      switch (rc) {
        case JOptionPane.YES_OPTION:
          _save();
          return true;
        case JOptionPane.NO_OPTION:
          return true;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }
    }

    /**
     * Called to ask the listener if it is OK to revert the current
     * document to a newer version saved on file.
     */
    public boolean shouldRevertFile(OpenDefinitionsDocument doc) {
      
      String fname;
      
      if (! _model.getActiveDocument().equals(doc)) {
        _model.setActiveDocument(doc);
      }
      
      try {
        File file = doc.getFile();
        fname = file.getName();
      }
      catch (IllegalStateException ise) {
        // No file exists
        fname = "Untitled file";
      }
      
      String text = fname + " has changed on disk. Would you like to " +
      "reload it?\nThis will discard any changes you have made.";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
                                             text,
                                             fname + " Modified on Disk",
                                             JOptionPane.YES_NO_OPTION);
      
      switch (rc) {
        case JOptionPane.YES_OPTION:
          return true;
        case JOptionPane.NO_OPTION:
          return false;
        case JOptionPane.CLOSED_OPTION:
        case JOptionPane.CANCEL_OPTION:
          return false;
        default:
          throw new RuntimeException("Invalid rc: " + rc);
      }
    }

  }
 

  /**
   * Prints a display label for each item in the document list.
   */
  private class DocCellRenderer extends DefaultListCellRenderer {
    /**
     * Change the display of the label, but keep other
     * behavior the same.
     */
    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean iss,
                                                  boolean chf)
    {
      // Use exisiting behavior
      super.getListCellRendererComponent(list, value, index, iss, chf);

      // Change label
      String label = _model.getDisplayFilename((OpenDefinitionsDocument)value);
      setText(label);

      return this;
    }
  }

  public JViewport getDefViewport() {
    JScrollPane defScroll = (JScrollPane)
      _defScrollPanes.get(_model.getActiveDocument());
    return defScroll.getViewport();
  }

  public void removeTab(Component c) {
    _tabbedPane.remove(c);
    ((TabbedPanel)c).setDisplayed(false);
    _tabbedPane.setSelectedIndex(0);
    _currentDefPane.requestFocus();
  }
  
  public void showTab(Component c) {
    int numVisible = 0;
    TabbedPanel tp;
    
    for (int i = 0; i < _tabs.size(); i++) {
      tp = (TabbedPanel)_tabs.get(i);
      if (tp == c) {
        // 2 right now is a magic number for the number of tabs always visible
        // interactions & console
        _tabbedPane.insertTab(tp.getName(), null, tp, null, numVisible + 2);
        _tabbedPane.setSelectedIndex(numVisible + 2);
        tp.setDisplayed(true);
        return;
      }
      if (tp.isDisplayed())
        numVisible++;
    }
  }
  
  private void _setDividerLocation() {    
    int divLocation = _mainSplit.getHeight() - 
      _mainSplit.getDividerSize() - 
      (int)_tabbedPane.getMinimumSize().getHeight();
    if (_mainSplit.getDividerLocation() > divLocation)
      _mainSplit.setDividerLocation(divLocation);
  }
  
  /**
   * Builds the Hashtables in KeyBindingManager that are used to keep track
   * of key-bindings and allows for live updating, conflict resolution, and
   * intelligent error messages (the ActionToNameMap)
   */
  private void _setUpKeyBindingMaps() {
    if (CodeStatus.DEVELOPMENT) {
      ActionMap _actionMap = _currentDefPane.getActionMap();
      _keyBindingManager.putActionToNameMap(_newAction, 
                                            "New File");
      _keyBindingManager.putActionToNameMap(_openAction, 
                                            "Open File");
      _keyBindingManager.putActionToNameMap(_saveAction, 
                                            "Save File");
      _keyBindingManager.putActionToNameMap(_saveAsAction, 
                                            "Save File As");
      _keyBindingManager.putActionToNameMap(_closeAction, 
                                            "Close File");
      _keyBindingManager.putActionToNameMap(_printPreviewAction, 
                                            "Print Preview");
      _keyBindingManager.putActionToNameMap(_printAction, 
                                            "Print");
      _keyBindingManager.putActionToNameMap(_quitAction, 
                                            "Quit");
      _keyBindingManager.putActionToNameMap(_undoAction, 
                                            "Undo");
      _keyBindingManager.putActionToNameMap(_redoAction, 
                                            "Redo");
      _keyBindingManager.putActionToNameMap(_cutAction, 
                                            "Cut");
      _keyBindingManager.putActionToNameMap(_copyAction, 
                                            "Copy");
      _keyBindingManager.putActionToNameMap(_pasteAction, 
                                            "Paste");
      _keyBindingManager.putActionToNameMap(_selectAllAction, 
                                            "Select All");
      _keyBindingManager.putActionToNameMap(_findReplaceAction, 
                                            "Find Replace");
      _keyBindingManager.putActionToNameMap(_gotoLineAction, 
                                            "Goto Line");
      _keyBindingManager.putActionToNameMap(_switchToPrevAction, 
                                            "Previous Document");
      _keyBindingManager.putActionToNameMap(_switchToNextAction, 
                                            "Next Document");
      _keyBindingManager.putActionToNameMap(_compileAction, 
                                            "Compile");
      _keyBindingManager.putActionToNameMap(_abortInteractionAction, 
                                            "Abort Interaction");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.backwardAction), 
                                            "Backward");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionBackwardAction), 
                                            "Selection Backward");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.beginAction), 
                                            "Begin Document");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionBeginAction), 
                                            "Selection Begin Document");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.beginLineAction), 
                                            "Begin Line");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionBeginLineAction), 
                                            "Selection Begin Line");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.beginParagraphAction), 
                                            "Begin Paragraph");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionBeginParagraphAction), 
                                            "Selection Begin Paragraph");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.previousWordAction), 
                                            "Previous Word");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionPreviousWordAction), 
                                            "Selection Previous Word");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.downAction), 
                                            "Down");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionDownAction), 
                                            "Selection Down");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.endAction), 
                                            "End Document");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionEndAction), 
                                            "Selection End Document");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.endLineAction), 
                                            "End Line");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionEndLineAction), 
                                            "Selection End Line");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.endParagraphAction), 
                                            "End Paragraph");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionEndParagraphAction), 
                                            "Selection End Paragraph");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.nextWordAction), 
                                            "Next Word");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionNextWordAction), 
                                            "Selection Next Word");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.forwardAction), 
                                            "Forward");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionForwardAction), 
                                            "Selection Forward");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.pageDownAction), 
                                            "Page Down");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.pageUpAction), 
                                            "Page Up");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.upAction), 
                                            "Up");
      _keyBindingManager.putActionToNameMap(_actionMap.get(DefaultEditorKit.selectionUpAction), 
                                            "Selection Up");
      _keyBindingManager.putActionToNameMap(_cutLineAction, 
                                            "Cut Line");
      
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_NEW_FILE), _newAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_OPEN_FILE), _openAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_SAVE_FILE), _saveAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_SAVE_FILE_AS), _saveAsAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_CLOSE_FILE), _closeAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_PRINT_PREVIEW), _printPreviewAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_PRINT), _printAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_QUIT), _quitAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_UNDO), _undoAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_REDO), _redoAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_CUT), _cutAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_COPY), _copyAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_PASTE), _pasteAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_SELECT_ALL), _selectAllAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_FIND_REPLACE), _findReplaceAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_GOTO_LINE), _gotoLineAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_PREVIOUS_DOCUMENT), 
                 _switchToPrevAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_NEXT_DOCUMENT), 
                 _switchToNextAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_COMPILE), _compileAction);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_ABORT_INTERACTION), 
                 _abortInteractionAction);
      
      _keyBindingManager.addShiftAction(KEY_BACKWARD, 
                                        DefaultEditorKit.backwardAction, 
                                        DefaultEditorKit.selectionBackwardAction);
 
      _keyBindingManager.addShiftAction(KEY_BEGIN_DOCUMENT, 
                                        DefaultEditorKit.beginAction, 
                                        DefaultEditorKit.selectionBeginAction);
      
      _keyBindingManager.addShiftAction(KEY_BEGIN_LINE, 
                                        DefaultEditorKit.beginLineAction, 
                                        DefaultEditorKit.selectionBeginLineAction);
          
      _keyBindingManager.addShiftAction(KEY_BEGIN_PARAGRAPH, 
                                        DefaultEditorKit.beginParagraphAction, 
                                        DefaultEditorKit.selectionBeginParagraphAction); 
      
      _keyBindingManager.addShiftAction(KEY_PREVIOUS_WORD, 
                                        DefaultEditorKit.previousWordAction, 
                                        DefaultEditorKit.selectionPreviousWordAction);
       
      _keyBindingManager.addShiftAction(KEY_DOWN, 
                                        DefaultEditorKit.downAction, 
                                        DefaultEditorKit.selectionDownAction);
      
      _keyBindingManager.addShiftAction(KEY_END_DOCUMENT, 
                                        DefaultEditorKit.endAction, 
                                        DefaultEditorKit.selectionEndAction);
       
      _keyBindingManager.addShiftAction(KEY_END_LINE, 
                                        DefaultEditorKit.endLineAction, 
                                        DefaultEditorKit.selectionEndLineAction);
      
      _keyBindingManager.addShiftAction(KEY_END_PARAGRAPH, 
                                        DefaultEditorKit.endParagraphAction, 
                                        DefaultEditorKit.selectionEndParagraphAction);
       
      _keyBindingManager.addShiftAction(KEY_NEXT_WORD, 
                                        DefaultEditorKit.nextWordAction, 
                                        DefaultEditorKit.selectionNextWordAction);
      
      _keyBindingManager.addShiftAction(KEY_FORWARD, 
                                        DefaultEditorKit.forwardAction, 
                                        DefaultEditorKit.selectionForwardAction);
      
      _keyBindingManager.addShiftAction(KEY_UP, 
                                        DefaultEditorKit.upAction, 
                                        DefaultEditorKit.selectionUpAction);     
      
      // These last methods have no default selection methods
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_PAGE_DOWN), 
                                   _actionMap.get(DefaultEditorKit.pageDownAction));
      _keyBindingManager.addListener(KEY_PAGE_DOWN, null);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_PAGE_UP), 
                                   _actionMap.get(DefaultEditorKit.pageUpAction));
      _keyBindingManager.addListener(KEY_PAGE_UP, null);
      _keyBindingManager.mapInsert(DrJava.CONFIG.getSetting(KEY_CUT_LINE), 
                                   _cutLineAction);
      _keyBindingManager.addListener(KEY_CUT_LINE, null);
    }
  }
  
  /**
   * The OptionListener for FONT_MAIN 
   */
  private class MainFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      _setMainFont();
    }
    
  }
    
  /**
   * The OptionListener for FONT_DOCLIST
   */
  private class DoclistFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      Font doclistFont = DrJava.CONFIG.getSetting(FONT_DOCLIST);
      _docList.setFont(doclistFont);
    }
  }
  
  
  /**
   *  The OptionListener for FONT_TOOLBAR
   */
  private class ToolbarFontOptionListener implements OptionListener<Font> {
    public void optionChanged(OptionEvent<Font> oce) {
      _updateToolbarButtons();
    }
  }
  /**
   *  The OptionListener for TOOLBAR options
   */
  private class ToolbarOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _updateToolbarButtons();
    }
  }
  
  /**
   *  The OptionListener for LINEENUM_ENABLED
   */
  private class LineEnumOptionListener implements OptionListener<Boolean> {
    public void optionChanged(OptionEvent<Boolean> oce) {
      _updateDefScrollRowHeader();
    }
  }
}
