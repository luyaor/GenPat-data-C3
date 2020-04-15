package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class allow the user to select a color
 * from a predefined set of available colors.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
public class ColorDialog extends Dialog {
	RGB rgb;
/**
 * Constructs a new instance of this class given only its parent.
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
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
public ColorDialog (Shell parent) {
	this (parent, SWT.NULL);
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
public ColorDialog (Shell parent, int style) {
	super (parent, style);
	checkSubclass ();
}
int cancelFunc (int widget, int callData) {
	OS.gtk_widget_destroy (callData);
	return 0;
}
int destroyFunc (int widget, int colorInfo) {
	OS.gtk_main_quit ();
	return 0;
}
/**
 * Returns the currently selected color in the receiver.
 *
 * @return the RGB value for the selected color, may be null
 *
 * @see PaletteData#getRGBs
 */
public RGB getRGB () {
	return rgb;
}
int okFunc (int widget, int callData) {
	double [] color = new double [4];
	OS.gtk_color_selection_get_color (OS.GTK_COLOR_SELECTION_COLORSEL(callData), color);
	rgb = new RGB ((int)(color [0] * 256), (int)(color [1] * 256), (int)(color [2] * 256));
	OS.gtk_widget_destroy (callData);
	return 0;
}
/**
 * Makes the receiver visible and brings it to the front
 * of the display.
 *
 * @return the selected color, or null if the dialog was
 *         cancelled, no color was selected, or an error
 *         occurred
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public RGB open () {
	int handle;
	byte [] titleBytes;
	titleBytes = Converter.wcsToMbcs (null, title, true);
	handle = OS.gtk_color_selection_dialog_new (titleBytes);
	if (parent!=null) {
		OS.gtk_window_set_modal(handle, true);
		OS.gtk_window_set_transient_for(handle, parent.topHandle());
	}	OS.gtk_widget_hide (OS.GTK_COLOR_SELECTION_OK_BUTTON(handle));
	if (rgb != null) {
		double [] color = new double [4];
		color [0] = (double)rgb.red / 256;
		color [1] = (double)rgb.green / 256;
		color [2] = (double)rgb.blue / 256;
		OS.gtk_color_selection_set_color (OS.GTK_COLOR_SELECTION_COLORSEL(handle), color);
	}
	Callback destroyCallback = new Callback (this, "destroyFunc", 2);
	int destroyFunc = destroyCallback.getAddress ();
	byte [] destroy = Converter.wcsToMbcs (null, "destroy", true);
	OS.gtk_signal_connect (handle, destroy, destroyFunc, handle);
	byte [] clicked = Converter.wcsToMbcs (null, "clicked", true);
	Callback okCallback = new Callback (this, "okFunc", 2);
	int okFunc = okCallback.getAddress ();
	Callback cancelCallback = new Callback (this, "cancelFunc", 2);
	int cancelFunc = cancelCallback.getAddress ();
	OS.gtk_signal_connect (OS.GTK_COLOR_SELECTION_OK_BUTTON(handle), clicked, okFunc, handle);
	OS.gtk_signal_connect (OS.GTK_COLOR_SELECTION_CANCEL_BUTTON(handle), clicked, cancelFunc, handle);
	rgb = null;
	OS.gtk_widget_show_now (handle);
	OS.gtk_main ();
	destroyCallback.dispose ();
	okCallback.dispose ();
	cancelCallback.dispose ();
	return rgb;
}
/**
 * Returns the receiver's selected color to be the argument.
 *
 * @param rgb the new RGB value for the selected color, may be
 *        null to let the platform to select a default when
 *        open() is called
 *
 * @see PaletteData#getRGBs
 */
public void setRGB (RGB rgb) {
	this.rgb = rgb;
}
}
