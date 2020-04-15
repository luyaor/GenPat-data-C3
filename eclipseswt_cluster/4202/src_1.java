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
 * Instances of this class are controls that allow the user
 * to choose an item from a list of items, or optionally 
 * enter a new value by typing it into an editable text
 * field. Often, <code>Combo</code>s are used in the same place
 * where a single selection <code>List</code> widget could
 * be used but space is limited. A <code>Combo</code> takes
 * less space than a <code>List</code> widget and shows
 * similar information.
 * <p>
 * Note: Since <code>Combo</code>s can contain both a list
 * and an editable text field, it is possible to confuse methods
 * which access one versus the other (compare for example,
 * <code>clearSelection()</code> and <code>deselectAll()</code>).
 * The API documentation is careful to indicate either "the
 * receiver's list" or the "the receiver's text field" to 
 * distinguish between the two cases.
 * </p><p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to add children to it, or set a layout on it.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DROP_DOWN, READ_ONLY, SIMPLE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see List
 */

public class Combo extends Composite {
	int padHandle, glist;
	int textLimit = LIMIT;
	public final static int LIMIT;
	
	/*
	* These values can be different on different platforms.
	* Therefore they are not initialized in the declaration
	* to stop the compiler from inlining.
	*/
	static {
		LIMIT = 0xFFFF;
	}

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
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
public Combo (Composite parent, int style) {
	super (parent, checkStyle (style));
}

/**
 * Adds the argument to the end of the receiver's list.
 *
 * @param string the new item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_ADDED - if the operation fails because of an operating system failure</li>
 * </ul>
 *
 * @see #add(String,int)
 */
public void add (String string) {
	checkWidget();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	String [] items = getItems ();
	String [] newItems = new String [items.length + 1];
	System.arraycopy (items, 0, newItems, 0, items.length);
	newItems [items.length] = string;
	setItems (newItems);
}

/**
 * Adds the argument to the receiver's list at the given
 * zero-relative index.
 * <p>
 * Note: To add an item at the end of the list, use the
 * result of calling <code>getItemCount()</code> as the
 * index or use <code>add(String)</code>.
 * </p>
 *
 * @param string the new item
 * @param index the index for the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_ADDED - if the operation fails because of an operating system failure</li>
 * </ul>
 *
 * @see #add(String)
 */
public void add (String string, int index) {
	checkWidget();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (!(0 <= index && index <= getItemCount ())) {
		error (SWT.ERROR_ITEM_NOT_ADDED);
	}
	String [] items = getItems ();
	String [] newItems = new String [items.length + 1];
	System.arraycopy (items, 0, newItems, 0, items.length);
	newItems [index] = string;
	setItems (newItems);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver's text is modified, by sending
 * it one of the messages defined in the <code>ModifyListener</code>
 * interface.
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
 * @see ModifyListener
 * @see #removeModifyListener
 */
public void addModifyListener (ModifyListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Modify, typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver's selection changes, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * <code>widgetSelected</code> is called when the combo's list selection changes.
 * <code>widgetDefaultSelected</code> is typically called when ENTER is pressed the combo's text area.
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
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection,typedListener);
	addListener (SWT.DefaultSelection,typedListener);
}

static int checkStyle (int style) {
	/*
	* Feature in Windows.  It is not possible to create
	* a combo box that has a border using Windows style
	* bits.  All combo boxes draw their own border and
	* do not use the standard Windows border styles.
	* Therefore, no matter what style bits are specified,
	* clear the BORDER bits so that the SWT style will
	* match the Windows widget.
	*
	* The Windows behavior is currently implemented on
	* all platforms.
	*/
	style &= ~SWT.BORDER;
	
	/*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
	style &= ~(SWT.H_SCROLL | SWT.V_SCROLL);
	style = checkBits (style, SWT.DROP_DOWN, SWT.SIMPLE, 0, 0, 0, 0);
	if ((style & SWT.SIMPLE) != 0) return style & ~SWT.READ_ONLY;
	return style;
}

/**
 * Sets the selection in the receiver's text field to an empty
 * selection starting just before the first character. If the
 * text field is editable, this has the effect of placing the
 * i-beam at the start of the text.
 * <p>
 * Note: To clear the selected items in the receiver's list, 
 * use <code>deselectAll()</code>.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #deselectAll
 */
public void clearSelection () {
	checkWidget();
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	int position = OS.gtk_editable_get_position (combo.entry);
	OS.gtk_editable_set_position (combo.entry, position);
}

void createHandle (int index) {
	state |= HANDLE;
	eventBoxHandle = OS.gtk_event_box_new ();
	if (eventBoxHandle == 0) error (SWT.ERROR_NO_HANDLES);
	padHandle = OS.gtk_fixed_new ();
	if (padHandle == 0) error (SWT.ERROR_NO_HANDLES);
	handle = OS.gtk_combo_new ();
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
	fixedHandle = OS.gtk_fixed_new();
	if (fixedHandle == 0) error (SWT.ERROR_NO_HANDLES);
}

void setHandleStyle() {
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	boolean isEditable = (style & SWT.READ_ONLY) == 0;
	OS.gtk_entry_set_editable (combo.entry, isEditable);
}

void configure () {
	_connectParent();
	OS.gtk_container_add(eventBoxHandle, padHandle);
	OS.gtk_fixed_put (padHandle, fixedHandle, (short)0, (short)0);
	OS.gtk_fixed_put (padHandle, handle, (short)0, (short)0);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	return _computeSize(wHint, hHint, changed);
}

void showHandle() {
	OS.gtk_widget_show(eventBoxHandle);
	OS.gtk_widget_show(padHandle);
	OS.gtk_widget_show(fixedHandle);
	OS.gtk_widget_show(handle);	
	OS.gtk_widget_realize (handle);
}

void deregister () {
	super.deregister ();
	WidgetTable.remove (padHandle);
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	WidgetTable.remove (combo.entry);
	WidgetTable.remove (combo.list);
	WidgetTable.remove (combo.button);
}

void hookEvents () {
	// TO DO - expose, enter/exit, focus in/out
	super.hookEvents ();
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	// TO DO - fix multiple selection events for one user action
	signal_connect (combo.list, "select_child", SWT.Selection, 3);
	signal_connect_after (combo.entry, "changed", SWT.Modify, 2);
	int mask =
		OS.GDK_POINTER_MOTION_MASK | 
		OS.GDK_BUTTON_PRESS_MASK | OS.GDK_BUTTON_RELEASE_MASK | 
		OS.GDK_KEY_PRESS_MASK | OS.GDK_KEY_RELEASE_MASK;
	int [] handles = new int [] {combo.entry, combo.list, combo.button};
	for (int i=0; i<handles.length; i++) {
		int handle = handles [i];
		if (!OS.GTK_WIDGET_NO_WINDOW (handle)) {
			OS.gtk_widget_add_events (handle, mask);
		}
		signal_connect_after (handle, "motion_notify_event", SWT.MouseMove, 3);
		signal_connect_after (handle, "button_press_event", SWT.MouseDown, 3);
		signal_connect_after (handle, "button_release_event", SWT.MouseUp, 3);
		signal_connect_after (handle, "key_press_event", SWT.KeyDown, 3);
		signal_connect_after (handle, "key_release_event", SWT.KeyUp, 3);
	}
}

int topHandle() { return eventBoxHandle; }
int parentingHandle() { return fixedHandle; }
boolean isMyHandle(int h) {
	if (h==eventBoxHandle) return true;
	if (h==padHandle) return true;
	if (h==fixedHandle) return true;
	if (h==handle) return true;
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	if (h== combo.entry) return true;
	if (h== combo.list) return true;
	if (h== combo.button) return true;
	return false;
}

void _connectChild (int h) {
	OS.gtk_fixed_put (fixedHandle, h, (short)0, (short)0);
}

Point _getClientAreaSize () {
	return UtilFuncs.getSize(fixedHandle);
}

boolean _setSize(int width, int height) {
	boolean differentExtent = UtilFuncs.setSize(eventBoxHandle, width,height);
	UtilFuncs.setSize    (fixedHandle, width,height);
	UtilFuncs.setSize    (handle,       width,height);
	return differentExtent;
}

/**
 * Deselects the item at the given zero-relative index in the receiver's 
 * list.  If the item at the index was already deselected, it remains
 * deselected. Indices that are out of range are ignored.
 *
 * @param index the index of the item to deselect
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void deselect (int index) {
	checkWidget();
	setItems (getItems ());
}

/**
 * Deselects all selected items in the receiver's list.
 * <p>
 * Note: To clear the selection in the receiver's text field,
 * use <code>clearSelection()</code>.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #clearSelection
 */
public void deselectAll () {
	checkWidget();
	setItems (getItems ());
}

/*
 * FIXME
protected boolean hasFocus () {
	return super.hasFocus();
}
*/

/**
 * Returns the item at the given, zero-relative index in the
 * receiver's list. Throws an exception if the index is out
 * of range.
 *
 * @param index the index of the item to return
 * @return the item at the given index
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_CANNOT_GET_ITEM - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public String getItem (int index) {
	checkWidget();
	String [] items = getItems ();
	if (!(0 <= index && index < items.length)) {
		error (SWT.ERROR_CANNOT_GET_ITEM);
	}
	return items [index];
}

/**
 * Returns the number of items contained in the receiver's list.
 *
 * @return the number of items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_CANNOT_GET_COUNT - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public int getItemCount () {
	checkWidget();
	if (glist == 0) return 0;
	return OS.g_list_length (glist);
}

/**
 * Returns the height of the area which would be used to
 * display <em>one</em> of the items in the receiver's list.
 *
 * @return the height of one item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_CANNOT_GET_ITEM_HEIGHT - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public int getItemHeight () {
	checkWidget();
	/* FIXME */
	return 0;
}

/**
 * Returns an array of <code>String</code>s which are the items
 * in the receiver's list. 
 * <p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its list of items, so modifying the array will
 * not affect the receiver. 
 * </p>
 *
 * @return the items in the receiver's list
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_CANNOT_GET_ITEM - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public String [] getItems () {
	checkWidget();
	if (glist == 0) return new String [0];
	int count = OS.g_list_length (glist);
	String [] items = new String [count];
	for (int i=0; i<count; i++) {
		int data = OS.g_list_nth_data (glist, i);
		int length = OS.strlen (data);
		byte [] buffer1 = new byte [length];
		OS.memmove (buffer1, data, length);
		char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
		items [i] = new String (buffer2, 0, length);
	}
	return items;
}

/**
 * Returns a <code>Point</code> whose x coordinate is the start
 * of the selection in the receiver's text field, and whose y
 * coordinate is the end of the selection. The returned values
 * are zero-relative. An "empty" selection as indicated by
 * the the x and y coordinates having the same value.
 *
 * @return a point representing the selection start and end
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getSelection () {
	checkWidget ();
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	GtkEditable editable = new GtkEditable();
	OS.memmove (editable, combo.entry, GtkEditable.sizeof);
	return new Point (editable.selection_start_pos, editable.selection_end_pos);
}

/**
 * Returns the zero-relative index of the item which is currently
 * selected in the receiver's list, or -1 if no item is selected.
 *
 * @return the index of the selected item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSelectionIndex () {
	checkWidget();
	//NOT RIGHT FOR EDITABLE
	return indexOf (getText ());
}

/**
 * Returns a string containing a copy of the contents of the
 * receiver's text field.
 *
 * @return the receiver's text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getText () {
	checkWidget();
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	int address = OS.gtk_entry_get_text (combo.entry);
	int length = OS.strlen (address);
	byte [] buffer1 = new byte [length];
	OS.memmove (buffer1, address, length);
	/*
	* This code is intentionally commented.
	* The GTK documentation explicitly states
	* that this address should not be freed.
	*/
//	OS.g_free (address);
	char [] buffer2 = Converter.mbcsToWcs (null, buffer1);
	return new String (buffer2, 0, buffer2.length);
}

