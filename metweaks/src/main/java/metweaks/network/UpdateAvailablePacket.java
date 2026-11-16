package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import metweaks.proxy.UpdateCheck;

public class UpdateAvailablePacket implements IMessage {
	public UpdateAvailablePacket() {}
	
	@Override
	public void fromBytes(ByteBuf buf) {}

	@Override
	public void toBytes(ByteBuf buf) {}
	
	public static class Handler implements IMessageHandler<UpdateAvailablePacket, IMessage> {
	    public IMessage onMessage(UpdateAvailablePacket packet, MessageContext context) {
	    	UpdateCheck.remoteAvailable = true;
		    System.out.println("update available on server");
			return null;
	    }
	}
}
