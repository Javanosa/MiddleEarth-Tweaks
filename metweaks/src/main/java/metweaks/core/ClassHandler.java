package metweaks.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import static metweaks.core.TCallsRanged.*;
import static metweaks.core.TCallsConquest.*;
import static metweaks.core.TCallsBlocks.*;
import static metweaks.core.TCallsGuards.*;
import static metweaks.core.TCallsMisc.*;

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
	    	
		}
	}
	
	static byte[] transform(final byte[] classBytes, final int i) {
        //try {
            final ClassNode classNode = new ClassNode();
            final ClassReader classReader = new ClassReader(classBytes);
            classReader.accept(classNode, 0);
            final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS/* | ClassWriter.COMPUTE_FRAMES*/);
            //System.out.println(classNode.name+" "+i);
            callByIndex(classNode, i);
            
            classNode.accept(classWriter);
            return classWriter.toByteArray();
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
	
	/*public static class LogWriter extends java.io.Writer {
        private final StringBuilder stringBuilder = new StringBuilder();

        @Override
        public void write(char[] cbuf, int off, int len) {
            stringBuilder.append(cbuf, off, len);
        }

        @Override
        public void flush() {
            ClassTransformer.log.info(stringBuilder.toString());
            stringBuilder.setLength(0);
        }

        @Override
        public void close() {}
    }*/
}
