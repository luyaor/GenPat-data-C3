/*******************************************************************************
 * Copyright (c) 2003, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.browser;

import org.eclipse.swt.*;
import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.mozilla.*;
import org.eclipse.swt.widgets.*;

/**
 * This class implements the nsIHelperAppLauncherDialog interface for mozilla
 * versions 1.4 - 1.8.x.  For mozilla versions >= 1.9 this interface is
 * implemented by class HelperAppLauncherDialog_1_9.  HelperAppLauncherDialogFactory
 * determines at runtime which of these classes to instantiate. 
 */
class HelperAppLauncherDialog {
	XPCOMObject supports;
	XPCOMObject helperAppLauncherDialog;
	int refCount = 0;

HelperAppLauncherDialog () {
	createCOMInterfaces ();
}

int AddRef () {
	refCount++;
	return refCount;
}

void createCOMInterfaces () {
	/* Create each of the interfaces that this object implements */
	supports = new XPCOMObject (new int[] {2, 0, 0}) {
		@Override
		public long /*int*/ method0 (long /*int*/[] args) {return QueryInterface (args[0], args[1]);}
		@Override
		public long /*int*/ method1 (long /*int*/[] args) {return AddRef ();}
		@Override
		public long /*int*/ method2 (long /*int*/[] args) {return Release ();}
	};
	
	helperAppLauncherDialog = new XPCOMObject (new int[] {2, 0, 0, 3, 5}) {
		@Override
		public long /*int*/ method0 (long /*int*/[] args) {return QueryInterface (args[0], args[1]);}
		@Override
		public long /*int*/ method1 (long /*int*/[] args) {return AddRef ();}
		@Override
		public long /*int*/ method2 (long /*int*/[] args) {return Release ();}
		@Override
		public long /*int*/ method3 (long /*int*/[] args) {return Show (args[0], args[1], (int)/*64*/args[2]);}
		@Override
		public long /*int*/ method4 (long /*int*/[] args) {return PromptForSaveToFile (args[0], args[1], args[2], args[3], args[4]);}
	};		
}

void disposeCOMInterfaces () {
	if (supports != null) {
		supports.dispose ();
		supports = null;
	}	
	if (helperAppLauncherDialog != null) {
		helperAppLauncherDialog.dispose ();
		helperAppLauncherDialog = null;	
	}
}

long /*int*/ getAddress () {
	return helperAppLauncherDialog.getAddress ();
}

int QueryInterface (long /*int*/ riid, long /*int*/ ppvObject) {
	if (riid == 0 || ppvObject == 0) return XPCOM.NS_ERROR_NO_INTERFACE;
	nsID guid = new nsID ();
	XPCOM.memmove (guid, riid, nsID.sizeof);
	
	if (guid.Equals (IIDStore.GetIID (nsISupports.class))) {
		XPCOM.memmove (ppvObject, new long /*int*/[] {supports.getAddress ()}, C.PTR_SIZEOF);
		AddRef ();
		return XPCOM.NS_OK;
	}
	if (guid.Equals (XPCOM.NS_IHELPERAPPLAUNCHERDIALOG_IID)) {
		XPCOM.memmove (ppvObject, new long /*int*/[] {helperAppLauncherDialog.getAddress ()}, C.PTR_SIZEOF);
		AddRef ();
		return XPCOM.NS_OK;
	}
	
	XPCOM.memmove (ppvObject, new long /*int*/[] {0}, C.PTR_SIZEOF);
	return XPCOM.NS_ERROR_NO_INTERFACE;
}
        	
int Release () {
	refCount--;
	/*
	* Note.  This instance lives as long as the download it is binded to.
	* Its reference count is expected to go down to 0 when the download
	* has completed or when it has been cancelled. E.g. when the user
	* cancels the File Dialog, cancels or closes the Download Dialog
	* and when the Download Dialog goes away after the download is completed.
	*/
	if (refCount == 0) disposeCOMInterfaces ();
	return refCount;
}

/* nsIHelperAppLauncherDialog */

int Show (long /*int*/ aLauncher, long /*int*/ aContext, int aReason) {
	/*
	 * The interface for nsIHelperAppLauncher changed as of mozilla 1.8.  Query the received
	 * nsIHelperAppLauncher for the new interface, and if it is not found then fall back to
	 * the old interface. 
	 */
	nsISupports supports = new nsISupports (aLauncher);
	long /*int*/[] result = new long /*int*/[1];
	int rc = supports.QueryInterface (IIDStore.GetIID (nsIHelperAppLauncher_1_8.class), result);
	if (rc == XPCOM.NS_OK) {	/* >= 1.8 */
		nsIHelperAppLauncher_1_8 helperAppLauncher = new nsIHelperAppLauncher_1_8 (aLauncher);
		rc = helperAppLauncher.SaveToDisk (0, 0);
		helperAppLauncher.Release ();
		return rc;
	}
	nsIHelperAppLauncher helperAppLauncher = new nsIHelperAppLauncher (aLauncher);	/* < 1.8 */
	return helperAppLauncher.SaveToDisk (0, 0);
}

int PromptForSaveToFile (long /*int*/ arg0, long /*int*/ arg1, long /*int*/ arg2, long /*int*/ arg3, long /*int*/ arg4) {
	long /*int*/ aDefaultFile, aSuggestedFileExtension, _retval;
	boolean hasLauncher = false;

	/*
	* The interface for nsIHelperAppLauncherDialog changed as of mozilla 1.5 when an
	* extra argument was added to the PromptForSaveToFile method (this resulted in all
	* subsequent arguments shifting right).  The workaround is to provide an XPCOMObject 
	* that fits the newer API, and to use the first argument's type to infer whether
	* the old or new nsIHelperAppLauncherDialog interface is being used (and by extension
	* the ordering of the arguments).  In mozilla >= 1.5 the first argument is an
	* nsIHelperAppLauncher. 
	*/
	/*
	 * The interface for nsIHelperAppLauncher changed as of mozilla 1.8, so the first
	 * argument must be queried for both the old and new nsIHelperAppLauncher interfaces. 
	 */
 	boolean using_1_8 = false;
	nsISupports support = new nsISupports (arg0);
	long /*int*/[] result = new long /*int*/[1];
	int rc = support.QueryInterface (IIDStore.GetIID (nsIHelperAppLauncher_1_8.class), result);
	if (rc == XPCOM.NS_OK) {
		using_1_8 = true;
		hasLauncher = true;
		new nsISupports (result[0]).Release ();
	} else {
		result[0] = 0;
		rc = support.QueryInterface (IIDStore.GetIID (nsIHelperAppLauncher.class, MozillaVersion.VERSION_BASE), result);
		if (rc == XPCOM.NS_OK) {
			hasLauncher = true;
			new nsISupports (result[0]).Release ();
		}
	}
	result[0] = 0;

	if (hasLauncher) {	/* >= 1.5 */
		aDefaultFile = arg2;
		aSuggestedFileExtension = arg3;
		_retval = arg4;
	} else {			/* 1.4 */
		aDefaultFile = arg1;
		aSuggestedFileExtension = arg2;
		_retval = arg3;
	}

	int length = XPCOM.strlen_PRUnichar (aDefaultFile);
	char[] dest = new char[length];
	XPCOM.memmove (dest, aDefaultFile, length * 2);
	String defaultFile = new String (dest);

	length = XPCOM.strlen_PRUnichar (aSuggestedFileExtension);
	dest = new char[length];
	XPCOM.memmove (dest, aSuggestedFileExtension, length * 2);
	String suggestedFileExtension = new String (dest);

	Shell shell = new Shell ();
	FileDialog fileDialog = new FileDialog (shell, SWT.SAVE);
	fileDialog.setFileName (defaultFile);
	fileDialog.setFilterExtensions (new String[] {suggestedFileExtension});
	String name = fileDialog.open ();
	shell.close ();
	if (name == null) {
		if (hasLauncher) {
			if (using_1_8) {
				nsIHelperAppLauncher_1_8 launcher = new nsIHelperAppLauncher_1_8 (arg0);
				rc = launcher.Cancel (XPCOM.NS_BINDING_ABORTED);
			} else {
				nsIHelperAppLauncher launcher = new nsIHelperAppLauncher (arg0);
				rc = launcher.Cancel ();
			}
			if (rc != XPCOM.NS_OK) Mozilla.error (rc);
			return XPCOM.NS_OK;
		}
		return XPCOM.NS_ERROR_FAILURE;
	}
	nsEmbedString path = new nsEmbedString (name);
	rc = XPCOM.NS_NewLocalFile (path.getAddress (), 1, result);
	path.dispose ();
	if (rc != XPCOM.NS_OK) Mozilla.error (rc);
	if (result[0] == 0) Mozilla.error (XPCOM.NS_ERROR_NULL_POINTER);
	/* Our own nsIDownload has been registered during the Browser initialization. It will be invoked by Mozilla. */
	XPCOM.memmove (_retval, result, C.PTR_SIZEOF);	
	return XPCOM.NS_OK;
}
}
