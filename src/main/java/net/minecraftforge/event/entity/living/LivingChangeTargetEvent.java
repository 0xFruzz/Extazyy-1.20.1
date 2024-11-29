package net.minecraftforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

public class LivingChangeTargetEvent extends LivingEvent {
   public LivingEntity getNewTarget() {
      return null;
   }

   public static enum LivingTargetType implements ILivingTargetType {
      MOB_TARGET,
      BEHAVIOR_TARGET;
   }
}