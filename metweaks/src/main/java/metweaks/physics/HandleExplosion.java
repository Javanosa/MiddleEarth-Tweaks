package metweaks.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.LOTRBannerProtection;
import lotr.common.LOTRBannerProtection.IFilter;
import lotr.common.LOTRConfig;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTREntityWargBombardier;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.core.HooksPhysics;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;

public class HandleExplosion {
	
	// Prevent Falling Blocks from placing any fire into any liquid
	// Prevent Falling Blocks from placing into Banner Protection with no exploder permission
	// Place Sound when block place (random steps determined by maxPhysicsPerExplosion)
	// 
	// make /metweaks command to setup explosionFactor = 1.0, explosionHoleFactor = 0.5, explosionPhysics = ON, maxPhysicsPerExplosion = 400, physicsBannerProtection, explosionPhysicsFactor = 1.0;
	
	
	public HandleExplosion() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	
	public static boolean isBlacklisted(Block block) {
		if(block == Blocks.tnt) { // explosion chain
			return true;
		}
		if(MeTweaks.lotr) {
			if(block == LOTRMod.orcBomb // explosion chain
			|| block == LOTRMod.rhunFireJar // explosion chain (instant)
			|| block == LOTRMod.termiteMound // duplicate
			) {
				return true;
			}
		}
		if(block instanceof net.minecraft.block.BlockDoor) {
			return true;
		}
		return false;
	}
	
	
	public static boolean canFallingPlaceHere(PhysicsEntity falling, int x, int y, int z) {
		
		if(MeTweaks.lotr && LOTRConfig.allowBannerProtection && falling.protectionFilter != null) {
			LOTRBannerProtection.IFilter filter = (IFilter) falling.protectionFilter;
			if(LOTRBannerProtection.isProtected(falling.worldObj, x, y, z, filter, false)) {
				return false;
			}
		}
		if(falling.func_145805_f() instanceof BlockFire) {
			return !(falling.worldObj.getBlock(x, y, z) instanceof BlockLiquid);
		}
		
		return true;
	}
	
	public static boolean explosionPhysics = true;
	public static int explosionPhysicsFactor = 100;
	public static float maxPhysicsPerExplosion = 900;
	public static float fallingblockDropChance = 0.0F;
	public static float explosionSizeFactor = 1f;
	public static float holeDepthFactor = 1.0F;
	public static boolean decreaseBigExplosions = false;
	public static boolean forceBlockDamage = false;
	public static double maxWargPlayerDistance = -1;
	public static double maxWargPlayerDistanceSq = maxWargPlayerDistance * maxWargPlayerDistance;
	public static float particleFactor = 0.1F;
	public static int particleFactorActual = updateParticleFactorActual();
	public static boolean fastMoveImpl = false;
	
	public static int updateParticleFactorActual() {
		return particleFactorActual = Math.max(100 - explosionPhysicsFactor, (int) (particleFactor * 100F));
	}
	
	
	
	public static final Random rand = new Random();
	
	private static long nextFBCount;
	private static int cachedSoundPercent;
	
	@SuppressWarnings("rawtypes")
	public static void updateFBCount(final World world) {
		nextFBCount = 250L + System.currentTimeMillis();
		
		
		final List all = world.loadedEntityList;
		final Class fallingBlockClass = PhysicsEntity.class;
		int i = 0;
		for(final Object o : all) {
			if(o.getClass() == fallingBlockClass) {
				i++;
			}
		}
		cachedSoundPercent = (int) (100 * (100F / i));
	}

	
	public static boolean canPlaySound(final World world) {
		if(!ExplosionPhysicsSettings.playBlockSounds) return false;
		
		if(nextFBCount < System.currentTimeMillis()) {
			updateFBCount(world);
		}
		
		final int percent = cachedSoundPercent;
		if(percent < 100 && world.rand.nextInt(100) >= percent) {
			return false;
		}
		return true;
	}
	
