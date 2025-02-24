package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import metweaks.ASMConfig;
import metweaks.MeTweaksConfig;
import metweaks.features.ItemUseHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

public class GuardmodeHornPacket implements IMessage {
	boolean change;
	boolean auto;
	int wanderRange;
	int guardRange;
	
	
	public GuardmodeHornPacket() {}
	
	public GuardmodeHornPacket(boolean change, boolean auto, int wanderRange, int guardRange) {
		this.change = change;
		this.auto = auto;
		this.wanderRange = wanderRange;
		this.guardRange = guardRange;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		change = buf.readBoolean();
		auto = buf.readBoolean();
		wanderRange = buf.readByte();
		guardRange = buf.readByte();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(change);
		buf.writeBoolean(auto);
		buf.writeByte(wanderRange);
		buf.writeByte(guardRange);
	}
	
	public static class Handler implements IMessageHandler<GuardmodeHornPacket, IMessage> {
		
	    public IMessage onMessage(GuardmodeHornPacket packet, MessageContext context) {
	    	EntityPlayer player = context.getServerHandler().playerEntity;
	    	InventoryPlayer inv = player.inventory;
	    	ItemStack current = inv.getCurrentItem();
	    	
	    	if(MeTweaksConfig.toggleGuardModeHorn && current != null && current.getItem() == LOTRMod.commandHorn ) {
	    		
	    		int meta = current.getItemDamage();
	    		if((meta == ItemUseHandler.TURN_ON_GUARDMODE || meta == ItemUseHandler.TURN_OFF_GUARDMODE) && current.hasDisplayName()) {
	    			
	    			NBTTagCompound nbt = current.getTagCompound();
	    			if(nbt == null)
	    				nbt = new NBTTagCompound();
	    			
	    				if(packet.guardRange > 0)
		    				nbt.setByte("GuardRange", (byte) MathHelper.clamp_int(packet.guardRange, LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX));
		    			
		    			
		    			if(ASMConfig.guardsWanderRange && packet.wanderRange > 0)
			    			nbt.setByte("WanderRange", (byte) MathHelper.clamp_int(packet.wanderRange, LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX));
		    			
		    			
		    			if(packet.change)
		    				nbt.setBoolean("Change", true);
		    			else nbt.removeTag("Change");
		    			
		    			if(packet.auto)
		    				nbt.setBoolean("Auto", true);
		    			else nbt.removeTag("Auto");
		    			
		    			NBTTagCompound display = (NBTTagCompound) nbt.getTag("display");
		    			
		    			if(packet.change) {
		    				NBTTagList lore = new NBTTagList();
		    				if(packet.guardRange > 0 || packet.auto)
		    					lore.appendTag(new NBTTagString(EnumChatFormatting.GRAY + StatCollector.translateToLocal("lotr.gui.warrior.guardRange")+": "+(packet.auto ? StatCollector.translateToLocal("options.guiScale.auto") : packet.guardRange)));
		    					// EnumChatFormatting.GRAY + "Guard Range: "+(packet.auto ? "Auto" : packet.guardRange)
		    				if(ASMConfig.guardsWanderRange && packet.wanderRange > 0)
								lore.appendTag(new NBTTagString(EnumChatFormatting.GRAY + StatCollector.translateToLocal("lotr.gui.warrior.wanderRange")+": "+packet.wanderRange));
		    					// EnumChatFormatting.GRAY + "Wander Range: "+packet.wanderRange
							
							display.setTag("Lore", lore);
		    			}
		    			else display.removeTag("Lore");
		    			
		    				
		    			
		    			current.setTagCompound(nbt);
	    			
	    			
	    		}
	    		else {
    				// default item
	    			current.setStackDisplayName("\u00a7r"+StatCollector.translateToLocal("gui.guardmodehorn.display")+StatCollector.translateToLocal("options.on"));
	    			
    			}
	    	}
			return packet;
	    }
	}

}
