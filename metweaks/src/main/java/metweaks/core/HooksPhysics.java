package metweaks.core;

import metweaks.physics.HandleExplosion;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class HooksPhysics {
	public static int nonAirBlocks = 0;
	
	public static void incrementBlock(boolean added, Block block) {
		if(!added || block == Blocks.air) return;
		//HashSet hashset = new HashSet();
		//hashset.add(hashset);
		nonAirBlocks++;
	}
	
	public static boolean canSpawnParticles()  {
		int percent = HandleExplosion.particleFactorActual;
		if(percent <= 0) {
			return false;
		}
		if(percent < 100 && HandleExplosion.rand.nextInt(100) >= percent) {
			return false;
		}
		return true;
	}
	
	public static void postDetonate(Explosion explosion, World world) {
		HandleExplosion.onExplosionDetonate(explosion, world);
	}
}
