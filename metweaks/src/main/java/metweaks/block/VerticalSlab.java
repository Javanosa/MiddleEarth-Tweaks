package metweaks.block;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class VerticalSlab extends Block {
	
	/*static IdentityHashMap<Block, Object> blockInfo;
	static Field field_encouragement;
	static Field field_flammibility;
	
	static {
		Field field_fireInfo = ReflectionHelper.findField(BlockFire.class, "blockInfo");
		try {
			blockInfo = (IdentityHashMap<Block, Object>) field_fireInfo.get(Blocks.fire);
        	@SuppressWarnings("rawtypes")
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
	
	public static boolean clientVertical;
	
	public static void setVerticalMode(EntityPlayer player, boolean enable) {
		
		UUID id = player.getUniqueID();
		if(enable) {
			if(!VerticalSlab.modesVertical.contains(id))
				VerticalSlab.modesVertical.add(id);
		}
		else
			VerticalSlab.modesVertical.remove(id);
		
	}

	public final Block slab;
	public int offset;
	
	public static final TIntObjectMap<VerticalSlab> allverticals = new TIntObjectHashMap<>();
	public static final Set<UUID> modesVertical = new HashSet<>();
	
	// key static index (meta)
	// value Block, meta, isPillar (for render connected textures)
	// class material, maybe lenght
	
	static Field field_unlocalizedName = ReflectionHelper.findField(Block.class, "unlocalizedName", "field_149770_b", "b");
	static Field field_blockHardness = ReflectionHelper.findField(Block.class, "blockHardness", "field_149782_v", "v");
	static Field field_blockResistance = ReflectionHelper.findField(Block.class, "blockResistance", "field_149781_w", "w");
	
	private String getRegName(int index) {
		String v = "v_";
		if(index > 0)
			v="v"+(index+1);
		
		String unlocalized = null;
		try {
			unlocalized = (String) field_unlocalizedName.get(slab);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		if(unlocalized != null) {
			
			/*String name = GameData.getBlockRegistry().getNameForObject(slab);
			name = name.replaceFirst("^minecraft\\:", "");
			name = name.replaceFirst("\\:tile\\.", "_");
			unlocalized = name.replace(':', '_');*/
			
			return v + unlocalized.replace(':', '_');
		}
		return null;
		
		
		
		//String unlocalized = slab.getUnlocalizedName().substring(5).replace(":", "_");
		//System.out.println(slab+" "+slab.getUnlocalizedName()+" "+slab.getUnlocalizedName().substring(5).replace(":", "_"));
		//return v+slab.getUnlocalizedName().substring(5).replace(":", "_");
	}
	
	public static int index(Block block, int damage) {
		// shift so its before hash
		return (block.hashCode() * 10) + (damage >> 2); // damage / 4
	}
	
	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
	    return slab.isFlammable(world, x, y, z, face);
	}
	  
	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
	    return slab.getFlammability(world, x, y, z, face);
	}
	
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
	    return slab.getFireSpreadSpeed(world, x, y, z, face);
	}
	
	public static void setupHarvestLvlsGlobal() {
		for(VerticalSlab v : allverticals.values(new VerticalSlab[0])) {
			v.setupHarvestLvls();
			//setFireInfoFrom(v, v.slab);
		}
	}
	
	public void setupHarvestLvls() {
		String harvestTool = slab.getHarvestTool(0);
		int harvestLvl = slab.getHarvestLevel(0);
		
		if(harvestTool != null && harvestLvl != -1) {
			setHarvestLevel(harvestTool, harvestLvl);
		}
	}
	
	public VerticalSlab(Block block, int subtypes, int index) {
		super(block.getMaterial());
		setLightOpacity(255);
		slab = block;
		
		useNeighborBrightness = true;
		stepSound = block.stepSound;
		// disabled
		//setHardness(block.getBlockHardness(null, 0, 0, 0));
		//setResistance((getExplosionResistance(null) * 5) / 3);
		
		try {
			setHardness(field_blockHardness.getFloat(block));
	        setResistance(field_blockResistance.getFloat(block) / 3F);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		String name = getRegName(index);
		
		if(name == null) {
			System.out.println("Skipping Registering VerticalSlab..."+getIdFromBlock(slab)+" "+slab.getUnlocalizedName());
			return;
		}
		
		setBlockName(name);
		GameRegistry.registerBlock(this, VerticalSlabItem.class, name);
		System.out.println("Registering..."+name+" "+getIdFromBlock(this)+" > "+slab.getUnlocalizedName()+" "+block.getMaterial().isSolid());
		
		allverticals.put(index(block, index * 4), this); // index * 4
		
		// round up to the nearest multiplier of 4
		subtypes = (subtypes + 3) & -4;
		// for more than 4 subtypes
		for(int i = 4; i < subtypes; i += 4) {
			VerticalSlab vertical = makeInstance(block, i >> 2); // i / 4
			vertical.offset = i;
		}
	}
	
	public VerticalSlab makeInstance(Block block, int index) {
		return new VerticalSlab(block, 0, index);
	}
	
	@Override
	public float getExplosionResistance(Entity entity) {
        return slab.getExplosionResistance(entity);
    }
	
	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
        return slab.getBlockHardness(world, x, y, z);
    }
	  
	@Override
	  public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		  return false;
	  }
		  
	  // used for flow vector, dont use that pls
	  /*public boolean isBlockSolid(IBlockAccess world, int x, int y, int z, int side) {
		  
		  int shape = world.getBlockMetadata(x, y, z) >> 2;
			return side != 1 && side != 0 && (shape == 0 && side == 2
			  || shape == 1 && side == 5
			  || shape == 2 && side == 3
			  || shape == 3 && side == 4);
	  }*/
	  
	  // placing, spawning
	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		int shape = world.getBlockMetadata(x, y, z) >> 2;
		return side != ForgeDirection.UP && side != ForgeDirection.DOWN && (shape == 0 && side == ForgeDirection.SOUTH
		  || shape == 1 && side == ForgeDirection.WEST
		  || shape == 2 && side == ForgeDirection.NORTH
		  || shape == 3 && side == ForgeDirection.EAST);
	}
	
	private boolean faceSolid(int shape, int side) {
		return shape == 0 && side == 3
			  || shape == 1 && side == 4
			  || shape == 2 && side == 2
			  || shape == 3 && side == 5;
	}

	// known issue: cornerstairs may cause incorrect calculation whether side must be rendered, rare tho
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if(!super.shouldSideBeRendered(world, x, y, z, side)) {
            return false;
        }
		
		
		
		
        Block neighbour = world.getBlock(x, y, z);
        
		if(neighbour instanceof VerticalSlab) {
			int opposite = Facing.oppositeSide[side];
			int xo = x + Facing.offsetsXForSide[opposite];
	        int yo = y + Facing.offsetsYForSide[opposite];
	        int zo = z + Facing.offsetsZForSide[opposite];
			int myshape = world.getBlockMetadata(xo, yo, zo) >> 2;
			int shape = world.getBlockMetadata(x, y, z) >> 2;
			
			// solid side?
			// no top or down
			
			
			
			
			
			/*
			3 shape 2 side 5 opposite 4
			2 shape 3 side 4 opposite 5
			2 shape 1 side 5 opposite 4
			1 shape 2 side 4 opposite 5
			 * */
			
			/*us:
				isfacesolid(us)
				anyThingExceptFrontFace(them)
				
			them:
				isfacesolid(them)
				anyThingExceptFrontFace(us)
				
				
				if(solid(us)) {
					solid(them)
				}
				else if(anyThingExceptFrontFace(us)) {
					if(solid(them);
				}
				
				if(solid(them)) {
				 	
				}
				
				*/
			
			
			// x z check: face mirror
			if(side > 1 && faceSolid(shape, opposite) && /*faceSolid(myshape, side) || */!faceSolid(myshape, opposite)) {
					
					 
					return false;
				
			}
			else {
				// same side?
				
		        
				// not same shape or backset front face
				return myshape != shape || faceSolid(shape, side) || faceSolid(myshape, opposite);
				
			}
		}
		else if(neighbour instanceof BlockSlab) {
			if(side <= 1) {
				boolean top = world.getBlockMetadata(x, y, z) > 7;
				// slab top and vertical above or slab bottom and vertical below
				if(top == (side == 0)/* || !top && side == 1*/) {
					return false;
				}
				
			}
			
			
			
		}
		else if(neighbour instanceof BlockStairs) {
			int opposite = Facing.oppositeSide[side];
			int xo = x + Facing.offsetsXForSide[opposite];
	        int yo = y + Facing.offsetsYForSide[opposite];
	        int zo = z + Facing.offsetsZForSide[opposite];
			int myshape = world.getBlockMetadata(xo, yo, zo) >> 2;
			int shape = world.getBlockMetadata(x, y, z) & 7;
			
			boolean bottom = shape < 4;// bottom
			if(side == 1) {
				// slab below bottom stair
				if(bottom) 
					return false;
				
				// slab below upside down stair
				else if((myshape == 0 && shape == 6) || (myshape == 1 && shape == 5) || (myshape == 2 && shape == 7) || (myshape == 3 && shape == 4))
					return false;
					
					
			}
			else if(side == 0) {
				
				// slab on top of upside down stair
				if(!bottom)
					return false;
				// slab on top of stair
				else if((myshape == 0 && shape == 2) || (myshape == 1 && shape == 1) || (myshape == 2 && shape == 3) || (myshape == 3 && shape == 0)) {
					
					return false;
				}
					
				
			}
			else {
				int sideshape = shape & 3;
				// slab behind stair back
				
				if(	   (side == 2 && myshape != 0 && sideshape == 2)
					|| (side == 4 && myshape != 3 && sideshape == 0) 
					|| (side == 3 && myshape != 2 && sideshape == 3)
					|| (side == 5 && myshape != 1 && sideshape == 1)) {
					
					return false;
				}
				
				/*if(side != 3 && (sideshape == 2 && side == 2
						|| sideshape == 0 && side == 4 
						|| sideshape == 3 && side == 3
						|| sideshape == 1 && side == 5)) {
					
				}*/
				
				
				// stair sideways to slab sideways
				else if((side == 3 || side == 2) && ((myshape == 3 && sideshape == 0) || (myshape == 1 && sideshape == 1))) {
					
					return false;
				}
				else if((side == 5 || side == 4) && ((myshape == 0 && sideshape == 2) || (myshape == 2 && sideshape == 3))) {
					
					return false;
				}
				
				
			}
		}
		return true;
		
	}
	
	
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int meta) {
		int half = meta & 3; // 0 1 2 3 -> & 7 = 
		return slab.getIcon(side, half + offset);
		
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconregister) {}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		
		
		switch(world.getBlockMetadata(x, y, z) >> 2) {
			case 0:
				setBlockBounds(
						// minX, minY, minZ
						0, 0, 0.5F, 
						// maxX, maxY, maxZ
						1, 1, 1);
				break;
			case 1:
				setBlockBounds(
						// minX, minY, minZ
						0, 0, 0, 
						// maxX, maxY, maxZ
						0.5F, 1, 1);
				break;
			case 2:
				setBlockBounds(
						// minX, minY, minZ
						0, 0, 0, 
						// maxX, maxY, maxZ
						1, 1, 0.5F);
				break;
			case 3:
				
				setBlockBounds(
						// minX, minY, minZ
						0.5F, 0, 0, 
						// maxX, maxY, maxZ
						1, 1, 1);
				break;
		}
    }
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public int damageDropped(int meta) {
		meta = (meta & 3) + offset;
		if(isMeTweaksSlab(slab)) {
			return slab.damageDropped(meta);
		}
		return meta;
		
	}
	
	
	
	public static boolean isMeTweaksSlab(Block single_slab) {
		return (MeTweaksConfig.woolSlabs || (MeTweaksConfig.barkBlocks && (MeTweaksConfig.barkSlabs || (MeTweaks.lotr && MeTweaksConfig.beamSlabs)))) && single_slab instanceof MeTweaksSlab;
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		int dmg = getDamageValue(world, x, y, z);
		if(isMeTweaksSlab(slab)) {
			MeTweaksSlab rslab = (MeTweaksSlab) slab;
			if(rslab.isWoodBeam) {
				return rslab.getPickBlockForMeta(dmg);
			}
		}
		return new ItemStack(slab, 1, dmg);
    }
	
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z) {
		
		return Item.getItemFromBlock(slab);
	}
	
	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		if(isMeTweaksSlab(slab)) {
			return slab.getItemDropped((meta & 3) + offset, rand, fortune);
		}
		return Item.getItemFromBlock(slab);
		
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, @SuppressWarnings("rawtypes") List list, Entity collider) {
		
		setBlockBoundsBasedOnState(world, x, y, z);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, collider);
    }
	
	public int metaState(World world, int x, int y, int z, int side, int type, EntityLivingBase entity, float hitX, float hitZ) {
		int meta = onBlockPlaced(world, x, y, z, side, hitX, 0, hitZ, type);
        
		// if down or top
        if(side <= 1 || entity.isSneaking()) {
        	int facing = MathHelper.floor_float((entity.rotationYaw * 4F / 360F) + 0.5F) & 3;
        	
        	// the client side doesnt care.
        	if(hitZ >= 0 && side <= 1)
        	switch(facing) {
        		case 0:
        			if(hitZ <= 0.5)
        				facing = 2;
        			break;
        		case 1:
        			if(hitX > 0.5)
        				facing = 3;
        			break;
        		case 2:
        			if(hitZ > 0.5)
        				facing = 0;
        			break;
        		case 3:
        			if(hitX <= 0.5)
        				facing = 1;
        			break;
        	}
        	
        	
         	meta = type + (facing << 2); // same as * 4
         	
         	
         	
         	
        }
        int original = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, meta, 0);
        setBlockBoundsBasedOnState(world, x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, original, 0);
		return meta;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		
		
		
		// prepare metadata before placing
		if(side > 2) {
			// south side 2 meta 0
			
			if(side == 3) {
				// north
				meta += 8;
			}
			else if(side == 4) {
				// east
				meta += 12;
			}
			else {
				// side 5
				meta += 4;
			}
		}
		return meta;
    }
	
	@Override
	public void setBlockBoundsForItemRender() {
		float offsetX = -0.2f;
		float offsetY = 0.2f;
    	
    	setBlockBounds(
				// minX, minY, minZ
				offsetX, offsetY, 0.5F, 
				// maxX, maxY, maxZ
				1+offsetX, 1+offsetY, 1);
    }
	
	@Override
	public int getRenderType() {
		return slab.getRenderType();
		
	}
}
