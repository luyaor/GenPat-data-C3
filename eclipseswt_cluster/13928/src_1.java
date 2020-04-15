/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.widgets.*;

class IE extends WebBrowser {

	OleFrame frame;
	OleControlSite site;
	OleAutomation auto;

	boolean back, forward, navigate, delaySetText, ignoreDispose;
	Point location;
	Point size;
	boolean addressBar = true, menuBar = true, statusBar = true, toolBar = true;
	int info;
	
	int globalDispatch;
	String html;

	static final int BeforeNavigate2 = 0xfa;
	static final int CommandStateChange = 0x69;
	static final int DocumentComplete = 0x103;
	static final int NavigateComplete2 = 0xfc;
	static final int NewWindow2 = 0xfb;
	static final int OnMenuBar = 0x100;
	static final int OnStatusBar = 0x101;
	static final int OnToolBar = 0xff;
	static final int OnVisible = 0xfe;
	static final int ProgressChange = 0x6c;
	static final int RegisterAsBrowser = 0x228;
	static final int StatusTextChange = 0x66;
	static final int TitleChange = 0x71;
	static final int WindowClosing = 0x107;
	static final int WindowSetHeight = 0x10b;
	static final int WindowSetLeft = 0x108;
	static final int WindowSetResizable = 0x106;
	static final int WindowSetTop = 0x109;
	static final int WindowSetWidth = 0x10a;

	static final short CSC_NAVIGATEFORWARD = 1;
	static final short CSC_NAVIGATEBACK = 2;
	static final int INET_E_DEFAULT_ACTION = 0x800C0011;
	static final int READYSTATE_COMPLETE = 4;
	static final int URLPOLICY_ALLOW = 0x00;
	static final int URLPOLICY_DISALLOW = 0x03;
	static final int URLZONE_LOCAL_MACHINE = 0;
	static final int URLZONE_INTRANET = 1;
	static final int URLACTION_ACTIVEX_MIN = 0x00001200;
	static final int URLACTION_ACTIVEX_MAX = 0x000013ff;
	static final int URLACTION_ACTIVEX_RUN = 0x00001200;
	static final int URLACTION_JAVA_MIN = 0x00001C00;
	static final int URLPOLICY_JAVA_LOW = 0x00030000;
	static final int URLACTION_JAVA_MAX = 0x00001Cff;
	
	static final int DISPID_AMBIENT_DLCONTROL = -5512;
	static final int DLCTL_DLIMAGES = 0x00000010;
	static final int DLCTL_VIDEOS = 0x00000020;
	static final int DLCTL_BGSOUNDS = 0x00000040;
	static final int DLCTL_NO_SCRIPTS = 0x00000080;
	static final int DLCTL_NO_JAVA = 0x00000100;
	static final int DLCTL_NO_RUNACTIVEXCTLS = 0x00000200;
	static final int DLCTL_NO_DLACTIVEXCTLS = 0x00000400;
	static final int DLCTL_DOWNLOADONLY = 0x00000800;
	static final int DLCTL_NO_FRAMEDOWNLOAD = 0x00001000;
	static final int DLCTL_RESYNCHRONIZE = 0x00002000;
	static final int DLCTL_PRAGMA_NO_CACHE = 0x00004000;
	static final int DLCTL_FORCEOFFLINE = 0x10000000;
	static final int DLCTL_NO_CLIENTPULL = 0x20000000;
	static final int DLCTL_SILENT = 0x40000000;
	static final int DOCHOSTUIFLAG_THEME = 0x00040000;
	static final int DOCHOSTUIFLAG_NO3DBORDER  = 0x0000004;
	static final int DOCHOSTUIFLAG_NO3DOUTERBORDER = 0x00200000;
	
	static final String ABOUT_BLANK = "about:blank"; //$NON-NLS-1$
	static final String CLSID_SHELLEXPLORER1 = "{EAB22AC3-30C1-11CF-A7EB-0000C05BAE0B}";
	static final String URL_DIRECTOR = "http://download.macromedia.com/pub/shockwave/cabs/director/sw.cab"; //$NON-NLS-1$

