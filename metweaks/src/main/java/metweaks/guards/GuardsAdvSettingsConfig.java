package metweaks.guards;

import metweaks.MeTweaksConfig;
import net.minecraftforge.common.config.Configuration;

public class GuardsAdvSettingsConfig {
	public static int maxAiRange = 48;
	
	public static int aiRangeUnlockLvL = 5;
	public static double aiRangeUnlockFactor = 0.5;
	
	public static boolean allowAiRangeModify = true;
	public static boolean allowAmmoRangeModify = true;
	public static boolean allowCheckSightModify = true;
	
	public static boolean autoScaleAiRange = false;
	
	
	
	public static void loadConfig() {
		Configuration config = MeTweaksConfig.config;
		String CATEGORY_GUARDS = MeTweaksConfig.CATEGORY_GUARDS;
		
		maxAiRange = config.get(CATEGORY_GUARDS, "Max AiRange", maxAiRange, null, 0, 127).getInt();
		aiRangeUnlockLvL = config.get(CATEGORY_GUARDS, "AiRange UnlockLvL", aiRangeUnlockLvL).getInt();
		aiRangeUnlockFactor = config.get(CATEGORY_GUARDS, "AiRange UnlockFactor", aiRangeUnlockFactor, "increase by lvl").getDouble();
		allowAiRangeModify = config.get(CATEGORY_GUARDS, "Allow Modify AiRange", allowAiRangeModify).getBoolean();
		allowAmmoRangeModify = config.get(CATEGORY_GUARDS, "Allow Modify AmmoRange", allowAmmoRangeModify).getBoolean();
		allowCheckSightModify = config.get(CATEGORY_GUARDS, "Allow Modify CheckSight", allowCheckSightModify).getBoolean();
		autoScaleAiRange = config.get(CATEGORY_GUARDS, "AutoScale AiRange", autoScaleAiRange, "Scales values based on wander/guardrange").getBoolean();
	}
	
}
