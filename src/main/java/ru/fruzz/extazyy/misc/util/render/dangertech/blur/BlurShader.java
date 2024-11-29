package ru.fruzz.extazyy.misc.util.render.dangertech.blur;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;

public class BlurShader extends Shader {
    private final Uniform uSize;
    private final Uniform uLocation;
    private final Uniform radius;
    private final Uniform inputResolution;
    private final Uniform brightness;
    private final Uniform quality;
    private final Uniform color1;
    private final Window window;
    private final RenderTarget input;

    public BlurShader() {
        super("blur", DefaultVertexFormat.POSITION);
        this.inputResolution = uniform("InputResolution");
        this.brightness = uniform("Brightness");
        this.quality = uniform("Quality");
        this.color1 = uniform("color1");
        this.uSize = uniform("uSize");
        this.uLocation = uniform("uLocation");
        this.radius = uniform("radius");
        this.window = Minecraft.getInstance().getWindow();
        this.input = new TextureTarget(window.getWidth(), window.getHeight(), false, Minecraft.ON_OSX);

    }

    public static void setupBuffer(RenderTarget frameBuffer) {
        if (frameBuffer.width != Minecraft.getInstance().getMainRenderTarget().width || frameBuffer.height != (Minecraft.getInstance().getMainRenderTarget().height)) {
            frameBuffer.resize(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, Minecraft.ON_OSX);
        } else {
            frameBuffer.clear(Minecraft.ON_OSX);
        }

    }

    public void setParameters(float f, float f2, float f3, float f4, float f5, int color, float f6, float f7) {
        setupBuffer(input);
        float f8 = (float) window.getGuiScale();
        radius.set(f5 * f8);
        uLocation.set(f * f8, -f2 * f8 + (float) window.getGuiScaledHeight() * f8 - f4 * f8);
        uSize.set(f3 * f8, f4 * f8);
        brightness.set(f7);
        quality.set(f6);
        color1.set(ColorUtil.r(color), ColorUtil.g(color), ColorUtil.b(color), ColorUtil.a(color));
    }

    @Override
    public void bind() {
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        input.bindWrite(false);
        GL30.glBindFramebuffer(36008, renderTarget.frameBufferId);
        GL30.glBlitFramebuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, renderTarget.width, renderTarget.height, 16384, 9729);
        renderTarget.bindWrite(false);
        inputResolution.set((float) renderTarget.width, (float) renderTarget.height);
        setSample("InputSampler", input.getColorTextureId());
        super.bind();
    }
}