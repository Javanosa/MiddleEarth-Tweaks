package metweaks.proxy;

import java.util.List;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import lotr.common.fac.LOTRControlZone;
import lotr.common.fac.LOTRFaction;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.block.BlocksCore;
import metweaks.block.BlocksItemsInit;
import metweaks.block.VerticalSlab;
import metweaks.client.gui.unitoverview.LOTRGuiHiredFarmerInvKeep;
import metweaks.client.gui.unitoverview.LOTRGuiHiredWarriorInvKeep;
import metweaks.client.gui.unitoverview.LOTRHiredFarmerInvKeep;
import metweaks.client.gui.unitoverview.LOTRHiredWarriorInvKeep;
import metweaks.events.ServerEventsFML;
import metweaks.events.ServerEventsForge;
import metweaks.features.isolated.NpcExplosionProtection;
import metweaks.features.isolated.QuestOfferSync;
import metweaks.features.isolated.WargSpiderFallDamage;
import metweaks.network.HiredAdvInfoPacket;
import metweaks.network.NetworkHandler;
import metweaks.network.SyncedConfig;
import metweaks.potion.LOTRPotionPoisonKillingFlex;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler {
	
	public static int beamSlabRenderID = -999;
	
	public void preInit(FMLPreInitializationEvent event) {
		
		MeTweaksConfig.init(event.getSuggestedConfigurationFile());
		
		NetworkHandler.init();
		
		if(ASMConfig.unitOverview) {
			NetworkRegistry.INSTANCE.registerGuiHandler(MeTweaks.instance, this);
		}
		
		//List<LOTRControlZone> list = LOTRFaction.HALF_TROLL.getControlZones();
		//list.add(new LOTRControlZone(1421, 2873, 200));
		
			
		new ServerEventsFML();
		new ServerEventsForge();
		
		
		if(MeTweaks.lotr) {
			if(MeTweaksConfig.reduceWargSpiderFalldamage) {
				new WargSpiderFallDamage();
			}
			
			if(ASMConfig.fixQuestofferSync) {
				new QuestOfferSync();
			}
			
			if(ASMConfig.npcExplosionProtection) {
				new NpcExplosionProtection();
			}
			
			
			if(MeTweaksConfig.killPotionID != 30)
				LOTRPotionPoisonKillingFlex.changePotionID(30, MeTweaksConfig.killPotionID);
			
			
		}
			

		
		if(MeTweaksConfig.barkBlocks || MeTweaksConfig.verticalSlabs || MeTweaksConfig.woolSlabs)
			BlocksCore.setupBlocks();

	}
	
	
	
	@SuppressWarnings({ })
	public void init(FMLInitializationEvent event) {
		
		
		if(ASMConfig.disableTinCopperSilverGen && MeTweaks.lotr) {
			BlocksItemsInit.disableTinCopperSilverGen();
		}
		
		
		// do this before verticalslabs harvest init to make it copy the desired settings
		if(ASMConfig.improveBlockBreakSpeeds) {
			BlocksItemsInit.improveBlockBreakSpeeds();
		}
		
		
		
		if(ASMConfig.foodConsumeDurations) {
			BlocksItemsInit.foodConsumeDurations();
		}
		
		
		if(MeTweaks.lotr && ASMConfig.draughtMaxStacksize > 1) {
			BlocksItemsInit.draughtMaxStacksize();
		}
		
		if(ASMConfig.stewMaxStacksize > 1) {
			int normal = ASMConfig.stewMaxStacksize;
			Items.mushroom_stew.setMaxStackSize(normal);
			if(MeTweaks.lotr) {
				LOTRMod.rabbitStew.setMaxStackSize(Math.max(ASMConfig.stewMaxStacksize / 2, 1));
				LOTRMod.torogStew.setMaxStackSize(Math.max(ASMConfig.stewMaxStacksize / 4, 1));
				LOTRMod.melonSoup.setMaxStackSize(normal);
				LOTRMod.leekSoup.setMaxStackSize(normal);
			}
		}
		
		
		
		if(MeTweaksConfig.verticalSlabs) {
			VerticalSlab.setupHarvestLvlsGlobal();
		}
		
		
	}

	public void openConfigConfirmGUI(SyncedConfig config) {}
	
	public void openVerticalSlabsDisabledWarn() {}
	
	public void loadClientConfig() {}

	public void openGuiHiredAdvSettings(HiredAdvInfoPacket packet, LOTREntityNPC npc) {}
	
	public void openGuiGuardmodeHorn(ItemStack item) {}
	

	
	public static final int GUI_ID_WARRIOR_INV_KEEP = 0;
	public static final int GUI_ID_FARMER_INV_KEEP = 1;
	
	
	
	private static LOTREntityNPC getSafeHired(int id, World world, EntityPlayer player, Task task) {
		Entity entity = world.getEntityByID(id);
	    if(entity instanceof LOTREntityNPC) {
	        LOTREntityNPC npc = (LOTREntityNPC)entity;
	        if(npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == player && npc.hiredNPCInfo.getTask() == task)
	          return npc; 
	      }
		return null; 
	}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int i, int j, int k) {
		if(ID == GUI_ID_FARMER_INV_KEEP) {
			LOTREntityNPC npc = getSafeHired(i, world, player, Task.FARMER);
			if(npc != null) return new LOTRHiredFarmerInvKeep(player.inventory, npc); 
		}
		else if(ID == GUI_ID_WARRIOR_INV_KEEP) {
			LOTREntityNPC npc = getSafeHired(i, world, player, Task.WARRIOR);
			if(npc != null) return new LOTRHiredWarriorInvKeep(player.inventory, npc); 
		} 
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int i, int j, int k) {
		if(ID == GUI_ID_FARMER_INV_KEEP) {
			LOTREntityNPC npc = getSafeHired(i, world, player, Task.FARMER);
			if(npc != null) return new LOTRGuiHiredFarmerInvKeep(player.inventory, npc); 
		}
		else if(ID == GUI_ID_WARRIOR_INV_KEEP) {
			LOTREntityNPC npc = getSafeHired(i, world, player, Task.WARRIOR);
			if(npc != null) return new LOTRGuiHiredWarriorInvKeep(player.inventory, npc); 
		}
		return null; 
	}
}
