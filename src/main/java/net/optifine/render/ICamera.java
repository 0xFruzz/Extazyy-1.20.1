package net.optifine.render;

import net.minecraft.world.phys.AABB;

public interface ICamera {
   void setCameraPosition(double var1, double var3, double var5);

   boolean isBoundingBoxInFrustum(AABB var1);

   boolean isBoxInFrustumFully(double var1, double var3, double var5, double var7, double var9, double var11);
}