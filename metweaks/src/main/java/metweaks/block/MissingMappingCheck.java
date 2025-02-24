package metweaks.block;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.Type;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class MissingMappingCheck {
	public static void check(FMLMissingMappingsEvent event) {
		for(MissingMapping mapping : event.get()) {
			String name = mapping.name;
			int split = name.indexOf(':');
			if(split >= 0) {
				String mod = name.substring(0, split);
				String prefix = name.substring(split+1, split+4); // 3 chars
				
				if(MeTweaks.MODID.equals(mod)) {
					
					// wrong named registry
					if(!MeTweaksConfig.verticalSlabs && prefix.matches("^v(_|\\d).*")) { // matches v_, v2, v3 ... it does not check for begin and fails for any characters after!
						// delayed to show after list
						Executors.newScheduledThreadPool(0).schedule(() -> {
							FMLLog.getLogger().warn(MeTweaks.MODNAME+": You saved this world with VerticalSlabs but disabled them, continue?");
							FMLLog.getLogger().warn(MeTweaks.MODNAME+": You can enable them in config/"+MeTweaks.MODID+".cfg -> VerticalSlabs");
							//proxy.openVerticalSlabsDisabledWarn();
							
						}, 1, TimeUnit.SECONDS);
						
						return;
					}
					else if(prefix.matches("v([1-4])1")) { // handle rename
						String reg = name.substring(split+1);
						
						
						int vIndex = 0;
						try {
							vIndex = Integer.parseInt(prefix.substring(1, 2))+1;
						}
						catch(NumberFormatException e) {
							e.printStackTrace();
							continue;
						}
						String newName = "v" + vIndex + reg.substring(3);
						
						boolean changed = false;
						
						if(mapping.type == Type.BLOCK) {
							Block block = GameRegistry.findBlock(mod, newName);
							if(block != null) {
								mapping.remap(block);
								changed = true;
							}
						}
						else {
							Item item = GameRegistry.findItem(mod, newName);
							if(item != null) {
								mapping.remap(item);
								changed = true;
							}
						}
						
						if(changed)
							FMLLog.getLogger().warn(MeTweaks.MODNAME+": changed "+mapping.type.name()+" "+reg+" to "+newName);
						
					}
					
				}
			}
			
			
		}
	}
}
