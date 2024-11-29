package net.optifine.entity.model;

import net.minecraft.client.model.geom.ModelPart;
import net.optifine.entity.model.anim.ModelUpdater;

public class CustomModelRenderer {
   private String modelPart;
   private boolean attach;
   private ModelPart modelRenderer;
   private ModelUpdater modelUpdater;

   public CustomModelRenderer(String modelPart, boolean attach, ModelPart modelRenderer, ModelUpdater modelUpdater) {
      this.modelPart = modelPart;
      this.attach = attach;
      this.modelRenderer = modelRenderer;
      this.modelUpdater = modelUpdater;
   }

   public ModelPart getModelRenderer() {
      return this.modelRenderer;
   }

   public String getModelPart() {
      return this.modelPart;
   }

   public boolean isAttach() {
      return this.attach;
   }

   public ModelUpdater getModelUpdater() {
      return this.modelUpdater;
   }
}