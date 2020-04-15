/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

class StackLayoutTab extends Tab {
	/* Controls for setting layout parameters */
	Button backButton, advanceButton;
	Spinner marginWidth, marginHeight;
	StackLayout stackLayout;
	int currentLayer = 0;
	/* TableEditors and related controls*/
	TableEditor comboEditor, nameEditor;
	CCombo combo;
	int prevSelected = 0;
	Text nameText;
	final int NAME_COL = 0;
	
	/**
	 * Creates the Tab within a given instance of LayoutExample.
	 */
	StackLayoutTab(LayoutExample instance) {
		super(instance);
	}
	
	/**
	 * Creates the widgets in the "child" group.
	 */
	void createChildWidgets() {
		/* Add common controls */
		super.createChildWidgets();
		
		/* Add TableEditors */
		comboEditor = new TableEditor(table);
		nameEditor = new TableEditor(table);
		table.addMouseListener (new MouseAdapter() {
			public void mouseDown(MouseEvent e) { 	
				resetEditors();
				index = table.getSelectionIndex();
				if(index == -1) return;
				//set top layer of stack to the selected item
				currentLayer = index;
				stackLayout.topControl = children[currentLayer];
				backButton.setEnabled(currentLayer > 0);
				advanceButton.setEnabled(currentLayer < children.length - 1);
				layoutComposite.layout();
				
				TableItem oldItem = comboEditor.getItem();
				newItem = table.getItem(index);
				if(newItem == oldItem || newItem != lastSelected) {
					lastSelected = newItem;
					return;
				}
				table.showSelection();				
				combo = new CCombo(table, SWT.READ_ONLY);				
				createComboEditor(combo, comboEditor);
				
				nameText = new Text(table, SWT.SINGLE);
				nameText.setText(((String[])data.elementAt(index))[NAME_COL]);
				createTextEditor(nameText, nameEditor, NAME_COL);
			}
		});		
		
		// Add listener to add an element to the table
		add.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {				
				if (event.detail == SWT.ARROW) {
					ToolItem item = (ToolItem)event.widget;
					ToolBar bar = item.getParent();
					Display display = bar.getDisplay();
					Shell shell = bar.getShell();
					final Menu menu = new Menu(shell, SWT.POP_UP);					
					for(int i = 0; i < OPTIONS.length; i++) {
						final MenuItem newItem = new MenuItem(menu, SWT.RADIO);
						newItem.setText(OPTIONS[i]);						
						newItem.addSelectionListener(new SelectionAdapter(){
							public void widgetSelected(SelectionEvent event) {
								MenuItem menuItem = (MenuItem)event.widget;
								if (menuItem.getSelection()) {
									Menu menu  = menuItem.getParent();
									prevSelected = menu.indexOf(menuItem);
									TableItem item = new TableItem(table, SWT.NONE);
									String name = menuItem.getText().toLowerCase() + String.valueOf(table.getItemCount() - 1);
									String[] insert = new String[] {name, menuItem.getText()};
									item.setText(insert);
									data.addElement(insert);
									resetEditors();
								}
							}
						});							
						newItem.setSelection(i == prevSelected);
					}
					Point pt = display.map(bar, null, event.x, event.y);
					menu.setLocation(pt.x, pt.y);
					menu.setVisible(true);
					
					while(menu != null && !menu.isDisposed() && menu.isVisible()) {
						if(!display.readAndDispatch()) {
							display.sleep();
						}
					}
					menu.dispose();
				} else {
					String selection = OPTIONS[prevSelected];
					TableItem item = new TableItem(table, 0);
					String name = selection.toLowerCase() + String.valueOf(table.indexOf(item));
					String[] insert = new String[] { name, selection }; 
					item.setText(insert);
					data.addElement(insert);
					resetEditors();
				}
				currentLayer = children.length -1;
				stackLayout.topControl = children[currentLayer];
				layoutComposite.layout();
				backButton.setEnabled(children.length > 1);
				advanceButton.setEnabled(false);
			}
		});
	}
	
	/**
	 * Creates the control widgets.
	 */
	void createControlWidgets() {
		/* Controls the margins of the StackLayout */
		Group marginGroup = new Group(controlGroup, SWT.NONE);
		marginGroup.setText (LayoutExample.getResourceString("Margins"));
		marginGroup.setLayout(new GridLayout(2, false));
		marginGroup.setLayoutData (new GridData(SWT.FILL, SWT.CENTER, true, false));
		new Label(marginGroup, SWT.NONE).setText("Margin Width");
		marginWidth = new Spinner(marginGroup, SWT.BORDER);
		marginWidth.setSelection(0);
		marginWidth.addSelectionListener(selectionListener);
		new Label(marginGroup, SWT.NONE).setText("Margin Height");
		marginHeight = new Spinner(marginGroup, SWT.BORDER);
		marginHeight.setSelection(0);
		marginHeight.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		marginHeight.addSelectionListener(selectionListener);
		
		/* Add common controls */
		super.createControlWidgets();
		
		/* Position the sash */
		sash.setWeights(new int[] {4,1});
	}
	
	/**
	 * Creates the example layout.
	 */
	void createLayout() {
		stackLayout = new StackLayout();
		layoutComposite.setLayout(stackLayout);
	}
	
	void createLayoutComposite() {
		layoutComposite = new Composite(layoutGroup, SWT.BORDER);
		layoutComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		createLayout();
	}
	
	/**
	 * Creates the layout group. This is the group on the
	 * left half of each example tab.
	 */
	void createLayoutGroup() {
		layoutGroup = new Group(sash, SWT.NONE);
		layoutGroup.setText(LayoutExample.getResourceString("Layout"));
		layoutGroup.setLayout(new GridLayout(2, true));
		createLayoutComposite();
		
		backButton = new Button(layoutGroup, SWT.PUSH);
	    backButton.setText("<<");
	    backButton.setEnabled(false);
		backButton.setLayoutData(new GridData (SWT.END, SWT.CENTER, false, false));
		backButton.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
		    	currentLayer--;
		    	stackLayout.topControl = children [currentLayer];
	    		layoutComposite.layout();
	    		backButton.setEnabled(currentLayer > 0);
	    		advanceButton.setEnabled(currentLayer < children.length);
	        }
		});		    
	    
	    advanceButton = new Button(layoutGroup, SWT.PUSH);
		advanceButton.setText(">>");
		advanceButton.setEnabled(false);
		advanceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
		    	currentLayer++;
		    	stackLayout.topControl = children [currentLayer];
		    	layoutComposite.layout();
		    	backButton.setEnabled(currentLayer > 0);
		    	advanceButton.setEnabled(currentLayer < children.length - 1);
	        }
		});		
	}
	
	/** 
	 * Disposes the editors without placing their contents
	 * into the table.
	 */
	void disposeEditors() {
		comboEditor.setEditor(null, null, -1);
		combo.dispose();
		nameText.dispose();
	}
	
	/**
	 * Generates code for the example layout.
	 */
	StringBuffer generateLayoutCode() {
		StringBuffer code = new StringBuffer();
		code.append("\t\tStackLayout stackLayout = new StackLayout ();\n");
		if (stackLayout.marginWidth != 0) {
			code.append("\t\tstackLayout.marginWidth = " + stackLayout.marginWidth + ";\n");
		}
		if (stackLayout.marginHeight != 0) {
			code.append("\t\tstackLayout.marginHeight = " + stackLayout.marginHeight + ";\n");
		}
		code.append("\t\tshell.setLayout (stackLayout);\n");
		for(int i = 0; i < children.length; i++) {
			Control control = children[i];
			code.append (getChildCode(control, i));
		}
		return code;
	}
	
	boolean needsCustom() {
		return true;
	}

	/**
	 * Returns the layout data field names.
	 */
	String[] getLayoutDataFieldNames() {
		return new String[] {"Control Name", "Control Type"};
	}
	
	/**
	 * Gets the text for the tab folder item.
	 */
	String getTabText() {
		return "StackLayout";
	}
	
	/**
	 * Takes information from TableEditors and stores it.
	 */
	void resetEditors() {
		TableItem oldItem = comboEditor.getItem();
		comboEditor.setEditor(null, null, -1);
		if(oldItem != null) {
			int row = table.indexOf(oldItem);
			try {				
				new String(nameText.getText());
			} catch(NumberFormatException e) {
				nameText.setText(oldItem.getText(NAME_COL));
			}
			String[] insert = new String[] {nameText.getText(), combo.getText()};
			data.setElementAt(insert, row);
			for(int i = 0 ; i < table.getColumnCount(); i++) {
				oldItem.setText(i, ((String[])data.elementAt(row))[i]);
			}
			disposeEditors();
		}
		refreshLayoutComposite();
		if(children.length > 0){
			stackLayout.topControl = children[currentLayer];
			layoutComposite.layout(true);			
		}
		layoutGroup.layout(true);
	}	

	/**
	 * Sets the state of the layout.
	 */
	void setLayoutState() {
		/* Set the margins and spacing */
		stackLayout.marginWidth = marginWidth.getSelection();		
		stackLayout.marginHeight = marginHeight.getSelection();		
	}	
	
}

