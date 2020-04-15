package org.eclipse.swt.widgets;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
 
import org.eclipse.swt.internal.carbon.OS;
import org.eclipse.swt.internal.carbon.DataBrowserCallbacks;
import org.eclipse.swt.internal.carbon.DataBrowserCustomCallbacks;
import org.eclipse.swt.internal.carbon.DataBrowserListViewColumnDesc;
import org.eclipse.swt.internal.carbon.Rect;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

public class Tree extends Composite {
	TreeItem [] items;
	GC paintGC;
	int anchorFirst, anchorLast;
	boolean ignoreSelect, ignoreExpand;
	static final int CHECK_COLUMN_ID = 1024;
	static final int COLUMN_ID = 1025;

public Tree (Composite parent, int style) {
	super (parent, checkStyle (style));
}

public void addSelectionListener(SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection, typedListener);
	addListener (SWT.DefaultSelection, typedListener);
}

public void addTreeListener(TreeListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Expand, typedListener);
	addListener (SWT.Collapse, typedListener);
} 

static int checkStyle (int style) {
	/*
	* Feature in Windows.  It is not possible to create
	* a tree that scrolls and does not have scroll bars.
	* The TVS_NOSCROLL style will remove the scroll bars
	* but the tree will never scroll.  Therefore, no matter
	* what style bits are specified, set the H_SCROLL and
	* V_SCROLL bits so that the SWT style will match the
	* widget that Windows creates.
	*/
	style |= SWT.H_SCROLL | SWT.V_SCROLL;
	return checkBits (style, SWT.SINGLE, SWT.MULTI, 0, 0, 0, 0);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int width = 0;
	if (wHint == SWT.DEFAULT) {
		TreeItem [] items = getItems ();
		GC gc = new GC (this);
		for (int i=0; i<items.length; i++) {
			Rectangle rect = items [i].getBounds ();
			width = Math.max (width, rect.width);
		}
		gc.dispose ();
		width = width * 2;
	} else {
		width = wHint;
	}
	if (width <= 0) width = DEFAULT_WIDTH;
	int height = 0;
	if (hHint == SWT.DEFAULT) {
		height = getItemCount () * getItemHeight ();
	} else {
		height = hHint;
	}
	if (height <= 0) height = DEFAULT_HEIGHT;
	Rectangle rect = computeTrim (0, 0, width, height);
	return new Point (rect.width, rect.height);
}

public Rectangle computeTrim (int x, int y, int width, int height) {
	checkWidget();
	Rect rect = new Rect ();
	OS.GetDataBrowserScrollBarInset (handle, rect);
	x -= rect.left;
	y -= rect.top;
	width += (rect.left + rect.right) * 3;
	height += rect.top + rect.bottom;
	return new Rectangle (x, y, width, height);
}

