package org.eclipse.swt.graphics;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.internal.carbon.*;
import org.eclipse.swt.*;

/**
 * Instances of this class manage operating system resources that
 * specify the appearance of the on-screen pointer. To create a
 * cursor you specify the device and either a simple cursor style
 * describing one of the standard operating system provided cursors
 * or the image and mask data for the desired appearance.
 * <p>
 * Application code must explicitly invoke the <code>Cursor.dispose()</code> 
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>
 *   CURSOR_ARROW, CURSOR_WAIT, CURSOR_CROSS, CURSOR_APPSTARTING, CURSOR_HELP,
 *   CURSOR_SIZEALL, CURSOR_SIZENESW, CURSOR_SIZENS, CURSOR_SIZENWSE, CURSOR_SIZEWE,
 *   CURSOR_SIZEN, CURSOR_SIZES, CURSOR_SIZEE, CURSOR_SIZEW, CURSOR_SIZENE, CURSOR_SIZESE,
 *   CURSOR_SIZESW, CURSOR_SIZENW, CURSOR_UPARROW, CURSOR_IBEAM, CURSOR_NO, CURSOR_HAND
 * </dd>
 * </dl>
 * <p>
 * Note: Only one of the above styles may be specified.
 * </p>
 */

public final class Cursor {
	
	/**
	 * the handle to the OS cursor resource
	 * (Warning: This field is platform dependent)
	 */
	public int handle;
	
	/**
	 * the device where this cursor was created
	 */
	Device device;
	
