/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;


import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class represent ....
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>DATE, TIME, CALENDAR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
/* UNDER DEVELOPMENT - DO NOT USE */
/*public*/ class DateTime extends Control {
	//TODO - features: short/long format, read-only
	//TODO - missing font, colors, check events
	//TODO - check background image
	static final int DateTimeProc;
	static final TCHAR DateTimeClass = new TCHAR (0, OS.DATETIMEPICK_CLASS, true);
	static final int CalendarProc;
	static final TCHAR CalendarClass = new TCHAR (0, OS.MONTHCAL_CLASS, true);
	static {
		WNDCLASS lpWndClass = new WNDCLASS ();
		OS.GetClassInfo (0, DateTimeClass, lpWndClass);
		DateTimeProc = lpWndClass.lpfnWndProc;
		OS.GetClassInfo (0, CalendarClass, lpWndClass);
		CalendarProc = lpWndClass.lpfnWndProc;
	}
	static final int MARGIN = 4;

/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
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
 * @see SWT#DATE
 * @see SWT#TIME
 * @see SWT#CALENDAR
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public DateTime (Composite parent, int style) {
	super (parent, checkStyle (style));
}

int callWindowProc (int hwnd, int msg, int wParam, int lParam) {
	if (handle == 0) return 0;
	return OS.CallWindowProc (windowProc (), hwnd, msg, wParam, lParam);
}

static int checkStyle (int style) {
	return checkBits (style, SWT.DATE, SWT.TIME, SWT.CALENDAR, 0, 0, 0);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when the control is selected, by sending
 * it one of the messages defined in the <code>SelectionListener</code>
 * interface.
 * <p>
 * <code>widgetSelected</code> is called when the control is selected.
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
	addListener (SWT.Selection, typedListener);
	addListener (SWT.DefaultSelection, typedListener);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int width = 0, height = 0;
	if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
		if ((style & SWT.CALENDAR) != 0) {
			RECT rect = new RECT ();
			OS.SendMessage(handle, OS.MCM_GETMINREQRECT, 0, rect);
			width = rect.right;
			height = rect.bottom;
		} else {
			int newFont, oldFont = 0;
			int hDC = OS.GetDC (handle);
			newFont = OS.SendMessage (handle, OS.WM_GETFONT, 0, 0);
			if (newFont != 0) oldFont = OS.SelectObject (hDC, newFont);
			TEXTMETRIC tm = OS.IsUnicode ? (TEXTMETRIC) new TEXTMETRICW () : new TEXTMETRICA ();
			OS.GetTextMetrics (hDC, tm);
			height = tm.tmHeight;
			int upDownHeight = OS.GetSystemMetrics (OS.SM_CYVSCROLL);
			height = Math.max (height, upDownHeight);
			String string = "00/00/0000";
			if ((style & SWT.TIME) != 0) string = "00:00:00 AM";
			RECT rect = new RECT ();
			TCHAR buffer = new TCHAR (getCodePage (), string, false);
			int flags = OS.DT_CALCRECT | OS.DT_EDITCONTROL | OS.DT_NOPREFIX;
			OS.DrawText (hDC, buffer, buffer.length (), rect, flags);
			width = rect.right - rect.left;
			if (newFont != 0) OS.SelectObject (hDC, oldFont);
			OS.ReleaseDC (handle, hDC);
			int upDownWidth = OS.GetSystemMetrics (OS.SM_CXVSCROLL);
			width += upDownWidth + MARGIN;
			// TODO: On Vista, can send DTM_GETDATETIMEPICKERINFO to ask the Edit control what its margins are
		}
	}
	if (width == 0) width = DEFAULT_WIDTH;
	if (height == 0) height = DEFAULT_HEIGHT;
	if (wHint != SWT.DEFAULT) width = wHint;
	if (hHint != SWT.DEFAULT) height = hHint;
	int border = getBorderWidth ();
	width += border * 2; height += border * 2;
	return new Point (width, height);
}

int defaultBackground () {
	return OS.GetSysColor (OS.COLOR_WINDOW);
}

