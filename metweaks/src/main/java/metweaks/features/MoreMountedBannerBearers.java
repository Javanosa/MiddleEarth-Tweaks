package metweaks.features;

import lotr.common.LOTRMod;
import lotr.common.entity.LOTREntities;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.npc.LOTREntityEasterlingArcher;
import lotr.common.entity.npc.LOTREntityEasterlingBannerBearer;
import lotr.common.entity.npc.LOTREntityEasterlingFireThrower;
import lotr.common.entity.npc.LOTREntityEasterlingGoldWarrior;
import lotr.common.entity.npc.LOTREntityEasterlingLevyman;
import lotr.common.entity.npc.LOTREntityEasterlingWarrior;
import lotr.common.entity.npc.LOTREntityNearHaradBannerBearer;
import lotr.common.entity.npc.LOTREntityNearHaradrimArcher;
import lotr.common.entity.npc.LOTREntityNearHaradrimWarrior;
import lotr.common.entity.npc.LOTREntityRangerNorth;
import lotr.common.entity.npc.LOTREntityRangerNorthBannerBearer;
import lotr.common.entity.npc.LOTREntitySouthronChampion;
import lotr.common.entity.npc.LOTRUnitTradeEntries;
import lotr.common.entity.npc.LOTRUnitTradeEntry;
import lotr.common.world.spawning.LOTRInvasions;
import lotr.common.world.spawning.LOTRInvasions.InvasionSpawnEntry;

public class MoreMountedBannerBearers {
	public static void setup() {
		LOTRUnitTradeEntries.RANGER_NORTH_CAPTAIN = new LOTRUnitTradeEntries(300.0F, new LOTRUnitTradeEntry[] { 
			new LOTRUnitTradeEntry(LOTREntityRangerNorth.class, 50, 0.0F), 
			new LOTRUnitTradeEntry(LOTREntityRangerNorth.class, LOTREntityHorse.class, "RangerNorth_Horse", 70, 100.0F), 
			new LOTRUnitTradeEntry(LOTREntityRangerNorthBannerBearer.class, 70, 150.0F),
			new LOTRUnitTradeEntry(LOTREntityRangerNorthBannerBearer.class, LOTREntityHorse.class, "RangerNorthBanner_Horse", 90, 200.0F).setPledgeExclusive()
		});
		
		
		
		// FMLInitializationEvent
		LOTRInvasions.NEAR_HARAD_COAST.invasionMobs.add(new InvasionSpawnEntry(LOTREntitySouthronChampionBannerBearer.class, 1));
		// FMLPreInitializationEvent
		// get lowest free id for mod compat
		LOTREntities.registerCreature(LOTREntitySouthronChampionBannerBearer.class, "ChampionBannerBearer", 408, 2171169, 11868955);
		// Trader Interaction
		LOTRUnitTradeEntries.NEAR_HARADRIM_WARLORD = new LOTRUnitTradeEntries(150.0F, new LOTRUnitTradeEntry[] { 
			new LOTRUnitTradeEntry(LOTREntityNearHaradrimWarrior.class, 30, 0.0F), 
			new LOTRUnitTradeEntry(LOTREntityNearHaradrimArcher.class, 50, 50.0F), 
			new LOTRUnitTradeEntry(LOTREntitySouthronChampion.class, LOTREntityHorse.class, "SouthronChampion_Horse", 60, 100.0F)
				.setMountArmor(LOTRMod.horseArmorNearHarad).setPledgeExclusive(), 
			new LOTRUnitTradeEntry(LOTREntityNearHaradBannerBearer.class, 50, 150.0F),
			new LOTRUnitTradeEntry(LOTREntitySouthronChampionBannerBearer.class, LOTREntityHorse.class, "SouthronChampionBanner_Horse", 100, 200.0F)
				.setMountArmor(LOTRMod.horseArmorNearHarad).setPledgeExclusive(), 
		});
		
		
		
		// FMLInitializationEvent
		LOTRInvasions.RHUN.invasionMobs.add(new InvasionSpawnEntry(LOTREntityEasterlingGoldWarriorBannerBearer.class, 1));
		// FMLPreInitializationEvent
		// get lowest free id for mod compat
		LOTREntities.registerCreature(LOTREntityEasterlingGoldWarriorBannerBearer.class, "RhunGoldBannerBearer", 409, 14265689, 12004653);
		// Trader Interaction
		LOTRUnitTradeEntries.EASTERLING_WARLORD = new LOTRUnitTradeEntries(150.0F, new LOTRUnitTradeEntry[] {
				new LOTRUnitTradeEntry(LOTREntityEasterlingLevyman.class, 20, 0.0F), 
				new LOTRUnitTradeEntry(LOTREntityEasterlingWarrior.class, 30, 50.0F), 
				new LOTRUnitTradeEntry(LOTREntityEasterlingArcher.class, 50, 100.0F), 
				new LOTRUnitTradeEntry(LOTREntityEasterlingGoldWarrior.class, 50, 200.0F)
		        	.setPledgeExclusive(), 
		        new LOTRUnitTradeEntry(LOTREntityEasterlingWarrior.class, LOTREntityHorse.class, "EasterlingWarrior_Horse", 50, 150.0F), 
		        new LOTRUnitTradeEntry(LOTREntityEasterlingArcher.class, LOTREntityHorse.class, "EasterlingArcher_Horse", 70, 200.0F), 
		        new LOTRUnitTradeEntry(LOTREntityEasterlingGoldWarrior.class, LOTREntityHorse.class, "EasterlingGoldWarrior_Horse", 70, 300.0F)
		        	.setMountArmor(LOTRMod.horseArmorRhunGold).setPledgeExclusive(), 
		        new LOTRUnitTradeEntry(LOTREntityEasterlingFireThrower.class, 60, 150.0F), 
		        new LOTRUnitTradeEntry(LOTREntityEasterlingBannerBearer.class, 50, 200.0F),
		        new LOTRUnitTradeEntry(LOTREntityEasterlingGoldWarriorBannerBearer.class, LOTREntityHorse.class, "EasterlingGoldWarriorBanner_Horse", 110, 400.0F)
	        		.setMountArmor(LOTRMod.horseArmorRhunGold).setPledgeExclusive(), 
		 });
		  
		  
	}
}
