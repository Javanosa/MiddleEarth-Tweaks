package metweaks;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigCompartiblity extends MeTweaksConfig {
	
	public static void makeBackUp() {
		// make backup
		try {
			String backup = MeTweaks.MODID+".cfg.backup"+new Random().nextInt(999999);
			FileUtils.copyFile(config.getConfigFile(), new File("config", backup));
			System.out.println("Backup of metweaks.cfg config save as "+backup);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// LT4 = Less Than Version 4
	public static void handleOldConfig_LT4() {
		config.getCategory(MeTweaksConfig.CATEGORY_HUD).remove("Entities: Blacklist"); // reset
		config.getCategory(MeTweaksConfig.CATEGORY_HUD).remove("Show Speed & Jump as Meters"); // reset
	}
	
	public static void handleOldConfig_LT3() {
		rename(MeTweaksConfig.CATEGORY_MISC, "NpcMounts Use Homerange", "Fix Guardmode For Mounted NPCs");
		
		String AICONQUEST = MeTweaksConfig.CATEGORY_AICONQUEST;
		rename(AICONQUEST, "AI Conquest Factor", "Factor Enemy");
		rename(AICONQUEST, "AI Conquest Factor Ally", "Factor Ally");
	}
	
	public static void handleOldConfig_LT2() {
		String GENERAL = CATEGORY_GENERAL;
	    String MOUNTSPEED = CATEGORY_MOUNTSPEED;
	    String MISC = CATEGORY_MISC;
		// just remove old
		config.getCategory(GENERAL).remove("allyConquestSpawns");
		config.getCategory(GENERAL).remove("allyKillReduceConquest");
		// just clear
		config.getCategory(CATEGORY_HUD).clear();
		config.getCategory("mountspeeds").clear();
	    // rename
		renameMove(GENERAL, MOUNTSPEED, "ModifyMountSpeed", "Modify Mountspeed");
		renameMove(GENERAL, MOUNTSPEED, "UseGlobalMountSpeed", "Use Global Mountspeed");
		renameMove(GENERAL, MOUNTSPEED, "GlobalMountSpeedMax", "Global Mountspeed Max");
		renameMove(GENERAL, MOUNTSPEED, "GlobalMountSpeedMin", "Global Mountspeed Min");
		renameMove(GENERAL, MOUNTSPEED, "IgnoreNpcMounts", "Ignore Npc Mounts");
		renameMove(GENERAL, MOUNTSPEED, "OnlyTamedMounts", "Only Tamed Mounts");

		renameMove(GENERAL, CATEGORY_BLOCKS, "MirrorVerticalSlabs", "Mirror VerticalSlabs");

		renameMove(GENERAL, MISC, "AllyConquestSpawns", "AllyConquest Spawns");
		renameMove(GENERAL, MISC, "AllyKillReduceConquest", "AllyKill Decrease Conquest");
		renameMove(GENERAL, MISC, "AlwaysSetBedSpawn", "Always Set Bedspawn");
		renameMove(GENERAL, MISC, "FixSpawnerInvenstoryLag", "Fix SpawnerInvenstoryLag");
		renameMove(GENERAL, MISC, "CheckEnemiesNearBed", "Check Enemies Near Bed");
		// only move
		config.moveProperty(GENERAL, "VerticalSlabs", CATEGORY_BLOCKS);
		config.moveProperty(GENERAL, "BarkBlocks", CATEGORY_BLOCKS);
		config.moveProperty(GENERAL, "KillPotionID", MISC);
		
	}
	
	public static void rename(String category, String oldName, String newName) {
		rename(config, category, oldName, newName);
	}
	
	public static void rename(Configuration config, String category, String oldName, String newName) {
        if(config.hasCategory(category)) {
        	if (config.getCategory(category).containsKey(oldName)) {
        		Property prop = config.getCategory(category).get(oldName);
        		config.get(category, newName, prop.getString(), prop.comment, prop.getType());
                config.getCategory(category).remove(oldName);
            }
        }
    }
	
	public static void renameMove(String oldCategory, String newCategory, String oldName, String newName) {
		rename(config, oldCategory, oldName, newName);
		config.moveProperty(oldCategory, newName, newCategory);
	}

	
}
