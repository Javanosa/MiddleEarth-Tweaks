package metweaks.farview;

import java.lang.reflect.Field;

import metweaks.MeTweaks;
import net.minecraft.util.LongHashMap;

public class FastCraftInfo {
	private static Field field_internaldata;
	public static boolean hasFastcraft;
	public static boolean version125;
	
	public static boolean useFastCraft = true;

	@SuppressWarnings("rawtypes")
	public static final Object getInternData(final LongHashMap map) {
		Object instance = null;
		try {
			if(field_internaldata == null) {
				Class internalDataClazz = Class.forName(version125 ? "fastcraft.aV" : "fastcraft.aU");
				Class mapClazz = Class.forName("fastcraft.X");
				String internaldataFieldName = "a";
				for(Field field : mapClazz.getDeclaredFields()) {
					if(internaldataFieldName.equals(field.getName()) && internalDataClazz.equals(field.getType())) {
						field.setAccessible(true);
						field_internaldata = field;
						break;
					}
				}
			}
			
			instance = field_internaldata.get(map);
			
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(instance == null) {
			throw new RuntimeException("Could'nt find FastCraft Field internaldata in fastcraft.FastLongHashMap");
		}
		return instance;
	}
	
	public static final ChunkIterator getChunkIterator(final LongHashMap chunkMap) {
		return hasFastcraft && useFastCraft ? new ChunkIteratorFC(chunkMap) : MeTweaks.hodgepodge ? new ChunkIteratorFastUtil(chunkMap) : new ChunkIteratorMET(chunkMap);
	}
}
