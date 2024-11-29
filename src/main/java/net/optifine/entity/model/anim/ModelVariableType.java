package net.optifine.entity.model.anim;

import net.minecraft.client.model.geom.ModelPart;
import net.optifine.Config;
import net.optifine.expr.ExpressionType;

public enum ModelVariableType {
   POS_X("tx"),
   POS_Y("ty"),
   POS_Z("tz"),
   ANGLE_X("rx"),
   ANGLE_Y("ry"),
   ANGLE_Z("rz"),
   SCALE_X("sx"),
   SCALE_Y("sy"),
   SCALE_Z("sz"),
   VISIBLE("visible", ExpressionType.BOOL),
   VISIBLE_BOXES("visible_boxes", ExpressionType.BOOL);

   private String name;
   private ExpressionType type;
   public static ModelVariableType[] VALUES = values();

   private ModelVariableType(String name) {
      this(name, ExpressionType.FLOAT);
   }

   private ModelVariableType(String name, ExpressionType type) {
      this.name = name;
      this.type = type;
   }

   public String getName() {
      return this.name;
   }

   public ExpressionType getType() {
      return this.type;
   }

   public float getFloat(ModelPart mr) {
      switch (this) {
         case POS_X:
            return mr.x;
         case POS_Y:
            return mr.y;
         case POS_Z:
            return mr.z;
         case ANGLE_X:
            return mr.xRot;
         case ANGLE_Y:
            return mr.yRot;
         case ANGLE_Z:
            return mr.zRot;
         case SCALE_X:
            return mr.xScale;
         case SCALE_Y:
            return mr.yScale;
         case SCALE_Z:
            return mr.zScale;
         default:
            Config.warn("GetFloat not supported for: " + this);
            return 0.0F;
      }
   }

   public void setFloat(ModelPart mr, float val) {
      switch (this) {
         case POS_X:
            mr.x = val;
            return;
         case POS_Y:
            mr.y = val;
            return;
         case POS_Z:
            mr.z = val;
            return;
         case ANGLE_X:
            mr.xRot = val;
            return;
         case ANGLE_Y:
            mr.yRot = val;
            return;
         case ANGLE_Z:
            mr.zRot = val;
            return;
         case SCALE_X:
            mr.xScale = val;
            return;
         case SCALE_Y:
            mr.yScale = val;
            return;
         case SCALE_Z:
            mr.zScale = val;
            return;
         default:
            Config.warn("SetFloat not supported for: " + this);
      }
   }

   public boolean getBool(ModelPart mr) {
      switch (this) {
         case VISIBLE:
            return mr.visible;
         case VISIBLE_BOXES:
            return !mr.skipDraw;
         default:
            Config.warn("GetBool not supported for: " + this);
            return false;
      }
   }

   public void setBool(ModelPart mr, boolean val) {
      switch (this) {
         case VISIBLE:
            mr.visible = val;
            return;
         case VISIBLE_BOXES:
            mr.skipDraw = !val;
            return;
         default:
            Config.warn("SetBool not supported for: " + this);
      }
   }

   public IModelVariable makeModelVariable(String name, ModelPart mr) {
      if (this.type == ExpressionType.FLOAT) {
         return new ModelVariableFloat(name, mr, this);
      } else if (this.type == ExpressionType.BOOL) {
         return new ModelVariableBool(name, mr, this);
      } else {
         Config.warn("Unknown model variable type: " + this.type);
         return null;
      }
   }

   public static ModelVariableType parse(String str) {
      for(int i = 0; i < VALUES.length; ++i) {
         ModelVariableType modelvariabletype = VALUES[i];
         if (modelvariabletype.getName().equals(str)) {
            return modelvariabletype;
         }
      }

      return null;
   }
}