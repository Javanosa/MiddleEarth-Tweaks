package metweaks.guards;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class DontSuffocateAI extends EntityAIBase {
	
	public EntityCreature mount;
	public int nextPath;
	
	// the question is, do you still see mount and rider disconnected when you rejoin? Does the "This mount belonged to an npc" pop up on interaction?
	public DontSuffocateAI(EntityCreature creature) {
		mount = creature;
	}
	
	public static boolean onAllCreatures = false;

	public static void applyDontSuffocate(Entity mount) {
		if(mount.riddenByEntity != null && !mount.worldObj.isRemote 
			&& mount instanceof EntityCreature && mount.riddenByEntity instanceof EntityCreature) {
			EntityCreature creature = (EntityCreature) mount;
			creature.tasks.addTask(3, new DontSuffocateAI(creature));
			
		}
	}
	
	public boolean isSuffocating() {
		Entity rider = mount.riddenByEntity;
		int y = MathHelper.floor_double((rider.getEyeHeight() - 0.05F) + rider.posY);
		if(y >= 0 && y < 256) {
			double posX = rider.posX;
			double posZ = rider.posZ;
			float width = rider.width * 0.8F;
			for (int side = 0; side < 4; side++) {
				 int x = MathHelper.floor_double(posX + (((side & 1) - 0.5F) * width));
			     int z = MathHelper.floor_double(posZ + (((side >> 1 & 1) - 0.5F) * width));
			     if(rider.worldObj.getChunkFromBlockCoords(x, z).getBlock(x & 15, y, z & 15).isNormalCube()) {
			    	 
			    	 return true;
			     }
			}
		}
		return false;
	}
	
	
	
	
	
	public boolean unsafePos(double xCoord, double yCoord, double zCoord, Entity rider) {
		double distanceY = (mount.posY - rider.posY) + rider.getEyeHeight() + 0.05; // buffer?
		int x = MathHelper.floor_double(xCoord);
		int y = MathHelper.floor_double(yCoord + distanceY);
		int z = MathHelper.floor_double(zCoord);
		return rider.worldObj.getBlock(x, y, z).isNormalCube();
		
	}
	
	@Override
	public void updateTask() {
		int xzRange = 5;
		int yRange = 3;
		Vec3 dest = null;
		EntityCreature rider = (EntityCreature) mount.riddenByEntity;
		Vec3 avoid = Vec3.createVectorHelper(rider.posX, rider.posY, rider.posZ);
		
		double speed = 1.0 / mount.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.37;
		
		
		for(int trys = 0; trys < 16; trys++) {
			// enlarge if needed
			if(trys == 1) {
				xzRange = 8;
				yRange = 4;
			}
			else if(trys == 4) {
				xzRange = 16;
				yRange = 7;
			}
			dest = PositionGenFix.findRandomTargetBlockAwayFrom(rider, xzRange, yRange, avoid);
			
			if(dest != null && !unsafePos(dest.xCoord, dest.yCoord, dest.zCoord, rider)) {
				mount.getNavigator().tryMoveToXYZ(dest.xCoord, dest.yCoord, dest.zCoord, speed);
				break;
			}
		}
	}
	
	@Override
	public boolean continueExecuting() {
		// we only need to run once in a while
		return false;
	}

	@Override
	public boolean shouldExecute() {
		if(mount.riddenByEntity != null && nextPath < mount.ticksExisted && isSuffocating()) {
			nextPath = mount.ticksExisted + 80; // 4 seconds
			return true;
		}
		return false;
	}

}
