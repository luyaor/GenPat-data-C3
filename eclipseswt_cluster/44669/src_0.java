/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.examples.layoutexample;


import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

class GridLayoutTab extends Tab {
	/* Controls for setting layout parameters */
	Spinner numColumns;
	Button makeColumnsEqualWidth;
	Spinner marginWidth, marginHeight, horizontalSpacing, verticalSpacing;
	/* The example layout instance */
	GridLayout gridLayout;
	/* TableEditors and related controls*/
	TableEditor nameEditor, comboEditor, widthEditor, heightEditor;
	TableEditor vAlignEditor, hAlignEditor, hIndentEditor;
	TableEditor hSpanEditor, vSpanEditor, hGrabEditor, vGrabEditor;
	CCombo combo, vAlign, hAlign, hGrab, vGrab;
	Text nameText, widthText, heightText, hIndent, hSpan, vSpan;
	int prevSelected = 0;
	/* Constants */
	final int NAME_COL = 0;
	final int COMBO_COL = 1;
	final int WIDTH_COL = 2;
	final int HEIGHT_COL = 3;
	final int HALIGN_COL = 4;
	final int VALIGN_COL = 5;
	final int HINDENT_COL = 6;
	final int HSPAN_COL = 7;
	final int VSPAN_COL = 8;
	final int HGRAB_COL = 9;
	final int VGRAB_COL = 10;
	
	final int TOTAL_COLS = 11;
		
	/**
	 * Creates the Tab within a given instance of LayoutExample.
	 */
	GridLayoutTab() {
	}
	