String getText (int start, int stop) {
	/*
	* NOTE: The current implementation uses substring ()
	* which can reference a potentially large character
	* array.
	*/
	return getText ().substring (start, stop - 1);
}

/**
 * Returns the height of the receivers's text field.
 *
 * @return the text height
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_CANNOT_GET_ITEM_HEIGHT - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public int getTextHeight () {
	checkWidget();
	/* A native approach, just measuring the entry:
	 * return UtilFuncs.getSize(_entryHandle).y;
	 * does not work - the entry is the same size as
	 * the whole combo.
	 */
	 error (SWT.ERROR_CANNOT_GET_ITEM_HEIGHT);
	 return 0;
}

/**
 * Returns the maximum number of characters that the receiver's
 * text field is capable of holding. If this has not been changed
 * by <code>setTextLimit()</code>, it will be the constant
 * <code>Combo.LIMIT</code>.
 * 
 * @return the text limit
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getTextLimit () {
	checkWidget();
	return textLimit;
}

/**
 * Searches the receiver's list starting at the first item
 * (index 0) until an item is found that is equal to the 
 * argument, and returns the index of that item. If no item
 * is found, returns -1.
 *
 * @param string the search item
 * @return the index of the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int indexOf (String string) {
	checkWidget();
	return indexOf (string, 0);
}

/**
 * Searches the receiver's list starting at the given, 
 * zero-relative index until an item is found that is equal
 * to the argument, and returns the index of that item. If
 * no item is found or the starting index is out of range,
 * returns -1.
 *
 * @param string the search item
 * @return the index of the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int indexOf (String string, int start) {
	checkWidget();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	String [] items = getItems ();
	for (int i=start; i<items.length; i++) {
		if (string.equals(items [i])) return i;
	}
	return -1;
}

int processModify (int arg0, int arg1, int int2) {
	sendEvent (SWT.Modify);
	return 0;
}

int processSelection (int int0, int int1, int int2) {
	postEvent (SWT.Selection);
	return 0;
}

void register () {
	super.register ();
	WidgetTable.put (padHandle, this);
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	WidgetTable.put (combo.entry, this);
	WidgetTable.put (combo.list, this);
	WidgetTable.put (combo.button, this);
}

void releaseHandle () {
	super.releaseHandle ();
	int padHandle = 0;
}

void releaseWidget () {
	if (glist != 0) {
		int count = OS.g_list_length (glist);
		for (int i=0; i<count; i++) {
			int data = OS.g_list_nth_data (glist, i);
			if (data != 0) OS.g_free (data);
		}
		OS.g_list_free (glist);
	}
	glist = 0;
	super.releaseWidget ();
}

/**
 * Removes the item from the receiver's list at the given
 * zero-relative index.
 *
 * @param index the index for the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_REMOVED - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public void remove (int index) {
	checkWidget();
	if (!(0 <= index && index < getItemCount ())) {
		error (SWT.ERROR_ITEM_NOT_REMOVED);
	}
	String [] oldItems = getItems ();
	String [] newItems = new String [oldItems.length - 1];
	System.arraycopy (oldItems, 0, newItems, 0, index);
	System.arraycopy (oldItems, index + 1, newItems, index, oldItems.length - index - 1);
	setItems (newItems);
}

/**
 * Removes the items from the receiver's list which are
 * between the given zero-relative start and end 
 * indices (inclusive).
 *
 * @param start the start of the range
 * @param end the end of the range
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if either the start or end are not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_REMOVED - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public void remove (int start, int end) {
	checkWidget();
	if (!(0 <= start && start <= end && end < getItemCount ())) {
		error (SWT.ERROR_ITEM_NOT_REMOVED);
	}
	String [] oldItems = getItems ();
	String [] newItems = new String [oldItems.length - (end - start + 1)];
	System.arraycopy (oldItems, 0, newItems, 0, start);
	System.arraycopy (oldItems, end + 1, newItems, start, oldItems.length - end - 1);
	setItems (newItems);
}

/**
 * Searches the receiver's list starting at the first item
 * until an item is found that is equal to the argument, 
 * and removes that item from the list.
 *
 * @param string the item to remove
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the string is not found in the list</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_REMOVED - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public void remove (String string) {
	checkWidget();
	int index = indexOf (string, 0);
	if (index != -1) remove (index);
}

/**
 * Removes all of the items from the receiver's list.
 * <p>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void removeAll () {
	checkWidget();
	setItems (new String [0]);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver's text is modified.
 *
 * @param listener the listener which should no longer be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ModifyListener
 * @see #addModifyListener
 */
