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
package org.eclipse.swt.custom;


import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;

/**
* DO NOT USE - UNDER CONSTRUCTION
*
* @since 3.0
*/

/**
 * Instances of this class implement a Composite that ...
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>,
 * it does not make sense to set a layout on it.
 * </p><p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NONE</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(None)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */

public class CBanner extends Composite {	

	Control left;
	Control right;
	Control bottom;
	
	int[] curve;
	int curveWidth = 0;
	int curveIndent = 0;
	int curveStart = 0;
	Rectangle curveRect = new Rectangle(0, 0, 0, 0);
	
	int rightWidth = SWT.DEFAULT;
	Cursor resizeCursor;
	boolean dragging = false;
	int rightDragDisplacement = 0;
	
	static final int OFFSCREEN = -200;
	static final int BORDER_BOTTOM = 2;
	static final int BORDER_TOP = 3;
	static final int BORDER_STRIPE = 1;
	static final int CURVE_TAIL = 200;
	
	static RGB BORDER1 = null;
	
		
/**
 * DO NOT USE - UNDER CONSTRUCTION
 *
 * @param parent a widget which will be the parent of the new instance (cannot be null)
 * @param style the style of widget to construct
 * 
 * @since 3.0
 */
public CBanner(Composite parent, int style) {
	super(parent, checkStyle(style));
	if (BORDER1 == null) BORDER1 = getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW).getRGB();
	resizeCursor = new Cursor(getDisplay(), SWT.CURSOR_SIZEWE);
	
	Listener listener = new Listener() {
		public void handleEvent(Event e) {
			switch (e.type) {
				case SWT.Dispose:
					onDispose(); break;
				case SWT.MouseDown:
					onMouseDown (e.x, e.y); break;
				case SWT.MouseExit:
					onMouseExit(); break;
				case SWT.MouseMove:
					onMouseMove(e.x, e.y); break;
				case SWT.MouseUp:
					onMouseUp(); break;
				case SWT.Paint:
					onPaint(e.gc); break;
				case SWT.Resize:
					onResize(); break;
			}
		}
	};
	int[] events = new int[] {SWT.Dispose, SWT.MouseDown, SWT.MouseExit, SWT.MouseMove, SWT.MouseUp, SWT.Paint, SWT.Resize};
	for (int i = 0; i < events.length; i++) {
		addListener(events[i], listener);
	}
}
static int checkStyle (int style) {
	return SWT.NONE;
}
public Point computeSize(int wHint, int hHint, boolean changed) {
	checkWidget();
	int height = hHint;
	int width = wHint;
	Point bottomSize = new Point(0, 0);
	if (bottom != null) {
		Point trim = bottom.computeSize(width, hHint);
		trim.x = trim.x - width;
		bottomSize = bottom.computeSize(width == SWT.DEFAULT ? SWT.DEFAULT : width - trim.x, SWT.DEFAULT);
		if (height != SWT.DEFAULT) {
			bottomSize.y = Math.min(bottomSize.y, height);
			height -= bottomSize.y + BORDER_TOP + BORDER_STRIPE + BORDER_BOTTOM;
		}
	}
	if (curve == null) {
		if (height == SWT.DEFAULT) {
			if (left != null) {
				Point s = left.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				updateCurve(s.y + BORDER_TOP + BORDER_BOTTOM + 2*BORDER_STRIPE);
			}
		} else {
			updateCurve(height);
		}
	} else {
		updateCurve(getSize().y);
	}
	if (height != SWT.DEFAULT && left != null && right != null) {
		height -= BORDER_TOP + BORDER_BOTTOM + 2*BORDER_STRIPE;
	}
	Point rightSize = new Point(0, 0);
	if (right != null) {
		Point trim = right.computeSize(rightWidth, height);
		trim.x = trim.x - rightWidth;
		rightSize = right.computeSize(rightWidth == SWT.DEFAULT ? SWT.DEFAULT : rightWidth - trim.x, rightWidth == SWT.DEFAULT ? SWT.DEFAULT : height);
		if (width != SWT.DEFAULT) {
			rightSize.x = Math.min(rightSize.x, width);
			width = Math.max(CURVE_TAIL, width - rightSize.x - curveWidth + 2* curveIndent);
		}
	}
	Point leftSize = new Point(0, 0);
	if (left != null && (width == SWT.DEFAULT || width > 0)) {
		Point trim = left.computeSize(width, height);
		trim.x = trim.x - width;
		leftSize = left.computeSize(width == SWT.DEFAULT ? SWT.DEFAULT : width - trim.x, height);
	}
	Point size = new Point(0, 0);
	size.x = leftSize.x;
	if (left != null && right!= null) size.x += curveWidth - 2*curveIndent;
	size.x += rightSize.x;
	size.y = leftSize.y > 0 ? leftSize.y : rightSize.y;
	if (left != null && right!= null) size.y +=  BORDER_TOP + BORDER_BOTTOM + 2*BORDER_STRIPE;
	size.y += bottomSize.y;
	if (bottom != null && (left != null || right != null)) size.y += BORDER_TOP + BORDER_BOTTOM + BORDER_STRIPE;
	
	if (wHint != SWT.DEFAULT) size.x = wHint;
	if (hHint != SWT.DEFAULT) size.y = hHint;
	
	return new Point(size.x, size.y);
}
public Rectangle computeTrim (int x, int y, int width, int height) {
	checkWidget ();
	return new Rectangle(x, y, width, height);
}
/**
* @since 3.0
*/
public Control getBottom() {
	checkWidget();
	return bottom;
}
public Rectangle getClientArea() {
	return new Rectangle(0, 0, 0, 0);
}

