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
package org.eclipse.swt.widgets;


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
 * Note: Only one of the styles DROP_DOWN and SIMPLE 
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see List
 */
public class Combo extends Composite {
	int arrowHandle, entryHandle, listHandle;
	String [] items = new String [0];
	/**
	 * the operating system limit for the number of characters
	 * that the text field in an instance of this class can hold
	 */
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
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
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
 * @see SWT#DROP_DOWN
 * @see SWT#READ_ONLY
 * @see SWT#SIMPLE
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
	String [] newItems = new String [items.length + 1];
	System.arraycopy (items, 0, newItems, 0, items.length);
	newItems [items.length] = string;
	setItems (newItems, true, true);
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
	if (!(0 <= index && index <= items.length)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] newItems = new String [items.length + 1];
	System.arraycopy (items, 0, newItems, 0, index);
	newItems [index] = string;
	System.arraycopy (items, index, newItems, index + 1, items.length - index);
	setItems (newItems, true, true);
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
	OS.gtk_editable_delete_selection (handle);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int width = OS.GTK_WIDGET_WIDTH (fixedHandle);
	int height = OS.GTK_WIDGET_HEIGHT (fixedHandle);
	OS.gtk_widget_set_size_request (handle, wHint, hHint);
	GtkRequisition requisition = new GtkRequisition ();
	OS.gtk_widget_size_request (handle, requisition);
	GtkRequisition entryRequesition = new GtkRequisition ();
	OS.gtk_widget_size_request (entryHandle, entryRequesition);
	GtkRequisition listRequesition = new GtkRequisition ();
	int listParent = OS.gtk_widget_get_parent (listHandle);
	OS.gtk_widget_size_request (listParent != 0 ? listParent : listHandle, listRequesition);
	OS.gtk_widget_set_size_request (handle, width, height);
	width = (requisition.width - entryRequesition.width) + listRequesition.width + 2;
	width = wHint == SWT.DEFAULT ? width : wHint;
	height = hHint == SWT.DEFAULT ? requisition.height : hHint;
	return new Point (width, height);
}

/**
 * Copies the selected text.
 * <p>
 * The current selection is copied to the clipboard.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @since 2.1
 */
public void copy () {
	checkWidget ();
	OS.gtk_editable_copy_clipboard (entryHandle);
}

void createHandle (int index) {
	state |= HANDLE;
	fixedHandle = OS.gtk_fixed_new ();
	if (fixedHandle == 0) error (SWT.ERROR_NO_HANDLES);
	OS.gtk_fixed_set_has_window (fixedHandle, true);
	handle = OS.gtk_combo_new ();
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
	int parentHandle = parent.parentingHandle ();
	OS.gtk_container_add (parentHandle, fixedHandle);
	OS.gtk_container_add (fixedHandle, handle);
	OS.gtk_widget_show (fixedHandle);
	OS.gtk_widget_show (handle);
	GtkCombo combo = new GtkCombo ();
	OS.memmove (combo, handle);
	entryHandle = combo.entry;
	listHandle = combo.list;
	
	/*
	* Feature in GTK.  There is no API to query the arrow
	* handle from a combo box although it is possible to
	* get the list and text field.  The arrow handle is needed
	* to hook events.  The fix is to find the first child that is
	* not the entry or list and assume this is the arrow handle.
	*/
	int list = OS.gtk_container_get_children (handle);
	if (list != 0) {
		int i = 0, count = OS.g_list_length (list);
		while (i<count) {
			int childHandle = OS.g_list_nth_data (list, i);
			if (childHandle != entryHandle && childHandle != listHandle) {
				arrowHandle = childHandle;
				break;
			}
			i++;
		}
		OS.g_list_free (list);
	}
	
	boolean editable = (style & SWT.READ_ONLY) == 0;
	OS.gtk_editable_set_editable (entryHandle, editable);
	OS.gtk_entry_set_activates_default (entryHandle, true);
	OS.gtk_combo_disable_activate (handle);
	OS.gtk_combo_set_case_sensitive (handle, true);
}

