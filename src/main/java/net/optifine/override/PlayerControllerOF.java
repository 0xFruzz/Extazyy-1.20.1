package net.optifine.override;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class PlayerControllerOF extends MultiPlayerGameMode {
   private boolean acting = false;
   private BlockPos lastClickBlockPos = null;
   private Entity lastClickEntity = null;

   public PlayerControllerOF(Minecraft mcIn, ClientPacketListener netHandler) {
      super(mcIn, netHandler);
   }

   public boolean startDestroyBlock(BlockPos loc, Direction face) {
      this.acting = true;
      this.lastClickBlockPos = loc;
      boolean flag = super.startDestroyBlock(loc, face);
      this.acting = false;
      return flag;
   }

   public boolean continueDestroyBlock(BlockPos posBlock, Direction directionFacing) {
      this.acting = true;
      this.lastClickBlockPos = posBlock;
      boolean flag = super.continueDestroyBlock(posBlock, directionFacing);
      this.acting = false;
      return flag;
   }

   public InteractionResult useItem(Player player, InteractionHand hand) {
      this.acting = true;
      InteractionResult interactionresult = super.useItem(player, hand);
      this.acting = false;
      return interactionresult;
   }

   public InteractionResult useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult rayTrace) {
      this.acting = true;
      this.lastClickBlockPos = rayTrace.getBlockPos();
      InteractionResult interactionresult = super.useItemOn(player, hand, rayTrace);
      this.acting = false;
      return interactionresult;
   }

   public InteractionResult interact(Player player, Entity target, InteractionHand hand) {
      this.lastClickEntity = target;
      return super.interact(player, target, hand);
   }

   public InteractionResult interactAt(Player player, Entity target, EntityHitResult ray, InteractionHand hand) {
      this.lastClickEntity = target;
      return super.interactAt(player, target, ray, hand);
   }

   public boolean isActing() {
      return this.acting;
   }

   public BlockPos getLastClickBlockPos() {
      return this.lastClickBlockPos;
   }

   public Entity getLastClickEntity() {
      return this.lastClickEntity;
   }
}