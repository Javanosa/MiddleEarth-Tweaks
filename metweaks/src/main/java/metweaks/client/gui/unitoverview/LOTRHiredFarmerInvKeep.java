package metweaks.client.gui.unitoverview;

import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.inventory.LOTRContainerHiredFarmerInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class LOTRHiredFarmerInvKeep extends LOTRContainerHiredFarmerInventory {
	public final LOTREntityNPC npc;
	
	public LOTRHiredFarmerInvKeep(InventoryPlayer inv, LOTREntityNPC theNpc) {
		super(inv, theNpc);
		npc = theNpc;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		
		return npc != null && npc.isEntityAlive() && npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == player && npc.hiredNPCInfo.getTask() == LOTRHiredNPCInfo.Task.FARMER;
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) {
		InventoryPlayer inv = player.inventory;
        if(inv.getItemStack() != null) {
            player.dropPlayerItemWithRandomChoice(inv.getItemStack(), false);
            inv.setItemStack(null);
        }
        
        if(!npc.worldObj.isRemote) {
        	npc.hiredNPCInfo.isGuiOpen = false;
        }
	}
}
