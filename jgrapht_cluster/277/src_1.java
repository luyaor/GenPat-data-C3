package org.jgrapht.generate;


import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

public class SimpleWeightedBipartiteGraphMatrixGenerator<V, E> extends WeightedGraphGenerator<V, E> {

    List<V> first;

    List<V> second;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public SimpleWeightedGraph<V, E> build() {

        if (weights == null)
            throw new IllegalArgumentException("Graph may not be constructed without weight-matrix specified");

        SimpleWeightedGraph<V, E> target;

        if (edgeFactory != null)
            target = new SimpleWeightedGraph<V, E>(edgeFactory);
        else if (edgeClass != null)
            target = new SimpleWeightedGraph<V, E>(edgeClass);
        else
            throw new IllegalArgumentException("Graph may not be constructed only with edge-factory or edge-class specified");

        if (first == null || second == null)
            throw new IllegalArgumentException("Graph may not be constructed without either of vertex-set partitions specified");

        assert second.size() == weights.length;

        for (V vertex : first) {
            target.addVertex(vertex);
        }

        for (V vertex : second) {
            target.addVertex(vertex);
        }

        for (int i=0; i < first.size(); ++i) {

            assert first.size() == weights[i].length;

            for (int j=0; j < second.size(); ++j) {
                target.setEdgeWeight(
                    target.addEdge(first.get(i), second.get(j)),
                    weights[i][j]
                );
            }
        }

        return target;

    }

    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> first(List<? extends V> first) {
        this.first = new ArrayList<V>(first);
        return this;
    }

    public SimpleWeightedBipartiteGraphMatrixGenerator<V, E> second(List<? extends V> second) {
        this.second = new ArrayList<V>(second);
        return this;
    }

}
