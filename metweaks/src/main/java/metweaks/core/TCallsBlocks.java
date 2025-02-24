package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.core.ClassHandler.*;

import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.util.TraceClassVisitor;

import metweaks.ASMConfig;

public class TCallsBlocks {
	static void transformItemSlab(ClassNode classNode) {
    	String name_use = obf ? "a" : "onItemUse"; // func_77648_a
    	String desc_use = obf ? "(Ladd;Lyz;Lahb;IIIIFFF)Z" : "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z";
    	String name = obf ? "a" : "func_150936_a";
    	String desc = obf ? "(Lahb;IIIILyz;Ladd;)Z" : "(Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z";
     	for(MethodNode method : classNode.methods) {
     		if(method.name.equals(name_use) && method.desc.equals(desc_use)) {
     			for(AbstractInsnNode node : method.instructions.toArray()) {
     				
     				if(node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 11) { // block
     					InsnList list = new InsnList();
     					list.add(new VarInsnNode(ALOAD, 2)); // player
     					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "isVerticalMode", obf ? "(Lyz;)Z" : "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
     	     			LabelNode label = new LabelNode();
     	     			list.add(new JumpInsnNode(IFEQ, label));
     	     			list.add(new VarInsnNode(ALOAD, 0)); // this (itemslab)
     	     			list.add(new FieldInsnNode(GETFIELD, obf ? "aeg" : "net/minecraft/item/ItemSlab", obf ? "c" : "field_150949_c", obf ? "Lalj;" : "Lnet/minecraft/block/BlockSlab;")); // singleslab
     	     			list.add(new VarInsnNode(ALOAD, 0)); // this (itemslab)
     	     			list.add(new FieldInsnNode(GETFIELD, obf ? "aeg" : "net/minecraft/item/ItemSlab", obf ? "d" : "field_150947_d", obf ? "Lalj;" : "Lnet/minecraft/block/BlockSlab;")); // doubleslab
    	     	    	list.add(new VarInsnNode(ALOAD, 1)); // stack
    	     	    	list.add(new VarInsnNode(ALOAD, 2)); // player
    	     	    	list.add(new VarInsnNode(ALOAD, 3)); // world
    	     	    	list.add(new VarInsnNode(ILOAD, 4)); // x
    	     	    	list.add(new VarInsnNode(ILOAD, 5)); // y
    	     	    	list.add(new VarInsnNode(ILOAD, 6)); // z
    	     	    	list.add(new VarInsnNode(ILOAD, 7)); // side
    	     	    	list.add(new VarInsnNode(FLOAD, 8)); // hitx
    	     	    	list.add(new VarInsnNode(FLOAD, 9)); // hity
    	     	    	list.add(new VarInsnNode(FLOAD, 10)); // hitz
    	     	    	list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onPlaceVertical", 
    	     	    			"(Lnet/minecraft/block/BlockSlab;Lnet/minecraft/block/Block;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z", false));
    	     			list.add(new InsnNode(IRETURN));
    	     			list.add(label);
    	     			method.instructions.insertBefore(node
    	     					.getPrevious()
    	     					.getPrevious()
    	     					.getPrevious()
    	     					.getPrevious()
    	     					.getPrevious()
    	     					.getPrevious()
    	     					, list);
    	     			log.info(coreprefix+": Patched ItemSlab.onItemUse()");
    	     			break;
     				}
     			}
     		}
     		// client
     		else if(method.name.equals(name) && method.desc.equals(desc)) {
     			
     			InsnList list = new InsnList();
     			list.add(new VarInsnNode(ALOAD, 6)); // player
     			// clientside, we can use clientVertical here
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "isVerticalMode", obf ? "(Lyz;)Z" : "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
	     			
     			LabelNode label = new LabelNode();
     			list.add(new JumpInsnNode(IFEQ, label));
     			list.add(new VarInsnNode(ALOAD, 0)); // this (itemslab)
     			list.add(new FieldInsnNode(GETFIELD, obf ? "aeg" : "net/minecraft/item/ItemSlab", obf ? "c" : "field_150949_c", obf ? "Lalj;" : "Lnet/minecraft/block/BlockSlab;")); // singleslab
     			
     	    	list.add(new VarInsnNode(ALOAD, 1)); // world
     	    	list.add(new VarInsnNode(ILOAD, 2)); // x
     	    	list.add(new VarInsnNode(ILOAD, 3)); // y
     	    	list.add(new VarInsnNode(ILOAD, 4)); // z
     	    	list.add(new VarInsnNode(ILOAD, 5)); // side
     	    	list.add(new VarInsnNode(ALOAD, 6)); // player
     	    	list.add(new VarInsnNode(ALOAD, 7)); // stack
     	    	list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "canPlaceVerticalHere", 
     	    			"(Lnet/minecraft/block/BlockSlab;Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z", false));
     			list.add(new InsnNode(IRETURN));
     			list.add(label);
     			method.instructions.insert(list);
     			log.info(coreprefix+": Patched ItemSlab.func_150936_a()");
     		}
     	}
     	
	}
    
    static void transformBlockSlab(ClassNode classNode) {
    	String name = obf ? "g" : "setBlockBoundsForItemRender";
     	for(MethodNode method : classNode.methods) {
     		if((method.name.equals(name) && method.desc.equals("()V"))) {
     			method.instructions.clear();
     			InsnList list = new InsnList();
     	    	list.add(new VarInsnNode(ALOAD, 0)); // block
     	    	list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "setBoundsForItemRender", "(Lnet/minecraft/block/Block;)V", false));
     			list.add(new InsnNode(RETURN));
     			method.instructions.insert(list);
     			log.info(coreprefix+": Replaced BlockSlab.setBlockBoundsForItemRender()");
     			return;
     		}
     	}
     	
	}
	
	static void transformBlockFenceGate(ClassNode classNode) {
    	String name = obf ? "c" : "canPlaceBlockAt";
    	String desc = obf ? "(Lahb;III)Z" : "(Lnet/minecraft/world/World;III)Z";
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals(desc)) {
    			method.instructions.clear();
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // block (this)
    			list.add(new VarInsnNode(ALOAD, 1)); // world
    			list.add(new VarInsnNode(ILOAD, 2)); // x
    			list.add(new VarInsnNode(ILOAD, 3)); // y
    			list.add(new VarInsnNode(ILOAD, 4)); // z
    			
    			list.add(new MethodInsnNode(INVOKESPECIAL, obf ? "akk" : "net/minecraft/block/BlockDirectional", name, desc, false)); // canPlaceBlockAt
    			list.add(new InsnNode(IRETURN));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched BlockFenceGate.canPlaceBlockAt()");
    			return;
    		}
    	}
	}
	
	static void transformBlockTrapdoor(ClassNode classNode) {
		String name = obf ? "a" : "onBlockPlaced";
     	String desc = obf ? "(Lahb;IIIIFFFI)I" : "(Lnet/minecraft/world/World;IIIIFFFI)I";
     	
     	String nameBy = obf ? "a" : "onBlockPlacedBy";
     	String descBy = obf ? "(Lahb;IIILsv;Ladd;)V" : "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;)V";
	
     	MethodNode methodnode = new MethodNode(ACC_PUBLIC, nameBy, descBy, null, null);
		
		InsnList ilist = new InsnList();
    	ilist.add(new VarInsnNode(ALOAD, 1)); // world
    	ilist.add(new VarInsnNode(ILOAD, 2)); // x
		ilist.add(new VarInsnNode(ILOAD, 3)); // y
		ilist.add(new VarInsnNode(ILOAD, 4)); // z
		ilist.add(new VarInsnNode(ALOAD, 5)); // living
		ilist.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onBlockPlacedByTP", "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/EntityLivingBase;)V", false));
		ilist.add(new InsnNode(RETURN));
		methodnode.instructions.insert(ilist);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method onBlockPlacedBy() to BlockTrapdoor");
     	
     	for(MethodNode method : classNode.methods) {
     		if((method.name.equals(name) && method.desc.equals(desc))) {
     			method.instructions.clear();
     			InsnList list = new InsnList();
    	    	list.add(new VarInsnNode(ILOAD, 5)); // side
    	    	list.add(new VarInsnNode(FLOAD, 7)); // hitY
    			list.add(new VarInsnNode(ILOAD, 9)); // meta
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onBlockPlacedTP", "(IFI)I", false));
    			list.add(new InsnNode(IRETURN));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Replaced BlockTrapdoor.onBlockPlaced()");
    			return;
     		}
     	}
    }

	static void transformBlockRotatedPillar(ClassNode classNode) {
		String name = obf ? "a" : "onBlockPlaced";
     	String desc = obf ? "(Lahb;IIIIFFFI)I" : "(Lnet/minecraft/world/World;IIIIFFFI)I";
     	 
     	for(MethodNode method : classNode.methods) {
     		if(method.name.equals(name) && method.desc.equals(desc)) {
	    		for(AbstractInsnNode node : method.instructions.toArray()) {
	    			if(node.getOpcode() == IRETURN) {
	    				InsnList toInsert = new InsnList();
	    				LabelNode label = new LabelNode();
	    				
	        			toInsert.add(new VarInsnNode(ILOAD, 9));
	        			toInsert.add(new InsnNode(ICONST_3));
	        			toInsert.add(new InsnNode(IOR));
	        			toInsert.add(new IntInsnNode(BIPUSH, 15));
	        			toInsert.add(new JumpInsnNode(IF_ICMPNE, label));
	        			toInsert.add(new IntInsnNode(BIPUSH, 12));
	        			toInsert.add(new VarInsnNode(ISTORE, 11));
	        			toInsert.add(label);
	        			method.instructions.insertBefore(node.getPrevious().getPrevious().getPrevious(), toInsert);
	        			log.info(coreprefix+": Patched BlockRotatedPillar.onBlockPlaced()");
	        			return;
	    			}
	    		}
	    	}
     	}
     	
	}
	
	static void transformSubBlocks(ClassNode classNode, boolean vanilla) {
		String name = obf ? vanilla ? "a" : "func_149666_a" : "getSubBlocks";
    	String desc = obf && vanilla ? "(Ladb;Labt;Ljava/util/List;)V" : "(Lnet/minecraft/item/Item;Lnet/minecraft/creativetab/CreativeTabs;Ljava/util/List;)V";
    	
    	String name_pickblock = "getPickBlock";
    	String desc_pickblock = "(Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;";
    	
    	
		MethodNode methodnode = new MethodNode(ACC_PUBLIC, name_pickblock, desc_pickblock, null, null);
		
		InsnList ilist = new InsnList();
    	ilist.add(new VarInsnNode(ALOAD, 0)); // block (this)
    	ilist.add(new VarInsnNode(ALOAD, 1)); // movingobjpos
		ilist.add(new VarInsnNode(ALOAD, 2)); // world
		ilist.add(new VarInsnNode(ILOAD, 3)); // x
		ilist.add(new VarInsnNode(ILOAD, 4)); // y
		ilist.add(new VarInsnNode(ILOAD, 5)); // z
		ilist.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getPickBlock", "(Lnet/minecraft/block/Block;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/world/World;III)Lnet/minecraft/item/ItemStack;", false));
		ilist.add(new InsnNode(ARETURN));
		methodnode.instructions.insert(ilist);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method getPickBlock() to "+classNode.name);
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals(desc)) {
      			MethodNode mv = new MethodNode();
      			method.instructions.accept(mv);
      			
      			AbstractInsnNode LOC_begin = null;
      			AbstractInsnNode LOC_end = method.instructions.getLast();
      			AbstractInsnNode LOC_end_clone = mv.instructions.getLast();
      			InsnList list = new InsnList();
      			while(LOC_end != null && LOC_end.getOpcode() != POP) {
  					LOC_end = LOC_end.getPrevious();
  				}
      			while(LOC_end_clone != null && LOC_end_clone.getOpcode() != POP) {
      				LOC_end_clone = LOC_end_clone.getPrevious();
  				}
      			
      			if(LOC_end == null) {
      				log.error(coreprefix+": Unable to patch getSubBlocks() in "+classNode.name);
      				return;
      			}
      			
      			for(AbstractInsnNode node : mv.instructions.toArray()) {
      				if(LOC_begin == null && node.getOpcode() == ALOAD && ((VarInsnNode) node).var == 3) { // first list
      					LOC_begin = node;
      				}
      				
      				if(LOC_begin != null) {
      					list.add(node);
      					if(node.getNext().getOpcode() == INVOKESPECIAL) { // add when meta is set (ILOAD index or ICONST_1 to 3
      						list.add(new LdcInsnNode(12));
      						list.add(new InsnNode(IADD));
      					}
      				}

      				if(LOC_end_clone == node)
      					break;
      			}
      			
      			if(list.size() == 0) {
      				log.error(coreprefix+": Unable to patch getSubBlocks() in "+classNode.name);
      				return;
      			}
      			
      			// WARN AbstractInsnNode.nextInsn and AbstractInsnNode.previousInsn are wrong now!
      			method.instructions.insert(LOC_end, list);
      			log.info(coreprefix+": Patched "+classNode.name+".getSubBlocks()");
      			return;
	    	}
      	}
	}
	
	static void transformItemCarvableSlab(ClassNode classNode) {
    	String name_use = "func_77648_a"; // func_77648_a
    	String desc_use = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z";
    	for(MethodNode method : classNode.methods) {
     		if(method.name.equals(name_use) && method.desc.equals(desc_use)) {
     			//for(AbstractInsnNode node : method.instructions.toArray()) {
     				
     				//if(node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 11) { // block
     					InsnList list = new InsnList();
     					list.add(new VarInsnNode(ALOAD, 2)); // player
     					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "isVerticalMode", obf ? "(Lyz;)Z" : "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
     	     			LabelNode label = new LabelNode();
     	     			list.add(new JumpInsnNode(IFEQ, label));
     	     			list.add(new VarInsnNode(ALOAD, 0)); // this (itemslab)
     	     			list.add(new FieldInsnNode(GETFIELD, "team/chisel/item/ItemCarvableSlab", "field_150939_a", "Lnet/minecraft/block/Block;")); // singleslab
     	     			//list.add(new VarInsnNode(ALOAD, 0)); // this (itemslab)
     	     			//list.add(new FieldInsnNode(GETFIELD, "team/chisel/item/ItemCarvableSlab", "field_150939_a", "Lnet/minecraft/block/Block;")); // doubleslab
    	     	    	list.add(new VarInsnNode(ALOAD, 1)); // stack
    	     	    	list.add(new VarInsnNode(ALOAD, 2)); // player
    	     	    	list.add(new VarInsnNode(ALOAD, 3)); // world
    	     	    	list.add(new VarInsnNode(ILOAD, 4)); // x
    	     	    	list.add(new VarInsnNode(ILOAD, 5)); // y
    	     	    	list.add(new VarInsnNode(ILOAD, 6)); // z
    	     	    	list.add(new VarInsnNode(ILOAD, 7)); // side
    	     	    	list.add(new VarInsnNode(FLOAD, 8)); // hitx
    	     	    	list.add(new VarInsnNode(FLOAD, 9)); // hity
    	     	    	list.add(new VarInsnNode(FLOAD, 10)); // hitz
    	     	    	list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "onPlaceVerticalChisel", 
    	     	    			"(Lnet/minecraft/block/Block;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z", false));
    	     			list.add(new InsnNode(IRETURN));
    	     			list.add(label);
    	     			method.instructions.insert(list);
    	     			log.info(coreprefix+": Patched ItemCarvableSlab.onItemUse()");
    	     			break;
     				//}
     			//}
     		}
     	}
     	
	}
	
	
	protected String[] names;
	
	public String getBlockName(int meta) {
		if(meta >= names.length)
			return String.valueOf(meta);
		return this.names[meta];
	}
	
	
	static void transformLOTRFABlockMultiWoodBeam(ClassNode classNode) {
		
        
        /*
        ILOAD meta
        ALOAD this
        GETFIELD metweaks/core/TCallsBlocks.names [Ljava/lang/String;
        ARRAYLENGTH
        IF_ICMPLT label
        
        ILOAD meta
        INVOKESTATIC java/lang/String.valueOf(Ljava/lang/Object;)Ljava/lang/String;
        ARETURN
        
        label
        */
        
        
        for(MethodNode method : classNode.methods) {
     		if(method.name.equals("getBlockName") && method.desc.equals("(I)Ljava/lang/String;")) {
     			//method.visitFrame(F_FULL, 0, new Object[] {}, 0, new Object[] {});
     			
     			LabelNode label = new LabelNode();
     	        InsnList list = new InsnList();
     	        list.add(new VarInsnNode(ILOAD, 1));
     	        list.add(new VarInsnNode(ALOAD, 0));
     	        list.add(new FieldInsnNode(GETFIELD, "eoa/lotrfa/common/block/multi/LOTRFABlockMultiWoodBeam", "names", "[Ljava/lang/String;"));
     	        list.add(new InsnNode(ARRAYLENGTH));
     	        list.add(new JumpInsnNode(IF_ICMPLT, label));
     	        
     	        list.add(new VarInsnNode(ILOAD, 1));
     	        list.add(new MethodInsnNode(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false));
     	        list.add(new InsnNode(ARETURN));
     	        
     	        list.add(new FrameNode(F_SAME, 2, null, 0, null)); // current 2 locals, this and int meta
     	        list.add(label);
     	        
     	        list.add(new VarInsnNode(ALOAD, 0));
    	        list.add(new FieldInsnNode(GETFIELD, "eoa/lotrfa/common/block/multi/LOTRFABlockMultiWoodBeam", "names", "[Ljava/lang/String;"));
    	        list.add(new VarInsnNode(ILOAD, 1));
    	        list.add(new InsnNode(AALOAD));
    	        list.add(new InsnNode(ARETURN));
    	        
    	        
    	        method.instructions.clear();
    	        method.localVariables.clear();
    	        
     			method.instructions.insert(list);
     			log.info(coreprefix+": Patched LOTRFABlockMultiWoodBeam.getBlockName()");
     			return;
     		}
        }
	}
}
