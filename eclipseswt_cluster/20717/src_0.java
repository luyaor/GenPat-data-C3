package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;

public class TrayIcon extends Widget {
	int /*long*/ id;
	int /*long*/ imageHandle;
	int /*long*/ tooltipsHandle;
	int /*long*/ trayWindow;
	int xMessageAtom;
	Image image;
	Menu menu;
	String toolTipText;

public TrayIcon (Display display) {
	if (display == null) display = Display.getCurrent ();
	if (display == null) display = Display.getDefault ();
	if (!display.isValidThread ()) {
		error (SWT.ERROR_THREAD_INVALID_ACCESS);
	}
	this.display = display;
	createWidget (0);

	int monitor = 0;
	int /*long*/ screen = OS.gdk_screen_get_default ();
	if (screen != 0) {
		monitor = OS.gdk_screen_get_number (screen);
	}
	byte [] trayBuffer = Converter.wcsToMbcs (null, "_NET_SYSTEM_TRAY_S" + monitor, true);
	int /*long*/ trayAtom = OS.gdk_atom_intern (trayBuffer, true);
	int /*long*/ xTrayAtom = OS.gdk_x11_atom_to_xatom (trayAtom);
	int /*long*/ xDisplay = OS.GDK_DISPLAY ();
	trayWindow = OS.XGetSelectionOwner (xDisplay, xTrayAtom);
	byte [] messageBuffer = Converter.wcsToMbcs (null, "_NET_SYSTEM_TRAY_OPCODE", true);
	int /*long*/ messageAtom = OS.gdk_atom_intern (messageBuffer, true);
	xMessageAtom = OS.gdk_x11_atom_to_xatom (messageAtom);

	XClientMessageEvent event = new XClientMessageEvent ();
	event.type = OS.ClientMessage;
	event.window = trayWindow;
	event.message_type = xMessageAtom;
	event.format = 32;
	event.data [0] = OS.GDK_CURRENT_TIME;
	event.data [1] = OS.SYSTEM_TRAY_REQUEST_DOCK;
	event.data [2] = id;
	event.data [3] = 0;
	event.data [4] = 0;
	int /*long*/ clientEvent = OS.g_malloc (XClientMessageEvent.sizeof);
	OS.memmove (clientEvent, event, XClientMessageEvent.sizeof);
	int result = OS.XSendEvent (OS.GDK_DISPLAY (), trayWindow, false, OS.NoEventMask, clientEvent);
	OS.g_free (clientEvent);
}

public void addSelectionListener(SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	TypedListener typedListener = new TypedListener (listener);
	addListener (SWT.Selection, typedListener);
	addListener (SWT.DefaultSelection, typedListener);
}

void createHandle (int index) {
	state |= HANDLE;
	handle = OS.gtk_plug_new (0);
	if (handle == 0) error (SWT.ERROR_NO_HANDLES);
	imageHandle = OS.gtk_image_new ();
	if (imageHandle == 0) error (SWT.ERROR_NO_HANDLES);
	OS.gtk_container_add (handle, imageHandle);
	OS.gtk_widget_show (handle);
	OS.gtk_widget_show (imageHandle);
	id = OS.gtk_plug_get_id (handle);
}

public Image getImage () {
	checkWidget ();
	return image;
}

public Menu getMenu () {
	checkWidget ();
	return menu;
}

public String getToolTipText () {
	checkWidget ();
	return toolTipText;
}

int gtk_button_press_event (int widget, int event) {
	GdkEventButton gdkEvent = new GdkEventButton ();
	OS.memmove (gdkEvent, event, GdkEventButton.sizeof);
	if (gdkEvent.type == OS.GDK_3BUTTON_PRESS) return 0;
	if (gdkEvent.button == 3) {
		if (menu != null) menu.setVisible (true);
		return 0;
	}
	double [] x = new double [1];
	double [] y = new double [1];
	OS.gdk_event_get_root_coords (event, x, y);
	Event ev = new Event ();		
	ev.x = (int) x [0];
	ev.y = (int) y [0];
	if (gdkEvent.type == OS.GDK_2BUTTON_PRESS) {
		sendEvent (SWT.DefaultSelection, ev);
	} else {
		sendEvent (SWT.Selection, ev);
	}
	return 0;
}

void hookEvents () {
	int eventMask = OS.GDK_BUTTON_PRESS_MASK;
	OS.gtk_widget_add_events (handle, eventMask);
	OS.g_signal_connect (handle, OS.button_press_event, display.windowProc3, BUTTON_PRESS_EVENT);
 }

public boolean getVisible () {
	checkWidget ();
	return OS.GTK_WIDGET_VISIBLE (handle);
}

void releaseWidget () {
	super.releaseWidget ();
	image = null;
	toolTipText = null;
	menu = null;
}

public void removeSelectionListener (SelectionListener listener) {
	checkWidget ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (SWT.Selection, listener);
	eventTable.unhook (SWT.DefaultSelection, listener);
}

public void setImage (Image image) {
	checkWidget ();
	if (image != null && image.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
	this.image = image;
	Rectangle rect = image.getBounds ();
	OS.gtk_widget_set_size_request (handle, rect.width, rect.height);
	OS.gtk_image_set_from_pixmap (imageHandle, image.pixmap, image.mask);
	OS.gtk_widget_show (imageHandle);
}

public void setMenu (Menu menu) {
	checkWidget ();
	if (menu != null && menu.isDisposed ()) error (SWT.ERROR_INVALID_ARGUMENT);
	this.menu = menu;
}

public void setToolTipText (String string) {
	checkWidget ();
	toolTipText = string;
	if (string == null) string = "";
	byte [] buffer = null;
	if (string.length () > 0) {
		buffer = Converter.wcsToMbcs (null, string, true);
	}
	if (tooltipsHandle == 0) {
		tooltipsHandle = OS.gtk_tooltips_new ();
		if (tooltipsHandle == 0) error (SWT.ERROR_NO_HANDLES);
		OS.g_object_ref (tooltipsHandle);
		OS.gtk_object_sink (tooltipsHandle);
	}
	OS.gtk_tooltips_set_tip (tooltipsHandle, handle, buffer, null);
}

public void setVisible (boolean visible) {
	checkWidget ();
	if (OS.GTK_WIDGET_VISIBLE (handle) == visible) return;
	if (visible) {
		/*
		* It is possible (but unlikely), that application
		* code could have disposed the widget in the show
		* event.  If this happens, just return.
		*/
		sendEvent (SWT.Show);
		if (isDisposed ()) return;
		OS.gtk_widget_show (handle);
	} else {
		OS.gtk_widget_hide (handle);
		sendEvent (SWT.Hide);
	}
}
}
