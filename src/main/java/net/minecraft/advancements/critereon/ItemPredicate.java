package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

public class ItemPredicate {
   public static final ItemPredicate ANY = new ItemPredicate();
   @Nullable
   private final TagKey<Item> tag;
   @Nullable
   private final Set<Item> items;
   private final MinMaxBounds.Ints count;
   private final MinMaxBounds.Ints durability;
   private final EnchantmentPredicate[] enchantments;
   private final EnchantmentPredicate[] storedEnchantments;
   @Nullable
   private final Potion potion;
   private final NbtPredicate nbt;

   public ItemPredicate() {
      this.tag = null;
      this.items = null;
      this.potion = null;
      this.count = MinMaxBounds.Ints.ANY;
      this.durability = MinMaxBounds.Ints.ANY;
      this.enchantments = EnchantmentPredicate.NONE;
      this.storedEnchantments = EnchantmentPredicate.NONE;
      this.nbt = NbtPredicate.ANY;
   }

   public ItemPredicate(@Nullable TagKey<Item> pTag, @Nullable Set<Item> pItems, MinMaxBounds.Ints pCount, MinMaxBounds.Ints pDurability, EnchantmentPredicate[] pEnchantments, EnchantmentPredicate[] pStoredEnchantments, @Nullable Potion pPotion, NbtPredicate pNbt) {
      this.tag = pTag;
      this.items = pItems;
      this.count = pCount;
      this.durability = pDurability;
      this.enchantments = pEnchantments;
      this.storedEnchantments = pStoredEnchantments;
      this.potion = pPotion;
      this.nbt = pNbt;
   }

