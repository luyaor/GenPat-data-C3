package edu.rice.cs.drjava.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import edu.rice.cs.drjava.model.*;
import edu.rice.cs.util.UnexpectedException;
import edu.rice.cs.drjava.Version;

/**
 * DrJava's main window.
 * @version $Id$
 */
public class MainFrame extends JFrame {
  private static final int INTERACTIONS_TAB = 0;
  private static final int COMPILE_TAB = 1;
  private static final int OUTPUT_TAB = 2;
  
  // GUI Dimensions
  private static final int GUI_WIDTH = 800;
  private static final int GUI_HEIGHT = 700;
  private static final int MAX_DOC_LIST_WIDTH = 200;
  
  private CompilerErrorPanel _errorPanel;
  private LinkedList _defScrollPanes;
  private DefinitionsPane _currentDefPane;
  private DefaultListModel _definitionsDocs;
  private int _currentDocIndex;
  private OutputPane _outputPane;
  private InteractionsPane _interactionsPane;
  private JTabbedPane _tabbedPane;
  private JSplitPane _docSplitPane;
  private JList _docList;
  private JMenuBar _menuBar;
  private JMenu _fileMenu;
  private JMenu _editMenu;
  private JMenu _helpMenu;
  private GlobalModel _model;
  private FindReplaceDialog _findReplace;
  private JButton _saveButton;
  private JButton _compileButton;
  private JMenuItem _saveMenuItem;
  private JMenuItem _compileMenuItem;

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
    public File getFile() throws OperationCanceledException {
      return getOpenFile();
    }
  };

  private FileSaveSelector _saveSelector = new FileSaveSelector() {
    public File getFile() throws OperationCanceledException {
      return getSaveFile();
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
  private Action _openAction = new AbstractAction("Open") {
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
  private Action _saveAsAction = new AbstractAction("Save as") {
    public void actionPerformed(ActionEvent ae) {
      _saveAs();
    }
  };

  /** Compiles the document in the definitions pane. */
  private Action _compileAction = new AbstractAction("Compile") {
    public void actionPerformed(ActionEvent ae) {
      _currentDefPane.getOpenDocument().startCompile();
    }
  };

  /** Closes the program. */
  private Action _quitAction = new AbstractAction("Quit") {
    public void actionPerformed(ActionEvent ae) {
      _model.quit();
    }
  };

  /** Opens the find/replace dialog. */
  private Action _findReplaceAction = new AbstractAction("Find/Replace") {
    public void actionPerformed(ActionEvent ae) {
      _findReplace.setMachine(_currentDefPane);
      _findReplace.show();
    }
  };

  /** Asks the user for a line number and goes there. */
  private Action _gotoLineAction = new AbstractAction("Goto line") {
    public void actionPerformed(ActionEvent ae) {
      _gotoLine();
    }
  };

  /** Clears DrJava's output console. */
  private Action _clearOutputAction = new AbstractAction("Clear Output") {
    public void actionPerformed(ActionEvent ae) {
      _model.resetConsole();
    }
  };

  /** Clears the interactions console. */
  private Action _resetInteractionsAction =
    new AbstractAction("Reset interactions")
  {
    public void actionPerformed(ActionEvent ae) {
      _model.resetInteractions();
    }
  };

  /** Pops up an info dialog. */
  private Action _aboutAction = new AbstractAction("About") {

    public void actionPerformed(ActionEvent ae) {
      final String message = "DrJava, brought to you by the Java PLT "
                           + "research group at Rice University.\n"
                           + "http://www.cs.rice.edu/~javaplt/drjava\n\n"
                           + "Version: "
                           + Version.BUILD_TIME;
      JOptionPane.showMessageDialog(MainFrame.this, message);
    }
  };


  /** Switches to next document. */
  private Action _switchToNextAction =
    new AbstractAction("Next Document")
  {
    public void actionPerformed(ActionEvent ae) {
      if (_currentDocIndex < _definitionsDocs.size()-1) {
        _currentDocIndex++;
      }
      _docList.setSelectedIndex(_currentDocIndex);
    }
  };

  /** Switches to previous document. */
  private Action _switchToPrevAction =
    new AbstractAction("Previous Document")
  {
    public void actionPerformed(ActionEvent ae) {
      if (_currentDocIndex > 0) {
        _currentDocIndex--;
      }
      _docList.setSelectedIndex(_currentDocIndex);
    }
  };


  /** How DrJava responds to window events. */
  private WindowListener _windowCloseListener = new WindowListener() {
    public void windowActivated(WindowEvent ev) {}
    public void windowClosed(WindowEvent ev) {}
    public void windowClosing(WindowEvent ev) {
      _model.quit();
    }
    public void windowDeactivated(WindowEvent ev) {}
    public void windowDeiconified(WindowEvent ev) {}
    public void windowIconified(WindowEvent ev) {}
    public void windowOpened(WindowEvent ev) {
      _currentDefPane.requestFocus();
    }
  };

  /** Creates the main window, and shows it. */
  public MainFrame() {
    _model = new DefaultGlobalModel();
    OpenDefinitionsDocument doc = _model.newFile();
    _currentDocIndex = 0;
    _definitionsDocs = new DefaultListModel();
    _definitionsDocs.addElement(doc);

    _openChooser = new JFileChooser(System.getProperty("user.dir"));
    _openChooser.setFileFilter(new JavaSourceFilter());
    _saveChooser = new JFileChooser(System.getProperty("user.dir"));
    //set up the hourglass cursor
    setGlassPane(new GlassPane());
    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    this.addWindowListener(_windowCloseListener);
    _model.addListener(new ModelListener());

    // DefinitionsPane
    _defScrollPanes = new LinkedList();
    JScrollPane defScroll = _createDefScrollPane(doc);
    _currentDefPane = (DefinitionsPane) defScroll.getViewport().getView();

    // Make the menu bar
    _setUpMenuBar();
    _setUpTabs();
    _setUpDocumentSelector();
    setBounds(0, 0, GUI_WIDTH, GUI_HEIGHT);
    setSize(GUI_WIDTH, GUI_HEIGHT);
    _setUpPanes();
    updateFileTitle();
    _setAllFonts(new Font("Monospaced", 0, 12));
    _docList.setFont(new Font("Monospaced", 0, 10));
    _findReplace = new FindReplaceDialog(this, _currentDefPane);
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
   * Updates the title bar with the name of the active document.
   */
  public void updateFileTitle() {
    String filename = _getDisplayFilename(_currentDefPane.getOpenDocument());
    setTitle(filename + " - DrJava");
    _docList.repaint();
  }

  /**
   * Prompt the user to select a place to open a file from, then load it.
   * Ask the user if they'd like to save previous changes (if the current
   * document has been modified) before opening.
   */
  public File getOpenFile() throws OperationCanceledException {
    _openChooser.setSelectedFile(null);
    int rc = _openChooser.showOpenDialog(this);
    return getFileName(_openChooser, rc);
  }

  /**
   * Prompt the user to select a place to save the current document.
   */
  public File getSaveFile() throws OperationCanceledException {
    _saveChooser.setSelectedFile(null);
    int rc = _saveChooser.showSaveDialog(this);
    return getFileName(_saveChooser, rc);
  }


  /**
   * Makes sure save and compile buttons and menu items
   * are enabled and disabled appropriately after document
   * modifications.
   */
  void installNewDocumentListener(Document d) {
    d.addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
        updateFileTitle();
      }
      public void insertUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
        updateFileTitle();
      }
      public void removeUpdate(DocumentEvent e) {
        _saveButton.setEnabled(true);
        _compileButton.setEnabled(false);
        _saveMenuItem.setEnabled(true);
        _compileMenuItem.setEnabled(false);
        updateFileTitle();
      }
    });
  }

  /**
   * Returns whether there is only one open document
   * which is untitled and unchanged.
   */
  private boolean _hasOneUnchangedDoc() {
    OpenDefinitionsDocument activeDoc = _currentDefPane.getOpenDocument();
    return ((_definitionsDocs.size() == 1) &&
            (activeDoc.isUntitled()) &&
            (!activeDoc.isModifiedSinceSave()));
  }

  private void _new() {
    _model.newFile();
  }

  private void _open() {
    // Close an untitled, unchanged document if it is the only one open
    boolean closeUntitled = _hasOneUnchangedDoc();
    OpenDefinitionsDocument oldDoc = _currentDefPane.getOpenDocument();

    try {
      _model.openFile(_openSelector);
      if (closeUntitled) {
        _model.closeFile(oldDoc);
      }
    }
    catch (AlreadyOpenException aoe) {
      // Switch to existing copy
      OpenDefinitionsDocument openDoc = aoe.getOpenDocument();
      _docList.setSelectedValue(openDoc, true);
    }
    catch (OperationCanceledException oce) {
      // Ok, don't open a file
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _close() {
    _model.closeFile(_currentDefPane.getOpenDocument());
    if (_definitionsDocs.size() == 0) {
      _new();
    }
  }

  private void _closeAll() {
    _model.closeAllFiles();
    if (_definitionsDocs.size() == 0) {
      _new();
    }
  }


  private void _save() {
    try {
      _currentDefPane.getOpenDocument().saveFile(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }


  private void _saveAs() {
    try {
      _currentDefPane.getOpenDocument().saveFileAs(_saveSelector);
    }
    catch (IOException ioe) {
      _showIOError(ioe);
    }
  }

  private void _showIOError(IOException ioe) {
    JOptionPane.showMessageDialog(this,
                                  "An I/O exception occurred during the last operation.\n" + ioe,
                                  "Input/output error",
                                  JOptionPane.ERROR_MESSAGE);
  }


  /**
   * Return the name of the file, or "(untitled)" if no file exists.
   */
  private String _getDisplayFilename(OpenDefinitionsDocument doc) {

    String filename = "(untitled)";
    try {
      File file = doc.getFile();
      filename = file.getName();
    }
    catch (IllegalStateException ise) {
      // No file, filename stays "Untitled"
    }
    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      filename = filename + " *";
    }
    return filename;
  }

  /**
   * Return the absolute path of the file, or "(untitled)" if no file exists.
   */
  private String _getDisplayFullPath(OpenDefinitionsDocument doc) {

    String path = "(untitled)";
    try {
      File file = doc.getFile();
      path = file.getAbsolutePath();
    }
    catch (IllegalStateException ise) {
      // No file, filename stays "Untitled"
    }
    // Mark if modified
    if (doc.isModifiedSinceSave()) {
      path = path + " *";
    }
    return path;
  }


// getChosenFile ?
  public File getFileName(JFileChooser fc, int choice)
    throws OperationCanceledException
  {
    switch (choice) {
      case JFileChooser.CANCEL_OPTION:case JFileChooser.ERROR_OPTION:
        throw new OperationCanceledException();
      case JFileChooser.APPROVE_OPTION:
        File chosen = fc.getSelectedFile();
        if (chosen != null)
          return chosen;
        else
          throw new RuntimeException("filechooser returned null file");
      default:                  // impossible since rc must be one of these
        throw  new RuntimeException("filechooser returned bad rc " + choice);
    }
  }

  /**
   * Ask the user what line they'd like to jump to, then go there.
   */
  private void _gotoLine() {
    final String msg = "What line would you like to go to?";
    final String title = "Jump to line";
    String lineStr = JOptionPane.showInputDialog(this,
                                                 msg,
                                                 title,
                                                 JOptionPane.QUESTION_MESSAGE);
    try {
      OpenDefinitionsDocument doc = _currentDefPane.getOpenDocument();
      int lineNum = Integer.parseInt(lineStr);
      int pos = doc.gotoLine(lineNum);
      _currentDefPane.setPositionAndScroll(pos);
      _currentDefPane.grabFocus();
    } catch (NumberFormatException nfe) {
      // invalid input for line number
      Toolkit.getDefaultToolkit().beep();
      // Do nothing.
    }
  }

  /**
   * Sets up the components of the menu bar and links them to the private
   * fields within MainFrame.  This method serves to make the code
   * more legible on the higher calling level, i.e., the constructor.
   */
  private void _setUpMenuBar() {
    _menuBar = new JMenuBar();
    _fileMenu = _setUpFileMenu();
    _editMenu = _setUpEditMenu();
    _helpMenu = _setUpHelpMenu();
    // Menu bars can actually hold anything!
    _menuBar.add(_fileMenu);
    _menuBar.add(_editMenu);
    _menuBar.add(_helpMenu);
    _setUpMenuBarButtons();
    setJMenuBar(_menuBar);
  }


  /**
   * Creates and returns a file menu.  Side effects: sets values for
   * _saveMenuItem and _compileMenuItem.
   */
  private JMenu _setUpFileMenu() {
    JMenuItem tmpItem;
    JMenu fileMenu = new JMenu("File");
    tmpItem = fileMenu.add(_newAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = fileMenu.add(_openAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = fileMenu.add(_saveAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                  ActionEvent.CTRL_MASK));

    // keep track of the save menu item
    _saveMenuItem = tmpItem;
    _saveMenuItem.setEnabled(false);

    tmpItem = fileMenu.add(_saveAsAction);

    tmpItem = fileMenu.add(_closeAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                  ActionEvent.CTRL_MASK));

    tmpItem = fileMenu.add(_closeAllAction);
    fileMenu.addSeparator();
    tmpItem = fileMenu.add(_compileAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));

    // keep track of the compile menu item
    _compileMenuItem = tmpItem;
    _compileMenuItem.setEnabled(false);

    fileMenu.addSeparator();
    tmpItem = fileMenu.add(_quitAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                  ActionEvent.CTRL_MASK));
    return fileMenu;
  }

  /**
   * Creates and returns a edit menu.
   */
  private JMenu _setUpEditMenu() {
    JMenuItem tmpItem;
    JMenu editMenu = new JMenu("Edit");
    /*The undo/redo menus and key action
     //tmpItem = editMenu.add(_currentDefPane.getUndoAction());
     //tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
     //                                             ActionEvent.CTRL_MASK));
     //tmpItem = editMenu.add(_currentDefPane.getRedoAction());
     //tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
     //                                             ActionEvent.CTRL_MASK));
     editMenu.addSeparator();
     */

    // set up the actions for cut/copy/paste with regards to menu
    // items and keystrokers.
    Action cutAction = new DefaultEditorKit.CutAction();
    cutAction.putValue(Action.NAME, "Cut");
    Action copyAction = new DefaultEditorKit.CopyAction();
    copyAction.putValue(Action.NAME, "Copy");
    Action pasteAction = new DefaultEditorKit.PasteAction();
    pasteAction.putValue(Action.NAME, "Paste");

    tmpItem = editMenu.add(cutAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(copyAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(pasteAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                  ActionEvent.CTRL_MASK));
    editMenu.addSeparator();
    tmpItem = editMenu.add(_findReplaceAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(_gotoLineAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                                  ActionEvent.CTRL_MASK));
    editMenu.add(_clearOutputAction);
    editMenu.add(_resetInteractionsAction);


    /** TEMPORARY
    editMenu.addSeparator();
    tmpItem = editMenu.add(_switchToNextAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                                  ActionEvent.CTRL_MASK));
    tmpItem = editMenu.add(_switchToPrevAction);
    tmpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,
                                                  ActionEvent.CTRL_MASK +
                                                  ActionEvent.SHIFT_MASK));
    // END TEMPORARY */

    // Add the menus to the menu bar
    return editMenu;
  }

  /**
   * Creates and returns a help menu.
   */
  private JMenu _setUpHelpMenu() {
    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(_aboutAction);
    return helpMenu;
  }

  /**
   * Sets up the save and compile buttons on the menu bar.
   */
  private void _setUpMenuBarButtons() {
    // Add buttons.
    _saveButton = new JButton(_saveAction);
    _saveButton.setEnabled(false);
    _menuBar.add(_saveButton); //, BorderLayout.??);
    _compileButton = new JButton(_compileAction);
    _menuBar.add(_compileButton); //, BorderLayout.??);
    _compileButton.setEnabled(false);
  }

  private void _setUpTabs() {
    _outputPane = new OutputPane();
    _errorPanel = new CompilerErrorPanel(_currentDefPane, _model);
    // Make the output view the active one
    _outputPane.makeActive();
    _interactionsPane = new InteractionsPane(_model);
    _tabbedPane = new JTabbedPane();
    _tabbedPane.add("Interactions", new JScrollPane(_interactionsPane));
    _tabbedPane.add("Compiler output", _errorPanel);
    _tabbedPane.add("Console", new JScrollPane(_outputPane));
    // Select interactions pane when interactions tab is selected
    _tabbedPane.addChangeListener(new ChangeListener() {

      /**
       * put your documentation comment here
       * @param e
       */
      public void stateChanged(ChangeEvent e) {
        if (_tabbedPane.getSelectedIndex() == INTERACTIONS_TAB) {
          _interactionsPane.grabFocus();
        }
      }
    });
  }

  /**
   * Configures the component used for selecting active documents.
   */
  private void _setUpDocumentSelector() {
    _docList = new JList(_definitionsDocs);// {
      //public String getToolTipText(MouseEvent event) {
      //  return "tool tip";
      //}
    //};
    _docList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _docList.setCellRenderer(new DocCellRenderer());

    _docList.setSelectedValue(_currentDefPane.getOpenDocument(), true);
    _docList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          _switchActiveDocument(
            (OpenDefinitionsDocument)_docList.getSelectedValue());
        }
      }
    });
  }  
  
  private void _setUpPanes() {
    // Document list pane
    JScrollPane listScroll =
      new JScrollPane(_docList,
                      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // DefinitionsPane
    JScrollPane defScroll = (JScrollPane)_defScrollPanes.get(_currentDocIndex);

    // Overall layout
    _docSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                   true,
                                   listScroll,
                                   defScroll);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                       true,
                                       _docSplitPane,
                                       _tabbedPane);
    getContentPane().add(split, BorderLayout.CENTER);
    // This is annoyingly order-dependent. Since split2 contains split1,
    // we need to get split2's divider set up first to give split1 an overall
    // size. Then we can set split1's divider. Ahh, Swing.
    // Also, according to the Swing docs, we need to set these dividers AFTER
    // we have shown the window. How annoying.
    split.setDividerLocation(2*getHeight()/3);
    //_docSplitPane.setDividerLocation(DOC_LIST_WIDTH);
    _setDocListDividerLocation();
  }

  /**
   * Create a new JScrollPane for a definitions pane, using the
   * current active document.
   * @param activeDoc The current active OpenDefDoc
   * @return JScrollPane containing a DefinitionsPane for the
   *         current document.
   */
  private JScrollPane _createDefScrollPane(OpenDefinitionsDocument activeDoc) {
    DefinitionsPane pane = new DefinitionsPane(this, _model, activeDoc);
    JScrollPane scroll = new JScrollPane(pane,
                                         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    _defScrollPanes.add(scroll);
    return scroll;
  }

  /**
   * Switch to the JScrollPane containing the DefinitionsPane
   * for the current active document.
   */
  private void _switchDefScrollPane() {
    //System.out.println("--switch--\n defScrollPanes: " + _defScrollPanes.size());
    //System.out.println(" _definitionsDocs: " + _definitionsDocs.size());
    JScrollPane scroll = (JScrollPane) _defScrollPanes.get(_currentDocIndex);
    if (scroll == null) {
      throw new UnexpectedException(new Exception(
        "Current definitions scroll pane not found."));
    }
    _docSplitPane.setRightComponent(scroll);
    _setDocListDividerLocation();
    _currentDefPane = (DefinitionsPane) scroll.getViewport().getView();
  }

  /**
   * Sets the location of the divider between then document
   * list and the definitions pane, based on the preferred
   * size of the document list, up to the MAX_DOC_LIST_WIDTH
   * threshold.
   */
  private void _setDocListDividerLocation() {
    Dimension dim = _docList.getPreferredSize();
    int width = (int) dim.getWidth();
    width += _docSplitPane.getDividerSize();
    if (width > MAX_DOC_LIST_WIDTH) {
      width = MAX_DOC_LIST_WIDTH;
    }
    _docSplitPane.setDividerLocation(width);
  }

  /**
   * Switches the active document to the given document.
   * This is called by a listener when a document is selected.
   * @param doc newly selected document
   */
  private void _switchActiveDocument(OpenDefinitionsDocument doc) {
    if (doc != null) {
      _currentDocIndex = _docList.getSelectedIndex();
      _switchDefScrollPane();

      OpenDefinitionsDocument activeDocument = _currentDefPane.getOpenDocument();
      boolean isModified = activeDocument.isModifiedSinceSave();
      boolean canCompile = (!isModified && !activeDocument.isUntitled());
      _saveButton.setEnabled(isModified);
      _compileButton.setEnabled(canCompile);
      _saveMenuItem.setEnabled(isModified);
      _compileMenuItem.setEnabled(canCompile);

      updateFileTitle();
      _currentDefPane.grabFocus();
      _currentDefPane.getHighlighter().removeAllHighlights();
    }
  }


  /**
   * put your documentation comment here
   * @param f
   */
  private void _setAllFonts(Font f) {
    _currentDefPane.setFont(f);
    _interactionsPane.setFont(f);
    _outputPane.setFont(f);
    _errorPanel.setListFont(f);
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

  private class ModelListener implements GlobalModelListener {
    public void newFileCreated(OpenDefinitionsDocument doc) {
      _definitionsDocs.addElement(doc);
      _createDefScrollPane(doc);
      _docList.setSelectedValue(doc, true);
      installNewDocumentListener(doc.getDocument());
    }

    public void fileSaved(OpenDefinitionsDocument doc) {
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(true);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(true);
      updateFileTitle();
      _currentDefPane.grabFocus();
    }

    public void fileOpened(OpenDefinitionsDocument doc) {
      _definitionsDocs.addElement(doc);
      _createDefScrollPane(doc);
      _docList.setSelectedValue(doc, true);
      installNewDocumentListener(doc.getDocument());
    }

    public void fileClosed(OpenDefinitionsDocument doc) {
      int index = _definitionsDocs.indexOf(doc);
      _definitionsDocs.remove(index);
      _defScrollPanes.remove(index);

      // Get next document
      int len = _definitionsDocs.size();
      if (len > 0) {
        if (_currentDocIndex >= len) {
          _currentDocIndex = len - 1;
        }
        _docList.setSelectedValue(_definitionsDocs.get(_currentDocIndex),
                                  true);
      }
    }

    public void compileStarted() {
      _tabbedPane.setSelectedIndex(COMPILE_TAB);
      _saveButton.setEnabled(false);
      _compileButton.setEnabled(false);
      _saveMenuItem.setEnabled(false);
      _compileMenuItem.setEnabled(false);
      hourglassOn();
    }

    public void compileEnded() {
      hourglassOff();
      _errorPanel.resetErrors(_model.getCompileErrors());
      _compileButton.setEnabled(true);
    }

    public void interactionsReset() {
    }

    public void consoleReset() {
    }

    public void saveBeforeProceeding(GlobalModelListener.SaveReason reason) {
      String message;
      if (reason == COMPILE_REASON) {
        message = "To compile, you must first save the current file." +
          "Would you like to save and then compile?";
      }
      else {
        throw new RuntimeException("Invalid reason for forcing a save.");
      }
      int rc = JOptionPane.showConfirmDialog(MainFrame.this, message, "Must save to continue", JOptionPane.YES_NO_OPTION);
      switch (rc) {
        case JOptionPane.YES_OPTION:
          _save();
          break;
        case JOptionPane.NO_OPTION:
          // do nothing
          break;
        default:
          throw new RuntimeException("Invalid rc from showConfirmDialog: " + rc);
      }
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

      _docList.setSelectedValue(doc, true);

      try {
        File file = doc.getFile();
        fname = file.getName();
      }
      catch (IllegalStateException ise) {
        // No file exists
        fname = "untitled file";
      }

      String text = fname + " has been modified. Would you like to " + "save?";
      int rc = JOptionPane.showConfirmDialog(MainFrame.this,
          "Would you like to save " + fname + "?",
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
      String label = _getDisplayFilename((OpenDefinitionsDocument) value);
      setText(label);

      return this;
    }
  }

}
