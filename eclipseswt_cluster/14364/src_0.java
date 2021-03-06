package org.eclipse.swt.widgets;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.internal.motif.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class provide a surface for drawing
 * arbitrary graphics.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * This class may be subclassed by custom control implementors
 * who are building controls that are <em>not</em> constructed
 * from aggregates of other controls. That is, they are either
 * painted using SWT graphics calls or are handled by native
 * methods.
 * </p>
 *
 * @see Composite
 */
public class Canvas extends Composite {
	Caret caret;
	
Canvas () {
	/* Do nothing */
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
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Canvas (Composite parent, int style) {
	super (parent, style);
}
/**
 * Returns the caret.
 * <p>
 * The caret for the control is automatically hidden
 * and shown when the control is painted or resized,
 * when focus is gained or lost and when an the control
 * is scrolled.  To avoid drawing on top of the caret,
 * the programmer must hide and show the caret when
 * drawing in the window any other time.
 * </p>
 *
 * @return the caret
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Caret getCaret () {
	checkWidget();
	return caret;
}

short [] getIMECaretPos () {
	if (caret == null) return super.getIMECaretPos ();
	int width = caret.width;
	if (width <= 0) width = 2;
	return new short[]{(short) (caret.x + width), (short) (caret.y + caret.height)};
}
int processFocusIn () {
	int result = super.processFocusIn ();
	if (caret != null) caret.setFocus ();
	return result;
}
int processFocusOut () {
	int result = super.processFocusOut ();
	if (caret != null) caret.killFocus ();
	return result;
}

int processPaint (int callData) {
	boolean isFocus = caret != null && caret.isFocusCaret ();
	if (isFocus) caret.killFocus ();
	int result = super.processPaint (callData);
	if (isFocus) caret.setFocus ();
	return result;
}

void redrawWidget (int x, int y, int width, int height, boolean all) {
	boolean isFocus = caret != null && caret.isFocusCaret ();
	if (isFocus) caret.killFocus ();
	super.redrawWidget (x, y, width, height, all);
	if (isFocus) caret.setFocus ();
}

void releaseWidget () {
	if (caret != null) caret.releaseResources ();
	caret = null;
	super.releaseWidget();
}

/**
 * Scrolls a rectangular area of the receiver by first copying 
 * the source area to the destination and then causing the area
 * of the source which is not covered by the destination to
 * be repainted. Children that intersect the rectangle are
 * optionally moved during the operation. In addition, outstanding
 * paint events are flushed before the source area is copied to
 * ensure that the contents of the canvas are drawn correctly.
 *
 * @param destX the x coordinate of the destination
 * @param destY the y coordinate of the destination
 * @param x the x coordinate of the source
 * @param y the y coordinate of the source
 * @param width the width of the area
 * @param height the height of the area
 * @param all <code>true</code>if children should be scrolled, and <code>false</code> otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void scroll (int destX, int destY, int x, int y, int width, int height, boolean all) {
	checkWidget();
	if (width <= 0 || height <= 0) return;
	int deltaX = destX - x, deltaY = destY - y;
	if (deltaX == 0 && deltaY == 0) return;
	if (!isVisible ()) return;
		
	/* Hide the caret */
	boolean isFocus = caret != null && caret.isFocusCaret ();
	if (isFocus) caret.killFocus ();
	
	/* Flush outstanding exposes */
	int xDisplay = OS.XtDisplay (handle);
	if (xDisplay == 0) return;
	int xWindow = OS.XtWindow (handle);
	if (xWindow == 0) return;
	XAnyEvent xEvent = new XAnyEvent ();
	OS.XSync (xDisplay, false);  OS.XSync (xDisplay, false);
	while (OS.XCheckWindowEvent (xDisplay, xWindow, OS.ExposureMask, xEvent)) {
		OS.XtDispatchEvent (xEvent);
	}

	/* Scroll the window */
	int xGC = OS.XCreateGC (xDisplay, xWindow, 0, null);
	OS.XCopyArea (xDisplay, xWindow, xWindow, xGC, x, y, width, height, destX, destY);
	OS.XFreeGC (xDisplay, xGC);
	boolean disjoint = (destX + width < x) || (x + width < destX) || (destY + height < y) || (y + height < destY);
	if (disjoint) {
		OS.XClearArea (xDisplay, xWindow, x, y, width, height, true);
	} else {
		if (deltaX != 0) {
			int newX = destX - deltaX;
			if (deltaX < 0) newX = destX + width;
			OS.XClearArea (xDisplay, xWindow, newX, y, Math.abs (deltaX), height, true);
		}
		if (deltaY != 0) {
			int newY = destY - deltaY;
			if (deltaY < 0) newY = destY + height;
			OS.XClearArea (xDisplay, xWindow, x, newY, width, Math.abs (deltaY), true);
		}
	}
	
	/* Show the caret */
	if (isFocus) caret.setFocus ();
}
/**
 * Sets the receiver's caret.
 * <p>
 * The caret for the control is automatically hidden
 * and shown when the control is painted or resized,
 * when focus is gained or lost and when an the control
 * is scrolled.  To avoid drawing on top of the caret,
 * the programmer must hide and show the caret when
 * drawing in the window any other time.
 * </p>
 * @param caret the new caret for the receiver, may be null
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the caret has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setCaret (Caret caret) {
	checkWidget();
	Caret newCaret = caret;
	Caret oldCaret = this.caret;
	this.caret = newCaret;
	if (hasFocus ()) {
		if (oldCaret != null) oldCaret.killFocus ();
		if (newCaret != null) {
			if (newCaret.isDisposed()) error(SWT.ERROR_INVALID_ARGUMENT);
			newCaret.setFocus ();
		}
	}
}
boolean setBounds (int x, int y, int width, int height, boolean move, boolean resize) {
	boolean isFocus = caret != null && caret.isFocusCaret ();
	if (isFocus) caret.killFocus ();
	boolean changed = super.setBounds (x, y, width, height, move, resize);
	if (isFocus) caret.setFocus ();
	return changed;
}
public void setFont (Font font) {
	checkWidget();
	super.setFont (font);
	if (caret != null) caret.setFont (font);
}
void updateCaret () {
	if (caret == null) return;
	if (!OS.IsDBLocale) return;
	short [] point = getIMECaretPos ();
	int ptr = OS.XtMalloc (4);
	OS.memmove (ptr, point, 4);
	int[] argList = {OS.XmNspotLocation, ptr};
	OS.XmImSetValues (handle, argList, argList.length / 2);
	if (ptr != 0) OS.XtFree (ptr);
}
}
