package net.optifine.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.util.RandomUtils;

public class ModelUtils {
   private static final RandomSource RANDOM = RandomUtils.makeThreadSafeRandomSource(0);

   public static void dbgModel(BakedModel model) {
      if (model != null) {
         Config.dbg("Model: " + model + ", ao: " + model.useAmbientOcclusion() + ", gui3d: " + model.isGui3d() + ", builtIn: " + model.isCustomRenderer() + ", particle: " + model.getParticleIcon());
         Direction[] adirection = Direction.VALUES;

         for(int i = 0; i < adirection.length; ++i) {
            Direction direction = adirection[i];
            List list = model.getQuads((BlockState)null, direction, RANDOM);
            dbgQuads(direction.getSerializedName(), list, "  ");
         }

         List list1 = model.getQuads((BlockState)null, (Direction)null, RANDOM);
         dbgQuads("General", list1, "  ");
      }
   }

   private static void dbgQuads(String name, List<BakedQuad> quads, String prefix) {
      for(BakedQuad bakedquad : quads) {
         dbgQuad(name, bakedquad, prefix);
      }
   }

   public static void dbgQuad(String name, BakedQuad quad, String prefix) {
      Config.dbg(prefix + "Quad: " + quad.getClass().getName() + ", type: " + name + ", face: " + quad.getDirection() + ", tint: " + quad.getTintIndex() + ", sprite: " + quad.getSprite());
      dbgVertexData(quad.getVertices(), "  " + prefix);
   }

   public static void dbgVertexData(int[] vd, String prefix) {
      int i = vd.length / 4;
      Config.dbg(prefix + "Length: " + vd.length + ", step: " + i);

      for(int j = 0; j < 4; ++j) {
         int k = j * i;
         float f = Float.intBitsToFloat(vd[k + 0]);
         float f1 = Float.intBitsToFloat(vd[k + 1]);
         float f2 = Float.intBitsToFloat(vd[k + 2]);
         int l = vd[k + 3];
         float f3 = Float.intBitsToFloat(vd[k + 4]);
         float f4 = Float.intBitsToFloat(vd[k + 5]);
         Config.dbg(prefix + j + " xyz: " + f + "," + f1 + "," + f2 + " col: " + l + " u,v: " + f3 + "," + f4);
      }

   }

   public static BakedModel duplicateModel(BakedModel model) {
      List<BakedQuad> list = duplicateQuadList(model.getQuads((BlockState)null, (Direction)null, RANDOM));
      Direction[] adirection = Direction.VALUES;
      Map<Direction, List<BakedQuad>> map = new HashMap<>();

      for(int i = 0; i < adirection.length; ++i) {
         Direction direction = adirection[i];
         List list1 = model.getQuads((BlockState)null, direction, RANDOM);
         List list2 = duplicateQuadList(list1);
         map.put(direction, list2);
      }

      List<BakedQuad> list3 = new ArrayList<>(list);
      Map<Direction, List<BakedQuad>> map1 = new HashMap<>(map);
      SimpleBakedModel simplebakedmodel = new SimpleBakedModel(list3, map1, model.useAmbientOcclusion(), model.isGui3d(), true, model.getParticleIcon(), model.getTransforms(), model.getOverrides());
      Reflector.SimpleBakedModel_generalQuads.setValue(simplebakedmodel, list);
      Reflector.SimpleBakedModel_faceQuads.setValue(simplebakedmodel, map);
      return simplebakedmodel;
   }

   public static List duplicateQuadList(List<BakedQuad> list) {
      ArrayList<BakedQuad> list2 = new ArrayList<BakedQuad>();
      for (BakedQuad quad : list) {
         BakedQuad quad2 = ModelUtils.duplicateQuad(quad);
         list2.add(quad2);
      }
      return list2;
   }

   public static BakedQuad duplicateQuad(BakedQuad quad) {
      return new BakedQuad((int[])quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
   }
}
