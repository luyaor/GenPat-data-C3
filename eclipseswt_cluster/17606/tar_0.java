package org.eclipse.swt.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;

public class ViewForm extends Composite {

	/**
	 * marginWidth specifies the number of pixels of horizontal margin
	 * that will be placed along the left and right edges of the form.
	 *
	 * The default value is 0.
	 */
 	public int marginWidth = 0;
	/**
	 * marginHeight specifies the number of pixels of vertical margin
	 * that will be placed along the top and bottom edges of the form.
	 *
	 * The default value is 0.
	 */
 	public int marginHeight = 0;
	
	/**
	 * Color of innermost line of drop shadow border.
	 */
	public static RGB borderInsideRGB  = new RGB (132, 130, 132);
	/**
	 * Color of middle line of drop shadow border.
	 */
	public static RGB borderMiddleRGB  = new RGB (143, 141, 138);
	/**
	 * Color of outermost line of drop shadow border.
	 */
	public static RGB borderOutsideRGB = new RGB (171, 168, 165);
	
	// SWT widgets
	private Control topLeft;
	private Control topCenter;
	private Control topRight;
	private Control content;
	
	// Configuration and state info
	private boolean separateTopCenter = false;
	private int drawLine1 = -1;
	private int drawLine2 = -1;
	
	private boolean showBorder = false;
	
	private int BORDER_TOP = 0;
	private int BORDER_BOTTOM = 0;
	private int BORDER_LEFT = 0;
	private int BORDER_RIGHT = 0;
	
	private Color borderColor1;
	private Color borderColor2;
	private Color borderColor3;
	
