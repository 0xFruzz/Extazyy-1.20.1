package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class ProgressScreen extends Screen implements ProgressListener {
   @Nullable
   private Component header;
   @Nullable
   private Component stage;
   private int progress;
   private boolean stop;
   private final boolean clearScreenAfterStop;
   private CustomLoadingScreen customLoadingScreen;

   public ProgressScreen(boolean pClearScreenAfterStop) {
      super(GameNarrator.NO_TITLE);
      this.clearScreenAfterStop = pClearScreenAfterStop;
      this.customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   protected boolean shouldNarrateNavigation() {
      return false;
   }

   public void progressStartNoAbort(Component pComponent) {
      this.progressStart(pComponent);
   }

   public void progressStart(Component pComponent) {
      this.header = pComponent;
      this.progressStage(Component.translatable("progress.working"));
   }

   public void progressStage(Component pComponent) {
      this.stage = pComponent;
      this.progressStagePercentage(0);
   }

   public void progressStagePercentage(int pProgress) {
      this.progress = pProgress;
   }

   public void stop() {
      this.stop = true;
   }

   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.stop) {
         if (this.clearScreenAfterStop) {
            this.minecraft.setScreen((Screen)null);
         }
      } else {
         if (this.customLoadingScreen != null && this.minecraft.level == null) {
            this.customLoadingScreen.drawBackground(this.width, this.height);
         } else {
            this.renderBackground(pGuiGraphics);
         }

         if (this.progress > 0) {
            if (this.header != null) {
               pGuiGraphics.drawCenteredString(this.font, this.header, this.width / 2, 70, 16777215);
            }

            if (this.stage != null && this.progress != 0) {
               pGuiGraphics.drawCenteredString(this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
            }
         }

         super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      }

   }
}