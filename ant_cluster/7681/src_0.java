/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.myrmidon.components.executor;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
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
import org.apache.myrmidon.components.configurer.Configurer;
import org.apache.myrmidon.components.type.TypeManager;

public class DefaultExecutor
    extends AbstractLoggable
    implements Executor, Composable
{
    private Configurer           m_configurer;
    private ComponentSelector    m_selector;

    private ComponentManager     m_componentManager;

    /**
     * Retrieve relevent services needed to deploy.
     *
     * @param componentManager the ComponentManager
     * @exception ComponentException if an error occurs
     */
    public void compose( final ComponentManager componentManager )
        throws ComponentException
    {
        //cache CM so it can be used while executing tasks
        m_componentManager = componentManager;

        m_configurer = (Configurer)componentManager.lookup( Configurer.ROLE );

        final TypeManager typeManager = (TypeManager)componentManager.lookup( TypeManager.ROLE );
        m_selector = (ComponentSelector)typeManager.lookup( Task.ROLE + "Selector" );
    }

    public void execute( final Configuration taskData, final TaskContext context )
        throws TaskException
    {
        getLogger().debug( "Creating" );
        final Task task = createTask( taskData.getName() );
        setupLogger( task );

        getLogger().debug( "Contextualizing" );
        doContextualize( task, taskData, context );

        getLogger().debug( "Composing" );
        doCompose( task, taskData );

        getLogger().debug( "Configuring" );
        doConfigure( task, taskData, context );

        getLogger().debug( "Initializing" );
        doInitialize( task, taskData );

        getLogger().debug( "Running" );

        task.execute();

        getLogger().debug( "Disposing" );
        doDispose( task, taskData );
    }

    private Task createTask( final String name )
        throws TaskException
    {
        try
        {
            return (Task)m_selector.select( name );
        }
        catch( final ComponentException ce )
        {
            throw new TaskException( "Unable to create task " + name, ce );
        }
    }

    private void doConfigure( final Task task,
                              final Configuration taskData,
                              final TaskContext context )
        throws TaskException
    {
        try { m_configurer.configure( task, taskData, context ); }
        catch( final Throwable throwable )
        {
            throw new TaskException( "Error configuring task " +  taskData.getName() + " at " +
                                     taskData.getLocation() + "(Reason: " +
                                     throwable.getMessage() + ")", throwable );
        }
    }

    private void doCompose( final Task task, final Configuration taskData )
        throws TaskException
    {
        if( task instanceof Composable )
        {
            try { ((Composable)task).compose( m_componentManager ); }
            catch( final Throwable throwable )
            {
                throw new TaskException( "Error composing task " +  taskData.getName() + " at " +
                                         taskData.getLocation() + "(Reason: " +
                                         throwable.getMessage() + ")", throwable );
            }
        }
    }

    private void doContextualize( final Task task,
                                  final Configuration taskData,
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
            throw new TaskException( "Error contextualizing task " +  taskData.getName() + " at " +
                                     taskData.getLocation() + "(Reason: " +
                                     throwable.getMessage() + ")", throwable );
        }
    }

    private void doDispose( final Task task, final Configuration taskData )
        throws TaskException
    {
        if( task instanceof Disposable )
        {
            try { ((Disposable)task).dispose(); }
            catch( final Throwable throwable )
            {
                throw new TaskException( "Error disposing task " +  taskData.getName() + " at " +
                                         taskData.getLocation() + "(Reason: " +
                                         throwable.getMessage() + ")", throwable );
            }
        }
    }

    private void doInitialize( final Task task, final Configuration taskData )
        throws TaskException
    {
        if( task instanceof Initializable )
        {
            try { ((Initializable)task).initialize(); }
            catch( final Throwable throwable )
            {
                throw new TaskException( "Error initializing task " +  taskData.getName() + " at " +
                                         taskData.getLocation() + "(Reason: " +
                                         throwable.getMessage() + ")", throwable );
            }
        }
    }
}
