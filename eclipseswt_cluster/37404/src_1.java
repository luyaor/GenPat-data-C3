package org.eclipse.swt.graphics;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.internal.carbon.*;
import org.eclipse.swt.*;

/**
 * Class <code>GC</code> is where all of the drawing capabilities that are 
 * supported by SWT are located. Instances are used to draw on either an 
 * <code>Image</code>, a <code>Control</code>, or directly on a <code>Display</code>.
 * <p>
 * Application code must explicitly invoke the <code>GC.dispose()</code> 
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required. This is <em>particularly</em>
 * important on Windows95 and Windows98 where the operating system has a limited
 * number of device contexts available.
 * </p>
 *
 * @see org.eclipse.swt.events.PaintEvent
 */
public final class GC {
	/**
	 * the handle to the OS device context
	 * (Warning: This field is platform dependent)
	 */
	public int handle;
	
	Drawable drawable;
	GCData data;

GC() {
}

/**	 
 * Constructs a new instance of this class which has been
 * configured to draw on the specified drawable. Sets the
 * foreground and background color in the GC to match those
 * in the drawable.
 * <p>
 * You must dispose the graphics context when it is no longer required. 
 * </p>
 * @param drawable the drawable to draw on
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the drawable is null</li>
 *    <li>ERROR_NULL_ARGUMENT - if there is no current device</li>
 *    <li>ERROR_INVALID_ARGUMENT
 *          - if the drawable is an image that is not a bitmap or an icon
 *          - if the drawable is an image or printer that is already selected
 *            into another graphics context</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for gc creation</li>
 * </ul>
 */
public GC(Drawable drawable) {
	if (drawable == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	GCData data = new GCData();
	int gdkGC = drawable.internal_new_GC(data);
	init(drawable, data, gdkGC);
}

/**	 
 * Invokes platform specific functionality to allocate a new graphics context.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>GC</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param drawable the Drawable for the receiver.
 * @param data the data for the receiver.
 *
 * @return a new <code>GC</code>
 *
 * @private
 */
public static GC carbon_new(Drawable drawable, GCData data) {
	GC gc = new GC();
	int context = drawable.internal_new_GC(data);
	gc.init(drawable, data, context);
	return gc;
}

/**
 * Copies a rectangular area of the receiver at the specified
 * position into the image, which must be of type <code>SWT.BITMAP</code>.
 *
 * @param x the x coordinate in the receiver of the area to be copied
 * @param y the y coordinate in the receiver of the area to be copied
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the image is not a bitmap or has been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void copyArea(Image image, int x, int y) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (image == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (image.type != SWT.BITMAP || image.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	//NOT IMPLEMENTED
}

/**
 * Copies a rectangular area of the receiver at the source
 * position onto the receiver at the destination position.
 *
 * @param srcX the x coordinate in the receiver of the area to be copied
 * @param srcY the y coordinate in the receiver of the area to be copied
 * @param width the width of the area to copy
 * @param height the height of the area to copy
 * @param destX the x coordinate in the receiver of the area to copy to
 * @param destY the y coordinate in the receiver of the area to copy to
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void copyArea(int srcX, int srcY, int width, int height, int destX, int destY) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width <= 0 || height <= 0) return;
	int deltaX = destX - srcX, deltaY = destY - srcY;
	if (deltaX == 0 && deltaY == 0) return;
	if (data.image != null) {
 		OS.CGContextSaveGState(handle);
 		OS.CGContextScaleCTM(handle, 1, -1);
 		OS.CGContextTranslateCTM(handle, 0, -(height + 2 * destY));
 		CGRect rect = new CGRect();
 		rect.x = destX;
 		rect.y = destY;
 		rect.width = width;
		rect.height = height;
		//NOT DONE - transparency
 		OS.CGContextDrawImage(handle, rect, data.image.handle);
 		OS.CGContextRestoreGState(handle);
 		return;
	}
	if (data.control != 0) {
		int window = OS.GetControlOwner(data.control);
		int port = OS.GetWindowPort(window);

		/* Calculate src and dest rectangles/regions */
		Rect rect = new Rect();
		OS.GetControlBounds(data.control, rect);		
		Rect srcRect = new Rect();
		OS.GetControlBounds(data.control, srcRect);
		int left = srcRect.left + srcX;
		int top = srcRect.top + srcY;
		OS.SetRect(srcRect, (short)left, (short)top, (short)(left + width), (short)(top + height));
		int srcRgn = OS.NewRgn();
		OS.RectRgn(srcRgn, srcRect);		
		OS.SectRect(rect, srcRect, srcRect);
		Rect destRect = new Rect ();
		destRect.left = srcRect.left;
		destRect.top = srcRect.top;
		destRect.right = srcRect.right;
		destRect.bottom = srcRect.bottom;
		OS.OffsetRect(destRect, (short)deltaX, (short)deltaY);
		int destRgn = OS.NewRgn();
		OS.RectRgn(destRgn, destRect);
		
		/* Copy bits with appropriated clipping region */
		if (!OS.EmptyRect(srcRect)) {
			int clipRgn = data.visibleRgn;
			if (data.clipRgn != 0) {
				clipRgn = OS.NewRgn();
				OS.SectRgn(data.clipRgn, clipRgn, clipRgn);
			}

			/*
			* Feature in the Macintosh.  ScrollRect() only copies bits
			* that are inside the specified rectangle.  This means that
			* it is not possible to copy non overlaping bits without
			* copying the bits in between the source and destination
			* rectangles.  The fix is to check if the source and
			* destination rectangles are disjoint and use CopyBits()
			* instead.
			*/
			boolean disjoint = (destX + width < srcX) || (srcX + width < destX) || (destY + height < srcY) || (srcY + height < destY);
			if (!disjoint && (deltaX == 0 || deltaY == 0)) {
				int[] currentPort = new int[1];
				OS.GetPort(currentPort);
				OS.SetPort(port);
				int oldClip = OS.NewRgn();
				OS.GetClip(oldClip);
				OS.SetClip(clipRgn);
				OS.UnionRect(srcRect, destRect, rect);
				OS.ScrollRect(rect, (short)deltaX, (short)deltaY, 0);
				OS.SetClip(oldClip);
				OS.DisposeRgn(oldClip);
				OS.SetPort(currentPort[0]);
			} else {
				int portBitMap = OS.GetPortBitMapForCopyBits (port);
				OS.CopyBits(portBitMap, portBitMap, srcRect, destRect, (short)OS.srcCopy, clipRgn);
				OS.QDFlushPortBuffer(port, destRgn);
			}
			
			if (clipRgn != data.visibleRgn) OS.DisposeRgn(clipRgn);
		}
		
		/* Invalidate src and obscured areas */
		int invalRgn = OS.NewRgn();
		OS.DiffRgn(srcRgn, data.visibleRgn, invalRgn);
		OS.OffsetRgn(invalRgn, (short)deltaX, (short)deltaY);
		OS.DiffRgn(srcRgn, destRgn, srcRgn);
		OS.UnionRgn(srcRgn, invalRgn, invalRgn);
		OS.SectRgn(data.visibleRgn, invalRgn, invalRgn);
		OS.InvalWindowRgn(window, invalRgn);
		OS.DisposeRgn(invalRgn);
		
		/* Dispose src and dest regions */
		OS.DisposeRgn(destRgn);
		OS.DisposeRgn(srcRgn);
	}
}

