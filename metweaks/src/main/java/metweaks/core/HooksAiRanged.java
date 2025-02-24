package metweaks.core;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public class HooksAiRanged {
	public static final Random rand = new Random();
	
	public static boolean getDecisionWait() {
		return rand.nextBoolean();
	}
	
	public static float getMoveRangeSq(EntityLiving theOwner, float attackRange) {
		float range = Math.min(theOwner.getNavigator().getPathSearchRange()-2, attackRange);
		return range * range;
		
	}
	
	public static EntityLivingBase execute(EntityLiving theOwner) {
		EntityLivingBase target = theOwner.getAttackTarget();
	    if(target == null || !target.isEntityAlive())
	    	return null;
	    
	    if(!theOwner.isEntityAlive())
	    	return null;
	    
	    return target;
	}
}
