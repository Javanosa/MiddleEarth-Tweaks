package metweaks.features;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRDimension;
import lotr.common.LOTRLevelData;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.spawning.LOTRBiomeInvasionSpawns;
import lotr.common.world.spawning.LOTREventSpawner;
import lotr.common.world.spawning.LOTREventSpawner.EventChance;
import lotr.common.world.spawning.LOTRInvasions;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

@SuppressWarnings("rawtypes")
public class LOTREventControl {
	
	public static Map<LOTRBiome, EventData> table = new HashMap<>();
	
	public static class EventData {
		// changes
		public List<LOTRInvasions> invasion_removes;
		public Map<EventChance, List<LOTRInvasions>> invasion_overrides;
		public List<Class> trader_removes;
		
		public List<Class> trader_overrides;
		public EventChance banditchance;
		
		// defaults
		public final Map<EventChance, List<LOTRInvasions>> default_invasionsByChance;
		public final List<LOTRInvasions> default_registeredInvasions;
		public final List<Class> default_traders;
		public final EventChance default_banditchance;
		
		// reference
		public Map<EventChance, List<LOTRInvasions>> invasionsByChance;
		public List<LOTRInvasions> registeredInvasions;
		public List<Class> traders;
		
		public EventData(Map<EventChance, List<LOTRInvasions>> def_byChance, List<LOTRInvasions> def_invasions, List<Class> def_traders, EventChance def_banditchance) {
			default_invasionsByChance = def_byChance;
			default_registeredInvasions = def_invasions;
			default_traders = def_traders;
			default_banditchance = def_banditchance;
		}
	}
	
	public static final Map<String, LOTRBiome> nameToBiome = getBiomeMap();
	
	public static Map<String, LOTRBiome> getBiomeMap() {
		Map<String, LOTRBiome> map = new HashMap<>();
		
		for(LOTRDimension dim : LOTRDimension.values()) {
			for(LOTRBiome biome : dim.biomeList) {
				if(biome == null) continue;
					
				map.put(biome.biomeName, biome);
			}
		}
		return map;
	}
	
	public static LOTRInvasions invasionFor(String name) {
		for(LOTRInvasions invasion : LOTRInvasions.values()) {
			if(invasion.name().equalsIgnoreCase(name)) {
				return invasion;
			}
		}
		return null;
	}

	public static EventChance chanceFor(String name) {
		for(EventChance chance : EventChance.values()) {
			if(chance.name().equalsIgnoreCase(name)) {
				return chance;
			}
		}
		return null;
	}
	
	public static Map<String, LOTRInvasions> invasion_lookup;
	
	public static LOTRInvasions invasionOf(String name) {
		return invasion_lookup.get(name);
	}
	
	public static Map<String, EventChance> chance_lookup;
	
	public static EventChance chanceOf(String name) {
		return chance_lookup.get(name);
	}
	
	
	public static final String[] strBiomes = getBiomeNames();
	
	public static final Map<String, String> biomeAliases = new HashMap<>();
	
	public static LOTRBiome getBiomeWithAlias(String arg) {
		if(biomeAliases.containsKey(arg)) {
			arg = biomeAliases.get(arg);
		}
		return nameToBiome.get(arg);
	}
	
	static {
		biomeAliases.put("northlands", "tundra");
		biomeAliases.put("northernForests", "taiga");
	}
	
	public static final String[] strChances = LOTREventControl.getUniqueChances();
	
	public static String[] getBiomeNames() {
		Set<String> set = nameToBiome.keySet();
		int size = set.size();
		String[] all = new String[size+2];
		set.toArray(all);
		all[size] = "northlands";
		all[++size] = "northernForests";
		
		return all;
	}
	
	public static final String name = "LOTREvents.dat";
	
	private static Field field_banditChance;
	
