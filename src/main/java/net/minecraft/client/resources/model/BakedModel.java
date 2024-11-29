package net.minecraft.client.resources.model;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.IForgeBakedModel;

public interface BakedModel extends IForgeBakedModel {
   List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom);

   boolean useAmbientOcclusion();

   boolean isGui3d();

   boolean usesBlockLight();

   boolean isCustomRenderer();

   TextureAtlasSprite getParticleIcon();

   default ItemTransforms getTransforms() {
      return ItemTransforms.NO_TRANSFORMS;
   }

   ItemOverrides getOverrides();
}