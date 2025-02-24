package metweaks.block;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRCreativeTabs;
import metweaks.MeTweaksConfig;
import metweaks.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MeTweaksSlab  extends BlockSlab {
	public final int types;
	public final int renderMetaOffset;
	public final Block fullBlock;
	
	public int subBlocksOffset;
	public boolean isWoodBeam;
	public MeTweaksSlab secondary;
	
	public static CreativeTabs tabTemp;
	
	public void setCreativeTabsLOTR() {
		if(tabTemp == null)
		try {
			tabTemp = ReflectionHelper.getPrivateValue(LOTRCreativeTabs.class, null, "tabBlock");
		} catch (Exception e) {}
		setCreativeTab(tabTemp);
	}
	
	public MeTweaksSlab getBeamSlab(int rotation) {
		boolean hasPart1 = renderMetaOffset == 0;
		// we want part1
		if(rotation < 8) {
			return hasPart1 ? this : secondary;
		}
		return hasPart1 ? secondary : this;
	}

	private String getRegName(int index) {
		String add = index > 1 ? "slab"+index : "slab_";
		
		String name = GameData.getBlockRegistry().getNameForObject(fullBlock);
		name = name.replaceFirst("^minecraft\\:", "");
		name = name.replaceFirst("\\:tile\\.", "_");
		name = name.replace(':', '_');
		return add+name;
		
		/*
		String unlocalized = null;
		try {
			unlocalized = (String) field_unlocalizedName.get(fullBlocks[0]);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		if(unlocalized != null) {
			return add + unlocalized.replace(":", "_");
		}
		return null;*/
	}
	
	/*static IdentityHashMap<Block, Object> blockInfo;
	static Field field_encouragement;
	static Field field_flammibility;
	
	static {
		Field field_fireInfo = ReflectionHelper.findField(BlockFire.class, "blockInfo");
		try {
			blockInfo = (IdentityHashMap<Block, Object>) field_fireInfo.get(Blocks.fire);
        	Class fireinfo = Class.forName("net.minecraft.block.BlockFire$FireInfo");
    		field_encouragement = ReflectionHelper.findField(fireinfo, "encouragement");
        	field_flammibility = ReflectionHelper.findField(fireinfo, "flammibility");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setFireInfoFrom(Block block, Block from) {
		if(blockInfo != null) {
        	Object info = blockInfo.get(from);
        	if(info != null) {
        		int encouragement = 0;
        		int flammibility = 0;
        		try {
	        		encouragement = field_encouragement.getInt(info);
	        		flammibility = field_flammibility.getInt(info);
	        	} catch (Exception e) {
	    			e.printStackTrace();
	    			return;
	    		}
        		Blocks.fire.setFireInfo(block, encouragement, flammibility);
        	}
        }
	}*/
	
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
	
	public MeTweaksSlab(int typesTotal, int renderMetaOff, int index, Block full) {
        super(false, full.getMaterial());
        fullBlock = full;
        renderMetaOffset = renderMetaOff;
        types = typesTotal;
        useNeighborBrightness = true;
        setCreativeTab(CreativeTabs.tabBlock);
        
        setStepSound(full.stepSound);
        
        
            
        
        
        String name = getRegName(index);
		
		setBlockName(name);
		GameRegistry.registerBlock(this, MeTweaksSlabItem.class, name);
		System.out.println("Registering..."+name+" "+getIdFromBlock(this)+" > "+GameData.getBlockRegistry().getNameForObject(full));
    }
	
	
    
    @Override
	public float getExplosionResistance(Entity entity) {
        return fullBlock.getExplosionResistance(entity);
    }
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
        return fullBlock.getBlockHardness(world, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {}

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
    	if(isWoodBeam) {
    		int dmg = meta & 7; // remove top / bottom
    		int rotation = (dmg + renderMetaOffset) & 12;
    		// allow rotation only be 0 or 12
    		if(rotation < 12) {
    			rotation = 0; // default rotation
    		}
    		// else use closed rotation and dmg

    		return Item.getItemFromBlock(getBeamSlab(rotation));
    	}
        return Item.getItemFromBlock(this);
    }
    
    @Override
    public int damageDropped(int meta) {
    	meta &= 7;
    	if(isWoodBeam) {
    		int rotation = (meta + renderMetaOffset) & 12;
    		// allow rotation only be 0 or 12
    		if(rotation < 12) {
    			rotation = 0; // default rotation
    			meta &= 3; // limit to type only
    		}
    		// else use closed rotation and dmg

    		return meta;
    	}
        return meta;
    }


    /*@Override
    protected ItemStack createStackedBlock(int i) {
    	return new ItemStack(this, 2, i & 7);
    }*/
    
    public ItemStack getPickBlockForMeta(int meta) {
    	int rotation = (meta + renderMetaOffset) & 12;
    	
		// allow rotation only be 0 or 12
		if(rotation < 12) {
			rotation = 0; // default rotation
			meta &= 3; // limit to type only
		}
		// else use closed rotation and dmg

		return new ItemStack(getBeamSlab(rotation), 1, meta);
    }
    
    @Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
    	if(isWoodBeam) {
    		return getPickBlockForMeta(getDamageValue(world, x, y, z));
    	}
    	return super.getPickBlock(target, world, x, y, z, player);
    }
    
    private boolean isTopShape(IBlockAccess world, int x, int y, int z, int side) {
    	int opposite = Facing.oppositeSide[side];
		return world.getBlockMetadata(x + Facing.offsetsXForSide[opposite], y + Facing.offsetsYForSide[opposite], z + Facing.offsetsZForSide[opposite]) > 7;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
    	// 1x1 and non transculent blocks nearby
    	// note: our solution does not allow x z variation
    	// top or bottom with space between neighbour block
    	if((side == 0 && minY > 0.0D) || (side == 1 && maxY < 1.0D)) {
    		return true;
    	}
    	
    	Block neighbour = world.getBlock(x, y, z);
    	if(neighbour.isOpaqueCube()) {
    		return false;
    	}
    	
    	if(neighbour instanceof BlockSlab) {
    		boolean myTop = isTopShape(world, x, y, z, side);
    		boolean top = world.getBlockMetadata(x, y, z) > 7;
    		
    		if(top == myTop) {
    			// except top bottom faces we need to render
    			return side <= 1;
    		}
    		// render faces that arent same shape and not top or bottom
    		return side > 1;
    	}
    	else if(neighbour instanceof BlockStairs) {
    		boolean myTop = isTopShape(world, x, y, z, side);
    		int shape = world.getBlockMetadata(x, y, z) & 7;
    		
    		
    		boolean top = shape > 3;
    		// note: rest is catched by 1x1 block check.
    		if(side <= 1) {
    			if(top != myTop)
    				return false;
    		}
    		else if(top == myTop) {
    			return false;
    		}
    		
    		int sideshape = shape & 3;
    		// stair back
    		if((sideshape == 2 && side == 2)
			|| (sideshape == 0 && side == 4) 
			|| (sideshape == 3 && side == 3)
			|| (sideshape == 1 && side == 5)) {
    			return false;
    		}
    		return true;
    	}
    	else if(MeTweaksConfig.verticalSlabs && neighbour instanceof VerticalSlab) {
    		if(side <= 1) return true;
    		int shape = world.getBlockMetadata(x, y, z) >> 2;
    		// vertical slab back
    		if(shape == 0 && side == 2
    		|| shape == 1 && side == 5
    		|| shape == 2 && side == 3
    		|| shape == 3 && side == 4) {
    			return false;
    		}
    		return true;
    	}
    	return true;
    }
    
    
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
       for(int i = subBlocksOffset; i < types+subBlocksOffset; i++)
          list.add(new ItemStack(item, 1, i));
    }
    
    @Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
    	// if top shape
    	return world.getBlockMetadata(x, y, z) > 7;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess world, int x, int y, int z, int side) {
    	// side 1 is from top side
    	return side == 1 && world.getBlockMetadata(x, y, z) > 7;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
    	return Item.getItemFromBlock(this);
    }

    @Override
    public int getRenderType() {

	    if(isWoodBeam)
	    	return CommonProxy.beamSlabRenderID;
	    return super.getRenderType();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
    	return fullBlock.getIcon(side, renderMetaOffset + (meta & 7));
    }

    // kinda useless
    public String func_150002_b(int i) {
    	return getUnlocalizedName() + "." + i;
    }
}
