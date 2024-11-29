package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.util.PropertiesOrdered;
import ru.fruzz.extazyy.misc.util.gif.Gif;
import ru.fruzz.extazyy.misc.util.render.DrawHelper;

public class LoadingOverlay extends Overlay {
   static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
   private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
   private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
   private static final IntSupplier BRAND_BACKGROUND = () -> {
      return Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
   };
   private static final int LOGO_SCALE = 240;
   private static final float LOGO_QUARTER_FLOAT = 60.0F;
   private static final int LOGO_QUARTER = 60;
   private static final int LOGO_HALF = 120;
   private static final float LOGO_OVERLAP = 0.0625F;
   private static final float SMOOTHING = 0.95F;
   public static final long FADE_OUT_TIME = 1000L;
   public static final long FADE_IN_TIME = 500L;
   private final Minecraft minecraft;
   private final ReloadInstance reload;
   private final Consumer<Optional<Throwable>> onFinish;
   private final boolean fadeIn;
   private float currentProgress;
   private long fadeOutStart = -1L;
   private long fadeInStart = -1L;
   private int colorBackground = BRAND_BACKGROUND.getAsInt();
   private int colorBar = BRAND_BACKGROUND.getAsInt();
   private int colorOutline = 16777215;
   private int colorProgress = 16777215;
   private GlBlendState blendState = null;
   private boolean fadeOut = false;

   public LoadingOverlay(Minecraft pMinecraft, ReloadInstance pReload, Consumer<Optional<Throwable>> pOnFinish, boolean pFadeIn) {
      this.minecraft = pMinecraft;
      this.reload = pReload;
      this.onFinish = pOnFinish;
      this.fadeIn = false;
   }

   public static void registerTextures(Minecraft pMinecraft) {
      pMinecraft.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
   }

