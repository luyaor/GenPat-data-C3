/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Lead:  Barak Naveh (barak_naveh@users.sourceforge.net)
 *
 * (C) Copyright 2003, by Barak Naveh and Contributors.
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
/* -----------------
 * VertexCovers.java
 * -----------------
 * (C) Copyright 2003, by Linda Buisman and Contributors.
 *
 * Original Author:  Linda Buisman
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Nov-2003 : Initial revision (LB);
 *
 */
package org._3pq.jgrapht.alg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.alg.util.VertexDegreeComparator;
import org._3pq.jgrapht.graph.AsUndirectedGraph;
import org._3pq.jgrapht.graph.Subgraph;
import org._3pq.jgrapht.graph.UndirectedSubgraph;

/**
 * Algorithms to find a vertex cover for a graph. A vertex cover is a set of
 * vertices that touches all the edges in the graph. The graph's vertex set is
 * a trivial cover. However, a <i>minimal</i> vertex set (or at least an
 * approximation for it) is usually desired. Finding a true minimal vertex
 * cover is an NP-Complete problem. For more on the vertex cover problem, see
 * <a href="http://mathworld.wolfram.com/VertexCover.html">
 * http://mathworld.wolfram.com/VertexCover.html</a>
 *
 * @author Linda Buisman
 *
 * @since Nov 6, 2003
 */
public class VertexCovers {
    /**
     * A greedy approximation algorithm for Vertex Cover on a specified graph.
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a vertex cover for the specified
     *         graph.
     */
    public Set findGreedyCover( Graph g ) {
        // C <-- �
        Set cover = new HashSet(  );

        // G' <-- G
        Subgraph sg =
            new UndirectedSubgraph( undirectedGraph( g ), null, null );

        // compare vertices in descending order of degree
        VertexDegreeComparator comp = new VertexDegreeComparator( sg );

        // while G' != �
        while( sg.edgeSet(  ).size(  ) > 0 ) {
            // v <-- vertex with maximum degree in G'
            Object v = Collections.max( sg.vertexSet(  ), comp );

            // C <-- C U {v}
            cover.add( v );

            // remove from G' every edge incident on v, and v itself
            sg.removeVertex( v );
        }

        return cover;
    }


    /**
     * Implements a p-time 2-approximation algorithm for Vertex Cover on the
     * specified graph.
     * 
     * <p>
     * This algorithm is due to Jenny Walter, CMPU-240: Lecture notes for
     * Language Theory and Computation, Fall 2002, Vassar College, <a
     * href="http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf">
     * 
     * http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf</a>.
     * </p>
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a vertex cover for the specified
     *         graph.
     */
    public Set findWalterCover( Graph g ) {
        // C <-- �
        Set cover = new HashSet(  );

        // G'=(V',E') <-- G(V,E)
        Subgraph sg = new Subgraph( undirectedGraph( g ), null, null );

        // while E' is non-empty
        while( sg.edgeSet(  ).size(  ) > 0 ) {
            // let (u,v) be an arbitrary edge of E'
            Edge e = (Edge) sg.edgeSet(  ).iterator(  ).next(  );

            // C <-- C U {u,v}
            Object u = e.getSource(  );
            Object v = e.getTarget(  );
            cover.add( u );
            cover.add( v );

            // remove from E' every edge incident on either u or v
            sg.removeVertex( u );
            sg.removeVertex( v );
        }

        return cover; // return C
    }


    /**
     * Returns an undirected version of the specified graph.
     *
     * @param g
     *
     * @return
     *
     * @throws IllegalArgumentException
     */
    private UndirectedGraph undirectedGraph( Graph g ) {
        // TODO: should move this method to GraphHelper.
        if( g instanceof DirectedGraph ) {
            return new AsUndirectedGraph( (DirectedGraph) g );
        }
        else if( g instanceof UndirectedGraph ) {
            return (UndirectedGraph) g;
        }
        else {
            throw new IllegalArgumentException( 
                "Graph must be either DirectedGraph or UndirectedGraph" );
        }
    }
}
