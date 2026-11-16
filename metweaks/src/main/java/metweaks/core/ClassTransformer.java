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
	public static boolean optifine;
	public static boolean lotrCore;
	public static int kcauldron;
	static final Logger log = FMLLog.getLogger();
	
	static List<String> classes = new ArrayList<>(0); // dummy list to avoid NPE
	
	
	public static void setup() {
		final String[] m = new String[104];
		
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
															m[68] = "team.chisel.ctmlib.CTMRenderer";
		}
		if(syncedconfig) {									
															m[15] = "cpw.mods.fml.common.network.handshake.FMLHandshakeMessage$ModIdData";
															m[16] = "cpw.mods.fml.common.network.handshake.FMLHandshakeClientState$4";
		}

		if(guardsWanderRange || guardsSyncSettings)								 m[17] = "metweaks.guards.HiredInfoAccess";
		if(guardsWanderRange || guardsSyncSettings || fixTeleportToHiringPlayer) m[18] = "lotr.common.entity.npc.LOTRHiredNPCInfo";
		if(guardsWanderRange ||fixInvisibility) {
															m[19] = "net.minecraft.entity.ai.EntityAITarget";
		}
		if(guardsWanderRange) {
															
															m[20] = "lotr.client.gui.LOTRGuiHiredWarrior";
															m[21] = "lotr.common.network.LOTRPacketHiredGui";
															m[22] = "lotr.common.network.LOTRPacketHiredUnitCommand$Handler";

		}
		if(lotrHudPositions) {								
															m[23] = "lotr.client.LOTRTickHandlerClient";
															m[24] = "lotr.client.model.LOTRModelCompass";
															m[25] = "lotr.client.render.entity.LOTRRenderPortal";
		}
		if(fangornTreePenaltyThreshold || hiredsConquerForTheirFaction || revengeCamouflageTargetChance > 0 || farView || bannerProtectionPatches) m[26] = "lotr.common.LOTREventHandler";
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
		
		if(draughtStackEffectsIncrease > 0) 				m[36] = "lotr.common.item.LOTRItemMug";
		if(draughtStackEffectsIncrease > 0 || patchDraughtUse) m[37] = "lotr.common.item.LOTRItemEntDraught";
		if(patchDraughtUse) {
															m[38] = "lotr.common.block.LOTRBlockEntJar";
															m[39] = "lotr.common.block.LOTRBlockMug";
		}
		if(passMissingDiscriminator > 0) 					m[40] = "cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec";
		if(improveBlockBreakSpeeds)							m[42] = "net.minecraft.item.ItemShears";
		
		if((projectilePatches && projectilePatchesReqClient) || entityPosRotationThreshold > 0)	m[43] = "net.minecraft.entity.EntityTrackerEntry";
		if(projectilePatches) {
			
															m[44] = "lotr.common.entity.projectile.LOTREntityProjectileBase";
															m[45] = "net.minecraft.entity.projectile.EntityArrow";
		}
		
		if(hiredsConquerForTheirFaction || moreEvents) {
															m[46] = "lotr.common.LOTRPlayerData";
		}
		
		if(entityTrackerPatches) {
															m[47] = "net.minecraft.entity.player.EntityPlayerMP";
															
		}
		
		if(entityTrackerPatches || farView) {
			m[48] = "net.minecraft.client.multiplayer.WorldClient";
		}
															
		//m[46] = "net.minecraft.client.renderer.RenderGlobal";
		if(fixServerJoinFreeze) {
			m[49] = "lotr.common.world.map.LOTRCustomWaypoint";
		}
		
		if(etFuturumCompatibility) {
			m[50] = "lotr.common.tileentity.LOTRTileEntityAlloyForgeBase";
			m[76] = "lotr.common.block.LOTRBlockChandelier";
		}
		
		if(versionCompat) {
			m[51] = "cpw.mods.fml.common.Loader";
		}
		
		if(renderPerformance) {
			/*
			m[52] = "net.minecraft.world.chunk.storage.ExtendedBlockStorage";
			m[53] = "net.minecraft.world.chunk.Chunk";
			m[54] = "net.minecraft.block.Block";
			m[55] = "net.minecraft.client.renderer.WorldRenderer";
			*/
			
			// patches Optifine updaterenderers return Bugs
			
			if(optifine) {
				m[56] = "net.minecraft.client.renderer.RenderGlobal";
				m[57] = "WrUpdaterSmooth";
			}
		}
		
		if(farView) {
			m[58] = "lotr.common.network.LOTRPacketBiomeVariantsUnwatch$Handler";
			m[59] = "metweaks.farview.FastCraftHelper";
			m[98] = "me.jellysquid.mods.sodium.client.render.chunk.map.ChunkTracker";
			m[101] = "metweaks.farview.CeleritasCalls";
			m[102] = "com.mitchej123.hodgepodge.client.ClientTicker";
		}
		
		if(lotrCore && bannerProtectionPatches) {
			m[60] = "net.minecraft.entity.item.EntityItemFrame";
			m[61] = "net.minecraft.entity.EntityHanging";
			m[62] = "net.minecraft.entity.item.EntityMinecart";
			m[63] = "lotr.common.entity.item.LOTREntityRugBase";
			m[64] = "lotr.common.entity.item.LOTREntityBanner";
			m[65] = "lotr.common.LOTRBannerProtection$FilterForPlayer";
			m[66] = "net.minecraft.block.BlockFarmland";
			m[67] = "net.minecraft.entity.projectile.EntityLargeFireball";
		}
		
		if(lotrHolidays > 0 || fellowshipFFPatches) {
			m[69] = "lotr.common.LOTRMod";
		}
		
		if(explosionPhysics) {
			m[70] = "net.minecraft.world.Explosion";
			m[71] = "net.minecraft.client.network.NetHandlerPlayClient";
			m[72] = "net.minecraft.block.BlockFalling";
			m[73] = "lotr.common.block.LOTRBlockSlabFalling";
			m[75] = "net.minecraft.world.WorldServer";
		}
		
		if(mountedBannerBearers) {
			m[74] = "lotr.common.entity.npc.LOTREntityRohanBannerBearer";
		}
		
		if(moreMountedBannerBearers) {
			m[77] = "lotr.common.entity.npc.LOTREntityRangerNorthBannerBearer";
		}
		
		if(throwableSpearToggle) {
			m[78] = "lotr.common.item.LOTRItemSpear";
			m[79] = "lotr.common.block.LOTRBlockGate";
		}
		
		if(fixFireworkLag) {
			m[80] = "net.minecraft.entity.item.EntityFireworkRocket";
		}
		
		if(hireableEnts) {
			m[81] = "lotr.client.render.entity.LOTRRenderEnt";
			m[82] = "lotr.common.entity.npc.LOTREntityEnt";
		}
		
		if(lotrCore && hearthFireDoesntSpread) {
			m[83] = "net.minecraft.block.BlockFire";
		}
		
		if(interactReachFix) {
			m[84] = "lotr.common.LOTRNetHandlerPlayServer";
		}
		
		if(unlockEnemyWaypoints) {
			m[85] = "lotr.common.world.map.LOTRWaypoint";
		}
		if(armorMaintainProtectionPercent > 0) {
			m[86] = "net.minecraftforge.common.ISpecialArmor$ArmorProperties";
		}
		//m[52] = "net.minecraft.client.multiplayer.ChunkProviderClient";
		
		if(smoothHeadYawMovement || smoothEntityMovement > 0) {
			m[87] = "net.minecraft.entity.EntityLivingBase";
		}
		
		if(smoothHeadYawMovement) {
			m[88] = "net.minecraft.client.entity.EntityOtherPlayerMP";
		}
		
		if(earlyPacketProcessing) {
			m[89] = "net.minecraft.network.NetworkSystem";
			m[90] = "net.minecraft.network.NetworkManager";
			m[91] = "metweaks.features.NetworkCalls";
			m[92] = "net.minecraft.server.MinecraftServer";
			m[93] = "net.minecraft.client.Minecraft";
		}
		
		if(smoothEntityMovement > 0 || experimentalFeatures) {
			m[99] = "metweaks.features.Getters";
		}
		if(experimentalFeatures) {
			m[100] = "net.minecraft.network.NetHandlerPlayServer";
		}
		
		if(noTCPDelay) {
			m[94] = "net.minecraft.client.network.OldServerPinger$2";
			m[95] = "net.minecraft.network.NetworkSystem$1";
			m[96] = "net.minecraft.network.NetworkManager$2";
		}
		
		if(prioritizePlayerTicks) {
			m[97] = "net.minecraft.world.World";
			
		}
		
		if(mutePlayerDetailsApiSpam) {
			m[103] = "lotr.common.playerdetails.LOTRPlayerDetailsCache";
		}

		
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
