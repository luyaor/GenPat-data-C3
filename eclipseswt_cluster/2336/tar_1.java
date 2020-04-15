package org.eclipse.swt.widgets;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class are responsible for managing the
 * connection between SWT and the underlying operating
 * system. Their most important function is to implement
 * the SWT event loop in terms of the platform event model.
 * They also provide various methods for accessing information
 * about the operating system, and have overall control over
 * the operating system resources which SWT allocates.
 * <p>
 * Applications which are built with SWT will <em>almost always</em>
 * require only a single display. In particular, some platforms
 * which SWT supports will not allow more than one <em>active</em>
 * display. In other words, some platforms do not support
 * creating a new display if one already exists that has not been
 * sent the <code>dispose()</code> message.
 * <p>
 * In SWT, the thread which creates a <code>Display</code>
 * instance is distinguished as the <em>user-interface thread</em>
 * for that display.
 * </p>
 * The user-interface thread for a particular display has the
 * following special attributes:
 * <ul>
 * <li>
 * The event loop for that display must be run from the thread.
 * </li>
 * <li>
 * Some SWT API methods (notably, most of the public methods in
 * <code>Widget</code> and its subclasses), may only be called
 * from the thread. (To support multi-threaded user-interface
 * applications, class <code>Display</code> provides inter-thread
 * communication methods which allow threads other than the 
 * user-interface thread to request that it perform operations
 * on their behalf.)
 * </li>
 * <li>
 * The thread is not allowed to construct other 
 * <code>Display</code>s until that display has been disposed.
 * (Note that, this is in addition to the restriction mentioned
 * above concerning platform support for multiple displays. Thus,
 * the only way to have multiple simultaneously active displays,
 * even on platforms which support it, is to have multiple threads.)
 * </li>
 * </ul>
 * Enforcing these attributes allows SWT to be implemented directly
 * on the underlying operating system's event model. This has 
 * numerous benefits including smaller footprint, better use of 
 * resources, safer memory management, clearer program logic,
 * better performance, and fewer overall operating system threads
 * required. The down side however, is that care must be taken
 * (only) when constructing multi-threaded applications to use the
 * inter-thread communication mechanisms which this class provides
 * when required.
 * </p><p>
 * All SWT API methods which may only be called from the user-interface
 * thread are distinguished in their documentation by indicating that
 * they throw the "<code>ERROR_THREAD_INVALID_ACCESS</code>"
 * SWT exception.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Close, Dispose</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 * @see #readAndDispatch
 * @see #sleep
 * @see #dispose
 */

public class Display extends Device {

	/**
	 * the handle to the OS message queue
	 * (Warning: This field is platform dependent)
	 */
	public MSG msg = new MSG ();
	
	/* Windows, Events and Callback */
	Event [] eventQueue;
	Callback windowCallback;
	int windowProc, threadId, processId;
	TCHAR windowClass;
	static int WindowClassCount = 0;
	static final String WindowName = "SWT_Window";
	EventTable eventTable, filterTable;

	/* Windows Message Filter */
	Callback msgFilterCallback;
	int msgFilterProc, hHook;
	MSG hookMsg = new MSG ();
	
	/* Sync/Async Widget Communication */
	Synchronizer synchronizer = new Synchronizer (this);
	Thread thread;

	/* Display Shutdown */
	Runnable [] disposeList;
	
	/* Timers */
	int timerCount;
	int [] timerIds;
	Runnable [] timerList;
	
	/* Keyboard and Mouse State */
	boolean lockActiveWindow;
	boolean lastVirtual, lastNull;
	int lastKey, lastAscii, lastMouse;
	byte [] keyboard = new byte [256];
	boolean accelKeyHit, mnemonicKeyHit;
	
	/* Message Only Window */
	int hwndMessage, messageProc;
	Callback messageCallback;
	int [] systemFonts;
	
	/* Image list cache */	
	ImageList[] imageList, toolImageList, toolHotImageList, toolDisabledImageList;

	/* Custom Colors for ChooseColor */
	int lpCustColors;

	/* Display Data */
	Object data;
	String [] keys;
	Object [] values;

	/* Popup Menus */
	Menu [] popups;
	
	/* Key Mappings */
	static final int [] [] KeyTable = {
		
		/* Keyboard and Mouse Masks */
		{OS.VK_MENU,	SWT.ALT},
		{OS.VK_SHIFT,	SWT.SHIFT},
		{OS.VK_CONTROL,	SWT.CONTROL},

		/* NOT CURRENTLY USED */		
//		{OS.VK_LBUTTON, SWT.BUTTON1},
//		{OS.VK_MBUTTON, SWT.BUTTON3},
//		{OS.VK_RBUTTON, SWT.BUTTON2},
		
		/* Non-Numeric Keypad Keys */
		{OS.VK_UP,		SWT.ARROW_UP},
		{OS.VK_DOWN,	SWT.ARROW_DOWN},
		{OS.VK_LEFT,	SWT.ARROW_LEFT},
		{OS.VK_RIGHT,	SWT.ARROW_RIGHT},
		{OS.VK_PRIOR,	SWT.PAGE_UP},
		{OS.VK_NEXT,	SWT.PAGE_DOWN},
		{OS.VK_HOME,	SWT.HOME},
		{OS.VK_END,		SWT.END},
		{OS.VK_INSERT,	SWT.INSERT},

		/* Virtual and Ascii Keys */
		{OS.VK_BACK,	SWT.BS},
		{OS.VK_RETURN,	SWT.CR},
		{OS.VK_DELETE,	SWT.DEL},
		{OS.VK_ESCAPE,	SWT.ESC},
		{OS.VK_RETURN,	SWT.LF},
		{OS.VK_TAB,		SWT.TAB},
	
		/* Functions Keys */
		{OS.VK_F1,	SWT.F1},
		{OS.VK_F2,	SWT.F2},
		{OS.VK_F3,	SWT.F3},
		{OS.VK_F4,	SWT.F4},
		{OS.VK_F5,	SWT.F5},
		{OS.VK_F6,	SWT.F6},
		{OS.VK_F7,	SWT.F7},
		{OS.VK_F8,	SWT.F8},
		{OS.VK_F9,	SWT.F9},
		{OS.VK_F10,	SWT.F10},
		{OS.VK_F11,	SWT.F11},
		{OS.VK_F12,	SWT.F12},
		
		/* Numeric Keypad Keys */
//		{OS.VK_ADD,			SWT.KP_PLUS},
//		{OS.VK_SUBTRACT,	SWT.KP_MINUS},
//		{OS.VK_MULTIPLY,	SWT.KP_TIMES},
//		{OS.VK_DIVIDE,		SWT.KP_DIVIDE},
//		{OS.VK_DECIMAL,		SWT.KP_DECIMAL},
//		{OS.VK_RETURN,		SWT.KP_CR},
//		{OS.VK_NUMPAD0,		SWT.KP_0},
//		{OS.VK_NUMPAD1,		SWT.KP_1},
//		{OS.VK_NUMPAD2,		SWT.KP_2},
//		{OS.VK_NUMPAD3,		SWT.KP_3},
//		{OS.VK_NUMPAD4,		SWT.KP_4},
//		{OS.VK_NUMPAD5,		SWT.KP_5},
//		{OS.VK_NUMPAD6,		SWT.KP_6},
//		{OS.VK_NUMPAD7,		SWT.KP_7},
//		{OS.VK_NUMPAD8,		SWT.KP_8},
//		{OS.VK_NUMPAD9,		SWT.KP_9},

	};

	/* Multiple Displays */
	static Display Default;
	static Display [] Displays = new Display [4];

	/* Modality */
	Shell [] modalWidgets;
	static boolean TrimEnabled = false;

