package com.mojang.blaze3d.platform;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.optifine.util.VideoModeComparator;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public final class Monitor {
   private final long monitor;
   private final List<VideoMode> videoModes;
   private VideoMode currentMode;
   private int x;
   private int y;

   public Monitor(long pMonitor) {
      this.monitor = pMonitor;
      this.videoModes = Lists.newArrayList();
      this.refreshVideoModes();
   }

   public void refreshVideoModes() {
      RenderSystem.assertInInitPhase();
      this.videoModes.clear();
      GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes(this.monitor);
      GLFWVidMode glfwvidmode = GLFW.glfwGetVideoMode(this.monitor);
      VideoMode videomode = new VideoMode(glfwvidmode);
      List<VideoMode> list = new ArrayList<>();

      for(int i = buffer.limit() - 1; i >= 0; --i) {
         buffer.position(i);
         VideoMode videomode1 = new VideoMode(buffer);
         if (videomode1.getRedBits() >= 8 && videomode1.getGreenBits() >= 8 && videomode1.getBlueBits() >= 8) {
            if (videomode1.getRefreshRate() < videomode.getRefreshRate()) {
               list.add(videomode1);
            } else {
               this.videoModes.add(videomode1);
            }
         }
      }

      list.sort((new VideoModeComparator()).reversed());

      for(VideoMode videomode2 : list) {
         if (getVideoMode(this.videoModes, videomode2.getWidth(), videomode2.getHeight()) == null) {
            this.videoModes.add(videomode2);
         }
      }

      this.videoModes.sort(new VideoModeComparator());
      int[] aint = new int[1];
      int[] aint1 = new int[1];
      GLFW.glfwGetMonitorPos(this.monitor, aint, aint1);
      this.x = aint[0];
      this.y = aint1[0];
      GLFWVidMode glfwvidmode1 = GLFW.glfwGetVideoMode(this.monitor);
      this.currentMode = new VideoMode(glfwvidmode1);
   }

   public VideoMode getPreferredVidMode(Optional<VideoMode> pVideoMode) {
      RenderSystem.assertInInitPhase();
      if (pVideoMode.isPresent()) {
         VideoMode videomode = pVideoMode.get();

         for(VideoMode videomode1 : this.videoModes) {
            if (videomode1.equals(videomode)) {
               return videomode1;
            }
         }
      }

      return this.getCurrentMode();
   }

   public int getVideoModeIndex(VideoMode pVideoMode) {
      RenderSystem.assertInInitPhase();
      return this.videoModes.indexOf(pVideoMode);
   }

   public VideoMode getCurrentMode() {
      return this.currentMode;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public VideoMode getMode(int pIndex) {
      return this.videoModes.get(pIndex);
   }

   public int getModeCount() {
      return this.videoModes.size();
   }

   public long getMonitor() {
      return this.monitor;
   }

   public String toString() {
      return String.format(Locale.ROOT, "Monitor[%s %sx%s %s]", this.monitor, this.x, this.y, this.currentMode);
   }

   public static VideoMode getVideoMode(List<VideoMode> list, int width, int height) {
      for(VideoMode videomode : list) {
         if (videomode.getWidth() == width && videomode.getHeight() == height) {
            return videomode;
         }
      }

      return null;
   }
}