/*******************************************************************************
 * Copyright (c) 2007, 2014 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fixed a resource leak warning
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileManager;

import junit.framework.TestCase;

/**
 * Test the implementation of the Filer interface,
 * in more detail than BatchDispatchTests does.
 * @since 3.4
 */
public class FileManagerTests extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testFileManager() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File dir = new File(tmpFolder, "src" + System.currentTimeMillis());
		dir.mkdirs();
		File inputFile = new File(dir, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write("public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		StandardJavaFileManager fileManager = null;
		try {
			fileManager = new EclipseFileManager(Locale.getDefault(), Charset.defaultCharset());
	
			List<File> fins = new ArrayList<File>();
			fins.add(dir);
	
			JavaFileManager.Location sourceLoc = javax.tools.StandardLocation.SOURCE_PATH;
			fileManager.setLocation(sourceLoc, fins);
	
			Set<JavaFileObject.Kind> fileTypes = new HashSet<JavaFileObject.Kind>();
			fileTypes.add(JavaFileObject.Kind.SOURCE);
	
			Iterable<? extends JavaFileObject> compilationUnits = fileManager.list(sourceLoc, "", fileTypes, true);
	
			Iterator<? extends JavaFileObject> it = compilationUnits.iterator();
			StringBuilder builder = new StringBuilder();
			while (it.hasNext()) {
				JavaFileObject next = it.next();
				String name = next.getName();
				name = name.replace('\\', '/');
				int lastIndexOf = name.lastIndexOf('/');
				builder.append(name.substring(lastIndexOf + 1));
			}
			assertEquals("Wrong contents", "X.java", String.valueOf(builder));
			
			List<File> files = new ArrayList<File>();
			files.add(dir);
			try {
				fileManager.getJavaFileObjectsFromFiles(files);
				fail("IllegalArgumentException should be thrown but not");
			} catch(IllegalArgumentException iae) {
				// Do nothing
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fileManager.close();
			} catch (IOException e) {
				//ignore the exception
			}
		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());
		assertTrue("delete failed", dir.delete());
	}
}
