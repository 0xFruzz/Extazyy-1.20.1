package net.optifine.gui;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class GuiOptionButtonOF extends Button implements IOptionControl {
   private final OptionInstance option;

   public GuiOptionButtonOF(int xIn, int yIn, int widthIn, int heightIn, OptionInstance optionIn, Component textIn, Button.OnPress pressableIn, Button.CreateNarration narrationIn) {
      super(xIn, yIn, widthIn, heightIn, textIn, pressableIn, narrationIn);
      this.option = optionIn;
   }

   public OptionInstance getOption() {
      return this.option;
   }

   public OptionInstance getControlOption() {
      return this.option;
   }
}