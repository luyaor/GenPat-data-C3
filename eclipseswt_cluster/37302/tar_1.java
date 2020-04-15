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

public class NSScroller extends NSControl {

public NSScroller() {
	super();
}

public NSScroller(int /*long*/ id) {
	super(id);
}

public NSScroller(id id) {
	super(id);
}

public int hitPart() {
	return OS.objc_msgSend(this.id, OS.sel_hitPart);
}

public static float scrollerWidth() {
	return (float)OS.objc_msgSend_fpret(OS.class_NSScroller, OS.sel_scrollerWidth);
}

public void setFloatValue(float aFloat, float proportion) {
	OS.objc_msgSend(this.id, OS.sel_setFloatValue_knobProportion_, aFloat, proportion);
}

}
