package metweaks.guards.customranged;

import java.util.Iterator;

import lotr.common.entity.ai.LOTREntityAIHorseMoveToRiderTarget;
import lotr.common.entity.ai.LOTREntityAIRangedAttack;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.core.HooksAiRanged;
import metweaks.guards.NpcReflectionAccess;
import metweaks.guards.customranged.CustomRanged.Category;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.Vec3;

public class HorseMoveToRiderTargetAiFix extends EntityAIBase {
	LOTREntityHorse mount;
	
	double speed;
	PathEntity path;
	int pathTime;
	boolean aimingBow;
	
	// ranged only
	public float moveRangeSq;
	boolean decisionWait;
	int repath;
	
	
	
	@SuppressWarnings("unchecked")
	public static void apply(Entity mount) {
		if(!mount.worldObj.isRemote && mount instanceof LOTREntityHorse) {
			LOTREntityHorse creature = (LOTREntityHorse) mount;
			
			Iterator<EntityAITaskEntry> it = creature.tasks.taskEntries.iterator();
			while(it.hasNext()) {
				EntityAITaskEntry entry = it.next();
				if(entry.action instanceof LOTREntityAIHorseMoveToRiderTarget) {
					entry.action = new HorseMoveToRiderTargetAiFix(creature);
					return;
				}
			}
		}
	}
	
	public HorseMoveToRiderTargetAiFix(LOTREntityHorse horse) {
		mount = horse;
		setMutexBits(3);
	}
	
	@Override
	public boolean shouldExecute() {
		if(check()) {
			return false;
		}
		
		
		
		if(!mount.getBelongsToNPC())
			return false; 
		
		// do this based on ranged code
		LOTREntityNPC npc = ((LOTREntityNPC) mount.riddenByEntity);
		if(isAimingRanged(npc)) return true;
		
		path = mount.getNavigator().getPathToEntityLiving(npc.getAttackTarget());
		return path != null;
	}
	
	public boolean isAimingRanged(LOTREntityNPC npc) {
		ItemStack stack = npc.getHeldItem();
		if(stack != null) {
			Category category = Category.getCategoryStrict(stack.getItem());
			
			/*if(category == null) {
			return false;
			}*/
			
			if(category != null && category != Category.SPEAR) {
				return true;
			}
			// we would have to check SPEAR for melee weapon too. Too much.
			
			/*ItemStack ranged = npc.npcItemsInv.getStackInSlot(2); // slot for ranged weapon without copy
			
			// if our ranged weapon is a spear we use spear as if its ranged
			if(ranged != null && Category.getCategory(ranged.getItem()) == Category.SPEAR) {
			ItemStack melee = npc.npcItemsInv.getStackInSlot(1); // slot for melee weapon without copy
			if(melee != null && Category.getCategory(melee.getItem()) != Category.SPEAR) {
			return true;
			}
			}*/
		} 
		return false;
	}
	
	public boolean check() {
		Entity rider = mount.riddenByEntity;
		
		if(rider == null || !(rider instanceof LOTREntityNPC) || !rider.isEntityAlive())
			return true;
		EntityLivingBase target = ((LOTREntityNPC) rider).getAttackTarget();
		
		if (target == null || !target.isEntityAlive())
			return true;
		return false;
	}
	
	@Override
	public boolean continueExecuting() {
		if(check()) {
			return false;
		}
		
		return aimingBow || !mount.getNavigator().noPath(); // continue if has path. in case of ranged continue if still aiming.
	}
	
	@Override
	public void startExecuting() {
	
		LOTREntityNPC npc = ((LOTREntityNPC) mount.riddenByEntity);
		speed = npc.getEntityAttribute(LOTREntityNPC.horseAttackSpeed).getAttributeValue();
		
		if(path != null)
			mount.getNavigator().setPath(path, speed);
		pathTime = 0;
		moveRangeSq = HooksAiRanged.getMoveRangeSq(mount, NpcReflectionAccess.getAmmoRange(npc));
		decisionWait = HooksAiRanged.getDecisionWait();
	}
	
	@Override
	public void resetTask() {
		mount.getNavigator().clearPathEntity();
		aimingBow = false;
		repath = 0;
	}
	
	@Override
	public void updateTask() {
		LOTREntityNPC rider = (LOTREntityNPC) mount.riddenByEntity;
		EntityLivingBase target = rider.getAttackTarget();
		aimingBow = isAimingRanged(rider);
		
		if(aimingBow) {
			// if we are close enough then do this
			double distanceSq = rider.getDistanceSqToEntity(target);
			boolean canSee = mount.getEntitySenses().canSee(target);
			repath = canSee ? ++repath : 0;
			if(distanceSq <= moveRangeSq && (canSee || decisionWait)) {
				if(distanceSq < 25.0D) {
					Vec3 vec = LOTREntityAIRangedAttack.findPositionAwayFrom(rider, target, 8, 16);
					if (vec != null)
					mount.getNavigator().tryMoveToXYZ(vec.xCoord, vec.yCoord, vec.zCoord, speed); 
				
				}
				else if(repath >= 20) {
					mount.getNavigator().clearPathEntity();
					repath = 0;
				} 
				return; // we handle it ourself!
			}
		}
		else {
			mount.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
			rider.rotationYaw = mount.rotationYaw;
			rider.rotationYawHead = mount.rotationYawHead;
		}
		
		if (--pathTime <= 0) {
			pathTime = 4 + mount.getRNG().nextInt(7);
			mount.getNavigator().tryMoveToEntityLiving(target, speed);
		} 
	}
}
