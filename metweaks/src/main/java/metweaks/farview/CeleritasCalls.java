package metweaks.farview;

import org.embeddedt.embeddium.impl.render.chunk.map.ChunkTrackerHolder;

import net.minecraft.world.World;

public class CeleritasCalls { 
	public static void loadChunk(World world, int x, int z) {
		ChunkTrackerHolder.get(world).onChunkStatusAdded(x, z, 3); 
	}
		  
	public static void unloadChunk(World world, int x, int z) {
		ChunkTrackerHolder.get(world).onChunkStatusRemoved(x, z, 3); 
	}
}
