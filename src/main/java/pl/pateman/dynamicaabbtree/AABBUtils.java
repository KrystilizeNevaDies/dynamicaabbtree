package pl.pateman.dynamicaabbtree;

import org.joml.AABBf;

/**
 * Created by pateman.
 */
public final class AABBUtils {

    private AABBUtils() {

    }

    public static double getWidth(AABBf aabb) {
        return aabb.maxX - aabb.minX;
    }

    public static double getHeight(AABBf aabb) {
        return aabb.maxY - aabb.minY;
    }

    public static double getDepth(AABBf aabb) {
        return aabb.maxZ - aabb.minZ;
    }

    public static double getArea(AABBf aabb) {
        final double width = getWidth(aabb);
        final double height = getHeight(aabb);
        final double depth = getDepth(aabb);
        return 2.0 * (width * height + width * depth + height * depth);
    }
}
