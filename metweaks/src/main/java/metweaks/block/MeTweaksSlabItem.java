package metweaks.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.block.LOTRBlockWoodBeam;
import metweaks.core.Hooks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class MeTweaksSlabItem extends ItemSlab {
	
	public MeTweaksSlab slab;

	public MeTweaksSlabItem(Block block) {
		super(block, null, null, false);
		slab = (MeTweaksSlab) block;
		setHasSubtypes(true);
		
	}
	
	/*public String nameForMeta(Block block, int meta) {
		
		
		if(block == Blocks.wool) {
			return ItemDye.field_150923_a[meta & 15];
		}
		else if(block == Blocks.log) {
			return BlockOldLog.field_150168_M[meta & 3];
		}
		else if(block == Blocks.log2) {
			return BlockNewLog.field_150169_M[meta & 2];
		}
		return String.valueOf(meta);
	}*/
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
        return slab.func_150002_b(stack.getItemDamage());
    }
	
	/*public String getUnlocalizedName(ItemStack stack) {
		int meta = (stack.getItemDamage() & 3) + slab.renderMetaOffset;
		return slab.fullBlocks[slab.getIndexForMeta(meta)].getUnlocalizedName()+"."+meta;
    }*/
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		final int originmeta = stack.getItemDamage();
		int meta = originmeta & 7;
		
		meta = meta + slab.renderMetaOffset;
		
		
		if(slab.isWoodBeam && slab.renderMetaOffset == 0) {
			// limit to first supported 4 types
			meta = meta & 3;
		}
		stack.setItemDamage(meta);
		String key = Item.getItemFromBlock(slab.fullBlock).getItemStackDisplayName(stack);
		stack.setItemDamage(originmeta);
        return (StatCollector.translateToLocal(key) + StatCollector.translateToLocal("metweaks.slabprefix")).trim();
    }
	
	@Override
	public int getMetadata(int meta) {
        return meta & 7;
    }

	public static int getAscRotation(Block block, int meta) {
		int ascRotation = 0;
		if(block instanceof LOTRBlockWoodBeam) {
        	ascRotation = meta & 12;
        }
		else { 
			if(block instanceof VerticalSlab) {
				meta &= 3;
				meta += ((VerticalSlab) block).offset;
				block = ((VerticalSlab) block).slab;
				
			}
			
			if(block instanceof MeTweaksSlab) {
	        	MeTweaksSlab rslab = (MeTweaksSlab) block;
	        	if(rslab.isWoodBeam) {
	        		ascRotation = ((meta & 7) + rslab.renderMetaOffset) & 12;
	        	}
	        }
        	
        }
		if(ascRotation == 12) ascRotation = 0;
        return ascRotation;
	}
	
	public static boolean isSlabWoodBeam(MeTweaksSlab slab) {
		return slab.isWoodBeam && slab.renderMetaOffset == 0;
	}
	
	public boolean aadjacentAndHalfCheck(Block fullblock, ItemStack stack, EntityPlayer player, World world, int side, int x, int y, int z, float hitY) {
		Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        int type = meta & 7;
        int itemDmg = stack.getItemDamage();
        
        // beam placement start
        MeTweaksSlab slab = this.slab;
        if(isSlabWoodBeam(slab)) {
        	int ascRotation = getAscRotation(block, meta);
	        slab = slab.getBeamSlab(ascRotation);
	        itemDmg |= ascRotation & 4;
        }
        
        // beam placement end
        
        if ((side == (meta > 7 ? 0 : 1)) && block == slab && type == (itemDmg)) {
        	if(fullblock == null) {
            	return true;
        	}
        	
        	
        	type = type + slab.renderMetaOffset;
        	
            if (world.checkNoEntityCollision(fullblock.getCollisionBoundingBoxFromPool(world, x, y, z)) && world.setBlock(x, y, z, fullblock, type, 3))
            {
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, fullblock.stepSound.func_150496_b(), (fullblock.stepSound.getVolume() + 1.0F) / 2.0F, fullblock.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
                
            }
            return true;
        }
        
        if(opposite(fullblock, stack, player, world, x, y, z, side)) return true;
        
       
        
        
        
        // regular placement
        if (!block.isReplaceable(world, x, y, z)) {
    		
       	 switch(side) {
	     		case 0: --y; break;
	     		case 1: ++y; break;
	     		case 2: --z; break;
	     		case 3: ++z; break;
	     		case 4: --x; break;
	     		case 5: ++x; break;
	     	}
        }
        
        
        
       	 
        
        if(world.canPlaceEntityOnSide(slab, x, y, z, false, side, null, stack)) {
	       	 if(fullblock == null) {
	       		 
	       		 return true;
	       	 }
	       	 
	       	int data = getMetadata(itemDmg);
	        int metadata = slab.onBlockPlaced(world, x, y, z, side, 0, hitY, 0, data);
	       	
       	 
       	 
            if(world.setBlock(x, y, z, slab, metadata, 3)) {
            	if(world.getBlock(x, y, z) == slab) {
           			slab.onBlockPlacedBy(world, x, y, z, player, stack);
           			slab.onPostBlockPlaced(world, x, y, z, metadata);
                }
	             world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, slab.stepSound.func_150496_b(), (slab.stepSound.getVolume() + 1F) / 2F, slab.stepSound.getPitch() * 0.8F);
	             --stack.stackSize;
	             
	             
            }
            return true;
        }
        
        return false;
        
        
	}
	
	
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if(stack.stackSize == 0) {
            return false;
        }
        else if(!player.canPlayerEdit(x, y, z, side, stack)) {
            return false;
        }
        
    	
    	if(Hooks.isVerticalMode(player)) {
    		return Hooks.onPlaceVertical(slab, slab.fullBlock, stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    	}
    	return aadjacentAndHalfCheck(slab.fullBlock, stack, player, world, side, x, y, z, hitY);
    }

	@SideOnly(Side.CLIENT)
    public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
    	if(Hooks.isVerticalMode(player)) {
    		return Hooks.canPlaceVerticalHere(slab, world, x, y, z, side, player, stack);
    	}
    	return aadjacentAndHalfCheck(null, stack, player, world, side, x, y, z, 0);
    }

    private boolean opposite(Block fullblock, ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side) {
    	switch(side) {
	 		case 0: --y; break;
	 		case 1: ++y; break;
	 		case 2: --z; break;
	 		case 3: ++z; break;
	 		case 4: --x; break;
	 		case 5: ++x; break;
    	}

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        int type = meta & 7;
        
        // beam placement start
        MeTweaksSlab slab = this.slab;
        int ascRotation = 0;
        if(isSlabWoodBeam(slab)) {
	        ascRotation = getAscRotation(block, meta);
	        slab = slab.getBeamSlab(ascRotation);
	        ascRotation &= 4;
        }
        
        // beam placement end

        if (block == slab && type == (stack.getItemDamage() | ascRotation)) {
        	if(fullblock == null) {
        		
            	return true;
        	}
        	
        	type = type + slab.renderMetaOffset;
        	
            if (world.checkNoEntityCollision(fullblock.getCollisionBoundingBoxFromPool(world, x, y, z)) && world.setBlock(x, y, z, fullblock, type, 3))
            {
                world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, fullblock.stepSound.func_150496_b(), (fullblock.stepSound.getVolume() + 1.0F) / 2.0F, fullblock.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
                
            }
            
            return true;
        }
        return false;
    }
	
}
