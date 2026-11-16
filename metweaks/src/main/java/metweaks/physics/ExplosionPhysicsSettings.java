package metweaks.physics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.FMLLog;
import metweaks.MeTweaksConfig;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;

public class ExplosionPhysicsSettings {
	public static boolean needsSave;
	public static final String name = "ExplosionPhysics.dat";
	
	private static File getExplosionPhysicsDat() {
	    return new File(DimensionManager.getCurrentSaveRootDirectory(), name);
	}
	
	public static void load() {
			
			File file = getExplosionPhysicsDat();
			if(!file.exists()) return; // nothing to load

			NBTTagCompound nbt;
			
			try{
				FileInputStream input = new FileInputStream(file);
			    nbt = CompressedStreamTools.readCompressed(input);
			    input.close();
			}
			catch (IOException e) {
				FMLLog.severe("Error loading "+name);
		        e.printStackTrace();
		        return;
			}
			
			if(nbt.hasKey("ExplosionPhysics", 99)) {
				HandleExplosion.explosionPhysics = nbt.getBoolean("ExplosionPhysics");
			}
			
			if(nbt.hasKey("ExplosionPhysicsFactor", 99)) {
				HandleExplosion.explosionPhysicsFactor = nbt.getInteger("ExplosionPhysicsFactor");
			}
			
			if(nbt.hasKey("MaxPhysicsPerExplosion", 99)) {
				HandleExplosion.maxPhysicsPerExplosion = nbt.getInteger("MaxPhysicsPerExplosion");
			}
			
			if(nbt.hasKey("PhysicsDropChance", 99)) {
				HandleExplosion.fallingblockDropChance = nbt.getFloat("PhysicsDropChance");
			}
			
			if(nbt.hasKey("SizeFactor", 99)) {
				HandleExplosion.explosionSizeFactor = nbt.getFloat("SizeFactor");
			}
			
			if(nbt.hasKey("HoleDepthFactor", 99)) {
				HandleExplosion.holeDepthFactor = nbt.getFloat("HoleDepthFactor");
			}
			
			if(nbt.hasKey("DecreaseBigExplosions", 99)) {
				HandleExplosion.decreaseBigExplosions = nbt.getBoolean("DecreaseBigExplosions");
			}
			
			if(nbt.hasKey("ForceBlockDamage", 99)) {
				HandleExplosion.forceBlockDamage = nbt.getBoolean("ForceBlockDamage");
			}
			
			if(nbt.hasKey("MaxWargPlayerDistance", 99)) {
				HandleExplosion.maxWargPlayerDistance = nbt.getDouble("MaxWargPlayerDistance");
			}
			
			if(nbt.hasKey("ParticleFactor", 99)) {
				HandleExplosion.particleFactor = nbt.getFloat("ParticleFactor");
			}
			
			if(nbt.hasKey("FastMoveImpl", 99)) {
				HandleExplosion.fastMoveImpl = nbt.getBoolean("FastMoveImpl");
			}
			
			// resync cached
			HandleExplosion.updateParticleFactorActual();
			HandleExplosion.maxWargPlayerDistanceSq = HandleExplosion.maxWargPlayerDistance * HandleExplosion.maxWargPlayerDistance;
	}
	
	
	public static void save() {
			
			if(needsSave) {
				needsSave = false;
				NBTTagCompound nbt = new NBTTagCompound();
				
				nbt.setBoolean("ExplosionPhysics", HandleExplosion.explosionPhysics);
				nbt.setInteger("ExplosionPhysicsFactor", HandleExplosion.explosionPhysicsFactor);
				nbt.setInteger("MaxPhysicsPerExplosion", (int) HandleExplosion.maxPhysicsPerExplosion);
				nbt.setFloat("PhysicsDropChance", HandleExplosion.fallingblockDropChance);
				nbt.setFloat("SizeFactor", HandleExplosion.explosionSizeFactor);
				nbt.setFloat("HoleDepthFactor", HandleExplosion.holeDepthFactor);
				nbt.setBoolean("DecreaseBigExplosions", HandleExplosion.decreaseBigExplosions);
				nbt.setBoolean("ForceBlockDamage", HandleExplosion.forceBlockDamage);
				nbt.setDouble("MaxWargPlayerDistance", HandleExplosion.maxWargPlayerDistance);
				nbt.setFloat("ParticleFactor", HandleExplosion.particleFactor);
				nbt.setBoolean("FastMoveImpl", HandleExplosion.fastMoveImpl);
				
				try {
					CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(getExplosionPhysicsDat()));
				} catch (IOException e) {
					FMLLog.severe("Error saving "+name);
					e.printStackTrace();
				}
			}
			