/**
* Returns the Control that appears on the left side of the banner.
* 
* @return the control that appears on the left side of the banner or null
* 
* @exception SWTException <ul>
*    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
*    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
* </ul>
* 
* @since 3.0
*/
public Control getLeft() {
	checkWidget();
	return left;
}

/**
* Returns the Control that appears on the right side of the banner.
* 
* @return the control that appears on the right side of the banner or null
* 
* @exception SWTException <ul>
*    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
*    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
* </ul>
* 
* @since 3.0
*/
public Control getRight() {
	checkWidget();
	return right;
}
public int getRightWidth() {
	checkWidget();
	if (right == null) return 0;
	if (rightWidth == SWT.DEFAULT) return right.computeSize(SWT.DEFAULT, getSize().y).x;
	return rightWidth;
}
public void layout (boolean changed) {
	checkWidget();
	Point size = getSize();
	boolean showCurve = left != null && right != null;
	int width = size.x;
	int height = size.y;
	
	Point bottomSize = new Point(0, 0);
	if (bottom != null) {
		Point trim = bottom.computeSize(width, SWT.DEFAULT);
		trim.x = trim.x - width;
		bottomSize = bottom.computeSize(width, SWT.DEFAULT);
		bottomSize.y = Math.min(bottomSize.y, height);
		height -= bottomSize.y + BORDER_TOP + BORDER_BOTTOM + BORDER_STRIPE;
	}
	
	if (showCurve) height -=  BORDER_TOP + BORDER_BOTTOM + 2*BORDER_STRIPE;
	Point rightSize = new Point(0,0);
	if (right != null) {
		Point trim = right.computeSize(rightWidth, height);
		trim.x = trim.x - rightWidth;
		rightSize = right.computeSize(rightWidth == SWT.DEFAULT ? SWT.DEFAULT : rightWidth - trim.x, rightWidth == SWT.DEFAULT ? SWT.DEFAULT : height);
		rightSize.x = Math.min(rightSize.x, width);
		width -= rightSize.x + curveWidth - 2* curveIndent;
		width = Math.max(width, CURVE_TAIL); 
	}

	Point leftSize = new Point(0, 0);
	if (left != null) {
		Point trim = left.computeSize(width, height);
		trim.x = trim.x - width;
		leftSize = left.computeSize(Math.max(0, width - trim.x), height);
		leftSize.y = Math.min(leftSize.y, height);
	}

	int x = 0;
	int y = 0;
	int oldStart = curveStart;
	Rectangle leftRect = null;
	Rectangle rightRect = null;
	Rectangle bottomRect = null;
	if (bottom != null) {
		bottomRect = new Rectangle(x, y+size.y-bottomSize.y, bottomSize.x, bottomSize.y);
	}
	if (showCurve) y += BORDER_TOP + BORDER_STRIPE;
	if(left != null) {
		if (leftSize.x > 0) {
			leftRect = new Rectangle(x, y, leftSize.x, leftSize.y);
			curveStart = x + leftSize.x - curveIndent;
			x += leftSize.x + curveWidth - 2*curveIndent;
		} else {
			leftRect = new Rectangle(0, 0, 0, 0);
			curveStart = 0;
			x += curveWidth - curveIndent;
		}
	}
	if (right != null) {
		rightRect = new Rectangle(x, y, rightSize.x, rightSize.y);
	}
	if (curveStart < oldStart) {
		redraw(curveStart - CURVE_TAIL, 0, oldStart + curveWidth - curveStart + CURVE_TAIL + 5, size.y, false);
	}
	if (curveStart > oldStart) {
		redraw(oldStart - CURVE_TAIL, 0, curveStart + curveWidth - oldStart + CURVE_TAIL + 5, size.y, false);
	}
	curveRect = new Rectangle(curveStart, 0, curveWidth, size.y);
	update();
	if (bottomRect != null) bottom.setBounds(bottomRect);
	if (rightRect != null) right.setBounds(rightRect);
	if (leftRect != null) left.setBounds(leftRect);
}
void onDispose() {
	if (resizeCursor != null) resizeCursor.dispose();
	resizeCursor = null;
	left = null;
	right = null;
}
void onMouseDown (int x, int y) {
	if (curveRect.contains(x, y)) {
		dragging = true;
		rightDragDisplacement = curveStart - x + curveWidth - curveIndent;
	}
}
void onMouseExit() {
	if (!dragging) setCursor(null);
}
void onMouseMove(int x, int y) {
	if (dragging) {
		Point size = getSize();
		if (!(0 < x && x < size.x)) return;
		rightWidth = size.x - x - rightDragDisplacement;
		rightWidth = Math.max(0, rightWidth);
		layout();
		return;
	}
	if (curveRect.contains(x, y)) {
		setCursor(resizeCursor); 
	} else {
		setCursor(null);
	}
}
void onMouseUp () {
	dragging = false;
}
void onPaint(GC gc) {
//	 Useful for debugging paint problems
//	{
//	Point size = getSize();	
//	gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_GREEN));
//	gc.fillRectangle(-10, -10, size.x+20, size.y+20);
//	}
	Point size = getSize();
	
	if (bottom != null && (left != null || right != null)) {
		Color border1 = new Color(getDisplay(), BORDER1);
		gc.setForeground(border1);
		int y = bottom.getBounds().y - BORDER_BOTTOM - BORDER_STRIPE;
		gc.drawLine(0, y, size.x, y);
		border1.dispose();
	}
	
	if (left == null || right == null) return;
	int[] line1 = new int[curve.length+6];
	int index = 0;
	int x = curveStart;
	int y = 0;
	line1[index++] = x + 1;
	line1[index++] = size.y - BORDER_STRIPE;
	for (int i = 0; i < curve.length/2; i++) {
		line1[index++]=x+curve[2*i];
		line1[index++]=y+curve[2*i+1];
	}
	line1[index++] = x + curveWidth;
	line1[index++] = 0;
	line1[index++] = size.x;
	line1[index++] = 0;
	
	Color background = getBackground();
		
	// Anti- aliasing
	int[] line2 = new int[line1.length];
	index = 0;
	for (int i = 0; i < line1.length/2; i++) { 
		line2[index] = line1[index++]  - 1;
		line2[index] = line1[index++];
	}
	int[] line3 = new int[line1.length];
	index = 0;
	for (int i = 0; i < line1.length/2; i++) {
		line3[index] = line1[index++] + 1;
		line3[index] = line1[index++];
	}
	RGB from = BORDER1;
	RGB to = background.getRGB();
	int red = from.red + 3*(to.red - from.red)/4;
	int green = from.green + 3*(to.green - from.green)/4;
	int blue = from.blue + 3*(to.blue - from.blue)/4;
	Color color = new Color(getDisplay(), red, green, blue);
	gc.setForeground(color);
	gc.drawPolyline(line2);
	gc.drawPolyline(line3);
	color.dispose();
	
	int x1 = Math.max(0, curveStart - CURVE_TAIL);
	Color border1 = new Color(getDisplay(), BORDER1);
	gc.setForeground(background);
	gc.setBackground(border1);
	gc.fillGradientRectangle(x1, size.y - BORDER_STRIPE, curveStart-x1+1, 1, false);
	gc.setForeground(border1);
	gc.drawPolyline(line1);
	border1.dispose();
}

