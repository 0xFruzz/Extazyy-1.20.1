package ru.fruzz.extazyy.misc.event.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.event.events.Event;

public class RenderEvent3D extends Event {

    private PoseStack poseStack;
    private float partialTicks;

    public RenderEvent3D() {
        this.poseStack = new PoseStack();
        this.partialTicks = 1;
    }

    public void setPoseStack(PoseStack poseStack) {
        this.poseStack = poseStack;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}
