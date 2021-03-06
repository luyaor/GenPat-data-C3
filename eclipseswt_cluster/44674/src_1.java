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

class DialogTab extends Tab {

	/* Example widgets and groups that contain them */
	Group dialogStyleGroup, resultGroup;
	Text textWidget;
	
	/* Style widgets added to the "Style" group */
	Combo dialogCombo;
	Button okButton, cancelButton;
	Button yesButton, noButton;
	Button retryButton;
	Button abortButton, ignoreButton;
	Button iconErrorButton, iconInformationButton, iconQuestionButton;
	Button iconWarningButton, iconWorkingButton;
	Button modelessButton, primaryModalButton, applicationModalButton, systemModalButton;
	Button saveButton, openButton;

	static String [] FilterExtensions	= {".txt.", ".bat", ".doc"};
	static String [] FilterNames		= {resControls.getString("FilterName_0"),
										   resControls.getString("FilterName_1"),
										   resControls.getString("FilterName_2")};

/**
* Handle a button style selection event.
*
* @param event the selection event
*/
void buttonStyleSelected(SelectionEvent event) {
	/*
	* Only certain combinations of button styles are
	* supported for various dialogs.  Make sure the
	* control widget reflects only valid combinations.
	*/
	okButton.setEnabled (
		!(yesButton.getSelection () || noButton.getSelection () || 
			retryButton.getSelection () || abortButton.getSelection () ||
				ignoreButton.getSelection ()));
	cancelButton.setEnabled (
		!(abortButton.getSelection () || ignoreButton.getSelection ()));
	yesButton.setEnabled (
		!(okButton.getSelection () || retryButton.getSelection () ||
			abortButton.getSelection () || ignoreButton.getSelection ()));
	noButton.setEnabled (
		!(okButton.getSelection () || retryButton.getSelection () ||
			abortButton.getSelection () || ignoreButton.getSelection ()));
	retryButton.setEnabled (
		!(okButton.getSelection() || yesButton.getSelection() || noButton.getSelection ()));
	abortButton.setEnabled (
		!(okButton.getSelection () || cancelButton.getSelection () ||
			yesButton.getSelection () || noButton.getSelection ()));
	ignoreButton.setEnabled (
		!(okButton.getSelection () || cancelButton.getSelection () |
			yesButton.getSelection () || noButton.getSelection ()));
}
/**
 * Handle the create button selection event.
 *
 * @param event org.eclipse.swt.events.SelectionEvent
 */
void createButtonSelected(SelectionEvent event) {

	/* Compute the appropriate dialog style */
	int style = SWT.NULL;
	if (okButton.getEnabled () && okButton.getSelection ()) style |= SWT.OK;
	if (cancelButton.getEnabled () && cancelButton.getSelection ()) style |= SWT.CANCEL;
	if (yesButton.getEnabled () && yesButton.getSelection ()) style |= SWT.YES;
	if (noButton.getEnabled () && noButton.getSelection ()) style |= SWT.NO;
	if (retryButton.getEnabled () && retryButton.getSelection ()) style |= SWT.RETRY;
	if (abortButton.getEnabled () && abortButton.getSelection ()) style |= SWT.ABORT;
	if (ignoreButton.getEnabled () && ignoreButton.getSelection ()) style |= SWT.IGNORE;
	if (iconErrorButton.getEnabled () && iconErrorButton.getSelection ()) style |= SWT.ICON_ERROR;
	if (iconInformationButton.getEnabled () && iconInformationButton.getSelection ()) style |= SWT.ICON_INFORMATION;
	if (iconQuestionButton.getEnabled () && iconQuestionButton.getSelection ()) style |= SWT.ICON_QUESTION;
	if (iconWarningButton.getEnabled () && iconWarningButton.getSelection ()) style |= SWT.ICON_WARNING;
	if (iconWorkingButton.getEnabled () && iconWorkingButton.getSelection ()) style |= SWT.ICON_WORKING;
	if (primaryModalButton.getEnabled () && primaryModalButton.getSelection ()) style |= SWT.PRIMARY_MODAL;
	if (applicationModalButton.getEnabled () && applicationModalButton.getSelection ()) style |= SWT.APPLICATION_MODAL;
	if (systemModalButton.getEnabled () && systemModalButton.getSelection ()) style |= SWT.SYSTEM_MODAL;
	if (saveButton.getEnabled () && saveButton.getSelection ()) style |= SWT.SAVE;
	if (openButton.getEnabled () && openButton.getSelection ()) style |= SWT.OPEN;

	/* Open the appropriate dialog type */
	String name = dialogCombo.getText ();
	Shell shell = tabFolderPage.getShell ();
	
	if (name.equals (resControls.getString("ColorDialog"))) {
		ColorDialog dialog = new ColorDialog (shell ,style);
		dialog.setRGB (new RGB (100, 100, 100));
		dialog.setText (resControls.getString("Title"));
		RGB result = dialog.open ();
		textWidget.append (resControls.getString("ColorDialog") + Text.DELIMITER);
		textWidget.append (resControls.getString("Result") + " " + result + Text.DELIMITER + Text.DELIMITER);
		return;
	}
	
	if (name.equals (resControls.getString("DirectoryDialog"))) {
		DirectoryDialog dialog = new DirectoryDialog (shell, style);
		dialog.setMessage (resControls.getString("Example_string"));
		dialog.setText (resControls.getString("Title"));
		String result = dialog.open ();
		textWidget.append (resControls.getString("DirectoryDialog") + Text.DELIMITER);
		textWidget.append (resControls.getString("Result") + " " + result + Text.DELIMITER + Text.DELIMITER);
		return;
	}
	
	if (name.equals (resControls.getString("FileDialog"))) {
		FileDialog dialog = new FileDialog (shell, style);
		dialog.setFileName (resControls.getString("readme_txt"));
		dialog.setFilterNames (FilterNames);
		dialog.setFilterExtensions (FilterExtensions);
		dialog.setText (resControls.getString("Title"));
		String result = dialog.open();
		textWidget.append (resControls.getString("FileDialog") + Text.DELIMITER);
		textWidget.append (resControls.getString("Result") + " " + result + Text.DELIMITER + Text.DELIMITER);
		return;
	}
	
	if (name.equals (resControls.getString("FontDialog"))) {
		FontDialog dialog = new FontDialog (shell, style);
		dialog.setText (resControls.getString("Title"));
		FontData result = dialog.open ();
		textWidget.append (resControls.getString("FontDialog") + Text.DELIMITER);
		textWidget.append (resControls.getString("Result") + " " + result + Text.DELIMITER + Text.DELIMITER);
		return;
	}

	if (name.equals(resControls.getString("MessageBox"))) {
		MessageBox dialog = new MessageBox (shell, style);
		dialog.setMessage (resControls.getString("Example_string"));
		dialog.setText (resControls.getString("Title"));
		int result = dialog.open ();
		textWidget.append (resControls.getString("MessageBox") + Text.DELIMITER);
		/*
		* The resulting integer depends on the original
		* dialog style.  Decode the result and display it.
		*/
		switch (result) {
			case SWT.OK:
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_OK"));
				break;
			case SWT.YES:
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_YES"));
				break;
			case SWT.NO:
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_NO"));
				break;
			case SWT.CANCEL:
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_CANCEL"));
				break;
			case SWT.ABORT: 
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_ABORT"));
				break;
			case SWT.RETRY:
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_RETRY"));
				break;
			case SWT.IGNORE:
				textWidget.append (resControls.getString("Result") + resControls.getString("SWT_IGNORE"));
				break;
			default:
				textWidget.append(resControls.getString("Result") + result);
				break;
		}
		textWidget.append (Text.DELIMITER + Text.DELIMITER);
	}
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
	controlGroup.setLayout(gridLayout);
	gridLayout.numColumns = 2;
	gridLayout.makeColumnsEqualWidth = true;
	controlGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	controlGroup.setText (resControls.getString("Parameters"));
	
	/*
	* Create a group to hold the dialog style combo box and
	* create dialog button.
	*/
	dialogStyleGroup = new Group (controlGroup, SWT.NULL);
	dialogStyleGroup.setLayout (new GridLayout ());
	GridData gridData = new GridData (GridData.HORIZONTAL_ALIGN_CENTER);
	gridData.horizontalSpan = 2;
	dialogStyleGroup.setLayoutData (gridData);
	dialogStyleGroup.setText (resControls.getString("Dialog_Type"));
}
/**
* Creates the "Control" widget children.
*/
void createControlWidgets () {

	/* Create the combo */
	String [] strings = {
		resControls.getString("ColorDialog"), 
		resControls.getString("DirectoryDialog"),
		resControls.getString("FileDialog"),
		resControls.getString("FontDialog"),
		resControls.getString("MessageBox"),
	};
	dialogCombo = new Combo (dialogStyleGroup, SWT.READ_ONLY);
	dialogCombo.setItems (strings);
	dialogCombo.setText (strings [0]);

	/* Create the create dialog button */
	Button createButton = new Button(dialogStyleGroup, SWT.NULL);
	createButton.setText (resControls.getString("Create_Dialog"));
	createButton.setLayoutData (new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

	/* Create a group for the various dialog button style controls */
	Group buttonStyleGroup = new Group (controlGroup, SWT.NULL);
	buttonStyleGroup.setLayout (new GridLayout ());
	buttonStyleGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	buttonStyleGroup.setText (resControls.getString("Button_Styles"));

	/* Create the button style buttons */
	okButton = new Button (buttonStyleGroup, SWT.CHECK);
	okButton.setText (resControls.getString("SWT_OK"));
	cancelButton = new Button (buttonStyleGroup, SWT.CHECK);
	cancelButton.setText (resControls.getString("SWT_CANCEL"));
	yesButton = new Button (buttonStyleGroup, SWT.CHECK);
	yesButton.setText (resControls.getString("SWT_YES"));
	noButton = new Button (buttonStyleGroup, SWT.CHECK);
	noButton.setText (resControls.getString("SWT_NO"));
	retryButton = new Button (buttonStyleGroup, SWT.CHECK);
	retryButton.setText (resControls.getString("SWT_RETRY"));
	abortButton = new Button (buttonStyleGroup, SWT.CHECK);
	abortButton.setText (resControls.getString("SWT_ABORT"));
	ignoreButton = new Button (buttonStyleGroup, SWT.CHECK);
	ignoreButton.setText (resControls.getString("SWT_IGNORE"));

	/* Create a group for the icon style controls */
	Group iconStyleGroup = new Group (controlGroup, SWT.NULL);
	iconStyleGroup.setLayout (new GridLayout ());
	iconStyleGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	iconStyleGroup.setText (resControls.getString("Icon_Styles"));

	/* Create the icon style buttons */
	iconErrorButton = new Button (iconStyleGroup, SWT.RADIO);
	iconErrorButton.setText (resControls.getString("SWT_ICON_ERROR"));
	iconInformationButton = new Button (iconStyleGroup, SWT.RADIO);
	iconInformationButton.setText (resControls.getString("SWT_ICON_INFORMATION"));
	iconQuestionButton = new Button (iconStyleGroup, SWT.RADIO);
	iconQuestionButton.setText (resControls.getString("SWT_ICON_QUESTION"));
	iconWarningButton = new Button (iconStyleGroup, SWT.RADIO);
	iconWarningButton.setText (resControls.getString("SWT_ICON_WARNING"));
	iconWorkingButton = new Button (iconStyleGroup, SWT.RADIO);
	iconWorkingButton.setText (resControls.getString("SWT_ICON_WORKING"));

	/* Create a group for the modal style controls */
	Group modalStyleGroup = new Group (controlGroup, SWT.NULL);
	modalStyleGroup.setLayout (new GridLayout ());
	modalStyleGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	modalStyleGroup.setText (resControls.getString("Modal_Styles"));

	/* Create the modal style buttons */
	modelessButton = new Button (modalStyleGroup, SWT.RADIO);
	modelessButton.setText (resControls.getString("SWT_MODELESS"));
	primaryModalButton = new Button (modalStyleGroup, SWT.RADIO);
	primaryModalButton.setText (resControls.getString("SWT_PRIMARY_MODAL"));
	applicationModalButton = new Button (modalStyleGroup, SWT.RADIO);
	applicationModalButton.setText (resControls.getString("SWT_APPLICATION_MODAL"));
	systemModalButton = new Button (modalStyleGroup, SWT.RADIO);
	systemModalButton.setText (resControls.getString("SWT_SYSTEM_MODAL"));

	/* Create a group for the file dialog style controls */
	Group fileDialogStyleGroup = new Group (controlGroup, SWT.NULL);
	fileDialogStyleGroup.setLayout (new GridLayout ());
	fileDialogStyleGroup.setLayoutData (new GridData (GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	fileDialogStyleGroup.setText (resControls.getString("File_Dialog_Styles"));

	/* Create the file dialog style buttons */
	saveButton = new Button (fileDialogStyleGroup, SWT.RADIO);
	saveButton.setText (resControls.getString("SWT_SAVE"));
	openButton = new Button(fileDialogStyleGroup, SWT.RADIO);
	openButton.setText(resControls.getString("SWT_OPEN"));

	/* Add the listeners */
	dialogCombo.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			dialogSelected (event);
		};
	});
	createButton.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			createButtonSelected (event);
		};
	});
	SelectionListener buttonStyleListener = new SelectionAdapter () {
		public void widgetSelected (SelectionEvent event) {
			buttonStyleSelected (event);
		};
	};
	okButton.addSelectionListener (buttonStyleListener);
	cancelButton.addSelectionListener (buttonStyleListener);
	yesButton.addSelectionListener (buttonStyleListener);
	noButton.addSelectionListener (buttonStyleListener);
	retryButton.addSelectionListener (buttonStyleListener);
	abortButton.addSelectionListener (buttonStyleListener);
	ignoreButton.addSelectionListener (buttonStyleListener);

	/* Set default values for style buttons */
	okButton.setEnabled (false);
	cancelButton.setEnabled (false);
	yesButton.setEnabled (false);
	noButton.setEnabled (false);
	retryButton.setEnabled (false);
	abortButton.setEnabled (false);
	ignoreButton.setEnabled (false);
	iconErrorButton.setEnabled (false);
	iconInformationButton.setEnabled (false);
	iconQuestionButton.setEnabled (false);
	iconWarningButton.setEnabled (false);
	iconWorkingButton.setEnabled (false);
	saveButton.setEnabled (false);
	openButton.setEnabled (false);
	openButton.setSelection (true);
	iconInformationButton.setSelection (true);
	modelessButton.setSelection (true);
}
/**
* Creates the "Example" group.
*/
void createExampleGroup () {
	super.createExampleGroup ();
	
	/*
	* Create a group for the text widget to display
	* the results returned by the example dialogs.
	*/
	resultGroup = new Group (exampleGroup, SWT.NULL);
	resultGroup.setLayout (new GridLayout ());
	resultGroup.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
	resultGroup.setText (resControls.getString("Dialog_Result"));
}
/**
* Creates the "Example" widgets.
*/
void createExampleWidgets () {
	/*
	* Create a multi lined, scrolled text widget for output.
	*/
	textWidget = new Text(resultGroup, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	GridData gridData = new GridData ();
	gridData.widthHint = 300;
	gridData.heightHint = 400;
	textWidget.setLayoutData (gridData);	
}
/**
* Handle a dialog type combo selection event.
*
* @param event the selection event
*/
void dialogSelected (SelectionEvent event) {

	/* Enable/Disable the buttons */
	String name = dialogCombo.getText ();
	boolean isMessageBox = name.equals (resControls.getString("MessageBox"));
	boolean isFileDialog = name.equals (resControls.getString("FileDialog"));
	okButton.setEnabled (isMessageBox);
	cancelButton.setEnabled (isMessageBox);
	yesButton.setEnabled (isMessageBox);
	noButton.setEnabled (isMessageBox);
	retryButton.setEnabled (isMessageBox);
	abortButton.setEnabled (isMessageBox);
	ignoreButton.setEnabled (isMessageBox);
	iconErrorButton.setEnabled (isMessageBox);
	iconInformationButton.setEnabled (isMessageBox);
	iconQuestionButton.setEnabled (isMessageBox);
	iconWarningButton.setEnabled (isMessageBox);
	iconWorkingButton.setEnabled  (isMessageBox);
	saveButton.setEnabled (isFileDialog);
	openButton.setEnabled (isFileDialog);

	/* Unselect the buttons */
	if (!isMessageBox) {
		okButton.setSelection (false);
		cancelButton.setSelection (false);
		yesButton.setSelection (false);
		noButton.setSelection (false);
		retryButton.setSelection (false);
		abortButton.setSelection (false);
		ignoreButton.setSelection (false);
	}
}
/**
* Gets the "Example" widget children.
*/
Control [] getExampleWidgets () {
	return new Control [0];
}
/**
* Gets the text for the tab folder item.
*/
String getTabText () {
	return resControls.getString("Dialog");
}
}
