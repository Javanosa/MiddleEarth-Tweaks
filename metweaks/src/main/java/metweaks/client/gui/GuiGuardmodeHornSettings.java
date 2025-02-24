package metweaks.client.gui;

import lotr.client.gui.LOTRGuiScreenBase;
import lotr.client.gui.LOTRGuiSlider;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import metweaks.ASMConfig;
import metweaks.network.GuardmodeHornPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

public class GuiGuardmodeHornSettings extends LOTRGuiScreenBase {
	
	public LOTRGuiSlider sliderGuardRange;
	public LOTRGuiSlider sliderWanderRange;
	public ItemStack item;
	public GuiButtonOnOff autoGuardRange;
	
	public GuiButton button_keep;
	public GuiButton button_change;
	
	public final String translatedtitle = "\u00a7l"+StatCollector.translateToLocal("gui.guardmodehorn.title");
	public final String translatedInfo = StatCollector.translateToLocal("gui.guardmodehorn.info");
	
	public int centerX;
	public int top;
	
	public GuiGuardmodeHornSettings(ItemStack item) {
		this.item = item;
	}
	 
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		
	
		centerX = width / 2;
	    top = (int) (height * 0.3);
	    
	    int guardRange = LOTRHiredNPCInfo.GUARD_RANGE_DEFAULT;
		int wanderRange = LOTRHiredNPCInfo.GUARD_RANGE_DEFAULT;
		boolean change = false;
		boolean auto = false;
	    
		NBTTagCompound nbt = item.getTagCompound();
	    if(nbt != null) {
	    	
	    	int gRange = nbt.getByte("GuardRange");
	    	if(gRange > 0) 
	    		guardRange = gRange;
	    	
	    	int wRange = nbt.getByte("WanderRange");
	    	if(wRange > 0) 
	    		wanderRange = wRange;
	    	
	    	change = nbt.hasKey("Change");
	    	
	    	auto = nbt.hasKey("Auto");
	   }
	    
	    
	    
		
		buttonList.add(button_keep = new GuiButton(0, centerX-100, top, 100, 20, StatCollector.translateToLocal("gui.guardmodehorn.keep"))); // Keep
		buttonList.add(button_change = new GuiButton(1, centerX	, top, 100, 20, StatCollector.translateToLocal("gui.guardmodehorn.change"))); // Change
		
		buttonList.add(autoGuardRange = new GuiButtonOnOff(2, centerX - 80, top + 25, 160, 20, StatCollector.translateToLocal("gui.guardmodehorn.autoGuardRange")));
		autoGuardRange.setState(auto);
		
		buttonList.add(sliderGuardRange = new LOTRGuiSlider(3, centerX - 80, top + 50, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.guardRange")));
		sliderGuardRange.setMinMaxValues(LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
		sliderGuardRange.setSliderValue(guardRange);
		
		if(ASMConfig.guardsWanderRange) {
			buttonList.add(sliderWanderRange = new LOTRGuiSlider(4, centerX - 80, top + 75, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.wanderRange")));
			sliderWanderRange.setMinMaxValues(LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
			sliderWanderRange.setSliderValue(wanderRange);
		}
		changeState(change);
		
		// Mode to apply on hireds
		// Keep Range
		// Set Range
		// Use AI Range
	}
	
	public void changeState(boolean change) {
		button_keep.enabled = change;
		button_change.enabled = !change;
			
		autoGuardRange.enabled = change;
		sliderGuardRange.enabled = change && !autoGuardRange.state;
		if(ASMConfig.guardsWanderRange)
			sliderWanderRange.enabled = change;
	}
	
	@Override
	public void actionPerformed(GuiButton button) {
		boolean change = button == button_change;
		if(change || button == button_keep) {
			changeState(change);
		}
		else if(button == autoGuardRange) {
			autoGuardRange.setState(!autoGuardRange.state);
			sliderGuardRange.enabled = !autoGuardRange.state;
		}
	}
	
	@Override
	public void onGuiClosed() {
		// send packet 
		
		int wanderRange = ASMConfig.guardsWanderRange ? sliderWanderRange.getSliderValue() : LOTRHiredNPCInfo.GUARD_RANGE_DEFAULT;
		NetworkHandler.networkWrapper.sendToServer(new GuardmodeHornPacket(button_keep.enabled, autoGuardRange.state, wanderRange, sliderGuardRange.getSliderValue()));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
	    
	    drawDefaultBackground();
	    
	    
	    drawCenteredString(translatedtitle, centerX, top-50, 0xFFAA00); // "\u00a76\u00a7lGuardmode Horn Settings"
	    drawCenteredString(translatedInfo, centerX, top-25, 0xffffff); // "Values to apply at Hireds on Use"
	    super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
