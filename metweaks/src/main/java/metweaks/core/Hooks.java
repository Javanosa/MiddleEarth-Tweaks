package metweaks.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import metweaks.ASMConfig;
import metweaks.MeTweaksConfig;
import metweaks.block.MeTweaksSlab;
import metweaks.block.MeTweaksSlabItem;
import metweaks.block.VerticalSlab;
import metweaks.events.ClientEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import team.chisel.block.BlockCarvableSlab;

public class Hooks {
	
	public static void warnMissingDiscriminator(String alert, FMLProxyPacket msg) {
		if(ASMConfig.passMissingDiscriminator == 1) {
			NetworkManager manager = msg.getOrigin();
			
			FMLLog.warning((manager != null ? manager.getSocketAddress() : "" ) + " to " + msg.getTarget() + " - " + alert);
		}
	}
	
	public static void consumeItem(ItemStack itemstack, EntityPlayer player, ItemStack empty) {
		//System.out.println(itemstack);
		if(empty == null) empty = new ItemStack(Items.bowl);
		int prevSize = itemstack.stackSize;
	  	itemstack.stackSize = Math.max(1, --prevSize); // avoid getting zero
	  	
	  	if(prevSize <= 0) {
	  		//itemstack.stackSize++;
	  	//	System.out.println(itemstack);
	  		player.inventory.setInventorySlotContents(player.inventory.currentItem, empty); 
	  		
	  	}
	  	else if (!player.inventory.addItemStackToInventory(empty)) {
	    	player.dropPlayerItemWithRandomChoice(empty, false);
	    } 
	}
	
	/*public static void consumeItem2(ItemStack itemstack, EntityPlayer player) {
		//System.out.println(itemstack);
		ItemStack emptyMug = LOTRItemMug.getVessel(itemstack).getEmptyVessel();
		int prevSize = itemstack.stackSize;
	  	itemstack.stackSize = Math.max(1, --prevSize); // avoid getting zero
	  	
	  	if(prevSize <= 0) {
	  		//itemstack.stackSize++;
	  	//	System.out.println(itemstack);
	  		player.inventory.setInventorySlotContents(player.inventory.currentItem, emptyMug); 
	  		
	  	}
	  	else if (!player.inventory.addItemStackToInventory(emptyMug)) {
	    	player.dropPlayerItemWithRandomChoice(emptyMug, false);
	    } 
	}*/
	
