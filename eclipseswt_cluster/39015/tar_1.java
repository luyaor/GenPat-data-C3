package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
/* Start ACCESSIBILITY */
import org.eclipse.swt.accessibility.*;
/* End ACCESSIBILITY */

/**
 * Control is the abstract superclass of all windowed user interface classes.
 * <p>
 * <dl>
 * <dt><b>Styles:</b>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b>
 * <dd>FocusIn, FocusOut, Help, KeyDown, KeyUp, MouseDoubleClick, MouseDown, MouseEnter,
 *     MouseExit, MouseHover, MouseUp, MouseMove, Move, Paint, Resize</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */

public abstract class Control extends Widget implements Drawable {
	/**
	 * the handle to the OS resource 
	 * (Warning: This field is platform dependent)
	 */
	public int handle;
	
	Composite parent;
	int drawCount, hCursor;
	int foreground, background;
	Menu menu;
	String toolTipText;
	Object layoutData;

/* Start ACCESSIBILITY */
	Accessible accessible;
/* End ACCESSIBILITY */
	

/**
 * Prevents uninitialized instances from being created outside the package.
 */
Control () {
}

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * for all SWT widget classes should include a comment which
 * describes the style constants which are applicable to the class.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Control (Composite parent, int style) {
	super (parent, style);
	this.parent = parent;
	createWidget ();
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control is moved or resized, by sending
 * it one of the messages defined in the <code>ControlListener</code>
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
 * @see ControlListener
 * @see #removeControlListener
 */
public void addControlListener(ControlListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Resize,typedListener);
	addListener (SWT.Move,typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control gains or loses focus, by sending
 * it one of the messages defined in the <code>FocusListener</code>
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
 * @see FocusListener
 * @see #removeFocusListener
 */
public void addFocusListener (FocusListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.FocusIn,typedListener);
	addListener (SWT.FocusOut,typedListener);
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
 * be notified when keys are pressed and released on the system keyboard, by sending
 * it one of the messages defined in the <code>KeyListener</code>
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
 * @see KeyListener
 * @see #removeKeyListener
 */
public void addKeyListener (KeyListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.KeyUp,typedListener);
	addListener (SWT.KeyDown,typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when mouse buttons are pressed and released, by sending
 * it one of the messages defined in the <code>MouseListener</code>
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
 * @see MouseListener
 * @see #removeMouseListener
 */
public void addMouseListener (MouseListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.MouseDown,typedListener);
	addListener (SWT.MouseUp,typedListener);
	addListener (SWT.MouseDoubleClick,typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the mouse passes or hovers over controls, by sending
 * it one of the messages defined in the <code>MouseTrackListener</code>
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
 * @see MouseTrackListener
 * @see #removeMouseTrackListener
 */
public void addMouseTrackListener (MouseTrackListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.MouseEnter,typedListener);
	addListener (SWT.MouseExit,typedListener);
	addListener (SWT.MouseHover,typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the mouse moves, by sending it one of the
 * messages defined in the <code>MouseMoveListener</code>
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
 * @see MouseMoveListener
 * @see #removeMouseMoveListener
 */
public void addMouseMoveListener (MouseMoveListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.MouseMove,typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the receiver needs to be painted, by sending it
 * one of the messages defined in the <code>PaintListener</code>
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
 * @see PaintListener
 * @see #removePaintListener
 */
public void addPaintListener (PaintListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Paint,typedListener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when traversal events occur, by sending it
 * one of the messages defined in the <code>TraverseListener</code>
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
 * @see TraverseListener
 * @see #removeTraverseListener
 */
public void addTraverseListener (TraverseListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Traverse,typedListener);
}

abstract int callWindowProc (int msg, int wParam, int lParam);

/**
 * Returns the preferred size of the receiver.
 * <p>
 * The <em>prefered size</em> of a control is the size that it would
 * best be displayed at. The width hint and height hint arguments
 * allow the caller to ask a control questions such as "Given a particular
 * width, how high does the control need to be to show all of the contents?"
 * To indicate that the caller does not wish to constrain a particular 
 * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint. 
 * </p>
 *
 * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
 * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
 * @return the preferred size of the control
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Layout
 */
public Point computeSize (int wHint, int hHint) {
	return computeSize (wHint, hHint, true);
}

/**
 * Returns the preferred size of the receiver.
 * <p>
 * The <em>prefered size</em> of a control is the size that it would
 * best be displayed at. The width hint and height hint arguments
 * allow the caller to ask a control questions such as "Given a particular
 * width, how high does the control need to be to show all of the contents?"
 * To indicate that the caller does not wish to constrain a particular 
 * dimension, the constant <code>SWT.DEFAULT</code> is passed for the hint. 
 * </p><p>
 * If the changed flag is <code>true</code>, it indicates that the receiver's
 * <em>contents</em> have changed, therefore any caches that a layout manager
 * containing the control may have been keeping need to be flushed. When the
 * control is resized, the changed flag will be <code>false</code>, so layout
 * manager caches can be retained. 
 * </p>
 *
 * @param wHint the width hint (can be <code>SWT.DEFAULT</code>)
 * @param hHint the height hint (can be <code>SWT.DEFAULT</code>)
 * @param changed <code>true</code> if the control's contents have changed, and <code>false</code> otherwise
 * @return the preferred size of the control.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Layout
 */
public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int width = DEFAULT_WIDTH;
	int height = DEFAULT_HEIGHT;
	if (wHint != SWT.DEFAULT) width = wHint;
	if (hHint != SWT.DEFAULT) height = hHint;
	int border = getBorderWidth ();
	width += border * 2;
	height += border * 2;
	return new Point (width, height);
}

Control computeTabGroup () {
	if (isTabGroup ()) return this;
	return parent.computeTabGroup ();
}

Control computeTabRoot () {
	Control [] tabList = parent._getTabList ();
	if (tabList != null) {
		int index = 0;
		while (index < tabList.length) {
			if (tabList [index] == this) break;
			index++;
		}
		if (index == tabList.length) {
			if (isTabGroup ()) return this;
		}
	}
	return parent.computeTabRoot ();
}

Control [] computeTabList () {
	if (isTabGroup ()) {
		if (getVisible () && getEnabled ()) {
			return new Control [] {this};
		}
	}
	return new Control [0];
}

void createHandle () {
	int hwndParent = 0;
	if (handle != 0) {
		hwndParent = handle;
	} else {
		if (parent != null) hwndParent = parent.handle;
	}
	handle = OS.CreateWindowEx (
		widgetExtStyle (),
		windowClass (),
		null,
		widgetStyle (),
		OS.CW_USEDEFAULT, 0, OS.CW_USEDEFAULT, 0,
		hwndParent,
		0,
		OS.GetModuleHandle (null),
		null);
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
	if (OS.IsDBLocale && parent != null) {
		int hIMC = OS.ImmGetContext (hwndParent);
		OS.ImmAssociateContext (handle, hIMC);
		OS.ImmReleaseContext (hwndParent, hIMC);
	}
}

void createWidget () {
	foreground = background = -1;
	createHandle ();
	register ();
	subclass ();
	setDefaultFont ();
}

int defaultBackground () {
	if (OS.IsWinCE) return OS.GetSysColor (OS.COLOR_WINDOW);
	return OS.GetSysColor (OS.COLOR_BTNFACE);
}

int defaultFont () {
	Display display = getDisplay ();
	return display.systemFont ();
}

int defaultForeground () {
	return OS.GetSysColor (OS.COLOR_WINDOWTEXT);
}

void deregister () {
	WidgetTable.remove (handle);
}

void destroyWidget () {
	int hwnd = handle;
	releaseHandle ();
	if (hwnd != 0) {
		OS.DestroyWindow (hwnd);
	}
}

void drawBackground (int hDC) {
	RECT rect = new RECT ();
	OS.GetClientRect (handle, rect);
	drawBackground (hDC, rect);
}

void drawBackground (int hDC, RECT rect) {
	Display display = getDisplay ();
	int hPalette = display.hPalette;
	if (hPalette != 0) {
		OS.SelectPalette (hDC, hPalette, false);
		OS.RealizePalette (hDC);
	}
	int pixel = getBackgroundPixel ();
	int hBrush = findBrush (pixel);
	OS.FillRect (hDC, rect, hBrush);
}

int findBrush (int pixel) {
	return parent.findBrush (pixel);
}

int findCursor () {
	if (hCursor != 0) return hCursor;
	return parent.findCursor ();
}

char findMnemonic (String string) {
	int index = 0;
	int length = string.length ();
	do {
		while (index < length && string.charAt (index) != Mnemonic) index++;
		if (++index >= length) return '\0';
		if (string.charAt (index) != Mnemonic) return string.charAt (index);
		index++;
	} while (index < length);
 	return '\0';
}

void fixFocus () {
	Shell shell = getShell ();
	Control control = this;
	while ((control = control.parent) != null) {
		if (control.setFocus () || control == shell) return;
	}
	OS.SetFocus (0);
}

/**
 * Forces the receiver to have the <em>keyboard focus</em>, causing
 * all keyboard events to be delivered to it.
 *
 * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #setFocus
 */
public boolean forceFocus () {
	checkWidget ();
	Decorations shell = menuShell ();
	shell.setSavedFocus (this);
	if (!isEnabled () || !isVisible () || !isActive ()) return false;
	if (isFocusControl ()) return true;
	shell.bringToTop ();
	/*
	* This code is intentionally commented.
	*
	* When setting focus to a control, it is
	* possible that application code can set
	* the focus to another control inside of
	* WM_SETFOCUS.  In this case, the original
	* control will no longer have the focus
	* and the call to setFocus() will return
	* false indicating failure.
	* 
	* We are still working on a solution at
	* this time.
	*/
//	if (OS.GetFocus () != OS.SetFocus (handle)) return false;
	OS.SetFocus (handle);
	if (!isFocusControl ()) return false;
	shell.setDefaultButton (null, false);
	return true;
}

/* Start ACCESSIBILITY */
/**
 * NOTE: The API in the accessibility package is NOT finalized.
 * Use at your own risk, because it will most certainly change.
 * The methods in AccessibleListener are more stable than those
 * in AccessibleControlListener, however please take nothing for
 * granted. The only reason this API is being released at this
 * time is so that other teams can try it out.
 */
/**
 * Returns the accessible object for the receiver.
 * If this is the first time this object is requested,
 * then the object is created and returned.
 *
 * @return the accessible object
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @see addAccessibleListener
 * @see addAccessibleControlListener
 */
public Accessible getAccessible () {
	checkWidget ();
	if (accessible == null) {
		accessible = Accessible.internal_new_accessible (this);
	}
	return accessible;
}
/* End ACCESSIBILITY */

/**
 * Returns the receiver's background color.
 *
 * @return the background color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Color getBackground () {
	checkWidget ();
	return Color.win32_new (getDisplay (), getBackgroundPixel ());
}

int getBackgroundPixel () {
	if (background == -1) return defaultBackground ();
	return background;
}

/**
 * Returns the receiver's border width.
 *
 * @return the border width
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getBorderWidth () {
	checkWidget ();
	int bits = OS.GetWindowLong (handle, OS.GWL_EXSTYLE);
	if ((bits & OS.WS_EX_CLIENTEDGE) != 0) return OS.GetSystemMetrics (OS.SM_CXEDGE);
	if ((bits & OS.WS_EX_STATICEDGE) != 0) return OS.GetSystemMetrics (OS.SM_CXBORDER);
	return 0;
}

/**
 * Returns a rectangle describing the receiver's size and location
 * relative to its parent (or its display if its parent is null).
 *
 * @return the receiver's bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Rectangle getBounds () {
	checkWidget ();
	int hwndParent = 0;
	if (parent != null) {
		hwndParent = parent.handle;
		if (parent.hdwp != 0) {
			int oldHdwp = parent.hdwp;
			parent.hdwp = 0;
			OS.EndDeferWindowPos (oldHdwp);
			int count = parent.getChildrenCount ();
			parent.hdwp = OS.BeginDeferWindowPos (count);
		}
	}
	RECT rect = new RECT ();
	OS.GetWindowRect (handle, rect);
	OS.MapWindowPoints (0, hwndParent, rect, 2);
	int width = rect.right - rect.left;
	int height =  rect.bottom - rect.top;
	return new Rectangle (rect.left, rect.top, width, height);
}

int getCodePage () {
	int hFont = OS.SendMessage (handle, OS.WM_GETFONT, 0, 0);
	LOGFONT logFont = new LOGFONT ();
	OS.GetObject (hFont, LOGFONT.sizeof, logFont);
	int cs = logFont.lfCharSet & 0xFF;
	int [] lpCs = new int [8];
	if (OS.TranslateCharsetInfo (cs, lpCs, OS.TCI_SRCCHARSET)) {
		return lpCs [1];
	}
	return OS.GetACP ();
}

/**
 * Returns the display that the receiver was created on.
 *
 * @return the receiver's display
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Display getDisplay () {
	Composite parent = this.parent;
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
	return OS.IsWindowEnabled (handle);
}

/**
 * Returns the font that the receiver will use to paint textual information.
 *
 * @return the receiver's font
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Font getFont () {
	checkWidget ();	
	int hFont = OS.SendMessage (handle, OS.WM_GETFONT, 0, 0);
	if (hFont == 0) hFont = defaultFont ();
	return Font.win32_new (getDisplay (), hFont);
}

/**
 * Returns the foreground color that the receiver will use to draw.
 *
 * @return the receiver's foreground color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Color getForeground () {
	checkWidget ();
	return Color.win32_new (getDisplay (), getForegroundPixel ());
}

int getForegroundPixel () {
	if (foreground == -1) return defaultForeground ();
	return foreground;
}

/**
 * Returns layout data which is associated with the receiver.
 *
 * @return the receiver's layout data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Object getLayoutData () {
	checkWidget ();
	return layoutData;
}

/**
 * Returns a point describing the receiver's location relative
 * to its parent (or its display if its parent is null).
 *
 * @return the receiver's location
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getLocation () {
	checkWidget ();
	int hwndParent = 0;
	if (parent != null) {
		hwndParent = parent.handle;
		if (parent.hdwp != 0) {
			int oldHdwp = parent.hdwp;
			parent.hdwp = 0;
			OS.EndDeferWindowPos (oldHdwp);
			int count = parent.getChildrenCount ();
			parent.hdwp = OS.BeginDeferWindowPos (count);
		}
	}
	RECT rect = new RECT ();
	OS.GetWindowRect (handle, rect);
	OS.MapWindowPoints (0, hwndParent, rect, 2);
	return new Point (rect.left, rect.top);
}

/**
 * Returns the receiver's pop up menu if it has one, or null
 * if it does not. All controls may optionally have a pop up
 * menu that is displayed when the user requests one for
 * the control. The sequence of key strokes, button presses
 * and/or button releases that are used to request a pop up
 * menu is platform specific.
 *
 * @return the receiver's menu
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Menu getMenu () {
	checkWidget ();
	return menu;
}

/**
 * Returns the receiver's parent, which must be a <code>Composite</code>
 * or null when the receiver is a shell that was created with null or
 * a display for a parent.
 *
 * @return the receiver's parent
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Composite getParent () {
	checkWidget ();
	return parent;
}

Control [] getPath () {
	int count = 0;
	Shell shell = getShell ();
	Control control = this;
	while (control != shell) {
		count++;
		control = control.parent;
	}
	control = this;
	Control [] result = new Control [count];
	while (control != shell) {
		result [--count] = control;
		control = control.parent;
	}
	return result;
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
 * Returns a point describing the receiver's size. The
 * x coordinate of the result is the width of the receiver.
 * The y coordinate of the result is the height of the
 * receiver.
 *
 * @return the receiver's size
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point getSize () {
	checkWidget ();
	if (parent != null && parent.hdwp != 0) {
		int oldHdwp = parent.hdwp;
		parent.hdwp = 0;
		OS.EndDeferWindowPos (oldHdwp);
		int count = parent.getChildrenCount ();
		parent.hdwp = OS.BeginDeferWindowPos (count);
	}
	RECT rect = new RECT ();
	OS.GetWindowRect (handle, rect);
	int width = rect.right - rect.left;
	int height = rect.bottom - rect.top;
	return new Point (width, height);
}

/**
 * Returns the receiver's tool tip text, or null if it has
 * not been set.
 *
 * @return the receiver's tool tip text
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public String getToolTipText () {
	checkWidget ();
	return toolTipText;
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
	int bits = OS.GetWindowLong (handle, OS.GWL_STYLE);
	return (bits & OS.WS_VISIBLE) != 0;
}

boolean hasCursor () {
	RECT rect = new RECT ();
	if (!OS.GetClientRect (handle, rect)) return false;
	if (OS.MapWindowPoints (handle, 0, rect, 2) == 0) return false;
	POINT pt = new POINT ();
	return (OS.GetCursorPos (pt) && OS.PtInRect (rect, pt));
}

boolean hasFocus () {
	/*
	* If a non-SWT child of the control has focus,
	* then this control is considered to have focus
	* even though it does not have focus in Windows.
	*/
	int hwndFocus = OS.GetFocus ();
	while (hwndFocus != 0) {
		if (hwndFocus == handle) return true;
		if (WidgetTable.get (hwndFocus) != null) {
			return false;
		}
		hwndFocus = OS.GetParent (hwndFocus);
	}
	return false;
}

/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Control</code>. It is marked public only so that it
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
	checkWidget();
	int hDC;
	if (data == null || data.ps == null) {
		hDC = OS.GetDC (handle);
	} else {
		hDC = OS.BeginPaint (handle, data.ps);
	}
	if (hDC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	if (data != null) {
		data.device = getDisplay ();
		data.foreground = getForegroundPixel ();
		data.background = getBackgroundPixel ();
		data.hFont = OS.SendMessage (handle, OS.WM_GETFONT, 0, 0);
		data.hwnd = handle;
	}
	return hDC;
}

/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Control</code>. It is marked public only so that it
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
	checkWidget ();
	if (data == null || data.ps == null) {
		OS.ReleaseDC (handle, hDC);
	} else {
		OS.EndPaint (handle, data.ps);
	}
}

boolean isActive () {
	Display display = getDisplay ();
	Shell modal = display.getModalShell ();
	if (modal != null && modal != this) {
		if ((modal.style & SWT.PRIMARY_MODAL) != 0) {
			Shell shell = getShell ();
			if (modal.parent == shell) {
				return false;
			}
		}
		int bits = SWT.APPLICATION_MODAL | SWT.SYSTEM_MODAL;
		if ((modal.style & bits) != 0) {
			Control control = this;
			while (control != null) {
				if (control == modal) break;
				control = control.parent;
			}
			if (control != modal) return false;
		}
	}
	return getShell ().getEnabled ();
}

public boolean isDisposed () {
	return handle == 0;
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
	return getEnabled () && parent.isEnabled ();
}

/**
 * Returns <code>true</code> if the receiver has the user-interface
 * focus, and <code>false</code> otherwise.
 *
 * @return the receiver's focus state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isFocusControl () {
	checkWidget ();
	return hasFocus ();
}

boolean isFocusAncestor () {
	Display display = getDisplay ();
	Control control = display.getFocusControl ();
	while (control != null && control != this) {
		control = control.parent;
	}
	return control == this;
}

/**
 * Returns <code>true</code> if the underlying operating
 * system supports this reparenting, otherwise <code>false</code>
 *
 * @return <code>true</code> if the widget can be reparented, otherwise <code>false</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean isReparentable () {
	checkWidget ();
	return true;
}

boolean isShowing () {
	/*
	* This is not complete.  Need to check if the
	* widget is obscurred by a parent or sibling.
	*/
	if (!isVisible ()) return false;
	Control control = this;
	while (control != null) {
		Point size = control.getSize ();
		if (size.x == 0 || size.y == 0) {
			return false;
		}
		control = control.parent;
	}
	return true;
	/*
	* Check to see if current damage is included.
	*/
//	if (!OS.IsWindowVisible (handle)) return false;
//	int flags = OS.DCX_CACHE | OS.DCX_CLIPCHILDREN | OS.DCX_CLIPSIBLINGS;
//	int hDC = OS.GetDCEx (handle, 0, flags);
//	int result = OS.GetClipBox (hDC, new RECT ());
//	OS.ReleaseDC (handle, hDC);
//	return result != OS.NULLREGION;
}

boolean isTabGroup () {
	int bits = OS.GetWindowLong (handle, OS.GWL_STYLE);
	return (bits & OS.WS_TABSTOP) != 0;
}

boolean isTabItem () {
	int bits = OS.GetWindowLong (handle, OS.GWL_STYLE);
	if ((bits & OS.WS_TABSTOP) != 0) return false;
	int code = OS.SendMessage (handle, OS.WM_GETDLGCODE, 0, 0);
	if ((code & OS.DLGC_STATIC) != 0) return false;
	if ((code & OS.DLGC_WANTALLKEYS) != 0) return false;
	if ((code & OS.DLGC_WANTARROWS) != 0) return false;
	if ((code & OS.DLGC_WANTTAB) != 0) return false;
	return true;
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
	return OS.IsWindowVisible (handle);
}

Decorations menuShell () {
	return parent.menuShell ();
}

boolean mnemonicHit (char key) {
	return false;
}

boolean mnemonicMatch (char key) {
	return false;
}

/**
 * Moves the receiver above the specified control in the
 * drawing order. If the argument is null, then the receiver
 * is moved to the top of the drawing order. The control at
 * the top of the drawing order will not be covered by other
 * controls even if they occupy intersecting areas.
 *
 * @param the sibling control (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void moveAbove (Control control) {
	checkWidget ();
	int hwndAbove = OS.HWND_TOP;
	if (control != null) {
		if (control.isDisposed ()) error(SWT.ERROR_INVALID_ARGUMENT);
		int hwnd = control.handle;
		if (hwnd == 0 || hwnd == handle) return;
		hwndAbove = OS.GetWindow (hwnd, OS.GW_HWNDPREV);
		/*
		* Bug in Windows.  For some reason, when GetWindow ()
		* with GW_HWNDPREV is used to query the previous window
		* in the z-order with the first child, Windows returns
		* the first child instead of NULL.  The fix is to detect
		* this case and move the control to the top.
		*/
		if (hwndAbove == 0 || hwndAbove == hwnd) {
			hwndAbove = OS.HWND_TOP;
		}
	}
	int flags = OS.SWP_NOSIZE | OS.SWP_NOMOVE | OS.SWP_NOACTIVATE; 
	OS.SetWindowPos (handle, hwndAbove, 0, 0, 0, 0, flags);
}

/**
 * Moves the receiver below the specified control in the
 * drawing order. If the argument is null, then the receiver
 * is moved to the bottom of the drawing order. The control at
 * the bottom of the drawing order will be covered by all other
 * controls which occupy intersecting areas.
 *
 * @param the sibling control (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the control has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void moveBelow (Control control) {
	checkWidget ();
	int hwndAbove = OS.HWND_BOTTOM;
	if (control != null) {
		if (control.isDisposed ()) error(SWT.ERROR_INVALID_ARGUMENT);
		hwndAbove = control.handle;
	}
	if (hwndAbove == 0 || hwndAbove == handle) return;
	int flags = OS.SWP_NOSIZE | OS.SWP_NOMOVE | OS.SWP_NOACTIVATE; 
	OS.SetWindowPos (handle, hwndAbove, 0, 0, 0, 0, flags);
}

/**
 * Causes the receiver to be resized to its preferred size.
 * For a composite, this involves computing the preferred size
 * from its layout, if there is one.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #computeSize
 */
public void pack () {
	checkWidget ();
	pack (true);
}

/**
 * Causes the receiver to be resized to its preferred size.
 * For a composite, this involves computing the preferred size
 * from its layout, if there is one.
 * <p>
 * If the changed flag is <code>true</code>, it indicates that the receiver's
 * <em>contents</em> have changed, therefore any caches that a layout manager
 * containing the control may have been keeping need to be flushed. When the
 * control is resized, the changed flag will be <code>false</code>, so layout
 * manager caches can be retained. 
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #computeSize
 */
public void pack (boolean changed) {
	checkWidget ();
	setSize (computeSize (SWT.DEFAULT, SWT.DEFAULT, changed));
}

/**
 * Causes the entire bounds of the receiver to be marked
 * as needing to be redrawn. The next time a paint request
 * is processed, the control will be completely painted.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #update
 */
public void redraw () {
	checkWidget ();
	if (!OS.IsWindowVisible (handle)) return;
	if (OS.IsWinCE) {
		OS.InvalidateRect (handle, null, true);
	} else {
		int flags = OS.RDW_ERASE | OS.RDW_FRAME | OS.RDW_INVALIDATE;
		OS.RedrawWindow (handle, null, 0, flags);
	}
}

/**
 * Causes the rectangular area of the receiver specified by
 * the arguments to be marked as needing to be redrawn. 
 * The next time a paint request is processed, that area of
 * the receiver will be painted. If the <code>all</code> flag
 * is <code>true</code>, any children of the receiver which
 * intersect with the specified area will also paint their
 * intersecting areas. If the <code>all</code> flag is 
 * <code>false</code>, the children will not be painted.
 *
 * @param x the x coordinate of the area to draw
 * @param y the y coordinate of the area to draw
 * @param width the width of the area to draw
 * @param height the height of the area to draw
 * @param all <code>true</code> if children should redraw, and <code>false</code> otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #update
 */
public void redraw (int x, int y, int width, int height, boolean all) {
	checkWidget ();
	if (width <= 0 || height <= 0) return;
	if (!OS.IsWindowVisible (handle)) return;
	RECT rect = new RECT ();
	OS.SetRect (rect, x, y, x + width, y + height);
	if (OS.IsWinCE) {
		OS.InvalidateRect (handle, rect, true);
	} else {
		int flags = OS.RDW_ERASE | OS.RDW_FRAME | OS.RDW_INVALIDATE;
		if (all) flags |= OS.RDW_ALLCHILDREN;
		OS.RedrawWindow (handle, rect, 0, flags);
	}
}

void register () {
	WidgetTable.put (handle, this);
}

void releaseHandle () {
	handle = 0;
}

void releaseWidget () {
	super.releaseWidget ();
	if (OS.IsDBLocale) {
		OS.ImmAssociateContext (handle, 0);
	}
	if (toolTipText != null) {
		Shell shell = getShell ();
		shell.setToolTipText (handle, null);
	}
	toolTipText = null;
	if (menu != null && !menu.isDisposed ()) {
		menu.dispose ();
	}
	menu = null;
	deregister ();
	unsubclass ();
	parent = null;
	layoutData = null;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is moved or resized.
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
 * @see ControlListener
 * @see #addControlListener
 */
public void removeControlListener (ControlListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Move, listener);
	eventTable.unhook (SWT.Resize, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control gains or loses focus.
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
 * @see FocusListener
 * @see #addFocusListener
 */
public void removeFocusListener(FocusListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.FocusIn, listener);
	eventTable.unhook (SWT.FocusOut, listener);
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
 * be notified when keys are pressed and released on the system keyboard.
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
 * @see KeyListener
 * @see #addKeyListener
 */
public void removeKeyListener(KeyListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.KeyUp, listener);
	eventTable.unhook (SWT.KeyDown, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the mouse passes or hovers over controls.
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
 * @see MouseTrackListener
 * @see #addMouseTrackListener
 */
public void removeMouseTrackListener(MouseTrackListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.MouseEnter, listener);
	eventTable.unhook (SWT.MouseExit, listener);
	eventTable.unhook (SWT.MouseHover, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when mouse buttons are pressed and released.
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
 * @see MouseListener
 * @see #addMouseListener
 */
public void removeMouseListener (MouseListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.MouseDown, listener);
	eventTable.unhook (SWT.MouseUp, listener);
	eventTable.unhook (SWT.MouseDoubleClick, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the mouse moves.
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
 * @see MouseMoveListener
 * @see #addMouseMoveListener
 */
public void removeMouseMoveListener(MouseMoveListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.MouseMove, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the receiver needs to be painted.
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
 * @see PaintListener
 * @see #addPaintListener
 */
public void removePaintListener(PaintListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook(SWT.Paint, listener);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when traversal events occur.
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
 * @see TraverseListener
 * @see #addTraverseListener
 */
public void removeTraverseListener(TraverseListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Traverse, listener);
}

boolean sendKeyEvent (int type, int msg, int wParam, int lParam) {
	Event event = new Event ();
	if (!setKeyState (event, type)) return true;
	return sendKeyEvent (type, msg, wParam, lParam, event);
}

boolean sendKeyEvent (int type, int msg, int wParam, int lParam, Event event) {
	postEvent (type, event);
	return true;
}

boolean sendMouseEvent (int type, int button, int msg, int wParam, int lParam) {
	Event event = new Event ();
	event.button = button;
	event.x = (short) (lParam & 0xFFFF);
	event.y = (short) (lParam >> 16);
	if (OS.GetKeyState (OS.VK_MENU) < 0) event.stateMask |= SWT.ALT;
	if ((wParam & OS.MK_SHIFT) != 0) event.stateMask |= SWT.SHIFT;
	if ((wParam & OS.MK_CONTROL) != 0) event.stateMask |= SWT.CONTROL;
	if ((wParam & OS.MK_LBUTTON) != 0) event.stateMask |= SWT.BUTTON1;
	if ((wParam & OS.MK_MBUTTON) != 0) event.stateMask |= SWT.BUTTON2;
	if ((wParam & OS.MK_RBUTTON) != 0) event.stateMask |= SWT.BUTTON3;
	switch (type) {
		case SWT.MouseDown:
		case SWT.MouseDoubleClick:
			if (button == 1) event.stateMask &= ~SWT.BUTTON1;
			if (button == 2) event.stateMask &= ~SWT.BUTTON2;
			if (button == 3) event.stateMask &= ~SWT.BUTTON3;
			break;
		case SWT.MouseUp:
			if (button == 1) event.stateMask |= SWT.BUTTON1;
			if (button == 2) event.stateMask |= SWT.BUTTON2;
			if (button == 3) event.stateMask |= SWT.BUTTON3;
			break;
	}
	return sendMouseEvent (type, msg, wParam, lParam, event);
}

boolean sendMouseEvent (int type, int msg, int wParam, int lParam, Event event) {
	postEvent (type, event);
	return true;
}

/**
 * Sets the receiver's background color to the color specified
 * by the argument, or to the default system color for the control
 * if the argument is null.
 *
 * @param color the new color (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setBackground (Color color) {
	checkWidget ();
	int pixel = -1;
	if (color != null) {
		if (color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		pixel = color.handle;
	}
	setBackgroundPixel (pixel);
}

void setBackgroundPixel (int pixel) {
	if (background == pixel) return;
	background = pixel;
	OS.InvalidateRect (handle, null, true);
}

/**
 * Sets the receiver's size and location to the rectangular
 * area specified by the arguments. The <code>x</code> and 
 * <code>y</code> arguments are relative to the receiver's
 * parent (or its display if its parent is null).
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause that
 * value to be set to zero instead.
 * </p>
 *
 * @param x the new x coordinate for the receiver
 * @param y the new y coordinate for the receiver
 * @param width the new width for the receiver
 * @param height the new height for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setBounds (int x, int y, int width, int height) {
	checkWidget ();
	setBounds (x, y, Math.max (0, width), Math.max (0, height), 0);
}

void setBounds (int x, int y, int width, int height, int flags) {
	flags |= OS.SWP_NOZORDER | OS.SWP_DRAWFRAME | OS.SWP_NOACTIVATE;
	if (parent == null) {
		OS.SetWindowPos (handle, 0, x, y, width, height, flags);
		return;
	}
	int count = parent.getChildrenCount ();
	if (parent.hdwp == 0) {
		if (count > 1) {
			int bits = OS.GetWindowLong (handle, OS.GWL_STYLE);
			if ((bits & OS.WS_CLIPSIBLINGS) == 0) flags |= OS.SWP_NOCOPYBITS;
		}
		OS.SetWindowPos (handle, 0, x, y, width, height, flags);
		return;
	}
	int hdwp = OS.DeferWindowPos (parent.hdwp, handle, 0, x, y, width, height, flags);
	if (hdwp == 0) {
		int oldHdwp = parent.hdwp;
		parent.hdwp = 0;
		OS.EndDeferWindowPos (oldHdwp);
		if (count > 1) hdwp = OS.BeginDeferWindowPos (count);
	}
	parent.hdwp = hdwp;
}

/**
 * Sets the receiver's size and location to the rectangular
 * area specified by the argument. The <code>x</code> and 
 * <code>y</code> fields of the rectangle are relative to
 * the receiver's parent (or its display if its parent is null).
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause that
 * value to be set to zero instead.
 * </p>
 *
 * @param rect the new bounds for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setBounds (Rectangle rect) {
	checkWidget ();
	if (rect == null) error (SWT.ERROR_NULL_ARGUMENT);
	setBounds (rect.x, rect.y, rect.width, rect.height);
}

/**
 * If the argument is <code>true</code>, causes the receiver to have
 * all mouse events delivered to it until the method is called with
 * <code>false</code> as the argument.
 *
 * @param capture <code>true</code> to capture the mouse, and <code>false</code> to release it
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setCapture (boolean capture) {
	checkWidget ();
	if (capture) {
		OS.SetCapture (handle);
	} else {
		if (OS.GetCapture () == handle) {
			OS.ReleaseCapture ();
		}
	}
}

/**
 * Sets the receiver's cursor to the cursor specified by the
 * argument, or to the default cursor for that kind of control
 * if the argument is null.
 * <p>
 * When the mouse pointer passes over a control its appearance
 * is changed to match the control's cursor.
 * </p>
 *
 * @param cursor the new cursor (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setCursor (Cursor cursor) {
	checkWidget ();
	hCursor = 0;
	if (cursor != null) {
		if (cursor.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		hCursor = cursor.handle;
	}
	int hwndCursor = OS.GetCapture ();
	if (hwndCursor == 0) {
		POINT pt = new POINT ();
		if (!OS.GetCursorPos (pt)) return;
		int hwnd = hwndCursor = OS.WindowFromPoint (pt);
		while (hwnd != 0 && hwnd != handle) {
			hwnd = OS.GetParent (hwnd);
		}
		if (hwnd == 0) return;
	}
	int lParam = OS.HTCLIENT | (OS.WM_MOUSEMOVE << 16);
	OS.SendMessage (hwndCursor, OS.WM_SETCURSOR, hwndCursor, lParam);
}

void setDefaultFont () {
	Display display = getDisplay ();
	int hFont = display.systemFont ();
	OS.SendMessage (handle, OS.WM_SETFONT, hFont, 0);
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

	/*
	* Feature in Windows.  If the receiver has focus, disabling
	* the receiver causes no window to have focus.  The fix is
	* to assign focus to the first ancestor window that takes
	* focus.  If no window will take focus, set focus to the
	* desktop.
	*/
	boolean fixFocus = false;
	if (!enabled) fixFocus = isFocusAncestor ();
	OS.EnableWindow (handle, enabled);
	if (fixFocus) fixFocus ();
}

/**
 * Causes the receiver to have the <em>keyboard focus</em>, 
 * such that all keyboard events will be delivered to it.
 *
 * @return <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #forceFocus
 */
public boolean setFocus () {
	checkWidget ();
	return forceFocus ();
}

/**
 * Sets the font that the receiver will use to paint textual information
 * to the font specified by the argument, or to the default font for that
 * kind of control if the argument is null.
 *
 * @param font the new font (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setFont (Font font) {
	checkWidget ();
	int hFont = 0;
	if (font != null) { 
		if (font.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		hFont = font.handle;
	}
	if (hFont == 0) hFont = defaultFont ();
	OS.SendMessage (handle, OS.WM_SETFONT, hFont, 1);
}

/**
 * Sets the receiver's foreground color to the color specified
 * by the argument, or to the default system color for the control
 * if the argument is null.
 *
 * @param color the new color (or null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setForeground (Color color) {
	checkWidget ();
	int pixel = -1;
	if (color != null) {
		if (color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		pixel = color.handle;
	}
	setForegroundPixel (pixel);
}

void setForegroundPixel (int pixel) {
	if (foreground == pixel) return;
	foreground = pixel;
	OS.InvalidateRect (handle, null, true);
}

boolean setInputState (Event event, int type) {
	if (OS.GetKeyState (OS.VK_MENU) < 0) event.stateMask |= SWT.ALT;
	if (OS.GetKeyState (OS.VK_SHIFT) < 0) event.stateMask |= SWT.SHIFT;
	if (OS.GetKeyState (OS.VK_CONTROL) < 0) event.stateMask |= SWT.CONTROL;
	if (OS.GetKeyState (OS.VK_LBUTTON) < 0) event.stateMask |= SWT.BUTTON1;
	if (OS.GetKeyState (OS.VK_MBUTTON) < 0) event.stateMask |= SWT.BUTTON2;
	if (OS.GetKeyState (OS.VK_RBUTTON) < 0) event.stateMask |= SWT.BUTTON3;
	switch (type) {
		case SWT.KeyDown:
		case SWT.Traverse:
			if (event.keyCode == SWT.ALT) event.stateMask &= ~SWT.ALT;
			if (event.keyCode == SWT.SHIFT) event.stateMask &= ~SWT.SHIFT;
			if (event.keyCode == SWT.CONTROL) event.stateMask &= ~SWT.CONTROL;
			break;
		case SWT.KeyUp:
			if (event.keyCode == SWT.ALT) event.stateMask |= SWT.ALT;
			if (event.keyCode == SWT.SHIFT) event.stateMask |= SWT.SHIFT;
			if (event.keyCode == SWT.CONTROL) event.stateMask |= SWT.CONTROL;
			break;
	}		
	return true;
}

boolean setKeyState (Event event, int type) {
	Display display = getDisplay ();
	if (display.lastAscii != 0) {
		event.character = mbcsToWcs ((char) display.lastAscii);
	}
	if (display.lastVirtual) {
		event.keyCode = Display.translateKey (display.lastKey);
	}
	if (event.keyCode == 0 && event.character == 0) {
		return false;
	}
	return setInputState (event, type);
}

/**
 * Sets the layout data associated with the receiver to the argument.
 * 
 * @param layoutData the new layout data for the receiver.
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLayoutData (Object layoutData) {
	checkWidget ();
	this.layoutData = layoutData;
}

/**
 * Sets the receiver's location to the point specified by
 * the arguments which are relative to the receiver's
 * parent (or its display if its parent is null).
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
	setBounds (x, y, 0, 0, OS.SWP_NOSIZE);
}

/**
 * Sets the receiver's location to the point specified by
 * the argument which is relative to the receiver's
 * parent (or its display if its parent is null).
 *
 * @param location the new location for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLocation (Point location) {
	checkWidget ();
	if (location == null) error (SWT.ERROR_NULL_ARGUMENT);
	setLocation (location.x, location.y);
}

/**
 * Sets the receiver's pop up menu to the argument.
 * All controls may optionally have a pop up
 * menu that is displayed when the user requests one for
 * the control. The sequence of key strokes, button presses
 * and/or button releases that are used to request a pop up
 * menu is platform specific.
 *
 * @param menu the new pop up menu
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_MENU_NOT_POP_UP - the menu is not a pop up menu</li>
 *    <li>ERROR_INVALID_PARENT - if the menu is not in the same widget tree</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the menu has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMenu (Menu menu) {
	checkWidget ();
	if (menu != null) {
		if (menu.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		if ((menu.style & SWT.POP_UP) == 0) {
			error (SWT.ERROR_MENU_NOT_POP_UP);
		}
		if (menu.parent != menuShell ()) {
			error (SWT.ERROR_INVALID_PARENT);
		}
	}
	this.menu = menu;
}

boolean setRadioFocus () {
	return false;
}

/**
 * If the argument is <code>false</code>, causes subsequent drawing
 * operations in the receiver to be ignored. No drawing of any kind
 * can occur in the receiver until the flag is set to true.
 * Graphics operations that occurred while the flag was
 * <code>false</code> are lost. When the flag is set to <code>true</code>,
 * the entire widget is marked as needing to be redrawn.
 * <p>
 * Note: This operation is a hint and may not be supported on some
 * platforms or for some widgets.
 * </p>
 *
 * @param redraw the new redraw state
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 * 
 * @see #redraw
 * @see #update
 */
public void setRedraw (boolean redraw) {
	checkWidget ();
	/*
	 * This code is intentionally commented.
	 *
	 * Feature in Windows.  When WM_SETREDRAW is used to turn
	 * off drawing in a widget, it clears the WS_VISIBLE bits
	 * and then sets them when redraw is turned back on.  This
	 * means that WM_SETREDRAW will make a widget unexpectedly
	 * visible.
	 *
	 * There is no fix at this time.
	 */
//	if (drawCount == 0) {
//		int bits = OS.GetWindowLong (handle, OS.GWL_STYLE);
//		if ((bits & OS.WS_VISIBLE) == 0) return;
//	}
	
	if (redraw) {
		if (--drawCount == 0) {
			OS.SendMessage (handle, OS.WM_SETREDRAW, 1, 0);
			if (OS.IsWinCE) {
				OS.InvalidateRect (handle, null, true);
			} else {
				int flags = OS.RDW_ERASE | OS.RDW_FRAME | OS.RDW_INVALIDATE | OS.RDW_ALLCHILDREN;
				OS.RedrawWindow (handle, null, 0, flags);
			}
		}
	} else {
		if (drawCount++ == 0) {
			OS.SendMessage (handle, OS.WM_SETREDRAW, 0, 0);
		}
	}
}

/**
 * Sets the receiver's size to the point specified by the arguments.
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause that
 * value to be set to zero instead.
 * </p>
 *
 * @param width the new width for the receiver
 * @param height the new height for the receiver
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSize (int width, int height) {
	checkWidget ();
	setBounds (0, 0, Math.max (0, width), Math.max (0, height), OS.SWP_NOMOVE);
}

/**
 * Sets the receiver's size to the point specified by the argument.
 * <p>
 * Note: Attempting to set the width or height of the
 * receiver to a negative number will cause them to be
 * set to zero instead.
 * </p>
 *
 * @param size the new size for the receiver
 * @param height the new height for the receiver
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSize (Point size) {
	checkWidget ();
	if (size == null) error (SWT.ERROR_NULL_ARGUMENT);
	setSize (size.x, size.y);
}

boolean setTabGroupFocus () {
	return setTabItemFocus ();
}

boolean setTabItemFocus () {
	if (!isShowing ()) return false;
	return setFocus ();
}

/**
 * Sets the receiver's tool tip text to the argument, which
 * may be null indicating that no tool tip text should be shown.
 *
 * @param string the new tool tip text (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setToolTipText (String string) {
	checkWidget ();
	Shell shell = getShell ();
	shell.setToolTipText (handle, toolTipText = string);
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
	int bits = OS.GetWindowLong (handle, OS.GWL_STYLE);
	if (((bits & OS.WS_VISIBLE) != 0) == visible) return;
	if (visible) {
		/*
		* It is possible (but unlikely), that application
		* code could have disposed the widget in the show
		* event.  If this happens, just return.
		*/
		sendEvent (SWT.Show);
		if (isDisposed ()) return;
	}
	
	/*
	* Feature in Windows.  If the receiver has focus, hiding
	* the receiver causes no window to have focus.  The fix is
	* to assign focus to the first ancestor window that takes
	* focus.  If no window will take focus, set focus to the
	* desktop.
	*/
//	boolean fixFocus = false;
//	if (!visible) fixFocus = isFocusAncestor ();
	OS.ShowWindow (handle, visible ? OS.SW_SHOW : OS.SW_HIDE);
	if (!visible) {
		/*
		* It is possible (but unlikely), that application
		* code could have disposed the widget in the show
		* event.  If this happens, just return.
		*/
		sendEvent (SWT.Hide);
		if (isDisposed ()) return;
	}
//	if (fixFocus) fixFocus ();
}

void sort (int [] items) {
	/* Shell Sort from K&R, pg 108 */
	int length = items.length;
	for (int gap=length/2; gap>0; gap/=2) {
		for (int i=gap; i<length; i++) {
			for (int j=i-gap; j>=0; j-=gap) {
		   		if (items [j] <= items [j + gap]) {
					int swap = items [j];
					items [j] = items [j + gap];
					items [j + gap] = swap;
		   		}
	    	}
	    }
	}
}

void subclass () {
	int oldProc = windowProc ();
	int newProc = getDisplay ().windowProc;
	if (oldProc == newProc) return;
	OS.SetWindowLong (handle, OS.GWL_WNDPROC, newProc);
}

/**
 * Returns a point which is the result of converting the
 * argument, which is specified in display relative coordinates,
 * to coordinates relative to the receiver.
 * <p>
 * @param point the point to be translated (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point toControl (Point point) {
	checkWidget ();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);
	POINT pt = new POINT ();
	pt.x = point.x;  pt.y = point.y; 
	OS.ScreenToClient (handle, pt);
	return new Point (pt.x, pt.y);
}

/**
 * Returns a point which is the result of converting the
 * argument, which is specified in coordinates relative to
 * the receiver, to display relative coordinates.
 * <p>
 * @param point the point to be translated (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public Point toDisplay (Point point) {
	checkWidget ();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);
	POINT pt = new POINT ();
	pt.x = point.x;  pt.y = point.y; 
	OS.ClientToScreen (handle, pt);
	return new Point (pt.x, pt.y);
}

boolean translateAccelerator (MSG msg) {
	return menuShell ().translateAccelerator (msg);
}

boolean translateMnemonic (char key) {
	if (!isVisible () || !isEnabled ()) return false;
	Event event = new Event ();
	event.doit = mnemonicMatch (key);
	event.detail = SWT.TRAVERSE_MNEMONIC;
	Display display = getDisplay ();
	display.lastVirtual = false;
	display.lastKey = 0;
	display.lastAscii = key;
	if (!setKeyState (event, SWT.Traverse)) {
		return false;
	}
	return traverse (event);
}

boolean translateMnemonic (MSG msg) {
	int hwnd = msg.hwnd;
	if (OS.GetKeyState (OS.VK_MENU) >= 0) {
		int code = OS.SendMessage (hwnd, OS.WM_GETDLGCODE, 0, 0);
		if ((code & OS.DLGC_WANTALLKEYS) != 0) return false;
		if ((code & OS.DLGC_BUTTON) == 0) return false;
	}
	Decorations shell = menuShell ();
	if (shell.isVisible () && shell.isEnabled ()) {
		char ch = mbcsToWcs ((char) msg.wParam);
		return ch != 0 && shell.translateMnemonic (ch);
	}
	return false;
}

boolean translateTraversal (MSG msg) {
	int hwnd = msg.hwnd;
	int key = msg.wParam;
	int detail = SWT.TRAVERSE_NONE;
	boolean doit = true, all = false;
	boolean lastVirtual = false;
	int lastKey = key, lastAscii = 0;
	switch (key) {
		case OS.VK_ESCAPE: {
			lastAscii = 27;
			Shell shell = getShell ();
			if (!shell.isVisible () || !shell.isEnabled ()) return false;
			int code = OS.SendMessage (hwnd, OS.WM_GETDLGCODE, 0, 0);
			if ((code & OS.DLGC_WANTALLKEYS) != 0) doit = false;
			if (shell.parent == null) doit = false;
			detail = SWT.TRAVERSE_ESCAPE;
			break;
		}
		case OS.VK_RETURN: {
			lastAscii = '\r';
			Button button = menuShell ().getDefaultButton ();
			if (button == null || button.isDisposed ()) return false;
			if (!button.isVisible () || !button.isEnabled ()) return false;
			int code = OS.SendMessage (hwnd, OS.WM_GETDLGCODE, 0, 0);
			if ((code & OS.DLGC_WANTALLKEYS) != 0) doit = false;
			detail = SWT.TRAVERSE_RETURN;
			break;
		}
		case OS.VK_TAB: {
			/*
			* NOTE: This code causes Shift+Tab and Ctrl+Tab to
			* always attempt traversal which is not the correct.
			* This behavior is currently relied on by StyledText.
			*/
			lastAscii = '\t';
			boolean next = OS.GetKeyState (OS.VK_SHIFT) >= 0;
			int code = OS.SendMessage (hwnd, OS.WM_GETDLGCODE, 0, 0);
			if ((code & (OS.DLGC_WANTTAB | OS.DLGC_WANTALLKEYS)) != 0) {
				if (next && OS.GetKeyState (OS.VK_CONTROL) >= 0) doit = false;
			}
			detail = next ? SWT.TRAVERSE_TAB_NEXT : SWT.TRAVERSE_TAB_PREVIOUS;
			break;
		}
		case OS.VK_UP:
		case OS.VK_LEFT:
		case OS.VK_DOWN:
		case OS.VK_RIGHT: {
			lastVirtual = true;
			int code = OS.SendMessage (hwnd, OS.WM_GETDLGCODE, 0, 0);
			if ((code & (OS.DLGC_WANTARROWS /*| OS.DLGC_WANTALLKEYS*/)) != 0) doit = false;
			boolean next = key == OS.VK_DOWN || key == OS.VK_RIGHT;
			detail = next ? SWT.TRAVERSE_ARROW_NEXT : SWT.TRAVERSE_ARROW_PREVIOUS;
			break;
		}
		case OS.VK_PRIOR:
		case OS.VK_NEXT: {
			all = true;
			lastVirtual = true;
			if (OS.GetKeyState (OS.VK_CONTROL) >= 0) return false;
			int code = OS.SendMessage (hwnd, OS.WM_GETDLGCODE, 0, 0);
			if ((code & OS.DLGC_WANTALLKEYS) != 0) doit = false;
			detail = key == OS.VK_PRIOR ? SWT.TRAVERSE_PAGE_PREVIOUS : SWT.TRAVERSE_PAGE_NEXT;
			break;
		}
		default:
			return false;
	}
	Event event = new Event ();
	event.doit = doit;
	event.detail = detail;
	Display display = getDisplay ();
	display.lastKey = lastKey;
	display.lastAscii = lastAscii;
	display.lastVirtual = lastVirtual;
	if (!setKeyState (event, SWT.Traverse)) {
		return false;
	}
	Shell shell = getShell ();
	Control control = this;
	do {
		if (control.traverse (event)) return true;
		if (control == shell) return false;
		control = control.parent;
	} while (all && control != null);
	return false;
}

boolean traverse (Event event) {
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the traverse
	* event.  If this happens, return true to stop further
	* event processing.
	*/	
	sendEvent (SWT.Traverse, event);
	if (isDisposed ()) return false;
	if (!event.doit) return false;
	switch (event.detail) {
		case SWT.TRAVERSE_NONE:				return true;
		case SWT.TRAVERSE_ESCAPE:			return traverseEscape ();
		case SWT.TRAVERSE_RETURN:			return traverseReturn ();
		case SWT.TRAVERSE_TAB_NEXT:			return traverseGroup (true);
		case SWT.TRAVERSE_TAB_PREVIOUS:		return traverseGroup (false);
		case SWT.TRAVERSE_ARROW_NEXT:		return traverseItem (true);
		case SWT.TRAVERSE_ARROW_PREVIOUS:	return traverseItem (false);
		case SWT.TRAVERSE_MNEMONIC:			return traverseMnemonic (event.character);	
		case SWT.TRAVERSE_PAGE_NEXT:		return traversePage (true);
		case SWT.TRAVERSE_PAGE_PREVIOUS:	return traversePage (false);
	}
	return false;
}

/**
 * Based on the argument, perform one of the expected platform
 * traversal action. The argument should be one of the constants:
 * <code>SWT.TRAVERSE_ESCAPE</code>, <code>SWT.TRAVERSE_RETURN</code>, 
 * <code>SWT.TRAVERSE_TAB_NEXT</code>, <code>SWT.TRAVERSE_TAB_PREVIOUS</code>, 
 * <code>SWT.TRAVERSE_ARROW_NEXT</code> and <code>SWT.TRAVERSE_ARROW_PREVIOUS</code>.
 *
 * @param traversal the type of traversal
 * @return true if the traversal succeeded
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public boolean traverse (int traversal) {
	checkWidget ();
	if (!isFocusControl () && !setFocus ()) return false;
	Event event = new Event ();
	event.doit = true;
	event.detail = traversal;
	return traverse (event);
}

boolean traverseEscape () {
	Shell shell = getShell ();
	if (shell.parent == null) return false;
	if (!shell.isVisible () || !shell.isEnabled ()) return false;
	shell.close ();
	return true;
}

boolean traverseGroup (boolean next) {
	Control root = computeTabRoot ();
	Control group = computeTabGroup ();
	Control [] list = root.computeTabList ();
	int length = list.length;
	int index = 0;
	while (index < length) {
		if (list [index] == group) break;
		index++;
	}
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in focus in
	* or out events.  Ensure that a disposed widget is
	* not accessed.
	*/
	if (index == length) return false;
	int start = index, offset = (next) ? 1 : -1;
	while ((index = ((index + offset + length) % length)) != start) {
		Control control = list [index];
		if (!control.isDisposed () && control.setTabGroupFocus ()) {
			if (!isDisposed () && !isFocusControl ()) return true;
		}
	}
	if (group.isDisposed ()) return false;
	return group.setTabGroupFocus ();
}

