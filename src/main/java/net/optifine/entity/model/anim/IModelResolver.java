package net.optifine.entity.model.anim;

import net.minecraft.client.model.geom.ModelPart;
import net.optifine.expr.IExpressionResolver;

public interface IModelResolver extends IExpressionResolver {
   ModelPart getModelRenderer(String var1);

   IModelVariable getModelVariable(String var1);
}