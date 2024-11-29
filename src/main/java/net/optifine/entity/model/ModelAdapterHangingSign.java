package net.optifine.entity.model;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterHangingSign extends ModelAdapter {
   private static Map<String, String> mapParts = makeMapParts();

   public ModelAdapterHangingSign() {
      super(BlockEntityType.HANGING_SIGN, "hanging_sign", 0.0F);
   }

   public Model makeModel() {
      return new HangingSignRenderer.HangingSignModel(bakeModelLayer(ModelLayers.createHangingSignModelName(WoodType.OAK)));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof HangingSignRenderer.HangingSignModel hangingsignrenderer$hangingsignmodel)) {
         return null;
      } else if (mapParts.containsKey(modelPart)) {
         String s = mapParts.get(modelPart);
         return hangingsignrenderer$hangingsignmodel.root.getChildModelDeep(s);
      } else {
         return null;
      }
   }

   public String[] getModelRendererNames() {
      return mapParts.keySet().toArray(new String[0]);
   }

   private static Map<String, String> makeMapParts() {
      Map<String, String> map = new LinkedHashMap<>();
      map.put("board", "board");
      map.put("plank", "plank");
      map.put("chains", "normalChains");
      map.put("chain_left1", "chainL1");
      map.put("chain_left2", "chainL2");
      map.put("chain_right1", "chainR1");
      map.put("chain_right2", "chainR2");
      map.put("chains_v", "vChains");
      return map;
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.HANGING_SIGN, index, () -> {
         return new HangingSignRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof HangingSignRenderer)) {
         return null;
      } else if (!Reflector.TileEntityHangingSignRenderer_hangingSignModels.exists()) {
         Config.warn("Field not found: TileEntityHangingSignRenderer.hangingSignModels");
         return null;
      } else {
         Map<WoodType, HangingSignRenderer.HangingSignModel> map = (Map)Reflector.getFieldValue(blockentityrenderer, Reflector.TileEntityHangingSignRenderer_hangingSignModels);
         if (map == null) {
            Config.warn("Field not found: TileEntityHangingSignRenderer.hangingSignModels");
            return null;
         } else {
            if (map instanceof ImmutableMap) {
               map = new HashMap<>(map);
               Reflector.TileEntityHangingSignRenderer_hangingSignModels.setValue(blockentityrenderer, map);
            }

            for(WoodType woodtype : new HashSet<>(map.keySet())) {
               map.put(woodtype, (HangingSignRenderer.HangingSignModel)modelBase);
            }

            return blockentityrenderer;
         }
      }
   }
}