	/* Package Name */
	static final String PACKAGE_PREFIX = "org.eclipse.swt.widgets.";
	/*
	* This code is intentionally commented.  In order
	* to support CLDC, .class cannot be used because
	* it does not compile on some Java compilers when
	* they are targeted for CLDC.
	*/
//	static {
//		String name = Display.class.getName ();
//		int index = name.lastIndexOf ('.');
//		PACKAGE_PREFIX = name.substring (0, index + 1);
//	}

	/*
	* TEMPORARY CODE.  Install the runnable that
	* gets the current display. This code will
	* be removed in the future.
	*/
	static {
		DeviceFinder = new Runnable () {
			public void run () {
				Device device = getCurrent ();
				if (device == null) {
					device = getDefault ();
				}
				setDevice (device);
			}
		};
	}	

/*
* TEMPORARY CODE.
*/
static void setDevice (Device device) {
	CurrentDevice = device;
}
	
/**
 * Constructs a new instance of this class.
 * <p>
 * Note: The resulting display is marked as the <em>current</em>
 * display. If this is the first display which has been 
 * constructed since the application started, it is also
 * marked as the <em>default</em> display.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see #getCurrent
 * @see #getDefault
 * @see Widget#checkSubclass
 * @see Shell
 */
public Display () {
	this (null);
}

public Display (DeviceData data) {
	super (data);
}

int asciiKey (int key) {
	if (OS.IsWinCE) return 0;
	
	/* Get the current keyboard. */
	for (int i=0; i<keyboard.length; i++) keyboard [i] = 0;
	if (!OS.GetKeyboardState (keyboard)) return 0;
		
	/* Translate the key to ASCII or UNICODE using the virtual keyboard */
	if (OS.IsUnicode) {
		char [] result = new char [1];
		if (OS.ToUnicode (key, key, keyboard, result, 1, 0) == 1) return result [0];
	} else {
		short [] result = new short [1];
		if (OS.ToAscii (key, key, keyboard, result, 0) == 1) return result [0];
	}
	return 0;
}

/**
 * Adds the listener to the collection of listeners who will
 * be notifed when an event of the given type occurs anywhere
 * in SWT. When the event does occur, the listener is notified
 * by sending it the <code>handleEvent()</code> message.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Listener
 * @see #removeFilter
 * @see #removeListener
 * 
 * @since 2.1 
 */
public void addFilter (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (filterTable == null) filterTable = new EventTable ();
	filterTable.hook (eventType, listener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notifed when an event of the given type occurs. When the
 * event does occur in the display, the listener is notified by
 * sending it the <code>handleEvent()</code> message.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Listener
 * @see #removeListener
 * 
 * @since 2.0 
 */
public void addListener (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) eventTable = new EventTable ();
	eventTable.hook (eventType, listener);
}

void addPopup (Menu menu) {
	if (popups == null) popups = new Menu [4];
	int length = popups.length;
	for (int i=0; i<length; i++) {
		if (popups [i] == menu) return;
	}
	int index = 0;
	while (index < length) {
		if (popups [index] == null) break;
		index++;
	}
	if (index == length) {
		Menu [] newPopups = new Menu [length + 4];
		System.arraycopy (popups, 0, newPopups, 0, length);
		popups = newPopups;
	}
	popups [index] = menu;
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread at the next 
 * reasonable opportunity. The caller of this method continues 
 * to run in parallel, and is not notified when the
 * runnable has completed.
 *
 * @param runnable code to run on the user-interface thread.
 *
 * @see #syncExec
 */
public void asyncExec (Runnable runnable) {
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
	synchronizer.asyncExec (runnable);
}

/**
 * Causes the system hardware to emit a short sound
 * (if it supports this capability).
 */
public void beep () {
	checkDevice ();
	OS.MessageBeep (OS.MB_OK);
}

/**
 * Checks that this class can be subclassed.
 * <p>
 * IMPORTANT: See the comment in <code>Widget.checkSubclass()</code>.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see Widget#checkSubclass
 */
protected void checkSubclass () {
	if (!isValidClass (getClass ())) error (SWT.ERROR_INVALID_SUBCLASS);
}

protected void checkDevice () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
}

static synchronized void checkDisplay (Thread thread) {
	for (int i=0; i<Displays.length; i++) {
		if (Displays [i] != null && Displays [i].thread == thread) {
			SWT.error (SWT.ERROR_THREAD_INVALID_ACCESS);
		}
	}
}

void clearModal (Shell shell) {
	if (modalWidgets == null) return;
	int index = 0, length = modalWidgets.length;
	while (index < length) {
		if (modalWidgets [index] == shell) break;
		if (modalWidgets [index] == null) return;
		index++;
	}
	if (index == length) return;
	System.arraycopy (modalWidgets, index + 1, modalWidgets, index, --length - index);
	modalWidgets [length] = null;
	if (index == 0 && modalWidgets [0] == null) modalWidgets = null;
	Shell [] shells = getShells ();
	for (int i=0; i<shells.length; i++) shells [i].updateModal ();
}

int controlKey (int key) {
	int upper = OS.CharUpper ((short) key);
	if (64 <= upper && upper <= 95) return upper & 0xBF;
	return key;
}

/**
 * Requests that the connection between SWT and the underlying
 * operating system be closed.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #dispose
 * 
 * @since 2.0
 */
public void close () {
	checkDevice ();
	Event event = new Event ();
	sendEvent (SWT.Close, event);
	if (event.doit) dispose ();
}

/**
 * Creates the device in the operating system.  If the device
 * does not have a handle, this method may do nothing depending
 * on the device.
 * <p>
 * This method is called before <code>init</code>.
 * </p>
 *
 * @param data the DeviceData which describes the receiver
 *
 * @see #init
 */
protected void create (DeviceData data) {
	checkSubclass ();
	checkDisplay (thread = Thread.currentThread ());
	createDisplay (data);
	register (this);
	if (Default == null) Default = this;
}

void createDisplay (DeviceData data) {
}

static synchronized void deregister (Display display) {
	for (int i=0; i<Displays.length; i++) {
		if (display == Displays [i]) Displays [i] = null;
	}
}

/**
 * Destroys the device in the operating system and releases
 * the device's handle.  If the device does not have a handle,
 * this method may do nothing depending on the device.
 * <p>
 * This method is called after <code>release</code>.
 * </p>
 * @see #dispose
 * @see #release
 */
protected void destroy () {
	if (this == Default) Default = null;
	deregister (this);
	destroyDisplay ();
}

void destroyDisplay () {
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread just before the
 * receiver is disposed.
 *
 * @param runnable code to run at dispose time.
 */
public void disposeExec (Runnable runnable) {
	checkDevice ();
	if (disposeList == null) disposeList = new Runnable [4];
	for (int i=0; i<disposeList.length; i++) {
		if (disposeList [i] == null) {
			disposeList [i] = runnable;
			return;
		}
	}
	Runnable [] newDisposeList = new Runnable [disposeList.length + 4];
	System.arraycopy (disposeList, 0, newDisposeList, 0, disposeList.length);
	newDisposeList [disposeList.length] = runnable;
	disposeList = newDisposeList;
}

/**
 * Does whatever display specific cleanup is required, and then
 * uses the code in <code>SWTError.error</code> to handle the error.
 *
 * @param code the descriptive error code
 *
 * @see SWTError#error
 */
void error (int code) {
	SWT.error (code);
}

boolean filterEvent (Event event) {
	if (filterTable != null) filterTable.sendEvent (event);
	return false;
}

boolean filters (int eventType) {
	if (filterTable == null) return false;
	return filterTable.hooks (eventType);
}

boolean filterMessage (MSG msg) {
	int message = msg.message;
	if (OS.WM_KEYFIRST <= message && message <= OS.WM_KEYLAST) {
		Control control = findControl (msg.hwnd);
		if (control != null) {
			if (translateAccelerator (msg, control)) return true;
			if (translateMnemonic (msg, control)) return true;
			if (translateTraversal (msg, control)) return true;
		}
	}
	return false;
}

/**
 * Given the operating system handle for a widget, returns
 * the instance of the <code>Widget</code> subclass which
 * represents it in the currently running application, if
 * such exists, or null if no matching widget can be found.
 *
 * @param handle the handle for the widget
 * @return the SWT widget that the handle represents
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Widget findWidget (int handle) {
	checkDevice ();
	return WidgetTable.get (handle);
}

Control findControl(int handle) {
	if (handle == 0) return null;
	/*
	* This code is intentionally commented.  It is possible
	* find the SWT control that is associated with a handle
	* that belongs to another process when the handle was
	* created by an in-proc OLE client.  In this case, the
	* handle comes from another process, but it is a child
	* of an SWT control.  For now, it is necessary to look
	* at handles that do not belong to the SWT process.
	*/
//	int [] hwndProcessId = new int [1];
//	OS.GetWindowThreadProcessId (handle, hwndProcessId);
//	if (hwndProcessId [0] != processId) return null;
	do {
		Control control = WidgetTable.get (handle);
		if (control != null && control.handle == handle) {
			return control;
		}
	} while ((handle = OS.GetParent (handle)) != 0);
	return null;
}


/**
 * Returns the display which the given thread is the
 * user-interface thread for, or null if the given thread
 * is not a user-interface thread for any display.
 *
 * @param thread the user-interface thread
 * @return the display for the given thread
 */
public static synchronized Display findDisplay (Thread thread) {
	for (int i=0; i<Displays.length; i++) {
		Display display = Displays [i];
		if (display != null && display.thread == thread) {
			return display;
		}
	}
	return null;
}

/**
 * Returns the currently active <code>Shell</code>, or null
 * if no shell belonging to the currently running application
 * is active.
 *
 * @return the active shell or null
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Shell getActiveShell () {
	checkDevice ();
	Control control = findControl (OS.GetActiveWindow ());
	if (control instanceof Shell) return (Shell) control;
	return null;
}

/**
 * Returns a rectangle describing the receiver's size and location.
 *
 * @return the bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Rectangle getBounds () {
	checkDevice ();
	if (OS.GetSystemMetrics (OS.SM_CMONITORS) < 2) {
		int width = OS.GetSystemMetrics (OS.SM_CXSCREEN);
		int height = OS.GetSystemMetrics (OS.SM_CYSCREEN);
		return new Rectangle (0, 0, width, height);
	}
	int x = OS.GetSystemMetrics (OS.SM_XVIRTUALSCREEN);
	int y = OS.GetSystemMetrics (OS.SM_YVIRTUALSCREEN);	
	int width = OS.GetSystemMetrics (OS.SM_CXVIRTUALSCREEN);
	int height = OS.GetSystemMetrics (OS.SM_CYVIRTUALSCREEN);
	return new Rectangle (x, y, width, height);
}

/**
 * Returns the display which the currently running thread is
 * the user-interface thread for, or null if the currently
 * running thread is not a user-interface thread for any display.
 *
 * @return the current display
 */
public static synchronized Display getCurrent () {
	return findDisplay (Thread.currentThread ());
}

public Rectangle getClientArea () {
	checkDevice ();
	if (OS.GetSystemMetrics (OS.SM_CMONITORS) < 2) {
		RECT rect = new RECT ();
		OS.SystemParametersInfo (OS.SPI_GETWORKAREA, 0, rect, 0);
		int width = rect.right - rect.left;
		int height = rect.bottom - rect.top;
		return new Rectangle (rect.left, rect.top, width, height);
	}
	int x = OS.GetSystemMetrics (OS.SM_XVIRTUALSCREEN);
	int y = OS.GetSystemMetrics (OS.SM_YVIRTUALSCREEN);	
	int width = OS.GetSystemMetrics (OS.SM_CXVIRTUALSCREEN);
	int height = OS.GetSystemMetrics (OS.SM_CYVIRTUALSCREEN);
	return new Rectangle (x, y, width, height);
}

/**
 * Returns the control which the on-screen pointer is currently
 * over top of, or null if it is not currently over one of the
 * controls built by the currently running application.
 *
 * @return the control under the cursor
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Control getCursorControl () {
	checkDevice ();
	POINT pt = new POINT ();
	if (!OS.GetCursorPos (pt)) return null;
	return findControl (OS.WindowFromPoint (pt));
}

/**
 * Returns the location of the on-screen pointer relative
 * to the top left corner of the screen.
 *
 * @return the cursor location
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getCursorLocation () {
	checkDevice ();
	POINT pt = new POINT ();
	OS.GetCursorPos (pt);
	return new Point (pt.x, pt.y);
}

/**
 * Returns the default display. One is created (making the
 * thread that invokes this method its user-interface thread)
 * if it did not already exist.
 *
 * @return the default display
 */
public static synchronized Display getDefault () {
	if (Default == null) Default = new Display ();
	return Default;
}

static boolean isValidClass (Class clazz) {
	String name = clazz.getName ();
	int index = name.lastIndexOf ('.');
	return name.substring (0, index + 1).equals (PACKAGE_PREFIX);
}

/**
 * Returns the application defined property of the receiver
 * with the specified name, or null if it has not been set.
 * <p>
 * Applications may have associated arbitrary objects with the
 * receiver in this fashion. If the objects stored in the
 * properties need to be notified when the display is disposed
 * of, it is the application's responsibility provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @param key the name of the property
 * @return the value of the property or null if it has not been set
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #setData
 * @see #disposeExec
 */
public Object getData (String key) {
	checkDevice ();
	if (key == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (keys == null) return null;
	for (int i=0; i<keys.length; i++) {
		if (keys [i].equals (key)) return values [i];
	}
	return null;
}

/**
 * Returns the application defined, display specific data
 * associated with the receiver, or null if it has not been
 * set. The <em>display specific data</em> is a single,
 * unnamed field that is stored with every display. 
 * <p>
 * Applications may put arbitrary objects in this field. If
 * the object stored in the display specific data needs to
 * be notified when the display is disposed of, it is the
 * application's responsibility provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @return the display specific data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - when called from the wrong thread</li>
 * </ul>
 *
 * @see #setData
 * @see #disposeExec
 */
public Object getData () {
	checkDevice ();
	return data;
}

/**
 * Returns the longest duration, in milliseconds, between
 * two mouse button clicks that will be considered a
 * <em>double click</em> by the underlying operating system.
 *
 * @return the double click time
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getDoubleClickTime () {
	checkDevice ();
	return OS.GetDoubleClickTime ();
}

/**
 * Returns the control which currently has keyboard focus,
 * or null if keyboard events are not currently going to
 * any of the controls built by the currently running
 * application.
 *
 * @return the control under the cursor
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Control getFocusControl () {
	checkDevice ();
	return findControl (OS.GetFocus ());
}

/**
 * Returns the maximum allowed depth of icons on this display.
 * On some platforms, this may be different than the actual
 * depth of the display.
 *
 * @return the maximum icon depth
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getIconDepth () {
	checkDevice ();

	/* Use the character encoding for the default locale */
	TCHAR buffer1 = new TCHAR (0, "Control Panel\\Desktop\\WindowMetrics", true);

	int [] phkResult = new int [1];
	int result = OS.RegOpenKeyEx (OS.HKEY_CURRENT_USER, buffer1, 0, OS.KEY_READ, phkResult);
	if (result != 0) return 4;
	int depth = 4;
	int [] lpcbData = {128};
	
	/* Use the character encoding for the default locale */
	TCHAR lpData = new TCHAR (0, lpcbData [0]);
	TCHAR buffer2 = new TCHAR (0, "Shell Icon BPP", true);
	
	result = OS.RegQueryValueEx (phkResult [0], buffer2, 0, null, lpData, lpcbData);
	if (result == 0) {
		try {
			depth = Integer.parseInt (lpData.toString (0, lpData.strlen ()));
		} catch (NumberFormatException e) {};
	}
	OS.RegCloseKey (phkResult [0]);
	return depth;
}

ImageList getImageList (Point size) {
	if (imageList == null) imageList = new ImageList [4];
	
	int i = 0;
	int length = imageList.length; 
	while (i < length) {
		ImageList list = imageList [i];
		if (list == null) break;
		if (list.getImageSize().equals(size)) {
			list.addRef();
			return list;
		}
		i++;
	}
	
	if (i == length) {
		ImageList [] newList = new ImageList [length + 4];
		System.arraycopy (imageList, 0, newList, 0, length);
		imageList = newList;
	}
	
	ImageList list = new ImageList();
	imageList [i] = list;
	list.addRef();
	return list;
}

ImageList getToolImageList (Point size) {
	if (toolImageList == null) toolImageList = new ImageList [4];
	
	int i = 0;
	int length = toolImageList.length; 
	while (i < length) {
		ImageList list = toolImageList [i];
		if (list == null) break;
		if (list.getImageSize().equals(size)) {
			list.addRef();
			return list;
		}
		i++;
	}
	
	if (i == length) {
		ImageList [] newList = new ImageList [length + 4];
		System.arraycopy (toolImageList, 0, newList, 0, length);
		toolImageList = newList;
	}
	
	ImageList list = new ImageList();
	toolImageList [i] = list;
	list.addRef();
	return list;
}

ImageList getToolHotImageList (Point size) {
	if (toolHotImageList == null) toolHotImageList = new ImageList [4];
	
	int i = 0;
	int length = toolHotImageList.length; 
	while (i < length) {
		ImageList list = toolHotImageList [i];
		if (list == null) break;
		if (list.getImageSize().equals(size)) {
			list.addRef();
			return list;
		}
		i++;
	}
	
	if (i == length) {
		ImageList [] newList = new ImageList [length + 4];
		System.arraycopy (toolHotImageList, 0, newList, 0, length);
		toolHotImageList = newList;
	}
	
	ImageList list = new ImageList();
	toolHotImageList [i] = list;
	list.addRef();
	return list;
}

ImageList getToolDisabledImageList (Point size) {
	if (toolDisabledImageList == null) toolDisabledImageList = new ImageList [4];
	
	int i = 0;
	int length = toolDisabledImageList.length; 
	while (i < length) {
		ImageList list = toolDisabledImageList [i];
		if (list == null) break;
		if (list.getImageSize().equals(size)) {
			list.addRef();
			return list;
		}
		i++;
	}
	
	if (i == length) {
		ImageList [] newList = new ImageList [length + 4];
		System.arraycopy (toolDisabledImageList, 0, newList, 0, length);
		toolDisabledImageList = newList;
	}
	
	ImageList list = new ImageList();
	toolDisabledImageList [i] = list;
	list.addRef();
	return list;
}

Shell getModalShell () {
	if (modalWidgets == null) return null;
	int index = modalWidgets.length;
	while (--index >= 0) {
		Shell shell = modalWidgets [index];
		if (shell != null) return shell;
	}
	return null;
}

int getLastEventTime () {
	return OS.IsWinCE ? OS.GetTickCount () : OS.GetMessageTime ();
}

/**
 * Returns an array containing all shells which have not been
 * disposed and have the receiver as their display.
 *
 * @return the receiver's shells
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Shell [] getShells () {
	checkDevice ();
	/*
	* NOTE:  Need to check that the shells that belong
	* to another display have not been disposed by the
	* other display's thread as the shells list is being
	* processed.
	*/
	int count = 0;
	Shell [] shells = WidgetTable.shells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (!shell.isDisposed () && this == shell.getDisplay ()) {
			count++;
		}
	}
	if (count == shells.length) return shells;
	int index = 0;
	Shell [] result = new Shell [count];
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (!shell.isDisposed () && this == shell.getDisplay ()) {
			result [index++] = shell;
		}
	}
	return result;
}

