package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements ContainerEntity {
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;

   protected AbstractMinecartContainer(EntityType<?> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected AbstractMinecartContainer(EntityType<?> pEntityType, double pX, double pY, double pZ, Level pLevel) {
      super(pEntityType, pLevel, pX, pY, pZ);
   }

   public void destroy(DamageSource pSource) {
      super.destroy(pSource);
      this.chestVehicleDestroyed(pSource, this.level(), this);
   }

   public ItemStack getItem(int pIndex) {
      return this.getChestVehicleItem(pIndex);
   }

   public ItemStack removeItem(int pIndex, int pCount) {
      return this.removeChestVehicleItem(pIndex, pCount);
   }

   public ItemStack removeItemNoUpdate(int pIndex) {
      return this.removeChestVehicleItemNoUpdate(pIndex);
   }

   public void setItem(int pIndex, ItemStack pStack) {
      this.setChestVehicleItem(pIndex, pStack);
   }

   public SlotAccess getSlot(int pSlot) {
      return this.getChestVehicleSlot(pSlot);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player pPlayer) {
      return this.isChestVehicleStillValid(pPlayer);
   }

   public void remove(Entity.RemovalReason pReason) {
      if (!this.level().isClientSide && pReason.shouldDestroy()) {
         Containers.dropContents(this.level(), this, this);
      }

      super.remove(pReason);
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.addChestVehicleSaveData(pCompound);
   }

   protected void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.readChestVehicleSaveData(pCompound);
   }

   public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      return this.interactWithContainerVehicle(pPlayer);
   }

   protected void applyNaturalSlowdown() {
      float f = 0.98F;
      if (this.lootTable == null) {
         int i = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
         f += (float)i * 0.001F;
      }

      if (this.isInWater()) {
         f *= 0.95F;
      }

      this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0D, (double)f));
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed) {
      this.lootTable = pLootTable;
      this.lootTableSeed = pLootTableSeed;
   }

   @Nullable
   public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
      if (this.lootTable != null && pPlayer.isSpectator()) {
         return null;
      } else {
         this.unpackChestVehicleLootTable(pPlayerInventory.player);
         return this.createMenu(pContainerId, pPlayerInventory);
      }
   }

   protected abstract AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory);

   @Nullable
   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(@Nullable ResourceLocation pLootTable) {
      this.lootTable = pLootTable;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long pLootTableSeed) {
      this.lootTableSeed = pLootTableSeed;
   }

   public NonNullList<ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public void clearItemStacks() {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   }
}