public void removeModifyListener (ModifyListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Modify, listener);	
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver's selection changes.
 *
 * @param listener the listener which should no longer be notified
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
public void removeSelectionListener (SelectionListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection,listener);	
}

/**
 * Selects the item at the given zero-relative index in the receiver's 
 * list.  If the item at the index was already selected, it remains
 * selected. Indices that are out of range are ignored.
 *
 * @param index the index of the item to select
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void select (int index) {
	checkWidget();
	String [] items = getItems ();
	if (index >= items.length) return;
	String selectedText = items [index];
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	OS.gtk_signal_handler_block_by_data (combo.entry, SWT.Modify);
	OS.gtk_signal_handler_block_by_data (combo.list, SWT.Selection);
	OS.gtk_list_select_item (combo.list, index);
	OS.gtk_entry_set_text (combo.entry, Converter.wcsToMbcs (null, selectedText, true));
	OS.gtk_signal_handler_unblock_by_data (combo.entry, SWT.Modify);
	OS.gtk_signal_handler_unblock_by_data (combo.list, SWT.Selection);
}

/**
 * Sets the text of the item in the receiver's list at the given
 * zero-relative index to the string argument. This is equivalent
 * to <code>remove</code>'ing the old item at the index, and then
 * <code>add</code>'ing the new item at that index.
 *
 * @param index the index for the item
 * @param string the new text for the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_REMOVED - if the remove operation fails because of an operating system failure</li>
 *    <li>ERROR_ITEM_NOT_ADDED - if the add operation fails because of an operating system failure</li>
 * </ul>
 */
