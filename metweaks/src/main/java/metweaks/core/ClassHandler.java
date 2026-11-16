package metweaks.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

import static metweaks.core.TCallsRanged.*;

import static metweaks.core.TCallsConquest.*;
import static metweaks.core.TCallsBlocks.*;
import static metweaks.core.TCallsGuards.*;
import static metweaks.core.TCallsMisc.*;
import static metweaks.core.TCallsProjectile.*;
import static metweaks.core.TCallsRenderPerformance.*;
import static metweaks.core.TCallsPhysics.*;
import static metweaks.core.TCallsNetwork.*;

public class ClassHandler {
	
	public static final void callByIndex(ClassNode c, int i) {
		switch(i) {
			case 0: // lotr.common.LOTRReflection
	    		transformLOTRReflection(c);									break;
	    	case 1: // lotr.common.world.spawning.LOTRBiomeSpawnList$FactionContainer
	    		transformLOTRBiomeSpawnList$FactionContainer(c);			break;
	    	case 2: // lotr.common.world.map.LOTRConquestGrid
	    		transformLOTRConquestGrid(c);								break;
	    	case 3: // lotr.common.world.map.LOTRConquestZone
	    		transformLOTRConquestZone(c);								break;
	    	case 4: // lotr.common.LOTRLevelData
	    		transformLOTRLevelData(c);									break;
	    	case 5: // net.minecraft.block.BlockFenceGate
	    		transformBlockFenceGate(c);									break;
	    	case 6: // net.minecraft.block.BlockNewLog
	    	case 7: // net.minecraft.block.BlockOldLog
	    		transformSubBlocks(c, true);								break;
	    	case 8: // lotr.common.block.LOTRBlockWoodBase
	    	case 9: // lotr.common.block.LOTRBlockWoodBeam
	    		transformSubBlocks(c, false);								break;
	    	case 10: // net.minecraft.block.BlockRotatedPillar
	    		transformBlockRotatedPillar(c);								break;
	    	case 11: // net.minecraft.block.BlockTrapDoor
	    		transformBlockTrapdoor(c);									break;
	    	case 12: // net.minecraft.block.BlockSlab
	    		transformBlockSlab(c);										break;
	    	case 13: // net.minecraft.item.ItemSlab
	    		transformItemSlab(c);										break;
			case 14: // team.chisel.item.ItemCarvableSlab
	    		transformItemCarvableSlab(c);								break;
	    	case 15: // cpw.mods.fml.common.network.handshake.FMLHandshakeMessage$ModIdData
	    		transformFMLHandshakeMessage$ModIdData(c);					break;
	    	case 16: // cpw.mods.fml.common.network.handshake.FMLHandshakeClientState$4
	    		transformFMLHandshakeClientState$WAITINGSERVERCOMPLETE(c);	break;
	    	case 17: // metweaks.guards.HiredInfoAccess
	    		transformHiredInfoAccess(c);								break;
	    	case 18: // lotr.common.entity.npc.LOTRHiredNPCInfo
	    		transformLOTRHiredNPCInfo(c);								break;
	    	case 19: // net.minecraft.entity.ai.EntityAITarget
	    		transformEntityAITarget(c);									break;
	    	case 20: // lotr.client.gui.LOTRGuiHiredWarrior
	    		transformLOTRGuiHiredWarrior(c);							break;
	    	case 21: // lotr.common.network.LOTRPacketHiredGui
	    		transformLOTRPacketHiredGui(c);								break;
	    	case 22: // lotr.common.network.LOTRPacketHiredUnitCommand$Handler
	    		transformLOTRPacketHiredUnitCommand$Handler(c);				break;
	    	case 23: // lotr.client.LOTRTickHandlerClient
	    		transformLOTRTickHandlerClient(c);							break;
	    	case 24: // lotr.client.model.LOTRModelCompass
	    	case 25: // lotr.client.render.entity.LOTRRenderPortal
	    		transformRingWriting(c, i == 25);							break;
	    	case 26: // lotr.common.LOTREventHandler
	    		transformLOTREventHandler(c);								break;
	    	case 27: // lotr.common.entity.ai.LOTREntityAIAttackOnCollide
	    		transformLOTREntityAIAttackOnCollide(c);					break;
	    	case 28: // net.minecraft.item.ItemFood
	    		transformItemFood(c);										break;
	    	case 29: // lotr.common.entity.npc.LOTREntityTauredainBlowgunner
	    		transformLOTREntityTauredainBlowgunner(c);					break;
	    	case 30: // net.minecraft.item.ItemHoe
	    		transformItemHoe(c);										break;
	    	case 31: // net.minecraft.entity.player.EntityPlayer
	    		transformEntityPlayer(c);									break;
	    	case 32: // lotr.common.entity.ai.LOTREntityAIRangedAttack
	    		transformLOTREntityAIRangedAttack(c);						break;
	    	case 33: // lotr.common.inventory.LOTRContainerHiredWarriorInventory
	    		transformLOTRContainerHiredWarriorInventory(c);				break;
	    	case 34: // lotr.client.gui.LOTRGuiHiredWarriorInventory
	    		transformLOTRGuiHiredWarriorInventory(c);					break;
	    	case 35: // lotr.common.entity.npc.LOTRInventoryHiredReplacedItems
	    		transformLOTRInventoryHiredReplacedItems(c);				break;
	    	case 36: // lotr.common.item.LOTRItemMug
	    		 transformLOTRItemMug(c);									break;
	    	case 37: // lotr.common.item.LOTRItemEntDraught
	    		 transformLOTRItemEntDraught(c);							break;
	    	case 38: // lotr.common.block.LOTRBlockEntJar				
	    		 modifyDraughBlock(c, false);								break;
	    	case 39: // lotr.common.block.LOTRBlockMug				
	    		 modifyDraughBlock(c, true);								break;
	    	case 40: // cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec
	    		 transformFMLIndexedMessageToMessageCodec(c);				break;
	    	case 41: // eoa.lotrfa.common.block.multi.LOTRFABlockMultiWoodBeam
	    		transformLOTRFABlockMultiWoodBeam(c);						break;
	    	case 42: // net.minecraft.item.ItemShears
	    		transformItemShears(c);										break;
	    	case 43: //net.minecraft.entity.EntityTrackerEntry
				transformEntityTrackerEntry(c);								break;
			case 44: // lotr.common.entity.projectile.LOTREntityProjectileBase
				transformLOTREntityProjectileBase(c);						break;
			case 45: // net.minecraft.entity.projectile.EntityArrow
				transformEntityArrow(c);									break;
			case 46: // lotr.common.LOTRPlayerData
				transformLOTRPlayerData(c);									break;
			case 47: // net.minecraft.entity.player.EntityPlayerMP
				transformEntityPlayerMP(c);									break;
			case 48: // net.minecraft.client.multiplayer.WorldClient
				transformWorldClient(c);									break;
			case 49: // lotr.common.world.map.LOTRCustomWaypoint
				transformLOTRCustomWaypoint(c);								break;
			case 50: // lotr.common.tileentity.LOTRTileEntityAlloyForgeBase
				transformLOTRTileEntityAlloyForgeBase(c);					break;
			case 51: // cpw.mods.fml.common.Loader
				transformLoader(c);											break;
			/*case 52: // net.minecraft.world.chunk.storage.ExtendedBlockStorage
				transformExtendedBlockStorage(c);							break;
			case 53: // net.minecraft.world.chunk.Chunk
				transformChunk(c);											break;
			case 54: // net.minecraft.block.Block
				transformBlock(c);											break;
			case 55: // net.minecraft.client.renderer.WorldRenderer
				transformWorldRenderer(c);									break;*/
			case 56: // net.minecraft.client.renderer.RenderGlobal
				transformRenderGlobal(c);									break;
			case 57: // WrUpdaterSmooth
				transformWrUpdaterSmooth(c);								break;
			case 58: // lotr.common.network.LOTRPacketBiomeVariantsUnwatch$Handler
				transformLOTRPacketBiomeVariantsUnwatch$Handler(c);			break;
			case 59: // metweaks.farview.FastCraftHelper
				transformFastCraftHelper(c);								break;
			case 60: // net.minecraft.entity.item.EntityItemFrame
			case 61: // net.minecraft.entity.EntityHanging
			case 62: // net.minecraft.entity.item.EntityMinecart
				transformBannerProtectable(c, false); 						break;
			case 63: // lotr.common.entity.item.LOTREntityRugBase
			case 64: // lotr.common.entity.item.LOTREntityBanner
				transformBannerProtectable(c, true); 						break;
			case 65: // lotr.common.LOTRBannerProtection$FilterForPlayer
				transformLOTRBannerProtection$FilterForPlayer(c); 			break;
			case 66: // net.minecraft.block.BlockFarmland
				transformBlockFarmland(c);									break;
			case 67: // net.minecraft.entity.projectile.EntityLargeFireball
				transformEntityLargeFireball(c);							break;
			case 68: // team.chisel.ctmlib.CTMRenderer
				transformCTMRenderer(c);									break;
			case 69: // lotr.common.LOTRMod
				transformLOTRMod(c);										break;
			case 70: // net.minecraft.world.Explosion
				transformExplosion(c);										break;	
			case 71: // net.minecraft.client.network.NetHandlerPlayClient
				replaceFBConstructors(c, 0);								break;
			case 72: // net.minecraft.block.BlockFalling
				replaceFBConstructors(c, 2);								break;
			case 73: // lotr.common.block.LOTRBlockSlabFalling
				replaceFBConstructors(c, 1);								break;
			case 74: // lotr.common.entity.npc.LOTREntityRohanBannerBearer
				transformBannerBearer(c);									break;
			case 75: // net.minecraft.world.WorldServer
				transformWorldServer(c);									break;
			case 76: // lotr.common.block.LOTRBlockChandelier
				transformLOTRBlockChandelier(c);							break;
			case 77: // lotr.common.entity.npc.LOTREntityRangerNorthBannerBearer
				transformBannerBearer(c);									break;
			case 78: // lotr.common.item.LOTRItemSpear
				transformLOTRItemSpear(c);									break;
			case 79: // lotr.common.block.LOTRBlockGate
				transformLOTRBlockGate(c);									break;
			case 80: // net.minecraft.entity.item.EntityFireworkRocket
				transformEntityFireworkRocket(c);							break;
			case 81: // lotr.client.render.entity.LOTRRenderEnt
				transformLOTRRenderEnt(c);									break;
			case 82: // lotr.common.entity.npc.LOTREntityEnt
				transformLOTREntityEnt(c);									break;
			case 83: // net.minecraft.block.BlockFire
				transformBlockFire(c);										break;
			case 84: // lotr.common.LOTRNetHandlerPlayServer
				transformLOTRNetHandlerPlayServer(c);						break;
			case 85: // lotr.common.world.map.LOTRWaypoint
				transformLOTRWaypoint(c);									break;
			case 86: // net.minecraftforge.common.ISpecialArmor$ArmorProperties
				transformArmorProperties(c);								break;
			case 87: // net.minecraft.entity.EntityLivingBase
				transformEntityLivingBase(c);								break;
			case 88: // net.minecraft.client.entity.EntityOtherPlayerMP
				applyHeadYawRotationPatch(c, false);						break;
			case 89: // net.minecraft.network.NetworkSystem
				transformNetworkSystem(c);									break;
			case 90: // net.minecraft.network.NetworkManager
				transformNetworkManager(c);									break;
			case 91: // metweaks.features.NetworkCalls
				transformNetworkCalls(c);									break;
			case 92: // net.minecraft.server.MinecraftServer
				transformMinecraftServer(c);								break;
			case 93: // net.minecraft.client.Minecraft
				transformMinecraft(c);										break;
			case 94: // net.minecraft.client.network.OldServerPinger$2
				patchTCPNoDelay(c);											break;
			case 95: // net.minecraft.network.NetworkSystem$1
				patchTCPNoDelay(c);											break;
			case 96: // net.minecraft.network.NetworkManager$2
				patchTCPNoDelay(c);											break;
			case 97: // net.minecraft.world.World
				transformWorld(c);											break;
			case 98: // me.jellysquid.mods.sodium.client.render.chunk.map.ChunkTracker
				transformChunkTracker(c);									break;
			case 99: // metweaks.features.Getters
				transformGetters(c);										break;
			case 100: // net.minecraft.network.NetHandlerPlayServer
				transformNetHandlerPlayServer(c);							break;
			case 101: // metweaks.farview.CeleritasCalls
				transformCeleritasCalls(c);									break;
			case 102: // com.mitchej123.hodgepodge.client.ClientTicker
				transformClientTicker(c);									break;
			case 103: // lotr.common.playerdetails.LOTRPlayerDetailsCache
				transformLOTRPlayerDetailsCache(c);							break; 
			
	    	
		}
	}
	
	static byte[] transform(final byte[] classBytes, final int i) {
        //try {
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(classBytes);
            classReader.accept(classNode, 0);
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            
            callByIndex(classNode, i);
            
            classNode.accept(classWriter);
            return classWriter.toByteArray();
            
            //byte[] bytes = classWriter.toByteArray();
            
            //return bytes;
        //}
        //catch (final Exception e) {
        //    e.printStackTrace();
        //}
        //return classBeingTransformed;
    }
	
	public static AbstractInsnNode lastBackwards(AbstractInsnNode last, int code) {
		while(last != null && last.getOpcode() != code) {
			last = last.getPrevious();
		}
		return last;
	}
	
	public static boolean isMethod(AbstractInsnNode node, String owner, String name, String desc) {
		MethodInsnNode method = (MethodInsnNode) node;
		if(name.equals(method.name) && desc.equals(method.desc) && owner.equals(method.owner)) {
			return true;
		}
		return false;
	}
}
