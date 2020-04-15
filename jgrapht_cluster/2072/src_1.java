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
 * EdgeFactories.java
 * ------------------
 * (C) Copyright 2003, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 04-Aug-2003 : Renamed from EdgeFactoryFactory & made utility class (BN);
 * 03-Nov-2003 : Made edge factories serializable (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org._3pq.jgrapht.edge;

import java.io.*;

import org._3pq.jgrapht.*;


/**
 * This utility class is a container of various {@link
 * org._3pq.jgrapht.EdgeFactory} classes.
 *
 * <p>Classes included here do not have substantial logic. They are grouped
 * together in this container in order to avoid clutter.</p>
 *
 * @author Barak Naveh
 * @since Jul 16, 2003
 */
public final class EdgeFactories
{

    //~ Constructors ----------------------------------------------------------

    private EdgeFactories()
    {
    } // ensure non-instantiability.

    //~ Inner Classes ---------------------------------------------------------

    /**
     * An EdgeFactory for producing directed edges.
     *
     * @author Barak Naveh
     * @since Jul 14, 2003
     */
    public static class DirectedEdgeFactory<V> extends AbstractEdgeFactory<V, DirectedEdge<V>>
    {
        private static final long serialVersionUID = 3618135658586388792L;

        /**
         * @see EdgeFactory#createEdge(Object, Object)
         */
        public DirectedEdge<V> createEdge(V source, V target)
        {
            return new DirectedEdge(source, target);
        }
    }

    /**
     * An EdgeFactory for producing directed edges with weights.
     *
     * @author Barak Naveh
     * @since Jul 14, 2003
     */
    public static class DirectedWeightedEdgeFactory<V>
        extends AbstractEdgeFactory<V, DirectedWeightedEdge<V>>
    {
        private static final long serialVersionUID = 3257002163870775604L;

        /**
         * @see EdgeFactory#createEdge(Object, Object)
         */
        public DirectedWeightedEdge createEdge(V source, V target)
        {
            return new DirectedWeightedEdge(source, target);
        }
    }

    /**
     * An EdgeFactory for producing undirected edges.
     *
     * @author Barak Naveh
     * @since Jul 14, 2003
     */
    public static class UndirectedEdgeFactory<V> extends AbstractEdgeFactory<V, UndirectedEdge<V>>
    {
        private static final long serialVersionUID = 3257007674431189815L;

        /**
         * @see EdgeFactory#createEdge(Object, Object)
         */
        public UndirectedEdge createEdge(V source, V target)
        {
            return new UndirectedEdge(source, target);
        }
    }

    /**
     * An EdgeFactory for producing undirected edges with weights.
     *
     * @author Barak Naveh
     * @since Jul 14, 2003
     */
    public static class UndirectedWeightedEdgeFactory<V>
        extends AbstractEdgeFactory<V, UndirectedWeightedEdge<V>>
    {
        private static final long serialVersionUID = 4048797883346269237L;

        /**
         * @see EdgeFactory#createEdge(Object, Object)
         */
        public UndirectedWeightedEdge createEdge(V source, V target)
        {
            return new UndirectedWeightedEdge(source, target);
        }
    }

    /**
     * A base class for edge factories.
     *
     * @author Barak Naveh
     * @since Nov 3, 2003
     */
    abstract static class AbstractEdgeFactory<V, E extends Edge<V>>
        implements EdgeFactory<V, E>, Serializable
    {
    }
}
