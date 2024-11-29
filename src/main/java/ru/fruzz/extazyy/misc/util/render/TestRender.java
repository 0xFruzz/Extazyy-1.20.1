package ru.fruzz.extazyy.misc.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import ru.fruzz.extazyy.misc.font.Texture;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import ru.fruzz.extazyy.misc.util.Mine;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Stack;

import static ru.fruzz.extazyy.misc.util.render.RenderUtil.matrixFrom;
import static ru.fruzz.extazyy.misc.util.render.RenderUtil.vertexLine;

public class TestRender implements Mine {

    //private static HudShader HUD_SHADER;

    public static final ResourceLocation star;
    public static final ResourceLocation heart;
    public static final ResourceLocation dollar;
    public static final ResourceLocation snowflake;
    public static final ResourceLocation firefly;
    public static final ResourceLocation arrow;
    public static final ResourceLocation capture;
    public static final ResourceLocation bubble;
    public static final ResourceLocation default_circle;
    public static final ResourceLocation CONTAINER_BACKGROUND;
    public static HashMap shadowCache;
    public static HashMap shadowCache1;
    static final Stack clipStack;


    public static void drawLine(@NotNull Vec3 start, @NotNull Vec3 end, @NotNull int color) {

        setupRender();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(2f);
        RenderSystem.disableDepthTest();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            PoseStack matrices = matrixFrom(start.x(), start.y, start.z);
            vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (end.x - start.x), (float) (end.y - start.y), (float) (end.z - start.z), color);

