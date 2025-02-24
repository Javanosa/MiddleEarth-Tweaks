package metweaks.features.isolated;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.LOTRBannerProtection;
import lotr.common.LOTRBannerProtection.IFilter;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;

public class NpcExplosionProtection {
	
	public NpcExplosionProtection() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void onExplosionDetonateProtect(ExplosionEvent.Detonate event) {
		if(ASMConfig.npcExplosionProtection && MeTweaks.lotr) {
			// if tnt primed explodes with valid epxloder but exploder has access
			// fireball = exploder null
			World world = event.world;
			Explosion ex = event.explosion;
			Entity exploder = ex.exploder;
			if(!world.isRemote && exploder instanceof EntityTNTPrimed) {
				if(((EntityTNTPrimed) exploder).getTntPlacedBy() instanceof LOTREntityNPC) {
					@SuppressWarnings("unchecked")
					List<ChunkPosition> blocks = ex.affectedBlockPositions;
					List<ChunkPosition> toRemove = new ArrayList<ChunkPosition>();
					IFilter filter = LOTRBannerProtection.anyBanner();
					for(ChunkPosition pos : blocks) {
						if(LOTRBannerProtection.isProtected(world, pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ, filter, false))
							toRemove.add(pos); 
					}
					
					blocks.removeAll(toRemove);
				}
			}
		}
	}
}