	public static List<PotionEffect> getStackedEffects(float strength, EntityLivingBase entity, List<PotionEffect> effects) {
		List<PotionEffect> list = new ArrayList<PotionEffect>();
		
		@SuppressWarnings("unchecked")
		Collection<PotionEffect> activeCollect = entity.getActivePotionEffects();
		for(int i = 0; i < effects.size(); i++) {
			
			PotionEffect effect = effects.get(i);
			
			int id = effect.getPotionID();
			float duration = effect.getDuration() * strength;
			
			PotionEffect active = null;
			for(PotionEffect p : activeCollect) {
				if(p.getPotionID() == id) {
					active = p;
					break;
				}
			}
			
			if(active != null) {
				int remain = active.getDuration();

				float factor = ASMConfig.draughtStackEffectsIncrease * 0.01F;//   0.5F;
				if(remain > duration) {
					duration = remain + (duration * factor);
				}
				else {
					duration = (remain * factor) + duration;
				}
			}
			list.add(new PotionEffect(id, (int) duration));
		}
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	public static Set getProperHoeBlocks() {
		return Sets.newHashSet();
	}
	
	public static void setHoeToolClass(ItemHoe item) {
		Field field_toolClass = ReflectionHelper.findField(ItemTool.class, "toolClass");
		//Field field_efficiency = ReflectionHelper.findField(ItemTool.class, "efficiencyOnProperMaterial", "field_77864_a");
		Field field_damage = ReflectionHelper.findField(ItemTool.class, "damageVsEntity", "field_77865_bY");
		try {
			field_toolClass.set(item, "hoe");
			//field_efficiency.setFloat(item, field_efficiency.getFloat(item) * 0.9F);
			field_damage.setFloat(item, 0);
		}
		catch(Exception ec) {
			ec.printStackTrace();
		}
		
		
		
	}
	
	public static void setShearsToolClass(ItemShears item) {
		Field field_toolClass = ReflectionHelper.findField(ItemTool.class, "toolClass");
		//Field field_efficiency = ReflectionHelper.findField(ItemTool.class, "efficiencyOnProperMaterial", "field_77864_a");
		Field field_damage = ReflectionHelper.findField(ItemTool.class, "damageVsEntity", "field_77865_bY");
		Field field_efficiency = ReflectionHelper.findField(ItemTool.class, "efficiencyOnProperMaterial", "field_77864_a");
		try {
			field_toolClass.set(item, "shears");
			//field_efficiency.setFloat(item, field_efficiency.getFloat(item) * 0.9F);
			field_damage.setFloat(item, 0);
			field_efficiency.setFloat(item, 5F);
		}
		catch(Exception ec) {
			ec.printStackTrace();
		}
		
		
		
	}
	
	
	
	public static final TObjectIntMap<Item> consumeDurations = new TObjectIntHashMap<>();
	
	public static int getMaxEatingDuration(ItemFood item) {
		if(consumeDurations.containsKey(item)) {
			return consumeDurations.get(item);
		}
		return 32;
	}
	
	
	
	
	
	public static boolean isVerticalMode(EntityPlayer player) {
		return VerticalSlab.modesVertical.contains(player.getUniqueID());
	}
	
	public static boolean oppositeSlabCheck(Block single_slab, Block double_slab, ItemStack stack, World world, int side, int x, int y, int z) {
		// make coords local here!
    	// take block in oppostive direction. See if it can do half to full
    	switch(side) {
     		case 0: --y; break;
     		case 1: ++y; break;
     		case 2: --z; break;
     		case 3: ++z; break;
     		case 4: --x; break;
     		case 5: ++x; break;
    	}
        Block block = world.getBlock(x, y, z);
        if(block instanceof VerticalSlab) {
       	 int meta = world.getBlockMetadata(x, y, z);
       	 
       	int itemDmg = stack.getItemDamage();
		int fullblockMetaOffset = 0;
		
    	if(VerticalSlab.isMeTweaksSlab(single_slab)) {
			MeTweaksSlab slab = (MeTweaksSlab) single_slab;

			if(MeTweaksSlabItem.isSlabWoodBeam(slab)) {
				int ascRotation = MeTweaksSlabItem.getAscRotation(block, meta);
		        single_slab = slab = slab.getBeamSlab(ascRotation);
		        
		        
		        itemDmg |= ascRotation & 4;
	        }
			fullblockMetaOffset = slab.renderMetaOffset;
		}
       	 
       	 VerticalSlab vertical = (VerticalSlab) block;
       	 int type = (meta & 3) + vertical.offset;
        
        
	        // do the same double block check again but without side check!
	        if(vertical.slab == single_slab && type == itemDmg) {
	        	if(double_slab == null) {
	        		//System.out.println("Allow half to full opposite");
	            	return true;
	        	}
	        	
	        	type += fullblockMetaOffset;
	        	
	            if (world.checkNoEntityCollision(double_slab.getCollisionBoundingBoxFromPool(world, x, y, z)) && world.setBlock(x, y, z, double_slab, type, 3)) {
	                												// getBreakSound()
	            	world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, double_slab.stepSound.func_150496_b(), (double_slab.stepSound.getVolume() + 1F) / 2F, double_slab.stepSound.getPitch() * 0.8F);
	                --stack.stackSize;
	                //System.out.println("Place half to full opposite");
    	            return true;
	            }
	            
	        } 
        }
		return false;
	}
	
	public static boolean onPlaceVerticalChisel(Block block, ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return verticalAdjacentAndHalfCheck(block, ((BlockCarvableSlab) block).master, stack, player, world, side, x, y, z, hitX, hitZ);
	}
	
	public static boolean onPlaceVertical(BlockSlab single_slab, Block double_slab, ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		return verticalAdjacentAndHalfCheck(single_slab, double_slab, stack, player, world, side, x, y, z, hitX, hitZ);
	}
	
	static boolean sendFacingTip = true;
	