/**
 * Disposes of the operating system resources associated with
 * the graphics context. Applications must dispose of all GCs
 * which they allocate.
 */
public void dispose() {
	if (handle == 0) return;
	if (data.device.isDisposed()) return;

	/* Free resources */
	int clipRgn = data.clipRgn;
	if (clipRgn != 0) OS.DisposeRgn(clipRgn);
	Image image = data.image;
	if (image != null) {
		image.memGC = null;
//		if (image.transparentPixel != -1) image.createMask();
	}
	int layout = data.layout;
	if (layout != 0) OS.ATSUDisposeTextLayout(layout);
	int style = data.style;
	if (style != 0) OS.ATSUDisposeTextLayout(style);

	/* Dispose the GC */
	drawable.internal_dispose_GC(handle, data);

	data.clipRgn = data.style = data.layout = 0;
	drawable = null;
	data.image = null;
	data = null;
	handle = 0;
}

/**
 * Draws the outline of a circular or elliptical arc 
 * within the specified rectangular area.
 * <p>
 * The resulting arc begins at <code>startAngle</code> and extends  
 * for <code>arcAngle</code> degrees, using the current color.
 * Angles are interpreted such that 0 degrees is at the 3 o'clock
 * position. A positive value indicates a counter-clockwise rotation
 * while a negative value indicates a clockwise rotation.
 * </p><p>
 * The center of the arc is the center of the rectangle whose origin 
 * is (<code>x</code>, <code>y</code>) and whose size is specified by the 
 * <code>width</code> and <code>height</code> arguments. 
 * </p><p>
 * The resulting arc covers an area <code>width + 1</code> pixels wide
 * by <code>height + 1</code> pixels tall.
 * </p>
 *
 * @param x the x coordinate of the upper-left corner of the arc to be drawn
 * @param y the y coordinate of the upper-left corner of the arc to be drawn
 * @param width the width of the arc to be drawn
 * @param height the height of the arc to be drawn
 * @param startAngle the beginning angle
 * @param arcAngle the angular extent of the arc, relative to the start angle
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if any of the width, height or endAngle is zero.</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawArc(int x, int y, int width, int height, int startAngle, int endAngle) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0) {
		x = x + width;
		width = -width;
	}
	if (height < 0) {
		y = y + height;
		height = -height;
	}
	if (width == 0 || height == 0 || endAngle == 0) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}	
	OS.CGContextBeginPath(handle);
    OS.CGContextSaveGState(handle);
    OS.CGContextTranslateCTM(handle, x + width / 2f, y + height / 2f);
    OS.CGContextScaleCTM(handle, width / 2f, height / 2f);
    OS.CGContextAddArc(handle, 0, 0, 1, -startAngle * (float)Math.PI / 180,  -endAngle * (float)Math.PI / 180, true);
    OS.CGContextRestoreGState(handle);
	OS.CGContextStrokePath(handle);
}

/** 
 * Draws a rectangle, based on the specified arguments, which has
 * the appearance of the platform's <em>focus rectangle</em> if the
 * platform supports such a notion, and otherwise draws a simple
 * rectangle in the receiver's foreground color.
 *
 * @param x the x coordinate of the rectangle
 * @param y the y coordinate of the rectangle
 * @param width the width of the rectangle
 * @param height the height of the rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawRectangle
 */
public void drawFocus(int x, int y, int width, int height) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	//NOT DONE
//	drawRectangle (x, y, width - 1, height - 1);
}

