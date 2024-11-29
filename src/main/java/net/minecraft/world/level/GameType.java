package net.minecraft.world.level;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Abilities;
import org.jetbrains.annotations.Contract;

public enum GameType implements StringRepresentable {
   SURVIVAL(0, "survival"),
   CREATIVE(1, "creative"),
   ADVENTURE(2, "adventure"),
   SPECTATOR(3, "spectator");

   public static final GameType DEFAULT_MODE = SURVIVAL;
   public static final StringRepresentable.EnumCodec<GameType> CODEC = StringRepresentable.fromEnum(GameType::values);
   private static final IntFunction<GameType> BY_ID = ByIdMap.continuous(GameType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
   private static final int NOT_SET = -1;
   private final int id;
   private final String name;
   private final Component shortName;
   private final Component longName;

   private GameType(int pId, String pName) {
      this.id = pId;
      this.name = pName;
      this.shortName = Component.translatable("selectWorld.gameMode." + pName);
      this.longName = Component.translatable("gameMode." + pName);
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   public Component getLongDisplayName() {
      return this.longName;
   }

   public Component getShortDisplayName() {
      return this.shortName;
   }

   public void updatePlayerAbilities(Abilities pAbilities) {
      if (this == CREATIVE) {
         pAbilities.mayfly = true;
         pAbilities.instabuild = true;
         pAbilities.invulnerable = true;
      } else if (this == SPECTATOR) {
         pAbilities.mayfly = true;
         pAbilities.instabuild = false;
         pAbilities.invulnerable = true;
         pAbilities.flying = true;
      } else {
         pAbilities.mayfly = false;
         pAbilities.instabuild = false;
         pAbilities.invulnerable = false;
         pAbilities.flying = false;
      }

      pAbilities.mayBuild = !this.isBlockPlacingRestricted();
   }

   public boolean isBlockPlacingRestricted() {
      return this == ADVENTURE || this == SPECTATOR;
   }

   public boolean isCreative() {
      return this == CREATIVE;
   }

   public boolean isSurvival() {
      return this == SURVIVAL || this == ADVENTURE;
   }

   public static GameType byId(int pId) {
      return BY_ID.apply(pId);
   }

   public static GameType byName(String pGamemodeName) {
      return byName(pGamemodeName, SURVIVAL);
   }

   @Nullable
   @Contract("_,!null->!null;_,null->_")
   public static GameType byName(String pTargetName, @Nullable GameType pFallback) {
      GameType gametype = CODEC.byName(pTargetName);
      return gametype != null ? gametype : pFallback;
   }

   public static int getNullableId(@Nullable GameType pGameType) {
      return pGameType != null ? pGameType.id : -1;
   }

   @Nullable
   public static GameType byNullableId(int pId) {
      return pId == -1 ? null : byId(pId);
   }
}