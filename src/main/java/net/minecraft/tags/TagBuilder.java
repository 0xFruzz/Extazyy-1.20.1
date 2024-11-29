package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder {
   private final List<TagEntry> entries = new ArrayList<>();

   public static TagBuilder create() {
      return new TagBuilder();
   }

   public List<TagEntry> build() {
      return List.copyOf(this.entries);
   }

   public TagBuilder add(TagEntry pEntry) {
      this.entries.add(pEntry);
      return this;
   }

   public TagBuilder addElement(ResourceLocation pElementLocation) {
      return this.add(TagEntry.element(pElementLocation));
   }

   public TagBuilder addOptionalElement(ResourceLocation pElementLocation) {
      return this.add(TagEntry.optionalElement(pElementLocation));
   }

   public TagBuilder addTag(ResourceLocation pTagLocation) {
      return this.add(TagEntry.tag(pTagLocation));
   }

   public TagBuilder addOptionalTag(ResourceLocation pTagLocation) {
      return this.add(TagEntry.optionalTag(pTagLocation));
   }
}