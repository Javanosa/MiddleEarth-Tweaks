package metweaks.client.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;

import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Util;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;

public class GuiTOSNotice extends GuiScreen {
	
	// 0 = disabled
	// 1 = disabled, but reenables if remote state tells so
	// 		forceDisclaimer.txt read time stamp
	// 2 
	// 		downloads config data and compares
	// 2 = enabled
			
	//		tosDisclaimer.txt
	//		
	
	// checkbox button "do not show again"
	// Confirm config changes
	// Close
	
	
	
	
		/*
		
		linesLeft.add("\u00a76\u00a7lWorking features on TOS \u00a7r(up to 1.6.16)");
		linesLeft.add("\u25A0 Unit Overview (U key)");
		linesLeft.add("\u25A0 Healthbar and Mountinfo (H key)");
		linesLeft.add("\u25A0 Unit Tracker (F7 + U key) to avoid losing troops during travel");
		linesLeft.add("\u25A0 /cfarview to view farer than the server distance");
		linesLeft.add("\u25A0 New Tablist");
		linesLeft.add("\u25A0 Deathscreen & Respawn Fix");
		linesLeft.add("\u25A0 Mounts running away Fix");
		linesLeft.add("\u25A0 Server Join Freeze Fix");
		linesLeft.add("\u25A0 Always set respawn on bed click");
		linesLeft.add("\u25A0 Customizable LOTR HUD Elements and Fixes");
		linesLeft.add("\u25A0 Optifine Performance Fix");
		linesLeft.add("\u25A0 Hired Unit Counts Overlay");
		linesLeft.add("\u25A0 More FPS during Rain");
		linesLeft.add("\u25A0 Logs your Death Locations");*/
	
	
	
	private List<String> linesTrimL = new ArrayList<>();
	private List<String> linesTrimR = new ArrayList<>();
	private int leftStart;
	private int rightStart;
	private int margin;
	private int lineHeight = 15;
	
	private static int origin_guiscale;
	private static boolean is_open;
	
	public GuiTOSNotice() {
		mc = Minecraft.getMinecraft();
		
		// if not normal
		if(mc.gameSettings.guiScale != 2 || is_open) {
			origin_guiscale = mc.gameSettings.guiScale;
			mc.gameSettings.setOptionValue(Options.GUI_SCALE, 2 - origin_guiscale);
		}
		else {
			is_open = true;
		}
	}
	
	@Override
	public void onGuiClosed() {
	    if(mc.gameSettings.guiScale == 2 && !is_open)
	    	mc.gameSettings.setOptionValue(Options.GUI_SCALE, origin_guiscale - 2); 
	    is_open = false;
	    super.onGuiClosed();
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		
		margin = Math.max(10, (width - 640) / 3);
		int sideWidth = (width - (margin * 3)) / 2;
		leftStart = margin;
		rightStart = margin + margin + sideWidth;
		
		linesTrimL.clear();
		for(String line : TosNoticeReader.linesLeft) {
			linesTrimL.addAll(fontRendererObj.listFormattedStringToWidth(line, sideWidth));
		}
		linesTrimR.clear();
		for(String line : TosNoticeReader.linesRight) {
			linesTrimR.addAll(fontRendererObj.listFormattedStringToWidth(line, sideWidth));
		}
		
		int y = Math.min(height - 70 - margin, 		margin + (linesTrimR.size() * lineHeight) + 25);
		
		buttonList.add(new GuiButton(0, rightStart, y, 170, 20, StatCollector.translateToLocal("gui.confirm.restart")));
		buttonList.add(new GuiButton(1, rightStart, y + 25, 60, 20, StatCollector.translateToLocal("gui.close")));
		buttonList.add(new GuiButton(2, rightStart + 65, y + 25, 160, 20, StatCollector.translateToLocal("gui.closeDontShowAgain")));
		boolean hasConfig = !TosNoticeReader.configChanges.isEmpty();
		buttonList.add(new GuiButton(3, rightStart, y + 50, 180, 20, StatCollector.translateToLocalFormatted(!hasConfig ? "gui.openConfig" : "gui.openConfig2", MeTweaks.MODID+"-ASM.properties")));
		if(hasConfig)
			buttonList.add(new GuiButton(4, rightStart + 185, y + 50, 110, 20, StatCollector.translateToLocalFormatted("gui.openConfig2", MeTweaks.MODID+".cfg")));
		
		
		
	}
	
