package metweaks.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import gnu.trove.map.TByteByteMap;
import gnu.trove.map.hash.TByteByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.potion.LOTRPotionPoisonKillingFlex;
import net.minecraftforge.common.config.Configuration;

public class SyncedConfig implements IMessage {
	public boolean verticalSlabs;
	public int killPotionID;
	public boolean mirrorVerticalSlabs;
	public boolean ASM_guardsEquipRanged;
	
	public boolean woolSlabs;
	public boolean barkSlabs;
	public boolean beamSlabs;
	public boolean barkWalls;
	public boolean barkStairs;
	public boolean woolStairs;
	
	public TByteByteMap compartible;
	
	// if its not readable, it will do default settings! hence why we need a working fallback
	public SyncedConfig() {
		// this stuff only needs to be called by the server and could be cached.
		
		if(MeTweaksConfig.debug >= 2)
			System.out.println("SyncedConfig init");
		
		verticalSlabs = MeTweaksConfig.verticalSlabs;
		killPotionID = MeTweaksConfig.killPotionID;
		mirrorVerticalSlabs = MeTweaksConfig.mirrorVerticalSlabs;
		ASM_guardsEquipRanged = MeTweaks.lotr && ASMConfig.guardsEquipRanged;
		
		woolSlabs = MeTweaksConfig.woolSlabs;
		barkSlabs = MeTweaksConfig.barkBlocks && MeTweaksConfig.barkSlabs;
		beamSlabs = MeTweaks.lotr && MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs;
		barkWalls = MeTweaksConfig.barkBlocks && MeTweaksConfig.barkWalls;
		barkStairs = MeTweaksConfig.barkBlocks && MeTweaksConfig.barkStairs;
		woolStairs = MeTweaksConfig.woolStairs;
	}
	
	public static final byte iVerticalSlabs = 0;
	public static final byte iGuardsEquipRanged = 1;
	public static final byte iWoolSlabs = 2;
	public static final byte iBarkSlabs = 3;
	public static final byte iBeamSlabs = 4;
	public static final byte iBarkWalls = 5;
	public static final byte iBarkStairs = 6;
	public static final byte iWoolStairs = 7;
	
	public void putCompartible(byte i, boolean val) {
		compartible.put(i, (byte) (val ? 1 : 0));
	}
	
	// client checks if it can connect to the server with the config it just received
	// optimize this?
	public boolean compartible() {
		compartible = new TByteByteHashMap(8);
		putCompartible(iVerticalSlabs, MeTweaksConfig.verticalSlabs || !verticalSlabs);
		putCompartible(iGuardsEquipRanged, (MeTweaks.lotr && ASMConfig.guardsEquipRanged) == ASM_guardsEquipRanged);
		putCompartible(iWoolSlabs, MeTweaksConfig.woolSlabs || !woolSlabs);
		putCompartible(iBarkSlabs, (MeTweaksConfig.barkBlocks && MeTweaksConfig.barkSlabs) || !barkSlabs);
		putCompartible(iBeamSlabs, (MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs) || !beamSlabs);
		putCompartible(iBarkWalls, (MeTweaksConfig.barkBlocks && MeTweaksConfig.barkWalls) || !barkWalls);
		putCompartible(iBarkStairs, (MeTweaksConfig.barkBlocks && MeTweaksConfig.barkStairs) || !barkStairs);
		putCompartible(iWoolStairs, MeTweaksConfig.woolStairs || !woolStairs);
		return !compartible.containsValue((byte) 0);
	}
	
	public void acceptSettings() {
		Configuration config = MeTweaksConfig.config;
		String categoryBlocks = MeTweaksConfig.CATEGORY_BLOCKS;
		if(save(iVerticalSlabs)) {
			config.get(categoryBlocks, "VerticalSlabs", true).set(verticalSlabs);
		}
		if(save(iWoolSlabs)) {
			config.get(categoryBlocks, "WoolSlabs", true).set(woolSlabs);
		}
		if(save(iBarkSlabs)) {
			config.get(categoryBlocks, "BarkSlabs", true).set(barkSlabs);
			config.get(categoryBlocks, "BarkBlocks", true).set(barkSlabs);
		}
		if(save(iBeamSlabs)) {
			config.get(categoryBlocks, "BeamSlabs", true).set(beamSlabs);
			config.get(categoryBlocks, "BarkBlocks", true).set(beamSlabs);
		}
		if(save(iBarkWalls)) {
			config.get(categoryBlocks, "BarkWalls", true).set(barkWalls);
			config.get(categoryBlocks, "BarkBlocks", true).set(barkWalls);
		}
		if(save(iBarkStairs)) {
			config.get(categoryBlocks, "BarkStairs", true).set(barkStairs);
			config.get(categoryBlocks, "BarkBlocks", true).set(barkStairs);
		}
		if(save(iWoolStairs)) {
			config.get(categoryBlocks, "WoolStairs", true).set(woolStairs);
		}
		
		
		config.save();
		
		if(save(iGuardsEquipRanged)) {
			ASMConfig.propertiesChange = new HashMap<>();
			ASMConfig.propertiesChange.put("guardsEquipRanged", String.valueOf(ASM_guardsEquipRanged));
			ASMConfig.init();
		}
		
		if(MeTweaksConfig.debug >= 2)
			System.out.println("acceptSettings");
	}
	
	public List<Object[]> mismatches;
	private String[] names;

