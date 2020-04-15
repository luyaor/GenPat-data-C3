package org.eclipse.swt.widgets;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.carbon.*;

/**
 * This class is the abstract superclass of all classes which
 * represent controls that have standard scroll bars.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>H_SCROLL, V_SCROLL</dd>
 * <dt><b>Events:</b>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the SWT implementation.
 * </p>
 */
public abstract class Scrollable extends Control {
 	int scrolledHandle /* formHandle */;
	int hScrollBar, vScrollBar;
	ScrollBar horizontalBar, verticalBar;
Scrollable () {
	/* Do nothing */
}
/**
 * Constructs a new instance of this class given its parent
 * and a style value describing its behavior and appearance.
 * <p>
 * The style value is either one of the style constants defined in
 * class <code>SWT</code> which is applicable to instances of this
 * class, or must be built by <em>bitwise OR</em>'ing together 
 * (that is, using the <code>int</code> "|" operator) two or more
 * of those <code>SWT</code> style constants. The class description
 * lists the style constants that are applicable to the class.
 * Style bits are also inherited from superclasses.
 * </p>
 *
 * @param parent a composite control which will be the parent of the new instance (cannot be null)
 * @param style the style of control to construct
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see SWT#H_SCROLL
 * @see SWT#V_SCROLL
 * @see Widget#checkSubclass
 * @see Widget#getStyle
 */
public Scrollable (Composite parent, int style) {
	super (parent, style);
}
/**
 * Given a desired <em>client area</em> for the receiver
 * (as described by the arguments), returns the bounding
 * rectangle which would be required to produce that client
 * area.
 * <p>
 * In other words, it returns a rectangle such that, if the
 * receiver's bounds were set to that rectangle, the area
 * of the receiver which is capable of displaying data
 * (that is, not covered by the "trimmings") would be the
 * rectangle described by the arguments (relative to the
 * receiver's parent).
 * </p>
 * 
 * @return the required bounds to produce the given client area
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #getClientArea
 */
public Rectangle computeTrim (int x, int y, int width, int height) {
	checkWidget();
	int border = getBorderWidth ();
	int trimX = x - border, trimY = y - border;
	int trimWidth = width + (border * 2), trimHeight = height + (border * 2);
	if (horizontalBar != null) {
		Display display = getDisplay ();
		trimY -= display.scrolledInsetY;
		trimHeight += display.scrolledInsetY + display.scrolledMarginY;
		if (verticalBar == null) {
			trimX -= display.scrolledInsetX;
			trimWidth += display.scrolledInsetX * 2;
			trimHeight -= display.scrolledInsetY * 2;
		}
	}
	if (verticalBar != null) {
		Display display = getDisplay ();
		trimX -= display.scrolledInsetX;
		trimWidth += display.scrolledInsetX + display.scrolledMarginX;
		if (horizontalBar == null) {
			trimY -= display.scrolledInsetY;
			trimHeight += display.scrolledInsetY * 2;
			trimWidth -= display.scrolledInsetX * 2;
		}
	}
	return new Rectangle (trimX, trimY, trimWidth, trimHeight);
}
ScrollBar createScrollBar (int type) {
    return new ScrollBar (this, type);
}
ScrollBar createStandardBar (int style) {
	if (scrolledHandle == 0) return null;
	ScrollBar bar = new ScrollBar ();
	bar.parent = this;
	bar.style = style;
	bar.state |= HANDLE;
	System.out.println("Scrollable.createStandardBar: nyi");
    /* AW
	int [] argList = {OS.XmNhorizontalScrollBar, 0, OS.XmNverticalScrollBar, 0};
	OS.XtGetValues (scrolledHandle, argList, argList.length / 2);
	if (style == SWT.H_SCROLL) bar.handle = argList [1];
	if (style == SWT.V_SCROLL) bar.handle = argList [3];
    */
	bar.hookEvents ();
	bar.register ();
	return bar;
}
void createWidget (int index) {
	super.createWidget (index);
	if ((style & SWT.H_SCROLL) != 0) horizontalBar = createScrollBar (SWT.H_SCROLL);
	if ((style & SWT.V_SCROLL) != 0) verticalBar = createScrollBar (SWT.V_SCROLL);
}
void deregister () {
	super.deregister ();
    /* AW
	if (formHandle != 0) WidgetTable.remove (formHandle);
    */
	if (scrolledHandle != 0) WidgetTable.remove (scrolledHandle);
}
/* AW
void enableWidget (boolean enabled) {
	super.enableWidget (enabled);
	if (formHandle != 0) enableHandle (enabled, formHandle);
	if (scrolledHandle != 0) {
		enableHandle (enabled, scrolledHandle);
		int [] argList = {
			OS.XmNhorizontalScrollBar, 0,
			OS.XmNverticalScrollBar, 0,
		};
		OS.XtGetValues (scrolledHandle, argList, argList.length / 2);
		if (argList [1] != 0) enableHandle (enabled, argList [1]);
		if (argList [3] != 0) enableHandle (enabled, argList [3]);
	}
}
*/
/**
 * Returns a rectangle which describes the area of the
 * receiver which is capable of displaying data (that is,
 * not covered by the "trimmings").
 * 
 * @return the client area
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see #computeTrim
 */
public Rectangle getClientArea () {
	checkWidget();
    /* AW
	int [] argList = {OS.XmNwidth, 0, OS.XmNheight, 0};
	OS.XtGetValues (handle, argList, argList.length / 2);
	return new Rectangle (0, 0, argList [1], argList [3]);
    */
	Rect bounds= new Rect();
	OS.GetControlBounds(handle, bounds);
	Rectangle r= new Rectangle (0, 0, bounds.right-bounds.left, bounds.bottom-bounds.top);
	/*
	if (r.isEmpty()) {
		System.out.println("Scrollable.getClientArea(" + this + "): " + r);
		//new Exception().printStackTrace();
	}
	*/
	return r;
}
/**
 * Returns the receiver's horizontal scroll bar if it has
 * one, and null if it does not.
 *
 * @return the horizontal scroll bar (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public ScrollBar getHorizontalBar () {
	checkWidget();
	return horizontalBar;
}
/**
 * Returns the receiver's vertical scroll bar if it has
 * one, and null if it does not.
 *
 * @return the vertical scroll bar (or null)
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public ScrollBar getVerticalBar () {
	checkWidget();
	return verticalBar;
}
boolean isTabGroup () {
	if ((state & CANVAS) != 0) return true;
	return super.isTabGroup ();
}
void manageChildren () {
    /* AW
	if (scrolledHandle != 0) {
		OS.XtSetMappedWhenManaged (scrolledHandle, false);
		OS.XtManageChild (scrolledHandle);
	}
	if (formHandle != 0) {
		OS.XtSetMappedWhenManaged (formHandle, false);
		OS.XtManageChild (formHandle);
	}
    */
	super.manageChildren ();
    /* AW
	if (formHandle != 0) {
		int [] argList = {OS.XmNborderWidth, 0};
		OS.XtGetValues (formHandle, argList, argList.length / 2);
		OS.XtResizeWidget (formHandle, 1, 1, argList [1]);
		OS.XtSetMappedWhenManaged (formHandle, true);
	}
	if (scrolledHandle != 0) {
		int [] argList = {OS.XmNborderWidth, 0};
		OS.XtGetValues (scrolledHandle, argList, argList.length / 2);
		OS.XtResizeWidget (scrolledHandle, 1, 1, argList [1]);
		OS.XtSetMappedWhenManaged (scrolledHandle, true);
	}
    */
}
/* AW
void propagateWidget (boolean enabled) {
	super.propagateWidget (enabled);
	if (formHandle != 0) propagateHandle (enabled, formHandle);
	if (scrolledHandle != 0) {
		propagateHandle (enabled, scrolledHandle);
		int [] argList = {
			OS.XmNhorizontalScrollBar, 0,
			OS.XmNverticalScrollBar, 0,
		};
		OS.XtGetValues (scrolledHandle, argList, argList.length / 2);
		if (argList [1] != 0) propagateHandle (enabled, argList [1]);
		if (argList [3] != 0) propagateHandle (enabled, argList [3]);
	}
}
*/
void register () {
	super.register ();
    /* AW
	if (formHandle != 0) WidgetTable.put (formHandle, this);
    */
	if (scrolledHandle != 0) WidgetTable.put (scrolledHandle, this);
}
void releaseHandle () {
	super.releaseHandle ();
	scrolledHandle = /* AW formHandle = */ 0;
}
void releaseWidget () {
	if (horizontalBar != null) {
		horizontalBar.releaseWidget ();
		horizontalBar.releaseHandle ();
	}
	if (verticalBar != null) {
		verticalBar.releaseWidget ();
		verticalBar.releaseHandle ();
	}
	horizontalBar = verticalBar = null;
	super.releaseWidget ();
}
/* AW
void setBackgroundPixel (int pixel) {
	super.setBackgroundPixel (pixel);
	if (scrolledHandle != 0) {
		int [] argList1 = {
			OS.XmNhorizontalScrollBar, 0,
			OS.XmNverticalScrollBar, 0,
		};
		OS.XtGetValues (scrolledHandle, argList1, argList1.length / 2);
		if (argList1 [1] != 0) OS.XmChangeColor (argList1 [1], pixel);
		if (argList1 [3] != 0) OS.XmChangeColor (argList1 [3], pixel);
	}
}
*/
int topHandle () {
	if (scrolledHandle != 0) return scrolledHandle;
    /* AW
	if (formHandle != 0) return formHandle;
    */
	return handle;
}

