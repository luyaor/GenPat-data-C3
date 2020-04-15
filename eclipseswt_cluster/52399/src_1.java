package org.eclipse.swt.graphics;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.*;
import java.io.*;

/**
 * Instances of this class are graphics which have been prepared
 * for display on a specific device. That is, they are ready
 * to paint using methods such as <code>GC.drawImage()</code>
 * and display on widgets with, for example, <code>Button.setImage()</code>.
 * <p>
 * If loaded from a file format that supports it, an
 * <code>Image</code> may have transparency, meaning that certain
 * pixels are specified as being transparent when drawn. Examples
 * of file formats that support transparency are GIF and PNG.
 * </p><p>
 * There are two primary ways to use <code>Images</code>. 
 * The first is to load a graphic file from disk and create an
 * <code>Image</code> from it. This is done using an <code>Image</code>
 * constructor, for example:
 * <pre>
 *    Image i = new Image(device, "C:\\graphic.bmp");
 * </pre>
 * A graphic file may contain a color table specifying which
 * colors the image was intended to possess. In the above example,
 * these colors will be mapped to the closest available color in
 * SWT. It is possible to get more control over the mapping of
 * colors as the image is being created, using code of the form:
 * <pre>
 *    ImageData data = new ImageData("C:\\graphic.bmp"); 
 *    RGB[] rgbs = data.getRGBs(); 
 *    // At this point, rgbs contains specifications of all
 *    // the colors contained within this image. You may
 *    // allocate as many of these colors as you wish by
 *    // using the Color constructor Color(RGB), then
 *    // create the image:
 *    Image i = new Image(device, data);</code>
 * </pre>
 * <p>
 * Applications which require even greater control over the image
 * loading process should use the support provided in class
 * <code>ImageLoader</code>.
 * </p><p>
 * Application code must explicitely invoke the <code>Image.dispose()</code> 
 * method to release the operating system resources managed by each instance
 * when those instances are no longer required.
 * </p>
 *
 * @see Color
 * @see ImageData
 * @see ImageLoader
 */

public final class Image implements Drawable {
	
	/**
	 * specifies whether the receiver is a bitmap or an icon
	 * (one of <code>SWT.BITMAP</code>, <code>SWT.ICON</code>)
	 */
	public int type;
	
	/**
	 * the OS resource of the image
	 * (Warning: This field is platform dependent)
	 */
	public int handle;
	
	/**
	 * the device where this image was created
	 */
	Device device;
	
	/**
	 * specifies the transparent pixel
	 * (Warning: This field is platform dependent)
	 */
	int transparentPixel = -1;
	
	/**
	 * the GC which is drawing on the image
	 * (Warning: This field is platform dependent)
	 */
	GC memGC;
	
	/**
	 * the alpha data for the image
	 * (Warning: This field is platform dependent)
	 */
	byte[] alphaData;
	
	/**
	 * the global alpha value to be used for every pixel
	 * (Warning: This field is platform dependent)
	 */
	int alpha = -1;
	
	/**
	 * specifies the default scanline padding
	 * (Warning: This field is platform dependent)
	 */
	static final int DEFAULT_SCANLINE_PAD = 4;

/**
 * Prevents uninitialized instances from being created outside the package.
 */
Image () {
}

/**
 * Constructs an empty instance of this class with the
 * specified width and height. The result may be drawn upon
 * by creating a GC and using any of its drawing operations,
 * as shown in the following example:
 * <pre>
 *    Image i = new Image(device, width, height);
 *    GC gc = new GC(i);
 *    gc.drawRectangle(0, 0, 50, 50);
 *    gc.dispose();
 * </pre>
 *
 * @param device the device on which to create the image
 * @param width the width of the new image
 * @param height the height of the new image
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if either the width or height is negative</li>
 * </ul>
 */
public Image(Device device, int width, int height) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, width, height);
	if (device.tracking) device.new_Object(this);	
}

/**
 * Constructs a new instance of this class based on the
 * provided image, with an appearance that varies depending
 * on the value of the flag. The possible flag values are:
 * <dl>
 * <dt><b>IMAGE_COPY</b></dt>
 * <dd>the result is an identical copy of srcImage</dd>
 * <dt><b>IMAGE_DISABLE</b></dt>
 * <dd>the result is a copy of srcImage which has a <em>disabled</em> look</dd>
 * <dt><b>IMAGE_GRAY</b></dt>
 * <dd>the result is a copy of srcImage which has a <em>gray scale</em> look</dd>
 * </dl>
 *
 * @param device the device on which to create the image
 * @param srcImage the image to use as the source
 * @param flag the style, either <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code>
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if srcImage is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the flag is not one of <code>IMAGE_COPY</code>, <code>IMAGE_DISABLE</code> or <code>IMAGE_GRAY</code></li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon, or
 *          is otherwise in an invalid state</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for image creation</li>
 * </ul>
 */
