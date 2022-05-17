package pl.pateman.dynamicaabbtree;

import org.joml.AABBf;

import static pl.pateman.dynamicaabbtree.AABBUtils.getArea;


public class AreaAABBHeuristicFunction<T extends Boundable> implements AABBTreeHeuristicFunction<T> {
    private final AABBf temp;

    public AreaAABBHeuristicFunction() {
        temp = new AABBf();
    }

    @Override
    public HeuristicResult getInsertionHeuristic(AABBf left, AABBf right, T object, AABBf objectAABB) {
        double diffA = getArea(left.union(objectAABB, temp)) - getArea(left);
        double diffB = getArea(right.union(objectAABB, temp)) - getArea(right);
        return diffA < diffB ? HeuristicResult.LEFT : HeuristicResult.RIGHT;
    }
}
