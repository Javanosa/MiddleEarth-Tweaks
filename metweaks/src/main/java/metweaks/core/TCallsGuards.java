package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.core.ClassHandler.*;

import org.objectweb.asm.tree.*;

import metweaks.ASMConfig;
import metweaks.guards.ExtraHiredData;

public class TCallsGuards {
	static void transformEntityAITarget(ClassNode classNode) {
    	String name_use = obf ? "a" : "isSuitableTarget";
    	String desc_use = obf ? "(Lsv;Z)Z" : "(Lnet/minecraft/entity/EntityLivingBase;Z)Z";
    	for(MethodNode method : classNode.methods) {
    		
     		if(method.name.equals(name_use) && method.desc.equals(desc_use)) {
     			AbstractInsnNode[] instrarray = method.instructions.toArray();
     			for(int i = 0; i < instrarray.length; i++) {
     				AbstractInsnNode node = instrarray[i];
     				
     				if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(obf ? "b" : "isWithinHomeDistance")) {
     					
     					AbstractInsnNode next = node.getNext();
     					if(!(next instanceof JumpInsnNode))
     						return;
     					
     					LabelNode label = ((JumpInsnNode) next).label;
     					InsnList list = new InsnList();
     					list.add(new VarInsnNode(ALOAD, 0)); // entityaitarget
     					list.add(new FieldInsnNode(GETFIELD, obf ? "vu" : "net/minecraft/entity/ai/EntityAITarget", obf ? "c" : "taskOwner", obf ? "Ltd;" : "Lnet/minecraft/entity/EntityCreature;")); // npc
     					list.add(new VarInsnNode(ALOAD, 1)); // target
     					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), "isGuardingHired", obf ? "(Ltd;Lsv;)Z" : "(Lnet/minecraft/entity/EntityCreature;Lnet/minecraft/entity/EntityLivingBase;)Z", false));
     				
     					list.add(new JumpInsnNode(IFNE, label));
     	     			
     					
     	     			// move to condition line start
    	     			method.instructions.insertBefore(instrarray[i-11], list);
    	     			
    	     			for(int n=-1; n <= 11; n++) {
    	     				AbstractInsnNode node2 = instrarray[i-n];
    	     				
    	     				method.instructions.remove(node2);
    	     				
    	     			}
    	     			log.info(coreprefix+": Patched EntityAITarget.isSuitableTarget()");
    	     			break;
     				}
     			}
     		}
     	}
	}
    
    static void transformLOTRGuiHiredWarrior(ClassNode classNode) {
    	 
		 classNode.visitField(ACC_PUBLIC, "sliderWanderRange", "Llotr/client/gui/LOTRGuiSlider;", null, null);
		 log.info(coreprefix+": Added Field sliderWanderRange to LOTRGuiHiredWarrior");
		 
    	 /*if(ASMConfig.guardsAdvancedSettings) {
    		 classNode.visitField(ACC_PUBLIC, "buttonAdvanced", "Llotr/client/gui/LOTRGuiButtonOptions;", null, null);
    		 log.info(coreprefix+": Added Field buttonAdvanced to LOTRGuiHiredWarrior");
    	 }*/
    		 
    	 
    	 for(MethodNode method : classNode.methods) {
    		 if(method.desc.equals("()V")) {
	    		 if(method.name.equals(obf ? "func_73866_w_" : "initGui")) {
	    			 for(AbstractInsnNode node : method.instructions.toArray()) {
	    				// after ASTORE squadron
	     				if(node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 2) { // squadron
	     					InsnList list = new InsnList();
							list.add(new VarInsnNode(ALOAD, 0)); // gui
							list.add(new VarInsnNode(ALOAD, 0)); // gui
							list.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarrior", "squadronNameField", "Lnet/minecraft/client/gui/GuiTextField;"));
							list.add(new VarInsnNode(ALOAD, 0)); // gui
							list.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarrior", "buttonTeleport", "Llotr/client/gui/LOTRGuiButtonOptions;"));
							list.add(new VarInsnNode(ALOAD, 0)); // gui
							list.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarrior", obf ? "field_146292_n" : "buttonList", "Ljava/util/List;"));
							list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), "initWarriorScreen", "(Llotr/client/gui/LOTRGuiHiredWarrior;Lnet/minecraft/client/gui/GuiTextField;Llotr/client/gui/LOTRGuiButtonOptions;Ljava/util/List;)V", false)); 
							method.instructions.insert(node, list);
							log.info(coreprefix+": Patched LOTRGuiHiredWarrior.initGui()");		
	     					break;
	     				}
	    			 }
	    		 }
	    		 else if(method.name.equals(obf ? "func_73876_c" : "updateScreen")) {
	    			 for(AbstractInsnNode node : method.instructions.toArray()) {
	    				 if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(obf ? "func_146178_a" : "updateCursorCounter")) {
	    					InsnList list = new InsnList();
							list.add(new VarInsnNode(ALOAD, 0)); // gui
							list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), "updateWarriorScreen", "(Llotr/client/gui/LOTRGuiHiredWarrior;)V", false)); 
							method.instructions.insert(node, list);
							log.info(coreprefix+": Patched LOTRGuiHiredWarrior.updateScreen()");		
	     					break;
	    				 }
	    			 }
	    		 }
    		 }
    	 }
    }
    
    static void transformLOTRPacketHiredGui(ClassNode classNode) {
    	classNode.visitField(ACC_PUBLIC, "wanderRange", "I", null, null);
    	log.info(coreprefix+": Added Field wanderRange to LOTRPacketHiredGui");
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.desc.equals("(Lio/netty/buffer/ByteBuf;)V")) {
    			
    			if(method.name.equals("fromBytes")) {
    				InsnList list = new InsnList();
    				LabelNode label = new LabelNode();
    				list.add(new VarInsnNode(ALOAD, 1)); // data
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "io/netty/buffer/ByteBuf", "isReadable", "()Z", false));
    				list.add(new JumpInsnNode(IFEQ, label));
    				list.add(new VarInsnNode(ALOAD, 0)); // packet
	    			list.add(new VarInsnNode(ALOAD, 1)); // data
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "io/netty/buffer/ByteBuf", "readInt", "()I", false));
	    			list.add(new FieldInsnNode(PUTFIELD, "lotr/common/network/LOTRPacketHiredGui", "wanderRange", "I")); // wanderRange
	    			list.add(label);
	    			method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
	    			log.info(coreprefix+": Patched LOTRPacketHiredGui.fromBytes()");
        		}
        		else if(method.name.equals("toBytes")) {
        			InsnList list = new InsnList();
        			list.add(new VarInsnNode(ALOAD, 1)); // data
	    			list.add(new VarInsnNode(ALOAD, 0)); // packet
	    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/network/LOTRPacketHiredGui", "wanderRange", "I")); // wanderRange
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "io/netty/buffer/ByteBuf", "writeInt", "(I)Lio/netty/buffer/ByteBuf;", false));
	    			list.add(new InsnNode(POP));
	    			method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
	    			log.info(coreprefix+": Patched LOTRPacketHiredGui.toBytes()");
        		}
    		}
    	}
    	
    }
    
    static void transformLOTRPacketHiredUnitCommand$Handler(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("onMessage") && method.desc.equals("(Llotr/common/network/LOTRPacketHiredUnitCommand;Lcpw/mods/fml/common/network/simpleimpl/MessageContext;)Lcpw/mods/fml/common/network/simpleimpl/IMessage;")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				// insert after first one, then stop
    				//printNode(node);
    				/*
					ILOAD page			
					ICONST_1
					IF_ICMPNE AK*/
    				
    				if(node.getOpcode() == ILOAD && ((VarInsnNode) node).var == 7 && node.getNext().getOpcode() == ICONST_1) { // page
    					InsnList list = new InsnList();
    	    			list.add(new VarInsnNode(ALOAD, 6)); // hiredNPC
    	    			list.add(new VarInsnNode(ILOAD, 8)); // action
    	    			list.add(new VarInsnNode(ILOAD, 9)); // value
    	    			//list.add(new VarInsnNode(ALOAD, 3)); // player
    	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), "onHiredUnitCommand", "(Llotr/common/entity/npc/LOTREntityNPC;II)V", false));
    	    			method.instructions.insert(node.getNext().getNext(), list);
    	    			log.info(coreprefix+": Patched LOTRPacketHiredUnitCommand$Handler.onMessage()");
    	    			//return;
    				}
    			}
    			
    		}
    	}
    }
    
    static void transformLOTRHiredNPCInfo(ClassNode classNode) {
    	if(ASMConfig.guardsSyncSettings || ASMConfig.guardsWanderRange) {
	    	for(FieldNode field : classNode.fields) {
	    		// we could use an access transformer here ngl
	    		if(field.name.equals("guardRange") && field.desc.equals("I") || 
	    			(ASMConfig.guardsAdvancedSettings && field.name.equals("theEntity")
	    			&& field.desc.equals("Llotr/common/entity/npc/LOTREntityNPC;"))
	    		) {
	    			field.access = ACC_PUBLIC;
	    			log.info(coreprefix+": Made LOTRHiredNPCInfo."+field.name+" public");
	    		}
	    	}
		}
	    	
    	if(ASMConfig.guardsAdvancedSettings) {
    		classNode.visitField(ACC_PUBLIC, "ext", Type.getDescriptor(ExtraHiredData.class), null, null);
    		log.info(coreprefix+": Added Field ext to LOTRHiredNPCInfo");
    	}
    	
    	if(ASMConfig.guardsWanderRange) {
    		classNode.visitField(ACC_PUBLIC, "wanderRange", "I", null, null);
    		log.info(coreprefix+": Added Field wanderRange to LOTRHiredNPCInfo");
    	}
    	
    	for(MethodNode method : classNode.methods) {
    		if(ASMConfig.guardsWanderRange) {
	    		if(method.name.equals("<init>") && method.desc.equals("(Llotr/common/entity/npc/LOTREntityNPC;)V")) {
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ALOAD, 0)); // info
	    			list.add(new FieldInsnNode(GETSTATIC, "lotr/common/entity/npc/LOTRHiredNPCInfo", "GUARD_RANGE_DEFAULT", "I"));
	    			list.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "wanderRange", "I"));
	    			method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
	    			log.info(coreprefix+": Patched LOTRHiredNPCInfo.<init>()");
	    		}
	    		
	    		else if(method.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
	    			boolean read = method.name.equals("readFromNBT");
	    			if(read || method.name.equals("writeToNBT")) {
	    				for(AbstractInsnNode node : method.instructions.toArray()) {
	    					//printNode(node);
		    				if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(read ? "readFromNBT" : "writeToNBT")) {
		    					InsnList list = new InsnList();
		    					list.add(new VarInsnNode(ALOAD, 0)); // info
		    	    			list.add(new VarInsnNode(ALOAD, 2)); // data
		    	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), method.name, "(Llotr/common/entity/npc/LOTRHiredNPCInfo;Lnet/minecraft/nbt/NBTTagCompound;)V", false));
		    	    			method.instructions.insert(node.getNext().getNext().getNext(), list);
		    	    			log.info(coreprefix+": Patched LOTRHiredNPCInfo."+method.name+"()");
		    	    			break;
		    				}
	    				}
	    			}
	    		}
	    		
	    		else if(method.name.equals("sendClientPacket") && method.desc.equals("(Z)V")) {
	    			for(AbstractInsnNode node : method.instructions.toArray()) {
	    				if(node.getOpcode() == GETSTATIC && ((FieldInsnNode) node).name.equals("networkWrapper")) {
			    			InsnList list = new InsnList();
			    			list.add(new VarInsnNode(ALOAD, 2)); // packet
			    			list.add(new VarInsnNode(ALOAD, 0)); // info
			    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "wanderRange", "I")); // wanderRange
			    			list.add(new FieldInsnNode(PUTFIELD, "lotr/common/network/LOTRPacketHiredGui", "wanderRange", "I")); // wanderRange
			    			method.instructions.insertBefore(node, list);
			    			log.info(coreprefix+": Patched LOTRHiredNPCInfo.sendClientPacket()");
			    			break;
	    				}
	    			}
	    		}
	    		else if(method.name.equals("receiveClientPacket") && method.desc.equals("(Llotr/common/network/LOTRPacketHiredGui;)V")) {
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ALOAD, 0)); // info
	    			list.add(new VarInsnNode(ALOAD, 1)); // packet
	    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/network/LOTRPacketHiredGui", "wanderRange", "I")); // wanderRange
	    			list.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "wanderRange", "I")); // wanderRange
	    			method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
	    			log.info(coreprefix+": Patched LOTRHiredNPCInfo.receiveClientPacket()");
	    		}
	    	}
    		
    		if(ASMConfig.guardsSyncSettings || ASMConfig.guardsWanderRange) {
    		
	    		if(method.name.equals("setGuardMode") && method.desc.equals("(Z)V")) {
	    			method.instructions.clear();
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ILOAD, 1)); // flag
	    			//list.add(new VarInsnNode(ALOAD, 0)); // info
					list.add(new VarInsnNode(ALOAD, 0)); // info
	    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "theEntity", "Llotr/common/entity/npc/LOTREntityNPC;"));
	    			//list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(PatchesGuardMode.class), "setGuardMode", "(ZLlotr/common/entity/npc/LOTRHiredNPCInfo;Llotr/common/entity/npc/LOTREntityNPC;)V", false));
	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), "setGuardMode", "(ZLlotr/common/entity/npc/LOTREntityNPC;)V", false));
	    			
	    			list.add(new InsnNode(RETURN));
	    			method.instructions.insert(list);
	    			log.info(coreprefix+": Patched LOTRHiredNPCInfo.setGuardMode()");
	    		}
	    		else if(method.name.equals("setGuardRange") && method.desc.equals("(I)V")) {
	    			method.instructions.clear();
	    			InsnList list = new InsnList();
					list.add(new VarInsnNode(ILOAD, 1)); // info
					list.add(new VarInsnNode(ALOAD, 0)); // info
	    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "theEntity", "Llotr/common/entity/npc/LOTREntityNPC;"));
	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksGuardMode.class), "setGuardRange", "(ILlotr/common/entity/npc/LOTREntityNPC;)V", false));
	    			list.add(new InsnNode(RETURN));
	    			method.instructions.insert(list);
	    			log.info(coreprefix+": Patched LOTRHiredNPCInfo.setGuardRange()");
	    		}
    		}
    		
    		if(ASMConfig.fixTeleportToHiringPlayer) {
    			if(method.name.equals("tryTeleportToHiringPlayer") && method.desc.equals("(Z)Z")) {
    				int found = 0;
	    			for(AbstractInsnNode node : method.instructions.toArray()) {
	    				if(node.getOpcode() == ICONST_1 && node.getNext().getOpcode() == IRETURN) {
	    					InsnList list = new InsnList();
	    					list.add(new VarInsnNode(ALOAD, 0)); // info
	    					list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "theEntity", "Llotr/common/entity/npc/LOTREntityNPC;"));
			    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "postTeleportToHiringPlayer", "(Llotr/common/entity/npc/LOTREntityNPC;)V", false));
	    					method.instructions.insertBefore(node, list);
	    					
	    					//break; // we need to place at all returns
	    					found++;
	    				}
	    			}
	    			if(found > 0)
	    				log.info(coreprefix+": Patched LOTRHiredNPCInfo.tryTeleportToHiringPlayer() ["+found+"]");
    			}
    		}
    		
    	}
	}
    
    static void transformHiredInfoAccess(ClassNode classNode) {
    	
    	for(MethodNode method : classNode.methods) {
			if(method.name.equals("setGuardRange") && method.desc.equals("(Llotr/common/entity/npc/LOTRHiredNPCInfo;I)V")) {
    			method.instructions.clear();
    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // hiredinfo
    			method.instructions.add(new VarInsnNode(ILOAD, 1)); // guardRange
    			method.instructions.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "guardRange", "I"));
    			method.instructions.add(new InsnNode(RETURN));
    			log.info(coreprefix+": Supply HiredInfoAccess.setGuardRange()");
    			if(!ASMConfig.guardsWanderRange) return; // we dont need more
    		}
    		
    		
    		if(ASMConfig.guardsWanderRange) {
	    		if(method.name.equals("getWanderRange") && method.desc.equals("(Llotr/common/entity/npc/LOTRHiredNPCInfo;)I")) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // hiredinfo
	    			method.instructions.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "wanderRange", "I"));
	    			method.instructions.add(new InsnNode(IRETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.getWanderRange()");
	    		}
	    		else if(method.name.equals("setWanderRange") && method.desc.equals("(Llotr/common/entity/npc/LOTRHiredNPCInfo;I)V")) {
	    			method.instructions.clear();
	    			//method.maxStack = 2;
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // hiredinfo
	    			method.instructions.add(new VarInsnNode(ILOAD, 1)); // wanderRange
	    			method.instructions.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "wanderRange", "I"));
	    			method.instructions.add(new InsnNode(RETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.setWanderRange()");
	    		}
	    		else if(method.name.equals("getNPC") && method.desc.equals("(Llotr/common/entity/npc/LOTRHiredNPCInfo;)Llotr/common/entity/npc/LOTREntityNPC;")) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // hiredinfo
	    			method.instructions.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "theEntity", "Llotr/common/entity/npc/LOTREntityNPC;"));
	    			method.instructions.add(new InsnNode(ARETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.getNPC()");
	    		}
	    		else if(method.name.equals("getExt") && method.desc.equals("(Llotr/common/entity/npc/LOTRHiredNPCInfo;)"+Type.getDescriptor(ExtraHiredData.class))) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // hiredinfo
	    			method.instructions.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "ext", Type.getDescriptor(ExtraHiredData.class)));
	    			method.instructions.add(new InsnNode(ARETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.getExt()");
	    		}
	    		
	    		
	    		else if(method.name.equals("setSliderWanderRange") && method.desc.equals("(Llotr/client/gui/LOTRGuiHiredWarrior;Llotr/client/gui/LOTRGuiSlider;)V")) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // gui
	    			method.instructions.add(new VarInsnNode(ALOAD, 1)); // sliderWanderRange
	    			method.instructions.add(new FieldInsnNode(PUTFIELD, "lotr/client/gui/LOTRGuiHiredWarrior", "sliderWanderRange", "Llotr/client/gui/LOTRGuiSlider;"));
	    			method.instructions.add(new InsnNode(RETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.setSliderWanderRange()");
	    		}
	    		/*else if(method.name.equals("setButtonAdvanced") && method.desc.equals("(Llotr/client/gui/LOTRGuiButtonOptions;)V")) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // gui
	    			method.instructions.add(new VarInsnNode(ALOAD, 1)); // buttonAdvanced
	    			method.instructions.add(new FieldInsnNode(PUTFIELD, "lotr/client/gui/LOTRGuiHiredWarrior", "buttonAdvanced", "Llotr/client/gui/LOTRGuiButtonOptions;"));
	    			method.instructions.add(new InsnNode(RETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.setButtonAdvanced()");
	    		}*/
	    		else if(method.name.equals("getSliderWanderRange") && method.desc.equals("(Llotr/client/gui/LOTRGuiHiredWarrior;)Llotr/client/gui/LOTRGuiSlider;")) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // gui
	    			method.instructions.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarrior", "sliderWanderRange", "Llotr/client/gui/LOTRGuiSlider;"));
	    			method.instructions.add(new InsnNode(ARETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.getSliderWanderRange()");
	    		}
	    		
	    		else if(method.name.equals("setExt") && method.desc.equals("(Llotr/common/entity/npc/LOTRHiredNPCInfo;"+Type.getDescriptor(ExtraHiredData.class)+")V")) {
	    			method.instructions.clear();
	    			method.instructions.add(new VarInsnNode(ALOAD, 0)); // hiredinfo
	    			method.instructions.add(new VarInsnNode(ALOAD, 1)); // ext
	    			method.instructions.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/npc/LOTRHiredNPCInfo", "ext", Type.getDescriptor(ExtraHiredData.class)));
	    			method.instructions.add(new InsnNode(RETURN));
	    			log.info(coreprefix+": Supply HiredInfoAccess.setExt()");
	    		}
    		}
    	}
    	
	}
}
