package net.optifine.config;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

public class SliderPercentageOptionOF extends OptionInstance {
   public SliderPercentageOptionOF(String name, double defVal) {
      super(name, OptionInstance.noTooltip(), (labelIn, valuein) -> {
         return Options.genericValueLabel(labelIn, Component.translatable(name, valuein));
      }, OptionInstance.UnitDouble.INSTANCE, defVal, (val) -> {
      });
   }

   public SliderPercentageOptionOF(String name, int valueMin, int valueMax, int step, int valueDef) {
      super(name, OptionInstance.noTooltip(), (labelIn, valuein) -> {
         return Options.genericValueLabel(labelIn, Component.translatable(name, valuein));
      }, (new OptionInstance.IntRange(valueMin / step, valueMax / step)).xmap((val) -> {
         return val * step;
      }, (val) -> {
         return val / step;
      }), Codec.intRange(valueMin, valueMax), valueDef, (val) -> {
      });
   }

   public SliderPercentageOptionOF(String name, int valueMin, int valueMax, int[] stepValues, int valueDef) {
      super(name, OptionInstance.noTooltip(), (labelIn, valuein) -> {
         return Options.genericValueLabel(labelIn, Component.translatable(name, valuein));
      }, (new OptionInstance.IntRange(0, stepValues.length - 1)).xmap((val) -> {
         return stepValues[val];
      }, (val) -> {
         return Options.indexOf(val, stepValues);
      }), Codec.intRange(valueMin, valueMax), valueDef, (val) -> {
      });
   }

   public double getOptionValue() {
      Options options = Minecraft.getInstance().options;
      return options.getOptionFloatValueOF(this);
   }

   public void setOptionValue(double value) {
      Options options = Minecraft.getInstance().options;
      options.setOptionFloatValueOF(this, value);
   }

   public Component getOptionText() {
      Options options = Minecraft.getInstance().options;
      return options.getKeyComponentOF(this);
   }
}