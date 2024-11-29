package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.optifine.Config;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTextureType;
import net.optifine.util.CounterInt;
import net.optifine.util.TextureUtils;

public class TextureAtlasSprite {
   private ResourceLocation atlasLocation;
   private SpriteContents contents;
   int x;
   int y;
   private float u0;
   private float u1;
   private float v0;
   private float v1;
   private int indexInMap = -1;
   public float baseU;
   public float baseV;
   public int sheetWidth;
   public int sheetHeight;
   private final ResourceLocation name;
   public int glSpriteTextureId = -1;
   public TextureAtlasSprite spriteSingle = null;
   public boolean isSpriteSingle = false;
   public static final String SUFFIX_SPRITE_SINGLE = ".sprite_single";
   public TextureAtlasSprite spriteNormal = null;
   public TextureAtlasSprite spriteSpecular = null;
   public ShadersTextureType spriteShadersType = null;
   public TextureAtlasSprite spriteEmissive = null;
   public boolean isSpriteEmissive = false;
   protected int animationIndex = -1;
   private boolean terrain;
   private boolean shaders;
   private boolean multiTexture;
   private ResourceManager resourceManager;
   private int imageWidth;
   private int imageHeight;
   private TextureAtlas atlasTexture;
   private SpriteContents.Ticker spriteContentsTicker;
   private TextureAtlasSprite parentSprite;
   protected boolean usesParentAnimationTime = false;

   public TextureAtlasSprite(ResourceLocation atlasLocation, ResourceLocation name) {
      this.atlasLocation = atlasLocation;
      this.name = name;
      this.contents = null;
      this.atlasTexture = null;
      this.x = 0;
      this.y = 0;
      this.u0 = 0.0F;
      this.u1 = 0.0F;
      this.v0 = 0.0F;
      this.v1 = 0.0F;
      this.imageWidth = 0;
      this.imageHeight = 0;
   }

   private TextureAtlasSprite(TextureAtlasSprite parent) {
      this.atlasTexture = parent.atlasTexture;
      this.name = parent.getName();
      SpriteContents spritecontents = parent.contents;
      this.contents = new SpriteContents(spritecontents.name(), new FrameSize(spritecontents.width, spritecontents.height), spritecontents.getOriginalImage(), spritecontents.getMetadata());
      this.contents.setSprite(this);
      this.contents.setScaleFactor(spritecontents.getScaleFactor());
      this.imageWidth = parent.imageWidth;
      this.imageHeight = parent.imageHeight;
      this.usesParentAnimationTime = true;
      this.x = 0;
      this.y = 0;
      this.u0 = 0.0F;
      this.u1 = 1.0F;
      this.v0 = 0.0F;
      this.v1 = 1.0F;
      this.baseU = Math.min(this.u0, this.u1);
      this.baseV = Math.min(this.v0, this.v1);
      this.indexInMap = parent.indexInMap;
      this.baseU = parent.baseU;
      this.baseV = parent.baseV;
      this.sheetWidth = parent.sheetWidth;
      this.sheetHeight = parent.sheetHeight;
      this.isSpriteSingle = true;
      this.animationIndex = parent.animationIndex;
      if (this.spriteContentsTicker != null && parent.spriteContentsTicker != null) {
         this.spriteContentsTicker.animationActive = parent.spriteContentsTicker.animationActive;
      }

   }

   public void init(ResourceLocation locationIn, SpriteContents contentsIn, int atlasWidthIn, int atlasHeightIn, int xIn, int yIn) {
      this.atlasLocation = locationIn;
      this.contents = contentsIn;
      this.contents.setSprite(this);
      this.sheetWidth = atlasWidthIn;
      this.sheetHeight = atlasHeightIn;
      this.imageWidth = this.contents.width;
      this.imageHeight = this.contents.height;
      this.x = xIn;
      this.y = yIn;
      this.u0 = (float)xIn / (float)atlasWidthIn;
      this.u1 = (float)(xIn + contentsIn.width()) / (float)atlasWidthIn;
      this.v0 = (float)yIn / (float)atlasHeightIn;
      this.v1 = (float)(yIn + contentsIn.height()) / (float)atlasHeightIn;
      this.baseU = Math.min(this.u0, this.u1);
      this.baseV = Math.min(this.v0, this.v1);
   }

   protected TextureAtlasSprite(ResourceLocation pAtlasLocation, SpriteContents pContents, int pOriginX, int pOriginY, int pX, int pY) {
      this(pAtlasLocation, pContents, pOriginX, pOriginY, pX, pY, (TextureAtlas)null, (ShadersTextureType)null);
   }

