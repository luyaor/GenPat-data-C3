package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */
 
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.motif.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;

/**
 * Instances of this class represent a selectable user interface object
 * that represents a button in a tool bar.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>PUSH, CHECK, RADIO, SEPARATOR, DROP_DOWN</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * </p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 */
public /*final*/ class ToolItem extends Item {
	ToolBar parent;
	Image hotImage, disabledImage;
	String toolTipText;
	Control control;
	boolean set;
	int mnemonicPos = -1;

/**
* Creates a new instance of the widget.
*/
public ToolItem (ToolBar parent, int style) {
	super (parent, checkStyle (style));
	this.parent = parent;
	parent.createItem (this, parent.getItemCount ());
	parent.relayout ();
}
/**
* Creates a new instance of the widget.
*/
public ToolItem (ToolBar parent, int style, int index) {
	super (parent, checkStyle (style));
	this.parent = parent;
	parent.createItem (this, index);
	parent.relayout ();
}
/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control is selected, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * When <code>widgetSelected</code> is called when the mouse is over the arrow portion of a drop-down tool,
 * the event object detail field contains the value <code>SWT.ARROW</code>.
 * <code>widgetDefaultSelected</code> is not called.
 * </p>
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #removeSelectionListener
 * @see SelectionEvent
 */
public void addSelectionListener(SelectionListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener(listener);
	addListener(SWT.Selection,typedListener);
	addListener(SWT.DefaultSelection,typedListener);
}
static int checkStyle (int style) {
	return checkBits (style, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.DROP_DOWN, 0);
}
protected void checkSubclass () {
	if (!isValidSubclass ()) error (SWT.ERROR_INVALID_SUBCLASS);
}
void createHandle (int index) {
	state |= HANDLE;
	int parentHandle = parent.handle;
	if ((style & SWT.SEPARATOR) != 0) {
		int [] argList = {
			OS.XmNwidth, 8,
			OS.XmNheight, 5,
			OS.XmNrecomputeSize, 0,
			OS.XmNpositionIndex, index,
			OS.XmNmappedWhenManaged, 0,
			OS.XmNancestorSensitive, 1,
		};
		handle = OS.XmCreateDrawnButton (parentHandle, null, argList, argList.length / 2);
		if (handle == 0) error (SWT.ERROR_NO_HANDLES);
		return;
	}
	int [] argList = {
		OS.XmNwidth, 24,
		OS.XmNheight, 22,
		OS.XmNrecomputeSize, 0,
		OS.XmNhighlightThickness, 0,
		OS.XmNmarginWidth, 2,
		OS.XmNmarginHeight, 1,
		OS.XmNtraversalOn, 0,
		OS.XmNpositionIndex, index,
		OS.XmNshadowType, OS.XmSHADOW_OUT,
		OS.XmNancestorSensitive, 1,
	};
	handle = OS.XmCreateDrawnButton (parentHandle, null, argList, argList.length / 2);
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
}

Point computeSize () {
	if ((style & SWT.SEPARATOR) != 0) {
		int [] argList = {
			OS.XmNwidth, 0,
			OS.XmNheight, 0,
		};
		OS.XtGetValues (handle, argList, argList.length / 2);
		int width = argList [1], height = argList [3];
		return new Point(width, height);
	}
	int [] argList = {
		OS.XmNmarginHeight, 0,
		OS.XmNmarginWidth, 0,
		OS.XmNshadowThickness, 0,
	};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int marginHeight = argList [1], marginWidth = argList [3];
	int shadowThickness = argList [5];
	if ((parent.style & SWT.FLAT) != 0) {
		Display display = getDisplay ();
		shadowThickness = Math.min (2, display.buttonShadowThickness);
	}
	int textWidth = 0, textHeight = 0;
	if (text.length () != 0) {
		GC gc = new GC (parent);
		Point textExtent = gc.textExtent (text);
		textWidth = textExtent.x;
		textHeight = textExtent.y;
		gc.dispose ();
	}
	int imageWidth = 0, imageHeight = 0;
	if (image != null) {
		Rectangle rect = image.getBounds ();
		imageWidth = rect.width;
		imageHeight = rect.height;
	}
	int width = 0, height = 0;
	if ((parent.style & SWT.RIGHT) != 0) {
		width = imageWidth + textWidth;
		height = Math.max (imageHeight, textHeight);
		if (imageWidth != 0 && textWidth != 0) width += 2;
	} else {
		height = imageHeight + textHeight;
		if (imageHeight != 0 && textHeight != 0) height += 2;
		width = Math.max (imageWidth, textWidth);
	}
	if ((style & SWT.DROP_DOWN) != 0) width += 12;
	
	/* The 24 and 22 values come from Windows */
	if (width != 0) {
		width += (marginWidth + shadowThickness) * 2 + 2;
	} else {
		width = 24;
	}
	if (height != 0) {
		height += (marginHeight + shadowThickness) * 2 + 2;
	} else {
		height = 22;
	}
	return new Point (width, height);
}
void createWidget (int index) {
	super.createWidget (index);
	toolTipText = "";
	parent.relayout ();
}
public void dispose () {
	if (!isValidWidget ()) return;
	ToolBar parent = this.parent;
	super.dispose ();
	parent.relayout ();
}
/**
 * Returns a rectangle describing the receiver's size and location
 * relative to its parent.
 *
 * @return the receiver's bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Rectangle getBounds () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0, OS.XmNwidth, 0, OS.XmNheight, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return new Rectangle ((short) argList [1], (short) argList [3], argList [5], argList [7]);
}
/**
 * Returns the control that is used to fill the bounds of
 * the item when the items is a <code>SEPARATOR</code>.
 *
 * @return the control
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Control getControl () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return control;
}
/**
* Gets the disabled image.
* <p>
* @return the image
*
* @exception SWTError(ERROR_THREAD_INVALID_ACCESS)
*	when called from the wrong thread
* @exception SWTError(ERROR_WIDGET_DISPOSED)
*	when the widget has been disposed
*/
public Image getDisabledmage () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return disabledImage;
}
/**
 * Returns <code>true</code> if the receiver is enabled, and
 * <code>false</code> otherwise.
 * <p>
 * A disabled control is typically not selectable from the
 * user interface and draws with an inactive or "grayed" look.
 * </p>
 *
 * @return the receiver's enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getEnabled () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int [] argList = {OS.XmNsensitive, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return argList [1] != 0;
}
public Display getDisplay () {
	Composite parent = this.parent;
	if (parent == null) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent.getDisplay ();
}
/**
 * Returns the receiver's hot image if it has one, or null
 * if it does not.
 * <p>
 * The hot image is displayed when the mouse enters the receiver.
 * </p>
 *
 * @return the receiver's hot image
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Image getHotImage () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return hotImage;
}
/**
 * Returns the receiver's parent, which must be a <code>ToolBar</code>.
 *
 * @return the receiver's parent
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public ToolBar getParent () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent;
}
/**
 * Returns <code>true</code> if the receiver is selected,
 * and false otherwise.
 * <p>
 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
 * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
 * it is selected when it is pushed.
 * </p>
 *
 * @return the selection state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getSelection () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if ((style & (SWT.CHECK | SWT.RADIO)) == 0) return false;
	return set;
}
/**
 * Returns the receiver's tool tip text, or null if it has not been set.
 *
 * @return the receiver's tool tip text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getToolTipText () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return toolTipText;
}
/**
 * Gets the width of the receiver.
 *
 * @return the width
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getWidth () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int [] argList = {OS.XmNwidth, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return argList [1];
}
boolean hasCursor () {
	int [] unused = new int [1], buffer = new int [1];
	int xDisplay = OS.XtDisplay (handle);
	int xWindow, xParent = OS.XDefaultRootWindow (xDisplay);
	do {
		if (OS.XQueryPointer (
			xDisplay, xParent, unused, buffer,
			unused, unused, unused, unused, unused) == 0) return false;
		if ((xWindow = buffer [0]) != 0) xParent = xWindow;
	} while (xWindow != 0);
	return handle == OS.XtWindowToWidget (xDisplay, xParent);
}
void hookEvents () {
	super.hookEvents ();
	if ((style & SWT.SEPARATOR) != 0) return;
	int windowProc = getDisplay ().windowProc;
	OS.XtAddCallback (handle, OS.XmNexposeCallback, windowProc, SWT.Paint);
	OS.XtAddEventHandler (handle, OS.ButtonPressMask, false, windowProc, SWT.MouseDown);
	OS.XtAddEventHandler (handle, OS.ButtonReleaseMask, false, windowProc, SWT.MouseUp);
	OS.XtAddEventHandler (handle, OS.PointerMotionMask, false, windowProc, SWT.MouseMove);
	OS.XtAddEventHandler (handle, OS.EnterWindowMask, false, windowProc, SWT.MouseEnter);
	OS.XtAddEventHandler (handle, OS.LeaveWindowMask, false, windowProc, SWT.MouseExit);
}
/**
 * Returns <code>true</code> if the receiver is enabled, and
 * <code>false</code> otherwise.
 * <p>
 * A disabled control is typically not selectable from the
 * user interface and draws with an inactive or "grayed" look.
 * </p>
 *
 * @return the receiver's enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isEnabled () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return getEnabled () && parent.isEnabled ();
}
void manageChildren () {
	OS.XtManageChild (handle);
}
void redraw () {
	int display = OS.XtDisplay (handle);
	if (display == 0) return;
	int window = OS.XtWindow (handle);
	if (window == 0) return;
	OS.XClearArea (display, window, 0, 0, 0, 0, true);
}
void releaseChild () {
	super.releaseChild ();
	parent.destroyItem (this);
}
void releaseWidget () {
	Display display = getDisplay ();
	display.releaseToolTipHandle (handle);
	super.releaseWidget ();
	parent = null;
	control = null;
	toolTipText = null;
	image = disabledImage = hotImage = null; 
}
/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is selected.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SelectionListener
 * @see #addSelectionListener
 */
