package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import static metweaks.core.ClassHandler.*;

import org.objectweb.asm.tree.*;

import metweaks.ASMConfig;
import metweaks.MeTweaksConfig;
import metweaks.network.SyncedConfig;

public class TCallsConquest {
	static void transformLOTRBiomeSpawnList$FactionContainer(ClassNode clazz) {
    	for(MethodNode method : clazz.methods) {
    		if(method.name.equals("getEffectiveConquestStrength") && method.desc.equals("(Lnet/minecraft/world/World;Llotr/common/world/map/LOTRConquestZone;)F")) {

        		boolean found = false;
        		LabelNode label = new LabelNode();
        		
				for(AbstractInsnNode node : method.instructions.toArray()) {
					
					if(node.getOpcode() == FSTORE && ((VarInsnNode) node).var == 3 && !found) {
						InsnList toInsert = new InsnList();
						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(MeTweaksConfig.class), "allyConquestSpawns", "Z"));	
						toInsert.add(new JumpInsnNode(IFEQ, label));
						method.instructions.insert(node, toInsert);
						found = true;
					}
					
					if(found && node.getOpcode() == FRETURN) {
						method.instructions.insertBefore(node.getPrevious(), label);
						log.info(coreprefix+ ": Patched LOTRBiomeSpawnList$FactionContainer.getEffectiveConquestStrength()");
						return;
					}
				}

            }
        }
    }
	
	static void transformLOTRLevelData(ClassNode clazz) {
    	for(MethodNode method : clazz.methods) {
    		if(method.desc.equals("()V") && (method.name.equals("save") || method.name.equals("load"))) {
    			boolean done = false;
    			boolean save = method.name.equals("save");
    			int counter = 0;
    			int localLevelData = save ? 1 : 0;
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == ALOAD && ((VarInsnNode) node).var == localLevelData && !done) { // levelData
    					counter++;
    					
    					if(counter < 5 && !save)
    						continue;
    					
    					InsnList toInsert = new InsnList();
    					
    					toInsert.add(new VarInsnNode(ALOAD, localLevelData));
    					toInsert.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), 
    							save ? "saveLvLData" : "loadLvLData", 
    							"(Lnet/minecraft/nbt/NBTTagCompound;)V", false));
						done = true;
						method.instructions.insertBefore(node, toInsert);
    					log.info(coreprefix+": Patched LOTRLevelData."+method.name+"()");
    					break;
    				}
    			}
    		}
    	}
    	
    }
    
    static void transformLOTRConquestZone(ClassNode clazz) {
    	for(MethodNode method : clazz.methods) {
    		if(method.name.equals("calcTimeStrReduction") && method.desc.equals("(J)F")) {
    			method.instructions.clear();
    			InsnList toInsert = new InsnList();
    			toInsert.add(new VarInsnNode(LLOAD, 1)); // worldTime
    			toInsert.add(new VarInsnNode(ALOAD, 0)); // zone
    			toInsert.add(new FieldInsnNode(GETFIELD, "lotr/common/world/map/LOTRConquestZone", "lastChangeTime", "J"));
    			toInsert.add(new FieldInsnNode(INVOKESTATIC, Type.getInternalName(Hooks.class), "getReductionTime", "(JJ)F"));
    			toInsert.add(new InsnNode(FRETURN));
    			method.instructions.insert(toInsert);
    			log.info(coreprefix+": Patched LOTRConquestZone.calcTimeStrReduction()");
    			return;
    		}
    	}
    }
    
    static void transformLOTRConquestGrid(ClassNode clazz) {
 	   for(MethodNode method : clazz.methods) {
     		
     	if(ASMConfig.allyConquestSpawns)
     		if(method.name.equals("getConquestEffectIn") && method.desc.equals(
             "(Lnet/minecraft/world/World;Llotr/common/world/map/LOTRConquestZone;Llotr/common/fac/LOTRFaction;)Llotr/common/world/map/LOTRConquestGrid$ConquestEffective;")) {
             		
     			for(AbstractInsnNode node : method.instructions.toArray()) {
     				
     				if(node.getOpcode() == GETSTATIC && node.getNext().getOpcode() == ARETURN && ((FieldInsnNode) node).name.equals("EFFECTIVE")) {
     					InsnList toInsert = new InsnList();
 						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(MeTweaksConfig.class), "allyConquestSpawns", "Z"));
 						LabelNode label = new LabelNode();
 						toInsert.add(new JumpInsnNode(IFNE, label));
 						toInsert.add(new FieldInsnNode(GETSTATIC, "lotr/common/world/map/LOTRConquestGrid$ConquestEffective", "NO_EFFECT", "Llotr/common/world/map/LOTRConquestGrid$ConquestEffective;"));
 						toInsert.add(new InsnNode(ARETURN));
 						toInsert.add(label);
 						method.instructions.insert(node.getNext().getNext().getNext(), toInsert);
 						log.info(coreprefix+": Patched LOTRConquestGrid.getConquestEffectIn()");
 						break;
     				}
     			}
     		}
     		
     	if(ASMConfig.allyKillReduceConquest)
         	if(method.name.equals("doRadialConquest") && method.desc.equals(
         		"(Lnet/minecraft/world/World;Llotr/common/world/map/LOTRConquestZone;Lnet/minecraft/entity/player/EntityPlayer;Llotr/common/fac/LOTRFaction;Llotr/common/fac/LOTRFaction;FF)F")) {

         		int foundParts = 1;
         		int offset = method.maxLocals;
         		int localEnemyFaction = offset++;
         		int localallylist = offset++;
         		
 				for(AbstractInsnNode node : method.instructions.toArray()) {
 					// FSTORE enemyConq
 					if(node.getOpcode() == FSTORE && ((VarInsnNode) node).var == 9 && foundParts == 0) { // centralConqBonus
 						InsnList toInsert3 = new InsnList();
 						toInsert3.add(new InsnNode(ACONST_NULL));
 						
 						toInsert3.add(new VarInsnNode(ASTORE, localallylist));
 						toInsert3.add(new VarInsnNode(ALOAD, 4)); // enemyFaction
 						LabelNode labellistnull = new LabelNode();
 						toInsert3.add(new JumpInsnNode(IFNULL, labellistnull));
 						toInsert3.add(new VarInsnNode(ALOAD, 4)); // enemyFaction
 						toInsert3.add(new MethodInsnNode(INVOKEVIRTUAL, "lotr/common/fac/LOTRFaction", "getConquestBoostRelations", "()Ljava/util/List;", false));
 						toInsert3.add(new VarInsnNode(ASTORE, localallylist));
 						toInsert3.add(labellistnull);
 						method.instructions.insert(node, toInsert3);
 						foundParts = 1;
 						
 						//ACONST_NULL
 						//ASTORE allylist
 						//ALOAD enemyFaction
 						//IFNULL G
 						//ALOAD enemyFaction
 						//INVOKEVIRTUAL lotr/common/fac/LOTRFaction.getConquestBoostRelations()Ljava/util/List;
 						//ASTORE allylist
 					}
 					
 					if(node.getOpcode() == FSTORE && ((VarInsnNode) node).var == 21 && foundParts == 1) { // enemyConq
 						InsnList toInsert2 = new InsnList();
 						toInsert2.add(new VarInsnNode(ALOAD, 4)); // enemyFaction
 						toInsert2.add(new VarInsnNode(ASTORE, localEnemyFaction));
 						
 						AbstractInsnNode nodeprev = node.getPrevious().getPrevious().getPrevious();
 						
 						method.instructions.insertBefore(nodeprev.getPrevious(), toInsert2);
 						if(nodeprev.getOpcode() == ALOAD) {
 							//((VarInsnNode) nodeprev).var = localEnemyFaction;
 							method.instructions.set(nodeprev, new VarInsnNode(ALOAD, localEnemyFaction));
 						}
 	
 						InsnList toInsert = new InsnList();
 						toInsert.add(new FieldInsnNode(GETSTATIC, Type.getInternalName(MeTweaksConfig.class), "allyKillReduceConquest", "Z"));
 						LabelNode label = new LabelNode();
 						toInsert.add(new JumpInsnNode(IFEQ, label));
 						
 						toInsert.add(new VarInsnNode(FLOAD, 21)); // enemyConq
 						toInsert.add(new InsnNode(FCONST_0));
 						toInsert.add(new InsnNode(FCMPG));
 						toInsert.add(new JumpInsnNode(IFGT, label));
 						
 						toInsert.add(new VarInsnNode(ALOAD, localEnemyFaction)); // enemyFaction // localEnemyFaction
 						
 						// INVOKEVIRTUAL lotr/common/fac/LOTRFaction.getConquestBoostRelations()Ljava/util/List;
 						// INVOKEINTERFACE java/util/List.iterator()Ljava/util/Iterator;
 						// ASTORE 5
 						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "lotr/common/fac/LOTRFaction","getConquestBoostRelations","()Ljava/util/List;", false));
 						//toInsert.add(new VarInsnNode(ALOAD, localallylist));
 						toInsert.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List","iterator","()Ljava/util/Iterator;", true));
 						
 						
 						int localIterator = offset++;
 						toInsert.add(new VarInsnNode(ASTORE, localIterator));
 						
 						LabelNode labelLoop = new LabelNode();
 						toInsert.add(labelLoop);
 						
 						toInsert.add(new VarInsnNode(ALOAD, localIterator));
 						// INVOKEINTERFACE java/util/Iterator.hasNext()Z
 						toInsert.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator","hasNext","()Z", true));
 						toInsert.add(new JumpInsnNode(IFEQ, label));
 						
 						toInsert.add(new VarInsnNode(ALOAD, localIterator));
 						toInsert.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator","next","()Ljava/lang/Object;", true));
 						toInsert.add(new TypeInsnNode(CHECKCAST, "lotr/common/fac/LOTRFaction"));
 						int localAllyFac = offset++;
 						toInsert.add(new VarInsnNode(ASTORE, localAllyFac));
 						//
 						toInsert.add(new VarInsnNode(ALOAD, 19)); // zone
 						toInsert.add(new VarInsnNode(ALOAD, localAllyFac));
 						toInsert.add(new VarInsnNode(ALOAD, 0)); // world
 						toInsert.add(new MethodInsnNode(INVOKEVIRTUAL, "lotr/common/world/map/LOTRConquestZone","getConquestStrength","(Llotr/common/fac/LOTRFaction;Lnet/minecraft/world/World;)F", false));
 						
 						
 						int localAllyConq = offset++;
 						toInsert.add(new VarInsnNode(FSTORE, localAllyConq));
 						toInsert.add(new VarInsnNode(FLOAD, localAllyConq));
 						
 						toInsert.add(new InsnNode(FCONST_0));
 						toInsert.add(new InsnNode(FCMPL));
 						LabelNode labelAmountCheck = new LabelNode();
 						toInsert.add(new JumpInsnNode(IFLE, labelAmountCheck));
 						
 						toInsert.add(new VarInsnNode(ALOAD, localAllyFac));
 						toInsert.add(new VarInsnNode(ASTORE, localEnemyFaction)); // enemyFaction // localEnemyFaction
 						
 						toInsert.add(new VarInsnNode(FLOAD, localAllyConq));
 						toInsert.add(new VarInsnNode(FSTORE, 21)); // enemyConq
 						
 						toInsert.add(new JumpInsnNode(GOTO, label));
 						
 						toInsert.add(labelAmountCheck);
 						//
 						
 						toInsert.add(new JumpInsnNode(GOTO, labelLoop));
 						toInsert.add(label);
 						method.instructions.insert(node.getNext(), toInsert);
 						foundParts = 2;
 					}
 					
 					if(foundParts >= 2 && (node.getOpcode() == ALOAD || node.getOpcode() == ASTORE) && ((VarInsnNode) node).var == 4) { // enemyFaction
 						method.instructions.set(node, new VarInsnNode(ALOAD, localEnemyFaction));
 						foundParts++;
 					}
 				}
 				
 				if(foundParts == 5)
 					log.info(coreprefix+": Patched LOTRConquestGrid.doRadialConquest()");
 				else
 					log.error(coreprefix+": Error Patching LOTRConquestGrid.doRadialConquest() I:"+foundParts);
             }
         }
     }
    
    static void transformLOTRReflection(ClassNode clazz) {
    	for(MethodNode method : clazz.methods) {
        	if(method.name.equals("getHorseInv") && method.desc.equals("(Lnet/minecraft/entity/passive/EntityHorse;)Lnet/minecraft/inventory/AnimalChest;")) {
        		method.instructions.clear();
        		method.tryCatchBlocks.clear();
        		
        		InsnList toInsert = new InsnList();
        		toInsert.add(new VarInsnNode(ALOAD, 0)); // horse
        		
        		toInsert.add(new MethodInsnNode(INVOKESTATIC, 
        				Type.getInternalName(HooksLOTR.class), 
    					"getHorseInv", 
    					"(Lnet/minecraft/entity/passive/EntityHorse;)Lnet/minecraft/inventory/AnimalChest;", false));
        		toInsert.add(new InsnNode(ARETURN));
        		
        		method.instructions.insert(toInsert);
        		log.info(coreprefix+ ": Replaced LOTRReflection.getHorseInv()");
        		return;
           }
        }
    }
    
    static void transformFMLHandshakeMessage$ModIdData(ClassNode classNode) {
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "syncedconfig", Type.getDescriptor(SyncedConfig.class), null, null));
    	log.info(coreprefix+": Added Field syncedconfig to FMLHandshakeMessage$ModIdData");
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.desc.equals("(Lio/netty/buffer/ByteBuf;)V")) {
    			boolean toBytes = method.name.equals("toBytes");
    			if(toBytes || method.name.equals("fromBytes")) {
    				InsnList list = new InsnList();
        			list.add(new VarInsnNode(ALOAD, 0)); // modIds
        			list.add(new TypeInsnNode(NEW, Type.getInternalName(SyncedConfig.class)));
        			list.add(new InsnNode(DUP));
        			list.add(new MethodInsnNode(INVOKESPECIAL, Type.getInternalName(SyncedConfig.class), "<init>", "()V", false));
        			
        			list.add(new FieldInsnNode(PUTFIELD, "cpw/mods/fml/common/network/handshake/FMLHandshakeMessage$ModIdData", "syncedconfig", Type.getDescriptor(SyncedConfig.class)));
        			
        			list.add(new VarInsnNode(ALOAD, 0)); // modIds
        			list.add(new FieldInsnNode(GETFIELD, "cpw/mods/fml/common/network/handshake/FMLHandshakeMessage$ModIdData", "syncedconfig", Type.getDescriptor(SyncedConfig.class)));
        			list.add(new VarInsnNode(ALOAD, 1)); // buffer
        			list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(SyncedConfig.class), method.name, "(Lio/netty/buffer/ByteBuf;)V", false));
        			
        			method.instructions.insertBefore(lastBackwards(method.instructions.getLast(), RETURN), list);
        			log.info(coreprefix+": Patched FMLHandshakeMessage$ModIdData."+method.name+"()");
    			}
    		}
    	}
    }
    
    static void transformFMLHandshakeClientState$WAITINGSERVERCOMPLETE(ClassNode classNode) {
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals("accept") && method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;Lcpw/mods/fml/common/network/handshake/FMLHandshakeMessage;)Lcpw/mods/fml/common/network/handshake/FMLHandshakeClientState;")) {
     			for(AbstractInsnNode node : method.instructions.toArray()) {
     				//if(modIds.syncedconfig.checkRestriction(ctx))
     	            //	return ERROR;

     				if(node.getOpcode() == ASTORE && ((VarInsnNode) node).var == 3) { // modIds
     					InsnList list = new InsnList();
     					list.add(new VarInsnNode(ALOAD, 3)); // modIds
     					
     					list.add(new FieldInsnNode(GETFIELD, "cpw/mods/fml/common/network/handshake/FMLHandshakeMessage$ModIdData", "syncedconfig", Type.getDescriptor(SyncedConfig.class)));
     					list.add(new VarInsnNode(ALOAD, 1)); // ctx
     					list.add(new MethodInsnNode(INVOKEVIRTUAL, Type.getInternalName(SyncedConfig.class), "checkRestriction", "(Lio/netty/channel/ChannelHandlerContext;)Z", false));
     					LabelNode label = new LabelNode();
     	     			list.add(new JumpInsnNode(IFEQ, label));
     	     			list.add(new FieldInsnNode(GETSTATIC, "cpw/mods/fml/common/network/handshake/FMLHandshakeClientState$4", "ERROR", "Lcpw/mods/fml/common/network/handshake/FMLHandshakeClientState;"));
     	     			list.add(new InsnNode(ARETURN));
     	     			list.add(label);
     					method.instructions.insert(node, list);
     					log.info(coreprefix+": FMLHandshakeClientState$WAITINGSERVERCOMPLETE.Patched accept()");
     					return;
     				}
     			}
     		}
    	}
    }
}
