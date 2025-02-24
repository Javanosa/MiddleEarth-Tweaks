package metweaks.client.features;

import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.events.ClientEvents;
import metweaks.guards.HiredInfoAccess;
import metweaks.network.HiredAdvInfoPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import static metweaks.events.GuardEvents.*;

import org.lwjgl.opengl.GL11;

public class GuardsHomeSetup {
	public static void handleClick(PlayerInteractEvent event) {
		long time = System.currentTimeMillis();
		if(nextInteract > time)
			return;
		
		nextInteract = time+50;
		
		if(event.entityPlayer.isSneaking()) {
			if(event.action == Action.RIGHT_CLICK_BLOCK) {
				
				Entity entity = event.world.getEntityByID(guardHomeEntityID);
				if(entity instanceof LOTREntityNPC) {
					LOTREntityNPC npc = (LOTREntityNPC) entity;
					if(npc.hiredNPCInfo.getHiringPlayer() == event.entityPlayer) {
						int x = event.x;
						int y = event.y;
						int z = event.z;
						if (!event.world.getBlock(x, y, z).isReplaceable(event.world, x, y, z)) {
				     		
				        	 switch(event.face) {
					     		case 0: --y; break;
					     		case 1: ++y; break;
					     		case 2: --z; break;
					     		case 3: ++z; break;
					     		case 4: --x; break;
					     		case 5: ++x; break;
					     	}
				         }
						
						guardHomePosTicks = 120*20;
						guardHomePos = new ChunkCoordinates(x, y, z);
						ClientEvents.actionBar(StatCollector.translateToLocal("lotr.gui.warrior.notifyhome"), true, 120*20, x+", "+y+", "+z, false); // Home set to
						npc.setHomeArea(x, y, z, HiredInfoAccess.getWanderRange(npc.hiredNPCInfo));
						HiredAdvInfoPacket packet = new HiredAdvInfoPacket(npc);
					    NetworkHandler.networkWrapper.sendToServer(packet);
					    
					}
				}
				
				event.setCanceled(true);
			}
			
		}
		else {
			
			guardHomeEntityID = -1;
			guardHomePosTicks = 0;
			ClientEvents.actionBar(null, false, 0, null, false); // empty actionbar
			event.setCanceled(true);
		}
	}
	
	public static void render() {
		// render transparent colored box
		double minX = guardHomePos.posX - RenderManager.renderPosX;
		double minY = guardHomePos.posY - RenderManager.renderPosY - 1;
		double minZ = guardHomePos.posZ - RenderManager.renderPosZ;
		double maxX = minX + 1;
		double maxY = minY + 1;
		double maxZ = minZ + 1;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINE_STRIP);
        tessellator.setColorRGBA(0, 255, 0, 255);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.draw();
        
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA(0, 255, 0, 31);
        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);
        tessellator.draw();
        
       
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
	}
}
