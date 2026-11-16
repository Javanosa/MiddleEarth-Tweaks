package metweaks.farview;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkIteratorFastUtil extends ChunkIterator {
	
	public static final LongHashMap getMapInstance() {
		return new FastUtilLongHashMap();
	}

	private final FastUtilLongHashMap chunkMap;
	
	public ChunkIteratorFastUtil(LongHashMap map) {
		chunkMap = (FastUtilLongHashMap) map;
		
	}
	
	@Override
	public void iterateTick(final METLongHashSet unloadable, final long time) {
		final ObjectSet<Entry<Object>> map = chunkMap.long2ObjectEntrySet();
		
		final boolean check = METChunkProviderClient.farViewEnabled && !METChunkProviderClient.tickUnloadable;
		
		for(final Entry<Object> entry : map) {
			if(!check || !unloadable.contains(entry.getLongKey())) {
				((Chunk) entry.getValue()).func_150804_b(System.currentTimeMillis() - time > 5L);
			}
		}
	}

	@Override
	public void iterateLastWorld(World world, METLongHashSet unloadable) {
		final ObjectSet<Entry<Object>> map = chunkMap.long2ObjectEntrySet();
		
		for(final Entry<Object> entry : map) {
			processLastWorldChunk((Chunk) entry.getValue(), world);
			
			unloadable.add(entry.getLongKey());
		}
	}
	
}
