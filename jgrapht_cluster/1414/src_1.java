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
/* ------------------
 * CycleDetector.java
 * ------------------
 * (C) Copyright 2004, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 16-Sept-2004 : Initial revision (JVS);
 * 07-Jun-2005 : Made generic (CH);
 *
 */
package org._3pq.jgrapht.alg;

import java.util.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.traverse.*;


/**
 * Performs cycle detection on a graph. The <i>inspected graph</i> is specified
 * at construction time and cannot be modified. Currently, the detector
 * supports only directed graphs.
 *
 * @author John V. Sichi
 * @since Sept 16, 2004
 */
public class CycleDetector<V, E extends Edge<V>>
{

    //~ Instance fields -------------------------------------------------------

    /**
     * Graph on which cycle detection is being performed.
     */
    Graph<V, E> m_graph;

    //~ Constructors ----------------------------------------------------------

    /**
     * Creates a cycle detector for the specified graph.  Currently only
     * directed graphs are supported.
     *
     * @param graph the DirectedGraph in which to detect cycles
     */
    @SuppressWarnings("unchecked")    // FIXME hb 28-Nov-05: Don't know how to solve this, yet
	public <EE extends E & DirEdge<V>> CycleDetector(DirectedGraph<V, EE> graph)
    {
        m_graph = (Graph<V,E>)graph;
    }

    //~ Methods ---------------------------------------------------------------

    /**
     * Performs yes/no cycle detection on the entire graph.
     *
     * @return true iff the graph contains at least one cycle
     */
    public boolean detectCycles()
    {
        try {
            execute(null, null);
        } catch (CycleDetectedException ex) {
            return true;
        }

        return false;
    }

    /**
     * Performs yes/no cycle detection on an individual vertex.
     *
     * @param v the vertex to test
     *
     * @return true if v is on at least one cycle
     */
    public boolean detectCyclesContainingVertex(V v)
    {
        try {
            execute(null, v);
        } catch (CycleDetectedException ex) {
            return true;
        }

        return false;
    }

    /**
     * Finds the vertex set for the subgraph of all cycles.
     *
     * @return set of all vertices which participate in at least one cycle in
     *         this graph
     */
    public Set<V> findCycles()
    {
        Set<V> set = new HashSet<V>();
        execute(set, null);

        return set;
    }

    /**
     * Finds the vertex set for the subgraph of all cycles which contain a
     * particular vertex.
     *
     * @param v the vertex to test
     *
     * @return set of all vertices reachable from v via at least one cycle
     */
    public Set<V> findCyclesContainingVertex(V v)
    {
        Set<V> set = new HashSet<V>();
        execute(set, v);

        return set;
    }

    private void execute(Set<V> s, V v)
    {
        ProbeIterator iter = new ProbeIterator(s, v);

        while (iter.hasNext()) {
            iter.next();
        }
    }

    //~ Inner Classes ---------------------------------------------------------

    /**
     * Exception thrown internally when a cycle is detected during a yes/no
     * cycle test.  Must be caught by top-level detection method.
     */
    private static class CycleDetectedException extends RuntimeException
    {
        private static final long serialVersionUID = 3834305137802950712L;
    }

    /**
     * Version of DFS which maintains a backtracking path used to probe for
     * cycles.
     */
    private class ProbeIterator extends DepthFirstIterator<V, E, Object>
    {
        private List<V> m_path;
        private Set<V> m_cycleSet;

        ProbeIterator(Set<V> cycleSet, V startVertex)
        {
            super(m_graph, startVertex);
            m_cycleSet = cycleSet;
            m_path = new ArrayList<V>();
        }

        /**
         * {@inheritDoc}
         */
        protected void encounterVertexAgain(V vertex, E edge)
        {
            super.encounterVertexAgain(vertex, edge);

            int i = m_path.indexOf(vertex);

            if (i > -1) {
                if (m_cycleSet == null) {
                    // we're doing yes/no cycle detection
                    throw new CycleDetectedException();
                }

                for (; i < m_path.size(); ++i) {
                    m_cycleSet.add(m_path.get(i));
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        protected V provideNextVertex()
        {
            V v = super.provideNextVertex();

            // backtrack
            for (int i = m_path.size() - 1; i >= 0; --i) {
                if (m_graph.containsEdge(m_path.get(i), v)) {
                    break;
                }

                m_path.remove(i);
            }

            m_path.add(v);

            return v;
        }
    }
}
