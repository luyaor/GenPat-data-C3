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
import java.util.ResourceBundle;

/**
* <code>Tab</code> is the abstract superclass of every page
* in the example's tab folder.  Each page in the tab folder
* describes a control.
*
* A Tab itself is not a control but instead provides a
* hierarchy with which to share code that is common to
* every page in the folder.
*
* A typical page in a Tab contains a two column composite.
* The left column contains the "Example" group.  The right
* column contains "Control" group.  The "Control" group
* contains controls that allow the user to interact with
* the example control.  The "Control" group typically
* contains a "Style", "Display" and "Size" group.  Subclasses
* can override these defaults to augment a group or stop
* a group from being created.
*/

abstract class Tab {

	protected static ResourceBundle resControls = ResourceBundle.getBundle("examples_control");
	
	/* Common control buttons */
	Button borderButton, enabledButton, visibleButton;
	Button preferredButton, tooSmallButton, smallButton, largeButton;

	/* Common groups and composites */
	Composite tabFolderPage;
	Group exampleGroup, controlGroup, displayGroup, sizeGroup, styleGroup;

	/* Sizing constants for the "Size" group */
	static final int TOO_SMALL_SIZE	= 10;
	static final int SMALL_SIZE		= 50;
	static final int LARGE_SIZE		= 100;
/**
* Creates the "Control" group.  The "Control" group
* is typically the right hand column in the tab.
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
	gridLayout.numColumns = 2;
	gridLayout.makeColumnsEqualWidth = true;
	controlGroup.setLayoutData (new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	controlGroup.setText (resControls.getString("Parameters"));

	/* Create individual groups inside the "Control" group */
	createStyleGroup ();
	createDisplayGroup ();
	createSizeGroup ();

