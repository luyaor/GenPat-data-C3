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
package org.eclipse.jdt.core.tests.util;

import java.io.*;
public class Util {
	public static String OUTPUT_DIRECTORY = "comptest";

	public static int MAX_PORT_NUMBER = 9999;

public static String[] concatWithClassLibs(String classpath, boolean inFront) {
	String[] classLibs = getJavaClassLibs();
	final int length = classLibs.length;
	String[] defaultClassPaths = new String[length + 1];
	if (inFront) {
		System.arraycopy(classLibs, 0, defaultClassPaths, 1, length);
		defaultClassPaths[0] = classpath;
	} else {
		System.arraycopy(classLibs, 0, defaultClassPaths, 0, length);
		defaultClassPaths[length] = classpath;
	} 
	return defaultClassPaths;
}
public static String convertToIndependantLineDelimiter(String source) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = source.length(); i < length; i++) {
		char car = source.charAt(i);
		if (car == '\r') {
			buffer.append('\n');
			if (i < length-1 && source.charAt(i+1) == '\n') {
				i++; // skip \n after \r
			}
		} else {
			buffer.append(car);
		}
	}
	return buffer.toString();
}
/**
 * Copy the given source (a file or a directory that must exists) to the given destination (a directory that must exists).
 */
public static void copy(String sourcePath, String destPath) {
	sourcePath = toNativePath(sourcePath);
	destPath = toNativePath(destPath);
	File source = new File(sourcePath);
	if (!source.exists()) return;
	File dest = new File(destPath);
	if (!dest.exists()) return;
	if (source.isDirectory()) {
		String[] files = source.list();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				String file = files[i];
				File sourceFile = new File(source, file);
				if (sourceFile.isDirectory()) {
					File destSubDir = new File(dest, file);
					destSubDir.mkdir();
					copy(sourceFile.getPath(), destSubDir.getPath());
				} else {
					copy(sourceFile.getPath(), dest.getPath());
				}
			}
		}
	} else {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(source);
			File destFile = new File(dest, source.getName());
			if (destFile.exists() && !destFile.delete()) {
				throw new IOException(destFile + " is in use");
			}
		 	out = new FileOutputStream(destFile);
			int bufferLength = 1024;
			byte[] buffer = new byte[bufferLength];
			int read = 0;
			while (read != -1) {
				read = in.read(buffer, 0, bufferLength);
				if (read != -1) {
					out.write(buffer, 0, read);
				}
			}
		} catch (IOException e) {
			throw new Error(e.toString());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
/**
 * Generate a display string from the given String.
 * @param indent number of tabs are added at the begining of each line.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.displayString("abc\ndef\tghi")]
*/
public static String displayString(String inputString){
	return displayString(inputString, 0);
}
/**
 * Generate a display string from the given String.
 * It converts:
 * <ul>
 * <li>\t to \t</li>
 * <li>\r to \\r</li>
 * <li>\n to \n</li>
 * <li>\b to \\b</li>
 * <li>\f to \\f</li>
 * <li>\" to \\\"</li>
 * <li>\' to \\'</li>
 * <li>\\ to \\\\</li>
 * <li>All other characters are unchanged.</li>
 * </ul>
 * This method doesn't convert \r\n to \n. 
 * <p>
 * Example of use:
 * <o>
 * <li>
 * <pre>
 * input string = "abc\ndef\tghi",
 * indent = 3
 * result = "\"\t\t\tabc\\n" +
 * 			"\t\t\tdef\tghi\""
 * </pre>
 * </li>
 * <li>
 * <pre>
 * input string = "abc\ndef\tghi\n",
 * indent = 3
 * result = "\"\t\t\tabc\\n" +
 * 			"\t\t\tdef\tghi\\n\""
 * </pre>
 * </li>
 * <li>
 * <pre>
 * input string = "abc\r\ndef\tghi\r\n",
 * indent = 3
 * result = "\"\t\t\tabc\\r\\n" +
 * 			"\t\t\tdef\tghi\\r\\n\""
 * </pre>
 * </li>
 * </ol>
 * </p>
 * 
 * @param inputString the given input string
 * @param indent number of tabs are added at the begining of each line.
 *
 * @return the displayed string
*/
public static String displayString(String inputString, int indent) {
	int length = inputString.length();
	StringBuffer buffer = new StringBuffer(length);
	java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true);
	for (int i = 0; i < indent; i++) buffer.append("\t");
	buffer.append("\"");
	while (tokenizer.hasMoreTokens()){

		String token = tokenizer.nextToken();
		if (token.equals("\r")) {
			buffer.append("\\r");
			if (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				if (token.equals("\n")) {
					buffer.append("\\n");
					if (tokenizer.hasMoreTokens()) {
						buffer.append("\" + \n");
						for (int i = 0; i < indent; i++) buffer.append("\t");
						buffer.append("\"");
					}
					continue;
				} else {
					buffer.append("\" + \n");
					for (int i = 0; i < indent; i++) buffer.append("\t");
					buffer.append("\"");
				}
			} else {
				continue;
			}
		} else if (token.equals("\n")) {
			buffer.append("\\n");
			if (tokenizer.hasMoreTokens()) {
				buffer.append("\" + \n");
				for (int i = 0; i < indent; i++) buffer.append("\t");
				buffer.append("\"");
			}
			continue;
		}	

		StringBuffer tokenBuffer = new StringBuffer();
		for (int i = 0; i < token.length(); i++){ 
			char c = token.charAt(i);
			switch (c) {
				case '\r' :
					tokenBuffer.append("\\r");
					break;
				case '\n' :
					tokenBuffer.append("\\n");
					break;
				case '\b' :
					tokenBuffer.append("\\b");
					break;
				case '\t' :
					tokenBuffer.append("\t");
					break;
				case '\f' :
					tokenBuffer.append("\\f");
					break;
				case '\"' :
					tokenBuffer.append("\\\"");
					break;
				case '\'' :
					tokenBuffer.append("\\'");
					break;
				case '\\' :
					tokenBuffer.append("\\\\");
					break;
				default :
					tokenBuffer.append(c);
			}
		}
		buffer.append(tokenBuffer.toString());
	}
	buffer.append("\"");
	return buffer.toString();
}
/**
 * Reads the content of the given source file and converts it to a display string.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("c:/temp/X.java", 0)]
*/
public static String fileContentToDisplayString(String sourceFilePath, int indent, boolean independantLineDelimiter) {
	File sourceFile = new File(sourceFilePath);
	if (!sourceFile.exists()) {
		System.out.println("File " + sourceFilePath + " does not exists.");
		return null;
	}
	if (!sourceFile.isFile()) {
		System.out.println(sourceFilePath + " is not a file.");
		return null;
	}
	StringBuffer sourceContentBuffer = new StringBuffer();
	FileInputStream input = null;
	try {
		input = new FileInputStream(sourceFile);
	} catch (FileNotFoundException e) {
		return null;
	}
	try { 
		int read;
		do {
			read = input.read();
			if (read != -1) {
				sourceContentBuffer.append((char)read);
			}
		} while (read != -1);
		input.close();
	} catch (IOException e) {
		e.printStackTrace();
		return null;
	} finally {
		try {
			input.close();
		} catch (IOException e2) {
		}
	}
	String sourceString = sourceContentBuffer.toString();
	if (independantLineDelimiter) {
		sourceString = convertToIndependantLineDelimiter(sourceString);
	}
	return displayString(sourceString, indent);
}
/**
 * Reads the content of the given source file, converts it to a display string.
 * If the destination file path is not null, writes the result to this file.
 * Otherwise writes it to the console.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.fileContentToDisplayString("c:/temp/X.java", 0, null)]
*/
public static void fileContentToDisplayString(String sourceFilePath, int indent, String destinationFilePath, boolean independantLineDelimiter) {
	String displayString = fileContentToDisplayString(sourceFilePath, indent, independantLineDelimiter);
	if (destinationFilePath == null) {
		System.out.println(displayString);
		return;
	}
	writeToFile(displayString, destinationFilePath);
}
/**
 * Flush content of a given directory (leaving it empty),
 * no-op if not a directory.
 */
