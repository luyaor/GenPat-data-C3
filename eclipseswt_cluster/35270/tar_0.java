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

public class NSPrintOperation extends NSObject {

public NSPrintOperation() {
	super();
}

public NSPrintOperation(int /*long*/ id) {
	super(id);
}

public NSPrintOperation(id id) {
	super(id);
}

public void cleanUpOperation() {
	OS.objc_msgSend(this.id, OS.sel_cleanUpOperation);
}

public NSGraphicsContext context() {
	int result = OS.objc_msgSend(this.id, OS.sel_context);
	return result != 0 ? new NSGraphicsContext(result) : null;
}

public NSGraphicsContext createContext() {
	int result = OS.objc_msgSend(this.id, OS.sel_createContext);
	return result != 0 ? new NSGraphicsContext(result) : null;
}

public boolean deliverResult() {
	return OS.objc_msgSend(this.id, OS.sel_deliverResult) != 0;
}

public void destroyContext() {
	OS.objc_msgSend(this.id, OS.sel_destroyContext);
}

public static NSPrintOperation printOperationWithView(NSView view, NSPrintInfo printInfo) {
	int result = OS.objc_msgSend(OS.class_NSPrintOperation, OS.sel_printOperationWithView_printInfo_, view != null ? view.id : 0, printInfo != null ? printInfo.id : 0);
	return result != 0 ? new NSPrintOperation(result) : null;
}

public boolean runOperation() {
	return OS.objc_msgSend(this.id, OS.sel_runOperation) != 0;
}

public static void setCurrentOperation(NSPrintOperation operation) {
	OS.objc_msgSend(OS.class_NSPrintOperation, OS.sel_setCurrentOperation_, operation != null ? operation.id : 0);
}

public void setJobTitle(NSString jobTitle) {
	OS.objc_msgSend(this.id, OS.sel_setJobTitle_, jobTitle != null ? jobTitle.id : 0);
}

public void setShowsPrintPanel(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setShowsPrintPanel_, flag);
}

public void setShowsProgressPanel(boolean flag) {
	OS.objc_msgSend(this.id, OS.sel_setShowsProgressPanel_, flag);
}

}
