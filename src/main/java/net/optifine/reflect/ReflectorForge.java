package net.optifine.reflect;

import com.mojang.blaze3d.vertex.PoseStack;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.client.model.ForgeFaceData;
import net.optifine.Log;
import net.optifine.util.StrUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ReflectorForge {
   public static Object EVENT_RESULT_ALLOW = Reflector.getFieldValue(Reflector.Event_Result_ALLOW);
   public static Object EVENT_RESULT_DENY = Reflector.getFieldValue(Reflector.Event_Result_DENY);
   public static Object EVENT_RESULT_DEFAULT = Reflector.getFieldValue(Reflector.Event_Result_DEFAULT);
   public static final boolean FORGE_ENTITY_CAN_UPDATE = Reflector.IForgeEntity_canUpdate.exists();

   public static void putLaunchBlackboard(String key, Object value) {
      Map map = (Map)Reflector.getFieldValue(Reflector.Launch_blackboard);
      if (map != null) {
         map.put(key, value);
      }
   }

   public static InputStream getOptiFineResourceStream(String path) {
      if (!Reflector.OptiFineResourceLocator.exists()) {
         return null;
      } else {
         path = StrUtils.removePrefix(path, "/");
         return (InputStream)Reflector.call(Reflector.OptiFineResourceLocator_getOptiFineResourceStream, path);
      }
   }

   public static ReflectorClass getReflectorClassOptiFineResourceLocator() {
      String s = "optifine.OptiFineResourceLocator";
      Object object = System.getProperties().get(s + ".class");
      if (object instanceof Class oclass) {
         return new ReflectorClass(oclass);
      } else {
         return new ReflectorClass(s);
      }
   }

   public static boolean calculateFaceWithoutAO(BlockAndTintGetter getter, BlockState state, BlockPos pos, BakedQuad quad, boolean isFaceCubic, float[] brightness, int[] lightmap) {
      if (quad.hasAmbientOcclusion()) {
         return false;
      } else {
         BlockPos blockpos = isFaceCubic ? pos.relative(quad.getDirection()) : pos;
         brightness[0] = brightness[1] = brightness[2] = brightness[3] = getter.getShade(quad.getDirection(), quad.isShade());
         lightmap[0] = lightmap[1] = lightmap[2] = lightmap[3] = LevelRenderer.getLightColor(getter, state, blockpos);
         return true;
      }
   }

   public static MapItemSavedData getMapData(ItemStack stack, Level world) {
      if (Reflector.ForgeHooksClient.exists()) {
         MapItem mapitem = (MapItem)stack.getItem();
         return MapItem.getSavedData(stack, world);
      } else {
         return MapItem.getSavedData(stack, world);
      }
   }

   public static String[] getForgeModIds() {
      if (!Reflector.ModList.exists()) {
         return new String[0];
      } else {
         Object object = Reflector.ModList_get.call();
         List list = (List)Reflector.getFieldValue(object, Reflector.ModList_mods);
         if (list == null) {
            return new String[0];
         } else {
            List<String> list1 = new ArrayList<>();

            for(Object object1 : list) {
               if (Reflector.ModContainer.isInstance(object1)) {
                  String s = Reflector.callString(object1, Reflector.ModContainer_getModId);
                  if (s != null && !s.equals("minecraft") && !s.equals("forge")) {
                     list1.add(s);
                  }
               }
            }

            String[] astring = list1.toArray(new String[list1.size()]);
            return astring;
         }
      }
   }

   public static boolean canDisableShield(ItemStack itemstack, ItemStack itemstack1, Player entityplayer, Mob entityLiving) {
      return Reflector.IForgeItemStack_canDisableShield.exists() ? Reflector.callBoolean(itemstack, Reflector.IForgeItemStack_canDisableShield, itemstack1, entityplayer, entityLiving) : itemstack.getItem() instanceof AxeItem;
   }

   public static Button makeButtonMods(TitleScreen guiMainMenu, int yIn, int rowHeightIn) {
      return !Reflector.ModListScreen_Constructor.exists() ? null : Button.builder(Component.translatable("fml.menu.mods"), (button) -> {
         Screen screen = (Screen)Reflector.ModListScreen_Constructor.newInstance(guiMainMenu);
         Minecraft.getInstance().setScreen(screen);
      }).pos(guiMainMenu.width / 2 - 100, yIn + rowHeightIn * 2).size(98, 20).build();
   }

   public static void setForgeLightPipelineEnabled(boolean value) {
      if (Reflector.ForgeConfig_Client_forgeLightPipelineEnabled.exists()) {
         setConfigClientBoolean(Reflector.ForgeConfig_Client_forgeLightPipelineEnabled, value);
      }

   }

   public static boolean getForgeUseCombinedDepthStencilAttachment() {
      return Reflector.ForgeConfig_Client_useCombinedDepthStencilAttachment.exists() ? getConfigClientBoolean(Reflector.ForgeConfig_Client_useCombinedDepthStencilAttachment, false) : false;
   }

   public static boolean getForgeCalculateAllNormals() {
      return Reflector.ForgeConfig_Client_calculateAllNormals.exists() ? getConfigClientBoolean(Reflector.ForgeConfig_Client_calculateAllNormals, false) : false;
   }

   public static boolean getConfigClientBoolean(ReflectorField configField, boolean def) {
      if (!configField.exists()) {
         return def;
      } else {
         Object object = Reflector.ForgeConfig_CLIENT.getValue();
         if (object == null) {
            return def;
         } else {
            Object object1 = Reflector.getFieldValue(object, configField);
            return object1 == null ? def : Reflector.callBoolean(object1, Reflector.ForgeConfigSpec_ConfigValue_get);
         }
      }
   }

   private static void setConfigClientBoolean(ReflectorField clientField, final boolean value) {
      if (clientField.exists()) {
         Object object = Reflector.ForgeConfig_CLIENT.getValue();
         if (object != null) {
            Object object1 = Reflector.getFieldValue(object, clientField);
            if (object1 != null) {
               Supplier<Boolean> supplier = new Supplier<Boolean>() {
                  public Boolean get() {
                     return value;
                  }
               };
               Reflector.setFieldValue(object1, Reflector.ForgeConfigSpec_ConfigValue_defaultSupplier, supplier);
               Object object2 = Reflector.getFieldValue(object1, Reflector.ForgeConfigSpec_ConfigValue_spec);
               if (object2 != null) {
                  Reflector.setFieldValue(object2, Reflector.ForgeConfigSpec_childConfig, (Object)null);
               }

               Log.dbg("Set ForgeConfig.CLIENT." + clientField.getTargetField().getName() + "=" + value);
            }
         }
      }
   }

   public static boolean canUpdate(Entity entity) {
      return FORGE_ENTITY_CAN_UPDATE ? Reflector.callBoolean(entity, Reflector.IForgeEntity_canUpdate) : true;
   }

   public static boolean isDamageable(Item item, ItemStack stack) {
      return Reflector.IForgeItem_isDamageable1.exists() ? Reflector.callBoolean(item, Reflector.IForgeItem_isDamageable1, stack) : item.canBeDepleted();
   }

   public static void fillNormal(int[] faceData, Direction facing, ForgeFaceData data) {
      boolean flag = false;
      if (Reflector.ForgeFaceData_calculateNormals.exists()) {
         flag = Reflector.callBoolean(data, Reflector.ForgeFaceData_calculateNormals);
      }

      Vector3f vector3f;
      if (!flag && !getForgeCalculateAllNormals()) {
         vector3f = new Vector3f((float)facing.getNormal().getX(), (float)facing.getNormal().getY(), (float)facing.getNormal().getZ());
      } else {
         Vector3f vector3f1 = getVertexPos(faceData, 3);
         Vector3f vector3f2 = getVertexPos(faceData, 1);
         vector3f = getVertexPos(faceData, 2);
         Vector3f vector3f3 = getVertexPos(faceData, 0);
         vector3f1.sub(vector3f2);
         vector3f.sub(vector3f3);
         vector3f.cross(vector3f1);
         vector3f.normalize();
      }

      int l = (byte)Math.round(vector3f.x() * 127.0F) & 255;
      int i1 = (byte)Math.round(vector3f.y() * 127.0F) & 255;
      int j1 = (byte)Math.round(vector3f.z() * 127.0F) & 255;
      int i = l | i1 << 8 | j1 << 16;
      int j = faceData.length / 4;

      for(int k = 0; k < 4; ++k) {
         faceData[k * j + 7] = i;
      }

   }

   private static Vector3f getVertexPos(int[] data, int vertex) {
      int i = data.length / 4;
      int j = vertex * i;
      float f = Float.intBitsToFloat(data[j]);
      float f1 = Float.intBitsToFloat(data[j + 1]);
      float f2 = Float.intBitsToFloat(data[j + 2]);
      return new Vector3f(f, f1, f2);
   }

   public static void postModLoaderEvent(ReflectorConstructor constr, Object... params) {
      Object object = Reflector.newInstance(constr, params);
      if (object != null) {
         postModLoaderEvent(object);
      }
   }

   public static void postModLoaderEvent(Object event) {
      if (event != null) {
         Object object = Reflector.ModLoader_get.call();
         if (object != null) {
            Reflector.callVoid(object, Reflector.ModLoader_postEvent, event);
         }
      }
   }

   public static void dispatchRenderStageS(ReflectorField stageField, LevelRenderer levelRenderer, PoseStack matrixStack, Matrix4f matrix, int ticks, Camera camera, Frustum frustum) {
      if (Reflector.ForgeHooksClient_dispatchRenderStageS.exists()) {
         if (stageField.exists()) {
            Object object = stageField.getValue();
            Reflector.ForgeHooksClient_dispatchRenderStageS.call(object, levelRenderer, matrixStack, matrix, ticks, camera, frustum);
         }
      }
   }
}