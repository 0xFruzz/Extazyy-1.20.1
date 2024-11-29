package ru.fruzz.extazyy.misc.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.math.MathHelpper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import ru.fruzz.extazyy.main.commands.impl.GpsCommand;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.Mine;
import lombok.experimental.UtilityClass;
import org.joml.Matrix4f;
import ru.fruzz.extazyy.misc.util.math.MathUtil;
import ru.fruzz.extazyy.misc.util.math.MathUtils;

import java.awt.*;
import java.util.Objects;

@UtilityClass
public class DrawHelper implements Mine {
    Shader RECTANGLE_SHADER = Shader.create("rectangle", DefaultVertexFormat.POSITION_TEX);


    public static void drawArrow(PoseStack e, Player player, float xcord, float zcord) {
        float xOffset = mc.getWindow().getGuiScaledWidth() / 2f;
        float yOffset = mc.getWindow().getGuiScaledHeight() / 2f;
        float animatedYaw = 0;
        float animatedPitch = 0;
        float yaw = 0;

        float animationStep = 0;
        yaw = MathUtils.fast(yaw, mc.player.getYRot(), 10);

        float size = 70;
        animationStep = MathUtils.fast(animationStep, size, 6);

        float targetYaw = getRotations(new Vector2f(xcord, zcord)) - mc.player.getYRot();
        double x = player.xo + (player.getX() - player.xo) * mc.getFrameTime()
                -  mc.getEntityRenderDispatcher().camera.getPosition().x;
        double z = player.zo + (player.getZ() - player.zo) * mc.getFrameTime()
                - mc.getEntityRenderDispatcher().camera.getPosition().z;

        double cos = MathHelpper.cos((float) (yaw * (Math.PI * 2 / 360)));
        double sin = MathHelpper.sin((float) (yaw * (Math.PI * 2 / 360)));
        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);

