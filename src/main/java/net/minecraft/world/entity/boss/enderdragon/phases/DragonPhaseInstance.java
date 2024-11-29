package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface DragonPhaseInstance {
   boolean isSitting();

   void doClientTick();

   void doServerTick();

   void onCrystalDestroyed(EndCrystal pCrystal, BlockPos pPos, DamageSource pDamageSource, @Nullable Player pPlayer);

   void begin();

   void end();

   float getFlySpeed();

   float getTurnSpeed();

   EnderDragonPhase<? extends DragonPhaseInstance> getPhase();

   @Nullable
   Vec3 getFlyTargetLocation();

   float onHurt(DamageSource pDamageSource, float pAmount);
}