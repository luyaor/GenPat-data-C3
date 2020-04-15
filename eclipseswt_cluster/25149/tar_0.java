package org.eclipse.swt.widgets;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.carbon.*;
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
 * 
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 * @see #readAndDispatch
 * @see #sleep
 * @see #dispose
 */
public class Display extends Device {

	/* Windows, Events and Callbacks */
	static String APP_NAME = "SWT";
	Event [] eventQueue;
	EventTable eventTable;
	
	/* Default Fonts, Colors, Insets, Widths and Heights. */
	Font defaultFont;
	Font listFont, textFont, buttonFont, labelFont, groupFont;		
	private short fHoverThemeFont;

	int dialogBackground, dialogForeground;
	int buttonBackground, buttonForeground, buttonShadowThickness;
	int compositeBackground, compositeForeground;
	int compositeTopShadow, compositeBottomShadow, compositeBorder;
	int listBackground, listForeground, listSelect, textBackground, textForeground;
	int labelBackground, labelForeground, scrollBarBackground, scrollBarForeground;
	int scrolledInsetX, scrolledInsetY, scrolledMarginX, scrolledMarginY;
	int defaultBackground, defaultForeground;
	int textHighlightThickness;
	
	/* System Colors */
	Color COLOR_WIDGET_DARK_SHADOW, COLOR_WIDGET_NORMAL_SHADOW, COLOR_WIDGET_LIGHT_SHADOW;
	Color COLOR_WIDGET_HIGHLIGHT_SHADOW, COLOR_WIDGET_BACKGROUND, COLOR_WIDGET_BORDER;
	Color COLOR_LIST_FOREGROUND, COLOR_LIST_BACKGROUND, COLOR_LIST_SELECTION, COLOR_LIST_SELECTION_TEXT;
	Color COLOR_INFO_BACKGROUND;
	
	/* Initial Guesses for Shell Trimmings. */
	int borderTrimWidth = 4, borderTrimHeight = 4;
	int resizeTrimWidth = 6, resizeTrimHeight = 6;
	int titleBorderTrimWidth = 5, titleBorderTrimHeight = 28;
	int titleResizeTrimWidth = 6, titleResizeTrimHeight = 29;
	int titleTrimWidth = 0, titleTrimHeight = 23;
	
	/* Sync/Async Widget Communication */
	Synchronizer synchronizer = new Synchronizer (this);
	Thread thread;
	
	/* Display Shutdown */
	Runnable [] disposeList;
	
	/* Timers */
	int [] timerIDs;
	Runnable [] timerList;
	int timerProc;
	
	/* Key Mappings. */
	static int [] [] KeyTable = {
	
		// AW
		//{49,				0x20},	// space
		{51,				SWT.BS},
		// AW
		
		// Keyboard and Mouse Masks
//		{OS.XK_Alt_L,		SWT.ALT},
//		{OS.XK_Alt_R,		SWT.ALT},
//		{OS.XK_Shift_L,		SWT.SHIFT},
//		{OS.XK_Shift_R,		SWT.SHIFT},
//		{OS.XK_Control_L,	SWT.CONTROL},
//		{OS.XK_Control_R,	SWT.CONTROL},
		
//		{OS.VK_LBUTTON, SWT.BUTTON1},
//		{OS.VK_MBUTTON, SWT.BUTTON3},
//		{OS.VK_RBUTTON, SWT.BUTTON2},
		
		// Non-Numeric Keypad Constants
		{126,				SWT.ARROW_UP},
		{125,				SWT.ARROW_DOWN},
		{123,				SWT.ARROW_LEFT},
		{124,				SWT.ARROW_RIGHT},
		{116,				SWT.PAGE_UP},
		{121,				SWT.PAGE_DOWN},
		{115,				SWT.HOME},
		{119,				SWT.END},
		{71,				SWT.INSERT},
//		{OS.XK_Delete,		SWT.DELETE},
	
		// Functions Keys 
		{122,		SWT.F1},
		{120,		SWT.F2},
		{99,		SWT.F3},
		{118,		SWT.F4},
		{96,		SWT.F5},
		{97,		SWT.F6},
		{98,		SWT.F7},
		{100,		SWT.F8},
		{101,		SWT.F9},
		{109,		SWT.F10},
		{103,		SWT.F11},
		{111,		SWT.F12},
	};

	/* Multiple Displays. */
	static Display Default;
	static Display [] Displays = new Display [4];

	/* Double Click */
	int lastTime, lastButton;
	
	/* mouse button state */
	int fMouseButtonState;
	
	/* Current caret */
	Caret currentCaret;
	int caretID, caretProc;
			
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
	
	/* Mouse Hover */
	int mouseHoverID, mouseHoverProc;
	int mouseHoverHandle, toolTipWindowHandle;
			
	/* Display Data */
	Object data;
	String [] keys;
	Object [] values;
	
	/* AW Mac */
	private static final int TOOLTIP_MARGIN= 3;
	private static final int HOVER_TIMEOUT= 500;	// in milli seconds
	private static final int SWT_USER_EVENT= ('S'<<24) + ('W'<<16) + ('T'<<8) + '1';
	private static final boolean SMART_REFRESH= false;

	// callback procs
	int fApplicationProc;
	//int fMouseProc;
	int fWindowProc;
	int fMenuProc;
	int fControlProc;
	int fControlActionProc;
	int fUserPaneDrawProc;
	int fUserPaneHitTestProc;
	int fDataBrowserDataProc;
	int fDataBrowserItemNotificationProc;
	
	private int fUpdateRegion;
	private int fTrackedControl;
	private int fFocusControl;
	private int fCurrentControl;
	private String fToolTipText;
	private int fLastHoverHandle;
	private boolean fInContextMenu;	// true while tracking context menu
	
