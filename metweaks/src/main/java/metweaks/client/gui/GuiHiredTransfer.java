package metweaks.client.gui;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import lotr.client.gui.LOTRGuiHiredInteract;
import lotr.client.gui.LOTRGuiScreenBase;
import lotr.common.LOTRSquadrons;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.MeTweaks;
import metweaks.client.gui.unitoverview.GuiUnitList.Entry;
import metweaks.client.gui.unitoverview.GuiUnitOverview;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.HiredTransferPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.Entity;
import net.minecraft.util.StatCollector;

public class GuiHiredTransfer extends LOTRGuiScreenBase {
	public LOTREntityNPC npc;
	public GuiTextField playerField;
	public GuiTextField companyField;
	public GuiButton transfer;
	public GuiButton pageSingleUnit;
	public GuiButton pageCompany;
	public GuiButton pageSelection;
	public GuiButton choose;
	public static int replyState;
	public int page;
	
	public static final int PAGE_ONE_UNIT = 0;
	public static final int PAGE_COMPANY = 1;
	public static final int PAGE_SELECTION = 2;
	
	public TIntList units = new TIntArrayList();
	
	// temp
	GuiUnitOverview overview;
	String tempPlayer;
	String tempCompany;

	public GuiHiredTransfer(LOTREntityNPC entity) {
		npc = entity;
		replyState = -1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
	    super.initGui();
	    
	    
	    
	    int halfWidth = width / 2;
	    int down3of5 = (int) (height * 0.6);
	    playerField = new GuiTextField(fontRendererObj, halfWidth - 60, down3of5 - 76, 120, 18);
	    playerField.setFocused(true);
	    playerField.setMaxStringLength(16);
	    
	    
	    
	    buttonList.add(transfer = new GuiButton(0, halfWidth - 65, down3of5 + 40, 60, 20, StatCollector.translateToLocal("gui.transfer.btn")));
	    buttonList.add(new GuiButton(1, halfWidth + 5, down3of5 + 40, 60, 20, StatCollector.translateToLocal("lotr.gui.dismiss.cancel")));
	    transfer.enabled = false;
	    
	    
	    buttonList.add(pageSingleUnit = new GuiButton(2, halfWidth - 120, down3of5 - 50, 80, 20, StatCollector.translateToLocal("gui.transfer.page0")));
	    buttonList.add(pageCompany = new GuiButton(3, halfWidth - 40, down3of5 - 50, 80, 20, StatCollector.translateToLocal("lotr.gui.warrior.squadron")));
	    buttonList.add(pageSelection = new GuiButton(4, halfWidth + 40, down3of5 - 50, 80, 20, StatCollector.translateToLocal("gui.transfer.page2")));
	    pageSingleUnit.enabled = false;
	    
	    companyField = new GuiTextField(fontRendererObj, halfWidth - 70, down3of5 - 20, 140, 18);
	    companyField.setMaxStringLength(LOTRSquadrons.SQUADRON_LENGTH_MAX);
	    
	    choose = new GuiButton(5, halfWidth - 30, down3of5 - 20, 60, 20, StatCollector.translateToLocal("gui.transfer.choose"));
	    
	    if(overview != null) {
	    	playerField.setText(tempPlayer);
			companyField.setText(tempCompany);
			
	    	actionPerformed(pageSelection);
			for(Entry entry : overview.unitsList.entrys) {
				if(entry.selected && entry.npc != null) {
					units.add(entry.npc.getEntityId());
				}
			}
		}
	}
	
	/*public void changeState(boolean change) {
		button_keep.enabled = change;
		button_change.enabled = !change;
			
		autoGuardRange.enabled = change;
		sliderGuardRange.enabled = change && !autoGuardRange.state;
	}*/
	
	@Override
	public void updateScreen() {
	    super.updateScreen();
	    playerField.updateCursorCounter();
	}
	
	public boolean possibleName(String txt) {
		return !StringUtils.isWhitespace(txt);
	}
	
	
	
	public void drawCenterString(String txt, int y, int c) {
		fontRendererObj.drawString(txt, (width - fontRendererObj.getStringWidth(txt)) / 2, y, c);
	}
	
	String translatedTitle = StatCollector.translateToLocal("gui.transfer.title");
	String translatedWarning = StatCollector.translateToLocal("gui.transfer.warning");
	String translatedWarningMount = StatCollector.translateToLocal("gui.transfer.warning.mount");
	String translatedWarningRider = StatCollector.translateToLocal("gui.transfer.warning.rider");
	