void createHandle () {
	int [] outControl = new int [1];
	int window = OS.GetControlOwner (parent.handle);
	OS.CreateDataBrowserControl (window, null, OS.kDataBrowserListView, outControl);
	if (outControl [0] == 0) error (SWT.ERROR_NO_HANDLES);
	handle = outControl [0];
	int selectionFlags = (style & SWT.SINGLE) != 0 ? OS.kDataBrowserSelectOnlyOne : OS.kDataBrowserCmdTogglesSelection;
	OS.SetDataBrowserSelectionFlags (handle, selectionFlags);
	OS.SetDataBrowserListViewHeaderBtnHeight (handle, (short) 0);
	OS.SetDataBrowserHasScrollBars (handle, (style & SWT.H_SCROLL) != 0, (style & SWT.V_SCROLL) != 0);
	//NOT DONE
	if ((style & SWT.H_SCROLL) == 0) OS.AutoSizeDataBrowserListViewColumns (handle);
	int position = 0;
	if ((style & SWT.CHECK) != 0) {
		DataBrowserListViewColumnDesc checkColumn = new DataBrowserListViewColumnDesc ();
		checkColumn.headerBtnDesc_version = OS.kDataBrowserListViewLatestHeaderDesc;
		checkColumn.propertyDesc_propertyID = CHECK_COLUMN_ID;
		checkColumn.propertyDesc_propertyType = OS.kDataBrowserCheckboxType;
		checkColumn.propertyDesc_propertyFlags = OS.kDataBrowserPropertyIsMutable;
		//NOT DONE
		checkColumn.headerBtnDesc_minimumWidth = 40;
		checkColumn.headerBtnDesc_maximumWidth = 40;
		checkColumn.headerBtnDesc_initialOrder = OS.kDataBrowserOrderIncreasing;
		OS.AddDataBrowserListViewColumn (handle, checkColumn, position++);
	}
	DataBrowserListViewColumnDesc column = new DataBrowserListViewColumnDesc ();
	column.headerBtnDesc_version = OS.kDataBrowserListViewLatestHeaderDesc;
	column.propertyDesc_propertyID = COLUMN_ID;
//	column.propertyDesc_propertyType = OS.kDataBrowserTextType; // OS.kDataBrowserIconAndTextType
	column.propertyDesc_propertyType = OS.kDataBrowserCustomType;
	column.propertyDesc_propertyFlags = OS.kDataBrowserListViewSelectionColumn | OS.kDataBrowserDefaultPropertyFlags;
	//NOT DONE
	column.headerBtnDesc_maximumWidth = 0x7FFF;
	column.headerBtnDesc_initialOrder = OS.kDataBrowserOrderIncreasing;
	OS.AddDataBrowserListViewColumn (handle, column, position);
	OS.SetDataBrowserListViewDisclosureColumn (handle, COLUMN_ID, true);
	OS.SetDataBrowserTableViewNamedColumnWidth (handle, COLUMN_ID, (short)800);

	/*
	* Feature in the Macintosh.  Scroll bars are not created until
	* the widget has a minimum size.  The fix is to force the scroll
	* bars to be created by temporarily giving the widget a size and
	* then restoring it to zero.
	* 
	* NOTE: The widget must be visible and SizeControl() must be used
	* to resize the widget to a minimim size or the widget will not
	* create the scroll bars.  This work around currently flashes.
	*/
	OS.SizeControl (handle, (short) 0xFF, (short) 0xFF);
	OS.SizeControl (handle, (short) 0, (short) 0);
}

void createItem (TreeItem item, TreeItem parentItem, int index) {
	int count = 0;
	for (int i=0; i<items.length; i++) {
		if (items [i] != null && items [i].parentItem == parentItem) count++;
	}
	if (index == -1) index = count;
	item.index = index;
	for (int i=0; i<items.length; i++) {
		if (items [i] != null && items [i].parentItem == parentItem) {
			if (items [i].index >= item.index) items [i].index++;
		}
	}
	int id = 0;
	while (id < items.length && items [id] != null) id++;
	if (id == items.length) {
		TreeItem [] newItems = new TreeItem [items.length + 4];
		System.arraycopy (items, 0, newItems, 0, items.length);
		items = newItems;
	}
	items [id] = item;
	item.id = id + 1;
	int parentID = OS.kDataBrowserNoItem;
	boolean expanded = true;
	if (parentItem != null) {
		parentID = parentItem.id;
		expanded = parentItem.getExpanded ();
	}
	if (expanded) {
		if (OS.AddDataBrowserItems (handle, parentID, 1, new int[] {item.id}, 0) != OS.noErr) {
			items [id] = null;
			error (SWT.ERROR_ITEM_NOT_ADDED);
		}
	}
}

ScrollBar createScrollBar (int style) {
	return createStandardBar (style);
}

void createWidget () {
	super.createWidget ();
	items = new TreeItem [4];
}

int defaultThemeFont () {	
	return OS.kThemeViewsFont;
}

public void deselectAll () {
	checkWidget ();
	ignoreSelect = true;
	OS.SetDataBrowserSelectedItems (handle, 0, null, OS.kDataBrowserItemsRemove);
	ignoreSelect = false;
}

void destroyItem (TreeItem item) {
	int parentID = item.parentItem == null ? OS.kDataBrowserNoItem : item.parentItem.id;
	if (OS.RemoveDataBrowserItems (handle, parentID, 1, new int[] {item.id}, 0) != OS.noErr) {
		error (SWT.ERROR_ITEM_NOT_REMOVED);
	}
	for (int i=0; i<items.length; i++) {
		if (items [i] != null) {
			TreeItem parentItem = items [i].parentItem;
			while (parentItem != null && parentItem != item) {
				parentItem = parentItem.parentItem;
			}
			if (parentItem == item) {
				TreeItem oldItem = items [i];
				items [i].id = 0;
				items [i].index = -1;	
				items [i] = null;
				oldItem.releaseResources ();
			}
		}
	}
	TreeItem parentItem = item.parentItem;
	for (int i=0; i<items.length; i++) {
		if (items [i] != null && items [i].parentItem == parentItem) {
			if (items [i].index >= item.index) --items [i].index;
		}
	}
	items [item.id - 1] = null;
	item.id = 0;
	item.index = -1;
}

