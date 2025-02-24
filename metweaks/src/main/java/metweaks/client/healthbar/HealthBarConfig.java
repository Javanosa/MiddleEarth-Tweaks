package metweaks.client.healthbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import cpw.mods.fml.common.registry.GameRegistry;
import metweaks.MeTweaksAPI;
import metweaks.MeTweaksConfig;
import metweaks.network.SyncedConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class HealthBarConfig {
	public static boolean renderHealth = true;
	public static boolean prevRenderHealth = true;
	
	public static boolean showAll = true;
	public static boolean showMouseover = true;
	public static boolean onlyInjured = true;
	public static boolean onlyHiredOrPlayer = false;
	
	public static boolean hideOnHireds = true;
	public static boolean hideOnFellowshipMembers = true;
	public static boolean hideOnPlayers = false;
	public static boolean hideOnSpeech = false;
	
	//public static boolean showAttributes = true;
	public static boolean attributesOnlyTamed = true;
	public static boolean attributesAll = false;
	
	public static boolean showIcons = true;

	public static boolean showSpeed = false;
	public static boolean showClampSpeed = true;
	public static boolean showJump = true;
	public static boolean showArmor = true;

	public static boolean showDesc = true;
	
	public static double offsetY = 0.5;
	public static int maxDistance = 25;
	public static int maxDistanceSq;
	
	public static int[] color_background;
	public static int[] color_injured;
	public static int[] color_health;
	public static int[] color_mount;
	public static int[] color_player;
	
	public static Item icon_speed;
	public static int icon_speed_meta;
	public static Item icon_jump;
	public static int icon_jump_meta;
	public static Item icon_armor;
	public static int icon_armor_meta;
	
	@SuppressWarnings("rawtypes")
	public static Set<Class> blacklist = new HashSet<>();
	
	public static boolean speedAsMeters = true;
	
	
	
	
	
	// font size is 7.99F for p, else 7 most of the time
	
	public static Pattern hexcolorPattern = Pattern.compile("#?[0-9a-fA-F]{6,8}");
	
	public static void loadConfig() {
		SyncedConfig.prevMirrorVerticalSlabs = MeTweaksConfig.mirrorVerticalSlabs;
		
		Configuration config = MeTweaksConfig.config;
		String CATEGORY_HUD = MeTweaksConfig.CATEGORY_HUD;
		
		List<String> ord = new ArrayList<>();
		ord.add("Enable Health Bar");
		ord.add("Entities: Show All");
		ord.add("Entities: Show Mouse Over");
		ord.add("Entities: Blacklist");
		ord.add("Entities: Only Hired Or Player");
		ord.add("Max Distance");
		ord.add("Offset Y");
		ord.add("Only Injured");
		config.getCategory(CATEGORY_HUD).setPropertyOrder(ord);
		
		
		
		
		renderHealth = config.get(CATEGORY_HUD, "Enable Health Bar", renderHealth).getBoolean();
		prevRenderHealth = renderHealth;
		
		showAll = config.get(CATEGORY_HUD, "Entities: Show All", showAll).getBoolean();
		showMouseover = config.get(CATEGORY_HUD, "Entities: Show Mouse Over", showMouseover).getBoolean();
		onlyHiredOrPlayer = config.get(CATEGORY_HUD, "Entities: Only Hired Or Player", onlyHiredOrPlayer).getBoolean();
		
		String[] str_blacklist = config.get(CATEGORY_HUD, "Entities: Blacklist", new String[] {"lotr.Butterfly", "lotr.Bird", "Bat", "lotr.Midges"}, "Use the names from Spawn Eggs Descripton or /summon").getStringList();
		
		blacklist.clear();
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, Class> map = EntityList.stringToClassMapping;
	
		
		
		for(String regName : str_blacklist) {
			String trim = regName.trim();
			if(map.containsKey(trim)) {
				blacklist.add(map.get(trim));
			}
		}
		
		maxDistance = config.get(CATEGORY_HUD, "Max Distance", maxDistance).getInt();
		maxDistanceSq = maxDistance * maxDistance;
		offsetY = config.get(CATEGORY_HUD, "Offset Y", offsetY).getDouble();
		onlyInjured = config.get(CATEGORY_HUD, "Only Injured", onlyInjured, "Only show health for injured mobs").getBoolean();
		
		String colordesc = "rgba (8 digit hex), Example: #ED731CEE";
		color_player = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUD, "Color Player", 
				"#B0951CDD", colordesc, hexcolorPattern).getString(), color_player, 128);
		color_health = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUD, "Color Health", 
				"#388520DD", colordesc, hexcolorPattern).getString(), color_health, 128);
		color_mount = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUD, "Color Mount", 
				"#1155ADDD", colordesc, hexcolorPattern).getString(), color_mount, 128);
		color_background = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUD, "Color Background", 
				"#00000040", colordesc, hexcolorPattern).getString(), color_background, 128);
		color_injured = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUD, "Color Injured", 
				"#9C1E13DD", colordesc, hexcolorPattern).getString(), color_injured, 128);
		
		
		
        
		
		hideOnHireds = config.get(CATEGORY_HUD, "Hide On Hireds", hideOnHireds).getBoolean();
		hideOnFellowshipMembers = config.get(CATEGORY_HUD, "Hide On FellowshipMembers", hideOnFellowshipMembers).getBoolean();
		hideOnPlayers = config.get(CATEGORY_HUD, "Hide On Players", hideOnPlayers).getBoolean();
		hideOnSpeech = config.get(CATEGORY_HUD, "Hide On Speech", hideOnSpeech).getBoolean();
		
		
		
		showIcons = config.get(CATEGORY_HUD, "Show Icons", showIcons).getBoolean();
		 
		showSpeed = config.get(CATEGORY_HUD, "Show Speed", showSpeed).getBoolean();
		showClampSpeed = config.get(CATEGORY_HUD, "Show Modified Speed", showClampSpeed).getBoolean();
		showJump = config.get(CATEGORY_HUD, "Show Jump", showJump).getBoolean();
		showArmor = config.get(CATEGORY_HUD, "Show Armor", showArmor).getBoolean();
		 
		showDesc = config.get(CATEGORY_HUD, "Show Description", showDesc).getBoolean();
		
		
		
		
		
		String iconDesc = "Format: modid:item:meta";
		Object[] speed = parseIcon(config.get(CATEGORY_HUD, "Icon Speed", "minecraft:blaze_powder:0", iconDesc), Items.blaze_powder, 0);
		icon_speed = (Item) speed[0];
		icon_speed_meta = (int) speed[1];
		Object[] jump = parseIcon(config.get(CATEGORY_HUD, "Icon Jump", "minecraft:golden_boots:0", iconDesc), Items.golden_boots, 0);
		icon_jump = (Item) jump[0];
		icon_jump_meta = (int) jump[1];
		Object[] armor = parseIcon(config.get(CATEGORY_HUD, "Icon Armor", "minecraft:iron_chestplate:0", iconDesc), Items.iron_chestplate, 0);
		icon_armor = (Item) armor[0];
		icon_armor_meta = (int) armor[1];
		
		
		//showAttributes = config.get(CATEGORY_HUD, "ShowAttributes", showAttributes).getBoolean();
		attributesOnlyTamed = config.get(CATEGORY_HUD, "Info Only On Tamed", attributesOnlyTamed).getBoolean();
		attributesAll = config.get(CATEGORY_HUD, "Info On All", attributesAll).getBoolean();
		
		speedAsMeters = config.get(CATEGORY_HUD, "Show Speed & Jump as Meters", speedAsMeters).getBoolean();
		
		
	}
	
	// returns [item, meta]
	private static Object[] parseIcon(Property property, Item fallback, int fallbackMeta) {
		String key = property.getString();
		String[] data = key.split(":", 3);
		String mod = "minecraft";
		String item = "";
		
		int meta = fallbackMeta;
		if(data.length == 1) {
			item = data[0];
		}
		else if(data.length >= 2) {
			mod = data[0];
			item = data[1];
			if(data.length == 3) {
				meta = Integer.valueOf(data[2]);
			}
		}
		Item found = null;
		// prevent unnecessary lookup
		if(data.length >= 1)
			found = GameRegistry.findItem(mod, item);
		
		if(found == null) {
			System.out.println("Unable to parse icon \""+key+"\" ("+mod+":"+item+":"+meta+")"); 
			found = fallback;
			property.setToDefault();
		}
		return new Object[] {found, meta};
	}
}
