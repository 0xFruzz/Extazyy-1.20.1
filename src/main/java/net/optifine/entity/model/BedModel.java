package net.optifine.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.optifine.Config;
import net.optifine.reflect.Reflector;

public class BedModel extends Model {
   public ModelPart headPiece;
   public ModelPart footPiece;
   public ModelPart[] legs = new ModelPart[4];

   public BedModel() {
      super(RenderType::entityCutoutNoCull);
      BlockEntityRenderDispatcher blockentityrenderdispatcher = Config.getMinecraft().getBlockEntityRenderDispatcher();
      BedRenderer bedrenderer = new BedRenderer(blockentityrenderdispatcher.getContext());
      ModelPart modelpart = (ModelPart)Reflector.TileEntityBedRenderer_headModel.getValue(bedrenderer);
      if (modelpart != null) {
         this.headPiece = modelpart.getChild("main");
         this.legs[0] = modelpart.getChild("left_leg");
         this.legs[1] = modelpart.getChild("right_leg");
      }

      ModelPart modelpart1 = (ModelPart)Reflector.TileEntityBedRenderer_footModel.getValue(bedrenderer);
      if (modelpart1 != null) {
         this.footPiece = modelpart1.getChild("main");
         this.legs[2] = modelpart1.getChild("left_leg");
         this.legs[3] = modelpart1.getChild("right_leg");
      }

   }

   public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
   }

   public BlockEntityRenderer updateRenderer(BlockEntityRenderer renderer) {
      if (!Reflector.TileEntityBedRenderer_headModel.exists()) {
         Config.warn("Field not found: TileEntityBedRenderer.head");
         return null;
      } else if (!Reflector.TileEntityBedRenderer_footModel.exists()) {
         Config.warn("Field not found: TileEntityBedRenderer.footModel");
         return null;
      } else {
         ModelPart modelpart = (ModelPart)Reflector.TileEntityBedRenderer_headModel.getValue(renderer);
         if (modelpart != null) {
            modelpart.addChildModel("main", this.headPiece);
            modelpart.addChildModel("left_leg", this.legs[0]);
            modelpart.addChildModel("right_leg", this.legs[1]);
         }

         ModelPart modelpart1 = (ModelPart)Reflector.TileEntityBedRenderer_footModel.getValue(renderer);
         if (modelpart1 != null) {
            modelpart1.addChildModel("main", this.footPiece);
            modelpart1.addChildModel("left_leg", this.legs[2]);
            modelpart1.addChildModel("right_leg", this.legs[3]);
         }

         return renderer;
      }
   }
}