int drawItemProc (int browser, int id, int property, int itemState, int theRect, int gdDepth, int colorDevice) {
	int index = id - 1;
	if (!(0 <= index && index < items.length)) return OS.noErr;
	TreeItem item = items [index];

//	if (false) {
//		int [] port = new int [1], gdh = new int [1];
//		OS.GetGWorld (port, gdh);
//		byte [] buffer = item.text.getBytes();
//		Rect rect = new Rect ();
//		OS.memcpy (rect, theRect, Rect.sizeof);
//		Display display = getDisplay ();
//		Color foreground = null, background = null;
//		if ((itemState & OS.kDataBrowserItemIsSelected) != 0)  {
//			foreground = display.getSystemColor (SWT.COLOR_LIST_SELECTION_TEXT);
//			background = display.getSystemColor (SWT.COLOR_LIST_SELECTION);
//		} else {
//			foreground = display.getSystemColor (SWT.COLOR_LIST_FOREGROUND);
//			background = display.getSystemColor (SWT.COLOR_LIST_BACKGROUND);
//		}
//		int red = (short) (background.handle [0] * 255);
//		int green = (short) (background.handle [1] * 255);
//		int blue = (short) (background.handle [2] * 255);
//		RGBColor color = new RGBColor ();
//		color.red = (short) (red << 8 | red);
//		color.green = (short) (green << 8 | green);
//		color.blue = (short) (blue << 8 | blue);
//		OS.RGBForeColor (color);
//		OS.PaintRect (rect);
//		red = (short) (foreground.handle [0] * 255);
//		green = (short) (foreground.handle [1] * 255);
//		blue = (short) (foreground.handle [2] * 255);
//		color.red = (short) (red << 8 | red);
//		color.green = (short) (green << 8 | green);
//		color.blue = (short) (blue << 8 | blue);
//		OS.RGBForeColor (color);
//		OS.MoveTo (rect.left, (short)(rect.top + 13));
//		OS.DrawText (buffer, (short) 0, (short) buffer.length);
//		OS.SetGWorld (port [0], gdh [0]);
////		System.out.println("x=" + rect.left + " y=" + rect.top + " width=" + (rect.right - rect.left) + " height=" + (rect.bottom - rect.top));
//		return OS.noErr;
//	} 

	Rect rect = new Rect ();
	OS.memcpy (rect, theRect, Rect.sizeof);
//	System.out.println("x=" + rect.left + " y=" + rect.top + " width=" + (rect.right - rect.left) + " height=" + (rect.bottom - rect.top));
	int x = rect.left;
	int y = rect.top;
	int width = rect.right - rect.left;
	int height = rect.bottom - rect.top;
	Rect controlRect = new Rect ();
	OS.GetControlBounds (handle, controlRect);
	x -= controlRect.left;
	y -= controlRect.top;
	GC gc = paintGC == null ? new GC (this) :  paintGC;
	int clip = OS.NewRgn ();
	OS.GetClip (clip);
	OS.OffsetRgn (clip, (short)-controlRect.left, (short)-controlRect.top);
	gc.setClipping (Region.carbon_new (clip));
	OS.DisposeRgn (clip);
	Display display = getDisplay ();
	Color foreground = item.foreground != null ? item.foreground : display.getSystemColor (SWT.COLOR_LIST_FOREGROUND);
	Color background = item.background != null ? item.background : display.getSystemColor (SWT.COLOR_LIST_BACKGROUND);
	gc.setForeground (foreground);
	gc.setBackground (background);
	gc.fillRectangle (x, y, width, height);
	Image image = item.image;
	if (image != null) {
		Rectangle bounds = image.getBounds ();
		gc.drawImage (image, 0, 0, bounds.width, bounds.height, x, y, bounds.width, height);
		x += bounds.width + 2;
	}
	Point extent = gc.stringExtent (item.text);
	if ((itemState & OS.kDataBrowserItemIsSelected) != 0) {
		gc.setForeground (display.getSystemColor (SWT.COLOR_LIST_SELECTION_TEXT));
		gc.setBackground (display.getSystemColor (SWT.COLOR_LIST_SELECTION));
		gc.fillRectangle (x, y, extent.x, height);
	}
	gc.drawString (item.text, x, y + (Math.max (0, (height - extent.y) / 2)));
	if (paintGC == null) gc.dispose ();
	return OS.noErr;
}

