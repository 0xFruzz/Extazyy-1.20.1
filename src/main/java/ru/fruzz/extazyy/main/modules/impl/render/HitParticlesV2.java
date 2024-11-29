package ru.fruzz.extazyy.main.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import lombok.Getter;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import ru.fruzz.extazyy.misc.event.EventHandler;
import ru.fruzz.extazyy.misc.event.events.impl.EventDamageEntity;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.main.modules.ModuleApi.CategoryUtil;
import ru.fruzz.extazyy.main.modules.ModuleApi.Module;
import ru.fruzz.extazyy.main.modules.ModuleApi.ModuleAnnotation;
import ru.fruzz.extazyy.main.modules.tools.imp.ModeTools;
import ru.fruzz.extazyy.main.modules.tools.imp.NumberTools;
import ru.fruzz.extazyy.misc.util.math.MathUtils;
import ru.fruzz.extazyy.misc.util.render.TestRender;
import ru.fruzz.extazyy.misc.util.color.ColorUtils;

import java.awt.*;
import java.util.ArrayList;

@ModuleAnnotation(name = "HitParticles", type = CategoryUtil.Render)
public class HitParticlesV2 extends Module {

    public ModeTools particletype = new ModeTools("Партиклы", "Рандомные", "Лого", "Сердечко", "Поинт", "Доллар");

    public NumberTools numberTools = new NumberTools("Кол-во партиклов", 15, 1, 30, 1);

    public ModeTools physich = new ModeTools("Физика", "Летающие", "Летающие", "Падающие");


    public HitParticlesV2() {
        addSettings(particletype, numberTools, physich);
    }

    //Пупу, список
    private final ArrayList<ParticleBase> particles = new ArrayList<>();


    /*
    Рендер 3Д, перебераем созданный ранее список всех партиклов
     */
    @EventHandler
    public void onup(RenderEvent3D e) {

        // сиськи жопа сиськи жопа
        particles.forEach(p -> p.render(e.getPoseStack()));
        //

    }
    /*
    ТикЕвент, каждый тик проверка на жизнь партикла и его удаление
     */
    @EventHandler
    public void Tick(TickEvent e) {
        particles.removeIf(ParticleBase::tick);
    }

    /*
    Дамадж евент, вызываем когда проходит удар по кому нить, спамним партиклы в радиусе 3 блоков
     */
    @EventHandler
    public void attackEvent(EventDamageEntity e) {
        for (int j = 0; j < numberTools.getValue().intValue(); j++) {
            particles.add(new ParticleBase(
                    (float) MathUtils.randomNumber(e.getTarget().getX(), e.getTarget().getX() + MathUtils.randomizeFloat(-3, 3)),
                    MathUtils.randomizeFloat((float) (e.getTarget().getY() + e.getTarget().getBbHeight() + 0.5f), (float) e.getTarget().getY()),
                    (float) MathUtils.randomNumber(e.getTarget().getZ(), e.getTarget().getZ() + MathUtils.randomizeFloat(-3, 3)),
                    0,
                    Mth.random(-0.2f, -0.05f),
                    0,
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
            case "Поинт":
                return new ResourceLocation("minecraft", "extazyy/images/glow.png");
            default:
                return new ResourceLocation("minecraft", "extazyy/images/dollar.png");
        }

    }

    
public class ParticleBase {

        private float prevposX, prevposY, prevposZ, posX, posY, posZ, motionX, motionY, motionZ;
        @Getter
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

            age = MathUtils.randomInt(25, 75);;
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
            if (posBlock(posX, posY - 3f / 10f, posZ)) {
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

        public void render(PoseStack stack) {
            stack.pushPose();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
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

            bufferBuilder.vertex(matrix1, 0, -0.5f, 0).uv(0f, 1f).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
            bufferBuilder.vertex(matrix1, -0.5f, -0.5f, 0).uv(1f, 1f).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
            bufferBuilder.vertex(matrix1, -0.5f, 0, 0).uv(1f, 0).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();
            bufferBuilder.vertex(matrix1, 0, 0, 0).uv(0, 0).color(TestRender.injectAlpha(color1, (int) (255 * ((float) age / (float) maxAge))).getRGB()).endVertex();

            BufferUploader.drawWithShader(bufferBuilder.end());
            RenderSystem.depthMask(true);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            stack.popPose();
        }
    }
}
