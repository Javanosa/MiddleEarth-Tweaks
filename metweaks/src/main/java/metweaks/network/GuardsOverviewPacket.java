package metweaks.network;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.netty.buffer.ByteBuf;
import lotr.common.entity.npc.*;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import lotr.common.inventory.LOTRInventoryNPC;
import metweaks.ASMConfig;
import metweaks.client.gui.unitoverview.GuiUnitOverview;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class GuardsOverviewPacket implements IMessage {
	// #1: ask for packet (no data)
	// #2: (server) send inventory items of all loaded hired entities
	// numEntities, {id, numItems, item array}, {id, numItems, item array}
	// #3: ask for action (entityID, optional action)
	
	public static final int SIZE_ITEMS = 4;
	
	// just the entity id from client indicates that we just wanna open their inventory
	
	public final TIntObjectMap<ItemStack[]> inventories = new TIntObjectHashMap<>();
	
	public GuardsOverviewPacket() {}
	
	public static Set<Class<? extends Entity>> rangedEntitiesOnly = new HashSet<>();
	
	public static Set<Class<? extends Entity>> rangedEntities;
	
	static {
		
		Set<Class<? extends Entity>> rangedOnly = rangedEntitiesOnly;
		rangedOnly.add(LOTREntityRohirrimArcher.class);
		rangedOnly.add(LOTREntityAngmarHillmanAxeThrower.class);
		rangedOnly.add(LOTREntityAngmarOrcArcher.class);
		rangedOnly.add(LOTREntityBlackUrukArcher.class);
		rangedOnly.add(LOTREntityBlackrootArcher.class);
		rangedOnly.add(LOTREntityBlueDwarfAxeThrower.class);
		rangedOnly.add(LOTREntityDaleArcher.class);
		rangedOnly.add(LOTREntityDolAmrothArcher.class);
		rangedOnly.add(LOTREntityDolGuldurOrcArcher.class);
		rangedOnly.add(LOTREntityDorwinionCrossbower.class);
		rangedOnly.add(LOTREntityDorwinionElfArcher.class);
		rangedOnly.add(LOTREntityDunlendingArcher.class);
		rangedOnly.add(LOTREntityDunlendingAxeThrower.class);
		rangedOnly.add(LOTREntityDwarfAxeThrower.class);
		rangedOnly.add(LOTREntityEasterlingArcher.class);
		rangedOnly.add(LOTREntityGondorArcher.class);
		rangedOnly.add(LOTREntityGulfHaradArcher.class);
		rangedOnly.add(LOTREntityGundabadOrcArcher.class);
		rangedOnly.add(LOTREntityGundabadUrukArcher.class);
		rangedOnly.add(LOTREntityHarnedorArcher.class);
		rangedOnly.add(LOTREntityIsengardSnagaArcher.class);
		rangedOnly.add(LOTREntityLamedonArcher.class);
		rangedOnly.add(LOTREntityMordorOrcArcher.class);
		rangedOnly.add(LOTREntityTauredainBlowgunner.class);
		rangedOnly.add(LOTREntityUrukHaiCrossbower.class);
		rangedOnly.add(LOTREntityUmbarArcher.class);
		rangedOnly.add(LOTREntityNomadArcher.class);
		rangedOnly.add(LOTREntityNearHaradrimArcher.class);
		rangedOnly.add(LOTREntityWoodElfScout.class);
		
		
		
		Set<Class<? extends Entity>> ranged = rangedEntities = new HashSet<>(rangedOnly);
		ranged.add(LOTREntityCorsair.class);
		ranged.add(LOTREntityHobbitBounder.class);
		ranged.add(LOTREntityLossarnachAxeman.class);
		ranged.add(LOTREntityLossarnachBannerBearer.class);
		ranged.add(LOTREntitySnowTroll.class);
		ranged.add(LOTREntityRangerIthilien.class);
		ranged.add(LOTREntityRangerIthilienBannerBearer.class);
		ranged.add(LOTREntityRangerNorth.class);
		ranged.add(LOTREntityRangerNorthBannerBearer.class);
		ranged.add(LOTREntityRivendellWarrior.class);
		ranged.add(LOTREntityRivendellBannerBearer.class);
		ranged.add(LOTREntityWoodElfWarrior.class);
		ranged.add(LOTREntityWoodElfBannerBearer.class);
		ranged.add(LOTREntityWoodElf.class);
		ranged.add(LOTREntityGaladhrimWarrior.class);
		ranged.add(LOTREntityGaladhrimBannerBearer.class);
		ranged.add(LOTREntityHighElfWarrior.class);
		ranged.add(LOTREntityHighElfBannerBearer.class);
		ranged.add(LOTREntityMountainTroll.class);
		ranged.add(LOTREntityGaladhrimWarden.class);
		ranged.add(LOTREntityHighElf.class);
		ranged.add(LOTREntityRivendellElf.class);
		ranged.add(LOTREntityGaladhrimElf.class);
		
		
		/*
		// shouldnt count as
		LOTREntityEasterlingFireThrower
		
		// traders only
		LOTREntityCorsairCaptain
		LOTREntityCorsairSlaver
		LOTREntityHobbitShirriff
		LOTREntityLossarnachCaptain
		LOTREntityRangerIthilienCaptain
		LOTREntityRangerNorthCaptain
		LOTREntityRivendellLord
		LOTREntityWoodElfCaptain
		LOTREntityWoodElfSmith
		LOTREntityGaladhrimLord
		LOTREntityHighElfLord
		
		// unavaible
		LOTREntityMallornEnt
		LOTREntityMarshWraith
		LOTREntityMountainTrollChieftain
		LOTREntityTormentedElf
		LOTREntityUtumnoOrcArcher
		*/
	}
	
	public GuardsOverviewPacket(EntityPlayer player, int entityID) {
		@SuppressWarnings("unchecked")
		List<Entity> entities = entityID == -1 ? player.worldObj.loadedEntityList : Arrays.asList(player.worldObj.getEntityByID(entityID));
		UUID uuid = player.getUniqueID();
		for(Entity entity : entities) {
			if(entity instanceof LOTREntityNPC) {
				LOTREntityNPC npc = (LOTREntityNPC) entity;
				if(npc.hiredNPCInfo.isActive && uuid.equals(npc.hiredNPCInfo.getHiringPlayerUUID())) {
					ItemStack[] items = new ItemStack[SIZE_ITEMS];
					if(npc.hiredNPCInfo.getTask() == Task.WARRIOR) {
						int rangedindex = 1;
						if(ASMConfig.unitOverviewHideMeleeForRangedOnly && rangedEntitiesOnly.contains(npc.getClass())) {
							rangedindex = 0;
						}
						else {
							ItemStack melee = null;
							if(npc.ridingEntity != null) {
								melee = npc.npcItemsInv.getMeleeWeaponMounted();
							}
							
							items[0] = melee != null ? melee : npc.npcItemsInv.getMeleeWeapon();
						}
						items[rangedindex] = npc.npcItemsInv.getRangedWeapon();
					}
					else {
						LOTRInventoryNPC inv = npc.hiredNPCInfo.getHiredInventory();
						for(int i = 0; i < 4; i++) {
							items[i] = inv.getStackInSlot(i);
						}
						
					}
					inventories.put(npc.getEntityId(), items);
				}
			}
		}
	}
	
	

	@Override
	public void fromBytes(ByteBuf buf) {
		
		
		int numNPCs = buf.readInt();
		PacketBuffer pb = new PacketBuffer(buf);
		try {
			for(int i = 0; i < numNPCs; i++) {
				int entityID = buf.readInt();
				ItemStack[] items = new ItemStack[SIZE_ITEMS];
				for(int s = 0; s < SIZE_ITEMS; s++) {
					items[s] = pb.readItemStackFromBuffer();
				}
				inventories.put(entityID, items);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		
		buf.writeInt(inventories.size());
		PacketBuffer pb = new PacketBuffer(buf);
		try {
			for(int key : inventories.keys()) {
				buf.writeInt(key);
				ItemStack[] items = inventories.get(key);
				for(int i = 0; i < SIZE_ITEMS; i++) {
					pb.writeItemStackToBuffer(items[i]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static class Handler implements IMessageHandler<GuardsOverviewPacket, IMessage> {
	    public IMessage onMessage(GuardsOverviewPacket packet, MessageContext context) {
	    	TIntObjectMap<ItemStack[]> map = packet.inventories;
	    	if(map.size() == 1 && GuiUnitOverview.inventories != null) {
	    		int entityID = map.keys()[0];
	    		GuiUnitOverview.inventories.put(entityID, map.get(entityID));
	    	}
	    	else {
	    		GuiUnitOverview.inventories = map;
	    	}
			return null;
	    }
	}

}
