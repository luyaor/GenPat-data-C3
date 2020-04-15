package org.eclipse.swt.graphics;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;

public abstract class Device implements Drawable {
	
	/* Debugging */
	public static boolean DEBUG;
	boolean debug = DEBUG;
	boolean tracking = DEBUG;
	Error [] errors;
	Object [] objects;
	
	/* Colormap and reference count */
	GdkColor [] gdkColors;
	int [] colorRefCount;
	
	/* Disposed flag */
	boolean disposed;
	
	/* Warning and Error Handlers */
	int logProc;
	Callback logCallback;
	String [] log_domains = {"Gtk", /*"Gdk", "GLib", "GdkPixbuf"*/};
	int [] handler_ids = new int [log_domains.length];
	boolean warnings = true;
	
	/*
	* The following colors are listed in the Windows
	* Programmer's Reference as the colors in the default
	* palette.
	*/
	Color COLOR_BLACK, COLOR_DARK_RED, COLOR_DARK_GREEN, COLOR_DARK_YELLOW, COLOR_DARK_BLUE;
	Color COLOR_DARK_MAGENTA, COLOR_DARK_CYAN, COLOR_GRAY, COLOR_DARK_GRAY, COLOR_RED;
	Color COLOR_GREEN, COLOR_YELLOW, COLOR_BLUE, COLOR_MAGENTA, COLOR_CYAN, COLOR_WHITE;
	
