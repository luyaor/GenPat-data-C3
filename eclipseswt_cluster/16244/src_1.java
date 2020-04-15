package org.eclipse.swt.examples.controlexample;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;

class ShellTab extends Tab {
	
	/* Style widgets added to the "Style" group */
	Button noParentButton, parentButton;
	Button noTrimButton, closeButton, titleButton, minButton, maxButton, borderButton, resizeButton;
	Button createButton, closeAllButton;
	Group parentStyleGroup;

	/* Variables used to track the open shells */
	int shellCount = 0;
	Shell [] shells = new Shell [4];
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

	/* Create the shell with or without a parent */
	if (noParentButton.getSelection ()) {
		shells [shellCount] = new Shell (style);
	} else {
		Shell shell = tabFolderPage.getShell ();
		shells [shellCount] = new Shell (shell, style);
	}

	/* Set the size, title and open the shell */
	shells [shellCount].setSize (300, 100);
	shells [shellCount].setText (resControls.getString("Title") + shellCount);
	shells [shellCount++].open ();
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
	controlGroup = new Group (tabFolderPage, SWT.NULL);
	GridLayout gridLayout= new GridLayout ();
	controlGroup.setLayout (gridLayout);
	gridLayout.numColumns = 1;
	gridLayout.makeColumnsEqualWidth = true;
	controlGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	controlGroup.setText (resControls.getString("Parameters"));

	/* Create individual groups inside the "Control" group */
	styleGroup = new Group (controlGroup, SWT.NULL);
	gridLayout = new GridLayout ();
	styleGroup.setLayout (gridLayout);
	gridLayout.numColumns = 2;
	styleGroup.setLayoutData (new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	styleGroup.setText (resControls.getString("Styles"));

	/* Create a group for the parent sytle controls */
	parentStyleGroup = new Group (styleGroup, SWT.NULL);
	parentStyleGroup.setLayout (new GridLayout ());
	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
	gridData.horizontalSpan = 2;
	parentStyleGroup.setLayoutData (gridData);
	parentStyleGroup.setText (resControls.getString("Parent"));
}
/**
* Creates the "Control" widget children.
*/
void createControlWidgets () {

	/* Create the parent style buttons */
	noParentButton = new Button (parentStyleGroup, SWT.RADIO);
	noParentButton.setText (resControls.getString("No_Parent"));
	parentButton = new Button (parentStyleGroup, SWT.RADIO);
	parentButton.setText (resControls.getString("Parent"));

	/* Create a group for the decoration style controls */
	Group decorationStyleGroup = new Group(styleGroup, SWT.NULL);
	decorationStyleGroup.setLayout (new GridLayout ());
	GridData gridData = new GridData (GridData.HORIZONTAL_ALIGN_CENTER);
	gridData.horizontalSpan = 2;
	decorationStyleGroup.setLayoutData (gridData);
	decorationStyleGroup.setText (resControls.getString("Decoration_Styles"));

	/* Create the decoration style buttons */
	noTrimButton = new Button (decorationStyleGroup, SWT.CHECK);
	noTrimButton.setText (resControls.getString("SWT_NO_TRIM"));
	closeButton = new Button (decorationStyleGroup, SWT.CHECK);
	closeButton.setText (resControls.getString("SWT_CLOSE"));
	titleButton = new Button (decorationStyleGroup, SWT.CHECK);
	titleButton.setText (resControls.getString("SWT_TITLE"));
	minButton = new Button (decorationStyleGroup, SWT.CHECK);
	minButton.setText (resControls.getString("SWT_MIN"));
	maxButton = new Button (decorationStyleGroup, SWT.CHECK);
	maxButton.setText (resControls.getString("SWT_MAX"));
	borderButton = new Button (decorationStyleGroup, SWT.CHECK);
	borderButton.setText (resControls.getString("SWT_BORDER"));
	resizeButton = new Button (decorationStyleGroup, SWT.CHECK);
	resizeButton.setText (resControls.getString("SWT_RESIZE"));

	/* Create the "create" and "closeAll" buttons */
	createButton = new Button (styleGroup, SWT.NULL);
	gridData = new GridData (GridData.HORIZONTAL_ALIGN_CENTER);
	createButton.setLayoutData (gridData);
	createButton.setText (resControls.getString("Create_Shell"));
	closeAllButton = new Button (styleGroup, SWT.NULL);
	closeAllButton.setText (resControls.getString("Close_All_Shells"));
	closeAllButton.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_CENTER));

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

	/* Set the default state */
	noParentButton.setSelection (true);
}
/**
* Handle a decoration button selection event.
*
* @param event org.eclipse.swt.events.SelectionEvent
*/
public void decorationButtonSelected(SelectionEvent event) {

	/*
	* Make sure if the No Trim button is selected then
	* all other decoration buttons are deselected.
	*/
	Button widget = (Button) event.widget;
	if (widget.getSelection() && widget != noTrimButton) {
		noTrimButton.setSelection (false);
		return;
	}
	if (widget.getSelection() && widget == noTrimButton) {
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
	return resControls.getString("Shell");
}
}