/**
 * Returns the thread that has invoked <code>syncExec</code>
 * or null if no such runnable is currently being invoked by
 * the user-interface thread.
 * <p>
 * Note: If a runnable invoked by asyncExec is currently
 * running, this method will return null.
 * </p>
 *
 * @return the receiver's sync-interface thread
 */
public Thread getSyncThread () {
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
	return synchronizer.syncThread;
}

/**
 * Returns the matching standard color for the given
 * constant, which should be one of the color constants
 * specified in class <code>SWT</code>. Any value other
 * than one of the SWT color constants which is passed
 * in will result in the color black. This color should
 * not be free'd because it was allocated by the system,
 * not the application.
 *
 * @param id the color constant
 * @return the matching color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see SWT
 */
public Color getSystemColor (int id) {
	checkDevice ();
	int pixel = 0x02000000;
	switch (id) {
		case SWT.COLOR_WIDGET_DARK_SHADOW:		pixel = OS.GetSysColor (OS.COLOR_3DDKSHADOW);	break;
		case SWT.COLOR_WIDGET_NORMAL_SHADOW:	pixel = OS.GetSysColor (OS.COLOR_3DSHADOW); 	break;
		case SWT.COLOR_WIDGET_LIGHT_SHADOW: 	pixel = OS.GetSysColor (OS.COLOR_3DLIGHT);  	break;
		case SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW:	pixel = OS.GetSysColor (OS.COLOR_3DHIGHLIGHT);  break;
		case SWT.COLOR_WIDGET_BACKGROUND: 		pixel = OS.GetSysColor (OS.COLOR_3DFACE);  	break;
		case SWT.COLOR_WIDGET_BORDER: 		pixel = OS.GetSysColor (OS.COLOR_WINDOWFRAME);  break;
		case SWT.COLOR_WIDGET_FOREGROUND:
		case SWT.COLOR_LIST_FOREGROUND: 		pixel = OS.GetSysColor (OS.COLOR_WINDOWTEXT);	break;
		case SWT.COLOR_LIST_BACKGROUND: 		pixel = OS.GetSysColor (OS.COLOR_WINDOW);  	break;
		case SWT.COLOR_LIST_SELECTION: 		pixel = OS.GetSysColor (OS.COLOR_HIGHLIGHT);	break;
		case SWT.COLOR_LIST_SELECTION_TEXT: 	pixel = OS.GetSysColor (OS.COLOR_HIGHLIGHTTEXT);break;
		case SWT.COLOR_INFO_FOREGROUND:		pixel = OS.GetSysColor (OS.COLOR_INFOTEXT);	break;
		case SWT.COLOR_INFO_BACKGROUND:		pixel = OS.GetSysColor (OS.COLOR_INFOBK);		break;
		case SWT.COLOR_TITLE_FOREGROUND: 		pixel = OS.GetSysColor (OS.COLOR_CAPTIONTEXT);	break;
		case SWT.COLOR_TITLE_BACKGROUND:		pixel = OS.GetSysColor (OS.COLOR_ACTIVECAPTION);		break;
		case SWT.COLOR_TITLE_BACKGROUND_GRADIENT: 
			pixel = OS.GetSysColor (OS.COLOR_GRADIENTACTIVECAPTION);
			if (pixel == 0) pixel = OS.GetSysColor (OS.COLOR_ACTIVECAPTION);
			break;
		case SWT.COLOR_TITLE_INACTIVE_FOREGROUND: 		pixel = OS.GetSysColor (OS.COLOR_INACTIVECAPTIONTEXT);	break;
		case SWT.COLOR_TITLE_INACTIVE_BACKGROUND:			pixel = OS.GetSysColor (OS.COLOR_INACTIVECAPTION);		break;
		case SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT:	
			pixel = OS.GetSysColor (OS.COLOR_GRADIENTINACTIVECAPTION);
			if (pixel == 0) pixel = OS.GetSysColor (OS.COLOR_INACTIVECAPTION);
			break;
		default:
			return super.getSystemColor (id);
	}
	return Color.win32_new (this, pixel);
}

