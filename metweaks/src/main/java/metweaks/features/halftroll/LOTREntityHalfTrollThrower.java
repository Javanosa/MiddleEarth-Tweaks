package metweaks.features.halftroll;

import lotr.common.entity.ai.LOTREntityAIRangedAttack;
import lotr.common.entity.npc.LOTREntityHalfTrollWarrior;
import lotr.common.entity.npc.LOTREntityMountainTroll;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.projectile.LOTREntityThrownRock;
import metweaks.guards.customranged.CustomRanged;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.World;

public class LOTREntityHalfTrollThrower extends LOTREntityHalfTrollWarrior {
	
	public LOTREntityHalfTrollThrower(World world) {
		super(world);
	}
	
	public EntityAIBase createHalfTrollAttackAI() {
		return new LOTREntityAIRangedAttack(this, 1.0D, 30, 60, 24.0F);
	}
	
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getAttributeMap().registerAttribute(LOTREntityMountainTroll.thrownRockDamage);
	}
	
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(21, (byte) 0);
	}
	
	public boolean isThrowingRocks() {
		return dataWatcher.getWatchableObjectByte(21) == 1;
	}
	
	public void setThrowingRocks(boolean flag) {
		dataWatcher.updateObject(21, (byte) (flag ? 1 : 0));
	}
	
	protected void onAttackModeChange(LOTREntityNPC.AttackMode mode, boolean mounted) {
		if(mode == LOTREntityNPC.AttackMode.IDLE) {
			setThrowingRocks(false);
		} else {
			setThrowingRocks(true);
		} 
	}
	
	public void attackEntityWithRangedAttack(EntityLivingBase target, float speed) {
		speed = Math.max(0.8F, speed * 1.5F);
		LOTREntityThrownRock rock = new LOTREntityThrownRock(worldObj, this);
		rock.setDamage((float) getEntityAttribute(LOTREntityMountainTroll.thrownRockDamage).getAttributeValue());
		EntityArrow template = new EntityArrow(worldObj, this, target, speed, 0.5F);
		rock.setLocationAndAngles(template.posX, template.posY, template.posZ, template.rotationYaw, template.rotationPitch);
		CustomRanged.setupProjectileVelocity(rock, target, speed, 0.99F, 0.1F, 0.5F);
		worldObj.spawnEntityInWorld(rock);
		playSound(getLivingSound(), getSoundVolume(), getSoundPitch() * 0.75F);
		swingItem();
	}
	
	public float getAlignmentBonus() {
		return 3F;
	}
}
