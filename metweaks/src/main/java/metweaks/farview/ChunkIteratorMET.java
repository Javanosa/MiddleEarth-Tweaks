package metweaks.farview;

import metweaks.farview.METLongHashMap.Bucket;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class ChunkIteratorMET extends ChunkIterator {

	private final METLongHashMap chunkMap;
	
	public ChunkIteratorMET(LongHashMap map) {
		chunkMap = (METLongHashMap) map;
		
	}
	
	@Override
	public void iterateTick(final METLongHashSet unloadable, final long time) {
		final boolean check = METChunkProviderClient.farViewEnabled && !METChunkProviderClient.tickUnloadable;
        
        final Bucket[] table = ((METLongHashMap) this.chunkMap).hashArray;
        
        for(Bucket item : table) {
        	while(item != null) {
        		if(!check || !unloadable.contains(item.key)) {
        			((Chunk) item.value).func_150804_b(System.currentTimeMillis() - time > 5L);
        		}
        		item = item.nextEntry;
        	}
        }
	}

	@Override
	public void iterateLastWorld(World world, METLongHashSet unloadable) {
		for(METLongHashMap.Bucket section : chunkMap.hashArray) {
        	for(METLongHashMap.Bucket entry = section; entry != null; entry = entry.nextEntry)
            {
        		processLastWorldChunk((Chunk) entry.getValue(), world);
        		
        		unloadable.add(entry.getKey());
            }
        }
	}
	
}