	/**
	 * data and mask used to create a Resize NS Cursor
	 */
	static final short [] SIZENS_SOURCE = new short[] {
		(short)0x0000,
		(short)0x0180,
		(short)0x03C0,
		(short)0x07E0,
		(short)0x0180,
		(short)0x0180,
		(short)0x0180,
	 	(short)0x7FFE,
	 	(short)0x7FFE,
		(short)0x0180,
		(short)0x0180,
		(short)0x0180,
		(short)0x07E0,
		(short)0x03C0,
		(short)0x0180,
		(short)0x0000,
	};
	static final short [] SIZENS_MASK = new short[] {
		(short)0x0180,
		(short)0x03C0,
		(short)0x07E0,
		(short)0x0FF0,
		(short)0x0FF0,
		(short)0x03C0,
		(short)0xFFFF,
	 	(short)0xFFFF,
	 	(short)0xFFFF,
		(short)0xFFFF,
		(short)0x03C0,
		(short)0x0FF0,
		(short)0x0FF0,
		(short)0x07E0,
		(short)0x03C0,
		(short)0x0180,
	};
	
/**
 * Prevents uninitialized instances from being created outside the package.
 */
Cursor() {
}

/**	 
 * Constructs a new cursor given a device and a style
 * constant describing the desired cursor appearance.
 * <p>
 * You must dispose the cursor when it is no longer required. 
 * </p>
 *
 * @param device the device on which to allocate the cursor
 * @param style the style of cursor to allocate
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_INVALID_ARGUMENT - when an unknown style is specified</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if a handle could not be obtained for cursor creation</li>
 * </ul>
 *
 * @see SWT#CURSOR_ARROW
 * @see SWT#CURSOR_WAIT
 * @see SWT#CURSOR_CROSS
 * @see SWT#CURSOR_APPSTARTING
 * @see SWT#CURSOR_HELP
 * @see SWT#CURSOR_SIZEALL
 * @see SWT#CURSOR_SIZENESW
 * @see SWT#CURSOR_SIZENS
 * @see SWT#CURSOR_SIZENWSE
 * @see SWT#CURSOR_SIZEWE
 * @see SWT#CURSOR_SIZEN
 * @see SWT#CURSOR_SIZES
 * @see SWT#CURSOR_SIZEE
 * @see SWT#CURSOR_SIZEW
 * @see SWT#CURSOR_SIZENE
 * @see SWT#CURSOR_SIZESE
 * @see SWT#CURSOR_SIZESW
 * @see SWT#CURSOR_SIZENW
 * @see SWT#CURSOR_UPARROW
 * @see SWT#CURSOR_IBEAM
 * @see SWT#CURSOR_NO
 * @see SWT#CURSOR_HAND
 */
public Cursor(Device device, int style) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	this.device = device;
	switch (style) {
		case SWT.CURSOR_HAND: 			handle = OS.kThemePointingHandCursor; break;
		case SWT.CURSOR_ARROW: 		handle = OS.kThemeArrowCursor; break;
		case SWT.CURSOR_WAIT: 			handle = OS.kThemeSpinningCursor; break;
		case SWT.CURSOR_CROSS: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_APPSTARTING: 	handle = OS.kThemeWatchCursor; break;
		case SWT.CURSOR_HELP: 			handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZEALL: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZENESW: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZENS: {
			org.eclipse.swt.internal.carbon.Cursor cursor = new org.eclipse.swt.internal.carbon.Cursor();
			cursor.data = SIZENS_SOURCE;
			cursor.mask = SIZENS_MASK;
			cursor.hotSpot_h = 7;
			cursor.hotSpot_v = 7;
			handle = OS.NewPtr(org.eclipse.swt.internal.carbon.Cursor.sizeof);
			if (handle == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			OS.memcpy(handle, cursor, org.eclipse.swt.internal.carbon.Cursor.sizeof);	
	 		break;
		}
		case SWT.CURSOR_SIZENWSE: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZEWE: 		handle = OS.kThemeResizeLeftRightCursor; break;
		case SWT.CURSOR_SIZEN: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZES: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZEE: 		handle = OS.kThemeResizeRightCursor; break;
		case SWT.CURSOR_SIZEW: 		handle = OS.kThemeResizeLeftCursor; break;
		case SWT.CURSOR_SIZENE: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZESE: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZESW: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_SIZENW: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_UPARROW: 		handle = OS.kThemeCrossCursor; break;
		case SWT.CURSOR_IBEAM: 		handle = OS.kThemeIBeamCursor; break;
		case SWT.CURSOR_NO: 			handle = OS.kThemeNotAllowedCursor; break;
		default:
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
}

/**	 
 * Constructs a new cursor given a device, image and mask
 * data describing the desired cursor appearance, and the x
 * and y co-ordinates of the <em>hotspot</em> (that is, the point
 * within the area covered by the cursor which is considered
 * to be where the on-screen pointer is "pointing").
 * <p>
 * The mask data is allowed to be null, but in this case the source
 * must be an ImageData representing an icon that specifies both
 * color data and mask data.
 * <p>
 * You must dispose the cursor when it is no longer required. 
 * </p>
 *
 * @param device the device on which to allocate the cursor
 * @param source the color data for the cursor
 * @param mask the mask data for the cursor (or null)
 * @param hotspotX the x coordinate of the cursor's hotspot
 * @param hotspotY the y coordinate of the cursor's hotspot
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if device is null and there is no current device</li>
 *    <li>ERROR_NULL_ARGUMENT - if the source is null</li>
 *    <li>ERROR_NULL_ARGUMENT - if the mask is null and the source does not have a mask</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the source and the mask are not the same 
 *          size, or either is not of depth one, or if the hotspot is outside 
 *          the bounds of the image</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if a handle could not be obtained for cursor creation</li>
 * </ul>
 */
public Cursor(Device device, ImageData source, ImageData mask, int hotspotX, int hotspotY) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	this.device = device;
	if (source == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (mask == null) {
		if (source.getTransparencyType() != SWT.TRANSPARENCY_MASK) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		mask = source.getTransparencyMask();
	}
	/* Check the bounds. Mask must be the same size as source */
	if (mask.width != source.width || mask.height != source.height) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	/* Check color depths */
	if (mask.depth != 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (source.depth != 1) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	/* Check the hotspots */
	if (hotspotX >= source.width || hotspotX < 0 ||
		hotspotY >= source.height || hotspotY < 0) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	/* Create the cursor */
	org.eclipse.swt.internal.carbon.Cursor cursor = new org.eclipse.swt.internal.carbon.Cursor();
	int width = Math.min(16, source.width);
	int height = Math.min(16, source.height);
	short[] srcData = cursor.data;
	short[] maskData = cursor.mask;
	for (int y= 0; y < height; y++) {
		short d = 0, m = 0;
		for (int x= 0; x < width; x++) {
			int bit= 1 >> x;
			if (source.getPixel(x, y) != 0) d |= bit;
			if (mask.getPixel(x, y) != 0) m |= bit;
		}
		srcData[y] = d;
		maskData[y] = m;
	}
	cursor.hotSpot_h = (short)Math.min(16, hotspotX);
	cursor.hotSpot_v = (short)Math.min(16, hotspotY);
	handle = OS.NewPtr(org.eclipse.swt.internal.carbon.Cursor.sizeof);
	if (handle == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	OS.memcpy(handle, cursor, org.eclipse.swt.internal.carbon.Cursor.sizeof);
}

/**
 * Disposes of the operating system resources associated with
 * the cursor. Applications must dispose of all cursors which
 * they allocate.
 */
public void dispose () {
	if (handle == -1) return;
	if (device.isDisposed()) return;
	switch (handle) {
		case OS.kThemePointingHandCursor:
		case OS.kThemeArrowCursor:
		case OS.kThemeSpinningCursor:
		case OS.kThemeCrossCursor:
		case OS.kThemeWatchCursor:
		case OS.kThemeIBeamCursor:
		case OS.kThemeNotAllowedCursor:
		case OS.kThemeResizeLeftRightCursor:
		case OS.kThemeResizeLeftCursor:
		case OS.kThemeResizeRightCursor:
			break;
		default:
			OS.DisposePtr(handle);
	}
	handle = -1;
	device = null;
}

/**
 * Compares the argument to the receiver, and returns true
 * if they represent the <em>same</em> object using a class
 * specific comparison.
 *
 * @param object the object to compare with this object
 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
 *
 * @see #hashCode
 */
public boolean equals (Object object) {
	if (object == this) return true;
	if (!(object instanceof Cursor)) return false;
	Cursor cursor = (Cursor) object;
	return device == cursor.device && handle == cursor.handle;
}

/**
 * Returns an integer hash code for the receiver. Any two 
 * objects which return <code>true</code> when passed to 
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @see #equals
 */
public int hashCode () {
	return handle;
}

/**
 * Returns <code>true</code> if the cursor has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the cursor.
 * When a cursor has been disposed, it is an error to
 * invoke any other method using the cursor.
 *
 * @return <code>true</code> when the cursor is disposed and <code>false</code> otherwise
 */
public boolean isDisposed() {
	return handle == -1;
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the receiver
 */
public String toString () {
	if (isDisposed()) return "Cursor {*DISPOSED*}";
	return "Cursor {" + handle + "}";
}

/**	 
 * Invokes platform specific functionality to allocate a new cursor.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Cursor</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param device the device on which to allocate the color
 * @param handle the handle for the cursor
 * 
 * @private
 */
public static Cursor carbon_new(Device device, int handle) {
	if (device == null) device = Device.getDevice();
	Cursor cursor = new Cursor();
	cursor.handle = handle;
	cursor.device = device;
	return cursor;
}

}
