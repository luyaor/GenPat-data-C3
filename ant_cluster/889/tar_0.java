/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.ant.modules.core;

import java.net.URL;
import org.apache.myrmidon.api.DataType;
import org.apache.myrmidon.api.TaskException;
import org.apache.myrmidon.components.deployer.DeploymentException;
import org.apache.myrmidon.components.type.DefaultTypeFactory;

/**
 * Method to register a single datatype.
 *
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 */
public class RegisterDataType
    extends AbstractTypeDefinition
{
    protected void registerResource( final String name,
                                     final String className,
                                     final URL url )
        throws TaskException
    {
        if( null == className )
        {
            try { getDeployer().deployDataType( name, url.toString(), url ); }
            catch( final DeploymentException de )
            {
                throw new TaskException( "Failed deploying " + name + " from " + url, de );
            }
        }
        else
        {
            final DefaultTypeFactory factory = new DefaultTypeFactory( new URL[] { url } );
            factory.addNameClassMapping( name, className );
            try { getTypeManager().registerType( DataType.ROLE, name, factory ); }
            catch( final Exception e )
            {
                throw new TaskException( "Failed registering " + name + " from " + url, e );
            }
        }
    }
}
