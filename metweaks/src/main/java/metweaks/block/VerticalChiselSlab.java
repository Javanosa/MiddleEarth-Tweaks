package metweaks.block;

import com.cricketcraft.chisel.api.ICarvable;
import com.cricketcraft.chisel.api.carving.IVariationInfo;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class VerticalChiselSlab extends VerticalSlab implements ICarvable {

	public final ICarvable carvableSlab;
	
	public VerticalChiselSlab(Block block, int subtypes, int index) {
		super(block, subtypes, index);
		carvableSlab = (ICarvable) block;
	}
	
	public VerticalSlab makeInstance(Block block, int index) {
		return new VerticalChiselSlab(block, 0, index);
	}

	@Override
	public IVariationInfo getManager(int arg0) {
		return carvableSlab.getManager(arg0);
	}

	@Override
	public IVariationInfo getManager(IBlockAccess arg0, int arg1, int arg2, int arg3, int arg4) {
		return carvableSlab.getManager(arg0, arg1, arg2, arg3, arg4);
	}

}
