package metweaks.client.gui.unitoverview;

import java.util.Arrays;
import java.util.Collections;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;
import lotr.common.LOTRMod;
import lotr.common.LOTRSquadrons;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import lotr.common.network.LOTRPacketHandler;
import lotr.common.network.LOTRPacketHiredUnitCommand;
import lotr.common.network.LOTRPacketHiredUnitInteract;
import lotr.common.network.LOTRPacketNPCSquadron;
import metweaks.MeTweaks;
import metweaks.client.healthbar.HealthBarConfig;
import metweaks.client.gui.unitoverview.GuiUnitList.Entry;
import metweaks.events.ClientEvents;
import metweaks.events.GuardEvents;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.GuardsOverviewPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

public class GuiUnitOverview extends GuiScreen {
	/*
	 
	 1. Easy Company mass assignment
	 2. Ability to see melee weapons of rangers / high elves all the time
	 3. Shows if equipment has penalties like poor protection
	 4. Overview of all inventory items of warriors and farmers
	 5. Rightclick on an entry to highlight the npc or multiple npc's (leftlick) in the world
	 6. Click on their items to edit their inventory
	 */
	
	
	
/*
 constructor: // everything except static data
 	
  
 reinit: // minor item updates, screen resolution updates
 	
 	
  
 update Filters:
 	update starts (all)
 	update lens (all)
 	update entry list (all)
 	if warrior toggle
 		update column button (len and starts (all)) and checkbox visibility (specific) (starts not yet needed ?)
 		
 toggle column (checkbox):
 	return sort prio index to first left
 	update starts (all)
 	update entry sort
 	update column button visibility (starts) (all)
 	update entry render
 	
 asc / desc column:
 	set sort prio index to current button
 	update entry sort
 	
 	
 	
 */
	
	// keybind: U = Unit Overview
	public static TIntObjectMap<ItemStack[]> inventories;
	public final GuiUnitList unitsList;
	
	public final String translatedtitle = StatCollector.translateToLocal("gui.unitoverview.title");
	public final String translatedFilters = StatCollector.translateToLocal("gui.unitoverview.filters");
	public final String translatedColumns = StatCollector.translateToLocal("gui.unitoverview.columns");
	
	GuiTextField filtercompany;
	// selection
	public TIntList selection;
	public boolean selectionMode;
	public GuiScreen parent;
	
	public static void resetMarkNpc() {
		GuardEvents.markNpcID = -1;
		GuardEvents.markNpcTicks = 0;
		ClientEvents.actionBar(null, false, 0, null, false); // empty actionbar
	}
	
	public GuiUnitOverview() {
		GuardEvents.tempGuiUnitOverview = null;
		GuardEvents.emptyPassed = false;
		GuardEvents.timeout = Long.MAX_VALUE;
		unitInvOpenID = -1;
		inventories = null; // reset
		resetMarkNpc();
		unitsList = new GuiUnitList(Minecraft.getMinecraft(), width-30, height-50, 0, 0, 25, this);	
	}
	
	
	
	public static boolean showFarmers = true;
	public static boolean showWarriors = true;
	
	public static final int columnIndex = 0;
	public static final int columnType = 1;
	public static final int columnName = 2;
	public static final int columnCompany = 3;
	
	public static final int columnArmor = 4;
	public static final int columnDistance = 5;
	public static final int columnInventory = 6;
	public static final int columnLvl = 7;
	
	
	
	
	
	// Disable / Enable Columns
	GuiCheckBox[] col_checkboxes;
	static boolean[] col_states = new boolean[8];
	static {
		Arrays.fill(col_states, true); // all columns are enabled by default
	}
	
	// Toggle ASC / DESC for columns
	GuiColumnButton[] col_buttons;
	static boolean[] col_asc = new boolean[8];
	
	GuiCheckBox checkboxWarriors;
	GuiCheckBox checkboxFarmers;
	
	@SuppressWarnings("unchecked")
	public int addColumnToggle(int xPos, int yPos, int index, String name) {
		int sizeX = fontRendererObj.getStringWidth(name) + 10;
		GuiCheckBox box = new GuiCheckBox(4, xPos, yPos, sizeX, 14, name);
		box.checked = col_states[index];
		buttonList.add(col_checkboxes[index] = box);
		return xPos + sizeX + 1;
	}
	
