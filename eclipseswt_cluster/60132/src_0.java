/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.program;


import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

import java.util.Vector;

/**
 * Instances of this class represent programs and
 * their associated file extensions in the operating
 * system.
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#program">Program snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public final class Program {
	String name, fullPath, identifier;

/**
 * Prevents uninitialized instances from being created outside the package.
 */
Program () {
}

/**
 * Finds the program that is associated with an extension.
 * The extension may or may not begin with a '.'.  Note that
 * a <code>Display</code> must already exist to guarantee that
 * this method returns an appropriate result.
 *
 * @param extension the program extension
 * @return the program or <code>null</code>
 *
 * @exception IllegalArgumentException <ul>
 *		<li>ERROR_NULL_ARGUMENT when extension is null</li>
 *	</ul>
 */
public static Program findProgram (String extension) {
	if (extension == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	if (extension.length () == 0) return null;
	if (extension.charAt(0) != '.') extension = "." + extension;
	NSWorkspace workspace = NSWorkspace.sharedWorkspace();
	int /*long*/ appName = OS.malloc(C.PTR_SIZEOF);
	int /*long*/ type = OS.malloc(C.PTR_SIZEOF);
	NSString temp = new NSString(OS.NSTemporaryDirectory());
	NSString fileName = NSString.stringWith("swt" + System.currentTimeMillis() + extension);
	NSString fullPath = temp.stringByAppendingPathComponent(fileName);
	NSFileManager fileManager = NSFileManager.defaultManager();
	fileManager.createFileAtPath(fullPath, null, null);
	workspace.getInfoForFile(fullPath, appName, type);
	fileManager.removeItemAtPath(fullPath, 0);
	int /*long*/ [] buffer = new int /*long*/[1];
	OS.memmove(buffer, appName, C.PTR_SIZEOF);
	OS.free(appName);
	OS.free(type);
	if (buffer [0] != 0) {
		NSString appPath = new NSString(buffer[0]);
		NSBundle bundle = NSBundle.bundleWithPath(appPath);
		if (bundle != null) return getProgram(bundle);
	}
	return null;
}

/**
 * Answer all program extensions in the operating system.  Note
 * that a <code>Display</code> must already exist to guarantee
 * that this method returns an appropriate result.
 *
 * @return an array of extensions
 */
public static String [] getExtensions () {
	NSMutableSet supportedDocumentTypes = (NSMutableSet)NSMutableSet.set();
	NSWorkspace workspace = NSWorkspace.sharedWorkspace();
	NSString CFBundleDocumentTypes = NSString.stringWith("CFBundleDocumentTypes");
	NSString CFBundleTypeExtensions = NSString.stringWith("CFBundleTypeExtensions");
	NSArray array = new NSArray(OS.NSSearchPathForDirectoriesInDomains(OS.NSAllApplicationsDirectory, OS.NSAllDomainsMask, true));
	int count = (int)/*64*/array.count();
	for (int i = 0; i < count; i++) {
		NSString path = new NSString(array.objectAtIndex(i));
		NSFileManager fileManager = NSFileManager.defaultManager();
		NSDirectoryEnumerator enumerator = fileManager.enumeratorAtPath(path);
		if (enumerator != null) {
			id id;
			while ((id = enumerator.nextObject()) != null) {
				enumerator.skipDescendents();
				NSString filePath = new NSString(id.id);
				NSString fullPath = path.stringByAppendingPathComponent(filePath);
				if (workspace.isFilePackageAtPath(fullPath)) {
					NSBundle bundle = NSBundle.bundleWithPath(fullPath);
					id = bundle.infoDictionary().objectForKey(CFBundleDocumentTypes);
					if (id != null) {
						NSDictionary documentTypes = new NSDictionary(id.id);
						NSEnumerator documentTypesEnumerator = documentTypes.objectEnumerator();
						while ((id = documentTypesEnumerator.nextObject()) != null) {
							NSDictionary documentType = new NSDictionary(id.id);
							id = documentType.objectForKey(CFBundleTypeExtensions);
							if (id != null) {
								supportedDocumentTypes.addObjectsFromArray(new NSArray(id.id));
							}
						}
					}
				}
			}
		}
	}
	int i = 0;
	String[] exts = new String[(int)/*64*/supportedDocumentTypes.count()];
	NSEnumerator enumerator = supportedDocumentTypes.objectEnumerator();
	id id;
	while ((id = enumerator.nextObject()) != null) {
		String ext = new NSString(id.id).getString();
		if (!ext.equals("*")) exts[i++] = "." + ext;
	}
	if (i != exts.length) {
		String[] temp = new String[i];
		System.arraycopy(exts, 0, temp, 0, i);
		exts = temp;
	}
	return exts;
}

static Program getProgram(NSBundle bundle) {
	NSString CFBundleName = NSString.stringWith("CFBundleName");
	NSString CFBundleDisplayName = NSString.stringWith("CFBundleDisplayName");
	NSString fullPath = bundle.bundlePath();
	NSString identifier = bundle.bundleIdentifier();
	id bundleName = bundle.objectForInfoDictionaryKey(CFBundleDisplayName);
    if (bundleName == null) {
        bundleName = bundle.objectForInfoDictionaryKey(CFBundleName);
    }
    if (bundleName == null) {
        bundleName = fullPath.lastPathComponent().stringByDeletingPathExtension();
    }
    NSString name = new NSString(bundleName.id);
    Program program = new Program();
    program.name = name.getString();
    program.fullPath = fullPath.getString();
    program.identifier = identifier != null ? identifier.getString() : "";
    return program;
}

/**
 * Answers all available programs in the operating system.  Note
 * that a <code>Display</code> must already exist to guarantee
 * that this method returns an appropriate result.
 *
 * @return an array of programs
 */
public static Program [] getPrograms () {
	Vector vector = new Vector();
	NSWorkspace workspace = NSWorkspace.sharedWorkspace();
	NSArray array = new NSArray(OS.NSSearchPathForDirectoriesInDomains(OS.NSAllApplicationsDirectory, OS.NSAllDomainsMask, true));
	int count = (int)/*64*/array.count();
	for (int i = 0; i < count; i++) {
		NSString path = new NSString(array.objectAtIndex(i));
		NSFileManager fileManager = NSFileManager.defaultManager();
		NSDirectoryEnumerator enumerator = fileManager.enumeratorAtPath(path);
		if (enumerator != null) {
			id id;
			while ((id = enumerator.nextObject()) != null) {
				enumerator.skipDescendents();
				NSString fullPath = path.stringByAppendingPathComponent(new NSString(id.id));
				if (workspace.isFilePackageAtPath(fullPath)) {
					NSBundle bundle = NSBundle.bundleWithPath(fullPath);
				    vector.addElement(getProgram(bundle));
				}
			}
		}
	}
	Program[] programs = new Program[vector.size()];
	vector.copyInto(programs);
	return programs;
}

/**
 * Launches the operating system executable associated with the file or
 * URL (http:// or https://).  If the file is an executable then the
 * executable is launched.  Note that a <code>Display</code> must already
 * exist to guarantee that this method returns an appropriate result.
 *
 * @param fileName the file or program name or URL (http:// or https://)
 * @return <code>true</code> if the file is launched, otherwise <code>false</code>
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT when fileName is null</li>
 * </ul>
 */
public static boolean launch (String fileName) {
	if (fileName == null) SWT.error (SWT.ERROR_NULL_ARGUMENT);
	if (fileName.indexOf(':') == -1) fileName = "file://" + fileName;
	NSWorkspace workspace = NSWorkspace.sharedWorkspace();
	//TODO use CFURLCreateStringByAddingPercentEscapes instead
	NSString str = NSString.stringWith(fileName).stringByAddingPercentEscapesUsingEncoding(OS.NSUTF8StringEncoding);
	return workspace.openURL(NSURL.URLWithString(str));
}

/**
 * Executes the program with the file as the single argument
 * in the operating system.  It is the responsibility of the
 * programmer to ensure that the file contains valid data for 
 * this program.
 *
 * @param fileName the file or program name
 * @return <code>true</code> if the file is launched, otherwise <code>false</code>
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT when fileName is null</li>
 * </ul>
 */
public boolean execute (String fileName) {
	if (fileName == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
	NSWorkspace workspace = NSWorkspace.sharedWorkspace();
	NSString fullPath = NSString.stringWith(fileName);
	if (fileName.indexOf(':') == -1) {
		return workspace.openFile(fullPath, NSString.stringWith(name));
	}
	//TODO use CFURLCreateStringByAddingPercentEscapes instead
	NSString str = fullPath.stringByAddingPercentEscapesUsingEncoding(OS.NSUTF8StringEncoding);
	NSArray urls = NSArray.arrayWithObject(NSURL.URLWithString(str));
	return workspace.openURLs(urls, NSString.stringWith(identifier), 0, null, 0);
}

/**
 * Returns the receiver's image data.  This is the icon
 * that is associated with the receiver in the operating
 * system.
 *
 * @return the image data for the program, may be null
 */
public ImageData getImageData () {
	NSWorkspace workspace = NSWorkspace.sharedWorkspace();
	NSString fullPath;
	if (this.fullPath != null) {
		fullPath = NSString.stringWith(this.fullPath);
	} else {
		fullPath = workspace.fullPathForApplication(NSString.stringWith(name));
	}
	if (fullPath != null) {
		NSImage nsImage = workspace.iconForFile(fullPath);
		if (nsImage != null) {
			NSSize size = new NSSize();
			size.width = size.height = 16;
			nsImage.setSize(size);
			NSBitmapImageRep imageRep = null;
			NSImageRep rep = nsImage.bestRepresentationForDevice(null);
			if (rep.isKindOfClass(OS.class_NSBitmapImageRep)) { 
				imageRep = new NSBitmapImageRep(rep.id);
			}
			if (imageRep != null) {
				int width = (int)/*64*/imageRep.pixelsWide();
				int height = (int)/*64*/imageRep.pixelsHigh();
				int bpr = (int)/*64*/imageRep.bytesPerRow();
				int bpp = (int)/*64*/imageRep.bitsPerPixel();
				int dataSize = height * bpr;
				byte[] srcData = new byte[dataSize];
				OS.memmove(srcData, imageRep.bitmapData(), dataSize);
				//TODO check color info
				PaletteData palette = new PaletteData(0xFF000000, 0xFF0000, 0xFF00);
				ImageData data = new ImageData(width, height, bpp, palette, 4, srcData);
				data.bytesPerLine = bpr;
				data.alphaData = new byte[width * height];
				for (int i = 3, o = 0; i < srcData.length; i+= 4, o++) {
					data.alphaData[o] = srcData[i];
				}
				return data;
			}
		}
	}
	return null;
}

/**
 * Returns the receiver's name.  This is as short and
 * descriptive a name as possible for the program.  If
 * the program has no descriptive name, this string may
 * be the executable name, path or empty.
 *
 * @return the name of the program
 */
public String getName () {
	return name;
}

/**
 * Compares the argument to the receiver, and returns true
 * if they represent the <em>same</em> object using a class
 * specific comparison.
 *
 * @param other the object to compare with this object
 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
 *
 * @see #hashCode()
 */
public boolean equals(Object other) {
	if (this == other) return true;
	if (other instanceof Program) {
		final Program program = (Program) other;
		return name.equals(program.name);
	}
	return false;
}

/**
 * Returns an integer hash code for the receiver. Any two 
 * objects that return <code>true</code> when passed to 
 * <code>equals</code> must return the same value for this
 * method.
 *
 * @return the receiver's hash
 *
 * @see #equals(Object)
 */
public int hashCode() {
	return name.hashCode();
}

/**
 * Returns a string containing a concise, human-readable
 * description of the receiver.
 *
 * @return a string representation of the program
 */
public String toString () {
	return "Program {" + name + "}";
}

}