public static void flushDirectoryContent(File dir) {
	if (dir.isDirectory()) {
		String[] files = dir.list();
		if (files == null) return;
		for (int i = 0, max = files.length; i < max; i++) {
			File current = new File(dir, files[i]);
			if (current.isDirectory()) {
				flushDirectoryContent(current);
			}
			current.delete();
		}
	}
}
/**
 * Search the user hard-drive for a Java class library.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJavaClassLib()]
*/
public static String[] getJavaClassLibs() {
	String jreDir = getJREDirectory();
	if (jreDir == null)  {
		return new String[] {};
	} else {
		final String vmName = System.getProperty("java.vm.name");
		if ("J9".equals(vmName)) {
			return new String[] { toNativePath(jreDir + "/lib/jclMax/classes.zip")};
		} else {
			File file = new File(jreDir + "/lib/rt.jar");
			if (file.exists()) {
				return new String[] {
					toNativePath(jreDir + "/lib/rt.jar")
				};				
			} else {				
				return new String[] { 
					toNativePath(jreDir + "/lib/core.jar"),
					toNativePath(jreDir + "/lib/security.jar"),
					toNativePath(jreDir + "/lib/graphics.jar")
				};
			}
		}
	}
}
public static String getJavaClassLibsAsString() {
	String[] classLibs = getJavaClassLibs();
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, max = classLibs.length; i < max; i++) {
		buffer
			.append(classLibs[i])
			.append(File.pathSeparatorChar);
		
	}
	return buffer.toString();
}
/**
 * Returns the JRE directory this tests are running on.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getJREDirectory()]
*/
public static String getJREDirectory() {
	return System.getProperty("java.home");
}
/**
 * Search the user hard-drive for a possible output directory.
 * Returns null if none could be found.
 *
 * Example of use: [org.eclipse.jdt.core.tests.util.Util.getOutputDirectory()]
*/
public static String getOutputDirectory() {
	String container = System.getProperty("user.home");
	if (container == null){
		return null;
	} else {
		return toNativePath(container) + File.separator + OUTPUT_DIRECTORY;
	}
}
/**
 * Returns whether one of the arguments is "-expert".
 */
public static boolean isExpert(String[] args) {
	for (int i = 0; i < args.length; i++) {
		if (args[i].toLowerCase().equals("-expert")) {
			return true;
		}
	}
	return false;
}
/**
 * Returns the next available port number on the local host.
 */
public static int nextAvailablePortNumber() {
	for (int i = MAX_PORT_NUMBER; i > 1000; i--) {
		int localPort = new SocketHelper().getAvailablePort(i);
		if (localPort != -1) {
			return localPort;
		}
	}
	return -1;
}
/**
 * Makes the given path a path using native path separators as returned by File.getPath()
 * and trimming any extra slash.
 */
public static String toNativePath(String path) {
	String nativePath = path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
	return
		nativePath.endsWith("/") || nativePath.endsWith("\\") ?
			nativePath.substring(0, nativePath.length() - 1) :
			nativePath;
}
public static void writeToFile(String contents, String destinationFilePath) {
	File destFile = new File(destinationFilePath);
	FileOutputStream output = null;
	try {
		output = new FileOutputStream(destFile);
		PrintWriter writer = new PrintWriter(output);
		writer.print(contents);
		writer.flush();
	} catch (IOException e) {
		e.printStackTrace();
		return;
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e2) {
			}
		}
	}
}
}
