package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import metweaks.MeTweaksConfig;
import metweaks.features.UnitMonitor;
import net.minecraft.entity.player.EntityPlayer;

public class SyncUnitTrackingModePacket implements IMessage {
	int mode;
	
	public SyncUnitTrackingModePacket() {}
	
	public SyncUnitTrackingModePacket(int m) {
		mode = m;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		mode = buf.readByte();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(mode);
	}
	
	public static class Handler implements IMessageHandler<SyncUnitTrackingModePacket, IMessage> {
	    public IMessage onMessage(SyncUnitTrackingModePacket packet, MessageContext context) {
	    	EntityPlayer player = context.getServerHandler().playerEntity;
	    	UnitMonitor.setUnitTrackingMode(player, packet.mode);
	    	if(MeTweaksConfig.debug >= 2)
	    		System.out.println("UnitTrackingMode for "+player.getDisplayName()+" "+packet.mode);
			return packet;
	    }
	}

}
