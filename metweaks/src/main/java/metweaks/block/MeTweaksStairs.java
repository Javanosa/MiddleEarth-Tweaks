package metweaks.block;

import java.util.List;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import metweaks.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MeTweaksStairs extends BlockStairs {

	// 2 x 8 meta data should be fine except lotr's structure generator ignoring bigger metadata most likely
	public Block fullBlock;
	public int fullMeta;
	public int subTypes;
	
	public MeTweaksStairs(Block full, int meta, int index, int types) {
		super(full, meta);
		fullBlock = full;
		fullMeta = meta;
		subTypes = types;
		setCreativeTab(CreativeTabs.tabBlock);
		useNeighborBrightness = true;
		
		index++; // begin with 1 instead of 0
		String name = BlocksCore.getRegName(index > 1 ? "stairs"+index : "stairs_", full, false);
		
		setBlockName(name);
		GameRegistry.registerBlock(this, MeTweaksStairsItem.class, name);
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
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
		// baseMeta + (meta / 8)	results in meta + 0 or 1
		
        return fullBlock.getIcon(side, fullMeta + (meta >> 3));
    }
	
	
	
	@Override
	public MapColor getMapColor(int meta) {
		// baseMeta + (meta / 8)	results in meta + 0 or 1
		return fullBlock.getMapColor(fullMeta + (meta >> 3));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
	    for(int i = 0; i < subTypes; i++) {
	    	//								i * 8 is 0 or 8
	    	list.add(new ItemStack(item, 1, i << 3));
	    }
	}
	
	@Override
	public int damageDropped(int meta) {
        return meta > 7 ? 8 : 0;
    }
	
	/*@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        return side != 0 && (side == 1 || hitY <= 0.5F) ? meta : meta | 4;
    }*/
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int facing = MathHelper.floor_float((entity.rotationYaw * 4.0F / 360.0F) + 0.5F) & 3;
        int meta = world.getBlockMetadata(x, y, z);// & 4;
        
        
        
        int operationOr = 0;
        
        switch(facing) {
	        case 0:
	        	operationOr = 2;
	        	break;
	        case 1:
	        	operationOr = 1;
	        	break;
	        case 2:
	        	operationOr = 3;
	        	break;
	       // case 3:
	       // 	operationOr = 0;
	       // 	break;
        }
        
        world.setBlockMetadataWithNotify(x, y, z, operationOr | meta, 2);
    }
	
	@Override
	public int getRenderType() {
		return CommonProxy.stairsRenderID;
	}
}
