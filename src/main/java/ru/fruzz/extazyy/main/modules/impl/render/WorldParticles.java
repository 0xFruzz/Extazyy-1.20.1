package ru.fruzz.extazyy.main.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.util.math.MathUtils;
import ru.fruzz.extazyy.misc.util.Mine;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

import java.awt.*;
import java.util.ArrayList;

@ModuleAnnotation(name = "WorldParticles", type = CategoryUtil.Render)
public class WorldParticles extends Module {

    public ModeTools particletype = new ModeTools("Партиклы", "Сердечко", "Сердечко", "Блум", "Доллар", "Перышки", "Блик", "Звездочка");

    public NumberTools numberTools = new NumberTools("Кол-во партиклов", 500, 1, 500, 1).setVisible(() -> !particletype.is("Перышки"));

    public NumberTools size = new NumberTools("Размер партиклов", 0.4f, 0, 2, 0.01f);

    public ModeTools physich = new ModeTools("Физика", "Летающие", "Летающие", "Падающие").setVisible(() -> !particletype.is("Перышки")).setVisible(() -> Extazyy.userInfo.isHasAdmin());


    public WorldParticles() {
        addSettings(particletype, numberTools, size);
    }

    //Пупу, список
    private final ArrayList<ParticleBase> particles = new ArrayList<>();


    /*
    Рендер 3Д, перебераем созданный ранее список всех партиклов
     */
    @EventHandler
    public void onup(RenderEvent3D e) {
        PoseStack stack = e.getPoseStack();
        stack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        // сиськи жопа сиськи жопа
        particles.forEach(p -> p.render(bufferBuilder));
        //
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        stack.popPose();
    }
    /*
    ТикЕвент, каждый тик проверка на жизнь партикла и его удаление
    Если партиклов стало меньше чем выставлено в настройках модуля - создаем новые в рандомной точке X Z - 96 блоков (48 вперед 48 назад) Y - 40 блоков (20 вверх 20 вниз)
     */
    @EventHandler
    public void Tick(TickEvent e) {
        particles.removeIf(ParticleBase::tick);
            if(particletype.is("Перышки")) {
                for (int j = 1; j < 50; j++) {
                    Mine.mc.level.addParticle(ParticleTypes.CHERRY_LEAVES, (mc.player.getX() + Mth.random(-20f, 20f)), (float) (mc.player.getY() + Mth.random(-20, 20f)), (float) (mc.player.getZ() + Mth.random(-20f, 20f)), 0.0D, 0.0D, 0.0D);
                }

                return;
            }
            for (int j = particles.size(); j < numberTools.getValue().intValue(); j++) {
                boolean drop = true;
                particles.add(new ParticleBase(
                        (float) (mc.player.getX() + Mth.random(-20f, 20f)),
                        (float) (mc.player.getY() + Mth.random(-20, 20f)),
                        (float) (mc.player.getZ() + Mth.random(-20f, 20f)),
                        drop ? 0 : Mth.random(-0.4f, 0.4f),
                        drop ? Mth.random(-0.2f, -0.05f) : Mth.random(-0.1f, 0.1f),
                        drop ? 0 : Mth.random(-0.4f, 0.4f),
                        resourceLocation()));

            }

    }

    public ResourceLocation resourceLocation() {
        switch (particletype.get()) {
            case "Лого":
                return new ResourceLocation("minecraft", "extazyy/images/logoo.png");
            case "Доллар":
                return new ResourceLocation("minecraft", "extazyy/images/dollar.png");
            case "Сердечко":
                return new ResourceLocation("minecraft", "extazyy/images/heart.png");
            case "Блум":
                return new ResourceLocation("minecraft", "extazyy/images/bloom.png");
            case "Блик":
                return new ResourceLocation("minecraft", "extazyy/images/blick.png");
            case "Звездочка":
                return new ResourceLocation("minecraft", "extazyy/images/star.png");
            default:
                return new ResourceLocation("minecraft", "extazyy/images/dollar.png");
        }

    }

    
public class ParticleBase {

        private float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        private int age, maxAge;
        ResourceLocation resourceLocation;

        public ParticleBase(float posX, float posY, float posZ, float motionX, float motionY, float motionZ, ResourceLocation type) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            prevposX = posX;
            prevposY = posY;
            prevposZ = posZ;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            age = MathUtils.randomInt(25, 205);;
            maxAge = age;
            this.resourceLocation = type;
        }

    public boolean tick() {
        age--;
        if(age < 0) {
            return true;
        }

        double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
        prevposX = posX;
        prevposY = posY;
        prevposZ = posZ;

        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        float x = posX;
        float y = posY;
        float z = posZ;
       /* if (posBlock(posX, posY - 3f / 10f, posZ)) {
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
        }*/

        if (physich.is("Падающие"))
            motionY -= 0.035f;

        motionX /= 1.005f;
        motionZ /= 1.005f;
        motionY /= 1.005f;


        return false;
    }

    private boolean posBlock(double x, double y, double z) {
        Block b = mc.level.getBlockState(BlockPos.containing(x, y, z)).getBlock();
        return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
    }



        public static Vec3 interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
            double x = prevposX + ((posX - prevposX) * mc.getFrameTime()) - mc.getEntityRenderDispatcher().camera.getPosition().x;
            double y = prevposY + ((posY - prevposY) * mc.getFrameTime()) - mc.getEntityRenderDispatcher().camera.getPosition().y();
            double z = prevposZ + ((posZ - prevposZ) * mc.getFrameTime()) - mc.getEntityRenderDispatcher().camera.getPosition().z;
            return new Vec3(x, y, z);
        }

        public void render(BufferBuilder bufferBuilder) {

            RenderSystem.setShaderTexture(0, this.resourceLocation);
            Camera camera = mc.gameRenderer.getMainCamera();
            Color color1 = new Color(ColorUtils.getRed(ColorUtils.getColorStyle(90)), ColorUtils.getGreen(ColorUtils.getColorStyle(90)),ColorUtils.getBlue(ColorUtils.getColorStyle(90)),255);
            Vec3 pos = interpolatePos(prevposX, prevposY, prevposZ, posX, posY, posZ);

            PoseStack matrices = new PoseStack();
            matrices.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
            matrices.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
            matrices.translate(pos.x, pos.y, pos.z);
            matrices.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            matrices.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

            Matrix4f matrix1 = matrices.last().pose();

            bufferBuilder.vertex(matrix1, 0, -size.getValue().floatValue(), 0).uv(0f, 1f).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
            bufferBuilder.vertex(matrix1, -size.getValue().floatValue(), -size.getValue().floatValue(), 0).uv(1f, 1f).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
            bufferBuilder.vertex(matrix1, -size.getValue().floatValue(), 0, 0).uv(1f, 0).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
            bufferBuilder.vertex(matrix1, 0, 0, 0).uv(0, 0).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
        }
    }
}
