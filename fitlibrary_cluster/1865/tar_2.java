/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArrayFixtureUnderTestWithMap extends fitlibrary.ArrayFixture {
	public ArrayFixtureUnderTestWithMap() throws Exception {
	    setActualCollection(query());
	}
	public List query() throws Exception {
        List result = new ArrayList();
        result.add(makeMap(new Integer(1), "one"));
        result.add(makeMap(new Integer(1), "two"));
        result.add(makeMap(new Integer(2), "two"));
        return result;
   }
	private Map makeMap(Integer plus, String ampersand) {
        Map map = new HashMap();
        map.put("plus",plus);
        map.put("ampersand",ampersand);
        return map;
    }
}