/**
 * Draws the given image in the receiver at the specified
 * coordinates.
 *
 * @param image the image to draw
 * @param x the x coordinate of where to draw
 * @param y the y coordinate of where to draw
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the given coordinates are outside the bounds of the image</li>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawImage(Image image, int x, int y) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (image == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (image.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	drawImage(image, 0, 0, -1, -1, x, y, -1, -1, true);
}

/**
 * Copies a rectangular area from the source image into a (potentially
 * different sized) rectangular area in the receiver. If the source
 * and destination areas are of differing sizes, then the source
 * area will be stretched or shrunk to fit the destination area
 * as it is copied. The copy fails if any part of the source rectangle
 * lies outside the bounds of the source image, or if any of the width
 * or height arguments are negative.
 *
 * @param image the source image
 * @param srcX the x coordinate in the source image to copy from
 * @param srcY the y coordinate in the source image to copy from
 * @param srcWidth the width in pixels to copy from the source
 * @param srcHeight the height in pixels to copy from the source
 * @param destX the x coordinate in the destination to copy to
 * @param destY the y coordinate in the destination to copy to
 * @param destWidth the width in pixels of the destination rectangle
 * @param destHeight the height in pixels of the destination rectangle
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the image is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the image has been disposed</li>
 *    <li>ERROR_INVALID_ARGUMENT - if any of the width or height arguments are negative.
 *    <li>ERROR_INVALID_ARGUMENT - if the source rectangle is not contained within the bounds of the source image</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES - if no handles are available to perform the operation</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (srcWidth == 0 || srcHeight == 0 || destWidth == 0 || destHeight == 0) return;
	if (srcX < 0 || srcY < 0 || srcWidth < 0 || srcHeight < 0 || destWidth < 0 || destHeight < 0) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	if (image == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (image.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	drawImage(image, srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight, false);
}

void drawImage(Image srcImage, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight, boolean simple) {
 	int imageHandle = srcImage.handle;
 	int imgWidth = OS.CGImageGetWidth(imageHandle);
 	int imgHeight = OS.CGImageGetHeight(imageHandle);
 	if (simple) {
 		srcWidth = destWidth = imgWidth;
 		srcHeight = destHeight = imgHeight;
 	} else {
 		simple = srcX == 0 && srcY == 0 &&
 			srcWidth == destWidth && destWidth == imgWidth &&
 			srcHeight == destHeight && destHeight == imgHeight;
		if (srcX + srcWidth > imgWidth || srcY + srcHeight > imgHeight) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
 	}
 	OS.CGContextSaveGState(handle);
 	OS.CGContextScaleCTM(handle, 1, -1);
 	OS.CGContextTranslateCTM(handle, 0, -(destHeight + 2 * destY));
 	CGRect rect = new CGRect();
 	rect.x = destX;
 	rect.y = destY;
 	rect.width = destWidth;
	rect.height = destHeight;
 	if (simple) {
 		OS.CGContextDrawImage(handle, rect, imageHandle);
 	} else {
	 	int width = OS.CGImageGetWidth(imageHandle);
		int height = OS.CGImageGetHeight(imageHandle);
		int bpc = OS.CGImageGetBitsPerComponent(imageHandle);
		int bpp = OS.CGImageGetBitsPerPixel(imageHandle);
		int bpr = OS.CGImageGetBytesPerRow(imageHandle);
		int colorspace = OS.CGImageGetColorSpace(imageHandle);
		int alphaInfo = OS.CGImageGetAlphaInfo(imageHandle);
		int data = srcImage.data + (srcY * bpr) + srcX * 4;
		int provider = OS.CGDataProviderCreateWithData(0, data, srcHeight * bpr, 0);
		if (provider == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		int subImage = OS.CGImageCreate(srcWidth, srcHeight, bpc, bpp, bpr, colorspace, alphaInfo, provider, null, false, 0);
		OS.CGDataProviderRelease(provider);
		if (subImage == 0) SWT.error(SWT.ERROR_NO_HANDLES);
 		OS.CGContextDrawImage(handle, rect, subImage);
 		OS.CGImageRelease(subImage);
 	}
 	OS.CGContextRestoreGState(handle);
}

/** 
 * Draws a line, using the foreground color, between the points 
 * (<code>x1</code>, <code>y1</code>) and (<code>x2</code>, <code>y2</code>).
 *
 * @param x1 the first point's x coordinate
 * @param y1 the first point's y coordinate
 * @param x2 the second point's x coordinate
 * @param y2 the second point's y coordinate
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawLine(int x1, int y1, int x2, int y2) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	OS.CGContextBeginPath(handle);
	OS.CGContextMoveToPoint(handle, x1, y1);
	OS.CGContextAddLineToPoint(handle, x2, y2);
	OS.CGContextStrokePath(handle);
}

/** 
 * Draws the outline of an oval, using the foreground color,
 * within the specified rectangular area.
 * <p>
 * The result is a circle or ellipse that fits within the 
 * rectangle specified by the <code>x</code>, <code>y</code>, 
 * <code>width</code>, and <code>height</code> arguments. 
 * </p><p> 
 * The oval covers an area that is <code>width + 1</code> 
 * pixels wide and <code>height + 1</code> pixels tall.
 * </p>
 *
 * @param x the x coordinate of the upper left corner of the oval to be drawn
 * @param y the y coordinate of the upper left corner of the oval to be drawn
 * @param width the width of the oval to be drawn
 * @param height the height of the oval to be drawn
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawOval(int x, int y, int width, int height) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0) {
		x = x + width;
		width = -width;
	}
	if (height < 0) {
		y = y + height;
		height = -height;
	}
	OS.CGContextBeginPath(handle);
    OS.CGContextSaveGState(handle);
    OS.CGContextTranslateCTM(handle, x + width / 2f, y + height / 2f);
    OS.CGContextScaleCTM(handle, width / 2f, height / 2f);
    OS.CGContextMoveToPoint(handle, 1, 0);
    OS.CGContextAddArc(handle, 0, 0, 1, 0, (float)(2 *Math.PI), true);
    OS.CGContextRestoreGState(handle);
	OS.CGContextStrokePath(handle);
}

/** 
 * Draws the closed polygon which is defined by the specified array
 * of integer coordinates, using the receiver's foreground color. The array 
 * contains alternating x and y values which are considered to represent
 * points which are the vertices of the polygon. Lines are drawn between
 * each consecutive pair, and between the first pair and last pair in the
 * array.
 *
 * @param pointArray an array of alternating x and y values which are the vertices of the polygon
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT if pointArray is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawPolygon(int[] pointArray) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (pointArray == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	float[] points = new float[pointArray.length];
	for (int i=0; i<points.length; i++) {
		points[i] = pointArray[i];
	}
	OS.CGContextBeginPath(handle);
	OS.CGContextAddLines(handle, points, points.length / 2);
	OS.CGContextClosePath(handle);
	OS.CGContextStrokePath(handle);
}

/** 
 * Draws the polyline which is defined by the specified array
 * of integer coordinates, using the receiver's foreground color. The array 
 * contains alternating x and y values which are considered to represent
 * points which are the corners of the polyline. Lines are drawn between
 * each consecutive pair, but not between the first pair and last pair in
 * the array.
 *
 * @param pointArray an array of alternating x and y values which are the corners of the polyline
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point array is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawPolyline(int[] pointArray) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (pointArray == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	float[] points = new float[pointArray.length];
	for (int i=0; i<points.length; i++) {
		points[i] = pointArray[i];
	}
	OS.CGContextBeginPath(handle);
	OS.CGContextAddLines(handle, points, points.length / 2);
	OS.CGContextStrokePath(handle);
}

/** 
 * Draws the outline of the rectangle specified by the arguments,
 * using the receiver's foreground color. The left and right edges
 * of the rectangle are at <code>x</code> and <code>x + width</code>. 
 * The top and bottom edges are at <code>y</code> and <code>y + height</code>. 
 *
 * @param x the x coordinate of the rectangle to be drawn
 * @param y the y coordinate of the rectangle to be drawn
 * @param width the width of the rectangle to be drawn
 * @param height the height of the rectangle to be drawn
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawRectangle(int x, int y, int width, int height) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0) {
		x = x + width;
		width = -width;
	}
	if (height < 0) {
		y = y + height;
		height = -height;
	}
	CGRect rect = new CGRect();
	rect.x = x;
	rect.y = y;
	rect.width = width;
	rect.height = height;
	OS.CGContextStrokeRect(handle, rect);
}

/** 
 * Draws the outline of the specified rectangle, using the receiver's
 * foreground color. The left and right edges of the rectangle are at
 * <code>rect.x</code> and <code>rect.x + rect.width</code>. The top 
 * and bottom edges are at <code>rect.y</code> and 
 * <code>rect.y + rect.height</code>. 
 *
 * @param rect the rectangle to draw
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawRectangle(Rectangle rect) {
	if (rect == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	drawRectangle (rect.x, rect.y, rect.width, rect.height);
}

/** 
 * Draws the outline of the round-cornered rectangle specified by 
 * the arguments, using the receiver's foreground color. The left and
 * right edges of the rectangle are at <code>x</code> and <code>x + width</code>. 
 * The top and bottom edges are at <code>y</code> and <code>y + height</code>.
 * The <em>roundness</em> of the corners is specified by the 
 * <code>arcWidth</code> and <code>arcHeight</code> arguments. 
 *
 * @param x the x coordinate of the rectangle to be drawn
 * @param y the y coordinate of the rectangle to be drawn
 * @param width the width of the rectangle to be drawn
 * @param height the height of the rectangle to be drawn
 * @param arcWidth the horizontal diameter of the arc at the four corners
 * @param arcHeight the vertical diameter of the arc at the four corners
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawRoundRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (arcWidth == 0 || arcHeight == 0) {
		drawRectangle(x, y, width, height);
    	return;
	}
	OS.CGContextBeginPath(handle);
	OS.CGContextSaveGState(handle);
	OS.CGContextTranslateCTM(handle, x, y);
	OS.CGContextScaleCTM(handle, arcWidth, arcHeight);
    float fw = width / (float)arcWidth;
	float fh = height / (float)arcHeight;
	OS.CGContextMoveToPoint(handle, fw, fh/2);
	OS.CGContextAddArcToPoint(handle, fw, fh, fw/2, fh, 1);
	OS.CGContextAddArcToPoint(handle, 0, fh, 0, fh/2, 1);
	OS.CGContextAddArcToPoint(handle, 0, 0, fw/2, 0, 1);
	OS.CGContextAddArcToPoint(handle, fw, 0, fw, fh/2, 1);
	OS.CGContextClosePath(handle);
	OS.CGContextRestoreGState(handle);
	OS.CGContextStrokePath(handle);
}

/** 
 * Draws the given string, using the receiver's current font and
 * foreground color. No tab expansion or carriage return processing
 * will be performed. The background of the rectangular area where
 * the string is being drawn will be filled with the receiver's
 * background color.
 *
 * @param string the string to be drawn
 * @param x the x coordinate of the top left corner of the rectangular area where the string is to be drawn
 * @param y the y coordinate of the top left corner of the rectangular area where the string is to be drawn
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawString (String string, int x, int y) {
	drawString(string, x, y, false);
}

/** 
 * Draws the given string, using the receiver's current font and
 * foreground color. No tab expansion or carriage return processing
 * will be performed. If <code>isTransparent</code> is <code>true</code>,
 * then the background of the rectangular area where the string is being
 * drawn will not be modified, otherwise it will be filled with the
 * receiver's background color.
 *
 * @param string the string to be drawn
 * @param x the x coordinate of the top left corner of the rectangular area where the string is to be drawn
 * @param y the y coordinate of the top left corner of the rectangular area where the string is to be drawn
 * @param isTransparent if <code>true</code> the background will be transparent, otherwise it will be opaque
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawString(String string, int x, int y, boolean isTransparent) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	int length = string.length();
	if (length == 0) return;
	OS.CGContextSaveGState(handle);
	OS.CGContextScaleCTM(handle, 1, -1);
	OS.CGContextTranslateCTM(handle, 0, -data.fontAscent);
	OS.CGContextSetFillColor(handle, data.foreground);
	char[] buffer = new char[length];
	string.getChars(0, length, buffer, 0);
	int ptr = OS.NewPtr(length * 2);
	OS.memcpy(ptr, buffer, length * 2);
	OS.ATSUSetTextPointerLocation(data.layout, ptr, 0, length, length);
	OS.ATSUSetRunStyle(data.layout, data.style, 0, length);
	OS.ATSUDrawText(data.layout, 0, length, x << 16, -y << 16);
	OS.DisposePtr(ptr);
	OS.CGContextRestoreGState(handle);
}

/** 
 * Draws the given string, using the receiver's current font and
 * foreground color. Tab expansion and carriage return processing
 * are performed. The background of the rectangular area where
 * the text is being drawn will be filled with the receiver's
 * background color.
 *
 * @param string the string to be drawn
 * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
 * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawText(String string, int x, int y) {
	drawText(string, x, y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
}

/** 
 * Draws the given string, using the receiver's current font and
 * foreground color. Tab expansion and carriage return processing
 * are performed. If <code>isTransparent</code> is <code>true</code>,
 * then the background of the rectangular area where the text is being
 * drawn will not be modified, otherwise it will be filled with the
 * receiver's background color.
 *
 * @param string the string to be drawn
 * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
 * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
 * @param isTransparent if <code>true</code> the background will be transparent, otherwise it will be opaque
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawText(String string, int x, int y, boolean isTransparent) {
	int flags = SWT.DRAW_DELIMITER | SWT.DRAW_TAB;
	if (isTransparent) flags |= SWT.DRAW_TRANSPARENT;
	drawText(string, x, y, flags);
}

/** 
 * Draws the given string, using the receiver's current font and
 * foreground color. Tab expansion, line delimiter and mnemonic
 * processing are performed according to the specified flags. If
 * <code>flags</code> includes <code>DRAW_TRANSPARENT</code>,
 * then the background of the rectangular area where the text is being
 * drawn will not be modified, otherwise it will be filled with the
 * receiver's background color.
 * <p>
 * The parameter <code>flags</code> may be a combination of:
 * <dl>
 * <dt><b>DRAW_DELIMITER</b></dt>
 * <dd>draw multiple lines</dd>
 * <dt><b>DRAW_TAB</b></dt>
 * <dd>expand tabs</dd>
 * <dt><b>DRAW_MNEMONIC</b></dt>
 * <dd>underline the mnemonic character</dd>
 * <dt><b>DRAW_TRANSPARENT</b></dt>
 * <dd>transparent background</dd>
 * </dl>
 * </p>
 *
 * @param string the string to be drawn
 * @param x the x coordinate of the top left corner of the rectangular area where the text is to be drawn
 * @param y the y coordinate of the top left corner of the rectangular area where the text is to be drawn
 * @param flags the flags specifing how to process the text
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void drawText (String string, int x, int y, int flags) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	//NOT DONE
	drawString(string, x, y);
}

/**
 * Compares the argument to the receiver, and returns true
 * if they represent the <em>same</em> object using a class
 * specific comparison.
 *
 * @param object the object to compare with this object
 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
 *
 * @see #hashCode
 */
