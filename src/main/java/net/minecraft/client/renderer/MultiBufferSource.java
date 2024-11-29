package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.optifine.Config;
import net.optifine.render.IBufferSourceListener;
import net.optifine.render.VertexBuilderDummy;
import net.optifine.util.TextureUtils;

public interface MultiBufferSource {
   static MultiBufferSource.BufferSource immediate(BufferBuilder pBuilder) {
      return immediateWithBuffers(ImmutableMap.of(), pBuilder);
   }

   static MultiBufferSource.BufferSource immediateWithBuffers(Map<RenderType, BufferBuilder> pMapBuilders, BufferBuilder pBuilder) {
      return new MultiBufferSource.BufferSource(pBuilder, pMapBuilders);
   }

   VertexConsumer getBuffer(RenderType pRenderType);

   default void flushRenderBuffers() {
   }

   default void flushCache() {
   }

   public static class BufferSource implements MultiBufferSource {
      protected final BufferBuilder builder;
      protected final Map<RenderType, BufferBuilder> fixedBuffers;
      protected RenderType lastState = null;
      protected final Set<BufferBuilder> startedBuffers = Sets.newIdentityHashSet();
      private final VertexConsumer DUMMY_BUFFER = new VertexBuilderDummy(this);
      private List<IBufferSourceListener> listeners = new ArrayList<>(4);
      private int maxCachedBuffers = 0;
      private Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> cachedBuffers = new Object2ObjectLinkedOpenHashMap<>();
      private Deque<BufferBuilder> freeBufferBuilders = new ArrayDeque<>();

      protected BufferSource(BufferBuilder pBuilder, Map<RenderType, BufferBuilder> pFixedBuffers) {
         this.builder = pBuilder;
         this.fixedBuffers = new Object2ObjectLinkedOpenHashMap<>(pFixedBuffers);
         this.builder.setRenderTypeBuffer(this);

         for(BufferBuilder bufferbuilder : pFixedBuffers.values()) {
            bufferbuilder.setRenderTypeBuffer(this);
         }

      }

      public VertexConsumer getBuffer(RenderType pRenderType) {
         this.addCachedBuffer(pRenderType);
         BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
         if (!Objects.equals(this.lastState, pRenderType) || !pRenderType.canConsolidateConsecutiveGeometry()) {
            if (this.lastState != null) {
               RenderType rendertype = this.lastState;
               if (!this.fixedBuffers.containsKey(rendertype)) {
                  this.endBatch(rendertype);
               }
            }

            if (this.startedBuffers.add(bufferbuilder)) {
               bufferbuilder.setRenderType(pRenderType);
               bufferbuilder.begin(pRenderType.mode(), pRenderType.format());
            }

            this.lastState = pRenderType;
         }

         return (VertexConsumer)(pRenderType.getTextureLocation() == TextureUtils.LOCATION_TEXTURE_EMPTY ? this.DUMMY_BUFFER : bufferbuilder);
      }

      private BufferBuilder getBuilderRaw(RenderType pRenderType) {
         return this.fixedBuffers.getOrDefault(pRenderType, this.builder);
      }

      public void endLastBatch() {
         if (this.lastState != null) {
            RenderType rendertype = this.lastState;
            if (!this.fixedBuffers.containsKey(rendertype)) {
               this.endBatch(rendertype);
            }

            this.lastState = null;
         }

      }

      public void endBatch() {
         if (!this.startedBuffers.isEmpty()) {
            if (this.lastState != null) {
               VertexConsumer vertexconsumer = this.getBuffer(this.lastState);
               if (vertexconsumer == this.builder) {
                  this.endBatch(this.lastState);
               }
            }

            if (!this.startedBuffers.isEmpty()) {
               for(RenderType rendertype : this.fixedBuffers.keySet()) {
                  this.endBatch(rendertype);
                  if (this.startedBuffers.isEmpty()) {
                     break;
                  }
               }

            }
         }
      }

      public void endBatch(RenderType pRenderType) {
         BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
         boolean flag = Objects.equals(this.lastState, pRenderType);
         if ((flag || bufferbuilder != this.builder) && this.startedBuffers.remove(bufferbuilder)) {
            this.fireFinish(pRenderType, bufferbuilder);
            pRenderType.end(bufferbuilder, RenderSystem.getVertexSorting());
            if (flag) {
               this.lastState = null;
            }
         }

      }

