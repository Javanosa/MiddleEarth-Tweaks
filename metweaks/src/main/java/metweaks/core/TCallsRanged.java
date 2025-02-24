package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.core.ClassHandler.*;

import org.objectweb.asm.tree.*;

import metweaks.ASMConfig;
import metweaks.guards.customranged.CustomRanged;

public class TCallsRanged {
	static void transformLOTREntityTauredainBlowgunner(ClassNode classNode) {
    	// disable patch if we use the new ranged bridge
    	
    	String name = obf ? "func_82196_d" : "attackEntityWithRangedAttack";
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;F)V")) {
	    		for(AbstractInsnNode node : method.instructions.toArray()) {
	    			if(node.getOpcode() == DUP) {
	    				LabelNode labelskippoison = new LabelNode();
	        			LabelNode labelskip = new LabelNode();
	        			
	        			InsnList list = new InsnList();
	        			list.add(new VarInsnNode(ALOAD, 0)); // this
	        			list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTREntityTauredainBlowgunner", obf ? "field_70146_Z" : "rand", "Ljava/util/Random;"));
	        			list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Random", "nextFloat", "()F", false));
	        			list.add(new LdcInsnNode(ASMConfig.tauredainPoisonDartChance / 100.0F)); // chance
	        			list.add(new InsnNode(FCMPG));
	        			list.add(new JumpInsnNode(IFGE, labelskippoison));
	        			list.add(new FieldInsnNode(GETSTATIC, "lotr/common/LOTRMod", "tauredainDartPoisoned", "Lnet/minecraft/item/Item;"));
	        			list.add(new JumpInsnNode(GOTO, labelskip));
	        			list.add(labelskippoison);
	        			
	        			method.instructions.insert(node.getNext(), labelskip);
	        			method.instructions.insertBefore(node.getNext(), list);
	        			log.info(coreprefix+": Patched LOTREntityTauredainBlowgunner.attackEntityWithRangedAttack()");
	        			return;
	    			}
	    		}
    		}
    	}
	}
	
	static void transformLOTREntityAIRangedAttack(ClassNode classNode) {
    	classNode.visitField(ACC_PUBLIC, "hasInit", "Z", null, null);
    	log.info(coreprefix+": Added Field needInit to LOTREntityAIRangedAttack");
    	
    	String nameUpdate = obf ? "func_75246_d" : "updateTask";
    	String nameReset = obf ? "func_75251_c" : "resetTask";
    	
    	String nameContinue = obf ? "func_75253_b" : "continueExecuting";
    	String nameExecute = obf ? "func_75250_a" : "shouldExecute";
    	
    	InsnList incrlist = new InsnList();
    	
    	InsnList insnUpdated = new InsnList();
    	
    	InsnList executeList = new InsnList();
    	
    	MethodNode methodToRemove = null;
    	
    	
    	if(ASMConfig.aiRangedImprovements) {
    		classNode.visitField(ACC_PUBLIC, "decisionWait", "Z", null, null);
        	log.info(coreprefix+": Added Field decisionWait to LOTREntityAIRangedAttack");
        	classNode.visitField(ACC_PUBLIC, "moveRangeSq", "F", null, null);
        	log.info(coreprefix+": Added Field moveRangeSq to LOTREntityAIRangedAttack");
        	
			insnUpdated.add(new VarInsnNode(ALOAD, 0)); // this
			
			insnUpdated.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksAiRanged.class), "getDecisionWait", "()Z", false));
			insnUpdated.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "decisionWait", "Z"));
			
			insnUpdated.add(new VarInsnNode(ALOAD, 0)); // this
			insnUpdated.add(new InsnNode(DUP)); // copy this
			
			insnUpdated.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "theOwner", "Lnet/minecraft/entity/EntityLiving;"));
			insnUpdated.add(new VarInsnNode(ALOAD, 0)); // this
			insnUpdated.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "attackRange", "F"));
			
			insnUpdated.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksAiRanged.class), "getMoveRangeSq", "(Lnet/minecraft/entity/EntityLiving;F)F", false));
			insnUpdated.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "moveRangeSq", "F"));
    	
			LabelNode labelcancel = new LabelNode();
			executeList.add(new VarInsnNode(ALOAD, 0)); // this
	    	executeList.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "theOwner", "Lnet/minecraft/entity/EntityLiving;"));
	    	executeList.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksAiRanged.class), "execute", "(Lnet/minecraft/entity/EntityLiving;)Lnet/minecraft/entity/EntityLivingBase;", false));
	    	executeList.add(new VarInsnNode(ASTORE, 1)); // store target
	    	executeList.add(new VarInsnNode(ALOAD, 1)); // target
	    	executeList.add(new JumpInsnNode(IFNULL, labelcancel)); // if(target != null)
	    	executeList.add(new VarInsnNode(ALOAD, 0)); // this
	    	executeList.add(new VarInsnNode(ALOAD, 1)); // target
	    	executeList.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "attackTarget", "Lnet/minecraft/entity/EntityLivingBase;"));
	    	executeList.add(new InsnNode(ICONST_1));
	    	executeList.add(new InsnNode(IRETURN));
	    	executeList.add(labelcancel);
	    	executeList.add(new InsnNode(ICONST_0));
	    	executeList.add(new InsnNode(IRETURN));
	    	
    	}
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.desc.equals("()V")) {
    			if(method.name.equals(nameUpdate)) {

    				int startIncr = -1;
    				boolean searchIncr = true;
    				boolean changeCondition = true;
	    			String itfcall = obf ? "func_82196_d" : "attackEntityWithRangedAttack";
	    			AbstractInsnNode[] insArr = method.instructions.toArray();
	    			for(int i = 0; i < insArr.length; i++) {
	    				AbstractInsnNode node = insArr[i];
	    				
	    				// remove first occurence of rangedAttackTime
	    				if(ASMConfig.aiRangedImprovements) {
	    					if(searchIncr) {
			    				if(node.getOpcode() == ALOAD) {
			    					startIncr = i;
			    				}
			    				else if(startIncr != -1 && node.getOpcode() == PUTFIELD && ((FieldInsnNode) node).name.equals("rangedAttackTime")) {
			    					for(int r = startIncr; r <= i; r++) { 
			    						AbstractInsnNode rNode =  insArr[r];
			    						method.instructions.remove(rNode);
			    						incrlist.add(rNode);
			    					}
			    					searchIncr = false;
			    				}
	    					}
	    					
	    					if(changeCondition && node.getOpcode() == DLOAD && ((VarInsnNode) node).var == 1) { // distanceSq
	    						
	    						AbstractInsnNode replaceNode = insArr[i+2];
	    						((FieldInsnNode) replaceNode).name = "moveRangeSq";
	    						
	    						
	    						AbstractInsnNode jumpNode = insArr[i+5];
	    						LabelNode skiplabel = ((JumpInsnNode) jumpNode).label;
	    						LabelNode startlabel = (LabelNode) jumpNode.getNext();
	    						
	    						InsnList list = new InsnList();
	    						list.add(new VarInsnNode(ILOAD, 3)); // canSee
	    						list.add(new JumpInsnNode(IFNE, startlabel));
	    						list.add(new VarInsnNode(ALOAD, 0)); // this
	    						list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "decisionWait", "Z"));
	    						list.add(new JumpInsnNode(IFEQ, skiplabel));
	    						method.instructions.insert(jumpNode, list);
	    						changeCondition = false;
	    					}
		    				
	    				}
	    				if(node.getOpcode() == INVOKEINTERFACE && ((MethodInsnNode) node).name.equals(itfcall)) {
	    					method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(CustomRanged.class), "launchProjectile", "(Lnet/minecraft/entity/IRangedAttackMob;Lnet/minecraft/entity/EntityLivingBase;F)V", false));
	    					break; // done for now
	    				}
	    			}
    			
    			
	    			InsnList list = new InsnList();
					LabelNode label = new LabelNode();
					
					list.add(new VarInsnNode(ALOAD, 0)); // this
					list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "hasInit", "Z"));
					list.add(new JumpInsnNode(IFNE, label));
					// START equipranged
					if(ASMConfig.guardsEquipRanged) {
						list.add(new VarInsnNode(ALOAD, 0)); // this
						list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "theOwnerRanged", "Lnet/minecraft/entity/IRangedAttackMob;"));
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(CustomRanged.class), "initRanged", "(Lnet/minecraft/entity/IRangedAttackMob;)V", false));
					}
					// STOP equipranged
					// START airangedimprovements
					if(ASMConfig.aiRangedImprovements) {
						list.add(insnUpdated);
					}
						
					
					// STOP airangedimprovements
					list.add(new VarInsnNode(ALOAD, 0)); // this
					list.add(new InsnNode(ICONST_1)); // hasInit = true
					list.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/ai/LOTREntityAIRangedAttack", "hasInit", "Z"));
					list.add(label);
					method.instructions.insert(list);
					
					// insert old incrementor for rangedAttackTime here
					if(ASMConfig.aiRangedImprovements) {
						method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), incrlist);
					}
					
					log.info(coreprefix+": Patched LOTREntityAIRangedAttack.updateTask()");
	    		}
    			else if(ASMConfig.aiRangedImprovements && method.name.equals(nameReset)) {
    				InsnList copyList = new InsnList();
	    			for(AbstractInsnNode t : insnUpdated.toArray()) {
	    				
	    				copyList.add(t.clone(null));
	    			}
					method.instructions.insert(copyList);
	    			
	    			log.info(coreprefix+": Patched LOTREntityAIRangedAttack.resetTask()");
	    		}
    		}
    		else if(ASMConfig.aiRangedImprovements && method.desc.equals("()Z")) {
    			boolean mContinue = method.name.equals(nameContinue);
    			if(mContinue || method.name.equals(nameExecute)) {
    				if(mContinue) {
    					methodToRemove = method;
    					
    				}
    				else {
	    				method.instructions.clear();
	    				method.localVariables.clear();
	    				method.instructions.insert(executeList);
	    				log.info(coreprefix+": Patched LOTREntityAIRangedAttack.shouldExecute()");
    				}
    			}
    		}
    	}
    	
    	if(methodToRemove != null) {
    		classNode.methods.remove(methodToRemove);
			log.info(coreprefix+": Removed LOTREntityAIRangedAttack.continueExecuting()");
    	}
    }
	
	static void transformLOTRContainerHiredWarriorInventory(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("<init>") && method.desc.equals("(Lnet/minecraft/entity/player/InventoryPlayer;Llotr/common/entity/npc/LOTREntityNPC;)V")) {
    			String searchCall = "isOrcBombardier";
    			boolean foundPart1 = false;
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				
    				if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(searchCall)) {
    					if(foundPart1) {
    						InsnList list = new InsnList();
    					    list.add(new VarInsnNode(ALOAD, 0)); // this
    						list.add(new VarInsnNode(ALOAD, 0)); // this
    						list.add(new FieldInsnNode(GETFIELD, "lotr/common/inventory/LOTRContainerHiredWarriorInventory", "theNPC", "Llotr/common/entity/npc/LOTREntityNPC;"));
    						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(CustomRanged.class), "setupContainer", "(Llotr/common/inventory/LOTRContainerHiredWarriorInventory;Llotr/common/entity/npc/LOTREntityNPC;)V", false));
    					    method.instructions.insert(node.getNext().getNext(), list);
    					    log.info(coreprefix+": Patched LOTRContainerHiredWarriorInventory.<init>()");
    						return;
    					}
    					searchCall = obf ? "func_75146_a" : "addSlotToContainer";
    					foundPart1 = true;
    				}
    			}
    		}
    	}
    }
	
	static void transformLOTRGuiHiredWarriorInventory(ClassNode classNode) {
    	String name = obf ? "func_146976_a" : "drawGuiContainerBackgroundLayer";
    	for(MethodNode method : classNode.methods) {
    		
    		if(method.name.equals(name) && method.desc.equals("(FII)V")) {
    			//CustomRanged.drawGuiContainerBackgroundLayer(containerInv, this, guiLeft, guiTop);
    			InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarriorInventory", "containerInv", "Llotr/common/inventory/LOTRContainerHiredWarriorInventory;"));
				list.add(new VarInsnNode(ALOAD, 0)); // this (instance)
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarriorInventory", obf ? "field_147003_i" : "guiLeft", "I"));
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, "lotr/client/gui/LOTRGuiHiredWarriorInventory", obf ? "field_147009_r" : "guiTop", "I"));
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(CustomRanged.class), "drawGuiContainerBackgroundLayer", "(Llotr/common/inventory/LOTRContainerHiredWarriorInventory;Llotr/client/gui/LOTRGuiHiredWarriorInventory;II)V", false));

				method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
				log.info(coreprefix+": Patched LOTRGuiHiredWarriorInventory.drawGuiContainerBackgroundLayer()");
				return;
    		}
    	}
    }
    
    static void transformLOTRInventoryHiredReplacedItems(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("setReplacedEquipment") && method.desc.equals("(ILnet/minecraft/item/ItemStack;Z)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == BASTORE) {
    					// flag = true to avoid execution of following codeblock
    					InsnList list = new InsnList();
    					list.add(new InsnNode(ICONST_1));
    					list.add(new VarInsnNode(ISTORE, 3)); // flag
    					method.instructions.insert(node, list);
    					log.info(coreprefix+": Patched LOTRInventoryHiredReplacedItems.setReplacedEquipment()");
    					break;
    				}
    				
    			}
    		}
    		else if(method.name.equals("equipReplacement") && method.desc.equals("(ILnet/minecraft/item/ItemStack;)V")) {
    			method.instructions.clear();
    			method.localVariables.clear();
    			// CustomRanged.equipReplacement(this, i, itemstack, replacedMeleeWeapons, theNPC, getReplacedEquipment(i));
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new VarInsnNode(ILOAD, 1)); // i (slot number)
    			list.add(new VarInsnNode(ALOAD, 2)); // itemstack
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRInventoryHiredReplacedItems", "replacedMeleeWeapons", "Z"));
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/npc/LOTRInventoryHiredReplacedItems", "theNPC", "Llotr/common/entity/npc/LOTREntityNPC;"));
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new VarInsnNode(ILOAD, 1)); // i (slot number)
    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "lotr/common/entity/npc/LOTRInventoryHiredReplacedItems", "getReplacedEquipment", "(I)Lnet/minecraft/item/ItemStack;", false));
			    
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(CustomRanged.class), "equipReplacement", "(Llotr/common/entity/npc/LOTRInventoryHiredReplacedItems;ILnet/minecraft/item/ItemStack;ZLlotr/common/entity/npc/LOTREntityNPC;Lnet/minecraft/item/ItemStack;)V", false));
    			list.add(new InsnNode(RETURN)); // this
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched LOTRInventoryHiredReplacedItems.equipReplacement()");
    			return; // prev method should have passed already, we can leave
    		}
    	}
    }
}
