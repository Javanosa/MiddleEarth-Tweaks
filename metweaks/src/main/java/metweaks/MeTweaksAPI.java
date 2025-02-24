package metweaks;

import java.util.Set;

import javax.annotation.Nullable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.block.VerticalSlab;
import metweaks.block.VerticalSlabFalling;
import metweaks.command.CommandEntitySelector;
import metweaks.core.Hooks;
import metweaks.events.ClientEvents;
import metweaks.guards.customranged.CustomRanged;
import metweaks.network.GuardsOverviewPacket;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;

public class MeTweaksAPI {

	// add vertical manually here...or tell me to add compartility for your Slab class
	// subtypes is the amount of types your slab instance supports
	public static void addVerticalSlab(Block slab, int subtypes) {
		new VerticalSlab(slab, subtypes, 0);
	}
	
	// add vertical manually here...or tell me to add compartility for your Slab class
	// subtypes is the amount of types your slab instance supports
	public static void addVerticalSlabFalling(Block slab, int subtypes) {
		new VerticalSlabFalling(slab, subtypes, 0);
	}
	
	// set stateValue to null if you just wanna output on / off
	@SideOnly(Side.CLIENT)
	public static void actionBar(String message, boolean stateOnOff, int ticks, @Nullable String stateValue, boolean disableState) {
		ClientEvents.actionBar(message, stateOnOff, ticks, stateValue, disableState);
	}
	
	// block placement flags like world.setBlock()
	// direction 0: south 1: west 2: north 3: east
	// Please use getVerticalSlab() and getMetaForVertical() when placing multiple blocks
	public static void setVerticalSlab(World world, int x, int y, int z, Block slab, int subtype, int facing, int flags) {
		VerticalSlab vertical = getVerticalSlab(slab, subtype);
		world.setBlock(x, y, z, vertical, getMetaForVertical(vertical, subtype, facing), flags);
	}
	
	// meta of the normal slab
	// direction 0: south 1: west 2: north 3: east
	public static int getMetaForVertical(VerticalSlab vertical, int subtype, int facing) {
		return subtype - vertical.offset + (facing << 2); // same as * 4
	}
	
	public static VerticalSlab getVerticalSlab(Block slab, int meta) {
		int i = VerticalSlab.index(slab, meta);
		if(VerticalSlab.allverticals.containsKey(i)) {
			return VerticalSlab.allverticals.get(i);
		}
		return null;
	}
	
	public static void setVerticalMode(EntityPlayer player, boolean enable) {
		VerticalSlab.setVerticalMode(player, enable);
		VerticalSlab.clientVertical = enable;
	}
	
	public static boolean isVerticalMode(EntityPlayer player) {
		return Hooks.isVerticalMode(player);
	}
	
	// returns rgba, works with 6 and 8 digit
	public static int[] parseHexColor(String hex, int[] rgba_defaults, int defaultalpha) {
		hex = hex.replace("#", "");
		int[] color = new int[4];
		color[3] = defaultalpha; // set default alpha
		try {
			for(int i=0; i < Math.min(4, hex.length() >> 1); i++) {
				
				color[i] = Integer.valueOf(hex.substring(i << 1, (i << 1) + 2), 16);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Unable to parse color: \""+hex+"\"");
			return rgba_defaults;
		}
		return color;
	}
	
	// since forge one's is broken
	public static void renameConfigProperty(Configuration config, String category, String oldName, String newName) {
        ConfigCompartiblity.rename(config, category, oldName, newName);
    }
	
	// returns [List<Entity results, int newArgsOffset] 		
	// newArgsOffset == offsetArgsStart indicates that string isnt a selector
	public static Object[] selectEntities(int offsetArgsStart, String[] args, ICommandSender sender) {
		return CommandEntitySelector.selectEntities(offsetArgsStart, args, sender);
	}
	
	// duration in ticks, default is 32
	// if you have access to the corresponding food class, please override getMaxEatingDuration() instead.
	public static void setConsumeDuration(Item food, int duration) {
		Hooks.consumeDurations.put(food, duration);
	}
	
	// updates move range for ranged ai
	public static void updateMoveRange(LOTREntityNPC npc, float ammoRange) {
		if(ASMConfig.aiRangedImprovements)
			CustomRanged.updateMoveRange(npc, ammoRange);
	}
	
	// whether an entity will show only a ranged weapon in the unit overview (currently)
	public static Set<Class<? extends Entity>> getRangedOnlyEntities() {
		return GuardsOverviewPacket.rangedEntitiesOnly;
	}
	
	// whether an entity is ranged
	public static Set<Class<? extends Entity>> getRangedEntities() {
		return GuardsOverviewPacket.rangedEntities;
	}
	
	
	
	
	
	
	
	
	
	// asm config
	
	
	
	
}
