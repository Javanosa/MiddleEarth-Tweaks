package metweaks.client.gui.config;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.Tessellator;

public class GuiMismatchesList extends GuiListExtended {
	
	public final GuiListExtended.IGuiListEntry[] entrys;
	public Minecraft mc;
	
	public int nameLen = 60;

	public GuiMismatchesList(Minecraft minecraft, int width, int height, int top, int bottom, int slotHeight, List<Object[]> mismatches) {
		super(minecraft, width, height, top, bottom, slotHeight);
		mc = minecraft;
		field_148163_i = false; // dont center vertical
		int len = mismatches.size();
		entrys = new GuiListExtended.IGuiListEntry[len];
		for(int i = 0; i < len; i++) {
			Object[] mismatch = mismatches.get(i);
			entrys[i] = new Entry(mismatch[0], mismatch[1], mismatch[2]);
			
		}
		
	}

	@Override
	public IGuiListEntry getListEntry(int i) {
		return entrys[i];
	}

	@Override
	protected int getSize() {
		return entrys.length;
	}
	
	protected int getScrollBarX() {
        return super.getScrollBarX() + 15;
    }
	
	public int getListWidth() {
		return nameLen + 170;
	}
	
	public class Entry implements IGuiListEntry {
		
		public final String name;
		public final GuiButton valueClient;
		public final GuiButton valueServer;
		
		public Entry(Object mismatch, Object valueServer , Object valueClient) {
			this.name = mismatch.toString();
			int len = mc.fontRenderer.getStringWidth(name);
			if(nameLen < len)
				nameLen = len;
			this.valueServer = new GuiButton(0, 0, 0, 80, 18, valueServer.toString());
			this.valueServer.packedFGColour = 0x38db07;
			this.valueClient = new GuiButton(0, 0, 0, 80, 18, valueClient.toString());
			this.valueClient.enabled = false;
			this.valueClient.packedFGColour = 0xed3309;
			
		}

		@Override
		public void drawEntry(int slot, int x, int y, int listWidth, int slotHeight, Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
			
			mc.fontRenderer.drawString(name, x, y + slotHeight / 2 - mc.fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);
			x += nameLen + 10;
			valueServer.xPosition = x;
	        valueServer.yPosition = y;
	        valueServer.drawButton(mc, 0, 0);
			x += 80;
			valueClient.xPosition = x;
			valueClient.yPosition = y;
            valueClient.drawButton(mc, 0, 0);
            
           
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
