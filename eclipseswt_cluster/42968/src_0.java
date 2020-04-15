package org.eclipse.swt.widgets;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2001  All Rights Reserved
 */

import org.eclipse.swt.internal.photon.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;

import java.util.EventListener;

public abstract class Widget {
	public int handle;
	int style;
	EventTable eventTable;
	Object data;
	String [] keys;
	Object [] values;
	
	static final int DEFAULT_WIDTH	= 64;
	static final int DEFAULT_HEIGHT = 64;

	static final char Mnemonic = '&';

Widget () {
	/* Do nothing */
}

public Widget (Widget parent, int style) {
	checkSubclass ();
	checkParent (parent);
	this.style = style;
}

static int checkBits (int style, int int0, int int1, int int2, int int3, int int4, int int5) {
	int mask = int0 | int1 | int2 | int3 | int4 | int5;
	if ((style & mask) == 0) style |= int0;
	if ((style & int0) != 0) style = (style & ~mask) | int0;
	if ((style & int1) != 0) style = (style & ~mask) | int1;
	if ((style & int2) != 0) style = (style & ~mask) | int2;
	if ((style & int3) != 0) style = (style & ~mask) | int3;
	if ((style & int4) != 0) style = (style & ~mask) | int4;
	if ((style & int5) != 0) style = (style & ~mask) | int5;
	return style;
}

void checkParent (Widget parent) {
	if (parent == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (!parent.isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
}

protected void checkSubclass () {
	if (!isValidSubclass ()) error (SWT.ERROR_INVALID_SUBCLASS);
}

protected void checkWidget () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
}

int copyPhImage(int image) {
	if (image == 0) return 0;
	int imageHandle = OS.PiDuplicateImage (image, 0);
	/* Bug in Photon - The image returned by PiDuplicateImage
	has the same mask_bm as the original image.
	*/
	PhImage_t phImage = new PhImage_t();
	OS.memmove (phImage, imageHandle, PhImage_t.sizeof);
	if (phImage.mask_bm != 0) {
		int length = phImage.mask_bpl * phImage.size_h;
		int ptr = OS.malloc (length);
		OS.memmove(ptr, phImage.mask_bm, length);
		phImage.mask_bm = ptr;
		OS.memmove (imageHandle, phImage, PhImage_t.sizeof);
	}
	return imageHandle;
}

public void addListener (int eventType, Listener handler) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (handler == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) eventTable = new EventTable ();
	eventTable.hook (eventType, handler);
}

public void addDisposeListener (DisposeListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Dispose, typedListener);
}

void createHandle (int index) {
	/* Do nothing */
}

void createWidget (int index) {
	createHandle (index);
	hookEvents ();
	register ();
}

void deregister () {
	if (handle == 0) return;
	WidgetTable.remove (handle);
}

void destroyWidget () {
	int topHandle = topHandle ();
	releaseHandle ();
	if (topHandle != 0) {
		OS.PtDestroyWidget (topHandle);
	}
}

public void dispose () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	releaseChild ();
	releaseWidget ();
	destroyWidget ();
}

static void error (int code) {
	SWT.error(code);
}

public Object getData () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return data;
}

public Object getData (String key) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (key == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (keys == null) return null;
	for (int i=0; i<keys.length; i++) {
		if (keys [i].equals (key)) return values [i];
	}
	return null;
}

public abstract Display getDisplay ();

String getName () {
	String string = getClass ().getName ();
	int index = string.length ();
	while (--index > 0 && string.charAt (index) != '.');
	return string.substring (index + 1, string.length ());
}

String getNameText () {
	return "";
}

public int getStyle () {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return style;
}

boolean hooks (int eventType) {
	if (eventTable == null) return false;
	return eventTable.hooks (eventType);
}

void hookEvents () {
	/* Do nothing */
}

public boolean isDisposed () {
	return handle == 0;
}

boolean isValidSubclass () {
	return Display.isValidClass (getClass ());
}

protected boolean isListening (int eventType) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	return hooks (eventType);
}

boolean isValidThread () {
	return getDisplay ().isValidThread ();
}

boolean isValidWidget () {
	return true /*handle != 0*/;
}

public void notifyListeners (int eventType, Event event) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (event == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	event.type = eventType;
	event.widget = this;
	eventTable.sendEvent (event);
}

