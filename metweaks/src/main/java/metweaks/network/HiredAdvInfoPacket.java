package metweaks.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.guards.ExtraHiredData;
import metweaks.guards.GuardsAdvSettingsConfig;
import metweaks.guards.HiredInfoAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;

public class HiredAdvInfoPacket implements IMessage {
	public int entityID;
	
	public int homeX;
	public int homeY;
	public int homeZ;
	
	public int unlockLvL;
	
	public byte ammoRangeDef;
	public byte ammoRange;
	public byte ammoRangeMax;
	
	public byte aiRangeDef;
	public byte aiRange;
	public byte aiRangeMax;
	
	public boolean ignoreSight;
	
	public boolean allowChangeSight;
	
	public HiredAdvInfoPacket() {}
	
	public HiredAdvInfoPacket(LOTREntityNPC npc) {
		entityID = npc.getEntityId();
		
		ChunkCoordinates home = npc.getHomePosition();
	    homeX = home.posX;
	    homeY = home.posY;
	    homeZ = home.posZ;

	    // only server to client
	    if(!npc.worldObj.isRemote && ASMConfig.guardsAdvancedSettings) {
	    	ExtraHiredData data = ExtraHiredData.load(npc);
	    	
	    	unlockLvL = GuardsAdvSettingsConfig.aiRangeUnlockLvL;
	    	aiRange = data.aiRange;
	    	aiRangeDef = data.aiRangeDef;
	    	
	    	ammoRange = data.ammoRange;
		    ammoRangeDef = data.ammoRangeDef;
		    
		    ignoreSight = data.ignoreSight;
		    
		    allowChangeSight = GuardsAdvSettingsConfig.allowCheckSightModify;
		    
		    byte bonus = ExtraHiredData.getBonus(npc);
		    if(aiRangeDef > 0)
		    	aiRangeMax = ExtraHiredData.getMax(bonus, aiRangeDef);
		    if(ammoRangeDef > 0)
		    	ammoRangeMax = ExtraHiredData.getMax(bonus, ammoRangeDef);
		    
		    if(MeTweaksConfig.debug >= 2) {
			    System.out.println("UnlockLVL: "+unlockLvL+" IgnoreSight: "+ignoreSight+" Bonus: "+bonus);
			    System.out.println("AI C: "+aiRange+" Def:"+aiRangeDef+" Max:"+aiRangeMax);
			    System.out.println("Ammo C: "+ammoRange+" Def:"+ammoRangeDef+" Max:"+ammoRangeMax);
		    }
		    
	    }
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityID = buf.readInt();
		
		homeX = buf.readInt();
		homeY = buf.readInt();
		homeZ = buf.readInt();
		
		unlockLvL = buf.readInt();
		
		ammoRangeDef = buf.readByte();
		ammoRange = buf.readByte();
		ammoRangeMax = buf.readByte();
		
		aiRangeDef = buf.readByte();
		aiRange = buf.readByte();
		aiRangeMax = buf.readByte();
		
		ignoreSight = buf.readBoolean();
		
		allowChangeSight = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityID);
		
		buf.writeInt(homeX);
		buf.writeInt(homeY);
		buf.writeInt(homeZ);
		
		buf.writeInt(unlockLvL);
		
		buf.writeByte(ammoRangeDef);
		buf.writeByte(ammoRange);
		buf.writeByte(ammoRangeMax);
		
		buf.writeByte(aiRangeDef);
		buf.writeByte(aiRange);
		buf.writeByte(aiRangeMax);
		buf.writeBoolean(ignoreSight);
		
		buf.writeBoolean(allowChangeSight);
		
		
	}
	
	public static class Handler implements IMessageHandler<HiredAdvInfoPacket, IMessage> {
	    public IMessage onMessage(HiredAdvInfoPacket packet, MessageContext context) {
	    	EntityPlayer player;
	    	Side side = context.side;

	    	if(side == Side.CLIENT) {
	    		player = LOTRMod.proxy.getClientPlayer();
	    	}
	    	else {
	    		player = context.getServerHandler().playerEntity;
	    	}
	    	
	    	Entity entity = player.worldObj.getEntityByID(packet.entityID);
	    	if(entity instanceof LOTREntityNPC) {
	    		LOTREntityNPC npc = (LOTREntityNPC) entity;
	    		if((side == Side.CLIENT || npc.hiredNPCInfo.isActive) && npc.hiredNPCInfo.getHiringPlayer() == player) {
	    			// both client/server
	    			
	    			
	    			if(side == Side.CLIENT) {
	    				MeTweaks.proxy.openGuiHiredAdvSettings(packet, npc);
	    			}
	    			else if(npc.hiredNPCInfo.guardMode) {
	    				int x = packet.homeX;
	    				int y = packet.homeY;
	    				int z = packet.homeZ;
	    				int range = HiredInfoAccess.getWanderRange(npc.hiredNPCInfo);
		    			
	    				npc.setHomeArea(x, y, z, range);
		    			if(npc.riddenByEntity != null && npc.riddenByEntity instanceof LOTREntityNPC) {
		    				// its a mount
		    				((EntityCreature) npc.riddenByEntity).setHomeArea(x, y, z, range);
		    				
		    			}
		    			else if(npc.ridingEntity != null && npc.ridingEntity instanceof LOTREntityNPC) {
		    				// its a rider
		    				((EntityCreature) npc.ridingEntity).setHomeArea(x, y, z, range);
		    				
		    			}
	    			}
	    		}
	    	}
			return null;
	    }
	}

}
