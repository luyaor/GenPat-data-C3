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
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ---------------------
 * DirectedSubgraph.java
 * ---------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org._3pq.jgrapht.graph;

import java.util.Set;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;

/**
 * A directed graph that is a subgraph on other graph.
 *
 * @see org._3pq.jgrapht.graph.Subgraph
 */
public class DirectedSubgraph<V, E extends Edge<V>> extends Subgraph<V, E> implements DirectedGraph<V, E> {
    private static final long serialVersionUID = 3616445700507054133L;

    /**
     * Creates a new directed subgraph.
     *
     * @param base the base (backing) graph on which the subgraph will be
     *        based.
     * @param vertexSubset vertices to include in the subgraph. If
     *        <code>null</code> then all vertices are included.
     * @param edgeSubset edges to in include in the subgraph. If
     *        <code>null</code> then all the edges whose vertices found in the
     *        graph are included.
     */
    public DirectedSubgraph( DirectedGraph<V, E> base, Set<V> vertexSubset,
        Set<E> edgeSubset ) {
        super( base, vertexSubset, edgeSubset );
    }
}
