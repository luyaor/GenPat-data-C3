/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.tools.todo.taskdefs.javac;

import org.apache.myrmidon.api.TaskException;
import org.apache.myrmidon.api.TaskContext;
import org.apache.tools.todo.types.Commandline;
import org.apache.tools.todo.types.Path;
import org.apache.tools.todo.types.PathUtil;
import org.apache.tools.todo.util.FileUtils;
import org.apache.tools.todo.taskdefs.javac.DefaultCompilerAdapter;

/**
 * The implementation of the jvc compiler from microsoft. This is primarily a
 * cut-and-paste from the original javac task before it was refactored.
 *
 * @author James Davidson <a href="mailto:duncan@x180.com">duncan@x180.com</a>
 * @author Robin Green <a href="mailto:greenrd@hotmail.com">greenrd@hotmail.com
 *      </a>
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @author <a href="mailto:jayglanville@home.com">J D Glanville</a>
 */
public class Jvc extends DefaultCompilerAdapter
{

    public boolean execute()
        throws TaskException
    {
        getTaskContext().debug( "Using jvc compiler" );

        Path classpath = new Path();

        // jvc doesn't support bootclasspath dir (-bootclasspath)
        // so we'll emulate it for compatibility and convenience.
        if( m_bootclasspath != null )
        {
            classpath.addPath( m_bootclasspath );
        }

        // jvc doesn't support an extension dir (-extdir)
        // so we'll emulate it for compatibility and convenience.
        addExtdirs( classpath );

        if( ( m_bootclasspath == null ) || m_bootclasspath.isEmpty() )
        {
            // no bootclasspath, therefore, get one from the java runtime
            m_includeJavaRuntime = true;
        }
        else
        {
            // there is a bootclasspath stated.  By default, the
            // includeJavaRuntime is false.  If the user has stated a
            // bootclasspath and said to include the java runtime, it's on
            // their head!
        }
        addCompileClasspath( classpath );

        // jvc has no option for source-path so we
        // will add it to classpath.
        classpath.addPath( src );

        Commandline cmd = new Commandline();
        cmd.setExecutable( "jvc" );

        if( m_destDir != null )
        {
            cmd.addArgument( "/d" );
            cmd.addArgument( m_destDir );
        }

        // Add the Classpath before the "internal" one.
        cmd.addArgument( "/cp:p" );
        cmd.addArgument( PathUtil.formatPath( classpath ) );

        // Enable MS-Extensions and ...
        cmd.addArgument( "/x-" );
        // ... do not display a Message about this.
        cmd.addArgument( "/nomessage" );
        // Do not display Logo
        cmd.addArgument( "/nologo" );

        if( m_debug )
        {
            cmd.addArgument( "/g" );
        }
        if( m_optimize )
        {
            cmd.addArgument( "/O" );
        }
        if( m_verbose )
        {
            cmd.addArgument( "/verbose" );
        }

        addCurrentCompilerArgs( cmd );

        int firstFileName = cmd.size();
        logAndAddFilesToCompile( cmd );

        return executeExternalCompile( cmd.getCommandline(), firstFileName ) == 0;
    }
}
