/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.iter;

import java.util.Iterator;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Creates an iterable based on the result of immediately traversing some other iterable (assumed to be finite); 
 * generated iterators will traverse those same values in the same order.  Changes to the wrapped iterable will 
 * <em>not</em> be reflected.
 */
public class SnapshotIterable<T> extends AbstractIterable<T> implements SizedIterable<T>, Serializable {
  
  private final ArrayList<T> _values;
  
  public SnapshotIterable(Iterable<? extends T> iterable) {
    _values = new ArrayList<T>(0); // minimize footprint of empty
    for (T e : iterable) { _values.add(e); }
  }
  
  public SnapshotIterable(Iterator<? extends T> iterator) {
    _values = new ArrayList<T>(0); // minimize footprint of empty
    while (iterator.hasNext()) { _values.add(iterator.next()); }
  }
    
  public Iterator<T> iterator() { return _values.iterator(); }
  public int size() { return _values.size(); }
  public int size(int bound) { int result = _values.size(); return result < bound ? result : bound; }
  public boolean isInfinite() { return false; }
  public boolean isFixed() { return true; }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SnapshotIterable<T> make(Iterable<? extends T> iterable) {
    return new SnapshotIterable<T>(iterable);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> SnapshotIterable<T> make(Iterator<? extends T> iterator) {
    return new SnapshotIterable<T>(iterator);
  }
}