	private Rectangle oldArea;
	private static final int OFFSCREEN = -200;
/**
* Creates a ViewForm.
* <p>
*	This method creates a child widget using style bits
* to select a particular look or set of properties. 
*
* @param parent	a composite widget (cannot be null)
* @param style	the bitwise OR'ing of widget styles
*
* @exception SWTError <ul>
*		<li> ERROR_THREAD_INVALID_ACCESS when called from the wrong thread </li>
* 		<li> ERROR_ERROR_NULL_ARGUMENT when the parent is null </li>
*	</ul>
*/			
public ViewForm(Composite parent, int style) {
	super(parent, checkStyle(style));
	
	borderColor1 = new Color(getDisplay(), borderInsideRGB);
	borderColor2 = new Color(getDisplay(), borderMiddleRGB);
	borderColor3 = new Color(getDisplay(), borderOutsideRGB);
	setBorderVisible((style & SWT.BORDER) != 0);

	addPaintListener(new PaintListener() {
		public void paintControl(PaintEvent event) {
			onPaint(event.gc);
		}
	});
	addControlListener(new ControlAdapter(){
		public void controlResized(ControlEvent e) {
			onResize();
		}
	});
	
	addListener(SWT.Dispose, new Listener() {
		public void handleEvent(Event e) {
			onDispose();
		}
	});	
}
/**
 * Check the style bits to ensure that no invalid styles are applied.
 * @private
 */
private static int checkStyle (int style) {
	int mask = SWT.FLAT;
	return style & mask | SWT.NO_REDRAW_RESIZE;
}
public Point computeSize(int wHint, int hHint, boolean changed) {
	// size of title bar area
	Point leftSize = new Point(0, 0);
	if (topLeft != null) {
		leftSize = topLeft.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	Point centerSize = new Point(0, 0);
	if (topCenter != null) {
		 centerSize = topCenter.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	Point rightSize = new Point(0, 0);
	if (topRight != null) {
		 rightSize = topRight.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	Point size = new Point(0, 0);
	// calculate width of title bar
	if (separateTopCenter) {
		size.x = leftSize.x + rightSize.x;
		size.x = Math.max(centerSize.x, size.x);
		size.y = Math.max(leftSize.y, rightSize.y);
		if (topCenter != null){
			size.y += centerSize.y;
		}
	} else {
		size.x = leftSize.x + centerSize.x + rightSize.x;
		size.y = Math.max(leftSize.y, Math.max(centerSize.y, rightSize.y));
	}
	
	if (content != null) {
		Point contentSize = new Point(0, 0);
		contentSize = content.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
		size.x = Math.max (size.x, contentSize.x);
		size.y += contentSize.y;
	}
	
	size.x += 2 * marginWidth + BORDER_LEFT + BORDER_RIGHT;
	size.y += 2 * marginHeight + BORDER_TOP + BORDER_BOTTOM;

	return size;
}
public Rectangle getClientArea() {
	Rectangle clientArea = super.getClientArea();
	clientArea.x += BORDER_LEFT;
	clientArea.y += BORDER_TOP;
	clientArea.width -= BORDER_LEFT + BORDER_RIGHT;
	clientArea.height -= BORDER_TOP + BORDER_BOTTOM;
	return clientArea;
}
/**
* Returns the content area.
*/
public Control getContent() {
	return content;
}
/**
* Returns Control that appears in the top center of the pane.
* Typically this is a toolbar.
*/
public Control getTopCenter() {
	return topCenter;
}
/**
* Returns the Control that appears in the top left corner of the pane.
* Typically this is a label such as CLabel.
*/
public Control getTopLeft() {
	return topLeft;
}
/**
* Returns the control in the top right corner of the pane.
* Typically this is a Close button or a composite with a Menu and Close button.
*/
public Control getTopRight() {
	return topRight;
}
public void layout (boolean changed) {
	Rectangle rect = getClientArea();
	
	drawLine1 = -1;
	drawLine2 = -1;
	
	Point leftSize = new Point(0, 0);
	if (topLeft != null && !topLeft.isDisposed()) {
		leftSize = topLeft.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	Point centerSize = new Point(0, 0);
	if (topCenter != null && !topCenter.isDisposed()) {
		 centerSize = topCenter.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	Point rightSize = new Point(0, 0);
	if (topRight != null && !topRight.isDisposed()) {
		 rightSize = topRight.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}
	
	int minTopWidth = leftSize.x + centerSize.x + rightSize.x + 2*marginWidth + 1; // +1 for highlight line	
	int height = rect.y + marginHeight;
	
	boolean top = false;
	if (separateTopCenter || minTopWidth > rect.width) {;
		int topHeight = Math.max(rightSize.y, leftSize.y);
		if (topRight != null && !topRight.isDisposed()) {
			top = true;
			topRight.setBounds(rect.x + rect.width - marginWidth - rightSize.x, 
			                   rect.y + 1 + marginHeight, 
			                   rightSize.x, topHeight);
			height += 1 + topHeight; // +1 for highlight line
		}
		if (topLeft != null && !topLeft.isDisposed()) {
			top = true;
			leftSize = topLeft.computeSize(rect.width - 2* marginWidth - rightSize.x - 1, SWT.DEFAULT);
			topLeft.setBounds(rect.x + 1 + marginWidth, 
			                  rect.y + 1 + marginHeight, 
			                  leftSize.x, topHeight);
			height = Math.max(height, rect.y + marginHeight + 1 + topHeight); // +1 for highlight line
		}
		if (topCenter != null && !topCenter.isDisposed()) {
			top = true;
			if (height > rect.y + marginHeight) {
				drawLine1 = height;
				height += 1; // +1 for divider line
			}
			centerSize = topCenter.computeSize(rect.width - 2 * marginWidth, SWT.DEFAULT);
			topCenter.setBounds(rect.x + rect.width - marginWidth - centerSize.x, 
			                    height, 
			                    centerSize.x, centerSize.y);
			height += centerSize.y;

		}		
	} else {
		int topHeight = Math.max(rightSize.y, Math.max(centerSize.y, leftSize.y));
		if (topRight != null && !topRight.isDisposed()) {
			top = true;
			topRight.setBounds(rect.x + rect.width - marginWidth - rightSize.x, 
			                   rect.y + marginHeight + 1, // +1 for highlight line
			                   rightSize.x, topHeight);
			height += 1 + topHeight; // +1 for highlight line
		}
		if (topCenter != null && !topCenter.isDisposed()) {
			top = true;
			topCenter.setBounds(rect.x + rect.width - marginWidth - rightSize.x - centerSize.x, 
			                    rect.y + marginHeight + 1, // +1 for highlight line
			                    centerSize.x, topHeight);
			height = Math.max(height, rect.y + marginHeight + 1 + topHeight); // +1 for highlight line                    
		}
		if (topLeft != null && !topLeft.isDisposed()) {
			top = true;
			leftSize = topLeft.computeSize(rect.width - 2 * marginWidth - rightSize.x - centerSize.x - 1, topHeight);
			topLeft.setBounds(rect.x + marginWidth + 1, // +1 for highlight line
			                  rect.y + marginHeight + 1, // +1 for highlight line
			                  leftSize.x, topHeight);
			height = Math.max(height, rect.y + marginHeight + 1 + topHeight); // +1 for highlight line
		}
	}

	if (content != null && !content.isDisposed()) {
		if (top) {
			drawLine2 = height;
			height += 1; // +1 for divider line
		}
		 content.setBounds(rect.x + marginWidth, 
		                   height, 
		                   rect.width - 2 * marginWidth, 
		                   rect.y + rect.height - height - marginHeight);
	}
}
private void onDispose() {
	if (borderColor1 != null) {
		borderColor1.dispose();
	}
	borderColor1 = null;
	
	if (borderColor2 != null) {
		borderColor2.dispose();
	}
	borderColor2 = null;
	
	if (borderColor3 != null) {
		borderColor3.dispose();
	}
	borderColor3 = null;
}
/**
* Draws the focus border.
*/
private void onPaint(GC gc) {
	Rectangle d = super.getClientArea();
	
	if (showBorder) {
		if ((getStyle() & SWT.FLAT) !=0) {
			gc.setForeground(borderColor1);
			gc.drawRectangle(d.x, d.y, d.x + d.width - 1, d.y + d.height - 1);
		} else {
			gc.setForeground(borderColor1);
			gc.drawRectangle(d.x, d.y, d.x + d.width - 3, d.y + d.height - 3);
		
			gc.setForeground(borderColor2);
			gc.drawLine(d.x + 1,           d.y + d.height - 2, d.x + d.width - 1, d.y + d.height - 2);
			gc.drawLine(d.x + d.width - 2, d.y + 1,            d.x + d.width - 2, d.y + d.height - 1);
		
			gc.setForeground(borderColor3);
			gc.drawLine(d.x + 2,           d.y + d.height - 1, d.x + d.width - 2, d.y + d.height - 1);
			gc.drawLine(d.x + d.width - 1, d.y + 2,            d.x + d.width - 1, d.y + d.height - 2);
		}
	}
		
	if (drawLine1 != -1) {
		// top seperator line
		gc.setForeground(borderColor1);
		gc.drawLine(d.x + BORDER_LEFT, drawLine1, d.x + d.width - BORDER_RIGHT, drawLine1);	
	}
	if (drawLine2 != -1) {
		// content separator line
		gc.setForeground(borderColor1);
		gc.drawLine(d.x + BORDER_LEFT, drawLine2, d.x + d.width - BORDER_RIGHT, drawLine2);
	}
	// highlight on top
	int y = drawLine1;
	if (y == -1){
		y = drawLine2;
	}
	if (y != -1) {
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		gc.drawLine(d.x + BORDER_LEFT + marginWidth, d.y + BORDER_TOP + marginHeight, 
		            d.x + BORDER_LEFT + marginWidth, y - 1);
		gc.drawLine(d.x + BORDER_LEFT + marginWidth, d.y + BORDER_TOP + marginHeight,
		            d.x + d.width - BORDER_RIGHT - marginWidth - 1, d.y + BORDER_TOP + marginHeight);
	}

	gc.setForeground(getForeground());
}
private void onResize() {
	layout();
	
	Rectangle area = super.getClientArea();
	if (oldArea == null || oldArea.width == 0 || oldArea.height == 0) {
		redraw();
	} else {
		int width = 0;
		if (oldArea.width < area.width) {
			width = area.width - oldArea.width + BORDER_RIGHT;
		} else if (oldArea.width > area.width) {
			width = BORDER_RIGHT;			
		}
		redraw(area.x + area.width - width, area.y, width, area.height, false);
		
		int height = 0;
		if (oldArea.height < area.height) {
			height = area.height - oldArea.height + BORDER_BOTTOM;		
		}
		if (oldArea.height > area.height) {
			height = BORDER_BOTTOM;		
		}
		redraw(area.x, area.y + area.height - height, area.width, height, false);
	}
	oldArea = area;
}
/**
* Sets the content.
* Setting the content to null will remove it from 
* the pane - however, the creator of the content must dispose of the content.
*/
public void setContent(Control content) {
	if (content != null && content.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (this.content != null && !this.content.isDisposed()) {
		this.content.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	this.content = content;
	layout();
}

/**
* Set the widget font.
* This will apply the font to the topLeft, topRight and topCenter widgets.
*/
public void setFont(Font f) {
	super.setFont(f);
	if (topLeft != null && !topLeft.isDisposed()) topLeft.setFont(f);
	if (topCenter != null && !topCenter.isDisposed()) topCenter.setFont(f);
	if (topRight != null && !topRight.isDisposed()) topRight.setFont(f);
	
	layout();
}
/**
 * Sets the layout which is associated with the receiver to be
 * the argument which may be null.
 * <p>
 * Note : ViewForm does not use a layout class to size and position its children.
 * </p>
 *
 * @param the receiver's new layout or null
 *
 * @exception SWTException <ul>
 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 */
public void setLayout (Layout layout) {
	return;
}
/**
* Set the control that appears in the top center of the pane.
* Typically this is a toolbar.
* The topCenter is optional.  Setting the topCenter to null will remove it from 
* the pane - however, the creator of the topCenter must dispose of the topCenter.
*/
public void setTopCenter(Control topCenter) {
	if (topCenter != null && topCenter.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (this.topCenter != null && !this.topCenter.isDisposed()) {
		this.topCenter.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	this.topCenter = topCenter;
	layout();
}
/**
* Set the control that appears in the top left corner of the pane.
* Typically this is a label such as CLabel.
* The topLeft is optional.  Setting the top left control to null will remove it from 
* the pane - however, the creator of the control must dispose of the control.
*/
public void setTopLeft(Control c) {
	if (c != null && c.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (this.topLeft != null && !this.topLeft.isDisposed()) {
		this.topLeft.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	this.topLeft = c;
	layout();
}
/**
* Set the control that appears in the top right corner of the pane.
* Typically this is a Close button or a composite with a Menu and Close button.
* The topRight is optional.  Setting the top right control to null will remove it from 
* the pane - however, the creator of the control must dispose of the control.
*/
public void setTopRight(Control c) {
	if (c != null && c.getParent() != this) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (this.topRight != null && !this.topRight.isDisposed()) {
		this.topRight.setBounds(OFFSCREEN, OFFSCREEN, 0, 0);
	}
	this.topRight = c;
	layout();
}
public void setBorderVisible(boolean show) {
	if (showBorder == show) return;
	
	showBorder = show;
	if (showBorder) {
		if ((getStyle() & SWT.FLAT)!= 0) {
			BORDER_LEFT = BORDER_TOP = BORDER_RIGHT = BORDER_BOTTOM = 1;
		} else {
			BORDER_LEFT = BORDER_TOP = 1;
			BORDER_RIGHT = BORDER_BOTTOM = 3;
		}
	} else {
		BORDER_BOTTOM = BORDER_TOP = BORDER_LEFT = BORDER_RIGHT = 0;
	}

	layout();
	redraw();
}
/**
* If true, the topCenter will always appear on a separate line by itself, otherwise the 
* topCenter will appear in the top row if there is room and will be moved to the second row if
* required.
*/
public void setTopCenterSeparate(boolean show) {
	separateTopCenter = show;
	layout();
}

}
