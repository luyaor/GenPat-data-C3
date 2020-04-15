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
package org._3pq.jgrapht.experimental.alg;

import java.io.*;

import org._3pq.jgrapht.*;


/**
 * Abstract superclass for algorithms on WeightedGraphs.
 *
 * @author Michael Behrisch
 */
public abstract class WeightedGraphAlgorithm<V,E extends Edge<V>> implements Serializable
{

    //~ Instance fields -------------------------------------------------------

    /**
     * The WeightedGraph object that the algorithm uses.
     */
    protected final WeightedGraph<V,E> _wgraph;

    /**
     * whether the stored graph is directed
     */
    protected final boolean _directed;

    //~ Constructors ----------------------------------------------------------

    /**
     * Creates a new WeightedGraphAlgorithm object.
     *
     * @param wgraph
     */
    public WeightedGraphAlgorithm(WeightedGraph<V,E> wgraph)
    {
        _wgraph = wgraph;
        _directed = wgraph instanceof DirectedGraph;
    }
}
