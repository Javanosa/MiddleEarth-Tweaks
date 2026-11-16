package metweaks.farview;

import java.util.List;

import metweaks.MeTweaks;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public abstract class ChunkIterator {
	
	public abstract void iterateTick(METLongHashSet unloadable, long time);
	
	public abstract void iterateLastWorld(World world, METLongHashSet unloadable);
	
	@SuppressWarnings("unchecked")
	public void processLastWorldChunk(Chunk chunk, World world) {
		chunk.worldObj = world; // assign new world to prevent memory leak
		
		for(List<Entity> list : chunk.entityLists) {
			
			list.clear();
			//world.unloadEntities(list);
			
		}
		
		if(METChunkProviderClient.keepTileEntities) {
			TileEntitySyncer.syncTileEntities(chunk, world);
		}
		else {
			chunk.chunkTileEntityMap.clear();
		}
		//chunk.chunkTileEntityMap.clear();
		
		
		
		/*Iterator<TileEntity> iterator = chunk.chunkTileEntityMap.values().iterator();

        while (iterator.hasNext())
        {
            TileEntity tileentity = (TileEntity) iterator.next();
            world.func_147457_a(tileentity);
           
        }*/
		
		chunk.hasEntities = false; // probably not needed
		
		int x = chunk.xPosition;
		int z = chunk.zPosition;
		if(MeTweaks.celeritasVers > 0) {
			if(MeTweaks.celeritasVers == 1) {
				CeleritasCallsOld.loadChunk(world, x, z);
			}
			else {
				CeleritasCalls.loadChunk(world, x, z);
			}
        	
        }
        
	}
}
