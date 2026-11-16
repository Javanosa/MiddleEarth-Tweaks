package metweaks.block;

import java.util.List;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MeTweaksWall extends BlockWall {
	
	// fence gates only render in lowered position next to the vanilla cobble wall 
	// currently meta offset is fixed as im only adding the bark walls
	// bark walls are supposed to be called "branches"
	
	public int subtypes;
	public Block fullBlock;

	public MeTweaksWall(Block full, int types) {
		super(full);
		subtypes = types;
		fullBlock = full;
		setCreativeTab(CreativeTabs.tabBlock);
		
		String name = BlocksCore.getRegName("wall_", full, false);
		
		setBlockName(name);
		GameRegistry.registerBlock(this, MeTweaksWall_Item.class, name);
		//System.out.println("Registering..."+name+" "+getIdFromBlock(this)+" > "+GameData.getBlockRegistry().getNameForObject(full));
		BlocksCore.addInfo(name+" "+GameData.getBlockRegistry().getNameForObject(full));
	}
	
	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
	    return fullBlock.isFlammable(world, x, y, z, face);
	}
	  
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
	    return fullBlock.getFlammability(world, x, y, z, face);
	}
	
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
	    return fullBlock.getFireSpreadSpeed(world, x, y, z, face);
	}
	
	@Override
	public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
		return true;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
	    for(int i = 0; i < subtypes; i++) {
	    	list.add(new ItemStack(item, 1, i));
	    }
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return fullBlock.getIcon(side, meta + 12);
	}

}
