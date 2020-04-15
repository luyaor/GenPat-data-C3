package org.jgrapht.experimental.subgraphisomorphism;

import java.util.Arrays;
import java.util.Comparator;

public class VF2SubState<V,E> {
	
	private static int NULL_NODE;

	private int[] core1,
	              core2,
			      in1,
			      in2,
			      out1,
			      out2;
	
	private int coreLen,
			
				n1,
				n2,
				
			    t1BothLen,
			    t2BothLen,
			    t1InLen,
			    t2InLen,
			    t1OutLen,
			    t2OutLen,
			    
			    addedVertex1,
				addVertex1,
				addVertex2;
	
	private GraphOrdering<V,E> g1,
	                           g2;
	
	private Comparator<V> vertexComparator;
	private Comparator<E> edgeComparator;
	
	
	
	public VF2SubState(
			GraphOrdering<V,E> g1,
			GraphOrdering<V,E> g2,
			
			Comparator<V> vertexComparator,
			Comparator<E> edgeComparator)
	{
		// to avoid this one should start the array at 1 instead of 0 and use 0 as value for NULL_NODE
		NULL_NODE = Integer.MAX_VALUE;
		
		this.g1 = g1;
		this.g2 = g2;
		this.vertexComparator = vertexComparator;
		this.edgeComparator   = edgeComparator;
		
		n1 = g1.getVertexCount();
		n2 = g2.getVertexCount();
		
		core1 = new int[n1];
		in1   = new int[n1];
		out1  = new int[n1];
		core2 = new int[n2];
		in2   = new int[n2];
		out2  = new int[n2];
		Arrays.fill(core1, NULL_NODE);
		Arrays.fill(core2, NULL_NODE);
		
		coreLen = 0;
		addedVertex1 = addVertex1 = addVertex2 = NULL_NODE;
		
		t1BothLen = t2BothLen = t1InLen = t2InLen = t1OutLen = t2OutLen = 0;
	}
	
