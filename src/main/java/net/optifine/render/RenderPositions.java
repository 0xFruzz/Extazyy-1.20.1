package net.optifine.render;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public class RenderPositions {
   private Vector3f positionDiv16;
   private Vector3f positionRender;

   public RenderPositions(Vector3f position) {
      this.positionDiv16 = (new Vector3f((Vector3fc)position)).div(16.0F);
      this.positionRender = new Vector3f((Vector3fc)this.positionDiv16);
   }

   public Vector3f getPositionDiv16() {
      return this.positionDiv16;
   }

   public Vector3f getPositionRender() {
      return this.positionRender;
   }

   public String toString() {
      return this.positionDiv16 + ", " + this.positionRender;
   }
}