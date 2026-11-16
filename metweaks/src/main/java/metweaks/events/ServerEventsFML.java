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
import metweaks.command.CommandConquestDecay;
import metweaks.command.CommandEntityStack;
import metweaks.features.LOTRDateCache;
import metweaks.features.PlayerRidingTracker;
import metweaks.features.ThrowableSpearState;
import metweaks.features.UnitMonitor;
import metweaks.network.NetworkHandler;
import metweaks.network.SyncConquestDecayPacket;
import metweaks.network.UpdateAvailablePacket;
import metweaks.proxy.UpdateCheck;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class ServerEventsFML {
	
	public ServerEventsFML() {
		
		FMLCommonHandler.instance().bus().register(this);
	}
	
	@SubscribeEvent
	public void onServerTick(ServerTickEvent event) {
		if(MeTweaks.lotr && MeTweaksConfig.fixMountsRunningAway) {
			PlayerRidingTracker.processEvent(true);
		}
		
		if(MeTweaks.lotr && ASMConfig.lotrHolidays == 1) {
			LOTRDateCache.updateDates();
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
		if(MeTweaks.lotr && ASMConfig.throwableSpearToggle)
			ThrowableSpearState.setThrowableMode(player, true);
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
			event.player.addChatMessage(UpdateCheck.notifyUpdate(false));
			NetworkHandler.networkWrapper.sendTo(new UpdateAvailablePacket(), (EntityPlayerMP) event.player);
		}
		
		if(MeTweaksConfig.conquestDecay != 3600 && CommandConquestDecay.syncConqDecay()) {
			NetworkHandler.networkWrapper.sendTo(new SyncConquestDecayPacket(), (EntityPlayerMP) event.player);
		}
	}
	
	
	
	
	
	
	
	
	
	
}
