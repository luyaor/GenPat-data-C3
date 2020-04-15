package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
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

public class ToolItem extends Item {
	int boxHandle;
	ToolBar parent;
	Control control;
	Image hotImage, disabledImage;
	int currentpixmap;
	boolean drawHotImage;
	int position;
	boolean configured=false;
	boolean shown=false;
	private int tooltipsHandle;

/**
 * Constructs a new instance of this class given its parent
 * (which must be a <code>ToolBar</code>) and a style value
 * describing its behavior and appearance. The item is added
 * to the end of the items maintained by its parent.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public ToolItem (ToolBar parent, int style) {
	super (parent, checkStyle (style));
	this.parent = parent;
	position = parent.getItemCount ();
	createWidget (position);
}
/**
 * Constructs a new instance of this class given its parent
 * (which must be a <code>ToolBar</code>), a style value
 * describing its behavior and appearance, and the index
 * at which to place it in the items maintained by its parent.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 * @param index the index to store the receiver in its parent
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public ToolItem (ToolBar parent, int style, int index) {
	super (parent, checkStyle (style));
	this.parent = parent;
	int count = parent.getItemCount ();
	if (!(0 <= index && index <= count)) {
		error (SWT.ERROR_ITEM_NOT_ADDED);
	}
	position = index;
	createWidget (index);
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
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection,typedListener);
	addListener (SWT.DefaultSelection,typedListener);
}
static int checkStyle (int style) {
	return checkBits (style, SWT.PUSH, SWT.CHECK, SWT.RADIO, SWT.SEPARATOR, SWT.DROP_DOWN, 0);
}

void createHandle (int index) {
	state |= HANDLE;
	int bits = SWT.SEPARATOR | SWT.RADIO | SWT.CHECK | SWT.PUSH;
	switch (style & bits) {
		case SWT.RADIO:
		case SWT.CHECK:
			_createToggleHandle(index);  return;
		case SWT.SEPARATOR:
			_createSeparatorHandle(index);  return;
		case SWT.PUSH:
		default:
			_createPushHandle(index);  return;
	}
}

private void _createSeparatorHandle(int index) {
	boxHandle = OS.gtk_event_box_new();
	if (boxHandle==0) error(SWT.ERROR_NO_HANDLES);
	boolean isVertical = (parent.getStyle()&SWT.VERTICAL) != 0;
	handle = isVertical? OS.gtk_hseparator_new() : OS.gtk_vseparator_new();
	if (handle==0) error(SWT.ERROR_NO_HANDLES);
}
private void _createPushHandle(int index) {	
	handle = OS.gtk_toolbar_insert_element (parent.handle,
		OS.GTK_TOOLBAR_CHILD_BUTTON,
		0, new byte[1], null, null,
		0, 0, 0,
		index);
	configured=true;
	shown=true;
}
private void _createToggleHandle(int index) {
	handle = OS.gtk_toolbar_insert_element (parent.handle,
		OS.GTK_TOOLBAR_CHILD_TOGGLEBUTTON,
		0, new byte[1], null, null,
		0, 0, 0,
		index);
	configured=true;
	shown=true;
}	


void configure() {
	// configure is done for non-separators
	if (configured) return;
	OS.gtk_toolbar_insert_widget (
		parent.handle,
		topHandle(),
		new byte[1], new byte[1],
		position);
	OS.gtk_container_add(boxHandle, handle);
}

void showHandle() {
	if (shown) return;
	if ((parent.getStyle()&SWT.VERTICAL)!=0) OS.gtk_widget_set_usize(handle, 15, 3);
		else OS.gtk_widget_set_usize(handle, 3, 15);
	OS.gtk_widget_show(boxHandle);
	OS.gtk_widget_show(handle);
}

void register() {
	super.register ();
	if (boxHandle != 0) WidgetTable.put (boxHandle, this);
}

void releaseHandle () {
	super.releaseHandle ();
	boxHandle = 0;
}

void deregister() {
	super.deregister ();
	if (boxHandle != 0) WidgetTable.remove (boxHandle);
}

int topHandle() {
	return (boxHandle==0)? handle : boxHandle;
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
	GtkWidget widget = new GtkWidget ();
	OS.memmove (widget, handle, GtkWidget.sizeof);
	return new Rectangle (widget.alloc_x, widget.alloc_y, widget.alloc_width, widget.alloc_height);
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
 * Returns the receiver's disabled image if it has one, or null
 * if it does not.
 * <p>
 * The disabled image is displayed when the receiver is disabled.
 * </p>
 *
 * @return the receiver's disabled image
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Image getDisabledImage () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	error(SWT.ERROR_NOT_IMPLEMENTED);
	return null;
}

public Display getDisplay () {
	ToolBar parent = this.parent;
	if (parent == null) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent.getDisplay ();
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
	GtkWidget widget = new GtkWidget ();
	OS.memmove (widget, handle, GtkWidget.sizeof);
	return (widget.flags & OS.GTK_SENSITIVE) != 0;     
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
	return null;
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
	return OS.gtk_toggle_button_get_active (handle);
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
	return "";
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
	GtkWidget widget = new GtkWidget ();
	OS.memmove (widget, handle, GtkWidget.sizeof);
	return widget.alloc_width;
}
void hookEvents () {
	if ((style & SWT.SEPARATOR) != 0) return;
	signal_connect(handle, "clicked",   SWT.Selection, 2);
	signal_connect(handle, "enter-notify-event", SWT.MouseEnter, 3);
	signal_connect(handle, "leave-notify-event", SWT.MouseExit,  3);
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

int processMouseEnter (int int0, int int1, int int2) {
	drawHotImage = (parent.style & SWT.FLAT) != 0 && hotImage != null;
	if ( drawHotImage && (currentpixmap != 0) ) { 
		OS.gtk_pixmap_set (currentpixmap, hotImage.pixmap, hotImage.mask);
	}
	return 0;
}

int processMouseExit (int int0, int int1, int int2) {
	if (drawHotImage) {
		drawHotImage = false;
		if (currentpixmap != 0 && image != null){
			OS.gtk_pixmap_set (currentpixmap, image.pixmap, image.mask);
		}	
	}
	return 0;
}
/*
int processPaint (int int0, int int1, int int2) {
	if (ignorePaint) return 0;
	Image currentImage = drawHotImage ? hotImage : image;
	if (!getEnabled()) {
		Display display = getDisplay ();
		currentImage = disabledImage;
		if (currentImage == null) {
			currentImage = new Image (display, image, SWT.IMAGE_DISABLE);
		}
	}	
	if (currentpixmap != 0 && currentImage != null)
		OS.gtk_pixmap_set (currentpixmap, currentImage.pixmap, currentImage.mask);
	return 0;
}
*/
int processSelection  (int int0, int int1, int int2) {
	if ((style & SWT.RADIO) != 0) {
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
	Event event = new Event ();
	postEvent (SWT.Selection, event);
	return 0;
}
void releaseWidget () {
	super.releaseWidget ();
	tooltipsHandle = 0;
	parent = null;
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
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection,listener);	
}
/**
 * Sets the control that is used to fill the bounds of
 * the item when the items is a <code>SEPARATOR</code>.
 *
 * @param control the new control
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li> 
 *    <li>ERROR_INVALID_PARENT - if the control is not in the same widget tree</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setControl (Control control) {
	checkWidget ();
	if (control != null) {
		if (control.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
		if (control.parent != parent) error (SWT.ERROR_INVALID_PARENT);
	}
	if ((style & SWT.SEPARATOR) == 0) return;
	Control newControl = control;
	Control oldControl = this.control;
	if (oldControl == newControl) return;
	if (oldControl != null) {
		int topHandle = control.topHandle ();
		int tempHandle = parent.tempHandle;
		OS.gtk_widget_reparent (topHandle, tempHandle);
	}
	this.control = newControl;
	if (newControl != null) {
		if (handle != boxHandle) {
			WidgetTable.remove (handle);
			OS.gtk_widget_destroy (handle);
			handle = boxHandle;
		}
		int topHandle = control.topHandle ();
		OS.gtk_widget_reparent (topHandle, boxHandle);
		//OS.gtk_widget_show (topHandle);
	} else {		
		boolean isVertical = (parent.getStyle () & SWT.VERTICAL) != 0;
		handle = isVertical ? OS.gtk_hseparator_new () : OS.gtk_vseparator_new ();
		if (handle == 0) error(SWT.ERROR_NO_HANDLES);
		OS.gtk_container_add (boxHandle, handle);
	}
}
/**
 * Sets the receiver's disabled image to the argument, which may be
 * null indicating that no disabled image should be displayed.
 * <p>
 * The disbled image is displayed when the receiver is disabled.
 * </p>
 *
 * @param image the disabled image to display on the receiver (may be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li> 
 * </ul>
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
	OS.gtk_widget_set_sensitive (handle, enabled);
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
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li> 
 * </ul>
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
}
public void setImage (Image image) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	super.setImage (image);
	if ((style & SWT.SEPARATOR) != 0) return;
	int list = OS.gtk_container_children (handle);
	if (list != 0) {
		int widget = OS.g_list_nth_data (list, 0);
		if (widget != 0) OS.gtk_widget_destroy (widget);
	}
	if (image != null) {
		int pixmap = OS.gtk_pixmap_new (image.pixmap, image.mask);
		OS.gtk_container_add (handle, pixmap);
		OS.gtk_widget_show (pixmap);
		currentpixmap = pixmap;
	}
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
	OS.gtk_signal_handler_block_by_data (handle, SWT.Selection);
	OS.gtk_toggle_button_set_active (handle, selected);
	OS.gtk_signal_handler_unblock_by_data (handle, SWT.Selection);
}
public void setText (String string) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	if ((style & SWT.SEPARATOR) != 0) return;
	text = string;
	if ((style & SWT.ARROW) != 0) return;
	int length = string.length ();
	char [] text = new char [length + 1];
	char [] pattern = new char [length + 1];
	string.getChars (0, length, text, 0);
	int i = 0, j = 0;
	while (i < length) {
		pattern [j] = ' ';
		if (text [i] == '&') {
			i++;
			if (i < length && text [i] != '&') {
				pattern [j] = '_';
			}
		}
		text [j++] = text [i++];
	}
	while (j < i) {
		text [j] = pattern [j] = '\0';
		j++;
	}
	int list = OS.gtk_container_children (handle);
	if (list != 0) {
		int widget = OS.g_list_nth_data (list, 0);
		if (widget !=  0) OS.gtk_widget_destroy (widget);
	}
	byte [] buffer1 = Converter.wcsToMbcs (null, text);
	int label = OS.gtk_label_new (buffer1);
	byte [] buffer2 = Converter.wcsToMbcs (null, pattern);
	OS.gtk_label_set_pattern (label, buffer2);	
	OS.gtk_container_add (handle, label);
	OS.gtk_widget_show (label);	
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
	if (tooltipsHandle == 0) tooltipsHandle = OS.gtk_tooltips_new();
	byte [] buffer = Converter.wcsToMbcs (null, string, true);
	OS.gtk_tooltips_set_tip(tooltipsHandle, handle, buffer, null);
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
	
	Point size = control.computeSize(width, SWT.DEFAULT);
	control.setSize(size);
}
}
