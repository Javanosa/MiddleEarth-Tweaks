package metweaks.features;

import java.util.List;
import java.util.UUID;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTRNPCMount;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class PlayerRidingTracker {
	
	// UUID playerUuid, int mountID
	public static TObjectIntMap<UUID> map = new TObjectIntHashMap<UUID>();
	static long nextRun;
	
	
	public static void processEvent(boolean server) {
		long current = System.currentTimeMillis();
		// server or notintregratedserver 
		
		if(nextRun < current && (server || (MinecraftServer.getServer() == null || !MinecraftServer.getServer().isServerRunning()))) {
			nextRun = current + 500;
			tick(server);
		}
		
	}
	
	public static void handlePlayer(EntityPlayer player, boolean server) {
		
		Entity mount = player.ridingEntity;
		UUID pid = player.getUniqueID();
		if(mount != null) {
			if(mount instanceof EntityCreature
			 && mount instanceof LOTRNPCMount
			 && (!map.containsKey(pid) || map.get(pid) != mount.getEntityId())) {
				map.put(pid, mount.getEntityId());
				if(server)
					((EntityCreature) mount).detachHome();
				
			}
		}
		else if(map.containsKey(pid)) {
			int id = map.remove(pid);
			mount = player.worldObj.getEntityByID(id);
			
			if(mount != null && mount instanceof LOTRNPCMount && mount instanceof EntityCreature) {
				EntityCreature creature = (EntityCreature) mount;
				if(server) {
					creature.getNavigator().clearPathEntity();
					creature.setHomeArea((int) creature.posX, (int) creature.posY, (int) creature.posZ, 8);
				}
				// client or intregratedserver
				boolean singlePlayer = MinecraftServer.getServer() != null && MinecraftServer.getServer().isSinglePlayer();
				if(!server || singlePlayer) {
					if(singlePlayer) {
						mount = LOTRMod.proxy.getClientWorld().getEntityByID(id);
						if(mount != null && mount instanceof LOTRNPCMount && mount instanceof EntityCreature) {
							creature = (EntityCreature) mount;
							
						}
					}
					// avoid keep moving on client
					creature.moveForward = 0F;
					creature.moveStrafing = 0f;
					creature.setJumping(false);
				}
				
				
			}
		}
	}
	
	public static void tick(boolean server) {
		if(!server) {
			EntityPlayer player = LOTRMod.proxy.getClientPlayer();
			if(player != null)
				handlePlayer(player, false);
			
			return;
		}
		
		@SuppressWarnings("unchecked")
		List<EntityPlayer> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		
		for(EntityPlayer player : players) {
			handlePlayer(player, true);
		}
	}
	
	public static void leave(EntityPlayer player) {
		map.remove(player.getUniqueID());
	}
}