   public boolean matches(ItemStack pItem) {
      if (this == ANY) {
         return true;
      } else if (this.tag != null && !pItem.is(this.tag)) {
         return false;
      } else if (this.items != null && !this.items.contains(pItem.getItem())) {
         return false;
      } else if (!this.count.matches(pItem.getCount())) {
         return false;
      } else if (!this.durability.isAny() && !pItem.isDamageableItem()) {
         return false;
      } else if (!this.durability.matches(pItem.getMaxDamage() - pItem.getDamageValue())) {
         return false;
      } else if (!this.nbt.matches(pItem)) {
         return false;
      } else {
         if (this.enchantments.length > 0) {
            Map<Enchantment, Integer> map = EnchantmentHelper.deserializeEnchantments(pItem.getEnchantmentTags());

            for(EnchantmentPredicate enchantmentpredicate : this.enchantments) {
               if (!enchantmentpredicate.containedIn(map)) {
                  return false;
               }
            }
         }

         if (this.storedEnchantments.length > 0) {
            Map<Enchantment, Integer> map1 = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(pItem));

            for(EnchantmentPredicate enchantmentpredicate1 : this.storedEnchantments) {
               if (!enchantmentpredicate1.containedIn(map1)) {
                  return false;
               }
            }
         }

         Potion potion = PotionUtils.getPotion(pItem);
         return this.potion == null || this.potion == potion;
      }
   }

   public static ItemPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "item");
         MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(jsonobject.get("count"));
         MinMaxBounds.Ints minmaxbounds$ints1 = MinMaxBounds.Ints.fromJson(jsonobject.get("durability"));
         if (jsonobject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
            Set<Item> set = null;
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "items", (JsonArray)null);
            if (jsonarray != null) {
               ImmutableSet.Builder<Item> builder = ImmutableSet.builder();

               for(JsonElement jsonelement : jsonarray) {
                  ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.convertToString(jsonelement, "item"));
                  builder.add(BuiltInRegistries.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
                     return new JsonSyntaxException("Unknown item id '" + resourcelocation + "'");
                  }));
               }

               set = builder.build();
            }

            TagKey<Item> tagkey = null;
            if (jsonobject.has("tag")) {
               ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "tag"));
               tagkey = TagKey.create(Registries.ITEM, resourcelocation1);
            }

            Potion potion = null;
            if (jsonobject.has("potion")) {
               ResourceLocation resourcelocation2 = new ResourceLocation(GsonHelper.getAsString(jsonobject, "potion"));
               potion = BuiltInRegistries.POTION.getOptional(resourcelocation2).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown potion '" + resourcelocation2 + "'");
               });
            }

            EnchantmentPredicate[] aenchantmentpredicate = EnchantmentPredicate.fromJsonArray(jsonobject.get("enchantments"));
            EnchantmentPredicate[] aenchantmentpredicate1 = EnchantmentPredicate.fromJsonArray(jsonobject.get("stored_enchantments"));
            return new ItemPredicate(tagkey, set, minmaxbounds$ints, minmaxbounds$ints1, aenchantmentpredicate, aenchantmentpredicate1, potion, nbtpredicate);
         }
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.items != null) {
            JsonArray jsonarray = new JsonArray();

            for(Item item : this.items) {
               jsonarray.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }

            jsonobject.add("items", jsonarray);
         }

         if (this.tag != null) {
            jsonobject.addProperty("tag", this.tag.location().toString());
         }

         jsonobject.add("count", this.count.serializeToJson());
         jsonobject.add("durability", this.durability.serializeToJson());
         jsonobject.add("nbt", this.nbt.serializeToJson());
         if (this.enchantments.length > 0) {
            JsonArray jsonarray1 = new JsonArray();

            for(EnchantmentPredicate enchantmentpredicate : this.enchantments) {
               jsonarray1.add(enchantmentpredicate.serializeToJson());
            }

            jsonobject.add("enchantments", jsonarray1);
         }

         if (this.storedEnchantments.length > 0) {
            JsonArray jsonarray2 = new JsonArray();

            for(EnchantmentPredicate enchantmentpredicate1 : this.storedEnchantments) {
               jsonarray2.add(enchantmentpredicate1.serializeToJson());
            }

            jsonobject.add("stored_enchantments", jsonarray2);
         }

         if (this.potion != null) {
            jsonobject.addProperty("potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
         }

         return jsonobject;
      }
   }

   public static ItemPredicate[] fromJsonArray(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonArray jsonarray = GsonHelper.convertToJsonArray(pJson, "items");
         ItemPredicate[] aitempredicate = new ItemPredicate[jsonarray.size()];

         for(int i = 0; i < aitempredicate.length; ++i) {
            aitempredicate[i] = fromJson(jsonarray.get(i));
         }

         return aitempredicate;
      } else {
         return new ItemPredicate[0];
      }
   }

   public static class Builder {
      private final List<EnchantmentPredicate> enchantments = Lists.newArrayList();
      private final List<EnchantmentPredicate> storedEnchantments = Lists.newArrayList();
      @Nullable
      private Set<Item> items;
      @Nullable
      private TagKey<Item> tag;
      private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;
      private MinMaxBounds.Ints durability = MinMaxBounds.Ints.ANY;
      @Nullable
      private Potion potion;
      private NbtPredicate nbt = NbtPredicate.ANY;

      private Builder() {
      }

      public static ItemPredicate.Builder item() {
         return new ItemPredicate.Builder();
      }

      public ItemPredicate.Builder of(ItemLike... pItems) {
         this.items = Stream.of(pItems).map(ItemLike::asItem).collect(ImmutableSet.toImmutableSet());
         return this;
      }

      public ItemPredicate.Builder of(TagKey<Item> pTag) {
         this.tag = pTag;
         return this;
      }

      public ItemPredicate.Builder withCount(MinMaxBounds.Ints pCount) {
         this.count = pCount;
         return this;
      }

      public ItemPredicate.Builder hasDurability(MinMaxBounds.Ints pDurability) {
         this.durability = pDurability;
         return this;
      }

      public ItemPredicate.Builder isPotion(Potion pPotion) {
         this.potion = pPotion;
         return this;
      }

      public ItemPredicate.Builder hasNbt(CompoundTag pNbt) {
         this.nbt = new NbtPredicate(pNbt);
         return this;
      }

      public ItemPredicate.Builder hasEnchantment(EnchantmentPredicate pEnchantment) {
         this.enchantments.add(pEnchantment);
         return this;
      }

      public ItemPredicate.Builder hasStoredEnchantment(EnchantmentPredicate pStoredEnchantment) {
         this.storedEnchantments.add(pStoredEnchantment);
         return this;
      }

      public ItemPredicate build() {
         return new ItemPredicate(this.tag, this.items, this.count, this.durability, this.enchantments.toArray(EnchantmentPredicate.NONE), this.storedEnchantments.toArray(EnchantmentPredicate.NONE), this.potion, this.nbt);
      }
   }
}