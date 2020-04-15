/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.cocoa;

public class NSCursor extends NSObject {

public NSCursor() {
	super();
}

public NSCursor(int /*long*/ id) {
	super(id);
}

public NSCursor(id id) {
	super(id);
}

public static NSCursor IBeamCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_IBeamCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor arrowCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_arrowCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor crosshairCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_crosshairCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public NSCursor initWithImage(NSImage newImage, NSPoint aPoint) {
	int result = OS.objc_msgSend(this.id, OS.sel_initWithImage_hotSpot_, newImage != null ? newImage.id : 0, aPoint);
	return result == this.id ? this : (result != 0 ? new NSCursor(result) : null);
}

public static NSCursor pointingHandCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_pointingHandCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor resizeDownCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_resizeDownCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor resizeLeftCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_resizeLeftCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor resizeLeftRightCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_resizeLeftRightCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor resizeRightCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_resizeRightCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor resizeUpCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_resizeUpCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public static NSCursor resizeUpDownCursor() {
	int result = OS.objc_msgSend(OS.class_NSCursor, OS.sel_resizeUpDownCursor);
	return result != 0 ? new NSCursor(result) : null;
}

public void set() {
	OS.objc_msgSend(this.id, OS.sel_set);
}

public static void setHiddenUntilMouseMoves(boolean flag) {
	OS.objc_msgSend(OS.class_NSCursor, OS.sel_setHiddenUntilMouseMoves_, flag);
}

public void setOnMouseEntered(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setOnMouseEntered_, flag);
}

}
