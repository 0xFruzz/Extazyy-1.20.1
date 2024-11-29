package net.minecraft.client.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.optifine.Config;
import net.optifine.RandomEntities;
import net.optifine.player.CapeUtils;
import net.optifine.player.PlayerConfiguration;
import net.optifine.player.PlayerConfigurations;
import net.optifine.reflect.Reflector;
import ru.fruzz.extazyy.misc.event.EventManager;
import ru.fruzz.extazyy.misc.event.events.impl.TickEvent;
import ru.fruzz.extazyy.misc.util.Mine;

public abstract class AbstractClientPlayer extends Player {
   private static final String SKIN_URL_TEMPLATE = "http://skins.minecraft.net/MinecraftSkins/%s.png";
   @Nullable
   private PlayerInfo playerInfo;
   protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
   public float elytraRotX;
   public float elytraRotY;
   public float elytraRotZ;
   public final ClientLevel clientLevel;
   private ResourceLocation locationOfCape = null;
   private long reloadCapeTimeMs = 0L;
   private boolean elytraOfCape = false;
   private String nameClear = null;
   public ShoulderRidingEntity entityShoulderLeft;
   public ShoulderRidingEntity entityShoulderRight;
   public ShoulderRidingEntity lastAttachedEntity;
   public float capeRotateX;
   public float capeRotateY;
   public float capeRotateZ;
   private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");

   public AbstractClientPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
      super(pClientLevel, pClientLevel.getSharedSpawnPos(), pClientLevel.getSharedSpawnAngle(), pGameProfile);
      this.clientLevel = pClientLevel;
      this.nameClear = pGameProfile.getName();
      if (this.nameClear != null && !this.nameClear.isEmpty()) {
         this.nameClear = StringUtil.stripColor(this.nameClear);
      }

      CapeUtils.downloadCape(this);
      PlayerConfigurations.getPlayerConfiguration(this);
   }

   public boolean isSpectator() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.getGameMode() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.getGameMode() == GameType.CREATIVE;
   }

   public boolean isCapeLoaded() {
      return this.getPlayerInfo() != null;
   }

   @Nullable
   protected PlayerInfo getPlayerInfo() {
      if (this.playerInfo == null) {
         this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
      }

      return this.playerInfo;
   }

   public void tick() {
      this.deltaMovementOnPreviousTick = this.getDeltaMovement();
      super.tick();
      if (this.lastAttachedEntity != null) {
         RandomEntities.checkEntityShoulder(this.lastAttachedEntity, true);
         this.lastAttachedEntity = null;
      }

   }

   public Vec3 getDeltaMovementLerped(float pPatialTick) {
      return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), (double)pPatialTick);
   }

   public boolean isSkinLoaded() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo != null && playerinfo.isSkinLoaded();
   }

   public ResourceLocation getSkinTextureLocation() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : playerinfo.getSkinLocation();
   }

   @Nullable
   public ResourceLocation getCloakTextureLocation() {
      if (!Config.isShowCapes()) {
         return null;
      } else {
         if (this.reloadCapeTimeMs != 0L && System.currentTimeMillis() > this.reloadCapeTimeMs) {
            CapeUtils.reloadCape(this);
            this.reloadCapeTimeMs = 0L;
            PlayerConfigurations.setPlayerConfiguration(this.getNameClear(), (PlayerConfiguration)null);
         }
            PlayerInfo playerinfo = this.getPlayerInfo();
            if(Mine.mc.player.getDisplayName().getString().equals(playerinfo.getProfile().getName())) {
               return playerinfo.getCapeLocation();
            } else {
               return this.locationOfCape;
            }
      }
   }

   public boolean isElytraLoaded() {
      return this.getPlayerInfo() != null;
   }

   @Nullable
   public ResourceLocation getElytraTextureLocation() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? null : playerinfo.getElytraLocation();
   }

   public static void registerSkinTexture(ResourceLocation pTextureLocation, String pName) {
      TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
      AbstractTexture abstracttexture = texturemanager.getTexture(pTextureLocation, MissingTextureAtlasSprite.getTexture());
      if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
         AbstractTexture abstracttexture1 = new HttpTexture((File)null, String.format(Locale.ROOT, "http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(pName)), DefaultPlayerSkin.getDefaultSkin(UUIDUtil.createOfflinePlayerUUID(pName)), true, (Runnable)null);
         texturemanager.register(pTextureLocation, abstracttexture1);
      }

   }

   public static ResourceLocation getSkinLocation(String pUsername) {
      return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtil.stripColor(pUsername)));
   }

   public String getModelName() {
      PlayerInfo playerinfo = this.getPlayerInfo();
      return playerinfo == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : playerinfo.getModelName();
   }

   public float getFieldOfViewModifier() {
      float f = 1.0F;
      if (this.getAbilities().flying) {
         f *= 1.1F;
      }

      f *= ((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / this.getAbilities().getWalkingSpeed() + 1.0F) / 2.0F;
      if (this.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
         f = 1.0F;
      }

      ItemStack itemstack = this.getUseItem();
      if (this.isUsingItem()) {
         if (itemstack.getItem() instanceof BowItem) {
            int i = this.getTicksUsingItem();
            float f1 = (float)i / 20.0F;
            if (f1 > 1.0F) {
               f1 = 1.0F;
            } else {
               f1 *= f1;
            }

            f *= 1.0F - f1 * 0.15F;
         } else if (Minecraft.getInstance().options.getCameraType().isFirstPerson() && this.isScoping()) {
            return 0.1F;
         }
      }

      return Reflector.ForgeHooksClient_getFieldOfViewModifier.exists() ? Reflector.callFloat(Reflector.ForgeHooksClient_getFieldOfViewModifier, this, f) : Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get().floatValue(), 1.0F, f);
   }

   public String getNameClear() {
      return this.nameClear;
   }

   public ResourceLocation getLocationOfCape() {
      return this.locationOfCape;
   }


   public void setLocationOfCape(ResourceLocation locationOfCape) {
      this.locationOfCape = locationOfCape;
   }

   public void setLocationOfCape(ResourceLocation locationOfCape, Player player) {
      if(Mine.mc.player.equals(player)) {
         this.locationOfCape = locationOfCape;
      } else {
         this.locationOfCape = locationOfCape;
      }
   }

   public boolean hasElytraCape() {
      ResourceLocation resourcelocation = this.getCloakTextureLocation();
      if (resourcelocation == null) {
         return false;
      } else {
         return resourcelocation == this.locationOfCape ? this.elytraOfCape : true;
      }
   }

   public void setElytraOfCape(boolean elytraOfCape) {
      this.elytraOfCape = elytraOfCape;
   }

   public boolean isElytraOfCape() {
      return this.elytraOfCape;
   }

   public long getReloadCapeTimeMs() {
      return this.reloadCapeTimeMs;
   }

   public void setReloadCapeTimeMs(long reloadCapeTimeMs) {
      this.reloadCapeTimeMs = reloadCapeTimeMs;
   }
}