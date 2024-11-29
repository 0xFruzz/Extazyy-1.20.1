package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public class HolderSetCodec<E> implements Codec<HolderSet<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<Holder<E>> elementCodec;
   private final Codec<List<Holder<E>>> homogenousListCodec;
   private final Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

   private static <E> Codec<List<Holder<E>>> homogenousList(Codec<Holder<E>> pHolderCodec, boolean pDisallowInline) {
      Codec<List<Holder<E>>> codec = ExtraCodecs.validate(pHolderCodec.listOf(), ExtraCodecs.ensureHomogenous(Holder::kind));
      return pDisallowInline ? codec : Codec.either(codec, pHolderCodec).xmap((p_206664_) -> {
         return p_206664_.map((p_206694_) -> {
            return p_206694_;
         }, List::of);
      }, (p_206684_) -> {
         return p_206684_.size() == 1 ? Either.right(p_206684_.get(0)) : Either.left(p_206684_);
      });
   }

   public static <E> Codec<HolderSet<E>> create(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<Holder<E>> pHolderCodec, boolean pDisallowInline) {
      return new HolderSetCodec<>(pRegistryKey, pHolderCodec, pDisallowInline);
   }

   private HolderSetCodec(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<Holder<E>> pElementCodec, boolean pDisallowInline) {
      this.registryKey = pRegistryKey;
      this.elementCodec = pElementCodec;
      this.homogenousListCodec = homogenousList(pElementCodec, pDisallowInline);
      this.registryAwareCodec = Codec.either(TagKey.hashedCodec(pRegistryKey), this.homogenousListCodec);
   }

   public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> pOps, T pInput) {
      if (pOps instanceof RegistryOps<T> registryops) {
         Optional<HolderGetter<E>> optional = registryops.getter(this.registryKey);
         if (optional.isPresent()) {
            HolderGetter<E> holdergetter = optional.get();
            return this.registryAwareCodec.decode(pOps, pInput).map((p_206682_) -> {
               return p_206682_.mapFirst((p_206679_) -> {
                  return p_206679_.map(holdergetter::getOrThrow, HolderSet::direct);
               });
            });
         }
      }

      return this.decodeWithoutRegistry(pOps, pInput);
   }

   public <T> DataResult<T> encode(HolderSet<E> pInput, DynamicOps<T> pOps, T pPrefix) {
      if (pOps instanceof RegistryOps<T> registryops) {
         Optional<HolderOwner<E>> optional = registryops.owner(this.registryKey);
         if (optional.isPresent()) {
            if (!pInput.canSerializeIn(optional.get())) {
               return DataResult.error(() -> {
                  return "HolderSet " + pInput + " is not valid in current registry set";
               });
            }

            return this.registryAwareCodec.encode(pInput.unwrap().mapRight(List::copyOf), pOps, pPrefix);
         }
      }

      return this.encodeWithoutRegistry(pInput, pOps, pPrefix);
   }

   private <T> DataResult<Pair<HolderSet<E>, T>> decodeWithoutRegistry(DynamicOps<T> pOps, T pInput) {
      return this.elementCodec.listOf().decode(pOps, pInput).flatMap((p_206666_) -> {
         List<Holder.Direct<E>> list = new ArrayList<>();

         for(Holder<E> holder : p_206666_.getFirst()) {
            if (!(holder instanceof Holder.Direct)) {
               return DataResult.error(() -> {
                  return "Can't decode element " + holder + " without registry";
               });
            }

            Holder.Direct<E> direct = (Holder.Direct)holder;
            list.add(direct);
         }

         return DataResult.success(new Pair<>(HolderSet.direct(list), p_206666_.getSecond()));
      });
   }

   private <T> DataResult<T> encodeWithoutRegistry(HolderSet<E> pInput, DynamicOps<T> pOps, T pPrefix) {
      return this.homogenousListCodec.encode(pInput.stream().toList(), pOps, pPrefix);
   }
}