	public void addColumn(int index, String name) {
		 addColumn(index, name, null);
	}
	
	@SuppressWarnings("unchecked")
	public void addColumn(int index, String name, IIcon icon) {
		int sizeX = fontRendererObj.getStringWidth(name) + 10+10;
		GuiColumnButton button = new GuiColumnButton(4, 0, 70, sizeX, 16, name);
		button.ascending = col_asc[index];
		button.visible = col_states[index];
		button.icon = icon;
		buttonList.add(col_buttons[index] = button);
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		
		
		
		
		unitsList.right = unitsList.width = width;
		unitsList.height = height;
		unitsList.top = 70;
		unitsList.bottom = height - 40;
		unitsList.width = width;
		unitsList.left = 20;
		unitsList.right = 20;
		
		
		unitsList.height = height;
		unitsList.top = 90;
		unitsList.bottom = height;
		unitsList.width = width;
		unitsList.left = 0;
		unitsList.right = width;
		
		
		
		int leftStart = 30 + fontRendererObj.getStringWidth(translatedFilters) + 5;
		int topStart = 28;
		filtercompany = new GuiTextField(fontRendererObj, leftStart, topStart, 90, 14);
		
		
		filtercompany.setMaxStringLength(LOTRSquadrons.SQUADRON_LENGTH_MAX);
		filtercompany.setFocused(true);
		
		col_checkboxes = new GuiCheckBox[8];
		//col_states = new boolean[8];
		
		col_buttons = new GuiColumnButton[8];
		//col_asc = new boolean[8];
		
		topStart--;
		leftStart += 100;
		buttonList.add(checkboxWarriors = new GuiCheckBox(1, leftStart, topStart, 60, 16, StatCollector.translateToLocal("gui.unitoverview.warriors")));
		buttonList.add(checkboxFarmers = new GuiCheckBox(2, leftStart + 70, topStart, 60, 16, StatCollector.translateToLocal("gui.unitoverview.farmers")));
		checkboxWarriors.checked = showWarriors;
		checkboxFarmers.checked = showFarmers;
		
		int xStart = 30 + fontRendererObj.getStringWidth(translatedColumns) + 10;
		int yPos = 50;
		
		String cType = StatCollector.translateToLocal("gui.unitoverview.cType");
		String cName = StatCollector.translateToLocal("gui.unitoverview.cName");
		String cCompany = StatCollector.translateToLocal("gui.unitoverview.cCompany");
		String cInv = StatCollector.translateToLocal("gui.unitoverview.cInv");
		String cLvl = StatCollector.translateToLocal("gui.unitoverview.cLvl");
		
		
		
		
		xStart = addColumnToggle(xStart, yPos, columnIndex, StatCollector.translateToLocal("gui.unitoverview.cIndex"));
		xStart = addColumnToggle(xStart, yPos, columnDistance, StatCollector.translateToLocal("gui.unitoverview.cBlocks"));
		xStart = addColumnToggle(xStart, yPos, columnType, cType);
		xStart = addColumnToggle(xStart, yPos, columnName, cName);
		xStart = addColumnToggle(xStart, yPos, columnInventory, cInv);
		xStart = addColumnToggle(xStart, yPos, columnCompany, cCompany);
		xStart = addColumnToggle(xStart, yPos, columnArmor, StatCollector.translateToLocal("gui.unitoverview.cArmor"));
		xStart = addColumnToggle(xStart, yPos, columnLvl, cLvl);
		
		
		
		
		addColumn(columnIndex, "");
		addColumn(columnType, cType);
		addColumn(columnName, cName);
		addColumn(columnCompany, cCompany);
		addColumn(columnArmor, "AA", HealthBarConfig.icon_armor.getIconFromDamage(HealthBarConfig.icon_armor_meta));
		addColumn(columnLvl, cLvl);
		addColumn(columnDistance, "AA", Items.compass.getIconFromDamage(0));
		addColumn(columnInventory, cInv);
		
		updateColumSelection(false);
		
		unitsList.initList();
		
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if(unitInvOpenID == -1) inventories = null;
		
		if(parent != null) {
			GuardEvents.nextToShow = parent;
		}
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) {
		if(filtercompany.textboxKeyTyped(typedChar, keyCode)) {
			unitsList.initList();
		}
		
		if(col_states[columnCompany] && !selectionMode) {
			for(Entry entry : unitsList.entrys) {
				if(entry.squadronField.textboxKeyTyped(typedChar, keyCode)) {
					LOTREntityNPC npc = entry.npc;
					npc.hiredNPCInfo.setSquadron(entry.squadronField.getText());
					LOTRPacketNPCSquadron packet = new LOTRPacketNPCSquadron(npc, npc.hiredNPCInfo.getSquadron());
				    LOTRPacketHandler.networkWrapper.sendToServer(packet);
				}
			}
		}
		
		
		super.keyTyped(typedChar, keyCode);
    }
	
	
	public void updateColumSelection(boolean force) {
		
		boolean state = showWarriors;
		
		col_checkboxes[columnArmor].visible = state;
		col_checkboxes[columnLvl].visible = state;
		
		if(force) {
			col_checkboxes[columnArmor].checked = state;
			col_checkboxes[columnLvl].checked = state;
			
			col_buttons[columnArmor].visible = state;
			col_buttons[columnLvl].visible = state;
			
			
			
			col_states[columnArmor] = state;
			col_states[columnLvl] = state;
		}
		
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button instanceof GuiCheckBox) {
			GuiCheckBox box = (GuiCheckBox) button;
			boolean newState = !box.checked;
			box.checked = newState;
			
			if(checkboxWarriors == button) {
				showWarriors = newState;
				// hide / show culumn buttons specific for warriors
				updateColumSelection(true);
				unitsList.initList();
			}
			
			if(checkboxFarmers == button) {
				showFarmers = newState;
				unitsList.initList();
			}
			
			for(int i = 0; i < col_checkboxes.length; i++) {
				GuiCheckBox checkbox = col_checkboxes[i];
				if(checkbox == button) {
					col_states[i] = newState;
					col_buttons[i].visible = newState;
					unitsList.updateListWidth();
					unitsList.updateStarts();
					
					break;
				}
			}
		}
		
