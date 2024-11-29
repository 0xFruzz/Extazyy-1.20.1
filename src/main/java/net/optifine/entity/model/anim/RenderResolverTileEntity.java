package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;

public class RenderResolverTileEntity implements IRenderResolver {
   public IExpression getParameter(String name) {
      RenderEntityParameterBool renderentityparameterbool = RenderEntityParameterBool.parse(name);
      if (renderentityparameterbool != null && renderentityparameterbool.isBlockEntity()) {
         return renderentityparameterbool;
      } else {
         RenderEntityParameterFloat renderentityparameterfloat = RenderEntityParameterFloat.parse(name);
         return renderentityparameterfloat != null && renderentityparameterfloat.isBlockEntity() ? renderentityparameterfloat : null;
      }
   }

   public boolean isTileEntity() {
      return true;
   }
}