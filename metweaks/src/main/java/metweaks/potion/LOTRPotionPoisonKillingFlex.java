package metweaks.potion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRDamage;
import lotr.common.LOTRMod;
import lotr.common.item.LOTRPoisonedDrinks;
import lotr.common.item.LOTRPotionPoisonKilling;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class LOTRPotionPoisonKillingFlex extends Potion{
	public LOTRPotionPoisonKillingFlex(int id) {
		super(id, true, Potion.poison.getLiquidColor());
		setPotionName("potion.lotr.drinkPoison");
		setIconIndex(0, 0);
		setEffectiveness(Potion.poison.getEffectiveness());
		LOTRPoisonedDrinks.killingPoison = this;

	}

	@Override
	public void performEffect(EntityLivingBase entity, int level) {
		entity.attackEntityFrom(LOTRDamage.poisonDrink, 1);
	}

	@Override
	public boolean isReady(int tick, int level) {
		int freq = 5 >> level;
		return (freq > 0) ? ((tick % freq == 0)) : true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasStatusIcon() { return false; }

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		LOTRMod.proxy.renderCustomPotionEffect(x, y, effect, mc);
	}
	
	
	public static void changePotionID(int oldid, int newid) {
		
		if(MeTweaks.lotr) {
			
			// remove old register
			if(oldid >= 0 && oldid <= 31) {
				Potion potion = Potion.potionTypes[oldid];
				if(potion != null) {
					if(MeTweaksConfig.debug >= 2)
						if(potion instanceof LOTRPotionPoisonKillingFlex || potion instanceof LOTRPotionPoisonKilling) {
							System.out.println("potionID " + potion.getName() + " changed from "+ oldid + " to "+newid);
						}
						else {
							System.out.println("potion " + potion.getName() + " of id "+ oldid + " removed");
						}
					Potion.potionTypes[oldid] = null;
				}
				
				
			}
			// add new register
			if(newid >= 0 && newid <= 31) {
				
				new LOTRPotionPoisonKillingFlex(newid);
			}
		}
			
		
	}
}
