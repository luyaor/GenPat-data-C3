/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;

public class TestSourceElementRequestor implements ISourceElementRequestor {
/**
 * DummySourceElementRequestor constructor comment.
 */
public TestSourceElementRequestor() {
	super();
}
/**
 * acceptConstructorReference method comment.
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {}
/**
 * acceptFieldReference method comment.
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * acceptImport method comment.
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand, int modifiers) {}
/**
 * acceptLineSeparatorPositions method comment.
 */
public void acceptLineSeparatorPositions(int[] positions) {}
/**
 * acceptMethodReference method comment.
 */
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {}
/**
 * acceptPackage method comment.
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {}
/**
 * acceptProblem method comment.
 */
public void acceptProblem(IProblem problem) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * acceptTypeReference method comment.
 */
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * acceptUnknownReference method comment.
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {}
/**
 * enterClass method comment.
 */
public void enterClass(TypeInfo typeInfo) {}
/**
 * enterCompilationUnit method comment.
 */
public void enterCompilationUnit() {}
/**
 * enterConstructor method comment.
 */
public void enterConstructor(MethodInfo methodInfo) {}
/**
 * enterEnum method comment.
 */
public void enterEnum(TypeInfo typeInfos) {}
/**
 * enterField method comment.
 */
public void enterField(FieldInfo fieldInfo) {}
/**
 * enterInterface method comment.
 */
public void enterInterface(TypeInfo typeInfo) {}
/**
 * enterMethod method comment.
 */
public void enterMethod(MethodInfo methodInfo) {}
/**
 * exitClass method comment.
 */
public void exitClass(int declarationEnd) {}
/**
 * exitCompilationUnit method comment.
 */
public void exitCompilationUnit(int declarationEnd) {}
/**
 * exitConstructor method comment.
 */
public void exitConstructor(int declarationEnd) {}
/**
 * exitEnum method comment.
 */
public void exitEnum(int declarationEnd) {}
/**
 * exitField method comment.
 */
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {}
/**
 * exitInterface method comment.
 */
public void exitInterface(int declarationEnd) {}
/**
 * exitMethod method comment.
 */
public void exitMethod(int declarationEnd) {}

/**
 * enterInitializer method comment.
 */
public void enterInitializer(int sourceStart, int sourceEnd) {
}

/**
 * exitInitializer method comment.
 */
public void exitInitializer(int sourceEnd) {
}

}
