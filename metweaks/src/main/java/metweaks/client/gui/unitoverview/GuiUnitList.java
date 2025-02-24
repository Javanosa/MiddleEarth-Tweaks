package metweaks.client.gui.unitoverview;

import static metweaks.client.gui.unitoverview.GuiUnitOverview.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import gnu.trove.map.TIntObjectMap;
import lotr.common.LOTRSquadrons;
import lotr.common.enchant.LOTREnchantment;
import lotr.common.enchant.LOTREnchantmentDurability;
import lotr.common.enchant.LOTREnchantmentHelper;
import lotr.common.entity.npc.LOTRBannerBearer;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRFarmhand;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import metweaks.network.GuardsOverviewPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;

public class GuiUnitList extends GuiListExtended {
	
	GuiUnitOverview instance;
	
	public static final RenderItem renderitem = new RenderItem();

	public GuiUnitList(Minecraft minecraft, int width, int height, int top, int bottom, int slotHeight, GuiUnitOverview gui) {
		super(minecraft, width, height, top, bottom, slotHeight);
		
		field_148163_i = false; // dont center vertically
		instance = gui;
	}
	
	public List<Entry> entrys;
	int listWidth;
	public static boolean skipUnequipable = false;
	
	@Override
	public int getListWidth() {
        return listWidth;
    }
	
	
	
	public int getListLeft() {
		return (width - getListWidth()) / 2;
	}
	
	@Override
	protected int getScrollBarX() {
        return getListWidth()+getListLeft()-8;
    }
	
	public int bannerBearers;
	public int soldiers;
	public int farmhands;
	public int riders;
	public int archers;
	
	public void initList() {
		resetLens();
		
		bannerBearers = 0;
		soldiers = 0;
		farmhands = 0;
		riders = 0;
		archers = 0;
		
		
		boolean warriors = showWarriors;
		boolean farmers = showFarmers;
		String squadron = instance.filtercompany.getText().toLowerCase();
		boolean checkSquadron = !StringUtils.isNullOrEmpty(squadron);
		entrys = new ArrayList<>();
		
		UUID uuid = Minecraft.getMinecraft().thePlayer.getUniqueID();
		@SuppressWarnings("unchecked")
		List<Entity> entities = Minecraft.getMinecraft().theWorld.loadedEntityList;
		for(Entity entity : entities) {
			if(entity instanceof LOTREntityNPC) {
				LOTREntityNPC npc = (LOTREntityNPC) entity;
				if(uuid.equals(npc.hiredNPCInfo.getHiringPlayerUUID())) {
					
					boolean warrior = npc.hiredNPCInfo.getTask() == Task.WARRIOR;
					
					if(!warriors && warrior) {
						continue;
					}
					
					if(!farmers && !warrior) {
						continue;
					}
					
					// if squadron search not empty
					// skip if squadron doesnt contains search or no squadron set
					if(checkSquadron) {
						String npcSquadron = npc.hiredNPCInfo.getSquadron();
						if(npcSquadron == null || !npcSquadron.toLowerCase().contains(squadron)) {
							continue;
						}
					}
					
					
					
					if(npc instanceof LOTRBannerBearer)
						bannerBearers++;
					else if(npc instanceof LOTRFarmhand)
						farmhands++;
					else soldiers++;
					
					if(GuardsOverviewPacket.rangedEntities.contains(npc.getClass())) {
						archers++;
					}
					
					if(npc.ridingEntity != null) {
						riders++;
					}
					
					if(warriors && skipUnequipable) {
						int slot = 0;
						boolean noEquip = true;
						// as long as slot low or no equip yet
						while(noEquip && slot < 7) {
							noEquip = !npc.canReEquipHired(slot, null);
							slot++;
						}
						
						if(noEquip) continue; // no need to show them in the list
					}
					
					entrys.add(new Entry(npc));
				}
			}
		}
		postInitLens();
		updateStarts();
		Collections.sort(entrys, new SortGlobal());
		
		
		
		
	}

	@Override
	public IGuiListEntry getListEntry(int i) {
		return entrys.get(i);
	}

	@Override
	protected int getSize() {
		return entrys.size();
	}
	
	
	
	public static final int MAX_LEN_NAME = 30;
	public static final int MAX_LEN_TYPE = 30;
	public static final int MAX_LEN_COMPANY = 30;
	public static final int MAX_LEN_DISTANCE = 30;
	
	
	
	// Row Data
	public int[] col_lens = new int[8];
	public int[] col_starts = new int [8];
	
	
	
	// min len is title
	// min len is longest entry (capped at like 20 chars), possibly using also short names for some?
	
	/*
	
	#gui 		darkened transparent background to allow well seeing of whats happening as we dont pause.
				use all with about 30px margin
				20+5px * 3 = control
				30px header
				30px footer margin
				30px left right
				
	#Title		Unit Overview (plain white text)
	#Settings	Filter Company [Text Field] [CheckBox Warriors] [CheckBox Farmers]
				Filter Company [Text Field]	[Warriors] [Farmers]
				
				Columns (Warriors only): 	Armor Lvl 
				Columns (Farmers only): 		-
				Colums (all):			Index Distance Type Name Inventory Company
				// use small checkboxes
				
				
				
	#Header	
		header with arrows of sort order which can be switched
	#List	
		
	#List Item
		right click = show in world
		left click = mark
			if any marked, show button "show"
	*/
	
