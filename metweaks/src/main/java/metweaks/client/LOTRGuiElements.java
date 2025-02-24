package metweaks.client;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.client.LOTRTickHandlerClient;
import lotr.client.gui.LOTRGuiMiniquestTracker;
import lotr.common.LOTRConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;

public class LOTRGuiElements {
	
	public static int compassX = 60; // position from Left (or Right if "compassRight" is enabled)
	public static int compassY = 40; // position from Top (Or Bottom if "compassBottom" is enabled)
	public static boolean compassRight = true;  // flip/move to the right side
	public static boolean compassBottom = false; // flip/move to the bottom
	
	public static boolean compassCenterVertical = false;  // center compass vertical
	public static boolean compassCenterHorizontal = false;  // center compass horizontal
	
	public static float compassScale = 1F;
	public static boolean autoAdjust = true; // adjust values to be readable and stay on the screen
	
	private static int compassBufferX;
	private static int compassBufferY;
	
	public static int metweaksMessageOffsetY = 0; // positive moves up, negative down
	
	public static boolean enableAlignmentBar = true;
	
	public static int questTrackerX = 16; // position from Left (or Right if "questTrackerRight" is enabled)
	public static int questTrackerY = 10; // position from Top
	
	private static Field field_barX;
	private static Field field_barY;
	
	public static float writingOuterScale = 1.05F;
	public static float writingInnerScale = 0.85F;
	
	public static int getPosition(int pos, int size, boolean flip, boolean center) {
		if(center) {
			
			return flip ? size - pos : size + pos;
		}
		return flip ? size - pos : pos;
	}
	
	public static void scaleCompass() {
		if(compassScale != 1) {
			
			GL11.glScalef(compassScale, compassScale, compassScale);
		}
	}
	
	public static int getCompassX(int width) {
		
		if(compassCenterHorizontal) width /= 2;
		
		return getPosition(autoAdjust ? Math.min(compassX, width - compassBufferX) : compassX, 		width, compassRight, compassCenterHorizontal);
	}
	
	public static int getCompassY(int height) {
		
		if(compassCenterVertical) height /= 2;
		
		return getPosition(autoAdjust ? Math.min(compassY, height - compassBufferY) : compassY, 	height, compassBottom, compassCenterVertical);
	}
	
	public static void loadConfig() {
		Configuration config = MeTweaksConfig.config;
		String CATEGORY_HUDELEMENTS = MeTweaksConfig.CATEGORY_HUDELEMENTS;
		
		
		
		
		
		
		
		compassX = config.get(CATEGORY_HUDELEMENTS, "Compass X", compassX, "position from Left (or Right if \"Compass Right\" is enabled)").getInt();
		compassY = config.get(CATEGORY_HUDELEMENTS, "Compass Y", compassY, "position from Top (Or Bottom if \"Compass Bottom\" is enabled)").getInt();
		
		compassRight = config.get(CATEGORY_HUDELEMENTS, "Compass Right", compassRight, "flip to the right side").getBoolean();
		compassBottom = config.get(CATEGORY_HUDELEMENTS, "Compass Bottom", compassBottom, "flip to the bottom").getBoolean();
		
		compassCenterVertical = config.get(CATEGORY_HUDELEMENTS, "Compass Center Vertical", compassCenterVertical).getBoolean();
		compassCenterHorizontal = config.get(CATEGORY_HUDELEMENTS, "Compass Center Horizontal", compassCenterHorizontal).getBoolean();
		
		
		compassScale = (float) config.get(CATEGORY_HUDELEMENTS, "Compass Scale", compassScale, null, 0.5, 5).getDouble();
		
		metweaksMessageOffsetY = config.get(CATEGORY_HUDELEMENTS, "ME-Tweaks Status Message Offset-Y", metweaksMessageOffsetY, "Negative values move it up, positive values down").getInt();
		
		questTrackerX = config.get(CATEGORY_HUDELEMENTS, "Quest Tracker X", questTrackerX, "position from Left (or Right if \"Flip Quest Tracker\" is enabled)").getInt();
		questTrackerY = config.get(CATEGORY_HUDELEMENTS, "Quest Tracker Y", questTrackerY, "position from Top").getInt();
		
		enableAlignmentBar = config.get(CATEGORY_HUDELEMENTS, "Enable Alignment Bar", enableAlignmentBar, "dont waste cpu to render alignment bar").getBoolean();
		
		autoAdjust = config.get(CATEGORY_HUDELEMENTS, "Auto Adjust Values", autoAdjust, "adjust values to be readable and stay on the screen").getBoolean();
		
		boolean changeWritingRadius = config.get(CATEGORY_HUDELEMENTS, "Change Ring Writing Radius", true, "much more clean on the compass").getBoolean();
		
		if(changeWritingRadius) {
			writingOuterScale = 1.01F;
			writingInnerScale = 0.91F;
		}
		else {
			writingOuterScale = 1.05F;
			writingInnerScale = 0.85F;
		}
		
		if(autoAdjust) {
			compassScale = MathHelper.clamp_float(compassScale, 0.75f, 2);
			
			compassBufferX = (int) (45 * compassScale);
			compassBufferY = compassBottom ? compassBufferX : (int) (40 * compassScale);
			
			
			compassX = Math.max(compassX, compassBufferX);
			if(compassCenterHorizontal) {
				compassX = 0;
			}
			
			compassY = Math.max(compassY, compassBufferY);
			if(compassCenterVertical) {
				compassY = 0;
			}
			
        	// right left 45
        	// top 40
        	// bottom 45
			
			
			
			metweaksMessageOffsetY = MathHelper.clamp_int(metweaksMessageOffsetY, -180, 0);
			
			questTrackerX = Math.max(10, questTrackerX);
			questTrackerY = Math.max(10, questTrackerY);
			
			
		}
		
		if(MeTweaks.lotr) {
			if(field_barX == null) {
				field_barX = ReflectionHelper.findField(LOTRGuiMiniquestTracker.class, "barX");
			}
			if(field_barY == null) {
				field_barY = ReflectionHelper.findField(LOTRGuiMiniquestTracker.class, "barY");
			}
			
			try {
				field_barX.set(LOTRTickHandlerClient.miniquestTracker, questTrackerX);
				field_barY.set(LOTRTickHandlerClient.miniquestTracker, questTrackerY);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// if lotr config change this is wrong
			if(autoAdjust && !LOTRConfig.compassExtraInfo) {
				compassY = Math.max(compassY -= 10, 35);
			}
		}
		
		
		
		
	}
	  
	
	
}