void onResize() {
	updateCurve(getSize().y);
	layout();
}
/**
 * 
 * 
 * @since 3.0
 */
public void setBottom(Control control) {
	checkWidget();
	if (control != null && control.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (bottom != null && !bottom.isDisposed()) {
		bottom.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	bottom = control;
	layout();
}
/**
 * Sets the layout which is associated with the receiver to be
 * the argument which may be null.
 * <p>
 * Note : CBanner does not use a layout class to size and position its children.
 * </p>
 *
 * @param layout the receiver's new layout or null
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLayout (Layout layout) {
	checkWidget();
	return;
}

/**
* Set the control that appears on the left side of the banner.
* The left control is optional.  Setting the left control to null will remove it from 
* the banner - however, the creator of the control must dispose of the control.
* 
* @param control the control to be displayed on the left or null
* 
* @exception SWTException <ul>
*    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
*    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
*    <li>ERROR_INVALID_ARGUMENT - if the left control was not created as a child of the receiver</li>
* </ul>
* 
* @since 3.0
*/
public void setLeft(Control control) {
	checkWidget();
	if (control != null && control.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (left != null && !left.isDisposed()) {
		left.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	left = control;
	layout();
}
/**
* Set the control that appears on the right side of the banner.
* The right control is optional.  Setting the right control to null will remove it from 
* the banner - however, the creator of the control must dispose of the control.
* 
* @param control the control to be displayed on the right or null
* 
* @exception SWTException <ul>
*    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
*    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
*    <li>ERROR_INVALID_ARGUMENT - if the right control was not created as a child of the receiver</li>
* </ul>
* 
* @since 3.0
*/
public void setRight(Control control) {
	checkWidget();
	if (control != null && control.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (right != null && !right.isDisposed()) {
		right.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	right = control;
	layout();
}
public void setRightWidth(int width) {
	checkWidget();
	if (width < SWT.DEFAULT) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	rightWidth = width;
	layout(true);
}
void updateCurve (int height) {
	int d = height - 12;
	curve = new int[]{0,12+d, 0,11+d, 3,11+d, 4,10+d, 6,10+d, 7,9+d, 8,9+d, 10,7+d, 11,7+d,
			          12,6+d, 12+d,6,
					  13+d,5, 14+d,5, 16+d,3, 17+d,3, 18+d,2, 20+d,2, 21+d,1, 25+d,1, 26+d,0}; 
	curveWidth = 26+d;
	curveIndent = 5;	
}
}
