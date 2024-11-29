package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

public class NearestAttackableWitchTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
   private boolean canAttack = true;

   public NearestAttackableWitchTargetGoal(Raider pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate) {
      super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pTargetPredicate);
   }

   public void setCanAttack(boolean pActive) {
      this.canAttack = pActive;
   }

   public boolean canUse() {
      return this.canAttack && super.canUse();
   }
}