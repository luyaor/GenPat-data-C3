/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.awt;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/* SWT Imports */
import org.eclipse.swt.*;
import org.eclipse.swt.internal.Library;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;

/* AWT Imports */
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SWT_AWT {

static boolean loaded;

static native final int /*long*/ getAWTHandle (Canvas canvas);

static synchronized void loadLibrary () {
	if (loaded) return;
	loaded = true;
	System.loadLibrary("jawt");
	Library.loadLibrary("swt-awt");
}

public static Frame new_Frame (final Composite parent) {
	if (parent == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	if ((parent.getStyle () & SWT.EMBEDDED) == 0) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	int /*long*/ handle = parent.embeddedHandle;
	/*
	 * Some JREs have implemented the embedded frame constructor to take an integer
	 * and other JREs take a long.  To handle this binary incompatability, use
	 * reflection to create the embedded frame.
	 */
	Class clazz = null;
	try {
		clazz = Class.forName("sun.awt.X11.XEmbeddedFrame");
	} catch (Throwable e) {
		SWT.error (SWT.ERROR_NOT_IMPLEMENTED, e);		
	}
	Object value = null;
	Constructor constructor = null;
	try {
		constructor = clazz.getConstructor (new Class [] {int.class});
		value = constructor.newInstance (new Object [] {new Integer ((int)/*64*/handle)});
	} catch (Throwable e1) {
		try {
			constructor = clazz.getConstructor (new Class [] {long.class});
			value = constructor.newInstance (new Object [] {new Long (handle)});
		} catch (Throwable e2) {
			SWT.error (SWT.ERROR_NOT_IMPLEMENTED, e2);
		}
	}
	final Frame frame = (Frame) value;
	try {
		/* Call registerListeners() to make XEmbed focus traversal work */
		Method method = clazz.getMethod("registerListeners", null);
		if (method != null) method.invoke(value, null);
	} catch (Throwable e) {}
	parent.getShell ().addListener (SWT.Move, new Listener () {
		public void handleEvent (Event e) {
			Display display = parent.getDisplay();
			final Point location = display.map(parent, null, 0, 0);
			EventQueue.invokeLater(new Runnable () {
				public void run () {
					frame.setLocation (location.x, location.y);
				}
			});
		}
	});
	parent.addListener (SWT.Dispose, new Listener () {
		public void handleEvent (Event event) {
			parent.setVisible(false);
			EventQueue.invokeLater(new Runnable () {
				public void run () {
					frame.dispose ();
				}
			});
		}
	});
	parent.getDisplay().asyncExec(new Runnable() {
		public void run () {
			if (parent.isDisposed()) return;
			Display display = parent.getDisplay();
			Rectangle clientArea = parent.getClientArea();
			final Rectangle bounds = display.map(parent, null, clientArea);
			EventQueue.invokeLater(new Runnable () {
				public void run () {
					frame.setBounds (bounds.x, bounds.y, bounds.width, bounds.height);
					frame.validate ();
				}
			});
		}
	});
	return frame;
}

public static Shell new_Shell (final Display display, final Canvas parent) {
	if (display == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	if (parent == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	int /*long*/ handle = 0;
	try {
		loadLibrary ();
		handle = getAWTHandle (parent);
	} catch (Throwable e) {
		SWT.error (SWT.ERROR_NOT_IMPLEMENTED, e);
	}
	if (handle == 0) SWT.error (SWT.ERROR_NOT_IMPLEMENTED);

	final Shell shell = Shell.gtk_new (display, handle);
	parent.addComponentListener(new ComponentAdapter () {
		public void componentResized (ComponentEvent e) {
			display.syncExec (new Runnable () {
				public void run () {
					Dimension dim = parent.getSize ();
					shell.setSize (dim.width, dim.height);
				}
			});
		}
	});
	shell.setVisible (true);
	return shell;
}
}
