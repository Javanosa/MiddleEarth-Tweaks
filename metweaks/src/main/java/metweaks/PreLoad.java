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
		
		if(client)
			c = metweaks.client.gui.config.MeTweaksGuiFactory.class;
		
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
				c = metweaks.guards.customranged.LOTRSlotRangedWeapon.class; // seemingly only if server
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
		
		
		
		long endtime = System.currentTimeMillis();
		System.out.println("preloading done ("+(endtime-starttime)+"ms)");
	}
}
