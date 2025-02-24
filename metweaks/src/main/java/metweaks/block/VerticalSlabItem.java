package metweaks.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class VerticalSlabItem extends ItemBlock {
	VerticalSlab vslab;
	
	public VerticalSlabItem(Block block) {
		super(block);
		vslab = (VerticalSlab) block;
		setHasSubtypes(true);
	}
	
	/*public String getUnlocalizedName(ItemStack stack) {
		int meta = (stack.getItemDamage() & 3) + vslab.offset;
		return vslab.slab.getUnlocalizedName()+"."+meta;
    }*/
	
	public String getItemStackDisplayName(ItemStack stack) {
		final int originmeta = stack.getItemDamage();
		int meta = (originmeta & 3) + vslab.offset;
		//String key = vslab.slab.getUnlocalizedName()+"."+meta+".name";
		
		stack.setItemDamage(meta);
		String key = Item.getItemFromBlock(vslab.slab).getItemStackDisplayName(stack);
		stack.setItemDamage(originmeta);
        return (StatCollector.translateToLocal("metweaks.verticalprefix")+StatCollector.translateToLocal(key)).trim();
    }
	
	@Override
	public int getMetadata(int meta) {
        return meta & 3;
    }

}