public Rectangle getClientArea () {
	checkWidget();
	Rect rect = new Rect (), inset = new Rect ();
	OS.GetControlBounds (handle, rect);
	OS.GetDataBrowserScrollBarInset (handle, inset);
	return new Rectangle (inset.left, inset.top, rect.right - rect.left + inset.right, rect.bottom - rect.top + inset.bottom);
}

public TreeItem getItem (Point point) {
	checkWidget ();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);
	Rect rect = new Rect ();
	OS.GetControlBounds (handle, rect);
	org.eclipse.swt.internal.carbon.Point pt = new org.eclipse.swt.internal.carbon.Point ();
	OS.SetPt (pt, (short) (point.x + rect.left), (short) (point.y + rect.top));
	//OPTIMIZE
	for (int i=0; i<items.length; i++) {
		TreeItem item = items [i];
		if (item != null) {
			if (OS.GetDataBrowserItemPartBounds (handle, item.id, COLUMN_ID, OS.kDataBrowserPropertyEnclosingPart, rect) == OS.noErr) {
				if (OS.PtInRect (pt, rect)) return item;
			}
		}
	}
	return null;
}

public int getItemCount () {
	checkWidget ();
	return getItemCount (null);
}

int getItemCount (TreeItem item) {
	checkWidget ();
	int count = 0;
	for (int i=0; i<items.length; i++) {
		if (items [i] != null && items [i].parentItem == item) count++;
	}
	return count;
}

public int getItemHeight () {
	checkWidget ();
	short [] height = new short [1];
	if (OS.GetDataBrowserTableViewRowHeight (handle, height) != OS.noErr) {
		error (SWT.ERROR_CANNOT_GET_ITEM_HEIGHT);
	}
	return height [0];
}

public TreeItem [] getItems () {
	checkWidget ();
	return getItems (null);
}

public TreeItem [] getItems (TreeItem item) {
	int count = 0;
	for (int i=0; i<items.length; i++) {
		if (items [i] != null && items [i].parentItem == item) count++;
	}
	TreeItem [] result = new TreeItem [count];
	for (int i=0; i<items.length; i++) {
		if (items [i] != null && items [i].parentItem == item) {
			result [items [i].index] = items [i];
		}
	}
	return result;
}

public TreeItem getParentItem () {
	checkWidget ();
	return null;
}

public TreeItem [] getSelection () {
	checkWidget ();
	int ptr = OS.NewHandle (0);
	if (OS.GetDataBrowserItems (handle, OS.kDataBrowserNoItem, true, OS.kDataBrowserItemIsSelected, ptr) != OS.noErr) {
		error (SWT.ERROR_CANNOT_GET_SELECTION);
	}
	int count = OS.GetHandleSize (ptr) / 4;
	TreeItem [] result = new TreeItem [count];
	OS.HLock (ptr);
	int [] start = new int [1];
	OS.memcpy (start, ptr, 4);
	int [] id = new int [1];
	for (int i=0; i<count; i++) {
		OS.memcpy (id, start [0] + (i * 4), 4);
		result [i] = items [id [0] - 1];
	}
	OS.HUnlock (ptr);
	OS.DisposeHandle (ptr);
	return result;
}

public int getSelectionCount () {
	checkWidget ();
	int [] count = new int [1];
	if (OS.GetDataBrowserItemCount (handle, OS.kDataBrowserNoItem, true, OS.kDataBrowserItemIsSelected, count) != OS.noErr) {
		error (SWT.ERROR_CANNOT_GET_COUNT);
	}
	return count [0];
}

public TreeItem getTopItem () {
	checkWidget();
	//OPTIMIZE
	Rect rect = new Rect ();
	OS.GetControlBounds (handle, rect);
	int offset = 0;
	int [] outMetric = new int [1];
	OS.GetThemeMetric (OS.kThemeMetricFocusRectOutset, outMetric);
	offset += outMetric [0];
	OS.GetThemeMetric (OS.kThemeMetricEditTextFrameOutset, outMetric);
	offset += outMetric [0];
	int y = rect.top + offset;
	for (int i=0; i<items.length; i++) {
		TreeItem item = items [i];
		if (item != null) {
			if (OS.GetDataBrowserItemPartBounds (handle, item.id, COLUMN_ID, OS.kDataBrowserPropertyEnclosingPart, rect) == OS.noErr) {
				if (rect.top <= y && y <= rect.bottom) return item;
			}
		}
	}
	return null;
}