	public VF2SubState(VF2SubState<V,E> s)	{
		g1 = s.g1;
		g2 = s.g2;
		
		core1 = s.core1;
		core2 = s.core2;
		in1   = s.in1;
		in2   = s.in2;
		out1  = s.out1;
		out2  = s.out2;
		
		coreLen = s.coreLen;
		
		n1    = s.n1;
		n2    = s.n2;
		
		t1BothLen = s.t1BothLen;
		t2BothLen = s.t2BothLen;
		t1InLen   = s.t1InLen;
		t2InLen   = s.t2InLen;
		t1OutLen  = s.t1OutLen;
		t2OutLen  = s.t2OutLen;
		
		vertexComparator = s.vertexComparator;
		edgeComparator   = s.edgeComparator;
		
		addVertex1 = s.addVertex1;
		addVertex2 = s.addVertex2;
		addedVertex1 = s.addedVertex1;
	}
	
	
	public boolean nextPair()	{
		if (addVertex2 == NULL_NODE)
			addVertex2 = 0;
		
		if (addVertex1 == NULL_NODE)
			addVertex1 = 0;
		else
			addVertex1++;
		
		// check incoming and outgoing edges
		if (t1BothLen > coreLen && t2BothLen > coreLen)	{
			
			// find minimum for addVertex2 in core2 and t2in/t2out
			while (addVertex2 < n2 &&
				    (core2[addVertex2] != NULL_NODE ||
				     out2[addVertex2]  == 0 ||
				     in2[addVertex2]   == 0))
			{
				addVertex2++;
				addVertex1 = 0;
			}
			
			// find first/next vertex for addVertex1 in core1 and t1in/t1out
			while (addVertex1 < n1 &&
					(core1[addVertex1] != NULL_NODE ||
					 out1[addVertex1]  == 0 ||
					 in1[addVertex1]   == 0))
			{
				addVertex1++;
			}
		}
		
		//check outgoing edges
		else if (t1OutLen > coreLen && t2OutLen > coreLen)	{
			while (addVertex2 < n2 &&
					(core2[addVertex2] != NULL_NODE ||
					 out2[addVertex2]  == 0))
			{
				addVertex2++;
				addVertex1 = 0;
			}
			
			while (addVertex1 < n1 &&
					(core1[addVertex1] != NULL_NODE ||
					 out1[addVertex1]   == 0))
			{
				addVertex1++;
			}
		}
		
		//check incoming edges
		else if (t1InLen > coreLen && t2InLen > coreLen)	{
			while (addVertex2 < n2 &&
					(core2[addVertex2] != NULL_NODE ||
					 in2[addVertex2]   == 0))
			{
				addVertex2++;
				addVertex1 = 0;
			}
			
			while (addVertex1 < n1 &&
					(core1[addVertex1] != NULL_NODE ||
					 in1[addVertex1]   == 0))
			{
				addVertex1++;
			}
		}
		
		//check new edges
		else	{
			while (addVertex2 < n2 &&
					core2[addVertex2] != NULL_NODE)
			{
				addVertex2++;
				addVertex1 = 0;
			}
			
			while (addVertex1 < n1 &&
					core1[addVertex1] != NULL_NODE)
			{
				addVertex1++;
			}
		}
		
		if (addVertex1 < n1 && addVertex2 < n2)
			return true;
		
		// there are no more pairs..
		addVertex1 = addVertex2 = NULL_NODE;
		return false;
	}
	
	
	public void addPair()	{
		coreLen++;
		addedVertex1 = addVertex1;
		
		if (in1[addVertex1] == 0)	{
			in1[addVertex1] = coreLen;
			t1InLen++;
			if (out1[addVertex1] > 0)
				t1BothLen++;
		}
		
		if (out1[addVertex1] == 0)	{
			out1[addVertex1] = coreLen;
			t1OutLen++;
			if (in1[addVertex1] > 0)
				t1BothLen++;
		}
		
		if (in2[addVertex2] == 0)	{
			in2[addVertex2] = coreLen;
			t2InLen++;
			if (out2[addVertex2] > 0)
				t2BothLen++;
		}
		
		if (out2[addVertex2] == 0)	{
			out2[addVertex2] = coreLen;
			t2OutLen++;
			if (in2[addVertex2] > 0)
				t2BothLen++;
		}
		
		core1[addVertex1] = addVertex2;
		core2[addVertex2] = addVertex1;
		
		for (int other : g1.getInEdges(addVertex1))	{
			if (in1[other] == 0)	{
				in1[other] = coreLen;
				t1InLen++;
				if (out1[other] > 0)
					t1BothLen++;
			}
		}
		
		for (int other : g1.getOutEdges(addVertex1))	{
			if (out1[other] == 0)	{
				out1[other] = coreLen;
				t1OutLen++;
				if (in1[other] > 0)
					t1BothLen++;
			}
		}
		
		for (int other : g2.getInEdges(addVertex2))	{
			if (in2[other] == 0)	{
				in2[other] = coreLen;
				t2InLen++;
				if (out2[other] > 0)
					t2BothLen++;
			}
		}
		
		for (int other : g2.getOutEdges(addVertex2))	{
			if (out2[other] == 0)	{
				out2[other] = coreLen;
				t2OutLen++;
				if (in2[other] > 0)
					t2BothLen++;
			}
		}
	}
	
	
	public boolean isGoal()	{
		return coreLen == n2;
	}
	
	
	
