package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.optifine.Config;
import net.optifine.SmartAnimations;
import net.optifine.render.GlAlphaState;
import net.optifine.render.GlBlendState;
import net.optifine.render.GlCullState;
import net.optifine.shaders.Shaders;
import net.optifine.util.LockCounter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@DontObfuscate
public class GlStateManager {
   private static final boolean ON_LINUX = Util.getPlatform() == Util.OS.LINUX;
   public static final int TEXTURE_COUNT = 12;
   private static final GlStateManager.BlendState BLEND = new GlStateManager.BlendState();
   private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
   private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
   private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
   private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
   private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
   private static final GlStateManager.ScissorState SCISSOR = new GlStateManager.ScissorState();
   private static int activeTexture;
   private static final GlStateManager.TextureState[] TEXTURES = IntStream.range(0, 32).mapToObj((p_157120_) -> {
      return new GlStateManager.TextureState();
   }).toArray((p_157122_) -> {
      return new GlStateManager.TextureState[p_157122_];
   });
   private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();
   private static boolean alphaTest = false;
   private static int alphaTestFunc = 519;
   private static float alphaTestRef = 0.0F;
   private static LockCounter alphaLock = new LockCounter();
   private static GlAlphaState alphaLockState = new GlAlphaState();
   private static LockCounter blendLock = new LockCounter();
   private static GlBlendState blendLockState = new GlBlendState();
   private static LockCounter cullLock = new LockCounter();
   private static GlCullState cullLockState = new GlCullState();
   public static boolean vboRegions;
   public static int GL_COPY_READ_BUFFER;
   public static int GL_COPY_WRITE_BUFFER;
   public static int GL_ARRAY_BUFFER;
   public static int GL_STATIC_DRAW;
   public static final int GL_QUADS = 7;
   public static final int GL_TRIANGLES = 4;
   public static final int GL_TEXTURE0 = 33984;
   public static final int GL_TEXTURE1 = 33985;
   public static final int GL_TEXTURE2 = 33986;
   private static int framebufferRead;
   private static int framebufferDraw;
   private static final int[] IMAGE_TEXTURES = new int[8];
   private static int glProgram = 0;
   public static float lastBrightnessX = 0.0F;
   public static float lastBrightnessY = 0.0F;

   public static void disableAlphaTest() {
      if (alphaLock.isLocked()) {
         alphaLockState.setDisabled();
      } else {
         alphaTest = false;
         applyAlphaTest();
      }
   }

   public static void enableAlphaTest() {
      if (alphaLock.isLocked()) {
         alphaLockState.setEnabled();
      } else {
         alphaTest = true;
         applyAlphaTest();
      }
   }

   public static void alphaFunc(int func, float ref) {
      if (alphaLock.isLocked()) {
         alphaLockState.setFuncRef(func, ref);
      } else {
         alphaTestFunc = func;
         alphaTestRef = ref;
         applyAlphaTest();
      }
   }

   public static void applyAlphaTest() {
      if (Config.isShaders()) {
         if (alphaTest && alphaTestFunc == 516) {
            Shaders.uniform_alphaTestRef.setValue(alphaTestRef);
         } else {
            Shaders.uniform_alphaTestRef.setValue(0.0F);
         }
      }

   }

