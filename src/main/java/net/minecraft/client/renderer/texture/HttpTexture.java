package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.Config;
import net.optifine.http.HttpPipeline;
import net.optifine.http.HttpRequest;
import net.optifine.http.HttpResponse;
import net.optifine.player.CapeImageBuffer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class HttpTexture extends SimpleTexture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SKIN_WIDTH = 64;
   private static final int SKIN_HEIGHT = 64;
   private static final int LEGACY_SKIN_HEIGHT = 32;
   @Nullable
   private final File file;
   private final String urlString;
   private final boolean processLegacySkin;
   @Nullable
   private final Runnable onDownloaded;
   @Nullable
   private CompletableFuture<?> future;
   private boolean uploaded;
   public Boolean imageFound = null;
   public boolean pipeline = false;
   private boolean uploadPending = false;

   public HttpTexture(@Nullable File pFile, String pUrlString, ResourceLocation pLocation, boolean pProcessLegacySkin, @Nullable Runnable pOnDownloaded) {
      super(pLocation);
      this.file = pFile;
      this.urlString = pUrlString;
      this.processLegacySkin = pProcessLegacySkin;
      this.onDownloaded = pOnDownloaded;
   }

   private void loadCallback(NativeImage pImage) {
      if (this.onDownloaded instanceof CapeImageBuffer) {
         CapeImageBuffer capeimagebuffer = (CapeImageBuffer)this.onDownloaded;
         pImage = capeimagebuffer.parseUserSkin(pImage);
         capeimagebuffer.skinAvailable();
      }

      this.setImageImpl(pImage);
   }

   private void setImageImpl(NativeImage nativeImageIn) {
      if (this.onDownloaded != null) {
         this.onDownloaded.run();
      }

      Minecraft.getInstance().execute(() -> {
         this.uploaded = true;
         if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
               this.upload(nativeImageIn);
            });
         } else {
            this.upload(nativeImageIn);
         }

      });
   }

   private void upload(NativeImage pImage) {
      TextureUtil.prepareImage(this.getId(), pImage.getWidth(), pImage.getHeight());
      pImage.upload(0, 0, 0, true);
      this.imageFound = pImage != null;
      this.size = pImage.getSize();
   }

   public void load(ResourceManager pResourceManager) throws IOException {
      Minecraft.getInstance().execute(() -> {
         if (!this.uploaded) {
            try {
               super.load(pResourceManager);
            } catch (IOException ioexception) {
               LOGGER.warn("Failed to load texture: {}", this.location, ioexception);
            }

            this.uploaded = true;
         }

      });
      if (this.future == null) {
         NativeImage nativeimage;
         if (this.file != null && this.file.isFile()) {
            LOGGER.debug("Loading http texture from local cache ({})", (Object)this.file);
            FileInputStream fileinputstream = new FileInputStream(this.file);
            nativeimage = this.load(fileinputstream);
         } else {
            nativeimage = null;
         }

         if (nativeimage != null) {
            this.loadCallback(nativeimage);
            this.loadingFinished();
         } else {
            this.future = CompletableFuture.runAsync(() -> {
               HttpURLConnection httpurlconnection = null;
               LOGGER.debug("Downloading http texture from {} to {}", this.urlString, this.file);
               if (this.shouldPipeline()) {
                  this.loadPipelined();
               } else {
                  try {
                     httpurlconnection = (HttpURLConnection)(new URL(this.urlString)).openConnection(Minecraft.getInstance().getProxy());
                     httpurlconnection.setDoInput(true);
                     httpurlconnection.setDoOutput(false);
                     httpurlconnection.connect();
                     if (httpurlconnection.getResponseCode() / 100 != 2) {
                        if (httpurlconnection.getErrorStream() != null) {
                           Config.readAll(httpurlconnection.getErrorStream());
                        }

                        return;
                     }

                     InputStream inputstream;
                     if (this.file != null) {
                        FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), this.file);
                        inputstream = new FileInputStream(this.file);
                     } else {
                        inputstream = httpurlconnection.getInputStream();
                     }

                     Minecraft.getInstance().execute(() -> {
                        NativeImage nativeimage1 = this.load(inputstream);
                        if (nativeimage1 != null) {
                           this.loadCallback(nativeimage1);
                           this.loadingFinished();
                        }

                     });
                     this.uploadPending = true;
                  } catch (Exception exception1) {
                     LOGGER.error("Couldn't download http texture", (Throwable)exception1);
                     return;
                  } finally {
                     if (httpurlconnection != null) {
                        httpurlconnection.disconnect();
                     }

                     this.loadingFinished();
                  }

               }
            }, this.getExecutor());
         }
      }

   }

   @Nullable
   private NativeImage load(InputStream pStream) {
      NativeImage nativeimage = null;

      try {
         nativeimage = NativeImage.read(pStream);
         if (this.processLegacySkin) {
            nativeimage = this.processLegacySkin(nativeimage);
         }
      } catch (Exception exception) {
         LOGGER.warn("Error while loading the skin texture", (Throwable)exception);
      }

      return nativeimage;
   }

   private boolean shouldPipeline() {
      if (!this.pipeline) {
         return false;
      } else {
         Proxy proxy = Minecraft.getInstance().getProxy();
         if (proxy.type() != Type.DIRECT && proxy.type() != Type.SOCKS) {
            return false;
         } else {
            return this.urlString.startsWith("http://");
         }
      }
   }

   private void loadPipelined() {
      try {
         HttpRequest httprequest = HttpPipeline.makeRequest(this.urlString, Minecraft.getInstance().getProxy());
         HttpResponse httpresponse = HttpPipeline.executeRequest(httprequest);
         if (httpresponse.getStatus() / 100 != 2) {
            return;
         }

         byte[] abyte = httpresponse.getBody();
         ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(abyte);
         NativeImage nativeimage;
         if (this.file != null) {
            FileUtils.copyInputStreamToFile(bytearrayinputstream, this.file);
            nativeimage = NativeImage.read(new FileInputStream(this.file));
         } else {
            nativeimage = NativeImage.read(bytearrayinputstream);
         }

         this.loadCallback(nativeimage);
         this.uploadPending = true;
      } catch (Exception exception) {
         LOGGER.error("Couldn't download http texture: " + exception.getClass().getName() + ": " + exception.getMessage());
         return;
      } finally {
         this.loadingFinished();
      }

   }

   private void loadingFinished() {
      if (!this.uploadPending) {
         if (this.onDownloaded instanceof CapeImageBuffer) {
            CapeImageBuffer capeimagebuffer = (CapeImageBuffer)this.onDownloaded;
            capeimagebuffer.cleanup();
         }

      }
   }

   public Runnable getProcessTask() {
      return this.onDownloaded;
   }

   private Executor getExecutor() {
      return this.urlString.startsWith("http://s.optifine.net") ? Util.getCapeExecutor() : Util.backgroundExecutor();
   }

   @Nullable
   private NativeImage processLegacySkin(NativeImage pImage) {
      int i = pImage.getHeight();
      int j = pImage.getWidth();
      if (j == 64 && (i == 32 || i == 64)) {
         boolean flag = i == 32;
         if (flag) {
            NativeImage nativeimage = new NativeImage(64, 64, true);
            nativeimage.copyFrom(pImage);
            pImage.close();
            pImage = nativeimage;
            nativeimage.fillRect(0, 32, 64, 32, 0);
            nativeimage.copyRect(4, 16, 16, 32, 4, 4, true, false);
            nativeimage.copyRect(8, 16, 16, 32, 4, 4, true, false);
            nativeimage.copyRect(0, 20, 24, 32, 4, 12, true, false);
            nativeimage.copyRect(4, 20, 16, 32, 4, 12, true, false);
            nativeimage.copyRect(8, 20, 8, 32, 4, 12, true, false);
            nativeimage.copyRect(12, 20, 16, 32, 4, 12, true, false);
            nativeimage.copyRect(44, 16, -8, 32, 4, 4, true, false);
            nativeimage.copyRect(48, 16, -8, 32, 4, 4, true, false);
            nativeimage.copyRect(40, 20, 0, 32, 4, 12, true, false);
            nativeimage.copyRect(44, 20, -8, 32, 4, 12, true, false);
            nativeimage.copyRect(48, 20, -16, 32, 4, 12, true, false);
            nativeimage.copyRect(52, 20, -8, 32, 4, 12, true, false);
         }

         setNoAlpha(pImage, 0, 0, 32, 16);
         if (flag) {
            doNotchTransparencyHack(pImage, 32, 0, 64, 32);
         }

         setNoAlpha(pImage, 0, 16, 64, 32);
         setNoAlpha(pImage, 16, 48, 48, 64);
         return pImage;
      } else {
         pImage.close();
         LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, this.urlString);
         return null;
      }
   }

   private static void doNotchTransparencyHack(NativeImage pImage, int pX, int pY, int pWidth, int pHeight) {
      for(int i = pX; i < pWidth; ++i) {
         for(int j = pY; j < pHeight; ++j) {
            int k = pImage.getPixelRGBA(i, j);
            if ((k >> 24 & 255) < 128) {
               return;
            }
         }
      }

      for(int l = pX; l < pWidth; ++l) {
         for(int i1 = pY; i1 < pHeight; ++i1) {
            pImage.setPixelRGBA(l, i1, pImage.getPixelRGBA(l, i1) & 16777215);
         }
      }

   }

   private static void setNoAlpha(NativeImage pImage, int pX, int pY, int pWidth, int pHeight) {
      for(int i = pX; i < pWidth; ++i) {
         for(int j = pY; j < pHeight; ++j) {
            pImage.setPixelRGBA(i, j, pImage.getPixelRGBA(i, j) | -16777216);
         }
      }

   }
}