package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionBool;

public interface IModelVariableBool extends IExpressionBool, IModelVariable {
   boolean getValue();

   void setValue(boolean var1);

   default void setValue(IExpression expr) {
      boolean flag = ((IExpressionBool)expr).eval();
      this.setValue(flag);
   }
}