package metweaks.client.healthbar;

import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.item.LOTRItemMountArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class LOTRFABridge {
	public static int getHorseArmorValue(EntityLivingBase living) {
		ItemStack stack = ((LOTREntityHorse) living).getMountArmor();
		if(stack != null) {
			Item item = stack.getItem();
			if(item instanceof LOTRItemMountArmor) {
				return ((LOTRItemMountArmor) item).getDamageReduceAmount();
			}
		}
		return 0;
	}
}
