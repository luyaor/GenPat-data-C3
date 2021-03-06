/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *    mkaufman@bea.com
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.EclipseRoundCompleteEvent;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl.AnnotationVisitor;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * Dispatch APT. 
 * @author tyeung
 *
 */
public class APTDispatch {

	public static APTBuildResult runAPTDuringBuild(
			final List<AnnotationProcessorFactory> factories, IFile file,
			IJavaProject javaProj) {
		
		//
		//  bail-out early if there aren't factories.
		// 
		if ( factories == null || factories.size() == 0 )
			return EMPTY_BUILD_RESULT;
		
		//
		// scan file for annotation instances, and bail early if none.
		// do this before construction ProcessorEnvImpl to avoid 
		// unnecessary creation of AST.
		//
		if ( ! hasAnnotationInstance( file ) )
			return EMPTY_BUILD_RESULT;
					
		ProcessorEnvImpl processorEnv = ProcessorEnvImpl
				.newProcessorEnvironmentForBuild( file, javaProj);
		Set newFiles = runAPT(factories, processorEnv);
		Set<String> newDependencies = processorEnv.getTypeDependencies();
		APTBuildResult result = new APTBuildResult( newFiles, newDependencies );
		return result;
	}

	/**
	 * Run annnotation processing.
	 * @param factories the list of annotation processor factories to be run.
	 * @return the set of files that need to be compiled.
	 */
	public static Set<IFile> runAPTDuringReconcile(
			final List<AnnotationProcessorFactory> factories,
			final CompilationUnit astCompilationUnit,
			ICompilationUnit compilationUnit, IJavaProject javaProj) {		
		
		//
		//  bail-out early if there aren't factories.
		// 
		if ( factories == null || factories.size() == 0 )
			return Collections.emptySet();
		
		ProcessorEnvImpl processorEnv = ProcessorEnvImpl
				.newProcessorEnvironmentForReconcile(astCompilationUnit,
						compilationUnit, javaProj);
		return runAPT(factories, processorEnv);
	}

