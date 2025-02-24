package metweaks.client.gui.unitoverview;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;

public class GuiColumnButton extends GuiButton {
	
	public boolean ascending;
	public IIcon icon;
	
	public GuiColumnButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
		super(buttonId, x, y, widthIn, heightIn, buttonText);
	}
	
	@Override // playPressSound()
	public void func_146113_a(SoundHandler soundHandler) {
		super.func_146113_a(soundHandler);
		ascending = !ascending;
	}
	
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1, 1, 1, 1);
            // hoverstate
            field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            int hoverState = getHoverState(field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawTexturedModalRect(xPosition, yPosition, 0, 46 + hoverState * 20, width / 2, height);
            drawTexturedModalRect(xPosition + width / 2, yPosition, 200 - width / 2, 46 + hoverState * 20, width / 2, height);
            mouseDragged(mc, mouseX, mouseY);
            int colorText = 0xE0E0E0; // almost white

            if (!enabled) {
                colorText = 0xA0A0A0; // light gray
            }
            else if (field_146123_n) { // hoverstate
                colorText = 0xFFFFA0; // light yellow
            }
            
            
            drawSprite(xPosition, yPosition, ascending ? 18 : 36, 0);
            
            
            if(icon != null) {
            	
            	mc.getTextureManager().bindTexture(TextureMap.locationItemsTexture);
            	
            	drawTexturedModelRectFromIcon(17 + xPosition+1, yPosition+1, icon, 14, 14);
            	
            	
            }
            else drawString(fontrenderer, displayString, 18 + xPosition, yPosition + (height - 8) / 2, colorText);
        }
    }

	
	private void drawSprite(int x, int y, int u, int v) {
        
        Minecraft.getMinecraft().getTextureManager().bindTexture(statIcons);
        final int size = 18;
        final float f = 0.0078125F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x,		  y + size, zLevel, u * f, 			(v + size) * f);
        tessellator.addVertexWithUV(x + size, y + size, zLevel, (u + size) * f, (v + size) * f);
        tessellator.addVertexWithUV(x + size, y, 		zLevel, (u + size) * f, v * f);
        tessellator.addVertexWithUV(x,		  y, 		zLevel, u * f, 			v * f);
        tessellator.draw();
    }
	
}
