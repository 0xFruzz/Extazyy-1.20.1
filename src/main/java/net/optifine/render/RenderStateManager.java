package net.optifine.render;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class RenderStateManager {
   private static boolean cacheEnabled;
   private static final RenderStateShard[] PENDING_CLEAR_STATES = new RenderStateShard[RenderType.getCountRenderStates()];

   public static void setupRenderStates(List<RenderStateShard> renderStates) {
      if (cacheEnabled) {
         setupCached(renderStates);
      } else {
         for(int i = 0; i < renderStates.size(); ++i) {
            RenderStateShard renderstateshard = renderStates.get(i);
            renderstateshard.setupRenderState();
         }

      }
   }

   public static void clearRenderStates(List<RenderStateShard> renderStates) {
      if (cacheEnabled) {
         clearCached(renderStates);
      } else {
         for(int i = 0; i < renderStates.size(); ++i) {
            RenderStateShard renderstateshard = renderStates.get(i);
            renderstateshard.clearRenderState();
         }

      }
   }

   private static void setupCached(List<RenderStateShard> renderStates) {
      for(int i = 0; i < renderStates.size(); ++i) {
         RenderStateShard renderstateshard = renderStates.get(i);
         setupCached(renderstateshard, i);
      }

   }

   private static void clearCached(List<RenderStateShard> renderStates) {
      for(int i = 0; i < renderStates.size(); ++i) {
         RenderStateShard renderstateshard = renderStates.get(i);
         clearCached(renderstateshard, i);
      }

   }

   private static void setupCached(RenderStateShard state, int index) {
      RenderStateShard renderstateshard = PENDING_CLEAR_STATES[index];
      if (renderstateshard != null) {
         if (state == renderstateshard) {
            PENDING_CLEAR_STATES[index] = null;
            return;
         }

         renderstateshard.clearRenderState();
         PENDING_CLEAR_STATES[index] = null;
      }

      state.setupRenderState();
   }

   private static void clearCached(RenderStateShard state, int index) {
      RenderStateShard renderstateshard = PENDING_CLEAR_STATES[index];
      if (renderstateshard != null) {
         renderstateshard.clearRenderState();
      }

      PENDING_CLEAR_STATES[index] = state;
   }

   public static void enableCache() {
      if (!cacheEnabled) {
         cacheEnabled = true;
         Arrays.fill(PENDING_CLEAR_STATES, (Object)null);
      }
   }

   public static void flushCache() {
      if (cacheEnabled) {
         disableCache();
         enableCache();
      }
   }

   public static void disableCache() {
      if (cacheEnabled) {
         cacheEnabled = false;

         for(int i = 0; i < PENDING_CLEAR_STATES.length; ++i) {
            RenderStateShard renderstateshard = PENDING_CLEAR_STATES[i];
            if (renderstateshard != null) {
               renderstateshard.clearRenderState();
            }
         }

         Arrays.fill(PENDING_CLEAR_STATES, (Object)null);
      }
   }
}