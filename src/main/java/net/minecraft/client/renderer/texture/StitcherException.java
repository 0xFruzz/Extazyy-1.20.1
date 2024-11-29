package net.minecraft.client.renderer.texture;

import java.util.Collection;
import java.util.Locale;

public class StitcherException extends RuntimeException {
   private final Collection<Stitcher.Entry> allSprites;

   public StitcherException(Stitcher.Entry pEntry, Collection<Stitcher.Entry> pAllSprites) {
      super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d - Maybe try a lower resolution resourcepack?", pEntry.name(), pEntry.width(), pEntry.height()));
      this.allSprites = pAllSprites;
   }

   public Collection<Stitcher.Entry> getAllSprites() {
      return this.allSprites;
   }

   public StitcherException(Stitcher.Entry entryIn, Collection<Stitcher.Entry> entriesIn, int atlasWidth, int atlasHeight, int maxWidth, int maxHeight) {
      super(String.format(Locale.ROOT, "Unable to fit: %s - size: %dx%d, atlas: %dx%d, atlasMax: %dx%d - Maybe try a lower resolution resourcepack?", "" + entryIn.name(), entryIn.width(), entryIn.height(), atlasWidth, atlasHeight, maxWidth, maxHeight));
      this.allSprites = entriesIn;
   }
}