	public static void setEnableOnDemand(int flag) {
		int setTo = flag == 1 ? Math.min(MeTweaksConfig.tosNotice, flag) : flag;
		MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_GENERAL, "Show TOS Notice", 2, "0 = Disabled, 1 = Enables on demand, 2 = Enabled").set(MeTweaksConfig.tosNotice = setTo);
		MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_GENERAL, "Reenable Notice Time", "0").set(MeTweaksConfig.reenableNoticeTime = String.valueOf(System.currentTimeMillis()));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
        if(button.enabled) {
        	// accept & restart
        	if(button.id == 0) {
        		setEnableOnDemand(1);
        		confirmConfig();
        	}
        	// close
        	else if(button.id <= 2) {
        		if(button.id == 2) {
        			setEnableOnDemand(1);
        			MeTweaksConfig.config.save();
        		}
        		mc.displayGuiScreen(null);
        	}
        	else if(button.id == 3) {
        		openConfigFile(MeTweaks.MODID+"-ASM.properties");
        	}
        	else if(button.id == 4) {
        		openConfigFile(MeTweaks.MODID+".cfg");
        	}
        }
    }
	
	private void confirmConfig() {
		// metweaks.cfg
		
		
		for(String entry : TosNoticeReader.configChanges) {
			int max = entry.length()-1;
			int nameStart = entry.indexOf('/');
			int valueStart = entry.indexOf('=');
			
			if(nameStart == -1 || valueStart == -1) continue;
			if(nameStart >= max || valueStart >= max) continue;
			
			String category = entry.substring(0, nameStart);
			String name = entry.substring(nameStart+1, valueStart);
			String value = entry.substring(valueStart+1);
			//MeTweaksConfig.config.get(category, name, "").set(value);
			ConfigCategory cat = MeTweaksConfig.config.getCategory(category);
			if(cat != null) {
				Property property = cat.get(name);
				if(property != null) {
					property.set(value);
					
				}
			}
			
		}
		
		MeTweaksConfig.config.save();
		
		// metweaks-ASM.properties
		boolean changesASM = false;
		ASMConfig.propertiesChange = new HashMap<>();
		
		for(String entry : TosNoticeReader.configChangesASM) {
			int max = entry.length()-1;
			int valueStart = entry.indexOf('=');
			
			
			if(valueStart == -1 || valueStart >= max) continue;
			
			String name = entry.substring(0, valueStart);
			String value = entry.substring(valueStart+1);
			ASMConfig.propertiesChange.put(name, value);
			changesASM = true;
		}
		
		if(changesASM)
			ASMConfig.init();
		
		mc.shutdown();
		
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks) {
		drawDefaultBackground();
		
		
		
		// break strings into lines that suit width
		// break strings into lines with multiple 
		
		int y = margin;
		for(String line : linesTrimL) {
        	drawString(fontRendererObj, line, leftStart, y, 0xFFFFFF);
        	y += lineHeight;
        }
		
		y = margin;
		//drawString(fontRendererObj, "Advised adjustments on TOS in /config/metweaks-ASM.properties:", rightStart, y, 0xFFFFFF);
		//y+= lineHeight;
		for(String line : linesTrimR) {
        	drawString(fontRendererObj, line, rightStart, y, 0xFFFFFF);
        	y += lineHeight;
        }
		
		super.drawScreen(mouseX, mouseY, ticks);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void openConfigFile(String name) {
		File file = new File("config", name);
        String absolute = file.getAbsolutePath();
        Logger logger = LogManager.getLogger();

        if (Util.getOSType() == Util.EnumOS.OSX) {
            try {
                logger.info(absolute);
                Runtime.getRuntime().exec(new String[] {"/usr/bin/open", absolute});
                return;
            }
            catch (IOException e) {
                logger.error("Couldn\'t open file", e);
            }
        }
        else if (Util.getOSType() == Util.EnumOS.WINDOWS) {
            String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", absolute);

            try {
                Runtime.getRuntime().exec(s1);
                return;
            }
            catch (IOException e) {
                logger.error("Couldn\'t open file", e);
            }
        }

        boolean failed = false;

        try
        {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
            oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, file.toURI());
        }
        catch (Throwable throwable)
        {
            logger.error("Couldn\'t open link", throwable);
            failed = true;
        }

        if (failed)
        {
            logger.info("Opening via system class!");
            Sys.openURL("file://" + absolute);
        }
	}
}
