package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent3D;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.*;
import net.optifine.Config;
import net.optifine.GlErrors;
import net.optifine.Lagometer;
import net.optifine.RandomEntities;
import net.optifine.entity.model.CustomEntityModels;
import net.optifine.gui.GuiChatOF;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.reflect.ReflectorResolver;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.TimedEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import ru.fruzz.extazyy.misc.util.render.dangertech.ESPUtil;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public class GameRenderer implements AutoCloseable {
    private static final ResourceLocation NAUSEA_LOCATION = new ResourceLocation("textures/misc/nausea.png");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEPTH_BUFFER_DEBUG = false;
    public static final float PROJECTION_Z_NEAR = 0.05F;
    private static final float GUI_Z_NEAR = 1000.0F;
    final Minecraft minecraft;
    private final ResourceManager resourceManager;
    private final RandomSource random = RandomSource.create();
    private float renderDistance;
    public final ItemInHandRenderer itemInHandRenderer;
    private final MapRenderer mapRenderer;
    private final RenderBuffers renderBuffers;
    private int tick;
    private float fov;
    private float oldFov;
    private float darkenWorldAmount;
    private float darkenWorldAmountO;
    private boolean renderHand = true;
    private boolean renderBlockOutline = true;
    private long lastScreenshotAttempt;
    private boolean hasWorldScreenshot;
    private long lastActiveTime = Util.getMillis();
    private final LightTexture lightTexture;
    private final OverlayTexture overlayTexture = new OverlayTexture();
    private boolean panoramicMode;
    private float zoom = 1.0F;
    private float zoomX;
    private float zoomY;
    public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
    @Nullable
    private ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;
    @Nullable
    PostChain postEffect;
    static final ResourceLocation[] EFFECTS = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
    public static final int EFFECT_NONE = EFFECTS.length;
    int effectIndex = EFFECT_NONE;
    private boolean effectActive;
    private final Camera mainCamera = new Camera();
    public ShaderInstance blitShader;
    private final Map<String, ShaderInstance> shaders = Maps.newHashMap();
    @Nullable
    private static ShaderInstance positionShader;
    @Nullable
    private static ShaderInstance positionColorShader;
    @Nullable
    private static ShaderInstance positionColorTexShader;
    @Nullable
    private static ShaderInstance positionTexShader;
    @Nullable
    private static ShaderInstance positionTexColorShader;
    @Nullable
    private static ShaderInstance particleShader;
    @Nullable
    private static ShaderInstance positionColorLightmapShader;
    @Nullable
    private static ShaderInstance positionColorTexLightmapShader;
    @Nullable
    private static ShaderInstance positionTexColorNormalShader;
    @Nullable
    private static ShaderInstance positionTexLightmapColorShader;
    @Nullable
    private static ShaderInstance rendertypeSolidShader;
    @Nullable
    private static ShaderInstance rendertypeCutoutMippedShader;
    @Nullable
    private static ShaderInstance rendertypeCutoutShader;
    @Nullable
    private static ShaderInstance rendertypeTranslucentShader;
    @Nullable
    private static ShaderInstance rendertypeTranslucentMovingBlockShader;
    @Nullable
    private static ShaderInstance rendertypeTranslucentNoCrumblingShader;
    @Nullable
    private static ShaderInstance rendertypeArmorCutoutNoCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntitySolidShader;
    @Nullable
    private static ShaderInstance rendertypeEntityCutoutShader;
    @Nullable
    private static ShaderInstance rendertypeEntityCutoutNoCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntityCutoutNoCullZOffsetShader;
    @Nullable
    private static ShaderInstance rendertypeItemEntityTranslucentCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntityTranslucentCullShader;
    @Nullable
    private static ShaderInstance rendertypeEntityTranslucentShader;
    @Nullable
    private static ShaderInstance rendertypeEntityTranslucentEmissiveShader;
    @Nullable
    private static ShaderInstance rendertypeEntitySmoothCutoutShader;
    @Nullable
    private static ShaderInstance rendertypeBeaconBeamShader;
    @Nullable
    private static ShaderInstance rendertypeEntityDecalShader;
    @Nullable
    private static ShaderInstance rendertypeEntityNoOutlineShader;
    @Nullable
    private static ShaderInstance rendertypeEntityShadowShader;
    @Nullable
    private static ShaderInstance rendertypeEntityAlphaShader;
    @Nullable
    private static ShaderInstance rendertypeEyesShader;
    @Nullable
    private static ShaderInstance rendertypeEnergySwirlShader;
    @Nullable
    private static ShaderInstance rendertypeLeashShader;
    @Nullable
    private static ShaderInstance rendertypeWaterMaskShader;
    @Nullable
    private static ShaderInstance rendertypeOutlineShader;
    @Nullable
    private static ShaderInstance rendertypeArmorGlintShader;
    @Nullable
    private static ShaderInstance rendertypeArmorEntityGlintShader;
    @Nullable
    private static ShaderInstance rendertypeGlintTranslucentShader;
    @Nullable
    private static ShaderInstance rendertypeGlintShader;
    @Nullable
    private static ShaderInstance rendertypeGlintDirectShader;
    @Nullable
    private static ShaderInstance rendertypeEntityGlintShader;
    @Nullable
    private static ShaderInstance rendertypeEntityGlintDirectShader;
    @Nullable
    private static ShaderInstance rendertypeTextShader;
    @Nullable
    private static ShaderInstance rendertypeTextBackgroundShader;
    @Nullable
    private static ShaderInstance rendertypeTextIntensityShader;
    @Nullable
    private static ShaderInstance rendertypeTextSeeThroughShader;
    @Nullable
    private static ShaderInstance rendertypeTextBackgroundSeeThroughShader;
    @Nullable
    private static ShaderInstance rendertypeTextIntensitySeeThroughShader;
    @Nullable
    private static ShaderInstance rendertypeLightningShader;
    @Nullable
    private static ShaderInstance rendertypeTripwireShader;
    @Nullable
    private static ShaderInstance rendertypeEndPortalShader;
    @Nullable
    private static ShaderInstance rendertypeEndGatewayShader;
    @Nullable
    private static ShaderInstance rendertypeLinesShader;
    @Nullable
    private static ShaderInstance rendertypeCrumblingShader;
    @Nullable
    private static ShaderInstance rendertypeGuiShader;
    @Nullable
    private static ShaderInstance rendertypeGuiOverlayShader;
    @Nullable
    private static ShaderInstance rendertypeGuiTextHighlightShader;
    @Nullable
    private static ShaderInstance rendertypeGuiGhostRecipeOverlayShader;
    private boolean initialized = false;
    private Level updatedWorld = null;
    private float clipDistance = 128.0F;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    private PostChain[] fxaaShaders = new PostChain[10];
    private boolean guiLoadingVisible = false;
    private static float renderPartialTicks;

    public GameRenderer(Minecraft pMinecraft, ItemInHandRenderer pItemInHandRenderer, ResourceManager pResourceManager, RenderBuffers pRenderBuffers) {
        this.minecraft = pMinecraft;
        this.resourceManager = pResourceManager;
        this.itemInHandRenderer = pItemInHandRenderer;
        this.mapRenderer = new MapRenderer(pMinecraft.getTextureManager());
        this.lightTexture = new LightTexture(this, pMinecraft);
        this.renderBuffers = pRenderBuffers;
        this.postEffect = null;
    }

    public void close() {
        this.lightTexture.close();
        this.mapRenderer.close();
        this.overlayTexture.close();
        this.shutdownEffect();
        this.shutdownShaders();
        if (this.blitShader != null) {
            this.blitShader.close();
        }

    }

    public void setRenderHand(boolean pRenderHand) {
        this.renderHand = pRenderHand;
    }

    public void setRenderBlockOutline(boolean pRenderBlockOutline) {
        this.renderBlockOutline = pRenderBlockOutline;
    }

    public void setPanoramicMode(boolean pPanoramicMode) {
        this.panoramicMode = pPanoramicMode;
    }

    public boolean isPanoramicMode() {
        return this.panoramicMode;
    }

    public void shutdownEffect() {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        this.postEffect = null;
        this.effectIndex = EFFECT_NONE;
    }

    public void togglePostEffect() {
        this.effectActive = !this.effectActive;
    }

    public void checkEntityPostEffect(@Nullable Entity pEntity) {
        if (this.postEffect != null) {
            this.postEffect.close();
        }

        this.postEffect = null;
        if (pEntity instanceof Creeper) {
            this.loadEffect(new ResourceLocation("shaders/post/creeper.json"));
        } else if (pEntity instanceof Spider) {
            this.loadEffect(new ResourceLocation("shaders/post/spider.json"));
        } else if (pEntity instanceof EnderMan) {
            this.loadEffect(new ResourceLocation("shaders/post/invert.json"));
        } else if (Reflector.ForgeHooksClient_loadEntityShader.exists()) {
            Reflector.call(Reflector.ForgeHooksClient_loadEntityShader, pEntity, this);
        }

    }

    public void cycleEffect() {
        if (this.minecraft.getCameraEntity() instanceof Player) {
            if (this.postEffect != null) {
                this.postEffect.close();
            }

            this.effectIndex = (this.effectIndex + 1) % (EFFECTS.length + 1);
            if (this.effectIndex == EFFECT_NONE) {
                this.postEffect = null;
            } else {
                this.loadEffect(EFFECTS[this.effectIndex]);
            }
        }

    }

    void loadEffect(ResourceLocation pResourceLocation) {
        if (GLX.isUsingFBOs()) {
            if (this.postEffect != null) {
                this.postEffect.close();
            }

            try {
                this.postEffect = new PostChain(this.minecraft.getTextureManager(), this.resourceManager, this.minecraft.getMainRenderTarget(), pResourceLocation);
                this.postEffect.resize(this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
                this.effectActive = true;
            } catch (IOException ioexception) {
                LOGGER.warn("Failed to load shader: {}", pResourceLocation, ioexception);
                this.effectIndex = EFFECT_NONE;
                this.effectActive = false;
            } catch (JsonSyntaxException jsonsyntaxexception) {
                LOGGER.warn("Failed to parse shader: {}", pResourceLocation, jsonsyntaxexception);
                this.effectIndex = EFFECT_NONE;
                this.effectActive = false;
            }

        }
    }

    public PreparableReloadListener createReloadListener() {
        return new SimplePreparableReloadListener<GameRenderer.ResourceCache>() {
            protected GameRenderer.ResourceCache prepare(ResourceManager p_251213_, ProfilerFiller p_251006_) {
                Map<ResourceLocation, Resource> map = p_251213_.listResources("shaders", (locIn) -> {
                    String s = locIn.getPath();
                    return s.endsWith(".json") || s.endsWith(Program.Type.FRAGMENT.getExtension()) || s.endsWith(Program.Type.VERTEX.getExtension()) || s.endsWith(".glsl");
                });
                Map<ResourceLocation, Resource> map1 = new HashMap<>();
                map.forEach((locIn, resIn) -> {
                    try (InputStream inputstream = resIn.open()) {
                        byte[] abyte = inputstream.readAllBytes();
                        map1.put(locIn, new Resource(resIn.source(), () -> {
                            return new ByteArrayInputStream(abyte);
                        }));
                    } catch (Exception exception1) {
                        GameRenderer.LOGGER.warn("Failed to read resource {}", locIn, exception1);
                    }

                });
                return new GameRenderer.ResourceCache(p_251213_, map1);
            }

            protected void apply(GameRenderer.ResourceCache p_251168_, ResourceManager p_248902_, ProfilerFiller p_251909_) {
                GameRenderer.this.reloadShaders(p_251168_);
                if (GameRenderer.this.postEffect != null) {
                    GameRenderer.this.postEffect.close();
                }

                GameRenderer.this.postEffect = null;
                if (GameRenderer.this.effectIndex == GameRenderer.EFFECT_NONE) {
                    GameRenderer.this.checkEntityPostEffect(GameRenderer.this.minecraft.getCameraEntity());
                } else {
                    GameRenderer.this.loadEffect(GameRenderer.EFFECTS[GameRenderer.this.effectIndex]);
                }

            }

            public String getName() {
                return "Shader Loader";
            }
        };
    }

    public void preloadUiShader(ResourceProvider pResourceProvider) {
        if (this.blitShader != null) {
            throw new RuntimeException("Blit shader already preloaded");
        } else {
            try {
                this.blitShader = new ShaderInstance(pResourceProvider, "blit_screen", DefaultVertexFormat.BLIT_SCREEN);
            } catch (IOException ioexception) {
                throw new RuntimeException("could not preload blit shader", ioexception);
            }

            rendertypeGuiShader = this.preloadShader(pResourceProvider, "rendertype_gui", DefaultVertexFormat.POSITION_COLOR);
            rendertypeGuiOverlayShader = this.preloadShader(pResourceProvider, "rendertype_gui_overlay", DefaultVertexFormat.POSITION_COLOR);
            positionShader = this.preloadShader(pResourceProvider, "position", DefaultVertexFormat.POSITION);
            positionColorShader = this.preloadShader(pResourceProvider, "position_color", DefaultVertexFormat.POSITION_COLOR);
            positionColorTexShader = this.preloadShader(pResourceProvider, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX);
            positionTexShader = this.preloadShader(pResourceProvider, "position_tex", DefaultVertexFormat.POSITION_TEX);
            positionTexColorShader = this.preloadShader(pResourceProvider, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR);
            rendertypeTextShader = this.preloadShader(pResourceProvider, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        }
    }

    private ShaderInstance preloadShader(ResourceProvider pResourceProvider, String pName, VertexFormat pFormat) {
        try {
            ShaderInstance shaderinstance = new ShaderInstance(pResourceProvider, pName, pFormat);
            this.shaders.put(pName, shaderinstance);
            return shaderinstance;
        } catch (Exception exception1) {
            throw new IllegalStateException("could not preload shader " + pName, exception1);
        }
    }

    void reloadShaders(ResourceProvider pResourceProvider) {
        RenderSystem.assertOnRenderThread();
        List<Program> list = Lists.newArrayList();
        list.addAll(Program.Type.FRAGMENT.getPrograms().values());
        list.addAll(Program.Type.VERTEX.getPrograms().values());
        list.forEach(Program::close);
        List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list1 = Lists.newArrayListWithCapacity(this.shaders.size());

        try {
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "particle", DefaultVertexFormat.PARTICLE), (p_172713_0_) -> {
                particleShader = p_172713_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position", DefaultVertexFormat.POSITION), (p_172710_0_) -> {
                positionShader = p_172710_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_color", DefaultVertexFormat.POSITION_COLOR), (p_172707_0_) -> {
                positionColorShader = p_172707_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_color_lightmap", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (p_172704_0_) -> {
                positionColorLightmapShader = p_172704_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_color_tex", DefaultVertexFormat.POSITION_COLOR_TEX), (p_172701_0_) -> {
                positionColorTexShader = p_172701_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_color_tex_lightmap", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (p_172698_0_) -> {
                positionColorTexLightmapShader = p_172698_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_tex", DefaultVertexFormat.POSITION_TEX), (p_172695_0_) -> {
                positionTexShader = p_172695_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_tex_color", DefaultVertexFormat.POSITION_TEX_COLOR), (p_172692_0_) -> {
                positionTexColorShader = p_172692_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_tex_color_normal", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL), (p_172689_0_) -> {
                positionTexColorNormalShader = p_172689_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "position_tex_lightmap_color", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR), (p_172686_0_) -> {
                positionTexLightmapColorShader = p_172686_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_solid", DefaultVertexFormat.BLOCK), (p_172683_0_) -> {
                rendertypeSolidShader = p_172683_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_cutout_mipped", DefaultVertexFormat.BLOCK), (p_172680_0_) -> {
                rendertypeCutoutMippedShader = p_172680_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_cutout", DefaultVertexFormat.BLOCK), (p_172677_0_) -> {
                rendertypeCutoutShader = p_172677_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_translucent", DefaultVertexFormat.BLOCK), (p_172674_0_) -> {
                rendertypeTranslucentShader = p_172674_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_translucent_moving_block", DefaultVertexFormat.BLOCK), (p_172671_0_) -> {
                rendertypeTranslucentMovingBlockShader = p_172671_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_translucent_no_crumbling", DefaultVertexFormat.BLOCK), (p_172668_0_) -> {
                rendertypeTranslucentNoCrumblingShader = p_172668_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY), (p_172665_0_) -> {
                rendertypeArmorCutoutNoCullShader = p_172665_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_solid", DefaultVertexFormat.NEW_ENTITY), (p_172662_0_) -> {
                rendertypeEntitySolidShader = p_172662_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_cutout", DefaultVertexFormat.NEW_ENTITY), (p_172659_0_) -> {
                rendertypeEntityCutoutShader = p_172659_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY), (p_172656_0_) -> {
                rendertypeEntityCutoutNoCullShader = p_172656_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY), (p_172653_0_) -> {
                rendertypeEntityCutoutNoCullZOffsetShader = p_172653_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY), (p_172650_0_) -> {
                rendertypeItemEntityTranslucentCullShader = p_172650_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY), (p_172647_0_) -> {
                rendertypeEntityTranslucentCullShader = p_172647_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_translucent", DefaultVertexFormat.NEW_ENTITY), (p_172644_0_) -> {
                rendertypeEntityTranslucentShader = p_172644_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_translucent_emissive", DefaultVertexFormat.NEW_ENTITY), (p_172641_0_) -> {
                rendertypeEntityTranslucentEmissiveShader = p_172641_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY), (p_172638_0_) -> {
                rendertypeEntitySmoothCutoutShader = p_172638_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_beacon_beam", DefaultVertexFormat.BLOCK), (p_172839_0_) -> {
                rendertypeBeaconBeamShader = p_172839_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_decal", DefaultVertexFormat.NEW_ENTITY), (p_172836_0_) -> {
                rendertypeEntityDecalShader = p_172836_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_no_outline", DefaultVertexFormat.NEW_ENTITY), (p_172833_0_) -> {
                rendertypeEntityNoOutlineShader = p_172833_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_shadow", DefaultVertexFormat.NEW_ENTITY), (p_172830_0_) -> {
                rendertypeEntityShadowShader = p_172830_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_alpha", DefaultVertexFormat.NEW_ENTITY), (p_172827_0_) -> {
                rendertypeEntityAlphaShader = p_172827_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_eyes", DefaultVertexFormat.NEW_ENTITY), (p_172824_0_) -> {
                rendertypeEyesShader = p_172824_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_energy_swirl", DefaultVertexFormat.NEW_ENTITY), (p_172821_0_) -> {
                rendertypeEnergySwirlShader = p_172821_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_leash", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (p_172818_0_) -> {
                rendertypeLeashShader = p_172818_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_water_mask", DefaultVertexFormat.POSITION), (p_172815_0_) -> {
                rendertypeWaterMaskShader = p_172815_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_outline", DefaultVertexFormat.POSITION_COLOR_TEX), (p_172812_0_) -> {
                rendertypeOutlineShader = p_172812_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_armor_glint", DefaultVertexFormat.POSITION_TEX), (p_172809_0_) -> {
                rendertypeArmorGlintShader = p_172809_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_armor_entity_glint", DefaultVertexFormat.POSITION_TEX), (p_172806_0_) -> {
                rendertypeArmorEntityGlintShader = p_172806_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_glint_translucent", DefaultVertexFormat.POSITION_TEX), (p_172804_0_) -> {
                rendertypeGlintTranslucentShader = p_172804_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_glint", DefaultVertexFormat.POSITION_TEX), (p_172802_0_) -> {
                rendertypeGlintShader = p_172802_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_glint_direct", DefaultVertexFormat.POSITION_TEX), (p_172800_0_) -> {
                rendertypeGlintDirectShader = p_172800_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_glint", DefaultVertexFormat.POSITION_TEX), (p_172798_0_) -> {
                rendertypeEntityGlintShader = p_172798_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_entity_glint_direct", DefaultVertexFormat.POSITION_TEX), (p_172795_0_) -> {
                rendertypeEntityGlintDirectShader = p_172795_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (p_172793_0_) -> {
                rendertypeTextShader = p_172793_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_text_background", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (p_268793_0_) -> {
                rendertypeTextBackgroundShader = p_268793_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_text_intensity", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (p_172791_0_) -> {
                rendertypeTextIntensityShader = p_172791_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (p_172788_0_) -> {
                rendertypeTextSeeThroughShader = p_172788_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_text_background_see_through", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP), (p_268792_0_) -> {
                rendertypeTextBackgroundSeeThroughShader = p_268792_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_text_intensity_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (p_172786_0_) -> {
                rendertypeTextIntensitySeeThroughShader = p_172786_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_lightning", DefaultVertexFormat.POSITION_COLOR), (p_172784_0_) -> {
                rendertypeLightningShader = p_172784_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_tripwire", DefaultVertexFormat.BLOCK), (p_172781_0_) -> {
                rendertypeTripwireShader = p_172781_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_end_portal", DefaultVertexFormat.POSITION), (p_172777_0_) -> {
                rendertypeEndPortalShader = p_172777_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_end_gateway", DefaultVertexFormat.POSITION), (p_172773_0_) -> {
                rendertypeEndGatewayShader = p_172773_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL), (p_172732_0_) -> {
                rendertypeLinesShader = p_172732_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_crumbling", DefaultVertexFormat.BLOCK), (p_234229_0_) -> {
                rendertypeCrumblingShader = p_234229_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_gui", DefaultVertexFormat.POSITION_COLOR), (p_285677_0_) -> {
                rendertypeGuiShader = p_285677_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_gui_overlay", DefaultVertexFormat.POSITION_COLOR), (p_285675_0_) -> {
                rendertypeGuiOverlayShader = p_285675_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_gui_text_highlight", DefaultVertexFormat.POSITION_COLOR), (p_285674_0_) -> {
                rendertypeGuiTextHighlightShader = p_285674_0_;
            }));
            list1.add(Pair.of(new ShaderInstance(pResourceProvider, "rendertype_gui_ghost_recipe_overlay", DefaultVertexFormat.POSITION_COLOR), (p_285676_0_) -> {
                rendertypeGuiGhostRecipeOverlayShader = p_285676_0_;
            }));
            ReflectorForge.postModLoaderEvent(Reflector.RegisterShadersEvent_Constructor, pResourceProvider, list1);
        } catch (IOException ioexception) {
            list1.forEach((p_172728_0_) -> {
                p_172728_0_.getFirst().close();
            });
            throw new RuntimeException("could not reload shaders", ioexception);
        }

        this.shutdownShaders();
        list1.forEach((p_234224_1_) -> {
            ShaderInstance shaderinstance = p_234224_1_.getFirst();
            this.shaders.put(shaderinstance.getName(), shaderinstance);
            p_234224_1_.getSecond().accept(shaderinstance);
        });
    }

    private void shutdownShaders() {
        RenderSystem.assertOnRenderThread();
        this.shaders.values().forEach(ShaderInstance::close);
        this.shaders.clear();
    }

    @Nullable
    public ShaderInstance getShader(@Nullable String pName) {
        return pName == null ? null : this.shaders.get(pName);
    }

    public void tick() {
        this.tickFov();
        this.lightTexture.tick();
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.mainCamera.tick();
        ++this.tick;
        this.itemInHandRenderer.tick();
        this.minecraft.levelRenderer.tickRain(this.mainCamera);
        this.darkenWorldAmountO = this.darkenWorldAmount;
        if (this.minecraft.gui.getBossOverlay().shouldDarkenScreen()) {
            this.darkenWorldAmount += 0.05F;
            if (this.darkenWorldAmount > 1.0F) {
                this.darkenWorldAmount = 1.0F;
            }
        } else if (this.darkenWorldAmount > 0.0F) {
            this.darkenWorldAmount -= 0.0125F;
        }

        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }

    }

    @Nullable
    public PostChain currentEffect() {
        return this.postEffect;
    }

    public void resize(int pWidth, int pHeight) {
        if (this.postEffect != null) {
            this.postEffect.resize(pWidth, pHeight);
        }

        this.minecraft.levelRenderer.resize(pWidth, pHeight);
    }

    public void pick(float pPartialTicks) {
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null && this.minecraft.level != null) {
            this.minecraft.getProfiler().push("pick");
            this.minecraft.crosshairPickEntity = null;
            double d0 = (double) this.minecraft.gameMode.getPickRange();
            boolean flag = Reflector.IForgePlayer_getEntityReach.exists();
            double d1 = d0;
            double d2 = d0;
            if (flag) {
                d2 = Reflector.callDouble(this.minecraft.player, Reflector.IForgePlayer_getEntityReach);
                d1 = Math.max(d0, d2);
            }

            this.minecraft.hitResult = entity.pick(d1, pPartialTicks, false);
            Vec3 vec3 = entity.getEyePosition(pPartialTicks);
            boolean flag1 = false;
            int i = 3;
            double d3 = d0;
            boolean flag2 = !Reflector.IForgePlayer_getEntityReach.exists();
            if (flag2 && this.minecraft.gameMode.hasFarPickRange()) {
                d3 = 6.0D;
                d0 = d3;
            } else {
                if (d0 > 3.0D) {
                    flag1 = true;
                }

                d0 = d0;
            }

            if (flag) {
                d0 = d3 = Math.max(d0, d2);
            }

            d3 *= d3;
            boolean flag3 = flag ? this.minecraft.hitResult.getType() != HitResult.Type.MISS : true;
            if (this.minecraft.hitResult != null && flag3) {
                d3 = this.minecraft.hitResult.getLocation().distanceToSqr(vec3);
                if (Reflector.IForgePlayer_getBlockReach.exists()) {
                    double d4 = Reflector.callDouble(this.minecraft.player, Reflector.IForgePlayer_getBlockReach);
                    if (d3 > d4 * d4) {
                        Vec3 vec32 = this.minecraft.hitResult.getLocation();
                        this.minecraft.hitResult = BlockHitResult.miss(vec32, Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(vec32));
                    }
                }
            }

            Vec3 vec34 = entity.getViewVector(1.0F);
            Vec3 vec31 = vec3.add(vec34.x * d0, vec34.y * d0, vec34.z * d0);
            float f = 1.0F;
            AABB aabb = entity.getBoundingBox().expandTowards(vec34.scale(d0)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(entity, vec3, vec31, aabb, (entityIn) -> {
                return !entityIn.isSpectator() && entityIn.isPickable();
            }, d3);
            if (entityhitresult != null) {
                label108:
                {
                    Vec3 vec33;
                    label97:
                    {
                        Entity entity1 = entityhitresult.getEntity();
                        vec33 = entityhitresult.getLocation();
                        double d5 = vec3.distanceToSqr(vec33);
                        boolean flag4 = d5 > d3 || d5 > d2 * d2;
                        if (flag) {
                            if (flag4) {
                                break label97;
                            }
                        } else if (flag1 && d5 > 9.0D) {
                            break label97;
                        }

                        if (!(d5 < d3) && this.minecraft.hitResult != null) {
                            break label108;
                        }

                        this.minecraft.hitResult = entityhitresult;
                        if (entity1 instanceof LivingEntity || entity1 instanceof ItemFrame) {
                            this.minecraft.crosshairPickEntity = entity1;
                        }
                        break label108;
                    }

                    this.minecraft.hitResult = BlockHitResult.miss(vec33, Direction.getNearest(vec34.x, vec34.y, vec34.z), BlockPos.containing(vec33));
                }
            }

            this.minecraft.getProfiler().pop();
        }

    }

    private void tickFov() {
        float f = 1.0F;
        if (this.minecraft.getCameraEntity() instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer) this.minecraft.getCameraEntity();
            f = abstractclientplayer.getFieldOfViewModifier();
        }

        this.oldFov = this.fov;
        this.fov += (f - this.fov) * 0.5F;
        if (this.fov > 1.5F) {
            this.fov = 1.5F;
        }

        if (this.fov < 0.1F) {
            this.fov = 0.1F;
        }

    }

    public double getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting) {
        if (this.panoramicMode) {
            return 90.0D;
        } else {
            double d0 = 70.0D;
            if (pUseFOVSetting) {
                d0 = (double) this.minecraft.options.fov().get().intValue();
                boolean flag = this.minecraft.getCameraEntity() instanceof AbstractClientPlayer && ((AbstractClientPlayer) this.minecraft.getCameraEntity()).isScoping();
                if (Config.isDynamicFov() || flag) {
                    d0 *= (double) Mth.lerp(pPartialTicks, this.oldFov, this.fov);
                }
            }

            boolean flag1 = false;
            if (this.minecraft.screen == null) {
                flag1 = this.minecraft.options.ofKeyBindZoom.isDown();
            }

            if (flag1) {
                if (!Config.zoomMode) {
                    Config.zoomMode = true;
                    Config.zoomSmoothCamera = this.minecraft.options.smoothCamera;
                    this.minecraft.options.smoothCamera = true;
                    this.minecraft.levelRenderer.needsUpdate();
                }

                if (Config.zoomMode) {
                    d0 /= 4.0D;
                }
            } else if (Config.zoomMode) {
                Config.zoomMode = false;
                this.minecraft.options.smoothCamera = Config.zoomSmoothCamera;
                this.minecraft.levelRenderer.needsUpdate();
            }

            if (pActiveRenderInfo.getEntity() instanceof LivingEntity && ((LivingEntity) pActiveRenderInfo.getEntity()).isDeadOrDying()) {
                float f = Math.min((float) ((LivingEntity) pActiveRenderInfo.getEntity()).deathTime + pPartialTicks, 20.0F);
                d0 /= (double) ((1.0F - 500.0F / (f + 500.0F)) * 2.0F + 1.0F);
            }

            FogType fogtype = pActiveRenderInfo.getFluidInCamera();
            if (fogtype == FogType.LAVA || fogtype == FogType.WATER) {
                d0 *= Mth.lerp(this.minecraft.options.fovEffectScale().get(), 1.0D, (double) 0.85714287F);
            }

            return Reflector.ForgeHooksClient_getFieldOfView.exists() ? Reflector.callDouble(Reflector.ForgeHooksClient_getFieldOfView, this, pActiveRenderInfo, pPartialTicks, d0, pUseFOVSetting) : d0;
        }
    }

    private void bobHurt(PoseStack pPoseStack, float pPartialTicks) {
        if (this.minecraft.getCameraEntity() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) this.minecraft.getCameraEntity();
            float f = (float) livingentity.hurtTime - pPartialTicks;
            if (livingentity.isDeadOrDying()) {
                float f1 = Math.min((float) livingentity.deathTime + pPartialTicks, 20.0F);
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(40.0F - 8000.0F / (f1 + 200.0F)));
            }

            if (f < 0.0F) {
                return;
            }

            f /= (float) livingentity.hurtDuration;
            f = Mth.sin(f * f * f * f * (float) Math.PI);
            float f3 = livingentity.getHurtDir();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-f3));
            float f2 = (float) ((double) (-f) * 14.0D * this.minecraft.options.damageTiltStrength().get());
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(f2));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(f3));
        }

    }

    private void bobView(PoseStack pPoseStack, float pPartialTicks) {
        if (this.minecraft.getCameraEntity() instanceof Player) {
            Player player = (Player) this.minecraft.getCameraEntity();
            float f = player.walkDist - player.walkDistO;
            float f1 = -(player.walkDist + f * pPartialTicks);
            float f2 = Mth.lerp(pPartialTicks, player.oBob, player.bob);
            pPoseStack.translate(Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F, -Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), 0.0F);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
        }

    }

    public void renderZoomed(float pZoom, float pZoomX, float pZoomY) {
        this.zoom = pZoom;
        this.zoomX = pZoomX;
        this.zoomY = pZoomY;
        this.setRenderBlockOutline(false);
        this.setRenderHand(false);
        this.renderLevel(1.0F, 0L, new PoseStack());
        this.zoom = 1.0F;
    }

    private void renderItemInHand(PoseStack pPoseStack, Camera pActiveRenderInfo, float pPartialTicks) {
        this.renderHand(pPoseStack, pActiveRenderInfo, pPartialTicks, true, true, false);
    }

    public void renderHand(PoseStack matrixStackIn, Camera activeRenderInfoIn, float partialTicks, boolean renderItem, boolean renderOverlay, boolean renderTranslucent) {
        if (!this.panoramicMode) {
            Shaders.beginRenderFirstPersonHand(renderTranslucent);
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(activeRenderInfoIn, partialTicks, false)));
            matrixStackIn.setIdentity();
            boolean flag = false;
            if (renderItem) {
                matrixStackIn.pushPose();
                this.bobHurt(matrixStackIn, partialTicks);
                if (this.minecraft.options.bobView().get()) {
                    this.bobView(matrixStackIn, partialTicks);
                }

                flag = this.minecraft.getCameraEntity() instanceof LivingEntity && ((LivingEntity) this.minecraft.getCameraEntity()).isSleeping();
                if (this.minecraft.options.getCameraType().isFirstPerson() && !flag && !this.minecraft.options.hideGui && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                    this.lightTexture.turnOnLightLayer();
                    if (Config.isShaders()) {
                        ShadersRender.renderItemFP(this.itemInHandRenderer, partialTicks, matrixStackIn, this.renderBuffers.bufferSource(), this.minecraft.player, this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks), renderTranslucent);
                    } else {
                        this.itemInHandRenderer.renderHandsWithItems(partialTicks, matrixStackIn, this.renderBuffers.bufferSource(), this.minecraft.player, this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks));
                    }

                    this.lightTexture.turnOffLightLayer();
                }

                matrixStackIn.popPose();
            }

            Shaders.endRenderFirstPersonHand();
            if (!renderOverlay) {
                return;
            }

            this.lightTexture.turnOffLightLayer();
            if (this.minecraft.options.getCameraType().isFirstPerson() && !flag) {
                ScreenEffectRenderer.renderScreenEffect(this.minecraft, matrixStackIn);
                this.bobHurt(matrixStackIn, partialTicks);
            }

            if (this.minecraft.options.bobView().get()) {
                this.bobView(matrixStackIn, partialTicks);
            }
        }

    }

    public void resetProjectionMatrix(Matrix4f pMatrix) {
        RenderSystem.setProjectionMatrix(pMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
    }

    public Matrix4f getProjectionMatrix(double pFov) {
        PoseStack posestack = new PoseStack();
        posestack.last().pose().identity();
        if (Config.isShaders() && Shaders.isRenderingFirstPersonHand()) {
            Shaders.applyHandDepth(posestack);
        }

        this.clipDistance = this.renderDistance + 1024.0F;
        if (this.zoom != 1.0F) {
            posestack.translate(this.zoomX, -this.zoomY, 0.0F);
            posestack.scale(this.zoom, this.zoom, 1.0F);
        }
        boolean enable = Extazyy.moduleManager.resolution.state;
        posestack.last().pose().mul((new Matrix4f()).setPerspective((float) (pFov * (double) ((float) Math.PI / 180F)),  enable ? Extazyy.moduleManager.resolution.width.getValue().floatValue() /Extazyy.moduleManager.resolution.width2.getValue().floatValue() : (float) this.minecraft.getWindow().getWidth() / (float) this.minecraft.getWindow().getHeight(), 0.05F, this.clipDistance));
        return posestack.last().pose();
    }

    public float getDepthFar() {
        return this.renderDistance * 4.0F;
    }

    public static float getNightVisionScale(LivingEntity pLivingEntity, float pNanoTime) {
        MobEffectInstance mobeffectinstance = pLivingEntity.getEffect(MobEffects.NIGHT_VISION);
        return !mobeffectinstance.endsWithin(200) ? 1.0F : 0.7F + Mth.sin(((float) mobeffectinstance.getDuration() - pNanoTime) * (float) Math.PI * 0.2F) * 0.3F;
    }

    public void render(float pPartialTicks, long pNanoTime, boolean pRenderLevel) {
        this.frameInit();
        renderPartialTicks = pPartialTicks;
        if (!this.minecraft.isWindowActive() && this.minecraft.options.pauseOnLostFocus && (!this.minecraft.options.touchscreen().get() || !this.minecraft.mouseHandler.isRightPressed())) {
            if (Util.getMillis() - this.lastActiveTime > 500L) {
                this.minecraft.pauseGame(false);
            }
        } else {
            this.lastActiveTime = Util.getMillis();
        }

        if (!this.minecraft.noRender) {
            int i = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth() / (double) this.minecraft.getWindow().getScreenWidth());
            int j = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight() / (double) this.minecraft.getWindow().getScreenHeight());

            if (pRenderLevel && this.minecraft.level != null && !Config.isReloadingResources()) {
                this.minecraft.getProfiler().push("level");
                this.renderLevel(pPartialTicks, pNanoTime, new PoseStack());
                this.tryTakeScreenshotIfNeeded();
                this.minecraft.levelRenderer.doEntityOutline();
                if (this.postEffect != null && this.effectActive) {
                    RenderSystem.disableBlend();
                    RenderSystem.disableDepthTest();
                    RenderSystem.resetTextureMatrix();
                    this.postEffect.process(pPartialTicks);
                    RenderSystem.enableTexture();
                }

                this.minecraft.getMainRenderTarget().bindWrite(true);
            } else {

                RenderSystem.viewport(0, 0, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight());
            }

            Window window = this.minecraft.getWindow();
            RenderSystem.clear(256, Minecraft.ON_OSX);
            float f = Reflector.ForgeHooksClient_getGuiFarPlane.exists() ? Reflector.ForgeHooksClient_getGuiFarPlane.callFloat() : 21000.0F;
            Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float) ((double) window.getWidth() / window.getGuiScale()), (float) ((double) window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, f);
            RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
            PoseStack posestack = RenderSystem.getModelViewStack();
            posestack.pushPose();
            posestack.setIdentity();
            float f1 = Reflector.ForgeHooksClient_getGuiFarPlane.exists() ? 1000.0F - f : -11000.0F;
            posestack.translate(0.0F, 0.0F, f1);
            RenderSystem.applyModelViewMatrix();
            Lighting.setupFor3DItems();
            GuiGraphics guigraphics = new GuiGraphics(this.minecraft, this.renderBuffers.bufferSource());
            if (this.lightTexture.isCustom()) {
                this.lightTexture.setAllowed(false);
            }

            if (pRenderLevel && this.minecraft.level != null) {
                this.minecraft.getProfiler().popPush("gui");
                if (this.minecraft.player != null) {
                    float f2 = Mth.lerp(pPartialTicks, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
                    float f3 = this.minecraft.options.screenEffectScale().get().floatValue();
                    if (f2 > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONFUSION) && f3 < 1.0F) {
                        this.renderConfusionOverlay(guigraphics, f2 * (1.0F - f3));
                    }
                }

                if (!this.minecraft.options.hideGui || this.minecraft.screen != null) {
                    this.renderItemActivationAnimation(this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), pPartialTicks);
                    this.minecraft.gui.render(guigraphics, pPartialTicks);
                    if (this.minecraft.options.ofShowFps && !this.minecraft.options.renderDebug) {
                        Config.drawFps(guigraphics);
                    }

                    if (this.minecraft.options.renderDebug) {
                        Lagometer.showLagometer(guigraphics, (int) this.minecraft.getWindow().getGuiScale());
                    }

                    RenderSystem.clear(256, Minecraft.ON_OSX);
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.guiLoadingVisible != (this.minecraft.getOverlay() != null)) {
                if (this.minecraft.getOverlay() != null) {
                    LoadingOverlay.registerTextures(this.minecraft);
                    if (this.minecraft.getOverlay() instanceof LoadingOverlay) {
                        LoadingOverlay loadingoverlay = (LoadingOverlay) this.minecraft.getOverlay();
                        loadingoverlay.update();
                    }
                }

                this.guiLoadingVisible = this.minecraft.getOverlay() != null;
            }

            if (this.minecraft.getOverlay() != null) {
                try {
                    this.minecraft.getOverlay().render(guigraphics, i, j, this.minecraft.getDeltaFrameTime());
                } catch (Throwable throwable1) {
                    CrashReport crashreport2 = CrashReport.forThrowable(throwable1, "Rendering overlay");
                    CrashReportCategory crashreportcategory2 = crashreport2.addCategory("Overlay render details");
                    crashreportcategory2.setDetail("Overlay name", () -> {
                        return this.minecraft.getOverlay().getClass().getCanonicalName();
                    });
                    throw new ReportedException(crashreport2);
                }
            } else if (this.minecraft.screen != null) {
                try {
                    if (Config.isCustomEntityModels()) {
                        CustomEntityModels.onRenderScreen(this.minecraft.screen);
                    }

                    if (Reflector.ForgeHooksClient_drawScreen.exists()) {
                        Reflector.callVoid(Reflector.ForgeHooksClient_drawScreen, this.minecraft.screen, guigraphics, i, j, this.minecraft.getDeltaFrameTime());
                    } else {
                        this.minecraft.screen.renderWithTooltip(guigraphics, i, j, this.minecraft.getDeltaFrameTime());
                    }
                } catch (Throwable throwable2) {
                    CrashReport crashreport = CrashReport.forThrowable(throwable2, "Rendering screen");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Screen render details");
                    crashreportcategory.setDetail("Screen name", () -> {
                        return this.minecraft.screen.getClass().getCanonicalName();
                    });
                    crashreportcategory.setDetail("Mouse location", () -> {
                        return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j, this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos());
                    });
                    crashreportcategory.setDetail("Screen size", () -> {
                        return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight(), this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getWindow().getGuiScale());
                    });
                    throw new ReportedException(crashreport);
                }

                try {
                    if (this.minecraft.screen != null) {
                        this.minecraft.screen.handleDelayedNarration();
                    }
                } catch (Throwable throwable1) {
                    CrashReport crashreport1 = CrashReport.forThrowable(throwable1, "Narrating screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Screen details");
                    crashreportcategory1.setDetail("Screen name", () -> {
                        return this.minecraft.screen.getClass().getCanonicalName();
                    });
                    throw new ReportedException(crashreport1);
                }
            }

            this.minecraft.getProfiler().push("toasts");
            this.minecraft.getToasts().render(guigraphics);
            this.minecraft.getProfiler().pop();
            guigraphics.flush();
            posestack.popPose();
            RenderSystem.applyModelViewMatrix();
            this.lightTexture.setAllowed(true);
        }

        this.frameFinish();
        this.waitForServerThread();
        MemoryMonitor.update();
        Lagometer.updateLagometer();
        if (this.minecraft.options.ofProfiler) {
            this.minecraft.options.renderDebugCharts = true;
        }

    }

    private void tryTakeScreenshotIfNeeded() {
        if (!this.hasWorldScreenshot && this.minecraft.isLocalServer()) {
            long i = Util.getMillis();
            if (i - this.lastScreenshotAttempt >= 1000L) {
                this.lastScreenshotAttempt = i;
                IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
                if (integratedserver != null && !integratedserver.isStopped()) {
                    integratedserver.getWorldScreenshotFile().ifPresent((p_234238_1_) -> {
                        if (Files.isRegularFile(p_234238_1_)) {
                            this.hasWorldScreenshot = true;
                        } else {
                            this.takeAutoScreenshot(p_234238_1_);
                        }

                    });
                }
            }
        }

    }

    private void takeAutoScreenshot(Path pPath) {
        if (this.minecraft.levelRenderer.countRenderedChunks() > 10 && this.minecraft.levelRenderer.hasRenderedAllChunks()) {
            NativeImage nativeimage = Screenshot.takeScreenshot(this.minecraft.getMainRenderTarget());
            Util.ioPool().execute(() -> {
                int i = nativeimage.getWidth();
                int j = nativeimage.getHeight();
                int k = 0;
                int l = 0;
                if (i > j) {
                    k = (i - j) / 2;
                    i = j;
                } else {
                    l = (j - i) / 2;
                    j = i;
                }

                try (NativeImage nativeimage1 = new NativeImage(64, 64, false)) {
                    nativeimage.resizeSubRectTo(k, l, i, j, nativeimage1);
                    nativeimage1.writeToFile(pPath);
                } catch (IOException ioexception1) {
                    LOGGER.warn("Couldn't save auto screenshot", (Throwable) ioexception1);
                } finally {
                    nativeimage.close();
                }

            });
        }

    }

    private boolean shouldRenderBlockOutline() {
        if (!this.renderBlockOutline) {
            return false;
        } else {
            Entity entity = this.minecraft.getCameraEntity();
            boolean flag = entity instanceof Player && !this.minecraft.options.hideGui;
            if (flag && !((Player) entity).getAbilities().mayBuild) {
                ItemStack itemstack = ((LivingEntity) entity).getMainHandItem();
                HitResult hitresult = this.minecraft.hitResult;
                if (hitresult != null && hitresult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockpos = ((BlockHitResult) hitresult).getBlockPos();
                    BlockState blockstate = this.minecraft.level.getBlockState(blockpos);
                    if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
                        flag = blockstate.getMenuProvider(this.minecraft.level, blockpos) != null;
                    } else {
                        BlockInWorld blockinworld = new BlockInWorld(this.minecraft.level, blockpos, false);
                        Registry<Block> registry = this.minecraft.level.registryAccess().registryOrThrow(Registries.BLOCK);
                        flag = !itemstack.isEmpty() && (itemstack.hasAdventureModeBreakTagForBlock(registry, blockinworld) || itemstack.hasAdventureModePlaceTagForBlock(registry, blockinworld));
                    }
                }
            }

            return flag;
        }
    }

    private final RenderEvent3D renderEvent3D = new RenderEvent3D();

    public void renderLevel(float pPartialTicks, long pFinishTimeNano, PoseStack pPoseStack) {
        this.lightTexture.updateLightTexture(pPartialTicks);
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }

        this.pick(pPartialTicks);
        if (Config.isShaders()) {
            Shaders.beginRender(this.minecraft, this.mainCamera, pPartialTicks, pFinishTimeNano);
        }

        this.minecraft.getProfiler().push("center");
        boolean flag = Config.isShaders();
        if (flag) {
            Shaders.beginRenderPass(pPartialTicks, pFinishTimeNano);
        }

        boolean flag1 = this.shouldRenderBlockOutline();
        this.minecraft.getProfiler().popPush("camera");
        Camera camera = this.mainCamera;
        this.renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);
        PoseStack posestack = new PoseStack();
        double d0 = this.getFov(camera, pPartialTicks, true);
        posestack.mulPoseMatrix(this.getProjectionMatrix(d0));
        PoseStack posestack1 = posestack;
        if (Shaders.isEffectsModelView()) {
            posestack = pPoseStack;
        }

        this.bobHurt(posestack, pPartialTicks);
        if (this.minecraft.options.bobView().get()) {
            this.bobView(posestack, pPartialTicks);
        }

        float f = this.minecraft.options.screenEffectScale().get().floatValue();
        float f1 = Mth.lerp(pPartialTicks, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity) * f * f;
        if (f1 > 0.0F) {
            int i = this.minecraft.player.hasEffect(MobEffects.CONFUSION) ? 7 : 20;
            float f2 = 5.0F / (f1 * f1 + 5.0F) - f1 * 0.04F;
            f2 *= f2;
            Axis axis = Axis.of(new Vector3f(0.0F, Mth.SQRT_OF_TWO / 2.0F, Mth.SQRT_OF_TWO / 2.0F));
            posestack.mulPose(axis.rotationDegrees(((float) this.tick + pPartialTicks) * (float) i));
            posestack.scale(1.0F / f2, 1.0F, 1.0F);
            float f3 = -((float) this.tick + pPartialTicks) * (float) i;
            posestack.mulPose(axis.rotationDegrees(f3));
        }

        if (Shaders.isEffectsModelView()) {
            posestack = posestack1;
        }

        Matrix4f matrix4f = posestack.last().pose();
        this.resetProjectionMatrix(matrix4f);
        camera.setup(this.minecraft.level, (Entity) (this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity()), !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), pPartialTicks);
        if (Reflector.ForgeHooksClient_onCameraSetup.exists()) {
            Object object = Reflector.ForgeHooksClient_onCameraSetup.call(this, camera, pPartialTicks);
            float f5 = Reflector.callFloat(object, Reflector.ViewportEvent_ComputeCameraAngles_getYaw);
            float f6 = Reflector.callFloat(object, Reflector.ViewportEvent_ComputeCameraAngles_getPitch);
            float f4 = Reflector.callFloat(object, Reflector.ViewportEvent_ComputeCameraAngles_getRoll);
            camera.setAnglesInternal(f5, f6);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(f4));
        }

        pPoseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Matrix3f matrix3f = (new Matrix3f(pPoseStack.last().normal())).invert();
        RenderSystem.setInverseViewRotationMatrix(matrix3f);
        this.minecraft.levelRenderer.prepareCullFrustum(pPoseStack, camera.getPosition(), this.getProjectionMatrix(Math.max(d0, (double) this.minecraft.options.fov().get().intValue())));
        this.minecraft.levelRenderer.renderLevel(pPoseStack, pPartialTicks, pFinishTimeNano, flag1, camera, this, this.lightTexture, matrix4f);



        this.minecraft.getProfiler().popPush("forge_render_last");
        if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
            ReflectorForge.dispatchRenderStageS(Reflector.RenderLevelStageEvent_Stage_AFTER_LEVEL, this.minecraft.levelRenderer, posestack, matrix4f, this.minecraft.levelRenderer.getTicks(), camera, this.minecraft.levelRenderer.getFrustum());
        }


        ESPUtil.setProjectionViewMatrix(getProjectionMatrix(d0), pPoseStack.last().pose());
        ///// render 3D
        renderEvent3D.setPoseStack(pPoseStack);
        renderEvent3D.setPartialTicks(pPartialTicks);
        EventManager.call(renderEvent3D);
        /////

        this.minecraft.getProfiler().popPush("hand");
        if (this.renderHand && !Shaders.isShadowPass) {
            if (flag) {
                ShadersRender.renderHand1(this, pPoseStack, camera, pPartialTicks);
                Shaders.renderCompositeFinal();
            }

            RenderSystem.clear(256, Minecraft.ON_OSX);
            if (flag) {
                ShadersRender.renderFPOverlay(this, pPoseStack, camera, pPartialTicks);
            } else {
                this.renderItemInHand(pPoseStack, camera, pPartialTicks);
            }
        }

        if (flag) {
            Shaders.endRender();
        }

        this.minecraft.getProfiler().pop();
    }

    public void resetData() {
        this.itemActivationItem = null;
        this.mapRenderer.resetData();
        this.mainCamera.reset();
        this.hasWorldScreenshot = false;
    }

    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    private void waitForServerThread() {
        this.serverWaitTimeCurrent = 0;
        if (Config.isSmoothWorld() && Config.isSingleProcessor()) {
            if (this.minecraft.isLocalServer()) {
                IntegratedServer integratedserver = this.minecraft.getSingleplayerServer();
                if (integratedserver != null) {
                    boolean flag = this.minecraft.isPaused();
                    if (!flag && !(this.minecraft.screen instanceof ReceivingLevelScreen)) {
                        if (this.serverWaitTime > 0) {
                            Lagometer.timerServer.start();
                            Config.sleep((long) this.serverWaitTime);
                            Lagometer.timerServer.end();
                            this.serverWaitTimeCurrent = this.serverWaitTime;
                        }

                        long i = System.nanoTime() / 1000000L;
                        if (this.lastServerTime != 0L && this.lastServerTicks != 0) {
                            long j = i - this.lastServerTime;
                            if (j < 0L) {
                                this.lastServerTime = i;
                                j = 0L;
                            }

                            if (j >= 50L) {
                                this.lastServerTime = i;
                                int k = integratedserver.getTickCount();
                                int l = k - this.lastServerTicks;
                                if (l < 0) {
                                    this.lastServerTicks = k;
                                    l = 0;
                                }

                                if (l < 1 && this.serverWaitTime < 100) {
                                    this.serverWaitTime += 2;
                                }

                                if (l > 1 && this.serverWaitTime > 0) {
                                    --this.serverWaitTime;
                                }

                                this.lastServerTicks = k;
                            }
                        } else {
                            this.lastServerTime = i;
                            this.lastServerTicks = integratedserver.getTickCount();
                            this.avgServerTickDiff = 1.0F;
                            this.avgServerTimeDiff = 50.0F;
                        }
                    } else {
                        if (this.minecraft.screen instanceof ReceivingLevelScreen) {
                            Config.sleep(20L);
                        }

                        this.lastServerTime = 0L;
                        this.lastServerTicks = 0;
                    }
                }
            }
        } else {
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
        }
    }

    private void frameInit() {
        Config.frameStart();
        GlErrors.frameStart();
        if (!this.initialized) {
            ReflectorResolver.resolve();
            if (Config.getBitsOs() == 64 && Config.getBitsJre() == 32) {
                Config.setNotify64BitJava(true);
            }

            this.initialized = true;
        }

        Level level = this.minecraft.level;
        if (level != null) {
            if (Config.getNewRelease() != null) {
                String s = "HD_U".replace("HD_U", "HD Ultra").replace("L", "Light");
                String s1 = s + " " + Config.getNewRelease();
                MutableComponent mutablecomponent = Component.literal(I18n.get("of.message.newVersion", "\u00a7n" + s1 + "\u00a7r"));
                mutablecomponent.setStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://optifine.net/downloads")));
                this.minecraft.gui.getChat().addMessage(mutablecomponent);
                Config.setNewRelease((String) null);
            }

            if (Config.isNotify64BitJava()) {
                Config.setNotify64BitJava(false);
                Component component = Component.literal(I18n.get("of.message.java64Bit"));
                this.minecraft.gui.getChat().addMessage(component);
            }
        }

        if (this.updatedWorld != level) {
            RandomEntities.worldChanged(this.updatedWorld, level);
            Config.updateThreadPriorities();
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
            assert level != null;
            this.updatedWorld = level;
        }

        if (!this.setFxaaShader(Shaders.configAntialiasingLevel)) {
            Shaders.configAntialiasingLevel = 0;
        }

        if (this.minecraft.screen != null && this.minecraft.screen.getClass() == ChatScreen.class) {
            this.minecraft.setScreen(new GuiChatOF((ChatScreen) this.minecraft.screen));
        }

    }

    private void frameFinish() {
        if (this.minecraft.level != null && Config.isShowGlErrors() && TimedEvent.isActive("CheckGlErrorFrameFinish", 10000L)) {
            int i = GlStateManager._getError();
            if (i != 0 && GlErrors.isEnabled(i)) {
                String s = Config.getGlErrorString(i);
                Component component = Component.literal(I18n.get("of.message.openglError", i, s));
                this.minecraft.gui.getChat().addMessage(component);
            }
        }

    }

    public void setScaleRender(int scale) {
        if (true)return;
        GLFW.glfwMakeContextCurrent(this.minecraft.getWindow().getWindow());
        GL.createCapabilities();
        Window mw = Minecraft.getInstance().getWindow();
        mw.setGuiScale(scale);

        int width = mw.getGuiScaledWidth();
        int height = mw.getGuiScaledHeight();

        GL11.glViewport(0, 0, width, height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager._viewport(0, 0, width, height);
    }

    public void setScaleRenderStandard() {
        setScaleRender(minecraft.options.guiScale().get());
    }

    public boolean setFxaaShader(int fxaaLevel) {
        if (!GLX.isUsingFBOs()) {
            return false;
        } else if (this.postEffect != null && this.postEffect != this.fxaaShaders[2] && this.postEffect != this.fxaaShaders[4]) {
            return true;
        } else if (fxaaLevel != 2 && fxaaLevel != 4) {
            if (this.postEffect == null) {
                return true;
            } else {
                this.postEffect.close();
                this.postEffect = null;
                return true;
            }
        } else if (this.postEffect != null && this.postEffect == this.fxaaShaders[fxaaLevel]) {
            return true;
        } else if (this.minecraft.level == null) {
            return true;
        } else {
            this.loadEffect(new ResourceLocation("shaders/post/fxaa_of_" + fxaaLevel + "x.json"));
            this.fxaaShaders[fxaaLevel] = this.postEffect;
            return this.effectActive;
        }
    }

    public static float getRenderPartialTicks() {
        return renderPartialTicks;
    }

    public void displayItemActivation(ItemStack pStack) {
        this.itemActivationItem = pStack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = this.random.nextFloat() * 2.0F - 1.0F;
        this.itemActivationOffY = this.random.nextFloat() * 2.0F - 1.0F;
    }

    private void renderItemActivationAnimation(int pWidthsp, int pHeightScaled, float pPartialTicks) {
        if (this.itemActivationItem != null && this.itemActivationTicks > 0) {
            int i = 40 - this.itemActivationTicks;
            float f = ((float) i + pPartialTicks) / 40.0F;
            float f1 = f * f;
            float f2 = f * f1;
            float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
            float f4 = f3 * (float) Math.PI;
            float f5 = this.itemActivationOffX * (float) (pWidthsp / 4);
            float f6 = this.itemActivationOffY * (float) (pHeightScaled / 4);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            PoseStack posestack = new PoseStack();
            posestack.pushPose();
            posestack.translate((float) (pWidthsp / 2) + f5 * Mth.abs(Mth.sin(f4 * 2.0F)), (float) (pHeightScaled / 2) + f6 * Mth.abs(Mth.sin(f4 * 2.0F)), -50.0F);
            float f7 = 50.0F + 175.0F * Mth.sin(f4);
            posestack.scale(f7, -f7, f7);
            posestack.mulPose(Axis.YP.rotationDegrees(900.0F * Mth.abs(Mth.sin(f4))));
            posestack.mulPose(Axis.XP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            posestack.mulPose(Axis.ZP.rotationDegrees(6.0F * Mth.cos(f * 8.0F)));
            MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
            this.minecraft.getItemRenderer().renderStatic(this.itemActivationItem, ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, posestack, multibuffersource$buffersource, this.minecraft.level, 0);
            posestack.popPose();
            multibuffersource$buffersource.endBatch();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();
        }

    }

    private void renderConfusionOverlay(GuiGraphics pGuiGraphics, float pScalar) {
        int i = pGuiGraphics.guiWidth();
        int j = pGuiGraphics.guiHeight();
        pGuiGraphics.pose().pushPose();
        float f = Mth.lerp(pScalar, 2.0F, 1.0F);
        pGuiGraphics.pose().translate((float) i / 2.0F, (float) j / 2.0F, 0.0F);
        pGuiGraphics.pose().scale(f, f, f);
        pGuiGraphics.pose().translate((float) (-i) / 2.0F, (float) (-j) / 2.0F, 0.0F);
        float f1 = 0.2F * pScalar;
        float f2 = 0.4F * pScalar;
        float f3 = 0.2F * pScalar;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        pGuiGraphics.setColor(f1, f2, f3, 1.0F);
        pGuiGraphics.blit(NAUSEA_LOCATION, 0, 0, -90, 0.0F, 0.0F, i, j, i, j);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pGuiGraphics.pose().popPose();
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public float getDarkenWorldAmount(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, this.darkenWorldAmountO, this.darkenWorldAmount);
    }

    public float getRenderDistance() {
        return this.renderDistance;
    }

    public Camera getMainCamera() {
        return this.mainCamera;
    }

    public LightTexture lightTexture() {
        return this.lightTexture;
    }

    public OverlayTexture overlayTexture() {
        return this.overlayTexture;
    }

    @Nullable
    public static ShaderInstance getPositionShader() {
        return positionShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorShader() {
        return positionColorShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorTexShader() {
        return positionColorTexShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexShader() {
        return positionTexShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexColorShader() {
        return positionTexColorShader;
    }

    @Nullable
    public static ShaderInstance getParticleShader() {
        return particleShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorLightmapShader() {
        return positionColorLightmapShader;
    }

    @Nullable
    public static ShaderInstance getPositionColorTexLightmapShader() {
        return positionColorTexLightmapShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexColorNormalShader() {
        return positionTexColorNormalShader;
    }

    @Nullable
    public static ShaderInstance getPositionTexLightmapColorShader() {
        return positionTexLightmapColorShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeSolidShader() {
        return rendertypeSolidShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeCutoutMippedShader() {
        return rendertypeCutoutMippedShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeCutoutShader() {
        return rendertypeCutoutShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTranslucentShader() {
        return rendertypeTranslucentShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTranslucentMovingBlockShader() {
        return rendertypeTranslucentMovingBlockShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTranslucentNoCrumblingShader() {
        return rendertypeTranslucentNoCrumblingShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeArmorCutoutNoCullShader() {
        return rendertypeArmorCutoutNoCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntitySolidShader() {
        return rendertypeEntitySolidShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityCutoutShader() {
        return rendertypeEntityCutoutShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityCutoutNoCullShader() {
        return rendertypeEntityCutoutNoCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityCutoutNoCullZOffsetShader() {
        return rendertypeEntityCutoutNoCullZOffsetShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeItemEntityTranslucentCullShader() {
        return rendertypeItemEntityTranslucentCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityTranslucentCullShader() {
        return rendertypeEntityTranslucentCullShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityTranslucentShader() {
        return rendertypeEntityTranslucentShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityTranslucentEmissiveShader() {
        return rendertypeEntityTranslucentEmissiveShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntitySmoothCutoutShader() {
        return rendertypeEntitySmoothCutoutShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeBeaconBeamShader() {
        return rendertypeBeaconBeamShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityDecalShader() {
        return rendertypeEntityDecalShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityNoOutlineShader() {
        return rendertypeEntityNoOutlineShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityShadowShader() {
        return rendertypeEntityShadowShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityAlphaShader() {
        return rendertypeEntityAlphaShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEyesShader() {
        return rendertypeEyesShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEnergySwirlShader() {
        return rendertypeEnergySwirlShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeLeashShader() {
        return rendertypeLeashShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeWaterMaskShader() {
        return rendertypeWaterMaskShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeOutlineShader() {
        return rendertypeOutlineShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeArmorGlintShader() {
        return rendertypeArmorGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeArmorEntityGlintShader() {
        return rendertypeArmorEntityGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGlintTranslucentShader() {
        return rendertypeGlintTranslucentShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGlintShader() {
        return rendertypeGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGlintDirectShader() {
        return rendertypeGlintDirectShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityGlintShader() {
        return rendertypeEntityGlintShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEntityGlintDirectShader() {
        return rendertypeEntityGlintDirectShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextShader() {
        return rendertypeTextShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextBackgroundShader() {
        return rendertypeTextBackgroundShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextIntensityShader() {
        return rendertypeTextIntensityShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextSeeThroughShader() {
        return rendertypeTextSeeThroughShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextBackgroundSeeThroughShader() {
        return rendertypeTextBackgroundSeeThroughShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTextIntensitySeeThroughShader() {
        return rendertypeTextIntensitySeeThroughShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeLightningShader() {
        return rendertypeLightningShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeTripwireShader() {
        return rendertypeTripwireShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEndPortalShader() {
        return rendertypeEndPortalShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeEndGatewayShader() {
        return rendertypeEndGatewayShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeLinesShader() {
        return rendertypeLinesShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeCrumblingShader() {
        return rendertypeCrumblingShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGuiShader() {
        return rendertypeGuiShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGuiOverlayShader() {
        return rendertypeGuiOverlayShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGuiTextHighlightShader() {
        return rendertypeGuiTextHighlightShader;
    }

    @Nullable
    public static ShaderInstance getRendertypeGuiGhostRecipeOverlayShader() {
        return rendertypeGuiGhostRecipeOverlayShader;
    }

    public static record ResourceCache(ResourceProvider original,
                                       Map<ResourceLocation, Resource> cache) implements ResourceProvider {

        public Optional<Resource> getResource(ResourceLocation p_251007_) {
            Resource resource = this.cache.get(p_251007_);
            return resource != null ? Optional.of(resource) : this.original.getResource(p_251007_);
        }

        public ResourceProvider original() {
            return this.original;
        }

        public Map<ResourceLocation, Resource> cache() {
            return this.cache;
        }
    }
}
