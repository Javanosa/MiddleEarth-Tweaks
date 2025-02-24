package metweaks.features;

import java.util.List;

import lotr.common.LOTRMod;
import lotr.common.LOTRSquadrons;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import lotr.common.item.LOTRItemEntDraught;
import lotr.common.item.LOTRItemMug;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.guards.HiredInfoAccess;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;

public class ItemUseHandler {
	public static final int TURN_ON_GUARDMODE = 4;
	public static final int TURN_OFF_GUARDMODE = 5;
	
	public static void handleItemUseStart(PlayerUseItemEvent.Start event) {
		if(event.item.getItem() == LOTRMod.commandHorn && event.entityPlayer.isSneaking()) {
			int meta = event.item.getItemDamage();
			if(meta == TURN_ON_GUARDMODE || meta == TURN_OFF_GUARDMODE) {
				if(event.entityPlayer.worldObj.isRemote) {
					MeTweaks.proxy.openGuiGuardmodeHorn(event.item);
				}
				event.setCanceled(true);
			}
		}
	}
	
	public static void handleItemUseFinish(PlayerUseItemEvent.Finish event) {
		ItemStack stack = event.item;
		if(ASMConfig.patchDraughtUse) {
			Item food = stack.getItem();
			boolean mug = MeTweaks.lotr && food instanceof LOTRItemMug;
			boolean onlyFood = food == Items.mushroom_stew || (MeTweaks.lotr && (food == LOTRMod.rabbitStew || food == LOTRMod.torogStew || food == LOTRMod.melonSoup || food == LOTRMod.leekSoup));
			if(mug || onlyFood || (MeTweaks.lotr && food instanceof LOTRItemEntDraught)) {
				EntityPlayer player = event.entityPlayer;
				if((stack.stackSize > (onlyFood ? 0 : 1)) && !player.capabilities.isCreativeMode) {
					// stacksize is already done for food
					if(!onlyFood)
						stack.stackSize--;
					ItemStack empty = new ItemStack(mug ? LOTRItemMug.getVessel(stack).getEmptyVesselItem() : Items.bowl);
					// attempt to add new stack to inv
					if(!player.inventory.addItemStackToInventory(empty)) {
						// else drop an itemstack
						player.dropPlayerItemWithRandomChoice(empty, false);
					}
					event.result = stack; // reset lotr's change
				}
			}
		}
		
		if(MeTweaks.lotr && MeTweaksConfig.toggleGuardModeHorn && event.item.getItem() == LOTRMod.commandHorn && !event.entityPlayer.worldObj.isRemote) {
			int meta = event.item.getItemDamage();
			if(meta ==TURN_ON_GUARDMODE || meta == TURN_OFF_GUARDMODE) {
				ItemStack item = event.item.copy();
				String state;
				if(meta == TURN_ON_GUARDMODE) {
					item.setItemDamage(TURN_OFF_GUARDMODE);
					state = StatCollector.translateToLocal("options.off");
				}
				else {
					item.setItemDamage(TURN_ON_GUARDMODE);
					state = StatCollector.translateToLocal("options.on");
				}
				
				item.setStackDisplayName("\u00a7r"+StatCollector.translateToLocal("gui.guardmodehorn.display")+state);
				toggleGuardmode(event.entityPlayer, meta == TURN_ON_GUARDMODE, item);
				
				InventoryPlayer inv = event.entityPlayer.inventory;
				
				
				
				if(inv.getStackInSlot(inv.currentItem).getItem() == LOTRMod.commandHorn)
					inv.setInventorySlotContents(inv.currentItem, item);
				else
					System.out.println("no command horn!");
				
			}
			
			// guardmode true / false
			
			
			// mode: unchanged, keep, auto
			// guardrange 1 to 64
			
			// #### if enabled
			// wanderrange 1 to 64
			
			// #### optional
			// aiRange
			// ammoRange
			// checkSight
		}
	}
	
	public static void toggleGuardmode(EntityPlayer player, boolean guardMode, ItemStack item) {

		int guardRange = 0; // unchanged;
		int wanderRange = 0; // unchanged;
		boolean change = false;
		boolean auto = false;
		
		NBTTagCompound nbt = item.getTagCompound();
	    if(nbt != null) {
			guardRange = nbt.getByte("GuardRange");
			wanderRange = MathHelper.clamp_int(nbt.getByte("WanderRange"), LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
			change = nbt.hasKey("Change");
			auto = nbt.hasKey("Auto");
		}
		
		@SuppressWarnings({ "rawtypes" })
		List entities = player.worldObj.loadedEntityList;
		for(Object entity : entities) {
			if(entity instanceof LOTREntityNPC) {
				LOTREntityNPC npc = (LOTREntityNPC) entity;
				if(npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == player) {
					LOTRHiredNPCInfo info = npc.hiredNPCInfo;
					if(info.getTask() == Task.WARRIOR && LOTRSquadrons.areSquadronsCompatible(npc, item)) {
						
						
						if(change) {
							
							
							if(ASMConfig.guardsWanderRange) {
								HiredInfoAccess.setWanderRange(info, wanderRange);
							}
							
							int guardRangeLoc = guardRange;
							
							if(auto) {
								guardRangeLoc = (int) npc.getEntityAttribute(SharedMonsterAttributes.followRange).getBaseValue();
							}
							
							guardRangeLoc = MathHelper.clamp_int(guardRangeLoc, LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
							info.setGuardRange(guardRangeLoc);
						}
						
						info.setGuardMode(guardMode);
					}
				}
			}
		}
	}
}