/**
 * Cuts the selected text.
 * <p>
 * The current selection is first copied to the
 * clipboard and then deleted from the widget.
 * </p>
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @since 2.1
 */
public void cut () {
	checkWidget ();
	OS.gtk_editable_cut_clipboard (entryHandle);
}

GdkColor defaultBackground () {
	Display display = getDisplay ();
	return display.COLOR_TEXT_BACKGROUND;
}

GdkColor defaultForeground () {
	Display display = getDisplay ();
	return display.COLOR_TEXT_FOREGROUND;
}

void deregister () {
	super.deregister ();
	if (arrowHandle != 0) WidgetTable.remove (arrowHandle);
	WidgetTable.remove (entryHandle);
	WidgetTable.remove (listHandle);
}

int fontHandle () {
	if (entryHandle != 0) return entryHandle;
	return super.fontHandle ();
}

public boolean forceFocus () {
	checkWidget();
	OS.gtk_widget_grab_focus (entryHandle);
	return OS.gtk_widget_is_focus (entryHandle);
}

boolean hasFocus () {
	if (super.hasFocus ()) return true;
	if (OS.GTK_WIDGET_HAS_FOCUS (entryHandle)) return true;
	if (OS.GTK_WIDGET_HAS_FOCUS (listHandle)) return true;
	return false;
}

void hookEvents () {
	super.hookEvents ();
	Display display = getDisplay ();
	int windowProc2 = display.windowProc2;
	int windowProc3 = display.windowProc3;
	// TODO - fix multiple selection events for one user action
	OS.g_signal_connect (listHandle, OS.select_child, windowProc3, SELECT_CHILD);
	OS.g_signal_connect_after (entryHandle, OS.changed, windowProc2, CHANGED);
	OS.g_signal_connect (entryHandle, OS.activate, windowProc2, ACTIVATE);
	int mask =
		OS.GDK_POINTER_MOTION_MASK | 
		OS.GDK_BUTTON_PRESS_MASK | OS.GDK_BUTTON_RELEASE_MASK | 
		OS.GDK_KEY_PRESS_MASK | OS.GDK_KEY_RELEASE_MASK;
	int [] handles = new int [] {arrowHandle, entryHandle, listHandle};
	for (int i=0; i<handles.length; i++) {
		int handle = handles [i];
		if (handle != 0) {
			OS.gtk_widget_add_events (handle, mask);
			OS.g_signal_connect (handle, OS.button_press_event, windowProc3, BUTTON_PRESS_EVENT);
			OS.g_signal_connect (handle, OS.button_release_event, windowProc3, BUTTON_RELEASE_EVENT);
			OS.g_signal_connect (handle, OS.motion_notify_event, windowProc3, MOTION_NOTIFY_EVENT);
			OS.g_signal_connect (handle, OS.key_press_event, windowProc3, KEY_PRESS_EVENT);
			OS.g_signal_connect (handle, OS.key_release_event, windowProc3, KEY_RELEASE_EVENT);
			OS.g_signal_connect (handle, OS.show_help, windowProc3, SHOW_HELP);
		
			/*
			* Feature in GTK.  Events such as mouse move are propagate up
			* the widget hierarchy and are seen by the parent.  This is the
			* correct GTK behavior but not correct for SWT.  The fix is to
			* hook a signal after and stop the propagation using a negative
			* event number to distinguish this case.
			*/
			OS.g_signal_connect_after (handle, OS.button_press_event, windowProc3, -BUTTON_PRESS_EVENT);
			OS.g_signal_connect_after (handle, OS.button_release_event, windowProc3, -BUTTON_RELEASE_EVENT);
			OS.g_signal_connect_after (handle, OS.motion_notify_event, windowProc3, -MOTION_NOTIFY_EVENT);
		}
	}
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
	setItems (items, true, getSelectionIndex () != index);
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
	setItems (items, true, false);
}

void enableWidget (boolean enabled) {
	OS.gtk_widget_set_sensitive (handle, enabled);
}

GdkColor getBackgroundColor () {
	return getBaseColor ();
}

