 /*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ICompilationParticipantResult;
import org.eclipse.jdt.core.compiler.ReconcileContext;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * A singleton object, created by callback through the
 * org.eclipse.jdt.core.compilationParticipants extension point.
 */
public class AptCompilationParticipant extends CompilationParticipant
{
	/** 
	 * Batch factories that claimed some annotation in a previous round of APT processing.
	 * This currently only apply to the build case since are only generating types during build
	 * and hence cause APT rounding.
	 * The set is an order preserving. The order is determined by their first invocation.
	 */
	private Set<AnnotationProcessorFactory> _previousRoundsBatchFactories = new LinkedHashSet<AnnotationProcessorFactory>();
	private int _buildRound = 0;
	private boolean _isBatch = false; 
	private static AptCompilationParticipant INSTANCE;
	/** 
	 * Files that has been processed by apt during the current build.
	 * Files that has been compiled may need re-compilation (from jdt's perspective) 
	 * because of newly generated types. APT only process each file once during a build and 
	 * this set will prevent unnecessary/incorrect compilation of already processed files.
	 */
	private Set<IFile> _processedFiles = null;
	
	public static AptCompilationParticipant getInstance() {
		return INSTANCE;
	}
	
	/**
	 * This class is constructed indirectly, by registering an extension to the 
	 * org.eclipse.jdt.core.compilationParticipants extension point.  Other
	 * clients should NOT construct this object.
	 */
	public AptCompilationParticipant()
	{
		INSTANCE = this;
	}
	
	public boolean isAnnotationProcessor(){
		return true;
	}
	
	public void buildStarting(ICompilationParticipantResult[] files, boolean isBatch){
		// this gets called multiple times during a build.
		// This gets called:
		// 1) after "aboutToBuild" is called.
        // 2) everytime an incremental build occur because of newly generated files
        // this gets called.
		if( _buildRound == 0 )
			_isBatch = isBatch;
	}
	
	public void processAnnotations(ICompilationParticipantResult[] allfiles) {	
		// This should not happen. There should always be file that that needs 
		// building when 
		final int total = allfiles == null ? 0 : allfiles.length;
		if( total == 0 )
			return;

		final IProject project = allfiles[0].getFile().getProject();
		final IJavaProject javaProject = JavaCore.create(project);
		// Don't dispatch on pre-1.5 project. They cannot legally have annotations
		String javaVersion = javaProject.getOption("org.eclipse.jdt.core.compiler.source", true); //$NON-NLS-1$		
		// Check for 1.3 or 1.4, as we don't want this to break in the future when 1.6
		// is a possibility
		if ("1.3".equals(javaVersion) || "1.4".equals(javaVersion)) { //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		if ( _isBatch && _buildRound == 0 ) {
			AnnotationProcessorFactoryLoader.getLoader().resetBatchProcessors(javaProject);
			_previousRoundsBatchFactories.clear();
		}
		
		try {
		
			// split up the list of files with annotations from those that don't
			// also exclude files that has already been processed.
			int annoFileCount = 0;
			int noAnnoFileCount = 0;
			for( int i=0; i<total; i++ ){
				if( _buildRound > 0 && _processedFiles.contains( allfiles[i].getFile() )){
					continue;
				}
				if( allfiles[i].hasAnnotations() )
					annoFileCount ++;
				else
					noAnnoFileCount ++;
			}
			// apt has already processed all files
			// files that are reported at this point is triggered by
			// dependencies introduced by type creation. 
			if( annoFileCount == 0 && noAnnoFileCount == 0 )
				return;
			
			ICompilationParticipantResult[] withAnnotation = null;
			ICompilationParticipantResult[] withoutAnnotation = null;
			
			if( annoFileCount != 0 )
				withAnnotation = new ICompilationParticipantResult[annoFileCount];
			if(noAnnoFileCount != 0 )
				withoutAnnotation = new ICompilationParticipantResult[noAnnoFileCount];
			int wIndex = 0; // index for 'withAnnotation' array
			int woIndex = 0; // index of 'withoutAnnotation' array
			for( int i=0; i<total; i++ ){		
				if( _processedFiles.contains( allfiles[i].getFile() ) )
					continue;
				if( allfiles[i].hasAnnotations() )
					withAnnotation[wIndex ++] = allfiles[i];
				else
					withoutAnnotation[woIndex ++] = allfiles[i];
			}
			
			for( ICompilationParticipantResult file : allfiles )
				_processedFiles.add(file.getFile());
		
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories =
				AnnotationProcessorFactoryLoader.getLoader().getFactoriesAndAttributesForProject(javaProject);
			
			AptProject aptProject = AptPlugin.getAptProject(javaProject);			
			Set<AnnotationProcessorFactory> dispatchedBatchFactories = 
				APTDispatchRunnable.runAPTDuringBuild(
						withAnnotation, 
						withoutAnnotation,
						aptProject, 
						factories, 
						_previousRoundsBatchFactories, 
						_isBatch);
			_previousRoundsBatchFactories.addAll(dispatchedBatchFactories);
		}
		finally {			
			_buildRound ++;
		}
	}
	
	public void reconcile(ReconcileContext context){
		
		try
		{	
			final ICompilationUnit workingCopy = context.getWorkingCopy();
			if( workingCopy == null ) 
				return;
			IJavaProject javaProject = workingCopy.getJavaProject();			
			if( javaProject == null )
				return;
			AptProject aptProject = AptPlugin.getAptProject(javaProject);
			
			Map<AnnotationProcessorFactory, FactoryPath.Attributes> factories = 
				AnnotationProcessorFactoryLoader.getLoader().getFactoriesAndAttributesForProject( javaProject );
			APTDispatchRunnable.runAPTDuringReconcile(context, aptProject, factories);
		}
		catch ( Throwable t )
		{
			AptPlugin.log(t, "Failure processing");  //$NON-NLS-1$
		}	
	}
	
	public void cleanStarting(IJavaProject javaProject){
		IProject p = javaProject.getProject();
		
		AptPlugin.getAptProject(javaProject).projectClean( true );
		try{
			// clear out all markers during a clean.
			IMarker[] markers = p.findMarkers(AptPlugin.APT_BATCH_PROCESSOR_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			if( markers != null ){
				for( IMarker marker : markers )
					marker.delete();
			}
		}
		catch(CoreException e){
			AptPlugin.log(e, "Unable to delete batch annotation processor markers"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Does APT have anything to do for this project?
	 * Even if there are no processors on the factory path, apt may still
	 * be involved during a clean.
	 */
	public boolean isActive(IJavaProject project){
		return AptConfig.isEnabled(project);
	}
	
	public int aboutToBuild(IJavaProject project) {
		if (AptConfig.isEnabled(project)) {
			// setup the classpath and make sure the generated source folder is on disk.
		AptPlugin.getAptProject(project).compilationStarted();
		}		
		_buildRound = 0; // reset
		_processedFiles = new HashSet<IFile>();
		// TODO: (wharley) if the factory path is different we need a full build
		return CompilationParticipant.READY_FOR_BUILD;
	}
}
