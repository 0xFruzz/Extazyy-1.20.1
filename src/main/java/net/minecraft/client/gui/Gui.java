package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ru.fruzz.extazyy.Extazyy;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.RenderEvent2D;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.CustomItems;
import net.optifine.TextureAnimations;
import net.optifine.reflect.Reflector;
import ru.fruzz.extazyy.misc.util.Mine;
import ru.fruzz.extazyy.misc.util.color.ColorUtil;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

public class Gui {
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
    private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
    private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
    private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
    private static final int COLOR_WHITE = 16777215;
    private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
    private static final int NUM_HEARTS_PER_ROW = 10;
    private static final int LINE_HEIGHT = 10;
    private static final String SPACER = ": ";
    private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SEPARATION = 8;
    private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    private final ItemRenderer itemRenderer;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
    private boolean chatDisabledByPlayerShown;
    public float vignetteBrightness = 1.0F;
    private int toolHighlightTimer;
    private ItemStack lastToolHighlight = ItemStack.EMPTY;
    private final DebugScreenOverlay debugScreen;
    private final SubtitleOverlay subtitleOverlay;
    private final SpectatorGui spectatorGui;
    private final PlayerTabOverlay tabList;
    private final BossHealthOverlay bossOverlay;
    private int titleTime;
    @Nullable
    private Component title;
    @Nullable
    private Component subtitle;
    private int titleFadeInTime;
    private int titleStayTime;
    private int titleFadeOutTime;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private int screenWidth;
    private int screenHeight;
    private float autosaveIndicatorValue;
    private float lastAutosaveIndicatorValue;
    private float scopeScale;

    public Gui(Minecraft pMinecraft, ItemRenderer pItemRenderer) {
        this.minecraft = pMinecraft;
        this.itemRenderer = pItemRenderer;
        this.debugScreen = new DebugScreenOverlay(pMinecraft);
        this.spectatorGui = new SpectatorGui(pMinecraft);
        this.chat = new ChatComponent(pMinecraft);
        this.tabList = new PlayerTabOverlay(pMinecraft, this);
        this.bossOverlay = new BossHealthOverlay(pMinecraft);
        this.subtitleOverlay = new SubtitleOverlay(pMinecraft);
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    private final RenderEvent2D renderEvent2D = new RenderEvent2D();

    public void render(GuiGraphics pGuiGraphics, float pPartialTick) {

        Window window = this.minecraft.getWindow();
        this.screenWidth = pGuiGraphics.guiWidth();
        this.screenHeight = pGuiGraphics.guiHeight() - 2;
        Font font = this.getFont();
        RenderSystem.enableBlend();
        if (Config.isVignetteEnabled()) {
            this.renderVignette(pGuiGraphics, this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
        }

        float f = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5F * f, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(pGuiGraphics, this.scopeScale);
            } else {
                this.scopeScale = 0.5F;
                ItemStack itemstack = this.minecraft.player.getInventory().getArmor(3);
                if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(pGuiGraphics, PUMPKIN_BLUR_LOCATION, 1.0F);
                }
            }
        }

        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(pGuiGraphics, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }

        float f1 = Mth.lerp(pPartialTick, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
        if (f1 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(pGuiGraphics, f1);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(pGuiGraphics);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(pPartialTick, pGuiGraphics);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.enableBlend();
            this.renderCrosshair(pGuiGraphics);
            GlStateManager.enableAlphaTest();
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(pGuiGraphics);
            this.minecraft.getProfiler().pop();
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(pGuiGraphics);
            }

            this.renderVehicleHealth(pGuiGraphics);
            RenderSystem.disableBlend();
            int i = this.screenWidth / 2 - 91;
            PlayerRideableJumping playerrideablejumping = this.minecraft.player.jumpableVehicle();
            if (playerrideablejumping != null) {
                this.renderJumpMeter(playerrideablejumping, pGuiGraphics, i);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(pGuiGraphics, i);
            }

            if (this.minecraft.options.ofHeldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(pGuiGraphics);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(pGuiGraphics);
            }
        }

        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            float f2 = (float) this.minecraft.player.getSleepTimer();
            float f5 = f2 / 100.0F;
            if (f5 > 1.0F) {
                f5 = 1.0F - (f2 - 100.0F) / 10.0F;
            }

