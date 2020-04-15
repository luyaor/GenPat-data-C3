package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */
 
import org.eclipse.swt.internal.motif.*;
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
	boolean hasLocation;
	MenuItem cascade, defaultItem;
	Decorations parent;
/**
* Creates a new instance of the widget.
*/
public Menu (Control parent) {
	this (checkNull(parent).getShell (), SWT.POP_UP);
}
/**
* Creates a new instance of the widget.
*/
public Menu (Decorations parent, int style) {
	super (parent, checkStyle (style));
	this.parent = parent;
	createWidget (0);
}
/**
* Creates a new instance of the widget.
*/
public Menu (Menu parentMenu) {
	this (checkNull(parentMenu).parent, SWT.DROP_DOWN);
}
/**
* Creates a new instance of the widget.
*/
public Menu (MenuItem parentItem) {
	this (checkNull(parentItem).parent);
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
	checkWidget();
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
public void addMenuListener(MenuListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener(SWT.Hide,typedListener);
	addListener(SWT.Show,typedListener);
}
static int checkStyle (int style) {
	return checkBits (style, SWT.POP_UP, SWT.BAR, SWT.DROP_DOWN, 0, 0, 0);
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
void createHandle (int index) {
	state |= HANDLE;
	
	/*
	* Bug in Motif. For some reason, creating a menu after any application context
	* and shell have been destroyed will segment fault unless a new application
	* context and shell have been created in the current thread.  The fix is to
	* detect this case and create and destroy a temporary application context and
	* shell.
	*/
	int xDisplay = 0, shellHandle = 0;
	if (Display.DisplayDisposed) {
		int [] argc = new int [] {0};
		int xtContext = OS.XtCreateApplicationContext ();
		xDisplay = OS.XtOpenDisplay (xtContext, null, null, null, 0, 0, argc, 0);
		shellHandle = OS.XtAppCreateShell (null, null, OS.TopLevelShellWidgetClass (), xDisplay, null, 0);
	}
	
	/* BAR menu */
	if ((style & SWT.BAR) != 0) {
		int parentHandle = parent.scrolledHandle;
		int [] argList = {OS.XmNancestorSensitive, 1};
		handle = OS.XmCreateMenuBar (parentHandle, null, argList, argList.length / 2);
		if (handle == 0) error (SWT.ERROR_NO_HANDLES);
		return;
	}
	
	/* POPUP and PULLDOWN menus */
	
	/*
	* Bug in Motif.  When an existing popup menu is destroyed just
	* before creating a new popup menu and the new menu is managed,
	* the cursor changes to the menu cursor but the new menu is not
	* displayed.  Also, Motif fails to show a popup menu when the
	* mouse is released.  Both problems stem from the fact that the
	* popup menu is in the widget tree of a visible shell.  The fix
	* is to create all popup menus as children of a hidden dialog
	* shell.  Menus created this way are automatically destroyed
	* when the shell is destroyed.
	*/
	byte [] buffer = new byte [1];
	int [] argList = {OS.XmNancestorSensitive, 1};
	if ((style & SWT.POP_UP) != 0) {
		int parentHandle = parent.dialogHandle ();
		handle = OS.XmCreatePopupMenu (parentHandle, buffer, argList, argList.length / 2);
	} else {
		/*
		* Bug in Linux.  For some reason, when the parent of the pulldown
		* menu is not the main window handle, XtDestroyWidget() occasionally
		* segment faults when the shell is destroyed.  The fix is to ensure
		* that the parent is the main window.
		*/
		int parentHandle = parent.scrolledHandle;
		handle = OS.XmCreatePulldownMenu (parentHandle, buffer, argList, argList.length / 2);
	}
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);

	/* Workaround for bug in Motif */
	if (Display.DisplayDisposed) {
		if (shellHandle != 0) OS.XtDestroyWidget (shellHandle);
		if (xDisplay != 0) {
			int xtContext = OS.XtDisplayToApplicationContext (xDisplay);
			OS.XtDestroyApplicationContext (xtContext);
		}
	}
}
void createWidget (int index) {
	super.createWidget (index);
	parent.add (this);
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
	checkWidget();
	return defaultItem;
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
	checkWidget();
	int [] argList = {OS.XmNsensitive, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return argList [1] != 0;
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
	checkWidget();
	int [] argList = {OS.XmNchildren, 0, OS.XmNnumChildren, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	if (argList [1] == 0) error (SWT.ERROR_CANNOT_GET_ITEM);
	int [] handles = new int [argList [3]];
	OS.memmove (handles, argList [1], argList[3] * 4);
	int i = 0, count = 0;
	while (i < argList [3]) {
		if (OS.XtIsManaged (handles [i])) {
			if (index == count) break;
			count++;
		}
		i++;
	}
	if (index != count) error (SWT.ERROR_INVALID_RANGE);
	Widget widget = WidgetTable.get (handles [i]);
	if (!(widget instanceof MenuItem)) error (SWT.ERROR_CANNOT_GET_ITEM);
	return (MenuItem) widget;
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
	checkWidget();
	int [] argList = {OS.XmNchildren, 0, OS.XmNnumChildren, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	if (argList [1] == 0 || argList [3] == 0) return 0;
	int [] handles = new int [argList [3]];
	OS.memmove (handles, argList [1], argList [3] * 4);
	int count = 0;
	for (int i=0; i<argList [3]; i++) {
		if (OS.XtIsManaged (handles [i])) count++;
	}
	return count;	
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
	checkWidget();
	int [] argList = {OS.XmNchildren, 0, OS.XmNnumChildren, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int ptr = argList [1], count = argList [3];
	if (count == 0 || ptr == 0) return new MenuItem [0];
	int [] handles = new int [count];
	OS.memmove (handles, ptr, count * 4);
	MenuItem [] items = new MenuItem [count];
	int i = 0, j = 0;
	while (i < count) {
		Widget item = WidgetTable.get (handles [i]);
		if (item != null) items [j++] = (MenuItem) item;
		i++;
	}
	if (i == j) return items;
	MenuItem [] newItems = new MenuItem [j];
	System.arraycopy (items, 0, newItems, 0, j);
	return newItems;
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
	checkWidget();
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
	checkWidget();
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
	checkWidget();
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
	checkWidget();
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
	checkWidget();
	return OS.XtIsManaged (handle);
}
void hookEvents () {
	int windowProc = getDisplay ().windowProc;
	OS.XtAddCallback (handle, OS.XmNhelpCallback, windowProc, SWT.Help);
	OS.XtAddCallback (handle, OS.XmNmapCallback, windowProc, SWT.Show);
	OS.XtAddCallback (handle, OS.XmNunmapCallback, windowProc, SWT.Hide);
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
	checkWidget();
	int [] argList = {OS.XmNchildren, 0, OS.XmNnumChildren, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	int [] handles = new int [argList [3]];
	OS.memmove (handles, argList [1], handles.length * 4);
	int index = 0;
	for (int i=0; i<handles.length; i++) {
		if (OS.XtIsManaged (handles [i])) {
			if (handles [i] == item.handle) return index;
			index++;
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
	checkWidget();
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
	checkWidget();
	return getVisible ();
}
int processHelp (int callData) {
	sendHelpEvent (callData);
	return 0;
}
int processHide (int callData) {
	sendEvent (SWT.Hide);
	return 0;
}
int processShow (int callData) {
	/*
	* SWT.Selection events are posted to allow stepping
	* in the VA/Java debugger.  SWT.Show events are
	* sent to ensure that application event handler
	* code runs before the menu is displayed.  This
	* means that SWT.Show events would normally occur
	* before SWT.Selection events.  While this is not 
	* strictly incorrect, applications often use the 
	* SWT.Selection event to update the state of menu
	* items and would like the ordering of events to 
	* be the other way around.
	*
	* The fix is to run the deferred events before
	* the menu is shown.  This means that stepping
	* through a selection event that was caused by
	* a popup menu will fail in VA/Java.
	*/
	Display display = getDisplay ();
	display.runDeferredEvents ();
	sendEvent (SWT.Show);
	return 0;
}
void releaseChild () {
	super.releaseChild ();
	if (cascade != null) cascade.setMenu (null);
	if (((style & SWT.BAR) != 0) && (this == parent.menuBar)) parent.setMenuBar (null);
}
void releaseWidget () {
	MenuItem [] items = getItems ();
	for (int i=0; i<items.length; i++) {
		MenuItem item = items [i];
		if (!item.isDisposed ()) {
			item.releaseWidget ();
			item.releaseHandle ();
		}
	}
	super.releaseWidget ();
	if (parent != null) parent.remove (this);
	parent = null;
	cascade = defaultItem = null;
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
	checkWidget();
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
public void removeMenuListener(MenuListener listener) {
	checkWidget();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.Hide, listener);
	eventTable.unhook(SWT.Show, listener);
}
void sendHelpEvent (int callData) {
	if (hooks (SWT.Help)) {
		postEvent (SWT.Help);
		return;
	}
	parent.sendHelpEvent (callData);
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
	checkWidget();
	if (item != null && item.isDisposed()) error(SWT.ERROR_INVALID_ARGUMENT);
	defaultItem = item;
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
	checkWidget();
	int [] argList = {OS.XmNsensitive, enabled ? 1 : 0};
	OS.XtSetValues (handle, argList, argList.length / 2);
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
	checkWidget();
	if ((style & (SWT.BAR | SWT.DROP_DOWN)) != 0) return;
	int [] argList = {OS.XmNx, x, OS.XmNy, y};
	OS.XtSetValues (handle, argList, argList.length / 2);
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
	checkWidget();
	if ((style & (SWT.BAR | SWT.DROP_DOWN)) != 0) return;
	if (visible) {
		int xDisplay = OS.XtDisplay (handle);
		if (xDisplay == 0) return;
		int xWindow = OS.XDefaultRootWindow (xDisplay);
		if (xWindow == 0) return;
		int [] rootX = new int [1], rootY = new int [1], unused = new int [1], mask = new int [1];
		if (OS.XQueryPointer (xDisplay, xWindow, unused, unused, rootX, rootY, unused, unused, mask) == 0) {
			return;
		}
		if (!hasLocation) {
			/*
			* Bug in Motif.  For some reason, when a menu is popped up
			* under the mouse, the menu will not highlight until the
			* mouse exits and then enters the menu again.  The fix is
			* to pop the menu up outside the current mouse position
			* causing highlighting to work properly when the user
			* waits for the menu to appear.
			*/
			rootX[0] += 1;  rootY[0] += 1;
			int [] argList = {OS.XmNx, rootX [0], OS.XmNy, rootY [0]};
			OS.XtSetValues (handle, argList, argList.length / 2);
		}
		OS.XtManageChild (handle);
		/*
		* Feature in Motif.  There is no API to force the menu
		* to accept keyboard traversal when popped up using
		* XtManageChild.  The fix is to call undocumented API
		* to do this.
		*/
		int flags = OS.Button1Mask | OS.Button2Mask | OS.Button3Mask;
		if ((mask [0] & flags) == 0) OS._XmSetMenuTraversal (handle, true);
	} else {
		OS.XtUnmanageChild (handle);
	}
}
}