	/*
	* TEMPORARY CODE. When a graphics object is
	* created and the device parameter is null,
	* the current Display is used. This presents
	* a problem because SWT graphics does not
	* reference classes in SWT widgets. The correct
	* fix is to remove this feature. Unfortunately,
	* too many application programs rely on this
	* feature.
	*
	* This code will be removed in the future.
	*/
	protected static Device CurrentDevice;
	protected static Runnable DeviceFinder;
	static {
		try {
			Class.forName ("org.eclipse.swt.widgets.Display");
		} catch (Throwable e) {}
	}	

/*
* TEMPORARY CODE.
*/
static Device getDevice () {
	if (DeviceFinder != null) DeviceFinder.run();
	Device device = CurrentDevice;
	CurrentDevice = null;
	return device;
}

/**
 * Constructs a new instance of this class.
 * <p>
 * You must dispose the device when it is no longer required. 
 * </p>
 *
 * @param data the DeviceData which describes the receiver
 *
 * @see #create
 * @see #init
 * @see DeviceData
 */
public Device(DeviceData data) {
	if (data != null) {
		debug = data.debug;
		tracking = data.tracking;
	}
	create (data);
	init ();
	if (tracking) {
		errors = new Error [128];
		objects = new Object [128];
	}
}

protected void checkDevice () {
	if (disposed) SWT.error(SWT.ERROR_DEVICE_DISPOSED);
}

protected void create (DeviceData data) {
}

/**
 * Disposes of the operating system resources associated with
 * the receiver. After this method has been invoked, the receiver
 * will answer <code>true</code> when sent the message
 * <code>isDisposed()</code>.
 *
 * @see #release
 * @see #destroy
 * @see #checkDevice
 */
public void dispose () {
	if (isDisposed()) return;
	checkDevice ();
	release ();
	destroy ();
	disposed = true;
	if (tracking) {
		objects = null;
		errors = null;
	}
}

void dispose_Object (Object object) {
	for (int i=0; i<objects.length; i++) {
		if (objects [i] == object) {
			objects [i] = null;
			errors [i] = null;
			return;
		}
	}
}

protected void destroy () {
}

/**
 * Returns a rectangle describing the receiver's size and location.
 *
 * @return the bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Rectangle getBounds () {
	checkDevice ();
	SWT.error (SWT.ERROR_NOT_IMPLEMENTED);
	return new Rectangle(0, 0, 0, 0);
}

/**
 * Returns a <code>DeviceData</code> based on the receiver.
 * Modifications made to this <code>DeviceData</code> will not
 * affect the receiver.
 *
 * @return a <code>DeviceData</code> containing the device's data and attributes
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see DeviceData
 */
public DeviceData getDeviceData () {
	checkDevice();
	DeviceData data = new DeviceData ();
	data.debug = debug;
	data.tracking = tracking;
	int count = 0, length = 0;
	if (tracking) length = objects.length;
	for (int i=0; i<length; i++) {
		if (objects [i] != null) count++;
	}
	int index = 0;
	data.objects = new Object [count];
	data.errors = new Error [count];
	for (int i=0; i<length; i++) {
		if (objects [i] != null) {
			data.objects [index] = objects [i];
			data.errors [index] = errors [i];
			index++;
		}
	}
	return data;
}

/**
 * Returns a rectangle which describes the area of the
 * receiver which is capable of displaying data.
 * 
 * @return the client area
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #getBounds
 */
public Rectangle getClientArea () {
	checkDevice ();
	return getBounds ();
}

/**
 * Returns the bit depth of the screen, which is the number of
 * bits it takes to represent the number of unique colors that
 * the screen is currently capable of displaying. This number 
 * will typically be one of 1, 8, 15, 16, 24 or 32.
 *
 * @return the depth of the screen
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public int getDepth () {
	checkDevice ();
	SWT.error (SWT.ERROR_NOT_IMPLEMENTED);
	return 0;
}

/**
 * Returns a point whose x coordinate is the horizontal
 * dots per inch of the display, and whose y coordinate
 * is the vertical dots per inch of the display.
 *
 * @return the horizontal and vertical DPI
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Point getDPI () {
	checkDevice ();
	SWT.error (SWT.ERROR_NOT_IMPLEMENTED);
	return new Point (72, 72);
}

/**
 * Returns <code>FontData</code> objects which describe
 * the fonts which match the given arguments. If the
 * <code>faceName</code> is null, all fonts will be returned.
 *
 * @param faceName the name of the font to look for, or null
 * @param scalable true if scalable fonts should be returned.
 * @return the matching font data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public FontData [] getFontList (String faceName, boolean scalable) {	
	checkDevice ();
	String xlfd;
	if (faceName == null) {
		xlfd = "-*";
	} else {
		int dashIndex = faceName.indexOf('-');
		if (dashIndex < 0) {
			xlfd = "-*-" + faceName + "-*";
		} else {
			xlfd = "-" + faceName + "-*";
		}
	}
	/* Use the character encoding for the default locale */
	byte [] buffer1 = Converter.wcsToMbcs (null, xlfd, true);
	int [] ret = new int [1];
	int listPtr = OS.XListFonts (buffer1, 65535, ret);
	int ptr = listPtr;
	int [] intBuf = new int [1];
	FontData [] fd = new FontData [ret [0]];
	int fdIndex = 0;
	for (int i = 0; i < ret [0]; i++) {
		OS.memmove (intBuf, ptr, 4);
		int charPtr = intBuf [0];
		int length = OS.strlen (charPtr);
		byte [] buffer2 = new byte [length];
		OS.memmove (buffer2, charPtr, length);
		/* Use the character encoding for the default locale */
		char [] chars = Converter.mbcsToWcs (null, buffer2);
		FontData data = FontData.gtk_new (new String (chars));
		boolean isScalable = data.averageWidth == 0 && data.pixels == 0 && data.points == 0;
		if (isScalable == scalable) {
			fd [fdIndex++] = data;
		}
		ptr += 4;
	}
	// FIXME, leaking font list
//	OS.XFreeFontNames (listPtr);
	if (fdIndex == ret [0]) return fd;
	FontData [] result = new FontData [fdIndex];
	System.arraycopy (fd, 0, result, 0, fdIndex);
	return result;
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
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see SWT
 */
public Color getSystemColor (int id) {
	checkDevice ();
	switch (id) {
		case SWT.COLOR_BLACK: 				return COLOR_BLACK;
		case SWT.COLOR_DARK_RED: 			return COLOR_DARK_RED;
		case SWT.COLOR_DARK_GREEN:	 		return COLOR_DARK_GREEN;
		case SWT.COLOR_DARK_YELLOW: 		return COLOR_DARK_YELLOW;
		case SWT.COLOR_DARK_BLUE: 			return COLOR_DARK_BLUE;
		case SWT.COLOR_DARK_MAGENTA: 		return COLOR_DARK_MAGENTA;
		case SWT.COLOR_DARK_CYAN: 			return COLOR_DARK_CYAN;
		case SWT.COLOR_GRAY: 				return COLOR_GRAY;
		case SWT.COLOR_DARK_GRAY: 			return COLOR_DARK_GRAY;
		case SWT.COLOR_RED: 				return COLOR_RED;
		case SWT.COLOR_GREEN: 				return COLOR_GREEN;
		case SWT.COLOR_YELLOW: 				return COLOR_YELLOW;
		case SWT.COLOR_BLUE: 				return COLOR_BLUE;
		case SWT.COLOR_MAGENTA: 			return COLOR_MAGENTA;
		case SWT.COLOR_CYAN: 				return COLOR_CYAN;
		case SWT.COLOR_WHITE: 				return COLOR_WHITE;
	}
	return COLOR_BLACK;
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
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Font getSystemFont () {
	checkDevice ();
	return null;
}

protected void init () {
	
	/* Create GTK warnings and error callbacks */
	logCallback = new Callback (this, "logProc", 4);
	logProc = logCallback.getAddress ();
	if (logProc == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	/* Set GTK warning and error handlers */
	if (debug) {
		int flags = OS.G_LOG_LEVEL_MASK | OS.G_LOG_FLAG_FATAL | OS.G_LOG_FLAG_RECURSION;
		for (int i=0; i<log_domains.length; i++) {
			byte [] log_domain = Converter.wcsToMbcs (null, log_domains [i], true);
			handler_ids [i] = OS.g_log_set_handler (log_domain, flags, logProc, 0);
		}
	}
	
	/* Create the standard colors */
	COLOR_BLACK = new Color (this, 0,0,0);
	COLOR_DARK_RED = new Color (this, 0x80,0,0);
	COLOR_DARK_GREEN = new Color (this, 0,0x80,0);
	COLOR_DARK_YELLOW = new Color (this, 0x80,0x80,0);
	COLOR_DARK_BLUE = new Color (this, 0,0,0x80);
	COLOR_DARK_MAGENTA = new Color (this, 0x80,0,0x80);
	COLOR_DARK_CYAN = new Color (this, 0,0x80,0x80);
	COLOR_GRAY = new Color (this, 0xC0,0xC0,0xC0);
	COLOR_DARK_GRAY = new Color (this, 0x80,0x80,0x80);
	COLOR_RED = new Color (this, 0xFF,0,0);
	COLOR_GREEN = new Color (this, 0,0xFF,0);
	COLOR_YELLOW = new Color (this, 0xFF,0xFF,0);
	COLOR_BLUE = new Color (this, 0,0,0xFF);
	COLOR_MAGENTA = new Color (this, 0xFF,0,0xFF);
	COLOR_CYAN = new Color (this, 0,0xFF,0xFF);
	COLOR_WHITE = new Color (this, 0xFF,0xFF,0xFF);
}

/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Device</code>. It is marked public only so that it
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
public abstract int internal_new_GC (GCData data);

/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Device</code>. It is marked public only so that it
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
public abstract void internal_dispose_GC (int handle, GCData data);

/**
 * Returns <code>true</code> if the device has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the device.
 * When a device has been disposed, it is an error to
 * invoke any other method using the device.
 *
 * @return <code>true</code> when the device is disposed and <code>false</code> otherwise
 */
public boolean isDisposed () {
	return disposed;
}

int logProc (int log_domain, int log_level, int message, int user_data) {
	if (DEBUG || (debug && warnings)) {
		new Error ().printStackTrace ();
		OS.g_log_default_handler (log_domain, log_level, message, 0);
	}
	return 0;
}

void new_Object (Object object) {
	for (int i=0; i<objects.length; i++) {
		if (objects [i] == null) {
			objects [i] = object;
			errors [i] = new Error ();
			return;
		}
	}
	Object [] newObjects = new Object [objects.length + 128];
	System.arraycopy (objects, 0, newObjects, 0, objects.length);
	newObjects [objects.length] = object;
	objects = newObjects;
	Error [] newErrors = new Error [errors.length + 128];
	System.arraycopy (errors, 0, newErrors, 0, errors.length);
	newErrors [errors.length] = new Error ();
	errors = newErrors;
}

protected void release () {
	if (gdkColors != null) {
		int colormap = OS.gdk_colormap_get_system();
		int [] pixel = new int [1];
		for (int i = 0; i < gdkColors.length; i++) {
			GdkColor color = gdkColors [i];
			if (color != null) {
				while (colorRefCount [i] > 0) {
					OS.gdk_color_free(color);
					--colorRefCount [i];
				}
			}
		}
	}
	gdkColors = null;
	colorRefCount = null;
	COLOR_BLACK = COLOR_DARK_RED = COLOR_DARK_GREEN = COLOR_DARK_YELLOW = COLOR_DARK_BLUE =
	COLOR_DARK_MAGENTA = COLOR_DARK_CYAN = COLOR_GRAY = COLOR_DARK_GRAY = COLOR_RED =
	COLOR_GREEN = COLOR_YELLOW = COLOR_BLUE = COLOR_MAGENTA = COLOR_CYAN = COLOR_WHITE = null;
		
	/* Free the GTK error and warning handler */
	int flags = OS.G_LOG_LEVEL_MASK | OS.G_LOG_FLAG_FATAL | OS.G_LOG_FLAG_RECURSION;
	for (int i=0; i<handler_ids.length; i++) {
		if (handler_ids [i] != 0) {
			byte [] log_domain = Converter.wcsToMbcs (null, log_domains [i], true);
			OS.g_log_remove_handler (log_domain, handler_ids [i]);
		}
	}
	logCallback.dispose ();  logCallback = null;
	handler_ids = null;  log_domains = null;
	logProc = 0;
}

/**
 * If the underlying window system supports printing warning messages
 * to the console, setting warnings to <code>true</code> prevents these
 * messages from being printed. If the argument is <code>false</code>
 * message printing is not blocked.
 *
 * @param warnings <code>true</code>if warnings should be handled, and <code>false</code> otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setWarnings (boolean warnings) {
	checkDevice ();
	this.warnings = warnings;
	if (debug) return;
	for (int i=0; i<handler_ids.length; i++) {
		if (handler_ids [i] != 0) {
			byte [] log_domain = Converter.wcsToMbcs (null, log_domains [i], true);
			OS.g_log_remove_handler (log_domain, handler_ids [i]);
		}
	}
	if (warnings) {
		int flags = OS.G_LOG_LEVEL_MASK | OS.G_LOG_FLAG_FATAL | OS.G_LOG_FLAG_RECURSION;
		for (int i=0; i<log_domains.length; i++) {
			byte [] log_domain = Converter.wcsToMbcs (null, log_domains [i], true);
			handler_ids [i] = OS.g_log_set_handler (log_domain, flags, logProc, 0);
		}
	}
}

}
