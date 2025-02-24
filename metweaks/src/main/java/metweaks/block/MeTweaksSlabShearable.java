package metweaks.block;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IShearable;

public class MeTweaksSlabShearable extends MeTweaksSlab implements IShearable{

	public MeTweaksSlabShearable(int typesTotal, int renderMetaOff, int index, Block full) {
		super(typesTotal, renderMetaOff, index, full);
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
		return new ArrayList<>(0);
	}

}
