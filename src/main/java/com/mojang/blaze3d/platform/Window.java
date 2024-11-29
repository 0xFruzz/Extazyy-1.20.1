package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Locale.Category;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.optifine.Config;
import net.optifine.reflect.Reflector;
import net.optifine.util.TextureUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public final class Window implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
   private final WindowEventHandler eventHandler;
   private final ScreenManager screenManager;
   private final long window;
   private int windowedX;
   private int windowedY;
   private int windowedWidth;
   private int windowedHeight;
   private Optional<VideoMode> preferredFullscreenVideoMode;
   private boolean fullscreen;
   private boolean actuallyFullscreen;
   private int x;
   private int y;
   private int width;
   private int height;
   private int framebufferWidth;
   private int framebufferHeight;
   private int guiScaledWidth;
   private int guiScaledHeight;
   private double guiScale;
   private String errorSection = "";
   private boolean dirty;
   private int framerateLimit;
   private boolean vsync;
   private boolean closed;

   public Window(WindowEventHandler pEventHandler, ScreenManager pScreenManager, DisplayData pDisplayData, @Nullable String pPreferredFullscreenVideoMode, String pTitle) {
      RenderSystem.assertInInitPhase();
      this.screenManager = pScreenManager;
      this.setBootErrorCallback();
      this.setErrorSection("Pre startup");
      this.eventHandler = pEventHandler;
      Optional<VideoMode> optional = VideoMode.read(pPreferredFullscreenVideoMode);
      if (optional.isPresent()) {
         this.preferredFullscreenVideoMode = optional;
      } else if (pDisplayData.fullscreenWidth.isPresent() && pDisplayData.fullscreenHeight.isPresent()) {
         this.preferredFullscreenVideoMode = Optional.of(new VideoMode(pDisplayData.fullscreenWidth.getAsInt(), pDisplayData.fullscreenHeight.getAsInt(), 8, 8, 8, 60));
      } else {
         this.preferredFullscreenVideoMode = Optional.empty();
      }

      this.actuallyFullscreen = this.fullscreen = pDisplayData.isFullscreen;
      Monitor monitor = pScreenManager.getMonitor(GLFW.glfwGetPrimaryMonitor());
      this.windowedWidth = this.width = pDisplayData.width > 0 ? pDisplayData.width : 1;
      this.windowedHeight = this.height = pDisplayData.height > 0 ? pDisplayData.height : 1;
      GLFW.glfwDefaultWindowHints();
      if (Config.isAntialiasing()) {
         GLFW.glfwWindowHint(135181, Config.getAntialiasingLevel());
      }

      GLFW.glfwWindowHint(139265, 196609);
      GLFW.glfwWindowHint(139275, 221185);
      GLFW.glfwWindowHint(139266, 3);
      GLFW.glfwWindowHint(139267, 2);
      GLFW.glfwWindowHint(139272, 204801);
      GLFW.glfwWindowHint(139270, 1);
      long i = 0L;
      if (Reflector.ImmediateWindowHandler_setupMinecraftWindow.exists()) {
         i = Reflector.ImmediateWindowHandler_setupMinecraftWindow.callLong((IntSupplier)() -> {
            return this.width;
         }, (IntSupplier)() -> {
            return this.height;
         }, (Supplier<String>)() -> {
            return pTitle;
         }, (LongSupplier)() -> {
            return this.fullscreen && monitor != null ? monitor.getMonitor() : 0L;
         });
         if (Config.isAntialiasing()) {
            GLFW.glfwDestroyWindow(i);
            i = 0L;
         }
      }

      if (i != 0L) {
         this.window = i;
      } else {
         this.window = GLFW.glfwCreateWindow(this.width, this.height, pTitle, this.fullscreen && monitor != null ? monitor.getMonitor() : 0L, 0L);
      }

      if (i == 0L || !Reflector.ImmediateWindowHandler_positionWindow.callBoolean(Optional.ofNullable(monitor), (IntConsumer)(w) -> {
         this.width = this.windowedWidth = w;
      }, (IntConsumer)(h) -> {
         this.height = this.windowedHeight = h;
      }, (IntConsumer)(x) -> {
         this.x = this.windowedX = x;
      }, (IntConsumer)(y) -> {
         this.y = this.windowedY = y;
      })) {
         if (monitor != null) {
            VideoMode videomode = monitor.getPreferredVidMode(this.fullscreen ? this.preferredFullscreenVideoMode : Optional.empty());
            this.windowedX = this.x = monitor.getX() + videomode.getWidth() / 2 - this.width / 2;
            this.windowedY = this.y = monitor.getY() + videomode.getHeight() / 2 - this.height / 2;
         } else {
            int[] aint1 = new int[1];
            int[] aint = new int[1];
            GLFW.glfwGetWindowPos(this.window, aint1, aint);
            this.windowedX = this.x = aint1[0];
            this.windowedY = this.y = aint[0];
         }
      }

      GLFW.glfwMakeContextCurrent(this.window);
      Locale locale = Locale.getDefault(Category.FORMAT);
      Locale.setDefault(Category.FORMAT, Locale.ROOT);
      GL.createCapabilities();
      Locale.setDefault(Category.FORMAT, locale);
      this.setMode();
      this.refreshFramebufferSize();
      GLFW.glfwSetFramebufferSizeCallback(this.window, this::onFramebufferResize);
      GLFW.glfwSetWindowPosCallback(this.window, this::onMove);
      GLFW.glfwSetWindowSizeCallback(this.window, this::onResize);
      GLFW.glfwSetWindowFocusCallback(this.window, this::onFocus);
      GLFW.glfwSetCursorEnterCallback(this.window, this::onEnter);
   }

   public int getRefreshRate() {
      RenderSystem.assertOnRenderThread();
      return GLX._getRefreshRate(this);
   }

   public boolean shouldClose() {
      return GLX._shouldClose(this);
   }

   public static void checkGlfwError(BiConsumer<Integer, String> pErrorConsumer) {
      RenderSystem.assertInInitPhase();

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
         int i = GLFW.glfwGetError(pointerbuffer);
         if (i != 0) {
            long j = pointerbuffer.get();
            String s = j == 0L ? "" : MemoryUtil.memUTF8(j);
            pErrorConsumer.accept(i, s);
         }
      }

   }

   public void setIcon(PackResources pPackResources, IconSet pIconSet) throws IOException {
      RenderSystem.assertInInitPhase();
      if (Minecraft.ON_OSX) {
         MacosUtil.loadIcon(pIconSet.getMacIcon(pPackResources));
      } else {
         System.out.println("ICONS " + pPackResources);
         List<IoSupplier<InputStream>> list = pIconSet.getStandardIcons(pPackResources);

         List<ByteBuffer> list1 = new ArrayList<>(list.size());

         try (MemoryStack memorystack = MemoryStack.stackPush()) {
            GLFWImage.Buffer buffer = GLFWImage.malloc(list.size(), memorystack);

            for(int i = 0; i < list.size(); ++i) {
               try (NativeImage nativeimage = NativeImage.read(list.get(i).get())) {
                  ByteBuffer bytebuffer = MemoryUtil.memAlloc(nativeimage.getWidth() * nativeimage.getHeight() * 4);
                  list1.add(bytebuffer);
                  bytebuffer.asIntBuffer().put(nativeimage.getPixelsRGBA());
                  buffer.position(i);
                  buffer.width(nativeimage.getWidth());
                  buffer.height(nativeimage.getHeight());
                  buffer.pixels(bytebuffer);
               }
            }

            GLFW.glfwSetWindowIcon(this.window, buffer.position(0));
         } finally {
            list1.forEach(MemoryUtil::memFree);
         }
      }

   }

   public void setErrorSection(String pErrorSection) {
      this.errorSection = pErrorSection;
      if (pErrorSection.equals("Startup")) {
         TextureUtils.registerTickableTextures();
      }

   }

   private void setBootErrorCallback() {
      RenderSystem.assertInInitPhase();
      GLFW.glfwSetErrorCallback(Window::bootCrash);
   }

   private static void bootCrash(int p_85413_, long p_85414_) {
      RenderSystem.assertInInitPhase();
      String s = "GLFW error " + p_85413_ + ": " + MemoryUtil.memUTF8(p_85414_);
      TinyFileDialogs.tinyfd_messageBox("Minecraft", s + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).", "ok", "error", false);
      throw new Window.WindowInitFailed(s);
   }

   public void defaultErrorCallback(int p_85383_, long p_85384_) {
      RenderSystem.assertOnRenderThread();
      String s = MemoryUtil.memUTF8(p_85384_);
      LOGGER.error("########## GL ERROR ##########");
      LOGGER.error("@ {}", (Object)this.errorSection);
      LOGGER.error("{}: {}", p_85383_, s);
   }

   public void setDefaultErrorCallback() {
      GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(this.defaultErrorCallback);
      if (glfwerrorcallback != null) {
         glfwerrorcallback.free();
      }

      TextureUtils.registerResourceListener();
   }

   public void updateVsync(boolean pVsync) {
      RenderSystem.assertOnRenderThreadOrInit();
      this.vsync = pVsync;
      GLFW.glfwSwapInterval(pVsync ? 1 : 0);
   }

   public void close() {
      RenderSystem.assertOnRenderThread();
      this.closed = true;
      Callbacks.glfwFreeCallbacks(this.window);
      this.defaultErrorCallback.close();
      GLFW.glfwDestroyWindow(this.window);
      GLFW.glfwTerminate();
   }

   private void onMove(long p_85389_, int p_85390_, int p_85391_) {
      this.x = p_85390_;
      this.y = p_85391_;
   }

   private void onFramebufferResize(long p_85416_, int p_85417_, int p_85418_) {
      if (p_85416_ == this.window) {
         int i = this.getWidth();
         int j = this.getHeight();
         if (p_85417_ != 0 && p_85418_ != 0) {
            this.framebufferWidth = p_85417_;
            this.framebufferHeight = p_85418_;
            if (this.getWidth() != i || this.getHeight() != j) {
               this.eventHandler.resizeDisplay();
            }
         }
      }

   }

   private void refreshFramebufferSize() {
      RenderSystem.assertInInitPhase();
      int[] aint = new int[1];
      int[] aint1 = new int[1];
      GLFW.glfwGetFramebufferSize(this.window, aint, aint1);
      this.framebufferWidth = aint[0] > 0 ? aint[0] : 1;
      this.framebufferHeight = aint1[0] > 0 ? aint1[0] : 1;
      if (this.framebufferHeight == 0 || this.framebufferWidth == 0) {
         Reflector.ImmediateWindowHandler_updateFBSize.call((IntConsumer)(w) -> {
            this.framebufferWidth = w;
         }, (IntConsumer)(h) -> {
            this.framebufferHeight = h;
         });
      }

   }

   private void onResize(long p_85428_, int p_85429_, int p_85430_) {
      this.width = p_85429_;
      this.height = p_85430_;
   }

   private void onFocus(long p_85393_, boolean p_85394_) {
      if (p_85393_ == this.window) {
         this.eventHandler.setWindowActive(p_85394_);
      }

   }

   private void onEnter(long p_85420_, boolean p_85421_) {
      if (p_85421_) {
         this.eventHandler.cursorEntered();
      }

   }

   public void setFramerateLimit(int pLimit) {
      this.framerateLimit = pLimit;
   }

   public int getFramerateLimit() {
      if (Minecraft.getInstance().options.enableVsync().get()) {
         return 260;
      } else {
         return this.framerateLimit <= 0 ? 260 : this.framerateLimit;
      }
   }

   public void updateDisplay() {
      RenderSystem.flipFrame(this.window);
      if (this.fullscreen != this.actuallyFullscreen) {
         this.actuallyFullscreen = this.fullscreen;
         this.updateFullscreen(this.vsync);
      }

   }

   public Optional<VideoMode> getPreferredFullscreenVideoMode() {
      return this.preferredFullscreenVideoMode;
   }

   public void setPreferredFullscreenVideoMode(Optional<VideoMode> pPreferredFullscreenVideoMode) {
      boolean flag = !pPreferredFullscreenVideoMode.equals(this.preferredFullscreenVideoMode);
      this.preferredFullscreenVideoMode = pPreferredFullscreenVideoMode;
      if (flag) {
         this.dirty = true;
      }

   }

   public void changeFullscreenVideoMode() {
      if (this.fullscreen && this.dirty) {
         this.dirty = false;
         this.setMode();
         this.eventHandler.resizeDisplay();
      }

   }

   private void setMode() {
      RenderSystem.assertInInitPhase();
      boolean flag = GLFW.glfwGetWindowMonitor(this.window) != 0L;
      if (this.fullscreen) {
         Monitor monitor = this.screenManager.findBestMonitor(this);
         if (monitor == null) {
            LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
            this.fullscreen = false;
         } else {
            if (Minecraft.ON_OSX) {
               MacosUtil.toggleFullscreen(this.window);
            }

            VideoMode videomode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);
            if (!flag) {
               this.windowedX = this.x;
               this.windowedY = this.y;
               this.windowedWidth = this.width;
               this.windowedHeight = this.height;
            }

            this.x = 0;
            this.y = 0;
            this.width = videomode.getWidth();
            this.height = videomode.getHeight();
            GLFW.glfwSetWindowMonitor(this.window, monitor.getMonitor(), this.x, this.y, this.width, this.height, videomode.getRefreshRate());
         }
      } else {
         this.x = this.windowedX;
         this.y = this.windowedY;
         this.width = this.windowedWidth;
         this.height = this.windowedHeight;
         GLFW.glfwSetWindowMonitor(this.window, 0L, this.x, this.y, this.width, this.height, -1);
      }

   }

   public void toggleFullScreen() {
      this.fullscreen = !this.fullscreen;
   }

   public void setWindowed(int pWindowedWidth, int pWindowedHeight) {
      this.windowedWidth = pWindowedWidth;
      this.windowedHeight = pWindowedHeight;
      this.fullscreen = false;
      this.setMode();
   }

   private void updateFullscreen(boolean pVsyncEnabled) {
      RenderSystem.assertOnRenderThread();

      try {
         this.setMode();
         this.eventHandler.resizeDisplay();
         this.updateVsync(pVsyncEnabled);
         this.updateDisplay();
      } catch (Exception exception) {
         LOGGER.error("Couldn't toggle fullscreen", (Throwable)exception);
      }

   }

   public int calculateScale(int pGuiScale, boolean pForceUnicode) {
      int i;
      for(i = 1; i != pGuiScale && i < this.framebufferWidth && i < this.framebufferHeight && this.framebufferWidth / (i + 1) >= 320 && this.framebufferHeight / (i + 1) >= 240; ++i) {
      }

      if (pForceUnicode && i % 2 != 0) {
         ++i;
      }

      return i;
   }

   public void setGuiScale(double pScaleFactor) {
      this.guiScale = pScaleFactor;
      int i = (int)((double)this.framebufferWidth / pScaleFactor);
      this.guiScaledWidth = (double)this.framebufferWidth / pScaleFactor > (double)i ? i + 1 : i;
      int j = (int)((double)this.framebufferHeight / pScaleFactor);
      this.guiScaledHeight = (double)this.framebufferHeight / pScaleFactor > (double)j ? j + 1 : j;
   }

   public void setTitle(String pTitle) {
      GLFW.glfwSetWindowTitle(this.window, pTitle);
   }

   public long getWindow() {
      return this.window;
   }

   public boolean isFullscreen() {
      return this.fullscreen;
   }

   public int getWidth() {
      return this.framebufferWidth;
   }

   public int getHeight() {
      return this.framebufferHeight;
   }

   public void setWidth(int pFramebufferWidth) {
      this.framebufferWidth = pFramebufferWidth;
   }

   public void setHeight(int pFramebufferHeight) {
      this.framebufferHeight = pFramebufferHeight;
   }

   public int getScreenWidth() {
      return this.width;
   }

   public int getScreenHeight() {
      return this.height;
   }

   public int getGuiScaledWidth() {
      return this.guiScaledWidth;
   }

   public int getGuiScaledHeight() {
      return this.guiScaledHeight;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public double getGuiScale() {
      return this.guiScale;
   }

   @Nullable
   public Monitor findBestMonitor() {
      return this.screenManager.findBestMonitor(this);
   }

   public void updateRawMouseInput(boolean pEnableRawMouseMotion) {
      InputConstants.updateRawMouseInput(this.window, pEnableRawMouseMotion);
   }

   public void resizeFramebuffer(int width, int height) {
      this.onFramebufferResize(this.window, width, height);
   }

   public boolean isClosed() {
      return this.closed;
   }

   public static class WindowInitFailed extends SilentInitException {
      WindowInitFailed(String p_85455_) {
         super(p_85455_);
      }
   }
}