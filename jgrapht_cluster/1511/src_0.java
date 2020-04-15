/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Lead:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2005, by Barak Naveh and Contributors.
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
/* -----------------
 * UniformEquivalenceComparator.java
 * -----------------
 * (C) Copyright 2005, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * Changes
 * -------
 */
package org._3pq.jgrapht.util.equivalence;

/**
 * This Equivalence comparator acts as if all elements are in one big global
 * equivalence class.  Useful when a comparator is needed, but there is no
 * important difference between the elements.  equivalenceCompare() always
 * return true; equivalenceHashcode() always returns 0.
 *
 * @author Assaf
 * @since Jul 21, 2005
 */
public class UniformEquivalenceComparator implements EquivalenceComparator
{

    //~ Methods ---------------------------------------------------------------

    /**
     * Always returns true.
     *
     * @see org._3pq.jgrapht.util.equivalence.EquivalenceComparator#equivalenceCompare(java.lang.Object,
     *      java.lang.Object, Object, Object)
     */
    public boolean equivalenceCompare(
        Object arg1,
        Object arg2,
        Object context1,
        Object context2)
    {
        return true;
    }

    /**
     * Always returns 0.
     *
     * @see org._3pq.jgrapht.util.equivalence.EquivalenceComparator#equivalenceHashcode(java.lang.Object,
     *      Object)
     */
    public int equivalenceHashcode(Object arg1, Object context)
    {
        return 0;
    }
}
