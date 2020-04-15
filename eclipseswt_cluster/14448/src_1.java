/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.tools.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class MacGeneratorUI {
	MacGenerator gen;

	Display display;
	Shell shell;
	Tree nodesTree;
	Table attribTable;

	public MacGeneratorUI(String[] xmls) {
		gen = new MacGenerator(xmls);
		gen.setOutputDir("../org.eclipse.swt/Eclipse SWT PI/cocoa/");
		gen.setMainClass("org.eclipse.swt.internal.cocoa.OS");
	}

	TreeItem lastParent;
	TreeItem addChild (Node node, TreeItem superItem) {
		String name = node.getNodeName();
		if (name.equals("#text")) return null;
		TreeItem parentItem = null;
		if (lastParent != null && lastParent.getParentItem() == superItem && name.equals(lastParent.getData())) {
			parentItem = lastParent;
		} else {
			TreeItem[] items = superItem.getItems();
			for (int i = 0; i < items.length; i++) {
				if (name.equals(items[i].getData())) {
					parentItem = items[i];
					break;
				}
			}
			if (parentItem == null) {
				parentItem = new TreeItem(superItem, SWT.NONE);
				parentItem.setData(name);
				parentItem.setText(getPrettyText(name));
			}
			lastParent = parentItem;
		}
		TreeItem item = new TreeItem(parentItem, SWT.NONE);
		Node idAttrib = gen.getIDAttribute(node);
		item.setText(idAttrib != null ? idAttrib.getNodeValue() : name);
		item.setData(node);
		checkItem(node, item);
		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength() > 0) new TreeItem(item, SWT.NONE);
		return item;
	}
	
	void checkPath(TreeItem item, boolean checked, boolean grayed) {
	    if (item == null) return;
	    if (grayed) {
	        checked = true;
	    } else {
	        int index = 0;
	        TreeItem[] items = item.getItems();
	        while (index < items.length) {
	            TreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) {
	                checked = grayed = true;
	                break;
	            }
	            index++;
	        }
	    }
	    item.setChecked(checked);
	    item.setGrayed(grayed);
	    checkPath(item.getParentItem(), checked, grayed);
	}
	
	void checkItem(Node node, TreeItem item) {
		NamedNodeMap attributes = node.getAttributes();
		Node gen = attributes.getNamedItem("swt_gen");
		if (gen != null) {
			String value = gen.getNodeValue();
			boolean grayed = value.equals("mixed");
			boolean checked = grayed || value.equals("true");
			item.setChecked(checked);
			item.setGrayed(grayed);
		}
	}
	
	boolean getEditable(TableItem item, int column) {
		if (!(item.getData() instanceof Node)) return false;
		String attribName = item.getText();
		return attribName.startsWith("swt_");
	}

	String getPrettyText(String text) {
		if (text.equals("class")) return "Classes";
		if (text.equals("depends_on")) return "Depends_on";
		return text.substring(0, 1).toUpperCase() + text.substring(1) + "s";
	}

	void checkChildren(TreeItem item) {
		TreeItem dummy;
		if (item.getItemCount() == 1 && (dummy = item.getItem(0)).getData() == null) {
			dummy.dispose();
			Node node = (Node)item.getData();
			NodeList childNodes = node.getChildNodes();
			for (int i = 0, length = childNodes.getLength(); i < length; i++) {
				addChild(childNodes.item(i), item);
			}
			/* Figure out categories state */
			TreeItem[] items = item.getItems();
			for (int i = 0; i < items.length; i++) {
				TreeItem[] children = items[i].getItems();
				int checkedCount = 0;
				for (int j = 0; j < children.length; j++) {
					if (children[j].getChecked()) checkedCount++;
					if (children[j].getGrayed()) break;
				}
				items[i].setChecked(checkedCount != 0);
				items[i].setGrayed(checkedCount != children.length);
			}
			TreeColumn[] columns = nodesTree.getColumns();
			for (int i = 0; i < columns.length; i++) {
				columns[i].pack();
			}
		}
	}
	void checkItems(TreeItem item, boolean checked) {
	    item.setGrayed(false);
	    item.setChecked(checked);
	    /*
	     * Note that this creates the whole tree underneath item
	     * so that the checked/grayed state can be kept in the
	     * UI only and updated when generate is called. This can
	     * be very expensive.
	     */
	    checkChildren(item);
	    TreeItem[] items = item.getItems();
	    for (int i = 0; i < items.length; i++) {
	        checkItems(items[i], checked);
	    }
	}
	
	void cleanup() {
		display.dispose();
	}

	Composite createSignaturesPanel(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginLeft = 5;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("Signatures:");
		
		final Text search = new Text(comp, SWT.BORDER | SWT.SINGLE | SWT.SEARCH);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		search.setLayoutData(data);
		search.setText(".*");
		search.addListener(SWT.DefaultSelection, new Listener() {
			public void handleEvent(Event arg0) {
				searchFor(search.getText());
			}
		});
		
		nodesTree = new Tree(comp, SWT.SINGLE | SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		nodesTree.setLayoutData(data);
		
		nodesTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TreeItem item = (TreeItem)event.item;
				if (item == null) return;
				if (event.detail != SWT.CHECK) {
					selectChild(item);
					return;
				}
				boolean checked = item.getChecked();
				item.getParent().setRedraw(false);
                checkItems(item, checked);
                checkPath(item.getParentItem(), checked, false);
                item.getParent().setRedraw(true);
			}
		});
		nodesTree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event event) {
				checkChildren((TreeItem)event.item);				
			}
		});
		
		return comp;
	}
	
	Composite createPropertiesPanel(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		comp.setLayout(layout);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("Properties:");
		
		attribTable = new Table(comp, SWT.BORDER | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		attribTable.setLayoutData(data);
		attribTable.setLinesVisible(true);
		attribTable.setHeaderVisible(true);
		TableColumn nameColumn = new TableColumn(attribTable, SWT.NONE);
		nameColumn.setText("Name");
		nameColumn.pack();
		TableColumn valueColumn = new TableColumn(attribTable, SWT.NONE);
		valueColumn.setText("Value");
		valueColumn.pack();
		
		final Text editorTx = new Text(attribTable, SWT.SINGLE);
		final TableEditor editor = new TableEditor(attribTable);
		editor.grabHorizontal = true;
		editor.setEditor(editorTx);
		Listener textListener = new Listener() {
			public void handleEvent(Event e) {
				if (e.type == SWT.Traverse) {
					switch (e.detail) {
						case SWT.TRAVERSE_ESCAPE:
							editor.setItem(null);
							break;
						default:
							return;
					}
				}
				editorTx.setVisible(false);
				TableItem item = editor.getItem();
				if (item == null) return;
				int column = editor.getColumn();
				String value = editorTx.getText();
				item.setText(column, value);
				Element node = (Element)item.getData();
				String name = item.getText();
				if (value.length() != 0) {
					node.setAttribute(name, value);
				} else {
					node.removeAttribute(name);
				}
			}
		};
		editorTx.addListener(SWT.DefaultSelection, textListener);
		editorTx.addListener(SWT.FocusOut, textListener);
		editorTx.addListener(SWT.Traverse, textListener);
		attribTable.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(final Event e) {
				e.display.asyncExec (new Runnable () {
					public void run () {
						if (attribTable.isDisposed ()) return;
						if (e.button != 1) return;
						Point pt = new Point(e.x, e.y);
						TableItem item = attribTable.getItem(pt);
						if (item == null) return;
						int column = -1;
						for (int i = 0; i < attribTable.getColumnCount(); i++) {
							if (item.getBounds(i).contains(pt)) {
								column = i;
								break;
							}				
						}
						if (column == -1) return;
						if (!getEditable(item, column)) return;
						editor.setColumn(column);
						editor.setItem(item);
						editorTx.setText(item.getText(column));
						editorTx.selectAll();
						editorTx.setVisible(true);
						editorTx.setFocus();
					}
				});
			}
		});
		
		return comp;
	}
	
	Composite createActionsPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 10;
		panel.setLayout(layout);
		
		Button generate = new Button(panel, SWT.PUSH);
		generate.setText("Generate");
		generate.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				TreeItem[] items = nodesTree.getItems();
				for (int i = 0; i < items.length; i++) {
					updateGenAttribute(items[i]);
				}
				gen.generateAll();
			}
		});
		return panel;
	}
	
	public void open() {
		display = new Display();
		shell = new Shell(display);

		Composite parent = shell;
		FormLayout layout = new FormLayout();
		parent.setLayout(layout);
		
		Composite signaturePanel = createSignaturesPanel(parent);
		final Sash sash = new Sash(parent, SWT.SMOOTH | SWT.VERTICAL);
		Composite propertiesPanel = createPropertiesPanel(parent);
		Composite actionsPanel = createActionsPanel(parent);

		FormData data;
		
		data = new FormData();		
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(sash, 0);
		data.bottom = new FormAttachment(100, 0);
		signaturePanel.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(null, Math.max(200, shell.getSize().x / 2));
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		sash.setLayoutData(data);
		
		data = new FormData();
		data.left = new FormAttachment(sash, sash.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(actionsPanel, 0);
		data.bottom = new FormAttachment(100, 0);
		propertiesPanel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		actionsPanel.setLayoutData(data);
		
		sash.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				Composite parent = sash.getParent();
				Rectangle rect = parent.getClientArea();
				event.x = Math.min (Math.max (event.x, 60), rect.width - 60);
				if (event.detail != SWT.DRAG) {
					FormData data = (FormData)sash.getLayoutData();
					data.left.offset = event.x;
					parent.layout(true);
				}
			}
		});

		updateNodes();
	}

	public void run() {
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		cleanup();
	}
	
	void searchFor(String name) {

	}
	
	void selectChild(TreeItem item) {
		attribTable.removeAll();
		if (!(item.getData() instanceof Node)) return;
		Node node = (Node)item.getData();
		NamedNodeMap attributes = node.getAttributes();
		String[] extraAttribs = gen.getExtraAttributeNames(node);
		for (int i = 0; i < extraAttribs.length; i++) {
			TableItem attribItem = new TableItem(attribTable, SWT.NONE);
			attribItem.setText(extraAttribs[i]);
			attribItem.setData(node);
			attribItem.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
			Node attrib = attributes.getNamedItem(extraAttribs[i]);
			if (attrib != null) {
				attribItem.setText(1, attrib.getNodeValue());
			}
			
		}
		checkItem(node, item);
		for (int i = 0, length = attributes.getLength(); i < length; i++) {
			Node attrib = attributes.item(i);
			String attribName = attrib.getNodeName();
			if (attribName.startsWith("swt_")) continue;
			TableItem attribItem = new TableItem(attribTable, SWT.NONE);
			attribItem.setText(attribName);
			attribItem.setText(1, attrib.getNodeValue());
		}
		attribTable.getColumn(0).pack();
		attribTable.getColumn(1).setWidth(500);
	}
	
	void updateGenAttribute (TreeItem item) {
		if (item.getData() instanceof Element) {
			Element node = (Element)item.getData();
			if (item.getChecked()) {
				if (item.getGrayed()) {
					node.setAttribute("swt_gen", "mixed");
				} else {
					node.setAttribute("swt_gen", "true");
				}
			} else {
				node.removeAttribute("swt_gen");
			}
		}
		TreeItem[] items = item.getItems();
		for (int i = 0; i < items.length; i++) {
			updateGenAttribute(items[i]);
		}
	}
	
	void updateNodes() {
		String[] xmls = gen.getXmls();
		if (xmls == null) return;
		Document[] documents = gen.getDocuments();
		for (int x = 0; x < xmls.length; x++) {
			String xmlPath = xmls[x];
			Document document = documents[x];
			if (document == null) {
				System.out.println("Could not find: " + xmlPath);
				continue;
			}
			TreeItem item = new TreeItem(nodesTree, SWT.NONE);
			item.setText(gen.getFileName(xmlPath));
			Node node = document.getDocumentElement();
			item.setData(node);
			checkItem(node, item);
			new TreeItem(item, SWT.NONE);
		}
		TreeColumn[] columns = nodesTree.getColumns();
		for (int i = 0; i < columns.length; i++) {
			columns[i].pack();
		}
	}

	public static void main(String[] args) {
		args = new String[]{
			"./Mac Generation/org/eclipse/swt/tools/internal/AppKitFull.bridgesupport",
			"./Mac Generation/org/eclipse/swt/tools/internal/FoundationFull.bridgesupport",
			"./Mac Generation/org/eclipse/swt/tools/internal/WebKitFull.bridgesupport",
		};
		try {
			MacGeneratorUI ui = new MacGeneratorUI(args);
			ui.open();
			ui.run();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