            int j = (int) (220.0F * f5) << 24 | 1052704;
            pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, this.screenHeight, j);
            this.minecraft.getProfiler().pop();
        }

        if (this.minecraft.isDemo()) {
            this.renderDemoOverlay(pGuiGraphics);
        }

        ///minecraft.gameRenderer.setScaleRender(2);
        renderEvent2D.setPoseStack(pGuiGraphics);
        renderEvent2D.setPartialTicks(pPartialTick);
        EventManager.call(renderEvent2D);


        ///minecraft.gameRenderer.setScaleRenderStandard();

        this.renderEffects(pGuiGraphics);
        if (this.minecraft.options.renderDebug) {//F3 SCREEN
            this.debugScreen.render(pGuiGraphics);
        }

        if (!this.minecraft.options.hideGui) {
            if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
                this.minecraft.getProfiler().push("overlayMessage");
                float f3 = (float) this.overlayMessageTime - pPartialTick;
                int j1 = (int) (f3 * 255.0F / 20.0F);
                if (j1 > 255) {
                    j1 = 255;
                }

                if (j1 > 8) {
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate((float) (this.screenWidth / 2), (float) (this.screenHeight - 68), 0.0F);
                    int l1 = 16777215;
                    if (this.animateOverlayMessageColor) {
                        l1 = Mth.hsvToRgb(f3 / 50.0F, 0.7F, 0.6F) & 16777215;
                    }

                    int k = j1 << 24 & -16777216;
                    int l = font.width(this.overlayMessageString);
                    this.drawBackdrop(pGuiGraphics, font, -4, l, 16777215 | k);
                    pGuiGraphics.drawString(font, this.overlayMessageString, -l / 2, -4, l1 | k);
                    pGuiGraphics.pose().popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                float f4 = (float) this.titleTime - pPartialTick;
                int k1 = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float f6 = (float) (this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f4;
                    k1 = (int) (f6 * 255.0F / (float) this.titleFadeInTime);
                }

                if (this.titleTime <= this.titleFadeOutTime) {
                    k1 = (int) (f4 * 255.0F / (float) this.titleFadeOutTime);
                }

                k1 = Mth.clamp(k1, 0, 255);
                if (k1 > 8) {
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().translate((float) (this.screenWidth / 2), (float) (this.screenHeight / 2), 0.0F);
                    RenderSystem.enableBlend();
                    pGuiGraphics.pose().pushPose();
                    pGuiGraphics.pose().scale(4.0F, 4.0F, 4.0F);
                    int i2 = k1 << 24 & -16777216;
                    int j2 = font.width(this.title);
                    this.drawBackdrop(pGuiGraphics, font, -10, j2, 16777215 | i2);
                    pGuiGraphics.drawString(font, this.title, -j2 / 2, -10, 16777215 | i2);
                    pGuiGraphics.pose().popPose();
                    if (this.subtitle != null) {
                        pGuiGraphics.pose().pushPose();
                        pGuiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
                        int l2 = font.width(this.subtitle);
                        this.drawBackdrop(pGuiGraphics, font, 5, l2, 16777215 | i2);
                        pGuiGraphics.drawString(font, this.subtitle, -l2 / 2, 5, 16777215 | i2);
                        pGuiGraphics.pose().popPose();
                    }

                    RenderSystem.disableBlend();
                    pGuiGraphics.pose().popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            this.subtitleOverlay.render(pGuiGraphics);
            Scoreboard scoreboard = this.minecraft.level.getScoreboard();
            Objective objective = null;
            PlayerTeam playerteam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
            if (playerteam != null) {
                int k2 = playerteam.getColor().getId();
                if (k2 >= 0) {
                    objective = scoreboard.getDisplayObjective(3 + k2);
                }
            }

            Objective objective1 = objective != null ? objective : scoreboard.getDisplayObjective(1);
            if (objective1 != null) {
             //   this.displayScoreboardSidebar(pGuiGraphics, objective1);
                if(Extazyy.getModuleManager().hud.state) {
                    Extazyy.getModuleManager().hud.displayScoreboardSidebar(pGuiGraphics, objective1);
                }
            }

            RenderSystem.enableBlend();
            int i3 = Mth.floor(this.minecraft.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
            int i1 = Mth.floor(this.minecraft.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
            this.minecraft.getProfiler().push("chat");
            this.chat.render(pGuiGraphics, this.tickCount, i3, i1);
            this.minecraft.getProfiler().pop();
            objective1 = scoreboard.getDisplayObjective(0);
            if (this.minecraft.options.keyPlayerList.isDown() && (!this.minecraft.isLocalServer() || this.minecraft.player.connection.getListedOnlinePlayers().size() > 1 || objective1 != null)) {
                this.tabList.setVisible(true);
                this.tabList.render(pGuiGraphics, this.screenWidth, scoreboard, objective1);
            } else {
                this.tabList.setVisible(false);
            }

            this.renderSavingIndicator(pGuiGraphics);
        }

    }

    private void drawBackdrop(GuiGraphics pGuiGraphics, Font pFont, int pYPosition, int pWidth, int pHeight) {
        int i = this.minecraft.options.getBackgroundColor(0.0F);
        if (i != 0) {
            int j = -pWidth / 2;
            pGuiGraphics.fill(j - 2, pYPosition - 2, j + pWidth + 2, pYPosition + 9 + 2, FastColor.ARGB32.multiply(i, pHeight));
        }

    }

    private void renderCrosshair(GuiGraphics pGuiGraphics) {
        Options options = this.minecraft.options;
        if (options.getCameraType().isFirstPerson() && (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult))) {
            if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
                Camera camera = this.minecraft.gameRenderer.getMainCamera();
                PoseStack posestack = RenderSystem.getModelViewStack();
                posestack.pushPose();
                posestack.mulPoseMatrix(pGuiGraphics.pose().last().pose());
                posestack.translate((float) (this.screenWidth / 2), (float) (this.screenHeight / 2), 0.0F);
                posestack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
                posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
                posestack.scale(-1.0F, -1.0F, -1.0F);
                RenderSystem.applyModelViewMatrix();
                RenderSystem.renderCrosshair(10);
                posestack.popPose();
                RenderSystem.applyModelViewMatrix();
            } else {
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                int i = 15;
                pGuiGraphics.blit(GUI_ICONS_LOCATION, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
                if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                    float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                    boolean flag = false;
                    if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
                        flag = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                        flag &= this.minecraft.crosshairPickEntity.isAlive();
                    }

                    int j = this.screenHeight / 2 - 7 + 16;
                    int k = this.screenWidth / 2 - 8;
                    if (flag) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, k, j, 68, 94, 16, 16);
                    } else if (f < 1.0F) {
                        int l = (int) (f * 17.0F);
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 94, 16, 4);
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 94, l, 4);
                    }
                }

                RenderSystem.defaultBlendFunc();
            }
        }

    }

    private boolean canRenderCrosshairForSpectator(HitResult pRayTrace) {
        if (pRayTrace == null) {
            return false;
        } else if (pRayTrace.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) pRayTrace).getEntity() instanceof MenuProvider;
        } else if (pRayTrace.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult) pRayTrace).getBlockPos();
            Level level = this.minecraft.level;
            return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
        } else {
            return false;
        }
    }

    protected void renderEffects(GuiGraphics pGuiGraphics) {
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            Screen screen = this.minecraft.screen;
            if (screen instanceof EffectRenderingInventoryScreen) {
                EffectRenderingInventoryScreen effectrenderinginventoryscreen = (EffectRenderingInventoryScreen) screen;
                if (effectrenderinginventoryscreen.canSeeEffects()) {
                    return;
                }
            }

            RenderSystem.enableBlend();
            int k1 = 0;
            int i = 0;
            MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());

            for (MobEffectInstance mobeffectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
                MobEffect mobeffect = mobeffectinstance.getEffect();
                IClientMobEffectExtensions iclientmobeffectextensions = IClientMobEffectExtensions.of(mobeffectinstance);
                if ((iclientmobeffectextensions == null || iclientmobeffectextensions.isVisibleInGui(mobeffectinstance)) && mobeffectinstance.showIcon()) {
                    int j = this.screenWidth;
                    int k = 1;
                    if (this.minecraft.isDemo()) {
                        k += 15;
                    }

                    if (mobeffect.isBeneficial()) {
                        ++k1;
                        j -= 25 * k1;
                    } else {
                        ++i;
                        j -= 25 * i;
                        k += 26;
                    }

                    float f = 1.0F;
                    if (mobeffectinstance.isAmbient()) {
                        pGuiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, j, k, 165, 166, 24, 24);
                    } else {
                        pGuiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, j, k, 141, 166, 24, 24);
                        if (mobeffectinstance.endsWithin(200)) {
                            int l = mobeffectinstance.getDuration();
                            int i1 = 10 - l / 20;
                            f = Mth.clamp((float) l / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + Mth.cos((float) l * (float) Math.PI / 5.0F) * Mth.clamp((float) i1 / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    if (iclientmobeffectextensions == null || !iclientmobeffectextensions.renderGuiIcon(mobeffectinstance, this, pGuiGraphics, j, k, 0.0F, f)) {
                        TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
                        float f1 = f;
                        int j1 = j;
                        int finalK = k;
                        list.add(() -> {
                            pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, f1);
                            pGuiGraphics.blit(j1 + 3, finalK + 3, 0, 18, 18, textureatlassprite);
                            pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                        });
                    }
                }
            }

            list.forEach(Runnable::run);
        }

    }

    private void renderHotbar(float pPartialTick, GuiGraphics pGuiGraphics) {
        Player player = this.getCameraPlayer();
        if (player != null) {
            ItemStack itemstack = player.getOffhandItem();
            HumanoidArm humanoidarm = player.getMainArm().getOpposite();
            int i = this.screenWidth / 2;
            int j = 182;
            int k = 91;
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
           // pGuiGraphics.blit(WIDGETS_LOCATION, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
           // pGuiGraphics.blit(WIDGETS_LOCATION, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
            DrawHelper.rectangle(pGuiGraphics.pose(), i - 91f, this.screenHeight - 21, 182f, 20, 3, new Color(30,30,30,100).getRGB());
            DrawHelper.rectangle(pGuiGraphics.pose(), i-88.5f + player.getInventory().selected * 20, this.screenHeight - 20.5f, 16, 1, 1, ColorUtil.getColorStyle(0));
                // DrawHelper.rectRGB(pGuiGraphics.pose(), i - 90f  + player.getInventory().selected * 20, this.screenHeight - 20,20,18, 2, Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed(0),  Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed(0),  Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed(0), Extazyy.themesUtil.getCurrentStyle().getColorLowSpeed(0));
            if (!itemstack.isEmpty()) {
                if (humanoidarm == HumanoidArm.LEFT) {
                  //  pGuiGraphics.blit(WIDGETS_LOCATION, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
                    DrawHelper.rectangle(pGuiGraphics.pose(), i - 90f - 29, this.screenHeight - 21f, 20, 20f, 3, new Color(30,30,30,100).getRGB());

                } else {
                    pGuiGraphics.blit(WIDGETS_LOCATION, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
                }
            }

            pGuiGraphics.pose().popPose();
            int l = 1;
            CustomItems.setRenderOffHand(false);

            for (int i1 = 0; i1 < 9; ++i1) {
                int j1 = i - 90 + i1 * 20 + 2;
                int k1 = this.screenHeight - 16 - 3;
                this.renderSlot(pGuiGraphics, j1, k1, pPartialTick, player, player.getInventory().items.get(i1), l++);
            }

            if (!itemstack.isEmpty()) {
                CustomItems.setRenderOffHand(true);
                int i2 = this.screenHeight - 16 - 3;
                if (humanoidarm == HumanoidArm.LEFT) {
                    this.renderSlot(pGuiGraphics, i - 91 - 26, i2, pPartialTick, player, itemstack, l++);
                } else {
                    this.renderSlot(pGuiGraphics, i + 91 + 10, i2, pPartialTick, player, itemstack, l++);
                }

                CustomItems.setRenderOffHand(false);
            }

            RenderSystem.enableBlend();
            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (f < 1.0F) {
                    int j2 = this.screenHeight - 20;
                    int k2 = i + 91 + 6;
                    if (humanoidarm == HumanoidArm.RIGHT) {
                        k2 = i - 91 - 22;
                    }

                    int l1 = (int) (f * 19.0F);
                    pGuiGraphics.blit(GUI_ICONS_LOCATION, k2, j2, 0, 94, 18, 18);
                    pGuiGraphics.blit(GUI_ICONS_LOCATION, k2, j2 + 18 - l1, 18, 112 - l1, 18, l1);
                }
            }

            RenderSystem.disableBlend();
        }

    }

    public void renderJumpMeter(PlayerRideableJumping pRideable, GuiGraphics pGuiGraphics, int pX) {
        this.minecraft.getProfiler().push("jumpBar");
        float f = this.minecraft.player.getJumpRidingScale();
        int i = 182;
        int j = (int) (f * 183.0F);
        int k = this.screenHeight - 32 + 3;
        pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, k, 0, 84, 182, 5);
        if (pRideable.getJumpCooldown() > 0) {
            pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, k, 0, 74, 182, 5);
        } else if (j > 0) {
            pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, k, 0, 89, j, 5);
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(GuiGraphics pGuiGraphics, int pX) {
        this.minecraft.getProfiler().push("expBar");
        int i = this.minecraft.player.getXpNeededForNextLevel();
        if (i > 0) {
            int j = 182;
            int k = (int) (this.minecraft.player.experienceProgress * 183.0F);
            int l = this.screenHeight - 32 + 3;
            pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, l, 0, 64, 182, 5);
            if (k > 0) {
                pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, l, 0, 69, k, 5);
            }
        }

        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            int j1 = 8453920;
            if (Config.isCustomColors()) {
                j1 = CustomColors.getExpBarTextColor(j1);
            }

            String s = "" + this.minecraft.player.experienceLevel;
            int k1 = (this.screenWidth - this.getFont().width(s)) / 2;
            int i1 = this.screenHeight - 31 - 4;
            pGuiGraphics.drawString(this.getFont(), s, k1 + 1, i1, 0, false);
            pGuiGraphics.drawString(this.getFont(), s, k1 - 1, i1, 0, false);
            pGuiGraphics.drawString(this.getFont(), s, k1, i1 + 1, 0, false);
            pGuiGraphics.drawString(this.getFont(), s, k1, i1 - 1, 0, false);
            pGuiGraphics.drawString(this.getFont(), s, k1, i1, j1, false);
            this.minecraft.getProfiler().pop();
        }

    }

    public void renderSelectedItemName(GuiGraphics pGuiGraphics) {
        this.renderSelectedItemName(pGuiGraphics, 0);
    }

    public void renderSelectedItemName(GuiGraphics graphicsIn, int yShift) {
        this.minecraft.getProfiler().push("selectedItemName");
        if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
            MutableComponent mutablecomponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
            if (Reflector.ForgeRarity_getStyleModifier.exists()) {
                Rarity rarity = this.lastToolHighlight.getRarity();
                UnaryOperator<Style> unaryoperator = (UnaryOperator) Reflector.call(rarity, Reflector.ForgeRarity_getStyleModifier);
                mutablecomponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(unaryoperator);
            }

            if (this.lastToolHighlight.hasCustomHoverName()) {
                mutablecomponent.withStyle(ChatFormatting.ITALIC);
            }

            Component component = mutablecomponent;
            if (Reflector.IForgeItemStack_getHighlightTip.exists()) {
                component = (Component) Reflector.call(this.lastToolHighlight, Reflector.IForgeItemStack_getHighlightTip, mutablecomponent);
            }

            int l = this.getFont().width(component);
            int i = (this.screenWidth - l) / 2;
            int j = this.screenHeight - Math.max(yShift, 59);
            if (!this.minecraft.gameMode.canHurtPlayer()) {
                j += 14;
            }

            int k = (int) ((float) this.toolHighlightTimer * 256.0F / 10.0F);
            if (k > 255) {
                k = 255;
            }

            if (k > 0) {
                graphicsIn.fill(i - 2, j - 2, i + l + 2, j + 9 + 2, this.minecraft.options.getBackgroundColor(0));
                Font font = null;
                IClientItemExtensions iclientitemextensions = IClientItemExtensions.of(this.lastToolHighlight);
                if (iclientitemextensions != null) {
                    font = iclientitemextensions.getFont(this.lastToolHighlight, IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
                }

                if (font != null) {
                    l = (this.screenWidth - font.width(component)) / 2;
                    graphicsIn.drawString(this.getFont(), component.getVisualOrderText(), i, j, 16777215 + (k << 24));
                } else {
                    graphicsIn.drawString(this.getFont(), mutablecomponent, i, j, 16777215 + (k << 24));
                }
            }
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(GuiGraphics pGuiGraphics) {
        this.minecraft.getProfiler().push("demo");
        Component component;
        if (this.minecraft.level.getGameTime() >= 120500L) {
            component = DEMO_EXPIRED_TEXT;
        } else {
            component = Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int) (120500L - this.minecraft.level.getGameTime())));
        }

        int i = this.getFont().width(component);
        pGuiGraphics.drawString(this.getFont(), component, this.screenWidth - i - 10, 5, 16777215);
        this.minecraft.getProfiler().pop();
    }




    private Player getCameraPlayer() {
        return !(this.minecraft.getCameraEntity() instanceof Player) ? null : (Player) this.minecraft.getCameraEntity();
    }

    private LivingEntity getPlayerVehicleWithHealth() {
        Player player = this.getCameraPlayer();
        if (player != null) {
            Entity entity = player.getVehicle();
            if (entity == null) {
                return null;
            }

            if (entity instanceof LivingEntity) {
                return (LivingEntity) entity;
            }
        }

        return null;
    }

    private int getVehicleMaxHearts(LivingEntity pVehicle) {
        if (pVehicle != null && pVehicle.showVehicleHealth()) {
            float f = pVehicle.getMaxHealth();
            int i = (int) (f + 0.5F) / 2;
            if (i > 30) {
                i = 30;
            }

            return i;
        } else {
            return 0;
        }
    }

    private int getVisibleVehicleHeartRows(int pVehicleHealth) {
        return (int) Math.ceil((double) pVehicleHealth / 10.0D);
    }

    private void renderPlayerHealth(GuiGraphics pGuiGraphics) {
        Player player = this.getCameraPlayer();
        if (player != null) {
            int i = Mth.ceil(player.getHealth());
            boolean flag = this.healthBlinkTime > (long) this.tickCount && (this.healthBlinkTime - (long) this.tickCount) / 3L % 2L == 1L;
            long j = Util.getMillis();
            if (i < this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = j;
                this.healthBlinkTime = (long) (this.tickCount + 20);
            } else if (i > this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = j;
                this.healthBlinkTime = (long) (this.tickCount + 10);
            }

            if (j - this.lastHealthTime > 1000L) {
                this.lastHealth = i;
                this.displayHealth = i;
                this.lastHealthTime = j;
            }

            this.lastHealth = i;
            int k = this.displayHealth;
            this.random.setSeed((long) (this.tickCount * 312871));
            FoodData fooddata = player.getFoodData();
            int l = fooddata.getFoodLevel();
            int i1 = this.screenWidth / 2 - 91;
            int j1 = this.screenWidth / 2 + 91;
            int k1 = this.screenHeight - 39;
            float f = Math.max((float) player.getAttributeValue(Attributes.MAX_HEALTH), (float) Math.max(k, i));
            int l1 = Mth.ceil(player.getAbsorptionAmount());
            int i2 = Mth.ceil((f + (float) l1) / 2.0F / 10.0F);
            int j2 = Math.max(10 - (i2 - 2), 3);
            int k2 = k1 - (i2 - 1) * j2 - 10;
            int l2 = k1 - 10;
            int i3 = player.getArmorValue();
            int j3 = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                j3 = this.tickCount % Mth.ceil(f + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            for (int k3 = 0; k3 < 10; ++k3) {
                if (i3 > 0) {
                    int l3 = i1 + k3 * 8;
                    if (k3 * 2 + 1 < i3) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, l3, k2, 34, 9, 9, 9);
                    }

                    if (k3 * 2 + 1 == i3) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, l3, k2, 25, 9, 9, 9);
                    }

                    if (k3 * 2 + 1 > i3) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, l3, k2, 16, 9, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");
            this.renderHearts(pGuiGraphics, player, i1, k1, j2, j3, f, i, k, l1, flag);
            LivingEntity livingentity = this.getPlayerVehicleWithHealth();
            int k5 = this.getVehicleMaxHearts(livingentity);
            if (k5 == 0) {
                this.minecraft.getProfiler().popPush("food");

                for (int i4 = 0; i4 < 10; ++i4) {
                    int j4 = k1;
                    int k4 = 16;
                    int l4 = 0;
                    if (player.hasEffect(MobEffects.HUNGER)) {
                        k4 += 36;
                        l4 = 13;
                    }

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (l * 3 + 1) == 0) {
                        j4 = k1 + (this.random.nextInt(3) - 1);
                    }

                    int i5 = j1 - i4 * 8 - 9;
                    pGuiGraphics.blit(GUI_ICONS_LOCATION, i5, j4, 16 + l4 * 9, 27, 9, 9);
                    if (i4 * 2 + 1 < l) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, i5, j4, k4 + 36, 27, 9, 9);
                    }

                    if (i4 * 2 + 1 == l) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, i5, j4, k4 + 45, 27, 9, 9);
                    }
                }

                l2 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            int l5 = player.getMaxAirSupply();
            int i6 = Math.min(player.getAirSupply(), l5);
            if (player.isEyeInFluid(FluidTags.WATER) || i6 < l5) {
                int j6 = this.getVisibleVehicleHeartRows(k5) - 1;
                l2 -= j6 * 10;
                int k6 = Mth.ceil((double) (i6 - 2) * 10.0D / (double) l5);
                int l6 = Mth.ceil((double) i6 * 10.0D / (double) l5) - k6;

                for (int j5 = 0; j5 < k6 + l6; ++j5) {
                    if (j5 < k6) {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, j1 - j5 * 8 - 9, l2, 16, 18, 9, 9);
                    } else {
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, j1 - j5 * 8 - 9, l2, 25, 18, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }

    }

    private void renderHearts(GuiGraphics pGuiGraphics, Player pPlayer, int pX, int pY, int pHeight, int pOffsetHeartIndex, float pMaxHealth, int pCurrentHealth, int pDisplayHealth, int pAbsorptionAmount, boolean pRenderHighlight) {
        Gui.HeartType gui$hearttype = Gui.HeartType.forPlayer(pPlayer);
        int i = 9 * (pPlayer.level().getLevelData().isHardcore() ? 5 : 0);
        int j = Mth.ceil((double) pMaxHealth / 2.0D);
        int k = Mth.ceil((double) pAbsorptionAmount / 2.0D);
        int l = j * 2;

        for (int i1 = j + k - 1; i1 >= 0; --i1) {
            int j1 = i1 / 10;
            int k1 = i1 % 10;
            int l1 = pX + k1 * 8;
            int i2 = pY - j1 * pHeight;
            if (pCurrentHealth + pAbsorptionAmount <= 4) {
                i2 += this.random.nextInt(2);
            }

            if (i1 < j && i1 == pOffsetHeartIndex) {
                i2 -= 2;
            }

            this.renderHeart(pGuiGraphics, Gui.HeartType.CONTAINER, l1, i2, i, pRenderHighlight, false);
            int j2 = i1 * 2;
            boolean flag = i1 >= j;
            if (flag) {
                int k2 = j2 - l;
                if (k2 < pAbsorptionAmount) {
                    boolean flag1 = k2 + 1 == pAbsorptionAmount;
                    this.renderHeart(pGuiGraphics, gui$hearttype == Gui.HeartType.WITHERED ? gui$hearttype : Gui.HeartType.ABSORBING, l1, i2, i, false, flag1);
                }
            }

            if (pRenderHighlight && j2 < pDisplayHealth) {
                boolean flag2 = j2 + 1 == pDisplayHealth;
                this.renderHeart(pGuiGraphics, gui$hearttype, l1, i2, i, true, flag2);
            }

            if (j2 < pCurrentHealth) {
                boolean flag3 = j2 + 1 == pCurrentHealth;
                this.renderHeart(pGuiGraphics, gui$hearttype, l1, i2, i, false, flag3);
            }
        }

    }

    private void renderHeart(GuiGraphics pGuiGraphics, Gui.HeartType pHeartType, int pX, int pY, int pYOffset, boolean pRenderHighlight, boolean pHalfHeart) {
        pGuiGraphics.blit(GUI_ICONS_LOCATION, pX, pY, pHeartType.getX(pHalfHeart, pRenderHighlight), pYOffset, 9, 9);
    }

    private void renderVehicleHealth(GuiGraphics pGuiGraphics) {
        LivingEntity livingentity = this.getPlayerVehicleWithHealth();
        if (livingentity != null) {
            int i = this.getVehicleMaxHearts(livingentity);
            if (i != 0) {
                int j = (int) Math.ceil((double) livingentity.getHealth());
                this.minecraft.getProfiler().popPush("mountHealth");
                int k = this.screenHeight - 39;
                int l = this.screenWidth / 2 + 91;
                int i1 = k;
                int j1 = 0;

                for (boolean flag = false; i > 0; j1 += 20) {
                    int k1 = Math.min(i, 10);
                    i -= k1;

                    for (int l1 = 0; l1 < k1; ++l1) {
                        int i2 = 52;
                        int j2 = 0;
                        int k2 = l - l1 * 8 - 9;
                        pGuiGraphics.blit(GUI_ICONS_LOCATION, k2, i1, 52 + j2 * 9, 9, 9, 9);
                        if (l1 * 2 + 1 + j1 < j) {
                            pGuiGraphics.blit(GUI_ICONS_LOCATION, k2, i1, 88, 9, 9, 9);
                        }

                        if (l1 * 2 + 1 + j1 == j) {
                            pGuiGraphics.blit(GUI_ICONS_LOCATION, k2, i1, 97, 9, 9, 9);
                        }
                    }

                    i1 -= 10;
                }
            }
        }

    }

    private void renderTextureOverlay(GuiGraphics pGuiGraphics, ResourceLocation pShaderLocation, float pAlpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, pAlpha);
        pGuiGraphics.blit(pShaderLocation, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSpyglassOverlay(GuiGraphics pGuiGraphics, float pScopeScale) {
        float f = (float) Math.min(this.screenWidth, this.screenHeight);
        float f1 = Math.min((float) this.screenWidth / f, (float) this.screenHeight / f) * pScopeScale;
        int i = Mth.floor(f * f1);
        int j = Mth.floor(f * f1);
        int k = (this.screenWidth - i) / 2;
        int l = (this.screenHeight - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        pGuiGraphics.blit(SPYGLASS_SCOPE_LOCATION, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, j1, this.screenWidth, this.screenHeight, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, l, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        pGuiGraphics.fill(RenderType.guiOverlay(), i1, l, this.screenWidth, j1, -90, -16777216);
    }

    private void updateVignetteBrightness(Entity pEntity) {
        if (pEntity != null) {
            BlockPos blockpos = BlockPos.containing(pEntity.getX(), pEntity.getEyeY(), pEntity.getZ());
            float f = LightTexture.getBrightness(pEntity.level().dimensionType(), pEntity.level().getMaxLocalRawBrightness(blockpos));
            float f1 = Mth.clamp(1.0F - f, 0.0F, 1.0F);
            this.vignetteBrightness += (f1 - this.vignetteBrightness) * 0.01F;
        }

    }

    private void renderVignette(GuiGraphics pGuiGraphics, Entity pEntity) {
        if (!Config.isVignetteEnabled()) {
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        } else {
            WorldBorder worldborder = this.minecraft.level.getWorldBorder();
            float f = (float) worldborder.getDistanceToBorder(pEntity);
            double d0 = Math.min(worldborder.getLerpSpeed() * (double) worldborder.getWarningTime() * 1000.0D, Math.abs(worldborder.getLerpTarget() - worldborder.getSize()));
            double d1 = Math.max((double) worldborder.getWarningBlocks(), d0);
            if ((double) f < d1) {
                f = 1.0F - (float) ((double) f / d1);
            } else {
                f = 0.0F;
            }

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            if (f > 0.0F) {
                f = Mth.clamp(f, 0.0F, 1.0F);
                pGuiGraphics.setColor(0.0F, f, f, 1.0F);
            } else {
                float f1 = this.vignetteBrightness;
                f1 = Mth.clamp(f1, 0.0F, 1.0F);
                pGuiGraphics.setColor(f1, f1, f1, 1.0F);
            }

            pGuiGraphics.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
        }
    }

    private void renderPortalOverlay(GuiGraphics pGuiGraphics, float pAlpha) {
        if (pAlpha < 1.0F) {
            pAlpha *= pAlpha;
            pAlpha *= pAlpha;
            pAlpha = pAlpha * 0.8F + 0.2F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, pAlpha);
        TextureAtlasSprite textureatlassprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        pGuiGraphics.blit(0, 0, -90, this.screenWidth, this.screenHeight, textureatlassprite);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlot(GuiGraphics pGuiGraphics, int pX, int pY, float pPartialTick, Player pPlayer, ItemStack pStack, int pSeed) {
        if (!pStack.isEmpty()) {
            float f = (float) pStack.getPopTime() - pPartialTick;
            if (f > 0.0F) {
                float f1 = 1.0F + f / 5.0F;
                pGuiGraphics.pose().pushPose();
                pGuiGraphics.pose().translate((float) (pX + 8), (float) (pY + 12), 0.0F);
                pGuiGraphics.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                pGuiGraphics.pose().translate((float) (-(pX + 8)), (float) (-(pY + 12)), 0.0F);
            }

            pGuiGraphics.renderItem(pPlayer, pStack, pX, pY, pSeed);
            if (f > 0.0F) {
                pGuiGraphics.pose().popPose();
            }

            pGuiGraphics.renderItemDecorations(this.minecraft.font, pStack, pX, pY);
        }

    }

    public void tick(boolean pPause) {
        this.tickAutosaveIndicator();
        if (!pPause) {
            this.tick();
        }

    }

    private void tick() {
        if (this.minecraft.level == null) {
            TextureAnimations.updateAnimations();
        }

        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }

        if (this.titleTime > 0) {
            --this.titleTime;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }

        ++this.tickCount;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.updateVignetteBrightness(entity);
        }

        if (this.minecraft.player != null) {
            ItemStack itemstack = this.minecraft.player.getInventory().getSelected();
            boolean flag = true;
            if (Reflector.IForgeItemStack_getHighlightTip.exists()) {
                Component component = (Component) Reflector.call(itemstack, Reflector.IForgeItemStack_getHighlightTip, itemstack.getHoverName());
                Component component1 = (Component) Reflector.call(this.lastToolHighlight, Reflector.IForgeItemStack_getHighlightTip, this.lastToolHighlight.getHoverName());
                flag = Config.equals(component, component1);
            }

            if (itemstack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (!this.lastToolHighlight.isEmpty() && itemstack.is(this.lastToolHighlight.getItem()) && itemstack.getHoverName().equals(this.lastToolHighlight.getHoverName()) && flag) {
                if (this.toolHighlightTimer > 0) {
                    --this.toolHighlightTimer;
                }
            } else {
                this.toolHighlightTimer = (int) (40.0D * this.minecraft.options.notificationDisplayTime().get());
            }

            this.lastToolHighlight = itemstack;
        }

        this.chat.tick();
    }

    private void tickAutosaveIndicator() {
        MinecraftServer minecraftserver = this.minecraft.getSingleplayerServer();
        boolean flag = minecraftserver != null && minecraftserver.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, flag ? 1.0F : 0.0F);
    }

    public void setNowPlaying(Component pDisplayName) {
        Component component = Component.translatable("record.nowPlaying", pDisplayName);
        this.setOverlayMessage(component, true);
        this.minecraft.getNarrator().sayNow(component);
    }

    public void setOverlayMessage(Component pComponent, boolean pAnimateColor) {
        this.setChatDisabledByPlayerShown(false);
        this.overlayMessageString = pComponent;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = pAnimateColor;
    }

    public void setChatDisabledByPlayerShown(boolean pChatDisabledByPlayerShown) {
        this.chatDisabledByPlayerShown = pChatDisabledByPlayerShown;
    }

    public boolean isShowingChatDisabledByPlayer() {
        return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
    }

    public void setTimes(int pTitleFadeInTime, int pTitleStayTime, int pTitleFadeOutTime) {
        if (pTitleFadeInTime >= 0) {
            this.titleFadeInTime = pTitleFadeInTime;
        }

        if (pTitleStayTime >= 0) {
            this.titleStayTime = pTitleStayTime;
        }

        if (pTitleFadeOutTime >= 0) {
            this.titleFadeOutTime = pTitleFadeOutTime;
        }

        if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        }

    }

    public void setSubtitle(Component pSubtitle) {
        this.subtitle = pSubtitle;
    }

    public void setTitle(Component pTitle) {
        this.title = pTitle;
        this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
    }

    public void clear() {
        this.title = null;
        this.subtitle = null;
        this.titleTime = 0;
    }

    public ChatComponent getChat() {
        return this.chat;
    }

    public int getGuiTicks() {
        return this.tickCount;
    }

    public static Font getFont() {
        return Mine.mc.font;
    }

    public SpectatorGui getSpectatorGui() {
        return this.spectatorGui;
    }

    public PlayerTabOverlay getTabList() {
        return this.tabList;
    }

    public void onDisconnected() {
        this.tabList.reset();
        this.bossOverlay.reset();
        this.minecraft.getToasts().clear();
        this.minecraft.options.renderDebug = false;
        this.chat.clearMessages(true);
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public void clearCache() {
        this.debugScreen.clearChunkCache();
    }

    private void renderSavingIndicator(GuiGraphics pGuiGraphics) {
        if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
            int i = Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F));
            if (i > 8) {
                Font font = this.getFont();
                int j = font.width(SAVING_TEXT);
                int k = 16777215 | i << 24 & -16777216;
                pGuiGraphics.drawString(font, SAVING_TEXT, this.screenWidth - j - 10, this.screenHeight - 15, k);
            }
        }

    }

    static enum HeartType {
        CONTAINER(0, false),
        NORMAL(2, true),
        POISIONED(4, true),
        WITHERED(6, true),
        ABSORBING(8, false),
        FROZEN(9, false);

        private final int index;
        private final boolean canBlink;

        private HeartType(int pIndex, boolean pCanBlink) {
            this.index = pIndex;
            this.canBlink = pCanBlink;
        }

        public int getX(boolean pHalfHeart, boolean pRenderHighlight) {
            int i;
            if (this == CONTAINER) {
                i = pRenderHighlight ? 1 : 0;
            } else {
                int j = pHalfHeart ? 1 : 0;
                int k = this.canBlink && pRenderHighlight ? 2 : 0;
                i = j + k;
            }

            return 16 + (this.index * 2 + i) * 9;
        }

        static Gui.HeartType forPlayer(Player pPlayer) {
            Gui.HeartType gui$hearttype;
            if (pPlayer.hasEffect(MobEffects.POISON)) {
                gui$hearttype = POISIONED;
            } else if (pPlayer.hasEffect(MobEffects.WITHER)) {
                gui$hearttype = WITHERED;
            } else if (pPlayer.isFullyFrozen()) {
                gui$hearttype = FROZEN;
            } else {
                gui$hearttype = NORMAL;
            }

            return gui$hearttype;
        }
    }
}
