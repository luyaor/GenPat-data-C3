/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

/**
 * Untar a file. Heavily based on the Expand task.
 *
 * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
 * @author <a href="mailto:umagesh@rediffmail.com">Magesh Umasankar</a>
 */
public class Untar extends Expand
{

    protected void expandFile( FileUtils fileUtils, File srcF, File dir )
    {
        TarInputStream tis = null;
        try
        {
            log( "Expanding: " + srcF + " into " + dir, Project.MSG_INFO );

            tis = new TarInputStream( new FileInputStream( srcF ) );
            TarEntry te = null;

            while( ( te = tis.getNextEntry() ) != null )
            {
                extractFile( fileUtils, srcF, dir, tis,
                    te.getName(),
                    te.getModTime(), te.isDirectory() );
            }
            log( "expand complete", Project.MSG_VERBOSE );

        }
        catch( IOException ioe )
        {
            throw new BuildException( "Error while expanding " + srcF.getPath(), ioe );
        }
        finally
        {
            if( tis != null )
            {
                try
                {
                    tis.close();
                }
                catch( IOException e )
                {}
            }
        }
    }
}