boolean traverseItem (boolean next) {
	Control [] children = parent._getChildren ();
	int length = children.length;
	int index = 0;
	while (index < length) {
		if (children [index] == this) break;
		index++;
	}
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in focus in
	* or out events.  Ensure that a disposed widget is
	* not accessed.
	*/
	int start = index, offset = (next) ? 1 : -1;
	while ((index = (index + offset + length) % length) != start) {
		Control child = children [index];
		if (!child.isDisposed () && child.isTabItem ()) {
			if (child.setTabItemFocus ()) return true;
		}
	}
	return false;
}

boolean traverseMnemonic (char key) {
	return mnemonicHit (key);
}

boolean traversePage (boolean next) {
	return false;
}

boolean traverseReturn () {
	Button button = menuShell ().getDefaultButton ();
	if (button == null || button.isDisposed ()) return false;
	if (!button.isVisible () || !button.isEnabled ()) return false;
	button.click ();
	return true;
}

void unsubclass () {
	int newProc = windowProc ();
	int oldProc = getDisplay ().windowProc;
	if (oldProc == newProc) return;
	OS.SetWindowLong (handle, OS.GWL_WNDPROC, newProc);
}

/**
 * Forces all outstanding paint requests for the widget tree
 * to be processed before this method returns.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #redraw
 */
