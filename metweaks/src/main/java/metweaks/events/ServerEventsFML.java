package metweaks.events;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.block.VerticalSlab;
import metweaks.command.CommandEntityStack;
import metweaks.features.PlayerRidingTracker;
import metweaks.features.UnitMonitor;
import metweaks.proxy.UpdateCheck;
import net.minecraft.entity.player.EntityPlayer;

public class ServerEventsFML {
	
	public ServerEventsFML() {
		
		FMLCommonHandler.instance().bus().register(this);
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if(MeTweaks.lotr && MeTweaksConfig.fixMountsRunningAway) {
			PlayerRidingTracker.processEvent(true);
		}
	}
	
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event) {
		if(MeTweaks.lotr && MeTweaksConfig.unitTracking && event.phase == Phase.START && event.side == Side.SERVER) {
			UnitMonitor.tick(event.world);
		}
	}
	
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
		EntityPlayer player = event.player;
		if(MeTweaksConfig.verticalSlabs)
			VerticalSlab.setVerticalMode(player, false);
		if(MeTweaks.lotr && MeTweaksConfig.unitTracking)
			UnitMonitor.setUnitTrackingMode(player, 0); // unset
		if(ASMConfig.entityStackCommand && CommandEntityStack.entitystack_states != null)
			CommandEntityStack.entitystack_states.remove(player.getUniqueID());
		if(MeTweaks.lotr && MeTweaksConfig.fixMountsRunningAway)
			PlayerRidingTracker.leave(player);
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		if(MeTweaksConfig.updateCheck && UpdateCheck.LATEST != null && UpdateCheck.canNotify(event.player)) {
			event.player.addChatMessage(UpdateCheck.notifyUpdate());
		}
	}
	
	
	
	
	
	
	
	
	
	
}
