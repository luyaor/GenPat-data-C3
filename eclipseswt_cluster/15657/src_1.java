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
 * Instances of this class are selectable user interface
 * objects that represent a range of positive, numeric values. 
 * <p>
 * At any given moment, a given slider will have a 
 * single <em>selection</em> that is considered to be its
 * value, which is constrained to be within the range of
 * values the slider represents (that is, between its
 * <em>minimum</em> and <em>maximum</em> values).
 * </p><p>
 * Typically, sliders will be made up of five areas:
 * <ol>
 * <li>an arrow button for decrementing the value</li>
 * <li>a page decrement area for decrementing the value by a larger amount</li>
 * <li>a <em>thumb</em> for modifying the value by mouse dragging</li>
 * <li>a page increment area for incrementing the value by a larger amount</li>
 * <li>an arrow button for incrementing the value</li>
 * </ol>
 * Based on their style, sliders are either <code>HORIZONTAL</code>
 * (which have left and right facing buttons for incrementing and
 * decrementing the value) or <code>VERTICAL</code> (which have
 * up and down facing buttons for incrementing and decrementing
 * the value).
 * </p><p>
 * On some platforms, the size of the slider's thumb can be
 * varied relative to the magnitude of the range of values it
 * represents (that is, relative to the difference between its
 * maximum and minimum values). Typically, this is used to
 * indicate some proportional value such as the ratio of the
 * visible area of a document to the total amount of space that
 * it would take to display it. SWT supports setting the thumb
 * size even if the underlying platform does not, but in this
 * case the appearance of the slider will not change.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles HORIZONTAL and VERTICAL may be specified.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 *
 * @see ScrollBar
 */

public class Slider extends Control {

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
public Slider (Composite parent, int style) {
	super (parent, checkStyle (style));
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver's value changes, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * When <code>widgetSelected</code> is called, the event object detail field contains one of the following values:
 * <code>0</code> - for the end of a drag.
 * <code>SWT.DRAG</code>.
 * <code>SWT.HOME</code>.
 * <code>SWT.END</code>.
 * <code>SWT.ARROW_DOWN</code>.
 * <code>SWT.ARROW_UP</code>.
 * <code>SWT.PAGE_DOWN</code>.
 * <code>SWT.PAGE_UP</code>.
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
public void addSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection,typedListener);
	addListener (SWT.DefaultSelection,typedListener);
}

static int checkStyle (int style) {
	return checkBits (style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0);
}

void createHandle (int index) {
	state |= HANDLE;
	fixedHandle = OS.gtk_fixed_new ();
	if (fixedHandle == 0) error (SWT.ERROR_NO_HANDLES);
	OS.gtk_fixed_set_has_window (fixedHandle, true);
	int hAdjustment = OS.gtk_adjustment_new (0, 0, 100, 1, 10, 10);
	if (hAdjustment == 0) error (SWT.ERROR_NO_HANDLES);
	if ((style & SWT.HORIZONTAL) != 0) {
		handle = OS.gtk_hscrollbar_new (hAdjustment);
	} else {
		handle = OS.gtk_vscrollbar_new (hAdjustment);
	}
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
	int parentHandle = parent.parentingHandle ();
	OS.gtk_container_add (parentHandle, fixedHandle);
	OS.gtk_container_add (fixedHandle, handle);
	OS.gtk_widget_show (fixedHandle);
	OS.gtk_widget_show (handle);
}

void hookEvents () {
	super.hookEvents ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	signal_connect (hAdjustment, "value_changed", SWT.Selection, 2);
}

void register () {
	super.register ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	WidgetTable.put (hAdjustment, this);
}

void deregister () {
	super.deregister ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	WidgetTable.remove (hAdjustment);
	/*
	* This code is intentionally commented.
	*/
//	OS.gtk_object_unref (hAdjustment);
//	OS.gtk_object_destroy (hAdjustment);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	// We are interested in the preferred size.
	// The native widget gives us what it thinks the minimum reasonable
	// size; we'll say we prefer to be twice that long, and exactly
	// that wide.
	int x,y;
	Point size = super.computeSize (wHint, hHint, changed);
	if (hHint==SWT.DEFAULT) {
		x = size.x;
		if ((style & SWT.HORIZONTAL) != 0) x = 2*x;
	} else x = hHint;
	if (wHint==SWT.DEFAULT) {
		y = size.y;
		if ((style & SWT.VERTICAL) != 0) y = 2*y;
	} else y = wHint;
	
	return new Point(x,y);
}

