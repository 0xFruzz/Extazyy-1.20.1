package net.optifine.config;

import net.minecraft.client.OptionInstance;

public interface SliderableValueSetInt<T> extends OptionInstance.SliderableValueSet<T> {
   OptionInstance.IntRangeBase getIntRange();
}