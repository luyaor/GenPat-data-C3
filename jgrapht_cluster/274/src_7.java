/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (barak_naveh@users.sourceforge.net)
 *
 * (C) Copyright 2003-2006, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* --------------------------
 * AbstractGraphIterator.java
 * --------------------------
 * (C) Copyright 2003-2006, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Aug-2003 : Adaptation to new event model (BN);
 * 04-May-2004 : Made generic (CH)
 *
 */
package org.jgrapht.traverse;

import java.util.*;

import org.jgrapht.event.*;


/**
 * An empty implementation of a graph iterator to minimize the effort required
 * to implement graph iterators.
 *
 * @author Barak Naveh
 * @since Jul 19, 2003
 */
public abstract class AbstractGraphIterator<V, E>
    implements GraphIterator<V, E>
{

    //~ Instance fields -------------------------------------------------------

    private List<TraversalListener<V, E>> m_traversalListeners =
        new ArrayList<TraversalListener<V, E>>();
    private boolean m_crossComponentTraversal = true;
    private boolean m_reuseEvents = false;

    //~ Methods ---------------------------------------------------------------

    /**
     * Sets the cross component traversal flag - indicates whether to traverse
     * the graph across connected components.
     *
     * @param crossComponentTraversal if <code>true</code> traverses across
     *                                connected components.
     */
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        m_crossComponentTraversal = crossComponentTraversal;
    }

    /**
     * Test whether this iterator is set to traverse the graph across connected
     * components.
     *
     * @return <code>true</code> if traverses across connected components,
     *         otherwise <code>false</code>.
     */
    public boolean isCrossComponentTraversal()
    {
        return m_crossComponentTraversal;
    }

    /**
     * @see GraphIterator#setReuseEvents(boolean)
     */
    public void setReuseEvents(boolean reuseEvents)
    {
        m_reuseEvents = reuseEvents;
    }

    /**
     * @see GraphIterator#isReuseEvents()
     */
    public boolean isReuseEvents()
    {
        return m_reuseEvents;
    }

    /**
     * Adds the specified traversal listener to this iterator.
     *
     * @param l the traversal listener to be added.
     */
    public void addTraversalListener(TraversalListener<V, E> l)
    {
        if (!m_traversalListeners.contains(l)) {
            m_traversalListeners.add(l);
        }
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes the specified traversal listener from this iterator.
     *
     * @param l the traversal listener to be removed.
     */
    public void removeTraversalListener(TraversalListener<V, E> l)
    {
        m_traversalListeners.remove(l);
    }

    /**
     * Informs all listeners that the traversal of the current connected
     * component finished.
     *
     * @param e the connected component finished event.
     */
    protected void fireConnectedComponentFinished(
        ConnectedComponentTraversalEvent e)
    {
        int len = m_traversalListeners.size();

        for (int i = 0; i < len; i++) {
            TraversalListener l = m_traversalListeners.get(i);
            l.connectedComponentFinished(e);
        }
    }

    /**
     * Informs all listeners that a traversal of a new connected component has
     * started.
     *
     * @param e the connected component started event.
     */
    protected void fireConnectedComponentStarted(
        ConnectedComponentTraversalEvent e)
    {
        int len = m_traversalListeners.size();

        for (int i = 0; i < len; i++) {
            TraversalListener l = m_traversalListeners.get(i);
            l.connectedComponentStarted(e);
        }
    }

    /**
     * Informs all listeners that a the specified edge was visited.
     *
     * @param e the edge traversal event.
     */
    protected void fireEdgeTraversed(EdgeTraversalEvent<V, E> e)
    {
        int len = m_traversalListeners.size();

        for (int i = 0; i < len; i++) {
            TraversalListener<V, E> l = m_traversalListeners.get(i);
            l.edgeTraversed(e);
        }
    }

    /**
     * Informs all listeners that a the specified vertex was visited.
     *
     * @param e the vertex traversal event.
     */
    protected void fireVertexTraversed(VertexTraversalEvent<V> e)
    {
        int len = m_traversalListeners.size();

        for (int i = 0; i < len; i++) {
            TraversalListener<V, E> l = m_traversalListeners.get(i);
            l.vertexTraversed(e);
        }
    }
}
