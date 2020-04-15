/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.examples.controlexample;


import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

class ShellTab extends Tab {	
	/* Style widgets added to the "Style" group */
	Button noParentButton, parentButton;
	Button noTrimButton, closeButton, titleButton, minButton, maxButton, borderButton, resizeButton, onTopButton;
	Button createButton, closeAllButton;
	Button modelessButton, primaryModalButton, applicationModalButton, systemModalButton;
	Group parentStyleGroup, modalStyleGroup;

	/* Variables used to track the open shells */
	int shellCount = 0;
	Shell [] shells = new Shell [4];
	
	/**
	 * Creates the Tab within a given instance of ControlExample.
	 */
	ShellTab(ControlExample instance) {
		super(instance);
	}

	/**
	 * Close all the example shells.
	 */
	void closeAllShells() {
		for (int i = 0; i<shellCount; i++) {
			if (shells[i] != null & !shells [i].isDisposed ()) {
				shells [i].dispose();
			}
		}
		shellCount = 0;
	}
	
	/**
	 * Handle the Create button selection event.
	 *
	 * @param event org.eclipse.swt.events.SelectionEvent
	 */
	public void createButtonSelected(SelectionEvent event) {
	
		/*
		 * Remember the example shells so they
		 * can be disposed by the user.
		 */
		if (shellCount >= shells.length) {
			Shell [] newShells = new Shell [shells.length + 4];
			System.arraycopy (shells, 0, newShells, 0, shells.length);
			shells = newShells;
		}
	
		/* Compute the shell style */
		int style = SWT.NONE;
		if (noTrimButton.getSelection()) style |= SWT.NO_TRIM;
		if (closeButton.getSelection()) style |= SWT.CLOSE;
		if (titleButton.getSelection()) style |= SWT.TITLE;
		if (minButton.getSelection()) style |= SWT.MIN;
		if (maxButton.getSelection()) style |= SWT.MAX;
		if (borderButton.getSelection()) style |= SWT.BORDER;
		if (resizeButton.getSelection()) style |= SWT.RESIZE;
		if (onTopButton.getSelection()) style |= SWT.ON_TOP;
		if (modelessButton.getSelection()) style |= SWT.MODELESS;
		if (primaryModalButton.getSelection()) style |= SWT.PRIMARY_MODAL;
		if (applicationModalButton.getSelection()) style |= SWT.APPLICATION_MODAL;
		if (systemModalButton.getSelection()) style |= SWT.SYSTEM_MODAL;
	
		/* Create the shell with or without a parent */
		if (noParentButton.getSelection ()) {
			shells [shellCount] = new Shell (style);
		} else {
			Shell shell = tabFolderPage.getShell ();
			shells [shellCount] = new Shell (shell, style);
		}
		final Shell currentShell = shells [shellCount];
		Button button = new Button(currentShell, SWT.PUSH);
		button.setBounds(20, 20, 120, 30);
		Button closeButton = new Button(currentShell, SWT.PUSH);
		closeButton.setBounds(160, 20, 120, 30);
		closeButton.setText(ControlExample.getResourceString("Close"));
		closeButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				currentShell.dispose();
			}
		});
	
		/* Set the size, title and open the shell */
		currentShell.setSize (300, 100);
		currentShell.setText (ControlExample.getResourceString("Title") + shellCount);
		hookListeners (currentShell);
		currentShell.open ();
		shellCount++;
	}
	
	/**
	 * Creates the "Control" group. 
	 */
	void createControlGroup () {
		/*
		 * Create the "Control" group.  This is the group on the
		 * left half of each example tab.  It consists of the
		 * style group, the display group and the size group.
		 */		
		controlGroup = new Group (tabFolderPage, SWT.NONE);
		GridLayout gridLayout= new GridLayout ();
		controlGroup.setLayout (gridLayout);
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		controlGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		controlGroup.setText (ControlExample.getResourceString("Parameters"));
	
		/* Create individual groups inside the "Control" group */
		styleGroup = new Group (controlGroup, SWT.NONE);
		gridLayout = new GridLayout ();
		styleGroup.setLayout (gridLayout);
		gridLayout.numColumns = 2;
		gridLayout.makeColumnsEqualWidth = true;
		styleGroup.setLayoutData (new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		styleGroup.setText (ControlExample.getResourceString("Styles"));
	
		/* Create a group for the parent style controls */
		parentStyleGroup = new Group (styleGroup, SWT.NONE);
		parentStyleGroup.setLayout (new GridLayout ());
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		parentStyleGroup.setLayoutData (gridData);
		parentStyleGroup.setText (ControlExample.getResourceString("Parent"));
	}
	
	/**
	 * Creates the "Control" widget children.
	 */
	void createControlWidgets () {
	
		/* Create the parent style buttons */
		noParentButton = new Button (parentStyleGroup, SWT.RADIO);
		noParentButton.setText (ControlExample.getResourceString("No_Parent"));
		parentButton = new Button (parentStyleGroup, SWT.RADIO);
		parentButton.setText (ControlExample.getResourceString("Parent"));
	
		/* Create a group for the decoration style controls */
		Group decorationStyleGroup = new Group(styleGroup, SWT.NONE);
		decorationStyleGroup.setLayout (new GridLayout ());
		GridData gridData = new GridData (GridData.HORIZONTAL_ALIGN_FILL);
		gridData.verticalSpan = 2;
		decorationStyleGroup.setLayoutData (gridData);
		decorationStyleGroup.setText (ControlExample.getResourceString("Decoration_Styles"));
	
		/* Create the decoration style buttons */
		noTrimButton = new Button (decorationStyleGroup, SWT.CHECK);
		noTrimButton.setText ("SWT.NO_TRIM");
		closeButton = new Button (decorationStyleGroup, SWT.CHECK);
		closeButton.setText ("SWT.CLOSE");
		titleButton = new Button (decorationStyleGroup, SWT.CHECK);
		titleButton.setText ("SWT.TITLE");
		minButton = new Button (decorationStyleGroup, SWT.CHECK);
		minButton.setText ("SWT.MIN");
		maxButton = new Button (decorationStyleGroup, SWT.CHECK);
		maxButton.setText ("SWT.MAX");
		borderButton = new Button (decorationStyleGroup, SWT.CHECK);
		borderButton.setText ("SWT.BORDER");
		resizeButton = new Button (decorationStyleGroup, SWT.CHECK);
		resizeButton.setText ("SWT.RESIZE");
		onTopButton = new Button (decorationStyleGroup, SWT.CHECK);
		onTopButton.setText ("SWT.ON_TOP");
	
		/* Create a group for the modal style controls */
		modalStyleGroup = new Group (styleGroup, SWT.NONE);
		modalStyleGroup.setLayout (new GridLayout ());
		modalStyleGroup.setText (ControlExample.getResourceString("Modal_Styles"));
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		modalStyleGroup.setLayoutData(gridData);
		
		/* Create the modal style buttons */
		modelessButton = new Button (modalStyleGroup, SWT.RADIO);
		modelessButton.setText ("SWT.MODELESS");
		primaryModalButton = new Button (modalStyleGroup, SWT.RADIO);
		primaryModalButton.setText ("SWT.PRIMARY_MODAL");
		applicationModalButton = new Button (modalStyleGroup, SWT.RADIO);
		applicationModalButton.setText ("SWT.APPLICATION_MODAL");
		systemModalButton = new Button (modalStyleGroup, SWT.RADIO);
		systemModalButton.setText ("SWT.SYSTEM_MODAL");
	
		/* Create the "create" and "closeAll" buttons */
		createButton = new Button (styleGroup, SWT.NONE);
		gridData = new GridData (GridData.HORIZONTAL_ALIGN_END);
		createButton.setLayoutData (gridData);
		createButton.setText (ControlExample.getResourceString("Create_Shell"));
		closeAllButton = new Button (styleGroup, SWT.NONE);
		gridData = new GridData (GridData.HORIZONTAL_ALIGN_BEGINNING);
		closeAllButton.setText (ControlExample.getResourceString("Close_All_Shells"));
		closeAllButton.setLayoutData (gridData);
	
		/* Add the listeners */
		createButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createButtonSelected(e);
			};
		});
		closeAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				closeAllShells ();
			};
		});
		SelectionListener decorationButtonListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				decorationButtonSelected(event);
			};
		};
		noTrimButton.addSelectionListener (decorationButtonListener);
		closeButton.addSelectionListener (decorationButtonListener);
		titleButton.addSelectionListener (decorationButtonListener);
		minButton.addSelectionListener (decorationButtonListener);
		maxButton.addSelectionListener (decorationButtonListener);
		borderButton.addSelectionListener (decorationButtonListener);
		resizeButton.addSelectionListener (decorationButtonListener);
		applicationModalButton.addSelectionListener (decorationButtonListener);
		systemModalButton.addSelectionListener (decorationButtonListener);
	
		/* Set the default state */
		noParentButton.setSelection (true);
		modelessButton.setSelection (true);
	}
	
	/**
	 * Handle a decoration button selection event.
	 *
	 * @param event org.eclipse.swt.events.SelectionEvent
	 */
	public void decorationButtonSelected(SelectionEvent event) {
	
		/* Make sure if the modal style is SWT.APPLICATION_MODAL or 
		 * SWT.SYSTEM_MODAL the style SWT.CLOSE is also selected.
		 * This is to make sure the user can close the shell.
		 */
		Button widget = (Button) event.widget;
		if (widget == applicationModalButton || widget == systemModalButton) {
			if (widget.getSelection()) {
				closeButton.setSelection (true);
				noTrimButton.setSelection (false);
			} 
			return;
		}
		if (widget == closeButton) {
			if (applicationModalButton.getSelection() || systemModalButton.getSelection()) {
				closeButton.setSelection (true);
			}
		}	
		/*
		 * Make sure if the No Trim button is selected then
		 * all other decoration buttons are deselected.
		 */
		if (widget.getSelection() && widget != noTrimButton) {
			noTrimButton.setSelection (false);
			return;
		}
		if (widget.getSelection() && widget == noTrimButton) {
			if (applicationModalButton.getSelection() || systemModalButton.getSelection()) {
				noTrimButton.setSelection (false);
				return;
			}
			closeButton.setSelection (false);
			titleButton.setSelection (false);
			minButton.setSelection (false);
			maxButton.setSelection (false);
			borderButton.setSelection (false);
			resizeButton.setSelection (false);
			return;
		}
	}
	
	/**
	 * Gets the text for the tab folder item.
	 */
	String getTabText () {
		return "Shell";
	}
}
