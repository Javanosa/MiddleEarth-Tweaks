package metweaks.features;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import metweaks.MeTweaksConfig;
import net.minecraft.entity.player.EntityPlayer;

public class ThrowableSpearState {
	public static boolean clientThrowable = true;
	public static boolean prevClientThrowable = true;
	
	public static void setThrowableMode(EntityPlayer player, boolean enable) {
		
		UUID id = player.getUniqueID();
		if(!enable) {
			if(!modes.contains(id))
				modes.add(id);
		}
		else
			modes.remove(id);
		
	}
	
	public static final Set<UUID> modes = new HashSet<>();
	
	public static void loadConfig() {
		prevClientThrowable = clientThrowable = MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_MISC, "Throwable Spears", clientThrowable).getBoolean();
		modes.clear();
	}
}
