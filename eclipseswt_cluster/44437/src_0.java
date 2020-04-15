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
import org.eclipse.swt.custom.*;

class TextTab extends ScrollableTab {

	/* Example widgets and groups that contain them */
	Text text;
	StyledText richText;
	Group textGroup, richTextGroup;

	/* Style widgets added to the "Style" group */
	Button readOnlyButton;
/**
* Creates the "Example" group.
*/
void createExampleGroup () {
	super.createExampleGroup ();
	
	/* Create a group for the text widget */
	textGroup = new Group (exampleGroup, SWT.NULL);
	textGroup.setLayout (new GridLayout ());
	textGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	textGroup.setText (resControls.getString("Text"));

	/* Create a group for the rich text widget */
	richTextGroup = new Group (exampleGroup, SWT.NULL);
	richTextGroup.setLayout (new GridLayout ());
	richTextGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	richTextGroup.setText (resControls.getString("RichText"));
}
/**
* Creates the "Example" widgets.
*/
void createExampleWidgets () {
	
	/* Compute the widget style */
	int style = SWT.NONE;
	if (singleButton.getSelection ()) style |= SWT.SINGLE;
	if (multiButton.getSelection ()) style |= SWT.MULTI;
	if (horizontalButton.getSelection ()) style |= SWT.H_SCROLL;
	if (verticalButton.getSelection ()) style |= SWT.V_SCROLL;
	if (readOnlyButton.getSelection ()) style |= SWT.READ_ONLY;
	if (borderButton.getSelection ()) style |= SWT.BORDER;

	/* Create the example widgets */
	text = new Text (textGroup, style);
	text.setText (resControls.getString("Example_string"));
	text.append (text.DELIMITER);
	text.append (resControls.getString("One_Two_Three"));
	richText = new StyledText (richTextGroup, style);
	richText.setText (resControls.getString("Example_string"));
	richText.append ("\n");
	richText.append (resControls.getString("One_Two_Three"));
}
/**
* Creates the "Style" group.
*/
void createStyleGroup() {
	super.createStyleGroup();

	/* Create the extra widgets */
	readOnlyButton = new Button (styleGroup, SWT.CHECK);
	readOnlyButton.setText (resControls.getString("SWT_READ_ONLY"));
}
/**
* Gets the "Example" widget children.
*/
Control [] getExampleWidgets () {
	return new Control [] {text, richText};
}
/**
* Gets the text for the tab folder item.
*/
String getTabText () {
	return resControls.getString("Text");
}
/**
* Sets the state of the "Example" widgets.
*/
void setExampleWidgetState () {
	super.setExampleWidgetState ();
	readOnlyButton.setSelection ((text.getStyle () & SWT.READ_ONLY) != 0);
}

}
