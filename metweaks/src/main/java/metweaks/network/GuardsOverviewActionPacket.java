package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import metweaks.MeTweaks;
import metweaks.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class GuardsOverviewActionPacket implements IMessage {
	public static final byte ACTION_SEND_DATA = 0;
	public static final byte ACTION_OPEN_INV = 1;
	
	byte action;
	int entityID;
	
	public GuardsOverviewActionPacket() {}
	
	public GuardsOverviewActionPacket(int entityID, byte action) {
		this.entityID = entityID;
		this.action = action;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		action = buf.readByte();
		entityID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(action);
		buf.writeInt(entityID);
	}
	
	public static class Handler implements IMessageHandler<GuardsOverviewActionPacket, IMessage> {
	    public IMessage onMessage(GuardsOverviewActionPacket packet, MessageContext context) {
	    	
	    	
	    	EntityPlayerMP player = context.getServerHandler().playerEntity;
	    	byte action = packet.action;
	    	if(action == ACTION_SEND_DATA) {
	    		NetworkHandler.networkWrapper.sendTo(new GuardsOverviewPacket(player, packet.entityID), player);
	    	}
	    	else if(action == ACTION_OPEN_INV) {
	    		World world = player.worldObj;
	    		Entity npc = world.getEntityByID(packet.entityID);
	    		if(npc != null && npc instanceof LOTREntityNPC) {
	    			LOTREntityNPC hired = (LOTREntityNPC) npc;
	    			if(hired.hiredNPCInfo.isActive && hired.hiredNPCInfo.getHiringPlayer() == player) {
	    				
	    				int guiID = hired.hiredNPCInfo.getTask() == Task.WARRIOR ? CommonProxy.GUI_ID_WARRIOR_INV_KEEP : CommonProxy.GUI_ID_FARMER_INV_KEEP;
	    				
	    				hired.hiredNPCInfo.sendClientPacket(false);
	    				hired.hiredNPCInfo.isGuiOpen = true;
	    				player.openGui(MeTweaks.instance, guiID, world, packet.entityID, 0, 0);
	    				
	    			}
	    		}
	    	}
			return null;
	    }
	}
}
