package metweaks.block;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.map.TObjectIntMap;
import lotr.common.LOTRDimension;
import lotr.common.LOTRMod;
import lotr.common.item.LOTRItemMug;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.biome.LOTRBiomeDecorator;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.core.Hooks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.world.gen.feature.WorldGenMinable;

public class BlocksItemsInit {
	public static void disableTinCopperSilverGen() {
		try {
			Field field_biomeOres = ReflectionHelper.findField(LOTRBiomeDecorator.class, "biomeOres");
			Field field_oreGen = ReflectionHelper.findField(Class.forName("lotr.common.world.biome.LOTRBiomeDecorator$OreGenerant"), "oreGen");
			Field field_block = ReflectionHelper.findField(WorldGenMinable.class, "field_150519_a");
			
				for(LOTRBiome biome : LOTRDimension.MIDDLE_EARTH.biomeList) {
					if(biome != null) {
						@SuppressWarnings("rawtypes")
						Iterator ores = ((List) field_biomeOres.get(biome.decorator)).iterator();
						while(ores.hasNext()) {
							Block block = (Block) field_block.get(field_oreGen.get(ores.next()));
							if(block == LOTRMod.oreCopper || block == LOTRMod.oreTin || block == LOTRMod.oreSilver) {
								
								ores.remove();
							}
						}
					}
				}
			}
			catch(Exception ec) {
				ec.printStackTrace();
			}
	}
	
	public static void improveBlockBreakSpeeds() {
		String tool = "axe";
		Blocks.melon_block.setHarvestLevel(tool, 0);
		tool = "hoe";
		Blocks.hay_block.setHarvestLevel(tool, 0);
		if(MeTweaks.lotr) {
			LOTRMod.thatch.setHarvestLevel(tool, 0);
			LOTRMod.stairsThatch.setHarvestLevel(tool, 0);
			LOTRMod.slabDoubleThatch.setHarvestLevel(tool, 0);
			LOTRMod.slabSingleThatch.setHarvestLevel(tool, 0);
			LOTRMod.daub.setHarvestLevel(tool, 0);
			LOTRMod.stairsReed.setHarvestLevel(tool, 0);
			LOTRMod.reedBars.setHarvestLevel(tool, 0);
		}
	}
	
	public static void foodConsumeDurations() {
		TObjectIntMap<Item> durations = Hooks.consumeDurations;
		int half = 16;
		
		durations.put(Items.cookie, half);
		durations.put(Items.melon, half);
		
		if(MeTweaks.lotr) {
			durations.put(LOTRMod.blackberry, half);
			durations.put(LOTRMod.blueberry, half);
			durations.put(LOTRMod.cranberry, half);
			durations.put(LOTRMod.elderberry, half);
			durations.put(LOTRMod.raspberry, half);
			durations.put(LOTRMod.banana, half);
			durations.put(LOTRMod.cherry, half);
			durations.put(LOTRMod.date, half);
			durations.put(LOTRMod.chestnutRoast, half);
			durations.put(LOTRMod.lemon, half);
			durations.put(LOTRMod.lime, half);
			durations.put(LOTRMod.leek, half);
			durations.put(LOTRMod.almond, half);
			durations.put(LOTRMod.grapeRed, half);
			durations.put(LOTRMod.grapeWhite, half);
			durations.put(LOTRMod.raisins, half);
			durations.put(LOTRMod.olive, half);
		}
	}
	
	public static void draughtMaxStacksize() {
		int draughtMaxStacksize = ASMConfig.draughtMaxStacksize;
		
		Iterator<Item> it = GameData.getItemRegistry().iterator();
		
		while(it.hasNext()) {
			Item item = it.next();
			if(item instanceof LOTRItemMug && ((LOTRItemMug) item).isFullMug) {
				item.setMaxStackSize(draughtMaxStacksize);
			}
		}
		
		LOTRMod.entDraught.setMaxStackSize(draughtMaxStacksize);
		
	}
}
