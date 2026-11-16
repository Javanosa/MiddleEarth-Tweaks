package metweaks.farview;

import net.minecraft.util.LongHashMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public class FastUtilLongHashMap extends LongHashMap {

	private final Long2ObjectMap<Object> map;

	public FastUtilLongHashMap() {
		map = new Long2ObjectOpenHashMap<>();
	}
	
	@Override
	public int getNumHashElements() {
		return map.size();
	}
	
	@Override
	public Object getValueByKey(long key) {
		return map.get(key);
	}
	
	@Override
	public boolean containsItem(long key) {
		return map.containsKey(key);
	}
	
	@Override
	public void add(long key, Object value) {
		map.put(key, value);
	}
	
	@Override
	public Object remove(long key) {
		return map.remove(key);
	}
	
	public final ObjectSet<Entry<Object>> long2ObjectEntrySet() {
		return map.long2ObjectEntrySet();
	}
}