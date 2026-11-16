package metweaks.block;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.Type;
import cpw.mods.fml.relauncher.ReflectionHelper;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.core.Hooks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class MissingMappingCheck {
	public static String changeName(MissingMapping mapping, String oldName, String newName) {
		boolean changed = false;
		
		if(mapping.type == Type.BLOCK) {
			Block block = GameRegistry.findBlock(MeTweaks.MODID, newName);
			if(block != null) {
				mapping.remap(block);
				changed = true;
			}
		}
		else {
			Item item = GameRegistry.findItem(MeTweaks.MODID, newName);
			if(item != null) {
				mapping.remap(item);
				changed = true;
			}
		}
		
		if(changed) {
			FMLLog.getLogger().warn(MeTweaks.MODNAME+": changed "+mapping.type.name()+" "+oldName+" to "+newName);
		}
		return newName;
	}
	
	private static Field field_aliases;
	private static Field field_itemRegistry;
	private static Field field_blockRegistry;
	
	public static void check(FMLMissingMappingsEvent event) {
		
		
		
		
		final GameData tempGameData = Hooks.temp_gameData;
		Hooks.temp_gameData = null;
		
		for(MissingMapping mapping : event.get()) {
			String name = mapping.name;
			int split = name.indexOf(':');
			if(split >= 0 && name.length() >= (split + 4)) {
				String prefix = name.substring(split+1, split+4); // 3 chars
				
				
					if(MeTweaksConfig.verticalSlabs) {
						String oldName = name.substring(split+1);
						String reg = oldName;
						
						if(prefix.matches("v([1-4])1")) { // handle rename // fix that i did stringwise +1 instead of mathematical +1 in index
							
							
							int vIndex = 0;
							try {
								vIndex = Integer.parseInt(prefix.substring(1, 2))+1;
							}
							catch(NumberFormatException e) {
								e.printStackTrace();
								continue;
							}
							String newName = "v" + vIndex + reg.substring(3);
							
							reg = changeName(mapping, reg, newName);
							
							//reg = newName; // in order to not break the next code part in case
						}
						
						if(VerticalSlab.localizedToRegistry.containsKey(reg)) {
							reg = changeName(mapping, reg, VerticalSlab.localizedToRegistry.get(reg));
							
							
						}
						
						if(VerticalSlab.MET_To_NoMET.containsKey(reg)) {
							String fullMETname = MeTweaks.MODID+":"+reg;
							reg = changeName(mapping, reg, VerticalSlab.MET_To_NoMET.get(reg));
							
							// remove invalid aliases
							
							if(tempGameData != null) {
							
								if(field_aliases == null) {
									field_aliases = ReflectionHelper.findField(FMLControlledNamespacedRegistry.class, "aliases");
									field_itemRegistry = ReflectionHelper.findField(GameData.class, "iBlockRegistry");
									field_blockRegistry = ReflectionHelper.findField(GameData.class, "iItemRegistry");
								}
									
								try {
									final Object instance = (mapping.type == Type.BLOCK ? field_blockRegistry : field_itemRegistry).get(tempGameData);
									
									@SuppressWarnings("unchecked")
									final Map<String, String> aliases = (Map<String, String>) field_aliases.get(instance);
									
									String searchFor = MeTweaks.MODID+":"+reg;
									String value = aliases.get(searchFor);
									
									if(value != null && value.equals(fullMETname)) {
										aliases.remove(searchFor);
										FMLLog.getLogger().warn(MeTweaks.MODNAME+": removed "+mapping.type.name()+"-Alias K: "+searchFor+" V: "+value);
									}
									
								}
								catch(Exception e) {
									e.printStackTrace();
								}
							}
							else {
								FMLLog.getLogger().error(MeTweaks.MODNAME+": GameData is null");
							}
						}
						
						
						
					}
					else if(prefix.matches("^v(_|\\d).*")) { // matches v_, v2, v3 ... it does not check for begin and fails for any characters after!
						// delayed to show after list
						Executors.newScheduledThreadPool(0).schedule(() -> {
							FMLLog.getLogger().warn(MeTweaks.MODNAME+": You saved this world with VerticalSlabs but disabled them, continue?");
							FMLLog.getLogger().warn(MeTweaks.MODNAME+": You can enable them in config/"+MeTweaks.MODID+".cfg -> VerticalSlabs");
							//proxy.openVerticalSlabsDisabledWarn();
							
						}, 1, TimeUnit.SECONDS);
						
						return;
					}
				
			}
			
			
		}
	}
}
