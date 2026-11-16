package metweaks.farview;

import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkIteratorFC extends ChunkIterator {

	private final FastCraftHelper helper;
	
	public ChunkIteratorFC(LongHashMap chunkMap) {
		helper = new FastCraftHelper(chunkMap);
		
	}

	@Override
	public void iterateTick(final METLongHashSet unloadable, final long time) {
		
		final long[] keys = helper.getKeys();
		final Object[] values = helper.getValues();
		final long noEntryValue = helper.getNoEntryValue();
		final int capacity = keys.length;
		
		final boolean check = METChunkProviderClient.farViewEnabled && !METChunkProviderClient.tickUnloadable;
		
		
		
		for(int i = 0; i < capacity; i++) {
			if(keys[i] == noEntryValue) continue;
			
			if(!check || !unloadable.contains(keys[i])) {
				//updated++;
				((Chunk) values[i]).func_150804_b(System.currentTimeMillis() - time > 5L);
			}
			
		}
		
		
	}

	@Override
	public void iterateLastWorld(World world, METLongHashSet unloadable) {
		final long[] keys = helper.getKeys();
		final Object[] values = helper.getValues();
		final long noEntryValue = helper.getNoEntryValue();
		final int capacity = keys.length;

		
		
		for(int i = 0; i < capacity; i++) {
			if(keys[i] == noEntryValue) continue;
			
			processLastWorldChunk((Chunk) values[i], world);
			
			unloadable.add(keys[i]);
			
		}
		
		
	}

}
