package metweaks.guards;

import java.util.Random;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class PositionGenFix  {
	// entities with usual negative path weight will fail else
	
	private static Vec3 tempVector = Vec3.createVectorHelper(0, 0, 0);

    public static Vec3 findRandomTargetBlockTowards(EntityCreature creature, int horizontalRange, int yRange, Vec3 goal) {
        tempVector.xCoord = goal.xCoord - creature.posX;
        tempVector.yCoord = goal.yCoord - creature.posY;
        tempVector.zCoord = goal.zCoord - creature.posZ;
        return findRandomTargetBlock(creature, horizontalRange, yRange, tempVector, false);
    }

    public static Vec3 findRandomTargetBlockAwayFrom(EntityCreature creature, int horizontalRange, int yRange, Vec3 avoid) {
        tempVector.xCoord = creature.posX - avoid.xCoord;
        tempVector.yCoord = creature.posY - avoid.yCoord;
        tempVector.zCoord = creature.posZ - avoid.zCoord;
        return findRandomTargetBlock(creature, horizontalRange, yRange, tempVector, true);
    }
    
    // performance loos of a loop that is barely used
    // performance loss by vector objects, input could be ints and output chunkcoordinates
    // fix not leaving home area when inside home and have to avoid that pos (make a very small buffer? dont just kill the home verifiy to prevent issues)
    // fix min distance sometimes too low so they dont move
    // path should be relatively short but atleast a few blocks
    // prevent position in underground aswell as in air
    // try to find direct position towards instead of totally random?
    // if rider is hit but not mount it wont get mad, espechially wargs
    // 
    
    private static Vec3 findRandomTargetBlock(EntityCreature creature, int horizontalRange, int yRange, Vec3 towards, boolean homeBuffer) {
        Random random = creature.getRNG();
        boolean found = false;
        int x = 0;
        int y = 0;
        int z = 0;
        
        boolean nearHome;

        if(creature.hasHome()) {
            double distance = creature.getHomePosition().getDistanceSquared(MathHelper.floor_double(creature.posX), MathHelper.floor_double(creature.posY), MathHelper.floor_double(creature.posZ)) + 4;
            double homeRange = creature.func_110174_bM() + horizontalRange;
            nearHome = distance < homeRange * homeRange;
        }
        else {
            nearHome = false;
        }
        
        float homeRange = Math.max(homeBuffer ? 8 : 0, creature.func_110174_bM());

        for(int trys = 0; trys < 10; trys++)  {
            int tempX = random.nextInt(2 * horizontalRange) - horizontalRange;
            int tempY = random.nextInt(2 * yRange) - yRange;
            int tempZ = random.nextInt(2 * horizontalRange) - horizontalRange;

            if (towards == null || tempX * towards.xCoord + tempZ * towards.zCoord >= 0) {
                tempX += MathHelper.floor_double(creature.posX);
                tempY += MathHelper.floor_double(creature.posY);
                tempZ += MathHelper.floor_double(creature.posZ);

                if(!nearHome || creature.getHomePosition().getDistanceSquared(tempX, tempY, tempZ) < homeRange) {
                	 x = tempX;
                     y = tempY;
                     z = tempZ;
                     found = true;
                     break;
                }
                
                
            }
        }

        return found ? Vec3.createVectorHelper(x, y, z) : null; 
    }
}