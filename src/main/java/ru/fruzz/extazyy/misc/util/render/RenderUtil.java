package ru.fruzz.extazyy.misc.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.world.entity.player.Player;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.misc.util.*;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40C;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;

import java.awt.*;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;

import static com.mojang.blaze3d.platform.GlConst.GL_ONE_MINUS_SRC_ALPHA;
import static com.mojang.blaze3d.platform.GlConst.GL_SRC_ALPHA;

public class RenderUtil implements Mine {

    public static void renderRoundedGradientRect(PoseStack matrices, int color1, int color2, int color3, int color4, float x, float y, float width, float height, float Radius) {
        Matrix4f matrix = matrices.last().pose();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);

        drawRound(matrices, x, y, width, height, Radius, color1);
        setupRender();
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        endRender();
    }

    public static void drawRound(PoseStack matrices, float x, float y, float width, float height, float radius, int color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4);
    }

    public static void renderRoundedQuad(PoseStack matrices, int c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrices.last().pose(), ColorUtil.getRed(c) / 255f, ColorUtil.getGreen(c) / 255f, ColorUtil.getBlue(c) / 255f, ColorUtil.getAlpha(c) / 255f, fromX, fromY, toX, toY, radius, samples);
        endRender();

    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radius, toY - radius, radius}, new double[]{toX - radius, fromY + radius, radius}, new double[]{fromX + radius, fromY + radius, radius}, new double[]{fromX + radius, toY - radius, radius}};
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
            }
            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    public static void endRender() {

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    /*public static void drawArrow(PoseStack stack, Vector3d vector3d) {
        NATIVE ERROR

        double x = vector3d.x - mc.player.getX();
        double z = vector3d.z - mc.player.getZ();

        double cos = MathHelpper.cos(Mine.mc.player.getYRot() * (Math.PI * 2 / 360));
        double sin = MathHelpper.sin(Mine.mc.player.getYRot() * (Math.PI * 2 / 360));
        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);
        double dst = Math.sqrt(Math.pow(vector3d.x - Mine.mc.player.getX(), 2) + Math.pow(vector3d.z - Mine.mc.player.getZ(), 2));

        float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);
        double x2 = 75 * MathHelpper.cos(Math.toRadians(angle)) + Mine.mc.getWindow().getGuiScaledWidth() / 2f;
        double y2 = 75 * (Mine.mc.player.getXRot() / 90) * MathHelpper.sin(Math.toRadians(angle)) + Mine.mc.getWindow().getGuiScaledHeight() / 2f;

        stack.pushPose();
        RenderSystem.disableBlend();
       // GL11.glTranslated(x2, y2, 0);
       // GL11.glRotatef(angle, 0, 0, 1);

        int clr = new Color(200,200,200,200).getRGB();

        //RenderUtil.Render2D.drawShadow(-3F, -3F, 8, 6F, 8, clr);
        RectUtil.drawTriangle(-4, -1F, 4F, 7F, 1, new Color(0, 0, 0, 32).getRGB());
        RectUtil.drawTriangle(-3F, 0F, 3F, 5F, 1, new Color(255,255,255,255).getRGB());
        // GL11.glRotatef(90, 0, 0, 1);
       // Fonts.mntsb32[14].drawCenteredStringWithOutline(stack, "Навигатор", 0, 7, -1);

        //Fonts.mntsb32[14].drawCenteredStringWithOutline(stack, (int) dst + "m", 0, 15, -1);

        RenderSystem.enableBlend();
        stack.popPose();//matrices.pop
    }*/



   public static void drawBox(PoseStack poseStack, AABB aabb, int color) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f m = poseStack.last().pose();
        RenderSystem.disableDepthTest();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        setFilledBoxVertexes(bufferBuilder, m, aabb, color);
        tessellator.end();
        endRender();
        RenderSystem.enableDepthTest();
    }
    public static HashMap<Vector4d, Player> positions = new HashMap<>();



    public static void preShaderDraw(PoseStack matrices, float x, float y, float width, float height) {
        setupRender();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = matrices.last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        setRectanglePoints(buffer, matrix, x, y, x + width, y + height);
    }

    public static void setRectanglePoints(BufferBuilder buffer, Matrix4f matrix, float x, float y, float x1, float y1) {
        buffer.vertex(matrix, x, y, 0).endVertex();
        buffer.vertex(matrix, x, y1, 0).endVertex();
        buffer.vertex(matrix, x1, y1, 0).endVertex();
        buffer.vertex(matrix, x1, y, 0).endVertex();
    }



    private void renderBox(double x, double y, double endX, double endY, int colors) {
        int getColor = colors;
        RenderUtil.drawMcRectBuilding(x - 0.5F, y - 0.5F, x + 5, y + 1, getColor);
        RenderUtil.drawMcRectBuilding(endX - 5, y - 0.5F, endX + 1, y + 1, getColor);
        RenderUtil.drawMcRectBuilding(x - 0.5F, endY - 0.5F, x + 5, endY + 1, getColor);
        RenderUtil.drawMcRectBuilding(endX - 5, endY - 0.5F, endX + 1, endY + 1, getColor);

        RenderUtil.drawMcRectBuilding(x - 0.5F, y + 1, x + 1, y + 5, getColor);
        RenderUtil.drawMcRectBuilding(x - 0.5F, endY - 5, x + 1, endY, getColor);
        RenderUtil.drawMcRectBuilding(endX - 0.5F, y + 1, endX + 1, y + 5, getColor);
        RenderUtil.drawMcRectBuilding(endX - 0.5F, endY - 5, endX + 1, endY, getColor);
    }

    public static void drawTextIn3D(String text, Vec3 pos, double offX, double offY, double textOffset, @NotNull int color, PoseStack matrices) {
        Camera camera = mc.gameRenderer.getMainCamera();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        matrices.translate(pos.x() - camera.getPosition().x, pos.y() - camera.getPosition().y(), pos.z() - camera.getPosition().z());
        setupRender();
        matrices.translate(offX, offY - 0.1, -0.01);
        matrices.scale(-0.025f, -0.025f, 0);
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        FontRenderers.msSemi16.drawCenteredString(matrices, text, textOffset, 0f, color);
        immediate.endBatch();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        endRender();
    }

    public static void setFilledBoxVertexes(BufferBuilder bufferBuilder, Matrix4f m, AABB box, int c) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPosition().x());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPosition().y());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPosition().z());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPosition().x());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPosition().y());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPosition().z());

        bufferBuilder.vertex(m, minX, minY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c).endVertex();

        bufferBuilder.vertex(m, minX, minY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c).endVertex();

        bufferBuilder.vertex(m, maxX, minY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c).endVertex();

        bufferBuilder.vertex(m, minX, minY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c).endVertex();

        bufferBuilder.vertex(m, minX, minY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c).endVertex();

        bufferBuilder.vertex(m, minX, maxY, minZ).color(c).endVertex();
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c).endVertex();
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c).endVertex();
    }

    public static void renderBox2(PoseStack stack ,double x, double y, double endX, double endY, int colors) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Matrix4f m = stack.last().pose();
        RenderSystem.disableDepthTest();
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        float size = Mth.clamp(1 + 1, 2, 5);
        int getColor = colors;
        drawMiniRect(x - 0.5F, y - 0.5F, x + 5, y + 1, getColor);
        drawMiniRect(endX - 5, y - 0.5F, endX + 1, y + 1, getColor);
        drawMiniRect(x - 0.5F, endY - 0.5F, x + 5, endY + 1, getColor);
        drawMiniRect(endX - 5, endY - 0.5F, endX + 1, endY + 1, getColor);

        drawMiniRect(x + 3.5F, y + 0, x + 5, y - 5, getColor);
        drawMiniRect(x + 3.5F, endY - 0, x + 5, endY + 5, getColor);
        drawMiniRect(endX - 3.5F, y - 5, endX - 5, y + 0, getColor);
        drawMiniRect(endX - 3.5F, endY - 0, endX - 5, endY + 5, getColor);

        tessellator.end();
        endRender();
        RenderSystem.enableDepthTest();


    }

    public static void drawMiniRect(double left,
                                          double top,
                                          double right,
                                          double bottom,
                                          int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.vertex(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(left, top, 0.0F).color(f, f1, f2, f3).endVertex();

    }

    public static @NotNull PoseStack matrixFrom(double x, double y, double z) {
        PoseStack matrices = new PoseStack();

        Camera camera = mc.getInstance().gameRenderer.getMainCamera();
        matrices.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

        matrices.translate(x - camera.getPosition().x(), y - camera.getPosition().y, z - camera.getPosition().z);

        return matrices;
    }

    public static void drawSphere(PoseStack matrix, float radius, int slices, int stacks, int color) {
        float drho = 3.1415927F / ((float) stacks);
        float dtheta = 6.2831855F / ((float) slices - 1f);
        float rho;
        float theta;
        float x;
        float y;
        float z;
        int i;
        int j;
        setupRender();
        for (i = 1; i < stacks; ++i) {
            rho = (float) i * drho;
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (j = 0; j < slices; ++j) {
                theta = (float) j * dtheta;
                x = (float) (Math.cos(theta) * Math.sin(rho));
                y = (float) (Math.sin(theta) * Math.sin(rho));
                z = (float) Math.cos(rho);
                bufferBuilder.vertex(matrix.last().pose(), x * radius, y * radius, z * radius).color(color).endVertex();
            }
            tessellator.end();
        }

        for (j = 0; j < slices; ++j) {
            theta = (float) j * dtheta;

            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for (i = 0; i <= stacks; ++i) {
                rho = (float) i * drho;
                x = (float) (Math.cos(theta) * Math.sin(rho));
                y = (float) (Math.sin(theta) * Math.sin(rho));
                z = (float) Math.cos(rho);
                bufferBuilder.vertex(matrix.last().pose(), x * radius, y * radius, z * radius).color(color).endVertex();
            }
            tessellator.end();
        }
        endRender();
    }

    public static void drawCircle3D(PoseStack stack, Entity ent, float radius, int color, int points, boolean hudColor, int colorOffset) {
        setupRender();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        double x = ent.xo + (ent.getX() - ent.xo) * mc.getFrameTime() - mc.getEntityRenderDispatcher().camera.getPosition().x;
        double y = ent.yo + (ent.getY() - ent.yo) * mc.getFrameTime() - mc.getEntityRenderDispatcher().camera.getPosition().y;
        double z = ent.zo + (ent.getZ() - ent.zo) * mc.getFrameTime() - mc.getEntityRenderDispatcher().camera.getPosition().z;
        stack.pushPose();
        stack.translate(x, y, z);

        Matrix4f matrix = stack.last().pose();
        for (int i = 0; i <= points; i++) {
            if (hudColor)
                color = Color.white.getRGB();

            bufferBuilder.vertex(matrix, (float) (radius * Math.cos(i * 6.28 / points)), 0f, (float) (radius * Math.sin(i * 6.28 / points))).color(color).endVertex();
        }

        tessellator.end();
        endRender();
        stack.translate(-x, -y, -z);
        stack.popPose();
    }


    public static void drawRect(PoseStack poseStack, double x, double y, double width, double height, Color color) {

        //RenderSystem.
        RenderSystem.disableDepthTest();
        setupRender();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        Matrix4f m = poseStack.last().pose();
       // bufferBuilder.vertex(m, x, y).color(color).endVertex();
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y - height);
        GL11.glVertex2d(x, y - height);
        tesselator.end();

        endRender();
    }

    public static void drawSetup() {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void drawFinish() {
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.resetTextureMatrix();
    }


    public static void renderCrosses(@NotNull AABB box, int color, float lineWidth) {
        setupRender();
        PoseStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        box = box.offset(box.minX, box.minY, box.minZ);

        vertexLine(matrices, buffer, (float) box.maxX, (float) box.minY, (float) box.minZ, (float) box.minX, (float) box.minY, (float) box.maxZ, color);
        vertexLine(matrices, buffer, (float) box.minX, (float) box.minY, (float) box.minZ, (float) box.maxX, (float) box.minY, (float) box.maxZ, color);

        tessellator.end();
        RenderSystem.enableCull();
        endRender();
    }

    public static void drawTargetEsp(PoseStack stack, @NotNull Entity target) {
        ArrayList<Vec3> vecs = new ArrayList<>();
        ArrayList<Vec3> vecs1 = new ArrayList<>();
        ArrayList<Vec3> vecs2 = new ArrayList<>();

        double x = target.xo + (target.getX() - target.xo) * mc.getFrameTime() - mc.getEntityRenderDispatcher().camera.getPosition().x;
        double y = target.yo + (target.getY() - target.yo) * mc.getFrameTime() - mc.getEntityRenderDispatcher().camera.getPosition().y;
        double z = target.zo + (target.getZ() - target.zo) * mc.getFrameTime() - mc.getEntityRenderDispatcher().camera.getPosition().z;


        double height = target.getBbHeight();

        for (int i = 0; i <= 361; ++i) {
            double v = Math.sin(Math.toRadians(i));
            double u = Math.cos(Math.toRadians(i));
            Vec3 vec = new Vec3((float) (u * 0.5f), height, (float) (v * 0.5f));
            vecs.add(vec);

            double v1 = Math.sin(Math.toRadians((i + 120) % 360));
            double u1 = Math.cos(Math.toRadians(i + 120) % 360);
            Vec3 vec1 = new Vec3((float) (u1 * 0.5f), height, (float) (v1 * 0.5f));
            vecs1.add(vec1);

            double v2 = Math.sin(Math.toRadians((i + 240) % 360));
            double u2 = Math.cos(Math.toRadians((i + 240) % 360));
            Vec3 vec2 = new Vec3((float) (u2 * 0.5f), height, (float) (v2 * 0.5f));
            vecs2.add(vec2);
            height -= 0.004f;
        }

        stack.pushPose();
        stack.translate(x, y, z);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        setupRender();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();


        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = stack.last().pose();

        for (int j = 0; j < vecs.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - System.currentTimeMillis()) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(matrix, (float) vecs.get(j).x, (float) vecs.get(j).y, (float) vecs.get(j).z).color(injectAlpha(new Color(255,255,255), (int) (alpha * 255)).getRGB()).endVertex();
            bufferBuilder.vertex(matrix, (float) vecs.get(j + 1).x, (float) vecs.get(j + 1).y + 0.1f, (float) vecs.get(j + 1).z).color(injectAlpha(new Color(255,255,255), (int) (alpha * 255)).getRGB()).endVertex();
        }
        tessellator.end();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int j = 0; j < vecs1.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - System.currentTimeMillis()) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(matrix, (float) vecs1.get(j).x, (float) vecs1.get(j).y, (float) vecs1.get(j).z).color(injectAlpha(new Color(255,255,255), (int) (alpha * 255)).getRGB()).endVertex();
            bufferBuilder.vertex(matrix, (float) vecs1.get(j + 1).x, (float) vecs1.get(j + 1).y + 0.1f, (float) vecs1.get(j + 1).z).color(injectAlpha(new Color(255,255,255), (int) (alpha * 255)).getRGB()).endVertex();
        }
        tessellator.end();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int j = 0; j < vecs2.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - System.currentTimeMillis()) / 5f)) % 360) / 60f;
            bufferBuilder.vertex(matrix, (float) vecs2.get(j).x, (float) vecs2.get(j).y, (float) vecs2.get(j).z).color(injectAlpha(new Color(255,255,255), (int) (alpha * 255)).getRGB()).endVertex();
            bufferBuilder.vertex(matrix, (float) vecs2.get(j + 1).x, (float) vecs2.get(j + 1).y + 0.1f, (float) vecs2.get(j + 1).z).color(injectAlpha(new Color(255,255,255), (int) (alpha * 255)).getRGB()).endVertex();
        }
        tessellator.end();

        RenderSystem.enableCull();
        stack.translate(-x, -y, -z);
        endRender();
        RenderSystem.enableDepthTest();
        stack.popPose();
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Mth.clamp(alpha, 0, 255));
    }


    public static void drawHoleOutline(@NotNull AABB box, int color, float lineWidth) {
        setupRender();
        PoseStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(lineWidth);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        box = box.offset(box.minX, box.minY, box.minZ);

        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float y2 = (float) box.maxY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float z2 = (float) box.maxZ;

        vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
        vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);

        vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
        vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
        vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
        vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);

        tessellator.end();
        RenderSystem.enableCull();
        endRender();
    }

    public static void vertexLine(@NotNull PoseStack matrices, @NotNull VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, @NotNull int lineColor) {
        Matrix4f model = matrices.last().pose();
        Matrix3f entry = matrices.last().normal();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);


        //TODO Test
        buffer.vertex(model, x1, y1, z1).color(ColorUtil.getRed(lineColor), ColorUtil.getGreen(lineColor), ColorUtil.getBlue(lineColor), ColorUtil.getAlpha(lineColor)).normal(entry, normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
        buffer.vertex(model, x2, y2, z2).color(ColorUtil.getRed(lineColor), ColorUtil.getGreen(lineColor), ColorUtil.getBlue(lineColor), ColorUtil.getAlpha(lineColor)).normal(entry,normalVec.x(), normalVec.y(), normalVec.z()).endVertex();
    }

    public static @NotNull Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = Mth.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }

    public static void drawMcRectBuilding(double left,
                                          double top,
                                          double right,
                                          double bottom,
                                          int color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.vertex(left, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(right, bottom, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(right, top, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(left, top, 0.0F).color(f, f1, f2, f3).endVertex();

    }







}