	@SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event) {
		HooksPhysics.nonAirBlocks = 0; // reset
		Explosion explosion = event.explosion;
		// reduce bigger explosions
		if(decreaseBigExplosions && explosion.explosionSize > 4) {
    		explosion.explosionSize *= 0.8f;
    	}
		
		explosion.explosionSize *= explosionSizeFactor;
	}
	
	
	
	public static void performHoleFactor(Explosion explosion, List<ChunkPosition> blocks, World world) {

		
		
		// reduce hole depth
        if(explosion.explosionSize > 2 || holeDepthFactor < 0.2F) {
        	float explosionSize = explosion.explosionSize;
        	double x = explosion.explosionX;
        	double y = explosion.explosionY;
        	double z = explosion.explosionZ;
        	Random random = world.rand;
        	float percentSize = holeDepthFactor * (explosionSize * 0.25F) + 1;
        
        	blocks.removeIf(pos -> {
        		// prozentual reducing of layers
	        	// 80 - (63 - 59) * 100;
	        	double distance = y - pos.chunkPosY;
	        	
	        	if(explosionSize > 3) {
	        		distance--;
	        		
		        	if(Math.abs(z - pos.chunkPosZ) + random.nextInt(3) < percentSize
		        	|| Math.abs(x - pos.chunkPosX) + random.nextInt(3) < percentSize) {
		        		distance--;
		        	}
	        	}
	        	
	        	if(percentSize < distance) {
	        		
	        		return true;
					
				}
	        	return false;
        	});
        	
	        
        }
	}
	
	
	// was not static
	public static boolean abortBlockDestruction(Entity exploder) {
		if(maxWargPlayerDistance > 0 && exploder instanceof LOTREntityWargBombardier) {
			@SuppressWarnings("unchecked")
			List<EntityPlayer> players = exploder.worldObj.playerEntities;
			for(EntityPlayer player : players) {
				if(player.getDistanceSqToEntity(exploder) <= maxWargPlayerDistanceSq) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	// was not static
	public static void sendExplosionPacket(Explosion explosion, World world, List<ChunkPosition> blocks) {
		
		// isSmoking: require solid both, require air client if particles
		// isFlaming: require air server
		
		if(explosion.isFlaming && (particleFactorActual <= 0 || !explosion.isSmoking)) {
			blocks = new ArrayList<>(blocks); // so its only reduced for the client
		}
		
		
		if(!explosion.isSmoking) {
            blocks.clear();
        }
		
		if(particleFactorActual <= 0) {
			blocks.removeIf(pos -> {
				if(world.getBlock(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ) == Blocks.air) {
					return true;
				}
	        	return false;
        	});
		}
		
		
		List<EntityPlayerMP> players = world.playerEntities;
    	double explosionX = explosion.explosionX;
    	double explosionY = explosion.explosionY;
    	double explosionZ = explosion.explosionZ;
    	float explosionSize = explosion.explosionSize;
    	for(EntityPlayerMP player : players) {
    		if(player.getDistanceSq(explosionX, explosionY, explosionZ) < 4096.0D) {
                player.playerNetServerHandler.sendPacket(new S27PacketExplosion(explosionX, explosionY, explosionZ, explosionSize, blocks, (Vec3) explosion.func_77277_b().get(player)));
            }
    	}
	}
	
	private static int countGS;
	
	/*@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
		if(event.explosion.getClass() != Explosion.class) {
			// mod compatiblity
			onExplosionDetonate(event.explosion, event.world);
		}
    }*/
	
	@SuppressWarnings("unchecked")
	public static void onExplosionDetonate(Explosion explosion, World world) {
	
	
	/*@SuppressWarnings("unchecked")
	@SubscribeEvent(priority = EventPriority.LOWEST)
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
		
		// PASS 1: Determine block destruction or not
		Explosion explosion = event.explosion;
		
		
		World world = event.world;*/
    	List<ChunkPosition> blocks = explosion.affectedBlockPositions;
    	
    	// destruction mode: turn on block damage if any blocks available
    	if(forceBlockDamage) {
    		if(!blocks.isEmpty()) {
    			explosion.isSmoking = true;
    		}
    	}
    	else if(abortBlockDestruction(explosion.exploder)) {
	 		blocks.clear();
	 		explosion.isSmoking = false;
     	}
    	// "doDestroyBlocks" or no blocks
    	if(!explosion.isSmoking || blocks.isEmpty()) {
    		sendExplosionPacket(explosion, world, blocks);
    		return;
    	}

    	// PASS 2: Setup Blocklist results
    	if(holeDepthFactor < 1.0F)
    		performHoleFactor(explosion, blocks, world);
    	
    	// PASS 3: Send Explosion Packet
    	sendExplosionPacket(explosion, world, blocks);
    	 
    	
    	if(!explosionPhysics) return;
    	// PASS 4: Flying Blocks
    	
    	
    	countGS = 0;
    	
    	int all = blocks.size();
    	
    	int filtered = HooksPhysics.nonAirBlocks;
		
		
		// determine number based percent
		int percentMax = (int) (100 * (maxPhysicsPerExplosion / filtered));
		
		
		
		int percent = Math.min(percentMax, explosionPhysicsFactor);
		
		
		
		Random rand = world.rand;
		
		
		
		Object filter = getProtectionFilter(explosion);
    	
    	
    	
        
    	
    	
    	
    	
		
    	blocks.removeIf(pos -> {
			
			int x = pos.chunkPosX;
			int y = pos.chunkPosY;
			int z = pos.chunkPosZ;
			
			Block block = world.getBlock(x, y, z);
			// solid block or fire
			if(block.getMaterial().isSolid() || block instanceof net.minecraft.block.BlockFire) {
				// no tile entities for now
				if(isBlacklisted(block) || block.hasTileEntity(0)) {
					return false;
				}
				
				if(percent < 100 && rand.nextInt(100) >= percent) {
					return false;
				}

				
				countGS++;
				
				
				int metadata = world.getBlockMetadata(x, y, z);
				
				
				world.setBlock(x, y, z, Blocks.air, 0, 1); // place air without sending a blockchange to the player, because should be computed by explosion. Neighbor changes still get send.
				
				// convert block
				if(block == Blocks.grass) {
					block = Blocks.dirt;
				}
				else if(MeTweaks.lotr && block == LOTRMod.mudGrass) {
					block = LOTRMod.mud;
				}
				
				PhysicsEntity falling = new PhysicsEntity(world, x + 0.5, y + 0.90625, z + 0.5, block, metadata);
				double diffX = x - explosion.explosionX;
				double diffY = ((y - explosion.explosionY) * 0.2) + 0.3;
				double diffZ = z - explosion.explosionZ;
				
				double length = Math.sqrt((diffX * diffX) + (diffY * diffY) + (diffZ * diffZ));
			    diffX /= length;
			    diffY /= length;
			    diffZ /= length;

			   
				// dropItem
				falling.field_145813_c = false;
				
				// time
				falling.field_145812_b = 1;
				
				falling.func_145806_a(true);
				
		        
				falling.setHurtAmountAndMax(2F, 10);
		        falling.motionX = diffX * 0.4;
				falling.motionY = diffY + 0.5;
				falling.motionZ = diffZ * 0.4;
				
				
				if(filter != null && MeTweaks.lotr && LOTRConfig.allowBannerProtection) {
					
					falling.protectionFilter = filter;
				}
				
				falling.explosionSize = explosion.explosionSize;
				world.spawnEntityInWorld(falling);
				return true;
			}
			return false;
    	});

    	if(MeTweaksConfig.debug > 0)
    		FMLLog.getLogger().info("Blocks: " + all + " / " + filtered +" / "+ countGS+" / Perc "+percent);
    	
		
	}
	
	public static Object getProtectionFilter(Explosion explosion) {
		Object filter = null;
    	Entity exploder = explosion.exploder;
    	if(exploder != null && MeTweaks.lotr && LOTRConfig.allowBannerProtection) {
	    	if(exploder instanceof LOTREntityNPC) {
	    		filter = LOTRBannerProtection.anyBanner();
	    	}
	    	else if(exploder instanceof net.minecraft.entity.monster.EntityMob) {
	    		filter = LOTRBannerProtection.anyBanner();
	    	} 
	    	else if(exploder instanceof EntityThrowable) {
	    		filter = LOTRBannerProtection.forThrown((EntityThrowable) exploder);
	    	} 
	    	else if(exploder instanceof EntityTNTPrimed) {
	    		EntityTNTPrimed tnt = (EntityTNTPrimed) exploder;
	    		if(tnt.getTntPlacedBy() instanceof LOTREntityNPC) {
	    			// fix
	    			filter = LOTRBannerProtection.anyBanner();
	    		}
	    		else{
	    			filter = LOTRBannerProtection.forTNT(tnt);
	    		}
	    	}
	    	else if(exploder instanceof EntityMinecartTNT) {
	    		filter = LOTRBannerProtection.forTNTMinecart((EntityMinecartTNT) exploder);
	    	}
	    	else {
	    		filter = LOTRBannerProtection.anyBanner();
	    	}
    	}
    	return filter;
	}
	
	
}