////////////////////////////
// Mac Stuff
////////////////////////////

	int createScrollView(int parentHandle, int style) {
		
		int features= OS.kControlSupportsEmbedding;
		//features |= OS.kControlSupportsFocus | OS.kControlGetsFocusOnClick
        int controlHandle = OS.NewControl(0, new Rect(), null, false, (short)features, (short)0, (short)0, (short)OS.kControlUserPaneProc, 0);
 		MacUtil.insertControl(controlHandle, parentHandle, -1);
		OS.HIViewSetVisible(controlHandle, true);
		
		if ((style & SWT.H_SCROLL) != 0) {
			hScrollBar= OS.NewControl(0, new Rect(), null, false, (short)0, (short)0, (short)100, (short)OS.kControlScrollBarLiveProc, 0);
 			MacUtil.insertControl(hScrollBar, controlHandle, -1);
			OS.HIViewSetVisible(hScrollBar, true);
			OS.SetControlAction(hScrollBar, getDisplay().fControlActionProc);
		}

		if ((style & SWT.V_SCROLL) != 0) {
			vScrollBar= OS.NewControl(0, new Rect(), null, false, (short)0, (short)0, (short)100, (short)OS.kControlScrollBarLiveProc, 0);
 			MacUtil.insertControl(vScrollBar, controlHandle, -1);
			OS.HIViewSetVisible(vScrollBar, true);
			OS.SetControlAction(vScrollBar, getDisplay().fControlActionProc);
		}
				
		return controlHandle;
	}

	void handleResize(int handle, Rect bounds) {
		super.handleResize(handle, bounds);
		_relayout();
	}
	
	void _relayout() {
		
		int hndl= scrolledHandle;
		if (hndl == 0)
			return;
		
		Rect bounds= new Rect();
		OS.GetControlBounds(hndl, bounds);
		
		boolean visible= OS.IsControlVisible(hndl);
		
		int x= 0;
		int y= 0;
		int w= bounds.right-bounds.left;
		int h= bounds.bottom-bounds.top;
	
		int s= 15;
		int ww= w;
		int hh= h;
		int style= getStyle();

		ScrollBar hsb= null;
		if ((style & SWT.H_SCROLL) != 0) {
			hsb= getHorizontalBar();
			if (hsb != null) {
				if (visible && !OS.IsControlVisible(hsb.handle))
					;
				else
					hh-= s;
			}
		}

		ScrollBar vsb= null;
		if ((style & SWT.V_SCROLL) != 0) {
			vsb= getVerticalBar();
			if (vsb != null) {
				if (visible && !OS.IsControlVisible(vsb.handle))
					;
				else
					ww-= s;
			}
		}

		CGRect rect = new CGRect();
		rect.x = x;
		rect.y = y;
		rect.width = ww;
		rect.height = hh;
		OS.HIViewSetFrame(handle, rect);

		if (hsb != null) {
			rect.x = x;
			rect.y = y+h-s;
			rect.width = ww;
			rect.height = s;
			OS.HIViewSetFrame(hsb.handle, rect);
		}
			
		if (vsb != null) {
			rect.x = x+w-s;
			rect.y = y;
			rect.width = s;
			rect.height = hh;
			OS.HIViewSetFrame(vsb.handle, rect);
		}
	}
}
