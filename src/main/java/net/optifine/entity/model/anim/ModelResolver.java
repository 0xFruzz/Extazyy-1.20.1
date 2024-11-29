package net.optifine.entity.model.anim;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.optifine.Config;
import net.optifine.entity.model.CustomModelRenderer;
import net.optifine.entity.model.ModelAdapter;
import net.optifine.expr.IExpression;
import net.optifine.util.Either;

public class ModelResolver implements IModelResolver {
   private ModelAdapter modelAdapter;
   private Model model;
   private CustomModelRenderer[] customModelRenderers;
   private ModelPart thisModelRenderer;
   private ModelPart partModelRenderer;
   private IRenderResolver renderResolver;

   public ModelResolver(ModelAdapter modelAdapter, Model model, CustomModelRenderer[] customModelRenderers) {
      this.modelAdapter = modelAdapter;
      this.model = model;
      this.customModelRenderers = customModelRenderers;
      Either<EntityType, BlockEntityType> either = modelAdapter.getType();
      if (either.getRight().isPresent()) {
         this.renderResolver = new RenderResolverTileEntity();
      } else {
         this.renderResolver = new RenderResolverEntity();
      }

   }

   public IExpression getExpression(String name) {
      IExpression iexpression = this.getModelVariable(name);
      if (iexpression != null) {
         return iexpression;
      } else {
         IExpression iexpression1 = this.renderResolver.getParameter(name);
         return iexpression1 != null ? iexpression1 : null;
      }
   }

   public ModelPart getModelRenderer(String name) {
      if (name == null) {
         return null;
      } else if (name.indexOf(":") >= 0) {
         String[] astring = Config.tokenize(name, ":");
         ModelPart modelpart3 = this.getModelRenderer(astring[0]);

         for(int j = 1; j < astring.length; ++j) {
            String s = astring[j];
            ModelPart modelpart4 = modelpart3.getChildDeep(s);
            if (modelpart4 == null) {
               return null;
            }

            modelpart3 = modelpart4;
         }

         return modelpart3;
      } else if (this.thisModelRenderer != null && name.equals("this")) {
         return this.thisModelRenderer;
      } else if (this.partModelRenderer != null && name.equals("part")) {
         return this.partModelRenderer;
      } else {
         ModelPart modelpart = this.modelAdapter.getModelRenderer(this.model, name);
         if (modelpart != null) {
            return modelpart;
         } else {
            for(int i = 0; i < this.customModelRenderers.length; ++i) {
               CustomModelRenderer custommodelrenderer = this.customModelRenderers[i];
               ModelPart modelpart1 = custommodelrenderer.getModelRenderer();
               if (name.equals(modelpart1.getId())) {
                  return modelpart1;
               }

               ModelPart modelpart2 = modelpart1.getChildDeep(name);
               if (modelpart2 != null) {
                  return modelpart2;
               }
            }

            return null;
         }
      }
   }

   public IModelVariable getModelVariable(String name) {
      String[] astring = Config.tokenize(name, ".");
      if (astring.length != 2) {
         return null;
      } else {
         String s = astring[0];
         String s1 = astring[1];
         if (s.equals("var") && !this.renderResolver.isTileEntity()) {
            return new EntityVariableFloat(name);
         } else if (s.equals("varb") && !this.renderResolver.isTileEntity()) {
            return new EntityVariableBool(name);
         } else if (s.equals("render")) {
            return !this.renderResolver.isTileEntity() ? RendererVariableFloat.parse(s1) : null;
         } else {
            ModelPart modelpart = this.getModelRenderer(s);
            if (modelpart == null) {
               return null;
            } else {
               ModelVariableType modelvariabletype = ModelVariableType.parse(s1);
               return modelvariabletype == null ? null : modelvariabletype.makeModelVariable(name, modelpart);
            }
         }
      }
   }

   public void setPartModelRenderer(ModelPart partModelRenderer) {
      this.partModelRenderer = partModelRenderer;
   }

   public void setThisModelRenderer(ModelPart thisModelRenderer) {
      this.thisModelRenderer = thisModelRenderer;
   }
}