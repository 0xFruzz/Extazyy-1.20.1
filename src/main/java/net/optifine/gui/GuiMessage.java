package net.optifine.gui;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.optifine.Config;

public class GuiMessage extends GuiScreenOF {
   private Screen parentScreen;
   private Component messageLine1;
   private Component messageLine2;
   private final List<FormattedCharSequence> listLines2 = Lists.newArrayList();
   protected String confirmButtonText;
   private int ticksUntilEnable;

   public GuiMessage(Screen parentScreen, String line1, String line2) {
      super(Component.translatable("of.options.detailsTitle"));
      this.parentScreen = parentScreen;
      this.messageLine1 = Component.literal(line1);
      this.messageLine2 = Component.literal(line2);
      this.confirmButtonText = I18n.get("gui.done");
   }

   public void init() {
      this.addRenderableWidget(new GuiButtonOF(0, this.width / 2 - 100, this.height / 6 + 96, this.confirmButtonText));
      this.listLines2.clear();
      this.listLines2.addAll(this.minecraft.font.split(this.messageLine2, this.width - 50));
   }

   protected void actionPerformed(AbstractWidget button) {
      Config.getMinecraft().setScreen(this.parentScreen);
   }

   public void render(GuiGraphics graphicsIn, int mouseX, int mouseY, float partialTicks) {
      this.renderBackground(graphicsIn);
      drawCenteredString(graphicsIn, this.fontRenderer, this.messageLine1, this.width / 2, 70, 16777215);
      int i = 90;

      for(FormattedCharSequence formattedcharsequence : this.listLines2) {
         drawCenteredString(graphicsIn, this.fontRenderer, formattedcharsequence, this.width / 2, i, 16777215);
         i += 9;
      }

      super.render(graphicsIn, mouseX, mouseY, partialTicks);
   }

   public void setButtonDelay(int ticksUntilEnable) {
      this.ticksUntilEnable = ticksUntilEnable;

      for(Button button : (List<Button>)(List<?>)this.getButtonList()) {
         button.active = false;
      }

   }

   public void tick() {
      super.tick();
      if (--this.ticksUntilEnable == 0) {
         for(Button button : (List<Button>)(List<?>)this.getButtonList()) {
            button.active = true;
         }
      }

   }
}
