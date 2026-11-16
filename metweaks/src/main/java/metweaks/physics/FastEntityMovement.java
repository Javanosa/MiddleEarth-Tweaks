
package metweaks.physics;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FastEntityMovement {
	
	
	static final List<AxisAlignedBB> collisionCache = new ArrayList<>();
	static final List<AxisAlignedBB> collisionCacheClient = new ArrayList<>();
	
	public static List<AxisAlignedBB> getCollidingBoundingBoxes(final World world, final Entity entity, final AxisAlignedBB box) {
		final List<AxisAlignedBB> list = world.isRemote ? collisionCacheClient : collisionCache;
		list.clear();
		
		final int minX = MathHelper.floor_double(box.minX);
		final int maxX = MathHelper.floor_double(box.maxX + 1.0D);
		final int minY = MathHelper.floor_double(box.minY);
		final int maxY = MathHelper.floor_double(box.maxY + 1.0D);
		final int minZ = MathHelper.floor_double(box.minZ);
		final int maxZ = MathHelper.floor_double(box.maxZ + 1.0D);

        for(int x = minX; x < maxX; ++x) {
            for (int z = minZ; z < maxZ; ++z) {
                if (world.blockExists(x, 64, z)) {
                    for (int y = minY - 1; y < maxY; ++y) {
                        Block block = world.getBlock(x, y, z);
                        block.addCollisionBoxesToList(world, x, y, z, box, list, entity);
                    }
                }
            }
        }
        
        return list;
	}

}
