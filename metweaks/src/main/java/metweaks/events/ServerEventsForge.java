package metweaks.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.command.CommandEntityStack;
import metweaks.features.AIConquest;
import metweaks.features.SleepHandler;
import metweaks.features.ItemUseHandler;
import metweaks.features.ModifierHandler;
import metweaks.guards.DontSuffocateAI;
import metweaks.guards.MountStayHomeAI;
import metweaks.guards.customranged.HorseMoveToRiderTargetAiFix;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

public class ServerEventsForge {
	public ServerEventsForge() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event) {
		if(MeTweaksConfig.aiConquest && !event.entity.worldObj.isRemote) {
			AIConquest.handleLivingDeath(event);
		}
	}
	
	@SubscribeEvent
	public void onPlayerSleep(PlayerSleepInBedEvent event) {
		if(MeTweaksConfig.alwaysSetBedSpawn || (MeTweaks.lotr && MeTweaksConfig.checkBedUnsafe))
			SleepHandler.handlePlayerSleep(event);
	}
	
	
	
	
	@SubscribeEvent
	public void onItemUseStart(PlayerUseItemEvent.Start event) {
		if(MeTweaksConfig.toggleGuardModeHorn && MeTweaks.lotr) {
			ItemUseHandler.handleItemUseStart(event);
		}
	}
	
	@SubscribeEvent
	public void onItemUseFinish(PlayerUseItemEvent.Finish event) {
		if(ASMConfig.patchDraughtUse || (MeTweaks.lotr && MeTweaksConfig.toggleGuardModeHorn)) {
			ItemUseHandler.handleItemUseFinish(event);
		}
	}
	
	
	
	@SubscribeEvent(receiveCanceled = true) // for stacking command when clicking onto hireds
	public void onEntityInteract(EntityInteractEvent event) {
		if(MeTweaksConfig.modifySpeed)
			ModifierHandler.checkspeedmods(event.target, event.target.worldObj, true);
		if(ASMConfig.entityStackCommand && !event.entityPlayer.worldObj.isRemote)
			CommandEntityStack.processInteract(event);
	}
	
	
	
	@SubscribeEvent
	public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
		
		if(MeTweaksConfig.modifySpeed)
			ModifierHandler.checkspeedmods(event.entity, event.world, false);
		
		if(MeTweaks.lotr) {
			if(MeTweaksConfig.npcMountStayHome)
				MountStayHomeAI.applyHiredMountHome(event.entity);
			
			if(MeTweaksConfig.ridersAvoidSuffocation)
				DontSuffocateAI.applyDontSuffocate(event.entity);
			
			if(ASMConfig.aiRangedImprovements)
				HorseMoveToRiderTargetAiFix.apply(event.entity);
		}
	}
}
