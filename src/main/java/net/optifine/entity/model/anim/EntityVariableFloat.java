package net.optifine.entity.model.anim;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public class EntityVariableFloat implements IModelVariableFloat {
   private String name;
   private static EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();

   public EntityVariableFloat(String name) {
      this.name = name;
   }

   public float eval() {
      return this.getValue();
   }

   public float getValue() {
      return getEntityValue(this.name);
   }

   public static float getEntityValue(String key) {
      SynchedEntityData synchedentitydata = getEntityData();
      if (synchedentitydata == null) {
         return 0.0F;
      } else if (synchedentitydata.modelVariables == null) {
         return 0.0F;
      } else {
         Float f = (Float)synchedentitydata.modelVariables.get(key);
         return f == null ? 0.0F : f;
      }
   }

   public void setValue(float value) {
      setEntityValue(this.name, value);
   }

   public static void setEntityValue(String key, float value) {
      SynchedEntityData synchedentitydata = getEntityData();
      if (synchedentitydata != null) {
         if (synchedentitydata.modelVariables == null) {
            synchedentitydata.modelVariables = new HashMap<>();
         }

         synchedentitydata.modelVariables.put(key, value);
      }
   }

   private static SynchedEntityData getEntityData() {
      Entity entity = renderManager.getRenderedEntity();
      return entity == null ? null : entity.getEntityData();
   }
}