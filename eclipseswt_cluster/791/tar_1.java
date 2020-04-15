package org.eclipse.swt.widgets;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 * Note: Only one of the styles DROP_DOWN and SIMPLE 
 * may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see List
 */
public class Combo extends Composite {
	int entryHandle, listHandle;
	int glist;
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
	String [] items = getItems ();
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
	if (!(0 <= index && index <= getItemCount ())) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] items = getItems ();
	String [] newItems = new String [items.length + 1];
	System.arraycopy (items, 0, newItems, 0, items.length);
	newItems [index] = string;
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
	width = (requisition.width - entryRequesition.width) + listRequesition.width;
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
	boolean editable = (style & SWT.READ_ONLY) == 0;
	OS.gtk_entry_set_editable (entryHandle, editable);
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
	return hasFocus ();
}

boolean hasFocus () {
	if (super.hasFocus ()) return true;
	if (OS.GTK_WIDGET_HAS_FOCUS (entryHandle)) return true;
	if (OS.GTK_WIDGET_HAS_FOCUS (listHandle)) return true;
	return false;
}

void hookEvents () {
	// TO DO - expose, enter/exit, focus in/out
	super.hookEvents ();
	Display display = getDisplay ();
	int windowProc2 = display.windowProc2;
	int windowProc3 = display.windowProc3;
	// TO DO - fix multiple selection events for one user action
	OS.gtk_signal_connect (listHandle, OS.select_child, windowProc3, SWT.Selection);
	OS.gtk_signal_connect_after (entryHandle, OS.changed, windowProc2, SWT.Modify);
	OS.gtk_signal_connect (entryHandle, OS.activate, windowProc2, SWT.DefaultSelection);
	int mask =
		OS.GDK_POINTER_MOTION_MASK | 
		OS.GDK_BUTTON_PRESS_MASK | OS.GDK_BUTTON_RELEASE_MASK | 
		OS.GDK_KEY_PRESS_MASK | OS.GDK_KEY_RELEASE_MASK;
	//FIXME - missing button handle
	int [] handles = new int [] {entryHandle, listHandle, /*combo.button*/};
	for (int i=0; i<handles.length; i++) {
		int handle = handles [i];
		OS.gtk_widget_add_events (handle, mask);
		OS.gtk_signal_connect (handle, OS.button_press_event, windowProc3, SWT.MouseDown);
		OS.gtk_signal_connect (handle, OS.button_release_event, windowProc3, SWT.MouseUp);
		OS.gtk_signal_connect (handle, OS.key_press_event, windowProc3, SWT.KeyDown);
		OS.gtk_signal_connect (handle, OS.key_release_event, windowProc3, SWT.KeyUp);
		OS.gtk_signal_connect (handle, OS.motion_notify_event, windowProc3, SWT.MouseMove);
		OS.gtk_signal_connect_after (handle, OS.button_press_event, windowProc3, -SWT.MouseDown);
		OS.gtk_signal_connect_after (handle, OS.button_release_event, windowProc3, -SWT.MouseUp);
		OS.gtk_signal_connect_after (handle, OS.key_press_event, windowProc3, -SWT.KeyDown);
		OS.gtk_signal_connect_after (handle, OS.key_release_event, windowProc3, -SWT.KeyUp);
		OS.gtk_signal_connect_after (handle, OS.motion_notify_event, windowProc3, -SWT.MouseMove);
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
	setItems (getItems (), true, getSelectionIndex () != index);
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
	setItems (getItems (), true, false);
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
	if (!(0 <= index && index < getItemCount ())) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] items = getItems ();
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
	int font = getFontDescription ();
	int context = OS.gtk_widget_get_pango_context (listHandle != 0 ? listHandle : handle);
	int lang = OS.pango_context_get_language (context);
	int metrics = OS.pango_context_get_metrics (context, font, lang);
	int ascent = OS.PANGO_PIXELS (OS.pango_font_metrics_get_ascent (metrics));
	int descent = OS.PANGO_PIXELS (OS.pango_font_metrics_get_descent (metrics));
	OS.pango_font_metrics_unref (metrics);
	return ascent + descent;
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
	/*
	 * The GTK documentation explicitly states
	 * that this address should not be freed.
	 */
	int address = OS.gtk_entry_get_text (entryHandle);
	int length = OS.strlen (address);
	byte [] buffer1 = new byte [length];
	OS.memmove (buffer1, address, length);
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
	int font = getFontDescription ();
	int context = OS.gtk_widget_get_pango_context (entryHandle != 0 ? entryHandle : handle);
	int lang = OS.pango_context_get_language (context);
	int metrics = OS.pango_context_get_metrics (context, font, lang);
	int ascent = OS.PANGO_PIXELS (OS.pango_font_metrics_get_ascent (metrics));
	int descent = OS.PANGO_PIXELS (OS.pango_font_metrics_get_descent (metrics));
	OS.pango_font_metrics_unref (metrics);
	return 8 + ascent + descent;
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

int processDefaultSelection (int int0, int int1, int int2) {
	postEvent (SWT.DefaultSelection);
	return 0;
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
	WidgetTable.put (entryHandle, this);
	WidgetTable.put (listHandle, this);
}

void releaseHandle () {
	super.releaseHandle ();
	entryHandle = listHandle = 0;
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
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] oldItems = getItems ();
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
	if (!(0 <= start && start <= end && end < getItemCount ())) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	String [] oldItems = getItems ();
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
	if (index < 0 || index >= getItemCount ()) return;
	OS.gtk_signal_handler_block_by_data (listHandle, SWT.Selection);
	OS.gtk_list_select_item (listHandle, index);
	OS.gtk_signal_handler_unblock_by_data (listHandle, SWT.Selection);
}

void setBackgroundColor (GdkColor color) {
	super.setBackgroundColor (color);
	if (entryHandle != 0) OS.gtk_widget_modify_base (entryHandle, 0, color);
	if (listHandle != 0) OS.gtk_widget_modify_base (listHandle, 0, color);
}

boolean setBounds (int x, int y, int width, int height, boolean move, boolean resize) {
	int newHeight = (resize && (style & SWT.DROP_DOWN) != 0) ? getTextHeight() : height;
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
	if (!(0 <= index && index <= getItemCount ())) {
		error (SWT.ERROR_INVALID_ARGUMENT);
	}
	String [] items = getItems ();
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
	int selectedIndex = -1;
	if (keepSelection) selectedIndex = getSelectionIndex();
	if (items.length == 0) {
		OS.gtk_list_clear_items (listHandle, 0, -1);
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
		OS.gtk_signal_handler_block_by_data (entryHandle, SWT.Modify);
		OS.gtk_signal_handler_block_by_data (listHandle, SWT.Selection);
		OS.gtk_combo_set_popdown_strings (handle, new_glist);
		OS.gtk_signal_handler_unblock_by_data (entryHandle, SWT.Modify);
		OS.gtk_signal_handler_unblock_by_data (listHandle, SWT.Selection);
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
	int [] position = new int [1];
	byte [] buffer = Converter.wcsToMbcs (null, string);
	OS.gtk_signal_handler_block_by_data (listHandle, SWT.Selection);
	OS.gtk_editable_delete_text (entryHandle, 0, -1);
	OS.gtk_editable_insert_text (entryHandle, buffer, buffer.length, position);
	OS.gtk_editable_set_position (entryHandle, 0);
	OS.gtk_signal_handler_unblock_by_data (listHandle, SWT.Selection);
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

}