/**
 * Returns the amount that the receiver's value will be
 * modified by when the up/down (or right/left) arrows
 * are pressed.
 *
 * @return the increment
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getIncrement () {
	checkWidget ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	return (int) adjustment.step_increment;
}

/**
 * Returns the maximum value which the receiver will allow.
 *
 * @return the maximum
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getMaximum () {
	checkWidget ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	return (int) adjustment.upper;
}

/**
 * Returns the minimum value which the receiver will allow.
 *
 * @return the minimum
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getMinimum () {
	checkWidget ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	return (int) adjustment.lower;
}

/**
 * Returns the amount that the receiver's value will be
 * modified by when the page increment/decrement areas
 * are selected.
 *
 * @return the page increment
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getPageIncrement () {
	checkWidget ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	return (int) adjustment.page_increment;
}

/**
 * Returns the single <em>selection</em> that is the receiver's value.
 *
 * @return the selection
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSelection () {
	checkWidget ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	return (int) adjustment.value;
}

/**
 * Returns the size of the receiver's thumb relative to the
 * difference between its maximum and minimum values.
 *
 * @return the thumb value
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getThumb () {
	checkWidget ();
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	return (int) adjustment.page_size;
}

int processSelection (int int0, int int1, int int2) {
	postEvent (SWT.Selection);
	return 0;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver's value changes.
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
 * Sets the amount that the receiver's value will be
 * modified by when the up/down (or right/left) arrows
 * are pressed to the argument, which must be at least 
 * one.
 *
 * @param value the new increment (must be greater than zero)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setIncrement (int value) {
	checkWidget();
	if (value < 1) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	adjustment.step_increment = (double) value;
	OS.memmove (hAdjustment, adjustment);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_changed (hAdjustment);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

/**
 * Sets the maximum value which the receiver will allow
 * to be the argument which must be greater than or
 * equal to zero.
 *
 * @param value the new maximum (must be zero or greater)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMaximum (int value) {
	checkWidget ();
	if (value < 0) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	adjustment.upper = (double) value;
	OS.memmove (hAdjustment, adjustment);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_changed (hAdjustment);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

/**
 * Sets the minimum value which the receiver will allow
 * to be the argument which must be greater than or
 * equal to zero.
 *
 * @param value the new minimum (must be zero or greater)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMinimum (int value) {
	checkWidget ();
	if (value < 0) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	adjustment.lower = (double) value;
	OS.memmove (hAdjustment, adjustment);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_changed (hAdjustment);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

/**
 * Sets the amount that the receiver's value will be
 * modified by when the page increment/decrement areas
 * are selected to the argument, which must be at least
 * one.
 *
 * @return the page increment (must be greater than zero)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setPageIncrement (int value) {
	checkWidget ();
	if (value < 1) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	adjustment.page_increment = (double) value;
	OS.memmove (hAdjustment, adjustment);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_changed (hAdjustment);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

/**
 * Sets the single <em>selection</em> that is the receiver's
 * value to the argument which must be greater than or equal
 * to zero.
 *
 * @param value the new selection (must be zero or greater)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSelection (int value) {
	checkWidget ();
	if (value < 0) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_set_value (hAdjustment, value);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

/**
 * Sets the size of the receiver's thumb relative to the
 * difference between its maximum and minimum values to the
 * argument which must be at least one.
 *
 * @param value the new thumb value (must be at least one)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see ScrollBar
 */
public void setThumb (int value) {
	checkWidget ();
	if (value < 1) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	adjustment.page_size = (double) value;
	OS.memmove (hAdjustment, adjustment);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_changed (hAdjustment);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

/**
 * Sets the receiver's selection, minimum value, maximum
 * value, thumb, increment and page increment all at once.
 * <p>
 * Note: This is equivalent to setting the values individually
 * using the appropriate methods, but may be implemented in a 
 * more efficient fashion on some platforms.
 * </p>
 *
 * @param selection the new selection value
 * @param minimum the new minimum value
 * @param maximum the new maximum value
 * @param thumb the new thumb value
 * @param increment the new increment value
 * @param pageIncrement the new pageIncrement value
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setValues (int selection, int minimum, int maximum, int thumb, int increment, int pageIncrement) {
	checkWidget ();
	if (selection < 0) return;
	if (minimum < 0) return;
	if (maximum < 0) return;
	if (thumb < 1) return;
	if (maximum - minimum - thumb < 0) return;
	if (increment < 1) return;
	if (pageIncrement < 1) return;
	int hAdjustment = OS.gtk_range_get_adjustment (handle);
	GtkAdjustment adjustment = new GtkAdjustment (hAdjustment);
	adjustment.value = (double) selection;
	adjustment.lower = (double) minimum;
	adjustment.upper = (double) maximum;
	adjustment.page_size = (double) thumb;
	adjustment.step_increment = (double) increment;
	adjustment.page_increment = (double) pageIncrement;
	OS.memmove (hAdjustment, adjustment);
	OS.gtk_signal_handler_block_by_data (hAdjustment, SWT.Selection);
	OS.gtk_adjustment_changed (hAdjustment);
	OS.gtk_adjustment_value_changed (hAdjustment);
	OS.gtk_signal_handler_unblock_by_data (hAdjustment, SWT.Selection);
}

}