int hitTestProc (int browser, int id, int property, int theRect, int mouseRect) {
//	int index = id - 1;
//	if (!(0 <= index && index < items.length)) return 0;
//	TreeItem item = items [index];
	return 1;
}

void hookEvents () {
	super.hookEvents ();
	Display display= getDisplay();
	DataBrowserCallbacks callbacks = new DataBrowserCallbacks ();
	callbacks.version = OS.kDataBrowserLatestCallbacks;
	OS.InitDataBrowserCallbacks (callbacks);
	callbacks.v1_itemDataCallback = display.itemDataProc;
	callbacks.v1_itemNotificationCallback = display.itemNotificationProc;
	OS.SetDataBrowserCallbacks (handle, callbacks);
	DataBrowserCustomCallbacks custom = new DataBrowserCustomCallbacks ();
	custom.version = OS.kDataBrowserLatestCustomCallbacks;
	OS.InitDataBrowserCustomCallbacks (custom);
	custom.v1_drawItemCallback = display.drawItemProc;
	custom.v1_hitTestCallback = display.hitTestProc;
	custom.v1_trackingCallback = display.trackingProc;
	OS.SetDataBrowserCustomCallbacks (handle, custom);
}

int itemDataProc (int browser, int id, int property, int itemData, int setValue) {
	int index = id - 1;
	if (!(0 <= index && index < items.length)) return OS.noErr;
	TreeItem item = items [index];
	switch (property) {
		case CHECK_COLUMN_ID: {
			if (setValue != 0) {
//				short [] theData = new short [1];
//				OS.GetDataBrowserItemDataButtonValue (itemData, theData);
//				item.checked = theData [0] == OS.kThemeButtonOn;
				item.checked = !item.checked;
				if (item.checked && item.grayed) {
					OS.SetDataBrowserItemDataButtonValue (itemData, (short) OS.kThemeButtonMixed);
				} else {
					int theData = item.checked ? OS.kThemeButtonOn : OS.kThemeButtonOff;
					OS.SetDataBrowserItemDataButtonValue (itemData, (short) theData);
				}
				Event event = new Event ();
				event.item = item;
				event.detail = SWT.CHECK;
				postEvent (SWT.Selection, event);
			} else {
//				short theData = (short)(item.checked ? OS.kThemeButtonOn : OS.kThemeButtonOff);
//				OS.SetDataBrowserItemDataButtonValue (itemData, theData);
				int theData = OS.kThemeButtonOff;
				if (item.checked) theData = item.grayed ? OS.kThemeButtonMixed : OS.kThemeButtonOn;
				OS.SetDataBrowserItemDataButtonValue (itemData, (short) theData);
			}
			break;
		}
//		case COLUMN_ID: {
//			String text = item.text;
//			char [] buffer = new char [text.length ()];
//			text.getChars (0, buffer.length, buffer, 0);
//			int ptr = OS.CFStringCreateWithCharacters (OS.kCFAllocatorDefault, buffer, buffer.length);
//			if (ptr == 0) error (SWT.ERROR_CANNOT_SET_TEXT);
//			OS.SetDataBrowserItemDataText (itemData, ptr);
//			OS.CFRelease (ptr);
//			break;
//		}
		case OS.kDataBrowserItemIsContainerProperty: {
			for (int i=0; i<items.length; i++) {
				if (items [i] != null && items [i].parentItem == item) {
					OS.SetDataBrowserItemDataBooleanValue (itemData, true);
				}
			}
			break;
		}
	}
	return OS.noErr;
}

