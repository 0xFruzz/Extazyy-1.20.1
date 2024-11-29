package net.optifine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import net.minecraft.resources.ResourceLocation;
import net.optifine.config.ConnectedParser;
import net.optifine.util.PropertiesOrdered;

public class RandomEntityProperties<T> {
   private String name = null;
   private String basePath = null;
   private RandomEntityContext<T> context;
   private T[] resources = null;
   private RandomEntityRule<T>[] rules = null;
   private int matchingRuleIndex = -1;

   public RandomEntityProperties(String path, ResourceLocation baseLoc, int[] variants, RandomEntityContext<T> context) {
      ConnectedParser connectedparser = new ConnectedParser(context.getName());
      this.name = connectedparser.parseName(path);
      this.basePath = connectedparser.parseBasePath(path);
      this.context = context;
      this.resources = (T[])(new Object[variants.length]);

      for(int i = 0; i < variants.length; ++i) {
         int j = variants[i];
         this.resources[i] = context.makeResource(baseLoc, j);
      }

   }

   public RandomEntityProperties(Properties props, String path, ResourceLocation baseResLoc, RandomEntityContext<T> context) {
      ConnectedParser connectedparser = context.getConnectedParser();
      this.name = connectedparser.parseName(path);
      this.basePath = connectedparser.parseBasePath(path);
      this.context = context;
      this.rules = this.parseRules(props, path, baseResLoc);
   }

   public String getName() {
      return this.name;
   }

   public String getBasePath() {
      return this.basePath;
   }

   public T[] getResources() {
      return this.resources;
   }

   public List<T> getAllResources() {
      List<T> list = new ArrayList<>();
      if (this.resources != null) {
         list.addAll(Arrays.asList(this.resources));
      }

      if (this.rules != null) {
         for(int i = 0; i < this.rules.length; ++i) {
            RandomEntityRule<T> randomentityrule = this.rules[i];
            if (randomentityrule.getResources() != null) {
               list.addAll(Arrays.asList(randomentityrule.getResources()));
            }
         }
      }

      return list;
   }

   public T getResource(IRandomEntity randomEntity, T resDef) {
      this.matchingRuleIndex = 0;
      if (this.rules != null) {
         for(int i = 0; i < this.rules.length; ++i) {
            RandomEntityRule<T> randomentityrule = this.rules[i];
            if (randomentityrule.matches(randomEntity)) {
               this.matchingRuleIndex = randomentityrule.getIndex();
               return randomentityrule.getResource(randomEntity.getId(), resDef);
            }
         }
      }

      if (this.resources != null) {
         int j = randomEntity.getId();
         int k = j % this.resources.length;
         return this.resources[k];
      } else {
         return resDef;
      }
   }

   private RandomEntityRule<T>[] parseRules(Properties props, String pathProps, ResourceLocation baseResLoc) {
      List list = new ArrayList();
      int i = 10;

      for(int j = 0; j < i; ++j) {
         int k = j + 1;
         String s = null;
         String[] astring = this.context.getResourceKeys();

         for(String s1 : astring) {
            s = props.getProperty(s1 + "." + k);
            if (s != null) {
               break;
            }
         }

         if (s != null) {
            RandomEntityRule<T> randomentityrule = new RandomEntityRule<>(props, pathProps, baseResLoc, k, s, this.context);
            list.add(randomentityrule);
            i = k + 10;
         }
      }

      return (RandomEntityRule<T>[]) list.toArray(new RandomEntityRule[list.size()]);
   }

   public boolean isValid(String path) {
      String s = this.context.getResourceNamePlural();
      if (this.resources == null && this.rules == null) {
         Config.warn("No " + s + " specified: " + path);
         return false;
      } else {
         if (this.rules != null) {
            for(int i = 0; i < this.rules.length; ++i) {
               RandomEntityRule randomentityrule = this.rules[i];
               if (!randomentityrule.isValid(path)) {
                  return false;
               }
            }
         }

         if (this.resources != null) {
            for(int j = 0; j < this.resources.length; ++j) {
               T t = this.resources[j];
               if (t == null) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public boolean isDefault() {
      if (this.rules != null) {
         return false;
      } else {
         return this.resources == null;
      }
   }

   public int getMatchingRuleIndex() {
      return this.matchingRuleIndex;
   }

   public static RandomEntityProperties parse(ResourceLocation propLoc, ResourceLocation resLoc, RandomEntityContext context) {
      String s = context.getName();

      try {
         String s1 = propLoc.getPath();
         Config.dbg(s + ": " + resLoc.getPath() + ", properties: " + s1);
         InputStream inputstream = Config.getResourceStream(propLoc);
         if (inputstream == null) {
            Config.warn(s + ": Properties not found: " + s1);
            return null;
         } else {
            Properties properties = new PropertiesOrdered();
            properties.load(inputstream);
            inputstream.close();
            RandomEntityProperties randomentityproperties = new RandomEntityProperties(properties, s1, resLoc, context);
            return !randomentityproperties.isValid(s1) ? null : randomentityproperties;
         }
      } catch (FileNotFoundException filenotfoundexception) {
         Config.warn(s + ": File not found: " + propLoc.getPath());
         return null;
      } catch (IOException ioexception) {
         ioexception.printStackTrace();
         return null;
      }
   }

   public String toString() {
      return this.name + ", path: " + this.basePath;
   }
}
