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

import java.util.Calendar;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.gtk.OS;

/*public*/ class DateTime extends Composite {
	int day, month, year, hour, minute, second;
	
	static final int MIN_YEAR = 1752; // Gregorian switchover in North America: September 19, 1752
	static final int MAX_YEAR = 9999;

public DateTime (Composite parent, int style) {
	super (parent, checkStyle (style));
}

static int checkStyle (int style) {
	/*
	* Even though it is legal to create this widget
	* with scroll bars, they serve no useful purpose
	* because they do not automatically scroll the
	* widget's client area.  The fix is to clear
	* the SWT style.
	*/
	style &= ~(SWT.H_SCROLL | SWT.V_SCROLL);
	return checkBits (style, SWT.DATE, SWT.TIME, SWT.CALENDAR, 0, 0, 0);
}

public void addSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection, typedListener);
	addListener (SWT.DefaultSelection, typedListener);
}

protected void checkSubclass () {
	if (!isValidSubclass ()) error (SWT.ERROR_INVALID_SUBCLASS);
}

public Point computeSize (int wHint, int hHint, boolean changed) {
	checkWidget ();
	int width = 0, height = 0;
	if (wHint == SWT.DEFAULT || hHint == SWT.DEFAULT) {
//		if ((style & SWT.CALENDAR) != 0) {
			// TODO: CALENDAR computeSize
			width = 300;
			height = 200;
//		} else {
			// TODO: TIME and DATE
//		}
	}
	if (width == 0) width = DEFAULT_WIDTH;
	if (height == 0) height = DEFAULT_HEIGHT;
	if (wHint != SWT.DEFAULT) width = wHint;
	if (hHint != SWT.DEFAULT) height = hHint;
	int border = getBorderWidth ();
	width += border * 2; height += border * 2;
	return new Point (width, height);
}

void createHandle (int index) {
//	if ((style & SWT.CALENDAR) != 0) {
		state |= HANDLE;
		fixedHandle = OS.g_object_new (display.gtk_fixed_get_type (), 0);
		if (fixedHandle == 0) error (SWT.ERROR_NO_HANDLES);
		OS.gtk_fixed_set_has_window (fixedHandle, true);
		handle = OS.gtk_calendar_new ();
		if (handle == 0) error (SWT.ERROR_NO_HANDLES);
		OS.gtk_container_add (fixedHandle, handle);
		OS.gtk_calendar_set_display_options(handle, OS.GTK_CALENDAR_SHOW_HEADING | OS.GTK_CALENDAR_SHOW_DAY_NAMES);
//	} else { /* SWT.DATE and SWT.TIME */
		// TODO: emulated date & time
//	}
}

void createWidget (int index) {
	super.createWidget (index);
//	if ((style & SWT.CALENDAR) != 0) {
		getDate();
//	}
}

void getDate() {
	int [] y = new int [1];
	int [] m = new int [1];
	int [] d = new int [1];
	OS.gtk_calendar_get_date(handle, y, m, d);
	year = y[0];
	month = m[0];
	day = d[0];
}

public int getDay () {
	checkWidget ();
	getDate();
	return day;
}

public int getHour () {
	checkWidget ();
	return hour;
}

public int getMinute () {
	checkWidget ();
	return minute;
}

public int getMonth () {
	checkWidget ();
	getDate();
	return month + 1;
}

public int getSecond () {
	checkWidget ();
	return second;
}

public int getYear () {
	checkWidget ();
	getDate();
	return year;
}

int /*long*/ gtk_day_selected (int /*long*/ widget) {
	sendSelectionEvent ();
	return 0;
}

int /*long*/ gtk_month_changed (int /*long*/ widget) {
	sendSelectionEvent ();
	return 0;
}

void hookEvents () {
	super.hookEvents();
//	if ((style & SWT.CALENDAR) != 0) {
		OS.g_signal_connect_closure (handle, OS.day_selected, display.closures [DAY_SELECTED], false);
		OS.g_signal_connect_closure (handle, OS.month_changed, display.closures [MONTH_CHANGED], false);
//	}
}

boolean isValid(int fieldName, int value) {
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.YEAR, year);
	calendar.set(Calendar.MONTH, month);
	int min = calendar.getActualMinimum(fieldName);
	int max = calendar.getActualMaximum(fieldName);
	return value >= min && value <= max;
}

void releaseWidget () {
	super.releaseWidget();
	//TODO: need to do anything here?
}

public void removeSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection, listener);	
}

void sendSelectionEvent () {
	int [] y = new int [1];
	int [] m = new int [1];
	int [] d = new int [1];
	OS.gtk_calendar_get_date(handle, y, m, d);
	//TODO: hour, minute, second?
	if (d[0] != day ||
		m[0] != month ||
		y[0] != year) {
		year = y[0];
		month = m[0];
		day = d[0];
		postEvent (SWT.Selection);
	}
}

public void setDay (int day) {
	checkWidget ();
	if (!isValid(Calendar.DAY_OF_MONTH, day)) return;
	this.day = day;
	OS.gtk_calendar_select_day(handle, day);
}

public void setHour (int hour) {
	checkWidget ();
	if (!isValid(Calendar.HOUR_OF_DAY, hour)) return;
	this.hour = hour;
}

public void setMinute (int minute) {
	checkWidget ();
	if (!isValid(Calendar.MINUTE, minute)) return;
	this.minute = minute;
}

public void setMonth (int month) {
	checkWidget ();
	month--;
	if (!isValid(Calendar.MONTH, month)) return;
	this.month = month;
	OS.gtk_calendar_select_month(handle, month, year);
}

public void setSecond (int second) {
	checkWidget ();
	if (!isValid(Calendar.SECOND, second)) return;
	this.second = second;
}

public void setYear (int year) {
	checkWidget ();
	//if (!isValid(Calendar.YEAR, year)) return;
	if (year < MIN_YEAR || year > MAX_YEAR) return;
	this.year = year;
	OS.gtk_calendar_select_month(handle, month, year);
}

}
