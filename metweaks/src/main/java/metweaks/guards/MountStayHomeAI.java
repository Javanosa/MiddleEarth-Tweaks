package metweaks.guards;

import lotr.common.entity.npc.LOTREntityNPC;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class MountStayHomeAI extends EntityAIBase {
	
	public EntityCreature mount;
	
	public static void applyHiredMountHome(Entity mount) {
		
		if(mount.riddenByEntity != null && !mount.worldObj.isRemote 
			&& mount instanceof EntityCreature && !(mount instanceof LOTREntityNPC) && mount.riddenByEntity instanceof LOTREntityNPC) {
			// rider must be LOTREntityNPC because of custom npc's, noppes.npcs.entity.EntityNPCInterface, maybe home pos is also 0, 0, 0
			// mount not LOTREntityNPC because they have a working home search
			
			EntityCreature horse = (EntityCreature) mount;
			horse.tasks.addTask(3, new MountStayHomeAI(horse, (EntityCreature) horse.riddenByEntity));
			
		}
	}
	
	public MountStayHomeAI(EntityCreature horse, EntityCreature rider) {
		mount = horse;
		setMutexBits(3);
		// we dont want the horse to wander away
		
		ChunkCoordinates home = rider.getHomePosition();
		mount.setHomeArea(home.posX, home.posY, home.posZ, getHomeRange(rider));
	}
	
	public int getHomeRange(EntityCreature npc) {
		return (int) Math.ceil(npc.func_110174_bM() + mount.width);
	}
	
	@Override
	public void updateTask() {
		
		EntityCreature rider = (EntityCreature) mount.riddenByEntity;
		ChunkCoordinates home = rider.getHomePosition();
		int range = getHomeRange(rider);
		int x = home.posX;
		int y = home.posY;
		int z = home.posZ;
		
		double distToHome = mount.getDistance(x + 0.5, y + 0.5, z + 0.5);
		// deteach for randomposgen
		mount.getLookHelper().setLookPosition(x, y, z, 10, mount.getVerticalFaceSpeed());
		rider.getLookHelper().setLookPosition(x, y, z, 10, rider.getVerticalFaceSpeed());
		
		mount.detachHome();
		
          double speed = 1.0 / mount.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * 0.37;
         
          if(distToHome < rider.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue() && mount.worldObj.blockExists(x, y, z)) {
        	 
          	mount.getNavigator().tryMoveToXYZ(x + 0.5D, y + 0.5D, z + 0.5D, speed);
          } 
          else {
            Vec3 path = null;
            for(int attempts = 0; attempts < 16 && path == null; attempts++)
              path = PositionGenFix.findRandomTargetBlockTowards(mount, 8, 7, Vec3.createVectorHelper(x, y, z)); 
            if (path != null) {
          	  mount.getNavigator().tryMoveToXYZ(path.xCoord, path.yCoord, path.zCoord, speed);
          	  
            }
          } 
          
          
       // return for randomposgen
        mount.setHomeArea(x, y, z, range);
	}
	
	public boolean outOfRange(EntityCreature rider) {
		ChunkCoordinates home = rider.getHomePosition();
		int range = getHomeRange(rider);
		boolean outOfRange = home.getDistanceSquared(MathHelper.floor_double(mount.posX), MathHelper.floor_double(mount.posY), MathHelper.floor_double(mount.posZ)) > range * range;
		if(outOfRange) {
			// we dont want the horse to wander away
			mount.setHomeArea(home.posX, home.posY, home.posZ, range);
		}
		return outOfRange;
	}

	@Override
	public boolean shouldExecute() {
		
		if(mount.riddenByEntity != null && mount.riddenByEntity instanceof EntityCreature) {
			EntityCreature rider = (EntityCreature) mount.riddenByEntity;
			
			if(rider.hasHome() && rider.getAttackTarget() == null && mount.getNavigator().noPath() && outOfRange(rider)) {
				
				return true;
			}
		}
		return false;
	}
}
