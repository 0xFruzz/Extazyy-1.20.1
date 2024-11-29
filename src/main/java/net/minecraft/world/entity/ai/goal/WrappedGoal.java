package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class WrappedGoal extends Goal {
   private final Goal goal;
   private final int priority;
   private boolean isRunning;

   public WrappedGoal(int pPriority, Goal pGoal) {
      this.priority = pPriority;
      this.goal = pGoal;
   }

   public boolean canBeReplacedBy(WrappedGoal pOther) {
      return this.isInterruptable() && pOther.getPriority() < this.getPriority();
   }

   public boolean canUse() {
      return this.goal.canUse();
   }

   public boolean canContinueToUse() {
      return this.goal.canContinueToUse();
   }

   public boolean isInterruptable() {
      return this.goal.isInterruptable();
   }

   public void start() {
      if (!this.isRunning) {
         this.isRunning = true;
         this.goal.start();
      }
   }

   public void stop() {
      if (this.isRunning) {
         this.isRunning = false;
         this.goal.stop();
      }
   }

   public boolean requiresUpdateEveryTick() {
      return this.goal.requiresUpdateEveryTick();
   }

   protected int adjustedTickDelay(int pAdjustment) {
      return this.goal.adjustedTickDelay(pAdjustment);
   }

   public void tick() {
      this.goal.tick();
   }

   public void setFlags(EnumSet<Goal.Flag> pFlagSet) {
      this.goal.setFlags(pFlagSet);
   }

   public EnumSet<Goal.Flag> getFlags() {
      return this.goal.getFlags();
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public int getPriority() {
      return this.priority;
   }

   public Goal getGoal() {
      return this.goal;
   }

   public boolean equals(@Nullable Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther != null && this.getClass() == pOther.getClass() ? this.goal.equals(((WrappedGoal)pOther).goal) : false;
      }
   }

   public int hashCode() {
      return this.goal.hashCode();
   }
}