	public static boolean verticalAdjacentAndHalfCheck(Block single_slab, Block double_slab, ItemStack stack, EntityPlayer player, World world, int side, int x, int y, int z, float hitX, float hitZ) {
		
		
		
		
		
		
		// handle placement
		boolean mirror = false;
		int shape = 0;
    	Block block = world.getBlock(x, y, z);
    	//int meta = world.getBlockMetadata(x, y, z); // not 100% needed but easier
    	
    	int itemDmg = stack.getItemDamage();
		int fullblockMetaOffset = 0;
		
    	if(VerticalSlab.isMeTweaksSlab(single_slab)) {
			MeTweaksSlab slab = (MeTweaksSlab) single_slab;

			if(MeTweaksSlabItem.isSlabWoodBeam(slab)) {
				int ascRotation = MeTweaksSlabItem.getAscRotation(block, world.getBlockMetadata(x, y, z));
		        single_slab = slab = slab.getBeamSlab(ascRotation);
		        
		        
		        itemDmg |= ascRotation & 4;
	        }
			fullblockMetaOffset = slab.renderMetaOffset;
		}
        
    	
    	
    	
    
         if(block instanceof VerticalSlab) {
        	 int meta = world.getBlockMetadata(x, y, z);
        	 
        	 
        	
        	 VerticalSlab vertical = (VerticalSlab) block;
        	 int type = (meta & 3) + vertical.offset;
        	 shape = meta >> 2;
        	 
        	 
        	 
        	 
        	 if(vertical.slab == single_slab && type == (itemDmg) && validVerticalShape(shape, side)) {
        		if(double_slab == null) {
        			// allow half to full
           		 	//System.out.println("Allow half to full side");
           		 	return true;
        		}
        		
        		type += fullblockMetaOffset;
        		
        		
        		
        		
        		
        		
        		if(world.checkNoEntityCollision(double_slab.getCollisionBoundingBoxFromPool(world, x, y, z)) 
        			 && world.setBlock(x, y, z, double_slab, type, 3)) {
						// getBreakSound
        			 
					world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, double_slab.stepSound.func_150496_b(), (double_slab.stepSound.getVolume() + 1F) / 2F, double_slab.stepSound.getPitch() * 0.8F);
					--stack.stackSize;
					//System.out.println("Place half to full side");
					return true;
				}
        	 }
        	 
        	 
        	 if(MeTweaksConfig.mirrorVerticalSlabs)
        	 switch(side) {
        	 	case 0:
        	 	case 1:
        	 		mirror = true;
        	 		break;
        	 	case 2:
        	 	case 3:
        	 		mirror = shape == 1 || shape == 3;
        	 		break;
        	 	case 4:
        	 	case 5:
        	 		mirror = shape == 0 || shape == 2;
        	 		break;
        	 }
        	 
        	
        	
         }	
					
        // gets the position in front of the clicked block        
         if(oppositeSlabCheck(single_slab, double_slab, stack, world, side, x, y, z)) return true;

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
         
         
         
         
         
