package metweaks.core;

import static metweaks.ASMConfig.*;
import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.FMLLog;

public class ClassTransformer implements IClassTransformer {
	static final String coreprefix = "MT-Core";
	static boolean obf = true;
	static final Logger log = FMLLog.getLogger();
	
	static List<String> classes = new ArrayList<>(0); // dummy list to avoid NPE
	
	public static void setup() {
		final String[] m = new String[43];
		
		if(horseinv) 										m[0	] = "lotr.common.LOTRReflection";	
		if(allyConquestSpawns) 								m[1	] = "lotr.common.world.spawning.LOTRBiomeSpawnList$FactionContainer";
		if(allyConquestSpawns || allyKillReduceConquest) 	m[2	] = "lotr.common.world.map.LOTRConquestGrid";
		if(conquestDecay) { 								
															m[3	] = "lotr.common.world.map.LOTRConquestZone";
															m[4	] = "lotr.common.LOTRLevelData";
		}
		if(fencegatePlacement)								m[5	] = "net.minecraft.block.BlockFenceGate";
		if(barkblocks) {									
															m[6	] = "net.minecraft.block.BlockNewLog";
															m[7	] = "net.minecraft.block.BlockOldLog";
															m[8	] = "lotr.common.block.LOTRBlockWoodBase";
															m[9	] = "lotr.common.block.LOTRBlockWoodBeam";
															m[10] = "net.minecraft.block.BlockRotatedPillar";
															m[41] = "eoa.lotrfa.common.block.multi.LOTRFABlockMultiWoodBeam";
		}
		if(trapdoorPlacement)								m[11] = "net.minecraft.block.BlockTrapDoor";
		if(verticalSlabPlacement) {							
															m[12] = "net.minecraft.block.BlockSlab";
															m[13] = "net.minecraft.item.ItemSlab";
															m[14] = "team.chisel.item.ItemCarvableSlab";
		}
		if(syncedconfig) {									
															m[15] = "cpw.mods.fml.common.network.handshake.FMLHandshakeMessage$ModIdData";
															m[16] = "cpw.mods.fml.common.network.handshake.FMLHandshakeClientState$4";
		}

		if(guardsWanderRange || guardsSyncSettings)								 m[17] = "metweaks.guards.HiredInfoAccess";
		if(guardsWanderRange || guardsSyncSettings || fixTeleportToHiringPlayer) m[18] = "lotr.common.entity.npc.LOTRHiredNPCInfo";
		if(guardsWanderRange) {
															m[19] = "net.minecraft.entity.ai.EntityAITarget";
															m[20] = "lotr.client.gui.LOTRGuiHiredWarrior";
															m[21] = "lotr.common.network.LOTRPacketHiredGui";
															m[22] = "lotr.common.network.LOTRPacketHiredUnitCommand$Handler";

		}
		if(lotrHudPositions) {								
															m[23] = "lotr.client.LOTRTickHandlerClient";
															m[24] = "lotr.client.model.LOTRModelCompass";
															m[25] = "lotr.client.render.entity.LOTRRenderPortal";
		}
		if(fangornTreePenaltyThreshold)						m[26] = "lotr.common.LOTREventHandler";
		if(fixSluggishHaradAttack)							m[27] = "lotr.common.entity.ai.LOTREntityAIAttackOnCollide";
		if(foodConsumeDurations)							m[28] = "net.minecraft.item.ItemFood";
		if(tauredainPoisonDartChance > 0 && !aiRangedImprovements)	m[29] = "lotr.common.entity.npc.LOTREntityTauredainBlowgunner";
		// disable patch if we use the new ranged bridge
		
		if(improveBlockBreakSpeeds)							m[30] = "net.minecraft.item.ItemHoe";
		if(adventureModePatches)							m[31] = "net.minecraft.entity.player.EntityPlayer";
		if(guardsEquipRanged || aiRangedImprovements)		m[32] = "lotr.common.entity.ai.LOTREntityAIRangedAttack";
		if(guardsEquipRanged) {	
															m[33] = "lotr.common.inventory.LOTRContainerHiredWarriorInventory";
															m[34] = "lotr.client.gui.LOTRGuiHiredWarriorInventory";
															m[35] = "lotr.common.entity.npc.LOTRInventoryHiredReplacedItems";
		}
		
		if(draughtStackEffectsIncrease > 0) 					 m[36] = "lotr.common.item.LOTRItemMug";
		if(draughtStackEffectsIncrease > 0 || patchDraughtUse) m[37] = "lotr.common.item.LOTRItemEntDraught";
		if(patchDraughtUse) {
															m[38] = "lotr.common.block.LOTRBlockEntJar";
															m[39] = "lotr.common.block.LOTRBlockMug";
		}
		if(passMissingDiscriminator > 0) 						m[40] = "cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec";
		if(improveBlockBreakSpeeds)							m[42] = "net.minecraft.item.ItemShears";
		
		
		classes = Arrays.asList(m);
		
		System.out.println(classes);
	}
	
	//static long prevTime;
	
	public byte[] transform(final String name, final String transformedName, final byte[] classBytes) {
		//long time = System.currentTimeMillis();
		//log.info((time-prevTime)+" "+transformedName);
		//prevTime = time;
		
		if(transformedName == null) System.err.println("transformedName is null");
		int i = classes.indexOf(transformedName);
    	return i == -1 ? classBytes : ClassHandler.transform(classBytes, i);
    }
}