	@Override
	public void drawScreen(int i, int j, float f) {
		drawDefaultBackground();
		super.drawScreen(i, j, f);
		int down3of5 = (int) (height * 0.6);
		drawCenterString(translatedTitle, down3of5 - 120, 0xFFFFFFFF);
	
		if(page == PAGE_ONE_UNIT) {
			
			
			int y = down3of5;
			drawCenterString(npc.getCommandSenderName(), y - 20, 0xFFFFFFFF);
			
			drawCenterString(translatedWarning, y, 0xFFFFFFFF);
			y += fontRendererObj.FONT_HEIGHT;
	
			Entity mount = npc.ridingEntity;
			Entity rider = npc.riddenByEntity;
			boolean hasMount = mount instanceof LOTREntityNPC && ((LOTREntityNPC) mount).hiredNPCInfo.getHiringPlayer() == this.mc.thePlayer;
			boolean hasRider = rider instanceof LOTREntityNPC && ((LOTREntityNPC) rider).hiredNPCInfo.getHiringPlayer() == this.mc.thePlayer;
			if(hasMount) {
				drawCenterString(translatedWarningMount, y, 0xFFAAAAAA);
				y += fontRendererObj.FONT_HEIGHT;
			} 
			if(hasRider) {
				drawCenterString(translatedWarningRider, y, 0xFFAAAAAA);
				y += fontRendererObj.FONT_HEIGHT;
			}
			
		}
		else if(page == PAGE_COMPANY) {
			companyField.drawTextBox();
		}
		
		if(page == PAGE_COMPANY || page == PAGE_SELECTION) {
			drawCenterString(StatCollector.translateToLocalFormatted("gui.transfer.selected", units.size()), down3of5 + 10, 0xFFFFFFFF);
		}
		
	
		if(replyState > 0) {
			String reply = StatCollector.translateToLocal("gui.transfer.reply"+replyState);
			drawCenterString(reply, down3of5 - 90, 0xFFff2e17);
		}
		transfer.enabled = true;
		transfer.enabled = replyState <= 0 && possibleName(playerField.getText()) && (page == 0 || !units.isEmpty());
		playerField.drawTextBox();
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if(playerField.textboxKeyTyped(typedChar, keyCode)) {
			replyState = -1;
			return;
		}
		if(companyField.textboxKeyTyped(typedChar, keyCode)) {
			replyState = -1;
			calcUnitsCompany();
			return;
		}
	    super.keyTyped(typedChar, keyCode);
	}
	 
	@Override
    protected void mouseClicked(int i, int j, int k) {
        super.mouseClicked(i, j, k);
        playerField.mouseClicked(i, j, k);
        companyField.mouseClicked(i, j, k);
    }
	
	public void calcUnitsCompany() {
		units.clear();
		UUID myID = Minecraft.getMinecraft().thePlayer.getUniqueID();
		String squadron = companyField.getText();
		if(!possibleName(squadron)) return;
		
		for(Object entity : npc.worldObj.loadedEntityList) {
			if(entity instanceof LOTREntityNPC) {
				LOTREntityNPC npc = (LOTREntityNPC) entity;
				if(myID.equals(npc.hiredNPCInfo.getHiringPlayerUUID())) {
					String npcSquadron = npc.hiredNPCInfo.getSquadron();
					if(npcSquadron != null && npcSquadron.equalsIgnoreCase(squadron)) {
						units.add(npc.getEntityId());
					}
				}
			}
		}
	      
	}

	@SuppressWarnings("unchecked")
	@Override
    protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
        if(button.enabled) {
        	if(button.id == 1) {
                mc.displayGuiScreen(new LOTRGuiHiredInteract(npc));
                return;
            } 
        	else if(button.id == 0) {
	            String name = playerField.getText();
	            if(possibleName(name)) {
	            	name = StringUtils.trim(name);
	            	
	            	HiredTransferPacket packet = new HiredTransferPacket(name, (byte) -1);
	            	if(page == PAGE_ONE_UNIT) {
	            		units.clear();
	            		units.add(npc.getEntityId());
	            		packet.units = units;
	            	}
	            	else if(page == PAGE_COMPANY) {
	            		packet.company = companyField.getText();
	            	}
	            	else if(page == PAGE_SELECTION) {
	            		packet.units = units;
	            	}
	            	
	                NetworkHandler.networkWrapper.sendToServer(packet);
	            } 
            }
        	else if(button.id <= 4) {
        		replyState = -1;
        		//int prevPage = page;
        		page = button.id - 2;
        		pageSingleUnit.enabled = true;
        		pageCompany.enabled = true;
        		pageSelection.enabled = true;
        		button.enabled = false;
        		
        		/*if(page > 0 && prevPage > 0) {
    				units.clear();
    			}*/
        		
        		if(page == PAGE_SELECTION) {
        			units.clear();
        			buttonList.add(choose);
        		}
        		else {
        			buttonList.remove(choose);
        		}
        		
        		if(page == PAGE_COMPANY) {
        			calcUnitsCompany();
        		}
        	}
        	else if(button.id == 5) {
        		replyState = -1;
        		if(overview == null) {
        			overview = new GuiUnitOverview();
            		overview.selection = units;
            		overview.selectionMode = true;
            		overview.parent = this;
            		
        		}
        		
        		tempPlayer = playerField.getText();
        		tempCompany = companyField.getText();
        		
        		// apply unit selection before list first render
        		Minecraft.getMinecraft().displayGuiScreen(overview);
    			if(MeTweaks.remotePresent)
    				NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(-1, GuardsOverviewActionPacket.ACTION_SEND_DATA));
        	}
        	
        }
    }
}
