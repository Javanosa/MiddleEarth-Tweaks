package metweaks.network;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.netty.buffer.ByteBuf;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRMod;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.entity.npc.LOTRUnitTradeEntry.PledgeType;
import lotr.common.fac.LOTRFaction;
import metweaks.client.gui.GuiHiredTransfer;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class HiredTransferPacket implements IMessage {
	
	public String playerName;
	
	public byte state;
	
	public static final byte STATE_SUCCESS = 0;
	public static final byte STATE_INVALID_PLAYER = 1;
	public static final byte STATE_SAME_PLAYER = 2;
	public static final byte STATE_REQUIRE_ALIGNMENT = 3;
	
	public static final byte STATE_NO_UNITS = 4;
	
	public String company;
	public TIntList units;
	
	public HiredTransferPacket() {}
	
	public HiredTransferPacket(String transformTo, byte answer) {
		playerName = transformTo;
		state = answer;
	}
	
	

	@Override
	public void fromBytes(ByteBuf buf) {
		state = buf.readByte();
		boolean useCompany = buf.readBoolean();
		if(buf.isReadable()) {
			int length = buf.readInt();
			playerName = buf.readBytes(length).toString(Charsets.UTF_8);
		}
		
		if(buf.isReadable()) {
			int length = buf.readInt();
			if(useCompany) {
				company = buf.readBytes(length).toString(Charsets.UTF_8);
			}
			else {
				units = new TIntArrayList();
				for(int i = 0; i < length; i++) {
					units.add(buf.readInt());
				}
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(state);
		boolean useCompany = company != null;
		buf.writeBoolean(useCompany);
		if(playerName != null) {
			byte[] sqBytes = playerName.getBytes(Charsets.UTF_8);
		    buf.writeInt(sqBytes.length);
		    buf.writeBytes(sqBytes);
	    }
		
		if(useCompany) {
			byte[] sqBytes = company.getBytes(Charsets.UTF_8);
		    buf.writeInt(sqBytes.length);
		    buf.writeBytes(sqBytes);
	    }
		else if(units != null && !units.isEmpty()) {
			int num = units.size();
			buf.writeInt(num);
			for(int i = 0; i < num; i++) {
				buf.writeInt(units.get(i));
			}
		}
	}
	
	public static class Handler implements IMessageHandler<HiredTransferPacket, IMessage> {
		
		
		
		static Field field_nameToProfileMap;
		@SuppressWarnings("rawtypes")
		static Class profileEntry_class;
		static Field field_profileEntry_date;
		static Field field_profileEntry_profile;
		
		@SuppressWarnings("rawtypes")
		public static UUID getQuickUUID(String name) {
			if(field_nameToProfileMap == null) {
				field_nameToProfileMap = ReflectionHelper.findField(PlayerProfileCache.class, "field_152661_c");
				try {
					profileEntry_class = Class.forName("net.minecraft.server.management.PlayerProfileCache$ProfileEntry");
				} catch (Exception e) {
					e.printStackTrace();
				}
				field_profileEntry_date = ReflectionHelper.findField(profileEntry_class, "field_152673_c");
				field_profileEntry_profile = ReflectionHelper.findField(profileEntry_class, "field_152672_b");
			}
			MinecraftServer server = MinecraftServer.getServer();
			PlayerProfileCache cache = server.func_152358_ax();
			String lowercased = name.toLowerCase(Locale.ROOT);
			GameProfile profile = null;
			
			try {
				Map nameToProfileMap = (Map) field_nameToProfileMap.get(cache);
				Object profileEntry = nameToProfileMap.get(lowercased);
				if(profileEntry != null) {
					Date date = (Date) field_profileEntry_date.get(profileEntry);
					if(date != null && date.getTime() >= new Date().getTime()) {
						profile = (GameProfile) field_profileEntry_profile.get(profileEntry);
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			if(profile == null) {
				// extra handling for singleplayer
				if(server.isSinglePlayer()) {
					profile = server.func_152358_ax().func_152655_a(name);
					
				}
				// extra handling for offline mode
				/*else if(!server.isServerInOnlineMode()) {
					
					
					return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
				}*/
			}
			
			return profile != null ? profile.getId() : null;
		}
		
		
		
		public static boolean validPledge(LOTREntityNPC npc, LOTRPlayerData data) {
			PledgeType type = npc.hiredNPCInfo.pledgeType;
		    if(type == PledgeType.NONE)
		        return true;
		    LOTRFaction fac = data.getPledgeFaction();
		    if(type == PledgeType.FACTION)
		    	return data.isPledgedTo(fac); 
		      if(type == PledgeType.ANY_ELF)
		        return (fac != null && fac.isOfType(LOTRFaction.FactionType.TYPE_ELF) && !fac.isOfType(LOTRFaction.FactionType.TYPE_MAN)); 
		      if(type == PledgeType.ANY_DWARF)
		        return (fac != null && fac.isOfType(LOTRFaction.FactionType.TYPE_DWARF)); 
		      return false;
		}
		
		public static void transferHired(LOTREntityNPC npc, UUID uuid)  {
			LOTRHiredNPCInfo info = npc.hiredNPCInfo;
			try {
				field_hiringPlayerUUID.set(info, uuid);
			} catch (Exception e) {
				e.printStackTrace();
			}
			info.setSquadron(info.getSquadron()); // just call markDirty()
		}
		
		static Field field_hiringPlayerUUID = ReflectionHelper.findField(LOTRHiredNPCInfo.class, "hiringPlayerUUID");
		
		public LOTREntityNPC validateNPC(Object entity, UUID myID) {
			if(entity instanceof LOTREntityNPC) {
				LOTREntityNPC npc = (LOTREntityNPC) entity;
				if(npc.hiredNPCInfo.isActive && myID.equals(npc.hiredNPCInfo.getHiringPlayerUUID())) {
					return npc;
				}
			}
			return null;
		}
		
		public boolean failRequirements(LOTREntityNPC npc, LOTRPlayerData data) {
			if(data == null || data.getAlignment(npc.getFaction()) < npc.hiredNPCInfo.alignmentRequiredToCommand
			|| !validPledge(npc, data))
					return true;
			return false;
		}
		
		public List<LOTREntityNPC> fromIDs(TIntList units, EntityPlayer player) {
			List<LOTREntityNPC> list = new ArrayList<>();
			
			if(units == null) return list;
			
			UUID myID = player.getUniqueID();
			
			for(int id : units.toArray()) {
				Object entity = player.worldObj.getEntityByID(id);
				LOTREntityNPC npc = validateNPC(entity, myID);
				if(npc != null) {
					list.add(npc);
				}
			}
			return list;
		}
		
		public List<LOTREntityNPC> fromCompany(String company, EntityPlayer player) {
			List<LOTREntityNPC> list = new ArrayList<>();
			if(StringUtils.isWhitespace(company)) return list;
			
			UUID myID = player.getUniqueID();
			

			for(Object entity : player.worldObj.loadedEntityList) {
				LOTREntityNPC npc = validateNPC(entity, myID);
				if(npc != null) {
					String npcSquadron = npc.hiredNPCInfo.getSquadron();
					if(npcSquadron != null && npcSquadron.equalsIgnoreCase(company)) {
						list.add(npc);
					}
				}
			}
			return list;
		}
		
	    public IMessage onMessage(HiredTransferPacket packet, MessageContext context) {
	    	EntityPlayer player;
	    	Side side = context.side;

	    	
	    	if(side == Side.CLIENT) {
	    		player = LOTRMod.proxy.getClientPlayer();
	    	}
	    	else {
	    		player = context.getServerHandler().playerEntity;
	    	}
	    	
	    	// handle client
	    	if(side == Side.CLIENT) {
	    		GuiHiredTransfer.replyState = packet.state;
				if(GuiHiredTransfer.replyState == STATE_SUCCESS) {
					((EntityClientPlayerMP) player).closeScreenNoPacket();
				}
				return null;
	    	}
	    	// get entities
	    	
	    	List<LOTREntityNPC> entities;
	    	if(packet.company != null) {
	    		entities = fromCompany(packet.company, player);
	    	}
	    	else {
	    		entities = fromIDs(packet.units, player);
	    	}
	    	
	    	
	    	byte answer = STATE_SUCCESS;
	    	if(entities.isEmpty()) {
	    		answer = STATE_NO_UNITS;
	    	}
	    	else {
	    		
	    		UUID uuid = player.getUniqueID();
	    		UUID transferUUID = getQuickUUID(packet.playerName);
	    		if(transferUUID == null) {
					answer = STATE_INVALID_PLAYER;
				}
				else if(transferUUID.equals(uuid)) {
					answer = STATE_SAME_PLAYER;
				}
				else {
					LOTRPlayerData data = LOTRLevelData.getData(transferUUID);
					for(LOTREntityNPC npc : entities) {
						if(failRequirements(npc, data)) {
							answer = STATE_REQUIRE_ALIGNMENT;
							break;
						}
					}
				}
	    		
	    		if(answer == STATE_SUCCESS) {
	    			for(LOTREntityNPC npc : entities) {
	    				transferHired(npc, transferUUID);
	    				Entity mount = npc.ridingEntity;
	    	            Entity rider = npc.riddenByEntity;
	    	            if(mount instanceof LOTREntityNPC) {
	    	              LOTREntityNPC mountNPC = (LOTREntityNPC) mount;
	    	              if (mountNPC.hiredNPCInfo.isActive && mountNPC.hiredNPCInfo.getHiringPlayerUUID() == uuid)
	    	            	  transferHired(mountNPC, transferUUID);
	    	            } 
	    	            if(rider instanceof LOTREntityNPC) {
	    	              LOTREntityNPC riderNPC = (LOTREntityNPC) rider;
	    	              if (riderNPC.hiredNPCInfo.isActive && riderNPC.hiredNPCInfo.getHiringPlayerUUID() == uuid)
	    	            	  transferHired(riderNPC, transferUUID);
	    	            }
    				}
    				
    	            EntityPlayerMP playerTransferTo = MinecraftServer.getServer().getConfigurationManager().func_152612_a(packet.playerName);
    	            if(playerTransferTo != null) {
    	            	boolean one = entities.size() == 1;
    	            	boolean company = packet.company != null;
    	            	String info = one ? entities.get(0).getCommandSenderName() : company ? packet.company : null;
	    	            ChatComponentText component = new ChatComponentText(StatCollector.translateToLocalFormatted(
	    	            		one ? "gui.transfer.sent" : company ? "gui.transfer.sentCompany" : "gui.transfer.sentMany", player.getCommandSenderName(), entities.size(), info));
	    				component.getChatStyle().setColor(EnumChatFormatting.GREEN);
	    				playerTransferTo.addChatComponentMessage(component);
    	            }
    			}
	    	}
	    	NetworkHandler.networkWrapper.sendTo(new HiredTransferPacket(null, answer), (EntityPlayerMP) player);
			return null;
	    }
	}

}