public boolean equals(Object object) {
	if (object == this) return true;
	if (!(object instanceof GC)) return false;
	return handle == ((GC)object).handle;
}

/**
 * Fills the interior of a circular or elliptical arc within
 * the specified rectangular area, with the receiver's background
 * color.
 * <p>
 * The resulting arc begins at <code>startAngle</code> and extends  
 * for <code>arcAngle</code> degrees, using the current color.
 * Angles are interpreted such that 0 degrees is at the 3 o'clock
 * position. A positive value indicates a counter-clockwise rotation
 * while a negative value indicates a clockwise rotation.
 * </p><p>
 * The center of the arc is the center of the rectangle whose origin 
 * is (<code>x</code>, <code>y</code>) and whose size is specified by the 
 * <code>width</code> and <code>height</code> arguments. 
 * </p><p>
 * The resulting arc covers an area <code>width + 1</code> pixels wide
 * by <code>height + 1</code> pixels tall.
 * </p>
 *
 * @param x the x coordinate of the upper-left corner of the arc to be filled
 * @param y the y coordinate of the upper-left corner of the arc to be filled
 * @param width the width of the arc to be filled
 * @param height the height of the arc to be filled
 * @param startAngle the beginning angle
 * @param arcAngle the angular extent of the arc, relative to the start angle
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if any of the width, height or endAngle is zero.</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawArc
 */