   protected TextureAtlasSprite(ResourceLocation locationIn, SpriteContents contentsIn, int atlasWidthIn, int atlasHeightIn, int xIn, int yIn, TextureAtlas atlas, ShadersTextureType spriteShadersTypeIn) {
      this.atlasTexture = atlas;
      this.spriteShadersType = spriteShadersTypeIn;
      this.atlasLocation = locationIn;
      this.contents = contentsIn;
      this.name = contentsIn.name();
      this.imageWidth = this.contents.width;
      this.imageHeight = this.contents.height;
      this.x = xIn;
      this.y = yIn;
      this.u0 = (float)xIn / (float)atlasWidthIn;
      this.u1 = (float)(xIn + contentsIn.width()) / (float)atlasWidthIn;
      this.v0 = (float)yIn / (float)atlasHeightIn;
      this.v1 = (float)(yIn + contentsIn.height()) / (float)atlasHeightIn;
      this.baseU = Math.min(this.u0, this.u1);
      this.baseV = Math.min(this.v0, this.v1);
      this.sheetWidth = atlasWidthIn;
      this.sheetHeight = atlasHeightIn;
      this.contents.setSprite(this);
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public float getU0() {
      return this.u0;
   }

   public float getU1() {
      return this.u1;
   }

   public SpriteContents contents() {
      return this.contents;
   }

   @Nullable
   public TextureAtlasSprite.Ticker createTicker() {
      final SpriteTicker spriteticker = this.contents.createTicker();
      if (spriteticker != null) {
         spriteticker.setSprite(this);
      }

      return spriteticker != null ? new TextureAtlasSprite.Ticker() {
         public void tickAndUpload() {
            spriteticker.tickAndUpload(TextureAtlasSprite.this.x, TextureAtlasSprite.this.y);
         }

         public void close() {
            spriteticker.close();
         }

         public TextureAtlasSprite getSprite() {
            return TextureAtlasSprite.this;
         }

         public SpriteTicker getSpriteTicker() {
            return spriteticker;
         }
      } : null;
   }

   public float getU(double pU) {
      float f = this.u1 - this.u0;
      return this.u0 + f * (float)pU / 16.0F;
   }

   public float getUOffset(float pOffset) {
      float f = this.u1 - this.u0;
      return (pOffset - this.u0) / f * 16.0F;
   }

   public float getV0() {
      return this.v0;
   }

   public float getV1() {
      return this.v1;
   }

   public float getV(double pV) {
      float f = this.v1 - this.v0;
      return this.v0 + f * (float)pV / 16.0F;
   }

   public float getVOffset(float pOffset) {
      float f = this.v1 - this.v0;
      return (pOffset - this.v0) / f * 16.0F;
   }

   public ResourceLocation atlasLocation() {
      return this.atlasLocation;
   }

   public String toString() {
      return "TextureAtlasSprite{name= " + this.name + ", contents='" + this.contents + "', u0=" + this.u0 + ", u1=" + this.u1 + ", v0=" + this.v0 + ", v1=" + this.v1 + "}";
   }

   public void uploadFirstFrame() {
      this.contents.uploadFirstFrame(this.x, this.y);
   }

   private float atlasSize() {
      float f = (float)this.contents.width() / (this.u1 - this.u0);
      float f1 = (float)this.contents.height() / (this.v1 - this.v0);
      return Math.max(f1, f);
   }

   public float uvShrinkRatio() {
      return 4.0F / this.atlasSize();
   }

   public VertexConsumer wrap(VertexConsumer pConsumer) {
      return new SpriteCoordinateExpander(pConsumer, this);
   }

   public int getIndexInMap() {
      return this.indexInMap;
   }

   public void updateIndexInMap(CounterInt counterInt) {
      if (this.indexInMap < 0) {
         if (this.atlasTexture != null) {
            TextureAtlasSprite textureatlassprite = this.atlasTexture.getRegisteredSprite(this.getName());
            if (textureatlassprite != null) {
               this.indexInMap = textureatlassprite.getIndexInMap();
            }
         }

         if (this.indexInMap < 0) {
            this.indexInMap = counterInt.nextValue();
         }

      }
   }

   public int getAnimationIndex() {
      return this.animationIndex;
   }

   public void setAnimationIndex(int animationIndex) {
      this.animationIndex = animationIndex;
      if (this.spriteSingle != null) {
         this.spriteSingle.setAnimationIndex(animationIndex);
      }

      if (this.spriteNormal != null) {
         this.spriteNormal.setAnimationIndex(animationIndex);
      }

      if (this.spriteSpecular != null) {
         this.spriteSpecular.setAnimationIndex(animationIndex);
      }

   }

   public boolean isAnimationActive() {
      return this.spriteContentsTicker == null ? false : this.spriteContentsTicker.animationActive;
   }

   public static void fixTransparentColor(NativeImage ni) {
      int[] aint = new int[ni.getWidth() * ni.getHeight()];
      ni.getBufferRGBA().get(aint);
      fixTransparentColor(aint);
      ni.getBufferRGBA().put(aint);
   }

   private static void fixTransparentColor(int[] data) {
      if (data != null) {
         long i = 0L;
         long j = 0L;
         long k = 0L;
         long l = 0L;

         for(int i1 = 0; i1 < data.length; ++i1) {
            int j1 = data[i1];
            int k1 = j1 >> 24 & 255;
            if (k1 >= 16) {
               int l1 = j1 >> 16 & 255;
               int i2 = j1 >> 8 & 255;
               int j2 = j1 & 255;
               i += (long)l1;
               j += (long)i2;
               k += (long)j2;
               ++l;
            }
         }

         if (l > 0L) {
            int l2 = (int)(i / l);
            int i3 = (int)(j / l);
            int j3 = (int)(k / l);
            int k3 = l2 << 16 | i3 << 8 | j3;

            for(int l3 = 0; l3 < data.length; ++l3) {
               int i4 = data[l3];
               int k2 = i4 >> 24 & 255;
               if (k2 <= 16) {
                  data[l3] = k3;
               }
            }

         }
      }
   }

   public double getSpriteU16(float atlasU) {
      float f = this.u1 - this.u0;
      return (double)((atlasU - this.u0) / f * 16.0F);
   }

   public double getSpriteV16(float atlasV) {
      float f = this.v1 - this.v0;
      return (double)((atlasV - this.v0) / f * 16.0F);
   }

   public void bindSpriteTexture() {
      if (this.glSpriteTextureId < 0) {
         this.glSpriteTextureId = TextureUtil.generateTextureId();
         int i = this.getMipmapLevels();
         TextureUtil.prepareImage(this.glSpriteTextureId, i, this.getWidth(), this.getHeight());
         boolean flag = this.atlasTexture.isTextureBlend(this.spriteShadersType);
         if (flag) {
            TextureUtils.applyAnisotropicLevel();
         } else {
            GlStateManager._texParameter(3553, 34046, 1.0F);
            int j = i > 0 ? 9984 : 9728;
            GlStateManager._texParameter(3553, 10241, j);
            GlStateManager._texParameter(3553, 10240, 9728);
         }
      }

      TextureUtils.bindTexture(this.glSpriteTextureId);
   }

   public void deleteSpriteTexture() {
      if (this.glSpriteTextureId >= 0) {
         TextureUtil.releaseTextureId(this.glSpriteTextureId);
         this.glSpriteTextureId = -1;
      }
   }

   public float toSingleU(float u) {
      u -= this.baseU;
      float f = (float)this.sheetWidth / (float)this.getWidth();
      return u * f;
   }

   public float toSingleV(float v) {
      v -= this.baseV;
      float f = (float)this.sheetHeight / (float)this.getHeight();
      return v * f;
   }

   public NativeImage[] getMipmapImages() {
      return this.contents.byMipLevel;
   }

   public int getMipmapLevels() {
      return this.contents.byMipLevel.length - 1;
   }

   public int getOriginX() {
      return this.x;
   }

   public int getOriginY() {
      return this.y;
   }

   public float getUnInterpolatedU(float u) {
      float f = this.u1 - this.u0;
      return (u - this.u0) / f * 16.0F;
   }

   public float getUnInterpolatedV(float v) {
      float f = this.v1 - this.v0;
      return (v - this.v0) / f * 16.0F;
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public TextureAtlas getTextureAtlas() {
      return this.atlasTexture;
   }

   public void setTextureAtlas(TextureAtlas atlas) {
      this.atlasTexture = atlas;
      if (this.spriteSingle != null) {
         this.spriteSingle.setTextureAtlas(atlas);
      }

      if (this.spriteNormal != null) {
         this.spriteNormal.setTextureAtlas(atlas);
      }

      if (this.spriteSpecular != null) {
         this.spriteSpecular.setTextureAtlas(atlas);
      }

   }

   public int getWidth() {
      return this.contents.getSpriteWidth();
   }

   public int getHeight() {
      return this.contents.getSpriteHeight();
   }

   public TextureAtlasSprite makeSpriteSingle() {
      TextureAtlasSprite textureatlassprite = new TextureAtlasSprite(this);
      textureatlassprite.isSpriteSingle = true;
      return textureatlassprite;
   }

   public TextureAtlasSprite makeSpriteShaders(ShadersTextureType type, int colDef, SpriteContents.AnimatedTexture parentAnimatedTexture) {
      String s = type.getSuffix();
      ResourceLocation resourcelocation = new ResourceLocation(this.getName().getNamespace(), this.getName().getPath() + s);
      ResourceLocation resourcelocation1 = this.atlasTexture.getSpritePath(resourcelocation);
      TextureAtlasSprite textureatlassprite = null;
      Optional<Resource> optional = this.resourceManager.getResource(resourcelocation1);
      if (optional.isPresent()) {
         try {
            Resource resource = optional.get();
            Resource resource1 = this.resourceManager.getResourceOrThrow(resourcelocation1);
            NativeImage nativeimage = NativeImage.read(resource.open());
            AnimationMetadataSection animationmetadatasection = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
            FrameSize framesize = animationmetadatasection.calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight());
            if (nativeimage.getWidth() != this.getWidth()) {
               NativeImage nativeimage1 = TextureUtils.scaleImage(nativeimage, this.getWidth());
               if (nativeimage1 != nativeimage) {
                  double d0 = 1.0D * (double)this.getWidth() / (double)nativeimage.getWidth();
                  nativeimage.close();
                  nativeimage = nativeimage1;
                  framesize = new FrameSize((int)((double)framesize.width() * d0), (int)((double)framesize.height() * d0));
               }
            }

            SpriteContents spritecontents1 = new SpriteContents(resourcelocation, framesize, nativeimage, animationmetadatasection);
            textureatlassprite = new TextureAtlasSprite(this.atlasLocation, spritecontents1, this.sheetWidth, this.sheetHeight, this.x, this.y, this.atlasTexture, type);
            textureatlassprite.parentSprite = this;
         } catch (IOException ioexception) {
         }
      }

      if (textureatlassprite == null) {
         NativeImage nativeimage2 = new NativeImage(this.getWidth(), this.getHeight(), false);
         int i = TextureUtils.toAbgr(colDef);
         nativeimage2.fillRect(0, 0, nativeimage2.getWidth(), nativeimage2.getHeight(), i);
         SpriteContents spritecontents = new SpriteContents(resourcelocation, new FrameSize(this.getWidth(), this.getHeight()), nativeimage2, AnimationMetadataSection.EMPTY);
         textureatlassprite = new TextureAtlasSprite(this.atlasLocation, spritecontents, this.sheetWidth, this.sheetHeight, this.x, this.y, this.atlasTexture, type);
      }

      if (this.terrain && this.multiTexture && !this.isSpriteSingle) {
         textureatlassprite.spriteSingle = textureatlassprite.makeSpriteSingle();
      }

      return textureatlassprite;
   }

   public boolean isTerrain() {
      return this.terrain;
   }

   private void setTerrain(boolean terrainIn) {
      this.terrain = terrainIn;
      this.multiTexture = false;
      this.shaders = false;
      if (this.spriteSingle != null) {
         this.deleteSpriteTexture();
         this.spriteSingle = null;
      }

      if (this.spriteNormal != null) {
         if (this.spriteNormal.spriteSingle != null) {
            this.spriteNormal.deleteSpriteTexture();
         }

         this.spriteNormal.contents().close();
         this.spriteNormal = null;
      }

      if (this.spriteSpecular != null) {
         if (this.spriteSpecular.spriteSingle != null) {
            this.spriteSpecular.deleteSpriteTexture();
         }

         this.spriteSpecular.contents().close();
         this.spriteSpecular = null;
      }

      this.multiTexture = Config.isMultiTexture();
      this.shaders = Config.isShaders();
      if (this.terrain && this.multiTexture && !this.isSpriteSingle) {
         this.spriteSingle = this.makeSpriteSingle();
      }

      if (this.shaders && !this.isSpriteSingle) {
         if (this.spriteNormal == null && Shaders.configNormalMap) {
            this.spriteNormal = this.makeSpriteShaders(ShadersTextureType.NORMAL, -8421377, this.contents.getAnimatedTexture());
         }

         if (this.spriteSpecular == null && Shaders.configSpecularMap) {
            this.spriteSpecular = this.makeSpriteShaders(ShadersTextureType.SPECULAR, 0, this.contents.getAnimatedTexture());
         }
      }

   }

   private static boolean matchesTiming(SpriteContents.AnimatedTexture at1, SpriteContents.AnimatedTexture at2) {
      if (at1 != null && at2 != null) {
         if (at1 == at2) {
            return true;
         } else {
            boolean flag = at1.interpolateFrames;
            boolean flag1 = at2.interpolateFrames;
            if (flag != flag1) {
               return false;
            } else {
               List<SpriteContents.FrameInfo> list = at1.frames;
               List<SpriteContents.FrameInfo> list1 = at2.frames;
               if (list != null && list1 != null) {
                  if (list.size() != list1.size()) {
                     return false;
                  } else {
                     for(int i = 0; i < list.size(); ++i) {
                        SpriteContents.FrameInfo spritecontents$frameinfo = list.get(i);
                        SpriteContents.FrameInfo spritecontents$frameinfo1 = list1.get(i);
                        if (spritecontents$frameinfo == null || spritecontents$frameinfo1 == null) {
                           return false;
                        }

                        if (spritecontents$frameinfo.index != spritecontents$frameinfo1.index) {
                           return false;
                        }

                        if (spritecontents$frameinfo.time != spritecontents$frameinfo1.time) {
                           return false;
                        }
                     }

                     return true;
                  }
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   public void update(ResourceManager resourceManager) {
      this.resourceManager = resourceManager;
      this.updateIndexInMap(this.atlasTexture.getCounterIndexInMap());
      this.setTerrain(this.atlasTexture.isTerrain());
   }

   public void updateAnimation() {
      if (this.spriteContentsTicker != null) {
         this.spriteContentsTicker.tickAndUpload(this.x, this.y);
      }

   }

   public void preTick() {
      if (this.spriteContentsTicker != null) {
         if (this.spriteSingle != null && this.spriteSingle.spriteContentsTicker != null && this.spriteSingle.usesParentAnimationTime) {
            this.spriteSingle.spriteContentsTicker.frame = this.spriteContentsTicker.frame;
            this.spriteSingle.spriteContentsTicker.subFrame = this.spriteContentsTicker.subFrame;
         }

         if (this.spriteNormal != null && this.spriteNormal.spriteContentsTicker != null && this.spriteNormal.usesParentAnimationTime) {
            this.spriteNormal.spriteContentsTicker.frame = this.spriteContentsTicker.frame;
            this.spriteNormal.spriteContentsTicker.subFrame = this.spriteContentsTicker.subFrame;
         }

         if (this.spriteSpecular != null && this.spriteSpecular.spriteContentsTicker != null && this.spriteSpecular.usesParentAnimationTime) {
            this.spriteSpecular.spriteContentsTicker.frame = this.spriteContentsTicker.frame;
            this.spriteSpecular.spriteContentsTicker.subFrame = this.spriteContentsTicker.subFrame;
         }

      }
   }

   public int getPixelRGBA(int frameIndex, int x, int y) {
      if (this.contents.getAnimatedTexture() != null) {
         x += this.contents.getAnimatedTexture().getFrameX(frameIndex) * this.contents.width;
         y += this.contents.getAnimatedTexture().getFrameY(frameIndex) * this.contents.height;
      }

      return this.contents.getOriginalImage().getPixelRGBA(x, y);
   }

   public SpriteContents.Ticker getSpriteContentsTicker() {
      return this.spriteContentsTicker;
   }

   public void setSpriteContentsTicker(SpriteContents.Ticker spriteContentsTicker) {
      if (this.spriteContentsTicker != null) {
         this.spriteContentsTicker.close();
      }

      this.spriteContentsTicker = spriteContentsTicker;
      if (this.spriteContentsTicker != null && this.parentSprite != null && this.parentSprite.contents != null) {
         this.usesParentAnimationTime = matchesTiming(this.contents.getAnimatedTexture(), this.parentSprite.contents.getAnimatedTexture());
      }

   }

   public void setTicker(TextureAtlasSprite.Ticker ticker) {
      SpriteTicker spriteticker = ticker.getSpriteTicker();
      if (spriteticker instanceof SpriteContents.Ticker spritecontents$ticker) {
         this.setSpriteContentsTicker(spritecontents$ticker);
      }

   }

   public void increaseMipLevel(int mipLevelIn) {
      this.contents.increaseMipLevel(mipLevelIn);
      if (this.spriteNormal != null) {
         this.spriteNormal.increaseMipLevel(mipLevelIn);
      }

      if (this.spriteSpecular != null) {
         this.spriteSpecular.increaseMipLevel(mipLevelIn);
      }

   }

   public interface Ticker extends AutoCloseable {
      void tickAndUpload();

      void close();

      default TextureAtlasSprite getSprite() {
         return null;
      }

      default SpriteTicker getSpriteTicker() {
         return null;
      }
   }
}