public void setItem (int index, String string) {
	checkWidget();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (!(0 <= index && index <= getItemCount ())) {
		error (SWT.ERROR_INVALID_ARGUMENT);
	}
	String [] items = getItems ();
	items [index] = string;
	setItems (items);
}

/**
 * Sets the receiver's list to be the given array of items.
 *
 * @param items the array of items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_ITEM_NOT_ADDED - if the operation fails because of an operating system failure</li>
 * </ul>
 */
public void setItems (String [] items) {
	checkWidget();
	if (items == null) error (SWT.ERROR_NULL_ARGUMENT);
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	if (items.length == 0) {
		OS.gtk_list_clear_items (combo.list, 0, -1);
		//LEAK
		glist = 0;
	} else {
		int new_glist = 0;
		for (int i=0; i<items.length; i++) {
			String string = items [i];
			// FIXME leaked strings and glist
			if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
			byte [] buffer = Converter.wcsToMbcs (null, string, true);
			int data = OS.g_malloc (buffer.length);
			OS.memmove (data, buffer, buffer.length);
			new_glist = OS.g_list_append (new_glist, data);
		}
		OS.gtk_signal_handler_block_by_data (combo.entry, SWT.Modify);
		OS.gtk_signal_handler_block_by_data (combo.list, SWT.Selection);
		OS.gtk_combo_set_popdown_strings (handle, new_glist);
		OS.gtk_signal_handler_unblock_by_data (combo.entry, SWT.Modify);
		OS.gtk_signal_handler_unblock_by_data (combo.list, SWT.Selection);
		if (glist != 0) {
			int count = OS.g_list_length (glist);
			for (int i=0; i<count; i++) {
				int data = OS.g_list_nth_data (glist, i);
				if (data != 0) OS.g_free (data);
			}
			OS.g_list_free (glist);
		}
		glist = new_glist;
	}
	OS.gtk_signal_handler_block_by_data (combo.entry, SWT.Modify);
	OS.gtk_editable_delete_text (combo.entry, 0, -1);
	OS.gtk_signal_handler_unblock_by_data (combo.entry, SWT.Modify);
}