GdkColor getForegroundColor () {
	return getTextColor ();
}

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
	if (!(0 <= index && index < items.length)) {
		error (SWT.ERROR_INVALID_RANGE);
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
	return items.length;
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
	return fontHeight (getFontDescription (), listHandle != 0 ? listHandle : handle);
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
	String [] result = new String [items.length];
	System.arraycopy (items, 0, result, 0, items.length);
	return result;
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
	int [] start = new int [1];
	int [] end = new int [1];
	OS.gtk_editable_get_selection_bounds (entryHandle, start, end);
	return new Point(start [0], end [0]);
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
	int address = OS.gtk_entry_get_text (entryHandle);
	int length = OS.strlen (address);
	byte [] buffer = new byte [length];
	OS.memmove (buffer, address, length);
	return new String (Converter.mbcsToWcs (null, buffer));
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
	return fontHeight (getFontDescription (), entryHandle != 0 ? entryHandle : handle) + 8;
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
	int limit = OS.gtk_entry_get_max_length (handle);
	return limit == 0 ? LIMIT : limit;
}

int gtk_activate (int widget) {
	postEvent (SWT.DefaultSelection);
	return 0;
}

int gtk_changed (int widget) {
	sendEvent (SWT.Modify);
	return 0;
}

int gtk_select_child (int list, int widget) {
	postEvent (SWT.Selection);
	return 0;
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
	for (int i=start; i<items.length; i++) {
		if (string.equals(items [i])) return i;
	}
	return -1;
}

/**
 * Pastes text from clipboard.
 * <p>
 * The selected text is deleted from the widget
 * and new text inserted from the clipboard.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @since 2.1
 */
public void paste () {
	checkWidget ();
	OS.gtk_editable_paste_clipboard (entryHandle);
}

int parentingHandle() {
	return fixedHandle;
}

void register () {
	super.register ();
	if (arrowHandle != 0) WidgetTable.put (arrowHandle, this);
	WidgetTable.put (entryHandle, this);
	WidgetTable.put (listHandle, this);
}

void releaseHandle () {
	super.releaseHandle ();
	entryHandle = listHandle = 0;
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
	if (!(0 <= index && index < items.length)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] oldItems = items;
	String [] newItems = new String [oldItems.length - 1];
	System.arraycopy (oldItems, 0, newItems, 0, index);
	System.arraycopy (oldItems, index + 1, newItems, index, oldItems.length - index - 1);
	setItems (newItems, true, true);
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
	if (!(0 <= start && start <= end && end < items.length)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] oldItems = items;
	String [] newItems = new String [oldItems.length - (end - start + 1)];
	System.arraycopy (oldItems, 0, newItems, 0, start);
	System.arraycopy (oldItems, end + 1, newItems, start, oldItems.length - end - 1);
	setItems (newItems, true, true);
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
	if (index == -1) error (SWT.ERROR_INVALID_ARGUMENT);
	remove (index);
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
	setItems (new String [0], false, false);
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
	if (index < 0 || index >= items.length) return;
	setText (items [index]);
}

void setBackgroundColor (GdkColor color) {
	super.setBackgroundColor (color);
	if (entryHandle != 0) OS.gtk_widget_modify_base (entryHandle, 0, color);
	if (listHandle != 0) OS.gtk_widget_modify_base (listHandle, 0, color);
}

boolean setBounds (int x, int y, int width, int height, boolean move, boolean resize) {
	int newHeight = (resize && (style & SWT.DROP_DOWN) != 0) ? getTextHeight () : height;
	return super.setBounds (x, y, width, newHeight, move, resize);
}

void setFontDescription (int font) {
	super.setFontDescription (font);
	if (entryHandle != 0) OS.gtk_widget_modify_font (entryHandle, font);
	if (listHandle != 0) OS.gtk_widget_modify_font (listHandle, font);
}

