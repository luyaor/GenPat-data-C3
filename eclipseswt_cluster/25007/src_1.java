/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;


import org.eclipse.swt.*;
import org.eclipse.swt.internal.win32.*;

/**
 * Instances of this class represent the system tray that is part
 * of the task bar status area on some operating systems.
 * 
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * 
 * @see Display#getSystemTray
 * 
 * @since 3.0
 */
public class Tray extends Widget {
	int itemCount;
	TrayItem [] items = new TrayItem [4];

Tray (Display display, int style) {
	if (display == null) display = Display.getCurrent ();
	if (display == null) display = Display.getDefault ();
	if (!display.isValidThread ()) {
		error (SWT.ERROR_THREAD_INVALID_ACCESS);
	}
	this.display = display;
}
	
void createItem (TrayItem item, int index) {
	if (!(0 <= index && index <= itemCount)) error (SWT.ERROR_INVALID_RANGE);
	if (itemCount == items.length) {
		TrayItem [] newItems = new TrayItem [items.length + 4];
		System.arraycopy (items, 0, newItems, 0, items.length);
		items = newItems;
	}
	if (!OS.IsWinCE) {
		NOTIFYICONDATA iconData = OS.IsUnicode ? (NOTIFYICONDATA) new NOTIFYICONDATAW () : new NOTIFYICONDATAA ();
		iconData.cbSize = NOTIFYICONDATA.sizeof;
		iconData.uID = display.nextTrayId++;
		iconData.hWnd = display.hwndMessage;
		iconData.uFlags = OS.NIF_MESSAGE;
		iconData.uCallbackMessage = Display.SWT_TRAYICONMSG;
		OS.Shell_NotifyIcon (OS.NIM_ADD, iconData);
	}
	System.arraycopy (items, index, items, index + 1, itemCount++ - index);
	items [index] = item;
}

void destroyItem (TrayItem item) {
	int index = 0;
	while (index < itemCount) {
		if (items [index] == item) break;
		index++;
	}
	if (index == itemCount) return;
	if (!OS.IsWinCE) {
		NOTIFYICONDATA iconData = OS.IsUnicode ? (NOTIFYICONDATA) new NOTIFYICONDATAW () : new NOTIFYICONDATAA ();
		iconData.cbSize = NOTIFYICONDATA.sizeof;
		iconData.uID = item.id;
		iconData.hWnd = display.hwndMessage;
		OS.Shell_NotifyIcon (OS.NIM_DELETE, iconData);
	}
	System.arraycopy (items, index + 1, items, index, --itemCount - index);
	items [itemCount] = null;
}

/**
 * Returns the item at the given, zero-relative index in the
 * receiver. Throws an exception if the index is out of range.
 *
 * @param index the index of the item to return
 * @return the item at the given index
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_RANGE - if the index is not between 0 and the number of elements in the list minus 1 (inclusive)</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TrayItem getItem (int index) {
	checkWidget ();
	if (!(0 <= index && index < itemCount)) error (SWT.ERROR_INVALID_RANGE);
	return items [index];
}

/**
 * Returns the number of items contained in the receiver.
 *
 * @return the number of items
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getItemCount () {
	checkWidget ();
	return itemCount;
}

/**
 * Returns an array of <code>TrayItem</code>s which are the items
 * in the receiver. 
 * <p>
 * Note: This is not the actual structure used by the receiver
 * to maintain its list of items, so modifying the array will
 * not affect the receiver. 
 * </p>
 *
 * @return the items in the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public TrayItem [] getItems () {
	checkWidget ();
	TrayItem [] result = new TrayItem [itemCount];
	System.arraycopy (items, 0, result, 0, result.length);
	return result;
}

}
