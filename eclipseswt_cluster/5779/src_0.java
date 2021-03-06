/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.gtk.OS;

class BrowserFactory {

WebBrowser createWebBrowser (int style) {
	boolean webkitInstalled = WebKit.IsInstalled ();
	if ((style & SWT.MOZILLA) != 0 || (!webkitInstalled && (style & SWT.WEBKIT) == 0)) {
		if (OS.GTK3) return null; // avoid libxul.so crash with Eclipse/SWT GTK 3 (XULRunner is not ported to GTK 3 yet)
		return new Mozilla ();
	}
	if (!webkitInstalled) return null;

	return new WebKit ();
}

}
