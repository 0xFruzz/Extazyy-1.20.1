package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlock extends BaseEntityBlock {
   protected SpawnerBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SpawnerBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.MOB_SPAWNER, pLevel.isClientSide ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick);
   }

   public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack, pDropExperience);
      if (pDropExperience) {
         int i = 15 + pLevel.random.nextInt(15) + pLevel.random.nextInt(15);
         this.popExperience(pLevel, pPos, i);
      }

   }

   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      Optional<Component> optional = this.getSpawnEntityDisplayName(pStack);
      if (optional.isPresent()) {
         pTooltip.add(optional.get());
      } else {
         pTooltip.add(CommonComponents.EMPTY);
         pTooltip.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
         pTooltip.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
      }

   }

   private Optional<Component> getSpawnEntityDisplayName(ItemStack pStack) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(pStack);
      if (compoundtag != null && compoundtag.contains("SpawnData", 10)) {
         String s = compoundtag.getCompound("SpawnData").getCompound("entity").getString("id");
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
         if (resourcelocation != null) {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation).map((p_255782_) -> {
               return Component.translatable(p_255782_.getDescriptionId()).withStyle(ChatFormatting.GRAY);
            });
         }
      }

      return Optional.empty();
   }
}