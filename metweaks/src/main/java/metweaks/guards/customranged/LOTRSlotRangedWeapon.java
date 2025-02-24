package metweaks.guards.customranged;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.guards.customranged.CustomRanged.Category;
import metweaks.guards.customranged.CustomRanged.DefaultDataRanged;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class LOTRSlotRangedWeapon extends Slot {
	
	Category category;
	boolean server;

	public LOTRSlotRangedWeapon(LOTREntityNPC npc, IInventory inv, int i, int x, int y) {
		super(inv, i, x, y);
		
		if(server = !npc.worldObj.isRemote) {
			DefaultDataRanged ddr = CustomRanged.getOrCreate(npc);
			if(ddr != null) {
				category = ddr.category;
			}
		}
	}
	
	@Override
	public int getSlotStackLimit() {
		return 64; // need to increased later for throwables, 64 for plates
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		Item item = stack.getItem();
		if(server && (category == null || !CustomRanged.allowCategory(item, category))) {
			
			return false;
		}
		
		return Category.getCategoryStrict(item) != null;
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBackgroundIconIndex() {
		return CustomRanged.iconRangedWeapon;
	}
	
	// attack time min / max needs to be set based on ranged weapon

}
