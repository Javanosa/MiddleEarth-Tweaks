package metweaks.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import lotr.common.entity.projectile.LOTREntityCrossbowBolt;
import lotr.common.entity.projectile.LOTREntityDart;
import lotr.common.entity.projectile.LOTREntitySpear;
import lotr.common.entity.projectile.LOTREntityThrowingAxe;
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
import metweaks.features.HireableEnts;
import metweaks.features.MoreMountedBannerBearers;
import metweaks.features.MountedBannerBearers;
import metweaks.features.halftroll.HalfTrollAdditions;
import metweaks.features.isolated.FastTravelHandler;
import metweaks.features.isolated.NpcExplosionProtection;
import metweaks.features.isolated.QuestOfferSync;
import metweaks.features.isolated.WargSpiderFallDamage;
import metweaks.network.HiredAdvInfoPacket;
import metweaks.network.NetworkHandler;
import metweaks.network.SyncedConfig;
import metweaks.physics.HandleExplosion;
import metweaks.physics.PhysicsEntity;
import metweaks.potion.LOTRPotionPoisonKillingFlex;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CommonProxy implements IGuiHandler {
	
	public static int beamSlabRenderID = -999;
	public static int stairsRenderID = -999;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void modifiyUpdateFrequency(Class clazz, int freq) {
		EntityRegistration er = EntityRegistry.instance().lookupModSpawn(clazz, false);
		if(er != null) {
			ReflectionHelper.setPrivateValue(EntityRegistration.class, er, freq, "updateFrequency");
			System.out.println(clazz.getName()+": changed updateFrequency to "+er.getUpdateFrequency());
		}
	}
	
	public void preInit(FMLPreInitializationEvent event) {
		
		MeTweaksConfig.init(event.getSuggestedConfigurationFile());
		
		NetworkHandler.init();
		
		if(ASMConfig.unitOverview) {
			NetworkRegistry.INSTANCE.registerGuiHandler(MeTweaks.instance, this);
		}
		
		
		
			
		new ServerEventsFML();
		new ServerEventsForge();
		
		if((MeTweaks.lotr && ASMConfig.fixQuestofferSync) || ASMConfig.entityTrackerPatches) {
			new QuestOfferSync();
		}
		
		if(MeTweaks.lotr) {
			if(MeTweaksConfig.reduceWargSpiderFalldamage) {
				new WargSpiderFallDamage();
			}
			
			if(ASMConfig.npcExplosionProtection) {
				new NpcExplosionProtection();
			}
			
			if(ASMConfig.moreEvents && (MeTweaks.astikoor || MeTweaks.customNpcs)) {
				new FastTravelHandler();
			}
			
			if(MeTweaksConfig.killPotionID != 30)
				LOTRPotionPoisonKillingFlex.changePotionID(30, MeTweaksConfig.killPotionID);
			
			if(ASMConfig.halfTrollAdditions) {
				HalfTrollAdditions.setup();
			}
			
			if(ASMConfig.mountedBannerBearers) {
				MountedBannerBearers.setup();
			}
			
			if(ASMConfig.moreMountedBannerBearers) {
				MoreMountedBannerBearers.setup();
			}
			
			if(ASMConfig.hireableEnts) {
				new HireableEnts();
			}
			
		}
			

		
		if(MeTweaksConfig.barkBlocks || MeTweaksConfig.verticalSlabs || MeTweaksConfig.woolSlabs || MeTweaksConfig.woolStairs)
			BlocksCore.setupBlocks();
		
		
		
		
		
		if(ASMConfig.explosionPhysics) {
			new HandleExplosion(); 
			PhysicsEntity.replaceVanilla();
		}
	}
	
	
	
	
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
		
		if(MeTweaks.lotr && ASMConfig.projectilePatches && ASMConfig.projectilePatchesReqClient) {
			modifiyUpdateFrequency(LOTREntityCrossbowBolt.class, 20);
			modifiyUpdateFrequency(LOTREntitySpear.class, 20);
			modifiyUpdateFrequency(LOTREntityThrowingAxe.class, 20);
			modifiyUpdateFrequency(LOTREntityDart.class, 20);
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