public void fillArc(int x, int y, int width, int height, int startAngle, int endAngle) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0) {
		x = x + width;
		width = -width;
	}
	if (height < 0) {
		y = y + height;
		height = -height;
	}
	if (width == 0 || height == 0 || endAngle == 0) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	OS.CGContextBeginPath(handle);
    OS.CGContextSaveGState(handle);
    OS.CGContextTranslateCTM(handle, x + width / 2f, y + height / 2f);
    OS.CGContextScaleCTM(handle, width / 2f, height / 2f);
    OS.CGContextMoveToPoint(handle, 0, 0);
    OS.CGContextAddArc(handle, 0, 0, 1, -startAngle * (float)Math.PI / 180,  -endAngle * (float)Math.PI / 180, true);
    OS.CGContextClosePath(handle);
    OS.CGContextRestoreGState(handle);
	OS.CGContextFillPath(handle);
}

/**
 * Fills the interior of the specified rectangle with a gradient
 * sweeping from left to right or top to bottom progressing
 * from the receiver's foreground color to its background color.
 *
 * @param x the x coordinate of the rectangle to be filled
 * @param y the y coordinate of the rectangle to be filled
 * @param width the width of the rectangle to be filled, may be negative
 *        (inverts direction of gradient if horizontal)
 * @param height the height of the rectangle to be filled, may be negative
 *        (inverts direction of gradient if vertical)
 * @param vertical if true sweeps from top to bottom, else 
 *        sweeps from left to right
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawRectangle
 */
public void fillGradientRectangle(int x, int y, int width, int height, boolean vertical) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if ((width == 0) || (height == 0)) return;
	
	/* Rewrite this to use GdkPixbuf */

	RGB backgroundRGB, foregroundRGB;
	backgroundRGB = getBackground().getRGB();
	foregroundRGB = getForeground().getRGB();

	RGB fromRGB, toRGB;
	fromRGB = foregroundRGB;
	toRGB   = backgroundRGB;
	boolean swapColors = false;
	if (width < 0) {
		x += width; width = -width;
		if (! vertical) swapColors = true;
	}
	if (height < 0) {
		y += height; height = -height;
		if (vertical) swapColors = true;
	}
	if (swapColors) {
		fromRGB = backgroundRGB;
		toRGB   = foregroundRGB;
	}
	if (fromRGB == toRGB) {
		fillRectangle(x, y, width, height);
		return;
	}
	ImageData.fillGradientRectangle(this, data.device,
		x, y, width, height, vertical, fromRGB, toRGB,
		8, 8, 8);
}

/** 
 * Fills the interior of an oval, within the specified
 * rectangular area, with the receiver's background
 * color.
 *
 * @param x the x coordinate of the upper left corner of the oval to be filled
 * @param y the y coordinate of the upper left corner of the oval to be filled
 * @param width the width of the oval to be filled
 * @param height the height of the oval to be filled
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawOval
 */
public void fillOval(int x, int y, int width, int height) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0) {
		x = x + width;
		width = -width;
	}
	if (height < 0) {
		y = y + height;
		height = -height;
	}
	OS.CGContextBeginPath(handle);
    OS.CGContextSaveGState(handle);
    OS.CGContextTranslateCTM(handle, x + width / 2f, y + height / 2f);
    OS.CGContextScaleCTM(handle, width / 2f, height / 2f);
    OS.CGContextMoveToPoint(handle, 1, 0);
    OS.CGContextAddArc(handle, 0, 0, 1, 0,  (float)(Math.PI * 2), false);
    OS.CGContextClosePath(handle);
    OS.CGContextRestoreGState(handle);
	OS.CGContextFillPath(handle);
}

/** 
 * Fills the interior of the closed polygon which is defined by the
 * specified array of integer coordinates, using the receiver's
 * background color. The array contains alternating x and y values
 * which are considered to represent points which are the vertices of
 * the polygon. Lines are drawn between each consecutive pair, and
 * between the first pair and last pair in the array.
 *
 * @param pointArray an array of alternating x and y values which are the vertices of the polygon
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT if pointArray is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawPolygon	
 */
public void fillPolygon(int[] pointArray) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (pointArray == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	float[] points = new float[pointArray.length];
	for (int i=0; i<points.length; i++) {
		points[i] = pointArray[i];
	}
	OS.CGContextBeginPath(handle);
	OS.CGContextAddLines(handle, points, points.length / 2);
	OS.CGContextClosePath(handle);
	OS.CGContextFillPath(handle);
}

/** 
 * Fills the interior of the rectangle specified by the arguments,
 * using the receiver's background color. 
 *
 * @param x the x coordinate of the rectangle to be filled
 * @param y the y coordinate of the rectangle to be filled
 * @param width the width of the rectangle to be filled
 * @param height the height of the rectangle to be filled
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawRectangle
 */
public void fillRectangle(int x, int y, int width, int height) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (width < 0) {
		x = x + width;
		width = -width;
	}
	if (height < 0) {
		y = y + height;
		height = -height;
	}
	CGRect rect = new CGRect();
	rect.x = x;
	rect.y = y;
	rect.width = width;
	rect.height = height;
	OS.CGContextFillRect(handle, rect);
}

/** 
 * Fills the interior of the specified rectangle, using the receiver's
 * background color. 
 *
 * @param rectangle the rectangle to be filled
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawRectangle
 */
public void fillRectangle(Rectangle rect) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (rect == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	fillRectangle(rect.x, rect.y, rect.width, rect.height);
}

/** 
 * Fills the interior of the round-cornered rectangle specified by 
 * the arguments, using the receiver's background color. 
 *
 * @param x the x coordinate of the rectangle to be filled
 * @param y the y coordinate of the rectangle to be filled
 * @param width the width of the rectangle to be filled
 * @param height the height of the rectangle to be filled
 * @param arcWidth the horizontal diameter of the arc at the four corners
 * @param arcHeight the vertical diameter of the arc at the four corners
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #drawRoundRectangle
 */
