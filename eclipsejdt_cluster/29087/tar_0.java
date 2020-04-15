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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class Requestor extends Assert implements ICompilerRequestor {
	public boolean hasErrors = false;
	public IProblemFactory problemFactory;
	public String outputPath;
	private boolean generateOutput;
	public Hashtable expectedProblems = new Hashtable();
	public String problemLog = "";
public Requestor(IProblemFactory problemFactory, String outputPath, boolean generateOutput) {
	this.problemFactory = problemFactory;
	this.outputPath = outputPath;
	this.generateOutput = generateOutput;
}
public void acceptResult(CompilationResult compilationResult) {
	StringBuffer buffer = new StringBuffer(100);
	hasErrors |= compilationResult.hasErrors();
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		IProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		for (int i = 0; i < count; i++) { 
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()).replace('/', '\\'));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(compilationResult.compilationUnit));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
		problemLog += buffer.toString();
	}
	outputClassFiles(compilationResult);	
}
protected void outputClassFiles(CompilationResult unitResult) {

	if ((unitResult != null) && (!unitResult.hasErrors() || generateOutput)) {
		Enumeration classFiles = unitResult.compiledTypes.elements();
		if (outputPath != null) {
			while (classFiles.hasMoreElements()) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = (ClassFile) classFiles.nextElement();
				String relativeName = 
					new String(classFile.fileName()).replace('/', File.separatorChar) + ".class";
				try {
					ClassFile.writeToDisk(true, outputPath, relativeName, classFile.getBytes());
				} catch(IOException e) {
				}
			}
		}
	}
}
}
