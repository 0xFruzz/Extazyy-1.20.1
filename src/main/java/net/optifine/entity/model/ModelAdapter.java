package net.optifine.entity.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.util.Either;

public abstract class ModelAdapter {
   private Either<EntityType, BlockEntityType> type;
   private String name;
   private float shadowSize;
   private String[] aliases;

   public ModelAdapter(EntityType entityType, String name, float shadowSize) {
      this(Either.makeLeft(entityType), name, shadowSize, (String[])null);
   }

   public ModelAdapter(EntityType entityType, String name, float shadowSize, String[] aliases) {
      this(Either.makeLeft(entityType), name, shadowSize, aliases);
   }

   public ModelAdapter(BlockEntityType tileEntityType, String name, float shadowSize) {
      this(Either.makeRight(tileEntityType), name, shadowSize, (String[])null);
   }

   public ModelAdapter(BlockEntityType tileEntityType, String name, float shadowSize, String[] aliases) {
      this(Either.makeRight(tileEntityType), name, shadowSize, aliases);
   }

   public ModelAdapter(Either<EntityType, BlockEntityType> type, String name, float shadowSize, String[] aliases) {
      this.type = type;
      this.name = name;
      this.shadowSize = shadowSize;
      this.aliases = aliases;
   }

   public Either<EntityType, BlockEntityType> getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public float getShadowSize() {
      return this.shadowSize;
   }

   public abstract Model makeModel();

   public abstract ModelPart getModelRenderer(Model var1, String var2);

   public abstract String[] getModelRendererNames();

   public abstract IEntityRenderer makeEntityRender(Model var1, float var2, RendererCache var3, int var4);

   public boolean setTextureLocation(IEntityRenderer er, ResourceLocation textureLocation) {
      return false;
   }

   public ModelPart[] getModelRenderers(Model model) {
      String[] astring = this.getModelRendererNames();
      List<ModelPart> list = new ArrayList<>();

      for(int i = 0; i < astring.length; ++i) {
         String s = astring[i];
         ModelPart modelpart = this.getModelRenderer(model, s);
         if (modelpart != null) {
            list.add(modelpart);
         }
      }

      return list.toArray(new ModelPart[list.size()]);
   }

   public static ModelPart bakeModelLayer(ModelLayerLocation loc) {
      return Minecraft.getInstance().getEntityRenderDispatcher().getContext().bakeLayer(loc);
   }
}