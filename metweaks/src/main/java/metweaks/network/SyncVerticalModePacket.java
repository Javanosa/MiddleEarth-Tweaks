package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import metweaks.MeTweaksConfig;
import metweaks.block.VerticalSlab;
import net.minecraft.entity.player.EntityPlayer;

public class SyncVerticalModePacket implements IMessage {
	boolean verticalMode;
	
	public SyncVerticalModePacket() {}
	
	public SyncVerticalModePacket(boolean mode) {
		verticalMode = mode;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		verticalMode = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(verticalMode);
	}
	
	public static class Handler implements IMessageHandler<SyncVerticalModePacket, IMessage> {
	    public IMessage onMessage(SyncVerticalModePacket packet, MessageContext context) {
	    	EntityPlayer player = context.getServerHandler().playerEntity;
	    	VerticalSlab.setVerticalMode(player, packet.verticalMode);
	    	if(MeTweaksConfig.debug >= 2)
	    		System.out.println("VerticalMode for "+player.getDisplayName()+" "+packet.verticalMode);
			return packet;
	    }
	}

}