	public static int getEnchantColor(ItemStack stack) {
		List<LOTREnchantment> enchants = LOTREnchantmentHelper.getEnchantList(stack);
		boolean hasEffect = false;
		for(LOTREnchantment enchant : enchants) {
			if(enchant.getClass() == LOTREnchantmentDurability.class) {
				continue;
			}
			
			/*if(enchant.getClass() == LOTREnchantmentProtectionRanged.class) {
				
			}
			
			if(enchant.getClass() == LOTREnchantmentProtectionFire.class || enchant.getClass() == LOTREnchantmentProtectionFall.class) {
				
			}*/
			
			
			if(!enchant.isBeneficial()) {
				return 0xFFb32424;
			}
			// enchants that have no effect on hireds:
			
			hasEffect = true;
		}
		
		return hasEffect ? 0xFF548a22 : 0x00DDDDDD;
	}
	
	public static String trim(String str, int maxLen) {
		if(str.length() > maxLen) {
			return str.substring(0, maxLen - 1);
		}
		return str;
	}
	
	public void resetLens() {
		Arrays.fill(col_lens, 0);
	}
	
	public void updateListWidth() {
		int width = 0;
		for(int i = 0; i < col_lens.length; i++) {
			if(col_states[i])
				width += col_lens[i];
		}
		listWidth = width;
	}
	
	public void updateLen(String str, int index) {
		int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(str);
		if(width > col_lens[index]) {
			col_lens[index] = width;
		}
		
	}
	
	public void postInitLens() {
		col_lens[columnInventory] = 7 * 20; // 7 slots, may later be done in entry constructor. We will see.
		col_lens[columnCompany] = Math.max(col_lens[columnCompany], 80);
		
		int buffer = 10;
		int lenNum = Minecraft.getMinecraft().fontRenderer.getStringWidth("999")+buffer;
		
		for(int i = 0; i < col_lens.length; i++) {
			GuiColumnButton btn = instance.col_buttons[i];
			int btnWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(btn.displayString) + 10+10+4;
			btn.width = col_lens[i] = Math.max(Math.max(lenNum, col_lens[i]+buffer), btnWidth);
			
		}
		updateListWidth();
		
	}
	
	public void updateStarts() {
		int start = (width - getListWidth()) / 2;
		for(int i = 0; i < col_lens.length; i++) {
			// check if column enabled
			if(col_states[i]) {
				col_starts[i] = start;
				instance.col_buttons[i].xPosition = start;
				start += col_lens[i];
			}
		}
	}
	