	static {
		NativeClearSessions = new Runnable() {
			public void run() {
				OS.InternetSetOption (0, OS.INTERNET_OPTION_END_BROWSER_SESSION, 0, 0);			}
		};
	}

public void create(Composite parent, int style) {
	info = IE.DOCHOSTUIFLAG_THEME;
	if ((style & SWT.BORDER) == 0) info |= IE.DOCHOSTUIFLAG_NO3DOUTERBORDER;
	frame = new OleFrame(browser, SWT.NONE);

	/*
	* Registry entry HKEY_CLASSES_ROOT\Shell.Explorer\CLSID indicates which version of
	* Shell.Explorer to use by default.  We usually want to use this value because it
	* typically points at the newest one that is available.  However it is possible for
	* this registry entry to be changed by another application to point at some other
	* Shell.Explorer version.
	*
	* The Browser depends on the Shell.Explorer version being at least Shell.Explorer.2.
	* If it is detected in the registry to be Shell.Explorer.1 then change the progId that
	* will be embedded to explicitly specify Shell.Explorer.2.
	*/
	String progId = "Shell.Explorer";	//$NON-NLS-1$
	TCHAR key = new TCHAR (0, "Shell.Explorer\\CLSID", true);	//$NON-NLS-1$
	int [] phkResult = new int [1];
	if (OS.RegOpenKeyEx (OS.HKEY_CLASSES_ROOT, key, 0, OS.KEY_READ, phkResult) == 0) {
		int [] lpcbData = new int [1];
		int result = OS.RegQueryValueEx (phkResult [0], null, 0, null, (TCHAR) null, lpcbData);
		if (result == 0) {
			TCHAR lpData = new TCHAR (0, lpcbData [0] / TCHAR.sizeof);
			result = OS.RegQueryValueEx (phkResult [0], null, 0, null, lpData, lpcbData);
			if (result == 0) {
				String clsid = lpData.toString (0, lpData.strlen ());
				if (clsid.equals (CLSID_SHELLEXPLORER1)) {
					/* Shell.Explorer.1 is the default, ensure that Shell.Explorer.2 is available */
					key = new TCHAR (0, "Shell.Explorer.2", true);	//$NON-NLS-1$
					int [] phkResult2 = new int [1];
					if (OS.RegOpenKeyEx (OS.HKEY_CLASSES_ROOT, key, 0, OS.KEY_READ, phkResult2) == 0) {
						/* specify that Shell.Explorer.2 is to be used */
						OS.RegCloseKey (phkResult2 [0]);
						progId = "Shell.Explorer.2";	//$NON-NLS-1$
					}
				}
			}
		}
		OS.RegCloseKey (phkResult [0]);
	}
	try {
		site = new WebSite(frame, SWT.NONE, progId); //$NON-NLS-1$
	} catch (SWTException e) {
		browser.dispose();
		SWT.error(SWT.ERROR_NO_HANDLES);
	}
	
	site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
	auto = new OleAutomation(site);

	Listener listener = new Listener() {
		public void handleEvent(Event e) {
			switch (e.type) {
				case SWT.Dispose: {
					/* make this handler run after other dispose listeners */
					if (ignoreDispose) {
						ignoreDispose = false;
						break;
					}
					ignoreDispose = true;
					browser.notifyListeners (e.type, e);
					e.type = SWT.NONE;
					if (auto != null) auto.dispose();
					auto = null;
					break;
				}
				case SWT.Resize: {
					frame.setBounds(browser.getClientArea());
					break;
				}
				case SWT.KeyDown:
				case SWT.KeyUp: {
					browser.notifyListeners(e.type, e);
					break;
				}
			}
		}
	};
	browser.addListener(SWT.Dispose, listener);
	browser.addListener(SWT.Resize, listener);
	site.addListener(SWT.KeyDown, listener);
	site.addListener(SWT.KeyUp, listener);
	
	OleListener oleListener = new OleListener() {
		public void handleEvent(OleEvent event) {
			if (auto == null) return;		/* receiver was disposed, callback is asynchronous */
			switch (event.type) {
				case BeforeNavigate2: {
					Variant varResult = event.arguments[1];
					String url = varResult.getString();
					LocationEvent newEvent = new LocationEvent(browser);
					newEvent.display = browser.getDisplay();
					newEvent.widget = browser;
					newEvent.location = url;
					newEvent.doit = true;
					for (int i = 0; i < locationListeners.length; i++) {
						locationListeners[i].changing(newEvent);
					}
					Variant cancel = event.arguments[6];
					if (cancel != null) {
						int pCancel = cancel.getByRef();
						COM.MoveMemory(pCancel, new short[]{newEvent.doit ? COM.VARIANT_FALSE : COM.VARIANT_TRUE}, 2);
				   }					
					break;
				}
				case CommandStateChange: {
					boolean enabled = false;
					Variant varResult = event.arguments[0];
					int command = varResult.getInt();
					varResult = event.arguments[1];
					enabled = varResult.getBoolean();
					switch (command) {
						case CSC_NAVIGATEBACK : back = enabled; break;
						case CSC_NAVIGATEFORWARD : forward = enabled; break;
					}
					break;
				}
				case DocumentComplete: {
					Variant varResult = event.arguments[0];
					IDispatch dispatch = varResult.getDispatch();

					varResult = event.arguments[1];
					String url = varResult.getString();
					if (html != null && url.equals(ABOUT_BLANK)) {
						Runnable runnable = new Runnable () {
							public void run() {
								if (browser.isDisposed() || html == null) return;
								int charCount = html.length();
								char[] chars = new char[charCount];
								html.getChars(0, charCount, chars, 0);
								html = null;
								int byteCount = OS.WideCharToMultiByte(OS.CP_UTF8, 0, chars, charCount, null, 0, null, null);
								/*
								* Note. Internet Explorer appears to treat the data loaded with 
								* nsIPersistStreamInit.Load as if it were encoded using the default
								* local charset.  There does not seem to be an API to set the
								* desired charset explicitely in this case.  The fix is to
								* prepend the UTF-8 Byte Order Mark signature to the data.
								*/
								byte[] UTF8BOM = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
								int	hGlobal = OS.GlobalAlloc(OS.GMEM_FIXED | OS.GMEM_ZEROINIT, UTF8BOM.length + byteCount);
								if (hGlobal != 0) {
									OS.MoveMemory(hGlobal, UTF8BOM, UTF8BOM.length);
									OS.WideCharToMultiByte(OS.CP_UTF8, 0, chars, charCount, hGlobal + UTF8BOM.length, byteCount, null, null);							
									int[] ppstm = new int[1];
									/* 
									* Note.  CreateStreamOnHGlobal is called with the flag fDeleteOnRelease.
									* If the call succeeds the buffer hGlobal is freed automatically
									* when the IStream object is released. If the call fails, free the buffer
									* hGlobal.
									*/
									if (OS.CreateStreamOnHGlobal(hGlobal, true, ppstm) == OS.S_OK) {
										int[] rgdispid = auto.getIDsOfNames(new String[] {"Document"}); //$NON-NLS-1$
										Variant pVarResult = auto.getProperty(rgdispid[0]);
										IDispatch dispatchDocument = pVarResult.getDispatch();
										int[] ppvObject = new int[1];
										int result = dispatchDocument.QueryInterface(COM.IIDIPersistStreamInit, ppvObject);
										if (result == OS.S_OK) {
											IPersistStreamInit persistStreamInit = new IPersistStreamInit(ppvObject[0]);
											if (persistStreamInit.InitNew() == OS.S_OK) {
												persistStreamInit.Load(ppstm[0]);
											}
											persistStreamInit.Release();
										}
										pVarResult.dispose();
										/*
										* This code is intentionally commented.  The IDispatch obtained from a Variant
										* did not increase the reference count for the enclosed interface.
										*/
										//dispatchDocument.Release();
										IUnknown stream = new IUnknown(ppstm[0]);
										stream.Release();
									} else {
										OS.GlobalFree(hGlobal);
									}
								}
							}
						};
						if (delaySetText) {
							delaySetText = false;
							browser.getDisplay().asyncExec(runnable);
						} else {
							runnable.run();
						}
					} else {
						Variant variant = new Variant(auto);
						IDispatch top = variant.getDispatch();
						LocationEvent locationEvent = new LocationEvent(browser);
						locationEvent.display = browser.getDisplay();
						locationEvent.widget = browser;
						locationEvent.location = url;
						locationEvent.top = top.getAddress() == dispatch.getAddress();
						for (int i = 0; i < locationListeners.length; i++) {
							locationListeners[i].changed(locationEvent);
						}
						if (browser.isDisposed()) return;
						/*
						 * This code is intentionally commented.  A Variant constructed from an
						 * OleAutomation object does not increase its reference count.  The IDispatch
						 * obtained from this Variant did not increase the reference count for the
						 * OleAutomation instance either. 
						 */
						//top.Release();
						//variant.dispose();
						/*
						 * Note.  The completion of the page loading is detected as
						 * described in the MSDN article "Determine when a page is
						 * done loading in WebBrowser Control". 
						 */
						if (globalDispatch != 0 && dispatch.getAddress() == globalDispatch) {
							/* final document complete */
							globalDispatch = 0;
							ProgressEvent progressEvent = new ProgressEvent(browser);
							progressEvent.display = browser.getDisplay();
							progressEvent.widget = browser;
							for (int i = 0; i < progressListeners.length; i++) {
								progressListeners[i].completed(progressEvent);
							}
						}
					}
											
					/*
					* This code is intentionally commented.  This IDispatch was received
					* as an argument from the OleEvent and it will be disposed along with
					* the other arguments.  
					*/
					//dispatch.Release();
					break;
				}
				case NavigateComplete2: {
					Variant varResult = event.arguments[0];
					IDispatch dispatch = varResult.getDispatch();
					if (globalDispatch == 0) globalDispatch = dispatch.getAddress();
					break;
				}
				case NewWindow2: {
					Variant cancel = event.arguments[1];
					int pCancel = cancel.getByRef();
					WindowEvent newEvent = new WindowEvent(browser);
					newEvent.display = browser.getDisplay();
					newEvent.widget = browser;
					newEvent.required = false;
					for (int i = 0; i < openWindowListeners.length; i++) {
						openWindowListeners[i].open(newEvent);
					}
					IE browser = (IE)newEvent.browser.webBrowser;
					boolean doit = browser != null && !browser.browser.isDisposed(); // TODO
					if (doit) {
						Variant variant = new Variant(browser.auto);
						IDispatch iDispatch = variant.getDispatch();
						Variant ppDisp = event.arguments[0];
						int byref = ppDisp.getByRef();
						if (byref != 0) COM.MoveMemory(byref, new int[] {iDispatch.getAddress()}, 4);
						/*
						* This code is intentionally commented.  A Variant constructed from an
						* OleAutomation object does not increase its reference count.  The IDispatch
						* obtained from this Variant did not increase the reference count for the
						* OleAutomation instance either. 
						*/
						//variant.dispose();
						//iDispatch.Release();
					}
					if (newEvent.required) {
						COM.MoveMemory(pCancel, new short[]{doit ? COM.VARIANT_FALSE : COM.VARIANT_TRUE}, 2);
					}
					break;
				}
				case OnMenuBar: {
					Variant arg0 = event.arguments[0];
					menuBar = arg0.getBoolean();
					break;
				}
				case OnStatusBar: {
					Variant arg0 = event.arguments[0];
					statusBar = arg0.getBoolean();
					break;
				}
				case OnToolBar: {
					Variant arg0 = event.arguments[0];
					toolBar = arg0.getBoolean();
					/*
					* Feature in Internet Explorer.  OnToolBar FALSE is emitted 
					* when both tool bar, address bar and menu bar must not be visible.
					* OnToolBar TRUE is emitted when either of tool bar, address bar
					* or menu bar is visible.
					*/
					if (!toolBar) {
						addressBar = false;
						menuBar = false;
					}
					break;
				}
				case OnVisible: {
					Variant arg1 = event.arguments[0];
					boolean visible = arg1.getBoolean();
					WindowEvent newEvent = new WindowEvent(browser);
					newEvent.display = browser.getDisplay();
					newEvent.widget = browser;
					if (visible) {
						if (addressBar) {
							/*
							* Bug in Internet Explorer.  There is no distinct notification for
							* the address bar.  If neither address, menu or tool bars are visible,
							* OnToolBar FALSE is emitted. For some reason, querying the value of
							* AddressBar in this case returns true even though it should not be
							* set visible.  The workaround is to only query the value of AddressBar
							* when OnToolBar FALSE has not been emitted.
							*/
							int[] rgdispid = auto.getIDsOfNames(new String[] { "AddressBar" }); //$NON-NLS-1$
							Variant pVarResult = auto.getProperty(rgdispid[0]);
							if (pVarResult != null && pVarResult.getType() == OLE.VT_BOOL) addressBar = pVarResult.getBoolean();
						}
						newEvent.addressBar = addressBar;
						newEvent.menuBar = menuBar;
						newEvent.statusBar = statusBar;
						newEvent.toolBar = toolBar;
						newEvent.location = location;
						newEvent.size = size;
						for (int i = 0; i < visibilityWindowListeners.length; i++) {
							visibilityWindowListeners[i].show(newEvent);
						}
						location = null;
						size = null;
					} else {
						for (int i = 0; i < visibilityWindowListeners.length; i++) {
							visibilityWindowListeners[i].hide(newEvent);
						}
					}
					break;
				}
				case ProgressChange: {
					Variant arg1 = event.arguments[0];
					int nProgress = arg1.getType() != OLE.VT_I4 ? 0 : arg1.getInt(); // may be -1
					Variant arg2 = event.arguments[1];
					int nProgressMax = arg2.getType() != OLE.VT_I4 ? 0 : arg2.getInt();
					ProgressEvent newEvent = new ProgressEvent(browser);
					newEvent.display = browser.getDisplay();
					newEvent.widget = browser;
					newEvent.current = nProgress;
					newEvent.total = nProgressMax;
					if (nProgress != -1) {
						for (int i = 0; i < progressListeners.length; i++) {
							progressListeners[i].changed(newEvent);
						}
					}
					break;
				}
				case StatusTextChange: {
					Variant arg1 = event.arguments[0];
					if (arg1.getType() == OLE.VT_BSTR) {
						String text = arg1.getString();
						StatusTextEvent newEvent = new StatusTextEvent(browser);
						newEvent.display = browser.getDisplay();
						newEvent.widget = browser;
						newEvent.text = text;
						for (int i = 0; i < statusTextListeners.length; i++) {
							statusTextListeners[i].changed(newEvent);
						}
					}
					break;
				}
				case TitleChange: {
					Variant arg1 = event.arguments[0];
					if (arg1.getType() == OLE.VT_BSTR) {
						String title = arg1.getString();
						TitleEvent newEvent = new TitleEvent(browser);
						newEvent.display = browser.getDisplay();
						newEvent.widget = browser;
						newEvent.title = title;
						for (int i = 0; i < titleListeners.length; i++) {
							titleListeners[i].changed(newEvent);
						}
					}
					break;
				}
				case WindowClosing: {
					WindowEvent newEvent = new WindowEvent(browser);
					newEvent.display = browser.getDisplay();
					newEvent.widget = browser;
					for (int i = 0; i < closeWindowListeners.length; i++) {
						closeWindowListeners[i].close(newEvent);
					}
					Variant cancel = event.arguments[1];
					int pCancel = cancel.getByRef();
					Variant arg1 = event.arguments[0];
					boolean isChildWindow = arg1.getBoolean();
					COM.MoveMemory(pCancel, new short[]{isChildWindow ? COM.VARIANT_FALSE : COM.VARIANT_TRUE}, 2);
					browser.dispose();
					break;
				}
				case WindowSetHeight: {
					if (size == null) size = new Point(0, 0);
					Variant arg1 = event.arguments[0];
					size.y = arg1.getInt();
					break;
				}
				case WindowSetLeft: {
					if (location == null) location = new Point(0, 0);
					Variant arg1 = event.arguments[0];
					location.x = arg1.getInt();
					break;
				}
				case WindowSetTop: {
					if (location == null) location = new Point(0, 0);
					Variant arg1 = event.arguments[0];
					location.y = arg1.getInt();
					break;
				}
				case WindowSetWidth: {
					if (size == null) size = new Point(0, 0);
					Variant arg1 = event.arguments[0];
					size.x = arg1.getInt();
					break;
				}
			}			
			/*
			* Dispose all arguments passed in the OleEvent.  This must be
			* done to properly release any IDispatch reference that was
			* automatically addRef'ed when constructing the OleEvent.  
			*/
			Variant[] arguments = event.arguments;
			for (int i = 0; i < arguments.length; i++) arguments[i].dispose();
		}
	};
	site.addEventListener(BeforeNavigate2, oleListener);
	site.addEventListener(CommandStateChange, oleListener);
	site.addEventListener(DocumentComplete, oleListener);
	site.addEventListener(NavigateComplete2, oleListener);
	site.addEventListener(NewWindow2, oleListener);
	site.addEventListener(OnMenuBar, oleListener);
	site.addEventListener(OnStatusBar, oleListener);
	site.addEventListener(OnToolBar, oleListener);
	site.addEventListener(OnVisible, oleListener);
	site.addEventListener(ProgressChange, oleListener);
	site.addEventListener(StatusTextChange, oleListener);
	site.addEventListener(TitleChange, oleListener);
	site.addEventListener(WindowClosing, oleListener);
	site.addEventListener(WindowSetHeight, oleListener);
	site.addEventListener(WindowSetLeft, oleListener);
	site.addEventListener(WindowSetTop, oleListener);
	site.addEventListener(WindowSetWidth, oleListener);
	
	Variant variant = new Variant(true);
	auto.setProperty(RegisterAsBrowser, variant);
	variant.dispose();
	
	variant = new Variant(false);
	int[] rgdispid = auto.getIDsOfNames(new String[] {"RegisterAsDropTarget"}); //$NON-NLS-1$
	if (rgdispid != null) auto.setProperty(rgdispid[0], variant);
	variant.dispose();
}

public boolean back() {
	if (!back) return false;
	int[] rgdispid = auto.getIDsOfNames(new String[] { "GoBack" }); //$NON-NLS-1$
	Variant pVarResult = auto.invoke(rgdispid[0]);
	return pVarResult != null && pVarResult.getType() == OLE.VT_EMPTY;
}

public boolean execute(String script) {
	/* get IHTMLDocument2 */
	int[] rgdispid = auto.getIDsOfNames(new String[]{"Document"}); //$NON-NLS-1$
	int dispIdMember = rgdispid[0];
	Variant pVarResult = auto.getProperty(dispIdMember);
	if (pVarResult == null || pVarResult.getType() == COM.VT_EMPTY) return false;
	OleAutomation document = pVarResult.getAutomation();
	pVarResult.dispose();

	/* get IHTMLWindow2 */
	rgdispid = document.getIDsOfNames(new String[]{"parentWindow"}); //$NON-NLS-1$
	if (rgdispid == null) {
		/* implies that browser's content is not a IHTMLDocument2 (eg.- acrobat reader) */
		document.dispose();
		return false;
	}
	dispIdMember = rgdispid[0];
	pVarResult = document.getProperty(dispIdMember);
	OleAutomation ihtmlWindow2 = pVarResult.getAutomation();
	pVarResult.dispose();
	document.dispose();
	
	rgdispid = ihtmlWindow2.getIDsOfNames(new String[] { "execScript", "code" }); //$NON-NLS-1$  //$NON-NLS-2$
	Variant[] rgvarg = new Variant[1];
	rgvarg[0] = new Variant(script);
	int[] rgdispidNamedArgs = new int[1];
	rgdispidNamedArgs[0] = rgdispid[1];
	pVarResult = ihtmlWindow2.invoke(rgdispid[0], rgvarg, rgdispidNamedArgs);
	rgvarg[0].dispose();
	ihtmlWindow2.dispose();
	if (pVarResult == null) return false;
	pVarResult.dispose();
	return true;
}

public boolean forward() {
	if (!forward) return false;
	int[] rgdispid = auto.getIDsOfNames(new String[] { "GoForward" }); //$NON-NLS-1$
	Variant pVarResult = auto.invoke(rgdispid[0]);
	return pVarResult != null && pVarResult.getType() == OLE.VT_EMPTY;
}

public String getUrl() {
	int[] rgdispid = auto.getIDsOfNames(new String[] { "LocationURL" }); //$NON-NLS-1$
	Variant pVarResult = auto.getProperty(rgdispid[0]);
	if (pVarResult == null || pVarResult.getType() != OLE.VT_BSTR)
		return "";
	String result = pVarResult.getString();
	pVarResult.dispose();
	return result;
}

public boolean isBackEnabled() {
	return back;
}

public boolean isForwardEnabled() {
	return forward;
}

public boolean isFocusControl () {
	return site.isFocusControl() || frame.isFocusControl();
}

public void refresh() {
	int[] rgdispid = auto.getIDsOfNames(new String[] { "Refresh" }); //$NON-NLS-1$
	auto.invoke(rgdispid[0]);
}

public boolean setText(String html) {
	/*
	* If the html field is non-null then the about:blank page is already being
	* loaded, so no Stop or Navigate is required.  Just set the html that is to
	* be shown.
	*/
	boolean blankLoading = this.html != null;
	this.html = html;
	if (blankLoading) return true;
	
	/*
	* Navigate to the blank page and insert the given html when
	* receiving the next DocumentComplete notification.  See the
	* MSDN article "Loading HTML content from a Stream".
	* 
	* Note.  Stop any pending request.  This is required to avoid displaying a
	* blank page as a result of consecutive calls to setUrl and/or setText.  
	* The previous request would otherwise render the new html content and
	* reset the html field before the browser actually navigates to the blank
	* page as requested below.
	* 
	* Feature in Internet Explorer.  Stopping pending requests when no request
	* is pending causes a default page 'Action cancelled' to be displayed.  The
	* workaround is to not invoke 'stop' when no request has been set since
	* that instance was created.
	*/
	int[] rgdispid;
	if (navigate) {
		/*
		* Stopping the loading of a page causes DocumentComplete events from previous
		* requests to be received before the DocumentComplete for this page.  In such
		* cases we must be sure to not set the html into the browser too soon, since
		* doing so could result in its page being cleared out by a subsequent
		* DocumentComplete.  The Browser's ReadyState can be used to determine whether
		* these extra events will be received or not.
		*/
		rgdispid = auto.getIDsOfNames(new String[] { "ReadyState" }); //$NON-NLS-1$
		Variant pVarResult = auto.getProperty(rgdispid[0]);
		if (pVarResult == null) return false;
		delaySetText = pVarResult.getInt() != READYSTATE_COMPLETE;
		pVarResult.dispose();
		rgdispid = auto.getIDsOfNames(new String[] { "Stop" }); //$NON-NLS-1$
		auto.invoke(rgdispid[0]);
	}
	rgdispid = auto.getIDsOfNames(new String[] { "Navigate", "URL" }); //$NON-NLS-1$ //$NON-NLS-2$
	navigate = true;
	Variant[] rgvarg = new Variant[1];
	rgvarg[0] = new Variant(ABOUT_BLANK);
	int[] rgdispidNamedArgs = new int[1];
	rgdispidNamedArgs[0] = rgdispid[1];
	Variant pVarResult = auto.invoke(rgdispid[0], rgvarg, rgdispidNamedArgs);
	rgvarg[0].dispose();
	if (pVarResult == null) return false;
	boolean result = pVarResult.getType() == OLE.VT_EMPTY;
	pVarResult.dispose();
	return result;
}

public boolean setUrl(String url) {
	html = null;

	/*
	* Bug in Internet Explorer.  For some reason, Navigating to an xml document before
	* a previous Navigate has completed will leave the Browser in a bad state if the
	* Navigate to the xml document does not complete.  This bad state causes a GP when
	* the parent window is eventually disposed.  The workaround is to issue a Stop before
	* navigating to any xml document. 
	*/
	if (url.endsWith(".xml")) {	//$NON-NLS-1$
		/*
		* Feature in Internet Explorer.  Stopping pending requests when no request has been
		* issued causes a default 'Action cancelled' page to be displayed.  Since Stop must
		* be issued here, the workaround is to first Navigate to the about:blank page before
		* issuing Stop so that the 'Action cancelled' page is not displayed.
		*/
		if (!navigate) {
			int[] rgdispid = auto.getIDsOfNames(new String[] { "Navigate", "URL" }); //$NON-NLS-1$ //$NON-NLS-2$
			Variant[] rgvarg = new Variant[1];
			rgvarg[0] = new Variant(ABOUT_BLANK);
			int[] rgdispidNamedArgs = new int[1];
			rgdispidNamedArgs[0] = rgdispid[1];
			auto.invoke(rgdispid[0], rgvarg, rgdispidNamedArgs);
			rgvarg[0].dispose();
		}
		int[] rgdispid = auto.getIDsOfNames(new String[] { "Stop" }); //$NON-NLS-1$
		auto.invoke(rgdispid[0]);
	}

	int[] rgdispid = auto.getIDsOfNames(new String[] { "Navigate", "URL" }); //$NON-NLS-1$ //$NON-NLS-2$
	navigate = true;
	Variant[] rgvarg = new Variant[1];
	rgvarg[0] = new Variant(url);
	int[] rgdispidNamedArgs = new int[1];
	rgdispidNamedArgs[0] = rgdispid[1];
	Variant pVarResult = auto.invoke(rgdispid[0], rgvarg, rgdispidNamedArgs);
	rgvarg[0].dispose();
	if (pVarResult == null) return false;
	boolean result = pVarResult.getType() == OLE.VT_EMPTY;
	pVarResult.dispose();
	return result;
}

public void stop() {
	int[] rgdispid = auto.getIDsOfNames(new String[] { "Stop" }); //$NON-NLS-1$
	auto.invoke(rgdispid[0]);
}
}
