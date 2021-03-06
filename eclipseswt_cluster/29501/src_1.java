package org.eclipse.swt.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
*
* A ControlEditor is a manager for a Control that appears above a composite and tracks with the
* moving and resizing of that composite.  It can be used to display one control above 
* another control.  This could be used when editing a control that does not have editing 
* capabilities by using a text editor or for launching a dialog by placing a button 
* above a control.
*
* <p> Here is an example of using a ControlEditor:
*
* <code><pre>
* Canvas canvas = new Canvas(shell, SWT.BORDER);
* canvas.setBounds(10, 10, 300, 300);	
* Color color = new Color(null, 255, 0, 0);
* canvas.setBackground(color);
* ControlEditor editor = new ControlEditor (canvas);
* // The editor will be a button in the bottom right corner of the canvas.
* // When selected, it will launch a Color dialog that will change the background 
* // of the canvas.
* Button button = new Button(canvas, SWT.PUSH);
* button.setText("Select Color...");
* button.addSelectionListener (new SelectionAdapter() {
* 	public void widgetSelected(SelectionEvent e) {
* 		ColorDialog dialog = new ColorDialog(shell);
* 		dialog.open();
* 		RGB rgb = dialog.getRGB();
* 		if (rgb != null) {
* 			if (color != null) color.dispose();
* 			color = new Color(null, rgb);
* 			canvas.setBackground(color);
* 		}
* 		
* 	}
* });
*
* editor.horizontalAlignment = SWT.RIGHT;
* editor.verticalAlignment = SWT.BOTTOM;
* editor.grabHorizontal = false;
* editor.grabVertical = false;
* Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
* editor.minimumWidth = size.x;
* editor.minimumHeight = size.y;
* editor.setEditor (button);
* </pre></code>
*/
public class ControlEditor {

	/**
	* Specifies how the editor should be aligned relative to the control.  Allowed values
	* are SWT.LEFT, SWT.RIGHT and SWT.CENTER.  The default value is SWT.CENTER.
	*/
	public int horizontalAlignment = SWT.CENTER;
	
	/**
	* Specifies whether the editor should be sized to use the entire width of the control.
	* True means resize the editor to the same width as the cell.  False means do not adjust 
	* the width of the editor.	The default value is false.
	*/
	public boolean grabHorizontal = false;
	
	/**
	* Specifies the minimum width the editor can have.  This is used in association with
	* a true value of grabHorizontal.  If the cell becomes smaller than the minimumWidth, the 
	* editor will not made smaller than the minumum width value.  The default value is 0.
	*/
	public int minimumWidth = 0;
	
	/**
	* Specifies how the editor should be aligned relative to the control.  Allowed values
	* are SWT.TOP, SWT.BOTTOM and SWT.CENTER.  The default value is SWT.CENTER.
	*/
	public int verticalAlignment = SWT.CENTER;
	
	/**
	* Specifies whether the editor should be sized to use the entire height of the control.
	* True means resize the editor to the same height as the underlying control.  False means do not adjust 
	* the height of the editor.	The default value is false.
	*/
	public boolean grabVertical = false;
	
	/**
	* Specifies the minimum height the editor can have.  This is used in association with
	* a true value of grabVertical.  If the control becomes smaller than the minimumHeight, the 
	* editor will not made smaller than the minumum height value.  The default value is 0.
	*/
	public int minimumHeight = 0;

	Composite parent;
	Control editor;
	private boolean hadFocus;
	private Listener internalListener;
/**
* Creates a ControlEditor for the specified Composite.
*
* @param parent the Composite above which this editor will be displayed
*
*/
public ControlEditor (Composite parent) {
	this.parent = parent;

	internalListener = new Listener() {
		public void handleEvent(Event e) {
			if (e.widget instanceof ScrollBar && e.type == SWT.Selection)
				scroll (e);
			else if (e.type == SWT.Resize)
				resize ();
		}
	};
	
	parent.addListener (SWT.Resize, internalListener);

	ScrollBar hBar = parent.getHorizontalBar ();
	if (hBar != null) hBar.addListener (SWT.Selection, internalListener);
	ScrollBar vBar = parent.getVerticalBar ();
	if (vBar != null) vBar.addListener (SWT.Selection, internalListener);
}
Rectangle computeBounds (){
	Rectangle clientArea = parent.getClientArea();
	Rectangle editorRect = new Rectangle(clientArea.x, clientArea.y, minimumWidth, minimumHeight);
	
	if (grabHorizontal)
		editorRect.width = Math.max(clientArea.width, minimumWidth);
	
	if (grabVertical)
		editorRect.height = Math.max(clientArea.height, minimumHeight);

	switch (horizontalAlignment) {
		case SWT.RIGHT:
			editorRect.x += clientArea.width - editorRect.width;
			break;
		case SWT.LEFT:
			// do nothing - clientArea.x is the right answer
			break;
		default:
			// default is CENTER
			editorRect.x += (clientArea.width - editorRect.width)/2;
	}
	
	switch (verticalAlignment) {
		case SWT.BOTTOM:
			editorRect.y += clientArea.height - editorRect.height;
			break;
		case SWT.TOP:
			// do nothing - clientArea.y is the right answer
			break;
		default :
			// default is CENTER
			editorRect.y += (clientArea.height - editorRect.height)/2;
	}

	
	return editorRect;

}
/**
 * Removes all associations between the Editor and the underlying composite.  The
 * composite and the editor Control are <b>not</b> disposed.
 */
public void dispose () {
	if (!parent.isDisposed()) {
		parent.removeListener (SWT.Resize, internalListener);
		ScrollBar hBar = parent.getHorizontalBar ();
		if (hBar != null) hBar.removeListener (SWT.Selection, internalListener);
		ScrollBar vBar = parent.getVerticalBar ();
		if (vBar != null) vBar.removeListener (SWT.Selection, internalListener);
	}
	
	parent = null;
	editor = null;
	hadFocus = false;
	internalListener = null;
}
/**
* Returns the Control that is displayed above the composite being edited.
*
* @return the Control that is displayed above the composite being edited
*/
public Control getEditor () {
	return editor;
}
void resize () {
	if (editor == null || editor.isDisposed()) return;
	if (editor.getVisible ()) {
		hadFocus = editor.isFocusControl();
	} // this doesn't work because
	  // resizing the column takes the focus away
	  // before we get here
	editor.setBounds (computeBounds ());
	if (hadFocus) editor.setFocus ();
}
void scroll (Event e) {
	if (editor == null || editor.isDisposed()) return;
	if (editor.getVisible ()) {
		hadFocus = editor.isFocusControl();
	}
	boolean visible = e.detail != SWT.DRAG;
	if (visible) {
		editor.setBounds (computeBounds ());
	}
	editor.setVisible (visible);
	if (visible && hadFocus) editor.setFocus ();
}
/**
* Specify the Control that is to be displayed.
*
* <p>Note: The Control provided as the editor <b>must</b> be created with its parent 
* being the Composite specified in the ControlEditor constructor.
* 
* @param editor the Control that is displayed above the composite being edited
*/
public void setEditor (Control editor) {
	
	if (editor == null) {
		// this is the case where the caller is setting the editor to be blank
		// set all the values accordingly
		this.editor = null;
		return;
	}
	
	this.editor = editor;
	
	editor.setVisible (false);
	editor.setBounds (computeBounds ());
	editor.setVisible (true);
}
}