public void fillRoundRectangle(int x, int y, int width, int height, int arcWidth, int arcHeight) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (arcWidth == 0 || arcHeight == 0) {
		fillRectangle(x, y, width, height);
    	return;
	}
	OS.CGContextBeginPath(handle);
	OS.CGContextSaveGState(handle);
	OS.CGContextTranslateCTM(handle, x, y);
	OS.CGContextScaleCTM(handle, arcWidth, arcHeight);
    float fw = width / (float)arcWidth;
	float fh = height / (float)arcHeight;
	OS.CGContextMoveToPoint(handle, fw, fh/2);
	OS.CGContextAddArcToPoint(handle, fw, fh, fw/2, fh, 1);
	OS.CGContextAddArcToPoint(handle, 0, fh, 0, fh/2, 1);
	OS.CGContextAddArcToPoint(handle, 0, 0, fw/2, 0, 1);
	OS.CGContextAddArcToPoint(handle, fw, 0, fw, fh/2, 1);
	OS.CGContextClosePath(handle);
	OS.CGContextRestoreGState(handle);
	OS.CGContextFillPath(handle);
}

/**
 * Returns the <em>advance width</em> of the specified character in
 * the font which is currently selected into the receiver.
 * <p>
 * The advance width is defined as the horizontal distance the cursor
 * should move after printing the character in the selected font.
 * </p>
 *
 * @param ch the character to measure
 * @return the distance in the x direction to move past the character before painting the next
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public int getAdvanceWidth(char ch) {	
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	//NOT DONE
	return stringExtent(new String(new char[]{ch})).x;
}

/** 
 * Returns the background color.
 *
 * @return the receiver's background color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Color getBackground() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return Color.carbon_new (data.device, data.background);
}

/**
 * Returns the width of the specified character in the font
 * selected into the receiver. 
 * <p>
 * The width is defined as the space taken up by the actual
 * character, not including the leading and tailing whitespace
 * or overhang.
 * </p>
 *
 * @param ch the character to measure
 * @return the width of the character
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public int getCharWidth(char ch) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	//NOT DONE
	return stringExtent(new String(new char[]{ch})).x;
}

/** 
 * Returns the bounding rectangle of the receiver's clipping
 * region. If no clipping region is set, the return value
 * will be a rectangle which covers the entire bounds of the
 * object the receiver is drawing on.
 *
 * @return the bounding rectangle of the clipping region
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Rectangle getClipping() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (data.clipRgn == 0) {
		int width = 0, height = 0;
		if (data.control != 0) {
			Rect bounds = new Rect();
			OS.GetControlBounds(data.control, bounds);
			width = bounds.right - bounds.left;
			height = bounds.bottom - bounds.top;
		}
		if (data.image != null) {
			int image = data.image.handle;
			width = OS.CGImageGetWidth(image);
			height = OS.CGImageGetHeight(image);
		}
		return new Rectangle(0, 0, width, height);
	}
	Rect bounds = new Rect();
	OS.GetRegionBounds(data.clipRgn, bounds);
	int width = bounds.right - bounds.left;
	int height = bounds.bottom - bounds.top;
	return new Rectangle(bounds.left, bounds.top, width, height);
}

/** 
 * Sets the region managed by the argument to the current
 * clipping region of the receiver.
 *
 * @param region the region to fill with the clipping region
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the region is null</li>
 * </ul>	
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void getClipping(Region region) {	
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (region == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (data.clipRgn == 0) {
		int width = 0, height = 0;
		if (data.control != 0) {
			Rect bounds = new Rect();
			OS.GetControlBounds(data.control, bounds);
			width = bounds.right - bounds.left;
			height = bounds.bottom - bounds.top;
		}
		if (data.image != null) {
			int image = data.image.handle;
			width = OS.CGImageGetWidth(image);
			height = OS.CGImageGetHeight(image);
		}
		OS.SetRectRgn(region.handle, (short) 0, (short) 0, (short) width, (short) height);
		return;
	}
	OS.CopyRgn(data.clipRgn, region.handle);
}

/** 
 * Returns the font currently being used by the receiver
 * to draw and measure text.
 *
 * @return the receiver's font
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Font getFont() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return data.font;
}

/**
 * Returns a FontMetrics which contains information
 * about the font currently being used by the receiver
 * to draw and measure text.
 *
 * @return font metrics for the receiver's font
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public FontMetrics getFontMetrics() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	Font font = data.font;
	FontInfo info = new FontInfo();
	OS.FetchFontInfo(font.id, font.size, font.style, info);
	FontMetrics fm = new FontMetrics();
	fm.ascent = info.ascent;
	fm.descent = info.descent;
	fm.leading = info.leading;
	/* This code is intentionaly comment. Not right for fixed width fonts. */
	//fm.averageCharWidth = info.widMax / 3;
	String s = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; 
	fm.averageCharWidth = stringExtent(s).x / s.length();
	fm.height = fm.ascent + fm.descent;
	return fm;
}

/** 
 * Returns the receiver's foreground color.
 *
 * @return the color used for drawing foreground things
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Color getForeground() {	
	if (handle == 0) SWT.error(SWT.ERROR_WIDGET_DISPOSED);
	return Color.carbon_new(data.device, data.foreground);	
}

/** 
 * Returns the receiver's line style, which will be one
 * of the constants <code>SWT.LINE_SOLID</code>, <code>SWT.LINE_DASH</code>,
 * <code>SWT.LINE_DOT</code>, <code>SWT.LINE_DASHDOT</code> or
 * <code>SWT.LINE_DASHDOTDOT</code>.
 *
 * @return the style used for drawing lines
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public int getLineStyle() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return data.lineStyle;
}

/** 
 * Returns the width that will be used when drawing lines
 * for all of the figure drawing operations (that is,
 * <code>drawLine</code>, <code>drawRectangle</code>, 
 * <code>drawPolyline</code>, and so forth.
 *
 * @return the receiver's line width 
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public int getLineWidth() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return data.lineWidth;
}

/** 
 * Returns <code>true</code> if this GC is drawing in the mode
 * where the resulting color in the destination is the
 * <em>exclusive or</em> of the color values in the source
 * and the destination, and <code>false</code> if it is
 * drawing in the mode where the destination color is being
 * replaced with the source color value.
 *
 * @return <code>true</code> true if the receiver is in XOR mode, and false otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public boolean getXORMode() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return data.xorMode;
}

/**
 * Returns an integer hash code for the receiver. Any two 
 * objects which return <code>true</code> when passed to 
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #equals
 */
public int hashCode() {
	return handle;
}

