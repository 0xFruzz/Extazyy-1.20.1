package ru.fruzz.extazyy.misc.event.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.fruzz.extazyy.misc.event.events.Event;


public class RenderNameTags extends Event {


        private PoseStack guiGraphics;
        private float partialTicks;

        public RenderNameTags() {
            this.guiGraphics = null;
            this.partialTicks = 1;
        }

        public void setPoseStack(PoseStack poseStack) {
            this.guiGraphics = poseStack;
        }

        public void setPartialTicks(float partialTicks) {
            this.partialTicks = partialTicks;
        }

        public float getPartialTicks() {
            return partialTicks;
        }

        public PoseStack getGuiGraphics() {
            return guiGraphics;
        }
    }


