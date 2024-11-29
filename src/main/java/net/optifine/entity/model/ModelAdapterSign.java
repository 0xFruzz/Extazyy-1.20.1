package net.optifine.entity.model;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class ModelAdapterSign extends ModelAdapter {
   public ModelAdapterSign() {
      super(BlockEntityType.SIGN, "sign", 0.0F);
   }

   public Model makeModel() {
      return new SignRenderer.SignModel(bakeModelLayer(ModelLayers.createSignModelName(WoodType.OAK)));
   }

   public ModelPart getModelRenderer(Model model, String modelPart) {
      if (!(model instanceof SignRenderer.SignModel signrenderer$signmodel)) {
         return null;
      } else if (modelPart.equals("board")) {
         return signrenderer$signmodel.root.getChildModelDeep("sign");
      } else {
         return modelPart.equals("stick") ? signrenderer$signmodel.root.getChildModelDeep("stick") : null;
      }
   }

   public String[] getModelRendererNames() {
      return new String[]{"board", "stick"};
   }

   public IEntityRenderer makeEntityRender(Model modelBase, float shadowSize, RendererCache rendererCache, int index) {
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BlockEntityRenderer blockentityrenderer = rendererCache.get(BlockEntityType.SIGN, index, () -> {
         return new SignRenderer(blockentityrenderdispatcher.getContext());
      });
      if (!(blockentityrenderer instanceof SignRenderer)) {
         return null;
      } else if (!Reflector.TileEntitySignRenderer_signModels.exists()) {
         Config.warn("Field not found: TileEntitySignRenderer.signModels");
         return null;
      } else {
         Map<WoodType, SignRenderer.SignModel> map = (Map)Reflector.getFieldValue(blockentityrenderer, Reflector.TileEntitySignRenderer_signModels);
         if (map == null) {
            Config.warn("Field not found: TileEntitySignRenderer.signModels");
            return null;
         } else {
            if (map instanceof ImmutableMap) {
               map = new HashMap<>(map);
               Reflector.TileEntitySignRenderer_signModels.setValue(blockentityrenderer, map);
            }

            for(WoodType woodtype : new HashSet<>(map.keySet())) {
               map.put(woodtype, (SignRenderer.SignModel)modelBase);
            }

            return blockentityrenderer;
         }
      }
   }
}