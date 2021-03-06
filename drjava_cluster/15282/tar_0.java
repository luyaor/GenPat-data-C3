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




package edu.rice.cs.util.docnavigation;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.tree.*;
import java.io.File;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.IOException;
import edu.rice.cs.util.*;

public class JTreeSortNavigator extends JTree 
  implements IDocumentNavigator, TreeSelectionListener, TreeExpansionListener {
  
  /**
   * maps documents to tree nodes
   */
  private HashMap<INavigatorItem, LeafNode> _doc2node =
    new HashMap<INavigatorItem, LeafNode>();
  
  /**
   * maps path's to nodes and nodes to paths
   */
  private BidirectionalHashMap<String, InnerNode> _path2node = 
    new BidirectionalHashMap<String, InnerNode>();
  
  /**
   * the model of the tree
   */
  private DefaultTreeModel _model;
  
  /**
   * the currently selected item
   */
  private NodeData _currSelected;
  
  /**
   * the node corresponding to the [external files] node in the tree
   * this will hold files that are not in the project directory
   */
  private StringNode _nonProjRoot = new StringNode("[External Files]");
  
  /**
   * flag if the nonproject node has children
   */
  private boolean _hasnonprojfilesopen = false;
  
  /** the collection of INavigationListeners listening to this JListNavigator */
  private Vector<INavigationListener> navListeners = new Vector<INavigationListener>();
  
  /**
   * the renderer for this JTree
   */
  protected CustomTreeCellRenderer _renderer;

  private java.util.List<GroupNode> _roots = new LinkedList<GroupNode>();
  
  /**
   * sets the foreground color of this JTree
   * @param c the color to set to
   */
  public void setForeground(Color c){
    super.setForeground(c);
    _renderer.setTextNonSelectionColor(c);
  }
  
  /**
   * sets the background color of this tree
   * @param c the color to set to
   */
  public void setBackground(Color c){
    super.setBackground(c);
    if(_renderer != null)
      _renderer.setBackgroundNonSelectionColor(c);
  }
  
  /**
   * standard constructor
   * @param name the name of the root node
   */
  public JTreeSortNavigator(String projfilepath) {
    super(new DefaultTreeModel(new RootNode(projfilepath.substring(projfilepath.lastIndexOf(File.separator)+1))));
    
//
    this.addTreeSelectionListener(this);
    this.addTreeExpansionListener(this);
    
    _model = (DefaultTreeModel) this.getModel();
//    _root = null;
//    _model.insertNodeInto(_root, (MutableTreeNode)_model.getRoot(),0);
//    _root = (FileNode) _model.getRoot();
    _renderer = new CustomTreeCellRenderer();
    _renderer.setOpaque(false);
//    _renderer.setJavaIcon(getIcon("javaicon.gif"));
    this.setCellRenderer(_renderer);

    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    
    //this.setShowsRootHandles(true);
  }
  
  /**
   * @return an AWT component which interacts with this document navigator
   */
  public Container asContainer() {
    return this;
  }
  
  /**
   * Adds an <code>IDocuemnt</code> to this navigator.
   *
   * @param doc the document to be added into this navigator.
   */
  public void addDocument(INavigatorItem doc) {
    GroupNode _root = null;
    for(GroupNode r: _roots){
      if(r.getFilter().accept(doc)) {
        _root = r;
        break;
      }
    }
    if(_root == null) {
      return;
    }

    
    LeafNode node = new LeafNode(doc);
    //_root.add(node);
    insertNodeSortedInto(node, _root);
    this.expandPath(new TreePath(_root.getPath()));
    _doc2node.put(doc, node);
    _hasnonprojfilesopen = true;
//    this.setActiveDoc(doc);
  }
  
  /**
   * Adds an <code>INavigatorItem</code> into this navigator in a position
   * relavite to a path. The actual behavior of
   * the navigator and implication of adding a document "relative to" the
   * second paramater is left up to the implementing class.
   *
   * @param doc        the document to be added into this navigator.
   * @param relativeto an existing document in the navigator.
   * @throws IllegalArgumentException if this navigator does not contain <code>relativeto</code> as tested by the
   *                                  <code>contains</code> method.
   */
  public void addDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    GroupNode _root = null;

    for(GroupNode r: _roots){
      if(r.getFilter().accept(doc)) {
        _root = r;
        break;
      }
    }
    
    if(_root == null) {
      return;
    }

    
    StringTokenizer tok = new StringTokenizer(path, File.separator);
    //ArrayList<String> elements = new ArrayList<String>();
    String pathSoFar="";
    InnerNode lastNode = _root;
    while(tok.hasMoreTokens()) {
      String element = tok.nextToken();
      pathSoFar += (element + "/");
      InnerNode thisNode;
      //System.out.println("pathsofar = " + pathSoFar);
      // if the node is not in the hashmap yet
      if (!_path2node.containsKey(pathSoFar)) {
        // make a new node
        
        /* this inserts a folder node */
        thisNode = new FileNode(new File(pathSoFar));
        insertFolderSortedInto(thisNode, lastNode);
          this.expandPath(new TreePath(lastNode.getPath()));
        // associate the path so far with that node
        _path2node.put(pathSoFar, thisNode);
      }
      else {
       // System.out.println("path2node contains pathSoFar");
        thisNode = _path2node.getValue(pathSoFar);
      }
      
      lastNode = thisNode;
      
      
      //elements.add(element);
    }
    
    /* lastNode is the node of the folder to add into */
    
    LeafNode child = new LeafNode(doc);
    _doc2node.put(doc, child);
    insertNodeSortedInto(child, lastNode);
//    _model.insertNodeInto(child, lastNode, lastNode.getChildCount());
    this.expandPath(new TreePath(lastNode.getPath()));
    child.setUserObject(doc);
  }
  
  
  private void addTopLevelGroupToRoot(InnerNode parent){
    int i;
    int indexInRoots = _roots.indexOf(parent);
    int num = _model.getChildCount(_model.getRoot());
    for(i=0;i<num;i++){
      TreeNode n = (TreeNode)_model.getChild(_model.getRoot(), i);
      if(_roots.indexOf(n) > indexInRoots){
        break;
      }
    }
    _model.insertNodeInto(parent, (MutableTreeNode)_model.getRoot(), i);
  }
  
  /**
   * inserts the child node (INavigatorItem) into the sorted position as a parent node's child
   * @param child the node to add
   * @param parent the node to add under
   */
  private void insertNodeSortedInto(LeafNode child, InnerNode parent){
    int numChildren = parent.getChildCount();
    int i=0;
    String newName = child.toString();
    String oldName = parent.getUserObject().toString();
    DefaultMutableTreeNode parentsKid;

    /**
     * check to make sure that the parent, if a top level group, is
     * added to the tree model group
     */
    if(((DefaultMutableTreeNode)_model.getRoot()).getIndex(parent) == -1 && 
       _roots.contains(parent)){
      addTopLevelGroupToRoot(parent);
    }

    
    
    while(i<numChildren){
      parentsKid = ((DefaultMutableTreeNode)parent.getChildAt(i));
      if(parentsKid instanceof InnerNode){
        // do nothing, it's a folder
      }else if(parentsKid instanceof LeafNode){
        oldName = ((LeafNode)parentsKid).getData().getName();
        if((newName.toUpperCase().compareTo(oldName.toUpperCase()) < 0)){
          break;
        }
      }else{
        throw new IllegalStateException("found a node in navigator that is not an InnerNode or LeafNode");
      }
      i++;
    }

    _model.insertNodeInto(child, parent, i);
  }
  
  /**
   * inserts a folder (String) into sorted position under the parent
   * @param child the folder to add
   * @param parent the folder to add under
   */
  private void insertFolderSortedInto(InnerNode child, InnerNode parent){
    int numChildren = parent.getChildCount();
    int i=0;
    String newName = child.toString();
    String oldName = parent.getUserObject().toString();
    DefaultMutableTreeNode parentsKid;

    if(((DefaultMutableTreeNode)_model.getRoot()).getIndex(parent) == -1 && 
       _roots.contains(parent)){
      addTopLevelGroupToRoot(parent);
    }

    int countFolders = 0;
    while(i<numChildren){
      parentsKid = ((DefaultMutableTreeNode)parent.getChildAt(i));
      if(parentsKid instanceof InnerNode){
        countFolders++;
        oldName = ((InnerNode)parentsKid).toString();
        if((newName.toUpperCase().compareTo(oldName.toUpperCase()) < 0)){
          break;
        }
      }else if(parentsKid instanceof LeafNode){
        // we're out of folders, and starting into the files, so just break out.
        break;
      }else{
        throw new IllegalStateException("found a node in navigator that is not an InnerNode or LeafNode");
      }
      i++;
    }
    _model.insertNodeInto(child, parent, i);
  }
  
  
  
  
  /**
   * Removes a given <code>INavigatorItem<code> from this navigator. Removes
   * all <code>INavigatorItem</code>s from this navigator that are "equal" (as
   * tested by the <code>equals</code> method) to the passed argument. Any of
   * the removed documents may be returned by this method.
   *
   * @param doc the docment to be removed
   * @return doc a document removed from this navigator as a result of invoking this method.
   * @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public INavigatorItem removeDocument(INavigatorItem doc) throws IllegalArgumentException {
    return removeNode(getNodeForDoc(doc));
  }
  
  
  private LeafNode getNodeForDoc(INavigatorItem doc){
    return _doc2node.get(doc);
  }
  
  /**
   * only takes in nodes that have a inavigatoritem as their object
   */
  private INavigatorItem removeNode(LeafNode toRemove){
    _model.removeNodeFromParent(toRemove);
    _doc2node.remove(toRemove.getData());
   
    Enumeration enumeration = ((DefaultMutableTreeNode)_model.getRoot()).depthFirstEnumeration();
    while(enumeration.hasMoreElements())
    {
      TreeNode next = (TreeNode)enumeration.nextElement();
      if(next.getChildCount() == 0 && 
         !_doc2node.containsValue(next) && 
         next != _model.getRoot())
      {
        _model.removeNodeFromParent((MutableTreeNode)next); 
        _path2node.removeKey((InnerNode)next);
      }
    }
   
  
    if(_nonProjRoot.getChildCount() == 0)
    {
      _hasnonprojfilesopen = false;
    }
    return (INavigatorItem)toRemove.getUserObject();
  }
    

  /**
   * Resets a given <code>INavigatorItem<code> in the tree.  This may affect the
   * placement of the item or its display to reflect any changes made in the model.
   * @param doc the docment to be refreshed
   * @throws IllegalArgumentException if this navigator contains no document
   *  that is equal to the passed document.
   */
  public void refreshDocument(INavigatorItem doc, String path) throws IllegalArgumentException {
    /**
     * since we are modifying the model of this tree, and in the middle of this modification,
     * we have an unstable model, we need to synchronize around it.
     * Note: this solved the bug where compile all with modified documents would throw an array
     * index out of bounds exception when painting.
     */
    synchronized(this){
      LeafNode node = getNodeForDoc(doc);
      removeNode(node);
      addDocument(doc, path);
    }
  }
  
  public void paint(Graphics g){
    /**
     * we don't want to paint if we're in the middle of some other
     * synchronized code, so we'll wait until we can aquire the lock
     * around ourself
     */
    synchronized(this){
      super.paint(g);
    }
  }
  
  /** sets the input document to be active */
  public void setActiveDoc(INavigatorItem doc){
    DefaultMutableTreeNode node = _doc2node.get(doc);
    if(this.contains(doc)){
      TreeNode[] nodes = node.getPath();
      TreePath path = new TreePath(nodes);
      this.expandPath(path);
      
      this.setSelectionPath(path);
      this.scrollPathToVisible(path);
    }
  }

  /**
   * Impose some ordering on the documents in the navigator, to facilitate
   * MainFrame's setActiveNextDocument()
   *
   * @param doc the INavigatorItem of interest
   * @return the INavigatorItem which comes after doc
   */
  public INavigatorItem getNext(INavigatorItem doc) {
    DefaultMutableTreeNode node = _doc2node.get(doc);
    // TODO: check for "package" case
    DefaultMutableTreeNode next = node.getNextLeaf();
    if (next == null || next == _model.getRoot()) return doc;
    return  (INavigatorItem) next.getUserObject();
  }
  
  /**
   * Impose some ordering on the documents in the navigator, to facilitate MainFrame's setActivePrevDocument()
   *
   * @param doc the INavigatorItem of interest
   * @return the INavigatorItem which comes before doc
   */
  public INavigatorItem getPrevious(INavigatorItem doc) {
    DefaultMutableTreeNode node = _doc2node.get(doc);
    // TODO: check for "package" case
    DefaultMutableTreeNode prev = node.getPreviousLeaf();
    if (prev == null || prev == _model.getRoot()) return doc;
    return  (INavigatorItem) prev.getUserObject();
  }
  
  /**
   * Tests to see if a given document is contained in this navigator.
   *
   * @param doc the document to test for containment.
   * @return <code>true</code> if this navigator contains a document that is "equal" (as tested by the
   *         <code>equals</code< method) to the passed document, else <code>false</code>.
   */
  public boolean contains(INavigatorItem doc) {
    return _doc2node.containsKey(doc);
  }
  
  /**
   * Returns all the <code>IDocuments</code> contained in this
   * navigator</code>. Does not assert any type of ordering on the returned
   * structure.
   *
   * @return an <code>INavigatorItem<code> enumeration of this navigator's contents.
   */
  public Enumeration<INavigatorItem> getDocuments() {
    
    final ArrayList<INavigatorItem> list = new ArrayList<INavigatorItem>();
    Enumeration e_tmp = ((DefaultMutableTreeNode)_model.getRoot()).depthFirstEnumeration();
    while(e_tmp.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)e_tmp.nextElement();
      if (node.isLeaf() && node != _model.getRoot()) 
        list.add((INavigatorItem)node.getUserObject());
    }
    
    return new Enumeration<INavigatorItem>() {
      
      private Iterator<INavigatorItem> it = list.iterator();
      
      public boolean hasMoreElements() {
        return it.hasNext();
      }
      
      public INavigatorItem nextElement() {
        return it.next();
      }
    };
  }
  
  /**
   * Returns the number of <code>IDocuments</code> contained by this
   * <code>IDocumentNavigator</code>
   *
   * @return the number of documents within this navigator.
   */
  public int getDocumentCount() {
    return _doc2node.size();
  }
  
  /**
   * Returns whether this <code>IDocumentNavigator</code> contains any <code>IDocuments</code>.
   *
   * @return <code>true</code> if this navigator contains one or more documents, else <code>false</code>.
   */
  public boolean isEmpty() {
    return _doc2node.isEmpty();
  }
  
  /**
   * Removes all <code>IDocuments</code> from this <code>IDocumentNavigator</code>.
   */
  public void clear() {
    _doc2node.clear();
    ((DefaultMutableTreeNode)_model.getRoot()).removeAllChildren();
  }
  
  /**
   * Adds an <code>INavigationListener</code> to this navigator. After
   * invoking this method, the passed listener will be eligable for observing
   * this navigator. If the provided listener is already observing this
   * navigator (as tested by the == operator), no action is taken.
   *
   * @param listener the listener to be added to this navigator.
   */
  public void addNavigationListener(INavigationListener listener) {
    navListeners.add(listener);
  }
  
  /**
   * Removes the given listener from observing this navigator. After invoking
   * this method, all observers observing this navigator "equal" (as tested by
   * the == operator) will no longer receive observable dispatches.
   *
   * @param listener the listener to be removed from this navigator
   */
  public void removeNavigationListener(INavigationListener listener) {
    navListeners.remove(listener);
  }
  
  /**
   * retuns a collection of all navigator listeners
   */
  public Collection<INavigationListener> getNavigatorListeners()
  {
    return navListeners;
  }
  
  /**
   * standard visitor pattern
   * @param algo the visitor to run
   * @param input the input for the visitor
   */
  public <InType, ReturnType> ReturnType execute(IDocumentNavigatorAlgo<InType, ReturnType> algo, InType input) {
    return algo.forTree(this, input);
  }
  
  /**
   * Called whenever the value of the selection changes.
   *
   * @param e the event that characterizes the change.
   */
  public void valueChanged(TreeSelectionEvent e) {
    Object treeNode = this.getLastSelectedPathComponent();
    if(treeNode == null || !(treeNode instanceof NodeData)) {
      return;
    }
    NodeData newSelection = (NodeData)treeNode;
    if(_currSelected != newSelection) {
      for(INavigationListener listener : navListeners) {
        listener.lostSelection(_currSelected);
        listener.gainedSelection(newSelection);
      }
      _currSelected = newSelection;
    }
  }
  
  
  /**
   * returns a renderer for this object
   */
  public Component getRenderer(){
    return _renderer;
  }
  
  /**
   * the cell renderer for this tree
   */
  private class CustomTreeCellRenderer extends DefaultTreeCellRenderer{
    private String    _filename;
    private ImageIcon _javaIcon;
    private ImageIcon _advancedMIcon;
    private ImageIcon _advancedIcon;
    private ImageIcon _intermediateMIcon;
    private ImageIcon _intermediateIcon;
    private ImageIcon _elementaryMIcon;
    private ImageIcon _elementaryIcon;
    private ImageIcon _otherIcon;
    private ImageIcon _javaMIcon;
    private ImageIcon _otherMIcon;
    private ImageIcon _projectIcon;
    
    /**
     * simple constructor
     */
    public CustomTreeCellRenderer(){
      _javaIcon   = _getIconResource("JavaIcon.gif");
      _javaMIcon  = _getIconResource("JavaMIcon.gif");
      _elementaryIcon   = _getIconResource("ElementaryIcon.gif");
      _elementaryMIcon  = _getIconResource("ElementaryMIcon.gif");
      _intermediateIcon   = _getIconResource("IntermediateIcon.gif");
      _intermediateMIcon  = _getIconResource("IntermediateMIcon.gif");
      _advancedIcon   = _getIconResource("AdvancedIcon.gif");
      _advancedMIcon  = _getIconResource("AdvancedMIcon.gif");
      _otherIcon  = _getIconResource("OtherIcon.gif");
      _otherMIcon = _getIconResource("OtherMIcon.gif");
      _projectIcon = _getIconResource("ProjectIcon.gif");
    }
    
    private ImageIcon _getIconResource(String name) {
      URL url = JTreeSortNavigator.class.getResource("icons/" + name);
      if (url != null) {
        return new ImageIcon(url);
      }
      return null;
    }
    
    /**
     * returns the component for a cell
     * @param tree
     */
    public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            if (node instanceof RootNode) {
              setIcon(_projectIcon);
            }else
            if(node.getUserObject() instanceof INavigatorItem){
              INavigatorItem doc = (INavigatorItem)(node.getUserObject());
              _filename = doc.toString();
              if (leaf) {
                if (_javaIcon != null && _filename.endsWith(".java  ")) {
                  setIcon(_javaIcon);
                }
                else if (_javaMIcon != null && _filename.endsWith(".java *")) {
                  setIcon(_javaMIcon);
                }
                else if (_elementaryIcon != null && _filename.endsWith(".dj0  ")) {
                  setIcon(_elementaryIcon);
                }
                else if (_elementaryMIcon != null && _filename.endsWith(".dj0 *")) {
                  setIcon(_elementaryMIcon);
                }
                else if (_intermediateIcon != null && _filename.endsWith(".dj1  ")) {
                  setIcon(_intermediateIcon);
                }
                else if (_intermediateMIcon != null && _filename.endsWith(".dj1 *")) {
                  setIcon(_intermediateMIcon);
                }
                else if (_intermediateIcon != null && _filename.endsWith(".dj2  ")) {
                  setIcon(_advancedIcon);
                }
                else if (_advancedMIcon != null && _filename.endsWith(".dj2 *")) {
                  setIcon(_advancedMIcon);
                }
                else if (_otherMIcon != null && _filename.endsWith(" *")) {
                  setIcon(_otherMIcon);
                }
                else if (_otherIcon != null) {
                  setIcon(_otherIcon);
                }
              }
            }else if(value instanceof String){
              // a directory
            }
            
            return this;
    }
  }

  
  /**
   * Selects the document at the x,y coordinate of the navigator pane and sets it to be
   * the currently active document.
   * @param x the x coordinate of the navigator pane
   * @param y the y coordinate of the navigator pane
   */
  public boolean selectDocumentAt(int x, int y) {
    TreePath path = getPathForLocation(x, y);
    if(path == null){
      return false;
    }else{
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
      if(node instanceof LeafNode){
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      }else if(node instanceof InnerNode){
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      }else if(node instanceof RootNode){
        this.expandPath(path);
        this.setSelectionPath(path);
        this.scrollPathToVisible(path);
        return true;
      }else{
        return false;
      }
    }
  }

  /**
   * @return true if a group if INavigatorItems selected
   */
  public boolean isGroupSelected(){
    TreePath p = getSelectionPath();
    TreeNode n = (TreeNode) p.getLastPathComponent();
    if(n instanceof InnerNode){
      return true;
    }else{
      return false;
    }
  }
  
  
  /**
   * returns true if a top level group is selected
   */
  public boolean isTopLevelGroupSelected(){
    TreePath p = getSelectionPath();
    TreeNode n = (TreeNode) p.getLastPathComponent();
    if(n instanceof GroupNode){
      return true;
    }else{
      return false;
    }
  }
  
  /**
   * returns the name of the top level group that the selected item is a
   * child or grandchild or grand...child of
   */
  public String getNameOfSelectedTopLevelGroup() throws GroupNotSelectedException{
    TreePath p = getSelectionPath();
    TreeNode n = (TreeNode) p.getLastPathComponent();
    
    if(n == _model.getRoot()){
      throw new GroupNotSelectedException("there is no top level group for the root of the tree");
    }
    
    while(!_roots.contains(n)){
      n = n.getParent();
    }
    
    return ((GroupNode)n).getData();
  }
  
  /**
   * returns the currently selected leaf node, or null
   * if the selected node is not a leaf
   */
  public INavigatorItem getCurrentSelectedLeaf(){
    return _currSelected.execute(_leafVisitor);
  }
  
  private NodeDataVisitor<INavigatorItem> _leafVisitor = new NodeDataVisitor<INavigatorItem>() {
    public INavigatorItem fileCase(File f){ return null; }
    public INavigatorItem stringCase(String s){ return null; }
    public INavigatorItem itemCase(INavigatorItem ini){ return ini; }
  };
  
  /**
   * @return true if the INavigatorItem is in the selected group, if a group is selected
   */
  public boolean isSelectedInGroup(INavigatorItem i){
    TreePath p = getSelectionPath();
    TreeNode n = (TreeNode) p.getLastPathComponent();
    TreeNode l = _doc2node.get(i);
    
    if(n == _model.getRoot()){
      return true;
    }
    
    while(l.getParent() != _model.getRoot()){
      if(l.getParent() == n){
        return true;
      }
      l = l.getParent();
    }
    
    return false;
  }
  
  /**
   * adds a top level group to the navigator
   */
  public void addTopLevelGroup(String name, INavigatorItemFilter f){
    if(f == null){
      throw new IllegalArgumentException("parameter 'f' is not allowed to be null");
    }
    GroupNode n = new GroupNode(name, f);
    _roots.add(n);
  }
  
  ///////// Methods that handle expansion/collapsing of folders in tree ///////////
  
  /**
   * Called whenever an item in the tree has been collapsed.
   */
  public void treeCollapsed(TreeExpansionEvent event) {
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof InnerNode) {
      ((InnerNode)o).setCollapsed(true);
    }
  }
  /**
   * Called whenever an item in the tree has been expanded.
   */
  public void treeExpanded(TreeExpansionEvent event) {
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof InnerNode) {
      ((InnerNode)o).setCollapsed(false);
    }
  }
     
  /**
   * Collapses all the paths in the tree that match one of the path strings included
   * in the given hash set.  Path strings must follow a specific format in order for
   * them to work. See the documentation of <code>generatePathString</code> for 
   * information on the format of the path strings.
   * @param paths A hash set of path strings.  
   */
  public void collapsePaths(String[] paths) {
    HashSet<String> set = new HashSet<String>();
    for (String s : paths) {
      set.add(s);
    }
    collapsePaths(set);
  }
  void collapsePaths(HashSet<String> paths) {
    Enumeration<TreeNode> nodes = ((DefaultMutableTreeNode)_model.getRoot()).depthFirstEnumeration();
    ArrayList<String> list = new ArrayList<String>();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)nodes.nextElement();
      if (tn instanceof InnerNode) {
        TreePath tp = new TreePath(tn.getPath());
        String s = generatePathString(tp);
        boolean shouldCollapse = paths.contains(s);
        if (shouldCollapse) { 
          collapsePath(tp);
        }
      }
    }
  }
  
  /**
   * @return an array of path strings corresponding to the paths of the tree nodes that
   * are currently collapsed. See the documentation of <code>generatePathString</code>
   * for information on the format of the path strings.
   */
  public String[] getCollapsedPaths() {
    Enumeration<TreeNode> nodes = ((DefaultMutableTreeNode)_model.getRoot()).depthFirstEnumeration();
    ArrayList<String> list = new ArrayList<String>();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)nodes.nextElement();
      if (tn instanceof InnerNode && ((InnerNode)tn).isCollapsed()) {
        TreePath tp = new TreePath(tn.getPath());
        list.add(generatePathString(tp));
      }
    }
    return list.toArray(new String[0]);
  }
  
  /**
   * Generates a path string for the given tree node.
   * <p>The path string does not include the project root node, but rather a period in 
   * its place. Following the "./" is one of the 3 main groups, "[ Source Files ]",
   * "[ Auxiliary ]", "[ External ]".  The nodes in the path are represented by their
   * names delimited by the forward slash ("/").  The path ends with a final delimeter.</p>
   * <p>(e.g. "./[ Source Files ]/util/docnavigation/")</p>
   * @return the path string for the given node in the JTree
   */
  public String generatePathString(TreePath tp) {
    String path = "";
    
    TreeNode root = (TreeNode) _model.getRoot();
    
    while (tp != null) {
      TreeNode curr = (TreeNode) tp.getLastPathComponent();
      if (curr == root) {
        path = "./" + path;
      }
      else {
        path = curr + "/" + path;
      }
      tp = tp.getParentPath();
    }
    
    return path;
  }
}

