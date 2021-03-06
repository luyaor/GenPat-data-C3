/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.builder.ProblemFactory;

/**
 * This operation is used to sort elements in a compilation unit according to
 * certain criteria.
 * 
 * @since 2.1
 */
public class SortElementsOperation extends JavaModelOperation {
	
	Comparator comparator;
	
	/**
	 * Constructor for SortElementsOperation.
	 * @param elements
	 */
	public SortElementsOperation(IJavaElement[] elements, Comparator comparator) {
		super(elements);
		this.comparator = comparator;
	}

	/**
	 * Returns the amount of work for the main task of this operation for
	 * progress reporting.
	 */
	protected int getMainAmountOfWork(){
		return fElementsToProcess.length;
	}
	
	/**
	 * @see org.eclipse.jdt.internal.core.JavaModelOperation#executeOperation()
	 */
	protected void executeOperation() throws JavaModelException {
		try {
			beginTask(Util.bind("operation.sortelements"), getMainAmountOfWork()); //$NON-NLS-1$
			for (int i = 0, max = fElementsToProcess.length; i < max; i++) {
				JavaElementDelta delta = newJavaElementDelta();
				ICompilationUnit unit = ((JavaElement) fElementsToProcess[i]).getCompilationUnit();
				if (unit == null) {
					return;
				}
				IBuffer buffer = unit.getBuffer();
				if (buffer  == null) { 
					return;
				}
				char[] bufferContents = buffer.getCharacters();
				processElement(unit,bufferContents);
				unit.save(null, false);
				boolean isWorkingCopy = unit.isWorkingCopy();
				if (!isWorkingCopy) {
					this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE);
				}
				worked(1);
				 // if unit is working copy, then save will have already fired the delta
				if (!isWorkingCopy
					&& !Util.isExcluded(unit)
					&& unit.getParent().exists()) {
					delta.changed(unit, IJavaElementDelta.F_CONTENT);
					addDelta(delta);
				}				
			}
		} finally {
			done();
		}
	}

	/**
	 * Method processElement.
	 * @param unit
	 * @param bufferContents
	 */
	private void processElement(ICompilationUnit unit, char[] source) throws JavaModelException {
		SortElementBuilder builder = new SortElementBuilder(source, comparator);
		SourceElementParser parser = new SourceElementParser(builder,
			ProblemFactory.getProblemFactory(Locale.getDefault()), new CompilerOptions(JavaCore.getOptions()), true);
		
		IPackageFragment packageFragment = (IPackageFragment)unit.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		char[][] expectedPackageName = null;
		if (packageFragment != null){
			expectedPackageName = CharOperation.splitOn('.', packageFragment.getElementName().toCharArray());
		}
		parser.parseCompilationUnit(
			new BasicCompilationUnit(
				source,
				expectedPackageName,
				unit.getElementName(),
				unit.getJavaProject().getOption(JavaCore.CORE_ENCODING, true)),
			false);
		unit.getBuffer().setContents(builder.getSource());
	}
	//TODO: (olivier) unused?
	private CompilationUnitDeclaration parseCompilationUnit(ICompilationUnit compilationUnit, char[] source) {
		Map settings = compilationUnit.getJavaProject().getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(settings);
		compilerOptions.complianceLevel = CompilerOptions.JDK1_4;
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
					compilerOptions, 
					new DefaultProblemFactory(Locale.getDefault())),
			false,
			compilerOptions.sourceLevel >= CompilerOptions.JDK1_4);
		org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit = 
			new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
				source, 
				"", //$NON-NLS-1$
				compilerOptions.defaultEncoding);
		CompilationUnitDeclaration compilationUnitDeclaration = parser.dietParse(sourceUnit, new CompilationResult(sourceUnit, 0, 0, compilerOptions.maxProblemsPerUnit));
		
		if (compilationUnitDeclaration.ignoreMethodBodies) {
			compilationUnitDeclaration.ignoreFurtherInvestigation = true;
			// if initial diet parse did not work, no need to dig into method bodies.
			return compilationUnitDeclaration; 
		}
		
		//fill the methods bodies in order for the code to be generated
		//real parse of the method....
		parser.scanner.setSource(source);
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = compilationUnitDeclaration.types;
		if (types != null) {
			for (int i = types.length; --i >= 0;)
				types[i].parseMethod(parser, compilationUnitDeclaration);
		}
		return compilationUnitDeclaration;
	}
	/**
	 * Possible failures:
	 * <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the compilation unit supplied to the operation is <code>null</code></li>.
	 *  <li>INVALID_ELEMENT_TYPES - the supplied elements are not an instance of IWorkingCopy</li>.
	 * </ul>
	 * @see IJavaModelStatus
	 * @see JavaConventions
	 */
	public IJavaModelStatus verify() {
		if (fElementsToProcess.length <= 0) {
			return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
		}
		for (int i = 0, max = fElementsToProcess.length; i < max; i++) {
			if (fElementsToProcess[i] == null) {
				return new JavaModelStatus(IJavaModelStatusConstants.NO_ELEMENTS_TO_PROCESS);
			}
			if (!(fElementsToProcess[i] instanceof IWorkingCopy)) {
				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, fElementsToProcess[i]);
			}
		}
		return JavaModelStatus.VERIFIED_OK;
	}
}
