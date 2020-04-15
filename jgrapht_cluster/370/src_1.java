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
/* ------------------------------
 * DijkstraShortestPathTest.java
 * ------------------------------
 * (C) Copyright 2003, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id: DijkstraShortestPathTest.java,v 1.4 2005/05/30 05:37:29 perfecthash Exp
 * $
 *
 * Changes
 * -------
 * 03-Sept-2003 : Initial revision (JVS);
 *
 */
package org._3pq.jgrapht.alg;

import java.util.*;

import junit.framework.*;

import org._3pq.jgrapht.*;
import org._3pq.jgrapht.graph.*;


/**
 * .
 *
 * @author John V. Sichi
 */
public class DijkstraShortestPathTest extends TestCase
{

    //~ Static fields/initializers --------------------------------------------

    static final String V1 = "v1";
    static final String V2 = "v2";
    static final String V3 = "v3";
    static final String V4 = "v4";
    static final String V5 = "v5";

    //~ Instance fields -------------------------------------------------------

    Edge m_e12;
    Edge m_e13;
    Edge m_e15;
    Edge m_e24;
    Edge m_e34;
    Edge m_e45;

    //~ Methods ---------------------------------------------------------------

    /**
     * .
     */
    public void testConstructor()
    {
        DijkstraShortestPath path;
        Graph g = create();

        path = new DijkstraShortestPath(g, V3, V4, Double.POSITIVE_INFINITY);
        assertEquals(
            Arrays.asList(new Edge [] {
                    m_e13, m_e12, m_e24
                }),
            path.getPathEdgeList());
        assertEquals(10.0, path.getPathLength(), 0);

        path = new DijkstraShortestPath(g, V3, V4, 7);
        assertNull(path.getPathEdgeList());
        assertEquals(Double.POSITIVE_INFINITY, path.getPathLength(), 0);
    }

    /**
     * .
     */
    public void testPathBetween()
    {
        List path;
        Graph g = create();

        path = DijkstraShortestPath.findPathBetween(g, V1, V2);
        assertEquals(Arrays.asList(new Edge [] { m_e12 }), path);

        path = DijkstraShortestPath.findPathBetween(g, V1, V4);
        assertEquals(Arrays.asList(new Edge [] {
                    m_e12, m_e24
                }), path);

        path = DijkstraShortestPath.findPathBetween(g, V1, V5);
        assertEquals(Arrays.asList(new Edge [] {
                    m_e12, m_e24, m_e45
                }), path);

        path = DijkstraShortestPath.findPathBetween(g, V3, V4);
        assertEquals(Arrays.asList(new Edge [] {
                    m_e13, m_e12, m_e24
                }), path);
    }

    private Graph create()
    {
        Graph g = new SimpleWeightedGraph();

        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);

        m_e12 = g.addEdge(V1, V2);
        m_e12.setWeight(2);

        m_e13 = g.addEdge(V1, V3);
        m_e13.setWeight(3);

        m_e24 = g.addEdge(V2, V4);
        m_e24.setWeight(5);

        m_e34 = g.addEdge(V3, V4);
        m_e34.setWeight(20);

        m_e45 = g.addEdge(V4, V5);
        m_e45.setWeight(5);

        m_e15 = g.addEdge(V1, V5);
        m_e15.setWeight(100);

        return g;
    }
}