/**
 * Returns a reasonable font for applications to use.
 * On some platforms, this will match the "default font"
 * or "system font" if such can be found.  This font
 * should not be free'd because it was allocated by the
 * system, not the application.
 * <p>
 * Typically, applications which want the default look
 * should simply not set the font on the widgets they
 * create. Widgets are always created with the correct
 * default font for the class of user-interface component
 * they represent.
 * </p>
 *
 * @return a font
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Font getSystemFont () {
	checkDevice ();
	int hFont = systemFont ();
	return Font.win32_new (this, hFont);	
}

/**
 * Returns the user-interface thread for the receiver.
 *
 * @return the receiver's user-interface thread
 */
public Thread getThread () {
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
	return thread;
}

/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Display</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param data the platform specific GC data 
 * @return the platform specific GC handle
 *
 * @private
 */
public int internal_new_GC (GCData data) {
	if (isDisposed()) SWT.error(SWT.ERROR_DEVICE_DISPOSED);
	int hDC = OS.GetDC (0);
	if (hDC == 0) SWT.error (SWT.ERROR_NO_HANDLES);
	if (data != null) {
		data.device = this;
		data.hFont = systemFont ();
	}
	return hDC;
}

/**
 * Initializes any internal resources needed by the
 * device.
 * <p>
 * This method is called after <code>create</code>.
 * </p>
 * 
 * @see #create
 */