/**
 * Sets the selection in the receiver's text field to the
 * range specified by the argument whose x coordinate is the
 * start of the selection and whose y coordinate is the end
 * of the selection. 
 *
 * @param a point representing the new selection start and end
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (Point selection) {
	checkWidget();
	if (selection == null) error (SWT.ERROR_NULL_ARGUMENT);
	GtkCombo gtkCombo = new GtkCombo ();
	OS.memmove (gtkCombo, handle, GtkCombo.sizeof);
	int entry = gtkCombo.entry;
	OS.gtk_editable_set_position (entry, selection.x);
	OS.gtk_editable_select_region (entry, selection.x, selection.y);
}

protected boolean setTabGroupFocus () {
	return setFocus ();
}

/**
 * Sets the contents of the receiver's text field to the
 * given string.
 * <p>
 * Note: The text field in a <code>Combo</code> is typically
 * only capable of displaying a single line of text. Thus,
 * setting the text to a string containing line breaks or
 * other special characters will probably cause it to 
 * display incorrectly.
 * </p>
 *
 * @param text the new text
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setText (String string) {
	checkWidget();
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (string == null) error (SWT.ERROR_NULL_ARGUMENT);
	GtkCombo gtkCombo = new GtkCombo ();
	OS.memmove (gtkCombo, handle, GtkCombo.sizeof);
	int entry = gtkCombo.entry;
	OS.gtk_editable_delete_text (entry, 0, -1);
	int [] position = new int [1];
	byte [] buffer = Converter.wcsToMbcs (null, string);
	OS.gtk_editable_insert_text (entry, buffer, buffer.length, position);
	OS.gtk_editable_set_position (entry, 0);
}

/**
 * Sets the maximum number of characters that the receiver's
 * text field is capable of holding to be the argument.
 *
 * @param limit new text limit
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_CANNOT_BE_ZERO - if the limit is zero</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setTextLimit (int limit) {
	checkWidget();
	if (limit == 0) error (SWT.ERROR_CANNOT_BE_ZERO);
	this.textLimit = (short) limit;
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle, GtkCombo.sizeof);
	OS.gtk_entry_set_max_length (combo.entry, (short) limit);
}

}
