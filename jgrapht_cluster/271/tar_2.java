/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* --------------
 * EqualsTest.java
 * --------------
 * (C) Copyright 2012, by Vladimir Kostyukov and Contributors.
 *
 * Original Author:  Vladimir Kostyukov
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 22-May-2012 : Initial revision (VK);
 *
 */

package org.jgrapht.graph;

import org.jgrapht.DirectedGraph;
import org.jgrapht.EnhancedTestCase;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.WeightedGraph;

public class EqualsTest
    extends EnhancedTestCase
{
    //~ Instance fields --------------------------------------------------------

    private String v1 = "v1";
    private String v2 = "v2";
    private String v3 = "v3";
    private String v4 = "v4";

    //~ Constructors -----------------------------------------------------------

    /**
     * @see junit.framework.TestCase#TestCase(java.lang.String)
     */
    public EqualsTest(String name)
    {
        super(name);
    }

    /**
     * Tests equals() method of DefaultDirectedGraph.
     */
    public void testDefaultDirectedGraph()
    {
        DirectedGraph<String, DefaultEdge> g1 =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        g1.addEdge(v1, v2);
        g1.addEdge(v2, v3);
        g1.addEdge(v3, v1);

        DirectedGraph<String, DefaultEdge> g2 = 
             new DefaultDirectedGraph<String, DefaultEdge>(
                 DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1);
        g2.addEdge(v2, v3);
        g2.addEdge(v1, v2);

        DirectedGraph<String, DefaultEdge> g3 = 
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
       g3.addVertex(v4);
       g3.addVertex(v3);
       g3.addVertex(v2);
       g3.addVertex(v1);
       g3.addEdge(v3, v1);
       g3.addEdge(v2, v3);

       assertTrue(g2.equals(g1));
       assertTrue(!g3.equals(g2));
    }

    /**
     * Tests equals() method of SimpleGraph.
     */
    public void testSimpleGraph()
    {
        UndirectedGraph<String, DefaultEdge> g1 =
            new SimpleGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        g1.addEdge(v1, v2);
        g1.addEdge(v2, v3);
        g1.addEdge(v3, v1);

        UndirectedGraph<String, DefaultEdge> g2 = 
             new SimpleGraph<String, DefaultEdge>(
                 DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1);
        g2.addEdge(v2, v3);
        g2.addEdge(v1, v2);

        UndirectedGraph<String, DefaultEdge> g3 = 
            new SimpleGraph<String, DefaultEdge>(
                DefaultEdge.class);
       g3.addVertex(v4);
       g3.addVertex(v3);
       g3.addVertex(v2);
       g3.addVertex(v1);
       g3.addEdge(v3, v1);
       g3.addEdge(v2, v3);

        assertTrue(g2.equals(g1));
        assertTrue(!g3.equals(g2));
    }

    /**
     * Tests equals() method for different graphs.
     */
    public void testDifferentGraphs()
    {
        DirectedGraph<String, DefaultEdge> g1 =
            new DefaultDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addVertex(v4);
        g1.addEdge(v1, v2);
        g1.addEdge(v2, v3);
        g1.addEdge(v3, v1);

        UndirectedGraph<String, DefaultEdge> g2 = 
             new SimpleGraph<String, DefaultEdge>(
                 DefaultEdge.class);
        g2.addVertex(v4);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1);
        g2.addEdge(v2, v3);
        g2.addEdge(v1, v2);

        assertTrue(!g2.equals(g1));
    }

    /**
     * Tests graph with non-Intrusive edges.
     */
    public void testGraphsWithNonIntrusiveEdge()
    {
        DirectedGraph<String, String> g1 =
            new DefaultDirectedGraph<String, String>(
                String.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2, v1 + v2);
        g1.addEdge(v3, v1, v3 + v1);

        DirectedGraph<String, String> g2 = 
             new DefaultDirectedGraph<String, String>(
                 String.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v3, v1, v3 + v1);
        g2.addEdge(v1, v2, v1 + v2);

        DirectedGraph<String, String> g3 = 
            new DefaultDirectedGraph<String, String>(
                String.class);
       g3.addVertex(v3);
       g3.addVertex(v2);
       g3.addVertex(v1);
       g3.addEdge(v3, v1, v3 + v1);
       g3.addEdge(v1, v2, v1 + v2);
       g3.addEdge(v2, v3, v2 + v3);

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));
    }

    /**
     * Tests pseudo graph.
     */
    public void testPseudograph() {
        UndirectedGraph<String, DefaultEdge> g1 =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2);
        g1.addEdge(v2, v3);
        g1.addEdge(v3, v1);
        g1.addEdge(v1, v2);
        g1.addEdge(v1, v1);

        UndirectedGraph<String, DefaultEdge> g2 =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        g2.addEdge(v1, v1);
        g2.addEdge(v1, v2);
        g2.addEdge(v3, v1);
        g2.addEdge(v2, v3);
        g2.addEdge(v1, v2);

        UndirectedGraph<String, DefaultEdge> g3 =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g3.addVertex(v3);
        g3.addVertex(v2);
        g3.addVertex(v1);
        g3.addEdge(v1, v1);
        g3.addEdge(v1, v2);
        g3.addEdge(v3, v1);
        g3.addEdge(v2, v3);

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));
    }

    /**
     * Tests weighted graph.
     */
    public void testWeightedGraph() {
        WeightedGraph<String, DefaultWeightedEdge> g1 =
            new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        DefaultWeightedEdge e112 = g1.addEdge(v1, v2);
        DefaultWeightedEdge e131 = g1.addEdge(v3, v1);
        g1.setEdgeWeight(e112, 10.0);
        g1.setEdgeWeight(e131, 20.0);

        WeightedGraph<String, DefaultWeightedEdge> g2 = 
             new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
                 DefaultWeightedEdge.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        DefaultWeightedEdge e231 = g2.addEdge(v3, v1);
        DefaultWeightedEdge e212 = g2.addEdge(v1, v2);
        g2.setEdgeWeight(e212, 10.0);
        g2.setEdgeWeight(e231, 20.0);

        WeightedGraph<String, DefaultWeightedEdge> g3 = 
            new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
       g3.addVertex(v3);
       g3.addVertex(v2);
       g3.addVertex(v1);
       DefaultWeightedEdge e331 = g3.addEdge(v3, v1);
       DefaultWeightedEdge e312 = g3.addEdge(v1, v2);
       g3.setEdgeWeight(e312, 20.0);
       g3.setEdgeWeight(e331, 30.0);

       assertTrue(g1.equals(g2));
       assertTrue(!g2.equals(g3));
    }

    /**
     * Tests graph with custom edges.
     */
    public void testGrapshWithCustomEdges() {
        UndirectedGraph<String, CustomEdge> g1 =
            new SimpleGraph<String, CustomEdge>(
                CustomEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        g1.addEdge(v1, v2, new CustomEdge("v1-v2"));
        g1.addEdge(v3, v1, new CustomEdge("v3-v1"));

        UndirectedGraph<String, CustomEdge> g2 =
            new SimpleGraph<String, CustomEdge>(
                CustomEdge.class);
        g2.addVertex(v1);
        g2.addVertex(v2);
        g2.addVertex(v3);
        g2.addEdge(v1, v2, new CustomEdge("v1-v2"));
        g2.addEdge(v3, v1, new CustomEdge("v3-v1"));

        UndirectedGraph<String, CustomEdge> g3 =
            new SimpleGraph<String, CustomEdge>(
                CustomEdge.class);
        g3.addVertex(v1);
        g3.addVertex(v2);
        g3.addVertex(v3);
        g3.addEdge(v1, v2, new CustomEdge("v1::v2"));
        g3.addEdge(v3, v1, new CustomEdge("v3-v1"));

        assertTrue(g1.equals(g2));
        assertTrue(!g2.equals(g3));
    }

    /**
     * Tests graphs witch custom weighted eges.
     */
    public void testGrapshWithCustomWeightedEdges() {
        WeightedGraph<String, CustomWeightedEdge> g1 =
            new DefaultDirectedWeightedGraph<String, CustomWeightedEdge>(
                CustomWeightedEdge.class);
        g1.addVertex(v1);
        g1.addVertex(v2);
        g1.addVertex(v3);
        CustomWeightedEdge e112 = new CustomWeightedEdge("v1-v2"); 
        g1.addEdge(v1, v2, e112);
        CustomWeightedEdge e131 = new CustomWeightedEdge("v3-v1"); 
        g1.addEdge(v3, v1, e131);
        g1.setEdgeWeight(e112, 10.0);
        g1.setEdgeWeight(e131, 20.0);

        WeightedGraph<String, CustomWeightedEdge> g2 = 
             new DefaultDirectedWeightedGraph<String, CustomWeightedEdge>(
                 CustomWeightedEdge.class);
        g2.addVertex(v3);
        g2.addVertex(v2);
        g2.addVertex(v1);
        CustomWeightedEdge e231 = new CustomWeightedEdge("v3-v1"); 
        g2.addEdge(v3, v1, e231);
        CustomWeightedEdge e212 = new CustomWeightedEdge("v1-v2");
        g2.addEdge(v1, v2, e212);
        g2.setEdgeWeight(e212, 10.0);
        g2.setEdgeWeight(e231, 20.0);

        WeightedGraph<String, CustomWeightedEdge> g3 = 
            new DefaultDirectedWeightedGraph<String, CustomWeightedEdge>(
                CustomWeightedEdge.class);
       g3.addVertex(v3);
       g3.addVertex(v2);
       g3.addVertex(v1);
       CustomWeightedEdge e331 = new CustomWeightedEdge("v3-v1");
       g3.addEdge(v3, v1, e331);
       CustomWeightedEdge e312 = new CustomWeightedEdge("v1-v2");
       g3.addEdge(v1, v2, e312);
       g3.setEdgeWeight(e312, 20.0);
       g3.setEdgeWeight(e331, 30.0);

       assertTrue(g1.equals(g2));
       assertTrue(!g2.equals(g3));
    }

    /**
     * Custom edge class.
     */
    public static class CustomEdge
        extends DefaultEdge
    {
        private static final long serialVersionUID = 1L;
        private String label;

        public CustomEdge(String label) {
            this.label = label; 
        }

        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof CustomEdge)) return false;

            CustomEdge edge = (CustomEdge) obj;
            return label.equals(edge.label);
        }
    }

    /**
     * Custom weighted edge class.
     */
    public static class CustomWeightedEdge
        extends DefaultWeightedEdge
    {
        private static final long serialVersionUID = 1L;
        private String label;

        public CustomWeightedEdge(String label) {
            this.label = label; 
        }

        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof CustomWeightedEdge)) return false;

            CustomWeightedEdge edge = (CustomWeightedEdge) obj;
            return label.equals(edge.label);
        }
    }
}