/**
 * Returns the receiver's ...
 *
 * @return the receiver's ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getDay () {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	return systime.wDay;
}

/**
 * Returns the receiver's ...
 *
 * @return the receiver's ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getHour () {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	return systime.wHour;
}

/**
 * Returns the receiver's ...
 *
 * @return the receiver's ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getMinute () {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	return systime.wMinute;
}

/**
 * Returns the receiver's ...
 *
 * @return the receiver's ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getMonth () {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	return systime.wMonth;
}

String getNameText () {
	return "DateTime";
}

/**
 * Returns the receiver's ...
 *
 * @return the receiver's ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getSecond () {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	return systime.wSecond;
}

/**
 * Returns the receiver's ...
 *
 * @return the receiver's ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public int getYear () {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	return systime.wYear;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when the control is selected.
 *
 * @param listener the listener which should no longer be notified
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
public void removeSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection, listener);	
}

/**
 * Sets the receiver's ...
 *
 * @param ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setDay (int day) {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_SETCURSEL : OS.DTM_SETSYSTEMTIME;
	systime.wDay = (short)day;
	OS.SendMessage (handle, msg, 0, systime);
}

/**
 * Sets the receiver's ...
 *
 * @param ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setHour (int hour) {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_SETCURSEL : OS.DTM_SETSYSTEMTIME;
	systime.wHour = (short)hour;
	OS.SendMessage (handle, msg, 0, systime);
}

/**
 * Sets the receiver's ...
 *
 * @param ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMinute (int minute) {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_SETCURSEL : OS.DTM_SETSYSTEMTIME;
	systime.wMinute = (short)minute;
	OS.SendMessage (handle, msg, 0, systime);
}

/**
 * Sets the receiver's ...
 *
 * @param ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setMonth (int month) {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_SETCURSEL : OS.DTM_SETSYSTEMTIME;
	systime.wMonth = (short)month;
	OS.SendMessage (handle, msg, 0, systime);
}

/**
 * Sets the receiver's ...
 *
 * @param ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setSecond (int second) {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_SETCURSEL : OS.DTM_SETSYSTEMTIME;
	systime.wSecond = (short)second;
	OS.SendMessage (handle, msg, 0, systime);
}

/**
 * Sets the receiver's ...
 *
 * @param ...
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setYear (int year) {
	checkWidget ();
	SYSTEMTIME systime = new SYSTEMTIME ();
	int msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_GETCURSEL : OS.DTM_GETSYSTEMTIME;
	OS.SendMessage (handle, msg, 0, systime);
	msg = (style & SWT.CALENDAR) != 0 ? OS.MCM_SETCURSEL : OS.DTM_SETSYSTEMTIME;
	systime.wYear = (short)year;
	OS.SendMessage (handle, msg, 0, systime);
}

int widgetStyle () {
	int bits = super.widgetStyle () | OS.WS_TABSTOP;
	if ((style & SWT.CALENDAR) != 0) return bits;
	if ((style & SWT.TIME) != 0) bits |= OS.DTS_TIMEFORMAT;
	if ((style & SWT.DATE) != 0) bits |= OS.DTS_SHORTDATECENTURYFORMAT | OS.DTS_UPDOWN;
	return bits;
}

TCHAR windowClass () {
	return (style & SWT.CALENDAR) != 0 ? CalendarClass : DateTimeClass;
}

int windowProc () {
	return (style & SWT.CALENDAR) != 0 ? CalendarProc : DateTimeProc;
}

LRESULT WM_ERASEBKGND (int wParam, int lParam) {
	super.WM_ERASEBKGND (wParam, lParam);
	drawBackground (wParam);
	return LRESULT.ONE;
}

LRESULT wmNotifyChild (NMHDR hdr, int wParam, int lParam) {
	switch (hdr.code) {
		case OS.MCN_SELCHANGE:
		case OS.DTN_DATETIMECHANGE:
			sendEvent (SWT.Selection);
			break;
	}
	return super.wmNotifyChild (hdr, wParam, lParam);
}
}
