package metweaks.client.gui.config;

import cpw.mods.fml.relauncher.ReflectionHelper;
import metweaks.network.SyncedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.util.StatCollector;

public class GuiConfigConfirm extends GuiScreen {
	public final GuiScreen parent;
	public final SyncedConfig config;
	public final GuiMismatchesList mismatchesList;
	public final GuiButton btnCancel;
	public final GuiButton btnConfirm;
	
	public final String translatedtitle = StatCollector.translateToLocal("gui.configconfirm.title");
	public final String translatedInfo = StatCollector.translateToLocal("gui.configconfirm.info");
	
	public final String translatedConfig = StatCollector.translateToLocal("gui.configconfirm.config");
	public final String translatedServer = StatCollector.translateToLocal("gui.configconfirm.server");
	public final String translatedYou = StatCollector.translateToLocal("gui.configconfirm.you");
	
	public int xOffsetList;
	
	public GuiConfigConfirm(GuiScreen screen, SyncedConfig syncedConfig) {
		parent = screen;
		config = syncedConfig;
		btnCancel = new GuiButton(0, 0, 0, 160, 20, StatCollector.translateToLocal("gui.cancel"));
		btnConfirm = new GuiButton(1, 0, 0, 160, 20, StatCollector.translateToLocal("gui.confirm.restart"));
		mismatchesList = new GuiMismatchesList(Minecraft.getMinecraft(), 0, 0, 0, 0, 25, config.mismatches());
		config.mismatches = null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		btnCancel.xPosition = width / 2 - 165;
		btnCancel.yPosition = height - 30;
		
		btnConfirm.xPosition = width / 2 + 5;
		btnConfirm.yPosition = height - 30;
		
		mismatchesList.right = mismatchesList.width = width;
		mismatchesList.height = height;
		mismatchesList.top = 70;
		mismatchesList.bottom = height - 40;
		
		buttonList.add(btnCancel);
		buttonList.add(btnConfirm);
		
		xOffsetList = (width / 2) - (mismatchesList.getListWidth() / 2) + 2;
		
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
        if(button.enabled) {
        	// cancel
        	if(button == btnCancel) {
        		mc.displayGuiScreen(parent);
        	}
        	// accept & restart
        	else if(button == btnConfirm) {
        		config.acceptSettings();
        		mc.shutdown();
        	}
        }
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks) {
		drawDefaultBackground();
        // ugly non threadsafe solution
        if(xOffsetList != 0) {
        	mismatchesList.drawScreen(mouseX, mouseY, ticks);
        	
            drawCenteredString(fontRendererObj, translatedConfig, xOffsetList + (mismatchesList.nameLen / 2), 53, 0xFFFFFF);
            drawCenteredString(fontRendererObj, translatedServer, xOffsetList + mismatchesList.nameLen + 10 + 40, 53, 0xFFFFFF);
           
            drawCenteredString(fontRendererObj, translatedYou, xOffsetList + mismatchesList.nameLen + 10 + 80 + 40, 53, 0xFFFFFF); // 16777215
        }
        
      
        drawCenteredString(fontRendererObj, translatedtitle, width / 2, 8, 0xFFFFFF); // "Config Change Confirm"
        drawCenteredString(fontRendererObj, translatedInfo, width / 2, 25, 0xAAAAAA); // "The following settings will be adjusted to play on this server"
        
        

        super.drawScreen(mouseX, mouseY, ticks);
    }
	
	
	public static void openConfigConfirmGUI(SyncedConfig config) {
		Minecraft mc = Minecraft.getMinecraft();
		GuiScreen screen = mc.currentScreen;
		if(screen instanceof GuiConnecting) {
			screen = ReflectionHelper.getPrivateValue(GuiConnecting.class, (GuiConnecting) mc.currentScreen, "field_146374_i");
		}
		
		mc.displayGuiScreen(new GuiConfigConfirm(screen, config));
	}
}
