package metweaks.features;

import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRTradeable;
import lotr.common.entity.npc.LOTRUnitTradeable;
import net.minecraft.entity.Entity;

public class CaptainsSeekTargets {
	public static void handle(Entity entity) {
		if(entity instanceof LOTRUnitTradeable && !(entity instanceof LOTRTradeable) && entity instanceof LOTREntityNPC) {
			((LOTREntityNPC) entity).addTargetTasks(true);
		}
	}
}
