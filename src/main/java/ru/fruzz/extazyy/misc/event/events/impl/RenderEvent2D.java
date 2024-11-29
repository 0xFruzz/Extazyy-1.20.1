package ru.fruzz.extazyy.misc.event.events.impl;

import ru.fruzz.extazyy.misc.event.events.Event;
import net.minecraft.client.gui.GuiGraphics;

public class RenderEvent2D extends Event {

    private GuiGraphics guiGraphics;
    private float partialTicks;

    public RenderEvent2D() {
        this.guiGraphics = null;
        this.partialTicks = 1;
    }

    public void setPoseStack(GuiGraphics poseStack) {
        this.guiGraphics = poseStack;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }
}
