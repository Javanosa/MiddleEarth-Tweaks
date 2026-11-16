package metweaks.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class MeTweaksWall_Item extends ItemBlock {
	
	public MeTweaksWall wall;

	public MeTweaksWall_Item(Block block) {
		super(block);
		wall = (MeTweaksWall) block;
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(int meta) {
        return meta;
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName() + "." + stack.getItemDamage();
    }

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		final int originmeta = stack.getItemDamage();
		int meta = originmeta;
		
		meta = meta + 12;
		
		stack.setItemDamage(meta);
		String key = Item.getItemFromBlock(wall.fullBlock).getItemStackDisplayName(stack);
		stack.setItemDamage(originmeta);
        return (StatCollector.translateToLocal(key) + StatCollector.translateToLocal("metweaks.wallprefix")).trim();
    }
}
