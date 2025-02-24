package metweaks.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.potion.LOTRPotionPoisonKillingFlex;

public class SyncedConfig implements IMessage {
	public boolean verticalSlabs;
	public int killPotionID;
	public boolean mirrorVerticalSlabs;
	public boolean ASM_guardsEquipRanged;
	
	public boolean woolSlabs;
	public boolean barkSlabs;
	public boolean beamSlabs;
	
	public Map<String, Boolean> compartible;
	
	public SyncedConfig() {
		if(MeTweaksConfig.debug >= 2)
			System.out.println("SyncedConfig init");
		
		verticalSlabs = MeTweaksConfig.verticalSlabs;
		killPotionID = MeTweaksConfig.killPotionID;
		mirrorVerticalSlabs = MeTweaksConfig.mirrorVerticalSlabs;
		ASM_guardsEquipRanged = MeTweaks.lotr && ASMConfig.guardsEquipRanged;
		
		woolSlabs = MeTweaksConfig.woolSlabs;
		barkSlabs = MeTweaksConfig.barkBlocks && MeTweaksConfig.barkSlabs;
		beamSlabs = MeTweaks.lotr && MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs;
	}
	
	public boolean compartible() {
		compartible = new HashMap<>();
		compartible.put("VerticalSlabs", MeTweaksConfig.verticalSlabs || MeTweaksConfig.verticalSlabs == verticalSlabs);
		compartible.put("metweaks-ASM.properties/guardsEquipRanged", (MeTweaks.lotr && ASMConfig.guardsEquipRanged) == ASM_guardsEquipRanged);
		compartible.put("WoolSlabs", MeTweaksConfig.woolSlabs || !woolSlabs);
		
		compartible.put("BarkSlabs & BarkBlocks", (MeTweaksConfig.barkBlocks && MeTweaksConfig.barkSlabs) || !barkSlabs);
		compartible.put("BeamSlabs & BarkBlocks", (MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs) || !beamSlabs);
		
		return !compartible.containsValue(false);
	}
	
	public void acceptSettings() {
		if(save("VerticalSlabs"))
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_BLOCKS, "VerticalSlabs", true).set(verticalSlabs);
		
		if(save("WoolSlabs"))
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_BLOCKS, "WoolSlabs", true).set(woolSlabs);
		if(save("BarkSlabs & BarkBlocks")) {
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_BLOCKS, "BarkSlabs", true).set(barkSlabs);
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_BLOCKS, "BarkBlocks", true).set(barkSlabs);
			
		}
		if(save("BeamSlabs & BarkBlocks")) {
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_BLOCKS, "BeamSlabs", true).set(beamSlabs);
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_BLOCKS, "BarkBlocks", true).set(beamSlabs);
			
		}
		
		MeTweaksConfig.config.save(); 
		if(MeTweaksConfig.debug >= 2)
			System.out.println("acceptSettings");
	}

	public List<Object[]> mismatches() {
		mismatches = new ArrayList<>();
		
		addIf("VerticalSlabs", verticalSlabs, MeTweaksConfig.verticalSlabs);
		addIf("metweaks-ASM.properties/guardsEquipRanged", ASM_guardsEquipRanged, ASMConfig.guardsEquipRanged);
		addIf("WoolSlabs", woolSlabs, MeTweaksConfig.woolSlabs);
		addIf("BarkSlabs & BarkBlocks", barkSlabs, MeTweaksConfig.barkBlocks && MeTweaksConfig.barkSlabs);
		addIf("BeamSlabs & BarkBlocks", beamSlabs, MeTweaksConfig.barkBlocks && MeTweaksConfig.beamSlabs);
		
		return mismatches;
	}
	
	public List<Object[]> mismatches;
	
	private boolean save(String mismatch) {
		return compartible.containsKey(mismatch) && !compartible.get(mismatch);
	}
	
	private void addIf(String mismatch, Object valueServer, Object valueClient) {
		if(compartible.containsKey(mismatch) && !compartible.get(mismatch)) {
			mismatches.add(new Object[] {mismatch, valueServer, valueClient});
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
		
	}
	
	public static boolean prevMirrorVerticalSlabs;
	
	public boolean checkRestriction(ChannelHandlerContext ctx) {
		if(MeTweaksConfig.debug >= 2) {
			System.out.println("checkRestriction");
			System.out.println("kID="+killPotionID+" "+MeTweaksConfig.killPotionID+" vSlabs="+verticalSlabs+" vMirror="+mirrorVerticalSlabs+" gRanged="+ASM_guardsEquipRanged+" woolSlabs="+woolSlabs+" barkSlabs="+barkSlabs+" beamSlabs="+beamSlabs+" barkBlocks="+MeTweaksConfig.barkBlocks+" compartible="+compartible()+" unrestricted="+MeTweaksConfig.unrestricted());
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
