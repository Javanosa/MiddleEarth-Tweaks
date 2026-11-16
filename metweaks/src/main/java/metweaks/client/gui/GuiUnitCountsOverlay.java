package metweaks.client.gui;

import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import lotr.client.gui.LOTRGuiHireBase;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.MeTweaksAPI;
import metweaks.MeTweaksConfig;
import metweaks.client.LOTRGuiElements;
import metweaks.client.healthbar.HealthBarConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class GuiUnitCountsOverlay {
	private static int hireds;
	private static String hiredsStr;
	
	public static int unitcountsX = 137; // position from Left (or Right if "unitcountsRight" is enabled)
	private static int unitcountsXTemp = unitcountsX;
	public static int unitcountsY = 0; // position from Top (Or Bottom if "unitcountsBottom" is enabled)
	public static boolean unitcountsRight = true;  // flip/move to the right side
	public static boolean unitcountsBottom = false; // flip/move to the bottom
	
	public static boolean unitcountsCenterVertical = false;  // center unitcounts vertical
	public static boolean unitcountsCenterHorizontal = false;  // center unitcounts horizontal
	
	public static float unitcountsScale = 1.0F;
	public static float unitcountsScaleInv = 1.0F;
	
	private static int unitcountsBufferX;
	private static int unitcountsBufferY;
	
	public static boolean unitcountsEnabled = true;
	public static int[] unitcountsColor;
	public static int unitcountsColorI;
	public static int[] unitcountsColorBackground;
	public static Item unitcountsIcon;
	public static int unitcountsIcon_meta;
	
	public static int getUnitcountsX(int width) {
		
		if(unitcountsCenterHorizontal) width /= 2;
		
		return LOTRGuiElements.getPosition(LOTRGuiElements.autoAdjust ? Math.min(unitcountsX, width - unitcountsBufferX) : unitcountsX, 		width, unitcountsRight, unitcountsCenterHorizontal);
	}
	
	public static int getUnitcountsY(int height) {
		
		if(unitcountsCenterVertical) height /= 2;
		
		return LOTRGuiElements.getPosition(LOTRGuiElements.autoAdjust ? Math.min(unitcountsY, height - unitcountsBufferY) : unitcountsY, 	height, unitcountsBottom, unitcountsCenterVertical);
	}
	
	public static int transformToInt(int[] rgba) {
		return (rgba[3] << 24) | (rgba[0] << 16) | (rgba[1] << 8) | (rgba[2]);
	}
	
	public static void loadConfig() {
		Configuration config = MeTweaksConfig.config;
		String CATEGORY_HUDELEMENTS = MeTweaksConfig.CATEGORY_HUDELEMENTS;
		
		unitcountsEnabled = config.get(CATEGORY_HUDELEMENTS, "Enable Unitcounts Overlay", unitcountsEnabled).getBoolean();
		
		String colordesc = "rgba (8 digit hex), Example: #ED731CEE";
		unitcountsColor = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUDELEMENTS, "Unitcounts Color", 
				"#6FFC03FF", colordesc, HealthBarConfig.hexcolorPattern).getString(), unitcountsColor, 128);
		unitcountsColorI = transformToInt(unitcountsColor);
		
		
		unitcountsColorBackground = MeTweaksAPI.parseHexColor(config.get(CATEGORY_HUDELEMENTS, "Unitcounts Background Color", 
				"#00000040", colordesc, HealthBarConfig.hexcolorPattern).getString(), unitcountsColorBackground, 128);
		
		
		
		String iconDesc = "Format: modid:item:meta";
		Property property = config.get(CATEGORY_HUDELEMENTS, "Unitcounts Icon", "lotr:item.commandSword:0", iconDesc);
		String prev = property.wasRead() ? property.getString() : property.getDefault();
		Object[] speed = HealthBarConfig.parseIcon(property, null, 0);
		property.set(prev);
		unitcountsIcon = (Item) speed[0];
		unitcountsIcon_meta = (int) speed[1];
		
		unitcountsX = config.get(CATEGORY_HUDELEMENTS, "Unitcounts X", unitcountsX, "position from Left (or Right if \"Unitcounts Right\" is enabled)").getInt();
		
		unitcountsY = config.get(CATEGORY_HUDELEMENTS, "Unitcounts Y", unitcountsY, "position from Top (Or Bottom if \"Unitcounts Bottom\" is enabled)").getInt();
		
		unitcountsRight = config.get(CATEGORY_HUDELEMENTS, "Unitcounts Right", unitcountsRight, "flip to the right side").getBoolean();
		unitcountsBottom = config.get(CATEGORY_HUDELEMENTS, "Unitcounts Bottom", unitcountsBottom, "flip to the bottom").getBoolean();
		
		unitcountsCenterVertical = config.get(CATEGORY_HUDELEMENTS, "Unitcounts Center Vertical", unitcountsCenterVertical).getBoolean();
		unitcountsCenterHorizontal = config.get(CATEGORY_HUDELEMENTS, "Unitcounts Center Horizontal", unitcountsCenterHorizontal).getBoolean();
		
		
		unitcountsScale = (float) config.get(CATEGORY_HUDELEMENTS, "Unitcounts Scale", unitcountsScale, null, 0.5, 5).getDouble();

		unitcountsScaleInv = 1F / unitcountsScale;
		
		if(LOTRGuiElements.autoAdjust) {
			int padding = 2;
			int vpadding = 4;
			int height = 7;

			
			unitcountsScale = MathHelper.clamp_float(unitcountsScale, 0.75f, 2);
			
			if(!unitcountsBottom) {
				height = 0;
			}
			
			unitcountsBufferX = (int) ((padding) * unitcountsScale);
			unitcountsBufferY = (int) ((height + vpadding) * unitcountsScale);
			
			unitcountsX = Math.max(unitcountsX, unitcountsBufferX);
			unitcountsY = Math.max(unitcountsY, unitcountsBufferY);
			
			if(unitcountsCenterVertical) {
				unitcountsY = 0;
			}
			
			if(unitcountsCenterHorizontal) {
				unitcountsX = 0;
			}
			
			unitcountsXTemp = unitcountsX;
		}
	}
	
	public static void tick() {
		int total = 0;
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		
		if(world != null && player != null) {
			UUID uuid = player.getUniqueID();
			@SuppressWarnings("unchecked")
			List<Entity> entities = world.loadedEntityList;
			for(Entity entity : entities) {
				if(entity instanceof LOTREntityNPC) {
					LOTREntityNPC npc = (LOTREntityNPC) entity;
					if(uuid.equals(npc.hiredNPCInfo.getHiringPlayerUUID())) {
						total++;
					}
				}
			}
			
			if(hireds != total) {
				hiredsStr = String.valueOf(total);
				
				if(LOTRGuiElements.autoAdjust && !unitcountsCenterHorizontal && unitcountsRight) {
					setupXTranslation(Minecraft.getMinecraft().fontRenderer.getStringWidth(hiredsStr));
				}
			}
		}
		
		hireds = total;
	}
	
	public static void setupXTranslation(int textwidth) {
		int padding = 2;
		boolean hasIcon = unitcountsIcon != null;
		int iconBuffer = hasIcon ? 11 + 2 : 0;
		int textlen = iconBuffer + textwidth;
		
		unitcountsBufferX = (int) ((textlen + padding) * unitcountsScale);
		unitcountsX = Math.max(unitcountsXTemp, unitcountsBufferX);
	}
	
	public static void render(ScaledResolution resolution) {
		Minecraft mc = Minecraft.getMinecraft();
		if(hireds > 0 && Minecraft.isGuiEnabled() && !mc.gameSettings.showDebugInfo && (mc.currentScreen == null || mc.currentScreen instanceof LOTRGuiHireBase)) {
			
			boolean hasIcon = unitcountsIcon != null;
			boolean hasBackground = unitcountsColorBackground[3] > 0; // alpha
			boolean hasScale = unitcountsScale != 1.0F;
			
			int x = getUnitcountsX(resolution.getScaledWidth());
			int y = getUnitcountsY(resolution.getScaledHeight());
			
			
			
			if(hasScale) {
				GL11.glPushMatrix();
				GL11.glScalef(unitcountsScale, unitcountsScale, 1.0F);
				x *= unitcountsScaleInv;
				y *= unitcountsScaleInv;
			}
			
			
			
			
			int iconBuffer = hasIcon ? 11 + 2 : 0;
			Tessellator tessellator = Tessellator.instance;
			
			if(hasBackground) {
				int padding = 2;
				int vpadding = 4;
				int height = 7;
				int textlen = iconBuffer + mc.fontRenderer.getStringWidth(hiredsStr);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				tessellator.startDrawingQuads();
				int[] rgba = unitcountsColorBackground;
				
				tessellator.setColorRGBA(rgba[0], rgba[1], rgba[2], rgba[3]);
				//tessellator.setColorRGBA(0, 0, 0, 64);
				tessellator.addVertex(x - padding, 			 y - vpadding, 0);
				tessellator.addVertex(x - padding, 			 y + height + vpadding, 0);
				tessellator.addVertex(x + textlen + padding, y + height + vpadding, 0);
				tessellator.addVertex(x + textlen + padding, y - vpadding, 0);
				tessellator.draw();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				
			}
			
			
			
			// draw icon
			if(hasIcon) {
				mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
				IIcon icon = unitcountsIcon.getIconFromDamage(unitcountsIcon_meta);
				if(icon != null) {
					tessellator.startDrawingQuads();
					tessellator.setColorOpaque_I(0xFFFFFF);
					int size = 11;
					int offset = 2; // offset Top
					tessellator.addVertexWithUV(x + 0, y - offset + size, 0, icon.getMinU(), icon.getMaxV());
				    tessellator.addVertexWithUV(x + size, y - offset + size, 0, icon.getMaxU(), icon.getMaxV());
				    tessellator.addVertexWithUV(x + size, y - offset + 0, 0, icon.getMaxU(), icon.getMinV());
				    tessellator.addVertexWithUV(x + 0, y - offset + 0, 0, icon.getMinU(), icon.getMinV());
				    tessellator.draw();
				}
			}
		    
			mc.fontRenderer.drawStringWithShadow(hiredsStr, x + iconBuffer, y, unitcountsColorI);
			
			if(hasScale) {
				GL11.glPopMatrix();
			}
		}
	}
}