	private void drawItemStack(ItemStack stack, int x, int y) {
		int color = getEnchantColor(stack);
		Gui.drawRect(x, y, x + 20, y + 1, color);
		
		Gui.drawRect(x, y + 19, x + 20, y + 20, color);
		
		Gui.drawRect(x, y, x + 1, y + 20, color);
		
		Gui.drawRect(x + 19, y, x + 20, y + 20, color);
		
		
		
		
		
		
		
	    RenderHelper.enableGUIStandardItemLighting();
        
       
		
        renderitem.zLevel = 200.0F;
        FontRenderer font = null;
        if (stack != null) font = stack.getItem().getFontRenderer(stack);
        
        if (font == null) font = Minecraft.getMinecraft().fontRenderer;
        renderitem.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, x+2, y+2);
        renderitem.renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), stack, x+2, y+2, null);
        
       
       
        renderitem.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
        
    }
	
	public class Entry implements IGuiListEntry {
		public LOTREntityNPC npc;
		
		// ranged/melee weapon needs server side (?)
		// Warriors
		// ID  Type		  	Name				LvL	Equipment				  Armor 
		// 1   Dwarf		Nuzfib				1	[0] [1] [2] [3] [4] [5]   0			
		// 999 Mordor Orc 	Bombur son of Pori 	999	armor melee ranged | bomb 999			
		
		// inventory needs server side
		// Farmers
		// ID  Type		  	Name				Inventory				
		// 1   Dwarf		Nuzfib				[0] [1] [2] [3]
		// 999 Mordor Orc 	Bombur son of Pori 	seeds, results, bonemeal
		//public final float blocks;
		public final int blocks;
		public String name;
		public final String type;
		public String company;
		public String armor;
		public final String distance;
		
		public final GuiTextField squadronField;
		
		
		public boolean selected;
		
		
		public Entry(LOTREntityNPC npc) {
			
			selected = instance.selection != null && instance.selection.contains(npc.getEntityId());
			type = trim(npc.getEntityClassName(), MAX_LEN_TYPE);
			String strName = npc.getNPCName();
			
			
			
			updateLen(type, GuiUnitOverview.columnType);
			
			if(!npc.getEntityClassName().equals(strName)) {
				strName = trim(strName, MAX_LEN_NAME);
				updateLen(strName, GuiUnitOverview.columnName);
				name = strName;
			}
			
			String strCompany = npc.hiredNPCInfo.getSquadron();
			if(!StringUtils.isNullOrEmpty(strCompany)) {
				strCompany = trim(strCompany, MAX_LEN_COMPANY);
				updateLen(strCompany, GuiUnitOverview.columnCompany);
				company = strCompany;
			}
			
			int armorValue = npc.getTotalArmorValue();
			if(armorValue > 0) {
				armor = ""+armorValue;
			}
			
			this.npc = npc;
			
			
			
			//blocks = npc.getDistanceToEntity(Minecraft.getMinecraft().thePlayer);
			//distance = trim(""+MathHelper.floor_float(blocks), MAX_LEN_DISTANCE);
			blocks = MathHelper.floor_float(npc.getDistanceToEntity(Minecraft.getMinecraft().thePlayer));
			distance = trim(""+blocks, MAX_LEN_DISTANCE);
			updateLen(distance, GuiUnitOverview.columnDistance);
			
			
			
			  squadronField = new GuiTextField( Minecraft.getMinecraft().fontRenderer, 0, 0, 80, 17);
		      squadronField.setMaxStringLength(LOTRSquadrons.SQUADRON_LENGTH_MAX);
		      if(company != null)
		    	  squadronField.setText(company);
		      squadronField.setEnableBackgroundDrawing(false);
		}
		
		
		
		@Override
		public void drawEntry(int slot, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
			boolean even = (slot & 1) == 1;
			
				
			
			
			if(selected) {
				
                Gui.drawRect(x-7, y, x+listWidth-5, y+slotHeight, 0xFF382a01);
			}
			
			if(even) {
				Gui.drawRect(x-5, y-1, x+listWidth-5, y+slotHeight+1, 0x10FFFFFF);
			}
			
			FontRenderer fs = Minecraft.getMinecraft().fontRenderer;
			// get top position for string rendering
			int ytxt = y + (slotHeight / 2) - (fs.FONT_HEIGHT / 2);
			
			if(col_states[columnIndex])
				fs.drawString(""+(slot+1), col_starts[columnIndex], ytxt, 0xffffff);
			
			if(col_states[columnType])
				fs.drawString(type, col_starts[columnType], ytxt, 0xffffff);
			
			
			if(col_states[columnName] && name != null)
				fs.drawString(name, col_starts[columnName], ytxt, 0xffffff);
			
			if(col_states[columnDistance])
				fs.drawString(distance, col_starts[columnDistance], ytxt, 0xffffff);
			
			
			boolean warrior = npc.hiredNPCInfo.getTask() == Task.WARRIOR;
			
			if(warrior && col_states[columnLvl]) {
				fs.drawString(""+npc.hiredNPCInfo.xpLevel, col_starts[columnLvl], ytxt, 0xfc4e03);
			}
			
			if(armor != null && col_states[columnArmor])
				fs.drawString(armor, col_starts[columnArmor], ytxt, 0x9e9e9e);
			
			
			
			
			if(col_states[columnCompany]) {
				int sqX = col_starts[columnCompany];
				squadronField.xPosition = sqX+4;
				squadronField.yPosition = y+6;
				int sqWidth = Math.max(80, col_lens[columnCompany]-5);
				squadronField.width = sqWidth;
				Gui.drawRect(sqX, y+2, sqX + sqWidth, y + 2 + squadronField.height, even ? 0x20FFFFFF : 0x1AFFFFFF);
				squadronField.drawTextBox();
			}
			
			
			
			// render armor items
			if(col_states[columnInventory]) {
				int xInv = col_starts[columnInventory];
			
				if(warrior) {
					for(int i = 0; i < 4; i++) {
						ItemStack stack = npc.getEquipmentInSlot(4-i);
						if(stack != null) {
							
							drawItemStack(stack, xInv, y);
						}
						xInv += 20;
					}
				}
				TIntObjectMap<ItemStack[]> invs = GuiUnitOverview.inventories;
				if(invs != null) {
					ItemStack[] items = invs.get(npc.getEntityId());
					if(items != null) {
						int numToRender = warrior ? 2 : 4;
						for(int i = 0; i < numToRender; i++) {
							ItemStack stack = items[i];
							if(stack != null) {
								drawItemStack(stack, xInv, y);
							}
							xInv += 20;
						}
					}
				}
				else if(warrior) {
					// render helf item of warrior
					ItemStack held = npc.getHeldItem();
					if(held != null) {
						drawItemStack(held, xInv, y);
						xInv += 20;
					}
				}
				
				if(warrior) {
					ItemStack bomb = npc.npcItemsInv.getBomb();
					if(bomb != null) {
						drawItemStack(bomb, xInv, y);
						xInv += 20;
					}
				}
			}
		}

		@Override
		public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_,
				int p_148278_6_) {
			return false;
		}

		@Override
		public void mouseReleased(int p_148277_1_, int p_148277_2_, int p_148277_3_, int p_148277_4_, int p_148277_5_,
				int p_148277_6_) {
		}
		
	}
	
	
	
}
