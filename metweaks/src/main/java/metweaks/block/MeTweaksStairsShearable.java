package metweaks.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IShearable;

public class MeTweaksStairsShearable extends MeTweaksStairs implements IShearable {

	public MeTweaksStairsShearable(Block full, int meta, int index, int types) {
		super(full, meta, index, types);
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
		if(fullBlock instanceof IShearable) {
			((IShearable) fullBlock).isShearable(item, world, x, y, z);
		}
		return false;
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		if(fullBlock instanceof IShearable) {
			((IShearable) fullBlock).onSheared(item, world, x, y, z, fortune);
		}
		return new ArrayList<>(0);
	}
}
