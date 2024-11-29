package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.optifine.Config;
import net.optifine.CustomItems;
import net.optifine.reflect.Reflector;
import net.optifine.util.TextureUtils;

public class HumanoidArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
   private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
   private final A innerModel;
   private final A outerModel;
   private final TextureAtlas armorTrimAtlas;

   public HumanoidArmorLayer(RenderLayerParent<T, M> pRenderer, A pInnerModel, A pOuterModel, ModelManager pModelManager) {
      super(pRenderer);
      this.innerModel = pInnerModel;
      this.outerModel = pOuterModel;
      this.armorTrimAtlas = pModelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.CHEST, pPackedLight, this.getArmorModel(EquipmentSlot.CHEST));
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.LEGS, pPackedLight, this.getArmorModel(EquipmentSlot.LEGS));
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.FEET, pPackedLight, this.getArmorModel(EquipmentSlot.FEET));
      this.renderArmorPiece(pPoseStack, pBuffer, pLivingEntity, EquipmentSlot.HEAD, pPackedLight, this.getArmorModel(EquipmentSlot.HEAD));
   }

   private void renderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, T pLivingEntity, EquipmentSlot pSlot, int pPackedLight, A pModel) {
      ItemStack itemstack = pLivingEntity.getItemBySlot(pSlot);
      Item item = itemstack.getItem();
      if (item instanceof ArmorItem armoritem) {
         if (armoritem.getEquipmentSlot() == pSlot) {
            this.getParentModel().copyPropertiesTo(pModel);
            this.setPartVisibility(pModel, pSlot);
            Model model = this.getArmorModelHook(pLivingEntity, itemstack, pSlot, pModel);
            boolean flag = this.usesInnerModel(pSlot);
            if (armoritem instanceof DyeableLeatherItem) {
               DyeableLeatherItem dyeableleatheritem = (DyeableLeatherItem)armoritem;
               int i = dyeableleatheritem.getColor(itemstack);
               float f = (float)(i >> 16 & 255) / 255.0F;
               float f1 = (float)(i >> 8 & 255) / 255.0F;
               float f2 = (float)(i & 255) / 255.0F;
               this.renderModel(pPoseStack, pBuffer, pPackedLight, armoritem, pModel, flag, f, f1, f2, this.getArmorResource(pLivingEntity, itemstack, pSlot, (String)null));
               this.renderModel(pPoseStack, pBuffer, pPackedLight, armoritem, pModel, flag, 1.0F, 1.0F, 1.0F, this.getArmorResource(pLivingEntity, itemstack, pSlot, "overlay"));
            } else {
               this.renderModel(pPoseStack, pBuffer, pPackedLight, armoritem, pModel, flag, 1.0F, 1.0F, 1.0F, this.getArmorResource(pLivingEntity, itemstack, pSlot, (String)null));
            }

            ArmorTrim.getTrim(pLivingEntity.level().registryAccess(), itemstack).ifPresent((armorTrimIn) -> {
               this.renderTrim(armoritem.getMaterial(), pPoseStack, pBuffer, pPackedLight, armorTrimIn, pModel, flag);
            });
            if (itemstack.hasFoil()) {
               this.renderGlint(pPoseStack, pBuffer, pPackedLight, pModel);
            }
         }
      }

   }

   protected void setPartVisibility(A pModel, EquipmentSlot pSlot) {
      pModel.setAllVisible(false);
      switch (pSlot) {
         case HEAD:
            pModel.head.visible = true;
            pModel.hat.visible = true;
            break;
         case CHEST:
            pModel.body.visible = true;
            pModel.rightArm.visible = true;
            pModel.leftArm.visible = true;
            break;
         case LEGS:
            pModel.body.visible = true;
            pModel.rightLeg.visible = true;
            pModel.leftLeg.visible = true;
            break;
         case FEET:
            pModel.rightLeg.visible = true;
            pModel.leftLeg.visible = true;
      }

   }

   private void renderModel(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ArmorItem pArmorItem, A pModel, boolean pWithGlint, float pRed, float pGreen, float pBlue, @Nullable String pArmorSuffix) {
      this.renderModel(pPoseStack, pBuffer, pPackedLight, pArmorItem, pModel, pWithGlint, pRed, pGreen, pBlue, this.getArmorLocation(pArmorItem, pWithGlint, pArmorSuffix));
   }

   private void renderModel(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, ArmorItem itemIn, A bipedModelIn, boolean isLegSlot, float red, float green, float blue, ResourceLocation armorResource) {
      VertexConsumer vertexconsumer = bufferIn.getBuffer(RenderType.armorCutoutNoCull(armorResource));
      bipedModelIn.renderToBuffer(matrixStackIn, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, red, green, blue, 1.0F);
   }

   private void renderTrim(ArmorMaterial pArmorMaterial, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ArmorTrim pTrim, A pModel, boolean pInnerTexture) {
      TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(pInnerTexture ? pTrim.innerTexture(pArmorMaterial) : pTrim.outerTexture(pArmorMaterial));
      textureatlassprite = TextureUtils.getCustomSprite(textureatlassprite);
      VertexConsumer vertexconsumer = textureatlassprite.wrap(pBuffer.getBuffer(Sheets.armorTrimsSheet()));
      pModel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderGlint(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, A pModel) {
      pModel.renderToBuffer(pPoseStack, pBuffer.getBuffer(RenderType.armorEntityGlint()), pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   private A getArmorModel(EquipmentSlot pSlot) {
      return (A)(this.usesInnerModel(pSlot) ? this.innerModel : this.outerModel);
   }

   private boolean usesInnerModel(EquipmentSlot pSlot) {
      return pSlot == EquipmentSlot.LEGS;
   }

   private ResourceLocation getArmorLocation(ArmorItem pArmorItem, boolean pLayer2, @Nullable String pSuffix) {
      String s = "textures/models/armor/" + pArmorItem.getMaterial().getName() + "_layer_" + (pLayer2 ? 2 : 1) + (pSuffix == null ? "" : "_" + pSuffix) + ".png";
      return ARMOR_LOCATION_CACHE.computeIfAbsent(s, ResourceLocation::new);
   }

   protected Model getArmorModelHook(T entity, ItemStack itemStack, EquipmentSlot slot, A model) {
      return (Model)(Reflector.ForgeHooksClient_getArmorModel.exists() ? (Model)Reflector.ForgeHooksClient_getArmorModel.call(entity, itemStack, slot, model) : model);
   }

   public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, String type) {
      ArmorItem armoritem = (ArmorItem)stack.getItem();
      String s = armoritem.getMaterial().getName();
      String s1 = "minecraft";
      int i = s.indexOf(58);
      if (i != -1) {
         s1 = s.substring(0, i);
         s = s.substring(i + 1);
      }

      String s2 = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", s1, s, this.usesInnerModel(slot) ? 2 : 1, type == null ? "" : String.format(Locale.ROOT, "_%s", type));
      if (Reflector.ForgeHooksClient_getArmorTexture.exists()) {
         s2 = Reflector.callString(Reflector.ForgeHooksClient_getArmorTexture, entity, stack, s2, slot, type);
      }

      ResourceLocation resourcelocation = ARMOR_LOCATION_CACHE.get(s2);
      if (resourcelocation == null) {
         resourcelocation = new ResourceLocation(s2);
         ARMOR_LOCATION_CACHE.put(s2, resourcelocation);
      }

      if (Config.isCustomItems()) {
         resourcelocation = CustomItems.getCustomArmorTexture(stack, slot, type, resourcelocation);
      }

      return resourcelocation;
   }
}