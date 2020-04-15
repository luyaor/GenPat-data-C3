/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibraryGeneric.specify;

public class ConcreteClassTwo implements InterfaceType {
    private int count;
    
    public int getCount() {
        return count*2;
    }
    public void setCount(int count) {
        this.count = count;
    }   
}
