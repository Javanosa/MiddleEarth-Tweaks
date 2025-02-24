package metweaks.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IShearable;

public class VerticalSlabShearable extends VerticalSlab implements IShearable {

	public VerticalSlabShearable(Block block, int subtypes, int index) {
		super(block, subtypes, index);
	}
	
	public VerticalSlab makeInstance(Block block, int index) {
		return new VerticalSlabShearable(block, 0, index);
	}
	
	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
		if(slab instanceof IShearable) {
			((IShearable) slab).isShearable(item, world, x, y, z);
		}
		return false;
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		if(slab instanceof IShearable) {
			((IShearable) slab).onSheared(item, world, x, y, z, fortune);
		}
		return new ArrayList<>(0);
	}

}
