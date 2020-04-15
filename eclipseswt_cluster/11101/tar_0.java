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

public class NSImageView extends NSControl {

public NSImageView() {
	super();
}

public NSImageView(int /*long*/ id) {
	super(id);
}

public NSImageView(id id) {
	super(id);
}

public void setImage(NSImage newImage) {
	OS.objc_msgSend(this.id, OS.sel_setImage_, newImage != null ? newImage.id : 0);
}

public void setImageAlignment(int newAlign) {
	OS.objc_msgSend(this.id, OS.sel_setImageAlignment_, newAlign);
}

public void setImageScaling(int newScaling) {
	OS.objc_msgSend(this.id, OS.sel_setImageScaling_, newScaling);
}

}