			//if(!MinecraftServer.getServer().isDedicatedServer()) {
			//	setFromDefaults();
			//}	
	}
	
	public static void setFromDefaults() {
		HandleExplosion.explosionPhysics = explosionPhysics;
		HandleExplosion.explosionPhysicsFactor = explosionPhysicsFactor;
		HandleExplosion.maxPhysicsPerExplosion = maxPhysicsPerExplosion;
		HandleExplosion.fallingblockDropChance = (float) fallingblockDropChance;
		HandleExplosion.explosionSizeFactor = (float) explosionSizeFactor;
		HandleExplosion.holeDepthFactor = (float) holeDepthFactor;
		HandleExplosion.decreaseBigExplosions = decreaseBigExplosions;
		HandleExplosion.forceBlockDamage = forceBlockDamage;
		HandleExplosion.maxWargPlayerDistance = maxWargPlayerDistance;
		HandleExplosion.particleFactor = (float) particleFactor;
		HandleExplosion.fastMoveImpl = fastMoveImpl;
		HandleExplosion.updateParticleFactorActual();
		HandleExplosion.maxWargPlayerDistanceSq = HandleExplosion.maxWargPlayerDistance * HandleExplosion.maxWargPlayerDistance;
	}
	
	public static boolean explosionPhysics = true;
	public static int explosionPhysicsFactor = 100;
	public static float maxPhysicsPerExplosion = 800;
	public static double fallingblockDropChance = 0.0;
	public static double explosionSizeFactor = 1.0;
	public static double holeDepthFactor = 1.0;
	public static boolean decreaseBigExplosions = false;
	public static boolean forceBlockDamage = false;
	public static double maxWargPlayerDistance = -1.0;
	public static double particleFactor = 0.1;
	public static boolean fastMoveImpl = false;
	public static float shadowSize = 0.5F;
	public static boolean playBlockSounds = true;
	public static boolean fastRenderImpl = true;
	
	public static void loadConfig() {
		Configuration config = MeTweaksConfig.config;
		String CATEGORY_EP = MeTweaksConfig.CATEGORY_EXPLOSION_PHYSICS;
		
		List<String> ord = new ArrayList<>(2);
		ord.add("Enable Explosion Physics");
		ord.add("Explosion Physics Factor");
		config.getCategory(CATEGORY_EP).setPropertyOrder(ord);
		
		explosionPhysics = config.get(CATEGORY_EP, "Enable Explosion Physics", explosionPhysics).getBoolean();
		explosionPhysicsFactor = (int) (100 * config.get(CATEGORY_EP, "Explosion Physics Factor", explosionPhysicsFactor * 0.01, "Factor of Physics Blocks vs normal Explosion Damage", 0.0, 1.0).getDouble());
		maxPhysicsPerExplosion = config.get(CATEGORY_EP, "Max Physics Per Explosion", (int) maxPhysicsPerExplosion).getInt();
		fallingblockDropChance = config.get(CATEGORY_EP, "Physics Drop Chance", fallingblockDropChance, null, 0.0, 1.0).getDouble();
		explosionSizeFactor = config.get(CATEGORY_EP, "Size Factor", explosionSizeFactor).getDouble();
		holeDepthFactor = config.get(CATEGORY_EP, "Hole Depth Factor", holeDepthFactor, "How deep a hole is going to be", 0.0, 1.0).getDouble();
		decreaseBigExplosions = config.get(CATEGORY_EP, "Decrease Big Explosions", decreaseBigExplosions, "Slightly reduce the size of big explosions").getBoolean();
		forceBlockDamage = config.get(CATEGORY_EP, "Force Block Damage", forceBlockDamage, "Any Explosion does block damage no matter what").getBoolean();
		maxWargPlayerDistance = config.get(CATEGORY_EP, "Max Warg To Player Distance", maxWargPlayerDistance, "Max distance to a player where a warg does block damage").getDouble();
		particleFactor = config.get(CATEGORY_EP, "Particle Factor", particleFactor, "Normally Exploded Blocks still do particles", 0.0, 1.0).getDouble();
		fastMoveImpl = config.get(CATEGORY_EP, "Fast Movement Implementation", fastMoveImpl).getBoolean();
		shadowSize = config.getFloat("Shadow Size", CATEGORY_EP, shadowSize, 0.0F, 1.0F, "");
		playBlockSounds = config.get(CATEGORY_EP, "Play Block Sounds", playBlockSounds).getBoolean();
		fastRenderImpl = config.get(CATEGORY_EP, "Fast Render Implementation", fastRenderImpl).getBoolean();
		setFromDefaults();
	}
}