   private static int replaceAlpha(int pColor, int pAlpha) {
      return pColor & 16777215 | pAlpha << 24;
   }

   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      PoseStack poseStack = pGuiGraphics.pose();
      float width = pGuiGraphics.guiWidth();
      float height = pGuiGraphics.guiHeight();
      DrawHelper.rectangle(poseStack, 0, 0, width, height, 0, new Color(0, 1, 8, 255).getRGB());
      long currentTime = Util.getMillis();
      if (this.fadeIn && this.fadeInStart == -1L) {
         this.fadeInStart = currentTime;
      }
      float fadeInTime = this.fadeInStart > -1L ? (float)(currentTime - this.fadeInStart) / 1000.0F : -1.0F;
      float fadeOutTime = this.fadeOutStart > -1L ? (float)(currentTime - this.fadeOutStart) / 1000.0F : -1.0F;
      float progress = this.reload.getActualProgress();
      this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + progress * 0.05F, 0.0F, 1.0F);

      int alpha = 50;
      if (this.currentProgress > 0.0F) {
         alpha = (int)(50 + (this.currentProgress * 200));
      }

      if (this.reload.isDone()) {
         alpha = 255;
      }
      int welcomeColor = new Color(255, 255, 255, alpha).getRGB();
      int[] resol = Gif.getTextureResolution(Gif.img(new ResourceLocation("minecraft", "extazyy/loading/logo.png")));
      DrawHelper.drawTextureAlpha(new ResourceLocation("minecraft", "extazyy/loading/logo.png"), poseStack.last().pose(), width / 2 - resol[0] / 4, height / 2 - resol[1] / 4, resol[0] / 2f , resol[1] / 2f , welcomeColor, alpha);
      if (fadeOutTime >= 2.0F) {
         this.minecraft.setOverlay((Overlay)null);
      }
      if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || fadeInTime >= 2.0F)) {
         this.fadeOutStart = Util.getMillis();
         if (this.minecraft.screen != null) {
            this.minecraft.screen.init(this.minecraft, pGuiGraphics.guiWidth(), pGuiGraphics.guiHeight());
         }
      }
   }



   private void drawProgressBar(GuiGraphics pGuiGraphics, int pMinX, int pMinY, int pMaxX, int pMaxY, float pPartialTick) {
      int i = Mth.ceil((float)(pMaxX - pMinX - 2) * this.currentProgress);
      int j = Math.round(pPartialTick * 255.0F);
      if (this.colorBar != this.colorBackground) {
         int k = this.colorBar >> 16 & 255;
         int l = this.colorBar >> 8 & 255;
         int i1 = this.colorBar & 255;
         int j1 = FastColor.ARGB32.color(j, k, l, i1);
         pGuiGraphics.fill(pMinX, pMinY, pMaxX, pMaxY, j1);
      }

      int j2 = this.colorProgress >> 16 & 255;
      int k2 = this.colorProgress >> 8 & 255;
      int l2 = this.colorProgress & 255;
      int i3 = FastColor.ARGB32.color(j, j2, k2, l2);
      pGuiGraphics.fill(pMinX + 2, pMinY + 2, pMinX + i, pMaxY - 2, i3);
      int k1 = this.colorOutline >> 16 & 255;
      int l1 = this.colorOutline >> 8 & 255;
      int i2 = this.colorOutline & 255;
      i3 = FastColor.ARGB32.color(j, k1, l1, i2);
      pGuiGraphics.fill(pMinX + 1, pMinY, pMaxX - 1, pMinY + 1, i3);
      pGuiGraphics.fill(pMinX + 1, pMaxY, pMaxX - 1, pMaxY - 1, i3);
      pGuiGraphics.fill(pMinX, pMinY, pMinX + 1, pMaxY, i3);
      pGuiGraphics.fill(pMaxX, pMinY, pMaxX - 1, pMaxY, i3);
   }

   public boolean isPauseScreen() {
      return true;
   }

   public void update() {
      this.colorBackground = BRAND_BACKGROUND.getAsInt();
      this.colorBar = BRAND_BACKGROUND.getAsInt();
      this.colorOutline = 16777215;
      this.colorProgress = 16777215;
      if (Config.isCustomColors()) {
         try {
            String s = "optifine/color.properties";
            ResourceLocation resourcelocation = new ResourceLocation(s);
            if (!Config.hasResource(resourcelocation)) {
               return;
            }

            InputStream inputstream = Config.getResourceStream(resourcelocation);
            Config.dbg("Loading " + s);
            Properties properties = new PropertiesOrdered();
            properties.load(inputstream);
            inputstream.close();
            this.colorBackground = readColor(properties, "screen.loading", this.colorBackground);
            this.colorOutline = readColor(properties, "screen.loading.outline", this.colorOutline);
            this.colorBar = readColor(properties, "screen.loading.bar", this.colorBar);
            this.colorProgress = readColor(properties, "screen.loading.progress", this.colorProgress);
            this.blendState = ShaderPackParser.parseBlendState(properties.getProperty("screen.loading.blend"));
         } catch (Exception exception) {
            Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
         }

      }
   }

   private static int readColor(Properties props, String name, int colDef) {
      String s = props.getProperty(name);
      if (s == null) {
         return colDef;
      } else {
         s = s.trim();
         int i = parseColor(s, colDef);
         if (i < 0) {
            Config.warn("Invalid color: " + name + " = " + s);
            return i;
         } else {
            Config.dbg(name + " = " + s);
            return i;
         }
      }
   }

   private static int parseColor(String str, int colDef) {
      if (str == null) {
         return colDef;
      } else {
         str = str.trim();

         try {
            return Integer.parseInt(str, 16) & 16777215;
         } catch (NumberFormatException numberformatexception) {
            return colDef;
         }
      }
   }

   public boolean isFadeOut() {
      return this.fadeOut;
   }

   static class LogoTexture extends SimpleTexture {
      public LogoTexture() {
         super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
      }

      protected SimpleTexture.TextureImage getTextureImage(ResourceManager pResourceManager) {
         VanillaPackResources vanillapackresources = Minecraft.getInstance().getVanillaPackResources();
         IoSupplier<InputStream> iosupplier = vanillapackresources.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
         if (iosupplier == null) {
            return new SimpleTexture.TextureImage(new FileNotFoundException(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION.toString()));
         } else {
            try (InputStream inputstream = getLogoInputStream(pResourceManager, iosupplier)) {
               return new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
            } catch (IOException ioexception1) {
               return new SimpleTexture.TextureImage(ioexception1);
            }
         }
      }

      private static InputStream getLogoInputStream(ResourceManager resourceManager, IoSupplier<InputStream> inputStream) throws IOException {
         return resourceManager.getResource(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION).isPresent() ? resourceManager.getResource(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION).get().open() : inputStream.get();
      }
   }
}