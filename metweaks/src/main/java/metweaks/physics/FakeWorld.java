package metweaks.physics;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public final class FakeWorld implements IBlockAccess {
	
	
	public static int fallingBlockX;
	public static int fallingBlockY;
	public static int fallingBlockZ;
	
	public static int fallingBlockMeta;
	public static Block fallingBlockBlock;
	
	
	public static World world;
	
	// cache
	private static int cached_light = -1;
	public static boolean isFire;

	public static final boolean isFallingPos(final int x, final int y, final int z) {
		if(fallingBlockX == x && fallingBlockY == y && fallingBlockZ == z) {
			return true;
		}
		return false;
	}
	
	public static final void reset() {
		world = null;
		fallingBlockBlock = null;
		cached_light = -1;
	}

	@Override
	public final Block getBlock(final int x, final int y, final int z) {
		if(isFallingPos(x, y, z)) {
			return fallingBlockBlock;
		}
		if(isFire && fallingBlockY > y) {
			return Blocks.stone;
		}
		return Blocks.air;
		
		
	}

	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		return world.getTileEntity(x, y, z);
	}

	@Override
	public final int getLightBrightnessForSkyBlocks(final int x, final int y, final int z, final int self) {
		if(cached_light != -1) {
			return cached_light;
		}
		return cached_light = world.getLightBrightnessForSkyBlocks(fallingBlockX, fallingBlockY, fallingBlockZ, self);
	}
	
	

	@Override
	public final int getBlockMetadata(final int x, final int y, final int z) {
		return fallingBlockMeta;
	}

	@Override
	public int isBlockProvidingPowerTo(int p_72879_1_, int p_72879_2_, int p_72879_3_, int p_72879_4_) {
		return world.isBlockProvidingPowerTo(p_72879_1_, p_72879_2_, p_72879_3_, p_72879_4_);
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		if(isFallingPos(x, y, z)) {
			return fallingBlockBlock.isAir(this, x, y, z);
		}
		if(fallingBlockBlock == Blocks.fire && isFallingPos(x, y+1, z)) {
			return false;
		}
		return true;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return world.getBiomeGenForCoords(x, z);
	}

	@Override
	public int getHeight() {
		return world.getHeight();
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return world.extendedLevelsInChunkCache();
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
		return world.isSideSolid(x, y, z, side, _default);
	}

}
