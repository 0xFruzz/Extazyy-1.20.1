package net.minecraft.network.protocol.game;

public interface ServerGamePacketListener extends ServerPacketListener {
   void handleAnimate(ServerboundSwingPacket pPacket);

   void handleChat(ServerboundChatPacket pPacket);

   void handleChatCommand(ServerboundChatCommandPacket pPacket);

   void handleChatAck(ServerboundChatAckPacket pPacket);

   void handleClientCommand(ServerboundClientCommandPacket pPacket);

   void handleClientInformation(ServerboundClientInformationPacket pPacket);

   void handleContainerButtonClick(ServerboundContainerButtonClickPacket pPacket);

   void handleContainerClick(ServerboundContainerClickPacket pPacket);

   void handlePlaceRecipe(ServerboundPlaceRecipePacket pPacket);

   void handleContainerClose(ServerboundContainerClosePacket pPacket);

   void handleCustomPayload(ServerboundCustomPayloadPacket pPacket);

   void handleInteract(ServerboundInteractPacket pPacket);

   void handleKeepAlive(ServerboundKeepAlivePacket pPacket);

   void handleMovePlayer(ServerboundMovePlayerPacket pPacket);

   void handlePong(ServerboundPongPacket pPacket);

   void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket pPacket);

   void handlePlayerAction(ServerboundPlayerActionPacket pPacket);

   void handlePlayerCommand(ServerboundPlayerCommandPacket pPacket);

   void handlePlayerInput(ServerboundPlayerInputPacket pPacket);

   void handleSetCarriedItem(ServerboundSetCarriedItemPacket pPacket);

   void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket pPacket);

   void handleSignUpdate(ServerboundSignUpdatePacket pPacket);

   void handleUseItemOn(ServerboundUseItemOnPacket pPacket);

   void handleUseItem(ServerboundUseItemPacket pPacket);

   void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket pPacket);

   void handleResourcePackResponse(ServerboundResourcePackPacket pPacket);

   void handlePaddleBoat(ServerboundPaddleBoatPacket pPacket);

   void handleMoveVehicle(ServerboundMoveVehiclePacket pPacket);

   void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket pPacket);

   void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket pPacket);

   void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket pPacket);

   void handleSeenAdvancements(ServerboundSeenAdvancementsPacket pPacket);

   void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket pPacket);

   void handleSetCommandBlock(ServerboundSetCommandBlockPacket pPacket);

   void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket pPacket);

   void handlePickItem(ServerboundPickItemPacket pPacket);

   void handleRenameItem(ServerboundRenameItemPacket pPacket);

   void handleSetBeaconPacket(ServerboundSetBeaconPacket pPacket);

   void handleSetStructureBlock(ServerboundSetStructureBlockPacket pPacket);

   void handleSelectTrade(ServerboundSelectTradePacket pPacket);

   void handleEditBook(ServerboundEditBookPacket pPacket);

   void handleEntityTagQuery(ServerboundEntityTagQuery pPacket);

   void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery pPacket);

   void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket pPacket);

   void handleJigsawGenerate(ServerboundJigsawGeneratePacket pPacket);

   void handleChangeDifficulty(ServerboundChangeDifficultyPacket pPacket);

   void handleLockDifficulty(ServerboundLockDifficultyPacket pPacket);

   void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket pPacket);
}