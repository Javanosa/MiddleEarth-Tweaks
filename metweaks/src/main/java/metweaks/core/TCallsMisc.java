package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.core.ClassHandler.*;
import org.objectweb.asm.tree.*;
import com.google.common.collect.HashMultimap;

import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.client.LOTRGuiElements;
import metweaks.farview.CeleritasCallsOld;
import metweaks.farview.FastCraftInfo;
import metweaks.farview.METChunkProviderClient;
import metweaks.features.LOTRDateCache;
import net.minecraft.launchwrapper.Launch;

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
     		if(ASMConfig.fangornTreePenaltyThreshold && method.name.equals("onBlockBreak") && method.desc.equals("(Lnet/minecraftforge/event/world/BlockEvent$BreakEvent;)V")) {
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
    					break;
    				}
    			}
 	    			
 	    	}
     		
     		if(ASMConfig.hiredsConquerForTheirFaction && method.name.equals("onLivingDeath") && method.desc.equals("(Lnet/minecraftforge/event/entity/living/LivingDeathEvent;)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				
    				// insert after alignmentBonus.killByHiredUnit = true;
    				if(node.getOpcode() == PUTFIELD && ((FieldInsnNode) node).name.equals("killByHiredUnit")) {
    					InsnList toInsert = new InsnList();
    					toInsert.add(new VarInsnNode(ALOAD, 4)); // source
    					toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "setTempHiredFaction", "(Lnet/minecraft/util/DamageSource;)V", false));
    					method.instructions.insert(node, toInsert);
    					log.info(coreprefix+": Patched LOTREventHandler.onLivingDeath()");
    					break;
    				}
    			}
 	    			
 	    	}
     		
     		if(ASMConfig.revengeCamouflageTargetChance > 0 && method.name.equals("onLivingSetAttackTarget") && method.desc.equals("(Lnet/minecraftforge/event/entity/living/LivingSetAttackTargetEvent;)V")) {
     			for(AbstractInsnNode node : method.instructions.toArray()) {
     				if(node.getOpcode() == ILOAD && node.getNext().getOpcode() == IFEQ && ((VarInsnNode) node).var == 2) { // sneaking
     					JumpInsnNode jumpNode = (JumpInsnNode) node.getNext();
     					InsnList toInsert = new InsnList();
    					toInsert.add(new VarInsnNode(ALOAD, 1)); // event
    					toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "shouldUnsetTarget", "(Lnet/minecraftforge/event/entity/living/LivingSetAttackTargetEvent;)Z", false));
    					toInsert.add(new JumpInsnNode(IFEQ, jumpNode.label));
    					method.instructions.insert(jumpNode, toInsert);
    					log.info(coreprefix+": Patched LOTREventHandler.onLivingSetAttackTarget()");
    					break;
     				}
     			}
     		}
     		
     		if(ASMConfig.farView && method.name.equals("onWorldUnload") && method.desc.equals("(Lnet/minecraftforge/event/world/WorldEvent$Unload;)V")) {
     			InsnList list = new InsnList();
				LabelNode label = new LabelNode();
				list.add(new VarInsnNode(ALOAD, 1)); // event
				list.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/event/world/WorldEvent$Unload", "world", "Lnet/minecraft/world/World;"));
				list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", obf ? "field_72995_K" : "isRemote", "Z"));
				list.add(new JumpInsnNode(IFEQ, label));
				
				list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(METChunkProviderClient.class), "farViewEnabled", "Z"));
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(METChunkProviderClient.class), "keepLastWorld", "Z"));
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new InsnNode(RETURN));
				list.add(label);
				method.instructions.insert(list);
				log.info(coreprefix+": Patched LOTREventHandler.onWorldUnload()");
				
     		}
     		
     		if(ASMConfig.bannerProtectionPatches) {
	     		if(method.name.equals("onBlockInteract") && method.desc.equals("(Lnet/minecraftforge/event/entity/player/PlayerInteractEvent;)V")) {
	     			replaceProtectionHandle(method, false);
	     		}
	     		
	     		if(method.name.equals("onEntityInteract") && method.desc.equals("(Lnet/minecraftforge/event/entity/player/EntityInteractEvent;)V")) {
	     			replaceProtectionHandle(method, true);
	     		}
     		}
     		
     		
 	    }
     	
     }
    
    private static void replaceProtectionHandle(MethodNode method, boolean entityAction) {
    	boolean searchReturn = false;
			LabelNode label = null;
			AbstractInsnNode insertAt = null;
			for(AbstractInsnNode node : method.instructions.toArray()) {
				// find isProtected
				// traverse until we find RETURN
				// save index and now traverse backwards until we find IFNE
				if(!searchReturn && node.getOpcode() == INVOKESTATIC && ((MethodInsnNode) node).name.equals("isProtected")) {
					searchReturn = true;
					label = ((JumpInsnNode) node.getNext()).label;
					insertAt = node.getPrevious().getPrevious(); // insert before ICONST_1 (true)
					method.instructions.remove(node.getPrevious()); // remove ICONST_1 (true)
				}
				
				if(searchReturn && node.getOpcode() == RETURN) {
					// stop before RETURN
					searchReturn = false;
					// we found it
					InsnList list = new InsnList();
					list.add(new VarInsnNode(ALOAD, 1)); // event
					
					if(entityAction) {
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "handleRIGHT_CLICK_ENTITY", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Llotr/common/LOTRBannerProtection$IFilter;Lnet/minecraftforge/event/entity/player/EntityInteractEvent;)Z", false));
					}
					else {
						// World world, int i, int j, int k, IFilter filter, Block block, ItemStack itemstack, PlayerInteractEvent event
						list.add(new VarInsnNode(ALOAD, 9)); // block
						list.add(new VarInsnNode(ALOAD, 4)); // itemstack
						list.add(new VarInsnNode(ALOAD, 11)); // perm
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "handleRIGHT_CLICK_BLOCK", "(Lnet/minecraft/world/World;IIILlotr/common/LOTRBannerProtection$IFilter;Lnet/minecraftforge/event/entity/player/PlayerInteractEvent;Lnet/minecraft/block/Block;Lnet/minecraft/item/ItemStack;Llotr/common/LOTRBannerProtection$Permission;)Z", false));
					}
					list.add(new JumpInsnNode(IFEQ, label));
					method.instructions.insert(insertAt, list);
					log.info(coreprefix+": Patched LOTREventHandler."+(entityAction ? "onEntityInteract()" : "onBlockInteract()"));
					break;
				}
				
				if(searchReturn) {
					// remove nodes, including isProtected()
					method.instructions.remove(node);
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
    		
    		if(ASMConfig.draughtStackEffectsIncrease > 0)
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
    
    
    
    static void transformLOTRCustomWaypoint(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
			if(method.name.equals("setSharingPlayerID") && method.desc.equals("(Ljava/util/UUID;)V")) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					// after if(MinecraftServer.getServer() != null)
					if(node.getOpcode() == IFNULL && node.getPrevious().getOpcode() == INVOKESTATIC) {
						JumpInsnNode jump = (JumpInsnNode) node;
						InsnList list = new InsnList();
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "isServerRunning", "()Z", false));
						list.add(new JumpInsnNode(IFEQ, jump.label));
						method.instructions.insert(node, list);
						log.info(coreprefix+": Patched LOTRCustomWaypoint.setSharingPlayerID()");
						return;
						
					}
				}
				
			}
		}
    }
    
    static void transformLOTRPacketBiomeVariantsUnwatch$Handler(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
			if(method.name.equals("onMessage") && method.desc.equals("(Llotr/common/network/LOTRPacketBiomeVariantsUnwatch;Lcpw/mods/fml/common/network/simpleimpl/MessageContext;)Lcpw/mods/fml/common/network/simpleimpl/IMessage;")) {
				InsnList list = new InsnList();
				LabelNode label = new LabelNode();
				list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(METChunkProviderClient.class), "farViewEnabled", "Z"));
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(METChunkProviderClient.class), "cacheVariants", "Z"));
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new InsnNode(ACONST_NULL));
				list.add(new InsnNode(ARETURN));
				list.add(label);
				method.instructions.insert(list);
				log.info(coreprefix+": Patched LOTRPacketBiomeVariantsUnwatch$Handler.onMessage()");
				return;
			}
    	}
    	
    	
    }
    
    static void transformFastCraftHelper(ClassNode classNode) {
    	String version = (String) Launch.blackboard.get("fcVersion");
    	
    	// testing
    	if(ASMConfig.forceFCV > 0) {
    		version = ASMConfig.forceFCV == 125 ? "1.25" : "1.23";
    	}
    	
    	if(version == null) return;
    	
    	boolean version125 = false;
    	switch(version) {
    		case "1.23":
    			break;
    		case "1.25":
    			version125 = true;
    			break;
    		default:
    			log.warn(coreprefix+": No known Fastcraft Version found");
    			return;
    	}
    	
    	FastCraftInfo.hasFastcraft = true;
    	FastCraftInfo.version125 = version125;
    	log.info(coreprefix+": Detected Fastcraft "+version);
    	
    	// make some fastcraft fields public or find a way to get it
    	// supply getters with fastcraft fields depending on version
    	// if fastcraft is loaded, a different map
    	
    	String helper = "metweaks/farview/FastCraftHelper";
    	String internalDesc = version125 ? "Lfastcraft/aV;" : "Lfastcraft/aU;";
    	String internalOwner = version125 ? "fastcraft/aV" : "fastcraft/aU";
    	String internName = "intern";
    	
    	classNode.visitField(ACC_PRIVATE & ACC_FINAL, internName, internalDesc, null, null);
    	log.info(coreprefix+": Added Field FastCraftHelper."+internName);
    	
    	for(MethodNode method : classNode.methods) {
    		String methodName = method.name;
    		String methodDesc = method.desc;
    		
			if(methodName.equals("<init>") && methodDesc.equals("(Lnet/minecraft/util/LongHashMap;)V")) {
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new VarInsnNode(ALOAD, 1)); // map
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(FastCraftInfo.class), "getInternData", "(Lnet/minecraft/util/LongHashMap;)Ljava/lang/Object;", false));
				list.add(new TypeInsnNode(CHECKCAST, internalOwner));
				list.add(new FieldInsnNode(PUTFIELD, helper, internName, internalDesc));
				method.instructions.insert(list);
				log.info(coreprefix+": Supplied FastCraftHelper.<init>()");
			}
			
			else if(methodName.equals("getMapInstance") && methodDesc.equals("()Lnet/minecraft/util/LongHashMap;")) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == ACONST_NULL) {
						InsnList list = new InsnList();
						list.add(new TypeInsnNode(NEW, "fastcraft/X"));
						list.add(new InsnNode(DUP));
						list.add(new MethodInsnNode(INVOKESPECIAL, "fastcraft/X", "<init>", "()V", false));
						
						method.instructions.insert(node, list);
						method.instructions.remove(node);
						log.info(coreprefix+": Supplied FastCraftHelper.getMapInstance()");
						break;
					}
				}
				
			}
			
			/*else if(methodName.equals("getCapacity") && methodDesc.equals("()I")) {
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, helper, internName, internalDesc));
				list.add(new FieldInsnNode(GETFIELD, internalOwner, version125 ? "a" : "b", "I"));
				list.add(new InsnNode(IRETURN));
				method.instructions.clear();
				method.instructions.insert(list);
				log.info(coreprefix+": Supplied FastCraftHelper.getCapacity()");
			}*/
			
			else if(methodName.equals("getNoEntryValue") && methodDesc.equals("()J")) {
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, helper, internName, internalDesc));
				list.add(new FieldInsnNode(GETFIELD, internalOwner, "a", "J"));
				list.add(new InsnNode(LRETURN));
				method.instructions.clear();
				method.instructions.insert(list);
				log.info(coreprefix+": Supplied FastCraftHelper.getNoEntryValue()");
			}
			
			else if(methodName.equals("getValues") && methodDesc.equals("()[Ljava/lang/Object;")) {
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, helper, internName, internalDesc));
				list.add(new FieldInsnNode(GETFIELD, internalOwner, "a", "[Ljava/lang/Object;"));
				list.add(new InsnNode(ARETURN));
				method.instructions.clear();
				method.instructions.insert(list);
				log.info(coreprefix+": Supplied FastCraftHelper.getValues()");
			}
			
			else if(methodName.equals("getKeys") && methodDesc.equals("()[J")) {
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // this
				list.add(new FieldInsnNode(GETFIELD, helper, internName, internalDesc));
				list.add(new FieldInsnNode(GETFIELD, internalOwner, "a", "[J"));
				list.add(new InsnNode(ARETURN));
				method.instructions.clear();
				method.instructions.insert(list);
				log.info(coreprefix+": Supplied FastCraftHelper.getKeys()");
			}
    	}
    }
    
    static void transformBannerProtectable(ClassNode classNode, boolean srgName) {
    	String name = obf ? srgName ? "func_70097_a" : "a" : "attackEntityFrom";
    	String desc = obf && !srgName ? "(Lro;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z";
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals(desc)) {
    			
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // entity (this)
    			list.add(new VarInsnNode(ALOAD, 1)); // damage source
    			list.add(new VarInsnNode(FLOAD, 2)); // damage amount
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "onAttackEntityFrom", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;F)Z", false));
    			LabelNode label = new LabelNode();
    			list.add(new JumpInsnNode(IFNE, label));
    			list.add(new InsnNode(ICONST_0));
    			list.add(new InsnNode(IRETURN));
    			
    			
    			list.add(label);
    			method.instructions.insert(list);
				log.info(coreprefix+": Patched "+classNode.name+".attackEntityFrom()");
				return;
    		}
    	}
    }
    
    static void transformLOTRBannerProtection$FilterForPlayer(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("warnProtection") && method.desc.equals("(Lnet/minecraft/util/IChatComponent;)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(obf ? "func_71120_a" : "sendContainerToPlayer")) {
    					LabelNode label = new LabelNode();
    					InsnList list = new InsnList();
    	    			list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(HooksLOTR.class), "sendSlotsOnPermDenied", "Z"));
    	    			list.add(new JumpInsnNode(IFEQ, label));
    	    			method.instructions.insertBefore(node.getPrevious().getPrevious().getPrevious().getPrevious(), list);
    	    			// insert label after branch
    	    			method.instructions.insert(node, label);
    	    			log.info(coreprefix+": Patched LOTRBannerProtection$FilterForPlayer.warnProtection()");
    	    			
    	    			
    	    			return;
    				}
    			}
    		}
    	}
    }
    
    static void transformBlockFarmland(ClassNode classNode) {
    	String name = obf ? "a" : "onFallenUpon";
    	String desc = obf ? "(Lahb;IIILsa;F)V" : "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;F)V";
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals(desc)) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == IFGE) {
    					LabelNode label = ((JumpInsnNode) node).label;
    					InsnList list = new InsnList();
    					list.add(new VarInsnNode(ALOAD, 5)); // entity
    	    			list.add(new VarInsnNode(ILOAD, 2)); // x
    	    			list.add(new VarInsnNode(ILOAD, 3)); // y
    	    			list.add(new VarInsnNode(ILOAD, 4)); // z
    	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "isFarmLandUnprotected", "(Lnet/minecraft/entity/Entity;III)Z", false));
    	    			list.add(new JumpInsnNode(IFEQ, label)); // z
    					
    					method.instructions.insert(node, list);
    					log.info(coreprefix+": Patched BlockFarmland.onFallenUpon()");
    					return;
    				}
    			}
    		}
    	}
    }
    
    static void transformEntityLargeFireball(ClassNode classNode) {
    	String name = obf ? "a" : "onImpact";
    	String desc = obf ? "(Lazu;)V" : "(Lnet/minecraft/util/MovingObjectPosition;)V";
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(name) && method.desc.equals(desc)) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == ACONST_NULL) {
    					method.instructions.set(node, new VarInsnNode(ALOAD, 0));
    					
    					log.info(coreprefix+": Patched EntityLargeFireball.onImpact()");
    					return;
    				}
    			}
    		}
    	}
    }
    
    static void transformLOTRMod(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(ASMConfig.lotrHolidays > 0 && method.desc.equals("()Z")) {
    			String name = method.name;
    			switch(name) {
    				case "isNewYearsDay":
    				case "isAprilFools":
    				case "isHalloween":
    				case "isChristmas":
    					method.instructions.clear();
    					method.localVariables.clear();
    					method.visitCode();
    					method.visitFieldInsn(GETSTATIC, Type.getInternalName(LOTRDateCache.class), name, "Z");
    					method.visitInsn(IRETURN);
    					method.visitEnd();
    					log.info(coreprefix+": Patched LOTRMod."+name+"()");
    					continue;
    			}
    		}
    		
    		if(ASMConfig.fellowshipFFPatches) {
	    		if(method.name.equals("canPlayerAttackEntity") && method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/EntityLivingBase;Z)Z")) {
	    			for(AbstractInsnNode node : method.instructions.toArray()) {
	    				// after line boolean friendlyFireEnabled
	    				if(node.getOpcode() == ISTORE && ((VarInsnNode) node).var == 5) { // friendlyFireEnabled
	    					InsnList list = new InsnList();
	    					list.add(new VarInsnNode(ALOAD, 1)); // target
	    					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "getRidingPlayerOptional", "(Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/entity/EntityLivingBase;", false));
	    					list.add(new VarInsnNode(ASTORE, 1)); // target
	    					method.instructions.insert(node, list);
	    					log.info(coreprefix+": Patched LOTRMod.canPlayerAttackEntity()");
	    					break;
	    				}
	    			}
	    		}
	    		
	    		if(method.name.equals("canNPCAttackEntity") && method.desc.equals("(Lnet/minecraft/entity/EntityCreature;Lnet/minecraft/entity/EntityLivingBase;Z)Z")) {
	    			boolean patchA = false;
	    			boolean patchB = false;
	    			boolean patchC = false;
	    			boolean patchD = false;
	    			
	    			LabelNode IF_ACMPNE_label = null;
	    			JumpInsnNode conditionToClose = null;
	    			AbstractInsnNode pos = null;
	    			
	    			
	    			
	    			for(AbstractInsnNode node : method.instructions.toArray()) {
	    				// label that directs right to after the hiringPlayer target checks
	    				if(!patchC && node.getOpcode() == IF_ACMPNE) {
	    					IF_ACMPNE_label = ((JumpInsnNode) node).label;
	    					patchC = true;
	    				}
	    				
	    				// hiringPlayer != null
	    				if(!patchA && node.getOpcode() == ALOAD && ((VarInsnNode) node).var == 5 && node.getNext().getOpcode() == IFNULL) {
	    					conditionToClose = (JumpInsnNode) node.getNext();
							patchA = true;
	    				}
	    				
	    				// after init line of LOTREntityNPC npc
	    				if(!patchD && node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 4) {
	    					pos = node;
							patchD = true;
	    				}
	    				
	    				// instead of targetNPC.hiredNPCInfo.isActive
	    				if(!patchB && node.getOpcode() == GETFIELD) {
	    					FieldInsnNode fnode = (FieldInsnNode) node;
	    					if(fnode.name.equals("isActive")) {
	    						InsnList list = new InsnList();
	        					//list.add(new VarInsnNode(ALOAD, 6)); // npc
	        					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "isHiredNPC", "(Llotr/common/entity/npc/LOTRHiredNPCInfo;)Z", false));
	        					method.instructions.insert(node, list);
	        					method.instructions.remove(node);
	    						log.info(coreprefix+": Patched LOTRMod.canNPCAttackEntity() #2");
	    						patchB = true;
	    					}
	    				}
	    				// instead of hiringPlayerUUID.equals(targetHiringPlayerUUID)
	    				if(node.getOpcode() == INVOKEVIRTUAL) {
	    					MethodInsnNode mnode = (MethodInsnNode) node;
	    					if(mnode.name.equals("equals") && mnode.owner.equals("java/util/UUID")) {
	    						InsnList list = new InsnList();
	        					list.add(new VarInsnNode(ALOAD, 0)); // attacker
	        					list.add(new VarInsnNode(ALOAD, 1)); // target
	        					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "preventIfNotBadRelation", "(Ljava/util/UUID;Ljava/util/UUID;Lnet/minecraft/entity/EntityCreature;Lnet/minecraft/entity/EntityLivingBase;)Z", false));
	        					method.instructions.insert(node, list);
	        					method.instructions.remove(node);
	        					log.info(coreprefix+": Patched LOTRMod.canNPCAttackEntity() #3");
	        					break;
	    					}
	    				}
	    			}
	    			
	    			if(pos != null && conditionToClose != null && IF_ACMPNE_label != null) {
	    				
	    				// add condition around the hiringPlayer != null block that only checks if the unit is hired without requiring the player to be online
	    				InsnList list = new InsnList();
						list.add(new VarInsnNode(ALOAD, 4)); // npc
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "isHired", "(Llotr/common/entity/npc/LOTREntityNPC;)Z", false));
						list.add(new JumpInsnNode(IFEQ, conditionToClose.label)); // npc
						method.instructions.insert(pos, list);
						
						
						// close if (hiringPlayer != null) { } earlier after checking target values
	    				conditionToClose.label = IF_ACMPNE_label;
	    				
	    				log.info(coreprefix+": Patched LOTRMod.canNPCAttackEntity() #1");
						
	    			}
	    			
	    			
	    		}
    		
    		}
    		
    		
    	}
    	
    }
    
    static void transformBannerBearer(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("<init>") && method.desc.equals("(Lnet/minecraft/world/World;)V")) {
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new InsnNode(ICONST_1)); // true
    			list.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/npc/LOTREntityNPC", "canBannerBearerSpawnRiding", "Z"));
    			method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
    			log.info(coreprefix+": Patched "+classNode.name+".<init>()");
    			return;
    		}
    	}
    	
    	
    }
    
    static void transformEntityFireworkRocket(ClassNode classNode) {
    	
    	classNode.fields.add(new FieldNode(ACC_PRIVATE, "velocityLifetime", "I", null, null));
    	log.info(coreprefix+": Added Field velocityLifetime to EntityFireworkRocket");
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "i" : "setVelocity") && method.desc.equals("(DDD)V")) {
    			
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new InsnNode(DUP)); // this
    			list.add(new FieldInsnNode(GETFIELD, obf ? "zf": "net/minecraft/entity/item/EntityFireworkRocket", obf ? "aa" : "ticksExisted", "I"));
    			list.add(new IntInsnNode(BIPUSH, 20));
    			list.add(new InsnNode(IADD));
    			list.add(new FieldInsnNode(PUTFIELD, obf ? "zf": "net/minecraft/entity/item/EntityFireworkRocket", "velocityLifetime", "I"));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched EntityFireworkRocket.setVelocity()");
    		}
    		
    		if(method.name.equals(obf ? "h" : "onUpdate") && method.desc.equals("()V")) {
    			
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == LDC && ((LdcInsnNode) node).cst.equals(0.04D)) {
    					InsnList list = new InsnList();
    	    			list.add(new VarInsnNode(ALOAD, 0)); // this
    	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "clampSpeed", "(Lnet/minecraft/entity/Entity;)V", false));
    	    			method.instructions.insert(node.getNext().getNext(), list);
    	    			log.info(coreprefix+": Patched EntityFireworkRocket.onUpdate() #1");
    	    			break;
    				}
    			}
    			
    			
    		    LabelNode label = new LabelNode();
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // this
    			list.add(new InsnNode(DUP)); // this
    			list.add(new FieldInsnNode(GETFIELD, obf ? "zf": "net/minecraft/entity/item/EntityFireworkRocket", "velocityLifetime", "I"));
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "stopUpdating", "(Lnet/minecraft/entity/Entity;I)Z", false));
    			list.add(new JumpInsnNode(IFEQ, label));
    			list.add(new InsnNode(RETURN));
    			list.add(label);
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched EntityFireworkRocket.onUpdate() #2");
    			
    			
    		}
    	}
    	
    	
    }
    
    static void transformLOTRRenderEnt(ClassNode classNode) {
    	// doRender(EntityLiving entity, double d, double d1, double d2, float f, float f1)
    	MethodNode methodnode = new MethodNode(ACC_PUBLIC, obf ? "func_76986_a" : "doRender", "(Lnet/minecraft/entity/EntityLiving;DDDFF)V", null, null);
		InsnList list = new InsnList();
		
		list.add(new VarInsnNode(ALOAD, 0)); // this
		list.add(new VarInsnNode(ALOAD, 1)); // entity
		list.add(new VarInsnNode(DLOAD, 2));
		list.add(new VarInsnNode(DLOAD, 4));
		list.add(new VarInsnNode(DLOAD, 6));
		list.add(new VarInsnNode(FLOAD, 8));
		list.add(new VarInsnNode(FLOAD, 9));
		
		list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/client/renderer/entity/RenderLiving", obf ? "func_76986_a" : "doRender", "(Lnet/minecraft/entity/EntityLiving;DDDFF)V", false));
		
		
		list.add(new VarInsnNode(ALOAD, 1)); // entity
		list.add(new VarInsnNode(DLOAD, 2));
		list.add(new VarInsnNode(DLOAD, 4));
		list.add(new VarInsnNode(DLOAD, 6));
		list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksClient.class), "doRenderEnt", "(Lnet/minecraft/entity/EntityLiving;DDD)V", false));
		
		list.add(new InsnNode(RETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method doRender() to LOTRRenderEnt");
		
		
    }
    
    static void transformLOTREntityEnt(ClassNode classNode) {
    	
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "func_70599_aP" : "getSoundVolume") && method.desc.equals("()F")) {
    			method.instructions.clear();
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0)); // npc
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "getEntSoundVolume", "(Llotr/common/entity/npc/LOTREntityNPC;)F", false));
    			list.add(new InsnNode(FRETURN));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Replaced LOTREntityEnt.getSoundVolume()");
    		}
    		
    		if(method.name.equals("func_145780_a") && method.desc.equals("(IIILnet/minecraft/block/Block;)V")) {
    		
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == LDC && ((LdcInsnNode) node).cst.equals(0.75F)) {
    					method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0)); // npc
    	    			method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "getEntSoundVolumeStep", "(Llotr/common/entity/npc/LOTREntityNPC;)F", false));
    	    			log.info(coreprefix+": Patched LOTREntityEnt.func_145780_a()");
    	    			break;
    				}
    			}
    		}
    		
    		if(method.name.equals(obf ? "func_70652_k" : "attackEntityAsMob") && method.desc.equals("(Lnet/minecraft/entity/Entity;)Z")) {
    			LabelNode searchLabel = null;
    			AbstractInsnNode pos = null;
    			
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(searchLabel != null) {
    					if(node != searchLabel) {
    						method.instructions.remove(node);
    						continue;
    					}
    					else {
    						InsnList list = new InsnList();
    		    			list.add(new VarInsnNode(ALOAD, 0)); // npc
    		    			list.add(new VarInsnNode(ALOAD, 1)); // target
    		    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "attackEntityAsEnt", "(Llotr/common/entity/npc/LOTREntityNPC;Lnet/minecraft/entity/Entity;)Z", false));
    		    			list.add(new InsnNode(IRETURN));
    		    			method.instructions.insert(pos, list);
    						log.info(coreprefix+": Patched LOTREntityEnt.attackEntityAsMob()");
    						break;
    					}
    				}
    				
    				if(node.getOpcode() == IFEQ) {
    					searchLabel = ((JumpInsnNode) node).label;
    					pos = node;
    				}
    				
    				
    			}
    		}
    	}
    	
    	
    }
    
    static void transformLOTRNetHandlerPlayServer(ClassNode classNode) {
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "func_147340_a" : "processUseEntity") && method.desc.equals("(Lnet/minecraft/network/play/client/C02PacketUseEntity;)V")) {
    			
    			
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == IFGE) {
    					LabelNode label = ((JumpInsnNode) node).label;
    					
    					// remove 
    					while(node != null && node.getOpcode() != ALOAD) {
    						AbstractInsnNode prev = node.getPrevious();
    						method.instructions.remove(node);
    						node = prev;
    					}
    					
    					
    					InsnList list = new InsnList();
    					
    					list.add(new VarInsnNode(DLOAD, 5)); // reach
    					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksLOTR.class), "inInteractionRange", "(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/entity/Entity;D)Z", false));
    					list.add(new JumpInsnNode(IFEQ, label));
    					method.instructions.insert(node, list);
    					
    					
    					log.info(coreprefix+": Patched LOTRNetHandlerPlayServer.processUseEntity()");
    					break;
    				}
    			}
    		}
    	}
    	
    	
    }
    
    static void transformLOTRWaypoint(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("isCompatibleAlignment") && method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)Z")) {
    			
    			InsnList list = new InsnList();
				
				list.add(new InsnNode(ICONST_1)); // true
				list.add(new InsnNode(IRETURN));
				
				method.instructions.insert(list);
				log.info(coreprefix+": Patched LOTRWaypoint.isCompatibleAlignment()");
				return;
    		}
    	}
    }
    
    static void transformChunkTracker(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("onChunkStatusRemoved") && method.desc.equals("(III)V")) {
    			
    			InsnList list = new InsnList();
				LabelNode label = new LabelNode();
				list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(CeleritasCallsOld.class), "preventUnload", "Z"));
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new InsnNode(RETURN));
				list.add(label);
				list.add(new FrameNode(F_SAME, 4, null, 0, null)); // current 4 locals, this, x, z, flag
				method.instructions.insert(list);
				log.info(coreprefix+": Patched ChunkTracker.onChunkStatusRemoved()");
				//return;
    		}
    	}
    	
    	
    }
    
    static void transformClientTicker(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("onClientTick") && method.desc.equals("(Lcpw/mods/fml/common/gameevent/TickEvent$ClientTickEvent;)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == IFNULL && node.getPrevious().getOpcode() == GETFIELD && "field_71441_e".equals(((FieldInsnNode) node.getPrevious()).name)) {
    					method.instructions.insertBefore(node, new InsnNode(POP));
    					method.instructions.insertBefore(node, new InsnNode(ICONST_0));
    					method.instructions.set(node, new JumpInsnNode(IFEQ, ((JumpInsnNode) node).label));
    					log.info(coreprefix+": Patched Hodgepodge ClientTicker.onClientTick()");
    					break;
    				}
    			}
    		}
    	}
    	
    	
    }
    
    static void transformCeleritasCalls(ClassNode classNode) {
    	if(MeTweaks.celeritasVers != 3) return;
    	
    	for(MethodNode method : classNode.methods) {
    		if(/*(method.name.equals("loadChunk") || method.name.equals("unloadChunk")) && */method.desc.equals("(Lnet/minecraft/world/World;II)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == INVOKEVIRTUAL) {
    					MethodInsnNode mnode = (MethodInsnNode) node;
    					method.instructions.set(node, new MethodInsnNode(INVOKEINTERFACE, mnode.owner, mnode.name, mnode.desc, true));
    					log.info(coreprefix+": Patched CeleritasCalls."+method.name+"()");
    					break;
    				}
    			}
    		}
    	}
    }
    
    static void transformLOTRPlayerDetailsCache(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("getPlayerDetails") && method.desc.equals("(Ljava/util/UUID;)Llotr/common/playerdetails/PlayerDetails;")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == INVOKESPECIAL && ((MethodInsnNode) node).name.equals("fireRequest")) {
    					method.instructions.remove(node.getPrevious());
    					method.instructions.remove(node.getPrevious());
    					method.instructions.remove(node);
    					log.info(coreprefix+": Patched LOTRPlayerDetailsCache.getPlayerDetails() #1");
    				}
    				
    				if(node.getOpcode() == ARETURN && node.getPrevious().getOpcode() == INVOKESTATIC) {
    					InsnList list = new InsnList();
    					int ldummy = method.maxLocals;
    					method.maxLocals++;
    					list.add(new VarInsnNode(ASTORE, ldummy)); // playerdetails dummy
    					list.add(new VarInsnNode(ALOAD, 0));
    					list.add(new FieldInsnNode(GETFIELD, "lotr/common/playerdetails/LOTRPlayerDetailsCache", "detailsForPlayer", "Ljava/util/Map;"));
    					list.add(new VarInsnNode(ALOAD, 1)); // uuid
    					list.add(new VarInsnNode(ALOAD, ldummy));
    					list.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true));
    					list.add(new InsnNode(POP));
    					list.add(new VarInsnNode(ALOAD, ldummy));
    					method.instructions.insertBefore(node, list);
    					log.info(coreprefix+": Patched LOTRPlayerDetailsCache.getPlayerDetails() #2");
    					break;
    					
    				}
    			}
    		}
    	}
    	
    	
    }
    
    static void transformArmorProperties(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("ApplyArmor")) {
    			
    			boolean notcauldron = method.desc.equals(obf ? "(Lsv;[Ladd;Lro;D)F" : "(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;D)F");
	    		if(notcauldron || method.desc.equals(obf ? "(Lsv;[Ladd;Lro;DZ)F" : "(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;DZ)F")) {
	    			for(AbstractInsnNode node : method.instructions.toArray()) {
	    				if(node.getOpcode() == INVOKEVIRTUAL) {
	    					MethodInsnNode mnode = (MethodInsnNode) node;
	    					if(mnode.name.equals(obf ? "o" : "getMaxDamage") && mnode.desc.equals("()I") && mnode.owner.equals(obf ? "abb" : "net/minecraft/item/ItemArmor")) {
	    					
		    					AbstractInsnNode lnode = node.getPrevious();
		    					node = lnode.getPrevious();
		    					
		    					while(node != null) {
		    						AbstractInsnNode nextNode = lnode.getNext();
		    						method.instructions.remove(lnode);
		    						if(lnode.getOpcode() == ISUB) {
		    							break;
		    						}
		    						lnode = nextNode;
		    						
		    					}
		    					
		    					InsnList list = new InsnList();
		    					list.add(new VarInsnNode(ALOAD, notcauldron ? 7 : 8));
		    					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getProcessedArmorDurability", "(Lnet/minecraft/item/ItemStack;)I", false));
		    					method.instructions.insert(node, list);
		    					
		    					log.info(coreprefix+": Patched ArmorProperties.ApplyArmor()");
		    					break;
	    					}
	    				}
	    			}
	    			/*ALOAD 9
	    		    INVOKEVIRTUAL net/minecraft/item/ItemArmor.getMaxDamage ()I
	    		    ICONST_1
	    		    IADD
	    		    ALOAD 7
	    		    INVOKEVIRTUAL net/minecraft/item/ItemStack.getItemDamage ()I
	    		    ISUB*/
	    		}
    		}
    	}
    	
    	
    }
    
   
    
    
}
