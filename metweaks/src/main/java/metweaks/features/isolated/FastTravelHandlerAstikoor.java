package metweaks.features.isolated;

import java.util.Set;

import astikoor.entity.EntityCart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class FastTravelHandlerAstikoor {
	public static void handleAstikoor(Object entity, Entity mount, int x, int y, int z, Set<EntityLiving> toTransport) {
		if(entity instanceof EntityCart) {
			EntityCart cart = (EntityCart) entity;
			Entity puller = cart.getPulling();
			if(puller != null && ((mount != null && mount == puller || toTransport.contains(puller)))) {
				cart = FastTravelHandler.fastTravelEntity(cart.worldObj, cart, x, y, z);
				//((EntityCart) entity).setPulling(puller); // this would need a mapping, but since its disattached anyway, why do care? We would have to reattach on Success btw

			}
		}
	}
}