void setForegroundColor (GdkColor color) {
	super.setForegroundColor (color);
	if (entryHandle != 0) OS.gtk_widget_modify_text (entryHandle, 0, color);
	if (listHandle != 0) OS.gtk_widget_modify_text (listHandle, 0, color);
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
	if (!(0 <= index && index <= items.length)) {
		error (SWT.ERROR_INVALID_ARGUMENT);
	}
	items [index] = string;
	setItems (items, true, true);
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
	setItems (items, false, false);
}
	
void setItems (String [] items, boolean keepText, boolean keepSelection) {
	String[] oldItems = this.items;
	this.items = items;
	int selectedIndex = -1;
	if (keepSelection) selectedIndex = getSelectionIndex ();
	if (items.length == 0) {
		int itemsList = OS.gtk_container_get_children (listHandle);
		if (itemsList != 0) {
			int count = OS.g_list_length (itemsList);
			for (int i=count - 1; i>=0; i--) {
				int widget = OS.g_list_nth_data (itemsList, i);
				OS.gtk_container_remove (listHandle, widget);
			}
			OS.g_list_free (itemsList);
		}
	} else {
		int glist = 0;
		for (int i=0; i<items.length; i++) {
			String string = items [i];
			if (string == null) break;
			byte [] buffer = Converter.wcsToMbcs (null, string, true);
			int data = OS.g_malloc (buffer.length);
			OS.memmove (data, buffer, buffer.length);
			glist = OS.g_list_append (glist, data);
		}
		/*
		* Feature in GTK.  A call to gtk_combo_set_popdown_strings will result in the top item
		* being selected and a SELECT_CHILD and CHANGED event will be sent.  In the case
		* where there are no entires in the widget and the first item has been added, inform the
		* application that a selection has occurred.  However, in all other cases, the previous
		* selection is maintained and the application should not be notified of a selection change.
		* The fix is to allow the SELECT_CHILD and CHANGED to be sent when the first item
		* is added.
		*/
		boolean block = oldItems != null && oldItems.length > 0;
		if (block) {
			OS.g_signal_handlers_block_matched (entryHandle, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
			OS.g_signal_handlers_block_matched (listHandle, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, SELECT_CHILD);
		}
		OS.gtk_combo_set_popdown_strings (handle, glist);
		if (block) {
			OS.g_signal_handlers_unblock_matched (entryHandle, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, CHANGED);
			OS.g_signal_handlers_unblock_matched (listHandle, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, SELECT_CHILD);
		}
		if (glist != 0) {
			int count = OS.g_list_length (glist);
			for (int i=0; i<count; i++) {
				int data = OS.g_list_nth_data (glist, i);
				if (data != 0) OS.g_free (data);
			}
			OS.g_list_free (glist);
		}
	}
	if (keepSelection) select (selectedIndex);
	if (!keepText) OS.gtk_editable_delete_text (entryHandle, 0, -1);
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
	OS.gtk_editable_set_position (entryHandle, selection.x);
	OS.gtk_editable_select_region (entryHandle, selection.x, selection.y);
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
	byte [] buffer = Converter.wcsToMbcs (null, string, true);
	OS.g_signal_handlers_block_matched (listHandle, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, SELECT_CHILD);
	OS.gtk_entry_set_text (entryHandle, buffer);
	OS.g_signal_handlers_unblock_matched (listHandle, OS.G_SIGNAL_MATCH_DATA, 0, 0, 0, 0, SELECT_CHILD);
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
	OS.gtk_entry_set_max_length (entryHandle, limit);
}

boolean translateTraversal (GdkEventKey keyEvent) {
	int key = keyEvent.keyval;
	switch (key) {
		case OS.GDK_KP_Enter:
		case OS.GDK_Return: {
			int imHandle = OS.GTK_ENTRY_IM_CONTEXT (entryHandle);
			if (imHandle != 0) {
				int [] preeditString = new int [1];
				OS.gtk_im_context_get_preedit_string (imHandle, preeditString, null, null);
				if (preeditString [0] != 0) {
					int lenght = OS.strlen (preeditString [0]);
					OS.g_free (preeditString [0]);
					if (lenght != 0) return false;
				}
			}
		}
	}
	return super.translateTraversal (keyEvent);
}

}
