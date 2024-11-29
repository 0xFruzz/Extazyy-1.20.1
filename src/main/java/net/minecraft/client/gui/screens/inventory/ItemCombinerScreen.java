package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
   private final ResourceLocation menuResource;

   public ItemCombinerScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, ResourceLocation pMenuResource) {
      super(pMenu, pPlayerInventory, pTitle);
      this.menuResource = pMenuResource;
   }

   protected void subInit() {
   }

   protected void init() {
      super.init();
      this.subInit();
      this.menu.addSlotListener(this);
   }

   public void removed() {
      super.removed();
      this.menu.removeSlotListener(this);
   }

   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderFg(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
   }

   protected void renderFg(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
   }

   protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
      pGuiGraphics.blit(this.menuResource, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      this.renderErrorIcon(pGuiGraphics, this.leftPos, this.topPos);
   }

   protected abstract void renderErrorIcon(GuiGraphics pGuiGraphics, int pX, int pY);

   public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
   }

   public void slotChanged(AbstractContainerMenu pContainerToSend, int pSlotInd, ItemStack pStack) {
   }
}