void postEvent (int eventType) {
	if (eventTable == null) return;
	postEvent (eventType, new Event ());
}

void postEvent (int eventType, Event event) {
	if (eventTable == null) return;
	event.type = eventType;
	event.widget = this;
	if (event.time == 0) {
		event.time = (int) System.currentTimeMillis ();
	}
	getDisplay ().postEvent (event);
}

int processDefaultSelection (int info) {
	return OS.Pt_CONTINUE;
}

int processFocusIn (int info) {
	return OS.Pt_CONTINUE;
}

int processFocusOut (int info) {
	return OS.Pt_CONTINUE;
}

int processHide (int info) {
	return OS.Pt_CONTINUE;
}

int processKey (int info) {
	return OS.Pt_CONTINUE;
}

int processEvent (int data, int info) {
	switch (data) {
//		case SWT.Arm:				return processArm (info);
//		case SWT.Dispose:			return processDispose (info);
		case SWT.DefaultSelection:	return processDefaultSelection (info);
		case SWT.FocusIn:			return processFocusIn (info);
		case SWT.FocusOut:			return processFocusOut (info);
//		case SWT.Help:				return processHelp (info);
		case SWT.Hide:				return processHide (info);
		case SWT.KeyDown:			
		case SWT.KeyUp:				return processKey (info);
		case SWT.Modify:			return processModify (info);
		case SWT.MouseDown:			
		case SWT.MouseMove:
		case SWT.MouseUp:			return processMouse (info);
		case SWT.MouseEnter:		return processMouseEnter (info);
//		case SWT.MouseExit:			return processMouseOut (info);
		case SWT.Move:				return processMove (info);
//		case SWT.Paint:				return processPaint (info);
		case SWT.Resize:			return processResize (info);
		case SWT.Show:				return processShow (info);
		case SWT.Selection:			return processSelection (info);
		case SWT.Verify:			return processVerify (info);
		case -1:					return processShellResize (info);
	}
	return OS.Pt_CONTINUE;
}

int processModify (int info) {
	return OS.Pt_CONTINUE;
}

int processMouse (int info) {
	return OS.Pt_CONTINUE;
}

int processMouseEnter (int info) {
	return OS.Pt_CONTINUE;
}

int processMove (int info) {
	return OS.Pt_CONTINUE;
}

int processPaint (int damage) {
	return OS.Pt_CONTINUE;
}

int processResize (int info) {
	return OS.Pt_CONTINUE;
}

int processShow (int info) {
	return OS.Pt_CONTINUE;
}

int processShellResize (int info) {
	return OS.Pt_CONTINUE;
}

int processSelection (int info) {
	return OS.Pt_CONTINUE;
}

int processVerify (int info) {
	return OS.Pt_CONTINUE;
}

void releaseChild () {
	/* Do nothing */
}

void register () {
	if (handle == 0) return;
	WidgetTable.put (handle, this);
}

void releaseHandle () {
	handle = 0;
//	state |= DISPOSED;
}

void releaseWidget () {
	sendEvent (SWT.Dispose);
//	state |= DISPOSED;
	eventTable = null;
	data = null;
}

public void removeListener (int eventType, Listener handler) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (handler == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (eventType, handler);
}

protected void removeListener (int eventType, EventListener handler) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (handler == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (eventType, handler);
}

public void removeDisposeListener (DisposeListener listener) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Dispose, listener);
}

void sendEvent (int eventType) {
	if (eventTable == null) return;
	sendEvent (eventType, new Event ());
}

void sendEvent (int eventType, Event event) {
	if (eventTable == null) return;
	event.widget = this;
	event.type = eventType;
	if (event.time == 0) {
		event.time = (int) System.currentTimeMillis ();
	}
	eventTable.sendEvent (event);
}

public void setData (Object data) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
	this.data = data;
}

public void setData (String key, Object value) {
	if (!isValidThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (!isValidWidget ()) error (SWT.ERROR_WIDGET_DISPOSED);
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

public String toString () {
	String string = "*Disposed*";
	if (!isDisposed ()) {
		string = "*Wrong Thread*";
		if (isValidThread ()) string = getNameText ();
	}
	return getName () + " {" + string + "}";
}

int topHandle () {
	return handle;
}

}