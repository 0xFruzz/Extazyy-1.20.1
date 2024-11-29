package net.optifine.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class IteratableOptionOF extends OptionInstance {
   public IteratableOptionOF(String nameIn) {
      super(nameIn, OptionInstance.noTooltip(), (labelIn, valueIn) -> {
         return (Boolean)valueIn ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
      }, OptionInstance.BOOLEAN_VALUES, false, (valueIn) -> {
      });
   }

   public void nextOptionValue(int dirIn) {
      Options options = Minecraft.getInstance().options;
      options.setOptionValueOF(this, dirIn);
   }

   public Component getOptionText() {
      Options options = Minecraft.getInstance().options;
      return options.getKeyComponentOF(this);
   }
}