public Image(Device device, Image srcImage, int flag) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	this.device = device;
	if (srcImage == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	switch (flag) {
		case SWT.IMAGE_COPY: {
			Rectangle r = srcImage.getBounds();
			this.type = srcImage.type;
			switch (type) {
				case SWT.BITMAP:
					/* Get the HDC for the device */
					int hDC = device.internal_new_GC(null);
					
					/* Copy the bitmap */
					int hdcSource = OS.CreateCompatibleDC(hDC);
					int hdcDest = OS.CreateCompatibleDC(hDC);
					int hOldSrc = OS.SelectObject(hdcSource, srcImage.handle);
					handle = OS.CreateCompatibleBitmap(hdcSource, r.width, r.height);
					if (handle == 0) SWT.error(SWT.ERROR_NO_HANDLES);
					int hOldDest = OS.SelectObject(hdcDest, handle);
					OS.BitBlt(hdcDest, 0, 0, r.width, r.height, hdcSource, 0, 0, OS.SRCCOPY);
					OS.SelectObject(hdcSource, hOldSrc);
					OS.SelectObject(hdcDest, hOldDest);
					OS.DeleteDC(hdcSource);
					OS.DeleteDC(hdcDest);

					/* Release the HDC for the device */
					device.internal_dispose_GC(hDC, null);

					transparentPixel = srcImage.transparentPixel;
					alpha = srcImage.alpha;
					if (srcImage.alphaData != null) {
						alphaData = new byte[srcImage.alphaData.length];
						System.arraycopy(srcImage.alphaData, 0, alphaData, 0, alphaData.length);
					}
					break;
				case SWT.ICON:
					handle = OS.CopyImage(srcImage.handle, OS.IMAGE_ICON, r.width, r.height, 0);
					if (handle == 0) SWT.error(SWT.ERROR_NO_HANDLES);			
					break;
				default:
					SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			}
			if (device.tracking) device.new_Object(this);	
			return;
		}
		case SWT.IMAGE_DISABLE: {
			Rectangle r = srcImage.getBounds();
			this.type = srcImage.type;
			byte[] rgbBwBitmapInfo = {
				40,0,0,0, /* biSize */
				(byte)(r.width & 0xFF), /* biWidth */
				(byte)((r.width & 0xFF00) >> 8),
				(byte)((r.width & 0xFF0000) >> 16),
				(byte)((r.width & 0xFF000000) >> 24),
				(byte)(r.height & 0xFF), /* biHeight */
				(byte)((r.height & 0xFF00) >> 8),
				(byte)((r.height & 0xFF0000) >> 16),
				(byte)((r.height & 0xFF000000) >> 24),
				1,0, /* biPlanes */
				1,0, /* biBitCount */
				0,0,0,0, /* biCompression */
				0,0,0,0, /* biSizeImage */
				0,0,0,0, /* biXPelsPerMeter */
				0,0,0,0, /* biYPelsPerMeter */
				0,0,0,0, /* biClrUsed */
				0,0,0,0, /* biClrImportant */
				0,0,0,0, /* First color: black */
				(byte)0xFF,(byte)0xFF,(byte)0xFF,0 /* Second color: white */
			};

			/* Get the HDC for the device */
			int hDC = device.internal_new_GC(null);

			/* Source DC */
			int hdcSource = OS.CreateCompatibleDC(hDC);
			if (hdcSource == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			/* Monochrome (Intermediate) DC */
			int bwDC = OS.CreateCompatibleDC(hdcSource);
			if (bwDC == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			/* Destination DC */
			int hdcBmp = OS.CreateCompatibleDC(hDC);
			if (hdcBmp == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			/* Monochrome (Intermediate) DIB section */
			int[] pbitsBW = new int[1];
			int hbmBW = OS.CreateDIBSection(bwDC, rgbBwBitmapInfo, OS.DIB_RGB_COLORS, pbitsBW, 0, 0);
			if (hbmBW == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			switch (type) {
				case SWT.BITMAP:
					/* Attach the bitmap to the source DC */
					int hOldSrc = OS.SelectObject(hdcSource, srcImage.handle);
					/* Create the destination bitmap */
					handle = OS.CreateCompatibleBitmap(hDC, r.width, r.height);
					if (handle == 0) SWT.error(SWT.ERROR_NO_HANDLES);
					/* Attach the DIB section and the new bitmap to the DCs */
					int hOldBw = OS.SelectObject(bwDC, hbmBW);
					int hOldBmp = OS.SelectObject(hdcBmp, handle);
					/* BitBlt the bitmap into the monochrome DIB section */
					OS.BitBlt(bwDC, 0, 0, r.width, r.height, hdcSource, 0, 0, OS.SRCCOPY);
					/* Paint the destination rectangle in gray */
					RECT rect = new RECT();
					rect.left = 0;
					rect.top = 0;
					rect.right = r.width;
					rect.bottom = r.height;
					OS.FillRect(hdcBmp, rect, OS.GetSysColorBrush(OS.COLOR_3DFACE));
					/*
					 * BitBlt the black bits in the monochrome bitmap into
					 * COLOR_3DHILIGHT bits in the destination DC.
					 * The magic ROP comes from Charles Petzold's book
					 */
					int hb = OS.CreateSolidBrush(OS.GetSysColor(OS.COLOR_3DHILIGHT));
					int oldBrush = OS.SelectObject(hdcBmp, hb);
					OS.BitBlt(hdcBmp, 1, 1, r.width, r.height, bwDC, 0, 0, 0xB8074A);
					/*
					 * BitBlt the black bits in the monochrome bitmap into 
					 * COLOR_3DSHADOW bits in the destination DC.
					 */
					hb = OS.CreateSolidBrush(OS.GetSysColor(OS.COLOR_3DSHADOW));
					OS.DeleteObject(OS.SelectObject(hdcBmp, hb));
					OS.BitBlt(hdcBmp, 0, 0, r.width, r.height, bwDC, 0, 0, 0xB8074A);
					OS.DeleteObject(OS.SelectObject(hdcBmp, oldBrush));
					/* Free resources */
					OS.SelectObject(hdcSource, hOldSrc);
					OS.SelectObject(hdcBmp, hOldBmp);
					OS.SelectObject(bwDC, hOldBw);
					OS.DeleteDC(hdcSource);
					OS.DeleteDC(bwDC);
					OS.DeleteDC(hdcBmp);
					OS.DeleteObject(hbmBW);
					
					/* Release the HDC for the device */
					device.internal_dispose_GC(hDC, null);
					break;
				case SWT.ICON:
					/* Get icon information */
					ICONINFO iconInfo = new ICONINFO();
					if (!OS.GetIconInfo(srcImage.handle, iconInfo))
						SWT.error(SWT.ERROR_INVALID_IMAGE);
					int hdcMask = OS.CreateCompatibleDC(hDC);
					/* Create the destination bitmaps */
					if (iconInfo.hbmColor == 0)
						hOldSrc = OS.SelectObject(hdcSource, iconInfo.hbmMask);
					else
						hOldSrc = OS.SelectObject(hdcSource, iconInfo.hbmColor);
					int newHbmp = OS.CreateCompatibleBitmap(hdcSource, r.width, r.height);
					if (newHbmp == 0) SWT.error(SWT.ERROR_NO_HANDLES);
					int newHmask = OS.CreateBitmap(r.width, r.height, 1, 1, null);
					if (newHmask == 0) SWT.error(SWT.ERROR_NO_HANDLES);
					/* BitBlt the source mask into the destination mask */
					int hOldMask = OS.SelectObject(hdcMask, newHmask);
					if (iconInfo.hbmColor != 0)
						OS.SelectObject(hdcSource, iconInfo.hbmMask);
					OS.SelectObject(hdcSource, iconInfo.hbmMask);
					OS.BitBlt(hdcMask, 0, 0, r.width, r.height, hdcSource, 0, 0, OS.SRCCOPY);
					/* Attach the monochrome DIB section and the destination bitmap to the DCs */
					hOldBw = OS.SelectObject(bwDC, hbmBW);
					/* BitBlt the bitmap into the monochrome DIB section */
					if (iconInfo.hbmColor == 0) {
						OS.SelectObject(hdcSource, iconInfo.hbmMask);
						OS.BitBlt(bwDC, 0, 0, r.width, r.height, hdcSource, 0, r.height, OS.SRCCOPY);
					} else {
						OS.SelectObject(hdcSource, iconInfo.hbmColor);
						OS.BitBlt(bwDC, 0, 0, r.width, r.height, hdcSource, 0, 0, OS.SRCCOPY);
					}
					/* Paint the destination rectangle in grey */
					rect = new RECT();
					rect.left = 0;
					rect.top = 0;
					rect.right = r.width;
					rect.bottom = r.height;
					hOldBmp = OS.SelectObject(hdcBmp, newHbmp);
					OS.FillRect(hdcBmp, rect, OS.GetSysColorBrush(OS.COLOR_3DFACE));
					/*
					 * BitBlt the black bits in the monochrome bitmap into
					 * COLOR_3DHILIGHT bits in the destination DC.
					 * The magic ROP comes from Charles Petzold's book
					 */
					hb = OS.CreateSolidBrush(OS.GetSysColor(OS.COLOR_3DSHADOW));
					oldBrush = OS.SelectObject(hdcBmp, hb);
					OS.BitBlt(hdcBmp, 0, 0, r.width, r.height, bwDC, 0, 0, 0xB8074A);
					/* Invert mask into hdcBw */
					OS.BitBlt(bwDC, 0, 0, r.width, r.height, hdcMask, 0, 0, OS.NOTSRCCOPY);
					/* Select black brush into destination */
					hb = OS.CreateSolidBrush(0);
					OS.DeleteObject(OS.SelectObject(hdcBmp, hb));
					/*
					 * Copy black bits from monochrome bitmap into black bits in the
					 * destination DC.
					 */
					OS.BitBlt(hdcBmp, 0, 0, r.width, r.height, bwDC, 0, 0, 0xB8074A);
					OS.DeleteObject(OS.SelectObject(hdcBmp, oldBrush));
					/* Free resources */
					OS.SelectObject(hdcSource, hOldSrc);
					OS.DeleteDC(hdcSource);
					OS.SelectObject(bwDC, hOldBw);
					OS.DeleteDC(bwDC);
					OS.SelectObject(hdcBmp, hOldBmp);
					OS.DeleteDC(hdcBmp);
					OS.SelectObject(hdcMask, hOldMask);
					OS.DeleteDC(hdcMask);
					OS.DeleteObject(hbmBW);
					
					/* Release the HDC for the device */
					device.internal_dispose_GC(hDC, null);
			
					/* Create the new iconinfo */
					ICONINFO newIconInfo = new ICONINFO();
					newIconInfo.fIcon = iconInfo.fIcon;
					newIconInfo.hbmMask = newHmask;
					newIconInfo.hbmColor = newHbmp;
					/* Create the new icon */
					handle = OS.CreateIconIndirect(newIconInfo);
					if (handle == 0) SWT.error(SWT.ERROR_NO_HANDLES);
					/* Free bitmaps */
					OS.DeleteObject(newHbmp);
					OS.DeleteObject(newHmask);
					if (iconInfo.hbmColor != 0)
						OS.DeleteObject(iconInfo.hbmColor);
					OS.DeleteObject(iconInfo.hbmMask);
					break;
				default:
					SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			}
			if (device.tracking) device.new_Object(this);	
			return;
		}
		case SWT.IMAGE_GRAY: {
			Rectangle r = srcImage.getBounds();
			ImageData data = srcImage.getImageData();
			PaletteData palette = data.palette;
			ImageData newData = data;
			if (!palette.isDirect) {
				/* Convert the palette entries to gray. */
				RGB [] rgbs = palette.getRGBs();
				for (int i=0; i<rgbs.length; i++) {
					if (data.transparentPixel != i) {
						RGB color = rgbs [i];
						int red = color.red;
						int green = color.green;
						int blue = color.blue;
						int intensity = (red+red+green+green+green+green+green+blue) >> 3;
						color.red = color.green = color.blue = intensity;
					}
				}
				newData.palette = new PaletteData(rgbs);
			} else {
				/* Create a 8 bit depth image data with a gray palette. */
				RGB[] rgbs = new RGB[256];
				for (int i=0; i<rgbs.length; i++) {
					rgbs[i] = new RGB(i, i, i);
				}
				newData = new ImageData(r.width, r.height, 8, new PaletteData(rgbs));
				newData.maskData = data.maskData;
				newData.maskPad = data.maskPad;

				/* Convert the pixels. */
				int[] scanline = new int[r.width];
				int redMask = palette.redMask;
				int greenMask = palette.greenMask;
				int blueMask = palette.blueMask;
				int redShift = palette.redShift;
				int greenShift = palette.greenShift;
				int blueShift = palette.blueShift;
				for (int y=0; y<r.height; y++) {
					int offset = y * newData.bytesPerLine;
					data.getPixels(0, y, r.width, scanline, 0);
					for (int x=0; x<r.width; x++) {
						int pixel = scanline[x];
						int red = pixel & redMask;
						red = (redShift < 0) ? red >>> -redShift : red << redShift;
						int green = pixel & greenMask;
						green = (greenShift < 0) ? green >>> -greenShift : green << greenShift;
						int blue = pixel & blueMask;
						blue = (blueShift < 0) ? blue >>> -blueShift : blue << blueShift;
						newData.data[offset++] =
							(byte)((red+red+green+green+green+green+green+blue) >> 3);
					}
				}
			}
			init (device, newData);
			if (device.tracking) device.new_Object(this);	
			return;
		}
		default:
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
}

/**
 * Constructs an empty instance of this class with the
 * width and height of the specified rectangle. The result
 * may be drawn upon by creating a GC and using any of its
 * drawing operations, as shown in the following example:
 * <pre>
 *    Image i = new Image(device, boundsRectangle);
 *    GC gc = new GC(i);
 *    gc.drawRectangle(0, 0, 50, 50);
 *    gc.dispose();
 * </pre>
 *
 * @param device the device on which to create the image
 * @param bounds a rectangle specifying the image's width and height (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the bounds rectangle is null</li>
 * </ul>
 */
public Image(Device device, Rectangle bounds) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (bounds == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, bounds.width, bounds.height);
	if (device.tracking) device.new_Object(this);	
}

/**
 * Constructs an instance of this class from the given
 * <code>ImageData</code>.
 *
 * @param device the device on which to create the image
 * @param data the image data to create the image from (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the image data is null</li>
 * </ul>
 */
public Image(Device device, ImageData data) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, data);
	if (device.tracking) device.new_Object(this);	
}

/**
 * Constructs an instance of this class, whose type is 
 * <code>SWT.ICON</code>, from the two given <code>ImageData</code>
 * objects. The two images must be the same size, and the mask image
 * must have a color depth of 1. Pixel transparency in either image
 * will be ignored. If either image is an icon to begin with, an
 * exception is thrown.
 * <p>
 * The mask image should contain white wherever the icon is to be visible,
 * and black wherever the icon is to be transparent. In addition,
 * the source image should contain black wherever the icon is to be
 * transparent.
 * </p>
 *
 * @param device the device on which to create the icon
 * @param source the color data for the icon
 * @param mask the mask data for the icon
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if either the source or mask is null </li>
 *    <li>ERROR_INVALID_ARGUMENT - if source and mask are different sizes or
 *          if the mask is not monochrome, or if either the source or mask
 *          is already an icon</li>
 * </ul>
 */
public Image(Device device, ImageData source, ImageData mask) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (source == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (mask == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (source.width != mask.width || source.height != mask.height) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	if (mask.depth != 1) {
		/*
		 * Feature in Windows. 1-bit DIB sections are buggy on Win98, so we
		 * create 4-bit DIBs when given a 1-bit ImageData. In order to allow
		 * users to draw on the masks, we must also support 4-bit masks in
		 * icon creation by converting them into 1-bit masks.
		 */
		if (mask.depth != 4) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		PaletteData palette = new PaletteData(new RGB[] {new RGB(0, 0, 0), new RGB(255,255,255)});
		ImageData tempMask = new ImageData(mask.width, mask.height, 1, palette);
		/* Find index of black in mask palette */
		RGB[] rgbs = mask.getRGBs();
		int blackIndex = 0;
		while (blackIndex < rgbs.length) {
			if (rgbs[blackIndex].equals(palette.colors[0])) break;
			blackIndex++;
		}
		if (blackIndex == rgbs.length) SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		int[] pixels = new int[mask.width];
		for (int y = 0; y < mask.height; y++) {
			mask.getPixels(0, y, mask.width, pixels, 0);
			for (int i = 0; i < pixels.length; i++) {
				if (pixels[i] == blackIndex) {
					pixels[i] = 0;
				} else {
					pixels[i] = 1;
				}
			}
			tempMask.setPixels(0, y, mask.width, pixels, 0);
		}
		mask = tempMask;
	}
	/* Create a temporary image and locate the black pixel */
	ImageData image;
	int blackIndex = 0;
	if (source.palette.isDirect) {
		image = new ImageData(source.width, source.height, source.depth, source.palette);
	} else {
		RGB black = new RGB(0, 0, 0);
		RGB[] rgbs = source.getRGBs();
		if (source.transparentPixel != -1) {
			/*
			 * The source had transparency, so we can use the transparent pixel
			 * for black.
			 */
			RGB[] newRGBs = new RGB[rgbs.length];
			System.arraycopy(rgbs, 0, newRGBs, 0, rgbs.length);
			if (source.transparentPixel >= newRGBs.length) {
				/* Grow the palette with black */
				rgbs = new RGB[source.transparentPixel + 1];
				System.arraycopy(newRGBs, 0, rgbs, 0, newRGBs.length);
				for (int i = newRGBs.length; i <= source.transparentPixel; i++) {
					rgbs[i] = new RGB(0, 0, 0);
				}
			} else {
				newRGBs[source.transparentPixel] = black;
				rgbs = newRGBs;
			}
			blackIndex = source.transparentPixel;
			image = new ImageData(source.width, source.height, source.depth, new PaletteData(rgbs));
		} else {
			while (blackIndex < rgbs.length) {
				if (rgbs[blackIndex].equals(black)) break;
				blackIndex++;
			}
			if (blackIndex == rgbs.length) {
				/*
				 * We didn't find black in the palette, and there is no transparent
				 * pixel we can use.
				 */
				if ((1 << source.depth) > rgbs.length) {
					/* We can grow the palette and add black */
					RGB[] newRGBs = new RGB[rgbs.length + 1];
					System.arraycopy(rgbs, 0, newRGBs, 0, rgbs.length);
					newRGBs[rgbs.length] = black;
					rgbs = newRGBs;
				} else {
					/* No room to grow the palette */
					blackIndex = -1;
				}
			}
			image = new ImageData(source.width, source.height, source.depth, new PaletteData(rgbs));
		}
	}
	if (blackIndex == -1) {
		/* There was no black in the palette, so just copy the data over */
		System.arraycopy(source.data, 0, image.data, 0, image.data.length);
	} else {
		/* Modify the source image to contain black wherever the mask is 0 */
		int[] imagePixels = new int[image.width];
		int[] maskPixels = new int[mask.width];
		for (int y = 0; y < image.height; y++) {
			source.getPixels(0, y, image.width, imagePixels, 0);
			mask.getPixels(0, y, mask.width, maskPixels, 0);
			for (int i = 0; i < imagePixels.length; i++) {
				if (maskPixels[i] == 0) imagePixels[i] = blackIndex;
			}
			image.setPixels(0, y, source.width, imagePixels, 0);
		}
	}
	/*
	 * Make sure the mask is padded properly. Windows requires icon masks
	 * to have a scanline pad of 2.
	 */
	int bytesPerLine = (((mask.width + 7) / 8) + 1) / 2 * 2;
	byte[] newMaskData = new byte[bytesPerLine * mask.height];
	ImageData newMask = new ImageData(mask.width, mask.height, 1, mask.palette, 2, newMaskData);
	int[] maskPixels = new int[mask.width];
	for (int y = 0; y < mask.height; y++) {
		mask.getPixels(0, y, mask.width, maskPixels, 0);
		newMask.setPixels(0, y, newMask.width, maskPixels, 0);
	}
	/* Set the fields and create the icon */
	image.maskPad = newMask.scanlinePad;
	image.maskData = newMask.data;
	init(device, image);
	if (device.tracking) device.new_Object(this);	
}

/**
 * Constructs an instance of this class by loading its representation
 * from the specified input stream. Throws an error if an error
 * occurs while loading the image, or if the result is an image
 * of an unsupported type.
 * <p>
 * This constructor is provided for convenience when loading a single
 * image only. If the stream contains multiple images, only the first
 * one will be loaded. To load multiple images, use 
 * <code>ImageLoader.load()</code>.
 * </p><p>
 * This constructor may be used to load a resource as follows:
 * </p>
 * <pre>
 *     new Image(device, clazz.getResourceAsStream("file.gif"));
 * </pre>
 *
 * @param device the device on which to create the image
 * @param stream the input stream to load the image from
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_IO - if an IO error occurs while reading data</li>
 * </ul>
 */
public Image (Device device, InputStream stream) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, new ImageData(stream));
	if (device.tracking) device.new_Object(this);	
}

/**
 * Constructs an instance of this class by loading its representation
 * from the file with the specified name. Throws an error if an error
 * occurs while loading the image, or if the result is an image
 * of an unsupported type.
 * <p>
 * This constructor is provided for convenience when loading
 * a single image only. If the specified file contains
 * multiple images, only the first one will be used.
 *
 * @param device the device on which to create the image
 * @param filename the name of the file to load the image from
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image file contains invalid data </li>
 *    <li>ERROR_IO - if an IO error occurs while reading data</li>
 * </ul>
 */
public Image (Device device, String filename) {
	if (device == null) device = Device.getDevice();
	if (device == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	init(device, new ImageData(filename));
	if (device.tracking) device.new_Object(this);	
}

/**
 * Disposes of the operating system resources associated with
 * the image. Applications must dispose of all images which
 * they allocate.
 */
public void dispose () {
	if (handle == 0) return;
	if (type == SWT.ICON) {
		OS.DestroyIcon (handle);
	} else {
		OS.DeleteObject (handle);
	}
	handle = 0;
	memGC = null;
	if (device.tracking) device.dispose_Object(this);
	device = null;
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
public boolean equals (Object object) {
	if (object == this) return true;
	if (!(object instanceof Image)) return false;
	Image image = (Image) object;
	return device == image.device && handle == image.handle;
}

/**
 * Returns the color to which to map the transparent pixel, or null if
 * the receiver has no transparent pixel.
 * <p>
 * There are certain uses of Images that do not support transparency
 * (for example, setting an image into a button or label). In these cases,
 * it may be desired to simulate transparency by using the background
 * color of the widget to paint the transparent pixels of the image.
 * Use this method to check which color will be used in these cases
 * in place of transparency. This value may be set with setBackground().
 * <p>
 *
 * @return the background color of the image, or null if there is no transparency in the image
 */
public Color getBackground() {
	if (transparentPixel == -1) return null;

	/* Get the HDC for the device */
	int hDC = device.internal_new_GC(null);
	
	/* Compute the background color */
	BITMAP bm = new BITMAP();		
	OS.GetObject(handle, BITMAP.sizeof, bm);
	int hdcMem = OS.CreateCompatibleDC(hDC);
	int hOldObject = OS.SelectObject(hdcMem, handle);
	int red = 0, green = 0, blue = 0;
	if (bm.bmBitsPixel <= 8)  {
		byte[] color = new byte[4];
		int numColors = OS.GetDIBColorTable(hdcMem, transparentPixel, 1, color);
		blue = color[0] & 0xFF;
		green = color[1] & 0xFF;
		red = color[2] & 0xFF;
	} else {
		switch (bm.bmBitsPixel) {
			case 16:
				blue = (transparentPixel & 0x1F) << 3;
				green = (transparentPixel & 0x3E0) >> 2;
				red = (transparentPixel & 0x7C00) >> 7;
				break;
			case 24:
				blue = (transparentPixel & 0xFF0000) >> 16;
				green = (transparentPixel & 0xFF00) >> 8;
				red = transparentPixel & 0xFF;
				break;
			case 32:
				blue = (transparentPixel & 0xFF000000) >>> 24;
				green = (transparentPixel & 0xFF0000) >> 16;
				red = (transparentPixel & 0xFF00) >> 8;
				break;
			default:
				return null;
		}
	}
	OS.SelectObject(hdcMem, hOldObject);
	OS.DeleteDC(hdcMem);
	
	/* Release the HDC for the device */
	device.internal_dispose_GC(hDC, null);
	return Color.win32_new(device, 0x02000000 | (blue << 16) | (green << 8) | red);
}

/**
 * Returns the bounds of the receiver. The rectangle will always
 * have x and y values of 0, and the width and height of the
 * image.
 *
 * @return a rectangle specifying the image's bounds
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 */
public Rectangle getBounds() {
	switch (type) {
		case SWT.BITMAP:
			BITMAP bm = new BITMAP();
			OS.GetObject(handle, BITMAP.sizeof, bm);
			return new Rectangle(0, 0, bm.bmWidth, bm.bmHeight);
		case SWT.ICON:
			ICONINFO info = new ICONINFO();
			OS.GetIconInfo(handle, info);
			int hBitmap = info.hbmColor;
			if (hBitmap == 0) hBitmap = info.hbmMask;
			bm = new BITMAP();
			OS.GetObject(hBitmap, BITMAP.sizeof, bm);
			if (hBitmap == info.hbmMask) bm.bmHeight /= 2;
			if (info.hbmColor != 0) OS.DeleteObject(info.hbmColor);
			if (info.hbmMask != 0) OS.DeleteObject(info.hbmMask);
			return new Rectangle(0, 0, bm.bmWidth, bm.bmHeight);
		default:
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			return null;
	}
}

/**
 * Returns an <code>ImageData</code> based on the receiver
 * Modifications made to this <code>ImageData</code> will not
 * affect the Image.
 *
 * @return an <code>ImageData</code> containing the image's data and attributes
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_IMAGE - if the image is not a bitmap or an icon</li>
 * </ul>
 *
 * @see ImageData
 */
public ImageData getImageData() {
	BITMAP bm;
	int depth, width, height;
	switch (type) {
		case SWT.ICON: {
			ICONINFO info = new ICONINFO();
			OS.GetIconInfo(handle, info);
			/* Get the basic BITMAP information */
			int hBitmap = info.hbmColor;
			if (hBitmap == 0) hBitmap = info.hbmMask;
			bm = new BITMAP();
			OS.GetObject(hBitmap, BITMAP.sizeof, bm);
			depth = bm.bmPlanes * bm.bmBitsPixel;
			width = bm.bmWidth;
			if (hBitmap == info.hbmMask) bm.bmHeight /= 2;
			height = bm.bmHeight;
			int numColors = 0;
			if (depth <= 8) numColors = 1 << depth;
			/* Create the BITMAPINFO */
			byte[] bmi = new byte[40 + numColors * 4];
			/* DWORD biSize = 40 */
			bmi[0] = 40; bmi[1] = bmi[2] = bmi[3] = 0;
			/* LONG biWidth = width */
			bmi[4] = (byte)(width & 0xFF);
			bmi[5] = (byte)((width >> 8) & 0xFF);
			bmi[6] = (byte)((width >> 16) & 0xFF);
			bmi[7] = (byte)((width >> 24) & 0xFF);
			/* LONG biHeight = height */
			bmi[8] = (byte)(-height & 0xFF);
			bmi[9] = (byte)((-height >> 8) & 0xFF);
			bmi[10] = (byte)((-height >> 16) & 0xFF);
			bmi[11] = (byte)((-height >> 24) & 0xFF);
			/* WORD biPlanes = 1 */
			bmi[12] = 1;
			bmi[13] = 0;
			/* WORD biBitCount = bm.bmPlanes * bm.bmBitsPixel */
			bmi[14] = (byte)(depth & 0xFF);
			bmi[15] = (byte)((depth >> 8) & 0xFF);
			/* DWORD biCompression = BI_RGB = 0 */
			bmi[16] = bmi[17] = bmi[18] = bmi[19] = 0;
			/* DWORD biSizeImage = 0 (default) */
			bmi[20] = bmi[21] = bmi[22] = bmi[23] = 0;
			/* LONG biXPelsPerMeter = 0 */
			bmi[24] = bmi[25] = bmi[26] = bmi[27] = 0;
			/* LONG biYPelsPerMeter = 0 */
			bmi[28] = bmi[29] = bmi[30] = bmi[31] = 0;
			/* DWORD biClrUsed = 0 */
			bmi[32] = bmi[33] = bmi[34] = bmi[35] = 0;
			/* DWORD biClrImportant = 0 */
			bmi[36] = bmi[37] = bmi[38] = bmi[39] = 0;

			/* Get the HDC for the device */
			int hDC = device.internal_new_GC(null);
	
			/* Create the DC and select the bitmap */
			int hBitmapDC = OS.CreateCompatibleDC(hDC);
			int hOldBitmap = OS.SelectObject(hBitmapDC, hBitmap);
			/* Select the palette if necessary */
			int oldPalette = 0;
			if (depth <= 8) {
				int hPalette = device.hPalette;
				if (hPalette != 0) {
					oldPalette = OS.SelectPalette(hBitmapDC, hPalette, false);
					OS.RealizePalette(hBitmapDC);
				}
			}
			/* Find the size of the image and allocate data */
			int imageSize;
			/* Call with null lpBits to get the image size */
			OS.GetDIBits(hBitmapDC, hBitmap, 0, height, 0, bmi, OS.DIB_RGB_COLORS);
			imageSize = (bmi[20] & 0xFF) | ((bmi[21] & 0xFF) << 8) | ((bmi[22] & 0xFF) << 16) | ((bmi[23] & 0xFF) << 24);
			byte[] data = new byte[imageSize];
			/* Get the bitmap data */
			int hHeap = OS.GetProcessHeap();
			int lpvBits = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, imageSize);
			OS.GetDIBits(hBitmapDC, hBitmap, 0, height, lpvBits, bmi, OS.DIB_RGB_COLORS);
			OS.MoveMemory(data, lpvBits, imageSize);
			/* Calculate the palette */
			PaletteData palette = null;
			if (depth <= 8) {
				RGB[] rgbs = new RGB[numColors];
				int srcIndex = 40;
				for (int i = 0; i < numColors; i++) {
					rgbs[i] = new RGB(bmi[srcIndex + 2] & 0xFF, bmi[srcIndex + 1] & 0xFF, bmi[srcIndex] & 0xFF);
					srcIndex += 4;
				}
				palette = new PaletteData(rgbs);
			} else if (depth == 16) {
				palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
			} else if (depth == 24) {
				palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			} else if (depth == 32) {
				palette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
			} else {
				SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
			}

			/* Do the mask */
			byte [] maskData = null;
			if (info.hbmColor == 0) {
				/* Do the bottom half of the mask */
				maskData = new byte[imageSize];
				OS.GetDIBits(hBitmapDC, hBitmap, height, height, lpvBits, bmi, OS.DIB_RGB_COLORS);
				OS.MoveMemory(maskData, lpvBits, imageSize);
			} else {
				/* Do the entire mask */
				/* Create the BITMAPINFO */
				bmi = new byte[48];
				/* DWORD biSize = 40 */
				bmi[0] = 40; bmi[1] = bmi[2] = bmi[3] = 0;
				/* LONG biWidth = width */
				bmi[4] = (byte)(width & 0xFF);
				bmi[5] = (byte)((width >> 8) & 0xFF);
				bmi[6] = (byte)((width >> 16) & 0xFF);
				bmi[7] = (byte)((width >> 24) & 0xFF);
				/* LONG biHeight = height */
				bmi[8] = (byte)(-height & 0xFF);
				bmi[9] = (byte)((-height >> 8) & 0xFF);
				bmi[10] = (byte)((-height >> 16) & 0xFF);
				bmi[11] = (byte)((-height >> 24) & 0xFF);
				/* WORD biPlanes = 1 */
				bmi[12] = 1;
				bmi[13] = 0;
				/* WORD biBitCount = 1 */
				bmi[14] = 1;
				bmi[15] = 0;
				/* DWORD biCompression = BI_RGB = 0 */
				bmi[16] = bmi[17] = bmi[18] = bmi[19] = 0;
				/* DWORD biSizeImage = 0 (default) */
				bmi[20] = bmi[21] = bmi[22] = bmi[23] = 0;
				/* LONG biXPelsPerMeter = 0 */
				bmi[24] = bmi[25] = bmi[26] = bmi[27] = 0;
				/* LONG biYPelsPerMeter = 0 */
				bmi[28] = bmi[29] = bmi[30] = bmi[31] = 0;
				/* DWORD biClrUsed = 0 */
				bmi[32] = bmi[33] = bmi[34] = bmi[35] = 0;
				/* DWORD biClrImportant = 0 */
				bmi[36] = bmi[37] = bmi[38] = bmi[39] = 0;
				/* First color black */
				bmi[40] = bmi[41] = bmi[42] = bmi[43] = 0;
				/* Second color white */
				bmi[44] = bmi[45] = bmi[46] = (byte)0xFF;
				bmi[47] = 0;
				OS.SelectObject(hBitmapDC, info.hbmMask);
				/* Call with null lpBits to get the image size */
				OS.GetDIBits(hBitmapDC, info.hbmMask, 0, height, 0, bmi, OS.DIB_RGB_COLORS);
				imageSize = (bmi[20] & 0xFF) | ((bmi[21] & 0xFF) << 8) | ((bmi[22] & 0xFF) << 16) | ((bmi[23] & 0xFF) << 24);
				maskData = new byte[imageSize];
				int lpvMaskBits = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, imageSize);
				OS.GetDIBits(hBitmapDC, info.hbmMask, 0, height, lpvMaskBits, bmi, OS.DIB_RGB_COLORS);
				OS.MoveMemory(maskData, lpvMaskBits, imageSize);	
				OS.HeapFree(hHeap, 0, lpvMaskBits);
				/* Loop to invert the mask */
				for (int i = 0; i < maskData.length; i++) {
					maskData[i] ^= -1;
				}
				/* Make sure mask scanlinePad is 2 */
				int desiredScanline = (width + 7) / 8;
				desiredScanline = desiredScanline + (desiredScanline % 2);
				int realScanline = imageSize / height;
				if (realScanline != desiredScanline) {
					byte[] newData = new byte[desiredScanline * height];
					int srcIndex = 0;
					int destIndex = 0;
					for (int i = 0; i < height; i++) {
						System.arraycopy(maskData, srcIndex, newData, destIndex, desiredScanline);
						destIndex += desiredScanline;
						srcIndex += realScanline;
					}
					maskData = newData;
				}
			}
			/* Clean up */
			OS.HeapFree(hHeap, 0, lpvBits);
			OS.SelectObject(hBitmapDC, hOldBitmap);
			if (oldPalette != 0) {
				OS.SelectPalette(hBitmapDC, oldPalette, false);
				OS.RealizePalette(hBitmapDC);
			}
			OS.DeleteDC(hBitmapDC);
			
			/* Release the HDC for the device */
			device.internal_dispose_GC(hDC, null);
			
			if (info.hbmColor != 0) OS.DeleteObject(info.hbmColor);
			if (info.hbmMask != 0) OS.DeleteObject(info.hbmMask);
			/* Construct and return the ImageData */
			ImageData imageData = new ImageData(width, height, depth, palette, 4, data);
			imageData.maskData = maskData;
//			imageData.maskPad = 4;
			imageData.maskPad = 2;
			return imageData;
		}
		case SWT.BITMAP: {
			/* Get the basic BITMAP information */
			bm = new BITMAP();
			OS.GetObject(handle, BITMAP.sizeof, bm);
			depth = bm.bmPlanes * bm.bmBitsPixel;
			width = bm.bmWidth;
			height = bm.bmHeight;
			/* Find out whether this is a DIB or a DDB. */
			boolean isDib = (bm.bmBits != 0);
			DIBSECTION dib = null;
			if (isDib) {
				dib = new DIBSECTION();
				OS.GetObject(handle, DIBSECTION.sizeof, dib);
			}
			/* Calculate number of colors */
			int numColors = 0;
			if (depth <= 8) {
				if (isDib) {
					numColors = dib.biClrUsed;
				} else {
					numColors = 1 << depth;
				}
			}
			/* Create the BITMAPINFO */
			byte[] bmi = null;
			if (!isDib) {
				bmi = new byte[40 + numColors * 4];
				/* DWORD biSize = 40 */
				bmi[0] = 40; bmi[1] = bmi[2] = bmi[3] = 0;
				/* LONG biWidth = width */
				bmi[4] = (byte)(width & 0xFF);
				bmi[5] = (byte)((width >> 8) & 0xFF);
				bmi[6] = (byte)((width >> 16) & 0xFF);
				bmi[7] = (byte)((width >> 24) & 0xFF);
				/* LONG biHeight = height */
				bmi[8] = (byte)(-height & 0xFF);
				bmi[9] = (byte)((-height >> 8) & 0xFF);
				bmi[10] = (byte)((-height >> 16) & 0xFF);
				bmi[11] = (byte)((-height >> 24) & 0xFF);
				/* WORD biPlanes = 1 */
				bmi[12] = 1;
				bmi[13] = 0;
				/* WORD biBitCount = bm.bmPlanes * bm.bmBitsPixel */
				bmi[14] = (byte)(depth & 0xFF);
				bmi[15] = (byte)((depth >> 8) & 0xFF);
				/* DWORD biCompression = BI_RGB = 0 */
				bmi[16] = bmi[17] = bmi[18] = bmi[19] = 0;
				/* DWORD biSizeImage = 0 (default) */
				bmi[20] = bmi[21] = bmi[22] = bmi[23] = 0;
				/* LONG biXPelsPerMeter = 0 */
				bmi[24] = bmi[25] = bmi[26] = bmi[27] = 0;
				/* LONG biYPelsPerMeter = 0 */
				bmi[28] = bmi[29] = bmi[30] = bmi[31] = 0;
				/* DWORD biClrUsed = 0 */
				bmi[32] = bmi[33] = bmi[34] = bmi[35] = 0;
				/* DWORD biClrImportant = 0 */
				bmi[36] = bmi[37] = bmi[38] = bmi[39] = 0;
			}
			/* Get the HDC for the device */
			int hDC = device.internal_new_GC(null);
			
			/* Create the DC and select the bitmap */
			int hBitmapDC = OS.CreateCompatibleDC(hDC);
			int hOldBitmap = OS.SelectObject(hBitmapDC, handle);
			/* Select the palette if necessary */
			int oldPalette = 0;
			if (!isDib && depth <= 8) {
				int hPalette = device.hPalette;
				if (hPalette != 0) {
					oldPalette = OS.SelectPalette(hBitmapDC, hPalette, false);
					OS.RealizePalette(hBitmapDC);
				}
			}
			/* Find the size of the image and allocate data */
			int imageSize;
			if (isDib) {
				imageSize = dib.biSizeImage;
			} else {
				/* Call with null lpBits to get the image size */
				OS.GetDIBits(hBitmapDC, handle, 0, height, 0, bmi, OS.DIB_RGB_COLORS);
				imageSize = (bmi[20] & 0xFF) | ((bmi[21] & 0xFF) << 8) | ((bmi[22] & 0xFF) << 16) | ((bmi[23] & 0xFF) << 24);
			}
			byte[] data = new byte[imageSize];
			/* Get the bitmap data */
			if (isDib) {
				OS.MoveMemory(data, bm.bmBits, imageSize);
			} else {
				int hHeap = OS.GetProcessHeap();
				int lpvBits = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, imageSize);
				OS.GetDIBits(hBitmapDC, handle, 0, height, lpvBits, bmi, OS.DIB_RGB_COLORS);
				OS.MoveMemory(data, lpvBits, imageSize);
				OS.HeapFree(hHeap, 0, lpvBits);
			}
			/* Calculate the palette */
			PaletteData palette = null;
			if (isDib) {
				if (depth <= 8) {
					byte[] colors = new byte[numColors * 4];
					OS.GetDIBColorTable(hBitmapDC, 0, numColors, colors);
					RGB[] rgbs = new RGB[numColors];
					int colorIndex = 0;
					for (int i = 0; i < rgbs.length; i++) {
						rgbs[i] = new RGB(colors[colorIndex + 2] & 0xFF, colors[colorIndex + 1] & 0xFF, colors[colorIndex] & 0xFF);
						colorIndex += 4;
					}
					palette = new PaletteData(rgbs);
				} else if (depth == 16) {
					palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
				} else if (depth == 24 || depth == 32) {
					palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
				} else {
					SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
				}
			} else {
				if (depth <= 8) {
					RGB[] rgbs = new RGB[numColors];
					int srcIndex = 40;
					for (int i = 0; i < numColors; i++) {
						rgbs[i] = new RGB(bmi[srcIndex + 2] & 0xFF, bmi[srcIndex + 1] & 0xFF, bmi[srcIndex] & 0xFF);
						srcIndex += 4;
					}
					palette = new PaletteData(rgbs);
				} else if (depth == 16) {
					palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
				} else if (depth == 24) {
					palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
				} else if (depth == 32) {
					palette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
				} else {
					SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
				}
			}
			/* Clean up */
			OS.SelectObject(hBitmapDC, hOldBitmap);
			if (oldPalette != 0) {
				OS.SelectPalette(hBitmapDC, oldPalette, false);
				OS.RealizePalette(hBitmapDC);
			}
			OS.DeleteDC(hBitmapDC);
			
			/* Release the HDC for the device */
			device.internal_dispose_GC(hDC, null);
			
			/* Construct and return the ImageData */
			ImageData imageData = new ImageData(width, height, depth, palette, 4, data);
			imageData.transparentPixel = this.transparentPixel;
			imageData.alpha = alpha;
			if (alpha == -1 && alphaData != null) {
				imageData.alphaData = new byte[alphaData.length];
				System.arraycopy(alphaData, 0, imageData.alphaData, 0, alphaData.length);
			}
			return imageData;
		}
		default:
			SWT.error(SWT.ERROR_UNSUPPORTED_FORMAT);
			return null;
	}
}

