package metweaks.client.gui.unitoverview;

import lotr.client.gui.LOTRGuiHiredFarmerInventory;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.events.GuardEvents;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;

public class LOTRGuiHiredFarmerInvKeep extends LOTRGuiHiredFarmerInventory {
	
	final GuiScreen parent;
	
	public LOTRGuiHiredFarmerInvKeep(InventoryPlayer inv, LOTREntityNPC entity) {
		super(inv, entity);
		parent = Minecraft.getMinecraft().currentScreen;
		inventorySlots = new LOTRHiredFarmerInvKeep(inv, entity);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		
		GuardEvents.nextToShow = parent;
		NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(GuiUnitOverview.unitInvOpenID, GuardsOverviewActionPacket.ACTION_SEND_DATA));
		GuiUnitOverview.unitInvOpenID = -1;
	}
}