         if(VerticalSlab.allverticals.containsKey(VerticalSlab.index(single_slab, itemDmg))) {
	         VerticalSlab vertical = VerticalSlab.allverticals.get(VerticalSlab.index(single_slab, itemDmg));
	         int type = itemDmg - vertical.offset;
	         int metadata = vertical.metaState(world, x, y, z, side, type, player, hitX, hitZ);
	         if(!player.isSneaking()) {
	        	 if(world.isRemote && MeTweaksConfig.showTips && sendFacingTip && (side > 1 || mirror)) {
	        		 ClientEvents.actionBar(StatCollector.translateToLocal("metweaks.verticalslabs.tip"), false, 80, null, true);
	        		 sendFacingTip = false;
	        	 }
	        	 if(mirror) {
		        	 metadata = type + shape * 4;
		        	 int original = world.getBlockMetadata(x, y, z);
		             world.setBlockMetadataWithNotify(x, y, z, metadata, 0);
		             vertical.setBlockBoundsBasedOnState(world, x, y, z);
		             world.setBlockMetadataWithNotify(x, y, z, original, 0);
		         }
	         }
	        
	        	 
	         
	          
	         if(world.canPlaceEntityOnSide(vertical, x, y, z, false, side, null, stack)) {
	        	 if(double_slab == null) {
	        		 //System.out.println("Allow new placement");
	        		 return true;
	        	 }
	        	 
	        	 
	        	 
	             if(world.setBlock(x, y, z, vertical, metadata, 3)) {
	            	 /*if(world.getBlock(x, y, z) == vertical) {
	            		 vertical.onBlockPlacedBy(world, x, y, z, player, stack);
	            		 vertical.onPostBlockPlaced(world, x, y, z, metadata);
		             }*/
		
		             world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, single_slab.stepSound.func_150496_b(), (single_slab.stepSound.getVolume() + 1F) / 2F, single_slab.stepSound.getPitch() * 0.8F);
		             --stack.stackSize;
		             //System.out.println("Place new");
		             return true;
	             }
	             
	         }
	         
	         
         }
         //System.out.println("End");
         return false;
	}
	
	private static boolean validVerticalShape(int shape, int side) {
		// "DOWN", "UP", "NORTH", "SOUTH", "WEST", "EAST"
		return shape == 0 && side == 2 || shape == 1 && side == 5 || shape == 2 && side == 3 || shape == 3 && side == 4;
	}
	
	public static boolean canPlaceVerticalHere(BlockSlab single_slab, World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
		return verticalAdjacentAndHalfCheck(single_slab, null, stack, player, world, side, x, y, z, -1, -1);
    }
	
	public static ItemStack getPickBlock(Block block, MovingObjectPosition target, World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if(meta < 12)
			meta &= 3;
		return new ItemStack(Item.getItemFromBlock(block), 1, meta);
	}
	
	public static void onBlockPlacedByTP(World world, int x, int y, int z, EntityLivingBase entity) {
		
        int meta = world.getBlockMetadata(x, y, z);
        
        if(meta == 8 || meta == 0) {
        	int facing = MathHelper.floor_float((entity.rotationYaw * 4F / 360F) + 0.5F) & 3;
        	
        	if(facing != 0) {
	        	switch(facing) {
		        	case 1: // west
		    			meta += 3;
		    			break;
	        		case 2: // north
	        			meta += 1;
	        			break;
	        		case 3: // east
	        			meta += 2;
	        			break;
	        		// south, do nothing
	        	}
	        	
	        	world.setBlockMetadataWithNotify(x, y, z, meta, 2);
        	}
        }
    }

	public static int onBlockPlacedTP(int side, float hitY, int meta) {
        // place from side
		switch(side) {
			case 3:
				meta = 1;
				break;
			case 4:
				meta = 2;
				break;
			case 5:
				meta = 3;
				break;
		}
        // turn side into top and turn top into top
        if(side != 1 && (hitY > 0.5F || side == 0)) {
        	meta |= 8;
        }
        return meta;
    }
	
	 public static void setBoundsForItemRender(Block block) {
		
		if(block.isOpaqueCube())  {
	    	 block.setBlockBounds(0, 0, 0, 1, 1, 1);
        }
		else if(VerticalSlab.clientVertical) {
			float offsetX = -0.2f;//0.05f;
			float offsetY = 0.2f;//0.1f;
			
			
        	/*block.setBlockBounds(
					// minX, minY, minZ
					0+offset, 0+offsetY, 0, 
					// maxX, maxY, maxZ
					0.5F+offset, 1+offsetY, 1);*/
        	//GL11.glTranslatef(0.05f, 0.1f, 0);
        	
        	block.setBlockBounds(
					// minX, minY, minZ
					offsetX, offsetY, 0.5F, 
					// maxX, maxY, maxZ
					1+offsetX, 1+offsetY, 1);
        }
        else {
            block.setBlockBounds(0, 0, 0, 1, 0.5F, 1);
        }
    }
	
	public static float getReductionTime(long worldTime, long lastChangeTime) {
		int ticks = (int) (worldTime - lastChangeTime);
		float decay = MeTweaksConfig.conquestDecay;
		float seconds = ticks / 20F;
		if(decay == 0)
			return 0;
		if (seconds > decay) {
			seconds -= decay;
			//float v = seconds / decay;
			
			/*if(worldTime > next) {
				next = worldTime+600;
				//System.out.println("D "+decay+"  S "+seconds+"  V "+v);
			}*/
			return seconds / decay;
		}
		return 0;
	}
	
	public static void saveLvLData(NBTTagCompound nbt) {
		 nbt.setFloat("ConqDecay", MeTweaksConfig.conquestDecay);
	}
	
	public static void loadLvLData(NBTTagCompound nbt) {
		if(nbt.hasKey("ConqDecay"))
			MeTweaksConfig.conquestDecay = nbt.getFloat("ConqDecay");
		else
			MeTweaksConfig.conquestDecay = 3600;
	}
	
	
	
	
	
	
}