	public boolean isFeasiblePair()	{
		//check for semantic equality of both vertexes
		if (!areCompatibleVertexes(addVertex1, addVertex2))
			return false;
		
		int termOut1 = 0,
			termOut2 = 0,
			termIn1  = 0,
			termIn2  = 0,
			new1     = 0,
			new2     = 0;
		
		//check outgoing edges of addVertex1
		for (int other1 : g1.getOutEdges(addVertex1))
		{
			if (core1[other1] != NULL_NODE)
			{
				int other2 = core1[other1];
				if (!g2.hasEdge(addVertex2, other2) ||
						!areCompatibleEdges(addVertex1, other1, addVertex2, other2))
					return false;
			}
			else
			{
				if (in1[other1] > 0)
					termIn1++;
				if (out1[other1] > 0)
					termOut1++;
				if (in1[other1] == 0 && out1[other1] == 0)
					new1++;
			}
		}
		
		//check incoming edges of addVertex1
		for (int other1 : g1.getInEdges(addVertex1))
		{
			if (core1[other1] != NULL_NODE)
			{
				int other2 = core1[other1];
				if (!g2.hasEdge(other2, addVertex2) ||
						!areCompatibleEdges(other1, addVertex1, other2, addVertex2))
					return false;
			}
			else
			{
				if (in1[other1] > 0)
					termIn1++;
				if (out1[other1] > 0)
					termOut1++;
				if (in1[other1] == 0 && out1[other1] == 0)
					new1++;
			}
		}
		
		//check outgoing edges of addVertex2
		for (int other2 : g2.getOutEdges(addVertex2))
		{
			if (core2[other2] != NULL_NODE)
			{
				int other1 = core2[other2];
				if (!g1.hasEdge(addVertex1, other1))
					return false;
			}
			else
			{
				if (in2[other2] > 0)
					termIn2++;
				if (out2[other2] > 0)
					termOut2++;
				if (in2[other2] == 0 && out2[other2] == 0)
					new2++;
			}
		}
		
		//check incoming edges of addVertex2
		for (int other2 : g2.getInEdges(addVertex2))
		{
			if (core2[other2] != NULL_NODE)
			{
				int other1 = core2[other2];
				if (!g1.hasEdge(other1, addVertex1))
					return false;
			}
			else
			{
				if (in2[other2] > 0)
					termIn2++;
				if (out2[other2] > 0)
					termOut2++;
				if (in2[other2] == 0 && out2[other2] == 0)
					new2++;
			}
		}
		
		return termIn1 >= termIn2 && termOut1 >= termOut2 && new1 >= new2;
	}
	
	
	public void backtrack()	{
		int addedVertex2 = core1[addedVertex1];
		
		if (in1[addedVertex1] == coreLen)
			in1[addedVertex1] = 0;
		
		for (int other : g1.getInEdges(addedVertex1))
		{
			if (in1[other] == coreLen)
				in1[other] = 0;
		}
		
		if (out1[addedVertex1] == coreLen)
			out1[addedVertex1] = 0;
		
		for (int other : g1.getOutEdges(addedVertex1))
		{
			if (out1[other] == coreLen)
				out1[other] = 0;
		}
		
		if (in2[addedVertex2] == coreLen)
			in2[addedVertex2] = 0;
		
		for (int other : g2.getInEdges(addedVertex2))
		{
			if (in2[other] == coreLen)
				in2[other] = 0;
		}
		
		if (out2[addedVertex2] == coreLen)
			out2[addedVertex2] = 0;
		
		for (int other : g2.getOutEdges(addedVertex2))
		{
			if (out2[other] == coreLen)
				out2[other] = 0;
		}
		
		core1[addedVertex1] = core2[addedVertex2] = NULL_NODE;
		coreLen--;
		addedVertex1 = NULL_NODE;
	}
	
	
	
	private boolean areCompatibleVertexes(int v1, int v2)	{
		return vertexComparator.compare(
				g1.getVertex(v1),
				g2.getVertex(v2)) == 0;
	}
	
	private boolean areCompatibleEdges(int v1, int v2, int u1, int u2)	{
		return edgeComparator.compare(
				g1.getEdge(v1, v2),
				g2.getEdge(u1, u2)) == 0;
	}
	
	
	public SubgraphIsomorphismRelation<V,E> getCurrentMatching()	{
		SubgraphIsomorphismRelation<V,E> rel =
				new SubgraphIsomorphismRelation<V,E>(g1, g2, core1, core2);
		return rel;
	}
	
	public void resetAddVertexes()	{
		addVertex1 = addVertex2 = NULL_NODE;
	}
}
