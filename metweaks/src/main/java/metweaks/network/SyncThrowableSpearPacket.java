package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import metweaks.MeTweaksConfig;
import metweaks.features.ThrowableSpearState;
import net.minecraft.entity.player.EntityPlayer;

public class SyncThrowableSpearPacket implements IMessage {
	boolean throwable;
	
	public SyncThrowableSpearPacket() {}
	
	public SyncThrowableSpearPacket(boolean mode) {
		throwable = mode;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		throwable = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(throwable);
	}
	
	public static class Handler implements IMessageHandler<SyncThrowableSpearPacket, IMessage> {
	    public IMessage onMessage(SyncThrowableSpearPacket packet, MessageContext context) {
	    	EntityPlayer player = context.getServerHandler().playerEntity;
	    	ThrowableSpearState.setThrowableMode(player, packet.throwable);
	    	if(MeTweaksConfig.debug >= 2)
	    		System.out.println("ThrowableSpearMode for "+player.getDisplayName()+" "+packet.throwable);
			return packet;
	    }
	}

}