public void update () {
	checkWidget ();
	if (OS.IsWinCE) {
		OS.UpdateWindow (handle);
	} else {
		int flags = OS.RDW_UPDATENOW | OS.RDW_ALLCHILDREN;
		OS.RedrawWindow (handle, null, 0, flags);
	}
}

void updateFont (Font oldFont, Font newFont) {
	Font font = getFont ();
	if (font.equals (oldFont)) setFont (newFont);
}

int widgetExtStyle () {
	if ((style & SWT.BORDER) != 0) return OS.WS_EX_CLIENTEDGE;
	return 0;
}

int widgetStyle () {
	/* Force strict clipping by setting WS_CLIPSIBLINGS */
	return OS.WS_CHILD | OS.WS_VISIBLE | OS.WS_CLIPSIBLINGS;
	
	/*
	* This code is intentionally commented.  When strict
	* clipping (clipping of both siblings and children)
	* was not enforced on all widgets, poorly written
	* application code could draw outside of the control.
	*/
//	int bits = OS.WS_CHILD | OS.WS_VISIBLE;
//	if ((style & SWT.CLIP_SIBLINGS) != 0) bits |= OS.WS_CLIPSIBLINGS;
//	if ((style & SWT.CLIP_CHILDREN) != 0) bits |= OS.WS_CLIPCHILDREN;
//	return bits;
}

