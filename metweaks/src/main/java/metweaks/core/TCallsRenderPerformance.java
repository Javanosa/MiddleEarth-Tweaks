package metweaks.core;

import static org.objectweb.asm.Opcodes.*;

import static metweaks.core.ClassTransformer.*;
import org.objectweb.asm.tree.*;

public class TCallsRenderPerformance {

	static void transformWrUpdaterSmooth(ClassNode classNode) {
        
        for(MethodNode method : classNode.methods) {
			if("updateRenderers".equals(method.name) 
				&& "(Lbma;Lsv;Z)Z".equals(method.desc)) {
				
				for(AbstractInsnNode node : method.instructions.toArray()) {
					// return (this.renderersUpdated > 0); to return true;
					if(node.getOpcode() == IRETURN) {
						while(node != null) {
							if(node.getOpcode() == ICONST_1) {
								// already patched?
								break;
							}
							
							if(node.getOpcode() == ICONST_0) {
								// patch
								method.instructions.set(node, new InsnNode(ICONST_1));
								log.info(coreprefix+": Patched WrUpdaterSmooth.updateRenderers()");
								break;
							}
							
							node = node.getPrevious();
						}
						break;
					}
				}
			}
		}
	}
	
	static void transformRenderGlobal(ClassNode classNode) {

        for(MethodNode method : classNode.methods) {
			if((obf ? "a" : "updateRenderers").equals(method.name) 
				&& (obf ? "(Lsv;Z)Z" : "(Lnet/minecraft/entity/EntityLivingBase;Z)Z").equals(method.desc)) {
				
				for(AbstractInsnNode node : method.instructions.toArray()) {
					// return (this.renderersUpdated > 0); to return true;
					if(node.getOpcode() == IRETURN && node.getPrevious().getOpcode() == ICONST_0) {
						method.instructions.set(node.getPrevious(), new InsnNode(ICONST_1));
						log.info(coreprefix+": Patched RenderGlobal.updateRenderers()");
						break;
					}
				}
			}
		}
	}
	
}
