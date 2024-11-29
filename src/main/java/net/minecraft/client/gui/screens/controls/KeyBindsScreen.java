package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyBindsScreen extends OptionsSubScreen {
   @Nullable
   public KeyMapping selectedKey;
   public long lastKeySelection;
   private KeyBindsList keyBindsList;
   private Button resetButton;

   public KeyBindsScreen(Screen pLastScreen, Options pOptions) {
      super(pLastScreen, pOptions, Component.translatable("controls.keybinds.title"));
   }

   protected void init() {
      this.keyBindsList = new KeyBindsList(this, this.minecraft);
      this.addWidget(this.keyBindsList);
      this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("controls.resetAll"), (p_269619_) -> {
         for(KeyMapping keymapping : this.options.keyMappings) {
            keymapping.setKey(keymapping.getDefaultKey());
         }

         this.keyBindsList.resetMappingAndUpdateButtons();
      }).bounds(this.width / 2 - 155, this.height - 29, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p_280847_) -> {
         this.minecraft.setScreen(this.lastScreen);
      }).bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20).build());
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.selectedKey != null) {
         this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(pButton));
         this.selectedKey = null;
         this.keyBindsList.resetMappingAndUpdateButtons();
         return true;
      } else {
         return super.mouseClicked(pMouseX, pMouseY, pButton);
      }
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.selectedKey != null) {
         if (pKeyCode == 256) {
            this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
         } else {
            this.options.setKey(this.selectedKey, InputConstants.getKey(pKeyCode, pScanCode));
         }

         this.selectedKey = null;
         this.lastKeySelection = Util.getMillis();
         this.keyBindsList.resetMappingAndUpdateButtons();
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      this.keyBindsList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      boolean flag = false;

      for(KeyMapping keymapping : this.options.keyMappings) {
         if (!keymapping.isDefault()) {
            flag = true;
            break;
         }
      }

      this.resetButton.active = flag;
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}