int itemNotificationProc (int browser, int id, int message) {
	int index = id - 1;
	if (!(0 <= index && index < items.length)) return OS.noErr;
	TreeItem item = items [index];
	switch (message) {
		case OS.kDataBrowserItemSelected:
		case OS.kDataBrowserItemDeselected: {
			if (ignoreSelect) break;
			int [] first = new int [1], last = new int [1];
			OS.GetDataBrowserSelectionAnchor (handle, first, last);
			boolean selected = false;
			if ((style & SWT.MULTI) != 0) {
				int modifiers = OS.GetCurrentEventKeyModifiers ();
				if ((modifiers & OS.shiftKey) != 0) {
					if (message == OS.kDataBrowserItemSelected) {
						selected = first [0] == id || last [0] == id;
					} else {
						selected = id == anchorFirst || id == anchorLast;
					}
				} else {
					if ((modifiers & OS.cmdKey) != 0) {
						selected = true;
					} else {
						selected = first [0] == last [0];
					}
				}
			} else {
				selected = message == OS.kDataBrowserItemSelected;
			}
			if (selected) {
				anchorFirst = first [0];
				anchorLast = last [0];
				Event event = new Event ();
				event.item = item;
				postEvent (SWT.Selection, event);
			}
			break;
		}	
		case OS.kDataBrowserItemDoubleClicked: {
			Event event = new Event ();
			event.item = item;
			postEvent (SWT.DefaultSelection, event);
			break;
		}
		case OS.kDataBrowserContainerClosed: {
			if (ignoreExpand) break;	
			Event event = new Event ();
			event.item = item;
			sendEvent (SWT.Collapse, event);
			break;
		}
		case OS.kDataBrowserContainerOpened: {	
			if (!ignoreExpand) {
				Event event = new Event ();
				event.item = item;
				sendEvent (SWT.Expand, event);
			}
			int count = 0;
			for (int i=0; i<items.length; i++) {
				if (items [i] != null && items [i].parentItem == item) count++;
			}
			int [] ids = new int [count];
			for (int i=0; i<items.length; i++) {
				if (items [i] != null && items [i].parentItem == item) {
					ids [items [i].index] = items [i].id;
				}
			}
			OS.AddDataBrowserItems (handle, id, ids.length, ids, 0);
			break;
		}
	}
	return OS.noErr;
}

int kEventControlDraw (int nextHandler, int theEvent, int userData) {
	GC currentGC = paintGC;
	if (currentGC == null) paintGC = new GC (this);
	int result = super.kEventControlDraw (nextHandler, theEvent, userData);
	if (currentGC == null) {
		paintGC.dispose ();
		paintGC = null;
	}
	return result;
}

int kEventMouseDown (int nextHandler, int theEvent, int userData) {
	int result = super.kEventMouseDown (nextHandler, theEvent, userData);
	if (result == OS.noErr) return result;
	/*
	* Feature in the Macintosh.  For some reason, when the user
	* clicks on the data browser, focus is assigned, then lost
	* and then reassigned causing kEvenControlSetFocusPart events.
	* The fix is to ignore kEvenControlSetFocusPart when the user
	* clicks and send the focus events from kEventMouseDown.
	*/
	Display display = getDisplay ();
	Control oldFocus = display.getFocusControl ();
	display.ignoreFocus = true;
	result = OS.CallNextEventHandler (nextHandler, theEvent);
	display.ignoreFocus = false;
	if (oldFocus != this) {
		if (oldFocus != null && !oldFocus.isDisposed ()) oldFocus.sendFocusEvent (false);
		if (!isDisposed () && isEnabled ()) sendFocusEvent (true);
	}
	return result;
}

void releaseWidget () {
	for (int i=0; i<items.length; i++) {
		TreeItem item = items [i];
		if (item != null && !item.isDisposed ()) {
			item.releaseResources ();
		}
	}
	super.releaseWidget ();
}

public void removeAll () {
	checkWidget ();
	if (OS.RemoveDataBrowserItems (handle, OS.kDataBrowserNoItem, 0, null, 0) != OS.noErr) {
		error (SWT.ERROR_ITEM_NOT_REMOVED);
	}
	for (int i=0; i<items.length; i++) {
		TreeItem item = items [i];
		if (item != null && !item.isDisposed ()) item.releaseResources ();
	}
	items = new TreeItem [4];
	anchorFirst = anchorLast = 0;
}

public void removeSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection, listener);	
}

public void removeTreeListener(TreeListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Expand, listener);
	eventTable.unhook (SWT.Collapse, listener);
}

public void setInsertMark (TreeItem item, boolean before) {
	checkWidget ();
	if (item != null) {
		if (item.isDisposed()) error(SWT.ERROR_INVALID_ARGUMENT);
	}
}

