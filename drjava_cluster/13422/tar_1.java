package edu.rice.cs.plt.iter;

import java.io.Serializable;

/**
 * Defines an iterable by composing two other iterables (or a value with an iterable).
 * Subsequent changes to the input lists will be reflected.
 */
public class ComposedIterable<T> extends AbstractIterable<T> 
  implements SizedIterable<T>, OptimizedLastIterable<T>, Serializable {
  
  private final Iterable<? extends T> _i1;
  private final int _i1Size; // negative implies dynamic size
  private final Iterable<? extends T> _i2;
  private final int _i2Size; // negative implies dynamic size
  
  /** The result contains {@code i1}'s elements followed by {@code i2}'s elements */
  public ComposedIterable(Iterable<? extends T> i1, Iterable<? extends T> i2) {
    _i1 = i1;
    _i2 = i2;
    if (IterUtil.isFixed(i1)) { _i1Size = IterUtil.sizeOf(_i1); }
    else { _i1Size = -1; }
    if (IterUtil.isFixed(i2)) { _i2Size = IterUtil.sizeOf(_i2); }
    else { _i2Size = -1; }
  }
    
  /** The result contains {@code v1} followed by {@code i2}'s elements */
  public ComposedIterable(T v1, Iterable<? extends T> i2) {
    this(new SingletonIterable<T>(v1), i2);
  }
  
  /** The result contains {@code i1}'s elements followed by {@code v2} */
  public ComposedIterable(Iterable<? extends T> i1, T v2) {
    this(i1, new SingletonIterable<T>(v2));
  }
  
  public ComposedIterator<T> iterator() { 
    return new ComposedIterator<T>(_i1.iterator(), _i2.iterator());
  }
  
  public int size() {
    int result = (_i1Size < 0 ? IterUtil.sizeOf(_i1) : _i1Size) +
                 (_i2Size < 0 ? IterUtil.sizeOf(_i2) : _i2Size);
    if (result < 0) { result = Integer.MAX_VALUE; } // overflow
    return result;
  }
  
  public int size(int bound) {
    int result = (_i1Size < 0 ? IterUtil.sizeOf(_i1, bound) : _i1Size) +
                 (_i2Size < 0 ? IterUtil.sizeOf(_i2, bound) : _i2Size);
    if (result < 0) { result = Integer.MAX_VALUE; } // overflow
    return result <= bound ? result : bound;
  }
  
  public boolean isFixed() { return _i1Size >= 0 && _i2Size >= 0; }
  
  /**
   * Determine the last value in the iterable.  This implementation will usually be faster than
   * the general approach of iterating through the entire list -- for a balanced
   * {@code ComposedIterable} tree, it takes log(n) time; if the right subtree is a singleton, 
   * the result is computed trivially.  (Note that the approach used avoids recursion in order 
   * to prevent a stack overflow.)
   */
  public T last() {
    Iterable<? extends T> lastIterable;
    if (IterUtil.isEmpty(_i2)) { lastIterable = _i1; }
    else { lastIterable = _i2; }
    
    while (lastIterable instanceof ComposedIterable<?>) {
      ComposedIterable<? extends T> cast = (ComposedIterable<? extends T>) lastIterable;
      if (IterUtil.isEmpty(cast._i2)) { lastIterable = cast._i1; }
      else { lastIterable = cast._i2; }
    }
    
    return IterUtil.last(lastIterable);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterable<T> make(Iterable<? extends T> i1, Iterable<? extends T> i2) {
    return new ComposedIterable<T>(i1, i2);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterable<T> make(T v1, Iterable<? extends T> i2) {
    return new ComposedIterable<T>(v1, i2);
  }
  
  /** Call the constructor (allows {@code T} to be inferred) */
  public static <T> ComposedIterable<T> make(Iterable<? extends T> i1, T v2) {
    return new ComposedIterable<T>(i1, v2);
  }
  
}
