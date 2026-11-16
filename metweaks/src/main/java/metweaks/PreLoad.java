package metweaks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import metweaks.client.healthbar.HealthBarConfig;

public class PreLoad {
	@SuppressWarnings({ "rawtypes", "unused" })
	public static void preloadClasses() {
		long starttime = System.currentTimeMillis();
		System.out.println("preloading...");
		
		boolean client = FMLCommonHandler.instance().getSide() == Side.CLIENT;
		boolean lotr = MeTweaks.lotr;
		// any feature that isnt disabled and likely to be used is gonna be loaded
		// what mod is loaded
		// server / client,	
		Class c;
		
	// PRE MENU
		if(lotr) {
			if(MeTweaksConfig.fixMountsRunningAway)
				c = metweaks.features.PlayerRidingTracker.class;
			// transformer
			if(ASMConfig.conquestDecay) {
				c = lotr.common.LOTRLevelData.class;
				c = lotr.common.world.map.LOTRConquestZone.class;
			}
				
			if(ASMConfig.allyConquestSpawns || ASMConfig.allyKillReduceConquest) {
				c = lotr.common.world.map.LOTRConquestGrid.class;
			}
			
	// PAST MENU
			
			if(MeTweaksConfig.npcMountStayHome || MeTweaksConfig.ridersAvoidSuffocation) {
				c = metweaks.guards.PositionGenFix.class;
			}
			
			if(MeTweaksConfig.npcMountStayHome) {
				c = metweaks.guards.MountStayHomeAI.class;
			}
			
			if(MeTweaksConfig.ridersAvoidSuffocation) {
				c = metweaks.guards.DontSuffocateAI.class;
			}
			
			if(ASMConfig.aiRangedImprovements) {
				c = metweaks.guards.customranged.HorseMoveToRiderTargetAiFix.class;
			}
			
			
				if(ASMConfig.conquestDecay)
					c = metweaks.command.CommandConquestDecay.class;
				if(ASMConfig.conquestClearCommand)
					c = metweaks.command.CommandConquestClear.class;
				if(MeTweaksConfig.toggleGuardModeHorn)
					c = metweaks.command.CommandGuardModeHorn.class;
				
				if(ASMConfig.lotrEventControl) {
					c = metweaks.command.CommandBanditChance.class;
					c = metweaks.command.CommandTravelingTrader.class;
					c = metweaks.command.CommandLotrInvasions.class;
					
				}
		
		}
		
		if(ASMConfig.entityKillCommand || ASMConfig.entityTpCommand || ASMConfig.entityEffectCommand) {
			c = metweaks.command.CommandEntitySelector.class;
			c = metweaks.command.CommandEntitySelector.Selector.class;
			
			if(ASMConfig.entityKillCommand)
				c = metweaks.command.CommandEntityKill.class;
			if(ASMConfig.entityTpCommand)
				c = metweaks.command.CommandEntityTp.class;
			if(ASMConfig.entityEffectCommand)
				c = metweaks.command.CommandEntityEffect.class;
		}
		
		if(ASMConfig.entityStackCommand)
			c = metweaks.command.CommandEntityStack.class;
		
		if(ASMConfig.giveManyCommand) {
			c = metweaks.command.CommandGiveMany.class;
		}
		
		if(ASMConfig.logPlayerDeathLocations) {
			c = metweaks.features.PlayerDeathLogger.class;
		}
		
		if(ASMConfig.explosionPhysics) {
			c = metweaks.command.CommandExplosionPhysics.class;
			//if(ExplosionPhysicsSettings.fastMoveImpl)
				c = metweaks.physics.FastEntityMovement.class;
			if(lotr)
			try {
				// anonymous inner classes
				c = Class.forName("lotr.common.LOTRBannerProtection$1"); // anyBanner()
				c = Class.forName("lotr.common.LOTRBannerProtection$5"); // forTNT()
				c = Class.forName("lotr.common.LOTRBannerProtection$7"); // forThrown()
			}
			catch(Exception e) {
				
			}
			
		}
		
		if(client) {
			c = metweaks.client.gui.config.MeTweaksGuiFactory.class;
			
			if(ASMConfig.tabListChanges) {
				c = metweaks.client.gui.GuiTabListOverlay.class;
			}
			
			if(ASMConfig.farView) {
				c = metweaks.farview.METLongHashMap.class;
				c = metweaks.farview.METLongHashMap.Bucket.class;
				c = metweaks.farview.FastCraftHelper.class;
				c = metweaks.farview.FastCraftInfo.class;
				c = metweaks.farview.ChunkIterator.class;
				c = metweaks.farview.ChunkIteratorFC.class;
				c = metweaks.farview.ChunkIteratorMET.class;
				c = metweaks.farview.ChunkIteratorFastUtil.class;
				c = metweaks.farview.METLongHashSet.class;
				c = metweaks.farview.METLongHashSet.KeyNode.class;
				c = metweaks.farview.FastUtilLongHashMap.class;
				
				
			}
		}
		
		if(lotr) {
			
			if(MeTweaksConfig.unitTracking)
				c = metweaks.features.UnitMonitor.class;
			if(ASMConfig.aiRangedImprovements || (ASMConfig.guardsWanderRange && ASMConfig.guardsAdvancedSettings))
				c = metweaks.guards.NpcReflectionAccess.class;
			if(ASMConfig.guardsEquipRanged || ASMConfig.aiRangedImprovements) {
				c = lotr.common.entity.ai.LOTREntityAIRangedAttack.class; // seemingly only if server
			}
			
			if(ASMConfig.guardsEquipRanged) {
				c = metweaks.guards.customranged.CustomRanged.class; // seemingly only if server
				c = lotr.common.inventory.LOTRContainerHiredWarriorInventory.class; // seemingly only if server
				c = lotr.common.entity.npc.LOTRInventoryHiredReplacedItems.class; // seemingly only if server
				//c = metweaks.guards.customranged.LOTRSlotRangedWeapon.class; // seemingly only if server
				//c = metweaks.guards.customranged.CustomRanged.Category.class; // seemingly only if server
			}
			if(MeTweaksConfig.aiConquest) {
				c = metweaks.features.AIConquest.class;
			}
			if(ASMConfig.aiRangedImprovements) {
				c = metweaks.guards.customranged.CustomRanged.DefaultDataRanged.class;
			}
			
			if(ASMConfig.guardsWanderRange && ASMConfig.guardsAdvancedSettings) {
				c = metweaks.client.features.GuardsHomeSetup.class;
			}
			
			if(ASMConfig.moreEvents) {
				c = metweaks.events.LOTRFastTravelEvent.class;
				c = metweaks.events.LOTRFastTravelEvent.Success.class;
				c = metweaks.events.LOTRFastTravelEvent.Transport.class;
				
				if(MeTweaks.astikoor) {
					c = metweaks.features.isolated.FastTravelHandlerAstikoor.class;
				}
			}
		}
		
		if(ASMConfig.patchDraughtUse || (lotr && MeTweaksConfig.toggleGuardModeHorn)) {
			c = metweaks.features.ItemUseHandler.class;
		}
		
		
		
		if(client && HealthBarConfig.renderHealth) {
			c = metweaks.client.healthbar.RenderHealthBar.class;
			if(MeTweaks.customNpcs) {
				c = metweaks.client.healthbar.CustomNpcsBridge.class;
			}
			if(MeTweaks.lotrfa) {
				c = metweaks.client.healthbar.LOTRFABridge.class;
			}
			if(MeTweaks.mpm) {
				c = metweaks.client.healthbar.MpmBridge.class;
			}
		}
		
		if(lotr && ASMConfig.unitOverview) {
			if(client) {
				c = metweaks.client.gui.unitoverview.GuiUnitList.class;
				c = metweaks.client.gui.unitoverview.GuiCheckBox.class;
				c = metweaks.client.gui.unitoverview.GuiColumnButton.class;
				c = metweaks.client.gui.unitoverview.GuiUnitList.Entry.class;
				c = metweaks.client.gui.unitoverview.SortGlobal.class;
				c = metweaks.client.gui.unitoverview.SortGlobal.Sorter.class;
				c = metweaks.client.features.NpcMarking.class;
				c = metweaks.client.gui.unitoverview.LOTRGuiHiredWarriorInvKeep.class;
				c = metweaks.client.gui.unitoverview.LOTRGuiHiredFarmerInvKeep.class;
			}
			c = metweaks.client.gui.unitoverview.LOTRHiredWarriorInvKeep.class;
			c = metweaks.client.gui.unitoverview.LOTRHiredFarmerInvKeep.class;
			
		}
		
		if(MeTweaksConfig.alwaysSetBedSpawn || (lotr && MeTweaksConfig.checkBedUnsafe))
			c = metweaks.features.SleepHandler.class;
		
		
		
		// transformer
		if(ASMConfig.syncedconfig)
			c = cpw.mods.fml.common.network.handshake.FMLHandshakeMessage.ModIdData.class;
		if(lotr && (MeTweaksConfig.toggleGuardModeHorn || ASMConfig.guardsWanderRange)) 
			c = metweaks.guards.HiredInfoAccess.class;
		if(lotr && client && ASMConfig.lotrHudPositions)
			c = lotr.client.model.LOTRModelCompass.class;
		if(lotr && ASMConfig.projectilePatches)
			c = net.minecraft.entity.EntityTrackerEntry.class;
		if(lotr && (ASMConfig.hiredsConquerForTheirFaction || ASMConfig.moreEvents)) 
			c = lotr.common.LOTRPlayerData.class;
		if(lotr && ASMConfig.fixServerJoinFreeze)
			c = lotr.common.world.map.LOTRCustomWaypoint.class;
		if(lotr && ASMConfig.bannerProtectionPatches)
			c = lotr.common.LOTRBannerProtection.FilterForPlayer.class;
		if(client && ASMConfig.explosionPhysics) {
			c = net.minecraft.client.network.NetHandlerPlayClient.class;
		}
		if(lotr && client && ASMConfig.fellowshipFFPatches) {
			c = metweaks.core.HooksClient.class;
		}
		
		if(client && ASMConfig.smoothHeadYawMovement) {
			c = net.minecraft.client.entity.EntityOtherPlayerMP.class;
		}
		
		if(ASMConfig.earlyPacketProcessing) {
			c = metweaks.features.NetworkCalls.class;
			c = net.minecraft.network.NetworkManager.class;
		}
		
		if(client && ASMConfig.smoothEntityMovement > 1) {
			c = metweaks.features.Getters.class;
		}
		
		//preloadMoreClasses(lotr, client);
		long endtime = System.currentTimeMillis();
		System.out.println("preloading done ("+(endtime-starttime)+"ms)");
	}
	
