package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */
 
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;

/**
 * Instances of this class are user interface objects that contain
 * menu items.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BAR, DROP_DOWN, POP_UP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Help, Hide, Show </dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */

public class Menu extends Widget {
	public int handle;
	int x, y;
	boolean hasLocation;
	MenuItem cascade;
	Decorations parent;

public Menu (Control parent) {
	this (checkNull (parent).getShell (), SWT.POP_UP);
}

public Menu (Decorations parent, int style) {
	super (parent, checkStyle (style));
	this.parent = parent;
	createWidget ();
}

public Menu (Menu parentMenu) {
	this (checkNull (parentMenu).parent, SWT.DROP_DOWN);
}

public Menu (MenuItem parentItem) {
	this (checkNull (parentItem).parent);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the help events are generated for the control, by sending
 * it one of the messages defined in the <code>HelpListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see HelpListener
 * @see #removeHelpListener
 */
public void addHelpListener (HelpListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Help, typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the help events are generated for the control, by sending
 * it one of the messages defined in the <code>MenuListener</code>
 * interface.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MenuListener
 * @see #removeMenuListener
 */
public void addMenuListener (MenuListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Hide,typedListener);
	addListener (SWT.Show,typedListener);
}

static Control checkNull (Control control) {
	if (control == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	return control;
}

static Menu checkNull (Menu menu) {
	if (menu == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	return menu;
}

static MenuItem checkNull (MenuItem item) {
	if (item == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	return item;
}

static int checkStyle (int style) {
	return checkBits (style, SWT.POP_UP, SWT.BAR, SWT.DROP_DOWN, 0, 0, 0);
}

void createHandle () {
	if ((style & SWT.BAR) != 0) {
		handle = OS.CreateMenu ();
	} else {
		handle = OS.CreatePopupMenu ();
	}
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
}

void createItem (MenuItem item, int index) {
	int count = OS.GetMenuItemCount (handle);
	if (!(0 <= index && index <= count)) error (SWT.ERROR_INVALID_RANGE);
	parent.add (item);
	/*
	* Bug in Windows.  For some reason, when InsertMenuItem ()
	* is used to insert an item without text, it is not possible
	* to use SetMenuItemInfo () to set the text at a later time.
	* The fix is to insert the item with an empty string.
	*/
	int hHeap = OS.GetProcessHeap ();
	int pszText = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, 1);
	MENUITEMINFO info = new MENUITEMINFO ();
	info.cbSize = MENUITEMINFO.sizeof;
	info.fMask = OS.MIIM_ID | OS.MIIM_TYPE;
	info.wID = item.id;
	info.fType = item.widgetStyle ();
	info.dwTypeData = pszText;
	boolean success = OS.InsertMenuItem (handle, index, true, info);
	if (pszText != 0) OS.HeapFree (hHeap, 0, pszText);
	if (!success) {
		parent.remove (item);
		error (SWT.ERROR_ITEM_NOT_ADDED);
	}
	redraw ();
}

void createWidget () {
	createHandle ();
	parent.add (this);
}

/*
* Currently not used.
*/
int defaultBackground () {
	return OS.GetSysColor (OS.COLOR_MENU);
}

/*
* Currently not used.
*/
int defaultForeground () {
	return OS.GetSysColor (OS.COLOR_MENUTEXT);
}

void destroyAcceleratorTable () {
	parent.destroyAcceleratorTable ();
}

void destroyItem (MenuItem item) {
	if (!OS.RemoveMenu (handle, item.id, OS.MF_BYCOMMAND)) {
		error (SWT.ERROR_ITEM_NOT_REMOVED);
	}
	redraw ();
}

void destroyWidget () {
	int hMenu = handle;
	releaseHandle ();
	if (hMenu != 0) {
		OS.DestroyMenu (hMenu);
	}
}

/**
 * Returns the default menu item or null if none has
 * been previously set.
 *
 * @return the default menu item.
 *
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public MenuItem getDefaultItem () {
	checkWidget ();
	int id = OS.GetMenuDefaultItem (handle, OS.MF_BYCOMMAND, OS.GMDI_USEDISABLED);
	if (id == -1) return null;
	MENUITEMINFO info = new MENUITEMINFO ();
	info.cbSize = MENUITEMINFO.sizeof;
	info.fMask = OS.MIIM_ID;
	if (OS.GetMenuItemInfo (handle, id, false, info)) {
		return parent.findMenuItem (info.wID);
	}
	return null;
}

public Display getDisplay () {
	Decorations parent = this.parent;
	if (parent == null) error (SWT.ERROR_WIDGET_DISPOSED);
	return parent.getDisplay ();
}

/**
 * Returns <code>true</code> if the receiver is enabled, and
 * <code>false</code> otherwise. A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 *
 * @return the receiver's enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getEnabled () {
	checkWidget ();
	return (state & DISABLED) == 0;
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
public MenuItem getItem (int index) {
	checkWidget ();
	MENUITEMINFO info = new MENUITEMINFO ();
	info.cbSize = MENUITEMINFO.sizeof;
	info.fMask = OS.MIIM_ID;
	if (!OS.GetMenuItemInfo (handle, index, true, info)) {
		error (SWT.ERROR_INVALID_RANGE);
	}
	return parent.findMenuItem (info.wID);
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
	return OS.GetMenuItemCount (handle);
}

/**
 * Returns an array of <code>MenuItem</code>s which are the items
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
public MenuItem [] getItems () {
	checkWidget ();
	int count = OS.GetMenuItemCount (handle);
	MenuItem [] items = new MenuItem [count];
	if (items.length == 0) return items;
	MENUITEMINFO info = new MENUITEMINFO ();
	info.cbSize = MENUITEMINFO.sizeof;
	info.fMask = OS.MIIM_ID;
	for (int i=0; i<count; i++) {
		if (OS.GetMenuItemInfo (handle, i, true, info)) {
			items [i] = parent.findMenuItem (info.wID);
		}
	}
	return items;
}

String getNameText () {
	String result = "";
	MenuItem [] items = getItems ();
	int length = items.length;
	if (length > 0) {
		for (int i=0; i<length-1; i++) {
			result = result + items [i].getNameText() + ", ";
		}
		result = result + items [length-1].getNameText ();
	}
	return result;
}

/**
 * Returns the receiver's parent, which must be a <code>Decorations</code>.
 *
 * @return the receiver's parent
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Decorations getParent () {
	checkWidget ();
	return parent;
}

/**
 * Returns the receiver's parent item, which must be a
 * <code>MenuItem</code> or null when the receiver is a
 * root.
 *
 * @return the receiver's parent item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public MenuItem getParentItem () {
	checkWidget ();
	return cascade;
}

/**
 * Returns the receiver's parent item, which must be a
 * <code>Menu</code> or null when the receiver is a
 * root.
 *
 * @return the receiver's parent item
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Menu getParentMenu () {
	checkWidget ();
	if (cascade != null) return cascade.parent;
	return null;
}

/**
 * Returns the receiver's shell. For all controls other than
 * shells, this simply returns the control's nearest ancestor
 * shell. Shells return themselves, even if they are children
 * of other shells.
 *
 * @return the receiver's shell
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #getParent
 */
public Shell getShell () {
	checkWidget ();
	return parent.getShell ();
}

/**
 * Returns <code>true</code> if the receiver is visible, and
 * <code>false</code> otherwise.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, this method
 * may still indicate that it is considered visible even though
 * it may not actually be showing.
 * </p>
 *
 * @return the receiver's visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean getVisible () {
	checkWidget ();
	return true;
}

/**
 * Searches the receiver's list starting at the first item
 * (index 0) until an item is found that is equal to the 
 * argument, and returns the index of that item. If no item
 * is found, returns -1.
 *
 * @param item the search item
 * @return the index of the item
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int indexOf (MenuItem item) {
	checkWidget ();
	if (item == null) error (SWT.ERROR_NULL_ARGUMENT);
	int count = OS.GetMenuItemCount (handle);
	MENUITEMINFO info = new MENUITEMINFO ();
	info.cbSize = MENUITEMINFO.sizeof;
	info.fMask = OS.MIIM_ID;
	for (int i=0; i<count; i++) {
		if (OS.GetMenuItemInfo (handle, i, true, info)) {
			if (info.wID == item.id) return i;
		}
	}
	return -1;
}

/**
 * Returns <code>true</code> if the receiver is enabled, and
 * <code>false</code> otherwise. A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 *
 * @return the receiver's enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isEnabled () {
	checkWidget ();
	Menu parentMenu = getParentMenu ();
	if (parentMenu == null) return getEnabled ();
	return getEnabled () && parentMenu.isEnabled ();
}

/**
 * Returns <code>true</code> if the receiver is visible, and
 * <code>false</code> otherwise.
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, this method
 * may still indicate that it is considered visible even though
 * it may not actually be showing.
 * </p>
 *
 * @return the receiver's visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isVisible () {
	checkWidget ();
	return getVisible ();
}

void redraw () {
	if ((style & SWT.BAR) != 0) {
		OS.DrawMenuBar (parent.handle);
		return;
	}
	if ((WIN32_MAJOR << 16 | WIN32_MINOR) < (4 << 16 | 10)) {
		return;
	}
	boolean hasCheck = false, hasImage = false;
	MenuItem [] items = getItems ();
	for (int i=0; i<items.length; i++) {
		MenuItem item = items [i];
		if (item.getImage () != null) {
			if ((hasImage = true) && hasCheck) break;
		}
		if ((item.getStyle () & (SWT.CHECK | SWT.RADIO)) != 0) {
			if ((hasCheck = true) && hasImage) break;
		}
	}
	MENUINFO lpcmi = new MENUINFO ();
	lpcmi.cbSize = MENUINFO.sizeof;
	lpcmi.fMask = OS.MIM_STYLE;
	OS.GetMenuInfo (handle, lpcmi);
	if (hasImage && !hasCheck) {
		lpcmi.dwStyle |= OS.MNS_CHECKORBMP;
	} else {
		lpcmi.dwStyle &= ~OS.MNS_CHECKORBMP;
	}
	OS.SetMenuInfo (handle, lpcmi);
}

void releaseChild () {
	super.releaseChild ();
	if (cascade != null) cascade.setMenu (null);
	if ((style & SWT.BAR) != 0 && this == parent.menuBar) {
		parent.setMenuBar (null);
	}
}

void releaseHandle () {
	handle = 0;
}

void releaseWidget () {
	MenuItem [] items = getItems ();
	for (int i=0; i<items.length; i++) {
		MenuItem item = items [i];
		if (!item.isDisposed ()) item.releaseWidget ();
	}
	super.releaseWidget ();
	if (parent != null) parent.remove (this);
	parent = null;
	cascade = null;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the help events are generated for the control.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see HelpListener
 * @see #addHelpListener
 */
public void removeHelpListener (HelpListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Help, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the menu events are generated for the control.
 *
 * @param listener the listener which should be notified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see MenuListener
 * @see #addMenuListener
 */
public void removeMenuListener (MenuListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Hide, listener);
	eventTable.unhook (SWT.Show, listener);
}

/**
 * Sets the default menu item to the argument or removes
 * the default emphasis when the argument is <code>null</code>.
 * 
 * @param item the default menu item or null
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the menu item has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setDefaultItem (MenuItem item) {
	checkWidget ();
	int command = -1;
	if (item != null) {
		if (item.isDisposed()) error(SWT.ERROR_INVALID_ARGUMENT);
		command = item.id;
	}
	OS.SetMenuDefaultItem (handle, command, OS.MF_BYCOMMAND);
	redraw ();
}

/**
 * Enables the receiver if the argument is <code>true</code>,
 * and disables it otherwise. A disabled control is typically
 * not selectable from the user interface and draws with an
 * inactive or "grayed" look.
 *
 * @param enabled the new enabled state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setEnabled (boolean enabled) {
	checkWidget ();
	state &= ~DISABLED;
	if (!enabled) state |= DISABLED;
}

/**
 * Sets the receiver's location to the point specified by
 * the arguments which are relative to the display.
 * <p>
 * Note:  This is different from most widgets where the
 * location of the widget is relative to the parent.
 * </p>
 *
 * @param x the new x coordinate for the receiver
 * @param y the new y coordinate for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLocation (int x, int y) {
	checkWidget ();
	this.x = x;  this.y = y;
	hasLocation = true;
}

/**
 * Marks the receiver as visible if the argument is <code>true</code>,
 * and marks it invisible otherwise. 
 * <p>
 * If one of the receiver's ancestors is not visible or some
 * other condition makes the receiver not visible, marking
 * it visible may not actually cause it to be displayed.
 * </p>
 *
 * @param visible the new visibility state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setVisible (boolean visible) {
	checkWidget ();
	if ((style & (SWT.BAR | SWT.DROP_DOWN)) != 0) return;
	int hwndParent = parent.handle;
	if (!visible) {
		OS.SendMessage (hwndParent, OS.WM_CANCELMODE, 0, 0);
		return;
	}
	int flags = OS.TPM_LEFTBUTTON | OS.TPM_RIGHTBUTTON | OS.TPM_LEFTALIGN;
	int nX = x, nY = y;
	if (!hasLocation) {
		int pos = OS.GetMessagePos ();
		nX = (short) (pos & 0xFFFF);
		nY = (short) (pos >> 16);
	}
	/*
	* Feature in Windows.  It is legal use TrackPopupMenu ()
	* to display an empty menu as long menu items are added
	* inside of WM_INITPOPUPMENU.  If no items are added, then
	* TrackPopupMenu () fails and does not send an indication
	* that the menu has been closed.  This is not strictly a
	* bug but leads to unwanted behavior when application code
	* assumes that every WM_INITPOPUPMENU will eventually result
	* in a WM_MENUSELECT, wParam=0xFFFF0000, lParam=0 to indicate
	* that the menu has been closed.  The fix is to detect the
	* case when TrackPopupMenu fails and the number of items in
	* the menu is zero and issue a fake WM_MENUSELECT.
	*/
	boolean success = OS.TrackPopupMenu (handle, flags, nX, nY, 0, hwndParent, null);
	if (!success && OS.GetMenuItemCount (handle) == 0) {
		OS.SendMessage (hwndParent, OS.WM_MENUSELECT, 0xFFFF0000, 0);
	}
}

}
