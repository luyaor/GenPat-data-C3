package fitlibraryGeneric.specify.genericFinder;

import fitlibrary.object.DomainFixtured;
import fitlibrary.traverse.DomainAdapter;

@SuppressWarnings({"unchecked","unused"})
public class NonGenericFinderForGeneric implements DomainAdapter, DomainFixtured  {
	private Pair<Integer,Integer> integerIntegerPair;
	
	public Pair<Integer,Integer> getIntegerIntegerPair() {
		return integerIntegerPair;
	}
	public void setIntegerIntegerPair(Pair<Integer,Integer> pair) {
		this.integerIntegerPair = pair;
	}
	public Pair findPair(String key) { 
		return new Pair<Integer,Integer>(1,2);
	}
	public Object getSystemUnderTest() {
		return null;
	}
}
