package net.optifine.gui;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

public class OptionFullscreenResolution {
   public static OptionInstance make() {
      Window window = Minecraft.getInstance().getWindow();
      Monitor monitor = window.findBestMonitor();
      int i;
      if (monitor == null) {
         i = -1;
      } else {
         Optional<VideoMode> optional = window.getPreferredFullscreenVideoMode();
         i = optional.map(monitor::getVideoModeIndex).orElse(-1);
      }

      OptionInstance<Integer> optioninstance = new OptionInstance<>("options.fullscreen.resolution", OptionInstance.noTooltip(), (p_232804_1_, p_232804_2_) -> {
         if (monitor == null) {
            return Component.translatable("options.fullscreen.unavailable");
         } else {
            return p_232804_2_ == -1 ? Options.genericValueLabel(p_232804_1_, Component.translatable("options.fullscreen.current")) : Options.genericValueLabel(p_232804_1_, Component.literal(monitor.getMode(p_232804_2_).toString()));
         }
      }, new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1), i, (p_232800_2_) -> {
         if (monitor != null) {
            window.setPreferredFullscreenVideoMode(p_232800_2_ == -1 ? Optional.empty() : Optional.of(monitor.getMode(p_232800_2_)));
         }

      });
      return optioninstance;
   }
}