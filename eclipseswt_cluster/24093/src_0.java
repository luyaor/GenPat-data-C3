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

public class NSIndexSet extends NSObject {

public NSIndexSet() {
	super();
}

public NSIndexSet(int /*long*/ id) {
	super(id);
}

public NSIndexSet(id id) {
	super(id);
}

public boolean containsIndex(int /*long*/ value) {
	return OS.objc_msgSend_bool(this.id, OS.sel_containsIndex_, value);
}

public int /*long*/ count() {
	return OS.objc_msgSend(this.id, OS.sel_count);
}

public int /*long*/ firstIndex() {
	return OS.objc_msgSend(this.id, OS.sel_firstIndex);
}

public int /*long*/ getIndexes(int[] /*long[]*/ indexBuffer, int /*long*/ bufferSize, int /*long*/ range) {
	return OS.objc_msgSend(this.id, OS.sel_getIndexes_maxCount_inIndexRange_, indexBuffer, bufferSize, range);
}

public static id indexSetWithIndex(int /*long*/ value) {
	int /*long*/ result = OS.objc_msgSend(OS.class_NSIndexSet, OS.sel_indexSetWithIndex_, value);
	return result != 0 ? new id(result) : null;
}

public id initWithIndex(int /*long*/ value) {
	int /*long*/ result = OS.objc_msgSend(this.id, OS.sel_initWithIndex_, value);
	return result != 0 ? new id(result) : null;
}

public id initWithIndexesInRange(NSRange range) {
	int /*long*/ result = OS.objc_msgSend(this.id, OS.sel_initWithIndexesInRange_, range);
	return result != 0 ? new id(result) : null;
}

}