		if(button instanceof GuiColumnButton) {
			GuiColumnButton column = (GuiColumnButton) button;
			
			for(int i = 0; i < col_buttons.length; i++) {
				GuiColumnButton columnbtn = col_buttons[i];
				if(columnbtn == button) {
					col_asc[i] = column.ascending;
					SortGlobal.onClickColumn(i);
					Collections.sort(unitsList.entrys, new SortGlobal());
					break;
				}
			}
			
		}
	}
	
	public static int unitInvOpenID;
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		filtercompany.mouseClicked(mouseX, mouseY, mouseButton);
		if(col_states[columnCompany] && mouseButton == 0 && !selectionMode) {
			for(Entry entry : unitsList.entrys) {
				entry.squadronField.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseY >= unitsList.top && mouseY <= unitsList.bottom) {
 			// getSlotIndexFromScreenCoords()
			 int i = unitsList.func_148124_c(mouseX, mouseY);
			 if(i != -1) {
				 Entry entry = (Entry) unitsList.getListEntry(i);
				 int id = entry.npc.getEntityId();
				 
				 if(selectionMode) {
					 boolean select = entry.selected;
					 entry.selected = !select;
					 if(select) {
						 selection.remove(id);
					 }
					 else {
						 selection.add(id);
					 }
					 return;
				 }
				 
				 boolean sound = false;
				 
				 if(mouseButton == 0 && col_states[columnInventory] && mouseX >= unitsList.col_starts[columnInventory] && mouseX <= unitsList.col_starts[columnInventory] + unitsList.col_lens[columnInventory]) {
				 
					unitInvOpenID = id;
					if(MeTweaks.remotePresent) {
						NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(id, GuardsOverviewActionPacket.ACTION_OPEN_INV));
					}
					else if(entry.npc.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer) < (11.5 * 11.5)) {
						LOTRPacketHandler.networkWrapper.sendToServer(new LOTRPacketHiredUnitInteract(id, 1));
						LOTRPacketHandler.networkWrapper.sendToServer(new LOTRPacketHiredUnitCommand(id, 0, entry.npc.hiredNPCInfo.getTask() == Task.WARRIOR ? 0 : 2, 0));
						GuardEvents.tempGuiUnitOverview = this;
						
					}
					
				 	sound = true;
				 }
				 
				 if(mouseButton == 1) {
					 GuardEvents.markNpcID = id;
					 GuardEvents.markNpcTicks = 20 * 60;
					 ClientEvents.actionBar(StatCollector.translateToLocalFormatted("gui.unitoverview.showNpc", entry.npc.getCommandSenderName()), false, 20*60, null, true);
					 mc.thePlayer.closeScreen();
					 sound = true;
				 }
				 
				 if(sound)
					 checkboxWarriors.func_146113_a(mc.getSoundHandler());
				 
				
				 
				 
				 
				 
				 
			 }
			 
			 
        }
        
    }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks) {
		drawDefaultBackground();
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.001f); // to allow minor alpha drawing
		unitsList.drawScreen(mouseX, mouseY, ticks);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.01f); // return to normal
		super.drawScreen(mouseX, mouseY, ticks);
		
		 if(mouseY >= unitsList.top && mouseY <= unitsList.bottom) {
 			// getSlotIndexFromScreenCoords()
			 int i = unitsList.func_148124_c(mouseX, mouseY);
			 
	         if(i != -1) {
	        	 
	        	 if(col_states[columnInventory] && mouseX >= unitsList.col_starts[columnInventory] && mouseX <= unitsList.col_starts[columnInventory] + unitsList.col_lens[columnInventory]) {
	        		 
	        		 Entry entry = (Entry) unitsList.getListEntry(i);
	        		 int invIndex = (mouseX - unitsList.col_starts[columnInventory]) / 20;
		    		   LOTREntityNPC npc = entry.npc;
		    		   ItemStack stack = null;
		    		   boolean noArmor = true;
		    		  
		    		   int id = npc.getEntityId();
		    		  
		    		   	   boolean warrior = npc.hiredNPCInfo.getTask() == Task.WARRIOR;
		    			   if(warrior) {
		    				   if(invIndex < 4/* && invIndex >= 0*/) {
		    					   stack = npc.getEquipmentInSlot(4-invIndex);
		    					   noArmor = false;
		    				   }
		    				   invIndex -= 4; // skip armor of warriors
		    			   }
		    			   
		    			   if(noArmor) {
		    				   if(inventories != null && inventories.containsKey(id)) {
		    					   invIndex = Math.min(invIndex, GuardsOverviewPacket.SIZE_ITEMS-1);
		    					   
		    					   stack = inventories.get(id)[invIndex];
		    				   }
		    				   if(warrior) {
		    					   if(invIndex == 2) { // index 7 but we start at zero and -4 because warrior armor
			    					   ItemStack bomb = npc.npcItemsInv.getBomb();
				   						if(bomb != null) {
				   							stack = bomb;
				   						}
			    				   }
			    				   else if(invIndex == 0 && inventories == null) { // fallback
			    					   ItemStack held = npc.getHeldItem();
				   						if(held != null) {
				   							stack = held;
				   						}
			    				   }
		    				   }
		    			   }
		    			   
		    			   if(stack != null) {
		    				   renderToolTip(stack, mouseX, mouseY);
		    				   
		    				   GL11.glDisable(GL11.GL_LIGHTING);
		    				   GL11.glDisable(GL11.GL_DEPTH_TEST);
		    				   GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		    		            
		    			   }  
	        	 }
	    	  }
	       }
		
		drawCenteredString(fontRendererObj, translatedtitle, width / 2, 10, 0xffffffff);
		
        
        
        
        drawString(fontRendererObj, translatedFilters, 30, 30, 0xffffffff);
        filtercompany.drawTextBox();

        drawString(fontRendererObj, translatedColumns, 30, 50+4, 0xffffffff);
        
        
        drawSummary();
        
    }
	
	public void drawSummary() {
		int soldiers = unitsList.soldiers;
        int farmhands = unitsList.farmhands;
        int banners = unitsList.bannerBearers;
        int riders = unitsList.riders;
        int archers = unitsList.archers;
        int total = farmhands + banners + soldiers;
        
        if(total == 0) return;
        
        int iconSize = 12;
        
        String totalStr = total+" \u00a7f(";
        
        boolean anySoldiers = soldiers > 0;
        boolean anyBanners = banners > 0;
        boolean anyFarmhands = farmhands > 0;
        boolean anyRiders = riders > 0;
        boolean anyArchers = archers > 0;
        
        boolean b = false;
       
        /*String soldiersStr = soldiers+"";
        String bannersStr = banners+"";
        String farmhandsStr = farmhands+"";
        String ridersStr = riders+"";
        String archersStr = archers+"";*/
        
        String archersStr = archers+"";
        String end = " )";
        
        if(!b && anyArchers) {
        	b = true;
        	archersStr += end;
        }
        
        String ridersStr = riders+"";
        
        if(!b && anyRiders) {
        	b = true;
        	ridersStr += end;
        }
        
        String farmhandsStr = farmhands+"";
        
        if(!b && anyFarmhands) {
        	b = true;
        	farmhandsStr += end;
        }
        
        String bannersStr = banners+"";
        
        if(!b && anyBanners) {
        	b = true;
        	bannersStr += end;
        }
        
        String soldiersStr = soldiers+"";
        
        if(!b) {
        	b = true;
        	soldiersStr += end;
        }
        
        
        
        
        
        
		int xTotal = checkboxFarmers.xPosition + checkboxFarmers.width+10;
		int xSoldiers = xTotal+fontRendererObj.getStringWidth(totalStr)+2;
		int xBanners = anySoldiers ? xSoldiers+fontRendererObj.getStringWidth(soldiersStr)+2+iconSize+2 : xSoldiers;
		int xFarmhands = anyBanners ? xBanners+fontRendererObj.getStringWidth(bannersStr)+2+iconSize+2 : xBanners;
		int xRiders = anyFarmhands ? xFarmhands+fontRendererObj.getStringWidth(farmhandsStr)+2+iconSize+2 : xFarmhands;
		int xArchers = anyRiders ? xRiders+fontRendererObj.getStringWidth(ridersStr)+2+iconSize+2 : xRiders;
		
		int y = checkboxFarmers.yPosition+2;
		//999 ($ 99 $ 99 $ 99)
        
        
        
        drawString(fontRendererObj, totalStr, xTotal, y+2, 0xff6ffc03); // green
        if(anySoldiers)
        drawString(fontRendererObj, soldiersStr, xSoldiers+iconSize+2, y+2, 0xffffffff);
        if(anyBanners)
        drawString(fontRendererObj, bannersStr, xBanners+iconSize+2, y+2, 0xffffffff);
        if(anyFarmhands)
        drawString(fontRendererObj, farmhandsStr, xFarmhands+iconSize+2, y+2, 0xffffffff);
        if(anyRiders)
        drawString(fontRendererObj, ridersStr, xRiders+iconSize+2, y+2, 0xffffffff);
        if(anyArchers)
        drawString(fontRendererObj, archersStr, xArchers+iconSize+2, y+2, 0xffffffff);
        
        
        //GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
        if(anySoldiers)
    	drawTexturedModelRectFromIcon(xSoldiers, y, Items.iron_sword.getIconFromDamage(0), iconSize, iconSize);
        if(anyBanners)
    	drawTexturedModelRectFromIcon(xBanners, y, LOTRMod.banner.getIconFromDamage(21), iconSize, iconSize);
        if(anyFarmhands)
    	drawTexturedModelRectFromIcon(xFarmhands, y, Items.iron_hoe.getIconFromDamage(0), iconSize, iconSize);
        if(anyRiders)
    	drawTexturedModelRectFromIcon(xRiders, y, LOTRMod.horseArmorRivendell.getIconFromDamage(0), iconSize, iconSize);
        if(anyArchers)
    	drawTexturedModelRectFromIcon(xArchers, y, LOTRMod.ironCrossbow.getIconFromDamage(0), iconSize, iconSize);
    	//GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	
}