   public static void _disableScissorTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      SCISSOR.mode.disable();
   }

   public static void _enableScissorTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      SCISSOR.mode.enable();
   }

   public static void _scissorBox(int pX, int pY, int pWidth, int pHeight) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL20.glScissor(pX, pY, pWidth, pHeight);
   }

   public static void _disableDepthTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      DEPTH.mode.disable();
   }

   public static void _enableDepthTest() {
      RenderSystem.assertOnRenderThreadOrInit();
      DEPTH.mode.enable();
   }

   public static void _depthFunc(int pDepthFunc) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (pDepthFunc != DEPTH.func) {
         DEPTH.func = pDepthFunc;
         GL11.glDepthFunc(pDepthFunc);
      }

   }

   public static void _depthMask(boolean pFlag) {
      RenderSystem.assertOnRenderThread();
      if (pFlag != DEPTH.mask) {
         DEPTH.mask = pFlag;
         GL11.glDepthMask(pFlag);
      }

   }

   public static void _disableBlend() {
      RenderSystem.assertOnRenderThread();
      if (blendLock.isLocked()) {
         blendLockState.setDisabled();
      } else {
         BLEND.mode.disable();
      }
   }

   public static void _enableBlend() {
      RenderSystem.assertOnRenderThread();
      if (blendLock.isLocked()) {
         blendLockState.setEnabled();
      } else {
         BLEND.mode.enable();
      }
   }

   public static void _blendFunc(int pSourceFactor, int pDestFactor) {
      RenderSystem.assertOnRenderThread();
      if (blendLock.isLocked()) {
         blendLockState.setFactors(pSourceFactor, pDestFactor);
      } else {
         if (pSourceFactor != BLEND.srcRgb || pDestFactor != BLEND.dstRgb || pSourceFactor != BLEND.srcAlpha || pDestFactor != BLEND.dstAlpha) {
            BLEND.srcRgb = pSourceFactor;
            BLEND.dstRgb = pDestFactor;
            BLEND.srcAlpha = pSourceFactor;
            BLEND.dstAlpha = pDestFactor;
            if (Config.isShaders()) {
               Shaders.uniform_blendFunc.setValue(pSourceFactor, pDestFactor, pSourceFactor, pDestFactor);
            }

            GL11.glBlendFunc(pSourceFactor, pDestFactor);
         }

      }
   }

   public static void _blendFuncSeparate(int pSrcFactor, int pDstFactor, int pSrcFactorAlpha, int pDstFactorAlpha) {
      RenderSystem.assertOnRenderThread();
      if (blendLock.isLocked()) {
         blendLockState.setFactors(pSrcFactor, pDstFactor, pSrcFactorAlpha, pDstFactorAlpha);
      } else {
         if (pSrcFactor != BLEND.srcRgb || pDstFactor != BLEND.dstRgb || pSrcFactorAlpha != BLEND.srcAlpha || pDstFactorAlpha != BLEND.dstAlpha) {
            BLEND.srcRgb = pSrcFactor;
            BLEND.dstRgb = pDstFactor;
            BLEND.srcAlpha = pSrcFactorAlpha;
            BLEND.dstAlpha = pDstFactorAlpha;
            if (Config.isShaders()) {
               Shaders.uniform_blendFunc.setValue(pSrcFactor, pDstFactor, pSrcFactorAlpha, pDstFactorAlpha);
            }

            glBlendFuncSeparate(pSrcFactor, pDstFactor, pSrcFactorAlpha, pDstFactorAlpha);
         }

      }
   }

   public static void _blendEquation(int pMode) {
      RenderSystem.assertOnRenderThread();
      GL14.glBlendEquation(pMode);
   }

   public static void init(GLCapabilities glCapabilities) {
      RenderSystem.assertOnGameThreadOrInit();
      Config.initDisplay();
      GL_COPY_READ_BUFFER = 36662;
      GL_COPY_WRITE_BUFFER = 36663;
      GL_ARRAY_BUFFER = 34962;
      GL_STATIC_DRAW = 35044;
      boolean flag = true;
      boolean flag1 = true;
      vboRegions = flag && flag1;
      if (!vboRegions) {
         List<String> list = new ArrayList<>();
         if (!flag) {
            list.add("OpenGL 1.3, ARB_copy_buffer");
         }

         if (!flag1) {
            list.add("OpenGL 1.4");
         }

         String s = "VboRegions not supported, missing: " + Config.listToString(list);
         Config.dbg(s);
      }

   }

   public static int glGetProgrami(int pProgram, int pPname) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetProgrami(pProgram, pPname);
   }

   public static void glAttachShader(int pProgram, int pShader) {
      RenderSystem.assertOnRenderThread();
      GL20.glAttachShader(pProgram, pShader);
   }

   public static void glDeleteShader(int pShader) {
      RenderSystem.assertOnRenderThread();
      GL20.glDeleteShader(pShader);
   }

   public static int glCreateShader(int pType) {
      RenderSystem.assertOnRenderThread();
      return GL20.glCreateShader(pType);
   }

   public static void glShaderSource(int pShader, List<String> pShaderData) {
      RenderSystem.assertOnRenderThread();
      StringBuilder stringbuilder = new StringBuilder();

      for(String s : pShaderData) {
         stringbuilder.append(s);
      }

      byte[] abyte = stringbuilder.toString().getBytes(Charsets.UTF_8);
      ByteBuffer bytebuffer = MemoryUtil.memAlloc(abyte.length + 1);
      bytebuffer.put(abyte);
      bytebuffer.put((byte)0);
      bytebuffer.flip();

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         PointerBuffer pointerbuffer = memorystack.mallocPointer(1);
         pointerbuffer.put(bytebuffer);
         GL20C.nglShaderSource(pShader, 1, pointerbuffer.address0(), 0L);
      } finally {
         MemoryUtil.memFree(bytebuffer);
      }

   }

   public static void glCompileShader(int pShader) {
      RenderSystem.assertOnRenderThread();
      GL20.glCompileShader(pShader);
   }

   public static int glGetShaderi(int pShader, int pPname) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetShaderi(pShader, pPname);
   }

   public static void _glUseProgram(int pProgram) {
      RenderSystem.assertOnRenderThread();
      if (glProgram != pProgram) {
         GL20.glUseProgram(pProgram);
         glProgram = pProgram;
      }
   }

   public static int glCreateProgram() {
      RenderSystem.assertOnRenderThread();
      return GL20.glCreateProgram();
   }

   public static void glDeleteProgram(int pProgram) {
      RenderSystem.assertOnRenderThread();
      GL20.glDeleteProgram(pProgram);
   }

   public static void glLinkProgram(int pProgram) {
      RenderSystem.assertOnRenderThread();
      GL20.glLinkProgram(pProgram);
   }

   public static int _glGetUniformLocation(int pProgram, CharSequence pName) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetUniformLocation(pProgram, pName);
   }

   public static void _glUniform1(int pLocation, IntBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform1iv(pLocation, pValue);
   }

   public static void _glUniform1i(int pLocation, int pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform1i(pLocation, pValue);
   }

   public static void _glUniform1(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform1fv(pLocation, pValue);
   }

   public static void _glUniform2(int pLocation, IntBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform2iv(pLocation, pValue);
   }

   public static void _glUniform2(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform2fv(pLocation, pValue);
   }

   public static void _glUniform3(int pLocation, IntBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform3iv(pLocation, pValue);
   }

   public static void _glUniform3(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform3fv(pLocation, pValue);
   }

   public static void _glUniform4(int pLocation, IntBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform4iv(pLocation, pValue);
   }

   public static void _glUniform4(int pLocation, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniform4fv(pLocation, pValue);
   }

   public static void _glUniformMatrix2(int pLocation, boolean pTranspose, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniformMatrix2fv(pLocation, pTranspose, pValue);
   }

   public static void _glUniformMatrix3(int pLocation, boolean pTranspose, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniformMatrix3fv(pLocation, pTranspose, pValue);
   }

   public static void _glUniformMatrix4(int pLocation, boolean pTranspose, FloatBuffer pValue) {
      RenderSystem.assertOnRenderThread();
      GL20.glUniformMatrix4fv(pLocation, pTranspose, pValue);
   }

   public static int _glGetAttribLocation(int pProgram, CharSequence pName) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetAttribLocation(pProgram, pName);
   }

   public static void _glBindAttribLocation(int pProgram, int pIndex, CharSequence pName) {
      RenderSystem.assertOnRenderThread();
      GL20.glBindAttribLocation(pProgram, pIndex, pName);
   }

   public static int _glGenBuffers() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL15.glGenBuffers();
   }

   public static int _glGenVertexArrays() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glGenVertexArrays();
   }

   public static void _glBindBuffer(int pTarget, int pBuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glBindBuffer(pTarget, pBuffer);
   }

   public static void _glBindVertexArray(int pArray) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBindVertexArray(pArray);
   }

   public static void _glBufferData(int pTarget, ByteBuffer pData, int pUsage) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glBufferData(pTarget, pData, pUsage);
   }

   public static void _glBufferData(int pTarget, long pSize, int pUsage) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glBufferData(pTarget, pSize, pUsage);
   }

   @Nullable
   public static ByteBuffer _glMapBuffer(int pTarget, int pAccess) {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL15.glMapBuffer(pTarget, pAccess);
   }

   public static void _glUnmapBuffer(int pTarget) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL15.glUnmapBuffer(pTarget);
   }

   public static void _glDeleteBuffers(int pBuffer) {
      RenderSystem.assertOnRenderThread();
      if (ON_LINUX) {
         GL32C.glBindBuffer(34962, pBuffer);
         GL32C.glBufferData(34962, 0L, 35048);
         GL32C.glBindBuffer(34962, 0);
      }

      GL15.glDeleteBuffers(pBuffer);
   }

   public static void _glCopyTexSubImage2D(int pTarget, int pLevel, int pXOffset, int pYOffset, int pX, int pY, int pWidth, int pHeight) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL20.glCopyTexSubImage2D(pTarget, pLevel, pXOffset, pYOffset, pX, pY, pWidth, pHeight);
   }

   public static void _glDeleteVertexArrays(int pArray) {
      RenderSystem.assertOnRenderThread();
      GL30.glDeleteVertexArrays(pArray);
   }

   public static void _glBindFramebuffer(int pTarget, int pFramebuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (pTarget == 36160) {
         if (framebufferRead == pFramebuffer && framebufferDraw == pFramebuffer) {
            return;
         }

         framebufferRead = pFramebuffer;
         framebufferDraw = pFramebuffer;
      } else if (pTarget == 36008) {
         if (framebufferRead == pFramebuffer) {
            return;
         }

         framebufferRead = pFramebuffer;
      }

      if (pTarget == 36009) {
         if (framebufferDraw == pFramebuffer) {
            return;
         }

         framebufferDraw = pFramebuffer;
      }

      GL30.glBindFramebuffer(pTarget, pFramebuffer);
   }

   public static void _glBlitFrameBuffer(int pSrcX0, int pSrcY0, int pSrcX1, int pSrcY1, int pDstX0, int pDstY0, int pDstX1, int pDstY1, int pMask, int pFilter) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBlitFramebuffer(pSrcX0, pSrcY0, pSrcX1, pSrcY1, pDstX0, pDstY0, pDstX1, pDstY1, pMask, pFilter);
   }

   public static void _glBindRenderbuffer(int pTarget, int pRenderBuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glBindRenderbuffer(pTarget, pRenderBuffer);
   }

   public static void _glDeleteRenderbuffers(int pRenderBuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glDeleteRenderbuffers(pRenderBuffer);
   }

   public static void _glDeleteFramebuffers(int pFrameBuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glDeleteFramebuffers(pFrameBuffer);
   }

   public static int glGenFramebuffers() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glGenFramebuffers();
   }

   public static int glGenRenderbuffers() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glGenRenderbuffers();
   }

   public static void _glRenderbufferStorage(int pTarget, int pInternalFormat, int pWidth, int pHeight) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glRenderbufferStorage(pTarget, pInternalFormat, pWidth, pHeight);
   }

   public static void _glFramebufferRenderbuffer(int pTarget, int pAttachment, int pRenderBufferTarget, int pRenderBuffer) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glFramebufferRenderbuffer(pTarget, pAttachment, pRenderBufferTarget, pRenderBuffer);
   }

   public static int glCheckFramebufferStatus(int pTarget) {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL30.glCheckFramebufferStatus(pTarget);
   }

   public static void _glFramebufferTexture2D(int pTarget, int pAttachment, int pTexTarget, int pTexture, int pLevel) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL30.glFramebufferTexture2D(pTarget, pAttachment, pTexTarget, pTexture, pLevel);
   }

   public static int getBoundFramebuffer() {
      RenderSystem.assertOnRenderThread();
      return _getInteger(36006);
   }

   public static void glActiveTexture(int pTexture) {
      RenderSystem.assertOnRenderThread();
      GL13.glActiveTexture(pTexture);
   }

   public static void glBlendFuncSeparate(int pSFactorRGB, int pDFactorRGB, int pSFactorAlpha, int pDFactorAlpha) {
      RenderSystem.assertOnRenderThread();
      GL14.glBlendFuncSeparate(pSFactorRGB, pDFactorRGB, pSFactorAlpha, pDFactorAlpha);
   }

   public static String glGetShaderInfoLog(int pShader, int pMaxLength) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetShaderInfoLog(pShader, pMaxLength);
   }

   public static String glGetProgramInfoLog(int pProgram, int pMaxLength) {
      RenderSystem.assertOnRenderThread();
      return GL20.glGetProgramInfoLog(pProgram, pMaxLength);
   }

   public static void setupLevelDiffuseLighting(Vector3f pLightingVector1, Vector3f pLightingVector2, Matrix4f pMatrix) {
      RenderSystem.assertOnRenderThread();
      Vector4f vector4f = pMatrix.transform(new Vector4f(pLightingVector1, 1.0F));
      Vector4f vector4f1 = pMatrix.transform(new Vector4f(pLightingVector2, 1.0F));
      RenderSystem.setShaderLights(new Vector3f(vector4f.x(), vector4f.y(), vector4f.z()), new Vector3f(vector4f1.x(), vector4f1.y(), vector4f1.z()));
   }

   public static void setupGuiFlatDiffuseLighting(Vector3f pLightingVector1, Vector3f pLightingVector2) {
      RenderSystem.assertOnRenderThread();
      Matrix4f matrix4f = (new Matrix4f()).scaling(1.0F, -1.0F, 1.0F).rotateY((-(float)Math.PI / 8F)).rotateX(2.3561945F);
      setupLevelDiffuseLighting(pLightingVector1, pLightingVector2, matrix4f);
   }

   public static void setupGui3DDiffuseLighting(Vector3f pLightingVector1, Vector3f pLightingVector2) {
      RenderSystem.assertOnRenderThread();
      Matrix4f matrix4f = (new Matrix4f()).rotationYXZ(1.0821041F, 3.2375858F, 0.0F).rotateYXZ((-(float)Math.PI / 8F), 2.3561945F, 0.0F);
      setupLevelDiffuseLighting(pLightingVector1, pLightingVector2, matrix4f);
   }

   public static void _enableCull() {
      RenderSystem.assertOnRenderThread();
      if (cullLock.isLocked()) {
         cullLockState.setEnabled();
      } else {
         CULL.enable.enable();
      }
   }

   public static void _disableCull() {
      RenderSystem.assertOnRenderThread();
      if (cullLock.isLocked()) {
         cullLockState.setDisabled();
      } else {
         CULL.enable.disable();
      }
   }

   public static void _polygonMode(int pFace, int pMode) {
      RenderSystem.assertOnRenderThread();
      GL11.glPolygonMode(pFace, pMode);
   }

   public static void _enablePolygonOffset() {
      RenderSystem.assertOnRenderThread();
      POLY_OFFSET.fill.enable();
   }

   public static void _disablePolygonOffset() {
      RenderSystem.assertOnRenderThread();
      POLY_OFFSET.fill.disable();
   }

   public static void _polygonOffset(float pFactor, float pUnits) {
      RenderSystem.assertOnRenderThread();
      if (pFactor != POLY_OFFSET.factor || pUnits != POLY_OFFSET.units) {
         POLY_OFFSET.factor = pFactor;
         POLY_OFFSET.units = pUnits;
         GL11.glPolygonOffset(pFactor, pUnits);
      }

   }

   public static void _enableColorLogicOp() {
      RenderSystem.assertOnRenderThread();
      COLOR_LOGIC.enable.enable();
   }

   public static void _disableColorLogicOp() {
      RenderSystem.assertOnRenderThread();
      COLOR_LOGIC.enable.disable();
   }

   public static void _logicOp(int pLogicOperation) {
      RenderSystem.assertOnRenderThread();
      if (pLogicOperation != COLOR_LOGIC.op) {
         COLOR_LOGIC.op = pLogicOperation;
         GL11.glLogicOp(pLogicOperation);
      }

   }

   public static void _activeTexture(int pTexture) {
      RenderSystem.assertOnRenderThread();
      if (activeTexture != pTexture - '\u84c0') {
         activeTexture = pTexture - '\u84c0';
         glActiveTexture(pTexture);
      }

   }

   public static void enableTexture() {
      RenderSystem.assertOnRenderThreadOrInit();
      TEXTURES[activeTexture].texture2DState = true;
   }

   public static void disableTexture() {
      RenderSystem.assertOnRenderThread();
      TEXTURES[activeTexture].texture2DState = false;
   }

   public static void _texParameter(int pTarget, int pParameterName, float pParameter) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexParameterf(pTarget, pParameterName, pParameter);
   }

   public static void _texParameter(int pTarget, int pParameterName, int pParameter) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexParameteri(pTarget, pParameterName, pParameter);
   }

   public static int _getTexLevelParameter(int pTarget, int pLevel, int pParameterName) {
      RenderSystem.assertInInitPhase();
      return GL11.glGetTexLevelParameteri(pTarget, pLevel, pParameterName);
   }

   public static int _genTexture() {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL11.glGenTextures();
   }

   public static void _genTextures(int[] pTextures) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glGenTextures(pTextures);
   }

   public static void _deleteTexture(int pTexture) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (pTexture != 0) {
         for(int i = 0; i < IMAGE_TEXTURES.length; ++i) {
            if (IMAGE_TEXTURES[i] == pTexture) {
               IMAGE_TEXTURES[i] = 0;
            }
         }

         GL11.glDeleteTextures(pTexture);

         for(GlStateManager.TextureState glstatemanager$texturestate : TEXTURES) {
            if (glstatemanager$texturestate.binding == pTexture) {
               glstatemanager$texturestate.binding = 0;
            }
         }

      }
   }

   public static void _deleteTextures(int[] pTextures) {
      RenderSystem.assertOnRenderThreadOrInit();

      for(GlStateManager.TextureState glstatemanager$texturestate : TEXTURES) {
         for(int i : pTextures) {
            if (glstatemanager$texturestate.binding == i) {
               glstatemanager$texturestate.binding = -1;
            }
         }
      }

      GL11.glDeleteTextures(pTextures);
   }

   public static void _bindTexture(int pTexture) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (pTexture != TEXTURES[activeTexture].binding) {
         TEXTURES[activeTexture].binding = pTexture;
         GL11.glBindTexture(3553, pTexture);
         if (SmartAnimations.isActive()) {
            SmartAnimations.textureRendered(pTexture);
         }
      }

   }

   public static int _getActiveTexture() {
      return activeTexture + '\u84c0';
   }

   public static void _texImage2D(int pTarget, int pLevel, int pInternalFormat, int pWidth, int pHeight, int pBorder, int pFormat, int pType, @Nullable IntBuffer pPixels) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexImage2D(pTarget, pLevel, pInternalFormat, pWidth, pHeight, pBorder, pFormat, pType, pPixels);
   }

   public static void _texSubImage2D(int pTarget, int pLevel, int pXOffset, int pYOffset, int pWidth, int pHeight, int pFormat, int pType, long pPixels) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glTexSubImage2D(pTarget, pLevel, pXOffset, pYOffset, pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static void upload(int pLevel, int pXOffset, int pYOffset, int pWidth, int pHeight, NativeImage.Format pFormat, IntBuffer pPixels, Consumer<IntBuffer> pOutput) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            _upload(pLevel, pXOffset, pYOffset, pWidth, pHeight, pFormat, pPixels, pOutput);
         });
      } else {
         _upload(pLevel, pXOffset, pYOffset, pWidth, pHeight, pFormat, pPixels, pOutput);
      }

   }

   private static void _upload(int pLevel, int pXOffset, int pYOffset, int pWidth, int pHeight, NativeImage.Format pFormat, IntBuffer pPixels, Consumer<IntBuffer> pOutput) {
      try {
         RenderSystem.assertOnRenderThreadOrInit();
         _pixelStore(3314, pWidth);
         _pixelStore(3316, 0);
         _pixelStore(3315, 0);
         pFormat.setUnpackPixelStoreState();
         GL11.glTexSubImage2D(3553, pLevel, pXOffset, pYOffset, pWidth, pHeight, pFormat.glFormat(), 5121, pPixels);
      } finally {
         pOutput.accept(pPixels);
      }

   }

   public static void _getTexImage(int pTex, int pLevel, int pFormat, int pType, long pPixels) {
      RenderSystem.assertOnRenderThread();
      GL11.glGetTexImage(pTex, pLevel, pFormat, pType, pPixels);
   }

   public static void _viewport(int pX, int pY, int pWidth, int pHeight) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager.Viewport.INSTANCE.x = pX;
      GlStateManager.Viewport.INSTANCE.y = pY;
      GlStateManager.Viewport.INSTANCE.width = pWidth;
      GlStateManager.Viewport.INSTANCE.height = pHeight;
      GL11.glViewport(pX, pY, pWidth, pHeight);
   }

   public static void _colorMask(boolean pRed, boolean pGreen, boolean pBlue, boolean pAlpha) {
      RenderSystem.assertOnRenderThread();
      if (pRed != COLOR_MASK.red || pGreen != COLOR_MASK.green || pBlue != COLOR_MASK.blue || pAlpha != COLOR_MASK.alpha) {
         COLOR_MASK.red = pRed;
         COLOR_MASK.green = pGreen;
         COLOR_MASK.blue = pBlue;
         COLOR_MASK.alpha = pAlpha;
         GL11.glColorMask(pRed, pGreen, pBlue, pAlpha);
      }

   }

   public static void _stencilFunc(int pFunc, int pRef, int pMask) {
      RenderSystem.assertOnRenderThread();
      if (pFunc != STENCIL.func.func || pFunc != STENCIL.func.ref || pFunc != STENCIL.func.mask) {
         STENCIL.func.func = pFunc;
         STENCIL.func.ref = pRef;
         STENCIL.func.mask = pMask;
         GL11.glStencilFunc(pFunc, pRef, pMask);
      }

   }

   public static void _stencilMask(int pMask) {
      RenderSystem.assertOnRenderThread();
      if (pMask != STENCIL.mask) {
         STENCIL.mask = pMask;
         GL11.glStencilMask(pMask);
      }

   }

   public static void _stencilOp(int pSfail, int pDpfail, int pDppass) {
      RenderSystem.assertOnRenderThread();
      if (pSfail != STENCIL.fail || pDpfail != STENCIL.zfail || pDppass != STENCIL.zpass) {
         STENCIL.fail = pSfail;
         STENCIL.zfail = pDpfail;
         STENCIL.zpass = pDppass;
         GL11.glStencilOp(pSfail, pDpfail, pDppass);
      }

   }

   public static void _clearDepth(double pDepth) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glClearDepth(pDepth);
   }

   public static void _clearColor(float pRed, float pGreen, float pBlue, float pAlpha) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glClearColor(pRed, pGreen, pBlue, pAlpha);
   }

   public static void _clearStencil(int pIndex) {
      RenderSystem.assertOnRenderThread();
      GL11.glClearStencil(pIndex);
   }

   public static void _clear(int pMask, boolean pCheckError) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glClear(pMask);
      if (pCheckError) {
         _getError();
      }

   }

   public static void _glDrawPixels(int pWidth, int pHeight, int pFormat, int pType, long pPixels) {
      RenderSystem.assertOnRenderThread();
      GL11.glDrawPixels(pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static void _vertexAttribPointer(int pIndex, int pSize, int pType, boolean pNormalized, int pStride, long pPointer) {
      RenderSystem.assertOnRenderThread();
      GL20.glVertexAttribPointer(pIndex, pSize, pType, pNormalized, pStride, pPointer);
   }

   public static void _vertexAttribIPointer(int pIndex, int pSize, int pType, int pStride, long pPointer) {
      RenderSystem.assertOnRenderThread();
      GL30.glVertexAttribIPointer(pIndex, pSize, pType, pStride, pPointer);
   }

   public static void _enableVertexAttribArray(int pIndex) {
      RenderSystem.assertOnRenderThread();
      GL20.glEnableVertexAttribArray(pIndex);
   }

   public static void _disableVertexAttribArray(int pIndex) {
      RenderSystem.assertOnRenderThread();
      GL20.glDisableVertexAttribArray(pIndex);
   }

   public static void _drawElements(int pMode, int pCount, int pType, long pIndices) {
      RenderSystem.assertOnRenderThread();
      GL11.glDrawElements(pMode, pCount, pType, pIndices);
      if (Config.isShaders() && Shaders.isRenderingWorld) {
         int i = Shaders.activeProgram.getCountInstances();
         if (i > 1) {
            for(int j = 1; j < i; ++j) {
               Shaders.uniform_instanceId.setValue(j);
               GL11.glDrawElements(pMode, pCount, pType, pIndices);
            }

            Shaders.uniform_instanceId.setValue(0);
         }
      }

   }

   public static void drawArrays(int mode, int first, int count) {
      RenderSystem.assertOnRenderThread();
      GL11.glDrawArrays(mode, first, count);
      if (Config.isShaders() && Shaders.isRenderingWorld) {
         int i = Shaders.activeProgram.getCountInstances();
         if (i > 1) {
            for(int j = 1; j < i; ++j) {
               Shaders.uniform_instanceId.setValue(j);
               GL11.glDrawArrays(mode, first, count);
            }

            Shaders.uniform_instanceId.setValue(0);
         }
      }

   }

   public static void _pixelStore(int pParameterName, int pParam) {
      RenderSystem.assertOnRenderThreadOrInit();
      GL11.glPixelStorei(pParameterName, pParam);
   }

   public static void _readPixels(int pX, int pY, int pWidth, int pHeight, int pFormat, int pType, ByteBuffer pPixels) {
      RenderSystem.assertOnRenderThread();
      GL11.glReadPixels(pX, pY, pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static void _readPixels(int pX, int pY, int pWidth, int pHeight, int pFormat, int pType, long pPixels) {
      RenderSystem.assertOnRenderThread();
      GL11.glReadPixels(pX, pY, pWidth, pHeight, pFormat, pType, pPixels);
   }

   public static int _getError() {
      RenderSystem.assertOnRenderThread();
      return GL11.glGetError();
   }

   public static String _getString(int pName) {
      RenderSystem.assertOnRenderThread();
      return GL11.glGetString(pName);
   }

   public static int _getInteger(int pPname) {
      RenderSystem.assertOnRenderThreadOrInit();
      return GL11.glGetInteger(pPname);
   }

   public static void color4f(float red, float green, float blue, float alpha) {
      RenderSystem.setShaderColor(red, green, blue, alpha);
   }

   public static int getActiveTextureUnit() {
      return '\u84c0' + activeTexture;
   }

   public static void bindCurrentTexture() {
      GL11.glBindTexture(3553, TEXTURES[activeTexture].binding);
   }

   public static int getBoundTexture() {
      return TEXTURES[activeTexture].binding;
   }

   public static int getBoundTexture(int textureUnit) {
      return TEXTURES[textureUnit].binding;
   }

   public static void checkBoundTexture() {
      if (Config.isMinecraftThread()) {
         int i = GL11.glGetInteger(34016);
         int j = GL11.glGetInteger(32873);
         int k = getActiveTextureUnit();
         int l = getBoundTexture();
         if (l > 0) {
            if (i != k || j != l) {
               Config.dbg("checkTexture: act: " + k + ", glAct: " + i + ", tex: " + l + ", glTex: " + j);
            }

         }
      }
   }

   public static void genTextures(IntBuffer buf) {
      GL11.glGenTextures(buf);
   }

   public static void deleteTextures(IntBuffer buf) {
      buf.rewind();

      while(buf.position() < buf.limit()) {
         int i = buf.get();
         _deleteTexture(i);
      }

      buf.rewind();
   }

   public static void lockAlpha(GlAlphaState stateNew) {
      if (!alphaLock.isLocked()) {
         getAlphaState(alphaLockState);
         setAlphaState(stateNew);
         alphaLock.lock();
      }
   }

   public static void unlockAlpha() {
      if (alphaLock.unlock()) {
         setAlphaState(alphaLockState);
      }
   }

   public static void getAlphaState(GlAlphaState state) {
      if (alphaLock.isLocked()) {
         state.setState(alphaLockState);
      } else {
         state.setState(alphaTest, alphaTestFunc, alphaTestRef);
      }
   }

   public static void setAlphaState(GlAlphaState state) {
      if (alphaLock.isLocked()) {
         alphaLockState.setState(state);
      } else {
         alphaTest = state.isEnabled();
         alphaFunc(state.getFunc(), state.getRef());
      }
   }

   public static void lockBlend(GlBlendState stateNew) {
      if (!blendLock.isLocked()) {
         getBlendState(blendLockState);
         setBlendState(stateNew);
         blendLock.lock();
      }
   }

   public static void unlockBlend() {
      if (blendLock.unlock()) {
         setBlendState(blendLockState);
      }
   }

   public static void getBlendState(GlBlendState gbs) {
      if (blendLock.isLocked()) {
         gbs.setState(blendLockState);
      } else {
         gbs.setState(BLEND.mode.enabled, BLEND.srcRgb, BLEND.dstRgb, BLEND.srcAlpha, BLEND.dstAlpha);
      }
   }

   public static void setBlendState(GlBlendState gbs) {
      if (blendLock.isLocked()) {
         blendLockState.setState(gbs);
      } else {
         BLEND.mode.setEnabled(gbs.isEnabled());
         if (!gbs.isSeparate()) {
            _blendFunc(gbs.getSrcFactor(), gbs.getDstFactor());
         } else {
            _blendFuncSeparate(gbs.getSrcFactor(), gbs.getDstFactor(), gbs.getSrcFactorAlpha(), gbs.getDstFactorAlpha());
         }

      }
   }

   public static void lockCull(GlCullState stateNew) {
      if (!cullLock.isLocked()) {
         getCullState(cullLockState);
         setCullState(stateNew);
         cullLock.lock();
      }
   }

   public static void unlockCull() {
      if (cullLock.unlock()) {
         setCullState(cullLockState);
      }
   }

   public static void getCullState(GlCullState state) {
      if (cullLock.isLocked()) {
         state.setState(cullLockState);
      } else {
         state.setState(CULL.enable.enabled, CULL.mode);
      }
   }

   public static void setCullState(GlCullState state) {
      if (cullLock.isLocked()) {
         cullLockState.setState(state);
      } else {
         CULL.enable.setEnabled(state.isEnabled());
         CULL.mode = state.getMode();
      }
   }

   public static void glMultiDrawArrays(int mode, IntBuffer bFirst, IntBuffer bCount) {
      GL14.glMultiDrawArrays(mode, bFirst, bCount);
      if (Config.isShaders()) {
         int i = Shaders.activeProgram.getCountInstances();
         if (i > 1) {
            for(int j = 1; j < i; ++j) {
               Shaders.uniform_instanceId.setValue(j);
               GL14.glMultiDrawArrays(mode, bFirst, bCount);
            }

            Shaders.uniform_instanceId.setValue(0);
         }
      }

   }

   public static void glMultiDrawElements(int modeIn, IntBuffer countsIn, int typeIn, PointerBuffer indicesIn) {
      RenderSystem.assertOnRenderThread();
      GL14.glMultiDrawElements(modeIn, countsIn, typeIn, indicesIn);
      if (Config.isShaders() && Shaders.isRenderingWorld) {
         int i = Shaders.activeProgram.getCountInstances();
         if (i > 1) {
            for(int j = 1; j < i; ++j) {
               Shaders.uniform_instanceId.setValue(j);
               GL14.glMultiDrawElements(modeIn, countsIn, typeIn, indicesIn);
            }

            Shaders.uniform_instanceId.setValue(0);
         }
      }

   }

   public static void clear(int mask) {
      _clear(mask, false);
   }

   public static void bufferSubData(int target, long offset, ByteBuffer data) {
      GL15.glBufferSubData(target, offset, data);
   }

   public static void copyBufferSubData(int readTarget, int writeTarget, long readOffset, long writeOffset, long size) {
      GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
   }

   public static void readPixels(int x, int y, int width, int height, int format, int type, long pixels) {
      GL11.glReadPixels(x, y, width, height, format, type, pixels);
   }

   public static int getFramebufferRead() {
      return framebufferRead;
   }

   public static int getFramebufferDraw() {
      return framebufferDraw;
   }

   public static void applyCurrentBlend() {
      if (BLEND.mode.enabled) {
         GL11.glEnable(3042);
      } else {
         GL11.glDisable(3042);
      }

      GL14.glBlendFuncSeparate(BLEND.srcRgb, BLEND.dstRgb, BLEND.srcAlpha, BLEND.dstAlpha);
   }

   public static void setBlendsIndexed(GlBlendState[] blends) {
      if (blends != null) {
         for(int i = 0; i < blends.length; ++i) {
            GlBlendState glblendstate = blends[i];
            if (glblendstate != null) {
               if (glblendstate.isEnabled()) {
                  GL30.glEnablei(3042, i);
               } else {
                  GL30.glDisablei(3042, i);
               }

               ARBDrawBuffersBlend.glBlendFuncSeparateiARB(i, glblendstate.getSrcFactor(), glblendstate.getDstFactor(), glblendstate.getSrcFactorAlpha(), glblendstate.getDstFactorAlpha());
            }
         }

      }
   }

   public static void bindImageTexture(int unit, int texture, int level, boolean layered, int layer, int access, int format) {
      if (unit >= 0 && unit < IMAGE_TEXTURES.length) {
         if (IMAGE_TEXTURES[unit] == texture) {
            return;
         }

         IMAGE_TEXTURES[unit] = texture;
      }

      GL42.glBindImageTexture(unit, texture, level, layered, layer, access, format);
   }

   public static int getProgram() {
      return glProgram;
   }

   static class BlendState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
      public int srcRgb = 1;
      public int dstRgb = 0;
      public int srcAlpha = 1;
      public int dstAlpha = 0;
   }

   static class BooleanState {
      private final int state;
      private boolean enabled;

      public BooleanState(int pState) {
         this.state = pState;
      }

      public void disable() {
         this.setEnabled(false);
      }

      public void enable() {
         this.setEnabled(true);
      }

      public void setEnabled(boolean pEnabled) {
         RenderSystem.assertOnRenderThreadOrInit();
         if (pEnabled != this.enabled) {
            this.enabled = pEnabled;
            if (pEnabled) {
               GL11.glEnable(this.state);
            } else {
               GL11.glDisable(this.state);
            }
         }

      }
   }

   static class ColorLogicState {
      public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
      public int op = 5379;
   }

   static class ColorMask {
      public boolean red = true;
      public boolean green = true;
      public boolean blue = true;
      public boolean alpha = true;
   }

   static class CullState {
      public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
      public int mode = 1029;
   }

   static class DepthState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
      public boolean mask = true;
      public int func = 513;
   }

   @DontObfuscate
   public static enum DestFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_COLOR(768),
      ZERO(0);

      public final int value;

      private DestFactor(int pValue) {
         this.value = pValue;
      }
   }

   public static enum LogicOp {
      AND(5377),
      AND_INVERTED(5380),
      AND_REVERSE(5378),
      CLEAR(5376),
      COPY(5379),
      COPY_INVERTED(5388),
      EQUIV(5385),
      INVERT(5386),
      NAND(5390),
      NOOP(5381),
      NOR(5384),
      OR(5383),
      OR_INVERTED(5389),
      OR_REVERSE(5387),
      SET(5391),
      XOR(5382);

      public final int value;

      private LogicOp(int pValue) {
         this.value = pValue;
      }
   }

   static class PolygonOffsetState {
      public final GlStateManager.BooleanState fill = new GlStateManager.BooleanState(32823);
      public final GlStateManager.BooleanState line = new GlStateManager.BooleanState(10754);
      public float factor;
      public float units;
   }

   static class ScissorState {
      public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3089);
   }

   @DontObfuscate
   public static enum SourceFactor {
      CONSTANT_ALPHA(32771),
      CONSTANT_COLOR(32769),
      DST_ALPHA(772),
      DST_COLOR(774),
      ONE(1),
      ONE_MINUS_CONSTANT_ALPHA(32772),
      ONE_MINUS_CONSTANT_COLOR(32770),
      ONE_MINUS_DST_ALPHA(773),
      ONE_MINUS_DST_COLOR(775),
      ONE_MINUS_SRC_ALPHA(771),
      ONE_MINUS_SRC_COLOR(769),
      SRC_ALPHA(770),
      SRC_ALPHA_SATURATE(776),
      SRC_COLOR(768),
      ZERO(0);

      public final int value;

      private SourceFactor(int pValue) {
         this.value = pValue;
      }
   }

   static class StencilFunc {
      public int func = 519;
      public int ref;
      public int mask = -1;
   }

   static class StencilState {
      public final GlStateManager.StencilFunc func = new GlStateManager.StencilFunc();
      public int mask = -1;
      public int fail = 7680;
      public int zfail = 7680;
      public int zpass = 7680;
   }

   static class TextureState {
      public boolean texture2DState;
      public int binding;
   }

   public static enum Viewport {
      INSTANCE;

      protected int x;
      protected int y;
      protected int width;
      protected int height;

      public static int x() {
         return INSTANCE.x;
      }

      public static int y() {
         return INSTANCE.y;
      }

      public static int width() {
         return INSTANCE.width;
      }

      public static int height() {
         return INSTANCE.height;
      }
   }
}