/**
 * Changes the parent of the widget to be the one provided if
 * the underlying operating system supports this feature.
 * Answers <code>true</code> if the parent is successfully changed.
 *
 * @param parent the new parent for the control.
 * @return <code>true</code> if the parent is changed and <code>false</code> otherwise.
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li> 
 * </ul>
 * @exception SWTError <ul>
 *		<li>ERROR_THREAD_INVALID_ACCESS when called from the wrong thread</li>
 *		<li>ERROR_WIDGET_DISPOSED when the widget has been disposed</li>
 *	</ul>
 */
public boolean setParent (Composite parent) {
	checkWidget ();
	if (parent == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (parent.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	if (OS.SetParent (handle, parent.handle) == 0) {
		return false;
	}
	this.parent = parent;
	return true;
}

abstract TCHAR windowClass ();

abstract int windowProc ();

int windowProc (int msg, int wParam, int lParam) {
	LRESULT result = null;
	switch (msg) {
		case OS.WM_ACTIVATE:			result = WM_ACTIVATE (wParam, lParam); break;
		case OS.WM_CHAR:				result = WM_CHAR (wParam, lParam); break;
		case OS.WM_CLEAR:				result = WM_CLEAR (wParam, lParam); break;
		case OS.WM_CLOSE:				result = WM_CLOSE (wParam, lParam); break;
		case OS.WM_COMMAND:			result = WM_COMMAND (wParam, lParam); break;
		case OS.WM_CONTEXTMENU:		result = WM_CONTEXTMENU (wParam, lParam); break;
		case OS.WM_CTLCOLORBTN:
		case OS.WM_CTLCOLORDLG:
		case OS.WM_CTLCOLOREDIT:
		case OS.WM_CTLCOLORLISTBOX:
		case OS.WM_CTLCOLORMSGBOX:
		case OS.WM_CTLCOLORSCROLLBAR:
		case OS.WM_CTLCOLORSTATIC:		result = WM_CTLCOLOR (wParam, lParam); break;
		case OS.WM_CUT:				result = WM_CUT (wParam, lParam); break;
		case OS.WM_DESTROY:			result = WM_DESTROY (wParam, lParam); break;
		case OS.WM_DRAWITEM:			result = WM_DRAWITEM (wParam, lParam); break;
		case OS.WM_ERASEBKGND:			result = WM_ERASEBKGND (wParam, lParam); break;
		case OS.WM_GETDLGCODE:			result = WM_GETDLGCODE (wParam, lParam); break;
		case OS.WM_HELP:				result = WM_HELP (wParam, lParam); break;
		case OS.WM_HSCROLL:			result = WM_HSCROLL (wParam, lParam); break;
		case OS.WM_IME_CHAR:			result = WM_IME_CHAR (wParam, lParam); break;
		case OS.WM_IME_COMPOSITION:	result = WM_IME_COMPOSITION (wParam, lParam); break;
		case OS.WM_INITMENUPOPUP:		result = WM_INITMENUPOPUP (wParam, lParam); break;
		case OS.WM_GETFONT:			result = WM_GETFONT (wParam, lParam); break;
/* Start ACCESSIBILITY */
		case OS.WM_GETOBJECT:			result = WM_GETOBJECT (wParam, lParam); break;
/* End ACCESSIBILITY */
		case OS.WM_KEYDOWN:			result = WM_KEYDOWN (wParam, lParam); break;
		case OS.WM_KEYUP:				result = WM_KEYUP (wParam, lParam); break;
		case OS.WM_KILLFOCUS:			result = WM_KILLFOCUS (wParam, lParam); break;
		case OS.WM_LBUTTONDBLCLK:		result = WM_LBUTTONDBLCLK (wParam, lParam); break;
		case OS.WM_LBUTTONDOWN:		result = WM_LBUTTONDOWN (wParam, lParam); break;
		case OS.WM_LBUTTONUP:			result = WM_LBUTTONUP (wParam, lParam); break;
		case OS.WM_MBUTTONDBLCLK:		result = WM_MBUTTONDBLCLK (wParam, lParam); break;
		case OS.WM_MBUTTONDOWN:		result = WM_MBUTTONDOWN (wParam, lParam); break;
		case OS.WM_MBUTTONUP:			result = WM_MBUTTONUP (wParam, lParam); break;
		case OS.WM_MEASUREITEM:		result = WM_MEASUREITEM (wParam, lParam); break;
		case OS.WM_MENUCHAR:			result = WM_MENUCHAR (wParam, lParam); break;
		case OS.WM_MENUSELECT:			result = WM_MENUSELECT (wParam, lParam); break;
		case OS.WM_MOUSEACTIVATE:		result = WM_MOUSEACTIVATE (wParam, lParam); break;
		case OS.WM_MOUSEHOVER:			result = WM_MOUSEHOVER (wParam, lParam); break;
		case OS.WM_MOUSELEAVE:			result = WM_MOUSELEAVE (wParam, lParam); break;
		case OS.WM_MOUSEMOVE:			result = WM_MOUSEMOVE (wParam, lParam); break;
		case OS.WM_MOUSEWHEEL:			result = WM_MOUSEWHEEL (wParam, lParam); break;
		case OS.WM_MOVE:				result = WM_MOVE (wParam, lParam); break;
		case OS.WM_NCACTIVATE:			result = WM_NCACTIVATE (wParam, lParam); break;
		case OS.WM_NCCALCSIZE:			result = WM_NCCALCSIZE (wParam, lParam); break;
		case OS.WM_NCHITTEST:			result = WM_NCHITTEST (wParam, lParam); break;
		case OS.WM_NOTIFY:				result = WM_NOTIFY (wParam, lParam); break;
		case OS.WM_PAINT:				result = WM_PAINT (wParam, lParam); break;
		case OS.WM_PALETTECHANGED:		result = WM_PALETTECHANGED (wParam, lParam); break;
		case OS.WM_PASTE:				result = WM_PASTE (wParam, lParam); break;
		case OS.WM_QUERYNEWPALETTE:	result = WM_QUERYNEWPALETTE (wParam, lParam); break;
		case OS.WM_QUERYOPEN:			result = WM_QUERYOPEN (wParam, lParam); break;
		case OS.WM_RBUTTONDBLCLK:		result = WM_RBUTTONDBLCLK (wParam, lParam); break;
		case OS.WM_RBUTTONDOWN:		result = WM_RBUTTONDOWN (wParam, lParam); break;
		case OS.WM_RBUTTONUP:			result = WM_RBUTTONUP (wParam, lParam); break;
		case OS.WM_SETCURSOR:			result = WM_SETCURSOR (wParam, lParam); break;
		case OS.WM_SETFOCUS:			result = WM_SETFOCUS (wParam, lParam); break;
		case OS.WM_SETFONT:			result = WM_SETFONT (wParam, lParam); break;
		case OS.WM_SETTINGCHANGE:		result = WM_SETTINGCHANGE (wParam, lParam); break;
		case OS.WM_SHOWWINDOW:			result = WM_SHOWWINDOW (wParam, lParam); break;
		case OS.WM_SIZE:				result = WM_SIZE (wParam, lParam); break;
		case OS.WM_SYSCHAR:			result = WM_SYSCHAR (wParam, lParam); break;
		case OS.WM_SYSCOLORCHANGE:		result = WM_SYSCOLORCHANGE (wParam, lParam); break;
		case OS.WM_SYSCOMMAND:			result = WM_SYSCOMMAND (wParam, lParam); break;
		case OS.WM_SYSKEYDOWN:			result = WM_SYSKEYDOWN (wParam, lParam); break;
		case OS.WM_SYSKEYUP:			result = WM_SYSKEYUP (wParam, lParam); break;
		case OS.WM_TIMER:				result = WM_TIMER (wParam, lParam); break;
		case OS.WM_UNDO:				result = WM_UNDO (wParam, lParam); break;
		case OS.WM_VSCROLL:			result = WM_VSCROLL (wParam, lParam); break;
		case OS.WM_WINDOWPOSCHANGING:	result = WM_WINDOWPOSCHANGING (wParam, lParam); break;
	}
	if (result != null) return result.value;
	return callWindowProc (msg, wParam, lParam);
}

LRESULT WM_ACTIVATE (int wParam, int lParam) {
	return null;
}

LRESULT WM_CHAR (int wParam, int lParam) {

	/*
	* Do not report a lead byte as a key pressed.
	*/
	Display display = getDisplay ();
	if (!OS.IsUnicode && OS.IsDBLocale) {
		byte lead = (byte) (wParam & 0xFF);
		if (OS.IsDBCSLeadByte (lead)) return null;
	}
	
	/*
	* Use VkKeyScan () to tell us if the character is a control
	* or a numeric key pad character with Num Lock down.  On
	* international keyboards, the control key may be down when
	* the character is not a control character.  In this case
	* use the last key (computed in WM_KEYDOWN) instead of wParam
	* as the keycode because there is not enough information to
	* compute the keycode in WPARAM.
	*/
	display.lastAscii = wParam;
	if (display.lastKey == 0) {
		display.lastKey = wParam;
		display.lastVirtual = display.isVirtualKey (wParam);
	} else {
		int result = OS.IsWinCE ? 0 : OS.VkKeyScan ((short) wParam);
		if (!OS.IsWinCE && (result == -1 || (result >> 8) <= 2)) {
			if (OS.GetKeyState (OS.VK_CONTROL) < 0) {
				display.lastVirtual = display.isVirtualKey (display.lastKey);
			}
		} else {
			display.lastKey = wParam;
			display.lastVirtual = false;
		}
	}
	if (!sendKeyEvent (SWT.KeyDown, OS.WM_CHAR, wParam, lParam)) {
		return LRESULT.ZERO;
	}
	return null;
}

LRESULT WM_CLEAR (int wParam, int lParam) {
	return null;
}

LRESULT WM_CLOSE (int wParam, int lParam) {
	return null;
}

LRESULT WM_COMMAND (int wParam, int lParam) {
	/*
	* When the WM_COMMAND message is sent from a
	* menu, the HWND parameter in LPARAM is zero.
	*/
	if (lParam == 0) {
		Decorations shell = menuShell ();
		if (shell.isEnabled ()) {
			int id = wParam & 0xFFFF;
			MenuItem item = shell.findMenuItem (id);
			if (item != null && item.isEnabled ()) {
				return item.wmCommandChild (wParam, lParam);
			}
		}
		return null;
	}
	Control control = WidgetTable.get (lParam);
	if (control == null) return null;
	return control.wmCommandChild (wParam, lParam);
}

LRESULT WM_CONTEXTMENU (int wParam, int lParam) {
	/*
	* Because context menus can be shared between controls
	* and the parent of all menus is the shell, the menu may
	* have been destroyed but not removed from the control.
	*/
	if (menu == null || menu.isDisposed ()) return null;
	menu.setVisible (true);
	return LRESULT.ZERO;
}

LRESULT WM_CTLCOLOR (int wParam, int lParam) {
	Display display = getDisplay ();
	int hPalette = display.hPalette;
	if (hPalette != 0) {
		OS.SelectPalette (wParam, hPalette, false);
		OS.RealizePalette (wParam);
	}
	Control control = WidgetTable.get (lParam);
	if (control == null) return null;
	return control.wmColorChild (wParam, lParam);
}

LRESULT WM_CUT (int wParam, int lParam) {
	return null;
}

LRESULT WM_DESTROY (int wParam, int lParam) {
	return null;
}

LRESULT WM_DRAWITEM (int wParam, int lParam) {
	DRAWITEMSTRUCT struct = new DRAWITEMSTRUCT ();
	OS.MoveMemory (struct, lParam, DRAWITEMSTRUCT.sizeof);
	if (struct.CtlType == OS.ODT_MENU) {
		Decorations shell = menuShell ();
		MenuItem item = shell.findMenuItem (struct.itemID);
		if (item == null) return null;
		return item.wmDrawChild (wParam, lParam);
	}
	Control control = WidgetTable.get (struct.hwndItem);
	if (control == null) return null;
	return control.wmDrawChild (wParam, lParam);
}

LRESULT WM_ERASEBKGND (int wParam, int lParam) {
	return null;
}

LRESULT WM_GETDLGCODE (int wParam, int lParam) {
	return null;
}

LRESULT WM_GETFONT (int wParam, int lParam) {
	return null;
}

/* Start ACCESSIBILITY */
LRESULT WM_GETOBJECT (int wParam, int lParam) {
	if (accessible != null) {
		int result = accessible.internal_WM_GETOBJECT (wParam, lParam);
		if (result != 0) {
			return new LRESULT(result);
		}
	}
	return null;
}
/* End ACCESSIBILITY */

LRESULT WM_HELP (int wParam, int lParam) {
	if (OS.IsWinCE) return null;
	HELPINFO lphi = new HELPINFO ();
	OS.MoveMemory (lphi, lParam, HELPINFO.sizeof);
	Decorations shell = menuShell ();
	if (!shell.isEnabled ()) return null;
	if (lphi.iContextType == OS.HELPINFO_MENUITEM) {
		MenuItem item = shell.findMenuItem (lphi.iCtrlId);
		if (item != null && item.isEnabled ()) {
			Widget widget = null;
			if (item.hooks (SWT.Help)) {
				widget = item;
			} else {
				Menu menu = item.parent;
				if (menu.hooks (SWT.Help)) widget = menu;
			}
			if (widget != null) {
				int hwndShell = shell.handle;
				OS.SendMessage (hwndShell, OS.WM_CANCELMODE, 0, 0);
				widget.postEvent (SWT.Help);
				return LRESULT.ONE;
			}
		}
		return null;
	}
	if (hooks (SWT.Help)) {
		postEvent (SWT.Help);
		return LRESULT.ONE;
	}
	return null;
}

LRESULT WM_HSCROLL (int wParam, int lParam) {
	if (lParam == 0) return null;
	Control control = WidgetTable.get (lParam);
	if (control == null) return null;
	return control.wmScrollChild (wParam, lParam);
}

LRESULT WM_IME_CHAR (int wParam, int lParam) {
	Display display = getDisplay ();
	display.lastKey = 0;
	display.lastAscii = wParam;
	display.lastVirtual = false;
	sendKeyEvent (SWT.KeyDown, OS.WM_IME_CHAR, wParam, lParam);
	sendKeyEvent (SWT.KeyUp, OS.WM_IME_CHAR, wParam, lParam);
	display.lastKey = display.lastAscii = 0;
	return LRESULT.ZERO;
}

LRESULT WM_IME_COMPOSITION (int wParam, int lParam) {
	return null;
}

LRESULT WM_INITMENUPOPUP (int wParam, int lParam) {
	
	/* Ignore WM_INITMENUPOPUP for an accelerator */
	Display display = getDisplay ();
	if (display.accelKeyHit) return null;

	/*
	* If the high order word of LPARAM is non-zero,
	* the menu is the system menu and we can ignore
	* WPARAM.  Otherwise, use WPARAM to find the menu.
	*/
	Shell shell = getShell ();
	Menu oldMenu = shell.activeMenu, newMenu = null;
	if ((lParam >> 16) == 0) {
		newMenu = menuShell ().findMenu (wParam);
	}	
	Menu menu = newMenu;
	while (menu != null && menu != oldMenu) {
		menu = menu.getParentMenu ();
	}
	if (menu == null) {
		menu = shell.activeMenu;
		while (menu != null) {
			/*
			* It is possible (but unlikely), that application
			* code could have disposed the widget in the hide
			* event.  If this happens, stop searching up the
			* ancestor list because there is no longer a link
			* to follow.
			*/
			menu.sendEvent (SWT.Hide);
			if (menu.isDisposed ()) break;
			menu = menu.getParentMenu ();
			Menu ancestor = newMenu;
			while (ancestor != null && ancestor != menu) {
				ancestor = ancestor.getParentMenu ();
			}
			if (ancestor != null) break;
		}
	}
	
	/*
	* The shell and the new menu may be disposed because of
	* sending the hide event to the ancestor menus but setting
	* a field to null in a disposed shell is not harmful.
	*/
	if (newMenu != null && newMenu.isDisposed ()) newMenu = null;
	shell.activeMenu = newMenu;
	
	/*
	* Send the show event
	*/
	if (newMenu != null && newMenu != oldMenu) {
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
		display.runDeferredEvents ();
		newMenu.sendEvent (SWT.Show);
		// widget could be disposed at this point
	}
	return null;
}

LRESULT WM_KEYDOWN (int wParam, int lParam) {
	
	/*
	* Do not report a lead byte as a key pressed.
	*/
	Display display = getDisplay ();
	if (!OS.IsUnicode && OS.IsDBLocale) {
		byte lead = (byte) (wParam & 0xFF);
		if (OS.IsDBCSLeadByte (lead)) {
			display.lastAscii = display.lastKey = 0;
			display.lastVirtual = false;
			return null;
		}
	}
	
	/* Ignore repeating modifier keys by testing key down state */
	if ((wParam == OS.VK_SHIFT) || (wParam == OS.VK_MENU) ||
		(wParam == OS.VK_CONTROL) || (wParam == OS.VK_CAPITAL) ||
		(wParam == OS.VK_NUMLOCK) || (wParam == OS.VK_SCROLL)) {
			if ((lParam & 0x40000000) != 0) return null;
		}

	/* Set last key and clear last ascii because a new key has been typed */
	display.lastAscii = 0;
	display.lastKey = wParam;

	/* Map the virtual key */
	int mapKey = OS.MapVirtualKey (display.lastKey, 2);

	/*
	* Bug in Windows 95 and NT.  When the user types an accent key such
	* as ^ to get an accented character on a German keyboard, the accent
	* key should be ignored and the next key that the user types is the
	* accented key.  On Windows 95 and NT, a call to ToAscii (), clears the
	* accented state such that the next WM_CHAR loses the accent.  The fix
	* is to detect the accent key stroke (called a dead key) by testing the
	* high bit of the value returned by MapVirtualKey ().  A further problem
	* is that the high bit on Windows NT is bit 32 while the high bit on
	* Windows 95 is bit 16.  They should both be bit 32.
	*/
	if (OS.IsWinNT) {
		if ((mapKey & 0x80000000) != 0) return null;
	} else {
		if ((mapKey & 0x8000) != 0) return null;
	}
	
	/*
	* If we are going to get a WM_CHAR, ensure that last key has
	* the correct character value for the key down and key up
	* events.  It is not sufficient to ignore the WM_KEYDOWN
	* (when we know we are going to get a WM_CHAR) and compute
	* the key in WM_CHAR because there is not enough information
	* by the time we get the WM_CHAR.  For example, when the user
	* types Ctrl+Shift+6 on a US keyboard, we get a WM_CHAR with 
	* wParam=30.  When the user types Ctrl+Shift+6 on a German 
	* keyboard, we also get a WM_CHAR with wParam=30.  On the US
	* keyboard Shift+6 is ^, on the German keyboard Shift+6 is &.
	* There is no way to map wParam=30 in WM_CHAR to the correct
	* value.  Also, on international keyboards, the control key
	* may be down when the user has not entered a control character.
	*/
	display.lastVirtual = (mapKey == 0);
	if (display.lastVirtual) {
		/*
		* Feature in Windows.  The virtual key VK_DELETE is not
		* treated as both a virtual key and an ASCII key by Windows.
		* Therefore, we will not receive a WM_CHAR for this key.
		* The fix is to treat VK_DELETE as a special case and map
		* the ASCII value explictly (Delete is 127).
		*/
		if (display.lastKey == OS.VK_DELETE) display.lastAscii = 127;
		/*
		* It is possible to get a WM_CHAR for a virtual key when
		* Num Lock is on.  If the user types Home while Num Lock 
		* is down, a WM_CHAR is issued with WPARM=55 (for the
		* character 7).  If we are going to get a WM_CHAR we need
		* to ensure that the last key has the correct value.  Note
		* that Ctrl+Home does not issue a WM_CHAR when Num Lock is
		* down.
		*/
		if (OS.VK_NUMPAD0 <= display.lastKey && display.lastKey <= OS.VK_DIVIDE) {
			if (display.asciiKey (display.lastKey) != 0) return null;
		}
	} else {
		/*
		* Get the shifted state or convert to lower case if necessary.
		* If the user types Ctrl+A, LastKey should be $a, not $A.  If
		* the user types Ctrl+Shift+A, LastKey should be $A.  If the user 
		* types Ctrl+Shift+6, the value of LastKey will depend on the 
		* international keyboard.
		*/
	 	if (OS.GetKeyState (OS.VK_SHIFT) < 0) {
			display.lastKey = display.shiftedKey (display.lastKey);
			if (display.lastKey == 0) display.lastKey = wParam;
	 	} else {
	 		display.lastKey = OS.CharLower ((short) mapKey);
	 	}
		/*
		* Some key combinations map to Windows ASCII keys depending
		* on the keyboard.  For example, Ctrl+Alt+Q maps to @ on a
		* German keyboard.  If the current key combination is special,
		* the correct character is placed in wParam for processing in
		* WM_CHAR.  If this is the case, issue the key down event from
		* inside WM_CHAR.
		*/
		int newKey = display.asciiKey (wParam);
		if (newKey != 0) {
			/*
			* When the user types Ctrl+Space, ToAscii () maps this to
			* Space.  Normally, ToAscii () maps a key to a different
			* key if both a WM_KEYDOWN and a WM_CHAR will be issued.
			* To avoid the extra OSxKeyDown, look for VK_SPACE and
			* issue the event from WM_CHAR.
			*/
			if (newKey == OS.VK_SPACE) {
				display.lastVirtual = true;
				return null;
			}
			if (newKey != wParam) return null;
		}
			
		/*
		* If the control key is not down at this point, then
		* the key that was pressed was an accent key.  In that
		* case, do not issue the key down event.
		*/
		if (OS.GetKeyState (OS.VK_CONTROL) >= 0) {
			display.lastKey = 0;
			return null;
		}
		
		/*
		* Virtual keys such as VK_RETURN are both virtual and ASCII keys.
		* Normally, these are marked virtual in WM_CHAR.  Since we will not
		* be getting a WM_CHAR for the key at this point, we need to test LastKey 
		* to see if it is virtual.  This happens when the user types Ctrl+Tab.
		*/
		display.lastVirtual = display.isVirtualKey (display.lastKey);
		display.lastAscii = display.controlKey (display.lastKey);
	}
	if (!sendKeyEvent (SWT.KeyDown, OS.WM_KEYDOWN, wParam, lParam)) {
		return LRESULT.ZERO;
	}
	return null;
}

LRESULT WM_KEYUP (int wParam, int lParam) {
	Display display = getDisplay ();
	
	/* Check for hardware keys */
	if (OS.IsWinCE) {
		if (OS.VK_APP1 <= wParam && wParam <= OS.VK_APP6) {
			display.lastVirtual = false;
			display.lastKey = display.lastAscii = 0;
			Event event = new Event ();
			event.detail = wParam - OS.VK_APP1 + 1;
			/* Check the bit 30 to get the key state */
			int type = (lParam & 0x40000000) != 0 ? SWT.HardKeyUp : SWT.HardKeyDown;
			if (setInputState (event, type)) sendEvent (type, event);
			return null;
		}
	}
	
	/*
	* If the key up is not hooked, reset last key
	* and last ascii in case the key down is hooked.
	*/
	if (!hooks (SWT.KeyUp)) {
		display.lastVirtual = false;
		display.lastKey = display.lastAscii = 0;
		return null;
	}
	
	/* Map the virtual key. */
	int mapKey = OS.MapVirtualKey (wParam, 2);

	/*
	* Bug in Windows 95 and NT.  When the user types an accent key such
	* as ^ to get an accented character on a German keyboard, the accent
	* key should be ignored and the next key that the user types is the
	* accented key.  On Windows 95 and NT, a call to ToAscii(), clears the
	* accented state such that the next WM_CHAR loses the accent.  The fix
	* is to detect the accent key stroke (called a dead key) by testing the
	* high bit of the value returned by MapVirtualKey().  A further problem
	* is that the high bit on Windows NT is bit 32 while the high bit on
	* Windows 95 is bit 16.  They should both be bit 32.
	*/
	if (OS.IsWinNT) {
		if ((mapKey & 0x80000000) != 0) return null;
	} else {
		if ((mapKey & 0x8000) != 0) return null;
	}
		
	display.lastVirtual = (mapKey == 0);
	if (display.lastVirtual) {
		display.lastKey = wParam;
	} else {
		if (display.lastKey == 0) {
			display.lastAscii = 0;
			return null;
		}
		display.lastVirtual = display.isVirtualKey (display.lastKey);
	}
	
	LRESULT result = null;
	if (!sendKeyEvent (SWT.KeyUp, OS.WM_KEYUP, wParam, lParam)) {
		result = LRESULT.ZERO;
	}
	display.lastVirtual = false;
	display.lastKey = display.lastAscii = 0;
	return result;
}

LRESULT WM_KILLFOCUS (int wParam, int lParam) {
	int code = callWindowProc (OS.WM_KILLFOCUS, wParam, lParam);
	Display display = getDisplay ();
	Shell shell = getShell ();
	
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the focus
	* out event.  If this happens keep going to send
	* the deactivate events.
	*/
	sendEvent (SWT.FocusOut);
	// widget could be disposed at this point
	
	/*
	* It is possible that the shell may be
	* disposed at this point.  If this happens
	* don't send the activate and deactivate
	* events.
	*/	
	if (!shell.isDisposed ()) {
		Control control = display.findControl (wParam);
		if (control == null || shell != control.getShell ()) {
			shell.setActiveControl (null);
		}
	}
	
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the focus
	* or deactivate events.  If this happens, end the
	* processing of the Windows message by returning
	* zero as the result of the window proc.
	*/
	if (isDisposed ()) return LRESULT.ZERO;
	if (code == 0) return LRESULT.ZERO;
	return new LRESULT (code);
}

LRESULT WM_LBUTTONDBLCLK (int wParam, int lParam) {
	/*
	* Feature in Windows. Windows sends the following
	* messages when the user double clicks the mouse:
	*
	*	WM_LBUTTONDOWN		- mouse down
	*	WM_LBUTTONUP		- mouse up
	*	WM_LBUTTONDBLCLK	- double click
	*	WM_LBUTTONUP		- mouse up
	*
	* Applications that expect matching mouse down/up
	* pairs will not see the second mouse down.  The
	* fix is to send a mouse down event.
	*/
	sendMouseEvent (SWT.MouseDown, 1, OS.WM_LBUTTONDOWN, wParam, lParam);
	sendMouseEvent (SWT.MouseDoubleClick, 1, OS.WM_LBUTTONDBLCLK, wParam, lParam);
	int result = callWindowProc (OS.WM_LBUTTONDBLCLK, wParam, lParam);
	if (OS.GetCapture () != handle) OS.SetCapture (handle);
	return new LRESULT (result);
}

LRESULT WM_LBUTTONDOWN (int wParam, int lParam) {
	sendMouseEvent (SWT.MouseDown, 1, OS.WM_LBUTTONDOWN, wParam, lParam);
	int result = callWindowProc (OS.WM_LBUTTONDOWN, wParam, lParam);
	if (OS.GetCapture () != handle) OS.SetCapture (handle);
	if (hooks (SWT.DragDetect)) {
		POINT pt = new POINT ();
		pt.x = (short) (lParam & 0xFFFF);
		pt.y = (short) (lParam >> 16);
		if (!OS.IsWinCE) {
			/*
			* The DragDetect function captures the mouse and tracks its movement until the user releases
			* the left button, presses the ESC key, or moves the mouse outside the drag rectangle around 
			* the specified point.   If the user moves the mouse outside of the drag rectangle, DragDetect
			* returns true.
			*/
			if (OS.DragDetect (handle, pt)) {
				sendEvent (SWT.DragDetect);
				// widget could be disposed at this point
			} else {
				/*
				* The Mouse up event and the ESC key event have been consumed by DragDetect so 
				* detect the cases and send the events.
				*/
				if (OS.GetKeyState (OS.VK_ESCAPE) >= 0) {
					sendMouseEvent (SWT.MouseUp, 1, OS.WM_LBUTTONUP, wParam, lParam);
					// widget could be disposed at this point
				}
			}
		}
	}
	return new LRESULT (result);
}

LRESULT WM_LBUTTONUP (int wParam, int lParam) {
	sendMouseEvent (SWT.MouseUp, 1, OS.WM_LBUTTONUP, wParam, lParam);
	int result = callWindowProc (OS.WM_LBUTTONUP, wParam, lParam);
	if ((wParam & (OS.MK_LBUTTON | OS.MK_MBUTTON | OS.MK_RBUTTON)) == 0)
		if (OS.GetCapture () == handle) OS.ReleaseCapture ();
	return new LRESULT (result);
}

LRESULT WM_MBUTTONDBLCLK (int wParam, int lParam) {
	/*
	* Feature in Windows. Windows sends the following
	* messages when the user double clicks the mouse:
	*
	*	WM_MBUTTONDOWN		- mouse down
	*	WM_MBUTTONUP		- mouse up
	*	WM_MLBUTTONDBLCLK	- double click
	*	WM_MBUTTONUP		- mouse up
	*
	* Applications that expect matching mouse down/up
	* pairs will not see the second mouse down.  The
	* fix is to send a mouse down event.
	*/
	sendMouseEvent (SWT.MouseDown, 2, OS.WM_MBUTTONDOWN, wParam, lParam);
	sendMouseEvent (SWT.MouseDoubleClick, 2, OS.WM_MBUTTONDBLCLK, wParam, lParam);
	int result = callWindowProc (OS.WM_MBUTTONDBLCLK, wParam, lParam);
	if (OS.GetCapture () != handle) OS.SetCapture (handle);
	return new LRESULT (result);
}

LRESULT WM_MBUTTONDOWN (int wParam, int lParam) {
	sendMouseEvent (SWT.MouseDown, 2, OS.WM_MBUTTONDOWN, wParam, lParam);
	int result = callWindowProc (OS.WM_MBUTTONDOWN, wParam, lParam);
	if (OS.GetCapture () != handle) OS.SetCapture(handle);
	return new LRESULT (result);
}

LRESULT WM_MBUTTONUP (int wParam, int lParam) {
	sendMouseEvent (SWT.MouseUp, 2, OS.WM_MBUTTONUP, wParam, lParam);
	int result = callWindowProc (OS.WM_MBUTTONUP, wParam, lParam);
	if ((wParam & (OS.MK_LBUTTON | OS.MK_MBUTTON | OS.MK_RBUTTON)) == 0)
		if (OS.GetCapture () == handle) OS.ReleaseCapture ();
	return new LRESULT (result);
}

LRESULT WM_MEASUREITEM (int wParam, int lParam) {
	MEASUREITEMSTRUCT struct = new MEASUREITEMSTRUCT ();
	OS.MoveMemory (struct, lParam, MEASUREITEMSTRUCT.sizeof);
	if (struct.CtlType == OS.ODT_MENU) {
		Decorations shell = menuShell ();
		MenuItem item = shell.findMenuItem (struct.itemID);
		if (item == null) return null;
		return item.wmMeasureChild (wParam, lParam);
	}
	int hwnd = OS.GetDlgItem (handle, struct.CtlID);
	Control control = WidgetTable.get (hwnd);
	if (control == null) return null;
	return control.wmMeasureChild (wParam, lParam);
}

LRESULT WM_MENUCHAR (int wParam, int lParam) {
	Display display = getDisplay ();
	display.mnemonicKeyHit = false;
	/*
	* Feature in Windows.  When the user types Alt+<key>
	* and <key> does not match a mnemonic in the System
	* menu or the menu bar, Windows beeps.  This beep is
	* unexpected and unwanted by applications that look
	* for Alt+<key>.  The fix is to detect the case and
	* stop Windows from beeping by closing the menu.
	*/
	int type = wParam >> 16;
	if (type == 0 || type == OS.MF_SYSMENU) {
		return new LRESULT (OS.MNC_CLOSE << 16);
	}
	return null;
}

LRESULT WM_MENUSELECT (int wParam, int lParam) {
	int code = wParam >> 16;
	Shell shell = getShell ();
	if (code == -1 && lParam == 0) {
		Menu menu = shell.activeMenu;
		while (menu != null) {
			/*
			* It is possible (but unlikely), that application
			* code could have disposed the widget in the hide
			* event.  If this happens, stop searching up the
			* parent list because there is no longer a link
			* to follow.
			*/
			menu.sendEvent (SWT.Hide);
			if (menu.isDisposed ()) break;
			menu = menu.getParentMenu ();
		}
		/*
		* The shell may be disposed because of sending the hide
		* event to the last active menu menu but setting a field
		* to null in a destroyed widget is not harmful.
		*/
		shell.activeMenu = null;
		return null;
	}
	if ((code & OS.MF_SYSMENU) != 0) return null;
	if ((code & OS.MF_HILITE) != 0) {
		MenuItem item = null;
		Decorations menuShell = menuShell ();
		if ((code & OS.MF_POPUP) != 0) {
			int index = wParam & 0xFFFF;
			MENUITEMINFO info = new MENUITEMINFO ();
			info.cbSize = MENUITEMINFO.sizeof;
			info.fMask = OS.MIIM_SUBMENU;
			if (OS.GetMenuItemInfo (lParam, index, true, info)) {
				Menu newMenu = menuShell.findMenu (info.hSubMenu);
				if (newMenu != null) item = newMenu.cascade;
			}	
		} else {
			Menu newMenu = menuShell.findMenu (lParam);
			if (newMenu != null) {
				int id = wParam & 0xFFFF;
				item = menuShell.findMenuItem (id);
			}
			Menu oldMenu = shell.activeMenu;
			if (oldMenu != null) {
				Menu ancestor = oldMenu;
				while (ancestor != null && ancestor != newMenu) {
					ancestor = ancestor.getParentMenu ();
				}
				if (ancestor == newMenu) {
					ancestor = oldMenu;
					while (ancestor != newMenu) {
						/*
						* It is possible (but unlikely), that application
						* code could have disposed the widget in the hide
						* event or the item about to be armed.  If this
						* happens, stop searching up the ancestor list
						* because there is no longer a link to follow.
						*/
						ancestor.sendEvent (SWT.Hide);
						if (ancestor.isDisposed ()) break;
						ancestor = ancestor.getParentMenu ();
					}
					/*
					* The shell and/or the item could be disposed when
					* processing hide events from above.  If this happens,
					* ensure that the shell is not accessed and that no
					* arm event is sent to the item.
					*/
					if (!shell.isDisposed ()) {
						if (newMenu != null && newMenu.isDisposed ()) {
							newMenu = null;
						}
						shell.activeMenu = newMenu;
					}
					if (item != null && item.isDisposed ()) item = null;
				}
			}
		}
		if (item != null) item.sendEvent (SWT.Arm);
	}
	return null;
}

LRESULT WM_MOUSEACTIVATE (int wParam, int lParam) {
	return null;
}

LRESULT WM_MOUSEHOVER (int wParam, int lParam) {
	int pos = OS.GetMessagePos ();
	Event event = new Event ();
	POINT pt = new POINT ();
	pt.x = (short) (pos & 0xFFFF);
	pt.y = (short) (pos >> 16); 
	OS.ScreenToClient (handle, pt);
	event.x = pt.x;
	event.y = pt.y;
	postEvent (SWT.MouseHover, event);
	return null;
}

LRESULT WM_MOUSELEAVE (int wParam, int lParam) {
	int pos = OS.GetMessagePos ();
	Event event = new Event ();
	POINT pt = new POINT ();
	pt.x = (short) (pos & 0xFFFF);
	pt.y = (short) (pos >> 16); 
	OS.ScreenToClient (handle, pt);
	event.x = pt.x;
	event.y = pt.y;
	postEvent (SWT.MouseExit, event);
	return null;
}

LRESULT WM_MOUSEMOVE (int wParam, int lParam) {
	if (!OS.IsWinCE) {
		boolean hooksEnter = hooks (SWT.MouseEnter);
		if (hooksEnter || hooks (SWT.MouseExit) || hooks (SWT.MouseHover)) {
			TRACKMOUSEEVENT lpEventTrack = new TRACKMOUSEEVENT ();
			lpEventTrack.cbSize = TRACKMOUSEEVENT.sizeof;
			lpEventTrack.dwFlags = OS.TME_QUERY;
			lpEventTrack.hwndTrack = handle;
			OS.TrackMouseEvent (lpEventTrack);
			if (lpEventTrack.dwFlags == 0) {
				lpEventTrack.dwFlags = OS.TME_LEAVE | OS.TME_HOVER;
				lpEventTrack.hwndTrack = handle;
				OS.TrackMouseEvent (lpEventTrack);
				if (hooksEnter) {
					Event event = new Event ();
					event.x = (short) (lParam & 0xFFFF);
					event.y = (short) (lParam >> 16);
					postEvent (SWT.MouseEnter, event);
				}
			} else {
				lpEventTrack.dwFlags = OS.TME_HOVER;
				OS.TrackMouseEvent (lpEventTrack);
			}
		}
	}
	Display display = getDisplay ();
	int pos = OS.GetMessagePos ();
	if (pos != display.lastMouse) {
		display.lastMouse = pos;
		sendMouseEvent (SWT.MouseMove, 0, OS.WM_MOUSEMOVE, wParam, lParam);
	}
	return null;
}

LRESULT WM_MOUSEWHEEL (int wParam, int lParam) {
	return null;
}

LRESULT WM_MOVE (int wParam, int lParam) {
	sendEvent (SWT.Move);
	// widget could be disposed at this point
	return null;
}

LRESULT WM_NCACTIVATE (int wParam, int lParam) {
	return null;
}

LRESULT WM_NCCALCSIZE (int wParam, int lParam) {
	return null;
}

LRESULT WM_NCHITTEST (int wParam, int lParam) {
	if (!isActive ()) return new LRESULT (OS.HTTRANSPARENT);
	return null;
}

LRESULT WM_NOTIFY (int wParam, int lParam) {
	NMHDR hdr = new NMHDR ();
	OS.MoveMemory (hdr, lParam, NMHDR.sizeof);
	int hwnd = hdr.hwndFrom;
	if (hwnd == 0) return null;
	Control control = WidgetTable.get (hwnd);
	if (control == null) return null;
	return control.wmNotifyChild (wParam, lParam);
}

LRESULT WM_PAINT (int wParam, int lParam) {

	/* Exit early - don't draw the background */
	if (!hooks (SWT.Paint)) return null;

	/* Get the damage */
	int rgn = 0;
	rgn = OS.CreateRectRgn (0, 0, 0, 0);
	OS.GetUpdateRgn (handle, rgn, false);
	int result = callWindowProc (OS.WM_PAINT, wParam, lParam);
	if (OS.IsWinCE) {
		RECT rect = new RECT ();
		OS.GetClipBox (rgn, rect);
		OS.InvalidateRect (handle, rect, false);
	} else {
		OS.InvalidateRgn (handle, rgn, false);
	}
	OS.DeleteObject (rgn);

	/* Create the paint GC */
	PAINTSTRUCT ps = new PAINTSTRUCT ();
	GCData data = new GCData ();
	data.ps = ps;
	GC gc = GC.win32_new (this, data);
	
	/* Send the paint event */
	Event event = new Event ();
	event.gc = gc;
	event.x = ps.left;
	event.y = ps.top;
	event.width = ps.right - ps.left;
	event.height = ps.bottom - ps.top;
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the paint
	* event.  If this happens, attempt to give back the
	* paint GC anyways because this is a scarce Windows
	* resource.
	*/
	sendEvent (SWT.Paint, event);
	// widget could be disposed at this point	

	/* Dispose the paint GC	*/
	event.gc = null;
	gc.dispose ();
	
	if (result == 0) return LRESULT.ZERO;
	return new LRESULT (result);
}

LRESULT WM_PALETTECHANGED (int wParam, int lParam) {
	return null;
}

LRESULT WM_PASTE (int wParam, int lParam) {
	return null;
}

LRESULT WM_QUERYNEWPALETTE (int wParam, int lParam) {
	return null;
}

LRESULT WM_QUERYOPEN (int wParam, int lParam) {
	return null;
}

LRESULT WM_RBUTTONDBLCLK (int wParam, int lParam) {
	/*
	* Feature in Windows. Windows sends the following
	* messages when the user double clicks the mouse:
	*
	*	WM_RBUTTONDOWN		- mouse down
	*	WM_RBUTTONUP		- mouse up
	*	WM_RBUTTONDBLCLK	- double click
	*	WM_LBUTTONUP		- mouse up
	*
	* Applications that expect matching mouse down/up
	* pairs will not see the second mouse down.  The
	* fix is to send a mouse down event.
	*/
	sendMouseEvent (SWT.MouseDown, 3, OS.WM_RBUTTONDOWN, wParam, lParam);
	sendMouseEvent (SWT.MouseDoubleClick, 3, OS.WM_RBUTTONDBLCLK, wParam, lParam);
	int result = callWindowProc (OS.WM_RBUTTONDBLCLK, wParam, lParam);
	if (OS.GetCapture () != handle) OS.SetCapture (handle);
	return new LRESULT (result);
}

LRESULT WM_RBUTTONDOWN (int wParam, int lParam) {
	sendMouseEvent (SWT.MouseDown, 3, OS.WM_RBUTTONDOWN, wParam, lParam);
	int result = callWindowProc (OS.WM_RBUTTONDOWN, wParam, lParam);
	if (OS.GetCapture () != handle) OS.SetCapture (handle);
	return new LRESULT (result);
}

LRESULT WM_RBUTTONUP (int wParam, int lParam) {
	sendMouseEvent (SWT.MouseUp, 3, OS.WM_RBUTTONUP, wParam, lParam);
	int result = callWindowProc (OS.WM_RBUTTONUP, wParam, lParam);
	if ((wParam & (OS.MK_LBUTTON | OS.MK_MBUTTON | OS.MK_RBUTTON)) == 0)
		if (OS.GetCapture () == handle) OS.ReleaseCapture ();
	return new LRESULT (result);
}

LRESULT WM_SETCURSOR (int wParam, int lParam) {
	int hitTest = lParam & 0xFFFF;
 	if (hitTest == OS.HTCLIENT) {
		Control control = WidgetTable.get (wParam);
		if (control == null) return null;
		int hCursor = control.findCursor ();
		if (hCursor != 0) {
			OS.SetCursor (hCursor);
			return LRESULT.ONE;
		}
	}
	return null;
}

LRESULT WM_SETFOCUS (int wParam, int lParam) {
	int code = callWindowProc (OS.WM_SETFOCUS, wParam, lParam);
	Shell shell = getShell ();
	
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the focus
	* in event.  If this happens keep going to send
	* the activate events.
	*/
	sendEvent (SWT.FocusIn);
	// widget could be disposed at this point
	
	/*
	* It is possible that the shell may be
	* disposed at this point.  If this happens
	* don't send the activate and deactivate
	* events.
	*/	
	if (!shell.isDisposed ()) {
		shell.setActiveControl (this);
	}

	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the focus
	* or activate events.  If this happens, end the
	* processing of the Windows message by returning
	* zero as the result of the window proc.
	*/
	if (isDisposed ()) return LRESULT.ZERO;
	if (code == 0) return LRESULT.ZERO;
	return new LRESULT (code);
}

LRESULT WM_SETTINGCHANGE (int wParam, int lParam) {
	return null;
}

LRESULT WM_SETFONT (int wParam, int lParam) {
	return null;
}

LRESULT WM_SHOWWINDOW (int wParam, int lParam) {
	return null;
}

LRESULT WM_SIZE (int wParam, int lParam) {
	sendEvent (SWT.Resize);
	// widget could be disposed at this point
	return null;
}

LRESULT WM_SYSCHAR (int wParam, int lParam) {
	Display display = getDisplay ();

	/* Set last key and last ascii because a new key has been typed */
	display.lastAscii = display.lastKey = wParam;
	display.lastVirtual = display.isVirtualKey (display.lastKey);

	/* Do not issue a key down if a menu bar mnemonic was invoked */
	if (!hooks (SWT.KeyDown)) return null;
	display.mnemonicKeyHit = true;
	int result = callWindowProc (OS.WM_SYSCHAR, wParam, lParam);
	if (!display.mnemonicKeyHit) {
		sendKeyEvent (SWT.KeyDown, OS.WM_SYSCHAR, wParam, lParam);
	}
	display.mnemonicKeyHit = false;
	return new LRESULT (result);
}

LRESULT WM_SYSCOLORCHANGE (int wParam, int lParam) {
	return null;
}

LRESULT WM_SYSCOMMAND (int wParam, int lParam) {
	/*
	* Check to see if the command is a system command or
	* a user menu item that was added to the System menu.
	* When a user item is added to the System menu,
	* WM_SYSCOMMAND must always return zero.
	*/
	if ((wParam & 0xF000) == 0) {
		Decorations shell = menuShell ();
		if (shell.isEnabled ()) {
			MenuItem item = shell.findMenuItem (wParam & 0xFFFF);
			if (item != null) item.wmCommandChild (wParam, lParam);
		}
		return LRESULT.ZERO;
	}

	/* Process the System Command */
	int cmd = wParam & 0xFFF0;
	switch (cmd) {
		case OS.SC_CLOSE:
			int hwndShell = menuShell ().handle;
			int bits = OS.GetWindowLong (hwndShell, OS.GWL_STYLE);
			if ((bits & OS.WS_SYSMENU) == 0) return LRESULT.ZERO;
			break;
		case OS.SC_KEYMENU:
		case OS.SC_HSCROLL:
		case OS.SC_VSCROLL:
			/*
			* Do not allow keyboard traversal of the menu bar
			* or scrolling when the shell is not enabled.
			*/
			if (!menuShell ().isEnabled ()) return LRESULT.ZERO;
			break;
		case OS.SC_MINIMIZE:
			/* Save the focus widget when the shell is minimized */
			menuShell ().saveFocus ();
			break;
	}
	return null;
}

LRESULT WM_SYSKEYDOWN (int wParam, int lParam) {
	Display display = getDisplay ();

	/*
	* Feature in Windows.  WM_SYSKEYDOWN is sent when
	* the user presses ALT-<aKey> or F10 without the ALT key.
	* In order to issue events for F10 (without the ALT key)
	* but ignore all other key presses without the ALT key,
	* make F10 a special case.
	*/
	if (wParam != OS.VK_F10) {
		/* Make sure WM_SYSKEYDOWN was sent by ALT-<aKey>. */
		if ((lParam & 0x20000000) == 0) return null;
	}

	/* If are going to get a WM_SYSCHAR, ignore this message. */
	if (OS.MapVirtualKey (wParam, 2) != 0) return null;
	
	/* Ignore repeating keys for modifiers by testing key down state. */
	if ((wParam == OS.VK_SHIFT) || (wParam == OS.VK_MENU) ||
		(wParam == OS.VK_CONTROL) || (wParam == OS.VK_CAPITAL) ||
		(wParam == OS.VK_NUMLOCK) || (wParam == OS.VK_SCROLL))
			if ((lParam & 0x40000000) != 0) return null;

	/* Set last key and clear last ascii because a new key has been typed. */
	display.lastAscii = 0;
	display.lastKey = wParam;
	display.lastVirtual = true;

	if (!sendKeyEvent (SWT.KeyDown, OS.WM_SYSKEYDOWN, wParam, lParam)) {
		return LRESULT.ZERO;
	}
	return null;
}

LRESULT WM_SYSKEYUP (int wParam, int lParam) {
	return WM_KEYUP (wParam, lParam);
}

LRESULT WM_TIMER (int wParam, int lParam) {
	return null;
}

LRESULT WM_UNDO (int wParam, int lParam) {
	return null;
}

LRESULT WM_VSCROLL (int wParam, int lParam) {
	if (lParam == 0) return null;
	Control control = WidgetTable.get (lParam);
	if (control == null) return null;
	return control.wmScrollChild (wParam, lParam);
}

LRESULT WM_WINDOWPOSCHANGING (int wParam, int lParam) {
	return null;
}

LRESULT wmColorChild (int wParam, int lParam) {
	if (background == -1 && foreground == -1) return null;
	int forePixel = foreground, backPixel = background;
	if (forePixel == -1) forePixel = defaultForeground ();
	if (backPixel == -1) backPixel = defaultBackground ();
	OS.SetTextColor (wParam, forePixel);
	OS.SetBkColor (wParam, backPixel);
	return new LRESULT (findBrush (backPixel));
}

LRESULT wmCommandChild (int wParam, int lParam) {
	return null;
}

LRESULT wmDrawChild (int wParam, int lParam) {
	return null;
}

LRESULT wmMeasureChild (int wParam, int lParam) {
	return null;
}

LRESULT wmNotifyChild (int wParam, int lParam) {
	return null;
}

LRESULT wmScrollChild (int wParam, int lParam) {
	return null;
}

}