	public static void setBanditChance(LOTRBiome biome, EventChance chance) {
		if(field_banditChance == null)
			field_banditChance = ReflectionHelper.findField(LOTRBiome.class, "banditChance");
		
		try {
			field_banditChance.set(biome, chance);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		EventData data = table.get(biome);
		data.banditchance = data.default_banditchance == chance ? null : chance;
	}
	
	
	
	public static boolean addTrader(LOTRBiome biome, Class trader, boolean checkDefault) {
		EventData data = table.get(biome);
		List<Class> traders = data.traders;
		
		if(traders.contains(trader)) {
			return false;
		}
		
		
		traders.add(trader);
		LOTREventSpawner.createTraderSpawner(trader);
		
		if(!checkDefault || !data.default_traders.contains(trader)) {
			if(data.trader_overrides == null)
				data.trader_overrides = new ArrayList<>();
			
			data.trader_overrides.add(trader);
		}
		
		List<Class> removes = data.trader_removes;
		if(removes != null) {
			removes.remove(trader);
			if(removes.isEmpty()) {
				data.trader_removes = null;
			}
		}
		return true;
		
	}
	
	public static boolean removeTrader(LOTRBiome biome, Class trader, boolean checkDefault) {
		EventData data = table.get(biome);
		if(!data.traders.remove(trader)) {
			return false;
		}
		if(!checkDefault || data.default_traders.contains(trader)) {
			
			if(data.trader_removes == null)
				data.trader_removes = new ArrayList<>();
			
			data.trader_removes.add(trader);
		}
		
		List<Class> adds = data.trader_overrides;
		if(adds != null) {
			adds.remove(trader);
			if(adds.isEmpty()) {
				data.trader_overrides = null;
			}
		}
		return true;
	}
	
	
	
	// add config option Enable Invasion Control and Load Default Preset (Makes invasions more widespread and overall a bit more common
	// add asm config option to disable lotrinvasions command
	
	// on new world load: do nothing ? how do we check for new world?
	// on world load with lotrinvasions.dat missing - do nothing
	// on world load with lotrinvasions.dat there - init file, read file, if overall empty stop
	// 		if not empty init defaults, init references
	
	// Removes ->
	
	public static boolean needsSave;
	
	private static File getEventsDat() {
	    return new File(LOTRLevelData.getOrCreateLOTRDir(), name);
	}
	
	public static boolean removeInvasion(LOTRBiome biome, LOTRInvasions invasion, boolean partial, boolean checkDefault) {
		EventData data = table.get(biome);
		// remove from byChance
		/*for(Entry<EventChance, List<LOTRInvasions>> entry : data.invasionsByChance.entrySet()) {
			List<LOTRInvasions> list = entry.getValue();
			if(list.contains(invasion)) {
				list.remove(invasion);
				
				if(list.isEmpty()) {
					data.invasionsByChance.remove(entry.getKey());
				}
			}
		}*/
		
		for(EventChance chance : data.invasionsByChance.keySet()) {
			if(data.invasionsByChance.get(chance).remove(invasion));
			
			if(data.invasionsByChance.get(chance).isEmpty()) {
				//data.invasionsByChance.remove(chance);
			}
				
		}
		
		
			
		

		if(data.invasion_overrides != null) {
			for(Entry<EventChance, List<LOTRInvasions>> entry : data.invasion_overrides.entrySet()) {
				List<LOTRInvasions> list = entry.getValue();
				if(list.contains(invasion)) {
					list.remove(invasion);
					
					if(list.isEmpty()) {
						data.invasion_overrides.remove(entry.getKey());
					}
				}
			}
			
			if(data.invasion_overrides.isEmpty())
				data.invasion_overrides = null;
		}
		
		if(partial) return false;
		
		// remove from list
		boolean result = data.registeredInvasions.remove(invasion);
		
		// it can be removed
		if(!checkDefault || data.default_registeredInvasions.contains(invasion)) {
			if(data.invasion_removes == null) {
				data.invasion_removes = new ArrayList<>();
			}
			
			if(!data.invasion_removes.contains(invasion))
				data.invasion_removes.add(invasion);
		}
		
		return result;
	}
	
	// 0 = back to default, 1 = Add, 2 = Change Chance
	public static int addOrSetInvasion(LOTRBiome biome, LOTRInvasions invasion, EventChance chance, boolean checkDefault) {
		
		EventData data = table.get(biome);
		
		int result = 0;
		
		Map<EventChance, List<LOTRInvasions>> byChance = data.invasionsByChance;
		
		boolean isDefault = false;
		// if its default we remove once and that readd without adding to the change tracking
		if(checkDefault && data.default_invasionsByChance.containsKey(chance) && data.default_invasionsByChance.get(chance).contains(invasion)) {
			isDefault = true;
		}

		if(data.registeredInvasions.contains(invasion)) {
			result = 2;
			removeInvasion(biome, invasion, true, true); // remove from byChance, remove overrides
		}
		else {
			// add
			data.registeredInvasions.add(invasion);
			result = 1;
		}

		 if(!isDefault) {
			 Map<EventChance, List<LOTRInvasions>> overrides = data.invasion_overrides;
			 if(overrides == null) {
				 overrides = data.invasion_overrides = new HashMap<>();
			 }
			 
			 if(!overrides.containsKey(chance)) {
				 overrides.put(chance, new ArrayList<>());
			 }
			 
			 overrides.get(chance).add(invasion);
			 
			 
		 }
		 
		 // remove removal entry
		 if(data.invasion_removes != null) {
			 data.invasion_removes.remove(invasion);
			 
			 if(data.invasion_removes.isEmpty())
				 data.invasion_removes = null;
		 }
		 
		 // actually add
		 
		 if(!byChance.containsKey(chance)) {
			 byChance.put(chance, new ArrayList<>());
		 }
		 
		 byChance.get(chance).add(invasion);
			 
		
		return isDefault ? 0 : result;
	}
	
	
	
	public static Map<String, LOTRBiome> getValidLotrBiomes() {
		Map<String, LOTRBiome> list = new HashMap<>();
		
		for(LOTRDimension lotrdim : LOTRDimension.values()) {
			for(LOTRBiome lotrbiome : lotrdim.biomeList) {
				if(lotrbiome != null) {
					list.put(lotrbiome.biomeName, lotrbiome);
				}
			}
		}
		
		return list;
	}
	
	public static String[] getUniqueChances() {
		return new String[] {
			// average factor of occurence per hour
			// more entries mean more occurence, so 2 invasions of COMMON is essentially 1.8 factor per hour to spawn one of the invasions
			"NEVER", 			// 0.0
			"RARE",				// 0.1
			"UNCOMMON",			// 0.3
			"BANDIT_COMMON", 	// 0.8
			"COMMON"			// 0.9
		};
	}
	
	@SuppressWarnings("unchecked")
	public static void initializeDefaults() {
		invasion_lookup = new HashMap<>();
		
		for(LOTRInvasions invasion : LOTRInvasions.values()) {
			invasion_lookup.put(invasion.name(), invasion);
		}
		
		chance_lookup = new HashMap<>();
		
		for(EventChance chance : EventChance.values()) {
			chance_lookup.put(chance.name(), chance);
		}
		
		Field f_invasions = ReflectionHelper.findField(LOTRBiomeInvasionSpawns.class, "registeredInvasions");
		Field f_byChance = ReflectionHelper.findField(LOTRBiomeInvasionSpawns.class, "invasionsByChance");
		Field f_traders = ReflectionHelper.findField(LOTRBiome.class, "spawnableTraders");
		
		List<LOTRInvasions> invasions = null;
		Map<EventChance, List<LOTRInvasions>> byChance = null;
		List<Class> traders = null;
		
		for(LOTRBiome biome : nameToBiome.values()) {
			try {
				invasions = (List<LOTRInvasions>) f_invasions.get(biome.invasionSpawns);
				byChance = (Map<EventChance, List<LOTRInvasions>>) f_byChance.get(biome.invasionSpawns);
				traders = (List<Class>) f_traders.get(biome);
			} 
			catch (Exception e) {
				e.printStackTrace(); 
				continue;
			}

			if(invasions != null && byChance != null) {
				EventData data = new EventData(cloneMap(byChance), cloneList(invasions), cloneList(traders), biome.getBanditChance());
				// access because its private
				data.invasionsByChance = byChance;
				data.registeredInvasions = invasions;
				data.traders = traders;
				table.put(biome, data);
			}
			else {
				FMLLog.severe("Biome '"+biome.biomeName+"' is missing data: "+invasions+" "+byChance+" "+traders);
			}
		}
		
		
	}
	
	public static Map<EventChance, List<LOTRInvasions>> cloneMap(Map<EventChance, List<LOTRInvasions>> byChanceIn) {
		
		Map<EventChance, List<LOTRInvasions>> byChance = new HashMap<>(byChanceIn.size());
		
		for(Entry<EventChance, List<LOTRInvasions>> entry : byChanceIn.entrySet()) {
			byChance.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}
		
		return byChance;
	}
	
	public static <T> List<T> cloneList(List<T> invasions) {
		return new ArrayList<>(invasions);
	}
	
	public static void setDefaults() {
		for(Entry<LOTRBiome, EventData> entry : table.entrySet()) {
			EventData data = entry.getValue();
			if(data.invasion_overrides != null || data.invasion_removes != null) {
				List<LOTRInvasions> newList = cloneList(data.default_registeredInvasions);
				List<LOTRInvasions> list = data.registeredInvasions;
				list.clear();
				list.addAll(newList);
				
	
				Map<EventChance, List<LOTRInvasions>> newMap = cloneMap(data.default_invasionsByChance);
				Map<EventChance, List<LOTRInvasions>> map = data.invasionsByChance;
				map.clear();
				map.putAll(newMap);
				
				data.invasion_overrides = null;
				data.invasion_removes = null;
			}
			
			if(data.banditchance != null) {
				setBanditChance(entry.getKey(), data.default_banditchance);
				
				data.banditchance = null;
			}
			
			if(data.trader_overrides != null || data.trader_removes != null) {
				List<Class> newList = cloneList(data.default_traders);
				List<Class> list = data.traders;
				list.clear();
				list.addAll(newList);
				
				data.trader_overrides = null;
				data.trader_removes = null;
			}
		}
		
		
	}
	
	public static void markDirty() {
		needsSave = true;
	}
	
	public static void save() {
		
		if(needsSave) {
			needsSave = false;
			NBTTagCompound nbt = new NBTTagCompound();
			
			for(Entry<LOTRBiome, EventData> pair : table.entrySet()) {
				EventData data = pair.getValue();
				NBTTagCompound biometag = new NBTTagCompound();
				
				if(data.invasion_removes != null) {
					NBTTagList tagList = new NBTTagList();
					for(LOTRInvasions invasion : data.invasion_removes) {
						tagList.appendTag(new NBTTagString(invasion.name()));
					}
					
					biometag.setTag("InvasionRemove", tagList);
				}
				
				
				
				if(data.invasion_overrides != null) {
					NBTTagCompound tagList = new NBTTagCompound();
					for(Entry<EventChance, List<LOTRInvasions>> entry : data.invasion_overrides.entrySet()) {
						NBTTagList invasions = new NBTTagList();
						//System.out.println(entry.getKey().name());
						for(LOTRInvasions invasion : entry.getValue()) {
							//System.out.println(invasion.name());
							invasions.appendTag(new NBTTagString(invasion.name()));
						}
						
						tagList.setTag(entry.getKey().name(), invasions);
					}
					biometag.setTag("Invasion", tagList);
				}
				
				if(data.trader_overrides != null) {
					NBTTagList tagList = new NBTTagList();
					for(Class trader : data.trader_overrides) {
						String name = (String) EntityList.classToStringMapping.get(trader);
						if(name != null)
							tagList.appendTag(new NBTTagString(name));
					}
					
					biometag.setTag("Trader", tagList);
				}
				
				if(data.trader_removes != null) {
					NBTTagList tagList = new NBTTagList();
					for(Class trader : data.trader_removes) {
						String name = (String) EntityList.classToStringMapping.get(trader);
						if(name != null)
							tagList.appendTag(new NBTTagString(name));
					}
					
					biometag.setTag("TraderRemove", tagList);
				}
				
				if(data.banditchance != null) {
					biometag.setString("Bandit", data.banditchance.name());
				}
				
				if(!biometag.hasNoTags())
					nbt.setTag(pair.getKey().biomeName, biometag);
			}
			
			//if(nbt.hasNoTags()) {
			//	getEventsDat().delete();
			//}
			//else {
				try {
			    	LOTRLevelData.saveNBTToFile(getEventsDat(), nbt);
				} catch (IOException e) {
					FMLLog.severe("Error saving "+name);
					e.printStackTrace();
				}
			//}
		}
		
		// server doesnt need to reset
		// if no change data was present, dont reset either
		//if(!MinecraftServer.getServer().isDedicatedServer()) {
			// reset to defaults
			// setDefaults();
			
		//}
		
		
		
		
		
	    
	    
	    
	   
	   
	    
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void load() {
		
		File file = getEventsDat();
		if(!file.exists()) return; // nothing to load
		
		NBTTagCompound nbt;
		
		try{
			FileInputStream input = new FileInputStream(file);
		    nbt = CompressedStreamTools.readCompressed(input);
		    input.close();
		}
		catch (IOException e) {
			FMLLog.severe("Error loading "+name);
	        e.printStackTrace();
	        return;
		}
		
		Set<String> keys = nbt.func_150296_c(); // getTags()
		for(String key : keys) {
			
			LOTRBiome biome = nameToBiome.get(key);
			if(biome != null) {
				
				NBTTagCompound biometag = (NBTTagCompound) nbt.getTag(key);
				
				if(biometag.hasKey("InvasionRemove")) {
					NBTTagList invasions = biometag.getTagList("InvasionRemove", 8); // stringlist
					for(int i = 0; i < invasions.tagCount(); i++) {
						LOTRInvasions invasion = invasionOf(invasions.getStringTagAt(i));
						if(invasion != null) {
							removeInvasion(biome, invasion, false, false);
						}
						
					}
				}
				
				if(biometag.hasKey("Invasion")) {
					NBTTagCompound invasions = (NBTTagCompound) biometag.getTag("Invasion");
					
					Set<String> chances = invasions.func_150296_c(); // getTags()
					for(String str_chance : chances) {
						
						EventChance chance = chanceOf(str_chance);
						
        	    		if(chance != null) {
	    	    			NBTTagList invasionsBC = invasions.getTagList(str_chance, 8); // stringlist
	    	    			
	    	    			for(int i = 0; i < invasionsBC.tagCount(); i++) {
	    	    	    		LOTRInvasions invasion = invasionOf(invasionsBC.getStringTagAt(i));
	    	    	    		
	    	    	    		if(invasion != null) {
	    	    	    			
	    	    	    			addOrSetInvasion(biome, invasion, chance, false);
		    	    	    		// add to biome with invasion and chance
	    	    	    		}
	    	    	    	}
        	    		}
					}
				}
				
				if(biometag.hasKey("Trader")) {
					NBTTagList traders = biometag.getTagList("Trader", 8); // stringlist
					for(int i = 0; i < traders.tagCount(); i++) {
						String traderClass = traders.getStringTagAt(i);
						Class clazz = (Class) EntityList.stringToClassMapping.get(traderClass);
						// register traveling trader
						if(clazz != null)
							addTrader(biome, clazz, false);
					}
				}
				
				if(biometag.hasKey("TraderRemove")) {
					NBTTagList traders = biometag.getTagList("TraderRemove", 8); // stringlist
					for(int i = 0; i < traders.tagCount(); i++) {
						String traderClass = traders.getStringTagAt(i);
						Class clazz = (Class) EntityList.stringToClassMapping.get(traderClass);
						// register traveling trader
						if(clazz != null)
							removeTrader(biome, clazz, false);
					}
				}
				
				if(biometag.hasKey("Bandit")) {
					EventChance banditchance = chanceOf(biometag.getString("Bandit"));
					if(banditchance != null) {
						setBanditChance(biome, banditchance);
					}
				}
			}
		}
	}
}
