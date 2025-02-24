package metweaks.features;

import java.util.List;

import lotr.common.LOTRDimension;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRMod;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public class SleepHandler {
	public static void handlePlayerSleep(PlayerSleepInBedEvent event) {
		EntityPlayer player = event.entityPlayer;
		
		if(!player.worldObj.isRemote) {
			if(MeTweaksConfig.alwaysSetBedSpawn) {
				player.setSpawnChunk(new ChunkCoordinates(event.x, event.y, event.z), false);
				
				ChatComponentText component = new ChatComponentText(StatCollector.translateToLocal("tile.bed.respawnSet")); // serverside lang control
				component.getChatStyle().setItalic(true);
				player.addChatComponentMessage(component);
			}
				
			
			if(MeTweaks.lotr && MeTweaksConfig.checkBedUnsafe && player.worldObj.provider.dimensionId == LOTRDimension.MIDDLE_EARTH.dimensionID) {
				int width = 8;
	            int height = 5;
	            LOTRPlayerData data = LOTRLevelData.getData(player);
	            @SuppressWarnings("rawtypes")
				List entities = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(
        				event.x - width, event.y - height, event.z - width, 
        				event.x + width, event.y + height, event.z + width), 
	            new IEntitySelector() {

					@Override
					public boolean isEntityApplicable(Entity entity) {
						if(entity instanceof LOTREntityNPC) {
							LOTREntityNPC npc = (LOTREntityNPC) entity;
							if(npc.getAttackTarget() == player) {
								return true;
							}
							float alignment = data.getAlignment(LOTRMod.getNPCFaction(npc));
						    return (alignment < 0F);
						}
						return false;
					}
	            	
	            });
	            

	            if(!entities.isEmpty()) {
	                event.result = EntityPlayer.EnumStatus.NOT_SAFE;
	            }
			}
		}
	}
}
