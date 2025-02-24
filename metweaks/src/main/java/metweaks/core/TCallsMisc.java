package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.ASMConfig.draughtStackEffectsIncrease;
import org.objectweb.asm.tree.*;
import com.google.common.collect.HashMultimap;

import metweaks.ASMConfig;
import metweaks.client.LOTRGuiElements;

public class TCallsMisc {
	static void transformLOTRTickHandlerClient(ClassNode classNode) {
    	
		for(MethodNode method : classNode.methods) {
			if(method.name.equals("onRenderTick") && method.desc.equals("(Lcpw/mods/fml/common/gameevent/TickEvent$RenderTickEvent;)V")) {
				boolean compassY = false;
				LabelNode label = null;
				
				for(AbstractInsnNode node : method.instructions.toArray()) {
					
					if(node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals("alignmentXCurrent")) {
						AbstractInsnNode put = node.getPrevious().getPrevious().getPrevious().getPrevious();
						method.instructions.insertBefore(put, new FieldInsnNode(GETSTATIC, Type.getInternalName(LOTRGuiElements.class), "enableAlignmentBar", "Z"));
						label = new LabelNode();
						
						method.instructions.insertBefore(put, new JumpInsnNode(IFEQ, label));
						
						
					}
					else if(!compassY && node.getOpcode() == ISTORE) {
						int varIndex = ((VarInsnNode) node).var;
						compassY = varIndex == 11; // compassY
						if(compassY || varIndex == 10) { // compassX
							method.instructions.remove(node.getPrevious());  // ISUB // BIPUSH 40
							if(compassY) {
								method.instructions.insertBefore(node, new VarInsnNode(ILOAD, 9)); // j (height)
							}
							else {
								method.instructions.remove(node.getPrevious()); // BIPUSH 60 
								
							}
							
							method.instructions.insertBefore(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(LOTRGuiElements.class), compassY ? "getCompassY" : "getCompassX", "(I)I", false));
						}
						
						
						
					}
					// after INVOKESTATIC org/lwjgl/opengl/GL11.glTranslatef(FFF)V
					else if(node.getOpcode() == INVOKESTATIC && ((MethodInsnNode) node).name.equals("glTranslatef")) {
						method.instructions.insert(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(LOTRGuiElements.class), "scaleCompass", "()V", false));
						
					}
					else if(node.getOpcode() == INVOKESPECIAL && ((MethodInsnNode) node).name.equals("renderAlignment")) {
						//System.out.println("labellli"+label);
						method.instructions.insert(node, label);
						
					}
					/*
					LDC 0.5F
					FSTORE scale
					
					LDC 0.5F
					GETSTATIC metweaks/client/LOTRGuiElements.compassScale F
					FMUL
					FSTORE scale
					*/
					else if(node.getOpcode() == FSTORE && ((VarInsnNode) node).var == 13) { // scale
						
						method.instructions.insertBefore(node, new FieldInsnNode(GETSTATIC, Type.getInternalName(LOTRGuiElements.class), "compassScale", "F"));
						method.instructions.insertBefore(node, new InsnNode(FMUL));
						log.info(coreprefix+": Patched LOTRTickHandlerClient.onRenderTick()");
						return;
					}
				}
				
				
			}
		}
	}
    
    static void transformRingWriting(ClassNode classNode, boolean portal) {
    	String name = "render";
    	String desc = "(FF)V";
    	
    	if(portal) {
    		name = obf ? "func_76986_a" : "doRender";
    		desc = "(Lnet/minecraft/entity/Entity;DDDFF)V";
    	}
    	
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(name) && method.desc.equals(desc)) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == LDC) {
						Object ldc = ((LdcInsnNode) node).cst;
						
						if(ldc.equals(1.05F)) {
							
							method.instructions.set(node, new FieldInsnNode(GETSTATIC, Type.getInternalName(LOTRGuiElements.class), "writingOuterScale", "F"));
							
						}
						else if(ldc.equals(0.85F)) {
							method.instructions.set(node, new FieldInsnNode(GETSTATIC, Type.getInternalName(LOTRGuiElements.class), "writingInnerScale", "F"));
							log.info(coreprefix+": Patched "+ (portal ? "LOTRRenderPortal.doRender()" : "LOTRModelCompass.render()"));
							return;
						}
					}
				}
				
			}
		}
    }
    
    static void transformLOTREntityAIAttackOnCollide(ClassNode classNode) {
    	String name = obf ? "func_75253_b" : "continueExecuting";
    	
    	for(MethodNode method : classNode.methods) {
			if(method.name.equals(name) && method.desc.equals("()Z")) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals("sightNotRequired")) {
						method.instructions.set(node, new InsnNode(ICONST_0));
						// if(false) {...}
						log.info(coreprefix+": Patched LOTREntityAIAttackOnCollide.continueExecuting()");
						return;
					}
				}
			}
    	}
	}
    
    static void transformLOTREventHandler(ClassNode clazz) {
     	for(MethodNode method : clazz.methods) {
 	    	if(method.name.equals("onBlockBreak") && method.desc.equals("(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				// insert after
    				// ALOAD block
    				// INVOKESTATIC lotr/common/block/LOTRBlockRottenLog.isRottenWood(Lnet/minecraft/block/Block;)Z
    				// IFNE AD
    				if(node.getOpcode() == INVOKESTATIC && node.getNext().getOpcode() == IFNE) {
    					InsnList toInsert = new InsnList();
    					toInsert.add(new VarInsnNode(ALOAD, 2)); // player
    					toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "isTreeAngry", "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
    					toInsert.add(new JumpInsnNode(IFEQ, ((JumpInsnNode) node.getNext()).label));
    					method.instructions.insertBefore(node.getNext().getNext(), toInsert);
    					log.info(coreprefix+": Patched LOTREventHandler.onBlockBreak()");
    					return;
    				}
    			}
 	    			
 	    	}
 	    }
     	
     }
    
    
    static void transformItemFood(ClassNode classNode) {
    	String name = obf ? "d_" : "getMaxItemUseDuration"; // "func_77626_a"
    	String desc = obf ? "(Ladd;)I" : "(Lnet/minecraft/item/ItemStack;)I";
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals(desc)) {
    			method.instructions.clear();
    			method.localVariables.clear();
    			
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // itemfood (this)
    			//list.add(new VarInsnNode(ALOAD, 1)); // itemstack
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getMaxEatingDuration", "(Lnet/minecraft/item/ItemFood;)I", false));
    			list.add(new InsnNode(IRETURN));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched ItemFood.getMaxItemUseDuration()");
    			return;
    		}
    	}
	}
    
    static void transformItemHoe(ClassNode classNode) {
		classNode.superName = obf ? "acg" :"net/minecraft/item/ItemTool"; // instead of just Item
		log.info(coreprefix+": Changed Superclass for ItemHoe to ItemTool");
		
		patchItemModifiers(classNode, "ItemHoe");
		
		for(MethodNode method : classNode.methods) {
			if(method.name.equals("<init>") && method.desc.equals(obf ? "(Ladc;)V" : "(Lnet/minecraft/item/Item$ToolMaterial;)V")) {
				// remove original init call
				for(AbstractInsnNode node : method.instructions.toArray()) {
					method.instructions.remove(node);
					if(node.getOpcode() == INVOKESPECIAL) {
						break;
					}
				}
				
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // hoe
				list.add(new LdcInsnNode(0F)); // tool attack damage
				list.add(new VarInsnNode(ALOAD, 1)); // toolmaterial
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getProperHoeBlocks", "()Ljava/util/Set;", false)); // get proper Blocks
				list.add(new MethodInsnNode(INVOKESPECIAL, classNode.superName, "<init>", obf ? "(FLadc;Ljava/util/Set;)V" : "(FLnet/minecraft/item/Item$ToolMaterial;Ljava/util/Set;)V", false)); // super constructor
				list.add(new VarInsnNode(ALOAD, 0)); // hoe
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "setHoeToolClass", "(Lnet/minecraft/item/ItemHoe;)V", false));
				method.instructions.insert(list);
				log.info(coreprefix+": Patched ItemHoe.<init>()");
				return;
			}
		}
	}
    
    public static void patchItemModifiers(ClassNode classNode, String to) {
    	MethodNode methodnode = new MethodNode(ACC_PUBLIC, obf ? "k" : "getItemAttributeModifiers", "()Lcom/google/common/collect/Multimap;", null, null);
		InsnList list = new InsnList();
		//list.add(new VarInsnNode(ALOAD, 0)); // shears
		//list.add(new MethodInsnNode(INVOKESPECIAL, obf ? "adb" : "net/minecraft/item/Item", obf ? "k" : "getItemAttributeModifiers", "()Lcom/google/common/collect/Multimap;", false)); // super constructor
		list.add(new MethodInsnNode(INVOKESTATIC, "com/google/common/collect/HashMultimap", "create", "()Lcom/google/common/collect/HashMultimap;", false)); // super constructor
		HashMultimap.create();
		list.add(new InsnNode(ARETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method getItemAttributeModifiers() to "+to);
    }
    
    static void transformItemShears(ClassNode classNode) {
		classNode.superName = obf ? "acg" :"net/minecraft/item/ItemTool"; // instead of just Item
		log.info(coreprefix+": Changed Superclass for ItemShears to ItemTool");
		
		patchItemModifiers(classNode, "ItemShears");
		
		for(MethodNode method : classNode.methods) {
			if(method.name.equals("<init>") && method.desc.equals("()V")) {
				// remove original init call
				for(AbstractInsnNode node : method.instructions.toArray()) {
					method.instructions.remove(node);
					if(node.getOpcode() == INVOKESPECIAL) {
						break;
					}
				}
				
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // shears
				list.add(new LdcInsnNode(0F)); // tool attack damage
				list.add(new FieldInsnNode(GETSTATIC, "net/minecraft/item/Item$ToolMaterial", "IRON", "Lnet/minecraft/item/Item$ToolMaterial;"));
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getProperHoeBlocks", "()Ljava/util/Set;", false)); // get proper Blocks
				list.add(new MethodInsnNode(INVOKESPECIAL, classNode.superName, "<init>", obf ? "(FLadc;Ljava/util/Set;)V" : "(FLnet/minecraft/item/Item$ToolMaterial;Ljava/util/Set;)V", false)); // super constructor
				list.add(new VarInsnNode(ALOAD, 0)); // hoe
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "setShearsToolClass", "(Lnet/minecraft/item/ItemShears;)V", false));
				method.instructions.insert(list);
				log.info(coreprefix+": Patched ItemShears.<init>()");
				return;
			}
		}
	}
    
    static void transformEntityPlayer(ClassNode classNode) {
    	
    	
    	
    	String name = obf ? "d" : "isCurrentToolAdventureModeExempt";
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals("(III)Z")) {
    			// --- no speed increase!
    			// web ungoliath - shears / sword
    			// tnt 			- hoe
    			// piston - pickaxe
    			// kebab - hoe
    			// bed - axe
    			// termite home - axe
    			
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				int code = node.getOpcode();
    				if(code == IF_ACMPEQ) {
    					InsnList list2 = new InsnList();
    					LabelNode label = new LabelNode();
    					list2.add(new InsnNode(ICONST_1));
    					list2.add(new InsnNode(IRETURN));
    					
    					list2.add(label);
    					
    					method.instructions.insert(node, list2);
    					method.instructions.set(node, new JumpInsnNode(IF_ACMPNE, label));
    					
    				}
    				else if(code == IFNE) {
    					InsnList list = new InsnList();
    					
    					list.add(new VarInsnNode(ALOAD, 4)); // block
    					list.add(new InsnNode(ICONST_0)); // meta
    					list.add(new VarInsnNode(ALOAD, 5)); // itemstack
    					
    					list.add(new MethodInsnNode(INVOKESTATIC, "net/minecraftforge/common/ForgeHooks", "canToolHarvestBlock", "(Lnet/minecraft/block/Block;ILnet/minecraft/item/ItemStack;)Z", false));
    					list.add(new JumpInsnNode(IFNE, ((JumpInsnNode) node).label));
    					method.instructions.insert(node, list);
    					log.info(coreprefix+": Patched EntityPlayer.isCurrentToolAdventureModeExempt()");
    					return;
    					
    				}
    			}
    		}
    	}
    }
    
    static void transformLOTRItemMug(ClassNode classNode) {
    	
    	String name = obf ? "func_77654_b" : "onEaten"; // func_77654_b
    	for(MethodNode method : classNode.methods) {
    		boolean onEaten = method.name.equals(name);
    		
    		if((onEaten && method.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    		|| (method.name.equals("applyToNPC") && method.desc.equals("(Llotr/common/entity/npc/LOTREntityNPC;Lnet/minecraft/item/ItemStack;)V"))) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == INVOKESPECIAL && ((MethodInsnNode) node).name.equals("convertPotionEffectsForStrength")) {
    					InsnList list = new InsnList();
    					method.instructions.remove(node.getPrevious().getPrevious()); // remove ALOAD 0 for INVOKESPECIAL
    					list.add(new VarInsnNode(ALOAD, onEaten ? 3 : 1)); // player / npc
    					
    					list.add(new VarInsnNode(ALOAD, 0)); // this
    					list.add(new FieldInsnNode(GETFIELD, "lotr/common/item/LOTRItemMug", "potionEffects", "Ljava/util/List;"));
    					method.instructions.insertBefore(node, list);
    					method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getStackedEffects", "(FLnet/minecraft/entity/EntityLivingBase;Ljava/util/List;)Ljava/util/List;", false));
    					log.info(coreprefix+": Patched LOTRItemMug."+(onEaten ? "onEaten()" : "applyToNPC()"));
    					
    				}
    			}
    		}
    	}
    	
    	
    }
    
    static void transformLOTRItemEntDraught(ClassNode classNode) {
    	String name = obf ? "func_77654_b" : "onEaten";
    	String nameUse = obf ? "func_77648_a" : "onItemUse";
    	for(MethodNode method : classNode.methods) {
    		
    		if(draughtStackEffectsIncrease > 0)
    		if(method.name.equals(name) && method.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 4) { // effects
    					InsnList list = new InsnList();
    					list.add(new InsnNode(FCONST_1)); // 1F strength
    					list.add(new VarInsnNode(ALOAD, 3)); // player
    					list.add(new VarInsnNode(ALOAD, 4)); // effects
    					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getStackedEffects", "(FLnet/minecraft/entity/EntityLivingBase;Ljava/util/List;)Ljava/util/List;", false));
    					list.add(new VarInsnNode(ASTORE, 4)); // effects
    					method.instructions.insert(node, list);
    					log.info(coreprefix+": Patched LOTRItemEntDraught.onEaten()");
    					
    				}
    			}
    		}
    		
    		if(ASMConfig.patchDraughtUse)
    		if(method.name.equals(nameUse) && method.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z")) {
    			if(replaceConsumeItem(method, 1, 2, -1, 	1, 9)) {
    				log.info(coreprefix+": Patched LOTRItemEntDraught.onItemUse()");
    			}
    		}
    		
    	}
    }
    
    public static boolean replaceConsumeItem(MethodNode method, int varStack, int varPlayer, int varEmpty, int occurrence, int remove) {
    	String searchName = obf ? "func_70299_a" : "setInventorySlotContents";
    	AbstractInsnNode[] nodes = method.instructions.toArray();
    	int counter = 0;
		for(int i = 0; i < nodes.length; i++) {
			AbstractInsnNode node = nodes[i];
			if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(searchName)) {
				counter++;
				
				if(counter != occurrence) continue;
				
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, varStack)); // itemstack
				list.add(new VarInsnNode(ALOAD, varPlayer)); // player
				list.add(varEmpty != -1 ? new VarInsnNode(ALOAD, varEmpty) : new InsnNode(ACONST_NULL)); // empty (null rn)
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "consumeItem", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)V", false));
				
				method.instructions.insert(node, list);
				
				// remove this entry and the 9 above
				for(int r = i-remove; r <= i; r++) {
					method.instructions.remove(nodes[r]);
				}
				return true;
			
			}
		}
		return false;
    }
    
    static void modifyDraughBlock(ClassNode classNode, boolean mug) {
        
        String name = obf ? "func_149727_a" : "onBlockActivated"; // func_149727_a
        for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z")) {
    			if(replaceConsumeItem(method, 10, 5, 	mug ? 14 : -1, 		mug ? 3 : 1, 	mug ? 6 : 9)) {
    				log.info(coreprefix+": Patched "+classNode.name+".onBlockActivated()");
    			}
    		}
        }
    }
    
    static void transformFMLIndexedMessageToMessageCodec(ClassNode classNode) {
    	
        
        for(MethodNode method : classNode.methods) {
    		if(method.name.equals("decode") && method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;Lcpw/mods/fml/common/network/internal/FMLProxyPacket;Ljava/util/List;)V")) {
    			AbstractInsnNode[] nodes = method.instructions.toArray();
    			for(int i = 0; i < nodes.length; i++) {
    				AbstractInsnNode node = nodes[i];
    				if(node.getOpcode() == NEW && ((TypeInsnNode) node).desc.equals("java/lang/NullPointerException")) {
    					method.instructions.remove(node.getNext()); // DUP
    					method.instructions.remove(node); // NEW java/lang/NullPointerException
    					
    				}
    				if(node.getOpcode() == ATHROW) {
    					/*NEW java/lang/NullPointerException
    				    DUP
    				    NEW java/lang/StringBuilder
    				    DUP
    				    INVOKESPECIAL java/lang/StringBuilder.<init> ()V
    				    LDC "Undefined message for discriminator "
    				    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    				    ILOAD 5
    				    INVOKEVIRTUAL java/lang/StringBuilder.append (I)Ljava/lang/StringBuilder;
    				    LDC " in channel "
    				    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    				    ALOAD 2
    				    INVOKEVIRTUAL cpw/mods/fml/common/network/internal/FMLProxyPacket.channel ()Ljava/lang/String;
    				    INVOKEVIRTUAL java/lang/StringBuilder.append (Ljava/lang/String;)Ljava/lang/StringBuilder;
    				    INVOKEVIRTUAL java/lang/StringBuilder.toString ()Ljava/lang/String;
    				    INVOKESPECIAL java/lang/NullPointerException.<init> (Ljava/lang/String;)V
    				    ATHROW*/
    					
    					//method.instructions.insert(node, new InsnNode(POP));
    					
    					method.instructions.insert(node, new InsnNode(RETURN));
    					method.instructions.insert(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "warnMissingDiscriminator", "(Ljava/lang/String;Lcpw/mods/fml/common/network/internal/FMLProxyPacket;)V", false));
    					method.instructions.insert(node, new VarInsnNode(ALOAD, 2)); // packet
    					
    					method.instructions.remove(node.getPrevious()); // INVOKESPECIAL java/lang/NullPointerException.<init> (Ljava/lang/String;)V
    					method.instructions.remove(node); // ATHROW
    					log.info(coreprefix+": Patched FMLIndexedMessageToMessageCodec.decode()");
    					return;
    				}
    			}
    		}
        }
        
        
    }
    
    
}
