/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.cocoa;

public class WebDataSource extends NSObject {

public WebDataSource() {
	super();
}

public WebDataSource(int /*long*/ id) {
	super(id);
}

public WebDataSource(id id) {
	super(id);
}

public NSString pageTitle() {
	int result = OS.objc_msgSend(this.id, OS.sel_pageTitle);
	return result != 0 ? new NSString(result) : null;
}

public WebDocumentRepresentation representation() {
	int result = OS.objc_msgSend(this.id, OS.sel_representation);
	return result != 0 ? new WebDocumentRepresentation(result) : null;
}

public NSMutableURLRequest request() {
	int result = OS.objc_msgSend(this.id, OS.sel_request);
	return result != 0 ? new NSMutableURLRequest(result) : null;
}

public WebFrame webFrame() {
	int result = OS.objc_msgSend(this.id, OS.sel_webFrame);
	return result != 0 ? new WebFrame(result) : null;
}

}
