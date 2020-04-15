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
/* --------------------------------------
 * ListenableUndirectedWeightedGraph.java
 * --------------------------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id: ListenableUndirectedWeightedGraph.java,v 1.3 2005/07/19 09:03:31
 * hammerc Exp $
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 06-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * An undirected weighted graph which is also {@link
 * org.jgrapht.ListenableGraph}.
 *
 * @see org.jgrapht.graph.DefaultListenableGraph
 */
public class ListenableUndirectedWeightedGraph<V, E extends Edge<V>>
    extends ListenableUndirectedGraph<V, E> implements WeightedGraph<V, E>
{

    //~ Static fields/initializers --------------------------------------------

    private static final long serialVersionUID = 3690762799613949747L;

    //~ Constructors ----------------------------------------------------------

    /**
     * Creates a new listenable undirected weighted graph.
     */
    public ListenableUndirectedWeightedGraph()
    {
        this(new SimpleWeightedGraph<V,E>());
    }

    /**
     * Creates a new listenable undirected weighted graph.
     *
     * @param base the backing graph.
     */
    public ListenableUndirectedWeightedGraph(WeightedGraph<V, E> base)
    {
        super((UndirectedGraph<V,E>) base);
    }
}