	/*@SuppressWarnings({ "rawtypes", "unused" })
	public static void preloadMoreClasses(boolean lotr, boolean client) {
		
		Class c;
		// preload for mainmenu animation
		if(lotr && client && LOTRConfig.customMainMenu) {
			c = cpw.mods.fml.client.FMLConfigGuiFactory.class;
			c = net.minecraft.client.gui.GuiNewChat.class;
			c = net.minecraft.client.gui.GuiStreamIndicator.class;
			c = net.minecraft.client.gui.GuiLanguage.class;
			c = net.minecraft.client.gui.GuiMultiplayer.class;
			c = cpw.mods.fml.client.GuiModList.class;
			c = lotr.client.gui.LOTRGuiMap.class;
			c = net.minecraft.client.gui.ScaledResolution.class;
			c = net.minecraft.client.gui.inventory.GuiContainerCreative.class;
			c = net.minecraft.client.gui.GuiTextField.class;
			c = lotr.client.gui.LOTRGuiFellowships.class;
			c = cpw.mods.fml.common.gameevent.TickEvent.Type.class;
			c = lotr.common.world.map.LOTRCustomWaypoint.class;
			c = lotr.client.gui.LOTRGuiRedBook.class;
			c = lotr.client.render.LOTRCloudRenderer.class;
			c = net.minecraft.scoreboard.ScorePlayerTeam.class;
		}
		
		// world creation menu
		if(client) {
			c = net.minecraft.client.gui.GuiCreateWorld.class;
			c = net.minecraft.client.gui.GuiRenameWorld.class;
			c = net.minecraft.client.gui.GuiErrorScreen.class;
		}
		
		// world join
		
		c = net.minecraft.world.chunk.storage.RegionFileCache.class;
		c = io.netty.util.concurrent.MultithreadEventExecutorGroup.class;
		c = io.netty.channel.MultithreadEventLoopGroup.class;
		c = io.netty.channel.nio.NioEventLoopGroup.class;
		//c = io.netty.util.concurrent.ScheduledFutureTask.class;
		c = io.netty.util.concurrent.SingleThreadEventExecutor.class;
		c = io.netty.channel.nio.NioEventLoop.class;
		c = net.minecraft.command.ServerCommandManager.class;
		c = net.minecraft.world.chunk.storage.AnvilChunkLoader.class;
		c = net.minecraftforge.event.terraingen.PopulateChunkEvent.Pre.class;
		c = net.minecraft.world.gen.ChunkProviderHell.class;
		c = net.minecraft.entity.DataWatcher.class;
		c = net.minecraft.pathfinding.PathNavigate.class;
		
		c = gnu.trove.map.TIntIntMap.class;
		c = gnu.trove.impl.hash.TIntIntHash.class;
		c = gnu.trove.map.hash.TIntIntHashMap.class;
		c = io.netty.bootstrap.AbstractBootstrap.class;
		c = io.netty.bootstrap.ServerBootstrap.class;
		c = io.netty.channel.local.LocalServerChannel.class;
		//c = net.minecraft.network.NetworkSystem$2.class;
		//c = net.minecraft.network.NetworkStatistics.PacketStatData.class;
		c = net.minecraft.network.play.client.C14PacketTabComplete.class;
		c = net.minecraft.network.play.client.C17PacketCustomPayload.class;
		c = net.minecraft.network.status.client.C00PacketServerQuery.class;
		c = net.minecraft.network.status.server.S00PacketServerInfo.class;
		c = net.minecraft.network.login.client.C01PacketEncryptionResponse.class;
		
		c = net.minecraft.server.network.NetHandlerLoginServer.class;
		
		c = net.minecraft.server.management.ItemInWorldManager.class;
		c = net.minecraft.network.PacketBuffer.class;
		c = net.minecraft.inventory.ContainerEnchantment.class;
		c = net.minecraft.inventory.ContainerRepair.class;
		c = net.minecraft.entity.player.InventoryPlayer.class;
		
		c = net.minecraft.stats.StatisticsFile.class;
		//c = io.netty.buffer.UnreleasableByteBuf.class;
		c = io.netty.buffer.CompositeByteBuf.class;
		//c = io.netty.buffer.ReadOnlyUnsafeDirectByteBuf.class;
		
		
		
		
		c = net.minecraft.world.ChunkCache.class;
		c = net.minecraft.pathfinding.PathFinder.class;
		c = gnu.trove.map.hash.TIntByteHashMap.class;
		
		
		c = net.minecraft.enchantment.EnchantmentHelper.class;
		c = net.minecraftforge.common.ISpecialArmor.ArmorProperties.class;
		
		
		
		if(client) {
			c = net.minecraft.client.shader.ShaderGroup.class;
			c = net.minecraft.client.network.NetHandlerLoginClient.class;
			c = net.minecraft.client.network.NetHandlerPlayClient.class;
			//c = cpw.mods.fml.common.network.handshake.FMLHandshakeClientState.class;
			c = net.minecraft.client.multiplayer.PlayerControllerMP.class;
			c = net.minecraft.client.renderer.ActiveRenderInfo.class;
			c = net.minecraft.client.util.QuadComparator.class;
			c = net.minecraft.client.gui.achievement.GuiAchievements.class;
			c = net.minecraft.client.gui.achievement.GuiStats.class;
		}
		// lotr
		if(lotr) {
			c = lotr.common.entity.npc.LOTRFamilyInfo.class;
			c = lotr.common.entity.npc.LOTREntityQuestInfo.class;
			c = lotr.common.inventory.LOTRContainerAnvil.class;
			c = lotr.common.entity.npc.LOTRTradeEntries.class;
			c = lotr.common.entity.ai.LOTREntityAIFarm.class;
			
			// lotr client
			if(client) {
				c = lotr.client.render.LOTRRenderNorthernLights.class;
				c = lotr.client.render.entity.LOTRNPCRendering.class;
			}
		}
		
	}*/
}
