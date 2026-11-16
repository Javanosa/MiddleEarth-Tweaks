package metweaks.proxy;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.client.Keys;
import metweaks.client.LOTRGuiElements;
import metweaks.client.features.MeTweaksRenderBlocks;
import metweaks.client.gui.GuiUnitCountsOverlay;
import metweaks.client.gui.TosNoticeReader;
import metweaks.client.gui.config.GuiConfigConfirm;
import metweaks.client.healthbar.HealthBarConfig;
import metweaks.command.ClientCommandTOSNotice;
import metweaks.command.ClientCommandDebug;
import metweaks.command.ClientCommandFarView;
import metweaks.events.ClientEvents;
import metweaks.events.GuardEvents;
import metweaks.guards.customranged.CustomRanged;
import metweaks.network.HiredAdvInfoPacket;
import metweaks.network.SyncedConfig;
import metweaks.physics.RenderPhysicsEntityFast;
import metweaks.physics.PhysicsEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ClientCommandHandler;

public class ClientProxy extends CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		
		Keys.init();
		
		new ClientEvents();
		
		
		if(MeTweaks.lotr) {
			new GuardEvents();
			if(ASMConfig.guardsEquipRanged) {
				new CustomRanged();
			}
			RequireLotr.preInit();
		}
		
		
		
		if(ASMConfig.farView)
			ClientCommandHandler.instance.registerCommand(new ClientCommandFarView());
		
		if(MeTweaksConfig.tosNotice >= 2)
			ClientCommandHandler.instance.registerCommand(new ClientCommandTOSNotice());
		
		if(ASMConfig.debugModeCommand) {
			ClientCommandHandler.instance.registerCommand(new ClientCommandDebug());
		}
		
		
		
		if(ASMConfig.explosionPhysics) {
			RenderingRegistry.registerEntityRenderingHandler(PhysicsEntity.class, new RenderPhysicsEntityFast());
			
		}
	}
	
	public void init(FMLInitializationEvent event) {
		super.init(event);
		if(MeTweaks.lotr && MeTweaksConfig.fixSpawnerInvenstoryLag) {
			RequireLotr.init();
		}
		
		if(MeTweaks.lotr && MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs) {
			CommonProxy.beamSlabRenderID = RenderingRegistry.getNextAvailableRenderId();
			RenderingRegistry.registerBlockHandler(CommonProxy.beamSlabRenderID, new MeTweaksRenderBlocks());
		}
		
		if(MeTweaksConfig.woolStairs || (MeTweaksConfig.barkBlocks && MeTweaksConfig.barkStairs)) {
			CommonProxy.stairsRenderID = RenderingRegistry.getNextAvailableRenderId();
			RenderingRegistry.registerBlockHandler(CommonProxy.stairsRenderID, new MeTweaksRenderBlocks());
		}
		
		if(MeTweaksConfig.tosNotice > 0) {
			TosNoticeReader.fetchTOSNotice();
		}
	}
		
	public void openConfigConfirmGUI(SyncedConfig config) {
		GuiConfigConfirm.openConfigConfirmGUI(config);
	}
	
	public void loadClientConfig() {
		HealthBarConfig.loadConfig();
		LOTRGuiElements.loadConfig();
		GuiUnitCountsOverlay.loadConfig();
	}
	
	public void openGuiHiredAdvSettings(HiredAdvInfoPacket packet, LOTREntityNPC npc) {
		RequireLotr.openGuiHiredAdvSettings(packet, npc);
	}
	
	public void openGuiGuardmodeHorn(ItemStack item) {
		RequireLotr.openGuiGuardmodeHorn(item);
	}
	
	
	
	
}
