package metweaks.client.features;

import static metweaks.events.GuardEvents.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import metweaks.client.healthbar.RenderHealthBar;
import metweaks.client.gui.unitoverview.GuiUnitOverview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class NpcMarking {
	private static FloatBuffer BUF_FLOAT_4;
	
	private static void enableMarking(int color) {
		if(BUF_FLOAT_4 == null) BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4);
        BUF_FLOAT_4.put(0, (color >> 16 & 255) / 255F);
        BUF_FLOAT_4.put(1, (color >> 8 & 255) / 255F);
        BUF_FLOAT_4.put(2, (color >> 0 & 255) / 255F);
        BUF_FLOAT_4.put(3, (color >> 24 & 255) / 255F);
        
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, 34160);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        
    }

	private static void disableMarking() {
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE);
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE);
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
    	GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
    }
	
	public static void reset(PlayerInteractEvent event) {
		GuiUnitOverview.resetMarkNpc();
		event.setCanceled(true);
	}
	
	public static void tick() {
		markNpcTicks--;
		
		if(markNpcTicks <= 0) {
			markNpcID = -1;
		}
	}
	
	public static void render(RenderWorldLastEvent event) {
		Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(markNpcID);
		if(entity != null && RenderHealthBar.isInRangeToRenderDist(Minecraft.getMinecraft().thePlayer.getDistanceSqToEntity(entity), entity)) {
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			
			enableMarking(0x4043c211);
			
			float ticks = event.partialTicks;
			double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ticks - RenderManager.renderPosX;
	        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks - RenderManager.renderPosY;
	        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ticks - RenderManager.renderPosZ;
	        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * ticks;
	        RenderManager render = RenderManager.instance;
	        
	        boolean prev = render.options.fancyGraphics;
	        // avoid drawing shadow
	        render.options.fancyGraphics = false;
	       
	        GL11.glColor4f(0.0f, 0.0F, 0.0F, 0.2F);
	        render.renderEntityWithPosYaw(entity, x, y, z, yaw, ticks);
	        render.options.fancyGraphics = prev;
			
			
			disableMarking();
			
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_BLEND);
		}
		else {
			GuiUnitOverview.resetMarkNpc();
		}
	}
}