        float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);
        animatedYaw = MathUtils.fast(animatedYaw, (1) * 10,
                5);
        animatedPitch = MathUtils.fast(animatedPitch,
                (1) * 10, 5);
        double x2 = animationStep * MathHelpper.cos((float) Math.toRadians(angle)) +  mc.getWindow().getGuiScaledWidth() / 2f;
        double y2 = animationStep * MathHelpper.sin((float) Math.toRadians(angle)) + mc.getWindow().getGuiScaledHeight() / 2f;

        x2 += animatedYaw;
        y2 += animatedPitch;

        e.pushPose();
        e.translate(x2, y2, 0.0F);
        e.mulPose(Axis.ZP.rotationDegrees(targetYaw));
        e.translate(-x2, -y2, 0.0F);

        DrawHelper.drawTextureRotate(
                new ResourceLocation("minecraft", "extazyy/images/arrow.png"),
                e.last().pose(), xOffset - 7, yOffset - 40,
                12.5f + 2, 12.5f + 2, 180,
                ColorUtil.getColorStyle(0)
        );

        e.popPose();
    }


    public static void drawDefaultArrow(PoseStack matrices, float x, float y, float size, float tracerWidth, float downHeight, boolean down, boolean glow, int color) {

        matrices.pushPose();
        setupRender();
        Matrix4f matrix = matrices.last().pose();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
        bufferBuilder.vertex(matrix, (x - size * tracerWidth), (y + size), 0.0F).color(color);
        bufferBuilder.vertex(matrix, x, (y + size - downHeight), 0.0F).color(color);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
        color = 1;
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);
        bufferBuilder.vertex(matrix, x, (y + size - downHeight), 0.0F).color(color);
        bufferBuilder.vertex(matrix, (x + size * tracerWidth), (y + size), 0.0F).color(color);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color);

        if (down) {
            color = 1;
            bufferBuilder.vertex(matrix, (x - size * tracerWidth), (y + size), 0.0F).color(color);
            bufferBuilder.vertex(matrix, (x + size * tracerWidth), (y + size), 0.0F).color(color);
            bufferBuilder.vertex(matrix, x, (y + size - downHeight), 0.0F).color(color);
            bufferBuilder.vertex(matrix, (x - size * tracerWidth), (y + size), 0.0F).color(color);
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        endRender();
        matrices.popPose();
    }

    public static void drawTriangle(PoseStack matrixStack, float x, float y, float width, float height, Color color) {

        matrixStack.pushPose();
        matrixStack.translate(x, y, 0); // Перемещаем треугольник в нужную позицию

        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        // fill
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        int colorRGB = color.getRGB();
        buffer.vertex(matrixStack.last().pose(), 0, -2, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), width, height, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), width, 0, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        BufferUploader.drawWithShader(buffer.end());

        // brightened fill
        Color brighterColor = color.brighter();
        int brighterRGB = brighterColor.getRGB();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrixStack.last().pose(), width, 0, 0)
                .color((brighterRGB >> 16) & 0xFF, (brighterRGB >> 8) & 0xFF, brighterRGB & 0xFF, brighterColor.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), width, height, 0)
                .color((brighterRGB >> 16) & 0xFF, (brighterRGB >> 8) & 0xFF, brighterRGB & 0xFF, brighterColor.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), width * 2, -2, 0)
                .color((brighterRGB >> 16) & 0xFF, (brighterRGB >> 8) & 0xFF, brighterRGB & 0xFF, brighterColor.getAlpha())
                .endVertex();
        BufferUploader.drawWithShader(buffer.end());

        // line
        RenderSystem.lineWidth(1.0F);
        buffer.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrixStack.last().pose(), 0, -2, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), width, height, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), width, 0, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        buffer.vertex(matrixStack.last().pose(), 0, -2, 0)
                .color((colorRGB >> 16) & 0xFF, (colorRGB >> 8) & 0xFF, colorRGB & 0xFF, color.getAlpha())
                .endVertex();
        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
        matrixStack.popPose();
    }


    public static int getDistance(BlockPos bp) {
        double d0 = mc.player.getX() - bp.getX();
        double d2 = mc.player.getZ() - bp.getZ();
        return (int) (Mth.sqrt((float) (d0 * d0 + d2 * d2)));
    }

    public static float getRotations(Vector2f vec) {
        if (mc.player == null) return 0;
        double x = vec.x - mc.player.getX();
        double z = vec.y - mc.player.getZ();
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }

    public static void scale(PoseStack ms, float posX, float posY, float width, float height, float scale, Runnable runnable) {
        float centerX = posX + width / 2;
        float centerY = posY + height / 2;

        ms.pushPose();
        ms.translate(centerX, centerY, 0);
        ms.scale(scale, scale, scale);
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
        ms.popPose();
    }

    public static void rotate(PoseStack ms, float posX, float posY, float width, float height, float angleDegrees, Runnable runnable) {
        float centerX = posX + width / 2;
        float centerY = posY + height / 2;

        ms.translate(centerX, centerY, 0);
        ms.mulPose(Axis.ZP.rotationDegrees(angleDegrees));
        ms.translate(-centerX, -centerY, 0);
        runnable.run();
    }


    public static @NotNull Vec3 projectCoordinates(@NotNull Vec3 pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        if (camera == null) return new Vec3(0, 0, 0);
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPosition().x;
        double deltaY = pos.y - camera.getPosition().y;
        double deltaZ = pos.z - camera.getPosition().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3(target.x / mc.getWindow().getGuiScale(), (displayHeight - target.y) / mc.getWindow().getGuiScale(), target.z);
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



    public void rectangle(PoseStack matrices, float x, float y, float width, float height, float rounding, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.vertex(model, x, y, 0).endVertex();
        bufferBuilder.vertex(model, x, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y, 0).endVertex();

        tessellator.end();

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }




    public static void drawArrow(PoseStack matrices, float x, float y, float size, int color) {
        RenderSystem.setShaderTexture(0, new ResourceLocation("minecraft", "extazyy/images/arrow.png"));
        setupRender();
        RenderSystem.setShaderColor(ColorUtil.getRed(color) / 255f, ColorUtil.getGreen(color) / 255f, ColorUtil.getBlue(color) / 255f, ColorUtil.getAlpha(color) / 255f);
        RenderSystem.disableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x - (size / 2f), y + size, 0).uv(0f, 1f);
        bufferBuilder.vertex(matrix, x + size / 2f, y + size, 0).uv(1f, 1f);
        bufferBuilder.vertex(matrix, x + size / 2f, y, 0).uv(1f, 0);
        bufferBuilder.vertex(matrix, x - (size / 2f), y, 0).uv(0, 0);
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        endRender();
    }

    public void rectanglescale(PoseStack matrices, float x, float y, float width, float height, float rounding, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = 1f;
        System.out.println("Width: " + window.getWidth() + " Height: " + window.getHeight());
        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.vertex(model, x, y, 0).endVertex();
        bufferBuilder.vertex(model, x, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y, 0).endVertex();

        tessellator.end();

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }


    public void drawSemiRoundRect(PoseStack matrices, float x, float y, float width, float height, float rounding1, float rounding2,float rounding3,float rounding4, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding1 * guiScale, rounding2 * guiScale, rounding3 * guiScale, rounding4 * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.vertex(model, x, y, 0).endVertex();
        bufferBuilder.vertex(model, x, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y, 0).endVertex();

        tessellator.end();

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }

    public void rectangleVector(PoseStack matrices, float x, float y, float width, float height, Vector4f vector4f, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(vector4f.x * guiScale, vector4f.y * guiScale, vector4f.z * guiScale, vector4f.w * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.vertex(model, x, y, 0).endVertex();
        bufferBuilder.vertex(model, x, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y, 0).endVertex();

        tessellator.end();

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }

    public static void drawTexture(ResourceLocation resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawQuadsTex(matrix4f, x, y, width, height);
    }
    public static void drawTexture(int resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawQuadsTex(matrix4f, x, y, width, height);
    }

    public static void drawTexture(ResourceLocation resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height, int color, int alpha) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        drawQuadsTex(matrix4f, x, y, width, height);
    }

    public static void drawTextureRotate(ResourceLocation resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height, float rotate, int color) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader); // Используем шейдер с поддержкой цвета
        drawQuadsTexRot(matrix4f, x, y, width, height, rotate, color);
    }


    public static void drawQuadsTex(Matrix4f matrix, float x, float y, float width, float height) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y, 0).uv(0.0F, 1.0F).endVertex();
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(0.0F, 0.0F).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(1.0F, 0.0F).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(1.0F, 1.0F).endVertex();
        tesselator.end();
    }

    public static void drawQuadsTexRot(Matrix4f matrix, float x, float y, float width, float height, float rotate, int color) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX); // Изменяем формат на поддерживающий цвет

        // Извлекаем RGBA-компоненты из параметра color
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Вращаем и добавляем каждую вершину с учетом цвета
        float[] v1 = rotatePoint(x, y, width, height, rotate, x, y);
        bufferBuilder.vertex(matrix, v1[0], v1[1], 0).color(red, green, blue, alpha).uv(0.0F, 1.0F).endVertex();

        float[] v2 = rotatePoint(x, y, width, height, rotate, x, y + height);
        bufferBuilder.vertex(matrix, v2[0], v2[1], 0).color(red, green, blue, alpha).uv(0.0F, 0.0F).endVertex();

        float[] v3 = rotatePoint(x, y, width, height, rotate, x + width, y + height);
        bufferBuilder.vertex(matrix, v3[0], v3[1], 0).color(red, green, blue, alpha).uv(1.0F, 0.0F).endVertex();

        float[] v4 = rotatePoint(x, y, width, height, rotate, x + width, y);
        bufferBuilder.vertex(matrix, v4[0], v4[1], 0).color(red, green, blue, alpha).uv(1.0F, 1.0F).endVertex();

        tesselator.end();
    }

    float[] rotatePoint(float x, float y, float width, float height, float rotate,float px, float py) {
        float angleInRadians = (float) Math.toRadians(rotate);

        // Вычисляем синус и косинус угла
        float cos = (float) Math.cos(angleInRadians);
        float sin = (float) Math.sin(angleInRadians);

        // Вычисляем центр объекта (для вращения относительно центра)
        float centerX = x + width / 2;
        float centerY = y + height / 2;
        float dx = px - centerX;
        float dy = py - centerY;
        float rotatedX = dx * cos - dy * sin + centerX;
        float rotatedY = dx * sin + dy * cos + centerY;
        return new float[] {rotatedX, rotatedY};
    }

    public static double calculateAngle(double playerX, double playerY, double pointX, double pointY) {
        double deltaX = pointX - playerX;
        double deltaY = pointY - playerY;
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    public static void drawTextureAlpha(ResourceLocation resourceLocation, Matrix4f matrix4f, float x, float y, float width, float height, int color, int alpha) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alphaFloat = alpha / 255.0F;
        RenderSystem.setShaderColor(red,green,blue,alphaFloat);
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        drawQuadsTexAlpha(matrix4f, x, y, width, height, color, alpha);
        RenderSystem.disableBlend();
    }

    public static void drawQuadsTexAlpha(Matrix4f matrix, float x, float y, float width, float height, int color, int alpha) {
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        float alphaFloat = alpha / 255.0F;

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        bufferBuilder.vertex(matrix, x, y, 0).uv(0.0F, 0.0F).color(red, green, blue, alphaFloat).endVertex();
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(0.0F, 1.0F).color(red, green, blue, alphaFloat).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(1.0F, 1.0F).color(red, green, blue, alphaFloat).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(1.0F, 0.0F).color(red, green, blue, alphaFloat).endVertex();

        tesselator.end();
    }




    public static void drawHead(PoseStack ms, Player player, float x, float y, float width, float height) {
        ResourceLocation texture = Objects.requireNonNull((Objects.requireNonNull(mc.getConnection())).getPlayerInfo(player.getUUID())).getSkinLocation();
        RenderSystem.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.blendFunc(770, 771);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = ms.last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix4f, x, y, 0).uv(1.125f, 1.125f).endVertex();
        bufferbuilder.vertex(matrix4f, x, y + height, 0).uv(1.125f, 1.25f).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y + height, 0).uv(1.25f, 1.25f).endVertex();
        bufferbuilder.vertex(matrix4f, x + width, y, 0).uv(1.25f, 1.125f).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static void beginScissor(double x, double y, double endX, double endY) {
        double width = endX - x;
        double height = endY - y;
        width = Math.max(0, width);
        height = Math.max(0, height);
        float d = (float) mc.getWindow().getGuiScale();
        int ay = (int) ((mc.getWindow().getGuiScaledHeight() - (y + height)) * d);
        RenderSystem.enableScissor((int) (x * d), ay, (int) (width * d), (int) (height * d));
    }

    public static void endScissor() {
        RenderSystem.disableScissor();
    }


    public void drawroundedline(PoseStack matrices, float x, float y, float width, float height, float round, int color, int colo2, int colo3, int colo4) {
        rectRGB(matrices, x,y,width,height,round,color, colo2,colo3,colo4);
    }

    public void rectRGB(PoseStack matrices, float x, float y, float width, float height, float rounding, int color, int color2, int color3, int color4) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = RenderSystem.renderThreadTesselator();

        Window window = mc.getWindow();
        float guiScale = (float) window.getGuiScale();

        RECTANGLE_SHADER.uniform("position").set(x * guiScale, window.getHeight() - (y * guiScale) - (height * guiScale));

        RECTANGLE_SHADER.uniform("size").set(width * guiScale, height * guiScale);
        RECTANGLE_SHADER.uniform("rounding").set(rounding * guiScale, rounding * guiScale, rounding * guiScale, rounding * guiScale);

        RECTANGLE_SHADER.uniform("smoothness").set(0F, 2F);

        RECTANGLE_SHADER.uniform("color1").set(
                ColorUtil.getRed(color) / 255F,
                ColorUtil.getGreen(color) / 255F,
                ColorUtil.getBlue(color) / 255F,
                ColorUtil.getAlpha(color) / 255F
        );

        RECTANGLE_SHADER.uniform("color2").set(
                ColorUtil.getRed(color2) / 255F,
                ColorUtil.getGreen(color2) / 255F,
                ColorUtil.getBlue(color2) / 255F,
                ColorUtil.getAlpha(color2) / 255F
        );

        RECTANGLE_SHADER.uniform("color3").set(
                ColorUtil.getRed(color3) / 255F,
                ColorUtil.getGreen(color3) / 255F,
                ColorUtil.getBlue(color3) / 255F,
                ColorUtil.getAlpha(color3) / 255F
        );

        RECTANGLE_SHADER.uniform("color4").set(
                ColorUtil.getRed(color4) / 255F,
                ColorUtil.getGreen(color4) / 255F,
                ColorUtil.getBlue(color4) / 255F,
                ColorUtil.getAlpha(color4) / 255F
        );

        RECTANGLE_SHADER.bind();

        Matrix4f model = matrices.last().pose();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        bufferBuilder.vertex(model, x, y, 0).endVertex();
        bufferBuilder.vertex(model, x, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y + height, 0).endVertex();
        bufferBuilder.vertex(model, x + width, y, 0).endVertex();

        tessellator.end();

        RECTANGLE_SHADER.unbind();

        RenderSystem.disableBlend();
    }

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static @NotNull Vec3 worldSpaceToScreenSpace(@NotNull Vec3 pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPosition().x;
        double deltaY = pos.y - camera.getPosition().y;
        double deltaZ = pos.z - camera.getPosition().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3(target.x / mc.getWindow().getGuiScale(), (displayHeight - target.y) / mc.getWindow().getGuiScale(), target.z);
    }

    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }



    public static boolean isInRegion(final double mouseX,
                                     final double mouseY,
                                     final float x,
                                     final float y,
                                     final float width,
                                     final float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }



}
