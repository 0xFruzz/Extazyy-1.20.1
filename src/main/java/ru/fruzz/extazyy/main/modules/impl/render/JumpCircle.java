package ru.fruzz.extazyy.main.modules.impl.render;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL46;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventPlayerJump;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.util.anim.Anim;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.render.dangertech.Vec2Vector;
import ru.fruzz.extazyy.misc.util.gif.Gif;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@ModuleAnnotation(name = "Jump Circle", type = CategoryUtil.Render)
public class JumpCircle extends Module {

    ArrayList<Circle> circles = new ArrayList<>();

    public static NumberTools scale = new NumberTools("Scale", 1, 0.5f, 5, 0.1f);
    public static NumberTools speed = new NumberTools("Speed", 10, 5.5f, 25, 0.1f);
    public static NumberTools time = new NumberTools("Time", 6, 0.5f, 25, 0.1f);
    public static ModeTools image = new ModeTools("Image", "Анархия", "Звезда", "Анархия", "Круг");
    static Gif gif;

    public JumpCircle() {
        addSettings(speed, scale, image, time);
    }

    private static ResourceLocation getImage() {
        switch (image.get()) {
            case "Звезда":
                return new ResourceLocation("minecraft", "extazyy/images/star.png");
            case "Анархия":
                return new ResourceLocation("minecraft", "extazyy/images/jump.png");
            case "Круг":
                return new ResourceLocation("minecraft", "extazyy/images/circle.png");
            default:
                return new ResourceLocation("minecraft", "extazyy/images/jump.png");
        }
    }


    public void onEnable() {
        try {
            gif = new Gif(new ResourceLocation("minecraft", "extazyy/gif/hearts.gif"));
        } catch (IOException e) {
        }
    }


    @EventHandler
    public void onjump(EventPlayerJump e) {
        int color1 = ColorUtil.getColorStyle(0);
        int color2 = ColorUtil.getColorStyle(30);
        int colorMiddle = ColorUtil.getColorStyle(15);
        this.circles.add(new Circle(new Vector3d(mc.player.getX(), mc.player.getY() + 1f, mc.player.getZ()), color1, colorMiddle, color2, colorMiddle));
    }

    @EventHandler
    public void onRender3D(RenderEvent3D event) {
        circles.removeIf(Circle::needToRemove);
        circles.stream().filter(Objects::nonNull).forEach(circleObj -> {
            circleObj.render(event, circleObj);
        });
    }

    private static float getMaxLifeTime() {
        return 1000 * Extazyy.moduleManager.jumper.time.getValue().floatValue();
    }

    private static float getMaxLifeTime2() {
        return 100 * Extazyy.moduleManager.jumper.speed.getValue().floatValue() * 2;
    }

    private static class Circle {
        public Vector3d pos;
        public int color, color2, color3, color4;
        public long bornTime;
        public long bornTime2;
        public Anim anim;
        public float alpha = 1.0f;
        private float scaleValue = 0.0f; // Значение для плавного увеличения и уменьшения размера

        public Circle(Vector3d pos, int color, int color2, int color3, int color4) {
            this.pos = pos;

            this.color = color;
            this.color2 = color2;
            this.color3 = color3;
            this.color4 = color4;
            bornTime = System.currentTimeMillis();
            bornTime2 = System.currentTimeMillis();
            this.anim = new Anim();
        }

        public float getLifeProgress() {
            return (System.currentTimeMillis() - bornTime) / getMaxLifeTime();
        }

        public float getCircleProgress() {
            return (System.currentTimeMillis() - bornTime2) / getMaxLifeTime2();
        }

        public boolean needToRemove() {
            return alpha <= 0;
        }

