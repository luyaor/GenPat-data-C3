package org.eclipse.swt.widgets;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */
 
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;

/**
 * Instances of the receiver represent a selectable user interface object
 * that allows the user to drag a rubber banded outline of the sash within
 * the parent control.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd> HORIZONTAL, VERTICAL</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
public class Sash extends Control {
	boolean dragging;
	int startX, startY, lastX, lastY;

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
public Sash (Composite parent, int style) {
	super (parent, checkStyle (style));
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control is selected, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * When <code>widgetSelected</code> is called, the x, y, width, and height fields of the event object are valid.
 * If the reciever is being dragged, the event object detail field contains the value <code>SWT.DRAG</code>.
 * <code>widgetDefaultSelected</code> is not called.
 * </p>
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
 * @see SelectionListener
 * @see #removeSelectionListener
 * @see SelectionEvent
 */
public void addSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection,typedListener);
	addListener (SWT.DefaultSelection,typedListener);
}

int callWindowProc (int msg, int wParam, int lParam) {
	if (handle == 0) return 0;
	return OS.DefWindowProc (handle, msg, wParam, lParam);
}

static int checkStyle (int style) {
	return checkBits (style, SWT.HORIZONTAL, SWT.VERTICAL, 0, 0, 0, 0);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int border = getBorderWidth ();
	int width = border * 2, height = border * 2;
	if ((style & SWT.HORIZONTAL) != 0) {
		width += DEFAULT_WIDTH;  height += 3;
	} else {
		width += 3; height += DEFAULT_HEIGHT;
	}
	if (wHint != SWT.DEFAULT) width = wHint + (border * 2);
	if (hHint != SWT.DEFAULT) height = hHint + (border * 2);
	return new Point (width, height);
}

void drawBand (int x, int y, int width, int height) {
	int hwndTrack = parent.handle;
	byte [] bits = {-86, 0, 85, 0, -86, 0, 85, 0, -86, 0, 85, 0, -86, 0, 85, 0};
	int stippleBitmap = OS.CreateBitmap (8, 8, 1, 1, bits);
	int stippleBrush = OS.CreatePatternBrush (stippleBitmap);
	int hDC = OS.GetDCEx (hwndTrack, 0, OS.DCX_CACHE);
	int oldBrush = OS.SelectObject (hDC, stippleBrush);
	OS.PatBlt (hDC, x, y, width, height, OS.PATINVERT);
	OS.SelectObject (hDC, oldBrush);
	OS.ReleaseDC (hwndTrack, hDC);
	OS.DeleteObject (stippleBrush);
	OS.DeleteObject (stippleBitmap);
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is selected.
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
 * @see SelectionListener
 * @see #addSelectionListener
 */
public void removeSelectionListener(SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection,listener);	
}

byte [] windowClass () {
	return getDisplay ().windowClass;
}

int windowProc () {
	return getDisplay ().windowProc;
}

LRESULT WM_ERASEBKGND (int wParam, int lParam) {
	LRESULT result = super.WM_ERASEBKGND (wParam, lParam);
	if (result != null) return result;
	RECT rect = new RECT ();
	OS.GetClientRect (handle, rect);
	int pixel = getBackgroundPixel ();
	int hBrush = findBrush (pixel);
	OS.FillRect (wParam, rect, hBrush);
	return LRESULT.ONE;
}

LRESULT WM_GETDLGCODE (int wParam, int lParam) {
	return new LRESULT (OS.DLGC_STATIC);
}

LRESULT WM_LBUTTONDOWN (int wParam, int lParam) {
	LRESULT result = super.WM_LBUTTONDOWN (wParam, lParam);

	/* Compute the banding rectangle */
	int hwndTrack = parent.handle;
	POINT pt = new POINT ();
	pt.x = (short) (lParam & 0xFFFF);
	pt.y = (short) (lParam >> 16);
	RECT rect = new RECT ();
	OS.GetWindowRect (handle, rect);
	OS.MapWindowPoints (handle, 0, pt, 1);
	startX = pt.x - rect.left;
	startY = pt.y - rect.top;
	OS.MapWindowPoints (0, hwndTrack, rect, 2);
	lastX = rect.left;  lastY = rect.top;
	int width = rect.right - rect.left;
	int height = rect.bottom - rect.top;

	/* The event must be sent because doit flag is used */
	Event event = new Event ();
	event.x = lastX;  event.y = lastY;
	event.width = width;  event.height = height;
	event.detail = SWT.DRAG;
	
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the selection
	* event.  If this happens, end the processing of the
	* Windows message by returning zero as the result of
	* the window proc.
	*/
	sendEvent (SWT.Selection, event);
	if (isDisposed ()) return LRESULT.ZERO;
	
	/* Draw the banding rectangle */
	if (event.doit) {
		dragging = true;
		menuShell ().bringToTop ();
		int flags = OS.RDW_UPDATENOW | OS.RDW_ALLCHILDREN;
		OS.RedrawWindow (hwndTrack, null, 0, flags);
		drawBand (lastX = event.x, lastY = event.y, width, height);
	}
	return result;
}

