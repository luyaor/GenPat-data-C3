/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.core.jdom.*;

import junit.framework.Test;

public class CreateTypeSourceExamplesTests extends AbstractJavaModelTests {
	IDOMFactory domFactory;

	static final String LINE_SEPARATOR = System.getProperty("line.separator");

public CreateTypeSourceExamplesTests(String name) {
	super(name);
}
public static Test suite() {
	return new Suite(CreateTypeSourceExamplesTests.class);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.domFactory = new DOMFactory();
}
/**
 * Example of creating a class with an extends clause
 */
public void testCreateClassWithExtends() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setSuperclass("Bar");
	assertEquals(
		"source code incorrect", 
		"public class Foo extends Bar {" + LINE_SEPARATOR +
		"}" + LINE_SEPARATOR,
		type.getContents());
}
/**
 * Example of creating a class with an implements clause.
 */
public void testCreateClassWithImplements() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setSuperInterfaces(new String[] {"ISomething", "IOtherwise"});
	assertEquals(
		"source code incorrect", 
		"public class Foo implements ISomething, IOtherwise {" + LINE_SEPARATOR +
		"}" + LINE_SEPARATOR,
		type.getContents());
}

/**
 * Example of creating a class with an implements clause.
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10979
 */
public void testCreateClassWithImplements2() {
	IDOMType type= this.domFactory.createType("class A implements I1 {\n}");
	type.addSuperInterface("I2");
	assertEquals(
		"source code incorrect", 
		"class A implements I1, I2 {\n}",
		type.getContents());
}

/**
 * Example of creating a class with an implements clause.
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10979
 */
public void testCreateClassWithImplements3() {
	IDOMType type= this.domFactory.createType("class A {\n}");
	type.setSuperInterfaces(new String[] {"I1", "I2"});
	assertEquals(
		"source code incorrect", 
		"class A implements I1, I2 {\n}",
		type.getContents());
}

/**
 * Example of creating a class with an implements clause.
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=10979
 */
public void testCreateClassWithImplements4() {
	IDOMType type= this.domFactory.createType("class A implements I1{\n}");
	type.addSuperInterface("I2");
	assertEquals(
		"source code incorrect", 
		"class A implements I1, I2{\n}",
		type.getContents());
}

/**
 * Example of creating a class with modifiers
 */
public void testCreateClassWithModifiers() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setFlags(IConstants.AccPublic | IConstants.AccFinal);
	assertEquals(
		"source code incorrect", 
		"public final class Foo {" + LINE_SEPARATOR +
		"}" + LINE_SEPARATOR,
		type.getContents());
}
/**
 * Example of creating a default class
 */
public void testCreateEmptyClass() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	assertEquals(
		"source code incorrect", 
		"public class Foo {" + LINE_SEPARATOR +
		"}" + LINE_SEPARATOR,
		type.getContents());
}
/**
 * Ensures that an interface is created using
 * <code>CreateTypeSourceOperation</code> and that the source
 * of the created interface is correct.
 */
public void testCreateEmptyInterface() {
	IDOMType type= this.domFactory.createType();
	type.setName("Foo");
	type.setClass(false);
	assertEquals(
		"source code incorrect", 
		"public interface Foo {" + LINE_SEPARATOR +
		"}" + LINE_SEPARATOR,
		type.getContents());
}
}
