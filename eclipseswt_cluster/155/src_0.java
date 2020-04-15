/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.carbon.*;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.widgets.*;

class MozillaDelegate {
	Browser browser;
	Listener listener;
	boolean hasFocus;
	Callback callback3;
	static Hashtable handles = new Hashtable ();
	
MozillaDelegate (Browser browser) {
	super ();
	this.browser = browser;
}

static Browser findBrowser (int handle) {
	LONG value = (LONG)handles.get (new LONG (handle));
	if (value != null) {
		Display display = Display.getCurrent ();
		return (Browser)display.findWidget (value.value);
	}
	return null;
}

static char[] mbcsToWcs (String codePage, byte [] buffer) {
	int encoding = OS.CFStringGetSystemEncoding ();
	int cfstring = OS.CFStringCreateWithBytes (OS.kCFAllocatorDefault, buffer, buffer.length, encoding, false);
	char[] chars = null;
	if (cfstring != 0) {
		int length = OS.CFStringGetLength (cfstring);
		chars = new char [length];
		if (length != 0) {
			CFRange range = new CFRange ();
			range.length = length;
			OS.CFStringGetCharacters (cfstring, range, chars);
		}
		OS.CFRelease (cfstring);
	}
	return chars;
}

static byte[] wcsToMbcs (String codePage, String string, boolean terminate) {
	char[] chars = new char [string.length()];
	string.getChars (0, chars.length, chars, 0);
	int cfstring = OS.CFStringCreateWithCharacters (OS.kCFAllocatorDefault, chars, chars.length);
	byte[] buffer = null;
	if (cfstring != 0) {
		CFRange range = new CFRange ();
		range.length = chars.length;
		int encoding = OS.CFStringGetSystemEncoding ();
		int[] size = new int[1];
		int numChars = OS.CFStringGetBytes (cfstring, range, encoding, (byte)'?', true, null, 0, size);
		buffer = new byte [size[0] + (terminate ? 1 : 0)];
		if (numChars != 0) {
			numChars = OS.CFStringGetBytes (cfstring, range, encoding, (byte)'?', true, buffer, size[0], size);
		}
		OS.CFRelease (cfstring);
	}
	return buffer;
}

public int eventProc3 (int nextHandler, int theEvent, int userData) {
	browser.getShell ().forceActive ();
	((Mozilla)browser.webBrowser).Activate ();
	return OS.eventNotHandledErr;
}

int getHandle () {
    int embedHandle = Cocoa.objc_msgSend (Cocoa.C_NSImageView, Cocoa.S_alloc);
	if (embedHandle == 0) SWT.error (SWT.ERROR_NO_HANDLES);
	NSRect r = new NSRect ();
	embedHandle = Cocoa.objc_msgSend (embedHandle, Cocoa.S_initWithFrame, r);
	int rc;
	int[] outControl = new int[1];
	if (OS.VERSION >= 0x1050) {
		rc = Cocoa.HICocoaViewCreate (embedHandle, 0, outControl);
	} else {
		try {
			System.loadLibrary ("frameembedding"); //$NON-NLS-1$
		} catch (UnsatisfiedLinkError e) {}
		rc = Cocoa.HIJavaViewCreateWithCocoaView (outControl, embedHandle);
	}
	if (rc != OS.noErr || outControl[0] == 0) SWT.error (SWT.ERROR_NO_HANDLES);
	int subHIView = outControl[0];
	HILayoutInfo newLayoutInfo = new HILayoutInfo ();
	newLayoutInfo.version = 0;
	OS.HIViewGetLayoutInfo (subHIView, newLayoutInfo);
	HISideBinding biding = newLayoutInfo.binding.top;
	biding.toView = 0;
	biding.kind = OS.kHILayoutBindMin;
	biding.offset = 0;
	biding = newLayoutInfo.binding.left;
	biding.toView = 0;
	biding.kind = OS.kHILayoutBindMin;
	biding.offset = 0;
	biding = newLayoutInfo.binding.bottom;
	biding.toView = 0;
	biding.kind = OS.kHILayoutBindMax;
	biding.offset = 0;
	biding = newLayoutInfo.binding.right;
	biding.toView = 0;
	biding.kind = OS.kHILayoutBindMax;
	biding.offset = 0;
	OS.HIViewSetLayoutInfo (subHIView, newLayoutInfo);
	OS.HIViewChangeFeatures (subHIView, OS.kHIViewFeatureIsOpaque, 0);
	OS.HIViewSetVisible (subHIView, true);
	int parentHandle = browser.handle;
	OS.HIViewAddSubview (browser.handle, subHIView);
	CGRect rect = new CGRect ();
	OS.HIViewGetFrame (parentHandle, rect);
	rect.x = rect.y = 0;
	OS.HIViewSetFrame (subHIView, rect);
	handles.put (new LONG (embedHandle), new LONG (browser.handle));

	callback3 = new Callback (this, "eventProc3", 3); //$NON-NLS-1$
	int [] mask = new int [] {
		OS.kEventClassMouse, OS.kEventMouseDown,
	};
	int controlTarget = OS.GetControlEventTarget (subHIView);
	OS.InstallEventHandler (controlTarget, callback3.getAddress (), mask.length / 2, mask, subHIView, null);

	return embedHandle;
}

String getLibraryName () {
	return "libxpcom.dylib"; //$NON-NLS-1$
}

void handleFocus () {
	if (hasFocus) return;
	hasFocus = true;
	((Mozilla)browser.webBrowser).Activate ();
	browser.setFocus ();
	listener = new Listener () {
		public void handleEvent (Event event) {
			if (event.widget == browser) return;
			((Mozilla)browser.webBrowser).Deactivate ();
			hasFocus = false;
			browser.getDisplay ().removeFilter (SWT.FocusIn, this);
			browser.getShell ().removeListener (SWT.Deactivate, this);
			listener = null;
		}
	
	};
	browser.getDisplay ().addFilter (SWT.FocusIn, listener);
	browser.getShell ().addListener (SWT.Deactivate, listener);
}

void onDispose (int embedHandle) {
	handles.remove (new LONG (embedHandle));
	if (callback3 != null) {
		callback3.dispose ();
		callback3 = null;
	}
	if (listener != null) {
		browser.getDisplay ().removeFilter (SWT.FocusIn, listener);
		browser.getShell ().removeListener (SWT.Deactivate, listener);
		listener = null;
	}
	browser = null;
}

void setSize (int embedHandle, int width, int height) {
	// TODO
}

}
