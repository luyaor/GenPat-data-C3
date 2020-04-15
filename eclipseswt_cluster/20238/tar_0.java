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

public class NSBox extends NSView {

public NSBox() {
	super();
}

public NSBox(int /*long*/ id) {
	super(id);
}

public NSBox(id id) {
	super(id);
}

public NSSize contentViewMargins() {
	NSSize result = new NSSize();
	OS.objc_msgSend_stret(result, this.id, OS.sel_contentViewMargins);
	return result;
}

public void setBorderType(int aType) {
	OS.objc_msgSend(this.id, OS.sel_setBorderType_, aType);
}

public void setBorderWidth(float borderWidth) {
	OS.objc_msgSend(this.id, OS.sel_setBorderWidth_, borderWidth);
}

public void setBoxType(int boxType) {
	OS.objc_msgSend(this.id, OS.sel_setBoxType_, boxType);
}

public void setContentView(NSView aView) {
	OS.objc_msgSend(this.id, OS.sel_setContentView_, aView != null ? aView.id : 0);
}

public void setContentViewMargins(NSSize offsetSize) {
	OS.objc_msgSend(this.id, OS.sel_setContentViewMargins_, offsetSize);
}

public void setTitle(NSString aString) {
	OS.objc_msgSend(this.id, OS.sel_setTitle_, aString != null ? aString.id : 0);
}

public void setTitleFont(NSFont fontObj) {
	OS.objc_msgSend(this.id, OS.sel_setTitleFont_, fontObj != null ? fontObj.id : 0);
}

public void setTitlePosition(int aPosition) {
	OS.objc_msgSend(this.id, OS.sel_setTitlePosition_, aPosition);
}

public void sizeToFit() {
	OS.objc_msgSend(this.id, OS.sel_sizeToFit);
}

}
