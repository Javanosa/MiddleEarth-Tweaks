package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.ASMConfig.entityPosRotationThreshold;
import static metweaks.ASMConfig.entityTrackerPatches;
import static metweaks.ASMConfig.farView;
import static metweaks.ASMConfig.projectilePatches;
import static metweaks.ASMConfig.projectilePatchesReqClient;
import static metweaks.core.ClassHandler.*;

import org.objectweb.asm.tree.*;
import metweaks.ASMConfig;
import metweaks.farview.METChunkProviderClient;

public class TCallsProjectile {
	
	static void transformLOTREntityProjectileBase(ClassNode classNode) {
		
        
        String nameRead = obf ? "func_70037_a" : "readEntityFromNBT";
        String nameWrite = obf ? "func_70014_b" : "writeEntityToNBT";
        
        String nameUpdate = obf ? "func_70071_h_" : "onUpdate";
        
        for(MethodNode method : classNode.methods) {
        	boolean read = method.name.equals(nameRead);
			if((read || method.name.equals(nameWrite)) && method.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				InsnList list = new InsnList();
				if(read) {
					list.add(new VarInsnNode(ALOAD, 0)); // this
					list.add(new VarInsnNode(ALOAD, 1)); // nbt
					list.add(new LdcInsnNode("ticksInGround"));
					list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", obf ? "func_74762_e" : "getInteger", "(Ljava/lang/String;)I", false));
					list.add(new FieldInsnNode(PUTFIELD, "lotr/common/entity/projectile/LOTREntityProjectileBase", "ticksInGround", "I"));
				}
				else {
					list.add(new VarInsnNode(ALOAD, 1)); // nbt
					list.add(new LdcInsnNode("ticksInGround"));
					list.add(new VarInsnNode(ALOAD, 0)); // this
					list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/projectile/LOTREntityProjectileBase", "ticksInGround", "I"));
					list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/NBTTagCompound", obf ? "func_74768_a" : "setInteger", "(Ljava/lang/String;I)V", false));
				}
				
				method.instructions.insert(list);
				log.info(coreprefix+": Patched LOTREntityProjectileBase."+(read ? "readEntityFromNBT()" : "writeEntityToNBT()"));
			}
			else if(method.name.equals(nameUpdate) || method.desc.equals("()V")) {
				LabelNode label = new LabelNode();
				LabelNode skipServerOnly = new LabelNode();
				int localOldBlockHit = method.maxLocals;
				
				method.maxLocals++;
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(obf ? "func_70097_a" : "attackEntityFrom")) {
						method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "attackEntityFromReplace", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;F)Z", false));
						InsnList list = new InsnList();
						list.add(new VarInsnNode(ALOAD, 0)); // this
						list.add(new FieldInsnNode(GETFIELD, "lotr/common/entity/projectile/LOTREntityProjectileBase", obf ? "field_70170_p" : "worldObj", "Lnet/minecraft/world/World;"));
						list.add(new FieldInsnNode(GETFIELD, "net/minecraft/world/World", obf ? "field_72995_K" : "isRemote", "Z"));
						list.add(new JumpInsnNode(IFNE, skipServerOnly)); // if not remote
						log.info(coreprefix+": Patched LOTREntityProjectileBase.onUpdate() #1");
					}
					
					if(!ASMConfig.alwaysAbsorbPiercingProjectiles && node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 6) { // entity list
						
						InsnList list = new InsnList();
						list.add(new VarInsnNode(ALOAD, 4)); // movingObjectPos old from blockhit
						list.add(new VarInsnNode(ASTORE, localOldBlockHit)); // save temp movingObjectPos
						list.add(label);
						method.instructions.insert(node, list);
						log.info(coreprefix+": Patched LOTREntityProjectileBase.onUpdate() #3");
						
					}
					
					else if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals("onCollideWithTarget")) {
						LabelNode conditionLabel = new LabelNode();
						
						
						InsnList list = new InsnList();
						list.add(skipServerOnly);
						list.add(new VarInsnNode(ALOAD, 0)); // this
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "piercingProjectile", "(Llotr/common/entity/projectile/LOTREntityProjectileBase;)Z", false));
						//list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/Entity", obf ? "field_70128_L" : "isDead", "Z"));
						list.add(new JumpInsnNode(IFEQ, conditionLabel)); // if not dead
						list.add(new VarInsnNode(ALOAD, 0)); // this
				    	//list.add(new VarInsnNode(ALOAD, 0)); // this
				    	list.add(new VarInsnNode(ALOAD, 9)); // target
						list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "addToBlackList", "(Llotr/common/entity/projectile/LOTREntityProjectileBase;Lnet/minecraft/entity/Entity;)V", false));
						if(!ASMConfig.alwaysAbsorbPiercingProjectiles) {
							//LabelNode labelskip = new LabelNode();
							//list.add(new JumpInsnNode(IFEQ, labelskip)); // if not absorbPiercingProjectiles
							list.add(new InsnNode(ACONST_NULL));
							list.add(new VarInsnNode(ASTORE, 5)); // reset entity to null
							list.add(new VarInsnNode(ALOAD, localOldBlockHit));
							list.add(new VarInsnNode(ASTORE, 4)); // reset movingObjectPos to old blockhit
							list.add(new JumpInsnNode(GOTO, label)); // jump for new processing
							//list.add(labelskip);
						}
						list.add(conditionLabel);
						method.instructions.insert(node, list);
						log.info(coreprefix+": Patched LOTREntityProjectileBase.onUpdate() #2");
					}
					else if(node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(obf ? "func_70067_L" : "canBeCollidedWith")) {
						method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0)); // this
						method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "allowProjectileTarget", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)Z", false));
						log.info(coreprefix+": Patched LOTREntityProjectileBase.onUpdate() #4");
					}
				}
				
				if(!ASMConfig.alwaysAbsorbPiercingProjectiles) {
					InsnList list = new InsnList();
					list.add(new VarInsnNode(ALOAD, 0)); // this
					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "resetBlacklist", "(Llotr/common/entity/projectile/LOTREntityProjectileBase;)V", false));
					method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
					log.info(coreprefix+": Patched LOTREntityProjectileBase.onUpdate() #5");
				}
			}
        }
        
        
        
        MethodNode methodnode = new MethodNode(ACC_PUBLIC, obf ? "func_70056_a" : "setPositionAndRotation2", "(DDDFFI)V", null, null);
		InsnList list = new InsnList();
    	list.add(new VarInsnNode(ALOAD, 0)); // this
    	list.add(new VarInsnNode(DLOAD, 1)); // x
		list.add(new VarInsnNode(DLOAD, 3)); // y
		list.add(new VarInsnNode(DLOAD, 5)); // z
		
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "lotr/common/entity/projectile/LOTREntityProjectileBase", obf ? "func_70107_b" : "setPosition", "(DDD)V", false));
		
		list.add(new VarInsnNode(ALOAD, 0)); // this
    	list.add(new VarInsnNode(FLOAD, 7)); // yaw
		list.add(new VarInsnNode(FLOAD, 8)); // pitch
		
		list.add(new MethodInsnNode(INVOKEVIRTUAL, "lotr/common/entity/projectile/LOTREntityProjectileBase", obf ? "func_70101_b" : "setRotation", "(FF)V", false));
		list.add(new InsnNode(RETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method setPositionAndRotation2() to LOTREntityProjectileBase");
        
		
		/*
		
		1. Prevent projectiles from going deep into surface
			- server and clientside blockraytracing directly after "skipping"
			- stop projectile and blacklist currently hit entity. (you can skip onUpdate of other
			
		2. continue to fly for allied npc attacking allied npcs (only)
		
		3. continue to fly for clientside for weapons that dont die (or in general, stops automatically with death)
		
		4. 

		if shooter npc allied with attacked npc, continue to fly,
		- this can be done in the entity raycast processing. 
		- (needs to deep into surface prevention)
		- also works on clientside
		
		
		
		 */
		
		
	}
	
	static void transformEntityArrow(ClassNode classNode) {
		String nameUpdate = obf ? "h" : "onUpdate";
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(nameUpdate) || method.desc.equals("()V")) {
				boolean fireRemoval = false;
				boolean fireInsert = false;
				
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(ASMConfig.projectilePatchesReqClient && node.getOpcode() == INVOKEVIRTUAL && ((MethodInsnNode) node).name.equals(obf ? "R" : "canBeCollidedWith")) {
						method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0)); // this
						method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "allowProjectileTarget", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)Z", false));
						log.info(coreprefix+": Patched EntityArrow.onUpdate() #4");
					}
					
					if(!fireRemoval 
						&& node.getOpcode() == INVOKEVIRTUAL 
						&& isMethod(node, obf ? "zc" : "net/minecraft/entity/projectile/EntityArrow", obf ? "al" : "isBurning", "()Z")) {
						
							Label stopBeforeLabel = ((JumpInsnNode) node.getNext()).label.getLabel();
							
							fireRemoval = true;
							
							// go back one entry because ALOAD 0 (loading entity (this) )
							node = node.getPrevious();
							
							// remove all entries until label is found
							// dont remove label as someone might use it
							while(node != null && !(node instanceof LabelNode && ((LabelNode) node).getLabel().equals(stopBeforeLabel))) {
								AbstractInsnNode next = node.getNext();
								method.instructions.remove(node);
								node = next;
							}
						
						
					}

					if(fireRemoval && !fireInsert 
						&& node.getOpcode() == INVOKEVIRTUAL 
						&& isMethod(node, obf ? "sa" : "net/minecraft/entity/Entity", obf ? "a" : "attackEntityFrom", obf ? "(Lro;F)Z" : "(Lnet/minecraft/util/DamageSource;F)Z")) {
							
							InsnList list = new InsnList();
							list.add(new VarInsnNode(ALOAD, 0)); // entity (this)
							list.add(new VarInsnNode(ALOAD, 4)); // movingobjectposition
							list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "setTargetFireIfBurning", "(Lnet/minecraft/entity/projectile/EntityArrow;Lnet/minecraft/util/MovingObjectPosition;)V", false));
							// after IFEQ
							method.instructions.insert(node.getNext(), list);
							log.info(coreprefix+": Patched EntityArrow.onUpdate() #4.1");
							fireInsert = true;
					}
				}
			}
		}
		

		
	}
	
	static void transformEntityTrackerEntry(ClassNode classNode) {
		int opcode = Math.min(ICONST_4, entityPosRotationThreshold + 3);
		
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(obf ? "a" : "sendLocationToAllClients") && method.desc.equals("(Ljava/util/List;)V")) {
				int replaces = 0;
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(projectilePatches && projectilePatchesReqClient)
						if(node.getOpcode() == INSTANCEOF) {
							if(((TypeInsnNode) node).desc.equals(obf ? "zc" : "net/minecraft/entity/projectile/EntityArrow")) {
								method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "sendLocationEarly", "(Lnet/minecraft/entity/Entity;)Z", false));
								log.info(coreprefix+": Patched EntityTrackerEntry.sendLocationToAllClients() #1");
								
							}
						}
				
					if(opcode >= ICONST_1) {
						if(node.getOpcode() == INVOKESTATIC && "abs".equals(((MethodInsnNode) node).name) && node.getNext().getOpcode() == ICONST_4) {
							/*AbstractInsnNode prevnode = node.getPrevious();
							if(prevnode.getOpcode() == ILOAD && ((VarInsnNode) prevnode).var == 7) { // deltaX
								LabelNode label = null;
								int jmps = 0;
								AbstractInsnNode loopNode = node;
								loop1 : while(loopNode != null) {
									AbstractInsnNode temp = loopNode.getNext();
									if(loopNode.getOpcode() == IF_ICMPGE) {
										label = ((JumpInsnNode) loopNode).label;
										jmps++;
										
									}
									
									method.instructions.remove(loopNode);
									loopNode = temp;
									
									if(jmps == 3) {
										break loop1;
									}
								}
								
								InsnList list = new InsnList();
								list.add(new VarInsnNode(ILOAD, 8)); // deltaY
								list.add(new VarInsnNode(ILOAD, 9)); // deltaZ
								list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "checkDeltaSquared", "(III)Z", false));
								list.add(new JumpInsnNode(IFEQ, label));
								node = prevnode;
								System.out.println(node+" "+prevnode+" "+label);
								method.instructions.insert(prevnode, list);
								
								log.info(coreprefix+": Patched EntityTrackerEntry.sendLocationToAllClients() #2");
							}
							else {*/
								method.instructions.set(node.getNext(), new InsnNode(opcode));
								replaces++;
							//}
							
						}
					}
					
				}
				
				if(replaces > 0)
					log.info(coreprefix+": Replaced "+replaces+" Pos/Rotation Thresholds in EntityTrackerEntry.sendLocationToAllClients()");
			}
		}
		

	}
	
	static void transformEntityPlayerMP(ClassNode classNode) {

		
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(obf ? "h" : "onUpdate") && method.desc.equals("()V")) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					// check if the code part starts here
					if(node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals(obf ? "bN" : "destroyedItemsNetCache")) {
						
						LabelNode beginLabel = null;
						// find first node of instruction set, before beginning label
						while(node != null) {
							if(node instanceof LabelNode) {
								beginLabel = (LabelNode) node;
								node = node.getNext(); // skip the label
								break;
							}
							node = node.getPrevious();
						}
						
						// remove nodes
						while(node != null) {
							AbstractInsnNode next = node.getNext();
							method.instructions.remove(node);
							
							// look for end now
							if(node.getOpcode() == GOTO) {
								JumpInsnNode jumpNode = (JumpInsnNode) node;
								if(jumpNode.label == beginLabel) {
									break;
								}
							}
							
							node = next;
						}
						break;
					}
				}
				
				
				InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0)); // player
				list.add(new VarInsnNode(ALOAD, 0)); // instance
				list.add(new FieldInsnNode(GETFIELD, obf ? "mw" : "net/minecraft/entity/player/EntityPlayerMP", obf ? "bN" : "destroyedItemsNetCache", "Ljava/util/List;")); // entityRemovalQueue
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "handleEntityRemovalQueue", "(Lnet/minecraft/entity/player/EntityPlayerMP;Ljava/util/List;)V", false));
				method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
				log.info(coreprefix+": Patched EntityPlayerMP.onUpdate()");
				return;
			}
		}
		
		
	}
	
	static void replaceAll(MethodNode method, String findDesc, String replaceDesc, String findOwner, String replaceOwner, String fieldName) {
		
		
		
		for(AbstractInsnNode node : method.instructions.toArray()) {
			switch(node.getOpcode()) {
				case GETFIELD:
				case PUTFIELD:
					if(node instanceof FieldInsnNode) {
						FieldInsnNode fieldnode = (FieldInsnNode) node;
						if(fieldnode.desc.equals(findDesc) && fieldnode.name.equals(fieldName)) {
							fieldnode.desc = replaceDesc;
						}
					}
					break;
				case NEW:
					if(node instanceof TypeInsnNode) {
						TypeInsnNode typenode = (TypeInsnNode) node;
						if(typenode.desc.equals(findOwner)) {
							typenode.desc = replaceOwner;
						}
					}
					break;
				case INVOKESPECIAL:
				case INVOKEVIRTUAL:
					if(node instanceof MethodInsnNode) {
						MethodInsnNode methodnode = (MethodInsnNode) node;
						if(methodnode.owner.equals(findOwner)) {
							methodnode.owner = replaceOwner;
							
							if(obf) {
								//d()Z
								//func_73156_b()Z
								if(methodnode.name.equals("d") && methodnode.desc.equals("()Z")) {
									methodnode.name = "func_73156_b";
								}
								// c (II)Lapx;
								// func_73158_c(II)Lnet/minecraft/world/chunk/Chunk;
								else if(methodnode.name.equals("c") && methodnode.desc.equals("(II)Lapx;")) {
									methodnode.name = "func_73158_c";
									methodnode.desc = "(II)Lnet/minecraft/world/chunk/Chunk;";
								}
								// b (II)V
								// unloadChunk(II)V
								else if(methodnode.name.equals("b") && methodnode.desc.equals("(II)V")) {
									methodnode.name = "unloadChunk";
									methodnode.desc = "(II)V";
								}
							}
						}
					}
					break;
			}
			
		}
	}
	
	static void transformWorldClient(ClassNode classNode) {
		
		
		String findOwner = null;
		String replaceOwner = null;
		
		String fieldName = null;
		
		String findDesc = null;
		String replaceDesc = null;
		
		if(farView) {
			findOwner = obf ? "bjd" : "net/minecraft/client/multiplayer/ChunkProviderClient";
			replaceOwner = Type.getInternalName(METChunkProviderClient.class);
			
			fieldName = obf ? "b" : "clientChunkProvider"; // field_73033_b
			
			findDesc = 'L' + findOwner + ';';
			replaceDesc = 'L' + replaceOwner + ';';
			
			for(FieldNode field : classNode.fields) {
				if(field.desc.equals(findDesc) && field.name.equals(fieldName)) {
					field.desc = replaceDesc;
					log.info(coreprefix+": Changed Field Desc WorldClient.clientChunkProvider to "+replaceDesc);
				}
			}
		}
		
		for(MethodNode method : classNode.methods) {
			if(entityTrackerPatches && method.name.equals(obf ? "a" : "removeAllEntities") && method.desc.equals("()V")) {
				int varEntity = method.maxLocals;
				method.maxLocals++;
				
				int index = 0;
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == INVOKEVIRTUAL) {
						MethodInsnNode methodNode = (MethodInsnNode) node;
						if(methodNode.name.equals(obf ? "b" : "onEntityRemoved") && methodNode.owner.equals(obf ? "bjf" : "net/minecraft/client/multiplayer/WorldClient") && methodNode.desc.equals(obf ? "(Lsa;)V" : "(Lnet/minecraft/entity/Entity;)V")) {
						
							index++;
							// at var before
							if(index == 1) {
								InsnList list = new InsnList();
								list.add(new VarInsnNode(ASTORE, varEntity)); // entity
								list.add(new VarInsnNode(ALOAD, varEntity)); // entity
								method.instructions.insertBefore(node, list);
							}
							
							// at removal handling after
							InsnList list2 = new InsnList();
							list2.add(new VarInsnNode(ALOAD, index == 1 ? varEntity : 2)); // entity
							list2.add(new VarInsnNode(ALOAD, 0)); // instance
							list2.add(new FieldInsnNode(GETFIELD, obf ? "bjf" : "net/minecraft/client/multiplayer/WorldClient", obf ? "c" : "entityHashSet", obf ? "Lpz;" : "Lnet/minecraft/util/IntHashMap;"));
							list2.add(new VarInsnNode(ALOAD, 0)); // instance
							list2.add(new FieldInsnNode(GETFIELD, obf ? "bjf" : "net/minecraft/client/multiplayer/WorldClient", obf ? "K" : "entitySpawnQueue", "Ljava/util/Set;"));
							list2.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "forceRemoveEntity", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/IntHashMap;Ljava/util/Set;)V", false)); // entity
							method.instructions.insert(node, list2);
							log.info(coreprefix+": Patched WorldClient.removeAllEntities() #"+index);
							//return;
						}
					}
				}
			}
			
			if(farView) {
				if(method.name.equals(obf ? "j" : "createChunkProvider") && method.desc.equals(obf ? "()Lapu;" : "()Lnet/minecraft/world/chunk/IChunkProvider;")) {
					replaceAll(method, findDesc, replaceDesc, findOwner, replaceOwner, fieldName);
					log.info(coreprefix+": Patched WorldClient.createChunkProvider()");
				}
				
				if(method.name.equals(obf ? "b" : "tick") && method.desc.equals("()V")) {
					replaceAll(method, findDesc, replaceDesc, findOwner, replaceOwner, fieldName);
					log.info(coreprefix+": Patched WorldClient.tick()");
				}
				
				if(method.name.equals(obf ? "a" : "doPreChunk") && method.desc.equals("(IIZ)V")) {
					replaceAll(method, findDesc, replaceDesc, findOwner, replaceOwner, fieldName);
					
					for(AbstractInsnNode node : method.instructions.toArray()) {
						if(node.getOpcode() == ILOAD && ((VarInsnNode) node).var == 3 && node.getNext().getOpcode() == IFNE) { // boolean unload
							JumpInsnNode jump = (JumpInsnNode) node.getNext();
							InsnList list = new InsnList();
							list.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(METChunkProviderClient.class), "farViewEnabled", "Z"));
							list.add(new JumpInsnNode(IFNE, jump.label));
							method.instructions.insert(jump, list);
							log.info(coreprefix+": Patched WorldClient.doPreChunk()");
							break;
						}
					}
				}
			}
			
			
		}
		
		
		
		
	}
	
	static void transformLOTRItemSpear(ClassNode classNode) {
		
		MethodNode methodnode = new MethodNode(ACC_PUBLIC, obf ? "func_77659_a" : "onItemRightClick", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", null, null);
		InsnList list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 3)); // player
		list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "isLockedMode", obf ? "(Lyz;)Z" : "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
		LabelNode label = new LabelNode();
		list.add(new JumpInsnNode(IFEQ, label));
		
		list.add(new VarInsnNode(ALOAD, 1)); // itemstack
		list.add(new InsnNode(ARETURN));
		
		list.add(label);
		list.add(new VarInsnNode(ALOAD, 0)); // this
		list.add(new VarInsnNode(ALOAD, 1)); // itemstack
		list.add(new VarInsnNode(ALOAD, 2)); // world
		list.add(new VarInsnNode(ALOAD, 3)); // player
		
		list.add(new MethodInsnNode(INVOKESPECIAL, "lotr/common/item/LOTRItemSword", obf ? "func_77659_a" : "onItemRightClick", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", false));
		list.add(new InsnNode(ARETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method onItemRightClick() to LOTRItemSpear");

	}
	
	static void transformLOTRBlockGate(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
			if(method.name.equals(obf ? "func_149727_a" : "onBlockActivated") && method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z")) {
				for(AbstractInsnNode node : method.instructions.toArray()) {
					if(node.getOpcode() == INVOKESTATIC) {
						if(((MethodInsnNode) node).name.equals("isRangedWeapon")) {
							method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 5)); // player
							method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksProjectile.class), "cancelGateActivation", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
							
							log.info(coreprefix+": Patched LOTRBlockGate.onBlockActivated()");
							return;
						}
					}
					
				}
			}
		}
		
		
	}
}
