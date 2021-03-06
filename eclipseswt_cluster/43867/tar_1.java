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
package org.eclipse.swt.browser;

import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.internal.win32.*;

class WebSite extends OleControlSite {
	COMObject iDocHostUIHandler;
	COMObject iServiceProvider;
	COMObject iInternetSecurityManager;

public WebSite(Composite parent, int style, String progId) {
	super(parent, style, progId);		
}

protected void createCOMInterfaces () {
	super.createCOMInterfaces();
	iDocHostUIHandler = new COMObject(new int[]{2, 0, 0, 4, 1, 5, 0, 0, 1, 1, 1, 3, 3, 2, 2, 1, 3, 2}){
		public int method0(int[] args) {return QueryInterface(args[0], args[1]);}
		public int method1(int[] args) {return AddRef();}
		public int method2(int[] args) {return Release();}
		public int method3(int[] args) {return ShowContextMenu(args[0], args[1], args[2], args[3]);}
		public int method4(int[] args) {return GetHostInfo(args[0]);}
		public int method5(int[] args) {return ShowUI(args[0], args[1], args[2], args[3], args[4]);}
		public int method6(int[] args) {return HideUI();}
		public int method7(int[] args) {return UpdateUI();}
		public int method8(int[] args) {return EnableModeless(args[0]);}
		public int method9(int[] args) {return OnDocWindowActivate(args[0]);}
		public int method10(int[] args) {return OnFrameWindowActivate(args[0]);}
		public int method11(int[] args) {return ResizeBorder(args[0], args[1], args[2]);}
		public int method12(int[] args) {return TranslateAccelerator(args[0], args[1], args[2]);}
		public int method13(int[] args) {return GetOptionKeyPath(args[0], args[1]);}
		public int method14(int[] args) {return GetDropTarget(args[0], args[1]);}
		public int method15(int[] args) {return GetExternal(args[0]);}
		public int method16(int[] args) {return TranslateUrl(args[0], args[1], args[2]);}		
		public int method17(int[] args) {return FilterDataObject(args[0], args[1]);}
	};
	iServiceProvider = new COMObject(new int[]{2, 0, 0, 3}){
		public int method0(int[] args) {return QueryInterface(args[0], args[1]);}
		public int method1(int[] args) {return AddRef();}
		public int method2(int[] args) {return Release();}
		public int method3(int[] args) {return QueryService(args[0], args[1], args[2]);}
	};
	iInternetSecurityManager = new COMObject(new int[]{2, 0, 0, 1, 1, 3, 4, 8, 7, 3, 3}){
		public int method0(int[] args) {return QueryInterface(args[0], args[1]);}
		public int method1(int[] args) {return AddRef();}
		public int method2(int[] args) {return Release();}
		public int method3(int[] args) {return SetSecuritySite(args[0]);}
		public int method4(int[] args) {return GetSecuritySite(args[0]);}
		public int method5(int[] args) {return MapUrlToZone(args[0], args[1], args[2]);}
		public int method6(int[] args) {return GetSecurityId(args[0], args[1], args[2], args[3]);}
		public int method7(int[] args) {return ProcessUrlAction(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);}
		public int method8(int[] args) {return QueryCustomPolicy(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);}
		public int method9(int[] args) {return SetZoneMapping(args[0], args[1], args[2]);}
		public int method10(int[] args) {return GetZoneMappings(args[0], args[1], args[2]);}
	};
}

protected void disposeCOMInterfaces() {
	super.disposeCOMInterfaces();
	if (iDocHostUIHandler != null) {
		iDocHostUIHandler.dispose();
		iDocHostUIHandler = null;
	}
	if (iServiceProvider != null) {
		iServiceProvider.dispose();
		iServiceProvider = null;
	}
	if (iInternetSecurityManager != null) {
		iInternetSecurityManager.dispose();
		iInternetSecurityManager = null;
	}
}

protected int QueryInterface(int riid, int ppvObject) {
	int result = super.QueryInterface(riid, ppvObject);
	if (result == COM.S_OK) return result;
	if (riid == 0 || ppvObject == 0) return COM.E_INVALIDARG;
	GUID guid = new GUID();
	COM.MoveMemory(guid, riid, GUID.sizeof);
	if (COM.IsEqualGUID(guid, COM.IIDIDocHostUIHandler)) {
		COM.MoveMemory(ppvObject, new int[] {iDocHostUIHandler.getAddress()}, 4);
		AddRef();
		return COM.S_OK;
	}
	if (COM.IsEqualGUID(guid, COM.IIDIServiceProvider)) {
		COM.MoveMemory(ppvObject, new int[] {iServiceProvider.getAddress()}, 4);
		AddRef();
		return COM.S_OK;
	}
	COM.MoveMemory(ppvObject, new int[] {0}, 4);
	return COM.E_NOINTERFACE;
}

/* IDocHostUIHandler */

int EnableModeless(int EnableModeless) {
	return COM.E_NOTIMPL;
}

int FilterDataObject(int pDO, int ppDORet) {
	return COM.E_NOTIMPL;
}

int GetDropTarget(int pDropTarget, int ppDropTarget) {
	return COM.E_NOTIMPL;
}

int GetExternal(int ppDispatch) {
	return COM.E_NOTIMPL;
}

int GetHostInfo(int pInfo) {
	return COM.E_NOTIMPL;
}

int GetOptionKeyPath(int pchKey, int dw) {
	return COM.E_NOTIMPL;
}

int HideUI() {
	return COM.E_NOTIMPL;
}

int OnDocWindowActivate(int fActivate) {
	return COM.E_NOTIMPL;
}

int OnFrameWindowActivate(int fActivate) {
	return COM.E_NOTIMPL;
}

int ResizeBorder(int prcBorder, int pUIWindow, int fFrameWindow) {
	return COM.E_NOTIMPL;
}

int ShowContextMenu(int dwID, int ppt, int pcmdtReserved, int pdispReserved) {
	/* disable default popup menu */
	return COM.S_OK;
}

int ShowUI(int dwID, int pActiveObject, int pCommandTarget, int pFrame, int pDoc) {
	return COM.E_NOTIMPL;
}

int TranslateAccelerator(int lpMsg, int pguidCmdGroup, int nCmdID) {
	/*
	* Feature on Internet Explorer.  By default the embedded Internet Explorer control runs
	* the Internet Explorer shortcuts (e.g. F5 for refresh).  This overrides the shortcuts
	* defined by SWT.  The workaround is to forward the accelerator keys to the parent window
	* and have Internet Explorer ignore the ones handled by the parent window.
	*/
	Menu menubar = getShell().getMenuBar();
	if (menubar != null && !menubar.isDisposed() && menubar.isEnabled()) {
		Shell shell = menubar.getShell();
		int hwnd = shell.handle;
		int hAccel = OS.SendMessage(hwnd, OS.WM_APP+1, 0, 0);
		if (hAccel != 0) {
			MSG msg = new MSG();
			OS.MoveMemory(msg, lpMsg, MSG.sizeof);
			if (OS.TranslateAccelerator(hwnd, hAccel, msg) != 0) return COM.S_OK;
		}
	}
	return COM.S_FALSE;
}

int TranslateUrl(int dwTranslate, int pchURLIn, int ppchURLOut) {
	return COM.E_NOTIMPL;
}

int UpdateUI() {
	return COM.E_NOTIMPL;
}

/* IServiceProvider */

int QueryService(int guidService, int riid, int ppvObject) {
	if (riid == 0 || ppvObject == 0) return COM.E_INVALIDARG;
	GUID guid = new GUID();
	COM.MoveMemory(guid, riid, GUID.sizeof);
	if (COM.IsEqualGUID(guid, COM.IIDIInternetSecurityManager)) {
		COM.MoveMemory(ppvObject, new int[] {iInternetSecurityManager.getAddress()}, 4);
		AddRef();
		return COM.S_OK;
	}
	COM.MoveMemory(ppvObject, new int[] {0}, 4);
	return COM.E_NOINTERFACE;
}

/* IInternetSecurityManager */

int SetSecuritySite(int pSite) {
	return Browser.INET_E_DEFAULT_ACTION;
}

int GetSecuritySite(int ppSite) {
	return Browser.INET_E_DEFAULT_ACTION;
}

int MapUrlToZone(int pwszUrl, int pdwZone, int dwFlags) {
	/*
	* Feature in IE 6 sp1.  HTML rendered in memory
	* does not enable local links but the exact same
	* HTML document loaded through a local file is
	* permitted to follow local links.  The workaround is
	* to return URLZONE_INTRANET instead of the default
	* value URLZONE_LOCAL_MACHINE.
	*/
	COM.MoveMemory(pdwZone, new int[] {Browser.URLZONE_INTRANET}, 4);
	return COM.S_OK;
}

int GetSecurityId(int pwszUrl, int pbSecurityId, int pcbSecurityId, int dwReserved) {
	return Browser.INET_E_DEFAULT_ACTION;
}

int ProcessUrlAction(int pwszUrl, int dwAction, int pPolicy, int cbPolicy, int pContext, int cbContext, int dwFlags, int dwReserved) {
	COM.MoveMemory(pPolicy, new int[] {Browser.URLPOLICY_ALLOW}, 4);
	return COM.S_OK;
}

int QueryCustomPolicy(int pwszUrl, int guidKey, int ppPolicy, int pcbPolicy, int pContext, int cbContext, int dwReserved) {
	return Browser.INET_E_DEFAULT_ACTION;
}

int SetZoneMapping(int dwZone, int lpszPattern, int dwFlags) {
	return Browser.INET_E_DEFAULT_ACTION;
}

int GetZoneMappings(int dwZone, int ppenumString, int dwFlags) {
	return COM.E_NOTIMPL;
}
}