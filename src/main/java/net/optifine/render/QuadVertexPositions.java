package net.optifine.render;

import net.optifine.util.IntExpiringCache;
import net.optifine.util.RandomUtils;

public class QuadVertexPositions extends IntExpiringCache<VertexPosition[]> {
   public QuadVertexPositions() {
      super('\uea60' + RandomUtils.getRandomInt(10000));
   }

   protected VertexPosition[] make() {
      return new VertexPosition[]{new VertexPosition(), new VertexPosition(), new VertexPosition(), new VertexPosition()};
   }
}