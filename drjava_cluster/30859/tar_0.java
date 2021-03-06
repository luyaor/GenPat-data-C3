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

package edu.rice.cs.drjava.ui.config;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Enumeration;
import java.io.IOException;
import java.util.TreeSet;
import java.util.Iterator;

import javax.swing.tree.*;
import gj.util.Hashtable;

import edu.rice.cs.drjava.DrJava;
import edu.rice.cs.drjava.config.*;
import edu.rice.cs.drjava.ui.*;
import edu.rice.cs.drjava.ui.KeyBindingManager.KeyStrokeData;

/**
 * The frame for setting Configuration options on the fly
 * @version $Id$
 */ 
public class ConfigFrame extends JFrame {
  
  private static final int FRAME_WIDTH = 700;
  private static final int FRAME_HEIGHT = 500;
  
  private JSplitPane _splitPane;
  private JTree _tree;
  private DefaultTreeModel _treeModel;
  private PanelTreeNode _rootNode;
  
  private JButton _okButton;
  private JButton _applyButton;
  private JButton _cancelButton;
  private JButton _defaultButton;
  private JPanel _mainPanel;
  /**
   * Sets up the frame and displays it.
   */
  public ConfigFrame () {
    super("Preferences");
    
    
    _createTree();
    
    _createPanels();
    
    
    _mainPanel= new JPanel();
    _mainPanel.setLayout(new BorderLayout());
    _tree.addTreeSelectionListener( new PanelTreeSelectionListener());
        
    Container cp = getContentPane();
    cp.setLayout(new BorderLayout());
    //cp.add(_mainPanel, BorderLayout.CENTER);
    
    if (_rootNode.getChildCount() != 0) {
      PanelTreeNode firstChild = (PanelTreeNode)_rootNode.getChildAt(0);
      TreePath path = new TreePath(firstChild.getPath());
      _tree.expandPath(path);
      _tree.setSelectionPath(path);
      //_mainPanel.add( firstChild.getPanel(), BorderLayout.CENTER);
    }
    
       
    JScrollPane treeScroll = new JScrollPane(_tree);
    //cp.add(treeScroll, BorderLayout.WEST);
    
    _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                treeScroll,
                                _mainPanel);
    cp.add(_splitPane, BorderLayout.CENTER);
    
    
    _okButton = new JButton("OK");
    _okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean successful = apply();
        if (successful) ConfigFrame.this.hide();
      }
    });
    
    _applyButton = new JButton("Apply");
    _applyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        apply();
      }
    });
    
    _cancelButton = new JButton("Cancel");
    _cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        reset();
        ConfigFrame.this.hide();
      }
    });
    
    _defaultButton = new JButton("Set as default");
    _defaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAsDefault();
      }
    });

    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_defaultButton);
    bottom.add(Box.createHorizontalGlue());
    bottom.add(_applyButton);
    bottom.add(_okButton);
    bottom.add(_cancelButton);
    bottom.add(Box.createHorizontalGlue());

    cp.add(bottom, BorderLayout.SOUTH);

    
    
    // Set all dimensions ----
    setSize( FRAME_WIDTH, FRAME_HEIGHT);
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
    int width = getWidth() / 4;
    System.out.println("width: " + getWidth());
    System.out.println("width for divider: " + width);
    _splitPane.setDividerLocation(width);
    _mainPanel.setPreferredSize(new Dimension(getWidth() - width, _splitPane.getHeight()));
  }

  /**
   * Call the update method to propagate down the tree, parsing input values into their config options
   */
  public boolean apply() {   
    // returns false if the update did not succeed
    return _rootNode.update();
  }
  
  /**
   * Resets the field of each option in the Preferences window to its actual stored value.
   */
  public void reset() {
    _rootNode.reset();
  }
  
  public void setAsDefault() {
    boolean successful = apply();
    if (successful) {
      try {
        DrJava.CONFIG.saveConfiguration();
      }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this,
                                      "Could not save changes to your '.drjava' file \n" +
                                      "in your home directory.\n\n" + ioe,
                                      "Could Not Save Changes",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
    }
  }
   
  private void _displayPanel(ConfigPanel cf) {

    _mainPanel.removeAll();
    _mainPanel.add(cf, BorderLayout.CENTER);
    _mainPanel.revalidate();
    _mainPanel.repaint();
    
  }
    
  private void _createTree() {
   
    _rootNode = new PanelTreeNode("Preferences");
    _treeModel = new DefaultTreeModel(_rootNode);
    _tree = new JTree(_treeModel);
    _tree.setEditable(false);
    _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    _tree.setShowsRootHandles(true);
    _tree.setRootVisible(false);
    
  }
  
  /**
   * Creates all of the panels contained within the frame.
   */
  private void _createPanels() {
    /*PanelTreeNode fontNode = _createPanel("Fonts");
    _setupFontPanel(fontNode.getPanel());
    */
    
    PanelTreeNode colorNode = _createPanel("Colors");
    _setupColorPanel(colorNode.getPanel());

    PanelTreeNode keystrokesNode = _createPanel(new KeyStrokeConfigPanel("Key Bindings"),
                                                _rootNode);

    _setupKeyBindingsPanel(keystrokesNode.getPanel());    

    PanelTreeNode miscNode = _createPanel("Miscellaneous");
    _setupMiscPanel(miscNode.getPanel());
    
  }
  
  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node.
   * @param t the title of this panel
   * @param parent the parent tree node
   * @return this tree node
   */
  private PanelTreeNode _createPanel(String t, PanelTreeNode parent) {
    
    //ConfigPanel newPanel = new ConfigPanel(t);
    //_panels.addElement( newPanel );
    PanelTreeNode ptNode = new PanelTreeNode(t);
    parent.add(ptNode);
    
    return ptNode;
  }
  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node.
   * @param t the title of this panel
   * @param parent the parent tree node
   * @return this tree node
   */
  private PanelTreeNode _createPanel(ConfigPanel c, PanelTreeNode parent) {
    PanelTreeNode ptNode = new PanelTreeNode(c);
    parent.add(ptNode);
    
    return ptNode;
  }
  /**
   * Creates an individual panel, adds it to the JTree and the list of panels, and
   *  returns the tree node. Adds to the root node.
   * @param t the title of this panel
   * @return this tree node
   */
  private PanelTreeNode _createPanel(String t) {
    return _createPanel(t, _rootNode);
  }
  
  /**
   * Add all of the components for the Font panel of the preferences window.
   */ 
  private void _setupFontPanel ( ConfigPanel panel) {
    //panel.addComponent( new FontOptionComponent (OptionConstants.
    //TO DO: write FontOptionComponent .. probably ought to be composed of three different entry fields,
    //  along the lines of word processors
    panel.displayComponents(); 
  }
  
  /**
   * Adds all of the components for the Color panel of the preferences window.
   */
  private void _setupColorPanel( ConfigPanel panel) {
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_NORMAL_COLOR, "Normal Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_KEYWORD_COLOR, "Keyword Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_TYPE_COLOR, "Type Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_COMMENT_COLOR, "Comment Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_DOUBLE_QUOTED_COLOR, "Double-quoted Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_SINGLE_QUOTED_COLOR, "Single-quoted Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_NUMBER_COLOR, "Number Color", this));
    panel.addComponent( new ColorOptionComponent (OptionConstants.DEFINITIONS_MATCH_COLOR, "Brace-matching Color", this));
    panel.displayComponents();
  }
 
  /**
   * Adds all of the components for the Key Bindings panel of the preferences window.
   */
  private void _setupKeyBindingsPanel( ConfigPanel panel) {     
    TreeSet _comps = new TreeSet();

    
    KeyStrokeData tmpKsd;
    KeyStrokeOptionComponent tmpKsoc;
    
    Enumeration e = KeyBindingManager.Singleton.getKeyStrokeData();
    while (e.hasMoreElements()) {
      tmpKsd = (KeyStrokeData) e.nextElement();
      if (tmpKsd.getOption() != null) {
        tmpKsoc = new KeyStrokeOptionComponent((KeyStrokeOption)tmpKsd.getOption(),
                                               tmpKsd.getName(), this);
        if (tmpKsoc != null) { 
          _comps.add(tmpKsoc);
        }
      }
    }
    ((KeyStrokeConfigPanel)panel).setKeyStrokeComponents(_comps);

    
    Iterator iter = _comps.iterator();
    while (iter.hasNext()) {
      KeyStrokeOptionComponent x = (KeyStrokeOptionComponent) iter.next();
      panel.addComponent(x);
    }

  /*
  public static final KeyStrokeOption KEY_NEW_FILE =
  public static final KeyStrokeOption KEY_OPEN_FILE =
  public static final KeyStrokeOption KEY_SAVE_FILE =
  public static final KeyStrokeOption KEY_SAVE_FILE_AS =
  public static final KeyStrokeOption KEY_CLOSE_FILE =
  public static final KeyStrokeOption KEY_PRINT_PREVIEW =
  public static final KeyStrokeOption KEY_PRINT =
  public static final KeyStrokeOption KEY_QUIT =
  public static final KeyStrokeOption KEY_UNDO =
  public static final KeyStrokeOption KEY_REDO =
  public static final KeyStrokeOption KEY_CUT =
  public static final KeyStrokeOption KEY_COPY =
  public static final KeyStrokeOption KEY_PASTE =
  public static final KeyStrokeOption KEY_SELECT_ALL =
  public static final KeyStrokeOption KEY_FIND_NEXT =
  public static final KeyStrokeOption KEY_FIND_REPLACE =
  public static final KeyStrokeOption KEY_GOTO_LINE =
  public static final KeyStrokeOption KEY_PREVIOUS_DOCUMENT =
  public static final KeyStrokeOption KEY_NEXT_DOCUMENT =
  public static final KeyStrokeOption KEY_COMPILE =
  public static final KeyStrokeOption KEY_ABORT_INTERACTION =
  public static final KeyStrokeOption KEY_BACKWARD =
  public static final KeyStrokeOption KEY_BEGIN_DOCUMENT =
  public static final KeyStrokeOption KEY_BEGIN_LINE =
  public static final KeyStrokeOption KEY_BEGIN_PARAGRAPH =
  public static final KeyStrokeOption KEY_PREVIOUS_WORD =
  public static final KeyStrokeOption KEY_DELETE_NEXT =
  public static final KeyStrokeOption KEY_DELETE_PREVIOUS =
  public static final KeyStrokeOption KEY_DOWN =
  public static final KeyStrokeOption KEY_UP =
  public static final KeyStrokeOption KEY_END_DOCUMENT =
  public static final KeyStrokeOption KEY_END_LINE =
  public static final KeyStrokeOption KEY_END_PARAGRAPH =
  public static final KeyStrokeOption KEY_NEXT_WORD =
  public static final KeyStrokeOption KEY_FORWARD =
  public static final KeyStrokeOption KEY_PAGE_DOWN =
  public static final KeyStrokeOption KEY_PAGE_UP =
  public static final KeyStrokeOption KEY_CUT_LINE =
  */
    panel.displayComponents();
  }
  
  /**
   *  Adds all of the components for the Miscellaneous panel of the preferences window.
   */
  private void _setupMiscPanel( ConfigPanel panel) {
    panel.addComponent( new StringOptionComponent ( OptionConstants.WORKING_DIRECTORY, "Working directory", this));
    panel.addComponent( new IntegerOptionComponent ( OptionConstants.INDENT_LEVEL, "Indent level", this));
    panel.addComponent( new IntegerOptionComponent ( OptionConstants.HISTORY_MAX_SIZE, "Size of Interactions command history", this));
    panel.addComponent( new BooleanOptionComponent ( OptionConstants.LINEENUM_ENABLED, "Line number enumeration", this));
    panel.displayComponents();
  }
  
  private class PanelTreeNode extends DefaultMutableTreeNode {
    
    private ConfigPanel _panel;
    
    public PanelTreeNode(String t) {
      super(t);
      _panel = new ConfigPanel(t);   
    }
      
    public PanelTreeNode(ConfigPanel c) {
      super(c.getTitle());
      _panel = c;
    }
    public ConfigPanel getPanel() {
      return _panel;
    }
    
    /**
     * Tells its panel to update, and tells all of its child nodes to update their panels.
     * @return whether the update succeeded.
     */ 
    public boolean update() {
      boolean isValidUpdate = _panel.update();
      //if this panel encountered an error while attempting to update, return false
      if (!isValidUpdate) {
        TreePath path = new TreePath(this.getPath());
        _tree.expandPath(path);
        _tree.setSelectionPath(path);
        return false;
      }
      
      Enumeration childNodes = this.children();
      while (childNodes.hasMoreElements()) {
        boolean isValidUpdateChildren = ((PanelTreeNode)childNodes.nextElement()).update();
        //if any of the children nodes encountered an error, return false
        if (!isValidUpdateChildren) return false;
      }
      
      return true;
    }
    
    /**
     * Tells its panel to reset, and tells all of its children to reset their panels.
     */
    public void reset() {
      _panel.reset();
      
      Enumeration childNodes = this.children();
      while (childNodes.hasMoreElements()) {
        ((PanelTreeNode)childNodes.nextElement()).reset();
      } 
    }
  }
  
  private class PanelTreeSelectionListener implements TreeSelectionListener {
    
    public void valueChanged(TreeSelectionEvent e) {
      Object o = _tree.getLastSelectedPathComponent();
      //System.out.println("Object o : "+o);
      if (o instanceof PanelTreeNode) {
        //System.out.println("o is instanceof PanelTreeNode");
        PanelTreeNode child = (PanelTreeNode) _tree.getLastSelectedPathComponent();
        _displayPanel(child.getPanel());
      }
    }
    
  }
    
}

