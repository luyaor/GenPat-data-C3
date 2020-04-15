package org.jgrapht.alg.isomorphism;

import java.util.Comparator;


public class VF2GraphMappingIterator<V,E>
    extends VF2MappingIterator<V, E>
{

    /**
     * @param ordering1
     * @param ordering2
     * @param vertexComparator
     * @param edgeComparator
     */
    public VF2GraphMappingIterator(
                    GraphOrdering<V, E> ordering1,
                    GraphOrdering<V, E> ordering2,
                    Comparator<V> vertexComparator,
                    Comparator<E> edgeComparator)
    {
        super(ordering1, ordering2, vertexComparator, edgeComparator);
    }


    @Override
    protected IsomorphicGraphMapping<V, E> match() {
        VF2State<V, E> s;

        if (stateStack.isEmpty()) {
            if (ordering1.getGraph().vertexSet().size() !=
                            ordering2.getGraph().vertexSet().size())
                return null;

            s = new VF2GraphIsomorphismState<V, E>(ordering1, ordering2,
                            vertexComparator, edgeComparator);

            if (ordering2.getGraph().vertexSet().isEmpty())
                return hadOneMapping != null ? null : s.getCurrentMapping();
        } else {
            stateStack.pop().backtrack();
            s = stateStack.pop();
        }


        while (true) {
            while (s.nextPair()) {
                if (s.isFeasiblePair()) {
                    stateStack.push(s);
                    s = new VF2GraphIsomorphismState<V,E>(s);
                    s.addPair();

                    if (s.isGoal()) {
                        stateStack.push(s);
                        return s.getCurrentMapping();
                    }

                    s.resetAddVertexes();
                }
            }

            if (stateStack.isEmpty())
                return null;

            s.backtrack();
            s = stateStack.pop();
        }
    }

}
