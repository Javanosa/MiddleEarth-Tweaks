package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import metweaks.MeTweaksConfig;

public class SyncConquestDecayPacket implements IMessage {
	float conqDecay;
	
	public SyncConquestDecayPacket() {
		conqDecay = MeTweaksConfig.conquestDecay;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		conqDecay = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(conqDecay);
	}
	
	public static class Handler implements IMessageHandler<SyncConquestDecayPacket, IMessage> {
	    public IMessage onMessage(SyncConquestDecayPacket packet, MessageContext context) {
	    	MeTweaksConfig.conquestDecay = packet.conqDecay;
	    	if(MeTweaksConfig.debug >= 2)
	    		System.out.println("Conquest Decay set from server to "+MeTweaksConfig.conquestDecay);
			return packet;
	    }
	}

}