LRESULT WM_LBUTTONUP (int wParam, int lParam) {
	LRESULT result = super.WM_LBUTTONUP (wParam, lParam);

	/* Compute the banding rectangle */
	if (!dragging) return result;
	dragging = false;
	RECT rect = new RECT ();
	OS.GetWindowRect (handle, rect);
	int width = rect.right - rect.left;
	int height = rect.bottom - rect.top;
	
	/* The event must be sent because doit flag is used */
	Event event = new Event ();
	event.x = lastX;  event.y = lastY;
	event.width = width;  event.height = height;
	drawBand (lastX, lastY, width, height);
	sendEvent (SWT.Selection, event);
	// widget could be disposed at this point
	return result;
}

LRESULT WM_MOUSEMOVE (int wParam, int lParam) {
	LRESULT result = super.WM_MOUSEMOVE (wParam, lParam);
	if (result != null) return result;
	if (!dragging || ((wParam & OS.MK_LBUTTON) == 0)) return result;

	/* Compute the banding rectangle */
	POINT pt = new POINT ();
	pt.x = (short) (lParam & 0xFFFF);
	pt.y = (short) (lParam >> 16);
	int hwndTrack = parent.handle;
	OS.MapWindowPoints (handle, hwndTrack, pt, 1);
	RECT rect = new RECT (), clientRect = new RECT ();
	OS.GetWindowRect (handle, rect);
	int width = rect.right - rect.left;
	int height = rect.bottom - rect.top;
	OS.GetClientRect (hwndTrack, clientRect);
	int clientWidth = clientRect.right - clientRect.left;
	int clientHeight = clientRect.bottom - clientRect.top;
	int newX = lastX, newY = lastY;
	if ((style & SWT.VERTICAL) != 0) {
		newX = Math.min (Math.max (0, pt.x - startX), clientWidth - width);
	} else {
		newY = Math.min (Math.max (0, pt.y - startY), clientHeight - height);
	}
	if ((newX == lastX) && (newY == lastY)) return result;
	drawBand (lastX, lastY, width, height);

	/* The event must be sent because doit flag is used */
	Event event = new Event ();
	event.x = newX;  event.y = newY;
	event.width = width;  event.height = height;
	event.detail = SWT.DRAG;
	
	/*
	* It is possible (but unlikely), that application
	* code could have disposed the widget in the selection
	* event.  If this happens, end the processing of the
	* Windows message by returning zero as the result of
	* the window proc.
	*/
	sendEvent (SWT.Selection, event);
	if (isDisposed ()) return LRESULT.ZERO;

	/* Draw the banding rectangle */
	if (event.doit) {
		lastX = event.x; lastY = event.y;
		int flags = OS.RDW_UPDATENOW | OS.RDW_ALLCHILDREN;
		OS.RedrawWindow (hwndTrack, null, 0, flags);
		drawBand (lastX, lastY, width, height);
	}
	return result;
}

LRESULT WM_SETCURSOR (int wParam, int lParam) {
	LRESULT result = super.WM_SETCURSOR (wParam, lParam);
	if (result != null) return result;
	int hitTest = lParam & 0xFFFF;
 	if (hitTest == OS.HTCLIENT) {
	 	int hCursor;
	 	if ((style & SWT.HORIZONTAL) != 0) {
			hCursor = OS.LoadCursor (0, OS.IDC_SIZENS);
	 	} else {
			hCursor = OS.LoadCursor (0, OS.IDC_SIZEWE);
	 	}
		OS.SetCursor (hCursor);
		return LRESULT.ONE;
	}
	return result;
}

}
