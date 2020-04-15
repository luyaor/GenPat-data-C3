/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * Implementation of JavaFileObject used for Java 6 annotation processing within the IDE.
 * This object is used only for writing source and class files.
 * 
 * @since 3.3
 */
public class IdeOutputJavaFileObject implements JavaFileObject {
	
	private final IdeProcessingEnvImpl _env;
	private final CharSequence _name;
	private final Collection<IFile> _parentFiles;

	public IdeOutputJavaFileObject(IdeProcessingEnvImpl env, CharSequence name, Collection<IFile> parentFiles) {
		_env = env;
		_parentFiles = parentFiles;
		_name = name;
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#getAccessLevel()
	 */
	@Override
	public Modifier getAccessLevel() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#getKind()
	 */
	@Override
	public Kind getKind() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#getNestingKind()
	 */
	@Override
	public NestingKind getNestingKind() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.JavaFileObject#isNameCompatible(java.lang.String, javax.tools.JavaFileObject.Kind)
	 */
	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#delete()
	 */
	@Override
	public boolean delete() {
		throw new UnsupportedOperationException("Deleting a file is not permitted from within an annotation processor");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getCharContent(boolean)
	 */
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		//TODO
		throw new IllegalStateException("Generated files are write-only");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getLastModified()
	 */
	@Override
	public long getLastModified() {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#getName()
	 */
	@Override
	public String getName() {
		return _name.toString();
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openInputStream()
	 */
	@Override
	public InputStream openInputStream() throws IOException {
		throw new IllegalStateException("Opening an input stream on a generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openOutputStream()
	 */
	@Override
	public OutputStream openOutputStream() throws IOException {
		//TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openReader(boolean)
	 */
	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		throw new IllegalStateException("Opening a reader on a generated file is not permitted");
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#openWriter()
	 */
	@Override
	public Writer openWriter() throws IOException {
		return new IdeJavaSourceFileWriter(_env, _name, _parentFiles);
	}

	/* (non-Javadoc)
	 * @see javax.tools.FileObject#toUri()
	 */
	@Override
	public URI toUri() {
		// The file does not exist until its writer is closed.
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