        tessellator.end();
        RenderSystem.enableCull();
        RenderSystem.lineWidth(1f);
        RenderSystem.enableDepthTest();
        endRender();

    }

    public static void addWindow(PoseStack poseStack, Rectangle rectangle) {
        Matrix4f matrix4f = poseStack.last().pose();
        Vector4f vector4f = new Vector4f(rectangle.x, rectangle.y, 0.0f, 1.0f);
        Vector4f vector4f2 = new Vector4f(rectangle.x1, rectangle.y1, 0.0f, 1.0f);
        vector4f.mulTranspose((Matrix4fc)matrix4f);
        vector4f2.mulTranspose((Matrix4fc)matrix4f);
        float f = vector4f.x();
        float f2 = vector4f.y();
        float f3 = vector4f2.x();
        float f4 = vector4f2.y();
        Rectangle rectangle2 = new Rectangle(f, f2, f3, f4);
        if (clipStack.empty()) {
            clipStack.push(rectangle2);
            TestRender.beginScissor(rectangle2.x, rectangle2.y, rectangle2.x1, rectangle2.y1);
        } else {
            Rectangle rectangle3 = (Rectangle)clipStack.peek();
            float f5 = rectangle3.x;
            float f6 = rectangle3.y;
            float f7 = rectangle3.x1;
            float f8 = rectangle3.y1;
            float f9 = Mth.clamp((float)rectangle2.x, (float)f5, (float)f7);
            float f10 = Mth.clamp((float)rectangle2.y, (float)f6, (float)f8);
            float f11 = Mth.clamp((float)rectangle2.x1, (float)f9, (float)f7);
            float f12 = Mth.clamp((float)rectangle2.y1, (float)f10, (float)f8);
            clipStack.push(new Rectangle(f9, f10, f11, f12));
            TestRender.beginScissor(f9, f10, f11, f12);
        }
    }

    public static void popWindow() {
        clipStack.pop();
        if (clipStack.empty()) {
            TestRender.endScissor();
        } else {
            Rectangle rectangle = (Rectangle)clipStack.peek();
            TestRender.beginScissor(rectangle.x, rectangle.y, rectangle.x1, rectangle.y1);
        }
    }

    public static void beginScissor(double d, double d2, double d3, double d4) {
        double d5 = d3 - d;
        double d6 = d4 - d2;
        d5 = Math.max(0.0, d5);
        d6 = Math.max(0.0, d6);
        float f = (float) mc.getWindow().getGuiScale();
        int n = (int)(((double)Mine.mc.getWindow().getGuiScaledHeight() - (d2 + d6)) * (double)f);
        RenderSystem.enableScissor((int)((int)(d * (double)f)), (int)n, (int)((int)(d5 * (double)f)), (int)((int)(d6 * (double)f)));
    }

    public static void endScissor() {
        RenderSystem.disableScissor();
    }

    public static void addWindow(PoseStack poseStack, float x, float y, float xEnd, float yEnd, double d) {
        float f5 = y + yEnd;
        float f6 = (float)((double)f5 * (1.0 - Mth.clamp(d, 0.0, (double)1.0025f)));
        float f7 = x;
        float f8 = y + f6;
        float f9 = xEnd;
        float f10 = yEnd - f6;
        if (f9 < f7) {
            f9 = f7;
        }
        if (f10 < f8) {
            f10 = f8;
        }
        TestRender.addWindow(poseStack, new Rectangle(f7, f8, f9, f10));
    }

    public static void horizontalGradient(PoseStack poseStack, float f, float f2, float f3, float f4, Color color, Color color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f).color(color2.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void verticalGradient(PoseStack poseStack, float f, float f2, float f3, float f4, Color color, Color color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f).color(color.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void drawRect(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void drawRectWithOutline(PoseStack poseStack, float f, float f2, float f3, float f4, Color color, Color color2) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(color2.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void drawRectDumbWay(PoseStack poseStack, float f, float f2, float f3, float f4, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void setRectPoints(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4, Color color, Color color2, Color color3, Color color4) {
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f).color(color3.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color4.getRGB());
    }

    public static boolean isHovered(double d, double d2, double d3, double d4, double d5, double d6) {
        return d >= d3 && d - d5 <= d3 && d2 >= d4 && d2 - d6 <= d4;
    }

    public static void drawBlurredShadow(PoseStack poseStack, float f, float f2, float f3, float f4, int n, Color color) {
        int n2;
        f -= (float)n;
        f2 -= (float)n;
        if (!shadowCache.containsKey(n2 = (int)((f3 += (float)(n * 2)) * (f4 += (float)(n * 2)) + f3 * (float)n))) {
            BufferedImage bufferedImage = new BufferedImage((int)f3, (int)f4, 2);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(new Color(-1));
            graphics.fillRect(n, n, (int)(f3 - (float)(n * 2)), (int)(f4 - (float)(n * 2)));
            graphics.dispose();
            GaussianFilter gaussianFilter = new GaussianFilter(n);
            BufferedImage bufferedImage2 = gaussianFilter.filter(bufferedImage, null);
            shadowCache.put(n2, new BlurredShadow(bufferedImage2));
            return;
        }
        ((BlurredShadow)shadowCache.get(n2)).bind();
        TestRender.setupRender();
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        TestRender.renderTexture(poseStack, f, f2, f3, f4, 0.0f, 0.0f, f3, f4, f3, f4);
        TestRender.endRender();
    }

    public static void drawGradientBlurredShadow(PoseStack poseStack, float f, float f2, float f3, float f4, int n, Color color, Color color2, Color color3, Color color4) {
        int n2;
       
        f -= (float)n;
        f2 -= (float)n;
        if (!shadowCache.containsKey(n2 = (int)((f3 += (float)(n * 2)) * (f4 += (float)(n * 2)) + f3 * (float)n))) {
            BufferedImage bufferedImage = new BufferedImage((int)f3, (int)f4, 2);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(new Color(-1));
            graphics.fillRect(n, n, (int)(f3 - (float)(n * 2)), (int)(f4 - (float)(n * 2)));
            graphics.dispose();
            GaussianFilter gaussianFilter = new GaussianFilter(n);
            BufferedImage bufferedImage2 = gaussianFilter.filter(bufferedImage, null);
            shadowCache.put(n2, new BlurredShadow(bufferedImage2));
            return;
        }
        ((BlurredShadow)shadowCache.get(n2)).bind();
        TestRender.setupRender();
        TestRender.renderGradientTexture(poseStack, f, f2, f3, f4, 0.0f, 0.0f, f3, f4, f3, f4, color, color2, color3, color4);
        TestRender.endRender();
    }

    public static void drawGradientBlurredShadow1(PoseStack poseStack, float f, float f2, float f3, float f4, int n, Color color, Color color2, Color color3, Color color4) {
        int n2;
        
        f -= (float)n;
        f2 -= (float)n;
        if (!shadowCache1.containsKey(n2 = (int)((f3 += (float)(n * 2)) * (f4 += (float)(n * 2)) + f3 * (float)n))) {
            BufferedImage bufferedImage = new BufferedImage((int)f3, (int)f4, 2);
            Graphics graphics = bufferedImage.getGraphics();
            graphics.setColor(new Color(-1));
            graphics.fillRect(n, n, (int)(f3 - (float)(n * 2)), (int)(f4 - (float)(n * 2)));
            graphics.dispose();
            BufferedImage bufferedImage2 = new GaussianFilter(n).filter(bufferedImage, null);
            BufferedImage bufferedImage3 = new BufferedImage((int)f3 + n * 2, (int)f4 + n * 2, 2);
            Graphics graphics2 = bufferedImage3.getGraphics();
            graphics2.setColor(new Color(0));
            graphics2.fillRect(0, 0, (int)f3 + n * 2, (int)f4 + n * 2);
            graphics2.dispose();
            BufferedImage bufferedImage4 = new BufferedImage((int)f3, (int)f4, 2);
            Graphics graphics3 = bufferedImage4.getGraphics();
            graphics3.drawImage(bufferedImage3, -n, -n, null);
            graphics3.drawImage(bufferedImage2, 0, 0, null);
            graphics3.dispose();
            shadowCache1.put(n2, new BlurredShadow(bufferedImage4));
            return;
        }
        ((BlurredShadow)shadowCache1.get(n2)).bind();
        TestRender.setupRender();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        TestRender.renderGradientTexture(poseStack, f, f2, f3, f4, 0.0f, 0.0f, f3, f4, f3, f4, color, color2, color3, color4);
        TestRender.endRender();
    }

    public static void registerBufferedImageTexture(Texture texture, BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)bufferedImage, "png", byteArrayOutputStream);
            byte[] byArray = byteArrayOutputStream.toByteArray();
            TestRender.registerTexture(texture, byArray);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static void registerTexture(Texture texture, byte[] byArray) {
        try {
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)byArray.length).put(byArray);
            byteBuffer.flip();
            DynamicTexture dynamicTexture = new DynamicTexture(NativeImage.read((ByteBuffer)byteBuffer));
            mc.execute(() -> mc.getTextureManager().register(texture, dynamicTexture));
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public static void renderTexture(PoseStack poseStack, double d, double d2, double d3, double d4, float f, float f2, double d5, double d6, double d7, double d8) {
        double d9 = d + d3;
        double d10 = d2 + d4;
        double d11 = 0.0;
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, (float)d, (float)d10, (float)d11).uv(f / (float)d7, (f2 + (float)d6) / (float)d8);
        bufferBuilder.vertex(matrix4f, (float)d9, (float)d10, (float)d11).uv((f + (float)d5) / (float)d7, (f2 + (float)d6) / (float)d8);
        bufferBuilder.vertex(matrix4f, (float)d9, (float)d2, (float)d11).uv((f + (float)d5) / (float)d7, f2 / (float)d8);
        bufferBuilder.vertex(matrix4f, (float)d, (float)d2, (float)d11).uv(f / (float)d7, (f2 + 0.0f) / (float)d8);
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void renderGradientTexture(PoseStack poseStack, double d, double d2, double d3, double d4, float f, float f2, double d5, double d6, double d7, double d8, Color color, Color color2, Color color3, Color color4) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        TestRender.renderGradientTextureInternal(bufferBuilder, poseStack, d, d2, d3, d4, f, f2, d5, d6, d7, d8, color, color2, color3, color4);
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void renderGradientTextureInternal(BufferBuilder bufferBuilder, PoseStack poseStack, double d, double d2, double d3, double d4, float f, float f2, double d5, double d6, double d7, double d8, Color color, Color color2, Color color3, Color color4) {
        double d9 = d + d3;
        double d10 = d2 + d4;
        double d11 = 0.0;
        Matrix4f matrix4f = poseStack.last().pose();
        bufferBuilder.vertex(matrix4f, (float)d, (float)d10, (float)d11).uv(f / (float)d7, (f2 + (float)d6) / (float)d8).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, (float)d9, (float)d10, (float)d11).uv((f + (float)d5) / (float)d7, (f2 + (float)d6) / (float)d8).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, (float)d9, (float)d2, (float)d11).uv((f + (float)d5) / (float)d7, f2 / (float)d8).color(color3.getRGB());
        bufferBuilder.vertex(matrix4f, (float)d, (float)d2, (float)d11).uv(f / (float)d7, (f2 + 0.0f) / (float)d8).color(color4.getRGB());
    }

    public static void renderRoundedGradientRect(PoseStack poseStack, Color color, Color color2, Color color3, Color color4, float f, float f2, float f3, float f4, float f5) {
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.colorMask((boolean)false, (boolean)false, (boolean)false, (boolean)true);
        RenderSystem.clearColor((float)0.0f, (float)0.0f, (float)0.0f, (float)0.0f);
        RenderSystem.clear((int)16384, (boolean)false);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        TestRender.drawRound(poseStack, f, f2, f3, f4, f5, color);
        TestRender.setupRender();
        RenderSystem.blendFunc((int)772, (int)773);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2 + f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2 + f4, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f + f3, f2, 0.0f).color(color3.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color4.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void drawRound(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, Color color) {
        TestRender.renderRoundedQuad(poseStack, color, f, f2, f3 + f, f4 + f2, f5, 4.0);
    }

    public static void renderRoundedQuad(PoseStack poseStack, Color color, double d, double d2, double d3, double d4, double d5, double d6) {
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        TestRender.renderRoundedQuadInternal(poseStack.last().pose(), (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f, d, d2, d3, d4, d5, d6);
        TestRender.endRender();
    }

    public static void renderRoundedQuad2(PoseStack poseStack, Color color, Color color2, Color color3, Color color4, double d, double d2, double d3, double d4, double d5) {
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        TestRender.renderRoundedQuadInternal2(poseStack.last().pose(), (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f, (float)color2.getRed() / 255.0f, (float)color2.getGreen() / 255.0f, (float)color2.getBlue() / 255.0f, (float)color2.getAlpha() / 255.0f, (float)color3.getRed() / 255.0f, (float)color3.getGreen() / 255.0f, (float)color3.getBlue() / 255.0f, (float)color3.getAlpha() / 255.0f, (float)color4.getRed() / 255.0f, (float)color4.getGreen() / 255.0f, (float)color4.getBlue() / 255.0f, (float)color4.getAlpha() / 255.0f, d, d2, d3, d4, d5);
        TestRender.endRender();
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix4f, float f, float f2, float f3, float f4, double d, double d2, double d3, double d4, double d5, double d6) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        double[][] dArrayArray = new double[][]{{d3 - d5, d4 - d5, d5}, {d3 - d5, d2 + d5, d5}, {d + d5, d2 + d5, d5}, {d + d5, d4 - d5, d5}};
        for (int i = 0; i < 4; ++i) {
            float f5;
            double[] dArray = dArrayArray[i];
            double d7 = dArray[2];
            for (double d8 = (double)i * 90.0; d8 < 90.0 + (double)i * 90.0; d8 += 90.0 / d6) {
                f5 = (float)Math.toRadians(d8);
                float f6 = (float)(Math.sin(f5) * d7);
                float f7 = (float)(Math.cos(f5) * d7);
                bufferBuilder.vertex(matrix4f, (float)dArray[0] + f6, (float)dArray[1] + f7, 0.0f).color(f, f2, f3, f4);
            }
            float f8 = (float)Math.toRadians(90.0 + (double)i * 90.0);
            float f9 = (float)(Math.sin(f8) * d7);
            f5 = (float)(Math.cos(f8) * d7);
            bufferBuilder.vertex(matrix4f, (float)dArray[0] + f9, (float)dArray[1] + f5, 0.0f).color(f, f2, f3, f4);
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void renderRoundedQuadInternal2(Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16, double d, double d2, double d3, double d4, double d5) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        double[][] dArrayArray = new double[][]{{d3 - d5, d4 - d5, d5}, {d3 - d5, d2 + d5, d5}, {d + d5, d2 + d5, d5}, {d + d5, d4 - d5, d5}};
        for (int i = 0; i < 4; ++i) {
            double[] dArray = dArrayArray[i];
            double d6 = dArray[2];
            block6: for (double d7 = (double)(i * 90); d7 < (double)(90 + i * 90); d7 += 10.0) {
                float f17 = (float)Math.toRadians(d7);
                float f18 = (float)(Math.sin(f17) * d6);
                float f19 = (float)(Math.cos(f17) * d6);
                switch (i) {
                    case 0: {
                        bufferBuilder.vertex(matrix4f, (float)dArray[0] + f18, (float)dArray[1] + f19, 0.0f).color(f5, f6, f7, f8);
                        continue block6;
                    }
                    case 1: {
                        bufferBuilder.vertex(matrix4f, (float)dArray[0] + f18, (float)dArray[1] + f19, 0.0f).color(f, f2, f3, f4);
                        continue block6;
                    }
                    case 2: {
                        bufferBuilder.vertex(matrix4f, (float)dArray[0] + f18, (float)dArray[1] + f19, 0.0f).color(f9, f10, f11, f12);
                        continue block6;
                    }
                    default: {
                        bufferBuilder.vertex(matrix4f, (float)dArray[0] + f18, (float)dArray[1] + f19, 0.0f).color(f13, f14, f15, f16);
                    }
                }
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void draw2DGradientRect(PoseStack poseStack, float f, float f2, float f3, float f4, Color color, Color color2, Color color3, Color color4) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f).color(color4.getRGB());
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(color2.getRGB());
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f).color(color.getRGB());
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f).color(color3.getRGB());
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void drawNewArrow(PoseStack poseStack, float f, float f2, float f3, Color color) {
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)arrow);
        TestRender.setupRender();
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix4f, f - f3 / 2.0f, f2 + f3, 0.0f).uv(0.0f, 1.0f);
        bufferBuilder.vertex(matrix4f, f + f3 / 2.0f, f2 + f3, 0.0f).uv(1.0f, 1.0f);
        bufferBuilder.vertex(matrix4f, f + f3 / 2.0f, f2, 0.0f).uv(1.0f, 0.0f);
        bufferBuilder.vertex(matrix4f, f - f3 / 2.0f, f2, 0.0f).uv(0.0f, 0.0f);
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        TestRender.endRender();
    }

    public static void drawDefaultArrow(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, boolean bl, boolean bl2, int n) {
        if (bl2) {
            TestRender.drawBlurredShadow(poseStack, f - f3 * f4, f2, f + f3 * f4 - (f - f3 * f4), f3, 10, TestRender.injectAlpha(new Color(n), 140));
        }
        poseStack.pushPose();
        TestRender.setupRender();
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(n);
        bufferBuilder.vertex(matrix4f, f - f3 * f4, f2 + f3, 0.0f).color(n);
        bufferBuilder.vertex(matrix4f, f, f2 + f3 - f5, 0.0f).color(n);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(n);
        n = TestRender.darker(new Color(n), 0.8f).getRGB();
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(n);
        bufferBuilder.vertex(matrix4f, f, f2 + f3 - f5, 0.0f).color(n);
        bufferBuilder.vertex(matrix4f, f + f3 * f4, f2 + f3, 0.0f).color(n);
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f).color(n);
        if (bl) {
            n = TestRender.darker(new Color(n), 0.6f).getRGB();
            bufferBuilder.vertex(matrix4f, f - f3 * f4, f2 + f3, 0.0f).color(n);
            bufferBuilder.vertex(matrix4f, f + f3 * f4, f2 + f3, 0.0f).color(n);
            bufferBuilder.vertex(matrix4f, f, f2 + f3 - f5, 0.0f).color(n);
            bufferBuilder.vertex(matrix4f, f - f3 * f4, f2 + f3, 0.0f).color(n);
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
        poseStack.popPose();
    }

    public static void endRender() {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    public static void drawGradientRound(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, Color color, Color color2, Color color3, Color color4) {
        TestRender.renderRoundedQuad2(poseStack, color, color2, color3, color4, f, f2, f + f3, f2 + f4, f5);
    }

    public static float scrollAnimate(float f, float f2, float f3) {
        boolean bl;
        boolean bl2 = bl = f > f2;
        if (f3 < 0.0f) {
            f3 = 0.0f;
        } else if (f3 > 1.0f) {
            f3 = 1.0f;
        }
        float f4 = Math.max(f, f2) - Math.min(f, f2);
        float f5 = f4 * f3;
        return f2 + (bl ? f5 : -f5);
    }

    public static Color injectAlpha(Color color, int n) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Mth.clamp((int)n, (int)0, (int)255));
    }

    public static Color TwoColoreffect(Color color, Color color2, double d, double d2) {
        int n = (int)(((double)System.currentTimeMillis() / d + d2) % 360.0);
        n = (n >= 180 ? 360 - n : n) * 2;
        return TestRender.interpolateColorC(color, color2, (float)n / 360.0f);
    }

    public static Color astolfo(boolean bl, int n) {
        float f = bl ? 3500.0f : 3000.0f;
        float f2 = System.currentTimeMillis() % (long)((int)f) + (long)n;
        if (f2 > f) {
            f2 -= f;
        }
        if ((f2 /= f) > 0.5f) {
            f2 = 0.5f - (f2 - 0.5f);
        }
        return Color.getHSBColor(f2 += 0.5f, 0.4f, 1.0f);
    }

    public static Color rainbow(int n, float f, float f2) {
        double d = Math.ceil((float)(System.currentTimeMillis() + (long)n) / 16.0f);
        return Color.getHSBColor((float)((d %= 360.0) / 360.0), f, f2);
    }

    
    public static Color fade(int n, int n2, Color color, float f) {
        float[] fArray = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int n3 = (int)((System.currentTimeMillis() / (long)n + (long)n2) % 360L);
        n3 = (n3 > 180 ? 360 - n3 : n3) + 180;
        Color color2 = new Color(Color.HSBtoRGB(fArray[0], fArray[1], (float)n3 / 360.0f));
        return new Color(color2.getRed(), color2.getGreen(), color2.getBlue(), Math.max(0, Math.min(255, (int)(f * 255.0f))));
    }

    public static Color getAnalogousColor(Color color) {
        float[] fArray = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float f = 0.84f;
        float f2 = fArray[0] - f;
        return new Color(Color.HSBtoRGB(f2, fArray[1], fArray[2]));
    }

    public static Color applyOpacity(Color color, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * f));
    }

    public static int applyOpacity(int n, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        Color color = new Color(n);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * f)).getRGB();
    }

    public static Color darker(Color color, float f) {
        return new Color(Math.max((int)((float)color.getRed() * f), 0), Math.max((int)((float)color.getGreen() * f), 0), Math.max((int)((float)color.getBlue() * f), 0), color.getAlpha());
    }

    public static Color rainbow(int n, int n2, float f, float f2, float f3) {
        int n3 = (int)((System.currentTimeMillis() / (long)n + (long)n2) % 360L);
        float f4 = (float)n3 / 360.0f;
        Color color = new Color(Color.HSBtoRGB(f4, f, f2));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, (int)(f3 * 255.0f))));
    }

    public static Color interpolateColorsBackAndForth(int n, int n2, Color color, Color color2, boolean bl) {
        int n3 = (int)((System.currentTimeMillis() / (long)n + (long)n2) % 360L);
        n3 = (n3 >= 180 ? 360 - n3 : n3) * 2;
        return bl ? TestRender.interpolateColorHue(color, color2, (float)n3 / 360.0f) : TestRender.interpolateColorC(color, color2, (float)n3 / 360.0f);
    }

    public static Color interpolateColorC(Color color, Color color2, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        return new Color(TestRender.interpolateInt(color.getRed(), color2.getRed(), f), TestRender.interpolateInt(color.getGreen(), color2.getGreen(), f), TestRender.interpolateInt(color.getBlue(), color2.getBlue(), f), TestRender.interpolateInt(color.getAlpha(), color2.getAlpha(), f));
    }

    public static Color interpolateColorHue(Color color, Color color2, float f) {
        f = Math.min(1.0f, Math.max(0.0f, f));
        float[] fArray = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float[] fArray2 = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        Color color3 = Color.getHSBColor(TestRender.interpolateFloat(fArray[0], fArray2[0], f), TestRender.interpolateFloat(fArray[1], fArray2[1], f), TestRender.interpolateFloat(fArray[2], fArray2[2], f));
        return new Color(color3.getRed(), color3.getGreen(), color3.getBlue(), TestRender.interpolateInt(color.getAlpha(), color2.getAlpha(), f));
    }

    public static double interpolate(double d, double d2, double d3) {
        return d + (d2 - d) * d3;
    }

    public static float interpolateFloat(float f, float f2, double d) {
        return (float)TestRender.interpolate(f, f2, (float)d);
    }

    public static int interpolateInt(int n, int n2, double d) {
        return (int)TestRender.interpolate(n, n2, (float)d);
    }

   /* public static void drawMainMenuShader(PoseStack poseStack, float f, float f2, float f3, float f4) {
        BufferBuilder bufferBuilder = TestRender.preShaderDraw(poseStack, f, f2, f3, f4);
        MAIN_MENU_PROGRAM.setParameters(f, f2, f3, f4);
        MAIN_MENU_PROGRAM.use();
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void drawArc(PoseStack poseStack, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        BufferBuilder bufferBuilder = TestRender.preShaderDraw(poseStack, f - f3 / 2.0f, f2 - f4 / 2.0f, f + f3 / 2.0f, f2 + f4 / 2.0f);
        ARC_PROGRAM.setParameters(f, f2, f3, f4, f5, f6, f7, f8);
        ARC_PROGRAM.use();
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }*/



    



    

    public static BufferBuilder preShaderDraw(PoseStack poseStack, float f, float f2, float f3, float f4) {
        TestRender.setupRender();
        Matrix4f matrix4f = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        
        TestRender.setRectanglePoints(bufferBuilder, matrix4f, f, f2, f + f3, f2 + f4);
        return bufferBuilder;
    }

    public static void setRectanglePoints(BufferBuilder bufferBuilder, Matrix4f matrix4f, float f, float f2, float f3, float f4) {
        bufferBuilder.vertex(matrix4f, f, f2, 0.0f);
        bufferBuilder.vertex(matrix4f, f, f4, 0.0f);
        bufferBuilder.vertex(matrix4f, f3, f4, 0.0f);
        bufferBuilder.vertex(matrix4f, f3, f2, 0.0f);
    }

    public static void drawOrbiz(PoseStack poseStack, float f, double d, Color color) {
        Matrix4f matrix4f = poseStack.last().pose();
        TestRender.setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= 20; ++i) {
            float f2 = (float)(Math.sin((float)i * 56.548656f / 180.0f) * d);
            float f3 = (float)(Math.cos((float)i * 56.548656f / 180.0f) * d);
            bufferBuilder.vertex(matrix4f, f2, f3, f).color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.4f);
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
        TestRender.endRender();
    }

    public static void drawStar(PoseStack poseStack, Color color, float f) {
        TestRender.setupRender();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)star);
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        TestRender.renderGradientTexture(poseStack, 0.0, 0.0, f, f, 0.0f, 0.0f, 128.0, 128.0, 128.0, 128.0, color, color, color, color);
        TestRender.endRender();
    }

    public static void drawHeart(PoseStack poseStack, Color color, float f) {
        TestRender.setupRender();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)heart);
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        TestRender.renderGradientTexture(poseStack, 0.0, 0.0, f, f, 0.0f, 0.0f, 128.0, 128.0, 128.0, 128.0, color, color, color, color);
        TestRender.endRender();
    }

    public static void drawBloom(PoseStack poseStack, Color color, float f) {
        TestRender.setupRender();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)firefly);
        RenderSystem.setShaderColor((float)((float)color.getRed() / 255.0f), (float)((float)color.getGreen() / 255.0f), (float)((float)color.getBlue() / 255.0f), (float)((float)color.getAlpha() / 255.0f));
        TestRender.renderGradientTexture(poseStack, 0.0, 0.0, f, f, 0.0f, 0.0f, 128.0, 128.0, 128.0, 128.0, color, color, color, color);
        TestRender.endRender();
    }

    public static void drawBubble(PoseStack poseStack, float f, float f2) {
        TestRender.setupRender();
        RenderSystem.blendFunc((GlStateManager.SourceFactor)GlStateManager.SourceFactor.SRC_ALPHA, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)bubble);
        poseStack.mulPose(Axis.ZP.rotationDegrees(f));
        float f3 = f2 * 2.0f;
        TestRender.renderGradientTexture(poseStack, -f3 / 2.0f, -f3 / 2.0f, f3, f3, 0.0f, 0.0f, 128.0, 128.0, 128.0, 128.0, TestRender.applyOpacity(ColorUtils.getColorStyle2(270), 1.0f - f2), TestRender.applyOpacity(ColorUtils.getColorStyle2(0), 1.0f - f2), TestRender.applyOpacity(ColorUtils.getColorStyle2(180), 1.0f - f2), TestRender.applyOpacity(ColorUtils.getColorStyle2(90), 1.0f - f2));
        TestRender.endRender();
    }

    public static void drawLine(float f, float f2, float f3, float f4, int n) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(f, f2, 0.0f).color(n);
        bufferBuilder.vertex(f3, f4, 0.0f).color(n);
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static boolean isDark(Color color) {
        return TestRender.isDark((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
    }

    public static boolean isDark(float f, float f2, float f3) {
        return TestRender.colorDistance(f, f2, f3, 0.0f, 0.0f, 0.0f) < TestRender.colorDistance(f, f2, f3, 1.0f, 1.0f, 1.0f);
    }

    public static float colorDistance(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = f4 - f;
        float f8 = f5 - f2;
        float f9 = f6 - f3;
        return (float)Math.sqrt(f7 * f7 + f8 * f8 + f9 * f9);
    }



    static {
        star = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/star.png");
        heart = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/heart.png");
        dollar = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/dollar.png");
        snowflake = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/snowflake.png");
        firefly = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/firefly.png");
        arrow = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/hud/elements/triangle.png");
        capture = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/hud/elements/capture.png");
        bubble = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/hitbubble.png");
        default_circle = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/particles/circle.png");
        CONTAINER_BACKGROUND = ResourceLocation.tryBuild((String)"thunderhack", (String)"textures/hud/elements/container.png");
        shadowCache = new HashMap();
        shadowCache1 = new HashMap();
        clipStack = new Stack();
    }

    public record Rectangle(float x, float y, float x1, float y1) {
        public boolean contains(double d, double d2) {
            return d >= (double)this.x && d <= (double)this.x1 && d2 >= (double)this.y && d2 <= (double)this.y1;
        }
    }

    public static class BlurredShadow {
        Texture id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));

        public BlurredShadow(BufferedImage bufferedImage) {
            TestRender.registerBufferedImageTexture(this.id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture((int)0, id);
        }
    }
}

