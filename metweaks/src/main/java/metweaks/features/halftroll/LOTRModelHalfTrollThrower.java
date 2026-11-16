package metweaks.features.halftroll;

import lotr.client.model.LOTRModelHalfTroll;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class LOTRModelHalfTrollThrower extends LOTRModelHalfTroll {
	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		LOTREntityHalfTrollThrower troll = (LOTREntityHalfTrollThrower) entity;
		boolean aiming = troll.isThrowingRocks();
		// remove riding status shortly to avoid weird arm movement during riding and aiming
		boolean prevRiding = isRiding;
		isRiding = isRiding && !aiming;
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		isRiding = prevRiding;
		// setup arm rotation for holding the rock
		if(aiming) {
			ModelRenderer rightArm = bipedRightArm;
			rightArm.rotateAngleX -= 0.5F;
			rightArm.rotateAngleZ -= 0.4F;
			bipedLeftArm.rotateAngleX = rightArm.rotateAngleX;
			bipedLeftArm.rotateAngleY = -rightArm.rotateAngleY;
			bipedLeftArm.rotateAngleZ = -rightArm.rotateAngleZ;
		}
	}
}