	public List<Object[]> mismatches() {
		mismatches = new ArrayList<>(8);
		names = new String[] {
			"VerticalSlabs",
			"metweaks-ASM.properties/guardsEquipRanged",
			"WoolSlabs",
			"BarkSlabs & BarkBlocks",
			"BeamSlabs & BarkBlocks",
			"BarkWalls & BarkBlocks",
			"BarkStairs & BarkBlocks",
			"WoolStairs"
		};
		
		addIf(iVerticalSlabs, verticalSlabs, MeTweaksConfig.verticalSlabs);
		addIf(iGuardsEquipRanged, ASM_guardsEquipRanged, ASMConfig.guardsEquipRanged);
		addIf(iWoolSlabs, woolSlabs, MeTweaksConfig.woolSlabs);
		addIf(iBarkSlabs, barkSlabs, MeTweaksConfig.barkBlocks && MeTweaksConfig.barkSlabs);
		addIf(iBeamSlabs, beamSlabs, MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs);
		addIf(iBarkWalls, barkWalls, MeTweaksConfig.barkBlocks && MeTweaksConfig.barkWalls);
		addIf(iBarkStairs, barkStairs, MeTweaksConfig.barkBlocks && MeTweaksConfig.barkStairs);
		addIf(iWoolStairs, woolStairs, MeTweaksConfig.woolStairs);
		
		return mismatches;
	}
	
	
	
	private boolean save(byte mismatch) {
		return compartible.containsKey(mismatch) && compartible.get(mismatch) == 0;
	}
	
	private void addIf(byte mismatch, Object valueServer, Object valueClient) {
		
		
		if(save(mismatch)) {
			mismatches.add(new Object[] {names[mismatch], valueServer, valueClient});
		}
		
	}
	
	public static boolean readBoolOpt(ByteBuf buf, boolean def) {
		if(buf.isReadable()) {
			return buf.readBoolean();
		}
		return def;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		if(buf.isReadable()) {
			// avoid any problems with block and item ids
			// optimize this?
			ByteBufUtils.readVarInt(buf, 3);
			ByteBufUtils.readVarInt(buf, 3);
			if(MeTweaksConfig.debug >= 2)
				System.out.println("read max:"+buf.readableBytes());
		
		
			verticalSlabs = buf.readBoolean();
			killPotionID = buf.readByte();
			mirrorVerticalSlabs = buf.readBoolean();
			ASM_guardsEquipRanged = readBoolOpt(buf, MeTweaks.lotr && ASMConfig.guardsEquipRanged);
			woolSlabs = readBoolOpt(buf, false);
			barkSlabs = readBoolOpt(buf, false);
			beamSlabs = readBoolOpt(buf, false);
			barkWalls = readBoolOpt(buf, false);
			barkStairs = readBoolOpt(buf, false);
			woolStairs = readBoolOpt(buf, false);
			
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if(MeTweaksConfig.unrestricted())
			return;
		// avoid any problems with block and item ids
		ByteBufUtils.writeVarInt(buf, 0, 3);
		ByteBufUtils.writeVarInt(buf, 0, 3);
		if(MeTweaksConfig.debug >= 2)
			System.out.println("write max:"+buf.capacity());
		buf.writeBoolean(verticalSlabs);
		buf.writeByte(killPotionID);
		buf.writeBoolean(mirrorVerticalSlabs);
		buf.writeBoolean(ASM_guardsEquipRanged);
		
		buf.writeBoolean(woolSlabs);
		buf.writeBoolean(barkSlabs);
		buf.writeBoolean(beamSlabs);
		buf.writeBoolean(barkWalls);
		buf.writeBoolean(barkStairs);
		buf.writeBoolean(woolStairs);
		
	}
	
	public static boolean prevMirrorVerticalSlabs;
	
	// executed on client
	public boolean checkRestriction(ChannelHandlerContext ctx) {
		if(MeTweaksConfig.debug >= 2) {
			System.out.println("checkRestriction");
			System.out.println("kID="+killPotionID+" "+MeTweaksConfig.killPotionID+" vSlabs="+verticalSlabs+" vMirror="+mirrorVerticalSlabs+" gRanged="+ASM_guardsEquipRanged+" woolSlabs="+woolSlabs+" barkSlabs="+barkSlabs+" beamSlabs="+beamSlabs+" barkWalls="+barkWalls+" barkStairs="+barkStairs+" woolStairs="+woolStairs+" barkBlocks="+MeTweaksConfig.barkBlocks+" compartible="+compartible()+" unrestricted="+MeTweaksConfig.unrestricted());
		}
		
		
		if(compartible()) {
			if(killPotionID != MeTweaksConfig.killPotionID)
				LOTRPotionPoisonKillingFlex.changePotionID(MeTweaksConfig.killPotionID, killPotionID);
			
			prevMirrorVerticalSlabs = MeTweaksConfig.mirrorVerticalSlabs;
			MeTweaksConfig.mirrorVerticalSlabs = mirrorVerticalSlabs;
				
			return false;
		}
			
		
		
		NetworkDispatcher dispatcher = ctx.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
		String text = MeTweaks.MODNAME+": Some Settings need to be adjusted to play on this server";
        
       
        
        dispatcher.rejectHandshake(text);
        
        
        MeTweaks.proxy.openConfigConfirmGUI(this);
        return true;
	}

}
