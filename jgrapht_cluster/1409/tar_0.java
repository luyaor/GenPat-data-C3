/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Lead:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2004, by Barak Naveh and Contributors.
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
/* ---------------------------
 * DefaultListenableGraph.java
 * ---------------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: DefaultListenableGraph.java,v 1.15 2005/09/05 04:00:57 perfecthash Exp
 * $
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 04-Aug-2003 : Strong refs to listeners instead of weak refs (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 07-Mar-2004 : Fixed unnecessary clone bug #819075 (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org._3pq.jgrapht.graph;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.event.*;


/**
 * A graph backed by the the graph specified at the constructor, which can be
 * listened by <code>GraphListener</code> s and by <code>
 * VertexSetListener</code> s. Operations on this graph "pass through" to the
 * to the backing graph. Any modification made to this graph or the backing
 * graph is reflected by the other.
 *
 * <p>This graph does <i>not</i> pass the hashCode and equals operations
 * through to the backing graph, but relies on <tt>Object</tt>'s <tt>
 * equals</tt> and <tt>hashCode</tt> methods.</p>
 *
 * @author Barak Naveh
 * @see GraphListener
 * @see VertexSetListener
 * @since Jul 20, 2003
 */
public class DefaultListenableGraph<V, E extends Edge<V>>
    extends GraphDelegator<V, E> implements ListenableGraph<V, E>, Cloneable
{

    //~ Static fields/initializers --------------------------------------------

    private static final long serialVersionUID = 3977575900898471984L;

    //~ Instance fields -------------------------------------------------------

    private ArrayList<GraphListener<V, E>> m_graphListeners = new ArrayList<GraphListener<V, E>>();
    private ArrayList<VertexSetListener<V>> m_vertexSetListeners =
        new ArrayList<VertexSetListener<V>>();
    private FlyweightEdgeEvent<V,E> m_reuseableEdgeEvent;
    private FlyweightVertexEvent<V> m_reuseableVertexEvent;
    private boolean m_reuseEvents;

    //~ Constructors ----------------------------------------------------------

    /**
     * Creates a new listenable graph.
     *
     * @param g the backing graph.
     */
    public DefaultListenableGraph(Graph<V, E> g)
    {
        this(g, false);
    }

    /**
     * Creates a new listenable graph. If the <code>reuseEvents</code> flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @param g the backing graph.
     * @param reuseEvents whether to reuse previously fired event objects
     *                    instead of creating a new event object for each
     *                    event.
     *
     * @throws IllegalArgumentException if the backing graph is already a
     *                                  listenable graph.
     */
    public DefaultListenableGraph(Graph<V, E> g, boolean reuseEvents)
    {
        super(g);
        m_reuseEvents = reuseEvents;
        m_reuseableEdgeEvent = new FlyweightEdgeEvent<V,E>(this, -1, null);
        m_reuseableVertexEvent = new FlyweightVertexEvent<V>(this, -1, null);

        // the following restriction could be probably relaxed in the future.
        if (g instanceof ListenableGraph) {
            throw new IllegalArgumentException(
                "base graph cannot be listenable");
        }
    }

    //~ Methods ---------------------------------------------------------------

    /**
     * If the <code>reuseEvents</code> flag is set to <code>true</code> this
     * class will reuse previously fired events and will not create a new
     * object for each event. This option increases performance but should be
     * used with care, especially in multithreaded environment.
     *
     * @param reuseEvents whether to reuse previously fired event objects
     *                    instead of creating a new event object for each
     *                    event.
     */
    public void setReuseEvents(boolean reuseEvents)
    {
        m_reuseEvents = reuseEvents;
    }

    /**
     * Tests whether the <code>reuseEvents</code> flag is set. If the flag is
     * set to <code>true</code> this class will reuse previously fired events
     * and will not create a new object for each event. This option increases
     * performance but should be used with care, especially in multithreaded
     * environment.
     *
     * @return the value of the <code>reuseEvents</code> flag.
     */
    public boolean isReuseEvents()
    {
        return m_reuseEvents;
    }

    /**
     * @see Graph#addEdge(Object, Object)
     */
    public E addEdge(V sourceVertex, V targetVertex)
    {
        E e = super.addEdge(sourceVertex, targetVertex);

        if (e != null) {
            fireEdgeAdded(e);
        }

        return e;
    }

    /**
     * @see Graph#addEdge(Edge)
     */
    public boolean addEdge(E e)
    {
        boolean modified = super.addEdge(e);

        if (modified) {
            fireEdgeAdded(e);
        }

        return modified;
    }

    /**
     * @see ListenableGraph#addGraphListener(GraphListener)
     */
    public void addGraphListener(GraphListener<V, E> l)
    {
        addToListenerList(m_graphListeners, l);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    public boolean addVertex(V v)
    {
        boolean modified = super.addVertex(v);

        if (modified) {
            fireVertexAdded(v);
        }

        return modified;
    }

    /**
     * @see ListenableGraph#addVertexSetListener(VertexSetListener)
     */
    public void addVertexSetListener(VertexSetListener<V> l)
    {
        addToListenerList(m_vertexSetListeners, l);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        try {
            DefaultListenableGraph<V,E> g = (DefaultListenableGraph) super.clone();
            g.m_graphListeners = new ArrayList<GraphListener<V, E>>();
            g.m_vertexSetListeners = new ArrayList<VertexSetListener<V>>();

            return g;
        } catch (CloneNotSupportedException e) {
            // should never get here since we're Cloneable
            e.printStackTrace();
            throw new RuntimeException("internal error");
        }
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = super.removeEdge(sourceVertex, targetVertex);

        if (e != null) {
            fireEdgeRemoved(e);
        }

        return e;
    }

    /**
     * @see Graph#removeEdge(Edge)
     */
    public boolean removeEdge(E e)
    {
        boolean modified = super.removeEdge(e);

        if (modified) {
            fireEdgeRemoved(e);
        }

        return modified;
    }

    /**
     * @see ListenableGraph#removeGraphListener(GraphListener)
     */
    public void removeGraphListener(GraphListener<V, E> l)
    {
        m_graphListeners.remove(l);
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    public boolean removeVertex(V v)
    {
        if (containsVertex(v)) {
            List<E> touchingEdgesList = edgesOf(v);

            // cannot iterate over list - will cause
            // ConcurrentModificationException
            // Edge[] touchingEdges = new Edge[ touchingEdgesList.size(  ) ];
            // touchingEdgesList.toArray( touchingEdges );

            removeAllEdges(new ArrayList<E>(touchingEdgesList));

            super.removeVertex(v); // remove the vertex itself

            fireVertexRemoved(v);

            return true;
        } else {
            return false;
        }
    }

    /**
     * @see ListenableGraph#removeVertexSetListener(VertexSetListener)
     */
    public void removeVertexSetListener(VertexSetListener<V> l)
    {
        m_vertexSetListeners.remove(l);
    }

    /**
     * Notify listeners that the specified edge was added.
     *
     * @param edge the edge that was added.
     */
    protected void fireEdgeAdded(E edge)
    {
        GraphEdgeChangeEvent<V, E> e =
            createGraphEdgeChangeEvent(GraphEdgeChangeEvent.EDGE_ADDED, edge);

        for (int i = 0; i < m_graphListeners.size(); i++) {
            GraphListener<V, E> l = m_graphListeners.get(i);

            l.edgeAdded(e);
        }
    }

    /**
     * Notify listeners that the specified edge was removed.
     *
     * @param edge the edge that was removed.
     */
    protected void fireEdgeRemoved(E edge)
    {
        GraphEdgeChangeEvent<V, E> e =
            createGraphEdgeChangeEvent(
                GraphEdgeChangeEvent.EDGE_REMOVED,
                edge);

        for (int i = 0; i < m_graphListeners.size(); i++) {
            GraphListener<V,E> l = m_graphListeners.get(i);

            l.edgeRemoved(e);
        }
    }

    /**
     * Notify listeners that the specified vertex was added.
     *
     * @param vertex the vertex that was added.
     */
    protected void fireVertexAdded(V vertex)
    {
        GraphVertexChangeEvent<V> e =
            createGraphVertexChangeEvent(
                GraphVertexChangeEvent.VERTEX_ADDED,
                vertex);

        for (int i = 0; i < m_vertexSetListeners.size(); i++) {
            VertexSetListener<V> l = m_vertexSetListeners.get(i);

            l.vertexAdded(e);
        }

        for (int i = 0; i < m_graphListeners.size(); i++) {
            GraphListener<V, E> l = m_graphListeners.get(i);

            l.vertexAdded(e);
        }
    }

    /**
     * Notify listeners that the specified vertex was removed.
     *
     * @param vertex the vertex that was removed.
     */
    protected void fireVertexRemoved(V vertex)
    {
        GraphVertexChangeEvent<V> e =
            createGraphVertexChangeEvent(
                GraphVertexChangeEvent.VERTEX_REMOVED,
                vertex);

        for (int i = 0; i < m_vertexSetListeners.size(); i++) {
            VertexSetListener<V> l = m_vertexSetListeners.get(i);

            l.vertexRemoved(e);
        }

        for (int i = 0; i < m_graphListeners.size(); i++) {
            GraphListener<V, E> l = m_graphListeners.get(i);

            l.vertexRemoved(e);
        }
    }

    private static <L extends EventListener> void addToListenerList(List<L> list,
        L l)
    {
        if (!list.contains(l)) {
            list.add(l);
        }
    }

    private GraphEdgeChangeEvent<V, E> createGraphEdgeChangeEvent(
        int eventType,
        E edge)
    {
        if (m_reuseEvents) {
            m_reuseableEdgeEvent.setType(eventType);
            m_reuseableEdgeEvent.setEdge(edge);

            return m_reuseableEdgeEvent;
        } else {
            return new GraphEdgeChangeEvent<V,E>(this, eventType, edge);
        }
    }

    private GraphVertexChangeEvent<V> createGraphVertexChangeEvent(
        int eventType,
        V vertex)
    {
        if (m_reuseEvents) {
            m_reuseableVertexEvent.setType(eventType);
            m_reuseableVertexEvent.setVertex(vertex);

            return m_reuseableVertexEvent;
        } else {
            return new GraphVertexChangeEvent<V>(this, eventType, vertex);
        }
    }

    //~ Inner Classes ---------------------------------------------------------

    /**
     * A reuseable edge event.
     *
     * @author Barak Naveh
     * @since Aug 10, 2003
     */
    private static class FlyweightEdgeEvent<VV, EE extends Edge<VV>>
        extends GraphEdgeChangeEvent<VV, EE>
    {
        private static final long serialVersionUID = 3907207152526636089L;

        /**
         * @see GraphEdgeChangeEvent#GraphEdgeChangeEvent(Object, int, Edge)
         */
        public FlyweightEdgeEvent(Object eventSource, int type, EE e)
        {
            super(eventSource, type, e);
        }

        /**
         * Sets the edge of this event.
         *
         * @param e the edge to be set.
         */
        protected void setEdge(EE e)
        {
            m_edge = e;
        }

        /**
         * Set the event type of this event.
         *
         * @param type the type to be set.
         */
        protected void setType(int type)
        {
            m_type = type;
        }
    }

    /**
     * A reuseable vertex event.
     *
     * @author Barak Naveh
     * @since Aug 10, 2003
     */
    private static class FlyweightVertexEvent<VV>
        extends GraphVertexChangeEvent<VV>
    {
        private static final long serialVersionUID = 3257848787857585716L;

        /**
         * @see GraphVertexChangeEvent#GraphVertexChangeEvent(Object, int,
         *      Object)
         */
        public FlyweightVertexEvent(Object eventSource, int type, VV vertex)
        {
            super(eventSource, type, vertex);
        }

        /**
         * Set the event type of this event.
         *
         * @param type type to be set.
         */
        protected void setType(int type)
        {
            m_type = type;
        }

        /**
         * Sets the vertex of this event.
         *
         * @param vertex the vertex to be set.
         */
        protected void setVertex(VV vertex)
        {
            m_vertex = vertex;
        }
    }
}
