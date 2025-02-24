package metweaks.client.gui;

import lotr.client.gui.LOTRGuiButtonOptions;
import lotr.client.gui.LOTRGuiHiredNPC;
import lotr.client.gui.LOTRGuiSlider;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.events.ClientEvents;
import metweaks.events.GuardEvents;
import metweaks.network.HiredAdvInfoPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

public class GuiHiredAdvSettings extends LOTRGuiHiredNPC {
	
	public LOTRGuiSlider sliderFollowRange;
	public LOTRGuiSlider sliderAmmoRange;
	public LOTRGuiButtonOptions buttonSetHome;
	public LOTRGuiButtonOptions buttonIgnoreSight;

	public GuiScreen parent;
	public HiredAdvInfoPacket packet;
	
	public int unlockInfoColor;
	public String unlockInfo;
	public String unlockInfoL2;
	public String performanceWarn;

	public GuiHiredAdvSettings(LOTREntityNPC npc, HiredAdvInfoPacket infopacket) {
		super(npc);
		parent = Minecraft.getMinecraft().currentScreen;
		packet = infopacket;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		page = 1;
		int left = guiLeft - 80 + xSize / 2;
		buttonList.add(buttonSetHome = new LOTRGuiButtonOptions(5, left, guiTop + 50, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.sethome")));
		//buttonSetHome.enabled = theNPC.hiredNPCInfo.guardMode;
		
		buttonList.add(sliderFollowRange = new LOTRGuiSlider(6, left, guiTop + 74, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.followRange")));
	    sliderFollowRange.setMinMaxValues(0, packet.aiRangeMax);
	    sliderFollowRange.setSliderValue(packet.aiRange);
	      
	    buttonList.add(sliderAmmoRange = new LOTRGuiSlider(7, left, guiTop + 98, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.ammoRange")));
	    sliderAmmoRange.setMinMaxValues(0, packet.ammoRangeMax);
	    sliderAmmoRange.setSliderValue(packet.ammoRange);
	     
	    buttonList.add(buttonIgnoreSight = new LOTRGuiButtonOptions(8, left, guiTop + 122, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.ignoreSight")));
	    buttonIgnoreSight.setState(packet.ignoreSight);
	   
	    boolean enabled = theNPC.hiredNPCInfo.xpLevel >= packet.unlockLvL;
	    buttonIgnoreSight.enabled = enabled && packet.allowChangeSight;
	    sliderFollowRange.enabled = enabled && packet.aiRangeMax > 0;
	    sliderAmmoRange.enabled = enabled && packet.ammoRangeMax > 0;
	    
	    unlockInfo = StatCollector.translateToLocalFormatted("lotr.gui.warrior.unlockSettings", packet.unlockLvL);
	    unlockInfoL2 =  StatCollector.translateToLocal("lotr.gui.warrior.unlockSettings2");
	    unlockInfoColor = enabled ? 0x0d6b09 : 0x9e1402;
	    
	    performanceWarn = StatCollector.translateToLocal("lotr.gui.warrior.performanceWarn");
	}
	
	@Override
	public void onGuiClosed() {
		GuardEvents.nextToShow = parent;
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.enabled) {
			if(button == buttonSetHome) {
				parent = null;
				mc.thePlayer.closeScreen();
				
				if(theNPC.hiredNPCInfo.guardMode) {
				
					ClientEvents.actionBar(StatCollector.translateToLocal("lotr.gui.warrior.setHomeTip"), false, 120*20, null, true);
					GuardEvents.guardHomePosTicks = 120*20;
					GuardEvents.guardHomePos = theNPC.getHomePosition();
					GuardEvents.guardHomeEntityID = theNPC.getEntityId();
				}
				else {
					ClientEvents.actionBar(StatCollector.translateToLocal("lotr.gui.warrior.requireGuardmode"), false, 80, null, true);
				}
			}
			else if(button == buttonIgnoreSight) {
				buttonIgnoreSight.setState(packet.ignoreSight = !packet.ignoreSight);
				sendActionPacket(button.id);
			}
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	    super.drawScreen(mouseX, mouseY, partialTicks);
	    
	   
	    if(sliderFollowRange.enabled) {
	    	int left = sliderFollowRange.xPosition + (int) ((packet.aiRangeDef / (float) packet.aiRangeMax) * (sliderFollowRange.width - 7));
	    	drawRect(left, sliderFollowRange.yPosition, left+6, sliderFollowRange.yPosition+20, 0x801b8216);
	    	
	    }
	    if(sliderAmmoRange.enabled && packet.ammoRangeMax > 0) {
	    	int left = sliderAmmoRange.xPosition + (int) ((packet.ammoRangeDef / (float) packet.ammoRangeMax) * (sliderAmmoRange.width - 7));
	    	
	    	drawRect(left, sliderAmmoRange.yPosition, left+6, sliderAmmoRange.yPosition+20, 0x801b8216);
	    }
	   
	    int x = guiLeft + xSize / 2;
	    int y = guiTop + 150;
	    drawCenteredString(unlockInfo, x, y, unlockInfoColor);
	    drawCenteredString(unlockInfoL2, x, y + 10, unlockInfoColor);
	    drawCenteredString(performanceWarn, x, y + 30, 0x333333);
	}
	
	@Override
	public void updateScreen() {
	    super.updateScreen();
	    
	    if(sliderFollowRange.dragging) {
	        int value = sliderFollowRange.getSliderValue();
	        sendActionPacket(sliderFollowRange.id, value);
	    }
	    else if(sliderAmmoRange.dragging) {
	        int value = sliderAmmoRange.getSliderValue();
	        sendActionPacket(sliderAmmoRange.id, value);
	    }
	}
}