/**
 * Returns an integer hash code for the receiver. Any two 
 * objects which return <code>true</code> when passed to 
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @see #equals
 */
public int hashCode () {
	return handle;
}

void init(Device device, int width, int height) {
	if (width <= 0 || height <= 0) {
		SWT.error (SWT.ERROR_INVALID_ARGUMENT);
	}
	this.device = device;
	type = SWT.BITMAP;
	
	/* Get the HDC for the device */
	int hDC = device.internal_new_GC(null);
			
	/* Fill the bitmap with the current background color */
	handle = OS.CreateCompatibleBitmap(hDC, width, height);
	int memDC = OS.CreateCompatibleDC(hDC);
	int hOldBitmap = OS.SelectObject(memDC, handle);
	OS.PatBlt(memDC, 0, 0, width, height, OS.PATCOPY);
	OS.SelectObject(memDC, hOldBitmap);
	OS.DeleteDC(memDC);
	
	/* Release the HDC for the device */	
	device.internal_dispose_GC(hDC, null);
}

void init(Device device, ImageData i) {
	if (i == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	this.device = device;
	/*
	 * BUG in Windows 98:
	 * A monochrome DIBSection will display as solid black
	 * on Windows 98 machines, even though it contains the
	 * correct data. The fix is to convert 1-bit ImageData
	 * into 4-bit ImageData before creating the image.
	 */
	/* Windows does not support 2-bit images. Convert to 4-bit image. */
	if ((i.depth == 1 && i.getTransparencyType() != SWT.TRANSPARENCY_MASK) || i.depth == 2) {
		ImageData img = new ImageData(i.width, i.height, 4, i.palette);
		ImageData.blit(ImageData.BLIT_SRC, 
			i.data, i.depth, i.bytesPerLine, ImageData.MSB_FIRST, 0, 0, i.width, i.height, null, null, null, -1, null, 0,
			img.data, img.depth, img.bytesPerLine, ImageData.MSB_FIRST, 0, 0, img.width, img.height, null, null, null, 
			false, false);
		img.transparentPixel = i.transparentPixel;
		img.maskPad = i.maskPad;
		img.maskData = i.maskData;
		img.alpha = i.alpha;
		img.alphaData = i.alphaData;
		i = img;
	}
	/*
	 * Windows supports 16-bit mask of (0x7C00, 0x3E0, 0x1F),
	 * 24-bit mask of (0xFF0000, 0xFF00, 0xFF) and 32-bit mask
	 * (0xFF000000, 0xFF0000, 0xFF00).  Make sure the image is
	 * Windows-supported.
	 */
	if (i.palette.isDirect) {
		PaletteData newPalette = null;
		PaletteData palette = i.palette;
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int order = ImageData.MSB_FIRST;
		switch (i.depth) {
			case 16:
				order = ImageData.LSB_FIRST;
				if (!(redMask == 0x7C00 && greenMask == 0x3E0 && blueMask == 0x1F)) {
					newPalette = new PaletteData(0x7C00, 0x3E0, 0x1F);
				}
				break;
			case 24: 
				if (!(redMask == 0xFF && greenMask == 0xFF00 && blueMask == 0xFF0000)) {
					newPalette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
				}
				break;
			case 32: 
				if (!(redMask == 0xFF && greenMask == 0xFF00 && blueMask == 0xFF0000)) {
					newPalette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
				}
				break;
			default:
				SWT.error(SWT.ERROR_UNSUPPORTED_DEPTH);
		}
		if (newPalette != null) {
			ImageData img = new ImageData(i.width, i.height, i.depth, newPalette);
			ImageData.blit(ImageData.BLIT_SRC, 
					i.data, i.depth, i.bytesPerLine, order, 0, 0, i.width, i.height, redMask, greenMask, blueMask, -1, null, 0,
					img.data, img.depth, img.bytesPerLine, order, 0, 0, img.width, img.height, newPalette.redMask, newPalette.greenMask, newPalette.blueMask,
					false, false);
			if (i.transparentPixel != -1) {
				img.transparentPixel = newPalette.getPixel(palette.getRGB(i.transparentPixel));
			}
			img.maskPad = i.maskPad;
			img.maskData = i.maskData;
			img.alpha = i.alpha;
			img.alphaData = i.alphaData;
			i = img;
		}
	}
	/* Construct bitmap info header by hand */
	RGB[] rgbs = i.palette.getRGBs();
	byte[] bmi;
	if (i.palette.isDirect)
		bmi = new byte[40];
	else
		bmi = new byte[40 + rgbs.length * 4];
	/* DWORD biSize = 40 */
	bmi[0] = 40; bmi[1] = 0; bmi[2] = 0; bmi[3] = 0;
	/* LONG biWidth = width */
	bmi[4] = (byte)(i.width & 0xFF);
	bmi[5] = (byte)((i.width >> 8) & 0xFF);
	bmi[6] = (byte)((i.width >> 16) & 0xFF);
	bmi[7] = (byte)((i.width >> 24) & 0xFF);
	/* LONG biHeight = height */
	int height = -i.height;
	bmi[8] = (byte)(height & 0xFF);
	bmi[9] = (byte)((height >> 8) & 0xFF);
	bmi[10] = (byte)((height >> 16) & 0xFF);
	bmi[11] = (byte)((height >> 24) & 0xFF);
	/* WORD biPlanes = 1 */
	bmi[12] = 1;
	bmi[13] = 0;
	/* WORD biBitCount = depth */
	bmi[14] = (byte)(i.depth & 0xFF);
	bmi[15] = (byte)((i.depth >> 8) & 0xFF);
	/* DWORD biCompression = BI_RGB = 0 */
	bmi[16] = bmi[17] = bmi[18] = bmi[19] = 0;
	/* DWORD biSizeImage = 0 (default) */
	bmi[20] = bmi[21] = bmi[22] = bmi[23] = 0;
	/* LONG biXPelsPerMeter = 0 */
	bmi[24] = bmi[25] = bmi[26] = bmi[27] = 0;
	/* LONG biYPelsPerMeter = 0 */
	bmi[28] = bmi[29] = bmi[30] = bmi[31] = 0;
	/* DWORD biClrUsed */
	if (rgbs == null) {
		bmi[32] = bmi[33] = bmi[34] = bmi[35] = 0;
	} else {
		bmi[32] = (byte)(rgbs.length & 0xFF);
		bmi[33] = (byte)((rgbs.length >> 8) & 0xFF);
		bmi[34] = (byte)((rgbs.length >> 16) & 0xFF);
		bmi[35] = (byte)((rgbs.length >> 24) & 0xFF);
	}
	/* DWORD biClrImportant = 0 */
	bmi[36] = bmi[37] = bmi[38] = bmi[39] = 0;
	/* Set the rgb colors into the bitmap info */
	int offset = 40;
	if (!i.palette.isDirect) {
		for (int j = 0; j < rgbs.length; j++) {
			bmi[offset] = (byte)rgbs[j].blue;
			bmi[offset + 1] = (byte)rgbs[j].green;
			bmi[offset + 2] = (byte)rgbs[j].red;
			bmi[offset + 3] = 0;
			offset += 4;
		}
	}
	int[] pBits = new int[1];
	int hDib = OS.CreateDIBSection(0, bmi, OS.DIB_RGB_COLORS, pBits, 0, 0);
	if (hDib == 0) SWT.error(SWT.ERROR_NO_HANDLES);
	/* In case of a scanline pad other than 4, do the work to convert it */
	byte[] data = i.data;
	if (i.scanlinePad != 4 && (i.bytesPerLine % 4 != 0)) {
		int newBpl = i.bytesPerLine + (4 - (i.bytesPerLine % 4));
		byte[] newData = new byte[i.height * newBpl];
		int srcPtr = 0;
		int destPtr = 0;
		for (int y = 0; y < i.height; y++) {
			System.arraycopy(data, srcPtr, newData, destPtr, i.bytesPerLine);
			srcPtr += i.bytesPerLine;
			destPtr += newBpl;
		}
		data = newData;
	}
	OS.MoveMemory(pBits[0], data, data.length);
	if (i.getTransparencyType() == SWT.TRANSPARENCY_MASK) {
		/* Get the HDC for the device */
		int hDC = device.internal_new_GC(null);
			
		/* Create the color bitmap */
		int hdcSrc = OS.CreateCompatibleDC(hDC);
		OS.SelectObject(hdcSrc, hDib);
		int hBitmap = OS.CreateCompatibleBitmap(hDC, i.width, i.height);
		if (hBitmap == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		int hdcDest = OS.CreateCompatibleDC(hDC);
		OS.SelectObject(hdcDest, hBitmap);
		OS.BitBlt(hdcDest, 0, 0, i.width, i.height, hdcSrc, 0, 0, OS.SRCCOPY);
		
		/* Release the HDC for the device */	
		device.internal_dispose_GC(hDC, null);
			
		/* Create the mask */
//		int hHeap = OS.GetProcessHeap();
//		int bmBits = OS.HeapAlloc(hHeap, OS.HEAP_ZERO_MEMORY, i.maskData.length);
//		OS.MoveMemory(bmBits, i.maskData, i.maskData.length);
//		BITMAP bm = new BITMAP();
//		bm.bmWidth = i.width;
//		bm.bmHeight = i.height;
//		bm.bmWidthBytes = (((i.width + 7) / 8) + 3) / 4 * 4;
//		bm.bmPlanes = 1;
//		bm.bmBitsPixel = 1;
//		bm.bmBits = bmBits;
//		int hMask = OS.CreateBitmapIndirect(bm);
//		OS.HeapFree(hHeap, 0, bmBits);
		int hMask = OS.CreateBitmap(i.width, i.height, 1, 1, i.maskData);
		if (hMask == 0) SWT.error(SWT.ERROR_NO_HANDLES);	
		OS.SelectObject(hdcSrc, hMask);
		OS.PatBlt(hdcSrc, 0, 0, i.width, i.height, OS.DSTINVERT);
		OS.DeleteDC(hdcSrc);
		OS.DeleteDC(hdcDest);
		
		/* Create the icon */
		ICONINFO info = new ICONINFO();
		info.fIcon = true;
		info.hbmColor = hBitmap;
		info.hbmMask = hMask;
		int hIcon = OS.CreateIconIndirect(info);
		if (hIcon == 0) SWT.error(SWT.ERROR_NO_HANDLES);
		OS.DeleteObject(hBitmap);
		OS.DeleteObject(hMask);
		OS.DeleteObject(hDib);
		this.handle = hIcon;
		this.type = SWT.ICON;
	} else {
		this.handle = hDib;
		this.type = SWT.BITMAP;
		this.transparentPixel = i.transparentPixel;
		if (this.transparentPixel == -1) {
			this.alpha = i.alpha;
			if (i.alpha == -1 && i.alphaData != null) {
				this.alphaData = new byte[i.alphaData.length];
				System.arraycopy(i.alphaData, 0, this.alphaData, 0, alphaData.length);
			}
		}
	}
}

/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param data the platform specific GC data 
 * @return the platform specific GC handle
 *
 * @private
 */
public int internal_new_GC (GCData data) {
	/*
	* Create a new GC that can draw into the image.
	* Only supported for bitmaps.
	*/
	if (type != SWT.BITMAP || memGC != null) {
		SWT.error(SWT.ERROR_INVALID_ARGUMENT);
	}
	
	/* Create a compatible HDC for the device */
	int hDC = device.internal_new_GC(null);
	int imageDC = OS.CreateCompatibleDC(hDC);
	device.internal_dispose_GC(hDC, null);
	if (imageDC == 0) SWT.error(SWT.ERROR_NO_HANDLES);

	if (data != null) {
		/* Set the GCData fields */
		data.device = device;
		data.image = this;
		data.hFont = device.getSystemFont().handle;
	}
	return imageDC;
}

/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param handle the platform specific GC handle
 * @param data the platform specific GC data 
 *
 * @private
 */
public void internal_dispose_GC (int hDC, GCData data) {
	OS.DeleteDC(hDC);
}

/**
 * Returns <code>true</code> if the image has been disposed,
 * and <code>false</code> otherwise.
 * <p>
 * This method gets the dispose state for the image.
 * When an image has been disposed, it is an error to
 * invoke any other method using the image.
 *
 * @return <code>true</code> when the image is disposed and <code>false</code> otherwise
 */
public boolean isDisposed() {
	return handle == 0;
}

/**
 * Sets the color to which to map the transparent pixel.
 * <p>
 * There are certain uses of <code>Images</code> that do not support
 * transparency (for example, setting an image into a button or label).
 * In these cases, it may be desired to simulate transparency by using
 * the background color of the widget to paint the transparent pixels
 * of the image. This method specifies the color that will be used in
 * these cases. For example:
 * <pre>
 *    Button b = new Button();
 *    image.setBackground(b.getBackground());>
 *    b.setImage(image);
 * </pre>
 * This method has no effect if the receiver does not have a transparent
 * pixel value.
 *
 * @param color the color to use when a transparent pixel is specified
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the color is null</li>
 * </ul>
 */
public void setBackground(Color color) {
	if (color == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	if (transparentPixel == -1) return;

	/* Get the HDC for the device */
	int hDC = device.internal_new_GC(null);
	
	/* Change the background color in the image */
	BITMAP bm = new BITMAP();		
	OS.GetObject(handle, BITMAP.sizeof, bm);
	int hdcMem = OS.CreateCompatibleDC(hDC);
	OS.SelectObject(hdcMem, handle);
	int maxColors = 1 << bm.bmBitsPixel;
	byte[] colors = new byte[maxColors * 4];
	int numColors = OS.GetDIBColorTable(hdcMem, 0, maxColors, colors);
	int offset = transparentPixel * 4;
	colors[offset] = (byte)color.getBlue();
	colors[offset + 1] = (byte)color.getGreen();
	colors[offset + 2] = (byte)color.getRed();
	OS.SetDIBColorTable(hdcMem, 0, numColors, colors);
	OS.DeleteDC(hdcMem);
	
	/* Release the HDC for the device */	
	device.internal_dispose_GC(hDC, null);
}
/**	 
 * Invokes platform specific functionality to allocate a new image.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Image</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param device the device on which to allocate the color
 * @param type the type of the image (<code>SWT.BITMAP</code> or <code>SWT.ICON</code>)
 * @param handle the OS handle for the image
 * @param hPalette the OS handle for the palette, or 0
 *
 * @private
 */
public static Image win32_new(Device device, int type, int handle) {
	if (device == null) device = Device.getDevice();
	Image image = new Image();
	image.type = type;
	image.handle = handle;
	image.device = device;
	return image;
}

}
