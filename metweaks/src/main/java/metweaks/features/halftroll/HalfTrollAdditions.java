package metweaks.features.halftroll;

import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRMod;
import lotr.common.entity.LOTREntities;
import lotr.common.entity.animal.LOTREntityRhino;
import lotr.common.entity.npc.LOTREntityHalfTroll;
import lotr.common.entity.npc.LOTREntityHalfTrollBannerBearer;
import lotr.common.entity.npc.LOTREntityHalfTrollScavenger;
import lotr.common.entity.npc.LOTREntityHalfTrollWarrior;
import lotr.common.entity.npc.LOTRUnitTradeEntries;
import lotr.common.entity.npc.LOTRUnitTradeEntry;
import lotr.common.fac.LOTRControlZone;
import lotr.common.fac.LOTRFaction;
import lotr.common.world.spawning.LOTRInvasions;
import lotr.common.world.spawning.LOTRInvasions.InvasionSpawnEntry;
import lotr.common.world.spawning.LOTRSpawnEntry;
import lotr.common.world.spawning.LOTRSpawnList;

public class HalfTrollAdditions {
	
	public static void setup() {
		
		
		// conditions: ASMConfig.halfTrollAdditions && MeTweaks.lotr
		
		// FMLPreInitializationEvent
		ReflectionHelper.setPrivateValue(LOTRSpawnList.class, LOTRSpawnList.HALF_TROLLS, Arrays.asList(new LOTRSpawnEntry[] {	
				new LOTRSpawnEntry(LOTREntityHalfTroll.class, 60, 2, 4), 
				new LOTRSpawnEntry(LOTREntityHalfTrollWarrior.class, 35, 2, 4), 
				new LOTRSpawnEntry(LOTREntityHalfTrollThrower.class, 30, 2, 4),
				new LOTRSpawnEntry(LOTREntityHalfTrollScavenger.class, 5, 1, 1)
			}), "spawnList");
		
		// alignment area
		// FMLInitializationEvent
		List<LOTRControlZone> list = LOTRFaction.HALF_TROLL.getControlZones();
		list.add(new LOTRControlZone(1403, 2763, 160));
		
		// FMLInitializationEvent
		LOTRInvasions.HALF_TROLL.invasionMobs.add(new InvasionSpawnEntry(LOTREntityHalfTrollThrower.class, 8));
		
		
		// FMLPreInitializationEvent
		// get lowest free id for mod compat
		LOTREntities.registerCreature(LOTREntityHalfTrollThrower.class, "HalfTrollStoneThrower", 407, 9403002, 6244662);
		
		// Trader Interaction
		LOTRUnitTradeEntries.HALF_TROLL_WARLORD = new LOTRUnitTradeEntries(200.0F, new LOTRUnitTradeEntry[] { 
				new LOTRUnitTradeEntry(LOTREntityHalfTroll.class, 30, 0.0F), 
				new LOTRUnitTradeEntry(LOTREntityHalfTrollWarrior.class, 50, 100.0F),
				new LOTRUnitTradeEntry(LOTREntityHalfTrollThrower.class, 60, 150.0F).setPledgeType(LOTRUnitTradeEntry.PledgeType.FACTION),
				new LOTRUnitTradeEntry(LOTREntityHalfTrollWarrior.class, LOTREntityRhino.class, "HalfTrollWarrior_Rhino", 70, 200.0F)
					.setMountArmor(LOTRMod.rhinoArmorHalfTroll, 0.5F), 
				new LOTRUnitTradeEntry(LOTREntityHalfTrollThrower.class, LOTREntityRhino.class, "HalfTrollStoneThrower_Rhino", 80, 250.0F)
					.setMountArmor(LOTRMod.rhinoArmorHalfTroll, 0.5F).setPledgeType(LOTRUnitTradeEntry.PledgeType.FACTION), 
				new LOTRUnitTradeEntry(LOTREntityHalfTrollBannerBearer.class, 70, 150.0F) 
		 });
		  
		
		
	}
}