      public VertexConsumer getBuffer(ResourceLocation textureLocation, VertexConsumer def) {
         if (!(this.lastState instanceof RenderType.CompositeRenderType)) {
            return def;
         } else {
            textureLocation = RenderType.getCustomTexture(textureLocation);
            RenderType.CompositeRenderType rendertype$compositerendertype = (RenderType.CompositeRenderType)this.lastState;
            RenderType.CompositeRenderType rendertype$compositerendertype1 = rendertype$compositerendertype.getTextured(textureLocation);
            return this.getBuffer(rendertype$compositerendertype1);
         }
      }

      public RenderType getLastRenderType() {
         return this.lastState;
      }

      public void flushRenderBuffers() {
         RenderType rendertype = this.lastState;
         this.endBatch();
         if (rendertype != null) {
            this.getBuffer(rendertype);
         }

      }

      public void addListener(IBufferSourceListener bsl) {
         this.listeners.add(bsl);
      }

      public boolean removeListener(IBufferSourceListener bsl) {
         return this.listeners.remove(bsl);
      }

      private void fireFinish(RenderType renderTypeIn, BufferBuilder bufferIn) {
         for(int i = 0; i < this.listeners.size(); ++i) {
            IBufferSourceListener ibuffersourcelistener = this.listeners.get(i);
            ibuffersourcelistener.finish(renderTypeIn, bufferIn);
         }

      }

      public VertexConsumer getDummyBuffer() {
         return this.DUMMY_BUFFER;
      }

      public void enableCache() {
      }

      public void flushCache() {
         int i = this.maxCachedBuffers;
         this.setMaxCachedBuffers(0);
         this.setMaxCachedBuffers(i);
      }

      public void disableCache() {
         this.setMaxCachedBuffers(0);
      }

      private void setMaxCachedBuffers(int maxCachedBuffers) {
         this.maxCachedBuffers = Math.max(maxCachedBuffers, 0);
         this.trimCachedBuffers();
      }

      private void addCachedBuffer(RenderType rt) {
         if (this.maxCachedBuffers > 0) {
            this.cachedBuffers.getAndMoveToLast(rt);
            if (!this.fixedBuffers.containsKey(rt)) {
               if (this.shouldCache(rt)) {
                  this.trimCachedBuffers();
                  BufferBuilder bufferbuilder = this.freeBufferBuilders.pollLast();
                  if (bufferbuilder == null) {
                     bufferbuilder = new BufferBuilder(256);
                  }

                  this.fixedBuffers.put(rt, bufferbuilder);
                  this.cachedBuffers.put(rt, bufferbuilder);
               }
            }
         }
      }

      private boolean shouldCache(RenderType rt) {
         ResourceLocation resourcelocation = rt.getTextureLocation();
         if (resourcelocation == null) {
            return false;
         } else if (!rt.canConsolidateConsecutiveGeometry()) {
            return false;
         } else {
            String s = resourcelocation.getPath();
            if (s.startsWith("skins/")) {
               return false;
            } else if (s.startsWith("capes/")) {
               return false;
            } else if (s.startsWith("capeof/")) {
               return false;
            } else if (s.startsWith("textures/entity/horse/")) {
               return false;
            } else if (s.startsWith("textures/entity/villager/")) {
               return false;
            } else {
               return !s.startsWith("textures/entity/warden/");
            }
         }
      }

      private void trimCachedBuffers() {
         while(this.cachedBuffers.size() > this.maxCachedBuffers) {
            RenderType rendertype = this.cachedBuffers.firstKey();
            if (rendertype == this.lastState) {
               return;
            }

            this.removeCachedBuffer(rendertype);
         }

      }

      private void removeCachedBuffer(RenderType rt) {
         BufferBuilder bufferbuilder = this.fixedBuffers.get(rt);
         if (bufferbuilder != null) {
            if (this.startedBuffers.contains(bufferbuilder)) {
               this.endBatch(rt);
            }

            if (bufferbuilder.building()) {
               Config.dbg("" + bufferbuilder);
            }

            this.freeBufferBuilders.add(bufferbuilder);
         }

         this.fixedBuffers.remove(rt);
         this.cachedBuffers.remove(rt);
      }
   }
}