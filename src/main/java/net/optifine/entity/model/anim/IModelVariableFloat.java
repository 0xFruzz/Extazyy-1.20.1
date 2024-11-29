package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionFloat;

public interface IModelVariableFloat extends IExpressionFloat, IModelVariable {
   float getValue();

   void setValue(float var1);

   default void setValue(IExpression expr) {
      float f = ((IExpressionFloat)expr).eval();
      this.setValue(f);
   }
}