protected void init () {
	super.init ();
		
	/* Create the callbacks */
	windowCallback = new Callback (this, "windowProc", 4);
	windowProc = windowCallback.getAddress ();
	if (windowProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	/* Remember the current procsss and thread */
	threadId = OS.GetCurrentThreadId ();
	processId = OS.GetCurrentProcessId ();
	
	/* Use the character encoding for the default locale */
	windowClass = new TCHAR (0, WindowName + WindowClassCount++, true);

	/* Register the SWT window class */
	int hHeap = OS.GetProcessHeap ();
	int hInstance = OS.GetModuleHandle (null);
	WNDCLASS lpWndClass = new WNDCLASS ();
	lpWndClass.hInstance = hInstance;
	lpWndClass.lpfnWndProc = windowProc;
	lpWndClass.style = OS.CS_BYTEALIGNWINDOW | OS.CS_DBLCLKS;
	lpWndClass.hCursor = OS.LoadCursor (0, OS.IDC_ARROW);
	int byteCount = windowClass.length () * TCHAR.sizeof;
	int lpszClassName = OS.HeapAlloc (hHeap, OS.HEAP_ZERO_MEMORY, byteCount);
	lpWndClass.lpszClassName = lpszClassName;
	OS.MoveMemory (lpszClassName, windowClass, byteCount);
	OS.RegisterClass (lpWndClass);
	
	/* Initialize the system font */
	int systemFont = 0;
	if (!OS.IsWinCE) {
		NONCLIENTMETRICS info = new NONCLIENTMETRICS ();
		info.cbSize = NONCLIENTMETRICS.sizeof;
		if (OS.SystemParametersInfo (OS.SPI_GETNONCLIENTMETRICS, 0, info, 0)) {
			systemFont = OS.CreateFontIndirect (info.lfMessageFont);
		}
	}
	if (systemFont == 0) systemFont = OS.GetStockObject (OS.DEFAULT_GUI_FONT);
	if (systemFont == 0) systemFont = OS.GetStockObject (OS.SYSTEM_FONT);
	if (systemFont != 0) systemFonts = new int [] {systemFont};
	
	/* Create the message only HWND */
	hwndMessage = OS.CreateWindowEx (0,
		windowClass,
		null,
		OS.WS_OVERLAPPED,
		0, 0, 0, 0,
		0,
		0,
		hInstance,
		null);
	messageCallback = new Callback (this, "messageProc", 4);
	messageProc = messageCallback.getAddress ();
	if (messageProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	OS.SetWindowLong (hwndMessage, OS.GWL_WNDPROC, messageProc);

	/* Create the message filter hook */
	if (!OS.IsWinCE) {
		msgFilterCallback = new Callback (this, "msgFilterProc", 3);
		msgFilterProc = msgFilterCallback.getAddress ();
		if (messageProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
		hHook = OS.SetWindowsHookEx (OS.WH_MSGFILTER, msgFilterProc, 0, threadId);
	}
}

/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Display</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param handle the platform specific GC handle
 * @param data the platform specific GC data 
 *
 * @private
 */
public void internal_dispose_GC (int hDC, GCData data) {
	OS.ReleaseDC (0, hDC);
}

boolean isWakeMessage (MSG msg) {
	return msg.hwnd == hwndMessage && msg.message == OS.WM_NULL;
}

boolean isValidThread () {
	return thread == Thread.currentThread ();
}

boolean isVirtualKey (int key) {
	switch (key) {
		case OS.VK_TAB:
		case OS.VK_RETURN:
		case OS.VK_BACK:
		case OS.VK_ESCAPE:
//		case OS.VK_DELETE:
		case OS.VK_SPACE:
		case OS.VK_MENU:
		case OS.VK_SHIFT:
		case OS.VK_CONTROL: return true;
	}
	return false;
}

int messageProc (int hwnd, int msg, int wParam, int lParam) {
	switch (msg) {
		case OS.WM_ENDSESSION:
			if (wParam != 0) dispose ();
			break;
		case OS.WM_NULL:
			runAsyncMessages ();
			break;
		case OS.WM_QUERYENDSESSION:
			Event event = new Event ();
			sendEvent (SWT.Close, event);
			if (!event.doit) return 0;
			break;
		case OS.WM_SETTINGCHANGE:
			updateFont ();
			break;
		case OS.WM_TIMER:
			runTimer (wParam);
			break;
	}
	return OS.DefWindowProc (hwnd, msg, wParam, lParam);
}

int msgFilterProc (int code, int wParam, int lParam) {
	if (code >= 0) {
		OS.MoveMemory (hookMsg, lParam, MSG.sizeof);
		if (hookMsg.message == OS.WM_NULL) runAsyncMessages ();
	}
	return OS.CallNextHookEx (hHook, code, wParam, lParam);
}

int numpadKey (int key) {
	switch (key) {
		case OS.VK_NUMPAD0:	return '0';
		case OS.VK_NUMPAD1:	return '1';
		case OS.VK_NUMPAD2:	return '2';
		case OS.VK_NUMPAD3:	return '3';
		case OS.VK_NUMPAD4:	return '4';
		case OS.VK_NUMPAD5:	return '5';
		case OS.VK_NUMPAD6:	return '6';
		case OS.VK_NUMPAD7:	return '7';
		case OS.VK_NUMPAD8:	return '8';
		case OS.VK_NUMPAD9:	return '9';
		case OS.VK_MULTIPLY:	return '*';
		case OS.VK_ADD: 		return '+';
		case OS.VK_SEPARATOR:	return '\0';
		case OS.VK_SUBTRACT:	return '-';
		case OS.VK_DECIMAL:	return '.';
		case OS.VK_DIVIDE:		return '/';
	}
	return 0;
}

void postEvent (Event event) {
	/*
	* Place the event at the end of the event queue.
	* This code is always called in the Display's
	* thread so it must be re-enterant but does not
	* need to be synchronized.
	*/
	if (eventQueue == null) eventQueue = new Event [4];
	int index = 0;
	int length = eventQueue.length;
	while (index < length) {
		if (eventQueue [index] == null) break;
		index++;
	}
	if (index == length) {
		Event [] newQueue = new Event [length + 4];
		System.arraycopy (eventQueue, 0, newQueue, 0, length);
		eventQueue = newQueue;
	}
	eventQueue [index] = event;
}

/**
 * Reads an event from the operating system's event queue,
 * dispatches it appropriately, and returns <code>true</code>
 * if there is potentially more work to do, or <code>false</code>
 * if the caller can sleep until another event is placed on
 * the event queue.
 * <p>
 * In addition to checking the system event queue, this method also
 * checks if any inter-thread messages (created by <code>syncExec()</code>
 * or <code>asyncExec()</code>) are waiting to be processed, and if
 * so handles them before returning.
 * </p>
 *
 * @return <code>false</code> if the caller can sleep upon return from this method
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #sleep
 * @see #wake
 */
public boolean readAndDispatch () {
	checkDevice ();
	runPopups ();
	if (OS.PeekMessage (msg, 0, 0, 0, OS.PM_REMOVE)) {
		if (!isWakeMessage (msg)) {
			if (!filterMessage (msg)) {
				OS.TranslateMessage (msg);
				OS.DispatchMessage (msg);
			}
			runDeferredEvents ();
			return true;
		}
	}
	return runAsyncMessages ();
}

static synchronized void register (Display display) {
	for (int i=0; i<Displays.length; i++) {
		if (Displays [i] == null) {
			Displays [i] = display;
			return;
		}
	}
	Display [] newDisplays = new Display [Displays.length + 4];
	System.arraycopy (Displays, 0, newDisplays, 0, Displays.length);
	newDisplays [Displays.length] = display;
	Displays = newDisplays;
}

/**
 * Releases any internal resources back to the operating
 * system and clears all fields except the device handle.
 * <p>
 * Disposes all shells which are currently open on the display. 
 * After this method has been invoked, all related related shells
 * will answer <code>true</code> when sent the message
 * <code>isDisposed()</code>.
 * </p><p>
 * When a device is destroyed, resources that were acquired
 * on behalf of the programmer need to be returned to the
 * operating system.  For example, if the device allocated a
 * font to be used as the system font, this font would be
 * freed in <code>release</code>.  Also,to assist the garbage
 * collector and minimize the amount of memory that is not
 * reclaimed when the programmer keeps a reference to a
 * disposed device, all fields except the handle are zero'd.
 * The handle is needed by <code>destroy</code>.
 * </p>
 * This method is called before <code>destroy</code>.
 * 
 * @see #dispose
 * @see #destroy
 */
protected void release () {
	sendEvent (SWT.Dispose, new Event ());
	Shell [] shells = WidgetTable.shells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (!shell.isDisposed ()) {
			if (this == shell.getDisplay ()) shell.dispose ();
		}
	}
	while (readAndDispatch ()) {};
	if (disposeList != null) {
		for (int i=0; i<disposeList.length; i++) {
			if (disposeList [i] != null) disposeList [i].run ();
		}
	}
	disposeList = null;
	synchronizer.releaseSynchronizer ();
	synchronizer = null;
	releaseDisplay ();
	super.release ();
}

void releaseDisplay () {

	/* Unhook the message hook */
	if (!OS.IsWinCE) {
		if (hHook != 0) OS.UnhookWindowsHookEx (hHook);
		hHook = 0;
		msgFilterCallback.dispose ();
		msgFilterCallback = null;
		msgFilterProc = 0;
	}

	/* Destroy the message only HWND */
	if (hwndMessage != 0) OS.DestroyWindow (hwndMessage);
	hwndMessage = 0;
	messageCallback.dispose ();
	messageCallback = null;
	messageProc = 0;
	
	/* Unregister the SWT Window class */
	int hHeap = OS.GetProcessHeap ();
	int hInstance = OS.GetModuleHandle (null);
	WNDCLASS lpWndClass = new WNDCLASS ();
	OS.GetClassInfo (0, windowClass, lpWndClass);
	OS.UnregisterClass (windowClass, hInstance);
	OS.HeapFree (hHeap, 0, lpWndClass.lpszClassName);
	windowClass = null;
	windowCallback.dispose ();
	windowCallback = null;
	windowProc = 0;
	
	/* Release the system fonts */
	if (systemFonts != null) {
		for (int i=0; i<systemFonts.length; i++) {
			if (systemFonts [i] != 0) OS.DeleteObject (systemFonts [i]);
		}
	}
	systemFonts = null;
	
	/* Release Custom Colors for ChooseColor */
	if (lpCustColors != 0) OS.HeapFree (hHeap, 0, lpCustColors);
	lpCustColors = 0;
	
	/* Release references */
	thread = null;
	msg = null;
	keyboard = null;
	modalWidgets = null;
	data = null;
	keys = null;
	values = null;
}

void releaseImageList (ImageList list) {
	int i = 0;
	int length = imageList.length; 
	while (i < length) {
		if (imageList [i] == list) {
			if (list.removeRef () > 0) return;
			list.dispose ();
			System.arraycopy (imageList, i + 1, imageList, i, --length - i);
			imageList [length] = null;
			for (int j=0; j<length; j++) {
				if (imageList [j] != null) return;
			}
			imageList = null;
			return;
		}
		i++;
	}
}

void releaseToolImageList (ImageList list) {
	int i = 0;
	int length = toolImageList.length; 
	while (i < length) {
		if (toolImageList [i] == list) {
			if (list.removeRef () > 0) return;
			list.dispose ();
			System.arraycopy (toolImageList, i + 1, toolImageList, i, --length - i);
			toolImageList [length] = null;
			for (int j=0; j<length; j++) {
				if (toolImageList [j] != null) return;
			}
			toolImageList = null;
			return;
		}
		i++;
	}
}

void releaseToolHotImageList (ImageList list) {
	int i = 0;
	int length = toolHotImageList.length; 
	while (i < length) {
		if (toolHotImageList [i] == list) {
			if (list.removeRef () > 0) return;
			list.dispose ();
			System.arraycopy (toolHotImageList, i + 1, toolHotImageList, i, --length - i);
			toolHotImageList [length] = null;
			for (int j=0; j<length; j++) {
				if (toolHotImageList [j] != null) return;
			}
			toolHotImageList = null;
			return;
		}
		i++;
	}
}

void releaseToolDisabledImageList (ImageList list) {
	int i = 0;
	int length = toolDisabledImageList.length; 
	while (i < length) {
		if (toolDisabledImageList [i] == list) {
			if (list.removeRef () > 0) return;
			list.dispose ();
			System.arraycopy (toolDisabledImageList, i + 1, toolDisabledImageList, i, --length - i);
			toolDisabledImageList [length] = null;
			for (int j=0; j<length; j++) {
				if (toolDisabledImageList [j] != null) return;
			}
			toolDisabledImageList = null;
			return;
		}
		i++;
	}
}

/**
 * Removes the listener from the collection of listeners who will
 * be notifed when an event of the given type occurs anywhere in SWT.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should no longer be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Listener
 * @see #addFilter
 * @see #addListener
 * 
 * @since 2.1 
 */
public void removeFilter (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (filterTable == null) return;
	filterTable.unhook (eventType, listener);
	if (filterTable.size () == 0) filterTable = null;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notifed when an event of the given type occurs.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should no longer be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Listener
 * @see #addListener
 * 
 * @since 2.0 
 */
public void removeListener (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (eventType, listener);
}

void removePopup (Menu menu) {
	if (popups == null) return;
	for (int i=0; i<popups.length; i++) {
		if (popups [i] == menu) {
			popups [i] = null;
			return;
		}
	}
}

boolean runAsyncMessages () {
	return synchronizer.runAsyncMessages ();
}

boolean runDeferredEvents () {
	/*
	* Run deferred events.  This code is always
	* called in the Display's thread so it must
	* be re-enterant but need not be synchronized.
	*/
	while (eventQueue != null) {
		
		/* Take an event off the queue */
		Event event = eventQueue [0];
		if (event == null) break;
		int length = eventQueue.length;
		System.arraycopy (eventQueue, 1, eventQueue, 0, --length);
		eventQueue [length] = null;

		/* Run the event */
		Widget widget = event.widget;
		if (widget != null && !widget.isDisposed ()) {
			Widget item = event.item;
			if (item == null || !item.isDisposed ()) {
				widget.sendEvent (event);
			}
		}

		/*
		* At this point, the event queue could
		* be null due to a recursive invokation
		* when running the event.
		*/
	}

	/* Clear the queue */
	eventQueue = null;
	return true;
}

void runPopups () {
	while (popups != null) {
		Menu menu = popups [0];
		if (menu == null) break;
		int length = popups.length;
		System.arraycopy (popups, 1, popups, 0, --length);
		popups [length] = null;
		menu._setVisible (true);
	}
	popups = null;
}

void runTimer (int id) {
	if (timerList != null && timerIds != null) {
		int index = 0;
		while (index <timerIds.length) {
			if (timerIds [index] == id) {
				OS.KillTimer (hwndMessage, timerIds [index]);
				timerIds [index] = 0;
				Runnable runnable = timerList [index];
				timerList [index] = null;
				if (runnable != null) runnable.run ();
				break;
			}
			index++;
		}
	}
}

void sendEvent (int eventType, Event event) {
	if (eventTable == null && filterTable == null) {
		return;
	}
	if (event == null) event = new Event ();
	event.display = this;
	event.type = eventType;
	if (event.time == 0) event.time = getLastEventTime ();
	if (!filterEvent (event)) {
		if (eventTable != null) eventTable.sendEvent (event);
	}
}

/**
 * Sets the location of the on-screen pointer relative to the top left corner
 * of the screen.  <b>Note: It is typically considered bad practice for a
 * program to move the on-screen pointer location.</b>
 *
 * @param point new position 
 * @since 2.0
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null
 * </ul>
 */
public void setCursorLocation (Point point) {
	checkDevice ();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);
	OS.SetCursorPos (point.x, point.y);
}

/**
 * Sets the application defined property of the receiver
 * with the specified name to the given argument.
 * <p>
 * Applications may have associated arbitrary objects with the
 * receiver in this fashion. If the objects stored in the
 * properties need to be notified when the display is disposed
 * of, it is the application's responsibility provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @param key the name of the property
 * @param value the new value for the property
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #setData
 * @see #disposeExec
 */
public void setData (String key, Object value) {
	checkDevice ();
	if (key == null) error (SWT.ERROR_NULL_ARGUMENT);
	
	/* Remove the key/value pair */
	if (value == null) {
		if (keys == null) return;
		int index = 0;
		while (index < keys.length && !keys [index].equals (key)) index++;
		if (index == keys.length) return;
		if (keys.length == 1) {
			keys = null;
			values = null;
		} else {
			String [] newKeys = new String [keys.length - 1];
			Object [] newValues = new Object [values.length - 1];
			System.arraycopy (keys, 0, newKeys, 0, index);
			System.arraycopy (keys, index + 1, newKeys, index, newKeys.length - index);
			System.arraycopy (values, 0, newValues, 0, index);
			System.arraycopy (values, index + 1, newValues, index, newValues.length - index);
			keys = newKeys;
			values = newValues;
		}
		return;
	}
	
	/* Add the key/value pair */
	if (keys == null) {
		keys = new String [] {key};
		values = new Object [] {value};
		return;
	}
	for (int i=0; i<keys.length; i++) {
		if (keys [i].equals (key)) {
			values [i] = value;
			return;
		}
	}
	String [] newKeys = new String [keys.length + 1];
	Object [] newValues = new Object [values.length + 1];
	System.arraycopy (keys, 0, newKeys, 0, keys.length);
	System.arraycopy (values, 0, newValues, 0, values.length);
	newKeys [keys.length] = key;
	newValues [values.length] = value;
	keys = newKeys;
	values = newValues;
}

/**
 * Sets the application defined, display specific data
 * associated with the receiver, to the argument.
 * The <em>display specific data</em> is a single,
 * unnamed field that is stored with every display. 
 * <p>
 * Applications may put arbitrary objects in this field. If
 * the object stored in the display specific data needs to
 * be notified when the display is disposed of, it is the
 * application's responsibility provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @param data the new display specific data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - when called from the wrong thread</li>
 * </ul>
 *
 * @see #getData
 * @see #disposeExec
 */
public void setData (Object data) {
	checkDevice ();
	this.data = data;
}

/**
 * On platforms which support it, sets the application name
 * to be the argument. On Motif, for example, this can be used
 * to set the name used for resource lookup.
 *
 * @param name the new app name
 */
public static void setAppName (String name) {
	/* Do nothing */
}

void setModal (Shell shell) {
	if (modalWidgets == null) modalWidgets = new Shell [4];
	int index = 0, length = modalWidgets.length;
	while (index < length) {
		if (modalWidgets [index] == shell) return;
		if (modalWidgets [index] == null) break;
		index++;
	}
	if (index == length) {
		Shell [] newModalWidgets = new Shell [length + 4];
		System.arraycopy (modalWidgets, 0, newModalWidgets, 0, length);
		modalWidgets = newModalWidgets;
	}
	modalWidgets [index] = shell;
	Shell [] shells = getShells ();
	for (int i=0; i<shells.length; i++) shells [i].updateModal ();
}

/**
 * Sets the synchronizer used by the display to be
 * the argument, which can not be null.
 *
 * @param synchronizer the new synchronizer for the display (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the synchronizer is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSynchronizer (Synchronizer synchronizer) {
	checkDevice ();
	if (synchronizer == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (this.synchronizer != null) {
		this.synchronizer.runAsyncMessages();
	}
	this.synchronizer = synchronizer;
}

int shiftedKey (int key) {
	if (OS.IsWinCE) return 0;
	
	/* Clear the virtual keyboard and press the shift key */
	for (int i=0; i<keyboard.length; i++) keyboard [i] = 0;
	keyboard [OS.VK_SHIFT] |= 0x80;

	/* Translate the key to ASCII or UNICODE using the virtual keyboard */
	if (OS.IsUnicode) {
		char [] result = new char [1];
		if (OS.ToUnicode (key, key, keyboard, result, 1, 0) == 1) return result [0];
	} else {
		short [] result = new short [1];
		if (OS.ToAscii (key, key, keyboard, result, 0) == 1) return result [0];
	}
	return 0;
}

/**
 * Causes the user-interface thread to <em>sleep</em> (that is,
 * to be put in a state where it does not consume CPU cycles)
 * until an event is received or it is otherwise awakened.
 *
 * @return <code>true</code> if an event requiring dispatching was placed on the queue.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #wake
 */
public boolean sleep () {
	checkDevice ();
	if (OS.IsWinCE) {
		OS.MsgWaitForMultipleObjectsEx (0, 0, OS.INFINITE, OS.QS_ALLINPUT, OS.MWMO_INPUTAVAILABLE);
		return true;
	}
	return OS.WaitMessage ();
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread at the next 
 * reasonable opportunity. The thread which calls this method
 * is suspended until the runnable completes.
 *
 * @param runnable code to run on the user-interface thread.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_FAILED_EXEC - if an exception occured when executing the runnable</li>
 * </ul>
 *
 * @see #asyncExec
 */
public void syncExec (Runnable runnable) {
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
	synchronizer.syncExec (runnable);
}

int systemFont () {
	int hFont = 0;
	if (systemFonts != null) {
		int length = systemFonts.length;
		if (length != 0) hFont = systemFonts [length - 1];
	}
	if (hFont == 0) hFont = OS.GetStockObject (OS.DEFAULT_GUI_FONT);
	if (hFont == 0) hFont = OS.GetStockObject (OS.SYSTEM_FONT);
	return hFont;
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread after the specified
 * number of milliseconds have elapsed. If milliseconds is less
 * than zero, the runnable is not executed.
 *
 * @param milliseconds the delay before running the runnable
 * @param runnable code to run on the user-interface thread
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #asyncExec
 */
public void timerExec (int milliseconds, Runnable runnable) {
	checkDevice ();
	if (runnable == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (timerList == null) timerList = new Runnable [4];
	if (timerIds == null) timerIds = new int [4];
	int index = 0;
	while (index < timerList.length) {
		if (timerList [index] == runnable) break;
		index++;
	}
	int timerId = 0;
	if (index != timerList.length) {
		timerId = timerIds [index];
		if (milliseconds < 0) {			
			OS.KillTimer (hwndMessage, timerId);
			timerList [index] = null;
			timerIds [index] = 0;
			return;
		}
	} else {
		if (milliseconds < 0) return;
		index = 0;
		while (index < timerList.length) {
			if (timerList [index] == null) break;
			index++;
		}
		timerCount++;
		timerId = timerCount;
		if (index == timerList.length) {
			Runnable [] newTimerList = new Runnable [timerList.length + 4];
			System.arraycopy (timerList, 0, newTimerList, 0, timerList.length);
			timerList = newTimerList;
			int [] newTimerIds = new int [timerIds.length + 4];
			System.arraycopy (timerIds, 0, newTimerIds, 0, timerIds.length);
			timerIds = newTimerIds;
		}
	}
	int newTimerID = OS.SetTimer (hwndMessage, timerId, milliseconds, 0);
	if (newTimerID != 0) {
		timerList [index] = runnable;
		timerIds [index] = newTimerID;
	}
}

boolean translateAccelerator (MSG msg, Control control) {
	accelKeyHit = true;
	boolean result = control.translateAccelerator (msg);
	accelKeyHit = false;
	return result;
}

static int translateKey (int key) {
	for (int i=0; i<KeyTable.length; i++) {
		if (KeyTable [i] [0] == key) return KeyTable [i] [1];
	}
	return 0;
}

boolean translateMnemonic (MSG msg, Control control) {
	switch (msg.message) {
		case OS.WM_CHAR:
		case OS.WM_SYSCHAR:
			return control.translateMnemonic (msg);
	}
	return false;
}

boolean translateTraversal (MSG msg, Control control) {
	switch (msg.message) {
		case OS.WM_KEYDOWN:
			switch (msg.wParam) {
				case OS.VK_RETURN:
				case OS.VK_ESCAPE:
				case OS.VK_TAB:
				case OS.VK_UP:
				case OS.VK_DOWN:
				case OS.VK_LEFT:
				case OS.VK_RIGHT:
				case OS.VK_PRIOR:
				case OS.VK_NEXT:
					return control.translateTraversal (msg);
			}
			break;
		case OS.WM_SYSKEYDOWN:
			switch (msg.wParam) {
				case OS.VK_MENU:
					return control.translateTraversal (msg);
			}
			break;
	}
	return false;
}

static int untranslateKey (int key) {
	for (int i=0; i<KeyTable.length; i++) {
		if (KeyTable [i] [1] == key) return KeyTable [i] [0];
	}
	return 0;
}

/**
 * Forces all outstanding paint requests for the display
 * to be processed before this method returns.
 *
 * @see Control#update
 */
public void update() {
	checkDevice ();
	Shell[] shells = WidgetTable.shells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (!shell.isDisposed () && this == shell.getDisplay ()) {
			shell.update ();
		}
	}
}

void updateFont () {
	if (OS.IsWinCE) return;
	Font oldFont = getSystemFont ();
	int systemFont = 0;
	NONCLIENTMETRICS info = new NONCLIENTMETRICS ();
	info.cbSize = NONCLIENTMETRICS.sizeof;
	if (OS.SystemParametersInfo (OS.SPI_GETNONCLIENTMETRICS, 0, info, 0)) {
		systemFont = OS.CreateFontIndirect (info.lfMessageFont);
	}
	if (systemFont == 0) systemFont = OS.GetStockObject (OS.DEFAULT_GUI_FONT);
	if (systemFont == 0) systemFont = OS.GetStockObject (OS.SYSTEM_FONT);
	if (systemFont == 0) return;
	int length = systemFonts == null ? 0 : systemFonts.length;
	int [] newFonts = new int [length + 1];
	if (systemFonts != null) {
		System.arraycopy (systemFonts, 0, newFonts, 0, length);
	}
	newFonts [length] = systemFont;
	systemFonts = newFonts;
	Font newFont = getSystemFont ();
	Shell [] shells = getShells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (!shell.isDisposed ()) {
			shell.updateFont (oldFont, newFont);
		}
	}
}

/**
 * If the receiver's user-interface thread was <code>sleep</code>'ing, 
 * causes it to be awakened and start running again. Note that this
 * method may be called from any thread.
 *
 * @see #sleep
 */
public void wake () {
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
	if (thread == Thread.currentThread ()) return;
	if (OS.IsWinCE) {
		OS.PostMessage (hwndMessage, OS.WM_NULL, 0, 0);
	} else {
		OS.PostThreadMessage (threadId, OS.WM_NULL, 0, 0);
	}
}

int windowProc (int hwnd, int msg, int wParam, int lParam) {
	Control control = WidgetTable.get (hwnd);
	if (control != null) {
		return control.windowProc (msg, wParam, lParam);
	}
	return OS.DefWindowProc (hwnd, msg, wParam, lParam);
}

static String withCrLf (String string) {

	/* If the string is empty, return the string. */
	int length = string.length ();
	if (length == 0) return string;
	
	/*
	* Check for an LF or CR/LF and assume the rest of
	* the string is formated that way.  This will not
	* work if the string contains mixed delimiters.
	*/
	int i = string.indexOf ('\n', 0);
	if (i == -1) return string;
	if (i > 0 && string.charAt (i - 1) == '\r') {
		return string;
	}

	/*
	* The string is formatted with LF.  Compute the
	* number of lines and the size of the buffer
	* needed to hold the result
	*/
	i++;	
	int count = 1;
	while (i < length) {
		if ((i = string.indexOf ('\n', i)) == -1) break;
		count++; i++;
	}
	count += length;

	/* Create a new string with the CR/LF line terminator. */
	i = 0;
	StringBuffer result = new StringBuffer (count);
	while (i < length) {
		int j = string.indexOf ('\n', i);
		if (j == -1) j = length;
		result.append (string.substring (i, j));
		if ((i = j) < length) {
			result.append ("\r\n");
			i++;
		}
	}
	return result.toString ();
}

}
