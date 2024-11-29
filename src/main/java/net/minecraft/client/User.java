package net.minecraft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class User {
   private final String name;
   private final String uuid;
   private final String accessToken;
   private final Optional<String> xuid;
   private final Optional<String> clientId;
   private final User.Type type;

   public User(String pName, String pUuid, String pAccessToken, Optional<String> pXuid, Optional<String> pClientId, User.Type pType) {
      this.name = pName;
      this.uuid = pUuid;
      this.accessToken = pAccessToken;
      this.xuid = pXuid;
      this.clientId = pClientId;
      this.type = pType;
   }

   public String getSessionId() {
      return "token:" + this.accessToken + ":" + this.uuid;
   }

   public String getUuid() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public Optional<String> getClientId() {
      return this.clientId;
   }

   public Optional<String> getXuid() {
      return this.xuid;
   }

   @Nullable
   public UUID getProfileId() {
      try {
         return UUIDTypeAdapter.fromString(this.getUuid());
      } catch (IllegalArgumentException illegalargumentexception) {
         return null;
      }
   }

   public GameProfile getGameProfile() {
      return new GameProfile(this.getProfileId(), this.getName());
   }

   public User.Type getType() {
      return this.type;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Type {
      LEGACY("legacy"),
      MOJANG("mojang"),
      MSA("msa");

      private static final Map<String, User.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_92560_) -> {
         return p_92560_.name;
      }, Function.identity()));
      private final String name;

      private Type(String pName) {
         this.name = pName;
      }

      @Nullable
      public static User.Type byName(String pTypeName) {
         return BY_NAME.get(pTypeName.toLowerCase(Locale.ROOT));
      }

      public String getName() {
         return this.name;
      }
   }
}