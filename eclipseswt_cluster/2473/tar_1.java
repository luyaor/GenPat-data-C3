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

public class NSView extends NSResponder {

public NSView() {
	super();
}

public NSView(int /*long*/ id) {
	super(id);
}

public NSView(id id) {
	super(id);
}

public void addSubview(NSView aView) {
	OS.objc_msgSend(this.id, OS.sel_addSubview_, aView != null ? aView.id : 0);
}

public void addSubview(NSView aView, int place, NSView otherView) {
	OS.objc_msgSend(this.id, OS.sel_addSubview_positioned_relativeTo_, aView != null ? aView.id : 0, place, otherView != null ? otherView.id : 0);
}

public void beginDocument() {
	OS.objc_msgSend(this.id, OS.sel_beginDocument);
}

public void beginPageInRect(NSRect aRect, NSPoint location) {
	OS.objc_msgSend(this.id, OS.sel_beginPageInRect_atPlacement_, aRect, location);
}

public NSRect bounds() {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_bounds);
	return result;
}

public NSPoint convertPoint_fromView_(NSPoint aPoint, NSView aView) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertPoint_fromView_, aPoint, aView != null ? aView.id : 0);
	return result;
}

public NSPoint convertPoint_toView_(NSPoint aPoint, NSView aView) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertPoint_toView_, aPoint, aView != null ? aView.id : 0);
	return result;
}

public NSPoint convertPointFromBase(NSPoint aPoint) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertPointFromBase_, aPoint);
	return result;
}

public NSPoint convertPointToBase(NSPoint aPoint) {
	NSPoint result = new NSPoint();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertPointToBase_, aPoint);
	return result;
}

public NSRect convertRect_fromView_(NSRect aRect, NSView aView) {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertRect_fromView_, aRect, aView != null ? aView.id : 0);
	return result;
}

public NSRect convertRect_toView_(NSRect aRect, NSView aView) {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertRect_toView_, aRect, aView != null ? aView.id : 0);
	return result;
}

public NSRect convertRectFromBase(NSRect aRect) {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertRectFromBase_, aRect);
	return result;
}

public NSRect convertRectToBase(NSRect aRect) {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertRectToBase_, aRect);
	return result;
}

public NSSize convertSize_fromView_(NSSize aSize, NSView aView) {
	NSSize result = new NSSize();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertSize_fromView_, aSize, aView != null ? aView.id : 0);
	return result;
}

public NSSize convertSize_toView_(NSSize aSize, NSView aView) {
	NSSize result = new NSSize();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertSize_toView_, aSize, aView != null ? aView.id : 0);
	return result;
}

public NSSize convertSizeFromBase(NSSize aSize) {
	NSSize result = new NSSize();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertSizeFromBase_, aSize);
	return result;
}

public NSSize convertSizeToBase(NSSize aSize) {
	NSSize result = new NSSize();
	OS.objc_msgSend_stret(result, this.id, OS.sel_convertSizeToBase_, aSize);
	return result;
}

public void displayIfNeeded() {
	OS.objc_msgSend(this.id, OS.sel_displayIfNeeded);
}

public void drawRect(NSRect rect) {
	OS.objc_msgSend(this.id, OS.sel_drawRect_, rect);
}

public void endDocument() {
	OS.objc_msgSend(this.id, OS.sel_endDocument);
}

public void endPage() {
	OS.objc_msgSend(this.id, OS.sel_endPage);
}

public NSRect frame() {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_frame);
	return result;
}

public NSView hitTest(NSPoint aPoint) {
	int result = OS.objc_msgSend(this.id, OS.sel_hitTest_, aPoint);
	return result == this.id ? this : (result != 0 ? new NSView(result) : null);
}

public NSView initWithFrame(NSRect frameRect) {
	int result = OS.objc_msgSend(this.id, OS.sel_initWithFrame_, frameRect);
	return result == this.id ? this : (result != 0 ? new NSView(result) : null);
}

public boolean isFlipped() {
	return OS.objc_msgSend(this.id, OS.sel_isFlipped) != 0;
}

public boolean isOpaque() {
	return OS.objc_msgSend(this.id, OS.sel_isOpaque) != 0;
}

public NSMenu menuForEvent(NSEvent event) {
	int result = OS.objc_msgSend(this.id, OS.sel_menuForEvent_, event != null ? event.id : 0);
	return result != 0 ? new NSMenu(result) : null;
}

public void removeFromSuperview() {
	OS.objc_msgSend(this.id, OS.sel_removeFromSuperview);
}

public boolean scrollRectToVisible(NSRect aRect) {
	return OS.objc_msgSend(this.id, OS.sel_scrollRectToVisible_, aRect) != 0;
}

public void setAutoresizesSubviews(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setAutoresizesSubviews_, flag);
}

public void setAutoresizingMask(int mask) {
	OS.objc_msgSend(this.id, OS.sel_setAutoresizingMask_, mask);
}

public void setFocusRingType(int focusRingType) {
	OS.objc_msgSend(this.id, OS.sel_setFocusRingType_, focusRingType);
}

public void setFrame(NSRect frameRect) {
	OS.objc_msgSend(this.id, OS.sel_setFrame_, frameRect);
}

public void setFrameOrigin(NSPoint newOrigin) {
	OS.objc_msgSend(this.id, OS.sel_setFrameOrigin_, newOrigin);
}

public void setFrameSize(NSSize newSize) {
	OS.objc_msgSend(this.id, OS.sel_setFrameSize_, newSize);
}

public void setHidden(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setHidden_, flag);
}

public void setNeedsDisplay(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setNeedsDisplay_, flag);
}

public void setNeedsDisplayInRect(NSRect invalidRect) {
	OS.objc_msgSend(this.id, OS.sel_setNeedsDisplayInRect_, invalidRect);
}

public void setToolTip(NSString string) {
	OS.objc_msgSend(this.id, OS.sel_setToolTip_, string != null ? string.id : 0);
}

public NSArray subviews() {
	int result = OS.objc_msgSend(this.id, OS.sel_subviews);
	return result != 0 ? new NSArray(result) : null;
}

public NSView superview() {
	int result = OS.objc_msgSend(this.id, OS.sel_superview);
	return result == this.id ? this : (result != 0 ? new NSView(result) : null);
}

public NSRect visibleRect() {
	NSRect result = new NSRect();
	OS.objc_msgSend_stret(result, this.id, OS.sel_visibleRect);
	return result;
}

public NSWindow window() {
	int result = OS.objc_msgSend(this.id, OS.sel_window);
	return result != 0 ? new NSWindow(result) : null;
}

}
