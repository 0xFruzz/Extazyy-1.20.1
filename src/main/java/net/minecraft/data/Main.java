package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.data.recipes.packs.BundleRecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.CatVariantTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Main {
   @DontObfuscate
   public static void main(String[] pArgs) throws IOException {
      SharedConstants.tryDetectVersion();
      OptionParser optionparser = new OptionParser();
      OptionSpec<Void> optionspec = optionparser.accepts("help", "Show the help menu").forHelp();
      OptionSpec<Void> optionspec1 = optionparser.accepts("server", "Include server generators");
      OptionSpec<Void> optionspec2 = optionparser.accepts("client", "Include client generators");
      OptionSpec<Void> optionspec3 = optionparser.accepts("dev", "Include development tools");
      OptionSpec<Void> optionspec4 = optionparser.accepts("reports", "Include data reports");
      OptionSpec<Void> optionspec5 = optionparser.accepts("validate", "Validate inputs");
      OptionSpec<Void> optionspec6 = optionparser.accepts("all", "Include all generators");
      OptionSpec<String> optionspec7 = optionparser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated");
      OptionSpec<String> optionspec8 = optionparser.accepts("input", "Input folder").withRequiredArg();
      OptionSet optionset = optionparser.parse(pArgs);
      if (!optionset.has(optionspec) && optionset.hasOptions()) {
         Path path = Paths.get(optionspec7.value(optionset));
         boolean flag = optionset.has(optionspec6);
         boolean flag1 = flag || optionset.has(optionspec2);
         boolean flag2 = flag || optionset.has(optionspec1);
         boolean flag3 = flag || optionset.has(optionspec3);
         boolean flag4 = flag || optionset.has(optionspec4);
         boolean flag5 = flag || optionset.has(optionspec5);
         DataGenerator datagenerator = createStandardGenerator(path, optionset.valuesOf(optionspec8).stream().map((p_129659_) -> {
            return Paths.get(p_129659_);
         }).collect(Collectors.toList()), flag1, flag2, flag3, flag4, flag5, SharedConstants.getCurrentVersion(), true);
         datagenerator.run();
      } else {
         optionparser.printHelpOn(System.out);
      }
   }

   private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> pTagProviderFactory, CompletableFuture<HolderLookup.Provider> pLookupProvider) {
      return (p_255476_) -> {
         return pTagProviderFactory.apply(p_255476_, pLookupProvider);
      };
   }

   public static DataGenerator createStandardGenerator(Path pOutputFolder, Collection<Path> pInputFolders, boolean pClient, boolean pServer, boolean pDev, boolean pReports, boolean pValidate, WorldVersion pVersion, boolean pAlwaysGenerate) {
      DataGenerator datagenerator = new DataGenerator(pOutputFolder, pVersion, pAlwaysGenerate);
      DataGenerator.PackGenerator datagenerator$packgenerator = datagenerator.getVanillaPack(pClient || pServer);
      datagenerator$packgenerator.addProvider((p_253388_) -> {
         return (new SnbtToNbt(p_253388_, pInputFolders)).addFilter(new StructureUpdater());
      });
      CompletableFuture<HolderLookup.Provider> completablefuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
      DataGenerator.PackGenerator datagenerator$packgenerator1 = datagenerator.getVanillaPack(pClient);
      datagenerator$packgenerator1.addProvider(ModelProvider::new);
      DataGenerator.PackGenerator datagenerator$packgenerator2 = datagenerator.getVanillaPack(pServer);
      datagenerator$packgenerator2.addProvider(bindRegistries(RegistriesDatapackGenerator::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(VanillaAdvancementProvider::create, completablefuture));
      datagenerator$packgenerator2.addProvider(VanillaLootTableProvider::create);
      datagenerator$packgenerator2.addProvider(VanillaRecipeProvider::new);
      TagsProvider<Block> tagsprovider1 = datagenerator$packgenerator2.addProvider(bindRegistries(VanillaBlockTagsProvider::new, completablefuture));
      TagsProvider<Item> tagsprovider = datagenerator$packgenerator2.addProvider((p_274753_) -> {
         return new VanillaItemTagsProvider(p_274753_, completablefuture, tagsprovider1.contentsGetter());
      });
      datagenerator$packgenerator2.addProvider(bindRegistries(BannerPatternTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(BiomeTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(CatVariantTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(DamageTypeTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(EntityTypeTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(FluidTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(GameEventTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(InstrumentTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(PaintingVariantTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(PoiTypeTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(StructureTagsProvider::new, completablefuture));
      datagenerator$packgenerator2.addProvider(bindRegistries(WorldPresetTagsProvider::new, completablefuture));
      datagenerator$packgenerator2 = datagenerator.getVanillaPack(pDev);
      datagenerator$packgenerator2.addProvider((p_253386_) -> {
         return new NbtToSnbt(p_253386_, pInputFolders);
      });
      datagenerator$packgenerator2 = datagenerator.getVanillaPack(pReports);
      datagenerator$packgenerator2.addProvider(bindRegistries(BiomeParametersDumpReport::new, completablefuture));
      datagenerator$packgenerator2.addProvider(BlockListReport::new);
      datagenerator$packgenerator2.addProvider(bindRegistries(CommandsReport::new, completablefuture));
      datagenerator$packgenerator2.addProvider(RegistryDumpReport::new);
      datagenerator$packgenerator2 = datagenerator.getBuiltinDatapack(pServer, "bundle");
      datagenerator$packgenerator2.addProvider(BundleRecipeProvider::new);
      datagenerator$packgenerator2.addProvider((p_253392_) -> {
         return PackMetadataGenerator.forFeaturePack(p_253392_, Component.translatable("dataPack.bundle.description"), FeatureFlagSet.of(FeatureFlags.BUNDLE));
      });
      return datagenerator;
   }
}