package metweaks.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class MeTweaksStairsItem extends ItemBlock {
	
	public MeTweaksStairs stairs;

	public MeTweaksStairsItem(Block block) {
		super(block);
		stairs = (MeTweaksStairs) block;
		setHasSubtypes(true);
	}
	
	@Override
	public int getMetadata(int meta) {
        return meta > 7 ? 8 : 0;
    }
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName() + "." + stack.getItemDamage();
    }

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		final int originmeta = stack.getItemDamage();
		// baseMeta + (meta / 8)	results in meta + 0 or 1
		int meta = stairs.fullMeta + (originmeta >> 3);

		stack.setItemDamage(meta);
		String key = Item.getItemFromBlock(stairs.fullBlock).getItemStackDisplayName(stack);
		stack.setItemDamage(originmeta);
        return (StatCollector.translateToLocal(key) + StatCollector.translateToLocal("metweaks.stairsprefix")).trim();
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta)  {
        return stairs.getIcon(2, meta);
    }
	
	
}