	private static boolean fgCarbonInitialized;
	/* end AW */
	
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
	super (checkNull (data));
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

void addMouseHoverTimeOut (int handle) {
	if (mouseHoverID != 0) OS.RemoveEventLoopTimer(mouseHoverID);
	mouseHoverID = 0;
	if (handle == fLastHoverHandle) return;
	int[] timer= new int[1];
	OS.InstallEventLoopTimer(OS.GetCurrentEventLoop(), HOVER_TIMEOUT / 1000.0, 0.0, mouseHoverProc, handle, timer);
	mouseHoverID = timer[0];
	mouseHoverHandle = handle;
}
static DeviceData checkNull (DeviceData data) {
	if (data == null) data = new DeviceData ();
	return data;
}
protected void checkDevice () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
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
	OS.SysBeep((short)100);
}
int caretProc (int clientData, int id) {
	if (id != caretID) {
		return 0;
	}
	OS.RemoveEventLoopTimer(id);
	caretID = 0;
	if (currentCaret == null) return 0;
	if (currentCaret.blinkCaret ()) {
		int blinkRate = currentCaret.blinkRate;
		int[] timer= new int[1];
		OS.InstallEventLoopTimer(OS.GetCurrentEventLoop(), blinkRate / 1000.0, 0.0, caretProc, 0, timer);
		caretID = timer[0];
	} else {
		currentCaret = null;
	}
	return 0;
}
static synchronized void checkDisplay (Thread thread) {
	for (int i=0; i<Displays.length; i++) {
		if (Displays [i] != null && Displays [i].thread == thread) {
			SWT.error (SWT.ERROR_THREAD_INVALID_ACCESS);
		}
	}
}
protected void checkSubclass () {
	if (!Display.isValidClass (getClass ())) {
		error (SWT.ERROR_INVALID_SUBCLASS);
	}
}
protected void create (DeviceData data) {
	checkSubclass ();
	checkDisplay (thread = Thread.currentThread ());
	createDisplay (data);
	register (this);
	if (Default == null) Default = this;
}
void createDisplay (DeviceData data) {
	
	/* Initialize X and Xt */
	synchronized (Display.class) {
		if (!fgCarbonInitialized) {
			OS.RegisterAppearanceClient();
			OS.TXNInitTextension();
			OS.InitCursor();
			if (OS.InitContextualMenus() != OS.kNoErr)
				System.out.println("Display.createDisplay: error in OS.InitContextualMenus");
		}
		fgCarbonInitialized = true;
	}
	
	/* Create the XDisplay */
	/* AW
	xDisplay = OS.XtOpenDisplay (xtContext, displayName, appName, appClass, 0, 0, argc, 0);
	*/
	// Mac
	xDisplay= 1;
}
synchronized static void deregister (Display display) {
	for (int i=0; i<Displays.length; i++) {
		if (display == Displays [i]) Displays [i] = null;
	}
}
protected void destroy () {
	if (this == Default) Default = null;
	deregister (this);
	destroyDisplay ();
}
void destroyDisplay () {
	/*
	* Destroy AppContext (this destroys the display)
	*/
	/* AW
	int xtContext = OS.XtDisplayToApplicationContext (xDisplay);
	OS.XtDestroyApplicationContext (xtContext);
	*/
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
void error (int code) {
	SWT.error(code);
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
	Control control = getFocusControl ();
	if (control == null) return null;
	return control.getShell ();
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
	//System.out.println("Display.getBounds");
	return new Rectangle (0, 0, 1152, 768);	// AW FIXME
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
	System.out.println("Display.getCursorControl: nyi");
	
	/* AW
	int [] unused = new int [1], buffer = new int [1];
	int xWindow, xParent = OS.XDefaultRootWindow (xDisplay);
	do {
		if (OS.XQueryPointer (
			xDisplay, xParent, unused, buffer,
			unused, unused, unused, unused, unused) == 0) return null;
		if ((xWindow = buffer [0]) != 0) xParent = xWindow;
	} while (xWindow != 0);
	int handle = OS.XtWindowToWidget (xDisplay, xParent);
	if (handle == 0) return null;
	do {
		Widget widget = WidgetTable.get (handle);
		if (widget != null && widget instanceof Control) {
			Control control = (Control) widget;
			if (control.getEnabled ()) return control;
		}
	} while ((handle = OS.XtParent (handle)) != 0);
	*/
	return null;
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
	MacPoint loc= new MacPoint();
	OS.GetGlobalMouse(loc.getData());
	return new Point (loc.getX(), loc.getY());
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
	return Display.ticksToMS(OS.GetDblTime()); 
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
	/* AW
	int [] buffer1 = new int [1], buffer2 = new int [1];
	OS.XGetInputFocus (xDisplay, buffer1, buffer2);
	int xWindow = buffer1 [0];
	if (xWindow == 0) return null;
	int handle = OS.XtWindowToWidget (xDisplay, xWindow);
	if (handle == 0) return null;
	handle = OS.XmGetFocusWidget (handle);
	*/
	int handle= fFocusControl;
	if (handle == 0) return null;
	do {
		Widget widget = WidgetTable.get (handle);
		if (widget instanceof Control) {
			Control window = (Control) widget;
			if (window.getEnabled ()) return window;
		}
	} while ((handle = MacUtil.getSuperControl (handle)) != 0);
	return null;
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
	System.out.println("getIconDepth: nyi");
	return 32;
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
	Color xColor = null;
	switch (id) {
		case SWT.COLOR_INFO_FOREGROUND: 		return super.getSystemColor (SWT.COLOR_BLACK);
		case SWT.COLOR_INFO_BACKGROUND: 		return COLOR_INFO_BACKGROUND;
		case SWT.COLOR_TITLE_FOREGROUND:		return super.getSystemColor (SWT.COLOR_WHITE);
		case SWT.COLOR_TITLE_BACKGROUND:		return super.getSystemColor (SWT.COLOR_DARK_BLUE);
		case SWT.COLOR_TITLE_BACKGROUND_GRADIENT:	return super.getSystemColor (SWT.COLOR_BLUE);
		case SWT.COLOR_TITLE_INACTIVE_FOREGROUND:	return super.getSystemColor (SWT.COLOR_BLACK);
		case SWT.COLOR_TITLE_INACTIVE_BACKGROUND:	return super.getSystemColor (SWT.COLOR_DARK_GRAY);
		case SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT:	return super.getSystemColor (SWT.COLOR_GRAY);
		case SWT.COLOR_WIDGET_DARK_SHADOW:	xColor = COLOR_WIDGET_DARK_SHADOW; break;
		case SWT.COLOR_WIDGET_NORMAL_SHADOW:	xColor = COLOR_WIDGET_NORMAL_SHADOW; break;
		case SWT.COLOR_WIDGET_LIGHT_SHADOW: 	xColor = COLOR_WIDGET_LIGHT_SHADOW; break;
		case SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW:	xColor = COLOR_WIDGET_HIGHLIGHT_SHADOW; break;
		case SWT.COLOR_WIDGET_BACKGROUND: 	xColor = COLOR_WIDGET_BACKGROUND; break;
		case SWT.COLOR_WIDGET_FOREGROUND:
		case SWT.COLOR_WIDGET_BORDER: 		xColor = COLOR_WIDGET_BORDER; break;
		case SWT.COLOR_LIST_FOREGROUND: 	xColor = COLOR_LIST_FOREGROUND; break;
		case SWT.COLOR_LIST_BACKGROUND: 	xColor = COLOR_LIST_BACKGROUND; break;
		case SWT.COLOR_LIST_SELECTION: 		xColor = COLOR_LIST_SELECTION; break;
		case SWT.COLOR_LIST_SELECTION_TEXT: 	xColor = COLOR_LIST_SELECTION_TEXT; break;
		default:
			return super.getSystemColor (id);	
	}
	if (xColor == null)
		System.out.println("Display.getSystemColor: color null " + id);
	if (xColor == null) return super.getSystemColor (SWT.COLOR_BLACK);
	//return Color.carbon_new (this, xColor);
	return xColor;
	// return getSystemColor(this, id);
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
	return defaultFont;
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
void hideToolTip () {
	if (toolTipWindowHandle != 0) {
		OS.HideWindow(toolTipWindowHandle);
		OS.DisposeWindow(toolTipWindowHandle);
		toolTipWindowHandle = 0;
	}
}
protected void init () {
	super.init ();
	
	/* Create the callbacks */
	fApplicationProc= OS.NewApplicationCallbackUPP(this, "handleApplicationCallback");
	if (fApplicationProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
		
	int[] mask2= new int[] {
		OS.kEventClassCommand, 1,
		
		//OS.kEventClassMenu, OS.kEventMenuBeginTracking,
		//OS.kEventClassMenu, OS.kEventMenuEndTracking,
	
		// we track down events here because we need to know when the user 
		//clicked in the menu bar
		OS.kEventClassMouse, OS.kEventMouseDown,
		// we track up and dragged events because
		// we need to get these events even if the mouse is outside of the window.
		OS.kEventClassMouse, OS.kEventMouseDragged,
		OS.kEventClassMouse, OS.kEventMouseUp,
		
		//OS.kEventClassKeyboard, OS.kEventRawKeyDown,
		//OS.kEventClassKeyboard, OS.kEventRawKeyRepeat,
		
		SWT_USER_EVENT, 54321,
		SWT_USER_EVENT, 54322,
	};
	if (OS.InstallEventHandler(OS.GetApplicationEventTarget(), fApplicationProc, mask2, 0) != OS.kNoErr)
		error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fWindowProc= OS.NewWindowCallbackUPP(this, "handleWindowCallback");
	if (fWindowProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);

	timerProc = OS.NewEventLoopTimerUPP(this, "timerProc");
	if (timerProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);

	caretProc = OS.NewEventLoopTimerUPP2(this, "caretProc");
	if (caretProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fControlActionProc= OS.NewControlActionUPP(this, "handleControlAction");
	if (fControlActionProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fUserPaneDrawProc= OS.NewControlUserPaneDrawUPP(this, "handleUserPaneDraw");
	if (fUserPaneDrawProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fUserPaneHitTestProc= OS.NewUserPaneHitTestUPP(this, "handleUserPaneHitTest");
	if (fUserPaneHitTestProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fDataBrowserDataProc= OS.NewDataBrowserDataCallbackUPP(this, "handleDataBrowserDataCallback");
	if (fDataBrowserDataProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fDataBrowserItemNotificationProc= OS.NewDataBrowserItemNotificationCallbackUPP(this, "handleDataBrowserItemNotificationCallback");
	if (fDataBrowserItemNotificationProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fMenuProc= OS.NewMenuCallbackUPP(this, "handleMenuCallback");
	if (fMenuProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	fControlProc= OS.NewControlCallbackUPP(this, "handleControlCallback");
	if (fControlProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);

	int textInputProc= OS.NewTextCallbackUPP(this, "handleTextCallback");
	if (textInputProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	int[] mask= new int[] {
		// OS.kEventClassTextInput, OS.kEventTextInputUnicodeForKeyEvent,
		
		OS.kEventClassKeyboard, OS.kEventRawKeyDown,
		OS.kEventClassKeyboard, OS.kEventRawKeyRepeat,
		OS.kEventClassKeyboard, OS.kEventRawKeyUp,
	};
	if (OS.InstallEventHandler(OS.GetUserFocusEventTarget(), textInputProc, mask, 0) != OS.kNoErr)
		error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	/*
	fMouseProc= OS.NewMouseMovedCallbackUPP(this, "handleMouseCallback");
	if (fMouseProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	*/
	
	mouseHoverProc = OS.NewEventLoopTimerUPP3(this, "mouseHoverProc");
	if (mouseHoverProc == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);


	buttonFont = Font.carbon_new (this, getThemeFont(OS.kThemeSmallSystemFont));
	buttonShadowThickness= 1;

	//scrolledInsetX = scrolledInsetY = 15;
	scrolledMarginX= scrolledMarginY= 15;
	compositeForeground = 0x000000;
	compositeBackground = 0xEEEEEE;
	
	groupFont = Font.carbon_new (this, getThemeFont(OS.kThemeSmallEmphasizedSystemFont));
	
	dialogForeground= 0x000000;
	dialogBackground= 0xffffff;
	
	labelForeground = 0x000000;
	labelBackground = 0xffffff; 
	labelFont = Font.carbon_new (this, getThemeFont(OS.kThemeSmallSystemFont));
	
	listForeground = 0x000000;
	listBackground = 0xffffff;
	listSelect = listForeground;	// if reversed colors
	listFont= Font.carbon_new (this, new MacFont((short)1)); // Mac Appl Font

	scrollBarForeground = 0x000000;
	scrollBarBackground = 0xffffff;

	textForeground = 0x000000;
	textBackground = 0xffffff;
	textHighlightThickness = 1; // ???
	textFont= Font.carbon_new (this, new MacFont((short)1));	// Mac Appl Font

	COLOR_WIDGET_DARK_SHADOW = new Color (this, 0x33, 0x33, 0x33);	
	COLOR_WIDGET_NORMAL_SHADOW = new Color (this, 0x66, 0x66, 0x66);	
	COLOR_WIDGET_LIGHT_SHADOW = new Color (this, 0x99, 0x99, 0x99);
	COLOR_WIDGET_HIGHLIGHT_SHADOW = new Color (this, 0xCC, 0xCC, 0xCC);	
	COLOR_WIDGET_BACKGROUND = new Color (this, 0xFF, 0xFF, 0xFF);	
	COLOR_WIDGET_BORDER = new Color (this, 0x00, 0x00, 0x00);	
	COLOR_LIST_FOREGROUND = new Color (this, 0x00, 0x00, 0x00);	
	COLOR_LIST_BACKGROUND = new Color (this, 0xFF, 0xFF, 0xFF);	
	COLOR_LIST_SELECTION = new Color (this, 0x66, 0x66, 0xCC);
	COLOR_LIST_SELECTION_TEXT = new Color (this, 0xFF, 0xFF, 0xFF);
	COLOR_INFO_BACKGROUND = new Color (this, 0xFF, 0xFF, 0xE1);
	
	fHoverThemeFont= OS.kThemeSmallSystemFont;

	defaultFont = Font.carbon_new (this, getThemeFont(OS.kThemeSmallSystemFont));
	
	defaultForeground = compositeForeground;
	defaultBackground = compositeBackground;
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
	/* AW
	int xDrawable = OS.XDefaultRootWindow (xDisplay);
	int xGC = OS.XCreateGC (xDisplay, xDrawable, 0, null);
	if (xGC == 0) SWT.error (SWT.ERROR_NO_HANDLES);
	OS.XSetSubwindowMode (xDisplay, xGC, OS.IncludeInferiors);
	if (data != null) {
		data.device = this;
		data.display = xDisplay;
		data.drawable = xDrawable;
		data.fontList = defaultFont;
		data.colormap = OS.XDefaultColormap (xDisplay, OS.XDefaultScreen (xDisplay));
	}
	return xGC;
	*/

	if (data != null) {
		data.device = this;
		/* AW
		data.display = xDisplay;
		data.drawable = xWindow;
		data.foreground = argList [1];
		data.background = argList [3];
		data.fontList = fontList;
		data.colormap = argList [5];
		*/
		data.foreground = 0x000000;
		data.background = 0xffffff;
		data.font = new MacFont((short)1);
		data.controlHandle = 0;
	}

	int wHandle= OS.FrontWindow();
	int xGC= OS.GetWindowPort(wHandle);
	if (xGC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	
    return xGC;
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
public void internal_dispose_GC (int gc, GCData data) {
	/* AW
	OS.XFreeGC(xDisplay, gc);
	*/
}
boolean isValidThread () {
	return thread == Thread.currentThread ();
}
static boolean isValidClass (Class clazz) {
	String name = clazz.getName ();
	int index = name.lastIndexOf ('.');
	return name.substring (0, index + 1).equals (PACKAGE_PREFIX);
}
int mouseHoverProc (int handle, int id) {
	if (mouseHoverID != 0) OS.RemoveEventLoopTimer(mouseHoverID);
	mouseHoverID = mouseHoverHandle = 0;
	Widget widget = WidgetTable.get (handle);
	if (widget == null) return 0;
	int rc= widget.processMouseHover(new Integer(id));
	sendUserEvent(54321);
	return rc;
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
	int[] evt= new int[1];
	int rc= OS.ReceiveNextEvent(null, OS.kEventDurationNoWait, true, evt);
	
	switch (rc) {
	case OS.kNoErr:
		int target= OS.GetEventDispatcherTarget();
		
		/*
		System.out.println("readAndDispatch: " + MacUtil.toString(eventClass));
		*/
		int event= evt[0];
		
		int eventClass= OS.GetEventClass(event);
		if (eventClass == OS.kEventClassMouse) {
			switch (OS.GetEventKind(event)) {
			case OS.kEventMouseDown:
			case OS.kEventMouseDragged:
				switch (MacEvent.getEventMouseButton(event)) {
				case 1:
					fMouseButtonState |= SWT.BUTTON1;
					break;
				case 2:
					fMouseButtonState |= SWT.BUTTON2;
					break;
				case 3:
					fMouseButtonState |= SWT.BUTTON3;
					break;
				}
				break;
			case OS.kEventMouseMoved:
				fMouseButtonState= 0;
				break;
			case OS.kEventMouseUp:
				switch (MacEvent.getEventMouseButton(event)) {
				case 1:
					fMouseButtonState &= ~SWT.BUTTON1;
					break;
				case 2:
					fMouseButtonState &= ~SWT.BUTTON2;
					break;
				case 3:
					fMouseButtonState &= ~SWT.BUTTON3;
					break;
				}
				break;
			}
		}	
		
		OS.SendEventToEventTarget(event, target);
		OS.ReleaseEvent(event);
		repairPending();
		runDeferredEvents ();
		return true;
		
	case OS.eventLoopTimedOutErr:
		break;	// no event: run async
		
	default:
		System.out.println("readAndDispatch: error " + rc);
		break;
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
protected void release () {
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

	/* Destroy the hidden Override shell parent */
	/* AW
	if (shellHandle != 0) OS.XtDestroyWidget (shellHandle);
	shellHandle = 0;
	*/
	
	/* Dispose the caret callback */
	/* AW
	if (caretID != 0) OS.XtRemoveTimeOut (caretID);
	*/
	if (caretID != 0) OS.RemoveEventLoopTimer(caretID);
	caretID = caretProc = 0;
	/* AW
	caretCallback.dispose ();
	caretCallback = null;
	*/
	
	/* Dispose the timer callback */
	if (timerIDs != null) {
		for (int i=0; i<timerIDs.length; i++) {
			/* AW
			if (timerIDs [i] != 0) OS.XtRemoveTimeOut (timerIDs [i]);
			*/
			if (timerIDs [i] != 0) OS.RemoveEventLoopTimer (timerIDs [i]);
		}
	}
	timerIDs = null;
	timerList = null;
	timerProc = 0;

	/* Dispose the mouse hover callback */
	if (mouseHoverID != 0) OS.RemoveEventLoopTimer(mouseHoverID);
	mouseHoverID = mouseHoverProc = mouseHoverHandle = toolTipWindowHandle = 0;

	/* Free the font lists */
	/* AW
	if (buttonFont != 0) OS.XmFontListFree (buttonFont);
	if (labelFont != 0) OS.XmFontListFree (labelFont);
	if (textFont != 0) OS.XmFontListFree (textFont);
	if (listFont != 0) OS.XmFontListFree (listFont);
	listFont = textFont = labelFont = buttonFont = 0;
	*/
	defaultFont = null;	
	
	/* Release references */
	thread = null;
	buttonBackground = buttonForeground = 0;
	defaultBackground = defaultForeground = 0;
	COLOR_WIDGET_DARK_SHADOW = COLOR_WIDGET_NORMAL_SHADOW = COLOR_WIDGET_LIGHT_SHADOW =
	COLOR_WIDGET_HIGHLIGHT_SHADOW = COLOR_WIDGET_BACKGROUND = COLOR_WIDGET_BORDER =
	COLOR_LIST_FOREGROUND = COLOR_LIST_BACKGROUND = COLOR_LIST_SELECTION = COLOR_LIST_SELECTION_TEXT = null;
	COLOR_INFO_BACKGROUND = null;
}
void releaseToolTipHandle (int handle) {
	if (mouseHoverHandle == handle) removeMouseHoverTimeOut ();
	if (toolTipWindowHandle != 0) {
		/* AW
		int shellParent = OS.XtParent(toolTipWindowHandle);
		if (handle == shellParent) toolTipWindowHandle = 0;
		*/
	}
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

void removeMouseHoverTimeOut () {
	if (mouseHoverID != 0) OS.RemoveEventLoopTimer(mouseHoverID);
	mouseHoverID = mouseHoverHandle = 0;
}
boolean runAsyncMessages () {
	return synchronizer.runAsyncMessages ();
}
boolean runDeferredEvents () {
	/*
	* Run deferred events.  This code is always
	* called  in the Display's thread so it must
	* be re-enterant need not be synchronized.
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
				widget.notifyListeners (event.type, event);
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
void sendEvent (int eventType, Event event) {
	if (eventTable == null) return;
	if (event == null) event = new Event ();
	event.display = this;
	event.type = eventType;
	if (event.time == 0) {
		/* AW
		if (OS.IsWinCE) {
			event.time = OS.GetTickCount ();
		} else {
			event.time = OS.GetMessageTime ();
		}
		*/
	}
	eventTable.sendEvent (event);
}
/**
 * On platforms which support it, sets the application name
 * to be the argument. On Motif, for example, this can be used
 * to set the name used for resource lookup.
 *
 * @param name the new app name
 */
public static void setAppName (String name) {
	APP_NAME = name;
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
	int x = point.x;
	int y = point.y;
	/* AW
	int xWindow = OS.XDefaultRootWindow (xDisplay);	
	OS.XWarpPointer (xDisplay, OS.None, xWindow, 0, 0, 0, 0, x, y);
	*/
	System.out.println("Display.setCursorLocation: nyi");
}

void setCurrentCaret (Caret caret) {
	if (caretID != 0) OS.RemoveEventLoopTimer(caretID);
	caretID = 0;
	currentCaret = caret;
	if (currentCaret != null) {
		int blinkRate = currentCaret.blinkRate;
		int[] timer= new int[1];
		OS.InstallEventLoopTimer(OS.GetCurrentEventLoop(), blinkRate / 1000.0, 0.0, caretProc, 0, timer);
		caretID = timer[0];
	}
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
void setToolTipText (int handle, String toolTipText) {
/* AW
	if (toolTipHandle == 0) return;
	int shellHandle = OS.XtParent (toolTipHandle);
	int shellParent = OS.XtParent (shellHandle);
	if (handle != shellParent) return;
*/
	showToolTip (handle, toolTipText);
}
void showToolTip (int handle, String toolTipText) {

	if (toolTipText == null || toolTipText.length () == 0) {
		if (toolTipWindowHandle != 0)
			OS.HideWindow(toolTipWindowHandle);
		return;
	}

	if (toolTipWindowHandle != 0)
		 return;
	
	if (handle != fCurrentControl) {
		//System.out.println("Display.showToolTip: handle is not current");
		//beep();
		return;
	}
	if (fMenuIsVisible) {
		//System.out.println("Display.showToolTip: menu is visible");
		//beep();
		return;
	}	
	if (OS.StillDown()) {
		//System.out.println("Display.showToolTip: button is down");
		//beep();
		return;
	}	
	
	toolTipText= MacUtil.removeMnemonics(toolTipText);
	
	// remember text
	fToolTipText= toolTipText;
	
	// calculate text bounding box
	short[] bounds= new short[2];
	short[] baseLine= new short[1];
	int sHandle= OS.CFStringCreateWithCharacters(toolTipText);
	OS.GetThemeTextDimensions(sHandle, fHoverThemeFont, OS.kThemeStateActive, false, bounds, baseLine);
	if (bounds[1] > 200) {	// too wide -> wrap text
		bounds[1]= (short) 200;
		OS.GetThemeTextDimensions(sHandle, fHoverThemeFont, OS.kThemeStateActive, true, bounds, baseLine);
	}
	OS.CFRelease(sHandle);
	int width= bounds[1] + 2*TOOLTIP_MARGIN;
	int height= bounds[0] + 2*TOOLTIP_MARGIN;
	
	// position just below mouse cursor
	MacPoint loc= new MacPoint();
	OS.GetGlobalMouse(loc.getData());
	int x= loc.getX() + 16;
	int y= loc.getY() + 16;

	// Ensure that the tool tip is on the screen.
	MacRect screenBounds= new MacRect();
	OS.GetAvailableWindowPositioningBounds(OS.GetMainDevice(), screenBounds.getData());
	x = Math.max (0, Math.min (x, screenBounds.getWidth() - width ));
	y = Math.max (0, Math.min (y, screenBounds.getHeight() - height ));

	// create window
	int[] wHandle= new int[1];
	if (OS.CreateNewWindow(OS.kHelpWindowClass, 0, new MacRect(x, y, width, height).getData(), wHandle) == OS.kNoErr) {
		toolTipWindowHandle= wHandle[0];
		int[] mask= new int[] {
			OS.kEventClassWindow, OS.kEventWindowDrawContent
		};
		OS.InstallEventHandler(OS.GetWindowEventTarget(toolTipWindowHandle), fWindowProc, mask, toolTipWindowHandle);
		OS.ShowWindow(toolTipWindowHandle);
		fLastHoverHandle= handle;
	}
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
	return OS.ReceiveNextEvent(null, OS.kEventDurationForever, false, null) == OS.kNoErr;
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
int textWidth2 (String string, GC gc) {
	if (string.length () == 0) return 0;
	/* AW
	String codePage = Converter.getCodePage (xDisplay, fontList);
	byte [] textBuffer = Converter.wcsToMbcs (codePage, string, true);
	int xmString = OS.XmStringGenerate (textBuffer, null, OS.XmCHARSET_TEXT, _MOTIF_DEFAULT_LOCALE);
	int width = OS.XmStringWidth (fontList, xmString);
	OS.XmStringFree (xmString);
	return width;
	*/
	return gc.stringExtent(string).x;
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
	if (timerList == null) timerList = new Runnable [4];
	if (timerIDs == null) timerIDs = new int [4];
	int index = 0;
	while (index < timerList.length) {
		if (timerList [index] == null) break;
		index++;
	}
	if (index == timerList.length) {
		Runnable [] newTimerList = new Runnable [timerList.length + 4];
		System.arraycopy (timerList, 0, newTimerList, 0, timerList.length);
		timerList = newTimerList;
		int [] newTimerIDs = new int [timerIDs.length + 4];
		System.arraycopy (timerIDs, 0, newTimerIDs, 0, timerIDs.length);
		timerIDs = newTimerIDs;
	}
	int[] timer= new int[1];
	OS.InstallEventLoopTimer(OS.GetCurrentEventLoop(), milliseconds / 1000.0, 0.0, timerProc, index, timer);
	int timerID = timer[0];
	
	if (timerID != 0) {
		timerIDs [index] = timerID;
		timerList [index] = runnable;
	}
}
int timerProc (int index, int id) {
	if (id != 0)
		OS.RemoveEventLoopTimer(id);
	if (timerList == null) return 0;
	if (0 <= index && index < timerList.length) {
		Runnable runnable = timerList [index];
		timerList [index] = null;
		timerIDs [index] = 0;
		if (runnable != null) runnable.run ();
	}
	return 0;
}
static int translateKey (int key) {
	for (int i=0; i<KeyTable.length; i++) {
		if (KeyTable [i] [0] == key) return KeyTable [i] [1];
	}
	return 0;
}
static int untranslateKey (int key) {
	for (int i=0; i<KeyTable.length; i++) {
		if (KeyTable [i] [1] == key) return KeyTable [i] [0];
	}
	return 0;
}

static short keyGlyph(int key) {
	switch (key) {
	case ' ':
		return OS.kMenuBlankGlyph;
	case '\t':
		return OS.kMenuTabRightGlyph;
	case SWT.SHIFT:
		return OS.kMenuShiftGlyph;
	case SWT.CONTROL:
		return OS.kMenuControlISOGlyph;
		//return OS.kMenuControlGlyph;
	case SWT.ALT:
		return OS.kMenuOptionGlyph;
	case SWT.ARROW_UP:
		//return OS.kMenuUpArrowDashedGlyph;
		return OS.kMenuUpArrowGlyph;
	case SWT.ARROW_DOWN:
		//return OS.kMenuDownwardArrowDashedGlyph;
		return OS.kMenuDownArrowGlyph;
	case SWT.ARROW_LEFT:
		//return OS.kMenuLeftArrowDashedGlyph;
		return OS.kMenuLeftArrowGlyph;
	case SWT.ARROW_RIGHT:
		//return OS.kMenuRightArrowDashedGlyph;
		return OS.kMenuRightArrowGlyph;
	case SWT.PAGE_UP:
		return OS.kMenuPageUpGlyph;
	case SWT.PAGE_DOWN:
		return OS.kMenuPageDownGlyph;
	case SWT.ESC:
		return OS.kMenuEscapeGlyph;
	case SWT.CR:
		//return OS.kMenuEnterGlyph;
		//return OS.kMenuNonmarkingReturnGlyph;
		//return OS.kMenuReturnR2LGlyph;
		//return OS.kMenuNonmarkingReturnGlyph;
		return OS.kMenuReturnGlyph;
	case SWT.BS:
		return OS.kMenuDeleteLeftGlyph;
	case SWT.DEL:
		return OS.kMenuDeleteRightGlyph;
	case SWT.F1:
		return OS.kMenuF1Glyph;
	case SWT.F2:
		return OS.kMenuF2Glyph;
	case SWT.F3:
		return OS.kMenuF3Glyph;
	case SWT.F4:
		return OS.kMenuF4Glyph;
	case SWT.F5:
		return OS.kMenuF5Glyph;
	case SWT.F6:
		return OS.kMenuF6Glyph;
	case SWT.F7:
		return OS.kMenuF7Glyph;
	case SWT.F8:
		return OS.kMenuF8Glyph;
	case SWT.F9:
		return OS.kMenuF9Glyph;
	case SWT.F10:
		return OS.kMenuF10Glyph;
	case SWT.F11:
		return OS.kMenuF11Glyph;
	case SWT.F12:
		return OS.kMenuF12Glyph;
	default:
		/*
		public static final short kMenuPencilGlyph = 15;
		public static final short kMenuCommandGlyph = 17;
		public static final short kMenuCheckmarkGlyph = 18;
		public static final short kMenuDiamondGlyph = 19;
		public static final short kMenuClearGlyph = 28;
		public static final short kMenuCapsLockGlyph = 99;
		public static final short kMenuHelpGlyph = 103;
		public static final short kMenuContextualMenuGlyph = 109;
		public static final short kMenuPowerGlyph = 110;
		*/
		return OS.kMenuNullGlyph;
	}
}

/**
 * Forces all outstanding paint requests for the display
 * to be processed before this method returns.
 *
 * @see Control#update
 */
public void update () {
	checkDevice ();
	/* AW
	XAnyEvent event = new XAnyEvent ();
	int mask = OS.ExposureMask | OS.ResizeRedirectMask |
		OS.StructureNotifyMask | OS.SubstructureNotifyMask |
		OS.SubstructureRedirectMask;
	OS.XSync (xDisplay, false); OS.XSync (xDisplay, false);
	while (OS.XCheckMaskEvent (xDisplay, mask, event)) OS.XtDispatchEvent (event);
	*/
	/*
	MacEvent event= new MacEvent();
	while (OS.GetNextEvent(OS.updateMask, event.getData()))
		dispatchEvent(event);
	*/
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
	/* Send a user event to wake up in ReceiveNextEvent */
	sendUserEvent(54322);
}
public int windowProc (int handle, int clientData, Object callData) {
	Widget widget = WidgetTable.get (handle);
	if (widget == null) return 0;
	return widget.processEvent (clientData, callData);
}
static String convertToLf(String text) {
	char Cr = '\r';
	char Lf = '\n';
	int length = text.length ();
	if (length == 0) return text;
	
	/* Check for an LF or CR/LF.  Assume the rest of the string 
	 * is formated that way.  This will not work if the string 
	 * contains mixed delimiters. */
	int i = text.indexOf (Lf, 0);
	if (i == -1 || i == 0) return text;
	if (text.charAt (i - 1) != Cr) return text;

	/* The string is formatted with CR/LF.
	 * Create a new string with the LF line delimiter. */
	i = 0;
	StringBuffer result = new StringBuffer ();
	while (i < length) {
		int j = text.indexOf (Cr, i);
		if (j == -1) j = length;
		String s = text.substring (i, j);
		result.append (s);
		i = j + 2;
		result.append (Lf);
	}
	return result.toString ();
}

////////////////////////////////////////////////////////////////////////////
// Some Mac helper functions
////////////////////////////////////////////////////////////////////////////

	//---- callbacks
	
	private void handleControlAction(int cHandle, short partCode) {
		// System.out.println("handleControlAction: " + WidgetTable.get (cHandle) + " " + partCode);
		windowProc(cHandle, SWT.Selection, new MacControlEvent(cHandle, partCode, true));
		repairPending();
	}

	private void handleUserPaneDraw(int cHandle, short partCode) {
		if (fUpdateRegion == 0) {
			if (false) {
				int updateRegion= OS.NewRgn();
				int wHandle= OS.GetControlOwner(cHandle);
				int portHandle= OS.GetWindowPort(wHandle);
				OS.QDGetDirtyRegion(portHandle, updateRegion);		
				windowProc(cHandle, SWT.Paint, new MacControlEvent(cHandle, updateRegion));
				OS.DisposeRgn(updateRegion);
			} else {	
				windowProc(cHandle, SWT.Paint, new MacControlEvent(cHandle, fUpdateRegion));
			}
		} else {
			windowProc(cHandle, SWT.Paint, new MacControlEvent(cHandle, fUpdateRegion));
		}
	}
		
	private int handleUserPaneHitTest(int cHandle, int x, int y) {
		Widget w= WidgetTable.get(cHandle);
		//System.out.println("handleControlHitTest: " + w);
		if (w instanceof Text || w instanceof Combo)
			return 112;
		return 111;
	}
		
	private int handleDataBrowserDataCallback(int cHandle, int colId, int rowID, int itemData) {
		Widget widget= WidgetTable.get(cHandle);
		if (widget instanceof List) {
			List list= (List) widget;
			list.handleItemCallback(cHandle, colId, rowID, itemData);
		}
		return OS.kNoErr;
	}
	
	private int handleDataBrowserItemNotificationCallback(int cHandle, int item, int message) {
		//System.out.println("Display.handleDataBrowserItemNotificationCallback: " + message);
		return OS.kNoErr;
	}
	
	private int handleMenuCallback(int eHandle, int mHandle) {
		switch (OS.GetEventKind(eHandle)) {
		case OS.kEventMenuPopulate:
		case OS.kEventMenuOpening:
		
			if (fInContextMenu)
				OS.SetMenuFont(mHandle, (short)1024, (short)11);	// AW: FIXME menu id
			/*
			// copy the menu's font
			short[] fontID= new short[1];
			short[] size= new short[1];
			OS.GetMenuFont(hMenu, fontID, size);
			OS.SetMenuFont(menu.handle, fontID[0], size[0]);
			*/ 
		
			windowProc(mHandle, SWT.Show, null);
			break;
		case OS.kEventMenuClosed:
			windowProc(mHandle, SWT.Hide, null);
			break;
		}
		return OS.kNoErr;
	}
	
	private int handleControlCallback(int eHandle, int cHandle) {
		Widget w= findWidget(cHandle);
		if (w instanceof Scrollable)
			((Scrollable)w).handleResizeScrollView(cHandle);
		return OS.kNoErr;
	}
	
	//private String fUnicodeString;
	
	private int handleTextCallback(int nextHandler, int eRefHandle) {
		
		int eventClass= OS.GetEventClass(eRefHandle);
		int eventKind= OS.GetEventKind(eRefHandle);
		
		switch (eventClass) {
		case OS.kEventClassTextInput:
			switch (eventKind) {
			case OS.kEventTextInputUnicodeForKeyEvent:
				return OS.eventNotHandledErr;
			default:
				System.out.println("Display.handleTextCallback: kEventClassTextInput: unexpected event kind");
				break;
			}
			break;
		case OS.kEventClassKeyboard:
			Control focus= getFocusControl();	
			switch (eventKind) {
			case OS.kEventRawKeyDown:
			case OS.kEventRawKeyRepeat:
				if (focus != null)
					return focus.sendKeyEvent(SWT.KeyDown, nextHandler, eRefHandle);
				break;
			case OS.kEventRawKeyUp:
				if (focus != null)
					return focus.sendKeyEvent(SWT.KeyUp, nextHandler, eRefHandle);
				break;
			default:
				System.out.println("Display.handleTextCallback: kEventClassKeyboard: unexpected event kind");
				break;
			}
			break;
		default:
			System.out.println("Display.handleTextCallback: unexpected event class");
			break;
		}
		return OS.eventNotHandledErr;
	}
	
	private int handleWindowCallback(int nextHandler, int eRefHandle, int whichWindow) {
		//whichWindow= getDirectObject(eRefHandle);
		int eventClass= OS.GetEventClass(eRefHandle);
		int eventKind= OS.GetEventKind(eRefHandle);
		
		switch (eventClass) {
			
		case OS.kEventClassMouse:
			return handleMouseCallback(nextHandler, eRefHandle, whichWindow);
			
		case OS.kEventClassWindow:
			switch (eventKind) {
			case OS.kEventWindowActivated:
				windowProc(whichWindow, SWT.FocusIn, new Boolean(true));
				return OS.kNoErr;
			case OS.kEventWindowDeactivated:
				//System.out.println("-kEventWindowDeactivated");
				windowProc(whichWindow, SWT.FocusIn, new Boolean(false));
				return OS.kNoErr;
			case OS.kEventWindowBoundsChanged:
				int[] attr= new int[1];
				OS.GetEventParameter(eRefHandle, OS.kEventParamAttributes, OS.typeUInt32, null, null, attr);	
				windowProc(whichWindow, SWT.Resize, new Integer(attr[0]));
				return OS.kNoErr;
			case OS.kEventWindowClose:
				windowProc(whichWindow, SWT.Dispose, null);
				return OS.kNoErr;
			case OS.kEventWindowDrawContent:
				if (toolTipWindowHandle == whichWindow) {
					processPaintToolTip(whichWindow);
				} else {
					updateWindow2(whichWindow);
				}
				return OS.kNoErr;
			default:
				System.out.println("handleWindowCallback: kEventClassWindow kind:" + eventKind);
				break;
			}
			break;
			
		default:
			System.out.println("handleWindowCallback: unexpected event class: " + MacUtil.toString(eventClass));
			break;
		}
		return OS.eventNotHandledErr;
	}

	private int handleApplicationCallback(int nextHandler, int eRefHandle, int userData) {
	
		MacEvent mEvent= new MacEvent(eRefHandle);
		
		int eventClass= OS.GetEventClass(eRefHandle);
		int eventKind= OS.GetEventKind(eRefHandle);
		
		switch (eventClass) {
			
		case OS.kEventClassCommand:
		
			if (eventKind == 1) {
				int[] rc= new int[4];
				OS.GetEventHICommand(eRefHandle, rc);
				
				//System.out.println("kEventClassCommand: " + rc[3]);
						
				// try to map the MenuRef to a SWT Menu
				Widget w= findWidget (rc[2]);
				if (w instanceof Menu) {
					Menu menu= (Menu) w;
					menu.handleMenu(rc[3]);
					OS.HiliteMenu((short)0);	// unhighlight what MenuSelect (or MenuKey) hilited
					return OS.kNoErr;
				}
				OS.HiliteMenu((short)0);	// unhighlight what MenuSelect (or MenuKey) hilited
				// we do not return kNoErr here so that the default handler
				// takes care of special menus like the Combo menu.
			}
			break;
				
		case OS.kEventClassMenu:
			switch (eventKind) {
			case OS.kEventMenuBeginTracking:
				//System.out.println("kEventMenuBeginTracking");
				break;
			case OS.kEventMenuEndTracking:
				//System.out.println("kEventMenuEndTracking");
				break;
			}
			break;
		
		case OS.kEventClassKeyboard:
			System.out.println("  handleApplicationCallback: kEventClassKeyboard");	
			switch (eventKind) {
			case OS.kEventRawKeyDown:
			case OS.kEventRawKeyRepeat:
				System.out.println("    kEventRawKeyDown | kEventRawKeyRepeat");
				int cmd= OS.MenuEvent(mEvent.getData());
				if (OS.HiWord(cmd) != 0) {
					System.out.println("    doMenuCommand: " + cmd);
					//doMenuCommand(cmd);
					return OS.kNoErr;
				}
				break;
									
			case OS.kEventHotKeyPressed:
				System.out.println("    kEventHotKeyPressed");
				break;
			}
			System.out.println("    end handleApplicationCallback: kEventClassKeyboard");	
			break;
			
		case OS.kEventClassMouse:
			switch (eventKind) {
				
			case OS.kEventMouseDown:
			
				fTrackedControl= 0;
				
				hideToolTip();
	
				MacPoint where= mEvent.getWhere();
				int[] w= new int[1];
				short part= OS.FindWindow(where.getData(), w);
								
				int oldPort= OS.GetPort();
				OS.SetPortWindowPort(w[0]);
				OS.GlobalToLocal(where.getData());
				OS.SetPort(oldPort);
				
				if (part == OS.inMenuBar) {
					int id= OS.MenuSelect(mEvent.getWhere().getData());
					//doMenuCommand(OS.MenuSelect(mEvent.getWhere().getData()));
					return OS.kNoErr;
				}
				break;
				
			case OS.kEventMouseDragged:
				return handleMouseCallback(nextHandler, eRefHandle, 0);
				
			case OS.kEventMouseUp:
				return handleMouseCallback(nextHandler, eRefHandle, 0);
			}
			return OS.eventNotHandledErr;
						
		case SWT_USER_EVENT:	// SWT1 user event
			//System.out.println("handleApplicationCallback: user event " + eventKind);
			return OS.kNoErr;
			
		default:
			System.out.println("handleApplicationCallback: unknown event class" + MacUtil.toString(eventClass));
			break;
		}
		return OS.eventNotHandledErr;
	}
		
	private int handleMouseCallback(int nextHandler, int eRefHandle, int whichWindow) {
		
		int eventKind= OS.GetEventKind(eRefHandle);
		
		if (eventKind == OS.kEventMouseDown) {
			//System.out.println("  handleMouseCallback: kEventMouseDown " + whichWindow);	
			//System.out.println("     frontw " + OS.FrontWindow());
			fTrackedControl= 0;
		}
		
		MacEvent me= new MacEvent(eRefHandle);
		
		short part= 0;
		MacPoint where= me.getWhere();
		
		if (whichWindow == 0) {
			if (fTrackedControl != 0) {
				whichWindow= OS.GetControlOwner(fTrackedControl);
			} else {
				int[] w= new int[1];
				part= OS.FindWindow(where.getData(), w);
				whichWindow= w[0];
				//part= getWindowDefPart(eRefHandle);
				//whichWindow= getDirectObject(eRefHandle);
			}
		} else {
			part= OS.FindWindow(where.getData(), new int[1]);
		}
		
		if (whichWindow == 0) {
			//System.out.println("Display.handleMouseCallback:  whichWindow == 0");
			return OS.eventNotHandledErr;
		}
			
		int oldPort= OS.GetPort();
		OS.SetPortWindowPort(whichWindow);
		OS.GlobalToLocal(where.getData());
		OS.SetPort(oldPort);
		
		switch (eventKind) {
		
		case OS.kEventMouseDown:
					
			if (!OS.IsWindowActive(whichWindow)) {
				// let the default handler activate the window
				// (I had no success when calling SelectWindow)
				return OS.eventNotHandledErr;
			}
		
			hideToolTip ();
		
			fTrackedControl= 0;
					
			if (part == OS.inContent)
				if (!handleContentClick(me, whichWindow))
					return OS.kNoErr;

			break;
		
		case OS.kEventMouseDragged:
			if (fTrackedControl != 0) {
				me.getData()[0]= 12345;
				windowProc(fTrackedControl, SWT.MouseMove, me);
				return OS.kNoErr;
			}
			break;

		case OS.kEventMouseUp:
			if (fTrackedControl != 0) {
				windowProc(fTrackedControl, SWT.MouseUp, me);
				fTrackedControl= 0;
				return OS.kNoErr;
			}	
			break;
			
		case OS.kEventMouseMoved:
		
			short[] cpart= new short[1];
			int whichControl= MacUtil.findControlUnderMouse(where, whichWindow, cpart);
		
			if (fCurrentControl != whichControl) {
			
				if (fCurrentControl != 0) {
					fLastHoverHandle= 0;
					windowProc(fCurrentControl, SWT.MouseExit, me);
				}
				
				fCurrentControl= whichControl;
				
				if (fCurrentControl != 0) {
					windowProc(fCurrentControl, SWT.MouseEnter, me);
				}
				return OS.kNoErr;			
			} else {
				if (fCurrentControl != 0) {
					windowProc(fCurrentControl, SWT.MouseMove, me);
					return OS.kNoErr;
				}
			}
			break;
		}
					
		return OS.eventNotHandledErr;
	}

	boolean setMacFocusHandle(int wHandle, int focusHandle) {
	
		/*
		int[] focusControl= new int[1];
		OS.GetKeyboardFocus(wHandle, focusControl);
		if (focusControl[0] == fFocusControl)
			return false;
			
		int rc= OS.SetKeyboardFocus(wHandle, focusHandle, (short)-1);
		if (rc != OS.kNoErr) {
			System.out.println("Display.setMacFocusHandle: SetKeyboardFocus " + rc);
			return false;
		}
		*/
		
		if (fFocusControl != focusHandle) {
			int oldFocus= fFocusControl;
			fFocusControl= focusHandle;
			
			if (oldFocus != 0)
				windowProc(oldFocus, SWT.FocusIn, new Boolean(false));
			
			//fFocusControl= focusHandle;
			
			int[] focusControl= new int[1];
			OS.GetKeyboardFocus(wHandle, focusControl);
			if (focusControl[0] != fFocusControl) {
				int rc= OS.SetKeyboardFocus(wHandle, focusHandle, (short)-1);
				//if (rc != OS.kNoErr)
				//	System.out.println("Display.setMacFocusHandle: SetKeyboardFocus " + rc);
			}

			if (fFocusControl != 0)
				windowProc(fFocusControl, SWT.FocusIn, new Boolean(true));
		}
		return true;
	}
			
	private boolean handleContentClick(MacEvent me, int whichWindow) {
	
		MacPoint where= me.getWhere();
		MacPoint globalPos= me.getWhere();
		
		/*
		hideToolTip ();
		
		if (whichWindow != OS.FrontNonFloatingWindow()) {
			System.out.println("  front click");
			//OS.SelectWindow(whichWindow);
			return true;
		}
		*/
		
		int savedPort= OS.GetPort();
		OS.SetPortWindowPort(whichWindow);
		OS.GlobalToLocal(where.getData());
		OS.SetPort(savedPort);
		
		short[] cpart= new short[1];
		//int whichControl= OS.FindControlUnderMouse(where.getData(), whichWindow, cpart);
		int whichControl= MacUtil.findControlUnderMouse(where, whichWindow, cpart);				
		
		Widget w= findWidget(whichControl);
		//System.out.println("  front click: " + w);
		
		// activate control
		/* AW already done in Control.processMouseDown
		Widget w1= findWidget(whichControl);
		Widget w2= findWidget(whichWindow);
		if (w1 instanceof Control && w2 instanceof Shell) {
			Shell shell= (Shell) w2;
			shell.setActiveControl((Control) w1);
		}
		*/
		
		// focus change
		setMacFocusHandle(whichWindow, whichControl);
								
		if (whichControl != 0) {
		
			// process the context menu
			Widget wc= WidgetTable.get(whichControl);
			if (wc instanceof Control) {
				Menu cm= ((Control)wc).getMenu();	// is a context menu installed?
				if (cm != null && OS.IsShowContextualMenuClick(me.getData())) {
					handleContextClick(cm, globalPos, me);
					return false;
				}
			}
		
			switch (cpart[0]) {
			case 0:
				break;

			case 111:	// User pane
				fTrackedControl= whichControl;	// starts mouse tracking
				windowProc(whichControl, SWT.MouseDown, me);
				break;

			case 112:	// User pane
				windowProc(whichControl, SWT.MouseDown, me);
				break;
				
			default:
				windowProc(whichControl, SWT.MouseDown, me);
				int cpart2= OS.HandleControlClick(whichControl, where.getData(), me.getModifiers(), -1);
				if (cpart2 != 0) {
					windowProc(whichControl, SWT.Selection, new MacControlEvent(whichControl, cpart2, false));
					repairPending();
				}
				break;
			}
		}
		return false;
	}

	/*
	private int handleKeyEvent2(int type, MacEvent me) {
		System.out.println("  handleKeyEvent: " + me.getKeyCode());	
		int[] focusControl= new int[1];
		int status= OS.GetKeyboardFocus(OS.FrontWindow(), focusControl);
		//System.out.println("--------> focus(" + status + ") " + focusControl[0]);
		if (status == OS.kNoErr && focusControl[0] != 0) {
			windowProc(focusControl[0], type, me);
			//return OS.kNoErr;
		} else if (fFocusControl != 0) {
			windowProc(fFocusControl, type, me);
			//return OS.kNoErr;
		}
		return OS.eventNotHandledErr;
	}
	*/

	private void handleContextClick(Menu cm, MacPoint globalPos, MacEvent me) {
		
		short menuId;
		short index;
		
		try {
			fInContextMenu= true;
			if (true) {
				int result= OS.PopUpMenuSelect(cm.handle, (short)globalPos.getY(), (short)globalPos.getX(), (short)1);
				if (result == OS.kNoErr)
					return;
				menuId= OS.HiWord(result);
				index= OS.LoWord(result);
			} else {
				// AW: not ready for primetime
				short[] id= new short[1];
				short[] ix= new short[1];
				if (OS.ContextualMenuSelect(cm.handle, globalPos.getData(), id, ix) != OS.kNoErr)
					return;
				menuId= id[0];
				index= ix[0];
			}
		} finally {
			fInContextMenu= false;
		}

		if (menuId != 0) {
			Menu menu= cm.getShell().findMenu(menuId);
			if (menu != null) {
				//System.out.println("handleMenu: " + index);
				menu.handleMenu(index);
			}
		}
	}
	
	public void updateWindow(int whichWindow) {
	
		int curPort= OS.GetPort();
		OS.SetPortWindowPort(whichWindow);
		OS.BeginUpdate(whichWindow);
		
		updateWindow2(whichWindow);

		OS.EndUpdate(whichWindow);
		OS.SetPort(curPort);
	}

	public void updateWindow2(int whichWindow) {
		if (toolTipWindowHandle == whichWindow) {
			processPaintToolTip(whichWindow);
		} else {
			fUpdateRegion= OS.NewRgn();
			OS.GetPortVisibleRegion(OS.GetPort(), fUpdateRegion);

			OS.EraseRgn(fUpdateRegion);
			OS.UpdateControls(whichWindow, fUpdateRegion);
				
			//OS.DrawGrowIcon(whichWindow);
			
			OS.DisposeRgn(fUpdateRegion);
			fUpdateRegion= 0;
		}
	}

	private int fRepairCount= 0;
	private int[] fRepairWindow= new int[40];
	private int[] fRepairRegion= new int[40];
	
	private void repairPending() {
		while (fRepairCount > 0) {
			fRepairCount--;
			repairRgn(fRepairWindow[fRepairCount], fRepairRegion[fRepairCount]);
		}
	}
	
	private void repairRgn(int wHandle, int rgn) {
		OS.InvalWindowRgn(wHandle, rgn);
		
		fUpdateRegion= rgn;
		OS.BeginUpdate(wHandle);
		OS.UpdateControls(wHandle, rgn);
		OS.EndUpdate(wHandle);

		OS.DisposeRgn(rgn);
		fUpdateRegion= 0;
	}
	
	public void repairWindow(int wHandle, int rgn, short dx, short dy) {
		if (SMART_REFRESH && fRepairCount < fRepairWindow.length) {
			OS.OffsetRgn(rgn, dx, dy);
			fRepairWindow[fRepairCount]= wHandle;
			fRepairRegion[fRepairCount]= rgn;
			fRepairCount++;
		} else {
			OS.InvalWindowRgn(wHandle, rgn);
			OS.DisposeRgn(rgn);
		}
	}
	
	/*
	private void doMenuCommand(int menuResult) {
		short menuID= OS.HiWord(menuResult);
		if (menuID != 0) {
			int frontWindow= OS.FrontWindow();
			Widget w= WidgetTable.get(frontWindow);
			if (w instanceof Shell) {
				Shell shell= (Shell) w;
				Menu menu= shell.findMenu(menuID);	
				if (menu != null)
					menu.handleMenu(menuResult);
			}
		}
		OS.HiliteMenu((short)0);	// unhighlight what MenuSelect (or MenuKey) hilited
	}
	*/
	
	public static int ticksToMS(int ticks) {
		return ticks * 17;		// 17 * 60 == 1000
	}
	
	static void processAllUpdateEvents(int cHandle) {
	
		if (SMART_REFRESH)
			return;
			
		if (true) {
			MacEvent me= new MacEvent();
			while (OS.GetNextEvent(OS.updateMask, me.getData()))
				;
				//if (me.getWhat() == OS.updateEvt)
				//	getDefault().updateWindow(me.getMessage());
		} else {
			int[] mask= new int[] {
				OS.kEventClassWindow, OS.kEventWindowDrawContent
			};
			int[] evt= new int[1];
			while (OS.ReceiveNextEvent(mask, 0.2, true, evt) == OS.kNoErr) {
				//System.out.println("got update");
				int rc= OS.SendEventToEventTarget(evt[0], OS.GetEventDispatcherTarget());
                //if (rc != OS.kNoErr)
				//	System.out.println("processAllUpdateEvents: " + rc);
				OS.ReleaseEvent(evt[0]);
			}
		}

		int wHandle= OS.GetControlOwner(cHandle);
		if (wHandle != 0) {
			int port= OS.GetWindowPort(wHandle);
			if (port != 0)
				OS.QDFlushPortBuffer(port, 0);
		}
	}
	
	private void flush(int wHandle) {
		if (wHandle != 0) {
			int port= OS.GetWindowPort(wHandle);
			if (port != 0)
				OS.QDFlushPortBuffer(port, 0);
		}
	}

	private void processPaintToolTip(int wHandle) {
			
		Color infoForeground = getSystemColor (SWT.COLOR_INFO_FOREGROUND);
		Color infoBackground = getSystemColor (SWT.COLOR_INFO_BACKGROUND);
		OS.RGBBackColor(infoBackground.handle);
		OS.RGBForeColor(infoForeground.handle);
		
		MacRect bounds= new MacRect();
		OS.GetWindowBounds(wHandle, OS.kWindowContentRgn, bounds.getData());
		
		bounds= new MacRect(0, 0, bounds.getWidth(), bounds.getHeight());
		
		OS.EraseRect(bounds.getData());
		
		if (fToolTipText != null) {
			int sHandle= OS.CFStringCreateWithCharacters(fToolTipText);
			bounds= new MacRect(TOOLTIP_MARGIN, TOOLTIP_MARGIN,
							bounds.getWidth()-2*TOOLTIP_MARGIN, bounds.getHeight()-2*TOOLTIP_MARGIN);
			OS.DrawThemeTextBox(sHandle, fHoverThemeFont, OS.kThemeStateActive, true, bounds.getData(), (short)0, 0);
			OS.CFRelease(sHandle);
		}
	}
	
	private void sendUserEvent(int kind) {
		int[] event= new int[1];
		OS.CreateEvent(0, SWT_USER_EVENT, kind, 0.0, OS.kEventAttributeUserEvent, event);
		if (event[0] != 0)
			OS.PostEventToQueue(OS.GetMainEventQueue(), event[0], (short)2);
	}
	
	public static MacFont getThemeFont(short themeFontId) {
		byte[] fontName= new byte[256];
		short[] fontSize= new short[1];
		byte[] style= new byte[1];
		OS.GetThemeFont(themeFontId, OS.smSystemScript, fontName, fontSize, style);
		return new MacFont(MacUtil.toString(fontName), fontSize[0], style[0]);
	}
	
	private boolean fMenuIsVisible;
	
	void menuIsVisible(boolean menuIsVisible) {
		fMenuIsVisible= menuIsVisible;
	}
}
