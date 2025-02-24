package metweaks.client.gui.unitoverview;

import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.inventory.LOTRContainerHiredWarriorInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class LOTRHiredWarriorInvKeep extends LOTRContainerHiredWarriorInventory {
	// dont close when out of range
	// return to our overview screen instead of npc hired screen
	// how do we open this container? needs special packet ig
	
	// extra packet for inventory stuff
	public final LOTREntityNPC npc;
	
	public LOTRHiredWarriorInvKeep(InventoryPlayer inv, LOTREntityNPC theNpc) {
		super(inv, theNpc);
		npc = theNpc;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return npc != null && npc.isEntityAlive() && npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayer() == player && npc.hiredNPCInfo.getTask() == LOTRHiredNPCInfo.Task.WARRIOR;
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
