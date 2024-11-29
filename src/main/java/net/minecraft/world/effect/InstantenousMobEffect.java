package net.minecraft.world.effect;

public class InstantenousMobEffect extends MobEffect {
   public InstantenousMobEffect(MobEffectCategory pCategory, int pColor) {
      super(pCategory, pColor);
   }

   public boolean isInstantenous() {
      return true;
   }

   public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
      return pDuration >= 1;
   }
}