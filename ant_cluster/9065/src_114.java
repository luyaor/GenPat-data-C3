/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.myrmidon.components.executor;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.DefaultComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLoggable;
import org.apache.avalon.framework.logger.Loggable;
import org.apache.log.Logger;
import org.apache.myrmidon.api.Task;
import org.apache.myrmidon.api.TaskContext;
import org.apache.myrmidon.api.TaskException;
import org.apache.myrmidon.api.TaskException;
import org.apache.myrmidon.interfaces.configurer.Configurer;
import org.apache.myrmidon.interfaces.executor.Executor;
import org.apache.myrmidon.interfaces.executor.ExecutionFrame;
import org.apache.myrmidon.interfaces.type.TypeException;
import org.apache.myrmidon.interfaces.type.TypeFactory;
import org.apache.myrmidon.interfaces.type.TypeManager;

public class DefaultExecutor
    extends AbstractLoggable
    implements Executor, Composable
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( DefaultExecutor.class );

    private Configurer           m_configurer;

    /**
     * Retrieve relevent services needed to deploy.
     *
     * @param componentManager the ComponentManager
     * @exception ComponentException if an error occurs
     */
    public void compose( final ComponentManager componentManager )
        throws ComponentException
    {
        m_configurer = (Configurer)componentManager.lookup( Configurer.ROLE );
    }

    public void execute( final Configuration taskModel, final ExecutionFrame frame )
        throws TaskException
    {
        debug( "creating.notice" );
        final Task task = createTask( taskModel.getName(), frame );

        debug( "logger.notice" );
        doLoggable( task, taskModel, frame.getLogger() );

        debug( "contextualizing.notice" );
        doContextualize( task, taskModel, frame.getContext() );

        debug( "composing.notice" );
        doCompose( task, taskModel, frame.getComponentManager() );

        debug( "configuring.notice" );
        doConfigure( task, taskModel, frame.getContext() );

        debug( "initializing.notice" );
        doInitialize( task, taskModel );

        debug( "executing.notice" );
        task.execute();

        debug( "disposing.notice" );
        doDispose( task, taskModel );
    }

    protected final void debug( final String key )
    {
        if( getLogger().isDebugEnabled() )
        {
            final String message = REZ.getString( key );
            getLogger().debug( message );
        }
    }

    protected final Task createTask( final String name, final ExecutionFrame frame )
        throws TaskException
    {
        try
        {
            final TypeFactory factory = frame.getTypeManager().getFactory( Task.ROLE );
            return (Task)factory.create( name );
        }
        catch( final TypeException te )
        {
            final String message = REZ.getString( "no-create.error", name );
            throw new TaskException( message, te );
        }
    }

    protected final void doConfigure( final Task task,
                                      final Configuration taskModel,
                                      final TaskContext context )
        throws TaskException
    {
        try { m_configurer.configure( task, taskModel, context ); }
        catch( final Throwable throwable )
        {
            final String message =
                REZ.getString( "config.error",
                               taskModel.getName(),
                               taskModel.getLocation(),
                               throwable.getMessage() );
            throw new TaskException( message, throwable );
        }
    }

    protected final void doCompose( final Task task,
                                    final Configuration taskModel,
                                    final ComponentManager componentManager )
        throws TaskException
    {
        if( task instanceof Composable )
        {
            try { ((Composable)task).compose( componentManager ); }
            catch( final Throwable throwable )
            {
                final String message =
                    REZ.getString( "compose.error",
                                   taskModel.getName(),
                                   taskModel.getLocation(),
                                   throwable.getMessage() );
                throw new TaskException( message, throwable );
            }
        }
    }

    protected final void doContextualize( final Task task,
                                          final Configuration taskModel,
                                          final TaskContext context )
        throws TaskException
    {
        try
        {
            if( task instanceof Contextualizable )
            {
                ((Contextualizable)task).contextualize( context );
            }
        }
        catch( final Throwable throwable )
        {
            final String message =
                REZ.getString( "compose.error",
                               taskModel.getName(),
                               taskModel.getLocation(),
                               throwable.getMessage() );
            throw new TaskException( message, throwable );
        }
    }

    protected final void doDispose( final Task task, final Configuration taskModel )
        throws TaskException
    {
        if( task instanceof Disposable )
        {
            try { ((Disposable)task).dispose(); }
            catch( final Throwable throwable )
            {
                final String message =
                    REZ.getString( "dispose.error",
                                   taskModel.getName(),
                                   taskModel.getLocation(),
                                   throwable.getMessage() );
                throw new TaskException( message, throwable );
            }
        }
    }

    protected final void doLoggable( final Task task,
                                     final Configuration taskModel,
                                     final Logger logger )
        throws TaskException
    {
        if( task instanceof Loggable )
        {
            try { ((Loggable)task).setLogger( logger ); }
            catch( final Throwable throwable )
            {
                final String message =
                    REZ.getString( "logger.error",
                                   taskModel.getName(),
                                   taskModel.getLocation(),
                                   throwable.getMessage() );
                throw new TaskException( message, throwable );
            }
        }
    }

    protected final void doInitialize( final Task task, final Configuration taskModel )
        throws TaskException
    {
        if( task instanceof Initializable )
        {
            try { ((Initializable)task).initialize(); }
            catch( final Throwable throwable )
            {
                final String message =
                    REZ.getString( "init.error",
                                   taskModel.getName(),
                                   taskModel.getLocation(),
                                   throwable.getMessage() );
                throw new TaskException( message, throwable );
            }
        }
    }
}
