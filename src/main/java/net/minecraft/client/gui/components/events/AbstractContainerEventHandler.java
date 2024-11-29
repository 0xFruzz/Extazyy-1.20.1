package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerEventHandler implements ContainerEventHandler {
   @Nullable
   private GuiEventListener focused;
   private boolean isDragging;

   public final boolean isDragging() {
      return this.isDragging;
   }

   public final void setDragging(boolean pDragging) {
      this.isDragging = pDragging;
   }

   @Nullable
   public GuiEventListener getFocused() {
      return this.focused;
   }

   public void setFocused(@Nullable GuiEventListener pListener) {
      if (this.focused != null) {
         this.focused.setFocused(false);
      }

      if (pListener != null) {
         pListener.setFocused(true);
      }

      this.focused = pListener;
   }
}