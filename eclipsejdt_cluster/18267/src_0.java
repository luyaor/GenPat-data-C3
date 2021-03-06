/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class AbstractCompilerToolTest extends BatchCompilerTest {
	public AbstractCompilerToolTest(String name) {
		super(name);
	}
	static class CompilerInvocationTestsArguments {
		StandardJavaFileManager standardJavaFileManager;
		List<String> options;
		String[] fileNames;
		CompilerInvocationTestsArguments(
				StandardJavaFileManager standardJavaFileManager, 
				List<String> options,
				String[] fileNames) {
			this.standardJavaFileManager = standardJavaFileManager;
			this.options = options;
			this.fileNames = fileNames;
		}
		@Override
		public String toString() {
			StringBuffer result = new StringBuffer();
			for (String option: this.options) {
				result.append(option);
				result.append(' ');
			}
			return result.toString();
		}
	}
	static class CompilerInvocationDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		PrintWriter err;
		public CompilerInvocationDiagnosticListener(PrintWriter err) {
			this.err = err;
		}
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			// TODO Auto-generated method stub
			
		}
	}
	static EclipseCompiler COMPILER = new EclipseCompiler();
	static JavaCompiler JAVAC_COMPILER = ToolProvider.getSystemJavaCompiler();
	@Override
	protected boolean invokeCompiler(
			PrintWriter out, 
			PrintWriter err,
			Object extraArguments,
			TestCompilationProgress compilationProgress) {
		CompilerInvocationTestsArguments arguments = (CompilerInvocationTestsArguments) extraArguments;
		StandardJavaFileManager manager = arguments.standardJavaFileManager;
		if (manager == null) {
			manager = JAVAC_COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
		}
		List<File> files = new ArrayList<File>();
		String[] fileNames = arguments.fileNames;
		for (int i = 0, l = fileNames.length; i < l; i++) {
			if (fileNames[i].startsWith(OUTPUT_DIR)) {
				files.add(new File(fileNames[i]));
			} else {
				files.add(new File(OUTPUT_DIR + File.separator + fileNames[i]));
			}
		}
		CompilationTask task = COMPILER.getTask(out, arguments.standardJavaFileManager /* carry the null over */, new CompilerInvocationDiagnosticListener(err), arguments.options, null, manager.getJavaFileObjectsFromFiles(files));
		return task.call();
	}
}
