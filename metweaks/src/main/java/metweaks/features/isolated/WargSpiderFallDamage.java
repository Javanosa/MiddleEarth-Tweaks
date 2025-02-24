package metweaks.features.isolated;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.entity.npc.LOTREntityNPCRideable;
import metweaks.MeTweaksConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class WargSpiderFallDamage {
	public WargSpiderFallDamage() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onLivingFall(LivingFallEvent event) {
		if(MeTweaksConfig.reduceWargSpiderFalldamage && event.entity instanceof LOTREntityNPCRideable && event.entity.riddenByEntity != null) {
			event.distance *= 0.5F;
		}
	}
}