	/*
	* For each Button child in the style group, add a selection
	* listener that will recreate the example controls.  If the
	* style group button is a RADIO button, ensure that the radio
	* button is selected before recreating the example controls.
	* When the user selects a RADIO button, the curreont RADIO
	* button in the group is deselected and the new RADIO button
	* is selected automatically.  The listeners are notified for
	* both these operations but typically only do work when a RADIO
	* button is selected.
	*/
	SelectionListener selectionListener = new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			if ((event.widget.getStyle () & SWT.RADIO) != 0) {
				if (!((Button) event.widget).getSelection ()) return;
			}
			recreateExampleWidgets ();
		};
	};
	Control [] children = styleGroup.getChildren ();
	for (int i=0; i<children.length; i++) {
		if (children [i] instanceof Button) {
			Button button = (Button) children [i];
			button.addSelectionListener (selectionListener);
		}
	}
}
/**
* Creates the "Control" widget children.
* Subclasses override this method to augment
* the standard controls created in the "Style",
* "Display" and "Size" groups.
*/
void createControlWidgets () {
}
/**
* Creates the "Display" group.  This is typically
* a child of the "Control" group.
*/
void createDisplayGroup () {

	/* Create the group */
	displayGroup = new Group (controlGroup, SWT.NONE);
	displayGroup.setLayout (new GridLayout ());
	displayGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	displayGroup.setText (resControls.getString("State"));

	/* Create the controls */
	enabledButton = new Button(displayGroup, SWT.CHECK);
	enabledButton.setText(resControls.getString("Enabled"));
	visibleButton = new Button(displayGroup, SWT.CHECK);
	visibleButton.setText(resControls.getString("Visible"));

	/* Add the listeners */
	enabledButton.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			setExampleWidgetEnabled ();
		}
	});
	visibleButton.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			setExampleWidgetVisibility ();
		}
	});

	/* Set the default state */
	enabledButton.setSelection(true);
	visibleButton.setSelection(true);
}
/**
* Creates the "Example" group.  The "Example" group
* is typically the left hand column in the tab.
*/
void createExampleGroup () {	
	/*
	* Create the example group.  This is the
	* group on the right half of each example
	* tab.
	*/	
	exampleGroup = new Group (tabFolderPage, SWT.NONE);
	GridLayout gridLayout = new GridLayout ();
	exampleGroup.setLayout (gridLayout);
	exampleGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
}
/**
* Creates the "Example" widget children of the "Example" group.
* Subclasses override this method to create the particular
* example control.
*/
void createExampleWidgets () {
	/* Do nothing */
}
/**
* Creates the "Size" group.  The "Size" group contains
* controls that allow the user to change the size of
* the example widgets.
*/
void createSizeGroup () {

	/* Create the group */
	sizeGroup = new Group (controlGroup, SWT.NONE);
	sizeGroup.setLayout (new GridLayout());
	sizeGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	sizeGroup.setText (resControls.getString("Size"));

	/* Create the controls */

	/*
	* The preferred size of a widget is the size returned
	* by widget.computeSize (SWT.DEFAULT, SWT.DEFAULT).
	* This size is defined on a widget by widget basis.
	* Many widgets will attempt to display their contents.
	*/
	preferredButton = new Button (sizeGroup, SWT.RADIO);
	preferredButton.setText (resControls.getString("Preferred"));
	tooSmallButton = new Button (sizeGroup, SWT.RADIO);
	tooSmallButton.setText (TOO_SMALL_SIZE + " X " + TOO_SMALL_SIZE);
	smallButton = new Button(sizeGroup, SWT.RADIO);
	smallButton.setText (SMALL_SIZE + " X " + SMALL_SIZE);
	largeButton = new Button (sizeGroup, SWT.RADIO);
	largeButton.setText (LARGE_SIZE + " X " + LARGE_SIZE);

	/* Add the listeners */
	SelectionAdapter selectionListener = new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			if (!((Button) event.widget).getSelection ()) return;
			setExampleWidgetSize ();
		};
	};
	preferredButton.addSelectionListener(selectionListener);
	tooSmallButton.addSelectionListener(selectionListener);
	smallButton.addSelectionListener(selectionListener);
	largeButton.addSelectionListener(selectionListener);

	/* Set the default state */
	preferredButton.setSelection (true);
}
/**
* Creates the "Style" group.  The "Style" group contains
* controls that allow the user to change the style of
* the example widgets.  Changing a widget "Style" causes
* the widget to be destroyed and recreated.
*/
void createStyleGroup () {
	styleGroup = new Group (controlGroup, SWT.NONE);
	styleGroup.setLayout (new GridLayout ());
	styleGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	styleGroup.setText (resControls.getString("Styles"));
}
/**
* Creates the tab folder page.
*
* @param tabFolder org.eclipse.swt.widgets.TabFolder
* @return the new page for the tab folder
*/
Composite createTabFolderPage (TabFolder tabFolder) {
	/*
	* Create a two column page.
	*/
	tabFolderPage = new Composite (tabFolder, SWT.NULL);
	GridLayout gridLayout = new GridLayout ();
	tabFolderPage.setLayout (gridLayout);
	gridLayout.numColumns = 2;

	/* Create the "Example" and "Control" columns */
	createExampleGroup ();
	createControlGroup ();

	/* Create the widgets in the two columns */
	createExampleWidgets ();
	createControlWidgets ();
	setExampleWidgetState ();
	
	return tabFolderPage;
}
/**
* Disposes the "Example" widgets.
*/
void disposeExampleWidgets () {
	Control [] controls = getExampleWidgets ();
	for (int i=0; i<controls.length; i++) {
		controls [i].dispose ();
	}
}
/**
* Gets the "Example" widget children.
*
* @return an array of example widget children
*/
Control [] getExampleWidgets () {
	return new Control [0];
}
/**
* Gets the text for the tab folder item.
*
* @return the text for the tab item
*/
String getTabText () {
	return "";
}
/**
* Recreates the "Example" widgets.
*/
void recreateExampleWidgets () {
	disposeExampleWidgets ();
	createExampleWidgets ();
	setExampleWidgetState ();
}
/**
* Sets the enabled state of the "Example" widgets.
*/
void setExampleWidgetEnabled () {
	Control [] controls = getExampleWidgets ();
	for (int i=0; i<controls.length; i++) {
		controls [i].setEnabled (enabledButton.getSelection ());
	}
}
/**
* Sets the size of the "Example" widgets.
*/
void setExampleWidgetSize () {
	int size = SWT.DEFAULT;
	if (preferredButton == null) return;
	if (preferredButton.getSelection()) size = SWT.DEFAULT;
	if (tooSmallButton.getSelection()) size = TOO_SMALL_SIZE;
	if (smallButton.getSelection()) size = SMALL_SIZE;
	if (largeButton.getSelection()) size = LARGE_SIZE;
	Control [] controls = getExampleWidgets ();
	for (int i=0; i<controls.length; i++) {
		GridData gridData = new GridData ();
		gridData.widthHint = size;
		gridData.heightHint = size;
		controls [i].setLayoutData (gridData);
	}
	/*
	* Force the entire widget tree to layout,
	* even when the child sizes nay not have
	* changed.
	*/
	int seenCount = 0;
	Composite [] seen = new Composite [4];
	for (int i=0; i<controls.length; i++) {
		Control control = controls [i];
		while (control != exampleGroup) {
			Composite parent = control.getParent ();
			int index = 0;
			while (index < seenCount) {
				if (seen [index] == parent) break;
				index++;
			}
			if (index == seenCount) parent.layout ();
			if (seenCount == seen.length) {
				Composite [] newSeen = new Composite [seen.length + 4];
				System.arraycopy (seen, 0, newSeen, 0, seen.length);
				seen = newSeen;
			}
			seen [seenCount++] = parent;
			control = control.getParent ();
		}
	}
}
/**
* Sets the state of the "Example" widgets.  Subclasses
* reimplement this method to set "Example" widget state
* that is specific to the widget.
*/
void setExampleWidgetState () {
	setExampleWidgetEnabled ();
	setExampleWidgetVisibility ();
	setExampleWidgetSize ();
}
/**
* Sets the visibility of the "Example" widgets.
*/
void setExampleWidgetVisibility () {
	Control [] controls = getExampleWidgets ();
	for (int i=0; i<controls.length; i++) {
		controls [i].setVisible (visibleButton.getSelection ());
	}
}
}
