package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.optifine.Config;
import net.optifine.EmissiveTextures;
import net.optifine.RandomEntities;
import net.optifine.shaders.ShadersTex;
import org.slf4j.Logger;

public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
   private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
   private final Set<Tickable> tickableTextures = Sets.newHashSet();
   private final Map<String, Integer> prefixRegister = Maps.newHashMap();
   private final ResourceManager resourceManager;
   private Int2ObjectMap<AbstractTexture> mapTexturesById = new Int2ObjectOpenHashMap<>();

   public TextureManager(ResourceManager pResourceManager) {
      this.resourceManager = pResourceManager;
   }

   public void bindForSetup(ResourceLocation pPath) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this._bind(pPath);
         });
      } else {
         this._bind(pPath);
      }

   }

   private void _bind(ResourceLocation pPath) {
      AbstractTexture abstracttexture = this.byPath.get(pPath);
      if (abstracttexture == null) {
         abstracttexture = new SimpleTexture(pPath);
         this.register(pPath, abstracttexture);
      }

      if (Config.isShaders()) {
         ShadersTex.bindTexture(abstracttexture);
      } else {
         abstracttexture.bind();
      }

   }

   public void register(ResourceLocation pPath, AbstractTexture pTexture) {
      pTexture = this.loadTexture(pPath, pTexture);
      AbstractTexture abstracttexture = this.byPath.put(pPath, pTexture);
      if (abstracttexture != pTexture) {
         if (abstracttexture != null && abstracttexture != MissingTextureAtlasSprite.getTexture()) {
            this.safeClose(pPath, abstracttexture);
         }

         if (pTexture instanceof Tickable) {
            this.tickableTextures.add((Tickable)pTexture);
         }
      }

      int i = pTexture.getId();
      if (i > 0) {
         this.mapTexturesById.put(i, pTexture);
      }

   }

   private void safeClose(ResourceLocation p_118509_, AbstractTexture p_118510_) {
      if (p_118510_ != MissingTextureAtlasSprite.getTexture()) {
         this.tickableTextures.remove(p_118510_);

         try {
            p_118510_.close();
         } catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", p_118509_, exception);
         }
      }

      p_118510_.releaseId();
   }

   private AbstractTexture loadTexture(ResourceLocation pPath, AbstractTexture pTexture) {
      try {
         pTexture.load(this.resourceManager);
         return pTexture;
      } catch (IOException ioexception) {
         if (pPath != INTENTIONAL_MISSING_TEXTURE) {
            LOGGER.warn("Failed to load texture: {}", (Object)pPath);
            LOGGER.warn(ioexception.getClass().getName() + ": " + ioexception.getMessage());
         }

         return MissingTextureAtlasSprite.getTexture();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Registering texture");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Resource location being registered");
         crashreportcategory.setDetail("Resource location", pPath);
         crashreportcategory.setDetail("Texture object class", () -> {
            return pTexture.getClass().getName();
         });
         throw new ReportedException(crashreport);
      }
   }

   public AbstractTexture getTexture(ResourceLocation pPath) {
      AbstractTexture abstracttexture = this.byPath.get(pPath);
      if (abstracttexture == null) {
         abstracttexture = new SimpleTexture(pPath);
         this.register(pPath, abstracttexture);
      }

      return abstracttexture;
   }

   public AbstractTexture getTexture(ResourceLocation pPath, AbstractTexture pDefaultTexture) {
      return this.byPath.getOrDefault(pPath, pDefaultTexture);
   }

   public ResourceLocation register(String pName, DynamicTexture pTexture) {
      Integer integer = this.prefixRegister.get(pName);
      if (integer == null) {
         integer = 1;
      } else {
         integer = integer + 1;
      }

      this.prefixRegister.put(pName, integer);
      ResourceLocation resourcelocation = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", pName, integer));
      this.register(resourcelocation, pTexture);
      return resourcelocation;
   }

   public CompletableFuture<Void> preload(ResourceLocation pPath, Executor pBackgroundExecutor) {
      if (!this.byPath.containsKey(pPath)) {
         PreloadedTexture preloadedtexture = new PreloadedTexture(this.resourceManager, pPath, pBackgroundExecutor);
         this.byPath.put(pPath, preloadedtexture);
         return preloadedtexture.getFuture().thenRunAsync(() -> {
            this.register(pPath, preloadedtexture);
         }, TextureManager::execute);
      } else {
         return CompletableFuture.completedFuture((Void)null);
      }
   }

   private static void execute(Runnable p_118489_) {
      Minecraft.getInstance().execute(() -> {
         RenderSystem.recordRenderCall(p_118489_::run);
      });
   }

   public void tick() {
      for(Tickable tickable : this.tickableTextures) {
         tickable.tick();
      }

   }

   public void release(ResourceLocation pPath) {
      AbstractTexture abstracttexture = this.byPath.remove(pPath);
      if (abstracttexture != null) {
         this.byPath.remove(pPath);
         this.safeClose(pPath, abstracttexture);
      }

   }

   public void close() {
      this.byPath.forEach(this::safeClose);
      this.byPath.clear();
      this.tickableTextures.clear();
      this.prefixRegister.clear();
      this.mapTexturesById.clear();
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      Config.dbg("*** Reloading textures ***");
      Config.log("Resource packs: " + Config.getResourcePackNames());
      Iterator iterator = this.byPath.keySet().iterator();

      while(iterator.hasNext()) {
         ResourceLocation resourcelocation = (ResourceLocation)iterator.next();
         String s = resourcelocation.getPath();
         if (s.startsWith("optifine/") || EmissiveTextures.isEmissive(resourcelocation)) {
            AbstractTexture abstracttexture = this.byPath.get(resourcelocation);
            if (abstracttexture instanceof AbstractTexture) {
               abstracttexture.releaseId();
            }

            iterator.remove();
         }
      }

      RandomEntities.update();
      EmissiveTextures.update();
      CompletableFuture<Void> completablefuture = new CompletableFuture<>();
      CompletableFuture.allOf(TitleScreen.preloadResources(this, pBackgroundExecutor), this.preload(AbstractWidget.WIDGETS_LOCATION, pBackgroundExecutor)).thenCompose(pStage::wait).thenAcceptAsync((p_244739_4_) -> {
         MissingTextureAtlasSprite.getTexture();
         RealmsMainScreen.updateTeaserImages(this.resourceManager);
         Set<Map.Entry<ResourceLocation, AbstractTexture>> set = new HashSet<>(this.byPath.entrySet());
         Iterator<Map.Entry<ResourceLocation, AbstractTexture>> iterator1 = set.iterator();

         while(iterator1.hasNext()) {
            Map.Entry<ResourceLocation, AbstractTexture> entry = iterator1.next();
            ResourceLocation resourcelocation1 = entry.getKey();
            AbstractTexture abstracttexture1 = entry.getValue();
            if (abstracttexture1 == MissingTextureAtlasSprite.getTexture() && !resourcelocation1.equals(MissingTextureAtlasSprite.getLocation())) {
               iterator1.remove();
            } else {
               abstracttexture1.reset(this, pResourceManager, resourcelocation1, pGameExecutor);
            }
         }

         Minecraft.getInstance().tell(() -> {
            completablefuture.complete((Void)null);
         });
      }, (runnableIn) -> {
         RenderSystem.recordRenderCall(runnableIn::run);
      });
      return completablefuture;
   }

   public void dumpAllSheets(Path pPath) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            this._dumpAllSheets(pPath);
         });
      } else {
         this._dumpAllSheets(pPath);
      }

   }

   private void _dumpAllSheets(Path pPath) {
      try {
         Files.createDirectories(pPath);
      } catch (IOException ioexception) {
         LOGGER.error("Failed to create directory {}", pPath, ioexception);
         return;
      }

      this.byPath.forEach((p_276077_1_, p_276077_2_) -> {
         if (p_276077_2_ instanceof Dumpable dumpable) {
            try {
               dumpable.dumpContents(p_276077_1_, pPath);
            } catch (IOException ioexception1) {
               LOGGER.error("Failed to dump texture {}", p_276077_1_, ioexception1);
            }
         }

      });
   }

   public AbstractTexture getTextureById(int id) {
      AbstractTexture abstracttexture = this.mapTexturesById.get(id);
      if (abstracttexture != null && abstracttexture.getId() != id) {
         this.mapTexturesById.remove(id);
         this.mapTexturesById.put(abstracttexture.getId(), abstracttexture);
         abstracttexture = null;
      }

      return abstracttexture;
   }

   public Collection<AbstractTexture> getTextures() {
      return this.byPath.values();
   }

   public Collection<ResourceLocation> getTextureLocations() {
      return this.byPath.keySet();
   }
}