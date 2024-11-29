package net.optifine;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.optifine.override.PlayerControllerOF;
import net.optifine.util.PropertiesOrdered;
import net.optifine.util.ResUtils;

public class CustomGuis {
   private static Minecraft mc = Config.getMinecraft();
   private static PlayerControllerOF playerControllerOF = null;
   private static CustomGuiProperties[][] guiProperties = null;
   public static boolean isChristmas = isChristmas();

   public static ResourceLocation getTextureLocation(ResourceLocation loc) {
      if (guiProperties == null) {
         return loc;
      } else {
         Screen screen = mc.screen;
         if (!(screen instanceof AbstractContainerScreen)) {
            return loc;
         } else if (loc.getNamespace().equals("minecraft") && loc.getPath().startsWith("textures/gui/")) {
            if (playerControllerOF == null) {
               return loc;
            } else {
               LevelReader levelreader = mc.level;
               if (levelreader == null) {
                  return loc;
               } else if (screen instanceof CreativeModeInventoryScreen) {
                  return getTexturePos(CustomGuiProperties.EnumContainer.CREATIVE, mc.player.blockPosition(), levelreader, loc, screen);
               } else if (screen instanceof InventoryScreen) {
                  return getTexturePos(CustomGuiProperties.EnumContainer.INVENTORY, mc.player.blockPosition(), levelreader, loc, screen);
               } else {
                  BlockPos blockpos = playerControllerOF.getLastClickBlockPos();
                  if (blockpos != null) {
                     if (screen instanceof AnvilScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.ANVIL, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof BeaconScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.BEACON, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof BrewingStandScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.BREWING_STAND, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof ContainerScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.CHEST, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof CraftingScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.CRAFTING, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof DispenserScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.DISPENSER, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof EnchantmentScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.ENCHANTMENT, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof FurnaceScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.FURNACE, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof HopperScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.HOPPER, blockpos, levelreader, loc, screen);
                     }

                     if (screen instanceof ShulkerBoxScreen) {
                        return getTexturePos(CustomGuiProperties.EnumContainer.SHULKER_BOX, blockpos, levelreader, loc, screen);
                     }
                  }

                  Entity entity = playerControllerOF.getLastClickEntity();
                  if (entity != null) {
                     if (screen instanceof HorseInventoryScreen) {
                        return getTextureEntity(CustomGuiProperties.EnumContainer.HORSE, entity, levelreader, loc);
                     }

                     if (screen instanceof MerchantScreen) {
                        return getTextureEntity(CustomGuiProperties.EnumContainer.VILLAGER, entity, levelreader, loc);
                     }
                  }

                  return loc;
               }
            }
         } else {
            return loc;
         }
      }
   }

   private static ResourceLocation getTexturePos(CustomGuiProperties.EnumContainer container, BlockPos pos, LevelReader blockAccess, ResourceLocation loc, Screen screen) {
      CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];
      if (acustomguiproperties == null) {
         return loc;
      } else {
         for(int i = 0; i < acustomguiproperties.length; ++i) {
            CustomGuiProperties customguiproperties = acustomguiproperties[i];
            if (customguiproperties.matchesPos(container, pos, blockAccess, screen)) {
               return customguiproperties.getTextureLocation(loc);
            }
         }

         return loc;
      }
   }

   private static ResourceLocation getTextureEntity(CustomGuiProperties.EnumContainer container, Entity entity, LevelReader blockAccess, ResourceLocation loc) {
      CustomGuiProperties[] acustomguiproperties = guiProperties[container.ordinal()];
      if (acustomguiproperties == null) {
         return loc;
      } else {
         for(int i = 0; i < acustomguiproperties.length; ++i) {
            CustomGuiProperties customguiproperties = acustomguiproperties[i];
            if (customguiproperties.matchesEntity(container, entity, blockAccess)) {
               return customguiproperties.getTextureLocation(loc);
            }
         }

         return loc;
      }
   }

   public static void update() {
      guiProperties = null;
      if (Config.isCustomGuis()) {
         List<List<CustomGuiProperties>> list = new ArrayList<>();
         PackResources[] apackresources = Config.getResourcePacks();

         for(int i = apackresources.length - 1; i >= 0; --i) {
            PackResources packresources = apackresources[i];
            update(packresources, list);
         }

         guiProperties = propertyListToArray(list);
      }
   }

   private static CustomGuiProperties[][] propertyListToArray(List<List<CustomGuiProperties>> listProps) {
      if (listProps.isEmpty()) {
         return null;
      } else {
         CustomGuiProperties[][] acustomguiproperties = new CustomGuiProperties[CustomGuiProperties.EnumContainer.values().length][];

         for(int i = 0; i < acustomguiproperties.length; ++i) {
            if (listProps.size() > i) {
               List<CustomGuiProperties> list = listProps.get(i);
               if (list != null) {
                  CustomGuiProperties[] acustomguiproperties1 = list.toArray(new CustomGuiProperties[list.size()]);
                  acustomguiproperties[i] = acustomguiproperties1;
               }
            }
         }

         return acustomguiproperties;
      }
   }

   private static void update(PackResources rp, List<List<CustomGuiProperties>> listProps) {
      String[] astring = ResUtils.collectFiles(rp, "optifine/gui/container/", ".properties", (String[])null);
      Arrays.sort((Object[])astring);

      for(int i = 0; i < astring.length; ++i) {
         String s = astring[i];
         Config.dbg("CustomGuis: " + s);

         try {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            InputStream inputstream = Config.getResourceStream(rp, PackType.CLIENT_RESOURCES, resourcelocation);
            if (inputstream == null) {
               Config.warn("CustomGuis file not found: " + s);
            } else {
               Properties properties = new PropertiesOrdered();
               properties.load(inputstream);
               inputstream.close();
               CustomGuiProperties customguiproperties = new CustomGuiProperties(properties, s);
               if (customguiproperties.isValid(s)) {
                  addToList(customguiproperties, listProps);
               }
            }
         } catch (FileNotFoundException filenotfoundexception) {
            Config.warn("CustomGuis file not found: " + s);
         } catch (Exception exception) {
            exception.printStackTrace();
         }
      }

   }

   private static void addToList(CustomGuiProperties cgp, List<List<CustomGuiProperties>> listProps) {
      if (cgp.getContainer() == null) {
         warn("Invalid container: " + cgp.getContainer());
      } else {
         int i = cgp.getContainer().ordinal();

         while(listProps.size() <= i) {
            listProps.add((List<CustomGuiProperties>)null);
         }

         List<CustomGuiProperties> list = listProps.get(i);
         if (list == null) {
            list = new ArrayList<>();
            listProps.set(i, list);
         }

         list.add(cgp);
      }
   }

   public static PlayerControllerOF getPlayerControllerOF() {
      return playerControllerOF;
   }

   public static void setPlayerControllerOF(PlayerControllerOF playerControllerOF) {
      CustomGuis.playerControllerOF = playerControllerOF;
   }

   private static boolean isChristmas() {
      Calendar calendar = Calendar.getInstance();
      return calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26;
   }

   private static void warn(String str) {
      Config.warn("[CustomGuis] " + str);
   }
}