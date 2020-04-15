package org.eclipse.swt.internal;
/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
 
/**
 * This class implements the conversions between unicode characters
 * and the <em>platform supported</em> representation for characters.
 * <p>
 * Note that, unicode characters which can not be found in the platform
 * encoding will be converted to an arbitrary platform specific character.
 * </p>
 */
 
public final class Converter {
	public static final byte [] NullByteArray = new byte [1];
	public static final char [] NullCharArray = new char [1];
	public static final byte [] EmptyByteArray = new byte [0];
	public static final char [] EmptyCharArray = new char [0];
/**
 * Returns the default code page for the platform where the
 * application is currently running.
 *
 * @return the default code page
 */
public static String defaultCodePage () {
	return null;
}
public static char [] mbcsToWcs (String codePage, byte [] buffer) {
	//SLOW AND BOGUS
	return new String (buffer).toCharArray ();
}
public static byte [] wcsToMbcs (String codePage, String string, boolean terminate) {
	//SLOW AND BOGUS
	if (!terminate) return string.getBytes ();
	byte [] buffer1 = string.getBytes ();
	byte [] buffer2 = new byte [buffer1.length + 1];
	System.arraycopy (buffer1, 0, buffer2, 0, buffer1.length);
	return buffer2;
}
public static byte [] wcsToMbcs (String codePage, char [] buffer, boolean terminate) {
	//SLOW AND BOGUS
	if (!terminate) return new String (buffer).getBytes ();
	byte [] buffer1 = new String (buffer).getBytes ();
	byte [] buffer2 = new byte [buffer1.length + 1];
	System.arraycopy (buffer1, 0, buffer2, 0, buffer1.length);
	return buffer2;
}
}
