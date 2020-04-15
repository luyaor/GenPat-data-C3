package org.eclipse.jdt.apt.core.internal;

import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Stores project-specific data for APT. Analagous to JavaProject
 * @author jgarms
 *
 */
public class AptProject {
	
	private final IJavaProject _javaProject;
	
	private final GeneratedFileManager _gfm;
	
	public AptProject(final IJavaProject javaProject) {
		_javaProject = javaProject;
		_gfm = new GeneratedFileManager(this);
	}
	
	public IJavaProject getJavaProject() {
		return _javaProject;
	}
	
	public GeneratedFileManager getGeneratedFileManager() {
		return _gfm;
	}
}
