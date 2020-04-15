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
/* -------------------------
 * BreadthFirstIterator.java
 * -------------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Liviu Rau
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 *
 */
package org._3pq.jgrapht.alg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.alg.AlgUtils.DirectedSpecifics;
import org._3pq.jgrapht.alg.AlgUtils.SimpleQueue;
import org._3pq.jgrapht.alg.AlgUtils.Specifics;
import org._3pq.jgrapht.alg.AlgUtils.UndirectedSpecifics;

/**
 * A breadth-first iterator for a directed and an undirected graph. For this
 * iterator to work correctly the graph must not be modified during iteration.
 * Currently there are no means to ensure that, nor to fail-fast. The result
 * of such modifications are undefined.
 *
 * @author Barak Naveh
 *
 * @since Jul 19, 2003
 */
public class BreadthFirstIterator extends AbstractGraphIterator {
    // todo: support ConcurrentModificationException if graph modified during iteration. 
    private Iterator    m_vertexIterator = null;
    private Set         m_seen      = new HashSet(  );
    private SimpleQueue m_queue     = new SimpleQueue(  );
    private Specifics   m_specifics;

    /**
     * Creates a new BreadthFirstIterator object.
     *
     * @param g the directed graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public BreadthFirstIterator( DirectedGraph g, Object startVertex ) {
        super(  );
        init( g, startVertex );
        m_specifics = new DirectedSpecifics( g );
    }


    /**
     * Creates a new BreadthFirstIterator object.
     *
     * @param g the undirected graph to be iterated.
     * @param startVertex the vertex iteration to be started.
     */
    public BreadthFirstIterator( UndirectedGraph g, Object startVertex ) {
        super(  );
        init( g, startVertex );
        m_specifics = new UndirectedSpecifics( g );
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext(  ) {
        if( m_queue.isEmpty(  ) ) {
            if( isCrossComponentTraversal(  ) ) {
                while( m_vertexIterator.hasNext(  ) ) {
                    Object v = m_vertexIterator.next(  );

                    if( !m_seen.contains( v ) ) {
                        m_seen.add( v );
                        m_queue.add( v );

                        return true;
                    }
                }

                return false;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }


    /**
     * @see java.util.Iterator#next()
     */
    public Object next(  ) {
        if( hasNext(  ) ) {
            Object nextVertex = m_queue.remove(  );
            fireVertexVisited( nextVertex );

            enqueueUnseenChildrenOf( nextVertex );

            return nextVertex;
        }
        else {
            throw new NoSuchElementException(  );
        }
    }


    private void enqueueUnseenChildrenOf( Object vertex ) {
        List edges = m_specifics.edgesOf( vertex );

        for( Iterator iter = edges.iterator(  ); iter.hasNext(  ); ) {
            Edge e = (Edge) iter.next(  );
            fireEdgeVisited( e );

            Object v = e.oppositeVertex( vertex );

            if( !m_seen.contains( v ) ) {
                m_seen.add( v );
                m_queue.add( v );
            }
        }
    }


    private void init( Graph g, Object startVertex ) {
        if( startVertex == null || g == null ) {
            throw new NullPointerException(  );
        }

        if( !g.containsVertex( startVertex ) ) {
            throw new IllegalArgumentException( "start vertex not in graph" );
        }

        m_seen.add( startVertex );
        m_queue.add( startVertex );
        m_vertexIterator = g.vertexSet(  ).iterator(  );
    }
}