        public void render(RenderEvent3D event, Circle circleObj) {
            circleObj.updateAlpha();
            circleObj.updateScale(); // Обновление значения масштаба
            PoseStack matrixStack = event.getPoseStack();
            matrixStack.pushPose();
            final double x = circleObj.pos.x();
            final double y = circleObj.pos.y();
            final double z = circleObj.pos.z();
            final EntityRenderDispatcher renderManager = mc.getInstance().getEntityRenderDispatcher();
            final Vector3d renderPos = Vec2Vector.convert(renderManager.camera.getPosition());
            matrixStack.translate((float) (x - renderPos.x()), (float) (y - renderPos.y()) - 0.45f, (float) (z - renderPos.z()));
            assert mc.player != null;
            matrixStack.mulPose(Axis.YN.rotation((float) Math.toRadians(180.0f)));
            matrixStack.mulPose(Axis.XN.rotation((float) Math.toRadians(-90)));

            // Используем масштабирование с плавным изменением размера
            matrixStack.scale(scaleValue, scaleValue, scaleValue);

            matrixStack.pushPose();
            matrixStack.translate(0, 0, 0.5f);

            float height = 33;
            float width = 33;

            DrawHelper.scale(matrixStack, -width / 2, -height / 2, width, height, scale.getValue().floatValue() / 4, () -> {
                DrawHelper.rotate(matrixStack, -width / 2, -height / 2, width, height, circleObj.getCircleProgress() * 360, () -> {

                    RenderSystem.enableDepthTest();
                    RenderSystem.depthMask(false);
                    RenderSystem.disableCull();
                    RenderSystem.setShaderColor(1, 1, 1, circleObj.getAlpha());
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GL46.GL_SRC_ALPHA, GL46.GL_ONE);

                    RenderSystem.setShaderTexture(0, getImage());
                    RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

                    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                    buffer.vertex(matrixStack.last().pose(), -5, -5, 0).uv(0.0F, 0.0F).color(ColorUtil.r(circleObj.color), ColorUtil.g(circleObj.color), ColorUtil.b(circleObj.color), circleObj.getAlpha()).endVertex();
                    buffer.vertex(matrixStack.last().pose(), -5, 5, 0).uv(0.0F, 1.0F).color(ColorUtil.r(circleObj.color2), ColorUtil.g(circleObj.color2), ColorUtil.b(circleObj.color2), circleObj.getAlpha()).endVertex();
                    buffer.vertex(matrixStack.last().pose(), 5, 5, 0).uv(1.0F, 1.0F).color(ColorUtil.r(circleObj.color3), ColorUtil.g(circleObj.color3), ColorUtil.b(circleObj.color3), circleObj.getAlpha()).endVertex();
                    buffer.vertex(matrixStack.last().pose(), 5, -5, 0).uv(1.0F, 0.0F).color(ColorUtil.r(circleObj.color4), ColorUtil.g(circleObj.color4), ColorUtil.b(circleObj.color4), circleObj.getAlpha()).endVertex();

                    BufferUploader.drawWithShader(buffer.end());

                    RenderSystem.depthMask(true);
                    RenderSystem.enableCull();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                });
            });
            matrixStack.popPose();
            matrixStack.popPose();
        }

        public void updateAlpha() {
            float lifeProgress = getLifeProgress();
            if (lifeProgress > 0.9f) {
                this.alpha = Math.max(1.0f - (lifeProgress - 0.85f) / 0.3f, 0.0f);
            }
        }

        public void updateScale() {
            float lifeProgress = getLifeProgress();
            // Увеличение масштаба при появлении и уменьшение перед исчезновением
            if (lifeProgress < 0.1f) {
                this.scaleValue = Math.min(lifeProgress / 0.1f, 1.0f); // Увеличение масштаба с 0 до 1
            } else if (lifeProgress > 0.9f) {
                this.scaleValue = Math.max(1.0f - (lifeProgress - 0.9f) / 0.1f, 0.0f); // Уменьшение масштаба с 1 до 0
            } else {
                this.scaleValue = 1.0f; // Полный масштаб в остальное время
            }
        }

        public float getAlpha() {
            return alpha;
        }
    }
}

