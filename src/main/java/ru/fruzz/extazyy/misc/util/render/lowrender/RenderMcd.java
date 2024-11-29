package ru.fruzz.extazyy.misc.util.render.lowrender;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.render.GaussianFilter;
import ru.fruzz.extazyy.misc.util.Mine;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix4f;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static ru.fruzz.extazyy.misc.font.GlyphMap.registerBufferedImageTexture;


public class RenderMcd implements Mine {

    public static HashMap<Integer, BlurredShadow> shadowCache = new HashMap<>();
    public static void drawBlurredShadow(PoseStack matrices, float x, float y, float width, float height, int blurRadius, int color) {
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        int identifier = (int) (width * height + width * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
        } else {
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }

        setupRender();
        RenderSystem.setShaderColor(ColorUtil.getRed(color) / 255f, ColorUtil.getGreen(color) / 255f, ColorUtil.getBlue(color) / 255f, ColorUtil.getAlpha(color) / 255f);
        renderTexture(matrices, x, y, width, height, 0, 0, width, height, width, height);
        endRender();
    }

    public static void renderTexture(PoseStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).uv((u) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z).uv((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).endVertex();
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).uv((u + (float) regionWidth) / (float) textureWidth, (v) / (float) textureHeight).endVertex();
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).uv((u) / (float) textureWidth, (v + 0.0F) / (float) textureHeight).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }


    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }




    public static class BlurredShadow {
        Texture id;

        public BlurredShadow(BufferedImage bufferedImage) {
            this.id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));
            registerBufferedImageTexture(id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture(0, id);
        }
    }
}

