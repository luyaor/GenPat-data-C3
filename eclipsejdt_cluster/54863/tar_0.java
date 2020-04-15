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
package org.eclipse.jdt.core.tests.eval;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Enumeration;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.runtime.LocalVirtualMachine;

import org.eclipse.jdt.core.tests.runtime.LocalVMLauncher;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.runtime.TargetInterface;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.eval.EvaluationContext;

public class EvaluationSetup extends TestDecorator {
	String jrePath;
	String evalDirectory;
	EvaluationContext context;
	TargetInterface target;
	LocalVirtualMachine launchedVM;
public EvaluationSetup(Test test) {
	super(test);
}
protected void initTest(Object test, EvaluationContext evalContext, TargetInterface targetInterface, LocalVirtualMachine localVM, INameEnvironment env) {
	if (test instanceof EvaluationTest) {
		EvaluationTest evalTest = (EvaluationTest)test;
		evalTest.context = evalContext;
		evalTest.target = targetInterface;
		evalTest.launchedVM = localVM;
		evalTest.env = env;
		return;
	}
	if (test instanceof TestSuite) {
		TestSuite evaluationTestClassSuite = (TestSuite) test;
		Enumeration evaluationTestClassTests = evaluationTestClassSuite.tests();
		while (evaluationTestClassTests.hasMoreElements()) {
			initTest(evaluationTestClassTests.nextElement(), evalContext, targetInterface, localVM, env);
		}
		return;
	}
		
}
public void run(TestResult result) {
	try {
		setUp();
		super.run(result);
	} finally {
		tearDown();
	}
}
protected void setUp() {
	// Launch VM in evaluation mode
	int evalPort = Util.nextAvailablePortNumber();
	try {
		LocalVMLauncher launcher = LocalVMLauncher.getLauncher();
		launcher.setVMPath(this.jrePath);
		launcher.setEvalPort(evalPort);
		launcher.setEvalTargetPath(this.evalDirectory);
		this.launchedVM = launcher.launch();
	} catch (TargetException e) {
		throw new Error(e.getMessage());
	}

	// Thread that read the stout of the VM so that the VM doesn't block
	try {
		startReader("VM's stdout reader", this.launchedVM.getInputStream(), System.out);
	} catch (TargetException e) {
	}

	// Thread that read the sterr of the VM so that the VM doesn't block
	try {
		startReader("VM's sterr reader",this.launchedVM.getErrorStream(), System.err);
	} catch (TargetException e) {
	}

	// Create context
	this.context = new EvaluationContext();

	// Create target
	this.target = new TargetInterface();
	this.target.connect("localhost", evalPort, 10000);

	// Create name environment
	INameEnvironment env = new FileSystem(Util.getJavaClassLibs(), new String[0], null);

	// Init wrapped suite
	initTest(fTest, this.context, this.target, this.launchedVM, env);
}
protected void startReader(String name, final InputStream in, final PrintStream out) {
	(new Thread(name) {
		public void run() {
			int read = 0;
			while (read != -1) {
				try {
					read = in.read();
				} catch (java.io.IOException e) {
					read = -1;
				}
				if (read != -1) {
					out.print((char) read);
				}
			}
		}
	}).start();
}
protected void tearDown() {
	if (context != null) {
		LocalVirtualMachine vm = this.launchedVM;
		if (vm != null) {
			try {
				if (this.target != null) {
					this.target.disconnect(); // Close the socket first so that the OS resource has a chance to be freed. 
				}
				int retry = 0;
				while (vm.isRunning() && (++retry < 20)) {
					try {
						Thread.sleep(retry * 100);
					} catch (InterruptedException e) {
					}
				}
				if (vm.isRunning()) {
					vm.shutDown();
				}
				this.context = null;
			} catch (TargetException e) {
				throw new Error(e.getMessage());
			}
		}
	}
}
}