	/**
	 * Creates the widgets in the "child" group.
	 */
	void createChildWidgets() {
		/* Create the TraverseListener */
		final TraverseListener traverseListener = new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if(e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_TAB_NEXT)
					resetEditors();
				if(e.detail == SWT.TRAVERSE_ESCAPE)
					disposeEditors();
			}
		};
		
		/* Add common controls */
		super.createChildWidgets();
			
		/* Add TableEditors */		
		nameEditor = new TableEditor(table);
		comboEditor = new TableEditor(table);
		widthEditor = new TableEditor(table);
		heightEditor = new TableEditor(table);
		vAlignEditor = new TableEditor(table);
		hAlignEditor = new TableEditor(table);
		hIndentEditor = new TableEditor(table);
		hSpanEditor = new TableEditor(table);
		vSpanEditor = new TableEditor(table);
		hGrabEditor = new TableEditor(table);
		vGrabEditor = new TableEditor(table);
		table.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				resetEditors();
				index = table.getSelectionIndex();
				Point pt = new Point(e.x, e.y);
                newItem = table.getItem(pt);
                if(newItem == null) return;
                TableItem oldItem = comboEditor.getItem();
                if(newItem == oldItem || newItem != lastSelected) {
					lastSelected = newItem;
					return;
				}
				table.showSelection();
				
				nameText = new Text(table, SWT.SINGLE);
				nameText.setText(((String [])data.elementAt(index))[NAME_COL]);
				createTextEditor(nameText, nameEditor, NAME_COL);
				
				combo = new CCombo(table, SWT.READ_ONLY);
				createComboEditor(combo, comboEditor);
								
				widthText = new Text(table, SWT.SINGLE);
				widthText.setText(((String [])data.elementAt(index))[WIDTH_COL]);
				createTextEditor(widthText, widthEditor, WIDTH_COL);
				
				heightText = new Text(table, SWT.SINGLE);
				heightText.setText(((String[])data.elementAt(index))[HEIGHT_COL]);
				createTextEditor(heightText, heightEditor, HEIGHT_COL);
				String[] alignValues = new String[] {"BEGINNING","CENTER","END","FILL"};
				hAlign = new CCombo(table, SWT.NONE);
				hAlign.setItems(alignValues);
				hAlign.setText(newItem.getText(HALIGN_COL));
				hAlignEditor.horizontalAlignment = SWT.LEFT;
				hAlignEditor.grabHorizontal = true;
				hAlignEditor.minimumWidth = 50;
				hAlignEditor.setEditor(hAlign, newItem, HALIGN_COL);
				hAlign.addTraverseListener(traverseListener);
				
				vAlign = new CCombo(table, SWT.NONE);
				vAlign.setItems(alignValues);
				vAlign.setText(newItem.getText(VALIGN_COL));
				vAlignEditor.horizontalAlignment = SWT.LEFT;
				vAlignEditor.grabHorizontal = true;
				vAlignEditor.minimumWidth = 50;
				vAlignEditor.setEditor(vAlign, newItem, VALIGN_COL);
				vAlign.addTraverseListener(traverseListener);
				
				hIndent = new Text(table, SWT.SINGLE);
				hIndent.setText(((String[])data.elementAt(index))[HINDENT_COL]);
				createTextEditor(hIndent, hIndentEditor, HINDENT_COL);
				
				hSpan = new Text(table, SWT.SINGLE);
				hSpan.setText (((String [])data.elementAt (index)) [HSPAN_COL]);
				createTextEditor (hSpan, hSpanEditor, HSPAN_COL);
				
				vSpan = new Text (table, SWT.SINGLE);
				vSpan.setText(((String[])data.elementAt(index))[VSPAN_COL]);
				createTextEditor(vSpan, vSpanEditor, VSPAN_COL);
				
				String[] boolValues = new String[] {"false","true"};
				hGrab = new CCombo(table, SWT.NONE);
				hGrab.setItems(boolValues);
				hGrab.setText(newItem.getText (HGRAB_COL));
				hGrabEditor.horizontalAlignment = SWT.LEFT;
				hGrabEditor.grabHorizontal = true;
				hGrabEditor.minimumWidth = 50;
				hGrabEditor.setEditor(hGrab, newItem, HGRAB_COL);
				hGrab.addTraverseListener(traverseListener);
				
				vGrab = new CCombo(table, SWT.NONE);
				vGrab.setItems(boolValues);
				vGrab.setText(newItem.getText (VGRAB_COL));
				vGrabEditor.horizontalAlignment = SWT.LEFT;
				vGrabEditor.grabHorizontal = true;
				vGrabEditor.minimumWidth = 50;
				vGrabEditor.setEditor(vGrab, newItem, VGRAB_COL);
				vGrab.addTraverseListener(traverseListener);
                
                for(int i=0; i<table.getColumnCount(); i++) {
                	Rectangle rect = newItem.getBounds(i);
                    if(rect.contains(pt)) {
                    	switch (i) {
                    		case NAME_COL:
                    			nameText.setFocus();
                    			break;
							case COMBO_COL :
								combo.setFocus();	
								break;
							case WIDTH_COL :	
								widthText.setFocus();
								break;
							case HEIGHT_COL :
								heightText.setFocus();
								break;
							case HALIGN_COL :
								hAlign.setFocus();
								break;
							case VALIGN_COL :
								vAlign.setFocus();
								break;
							case HINDENT_COL :
								hIndent.setFocus();
								break;
							case HSPAN_COL :
								hSpan.setFocus();
								break;
							case VSPAN_COL :
								vSpan.setFocus();
								break;
							case HGRAB_COL :
								hGrab.setFocus();
								break;
							case VGRAB_COL :
								vGrab.setFocus();
								break;
							default :
								resetEditors();
								break;
						}
                    }
                } 
			}
		});
		
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {				
				if(event.detail == SWT.ARROW) {
					ToolItem item = (ToolItem)event.widget;
					ToolBar bar = item.getParent();
					Display display = bar.getDisplay();
					Shell shell = bar.getShell();
					Menu menu = new Menu(shell, SWT.POP_UP);
					for(int i = 0; i < OPTIONS.length; i++) {
						final MenuItem newItem = new MenuItem(menu, SWT.RADIO);
						newItem.setText(OPTIONS[i]);						
						newItem.addSelectionListener(new SelectionAdapter(){
							public void widgetSelected(SelectionEvent event) {
								MenuItem menuItem = (MenuItem)event.widget;
								if(menuItem.getSelection()) {
									Menu menu  = menuItem.getParent();
									prevSelected = menu.indexOf(menuItem);
									TableItem item = new TableItem(table, SWT.NONE);
									String name = menuItem.getText().toLowerCase() + String.valueOf(table.indexOf(item));
									String[] insert = new String[] {name, menuItem.getText(),
											"-1","-1","BEGINNING","CENTER",
											"0","1","1","false","false"};
									item.setText(insert);
									data.addElement(insert);
									resetEditors ();
								}
							}
						});							
						newItem.setSelection(i == prevSelected);
					}
					Point pt = display.map(bar, null, event.x, event.y);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);					
					while (menu != null && !menu.isDisposed() && menu.isVisible()) {
						if (!display.readAndDispatch()) {
							display.sleep();
						}
					}
					menu.dispose();
				} else {
					String selection = OPTIONS[prevSelected];
					TableItem item = new TableItem(table, 0);
					String name = selection.toLowerCase() + String.valueOf(table.indexOf(item));
					String[] insert = new String[] {name, selection,
							"-1","-1","BEGINNING","CENTER",
							"0","1","1","false","false"};
					item.setText(insert);
					data.addElement(insert);
					resetEditors();
				}
			}
		});
	}

	/**
	 * Creates the control widgets.
	 */
	void createControlWidgets() {
        /* Controls the columns in the GridLayout */
		Group columnGroup = new Group(controlGroup, SWT.NONE);
		columnGroup.setText(LayoutExample.getResourceString ("Columns"));
		columnGroup.setLayout(new GridLayout(2, false));
		columnGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		new Label(columnGroup, SWT.NONE).setText("Number of Columns");
		numColumns = new Spinner(columnGroup, SWT.BORDER);
		numColumns.setMinimum(1);
		numColumns.addSelectionListener(selectionListener);
		makeColumnsEqualWidth = new Button(columnGroup, SWT.CHECK);
		makeColumnsEqualWidth.setText("Make Columns Equal Width");
		makeColumnsEqualWidth.addSelectionListener(selectionListener);
		makeColumnsEqualWidth.setEnabled(false);
		makeColumnsEqualWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		/* Controls the margins and spacing of the GridLayout */
		Group marginGroup = new Group(controlGroup, SWT.NONE);
		marginGroup.setText (LayoutExample.getResourceString("Margins_Spacing"));
		marginGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		marginGroup.setLayout(new GridLayout(2, false));
		new Label (marginGroup, SWT.NONE).setText("Margin Width");
		marginWidth = new Spinner (marginGroup, SWT.BORDER);
		marginWidth.setSelection(5);
		marginWidth.addSelectionListener(selectionListener);
		new Label(marginGroup, SWT.NONE).setText("Margin Height");
		marginHeight = new Spinner(marginGroup, SWT.BORDER);
		marginHeight.setSelection(5);
		marginHeight.addSelectionListener(selectionListener);
		new Label(marginGroup, SWT.NONE).setText("Horizontal Spacing");
		horizontalSpacing = new Spinner(marginGroup, SWT.BORDER);
		horizontalSpacing.setSelection(5);
		horizontalSpacing.addSelectionListener(selectionListener);
		new Label(marginGroup, SWT.NONE).setText("Vertical Spacing");
		verticalSpacing = new Spinner(marginGroup, SWT.BORDER);
		verticalSpacing.setSelection(5);
		verticalSpacing.addSelectionListener(selectionListener);
        
		/* Add common controls */
		super.createControlWidgets();
		controlGroup.pack();
	}
	
	/**
	 * Creates the example layout.
	 */
	void createLayout() {
		gridLayout = new GridLayout();
		layoutComposite.setLayout(gridLayout);
	}
	
	/** 
	 * Disposes the editors without placing their contents
	 * into the table.
	 */
	void disposeEditors() {
		comboEditor.setEditor(null, null, -1);
		combo.dispose();
		nameText.dispose();
		widthText.dispose();
		heightText.dispose();
		hAlign.dispose();
		vAlign.dispose();
		hIndent.dispose();
		hSpan.dispose();
		vSpan.dispose();
		hGrab.dispose();
		vGrab.dispose();
	}
	
	/**
	 * Generates code for the example layout.
	 */	
	StringBuffer generateLayoutCode() {
		StringBuffer code = new StringBuffer();
		code.append("\t\tGridLayout gridLayout = new GridLayout ();\n");
		if(gridLayout.numColumns != 1) {
			code.append("\t\tgridLayout.numColumns = " + gridLayout.numColumns + ";\n");
		}
		if(gridLayout.makeColumnsEqualWidth) {
			code.append("\t\tgridLayout.makeColumnsEqualWidth = true;\n");
		}
		if(gridLayout.marginWidth != 5) {
			code.append("\t\tgridLayout.marginWidth = " + gridLayout.marginWidth + ";\n");
		}
		if(gridLayout.marginHeight != 5) {
			code.append("\t\tgridLayout.marginHeight = " + gridLayout.marginHeight + ";\n");
		}
		if(gridLayout.horizontalSpacing != 5) {
			code.append("\t\tgridLayout.horizontalSpacing = " + gridLayout.horizontalSpacing + ";\n");
		}
		if(gridLayout.verticalSpacing != 5) {
			code.append("\t\tgridLayout.verticalSpacing = " + gridLayout.verticalSpacing + ";\n");
		}
		code.append("\t\tshell.setLayout (gridLayout);\n");
		
		boolean first = true;
		for(int i = 0; i < children.length; i++) {
			Control control = children[i];
			code.append(getChildCode(control, i));
			GridData data = (GridData)control.getLayoutData();
			if(data != null) {
				code.append("\t\t");
				if(first) {
					code.append("GridData ");
					first = false;
				}
				code.append ("data = new GridData ();\n");	
				if (data.widthHint != SWT.DEFAULT) {
					code.append("\t\tdata.widthHint = " + data.widthHint + ";\n");
				}
				if (data.heightHint != SWT.DEFAULT) {
					code.append("\t\tdata.heightHint = " + data.heightHint + ";\n");
				}
				if(data.horizontalAlignment != SWT.BEGINNING) {
					String alignment;
					int hAlignment = data.horizontalAlignment;
					if(hAlignment == SWT.CENTER) alignment = "SWT.CENTER";
					else if(hAlignment == SWT.END) alignment = "SWT.END";
					else alignment = "SWT.FILL";
					code.append("\t\tdata.horizontalAlignment = " + alignment + ";\n");
				}
				if(data.verticalAlignment != SWT.CENTER) {
					String alignment;
					int vAlignment = data.verticalAlignment;
					if(vAlignment == SWT.BEGINNING) alignment = "SWT.BEGINNING";
					else if(vAlignment == SWT.END) alignment = "SWT.END";
					else alignment = "SWT.FILL";
					code.append("\t\tdata.verticalAlignment = " + alignment + ";\n");
				}	
				if(data.horizontalIndent != 0) {
					code.append ("\t\tdata.horizontalIndent = " + data.horizontalIndent + ";\n");
				}
				if(data.horizontalSpan != 1) {
					code.append ("\t\tdata.horizontalSpan = " + data.horizontalSpan + ";\n");
				}
				if(data.verticalSpan != 1) {
					code.append ("\t\tdata.verticalSpan = " + data.verticalSpan + ";\n");
				}
				if(data.grabExcessHorizontalSpace) {
					code.append ("\t\tdata.grabExcessHorizontalSpace = true;\n");
				}
				if(data.grabExcessVerticalSpace) {
					code.append ("\t\tdata.grabExcessVerticalSpace = true;\n");
				}
				if(code.substring(code.length() - 33).equals("GridData data = new GridData ();\n")) {
					code.delete(code.length() - 33, code.length());
					first = true;
				} else if(code.substring(code.length () - 24).equals("data = new GridData ();\n")) { 
					code.delete(code.length() - 24, code.length());
				} else{	
					code.append("\t\t" + names[i] + ".setLayoutData (data);\n");
				}
			}
		}
		return code;
	}
	
	/**
	 * Returns the layout data field names.
	 */
	String[] getLayoutDataFieldNames() {
		return new String[] {
			"Control Name",
			"Control Type", 
			"width", 
			"height", 
			"horizontalAlignment", 
			"verticalAlignment", 
			"horizontalIndent", 
			"horizontalSpan",
			"verticalSpan", 
			"grabExcessHorizontalSpace", 
			"grabExcessVerticalSpace"
		};
	}
	
	/**
	 * Gets the text for the tab folder item.
	 */
	String getTabText() {
		return "GridLayout";
	}
	
	/**
	 * Takes information from TableEditors and stores it.
	 */
	void resetEditors() {
		resetEditors(false);
	}
	
	void resetEditors(boolean tab) {
		TableItem oldItem = comboEditor.getItem();
		if(oldItem != null) {
			int row = table.indexOf(oldItem);
			/** Make sure user enters a valid data*/
			try {				
				new String(nameText.getText ());
			} catch(NumberFormatException e) {
				nameText.setText(oldItem.getText (NAME_COL));
			}
			try {
				new Integer(widthText.getText()).intValue();
			} catch(NumberFormatException e) {
				widthText.setText(oldItem.getText(WIDTH_COL));
			}
			try {
				new Integer(heightText.getText()).intValue();
			} catch(NumberFormatException e) {
				heightText.setText(oldItem.getText(HEIGHT_COL));
			}
			try {
				new Integer(hIndent.getText()).intValue();
			} catch(NumberFormatException e) {
				hIndent.setText(oldItem.getText(HINDENT_COL));
			}
			try {
				new Integer(hSpan.getText()).intValue();
			} catch(NumberFormatException e) {
				hSpan.setText(oldItem.getText(HSPAN_COL));
			}
			try {
				new Integer(vSpan.getText()).intValue();
			} catch(NumberFormatException e) {
				vSpan.setText(oldItem.getText(VSPAN_COL));
			}
			String[] insert = new String[] {
				nameText.getText(), combo.getText(), widthText.getText(), heightText.getText(),
				hAlign.getText(), vAlign.getText(), hIndent.getText(), 
				hSpan.getText(), vSpan.getText(), hGrab.getText(), vGrab.getText()
			};
			data.setElementAt(insert, row);
			for(int i = 0; i < TOTAL_COLS; i++) {
				oldItem.setText(i, ((String[])data.elementAt(row))[i]);
			}
			if(!tab) disposeEditors();
		}
		setLayoutState();		
		refreshLayoutComposite();
		setLayoutData();
		layoutComposite.layout(true);
		layoutGroup.layout(true);
	}	
	
	/**
	 * Sets the layout data for the children of the layout.
	 */
	void setLayoutData() {
		Control[] children = layoutComposite.getChildren();
		TableItem[] items = table.getItems();
		GridData data;
		int hIndent, hSpan, vSpan;
		String vAlign, hAlign, vGrab, hGrab;
		for(int i = 0; i < children.length; i++) {
			data = new GridData();
			/* Set widthHint and heightHint */
			data.widthHint = new Integer(items [i].getText(WIDTH_COL)).intValue();
			data.heightHint = new Integer(items [i].getText(HEIGHT_COL)).intValue();
			/* Set vertical alignment and horizontal alignment */
			hAlign = items[i].getText(HALIGN_COL);
			if (hAlign.equals("CENTER")) {
				data.horizontalAlignment = SWT.CENTER;
			} else if(hAlign.equals("END")) {
				data.horizontalAlignment = SWT.END;
			} else if(hAlign.equals("FILL")) {
				data.horizontalAlignment = SWT.FILL;
			} else {
				data.horizontalAlignment = SWT.BEGINNING;
			}
			vAlign = items [i].getText(VALIGN_COL);
			if (vAlign.equals("BEGINNING")) {
				data.verticalAlignment = SWT.BEGINNING;
			} else if (vAlign.equals("END")) {
				data.verticalAlignment = SWT.END;
			} else if (vAlign.equals("FILL")) {
				data.verticalAlignment = SWT.FILL;
			} else {
				data.verticalAlignment = SWT.CENTER;
			}
			/* Set indents and spans */
			hIndent = new Integer(items[i].getText(HINDENT_COL)).intValue();
			data.horizontalIndent = hIndent;
			hSpan = new Integer(items [i].getText(HSPAN_COL)).intValue();
			data.horizontalSpan = hSpan;
			vSpan = new Integer(items[i].getText(VSPAN_COL)).intValue();
			data.verticalSpan = vSpan;
			/* Set grabbers */
			hGrab = items[i].getText(HGRAB_COL);
			if(hGrab.equals("true")) {
				data.grabExcessHorizontalSpace = true;
			} else {
				data.grabExcessHorizontalSpace = false;
			}
			vGrab = items [i].getText(VGRAB_COL);
			if (vGrab.equals("true")) {
				data.grabExcessVerticalSpace = true;
			} else {
				data.grabExcessVerticalSpace = false;
			}
			children[i].setLayoutData (data);
		}
	}
	
	/**
	 * Sets the state of the layout.
	 */
	void setLayoutState() {
		/* Set the columns for the layout */
		gridLayout.numColumns = numColumns.getSelection();
		gridLayout.makeColumnsEqualWidth = makeColumnsEqualWidth.getSelection();
		makeColumnsEqualWidth.setEnabled(numColumns.getSelection() > 1);
		
		/* Set the margins and spacing */
		gridLayout.marginWidth = marginWidth.getSelection();
		gridLayout.marginHeight = marginHeight.getSelection();
		gridLayout.horizontalSpacing = horizontalSpacing.getSelection();
		gridLayout.verticalSpacing = verticalSpacing.getSelection();
	}
}
