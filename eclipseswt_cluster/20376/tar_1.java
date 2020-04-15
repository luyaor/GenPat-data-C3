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

public class NSWindow extends NSResponder {

public NSWindow() {
	super();
}

public NSWindow(int /*long*/ id) {
	super(id);
}

public NSWindow(id id) {
	super(id);
}

public float alphaValue() {
	return (float)OS.objc_msgSend_fpret(this.id, OS.sel_alphaValue);
}

public NSPoint cascadeTopLeftFromPoint(NSPoint topLeftPoint) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_cascadeTopLeftFromPoint_, topLeftPoint);
	return result;
}

public void close() {
	OS.objc_msgSend(this.id, OS.sel_close);
}

public NSRect contentRectForFrameRect(NSRect frameRect) {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_contentRectForFrameRect_, frameRect);
	return result;
}

public NSView contentView() {
	int result = OS.objc_msgSend(this.id, OS.sel_contentView);
	return result != 0 ? new NSView(result) : null;
}

public NSPoint convertBaseToScreen(NSPoint aPoint) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertBaseToScreen_, aPoint);
	return result;
}

public NSPoint convertScreenToBase(NSPoint aPoint) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertScreenToBase_, aPoint);
	return result;
}

public NSText fieldEditor(boolean createFlag, id anObject) {
	int result = OS.objc_msgSend(this.id, OS.sel_fieldEditor_forObject_, createFlag, anObject != null ? anObject.id : 0);
	return result != 0 ? new NSText(result) : null;
}

public NSResponder firstResponder() {
	int result = OS.objc_msgSend(this.id, OS.sel_firstResponder);
	return result != 0 ? new NSResponder(result) : null;
}

public NSRect frame() {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_frame);
	return result;
}

public NSRect frameRectForContentRect(NSRect contentRect) {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_frameRectForContentRect_, contentRect);
	return result;
}

public NSGraphicsContext graphicsContext() {
	int result = OS.objc_msgSend(this.id, OS.sel_graphicsContext);
	return result != 0 ? new NSGraphicsContext(result) : null;
}

public NSWindow initWithContentRect(NSRect contentRect, int aStyle, int bufferingType, boolean flag) {
	int result = OS.objc_msgSend(this.id, OS.sel_initWithContentRect_styleMask_backing_defer_, contentRect, aStyle, bufferingType, flag);
	return result == this.id ? this : (result != 0 ? new NSWindow(result) : null);
}

public NSWindow initWithContentRect(NSRect contentRect, int aStyle, int bufferingType, boolean flag, NSScreen screen) {
	int result = OS.objc_msgSend(this.id, OS.sel_initWithContentRect_styleMask_backing_defer_screen_, contentRect, aStyle, bufferingType, flag, screen != null ? screen.id : 0);
	return result == this.id ? this : (result != 0 ? new NSWindow(result) : null);
}

public boolean isVisible() {
	return OS.objc_msgSend(this.id, OS.sel_isVisible) != 0;
}

public boolean makeFirstResponder(NSResponder aResponder) {
	return OS.objc_msgSend(this.id, OS.sel_makeFirstResponder_, aResponder != null ? aResponder.id : 0) != 0;
}

public void makeKeyAndOrderFront(id sender) {
	OS.objc_msgSend(this.id, OS.sel_makeKeyAndOrderFront_, sender != null ? sender.id : 0);
}

public NSPoint mouseLocationOutsideOfEventStream() {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_mouseLocationOutsideOfEventStream);
	return result;
}

public void orderFront(id sender) {
	OS.objc_msgSend(this.id, OS.sel_orderFront_, sender != null ? sender.id : 0);
}

public void orderFrontRegardless() {
	OS.objc_msgSend(this.id, OS.sel_orderFrontRegardless);
}

public void orderOut(id sender) {
	OS.objc_msgSend(this.id, OS.sel_orderOut_, sender != null ? sender.id : 0);
}

public NSScreen screen() {
	int result = OS.objc_msgSend(this.id, OS.sel_screen);
	return result != 0 ? new NSScreen(result) : null;
}

public void setAcceptsMouseMovedEvents(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setAcceptsMouseMovedEvents_, flag);
}

public void setAlphaValue(float windowAlpha) {
	OS.objc_msgSend(this.id, OS.sel_setAlphaValue_, windowAlpha);
}

public void setBackgroundColor(NSColor color) {
	OS.objc_msgSend(this.id, OS.sel_setBackgroundColor_, color != null ? color.id : 0);
}

public void setContentView(NSView aView) {
	OS.objc_msgSend(this.id, OS.sel_setContentView_, aView != null ? aView.id : 0);
}

public void setDelegate(id anObject) {
	OS.objc_msgSend(this.id, OS.sel_setDelegate_, anObject != null ? anObject.id : 0);
}

public void setFrame(NSRect frameRect, boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setFrame_display_, frameRect, flag);
}

public void setHasShadow(boolean hasShadow) {
	OS.objc_msgSend(this.id, OS.sel_setHasShadow_, hasShadow);
}

public void setLevel(int newLevel) {
	OS.objc_msgSend(this.id, OS.sel_setLevel_, newLevel);
}

public void setOpaque(boolean isOpaque) {
	OS.objc_msgSend(this.id, OS.sel_setOpaque_, isOpaque);
}

public void setTitle(NSString aString) {
	OS.objc_msgSend(this.id, OS.sel_setTitle_, aString != null ? aString.id : 0);
}

public int styleMask() {
	return OS.objc_msgSend(this.id, OS.sel_styleMask);
}

public int windowNumber() {
	return OS.objc_msgSend(this.id, OS.sel_windowNumber);
}

}
