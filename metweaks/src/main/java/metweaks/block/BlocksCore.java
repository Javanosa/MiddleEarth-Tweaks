package metweaks.block;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import ganymedes01.etfuturum.blocks.ISubBlocksBlock;
import lotr.common.LOTRCreativeTabs;
import lotr.common.block.LOTRBlockSlabBase;
import lotr.common.block.LOTRBlockSlabFalling;
import lotr.common.block.LOTRBlockWoodBase;
import lotr.common.block.LOTRBlockWoodBeam;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IShearable;
import team.chisel.block.BlockCarvableSlab;

public class BlocksCore {
	
	private static String debug = "";
	public static boolean skipLog;
	
	public static void addInfo(String string) {
		if(!skipLog) debug += "\n	"+string;
	}
	
	public static String getRegName(String prefix, Block fullBlock, boolean removeMETPrefix) {
		String name = GameData.getBlockRegistry().getNameForObject(fullBlock);
		name = name.replaceFirst("^minecraft\\:", "");
		if(removeMETPrefix)
			name = name.replaceFirst("^"+MeTweaks.MODID+"\\:", "");
		name = name.replaceFirst("\\:tile\\.", "_");
		name = name.replace(':', '_');
		return prefix+name;
		
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
	
	private static CreativeTabs tabTemp;
	
	public static CreativeTabs getBlocksTabLOTR() {
		if(tabTemp == null)
		try {
			tabTemp = ReflectionHelper.getPrivateValue(LOTRCreativeTabs.class, null, "tabBlock");
		} catch (Exception e) {}
		return tabTemp;
	}
	
	public static void setupBlocks() {
		
		skipLog = true;
		if(MeTweaksConfig.woolSlabs) {
			MeTweaksSlab woolSlab1 = new MeTweaksSlabShearable(8, 0, 1, Blocks.wool);
			MeTweaksSlab woolSlab2 = new MeTweaksSlabShearable(8, 8, 2, Blocks.wool);
			woolSlab1.setHarvestLevel("shears", 0);
			woolSlab2.setHarvestLevel("shears", 0);
			
			for(int i = 0; i < 16; i++) {
				GameRegistry.addRecipe(new ItemStack(i < 8 ? woolSlab1 : woolSlab2, 6, i), new Object[] {"###", '#', new ItemStack(Blocks.wool, 1, i)});
			}
		}
		
		if(MeTweaksConfig.woolStairs) {
			// register every second meta
			for(int i = 0; i < 16; i += 2) {
				MeTweaksStairs stairs = new MeTweaksStairsShearable(Blocks.wool, i, i >> 1, 2); // we know we will always have 2 subtypes
				stairs.setHarvestLevel("shears", 0);
				for(int k = i; k < (i + 2); k++) {
					//												0 or 8																	
					GameRegistry.addRecipe(new ItemStack(stairs, 4, (k & 1) << 3), new Object[] {"#  ", "## ", "###", '#', new ItemStack(Blocks.wool, 1, k)});
				}
			}
		}
		skipLog = false;
		
		
		LinkedHashSet<Block> slabs = new LinkedHashSet<>();
		//Set<Block> rotateblocks = new HashSet<>();
		LinkedHashSet<Block> rotateblocks = new LinkedHashSet<>();
		
		Iterator<Block> it = GameData.getBlockRegistry().iterator();
		
		while(it.hasNext()) {
			Block block = it.next();
			if(block instanceof BlockSlab) {
				BlockSlab slab = (BlockSlab) block;
				if(!slab.isOpaqueCube()) {
					slabs.add(block);
					if(MeTweaksConfig.debug >= 1)
						BlocksCore.addInfo("Found Slab ... "+block.getUnlocalizedName()+" "+Block.getIdFromBlock(block));
				}
			}
			else if(MeTweaks.chisel && block instanceof BlockCarvableSlab) {
				
				BlockCarvableSlab slab = (BlockCarvableSlab) block;
				if(slab.isBottom) {
					slabs.add(block);
					
					if(MeTweaksConfig.debug >= 1)
						BlocksCore.addInfo("Found Slab ... "+block.getUnlocalizedName()+" "+Block.getIdFromBlock(block));
				}
			}
			else if(block instanceof BlockRotatedPillar) {
				 rotateblocks.add(block);
			}
		}
		
		if(MeTweaksConfig.barkBlocks) {
			Field field_lotr_woodnames = null;
			Field field_lotr_woodnamesBeam = null;
			Field field_woodnames_old = null;
			Field field_woodnames_new = null;
			
				try {
					if(MeTweaks.lotr) {
						field_lotr_woodnames = LOTRBlockWoodBase.class.getDeclaredField("woodNames");
						field_lotr_woodnames.setAccessible(true);
						field_lotr_woodnamesBeam = LOTRBlockWoodBeam.class.getDeclaredField("woodNames");
						field_lotr_woodnamesBeam.setAccessible(true);
					}
					field_woodnames_old = ReflectionHelper.findField(BlockOldLog.class, new String[] {"field_150168_M"});
					field_woodnames_new = ReflectionHelper.findField(BlockNewLog.class, new String[] {"field_150169_M"});
					
					Field field_unlocalizednames = ReflectionHelper.findField(ItemMultiTexture.class, new String[] {"field_150942_c"});
					
					field_unlocalizednames.set(Item.getItemFromBlock(Blocks.log2), 
							new String[] {
							"acacia", "big_oak", null, null, 
							null, null, null, null, 
							null, null, null, null,
							"acacia.bark", "big_oak.bark", null, null});
					field_unlocalizednames.set(Item.getItemFromBlock(Blocks.log), 
							new String[] {
							"oak", "spruce", "birch", "jungle", 
							null, null, null, null, 
							null, null, null, null, 
							"oak.bark", "spruce.bark", "birch.bark", "jungle.bark"});
					
					
					
				} catch (Exception e) { e.printStackTrace(); }
			
			LinkedHashMap<Block, Integer> beamAndMeta = null;// = new LinkedHashMap<>();
					
			for(Block log : rotateblocks) {
				Field subtypesfield = null;
				if(log instanceof BlockOldLog)
					subtypesfield = field_woodnames_old;
				if(log instanceof BlockNewLog)
					subtypesfield = field_woodnames_new;
				
				if(MeTweaks.lotr) {
					if(log instanceof LOTRBlockWoodBase)
						subtypesfield = field_lotr_woodnames;
					if(log instanceof LOTRBlockWoodBeam)
						subtypesfield = field_lotr_woodnamesBeam;
						
				}
					
				if(subtypesfield == null)
					continue;
				
				int subtypes = 0;
				try {
					subtypes = ( ( Object[] ) subtypesfield.get(log) ).length;
				}catch (Exception e){
					e.printStackTrace();
					
				}
				
				for(int indexer = 0; indexer < subtypes; indexer++) {
					//									    num  barkmeta-offset
					GameRegistry.addRecipe(new ItemStack(log, 3, 12 + indexer), new Object[] {"##", "##", '#', new ItemStack(log, 1, indexer)});
				}
				
				
				
				boolean isBeam = MeTweaks.lotr && log instanceof LOTRBlockWoodBeam;
				
				if(isBeam) {
					if(MeTweaksConfig.beamSlabs) {
						if(beamAndMeta == null) beamAndMeta = new LinkedHashMap<>();
						
						beamAndMeta.put(log, subtypes);
					}
				}
				else {
					boolean isLotrWood = MeTweaks.lotr && log instanceof LOTRBlockWoodBase;
					if(MeTweaksConfig.barkSlabs/* && log instanceof BlockLog*/) {
						MeTweaksSlab slab = new MeTweaksSlab(subtypes, 12, 1, log);
						slabs.add(slab);
						if(isLotrWood) {
							slab.setCreativeTab(getBlocksTabLOTR());
						}
						for(int i = 0; i < subtypes; i++) {
							GameRegistry.addRecipe(new ItemStack(slab, 6, i), new Object[] {"###", '#', new ItemStack(log, 1, 12 + i)});
						}
					}
					
					if(MeTweaksConfig.barkWalls) {
					
						MeTweaksWall wall = new MeTweaksWall(log, subtypes);
						if(isLotrWood) {
							wall.setCreativeTab(getBlocksTabLOTR());
						}
						for(int i = 0; i < subtypes; i++) {
							GameRegistry.addRecipe(new ItemStack(wall, 6, i), new Object[] {"###", "###", '#', new ItemStack(log, 1, 12 + i)});
						}
					}
					
					if(MeTweaksConfig.barkStairs) {
						// processing every 2 subtypes
						for(int i = 0; i < subtypes; i += 2) {
							//									render offset of 12,		i is always 1 smaller than subtypes plus it need to be smaller so <=
							MeTweaksStairs stairs = new MeTweaksStairs(log, i + 12, i >> 1, (i + 1) < subtypes ? 2 : 1);
							if(isLotrWood) {
								stairs.setCreativeTab(getBlocksTabLOTR());
							}
							
							for(int k = i; k < (i + 2); k++) {
								// 												0 or 8														render offset of 12
								GameRegistry.addRecipe(new ItemStack(stairs, 4, (k & 1) << 3), new Object[] {"#  ", "## ", "###", '#', new ItemStack(log, 1, k + 12)});
							}
						}
					}
				}
				
				
				
				
				
				
				
				
			}
			
			if(beamAndMeta != null)
			for(Block log : beamAndMeta.keySet()) {
				int subtypes = beamAndMeta.get(log);
				MeTweaksSlab slab = new MeTweaksSlab(subtypes, 0, 1, log);
				slabs.add(slab);
				slab.isWoodBeam = true;
				for(int i = 0; i < subtypes; i++) {
					GameRegistry.addRecipe(new ItemStack(slab, 6, i), new Object[] {"###", '#', new ItemStack(log, 1, i)});
				}
				
				MeTweaksSlab slab2 = new MeTweaksSlab(subtypes, 8, 2, log);
				slabs.add(slab2);
				slab2.subBlocksOffset = 4;
				slab2.isWoodBeam = true;
				// chain
				slab2.secondary = slab;
				slab.secondary = slab2;
				slab2.setCreativeTab(getBlocksTabLOTR());
				slab.setCreativeTab(getBlocksTabLOTR());
				
				
				for(int i = 0; i < subtypes; i++) {
					GameRegistry.addRecipe(new ItemStack(slab2, 6, 4+i), new Object[] {"###", '#', new ItemStack(log, 1, 12+i)});
				}
			}
		}
		
		if(MeTweaksConfig.verticalSlabs) {
			Field field_subtypes = null;
			if(MeTweaks.lotr)
				try {
					field_subtypes = LOTRBlockSlabBase.class.getDeclaredField("subtypes");
					field_subtypes.setAccessible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			
			
			for(Block block : slabs) {
				int subtypes = 8;
				
				
				
				if(MeTweaks.lotr) {
					if(block instanceof LOTRBlockSlabBase)
					try {
						subtypes = field_subtypes.getInt(block);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if(block instanceof LOTRBlockSlabFalling) {
						new VerticalSlabFalling(block, subtypes, 0);
						continue;
					}
				}
				
				if(MeTweaks.chisel && block instanceof BlockCarvableSlab) {
					subtypes = 16;
					new VerticalChiselSlab(block, subtypes, 0);
					continue;
				}
				
				if(MeTweaks.etfuturum && block instanceof ISubBlocksBlock) {
					subtypes = ((ISubBlocksBlock) block).getTypes().length;
				}
				
				if(MeTweaks.netherEnderLicious) {
					int types = NetherEnderLiciousSlabs.getNumTypes(block);
					if(types != 8) {
						subtypes = types;
					}
				}
				
				if(VerticalSlab.isMeTweaksSlab(block)) {
					MeTweaksSlab rslab = (MeTweaksSlab) block;
					if(!rslab.isWoodBeam) {
						// beams need atleast 8 for their directions
						subtypes = rslab.types;
					}
				}
				
				if(block instanceof IShearable) {
					new VerticalSlabShearable(block, subtypes, 0);
				}
				else {
					new VerticalSlab(block, subtypes, 0);
				}
				
			}
			
			addInfo("Registered "+VerticalSlab.allverticals.size()+" Vertical-Slab-Containers");
			
		}
		
		if(debug.length() > 0) System.out.println(debug);
		debug = null;
	}
}