public void selectAll () {
	checkWidget ();
	if ((style & SWT.SINGLE) != 0) return;
	ignoreSelect = true;
	OS.SetDataBrowserSelectedItems (handle, 0, null, OS.kDataBrowserItemsAssign);
	ignoreSelect = false;
}

public void setSelection (TreeItem [] items) {
	checkWidget ();
	if (items == null) error (SWT.ERROR_NULL_ARGUMENT);
	int[] ids = new int [items.length];
	for (int i=0; i<items.length; i++) {
		if (items [i] == null) error (SWT.ERROR_INVALID_ARGUMENT);
		if (items [i].isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
		ids [i] = items [i].id;
		showItem (items [i], false);
	}
	ignoreSelect = true;
	OS.SetDataBrowserSelectedItems (handle, ids.length, ids, OS.kDataBrowserItemsAssign);
	ignoreSelect = false;
	if (items.length > 0) showItem (items [0], true);
}

public void setTopItem (TreeItem item) {
	checkWidget();
	if (item == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (item.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
	showItem (item, false);	
	OS.RevealDataBrowserItem (handle, item.id, COLUMN_ID, (byte) OS.kDataBrowserRevealWithoutSelecting);
//	Rect rect = new Rect ();
//	OS.GetControlBounds (handle, rect);
//	int x = rect.left, y = rect.top;
//	if (OS.GetDataBrowserItemPartBounds (handle, item.id, COLUMN_ID, OS.kDataBrowserPropertyEnclosingPart, rect) == OS.noErr) {
//		OS.SetDataBrowserScrollPosition (handle, rect.top - y - 3, 0);
//	}
}

public void showItem (TreeItem item) {
	checkWidget ();
	if (item == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (item.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
	showItem (item, true);
}

void showItem (TreeItem item, boolean scroll) {
	int count = 0;
	TreeItem parentItem = item.parentItem;
	while (parentItem != null && !parentItem.getExpanded ()) {
		count++;
		parentItem = parentItem.parentItem;
	}
	int index = 0;
	parentItem = item.parentItem;
	TreeItem [] path = new TreeItem [count];
	while (parentItem != null && !parentItem.getExpanded ()) {
		path [index++] = parentItem;
		parentItem = parentItem.parentItem;
	}
	for (int i=path.length-1; i>=0; --i) {
		path [i].setExpanded (true);
	}
//	if (scroll) {
//		short [] width = new short [1];
//		OS.GetDataBrowserTableViewNamedColumnWidth (handle, COLUMN_ID, width);
//		Rect rect = new Rect (), inset = new Rect ();
//		OS.GetControlBounds (handle, rect);
//		OS.GetDataBrowserScrollBarInset (handle, inset);
//		OS.SetDataBrowserTableViewNamedColumnWidth (handle, COLUMN_ID, (short)(rect.right - rect.left - inset.left - inset.right));
//		OS.RevealDataBrowserItem (handle, item.id, COLUMN_ID, (byte) OS.kDataBrowserRevealWithoutSelecting);
//		OS.SetDataBrowserTableViewNamedColumnWidth (handle, COLUMN_ID, (short)width [0]);
//	}
	if (scroll) {
		//OPTIMIZE
		Rectangle treeRect = getClientArea ();
		Rectangle itemRect = item.getBounds ();
		if (treeRect.contains (itemRect.x, itemRect.y)) return;
		OS.RevealDataBrowserItem (handle, item.id, COLUMN_ID, (byte) OS.kDataBrowserRevealWithoutSelecting);
		int [] top = new int [1], left = new int [1];
		OS.GetDataBrowserScrollPosition (handle, top, left);
		OS.SetDataBrowserScrollPosition (handle, top [0], 0);
		itemRect = item.getBounds ();
		if (!treeRect.contains (itemRect.x, itemRect.y)) {
			OS.RevealDataBrowserItem (handle, item.id, COLUMN_ID, (byte) OS.kDataBrowserRevealWithoutSelecting);
		}
	}
}

public void showSelection () {
	checkWidget ();
	//OPTIMIZE
	TreeItem [] selection = getSelection ();
	if (selection.length > 0) showItem (selection [0], true);
}

int trackingProc (int browser, int id, int property, int theRect, int startPt, int modifiers) {
//	int index = id - 1;
//	if (!(0 <= index && index < items.length)) return 0;
//	TreeItem item = items [index];
	return 1;
}

}
