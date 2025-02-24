package metweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import cpw.mods.fml.client.config.IConfigElement;
import lotr.common.entity.npc.LOTRNPCMount;
import lotr.common.item.LOTRPoisonedDrinks;
import metweaks.features.ModifierHandler;
import metweaks.features.ModifierHandler.MountData;
import metweaks.guards.GuardsAdvSettingsConfig;
import metweaks.potion.LOTRPotionPoisonKillingFlex;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class MeTweaksConfig {
	// setup
	private static boolean startup = true;
	public static Configuration config;
	
	public static String CATEGORY_GENERAL;
	public static String CATEGORY_HUD;
	public static String CATEGORY_MOUNTSPEEDS;
	public static String CATEGORY_GUARDS;
	public static String CATEGORY_MOUNTSPEED;
	public static String CATEGORY_BLOCKS;
	public static String CATEGORY_MISC;
	public static String CATEGORY_HUDELEMENTS;
	public static String CATEGORY_AICONQUEST;
	
	static final Set<ConfigCategory> categories = new HashSet<ConfigCategory>();
	
	static final Map<Class<? extends EntityLivingBase>, MountData> mountcache = new HashMap<>();
	// values
	public static boolean modifySpeed = true;
	public static boolean useGlobalSpeed = true;
	public static double globalSpeedMax = 0.2;
	public static double globalSpeedMin = 0;
	public static boolean allyConquestSpawns = true;
	public static boolean allyKillReduceConquest = true;
	public static float conquestDecay = 3600;
	
	public static boolean alwaysSetBedSpawn = true;
	public static boolean checkBedUnsafe = true;
	
	public static boolean verticalSlabs = true;
	public static boolean mirrorVerticalSlabs = true;
	public static boolean barkBlocks = true;
	public static boolean woolSlabs = true;
	public static boolean barkSlabs = true;
	public static boolean beamSlabs = true;
	
	
	public static boolean updateCheck = true;
	public static int debug;
	public static boolean ignoreNPCmounts = true;
	public static boolean onlyTamed = true;
	public static boolean fixSpawnerInvenstoryLag = true;
	
	public static boolean showTips = true;
	
	public static int killPotionID = 30;
	
	public static boolean npcMountStayHome = true;
	public static boolean toggleGuardModeHorn = true;
	public static boolean ridersAvoidSuffocation = true;
	
	public static int fangornTreePenaltyThreshold = 100;
	
	public static boolean fixMountsRunningAway = true;
	public static boolean reduceWargSpiderFalldamage = true;
	public static boolean unitTracking = true;
	
	public static boolean aiConquest = false;
	public static float aiConqFactorEnemy = 0.25f;
	public static float aiConqFactorAlly = 0.0f;
	public static boolean enforceAIConquest = false;
	
	public static boolean unrestricted() {
		return !verticalSlabs && killPotionID == 30 && !ASMConfig.guardsEquipRanged && !woolSlabs && !(barkBlocks && (barkSlabs || beamSlabs));
	}
	
	public static void checkPotionID() {
		
		if(MeTweaks.lotr && killPotionID != LOTRPoisonedDrinks.killingPoison.id) {
			LOTRPotionPoisonKillingFlex.changePotionID(LOTRPoisonedDrinks.killingPoison.id, killPotionID);
			
		}
	}
	
	

	private static String addCategory(String name, String desc) {
		ConfigCategory category = config.getCategory(name);
		category.setLanguageKey(MeTweaks.MODID + ".config." + name);
		if(desc != null)
			category.setComment(desc);
		
		categories.add(category);
		return name;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void init(File file) {
		
		
		
		if(ASMConfig.initMountspeeds) {
		
			Map<Class, String> map = EntityList.classToStringMapping;
			map.forEach((Class clazz, String name) -> {
				if(EntityHorse.class.isAssignableFrom(clazz) || (MeTweaks.lotr && LOTRNPCMount.class.isAssignableFrom(clazz))) {
					MountData data = new MountData();
					data.name = name;
					mountcache.put(clazz, data);
				}
			});
			defaults("EntityHorse", new double[] {
				.112, .338, // horse
				.174, .175, // donkey
				.112, .338, // mule
				.2,	  .201, // zombie
				.2,	  .201  // skeleton
			});
			
			defaults("lotr.Horse", new double[] {
				.112, .439, // horse
				.174, .227, // donkey
				.112, .439, // mule
				.2,   .201, // zombie
				.2,	  .201  // skeleton
			});
			
			defaults("lotr.ShirePony", new double[] {.139, .14});
			defaults("lotr.Boar", new double[] {.112, .29});
			defaults("lotr.Camel", new double[] {.174, .175});

		}
		
		config = new Configuration(file);
		CATEGORY_GENERAL = addCategory("general", null);
		CATEGORY_MOUNTSPEEDS = addCategory("individual-mountspeeds", "Disable \"Use Global MountSpeed\". 0.2 is equal to minecart on goldrails. Make sure \"Modify MountSpeed\" is enabled");
		CATEGORY_HUD = addCategory("healthbar", "Show health and info above mobs");
		CATEGORY_GUARDS = addCategory("guards", "Configure Advanced Settings of Hireds");
		CATEGORY_MOUNTSPEED = addCategory("mountspeed", null);
		CATEGORY_BLOCKS = addCategory("blocks", null);
		CATEGORY_MISC = addCategory("misc", null);
		CATEGORY_HUDELEMENTS = addCategory("hudelements", "Move around compass, quest tracker, metweaks status message");
		CATEGORY_AICONQUEST = addCategory("aiconquest", null);
		
		load();
	}
	
	@SuppressWarnings("unchecked")
	public static void defaults(final String key, final double[] speed) {
		@SuppressWarnings("rawtypes")
		final Class clazz = (Class) EntityList.stringToClassMapping.get(key);
		if(clazz != null && mountcache.containsKey(clazz)) {
				MountData data = mountcache.get(clazz);
				
				
				int i = 0;
				while(i < speed.length-1) {
					
					final MountData temp = new MountData(speed[i], speed[i+1]);
					
					if(i >= 2) {
						
						data.subtypes[(i >> 1) -1] = temp; // fill subdata starting at the 3rd value
					}else {
						// init mountdata
						temp.name = data.name;
						data = temp;
						if(speed.length > 2) // add subtypes if more than 2 values
							data.subtypes = new MountData[(speed.length >> 1) - 1];
					}

					i+=2;
				}
				mountcache.replace(clazz, data);
				
		}
	}
	
	public static String getType(int type) {
		switch(type) {
			default:
				return ".donkey";
			case 1:
				return ".mule";
			case 2:
				return ".zombie";
			case 3:
				return ".skeleton";
		}
	}

	public static void load() {
		Configuration config = MeTweaksConfig.config;
		int latestversion = 4;
		
		Property verProperty = config.get(CATEGORY_GENERAL, "ConfigVersion", latestversion).setShowInGui(false);
		int configversion = verProperty.getInt();
		

		
		if(configversion < latestversion) {
			ConfigCompartiblity.makeBackUp();
			
			if(configversion < 2)
				ConfigCompartiblity.handleOldConfig_LT2();
			if(configversion < 3)
				ConfigCompartiblity.handleOldConfig_LT3();
			if(configversion < 4)
				ConfigCompartiblity.handleOldConfig_LT4();
			verProperty.set(latestversion);
		}
		
		
		
		
		
		
		// blocked 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23		, 30
		// free 0, 24, 25, 26, 27, 28, 29, 31
		killPotionID = MathHelper.clamp_int(
				config.get(CATEGORY_MISC, "KillPotionID", 30, null, 0, 31)
				.getInt(), 
				0, 31);
		
		if(!startup)
			checkPotionID();
		

		
		
		debug = config.get(CATEGORY_GENERAL, "DebugLVL", debug, "0 = None, 1 = Minor, 2 = Middle, 3 = High", 0, 3).getInt();
		showTips  = config.get(CATEGORY_GENERAL, "Show Tips", showTips).getBoolean();
		
		
		
		List<String> ord = new ArrayList<>(2);
		ord.add("Modify Mountspeed");
		ord.add("Use Global Mountspeed");
		String MOUNTSPEED = CATEGORY_MOUNTSPEED;
		config.getCategory(MOUNTSPEED).setPropertyOrder(ord);
		
		modifySpeed = config.get(MOUNTSPEED, "Modify Mountspeed", modifySpeed).getBoolean();
		useGlobalSpeed = config.get(MOUNTSPEED, "Use Global Mountspeed", useGlobalSpeed, "Disable to use individual mountspeeds").getBoolean();
		globalSpeedMax = config.get(MOUNTSPEED, "Global Mountspeed Max", globalSpeedMax, null, 0, 1).getDouble();
		globalSpeedMin = config.get(MOUNTSPEED, "Global Mountspeed Min", globalSpeedMin, null, 0, 1).getDouble();
		ignoreNPCmounts = config.get(MOUNTSPEED, "Ignore Npc Mounts", ignoreNPCmounts).getBoolean();
		onlyTamed = config.get(MOUNTSPEED, "Only Tamed Mounts", onlyTamed).getBoolean();
		
		String MISC = MeTweaksConfig.CATEGORY_MISC;
		alwaysSetBedSpawn = config.get(MISC, "Always Set Bedspawn", alwaysSetBedSpawn).getBoolean();
		checkBedUnsafe = config.get(MISC, "Check Enemies Near Bed", checkBedUnsafe, "For MiddleEarth Entities").getBoolean();
		allyConquestSpawns = config.get(MISC, "AllyConquest Spawns", allyConquestSpawns).getBoolean();
		allyKillReduceConquest = config.get(MISC, "AllyKill Decrease Conquest", allyKillReduceConquest).getBoolean();
		fixSpawnerInvenstoryLag = config.get(MISC, "Fix SpawnerInvenstoryLag", fixSpawnerInvenstoryLag).setRequiresMcRestart(true).getBoolean();
		npcMountStayHome = config.get(MISC, "Fix Guardmode For Mounted NPCs", npcMountStayHome).getBoolean();
		toggleGuardModeHorn = config.get(MISC, "Toggle-Guardmode-Horn", toggleGuardModeHorn).getBoolean();
		ridersAvoidSuffocation = config.get(MISC, "Riders Avoid Suffocating in Blocks", ridersAvoidSuffocation).getBoolean();
		fangornTreePenaltyThreshold = config.get(MISC, "Fangorn Tree Pentalty Threshold", fangornTreePenaltyThreshold, "Alignment from which cutting wood is tolerated in moderation").getInt();
		fixMountsRunningAway = config.get(MISC, "Fix Mounts Running Away", fixMountsRunningAway).setRequiresWorldRestart(true).getBoolean();
		reduceWargSpiderFalldamage = config.get(MISC, "Reduce Warg/Spider Rider Falldamage", reduceWargSpiderFalldamage).getBoolean();
		unitTracking = config.get(MISC, "Unit Tracking", unitTracking, "Use F7 + Unit Overview Key. Keeps Units from getting lost").getBoolean();
		mirrorVerticalSlabs = config.get(CATEGORY_BLOCKS, "Mirror VerticalSlabs", mirrorVerticalSlabs, "Mirror VerticalSlab if placed on the side of another").getBoolean();
		
		aiConquest = config.get(CATEGORY_AICONQUEST, "Enable AI Conquest", aiConquest, "NPCs will conquest on their own").getBoolean();
		enforceAIConquest = config.get(CATEGORY_AICONQUEST, "Global", enforceAIConquest, "Ignore players not having pledged and conquest kills enabled").getBoolean();
		aiConqFactorEnemy = (float) config.get(CATEGORY_AICONQUEST, "Factor Enemy", aiConqFactorEnemy).getDouble();
		aiConqFactorAlly = (float) config.get(CATEGORY_AICONQUEST, "Factor Ally", aiConqFactorAlly).getDouble();
		
		if(modifySpeed) {
			String MOUNTSPEEDS = CATEGORY_MOUNTSPEEDS;
			mountcache.forEach((Class<? extends EntityLivingBase> clazz, MountData origin) -> {
				double min = config.get(MOUNTSPEEDS, origin.name+" min", origin.min, null, 0, 1).getDouble();
				double max = config.get(MOUNTSPEEDS, origin.name+" max", origin.max, null, 0, 1).getDouble();
				MountData data = new MountData(min, max);
				
				if(origin.subtypes != null) {
					data.subtypes = new MountData[origin.subtypes.length];
					for(int i = 0; i < origin.subtypes.length; i++) {
						double min2 = config.get(MOUNTSPEEDS, origin.name+getType(i)+" min", origin.subtypes[i].min, null, 0, 1).getDouble();
						double max2 = config.get(MOUNTSPEEDS, origin.name+getType(i)+" max", origin.subtypes[i].max, null, 0, 1).getDouble();
						data.subtypes[i] = new MountData(min2, max2);
					}
				}
				ModifierHandler.mountdata.put(clazz, data);
			});
		}
		

		
		// not getting changed after startup
		if(startup) {
			verticalSlabs = config.get(CATEGORY_BLOCKS, "VerticalSlabs", verticalSlabs).setRequiresMcRestart(true).getBoolean();
			
			barkBlocks = config.get(CATEGORY_BLOCKS, "BarkBlocks", barkBlocks).setRequiresMcRestart(true).getBoolean();
			updateCheck = config.get(CATEGORY_GENERAL, "UpdateCheck", updateCheck).getBoolean();
			
			beamSlabs = config.get(CATEGORY_BLOCKS, "WoodBeamSlabs", beamSlabs, "Requires BarkBlocks").setRequiresMcRestart(true).getBoolean();
			barkSlabs = config.get(CATEGORY_BLOCKS, "BarkSlabs", barkSlabs, "Requires BarkBlocks").setRequiresMcRestart(true).getBoolean();
			woolSlabs = config.get(CATEGORY_BLOCKS, "WoolSlabs", woolSlabs).setRequiresMcRestart(true).getBoolean();
			startup = false;
		}
		
		
		
		if(MeTweaks.lotr && ASMConfig.guardsWanderRange && ASMConfig.guardsAdvancedSettings)
			GuardsAdvSettingsConfig.loadConfig();
		
		MeTweaks.proxy.loadClientConfig();
		
		if(config.hasChanged()) config.save(); 
	}
	
	@SuppressWarnings("rawtypes")
	public static List<IConfigElement> getElementList() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
	    for(ConfigCategory category : categories) {
	      ConfigElement element = new ConfigElement(category);
	      list.add(element);
	    } 
	    return list;
	}
}
