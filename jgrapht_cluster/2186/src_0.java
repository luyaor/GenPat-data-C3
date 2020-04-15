/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------------
 * VF2SubgraphIsomorphismInspector.java
 * -------------------------
 * (C) Copyright 2015, by Fabian Späh and Contributors.
 *
 * Original Author:  Fabian Späh
 * Contributor(s):   Rita Dobler
 *
 * $Id$
 *
 * Changes
 * -------
 * 20-Jun-2015 : Initial revision (FS);
 *
 */
package org.jgrapht.alg.isomorphism;

import java.util.Comparator;

import org.jgrapht.Graph;


/**
 * This is an implementation of the VF2 algorithm using its feature of detecting
 * <a href="http://mathworld.wolfram.com/Vertex-InducedSubgraph.html">
 * subgraph isomorphism between two graphs</a> as described in Cordella et al. A
 * (sub)graph isomorphism algorithm for matching large graphs (2004),
 * DOI:10.1109/TPAMI.2004.75,
 * <a href="http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1323804">
 * http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=1323804</a>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 */
public class VF2SubgraphIsomorphismInspector<V,E>
    extends VF2AbstractIsomorphismInspector<V,E>
{

    public VF2SubgraphIsomorphismInspector(
                    Graph<V,E> graph1,
                    Graph<V,E> graph2,
                    Comparator<V> vertexComparator,
                    Comparator<E> edgeComparator)
    {
        super(graph1, graph2, vertexComparator, edgeComparator);
    }

    public VF2SubgraphIsomorphismInspector(
                    Graph<V, E> graph1,
                    Graph<V, E> graph2)
    {
        super(graph1, graph2);
    }


    @Override
    public VF2SubgraphMappingIterator<V, E> getMappings() {
        return new VF2SubgraphMappingIterator<V,E>(ordering1, ordering2,
                        vertexComparator, edgeComparator);
    }

}
