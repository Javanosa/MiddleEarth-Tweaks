package metweaks.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;

public class VerticalSlabFalling extends VerticalSlab {
	
	public VerticalSlabFalling(Block block, int subtypes, int index) {
		super(block, subtypes, index);
	}

	public VerticalSlab makeInstance(Block block, int index) {
		return new VerticalSlabFalling(block, 0, index);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
	}
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		if(!world.isRemote) tryBlockFall(world, x, y, z); 
	}
	
	private void tryBlockFall(World world, int x, int y, int z) {
		if(BlockFalling.func_149831_e(world, x, y - 1, z) && y >= 0) {
			int range = 32;
			if(!BlockFalling.fallInstantly && world.checkChunksExist(x - range, y - range, z - range, x + range, y + range, z + range)) {
				if(!world.isRemote) {
					EntityFallingBlock fallingBlock = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, this, world.getBlockMetadata(x, y, z));
					world.spawnEntityInWorld(fallingBlock);
				} 
			}
			else {
				world.setBlockToAir(x, y, z);
				
				while(BlockFalling.func_149831_e(world, x, y - 1, z) && y > 0)
					y--; 
				if(y > 0) world.setBlock(x, y, z, this); 
			} 
		} 
	}
	
	@Override
	public int tickRate(World world) {
		return 2;
	}
}
