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

class ComboTab extends Tab {

	/* Example widgets and groups that contain them */
	Combo combo1;
	Group comboGroup;
	
	/* Style widgets added to the "Style" group */
	Button dropDownButton, readOnlyButton, simpleButton;
	
	static String [] ListData = {resControls.getString("ListData0_0"),
								 resControls.getString("ListData0_1"),
								 resControls.getString("ListData0_2"),
								 resControls.getString("ListData0_3"),
								 resControls.getString("ListData0_4"),
								 resControls.getString("ListData0_5"),
								 resControls.getString("ListData0_6"),
								 resControls.getString("ListData0_7"),
								 resControls.getString("ListData0_8")};
/**
* Creates the "Example" group.
*/
void createExampleGroup () {
	super.createExampleGroup ();
	
	/* Create a group for the combo box */
	comboGroup = new Group (exampleGroup, SWT.NULL);
	comboGroup.setLayout (new GridLayout ());
	comboGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	comboGroup.setText (resControls.getString("Combo"));
}
/**
* Creates the "Example" widgets.
*/
void createExampleWidgets () {
	
	/* Compute the widget style */
	int style = SWT.NONE;
	if (dropDownButton.getSelection ()) style |= SWT.DROP_DOWN;
	if (readOnlyButton.getSelection ()) style |= SWT.READ_ONLY;
	if (simpleButton.getSelection ()) style |= SWT.SIMPLE;
	if (borderButton.getSelection ()) style |= SWT.BORDER;
	
	/* Create the example widgets */
	combo1 = new Combo (comboGroup, style);
	combo1.setItems (ListData);
	if (ListData.length >= 3) {
		combo1.setText(ListData [2]);
	}

}
/**
* Creates the "Style" group.
*/
void createStyleGroup () {
	super.createStyleGroup ();

	/* Create the extra widgets */
	dropDownButton = new Button (styleGroup, SWT.RADIO);
	dropDownButton.setText (resControls.getString("SWT_DROP_DOWN"));
	simpleButton = new Button (styleGroup, SWT.RADIO);
	simpleButton.setText(resControls.getString("SWT_SIMPLE"));
	readOnlyButton = new Button (styleGroup, SWT.CHECK);
	readOnlyButton.setText (resControls.getString("SWT_READ_ONLY"));
	borderButton = new Button (styleGroup, SWT.CHECK);
	borderButton.setText (resControls.getString("SWT_BORDER"));
}
/**
* Gets the "Example" widget children.
*/
Control [] getExampleWidgets () {
	return new Control [] {combo1};
}
/**
* Gets the text for the tab folder item.
*/
String getTabText () {
	return resControls.getString("Combo");
}
/**
* Sets the state of the "Example" widgets.
*/
void setExampleWidgetState () {
	super.setExampleWidgetState ();
	dropDownButton.setSelection ((combo1.getStyle () & SWT.DROP_DOWN) != 0);
	simpleButton.setSelection ((combo1.getStyle () & SWT.SIMPLE) != 0);
	readOnlyButton.setSelection ((combo1.getStyle () & SWT.READ_ONLY) != 0);
	borderButton.setSelection ((combo1.getStyle () & SWT.BORDER) != 0);
}
}
