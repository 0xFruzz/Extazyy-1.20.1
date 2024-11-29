package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

public interface ClientGamePacketListener extends PacketListener {
   void handleAddEntity(ClientboundAddEntityPacket pPacket);

   void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket pPacket);

   void handleAddObjective(ClientboundSetObjectivePacket pPacket);

   void handleAddPlayer(ClientboundAddPlayerPacket pPacket);

   void handleAnimate(ClientboundAnimatePacket pPacket);

   void handleHurtAnimation(ClientboundHurtAnimationPacket pPacket);

   void handleAwardStats(ClientboundAwardStatsPacket pPacket);

   void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket);

   void handleBlockDestruction(ClientboundBlockDestructionPacket pPacket);

   void handleOpenSignEditor(ClientboundOpenSignEditorPacket pPacket);

   void handleBlockEntityData(ClientboundBlockEntityDataPacket pPacket);

   void handleBlockEvent(ClientboundBlockEventPacket pPacket);

   void handleBlockUpdate(ClientboundBlockUpdatePacket pPacket);

   void handleSystemChat(ClientboundSystemChatPacket pPacket);

   void handlePlayerChat(ClientboundPlayerChatPacket pPacket);

   void handleDisguisedChat(ClientboundDisguisedChatPacket pPacket);

   void handleDeleteChat(ClientboundDeleteChatPacket pPacket);

   void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket pPacket);

   void handleMapItemData(ClientboundMapItemDataPacket pPacket);

   void handleContainerClose(ClientboundContainerClosePacket pPacket);

   void handleContainerContent(ClientboundContainerSetContentPacket pPacket);

   void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket pPacket);

   void handleContainerSetData(ClientboundContainerSetDataPacket pPacket);

   void handleContainerSetSlot(ClientboundContainerSetSlotPacket pPacket);

   void handleCustomPayload(ClientboundCustomPayloadPacket pPacket);

   void handleDisconnect(ClientboundDisconnectPacket pPacket);

   void handleEntityEvent(ClientboundEntityEventPacket pPacket);

   void handleEntityLinkPacket(ClientboundSetEntityLinkPacket pPacket);

   void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket);

   void handleExplosion(ClientboundExplodePacket pPacket);

   void handleGameEvent(ClientboundGameEventPacket pPacket);

   void handleKeepAlive(ClientboundKeepAlivePacket pPacket);

   void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket pPacket);

   void handleChunksBiomes(ClientboundChunksBiomesPacket pPacket);

   void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket);

   void handleLevelEvent(ClientboundLevelEventPacket pPacket);

   void handleLogin(ClientboundLoginPacket pPacket);

   void handleMoveEntity(ClientboundMoveEntityPacket pPacket);

   void handleMovePlayer(ClientboundPlayerPositionPacket pPacket);

   void handleParticleEvent(ClientboundLevelParticlesPacket pPacket);

   void handlePing(ClientboundPingPacket pPacket);

   void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket);

   void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket pPacket);

   void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket pPacket);

   void handleRemoveEntities(ClientboundRemoveEntitiesPacket pPacket);

   void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket pPacket);

   void handleRespawn(ClientboundRespawnPacket pPacket);

   void handleRotateMob(ClientboundRotateHeadPacket pPacket);

   void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket);

   void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket);

   void handleSetEntityData(ClientboundSetEntityDataPacket pPacket);

   void handleSetEntityMotion(ClientboundSetEntityMotionPacket pPacket);

   void handleSetEquipment(ClientboundSetEquipmentPacket pPacket);

   void handleSetExperience(ClientboundSetExperiencePacket pPacket);

   void handleSetHealth(ClientboundSetHealthPacket pPacket);

   void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket pPacket);

   void handleSetScore(ClientboundSetScorePacket pPacket);

   void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket pPacket);

   void handleSetTime(ClientboundSetTimePacket pPacket);

   void handleSoundEvent(ClientboundSoundPacket pPacket);

   void handleSoundEntityEvent(ClientboundSoundEntityPacket pPacket);

   void handleTakeItemEntity(ClientboundTakeItemEntityPacket pPacket);

   void handleTeleportEntity(ClientboundTeleportEntityPacket pPacket);

   void handleUpdateAttributes(ClientboundUpdateAttributesPacket pPacket);

   void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket);

   void handleUpdateTags(ClientboundUpdateTagsPacket pPacket);

   void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket pPacket);

   void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket pPacket);

   void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket pPacket);

   void handleChangeDifficulty(ClientboundChangeDifficultyPacket pPacket);

   void handleSetCamera(ClientboundSetCameraPacket pPacket);

   void handleInitializeBorder(ClientboundInitializeBorderPacket pPacket);

   void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket pPacket);

   void handleSetBorderSize(ClientboundSetBorderSizePacket pPacket);

   void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket pPacket);

   void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket pPacket);

   void handleSetBorderCenter(ClientboundSetBorderCenterPacket pPacket);

   void handleTabListCustomisation(ClientboundTabListPacket pPacket);

   void handleResourcePack(ClientboundResourcePackPacket pPacket);

   void handleBossUpdate(ClientboundBossEventPacket pPacket);

   void handleItemCooldown(ClientboundCooldownPacket pPacket);

   void handleMoveVehicle(ClientboundMoveVehiclePacket pPacket);

   void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket pPacket);

   void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket pPacket);

   void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket);

   void handleCommands(ClientboundCommandsPacket pPacket);

   void handleStopSoundEvent(ClientboundStopSoundPacket pPacket);

   void handleCommandSuggestions(ClientboundCommandSuggestionsPacket pPacket);

   void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket);

   void handleLookAt(ClientboundPlayerLookAtPacket pPacket);

   void handleTagQueryPacket(ClientboundTagQueryPacket pPacket);

   void handleLightUpdatePacket(ClientboundLightUpdatePacket pPacket);

   void handleOpenBook(ClientboundOpenBookPacket pPacket);

   void handleOpenScreen(ClientboundOpenScreenPacket pPacket);

   void handleMerchantOffers(ClientboundMerchantOffersPacket pPacket);

   void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket pPacket);

   void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket pPacket);

   void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket pPacket);

   void handleBlockChangedAck(ClientboundBlockChangedAckPacket pPacket);

   void setActionBarText(ClientboundSetActionBarTextPacket pPacket);

   void setSubtitleText(ClientboundSetSubtitleTextPacket pPacket);

   void setTitleText(ClientboundSetTitleTextPacket pPacket);

   void setTitlesAnimation(ClientboundSetTitlesAnimationPacket pPacket);

   void handleTitlesClear(ClientboundClearTitlesPacket pPacket);

   void handleServerData(ClientboundServerDataPacket pPacket);

   void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket pPacket);

   void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket pPacket);

   void handleBundlePacket(ClientboundBundlePacket pPacket);

   void handleDamageEvent(ClientboundDamageEventPacket pPacket);
}