void init(Drawable drawable, GCData data, int context) {
	int colorspace = data.device.colorspace;
	OS.CGContextSetStrokeColorSpace(context, colorspace);
	OS.CGContextSetFillColorSpace(context, colorspace);
	float[] foreground = data.foreground;
	if (foreground != null) OS.CGContextSetStrokeColor(context, foreground);
	float[] background = data.background;
	if (background != null) OS.CGContextSetFillColor(context, background);

	int[] buffer = new int[1];
	OS.ATSUCreateTextLayout(buffer);
	if (buffer[0] == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	data.layout = buffer[0];
	OS.ATSUCreateStyle(buffer);
	if (buffer[0] == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	data.style = buffer[0];
	
	int ptr = OS.NewPtr(4);
	buffer[0] = context;
	OS.memcpy(ptr, buffer, 4);
	int[] tags = new int[]{OS.kATSUCGContextTag};
	int[] sizes = new int[]{4};
	int[] values = new int[]{ptr};
	OS.ATSUSetLayoutControls(data.layout, tags.length, tags, sizes, values);
	OS.DisposePtr(ptr);

	Image image = data.image;
	if (image != null) {
		image.memGC = this;
		/*
		 * The transparent pixel mask might change when drawing on
		 * the image.  Destroy it so that it is regenerated when
		 * necessary.
		 */
//		if (image.transparentPixel != -1) image.destroyMask();
	}
	this.drawable = drawable;
	this.data = data;
	handle = context;

	Font font = data.font;
	if (font != null) setFont(font);
}

/**
 * Returns <code>true</code> if the receiver has a clipping
 * region set into it, and <code>false</code> otherwise.
 * If this method returns false, the receiver will draw on all
 * available space in the destination. If it returns true, 
 * it will draw only in the area that is covered by the region
 * that can be accessed with <code>getClipping(region)</code>.
 *
 * @return <code>true</code> if the GC has a clipping region, and <code>false</code> otherwise
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public boolean isClipped() {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	return data.clipRgn != 0;
}

/**
 * Returns <code>true</code> if the GC has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the GC.
 * When a GC has been disposed, it is an error to
 * invoke any other method using the GC.
 *
 * @return <code>true</code> when the GC is disposed and <code>false</code> otherwise
 */
public boolean isDisposed() {
	return handle == 0;
}

/**
 * Sets the background color. The background color is used
 * for fill operations and as the background color when text
 * is drawn.
 *
 * @param color the new background color for the receiver
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setBackground(Color color) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (color == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	data.background = color.handle;
	OS.CGContextSetFillColor(handle, color.handle);
}

/**
 * Sets the area of the receiver which can be changed
 * by drawing operations to the rectangular area specified
 * by the arguments.
 *
 * @param x the x coordinate of the clipping rectangle
 * @param y the y coordinate of the clipping rectangle
 * @param width the width of the clipping rectangle
 * @param height the height of the clipping rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setClipping(int x, int y, int width, int height) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (data.clipRgn == 0) data.clipRgn = OS.NewRgn();
	OS.SetRectRgn(data.clipRgn, (short)x, (short)y, (short)(x + width), (short)(y + height));
	setCGClipping();
}

/**
 * Sets the area of the receiver which can be changed
 * by drawing operations to the rectangular area specified
 * by the argument.
 *
 * @param rect the clipping rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setClipping(Rectangle r) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (r == null) {
		if (data.clipRgn != 0) {
			OS.DisposeRgn(data.clipRgn);
			data.clipRgn = 0;
		} else {
			return;
		}
	} else {
		if (data.clipRgn == 0) data.clipRgn = OS.NewRgn();
		OS.SetRectRgn(data.clipRgn, (short)r.x, (short)r.y, (short)(r.x + r.width), (short)(r.y + r.height));
	}
	setCGClipping();
}

/**
 * Sets the area of the receiver which can be changed
 * by drawing operations to the region specified
 * by the argument.
 *
 * @param rect the clipping region.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setClipping(Region region) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (region == null) {
		if (data.clipRgn != 0) {
			OS.DisposeRgn(data.clipRgn);
			data.clipRgn = 0;
		} else {
			return;
		}
	} else {
		if (data.clipRgn == 0) data.clipRgn = OS.NewRgn();
		OS.CopyRgn(region.handle, data.clipRgn);
	}
	setCGClipping();
}

void setCGClipping () {
	if (data.control == 0) {
		OS.CGContextScaleCTM(handle, 1, -1);
		if (data.clipRgn != 0) {
			OS.ClipCGContextToRegion(handle, new Rect(), data.clipRgn);
		} else {
			int rgn = OS.NewRgn();
			OS.SetRectRgn(rgn, (short)-32768, (short)-32768, (short)32767, (short)32767);
			OS.ClipCGContextToRegion(handle, new Rect(), rgn);
			OS.DisposeRgn(rgn);
		}
		OS.CGContextScaleCTM(handle, 1, -1);
		return;
	}
	int window = OS.GetControlOwner(data.control);
	int port = OS.GetWindowPort(window);
	Rect rect = new Rect();
	OS.GetControlBounds(data.control, rect);
	Rect portRect = new Rect();
	OS.GetPortBounds(port, portRect);
	int portHeight = portRect.bottom - portRect.top;
	OS.CGContextTranslateCTM(handle, -rect.left, portHeight - rect.top);
	OS.CGContextScaleCTM(handle, 1, -1);
	if (data.clipRgn != 0) { 
		int rgn = OS.NewRgn();
		OS.CopyRgn(data.clipRgn, rgn);
		OS.OffsetRgn(rgn, rect.left, rect.top);
		OS.SectRgn(data.visibleRgn, rgn, rgn);
		OS.ClipCGContextToRegion(handle, portRect, rgn);
		OS.DisposeRgn(rgn);
	} else {
		OS.ClipCGContextToRegion(handle, portRect, data.visibleRgn);
	}
	OS.CGContextScaleCTM(handle, 1, -1);
	OS.CGContextTranslateCTM(handle, rect.left, -portHeight + rect.top);
}

/** 
 * Sets the font which will be used by the receiver
 * to draw and measure text to the argument. If the
 * argument is null, then a default font appropriate
 * for the platform will be used instead.
 *
 * @param font the new font for the receiver, or null to indicate a default font
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the font has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setFont(Font font) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (font == null) font = data.device.systemFont;
	if (font.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	data.font = font;
	int ptr = OS.NewPtr(16);
	OS.memcpy(ptr, new int[]{font.handle}, 4); 
	OS.memcpy(ptr + 4, new int[]{font.size << 16}, 4); 
	OS.memcpy(ptr + 8, new byte[]{(font.style & OS.bold) != 0 ? (byte)1 : 0}, 1); 
	OS.memcpy(ptr + 9, new byte[]{(font.style & OS.italic) != 0 ? (byte)1 : 0}, 1); 
	int[] tags = new int[]{OS.kATSUFontTag, OS.kATSUSizeTag, OS.kATSUQDBoldfaceTag, OS.kATSUQDItalicTag};
	int[] sizes = new int[]{4, 4, 1, 1};
	int[] values = new int[]{ptr, ptr + 4, ptr + 8, ptr + 9};
	OS.ATSUSetAttributes(data.style, tags.length, tags, sizes, values);
	OS.DisposePtr(ptr);
	FontInfo info = new FontInfo();
	OS.FetchFontInfo(font.id, font.size, font.style, info);
	data.fontAscent = info.ascent;
	data.fontDescent = info.descent;
}

/**
 * Sets the foreground color. The foreground color is used
 * for drawing operations including when text is drawn.
 *
 * @param color the new foreground color for the receiver
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the color has been disposed</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setForeground(Color color) {	
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (color == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (color.isDisposed()) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	data.foreground = color.handle;
	OS.CGContextSetStrokeColor(handle, color.handle);
}

/** 
 * Sets the receiver's line style to the argument, which must be one
 * of the constants <code>SWT.LINE_SOLID</code>, <code>SWT.LINE_DASH</code>,
 * <code>SWT.LINE_DOT</code>, <code>SWT.LINE_DASHDOT</code> or
 * <code>SWT.LINE_DASHDOTDOT</code>.
 *
 * @param lineStyle the style to be used for drawing lines
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setLineStyle(int lineStyle) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	switch (lineStyle) {
		case SWT.LINE_SOLID:
			OS.CGContextSetLineDash(handle, 0, null, 0);
			break;
		case SWT.LINE_DASH:
			OS.CGContextSetLineDash(handle, 0, new float[]{18, 6}, 2);
			break;
		case SWT.LINE_DOT:
			OS.CGContextSetLineDash(handle, 0, new float[]{3, 3}, 2);
			break;
		case SWT.LINE_DASHDOT:
			OS.CGContextSetLineDash(handle, 0, new float[]{9, 6, 3, 6}, 4);
			break;
		case SWT.LINE_DASHDOTDOT:
			OS.CGContextSetLineDash(handle, 0, new float[]{9, 3, 3, 3, 3, 3}, 6);
			break;
		default:
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	data.lineStyle = lineStyle;
}

/** 
 * Sets the width that will be used when drawing lines
 * for all of the figure drawing operations (that is,
 * <code>drawLine</code>, <code>drawRectangle</code>, 
 * <code>drawPolyline</code>, and so forth.
 *
 * @param lineWidth the width of a line
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setLineWidth(int width) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	data.lineWidth = width;
	OS.CGContextSetLineWidth(handle, width);
}

/** 
 * If the argument is <code>true</code>, puts the receiver
 * in a drawing mode where the resulting color in the destination
 * is the <em>exclusive or</em> of the color values in the source
 * and the destination, and if the argument is <code>false</code>,
 * puts the receiver in a drawing mode where the destination color
 * is replaced with the source color value.
 *
 * @param xor if <code>true</code>, then <em>xor</em> mode is used, otherwise <em>source copy</em> mode is used
 *
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void setXORMode(boolean xor) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	//NOT DONE
	data.xorMode = xor;
}

/**
 * Returns the extent of the given string. No tab
 * expansion or carriage return processing will be performed.
 * <p>
 * The <em>extent</em> of a string is the width and height of
 * the rectangular area it would cover if drawn in a particular
 * font (in this case, the current font in the receiver).
 * </p>
 *
 * @param string the string to measure
 * @return a point containing the extent of the string
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Point stringExtent(String string) {	
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	int length = string.length();
	if (length == 0) return new Point(0, data.fontAscent + data.fontDescent);
	char[] buffer = new char[length];
	string.getChars(0, length, buffer, 0);
	int ptr1 = OS.NewPtr(length * 2);
	OS.memcpy(ptr1, buffer, length * 2);
	OS.ATSUSetTextPointerLocation(data.layout, ptr1, 0, length, length);
	OS.ATSUSetRunStyle(data.layout, data.style, 0, length);
	int ptr2 = OS.NewPtr(ATSTrapezoid.sizeof);
	OS.ATSUGetGlyphBounds(data.layout, 0, 0, 0, length, (short)OS.kATSUseDeviceOrigins, 1, ptr2, null);
	OS.DisposePtr(ptr1);
	ATSTrapezoid trapezoid = new ATSTrapezoid();
	OS.memcpy(trapezoid, ptr2, ATSTrapezoid.sizeof);
	OS.DisposePtr(ptr2);
	int width = (trapezoid.upperRight_x >> 16) - (trapezoid.upperLeft_x >> 16);
	int height = (trapezoid.lowerRight_y >> 16) - (trapezoid.upperRight_y >> 16);
	return new Point(width, height);
}

/**
 * Returns the extent of the given string. Tab expansion and
 * carriage return processing are performed.
 * <p>
 * The <em>extent</em> of a string is the width and height of
 * the rectangular area it would cover if drawn in a particular
 * font (in this case, the current font in the receiver).
 * </p>
 *
 * @param string the string to measure
 * @return a point containing the extent of the string
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Point textExtent(String string) {
	return textExtent(string, SWT.DRAW_DELIMITER | SWT.DRAW_TAB);
}

/**
 * Returns the extent of the given string. Tab expansion, line
 * delimiter and mnemonic processing are performed according to
 * the specified flags, which can be a combination of:
 * <dl>
 * <dt><b>DRAW_DELIMITER</b></dt>
 * <dd>draw multiple lines</dd>
 * <dt><b>DRAW_TAB</b></dt>
 * <dd>expand tabs</dd>
 * <dt><b>DRAW_MNEMONIC</b></dt>
 * <dd>underline the mnemonic character</dd>
 * <dt><b>DRAW_TRANSPARENT</b></dt>
 * <dd>transparent background</dd>
 * </dl>
 * <p>
 * The <em>extent</em> of a string is the width and height of
 * the rectangular area it would cover if drawn in a particular
 * font (in this case, the current font in the receiver).
 * </p>
 *
 * @param string the string to measure
 * @param flags the flags specifing how to process the text
 * @return a point containing the extent of the string
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the string is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_GRAPHIC_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Point textExtent(String string, int flags) {
	if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
	if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	//NOT DONE
	return stringExtent(string);
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the receiver
 */
public String toString () {
	if (isDisposed()) return "GC {*DISPOSED*}";
	return "GC {" + handle + "}";
}

}
