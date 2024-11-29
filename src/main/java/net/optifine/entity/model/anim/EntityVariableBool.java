package net.optifine.entity.model.anim;

import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

public class EntityVariableBool implements IModelVariableBool {
   private String name;
   private EntityRenderDispatcher renderManager;

   public EntityVariableBool(String name) {
      this.name = name;
      this.renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
   }

   public boolean eval() {
      return this.getValue();
   }

   public boolean getValue() {
      SynchedEntityData synchedentitydata = this.getEntityData();
      if (synchedentitydata == null) {
         return false;
      } else if (synchedentitydata.modelVariables == null) {
         return false;
      } else {
         Boolean obool = (Boolean)synchedentitydata.modelVariables.get(this.name);
         return obool == null ? false : obool;
      }
   }

   public void setValue(boolean value) {
      SynchedEntityData synchedentitydata = this.getEntityData();
      if (synchedentitydata != null) {
         if (synchedentitydata.modelVariables == null) {
            synchedentitydata.modelVariables = new HashMap<>();
         }

         synchedentitydata.modelVariables.put(this.name, value);
      }
   }

   private SynchedEntityData getEntityData() {
      Entity entity = this.renderManager.getRenderedEntity();
      return entity == null ? null : entity.getEntityData();
   }
}