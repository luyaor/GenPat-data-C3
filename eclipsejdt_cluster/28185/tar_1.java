/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.env;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.core.JavaModelException;

import com.sun.mirror.apt.Filer;


public class FilerImpl implements Filer {

	private ProcessorEnvImpl _env;
	private boolean _generatedClassFiles = false;
	
    public FilerImpl( ProcessorEnvImpl env )
    {
        _env = env;
    }
	
    /**
     * Creates a new source file and returns a writer for it. The file's name 
     * and path (relative to the root of all newly created source files) is 
     * based on the type to be declared in that file. If more than one type is 
     * being declared, the name of the principal top-level type (the public 
     * one, for example) should be used. 
     * 
     * Character set used is the default character set for the platform
     * 
     * @param name - canonical (fully qualified) name of the principal type being declared in this file 
     */
    public PrintWriter createSourceFile(String typeName) throws IOException 
    {
    	_env.checkValid();
        return new JavaSourceFilePrintWriter( typeName, new StringWriter(), _env ); 
    }


    /**  
     * Creates a new class file, and returns a stream for writing to it. The 
     * file's name and path (relative to the root of all newly created class 
     * files) is based on the name of the type being written. 
     *  
     * @param name - canonical (fully qualified) name of the type being written 
     * @return -a stream for writing to the new file 
     */
    public OutputStream createClassFile(String name) throws IOException 
    {
    	_env.checkValid();
		_generatedClassFiles = true;
		
		// We do not want to write to disk during reconcile
		if (_env.getPhase() == Phase.RECONCILE) {
			return new NoOpOutputStream();
		}
		
    	GeneratedFileManager gfm = _env.getAptProject().getGeneratedFileManager();
    	IPath path;
    	try 
    	{
    		 path = gfm.getGeneratedSourceFolderOutputLocation();
    	}
    	catch ( Exception e )
    	{
    		// TODO - stop throwing this exception
    		AptPlugin.log(e, "Failure getting the output file"); //$NON-NLS-1$
    		throw new IOException();
    	}
    	
    	path = path.append(name.replace('.', File.separatorChar) + ".class"); //$NON-NLS-1$
    	
        IFile file = _env.getProject().getFile(path);
        return new BinaryFileOutputStream(file);
    }
	
	public boolean hasGeneratedClassFile(){ return _generatedClassFiles; }

    /**
     * Creates a new text file, and returns a writer for it. The file is 
     * located along with either the newly created source or newly created 
     * binary files. It may be named relative to some package (as are source 
     * and binary files), and from there by an arbitrary pathname. In a loose 
     * sense, the pathname of the new file will be the concatenation of loc, 
     * pkg, and relPath. 
     * 
     * A charset for encoding the file may be provided. If none is given, 
     * the charset used to encode source files (see createSourceFile(String)) will be used. 
     *
     * @param loc - location of the new file
     * @param pkg - package relative to which the file should be named, or the empty string if none
     * @param relPath - final pathname components of the file
     * @param charsetName - the name of the charset to use, or null if none is being explicitly specified 
     * @return - a writer for the new file 
     */
    public PrintWriter createTextFile(Filer.Location loc, String pkg, File relPath, String charsetName) 
        throws IOException 
    {
    	_env.checkValid();
    	
    	// If we're reconciling, we do not want to actually create the text file
    	if (_env.getPhase() == Phase.RECONCILE) {
    		return new NoOpPrintWriter();
    	}
    	
    	IPath path = getOutputFileForLocation( loc, pkg, relPath );
    	IFile file = _env.getProject().getFile(path);
    	OutputStream binaryOut = new BinaryFileOutputStream(file);
 
    	if (charsetName == null) {
    		return new PrintWriter(binaryOut);
    	}
    	else {
    		OutputStreamWriter outWriter = new OutputStreamWriter(binaryOut, charsetName);
    		return new PrintWriter(outWriter);
    	}
    }

    /**
     * Creates a new binary file, and returns a stream for writing to it. The 
     * file is located along with either the newly created source or newly 
     * created binary files. It may be named relative to some package (as 
     * are source and binary files), and from there by an arbitrary pathname. 
     * In a loose sense, the pathname of the new file will be the concatenation 
     * of loc, pkg, and relPath. 
     * 
     * @param loc - location of the new file
     * @param pkg - package relative to which the file should be named, or the empty string if none
     * @param relPath - final pathname components of the file 
     * @return a stream for writing to the new file 
     */
    public OutputStream createBinaryFile(Filer.Location loc, String pkg, File relPath)
        throws IOException 
    {
    	_env.checkValid();
    	
    	// We do not want to write to disk during reconcile
		if (_env.getPhase() == Phase.RECONCILE) {
			return new NoOpOutputStream();
		}
    	
    	IPath path = getOutputFileForLocation( loc, pkg, relPath );
    	IFile file = _env.getProject().getFile(path);
    	return new BinaryFileOutputStream(file);
    }
	
    /**
     * Return a project-relative path
     */
    private IPath getOutputFileForLocation( Filer.Location loc, String pkg, File relPath )
    	throws IOException
    {
    	GeneratedFileManager gfm = _env.getAptProject().getGeneratedFileManager();
    	IPath path = null;
    	if ( loc == Filer.Location.CLASS_TREE )
    	{
    		try 
    		{
    			path = gfm.getGeneratedSourceFolderOutputLocation();
    		}
    		catch ( JavaModelException e )
    		{
    			AptPlugin.log(e, "Failure getting the output file"); //$NON-NLS-1$
    			throw new IOException();
    		}
    	}
    	else if ( loc == Filer.Location.SOURCE_TREE ) {
    		path = gfm.getGeneratedSourceFolder().getProjectRelativePath();
    	}
    	
        if( pkg != null )
            path = path.append(pkg.replace('.', File.separatorChar) );

        path = path.append(relPath.getPath() );
    	
        // Create the parent folder (need an absolute path temporarily)
        IPath absolutePath = _env.getProject().getLocation().append(path);
        File parentFile = absolutePath.toFile().getParentFile();
        FileSystemUtil.mkdirs( parentFile );
        
    	return path;
    }
    
    
}
