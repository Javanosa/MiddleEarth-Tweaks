package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import static metweaks.core.ClassTransformer.*;
import org.objectweb.asm.tree.*;
import metweaks.ASMConfig;

public class TCallsNetwork {
	
	
	static void transformEntityLivingBase(ClassNode classNode) {
		if(ASMConfig.smoothHeadYawMovement)
			applyHeadYawRotationPatch(classNode, true);
		
		if(ASMConfig.smoothEntityMovement == 0) return;
		
		classNode.visitField(ACC_PUBLIC, "cmTick", "I", null, null);
		log.info(coreprefix+": Added Field EntityLivingBase.cmTick");
		
		for(MethodNode method : classNode.methods) {
    		if(method.desc.equals("(FF)V") && method.name.equals(obf ? "e" : "moveEntityWithHeading")) {
    			InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "cancelClientMovement", "(Lnet/minecraft/entity/EntityLivingBase;)Z", false));
				LabelNode label = new LabelNode();
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new InsnNode(RETURN));
				list.add(label);
				list.add(new FrameNode(F_SAME, 3, null, 0, null));
				method.instructions.insert(list);
    			log.info(coreprefix+": Patched EntityLivingBase.moveEntityWithHeading()");
    		}
		}
		

	}
	
	private static void addField(ClassNode classNode, String name, String desc) {
		classNode.visitField(ACC_PUBLIC, name, desc, null, null);
		log.info(coreprefix+": Added Field NetHandlerPlayServer."+name);
	}
	
	static void transformNetHandlerPlayServer(ClassNode clazz) {
		boolean cauldron = CoreMod.hasKCauldron();
		
		/*
		
		// publish fields
		private double lastPosX;
	    private double lastPosY;
	    private double lastPosZ;
	    private boolean hasMoved = true;
	    private int networkTickCount;
	    private int floatingTickCount;
		
		
		// add fields
		private double firstGoodX;
	    private double firstGoodY;
	    private double firstGoodZ;
	    private int movePacketCounter;
	    private int lastMovePacketCounter;
	    private boolean floating;
	    private int lastPositionUpdate;
		
		*/
		
		final List<String> fields = Arrays.asList(obf ? new String[] {"o", "p", "q", "f", "e", "r"} : new String[] {"lastPosX", "lastPosY", "lastPosZ", "floatingTickCount", "networkTickCount", "hasMoved"});

		for(FieldNode field : clazz.fields) {
			if(fields.contains(field.name)) {
				field.access = ACC_PUBLIC;
				log.info(coreprefix+": Made Field NetHandlerPlayServer."+field.name+" public");
			}
		}
		
		addField(clazz, "firstGoodX", "D");
		addField(clazz, "firstGoodY", "D");
		addField(clazz, "firstGoodZ", "D");
		addField(clazz, "movePacketCounter", "I");
		addField(clazz, "lastMovePacketCounter", "I");
		addField(clazz, "lastPositionUpdate", "I");
		addField(clazz, "floating", "Z");
		
		for(MethodNode method : clazz.methods) {
    		if(cauldron ? (method.desc.equals("(Lorg/bukkit/Location;)V") && method.name.equals("teleport")) : (method.desc.equals("(DDDFF)V") && method.name.equals(obf ? "a" : "setPlayerLocation"))) {
    			InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "markLastPosUpdate", "(Lnet/minecraft/network/NetHandlerPlayServer;)V", false));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched NetHandlerPlayServer."+(cauldron ? "teleport()" : "setPlayerLocation()"));
    		}
    		else if(method.desc.equals(obf ? "(Ljd;)V" : "(Lnet/minecraft/network/play/client/C03PacketPlayer;)V") && method.name.equals(obf ? "a" : "processPlayer")) {
    			InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new VarInsnNode(ALOAD, 1));
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "processPlayer", "(Lnet/minecraft/network/NetHandlerPlayServer;Lnet/minecraft/network/play/client/C03PacketPlayer;)Z", false));
				LabelNode label = new LabelNode();
				list.add(new JumpInsnNode(IFEQ, label));
				list.add(new InsnNode(RETURN));
				list.add(label);
				list.add(new FrameNode(F_SAME, 2, null, 0, null));
				//method.instructions.clear();
				//method.localVariables.clear();
				method.instructions.insert(list);
    			log.info(coreprefix+": Patched NetHandlerPlayServer.processPlayer()");
    		}
    		else if(method.desc.equals("()V") && method.name.equals(obf ? "a" : "onNetworkTick")) {
    			InsnList list = new InsnList();
				list.add(new VarInsnNode(ALOAD, 0));
				list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "onNetworkTick", "(Lnet/minecraft/network/NetHandlerPlayServer;)V", false));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched NetHandlerPlayServer.onNetworkTick()");
    		}
		}
		
		
	}
	
	static void transformGetters(ClassNode classNode) {
		Map<String, String> obfMap = null;
		if(obf) {
			obfMap = new HashMap<>(6);
			obfMap.put("lastPosX", "field_147373_o");
			obfMap.put("lastPosY", "field_147382_p");
			obfMap.put("lastPosZ", "field_147381_q");
			obfMap.put("floatingTickCount", "field_147365_f");
			obfMap.put("networkTickCount", "field_147368_e");
			obfMap.put("hasMoved", "field_147380_r");
		}
		
		
		
		for(MethodNode method : classNode.methods) {
			String desc = method.desc;
			String name = method.name;
    		if(!"()V".equals(desc) && !"<init>".equals(name)) {
    			InsnList list = new InsnList();
    			list.add(new VarInsnNode(ALOAD, 0));
    			
    			int endParam = desc.indexOf(')');
    			String returnType = desc.substring(endParam+1);
    			String params = desc.substring(0, endParam);
    			String owner = params.substring(2, params.indexOf(';'));
    			
    			char ctype = returnType.charAt(0);
    			
    			if(obf) {
    				String obfName = obfMap.get(name);
    				if(obfName != null) name = obfName;
    			}
    			
    			if(ctype == 'V') {
    				
    				int i = params.indexOf(';');
    				returnType = params.substring(i + 1, i + 2);
    				ctype = returnType.charAt(0);
    				
    				
    				
    				list.add(new VarInsnNode((ctype == 'I' || ctype == 'Z') ? ILOAD : DLOAD, 1));
        			list.add(new FieldInsnNode(PUTFIELD, owner, name, returnType));
        			list.add(new InsnNode(RETURN));
    			}
    			else {
        			list.add(new FieldInsnNode(GETFIELD, owner, name, returnType));
        			list.add(new InsnNode((ctype == 'I' || ctype == 'Z') ? IRETURN : DRETURN));
    			}
    			
    			//System.out.println(owner+" "+name+" "+returnType+" "+ctype);
    			
    			method.instructions.clear();
    			method.instructions.insert(list);
    			log.info(coreprefix+": Supply Getters."+method.name+desc);
    		}
		}
	}
	
	static void transformWorld(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "h" : "updateEntities") && method.desc.equals("()V")) {
    			boolean patchA = false;
    			boolean patchB = false;
    			
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(!patchA && node.getOpcode() == LDC && "regular".equals(((LdcInsnNode) node).cst)) {
    					patchA = true;
    					InsnList list = new InsnList();
    					list.add(new VarInsnNode(ALOAD, 0)); // world
    					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "tickPlayers", "(Lnet/minecraft/world/World;)V", false));
    	    			method.instructions.insertBefore(node.getPrevious().getPrevious(), list);
    	    			log.info(coreprefix+": Patched World.updateEntities() #1");
    					
    				}
    				
    				if(patchA && !patchB && node.getOpcode() == LDC && "tick".equals(((LdcInsnNode) node).cst)) {
    					patchB = true;
    				}
    				
    				if(patchB && node.getOpcode() == IFNE) {
    					LabelNode label = ((JumpInsnNode) node).label;
    					InsnList list = new InsnList();
    					list.add(new VarInsnNode(ALOAD, 0));
    					list.add(new VarInsnNode(ALOAD, 2)); // entity
    					list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "canTickHere", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)Z", false));
    					list.add(new JumpInsnNode(IFEQ, label));
    					method.instructions.insert(node, list);
    	    			log.info(coreprefix+": Patched World.updateEntities() #2");
    					
    					break;
    				}
    				
    				
    			}
    		}
		}
		
		
	}
	
	static void transformNetworkSystem(ClassNode classNode) {
		MethodNode methodnode = new MethodNode(ACC_PUBLIC, "getNetManagers", "()Ljava/util/List;", null, null);
		InsnList list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new FieldInsnNode(GETFIELD, obf ? "nc" : "net/minecraft/network/NetworkSystem", obf ? "f" : "networkManagers", "Ljava/util/List;"));
		list.add(new InsnNode(ARETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method getNetManagers() to NetworkSystem");
		
		
	}
	
	static void transformNetworkManager(ClassNode classNode) {
		MethodNode methodnode = new MethodNode(ACC_PUBLIC, "getReceivedPacketsQueue", "()Ljava/util/Queue;", null, null);
		InsnList list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new FieldInsnNode(GETFIELD, obf ? "ej" : "net/minecraft/network/NetworkManager", obf ? "k" : "receivedPacketsQueue", "Ljava/util/Queue;"));
		list.add(new InsnNode(ARETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method getReceivedPacketsQueue() to NetworkManager");
		
		methodnode = new MethodNode(ACC_PUBLIC, "getConnectionState", obf ? "()Leo;" : "()Lnet/minecraft/network/EnumConnectionState;", null, null);
		list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new FieldInsnNode(GETFIELD, obf ? "ej" : "net/minecraft/network/NetworkManager", obf ? "p" : "connectionState", obf ? "Leo;" : "Lnet/minecraft/network/EnumConnectionState;"));
		list.add(new InsnNode(ARETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method getConnectionState() to NetworkManager");
		
		methodnode = new MethodNode(ACC_PUBLIC, "setConnectionStateRaw", obf ? "(Leo;)V" : "(Lnet/minecraft/network/EnumConnectionState;)V", null, null);
		list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new VarInsnNode(ALOAD, 1));
		list.add(new FieldInsnNode(PUTFIELD, obf ? "ej" : "net/minecraft/network/NetworkManager", obf ? "p" : "connectionState", obf ? "Leo;" : "Lnet/minecraft/network/EnumConnectionState;"));
		list.add(new InsnNode(RETURN));
		methodnode.instructions.insert(list);
		classNode.methods.add(methodnode);
		log.info(coreprefix+": Added Method setConnectionStateRaw() to NetworkManager");
		
		for(MethodNode method : classNode.methods) {
			
    		if(method.name.equals(obf ? "a" : "processReceivedPackets") && method.desc.equals("()V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == SIPUSH && ((IntInsnNode) node).operand == 1000) {
    					method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0));
    					method.instructions.insertBefore(node, new FieldInsnNode(GETFIELD, obf ? "ej" : "net/minecraft/network/NetworkManager", obf ? "j" : "isClientSide", "Z"));
    					method.instructions.insertBefore(node, new VarInsnNode(ALOAD, 0));
    					method.instructions.set(node, new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "processPacketsAmount", "(ZLnet/minecraft/network/NetworkManager;)I", false));
    					log.info(coreprefix+": Patched NetworkManager.processReceivedPackets()");
    					break;
    				}
    			}
    		}
		}
		
		
	}

	static void transformNetworkCalls(ClassNode classNode) {
		
		
		
		for(MethodNode method : classNode.methods) {
			
	    		if(method.name.equals("getNetManagers") && method.desc.equals("(Lnet/minecraft/network/NetworkSystem;)Ljava/util/List;")) {
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ALOAD, 0));
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/NetworkSystem", "getNetManagers", "()Ljava/util/List;", false));
	    			list.add(new InsnNode(ARETURN));
	    			method.instructions.clear();
	    			method.instructions.insert(list);
	    			log.info(coreprefix+": Supply NetworkCalls.getNetManagers()");
	    		}
	    		
	    		else if(method.name.equals("getReceivedPacketsQueue") && method.desc.equals("(Lnet/minecraft/network/NetworkManager;)Ljava/util/Queue;")) {
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ALOAD, 0));
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/NetworkManager", "getReceivedPacketsQueue", "()Ljava/util/Queue;", false));
	    			list.add(new InsnNode(ARETURN));
	    			method.instructions.clear();
	    			method.instructions.insert(list);
	    			log.info(coreprefix+": Supply NetworkCalls.getReceivedPacketsQueue()");
	    		}
	    		
	    		else if(method.name.equals("getConnectionState") && method.desc.equals("(Lnet/minecraft/network/NetworkManager;)Lnet/minecraft/network/EnumConnectionState;")) {
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ALOAD, 0));
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/NetworkManager", "getConnectionState", "()Lnet/minecraft/network/EnumConnectionState;", false));
	    			list.add(new InsnNode(ARETURN));
	    			method.instructions.clear();
	    			method.instructions.insert(list);
	    			log.info(coreprefix+": Supply NetworkCalls.getConnectionState()");
	    		}
	    		
	    		else if(method.name.equals("setConnectionStateRaw") && method.desc.equals("(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/network/EnumConnectionState;)V")) {
	    			InsnList list = new InsnList();
	    			list.add(new VarInsnNode(ALOAD, 0));
	    			list.add(new VarInsnNode(ALOAD, 1));
	    			list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/NetworkManager", "setConnectionStateRaw", "(Lnet/minecraft/network/EnumConnectionState;)V", false));
	    			list.add(new InsnNode(RETURN));
	    			method.instructions.clear();
	    			method.instructions.insert(list);
	    			log.info(coreprefix+": Supply NetworkCalls.setConnectionStateRaw()");
	    		}
			
			
			
		}
		
		
	}
	
	static void transformMinecraftServer(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "v" : "updateTimeLightAndEntities") && method.desc.equals("()V")) {
    			InsnList list = new InsnList();
    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "processPacketsServer", "()V", false));
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched MinecraftServer.updateTimeLightAndEntities()");
    			//return;
    		}
		}
		
		
	}
	
	

	static void transformMinecraft(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "ak" : "runGameLoop") && method.desc.equals("()V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == LDC && "tick".equals(((LdcInsnNode) node).cst)) {
    					InsnList list = new InsnList();
    					list.add(new VarInsnNode(ALOAD, 0));
    					list.add(new FieldInsnNode(GETFIELD, obf ? "bao" : "net/minecraft/client/Minecraft", obf ? "ak" : "myNetworkManager", obf ? "Lej;" : "Lnet/minecraft/network/NetworkManager;"));
    	    			list.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "processPacketsClient", "(Lnet/minecraft/network/NetworkManager;)V", false));
    	    			method.instructions.insert(node.getNext(), list);
    	    			log.info(coreprefix+": Patched Minecraft.runGameLoop()");
    					return;
    				}
    			}
    			
    			
    		}
		}
		
	}
	
	static void patchTCPNoDelay(ClassNode classNode) {
		for(MethodNode method : classNode.methods) {
    		if(method.name.equals("initChannel") && method.desc.equals("(Lio/netty/channel/Channel;)V")) {
    			for(AbstractInsnNode node : method.instructions.toArray()) {
    				if(node.getOpcode() == GETSTATIC && ((FieldInsnNode) node).name.equals("TCP_NODELAY")) {
    					method.instructions.set(node.getNext(), new InsnNode(ICONST_1));
    					log.info(coreprefix+": Patched "+classNode.name+".initChannel()");
    					break;
    				}
    			}
    		}
		}
		
		
	}
	
	static void applyHeadYawRotationPatch(ClassNode classNode, boolean livingclass) {
		
		
    	String owner = obf ? "sv" : "net/minecraft/entity/EntityLivingBase";
    	String increments = "newHeadRotationIncrements";
    	String newRotation = "newRotationYawHead";
    	
    	if(livingclass) {
	    	classNode.visitField(ACC_PROTECTED, increments, "I", null, null);
	    	classNode.visitField(ACC_PROTECTED, newRotation, "F", null, null);
	    	log.info(coreprefix+": Added Field EntityLivingBase."+increments);
	    	log.info(coreprefix+": Added Field EntityLivingBase."+newRotation);
    	}
    	
    	InsnList list = new InsnList();
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new VarInsnNode(FLOAD, 1)); // paramNewRotationYawHead
		list.add(new FieldInsnNode(PUTFIELD, owner, newRotation, "F"));
		list.add(new VarInsnNode(ALOAD, 0));
		list.add(new InsnNode(livingclass ? ICONST_3 : ICONST_2));
		list.add(new FieldInsnNode(PUTFIELD, owner, increments, "I"));
		list.add(new InsnNode(RETURN));
    	
		
    	
    	boolean exists = false;
    	
    	for(MethodNode method : classNode.methods) {
    		if(method.name.equals(obf ? "e" : "setRotationYawHead") && method.desc.equals("(F)V")) {
    			exists = true;
    			
    			
    			method.instructions.clear();
    			
    			method.instructions.insert(list);
    			log.info(coreprefix+": Patched "+(livingclass ? "EntityLivingBase" : "EntityOtherPlayerMP")+".setRotationYawHead()");
    			
    		}
    		
    		else if(method.name.equals(obf ? "e" : "onLivingUpdate") && method.desc.equals("()V")) {
    			InsnList list2 = new InsnList();
    			LabelNode label = new LabelNode();
    			list2.add(new VarInsnNode(ALOAD, 0));
    			list2.add(new FieldInsnNode(GETFIELD, owner, increments, "I"));
    			list2.add(new JumpInsnNode(IFLE, label));
    			
    			list2.add(new VarInsnNode(ALOAD, 0));
    			list2.add(new InsnNode(DUP));
    			list2.add(new FieldInsnNode(GETFIELD, owner, increments, "I"));
    			list2.add(new VarInsnNode(ALOAD, 0));
    			list2.add(new FieldInsnNode(GETFIELD, owner, newRotation, "F"));
    			list2.add(new MethodInsnNode(INVOKESTATIC, Type.getInternalName(HooksNetwork.class), "livingUpdateHead", "(Lnet/minecraft/entity/EntityLivingBase;IF)V", false));
    			
    			
    			list2.add(new VarInsnNode(ALOAD, 0));
    			list2.add(new InsnNode(DUP));
    			list2.add(new FieldInsnNode(GETFIELD, owner, increments, "I"));
    			list2.add(new InsnNode(ICONST_1));
    			list2.add(new InsnNode(ISUB));
    			list2.add(new FieldInsnNode(PUTFIELD, owner, increments, "I"));
    			
    			list2.add(new FrameNode(F_SAME, 1, null, 0, null)); // current 1 locals, this
    			list2.add(label);
    			method.instructions.insert(list2);
    			log.info(coreprefix+": Patched "+(livingclass ? "EntityLivingBase" : "EntityOtherPlayerMP")+".onLivingUpdate()");
    			
    			
    			
    		}
    	}
    	
    	if(!exists) {
    		MethodNode methodnode = new MethodNode(ACC_PUBLIC, obf ? "e" : "setRotationYawHead", "(F)V", null, null);
			methodnode.instructions.insert(list);
			classNode.methods.add(methodnode);
			log.info(coreprefix+": Added Method setRotationYawHead() to "+(livingclass ? "EntityLivingBase" : "EntityOtherPlayerMP"));
    	}
    	
    	
    }
    
    
}
