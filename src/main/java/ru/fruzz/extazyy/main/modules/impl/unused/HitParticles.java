package ru.fruzz.extazyy.main.modules.impl.unused;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventDamageEntity;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.font.FontRenderers;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.misc.util.anim.Anim;
import ru.fruzz.extazyy.misc.util.math.MathUtils;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

@ModuleAnnotation(name = "HitParticles", type = CategoryUtil.Render)
public class HitParticles extends Module {

     private final HashMap<Integer, Float> healthMap = new HashMap<>();
    private final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();

    public ModeTools particletype = new ModeTools("Партиклы", "Рандомные", "Рандомные", "Лого", "Сердечко", "Разбитое сердце", "Анархия", "Хп", "Сердечко красиви", "Поинт", "Доллар");

    public NumberTools numberTools = new NumberTools("Кол-во партиклов", 8, 1, 25, 1);

    public ModeTools physich = new ModeTools("Физика", "Летающие", "Летающие", "Падающие");

    public HitParticles() {
        addSettings(particletype, numberTools, physich);
    }

    @EventHandler
    public void attack(EventDamageEntity e) {
        Color c = new Color(255,255,255,255);
        int type;
        boolean rand = false;
        switch (particletype.get()) {
            case "Рандомные":
                rand = true;
                type = 0;
                break;
            case "Лого":
                type = 5;
                break;
            case "Хп":
                type = 4;
                break;
            case "Разбитое сердце":
                type = 2;
                break;
            case "Сердечко":
                type = 3;
                break;
            case "Сердечко красиви":
                type = 6;
                break;
            case "Поинт":
                type = 7;
                break;
            case "Доллар":
                type = 8;
                break;
            case "Анархия":
                type = 1;
                break;
            default:
                rand = false;
                type = 1;
                break;
        }

        for (int i = 0; i < numberTools.getValue().intValue(); i++) {
            particles.add(new Particle((float) MathUtils.randomNumber(e.getTarget().getX(), e.getTarget().getX() + MathUtils.randomizeFloat(-3, 3)), MathUtils.randomizeFloat((float) (e.getTarget().getY() + e.getTarget().getBbHeight() + 0.5f), (float) e.getTarget().getY()), (float) MathUtils.randomNumber(e.getTarget().getZ(), e.getTarget().getZ() + MathUtils.randomizeFloat(-3, 3)), c,
                    MathUtils.randomizeFloat(0, 180), MathUtils.randomizeFloat(10f, 60f), 20, rand ? MathUtils.randomInt(1, 8) : type));
        }
    }

    @EventHandler
    public void onUpdate(TickEvent e) {
        particles.removeIf(Particle::update);

    }


    @EventHandler
    public void on3D(RenderEvent3D e) {
        RenderSystem.enableDepthTest();
        if (mc.player != null && mc.level != null) {
            for (Particle particle : particles) {
                particle.render(e.getPoseStack());
            }
        }
        RenderSystem.disableDepthTest();
    }


    public class Particle {
        float x;
        float y;
        float z;

        float px;
        float py;
        float pz;

        float motionX;
        float motionY;
        float motionZ;

        float rotationAngle;
        float rotationSpeed;
        float health;

        int type;

        long time;
        Color color;

        public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed, float health, int type) {
            this.x = x;
            this.y = y;
            this.z = z;
            px = x;
            py = y;
            pz = z;
            motionX = MathUtils.randomizeFloat(-(float) 1 / 100f, (float) 1 / 100f);
            motionY = MathUtils.randomizeFloat(-(float) 1 / 100f, (float) 1 / 100f);
            motionZ = MathUtils.randomizeFloat(-(float) 1 / 100f, (float) 1 / 100f);
            time = System.currentTimeMillis();
            this.type = type;
            this.color = color;
            this.rotationAngle = rotationAngle;
            this.rotationSpeed = rotationSpeed;
            this.health = health;
        }

        public long getTime() {
            return time;
        }

        public boolean update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
            px = x;
            py = y;
            pz = z;

            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - 3f / 10f, z)) {
                motionY = -motionY / 1.1f;
                motionX = motionX / 1.1f;
                motionZ = motionZ / 1.1f;
            } else {
                if (posBlock(x - sp, y, z - sp)
                        || posBlock(x + sp, y, z + sp)
                        || posBlock(x + sp, y, z - sp)
                        || posBlock(x - sp, y, z + sp)
                        || posBlock(x + sp, y, z)
                        || posBlock(x - sp, y, z)
                        || posBlock(x, y, z + sp)
                        || posBlock(x, y, z - sp)
                ) {
                    motionX = -motionX;
                    motionZ = -motionZ;
                }
            }

            if (physich.is("Падающие"))
                motionY -= 0.035f;

            motionX /= 1.005f;
            motionZ /= 1.005f;
            motionY /= 1.005f;

            return System.currentTimeMillis() - getTime() > MathUtils.randomNumber(8,1) * 1000;
        }

        public static double interpolate(double oldValue, double newValue, double interpolationValue) {
            return (oldValue + (newValue - oldValue) * interpolationValue);
        }

        public void render(PoseStack matrixStack) {
            float size = 1f;
            float scale = 0.025f * size;

            final double posX = interpolate(px, x, mc.getFrameTime()) - mc.getEntityRenderDispatcher().camera.getPosition().x;
            final double posY = interpolate(py, y, mc.getFrameTime()) + 0.1 - mc.getEntityRenderDispatcher().camera.getPosition().y;
            final double posZ = interpolate(pz, z, mc.getFrameTime()) - mc.getEntityRenderDispatcher().camera.getPosition().z;

            matrixStack.pushPose();
            matrixStack.translate(posX, posY, posZ);

            matrixStack.scale(scale, scale, scale);

            matrixStack.translate(size / 2, size / 2, size / 2);
            matrixStack.mulPose(Axis.YP.rotationDegrees(-mc.gameRenderer.getMainCamera().getYRot()));
            matrixStack.mulPose(Axis.XP.rotationDegrees(mc.gameRenderer.getMainCamera().getXRot()));
            if (particletype.is("Хп")) {
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
            } else {
                matrixStack.mulPose(Axis.ZP.rotationDegrees(rotationAngle += (float) (Anim.deltaTime() * rotationSpeed)));
            }
            matrixStack.translate(-size / 2, -size / 2, -size / 2);


            switch (type) {
                case 1:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/damage.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;
                case 2:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/critical_hit.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;
                case 3:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/enchanted_hit.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;
                case 4:
                    FontRenderers.msSemi14.drawCenteredString(matrixStack, health + "", 0,0, new Color(255,255,255,255));
                    break;
                case 5:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/logo2.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;
                case 6:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/heart.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;
                case 7:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/glow.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;
                case 8:
                    DrawHelper.drawTexture(new ResourceLocation("minecraft", "extazyy/images/dollar.png"), matrixStack.last().pose(), 0, 0, 10, 10);
                    break;

            }


            matrixStack.scale(0.8f, 0.8f, 0.8f);
            matrixStack.popPose();
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = mc.level.getBlockState(BlockPos.containing(x, y, z)).getBlock();
            return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
        }
    }

    public enum Physics {
        Fall, Fly
    }

    public enum Mode {
        Orbiz, Stars, Hearts, Bloom, Text
    }

    public enum ColorMode {
        Custom, Sync
    }
}


