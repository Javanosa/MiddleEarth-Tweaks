package metweaks.features;

import java.util.List;

import cpw.mods.fml.common.FMLLog;
import lotr.common.LOTRDimension;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.fac.LOTRFaction;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.map.LOTRConquestGrid;
import lotr.common.world.map.LOTRConquestZone;
import metweaks.MeTweaksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class AIConquest {

	// use entity because player might be too far away
	public static float getConqGainRate(Entity entity) {
	    BiomeGenBase biome = entity.worldObj.getBiomeGenForCoords(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posZ));
	    if(biome instanceof LOTRBiome) {
	      LOTRBiome lotrbiome = (LOTRBiome) biome;
	      return lotrbiome.npcSpawnList.conquestGainRate;
	    } 
	    return 1;
	}

	public static void handleLivingDeath(LivingDeathEvent event) {
			
		Entity killer = event.source.getEntity();
		
		
		if(killer != null && killer instanceof LOTREntityNPC && !((LOTREntityNPC) killer).hiredNPCInfo.isActive) {
			Entity victim = event.entity;
			
			if(victim instanceof LOTREntityNPC && LOTRConquestGrid.conquestEnabled(victim.worldObj) && victim.dimension == LOTRDimension.MIDDLE_EARTH.dimensionID) {
				LOTREntityNPC npcVictim = (LOTREntityNPC) victim;
				LOTRFaction facVictim = ((LOTREntityNPC) victim).getFaction();
				LOTRFaction facKiller = ((LOTREntityNPC) killer).getFaction();
				
				if(facKiller.isPlayableAlignmentFaction()) {
				
					EntityPlayer player = getNearestPlayerPledged(killer);
					
					if(player != null) {
						LOTRPlayerData playerData = LOTRLevelData.getData(player);
						boolean doConquest = MeTweaksConfig.enforceAIConquest || (playerData.getPledgeFaction() != null && playerData.getEnableConquestKills());
						
						
						if(doConquest) {
							
							boolean killerIsEnemy = playerData.getAlignment(facKiller) < 0f;
							if((MeTweaksConfig.aiConqFactorAlly > 0 || killerIsEnemy) && facKiller.isMortalEnemy(facVictim)) {
							
								LOTRConquestZone zone = LOTRConquestGrid.getZoneByEntityCoords(killer);
								float alignBonus = npcVictim.getAlignmentBonus() * (killerIsEnemy ? MeTweaksConfig.aiConqFactorEnemy : MeTweaksConfig.aiConqFactorAlly); 
						        float conqAmount = alignBonus * LOTRLevelData.getConquestRate();
						        float conqGainRate = getConqGainRate(victim);
						        
						        float conqChange = conqAmount * conqGainRate;
						       
						        float conquest = LOTRConquestGrid.doRadialConquest(victim.worldObj, zone, player, facKiller, facVictim, conqChange, conqChange);
						        
						        if(conquest != 0 && MeTweaksConfig.debug >= 1) {
							    	String strConq = (conquest > 0 ? "+" : "") + Math.round(conquest * 100.0F) / 100.0F;
							    	if(strConq.endsWith(".0")) strConq = strConq.substring(0, strConq.length() - 2);
							    		
							    	FMLLog.getLogger().info("AIConquest: " + strConq + " Conquest to " + StatCollector.translateToLocal(facKiller.untranslatedFactionName()));
							    }
						        
							}
						}
					}
				}
			}
			
		}
		
	}
	
	public static EntityPlayer getNearestPlayerPledged(Entity entity) {
		EntityPlayer result = null;
		int lowestdistance = Integer.MAX_VALUE;
		@SuppressWarnings("unchecked")
		List<EntityPlayer> list = entity.worldObj.playerEntities;
		for(EntityPlayer player : list) {
			int distance = (int) player.getDistanceSqToEntity(entity);
			if(distance < lowestdistance) {
				LOTRPlayerData playerData = LOTRLevelData.getData(player);
				if(playerData.getPledgeFaction() != null && playerData.getEnableConquestKills()) {
					lowestdistance = distance;
	    			result = player;
				}
			}
		}
		return result;
		
	}
}
