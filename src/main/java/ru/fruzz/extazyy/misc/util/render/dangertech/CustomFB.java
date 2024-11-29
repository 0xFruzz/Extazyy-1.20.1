package ru.fruzz.extazyy.misc.util.render.dangertech;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import ru.fruzz.extazyy.misc.util.render.dangertech.blur.Shader;

public class CustomFB extends TextureTarget {
    private final Minecraft minecraft;

    public CustomFB(boolean pUseDepthBuffer, Minecraft minecraft) {
        super(1, 1, pUseDepthBuffer, Minecraft.ON_OSX);
        this.minecraft = minecraft;
    }

    public CustomFB(Minecraft minecraft) {
        this(true, minecraft);
    }

    public void write(boolean clear) {
        if (clear) {
            clear();
        }
        super.bindWrite(false);
    }

    public void endWrite() {
        super.unbindWrite();
        minecraft.getMainRenderTarget().bindWrite(true);
    }

    public void setSampler(Shader sampler, String name) {
        sampler.setSample(name, this.getColorTextureId());
    }

    public void clear() {
        super.clear(Minecraft.ON_OSX);
    }

    public void updateSize() {
        if (this.width != Minecraft.getInstance().getMainRenderTarget().width || this.height != (Minecraft.getInstance().getMainRenderTarget().height)) {
            super.resize(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, Minecraft.ON_OSX);
        } else {
            clear();
        }
    }
}