public void removeSelectionListener(SelectionListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.Selection, listener);
	eventTable.unhook(SWT.DefaultSelection,listener);	
}
void selectRadio () {
	this.setSelection (true);
	ToolItem [] items = parent.getItems ();
	int index = 0;
	while (index < items.length && items [index] != this) index++;
	ToolItem item;
	int i = index;
	while (--i >= 0 && ((item = items [i]).style & SWT.RADIO) != 0) {
		item.setSelection (false);
	}
	i = index;
	while (++i < items.length && ((item = items [i]).style & SWT.RADIO) != 0) {
		item.setSelection (false);
	}
}
void setBounds (int x, int y, int width, int height) {
	if (control != null) control.setBounds(x, y, width, height);
	/*
	* Feature in Motif.  Motif will not allow a window
	* to have a zero width or zero height.  The fix is
	* to ensure these values are never zero.
	*/
	int newWidth = Math.max (width, 1), newHeight = Math.max (height, 1);
	OS.XtConfigureWidget (handle, x, y, newWidth, newHeight, 0);
}
/**
 * Sets the control that is used to fill the bounds of
 * the item when the items is a <code>SEPARATOR</code>.
 *
 * @param control the new control
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setControl (Control control) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (control != null && control.parent != parent) {
		error (SWT.ERROR_INVALID_PARENT);
	}
	if ((style & SWT.SEPARATOR) == 0) return;
	this.control = control;
	if (control != null && !control.isDisposed ()) {
		control.setBounds (getBounds ());
	}
}
/**
 * Enables the receiver if the argument is <code>true</code>,
 * and disables it otherwise.
 * <p>
 * A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 * </p>
 *
 * @param enabled the new enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setEnabled (boolean enabled) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	int [] argList = {OS.XmNsensitive, enabled ? 1 : 0};
	OS.XtSetValues (handle, argList, argList.length / 2);
}
/**
 * Sets the receiver's disabled image to the argument, which may be
 * null indicating that no disabled image should be displayed.
 * <p>
 * The disbled image is displayed when the receiver is disabled.
 * </p>
 *
 * @param image the hot image to display on the receiver (may be null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setDisabledImage (Image image) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if ((style & SWT.SEPARATOR) != 0) return;
	disabledImage = image;
	if (!getEnabled ()) redraw ();
}
/**
 * Sets the receiver's hot image to the argument, which may be
 * null indicating that no hot image should be displayed.
 * <p>
 * The hot image is displayed when the mouse enters the receiver.
 * </p>
 *
 * @param image the hot image to display on the receiver (may be null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setHotImage (Image image) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if ((style & SWT.SEPARATOR) != 0) return;
	hotImage = image;
	if ((parent.style & SWT.FLAT) != 0) redraw ();
}
public void setImage (Image image) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if ((style & SWT.SEPARATOR) != 0) return;
	super.setImage (image);
	Point size = computeSize ();
	setSize (size.x, size.y);
	redraw ();
}

/**
 * Sets the selection state of the receiver.
 * <p>
 * When the receiver is of type <code>CHECK</code> or <code>RADIO</code>,
 * it is selected when it is checked. When it is of type <code>TOGGLE</code>,
 * it is selected when it is pushed.
 * </p>
 *
 * @param selected the new selection state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (boolean selected) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if ((style & (SWT.CHECK | SWT.RADIO)) == 0) return;
	if (selected == set) return;
	set = selected;
	setDrawPressed (set);
}
void setSize (int width, int height) {
	int [] argList = {OS.XmNwidth, 0, OS.XmNheight, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	if (argList [1] != width || argList [3] != height) {
		OS.XtResizeWidget (handle, width, height, 0);
		parent.relayout ();
	}
}
public void setText (String string) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	if ((style & SWT.SEPARATOR) != 0) return;
	// Strip out mnemonics
	char [] text = new char [string.length ()];
	string.getChars (0, text.length, text, 0);
	int i=0, j=0;
	mnemonicPos = -1;
	while (i < text.length) {
		if ((text [j++] = text [i++]) == Mnemonic) {
			if (i == text.length) {continue;}
			if (text [i] == Mnemonic) {i++; continue;}
			j--;
			if (mnemonicPos == -1) mnemonicPos = j;
		}
	}
	string = new String(text, 0, j);
	super.setText (string);
	Point size = computeSize ();
	setSize (size.x, size.y);
	int [] argList = {
		OS.XmNmnemonic, (mnemonicPos == -1) ? OS.XK_VoidSymbol : string.charAt(mnemonicPos),
	};
	OS.XtSetValues (handle, argList, argList.length / 2);
	redraw ();
}

/**
 * Sets the receiver's tool tip text to the argument, which
 * may be null indicating that no tool tip text should be shown.
 *
 * @param string the new tool tip text (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setToolTipText (String string) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	toolTipText = string;
}
/**
 * Sets the width of the receiver.
 *
 * @param width the new width
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setWidth (int width) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if ((style & SWT.SEPARATOR) == 0) return;
	if (width < 0) return;
	int [] argList = {OS.XmNheight, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	setSize (width, argList [1]);
	if (control != null && !control.isDisposed ()) {
		control.setBounds (getBounds ());
	}
}
void setDrawPressed (boolean value) {
	int shadowType = value ? OS.XmSHADOW_IN : OS.XmSHADOW_OUT;
	int [] argList = {OS.XmNshadowType, shadowType};
	OS.XtSetValues(handle, argList, argList.length / 2);
}
int processMouseDown (int callData) {
	Display display = getDisplay ();
	display.hideToolTip ();
	XButtonEvent xEvent = new XButtonEvent ();
	OS.memmove (xEvent, callData, XButtonEvent.sizeof);
	if (xEvent.button == 1) {
		if (!set && (style & SWT.RADIO) == 0) {
			setDrawPressed (!set);
		}
	}
	
	/*
	* Forward the mouse event to the parent.
	* This is necessary so that mouse listeners
	* in the parent will be called, despite the
	* fact that the event did not really occur
	* in X in the parent.  This is done to be
	* compatible with Windows.
	*/
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	xEvent.window = OS.XtWindow (parent.handle);
	xEvent.x += argList [1];  xEvent.y += argList [3];
	OS.memmove (callData, xEvent, XButtonEvent.sizeof);
	parent.processMouseDown (callData);

	return 0;
}
int processMouseEnter (int callData) {
	XCrossingEvent xEvent = new XCrossingEvent ();
	OS.memmove (xEvent, callData, XCrossingEvent.sizeof);
	if ((xEvent.state & OS.Button1Mask) != 0) setDrawPressed (!set);
	else if ((parent.style & SWT.FLAT) != 0) redraw ();
	return 0;
}
int processMouseExit (int callData) {
	Display display = getDisplay ();
	display.removeMouseHoverTimeOut ();
	display.hideToolTip ();
	XCrossingEvent xEvent = new XCrossingEvent ();
	OS.memmove (xEvent, callData, XCrossingEvent.sizeof);
	if ((xEvent.state & OS.Button1Mask) != 0) setDrawPressed (set);
	else if ((parent.style & SWT.FLAT) != 0) redraw ();
	return 0;
}
Point toControl (Point point) {
	short [] root_x = new short [1], root_y = new short [1];
	OS.XtTranslateCoords (handle, (short) 0, (short) 0, root_x, root_y);
	return new Point (point.x - root_x [0], point.y - root_y [0]);
}
int processMouseHover (int id) {
	Display display = getDisplay ();
	Point local = toControl (display.getCursorLocation ());
	display.showToolTip (handle, toolTipText);
	return 0;
}
int processMouseMove (int callData) {
	Display display = getDisplay ();
	display.addMouseHoverTimeOut (handle);

	/*
	* Forward the mouse event to the parent.
	* This is necessary so that mouse listeners
	* in the parent will be called, despite the
	* fact that the event did not really occur
	* in X in the parent.  This is done to be
	* compatible with Windows.
	*/
	XButtonEvent xEvent = new XButtonEvent ();
	OS.memmove (xEvent, callData, XButtonEvent.sizeof);
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	xEvent.window = OS.XtWindow (parent.handle);
	xEvent.x += argList [1];  xEvent.y += argList [3];
	/*
	* This code is intentionally commented.
	* Currently, the implementation of the
	* mouse move code in the parent interferes
	* with tool tips for tool items.
	*/
//	OS.memmove (callData, xEvent, XButtonEvent.sizeof);
//	parent.processMouseMove (callData);
	parent.sendMouseEvent (SWT.MouseMove, 0, xEvent.state, xEvent);

	return 0;
}
int processMouseUp (int callData) {
	Display display = getDisplay ();
	display.hideToolTip(); 
	XButtonEvent xEvent = new XButtonEvent ();
	OS.memmove (xEvent, callData, XButtonEvent.sizeof);
	if (xEvent.button == 1) {
		int [] argList = {OS.XmNwidth, 0, OS.XmNheight, 0};
		OS.XtGetValues (handle, argList, argList.length / 2);
		int width = argList [1], height = argList [3];
		if (0 <= xEvent.x && xEvent.x < width && 0 <= xEvent.y && xEvent.y < height) {
			if ((style & SWT.RADIO) != 0) {
				selectRadio ();
			} else {
				if ((style & SWT.CHECK) != 0) setSelection(!set);			
			}
			Event event = new Event ();
			if ((style & SWT.DROP_DOWN) != 0) {
				if (xEvent.x > width - 12) event.detail = SWT.ARROW;
			}
			postEvent (SWT.Selection, event);
		}
		setDrawPressed(set);
	}

	/*
	* Forward the mouse event to the parent.
	* This is necessary so that mouse listeners
	* in the parent will be called, despite the
	* fact that the event did not really occur
	* in X in the parent.  This is done to be
	* compatible with Windows.
	*/
	int [] argList = {OS.XmNx, 0, OS.XmNy, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	xEvent.window = OS.XtWindow (parent.handle);
	xEvent.x += argList [1];  xEvent.y += argList [3];
	OS.memmove (callData, xEvent, XButtonEvent.sizeof);
	parent.processMouseUp (callData);

	return 0;
}
int processPaint (int callData) {
	if ((style & SWT.SEPARATOR) != 0) return 0;
	int xDisplay = OS.XtDisplay (handle);
	if (xDisplay == 0) return 0;
	int xWindow = OS.XtWindow (handle);
	if (xWindow == 0) return 0;
	int [] argList = {
		OS.XmNcolormap, 0,
		OS.XmNwidth, 0,
		OS.XmNheight, 0,
	};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int width = argList [3], height = argList [5];
	
	Image currentImage = image;
	boolean enabled = getEnabled();

	if ((parent.style & SWT.FLAT) != 0) {
		Display display = getDisplay ();
		boolean hasCursor = hasCursor ();
		
		/* Set the shadow thickness */
		int thickness = 0;
		if (set || (hasCursor && enabled)) {
			thickness = Math.min (2, display.buttonShadowThickness);
		}
		argList = new int [] {OS.XmNshadowThickness, thickness};
		OS.XtSetValues (handle, argList, argList.length / 2);
		
		/* Determine if hot image should be used */
		if (enabled && hasCursor && hotImage != null) {
			currentImage = hotImage;
		}
	}

	ToolDrawable wrapper = new ToolDrawable ();
	wrapper.device = getDisplay ();
	wrapper.display = xDisplay;
	wrapper.drawable = xWindow;
	wrapper.fontList = parent.getFontList ();
	wrapper.colormap = argList [1];	
	GC gc = new GC (wrapper);
	
	XmAnyCallbackStruct cb = new XmAnyCallbackStruct ();
	OS.memmove (cb, callData, XmAnyCallbackStruct.sizeof);
	if (cb.event != 0) {
		XExposeEvent xEvent = new XExposeEvent ();
		OS.memmove (xEvent, cb.event, XExposeEvent.sizeof);
		Rectangle rect = new Rectangle (xEvent.x, xEvent.y, xEvent.width, xEvent.height);
		gc.setClipping (rect);
	}
	
	if (!enabled) {
		Display display = getDisplay ();
		currentImage = disabledImage;
		if (currentImage == null) {
			currentImage = new Image (display, image, SWT.IMAGE_DISABLE);
		}
		Color disabledColor = display.getSystemColor (SWT.COLOR_WIDGET_NORMAL_SHADOW);
		gc.setForeground (disabledColor);
	} else {
		gc.setForeground (parent.getForeground ());
	}
	gc.setBackground (parent.getBackground ());
		
	int textX = 0, textY = 0, textWidth = 0, textHeight = 0;
	int preMnemonicWidth = 0, mnemonicWidth = 0;
	if (text.length () != 0) {
		if (mnemonicPos != -1) {
			preMnemonicWidth = gc.textExtent(text.substring(0, mnemonicPos)).x;
			mnemonicWidth = gc.getAdvanceWidth(text.charAt(mnemonicPos));
			int postMnemonicWidth = gc.textExtent(text.substring(mnemonicPos + 1)).x;
			textWidth = preMnemonicWidth + mnemonicWidth + postMnemonicWidth;
			textHeight = gc.textExtent(text).y;
		} else {
			Point textExtent = gc.textExtent (text);
			textWidth = textExtent.x;
			textHeight = textExtent.y;
		}
	}	
	int imageX = 0, imageY = 0, imageWidth = 0, imageHeight = 0;
	if (currentImage != null) {
		Rectangle imageBounds = currentImage.getBounds ();
		imageWidth = imageBounds.width;
		imageHeight = imageBounds.height;
	}
	
	int spacing = 0;
	if (textWidth != 0 && imageWidth != 0) spacing = 2;
	if ((parent.style & SWT.RIGHT) != 0) {
		imageX = (width - imageWidth - textWidth - spacing) / 2;
		imageY = (height - imageHeight) / 2;
		textX = spacing + imageX + imageWidth;
		textY = (height - textHeight) / 2;
	} else {		
		imageX = (width - imageWidth) / 2;
		imageY = (height - imageHeight - textHeight - spacing) / 2;
		textX = (width - textWidth) / 2;
		textY = spacing + imageY + imageHeight;
	}
	
	if ((style & SWT.DROP_DOWN) != 0) {
		textX -= 6;  imageX -=6;
	}
	if (textWidth > 0) {
		if (mnemonicPos != -1) {
			int x = textX;
			if (preMnemonicWidth > 0) {
				gc.drawText(text.substring(0, mnemonicPos), x, textY, false);
				x += preMnemonicWidth;
			}
			if (mnemonicWidth > 0) {
				gc.drawText(text.substring(mnemonicPos, mnemonicPos + 1), x, textY, false);
				// draw an underscore just like the one Motif uses
				FontMetrics fontMetrics = gc.getFontMetrics();
				int underlineY = textY + fontMetrics.getHeight();
				gc.drawLine(x, underlineY, x + mnemonicWidth, underlineY);
				x += mnemonicWidth;
			}
			if (mnemonicPos < text.length()) gc.drawText(text.substring(mnemonicPos + 1),
				x, textY, false);
		} else {
			gc.drawText(text, textX, textY, false);
		}
	}
	if (imageWidth > 0) gc.drawImage(currentImage, imageX, imageY);
	if ((style & SWT.DROP_DOWN) != 0) {
		int startX = width - 12, startY = (height - 2) / 2;
		int [] arrow = {startX, startY, startX + 3, startY + 3, startX + 6, startY};
		gc.setBackground (parent.getForeground ());
		gc.fillPolygon (arrow);
		gc.drawPolygon (arrow);
	}
	gc.dispose ();
	
	if (!enabled && disabledImage == null) {
		if (currentImage != null) currentImage.dispose ();
	}
	return 0;
}
}
