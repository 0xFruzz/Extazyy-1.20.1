package net.optifine.entity.model.anim;

import net.minecraft.client.model.geom.ModelPart;
import net.optifine.expr.IExpressionBool;

public class ModelVariableBool implements IExpressionBool, IModelVariableBool {
   private String name;
   private ModelPart modelRenderer;
   private ModelVariableType enumModelVariable;

   public ModelVariableBool(String name, ModelPart modelRenderer, ModelVariableType enumModelVariable) {
      this.name = name;
      this.modelRenderer = modelRenderer;
      this.enumModelVariable = enumModelVariable;
   }

   public boolean eval() {
      return this.getValue();
   }

   public boolean getValue() {
      return this.enumModelVariable.getBool(this.modelRenderer);
   }

   public void setValue(boolean value) {
      this.enumModelVariable.setBool(this.modelRenderer, value);
   }

   public String toString() {
      return this.name;
   }
}