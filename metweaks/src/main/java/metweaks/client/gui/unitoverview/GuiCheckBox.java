package metweaks.client.gui.unitoverview;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuiCheckBox extends GuiButton {
	public boolean checked;

	public GuiCheckBox(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}
	
	
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if(!visible) return;
		
        
        
        
        // draw outlines
        if(checked)  {
        	int widthRoundDown = (width / 2) * 2;
        	drawRect(xPosition-1, yPosition-1, xPosition+widthRoundDown+1, yPosition+height+1, 0xffaaaaaa);
        }
        	
        
        super.drawButton(mc, mouseX, mouseY);
    }
	
}