	private static Set<IFile> runAPT(
			final List<AnnotationProcessorFactory> factories,
			final ProcessorEnvImpl processorEnv) {
		try {
			if (factories.size() == 0)
				return Collections.emptySet();

			if ( ! processorEnv.getFile().exists() )
				return Collections.emptySet();
			
			// clear out all the markers from the previous round.
			final String markerType = processorEnv.getPhase() == ProcessorEnvImpl.Phase.RECONCILE ? ProcessorEnvImpl.RECONCILE_MARKER
					: ProcessorEnvImpl.BUILD_MARKER;
			try {
				processorEnv.getFile().deleteMarkers(markerType, true,
						IResource.DEPTH_INFINITE);

			} catch (CoreException e) {
				throw new IllegalStateException(e);
			}
			final Map<String, AnnotationTypeDeclaration> annotationDecls = getAnnotationTypeDeclarations(
					processorEnv.getAstCompilationUnit(), processorEnv);
			if (annotationDecls.isEmpty())
				return Collections.emptySet();

			for (int i = 0, size = factories.size(); i < size; i++) {
				final AnnotationProcessorFactory factory = (AnnotationProcessorFactory) factories
						.get(i);
				final Set<AnnotationTypeDeclaration> factoryDecls = getAnnotations(
						factory, annotationDecls);

				if (factoryDecls != null && factoryDecls.size() > 0) {
					final AnnotationProcessor processor = factory
							.getProcessorFor(factoryDecls, processorEnv);
					if (processor != null)
						processor.process();
				}

				if (annotationDecls.isEmpty())
					break;
			}
			// TODO: (theodora) log unclaimed annotations.

			// notify the processor listeners
			final Set<AnnotationProcessorListener> listeners = processorEnv
					.getProcessorListeners();
			for (AnnotationProcessorListener listener : listeners) {
				EclipseRoundCompleteEvent event = null;
				if (listener instanceof RoundCompleteListener) {
					if (event == null)
						event = new EclipseRoundCompleteEvent(processorEnv);
					final RoundCompleteListener rcListener = (RoundCompleteListener) listener;
					rcListener.roundComplete(event);
				}
			}

			final Set<IFile> generatedFiles = new HashSet<IFile>();
			generatedFiles.addAll( processorEnv.getGeneratedFiles() );
			processorEnv.close();
			return generatedFiles;

			// log unclaimed annotations.
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return Collections.emptySet();
	}

	/**
	 * invoking annotation processors respecting apt semantics.
	 */
	private static void checkAnnotations(
			final List<AnnotationProcessorFactory> factories,
			final Map<String, AnnotationTypeDeclaration> declarations,
			final ProcessorEnvImpl env) {
		for (int i = 0, size = factories.size(); i < size; i++) {
			final AnnotationProcessorFactory factory = (AnnotationProcessorFactory) factories
					.get(i);
			final Set<AnnotationTypeDeclaration> factoryDecls = getAnnotations(
					factory, declarations);
			final AnnotationProcessor processor = factory.getProcessorFor(
					factoryDecls, env);
			processor.process();
			if (declarations.isEmpty())
				return;
		}
		// log unclaimed annotations.
	}

	private static Map<String, AnnotationTypeDeclaration> getAnnotationTypeDeclarations(
			CompilationUnit astCompilationUnit, ProcessorEnvImpl env) {
		final List<Annotation> instances = new ArrayList<Annotation>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		astCompilationUnit.accept(new AnnotationVisitor(instances));
		final Map<String, AnnotationTypeDeclaration> decls = new HashMap<String, AnnotationTypeDeclaration>();
		for (int i = 0, size = instances.size(); i < size; i++) {
			final Annotation instance = instances.get(i);
			final ITypeBinding annoType = instance.resolveTypeBinding();
			if (annoType == null)
				continue;
			final TypeDeclarationImpl annoDecl = Factory.createReferenceType(
					annoType, env);
			if (annoDecl.kind() == EclipseMirrorImpl.MirrorKind.TYPE_ANNOTATION)
				decls.put(annoDecl.getQualifiedName(),
						(AnnotationTypeDeclaration) annoDecl);
		}
		return decls;
	}

	/**
	 * @return the set of {@link AnnotationTypeDeclaration} that {@link #factory} supports or null
	 *         if the factory doesn't support any of the declarations.
	 *         If the factory supports "*", then the empty set will be returned
	 *
	 * This method will destructively modify {@link #declarations}. Entries will be removed from
	 * {@link #declarations} as the declarations are being added into the returned set.
	 */
	private static Set<AnnotationTypeDeclaration> getAnnotations(
			final AnnotationProcessorFactory factory,
			final Map<String, AnnotationTypeDeclaration> declarations)

	{
		final Collection<String> supportedTypes = factory
				.supportedAnnotationTypes();

		if (supportedTypes == null || supportedTypes.size() == 0)
			return Collections.emptySet();

		final Set<AnnotationTypeDeclaration> fDecls = new HashSet<AnnotationTypeDeclaration>();

		for (Iterator<String> it = supportedTypes.iterator(); it.hasNext();) {
			final String typeName = it.next();
			if (typeName.equals("*")) {
				declarations.clear();
				return Collections.emptySet();
			} else if (typeName.endsWith("*")) {
				final String prefix = typeName.substring(0,
						typeName.length() - 2);
				for (Iterator<Map.Entry<String, AnnotationTypeDeclaration>> entries = declarations
						.entrySet().iterator(); entries.hasNext();) {
					final Map.Entry<String, AnnotationTypeDeclaration> entry = entries
							.next();
					final String key = entry.getKey();
					if (key.startsWith(prefix)) {
						fDecls
								.add((AnnotationTypeDeclaration) entry
										.getValue());
						entries.remove();
					}
				}
			} else {
				final AnnotationTypeDeclaration decl = declarations
						.get(typeName);
				if (decl != null) {
					fDecls.add(decl);
					declarations.remove(typeName);
				}
			}
		}
		return fDecls.isEmpty() ? null : fDecls;
	}
	
	/**
	 * scan the source code to see if there are any annotation tokens
	 */
	private static boolean hasAnnotationInstance( IFile f )
	{
		try
		{
			char[] source = ProcessorEnvImpl.getFileContents( f );
			IScanner scanner = ToolFactory.createScanner( 
				false, false, false, CompilerOptions.VERSION_1_5 );
			scanner.setSource( source );
			int token = scanner.getNextToken();
			while ( token != ITerminalSymbols.TokenNameEOF )
			{
				token = scanner.getNextToken();
				if ( token == ITerminalSymbols.TokenNameAT )
				{
					//
					// found an @ sign, see if next token is "interface"
					// @interface is an annotation decl and not an annotation
					// instance.  
					//
					token = scanner.getNextToken();
					if ( token != ITerminalSymbols.TokenNameinterface )
						return true;
				}
			}
			return false;
		}
		catch( InvalidInputException iie )
		{
			// lex error, so report false
			return false;
		}
		catch( Exception e )
		{
			// TODO:  deal with this exception
			e.printStackTrace();
			return false;
		}
	}
	
	public static class APTBuildResult
	{
		APTBuildResult( Set<IFile> files, Set<String> deps )
		{
			_newFiles = files;
			_newDependencies = deps;
		}
		
		private Set<IFile> _newFiles;
		private Set<String> _newDependencies;
		
		Set<IFile> getNewFiles() { return _newFiles; }
		Set<String> getNewDependencies() { return _newDependencies; }
	}

	public static final APTBuildResult EMPTY_BUILD_RESULT = new APTBuildResult( (Set<IFile>)Collections.emptySet(), (Set<String>)Collections.emptySet() );
	
}
