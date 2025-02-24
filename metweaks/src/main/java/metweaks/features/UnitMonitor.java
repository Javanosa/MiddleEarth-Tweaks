package metweaks.features;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaksConfig;
import metweaks.core.HooksLOTR;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class UnitMonitor {
	
	
	
	
	static int skipTick;
	static final int DELAY_TICKS = 1;
	
	static Field field_chunksToUnload = ReflectionHelper.findField(ChunkProviderServer.class, "chunksToUnload", "field_73248_b", "c");
	
	
	public static int trackingRadius;
	
	
	public static int clientUnitTracking;
	public static TObjectIntMap<UUID> modesUnitTracking = new TObjectIntHashMap<>();
	
	public static TIntIntMap failedTeleports = new TIntIntHashMap();
	
	static long nextClearCache;
	
	public static void setUnitTrackingMode(EntityPlayer player, int state) {
		
		UUID id = player.getUniqueID();
		if(state == 0) {
			modesUnitTracking.remove(id);
		}
		else {
			modesUnitTracking.put(id, state);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isAtEdgeSimple(LOTREntityNPC npc, Set chunksToUnload, EntityPlayer player, boolean near) {
        if(near) {
	       
	       
			if(Math.abs(player.posX - npc.posX) > trackingRadius
			|| Math.abs(player.posZ - npc.posZ) > trackingRadius) {
				return true;
		    }
        }
		
		if(chunksToUnload.contains(ChunkCoordIntPair.chunkXZ2Int(npc.chunkCoordX, npc.chunkCoordZ))) {
        	
			return true;
		}
		
        
		return false;
	}
	
	
	
	
	
	@SuppressWarnings("rawtypes")
	public static void tick(World world) {
		if(modesUnitTracking.isEmpty()) return;

		long current = System.currentTimeMillis();
		if(nextClearCache < current) {
			if(!failedTeleports.isEmpty())
				failedTeleports.clear();
			nextClearCache = current + 3000;
		}
		
			Set chunksToUnload = null;
			List entities = world.loadedEntityList;
			
			for(Object entity : entities) {
			    if(entity instanceof LOTREntityNPC) {
			        LOTREntityNPC npc = (LOTREntityNPC) entity;
			        if(npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.shouldFollowPlayer() // && npc.ticksExisted > 40
			        		) {
			        	
			        	UUID uuid = npc.hiredNPCInfo.getHiringPlayerUUID(); //player.getUniqueID();
		        		if(!modesUnitTracking.containsKey(uuid)) continue;
		        		
		        		
			        	
		        		EntityPlayer player = npc.hiredNPCInfo.getHiringPlayer();
			        	if(player == null || !npc.hiredNPCInfo.teleportAutomatically
			        				) continue;
			        		
			        		
			        		
		        		
		        	
			        	if(/*usePendingUnload && */chunksToUnload == null) {
			        		IChunkProvider provider = world.getChunkProvider();
			        		if(provider.getClass() == ChunkProviderServer.class) {
			        			try {
			    					chunksToUnload = (Set) field_chunksToUnload.get(provider);
			    					
			    					
			    				} catch (Exception e) {
			    					e.printStackTrace();
			    					return;
			    					
			    				}
			        			
			        			
			        		}
			        		
			        		// Blocks 80
			        		//
			        		// V 	US	Track	Blocks	
			        		// 2	44	3		48
			        		// 3	44	3		48
			        		// 4	60	4		64
			        		// 5	72	5		80
			        		// 6	64  5		80
			        		
			        		trackingRadius = (MathHelper.clamp_int(MinecraftServer.getServer().getConfigurationManager().getViewDistance(), 3, 4) << 4) - 4;
			        	}
			        	
			            // 1. own chunk
			            // 2. radial chunks
			            // 3. corner chunns
			            // 4. negated entity tick chunk exists requirements
			        	
			        	
			        	
			            if(isAtEdgeSimple(npc, chunksToUnload, player, modesUnitTracking.get(uuid) == 2) && (!failedTeleports.containsKey(npc.getEntityId()) || failedTeleports.get(npc.getEntityId()) <= 2)) {
			            	LOTRPlayerData data = LOTRLevelData.getData(npc.hiredNPCInfo.getHiringPlayerUUID());
			            	if(data.getTargetFTWaypoint() != null || data.getTimeSinceFT() < 40) {
			            		if(MeTweaksConfig.debug >= 2)
			            			System.out.println("Teleport aborted (ft)");
			            		continue;
			            	}
			            	
			            	
			            	
			            	
			            	// What happens in updateEntityWithOptionalForce
			            	// unloadedEntityList handling
			            	// time of action (event time)
			            	
			            	boolean success = npc.hiredNPCInfo.tryTeleportToHiringPlayer(true);
			                if(success && !ASMConfig.fixTeleportToHiringPlayer) {
			                	HooksLOTR.postTeleportToHiringPlayer(npc);
			                }
			                
			                if(!success) {
			                	failedTeleports.adjustOrPutValue(npc.getEntityId(), 1, 1);
			                	if(MeTweaksConfig.debug >= 2)
			                		System.out.println("Failed to teleport");
			                }
			               
			                // optional:
			                // check if entity added to chunk
			                // updatesentitxwithAdditionalForce(true)

			                // something to avoid spam of failed teleportation
			                // debug hired npc creation on client
			            }
			        	
			        }
			    }
			}
		
	}
}
