package metweaks.client.gui.unitoverview;

import lotr.client.gui.LOTRGuiHiredWarriorInventory;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;

public class LOTRGuiHiredWarriorInvKeep extends LOTRGuiHiredWarriorInventory {
	
	final GuiScreen parent;
	
	public LOTRGuiHiredWarriorInvKeep(InventoryPlayer inv, LOTREntityNPC entity) {
		super(inv, entity);
		parent = Minecraft.getMinecraft().currentScreen;
		inventorySlots = new LOTRHiredWarriorInvKeep(inv, entity);
	}
	
	public static boolean handleClose(int keyCode, GuiScreen parent, Minecraft mc) {
		if (keyCode == 1 || keyCode == mc.gameSettings.keyBindInventory.getKeyCode())
        {
			
            NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(GuiUnitOverview.unitInvOpenID, GuardsOverviewActionPacket.ACTION_SEND_DATA));
    		GuiUnitOverview.unitInvOpenID = -1;
    		mc.thePlayer.openContainer = mc.thePlayer.inventoryContainer;
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return true;
        }
		return false;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode)
    {
		if(handleClose(keyCode, parent, mc)) return;
        super.keyTyped(typedChar, keyCode);
    }
	
	/*@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		//mc.inGameHasFocus = true;
		GuardEvents.nextToShow = parent;
		NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(GuiUnitOverview.unitInvOpenID, GuardsOverviewActionPacket.ACTION_SEND_DATA));
		GuiUnitOverview.unitInvOpenID = -1;
		
	}*/
}
