package net.minecraft.server.packs;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import org.slf4j.Logger;

public class PathPackResources extends AbstractPackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Joiner PATH_JOINER = Joiner.on("/");
   public final Path root;

   public PathPackResources(String pName, Path pRoot, boolean pIsBuiltin) {
      super(pName, pIsBuiltin);
      this.root = pRoot;
   }

   @Nullable
   public IoSupplier<InputStream> getRootResource(String... pElements) {
      FileUtil.validatePath(pElements);
      Path path = FileUtil.resolvePath(this.root, List.of(pElements));
      return Files.exists(path) ? IoSupplier.create(path) : null;
   }

   public static boolean validatePath(Path pPath) {
      return true;
   }

   @Nullable
   public IoSupplier<InputStream> getResource(PackType pPackType, ResourceLocation pLocation) {
      Path path = this.root.resolve(pPackType.getDirectory()).resolve(pLocation.getNamespace());
      return getResource(pLocation, path);
   }

   public static IoSupplier<InputStream> getResource(ResourceLocation pLocation, Path pPath) {
      return FileUtil.decomposePath(pLocation.getPath()).get().map((p_245898_1_) -> {
         Path path = FileUtil.resolvePath(pPath, p_245898_1_);
         return returnFileIfExists(path);
      }, (p_246537_1_) -> {
         LOGGER.error("Invalid path {}: {}", pLocation, p_246537_1_.message());
         return null;
      });
   }

   @Nullable
   private static IoSupplier<InputStream> returnFileIfExists(Path pPath) {
      return Files.exists(pPath) && validatePath(pPath) ? IoSupplier.create(pPath) : null;
   }

   public void listResources(PackType pPackType, String pNamespace, String pPath, PackResources.ResourceOutput pResourceOutput) {
      FileUtil.decomposePath(pPath).get().ifLeft((p_245378_4_) -> {
         Path path = this.root.resolve(pPackType.getDirectory()).resolve(pNamespace);
         listPath(pNamespace, path, p_245378_4_, pResourceOutput);
      }).ifRight((p_246950_1_) -> {
         LOGGER.error("Invalid path {}: {}", pPath, p_246950_1_.message());
      });
   }

   public static void listPath(String pNamespace, Path pNamespacePath, List<String> pDecomposedPath, PackResources.ResourceOutput pResourceOutput) {
      Path path = FileUtil.resolvePath(pNamespacePath, pDecomposedPath);

      try (Stream<Path> stream = Files.find(path, Integer.MAX_VALUE, (p_247260_0_, p_247260_1_) -> {
            return p_247260_1_.isRegularFile();
         })) {
         stream.forEach((p_247327_3_) -> {
            String s = PATH_JOINER.join(pNamespacePath.relativize(p_247327_3_));
            ResourceLocation resourcelocation = ResourceLocation.tryBuild(pNamespace, s);
            if (resourcelocation == null) {
               Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in pack: %s:%s, ignoring", pNamespace, s));
            } else {
               pResourceOutput.accept(resourcelocation, IoSupplier.create(p_247327_3_));
            }

         });
      } catch (NoSuchFileException nosuchfileexception) {
      } catch (IOException ioexception1) {
         LOGGER.error("Failed to list path {}", path, ioexception1);
      }

   }

   public Set<String> getNamespaces(PackType pType) {
      Set<String> set = Sets.newHashSet();
      Path path = this.root.resolve(pType.getDirectory());

      try (DirectoryStream<Path> directorystream = Files.newDirectoryStream(path)) {
         for(Path path1 : directorystream) {
            String s = path1.getFileName().toString();
            if (s.equals(s.toLowerCase(Locale.ROOT))) {
               set.add(s);
            } else {
               LOGGER.warn("Ignored non-lowercase namespace: {} in {}", s, this.root);
            }
         }
      } catch (NoSuchFileException nosuchfileexception) {
      } catch (IOException ioexception1) {
         LOGGER.error("Failed to list path {}", path, ioexception1);
      }

      return set;
   }

   public void close() {
   }
}