package pl.pateman.dynamicaabbtree;

import org.joml.AABBf;

public class TestEntity implements Boundable, Identifiable
{

   private final float x;
   private final float y;
   private final float z;

   private final float width;
   private final float height;
   private final float depth;

   private final int id;

   TestEntity(int id, float x, float y, float z, float width, float height, float depth) {
      this.id = id;
      this.x = x;
      this.y = y;
      this.z = z;
      this.width = width;
      this.height = height;
      this.depth = depth;
   }

   TestEntity(int id, float x, float y, float width, float height) {
      this(id, x, y, 0, width, height, 0);
   }

   @Override
   public AABBf getAABB(AABBf dest)
   {
      if (dest == null)
      {
         dest = new AABBf();
      }
      dest.setMin(x, y, z);
      dest.setMax(x + width, y + height, z + depth);
      return dest;
   }

   @Override
   public long getID()
   {
      return id;
   }
}
