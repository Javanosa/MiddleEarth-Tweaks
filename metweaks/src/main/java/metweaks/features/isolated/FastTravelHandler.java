package metweaks.features.isolated;

import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import metweaks.MeTweaks;
import metweaks.events.LOTRFastTravelEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.entity.EntityNPCInterface;

public class FastTravelHandler {

	public FastTravelHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Entity> T fastTravelEntity(World world, T entity, int x, int y, int z) {
		String entityID = EntityList.getEntityString(entity);
		NBTTagCompound nbt = new NBTTagCompound();
		entity.writeToNBT(nbt);
		entity.setDead();
		Entity created = EntityList.createEntityByName(entityID, world);
		created.readFromNBT(nbt);
		created.setLocationAndAngles(x + 0.5D, y, z + 0.5D, created.rotationYaw, created.rotationPitch);
		created.fallDistance = 0.0F;
		world.spawnEntityInWorld(created);
		world.updateEntityWithOptionalForce(created, false);
		return (T) created;
	}
	
	@SuppressWarnings({ "rawtypes" })
	@SubscribeEvent
	public void onFastTravelTransport(LOTRFastTravelEvent.Transport event) {
		if(!MeTweaks.astikoor && !MeTweaks.customNpcs) return;
		
		EntityPlayer player = event.player;
		World world = player.worldObj;
		List entities;
		if(MeTweaks.astikoor) {
			// we need a new list with all types included
			entities = world.getEntitiesWithinAABB(Entity.class, player.boundingBox.expand(256D, 256D, 256D));
		} else {
			entities = event.nearbyEntities;
		}
		Set<EntityLiving> toTransport = event.toTransport;
		int x = event.waypoint.getXCoord();
		int z = event.waypoint.getZCoord();
		int y = event.waypoint.getYCoord(world, x, z);
	    Entity mount = event.player.ridingEntity;
		for(Object entity : entities) {
	    	if(MeTweaks.astikoor) {
	    		FastTravelHandlerAstikoor.handleAstikoor(entity, mount, x, y, z, toTransport);
	    	}
	    	
	    	if(MeTweaks.customNpcs && entity instanceof EntityNPCInterface) {
	    		EntityNPCInterface customNpc = (EntityNPCInterface) entity;
	    		if(customNpc.isFollower() && customNpc.getOwner() == player) {
	    			toTransport.add(customNpc);
	    			//System.out.println("Transport CNpc: "+customNpc);
	    		}
	    	}
	    }
	}
}
