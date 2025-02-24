package metweaks.guards;

import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaksConfig;
import metweaks.guards.customranged.CustomRanged;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public class ExtraHiredData {
	public byte ammoRangeDef;
	public byte ammoRange;
	public byte ammoRangePreTemp;
	
	public byte aiRangeDef;
	public byte aiRange;	
	
	public boolean ignoreSight;
	
	public static byte getBonus(LOTREntityNPC npc) {
		byte bonus = 0;
	    if(npc.hiredNPCInfo.xpLevel >= GuardsAdvSettingsConfig.aiRangeUnlockLvL) {
	    	bonus = (byte) (1 + (npc.hiredNPCInfo.xpLevel - GuardsAdvSettingsConfig.aiRangeUnlockLvL) * GuardsAdvSettingsConfig.aiRangeUnlockFactor);
	    }
		return bonus;
	}
	
	public static byte getMax(int bonus, int def) {
		 return (byte) Math.min(def + bonus, Math.max(def, GuardsAdvSettingsConfig.maxAiRange));
	}
	
	public void readNBT(LOTREntityNPC npc, NBTTagCompound data) {
		// load default
		// doesnt need save but might be usefull
    	
		if(GuardsAdvSettingsConfig.allowAiRangeModify) {
	    	aiRange = (byte) npc.getEntityAttribute(SharedMonsterAttributes.followRange).getBaseValue();
	    	if(data.hasKey("AiRangeDef")) {
	    		aiRangeDef = data.getByte("AiRangeDef");
		    }
		    else {
		    	aiRangeDef = aiRange;
	    		
		    }
		}
		
		if(GuardsAdvSettingsConfig.allowAmmoRangeModify) {
			
	    	if(data.hasKey("AmmoRangeDef")) {
	    		ammoRangeDef = data.getByte("AmmoRangeDef");
	    		// we need the true init value to determine the range factor
	    		if(!CustomRanged.preInit(npc))
	    			ammoRangePreTemp = NpcReflectionAccess.getAmmoRange(npc);
	    	}
	    	else {
	    		ammoRangePreTemp = ammoRangeDef = NpcReflectionAccess.getAmmoRange(npc);
	    		
	    		
	    	}
	    	
	    	ammoRange = ammoRangeDef;
	    	if(data.hasKey("AmmoRange")) {
		    	ammoRange = data.getByte("AmmoRange");
		    	NpcReflectionAccess.setAmmoRange(npc, ammoRange);
		    	
		    }
		}
    	
		if(GuardsAdvSettingsConfig.allowCheckSightModify) {
		    if(data.hasKey("IgnoreSight")) {
		    	NpcReflectionAccess.setCheckSight(npc, false);
		    	ignoreSight = true;
		    	
		    }
		}
	}
	
	public void writeNBT(NBTTagCompound data) {
		if(GuardsAdvSettingsConfig.allowAiRangeModify)
			data.setByte("AiRangeDef", aiRangeDef);
		
		if(GuardsAdvSettingsConfig.allowAmmoRangeModify) {
			data.setByte("AmmoRangeDef", ammoRangeDef);
			if(ammoRange != ammoRangeDef) {
				data.setByte("AmmoRange", ammoRange);
			}
			else {
				data.removeTag("AmmoRange");
			}
		}
		if(GuardsAdvSettingsConfig.allowCheckSightModify) {
			if(ignoreSight) {
				data.setBoolean("IgnoreSight", ignoreSight);
			}
			else {
				data.removeTag("IgnoreSight");
			}
		}
		
		
		
	}
	
	public static ExtraHiredData load(LOTREntityNPC npc) {
		ExtraHiredData data = HiredInfoAccess.getExt(npc.hiredNPCInfo);
		
		if(data == null) {
			
			data = new ExtraHiredData();
			data.readNBT(npc, new NBTTagCompound()); 
			HiredInfoAccess.setExt(npc.hiredNPCInfo, data);
			
			
			
		}
		return data;
	}
	
	public void setIgnoreSight(boolean ignore, LOTREntityNPC npc) {
		if(GuardsAdvSettingsConfig.allowCheckSightModify) {
			ignoreSight = ignore;
			NpcReflectionAccess.setCheckSight(npc, !ignore);
			if(MeTweaksConfig.debug >= 2)
				System.out.println("IgnoreSight: "+ignoreSight+" RefSi:"+!NpcReflectionAccess.getCheckSight(npc));
		}
	}

	public void setAiRange(byte range, LOTREntityNPC npc) {
		if(GuardsAdvSettingsConfig.allowAiRangeModify) {
			aiRange = range;
	    	npc.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(range);
	    	if(MeTweaksConfig.debug >= 2) {
	    		System.out.println("UnlockLVL: "+GuardsAdvSettingsConfig.aiRangeUnlockLvL+" Bonus: "+getBonus(npc));
		    	System.out.println("AI C: "+aiRange+" Def:"+aiRangeDef+" Max:"+getMax(getBonus(npc), aiRangeDef)+" Ref:"+(byte) npc.getEntityAttribute(SharedMonsterAttributes.followRange).getBaseValue());
	    	}
	    	if(ASMConfig.aiRangedImprovements)
	    		CustomRanged.updateMoveRange(npc, ammoRange);
	    }
	}
	
	public void setAmmoRangeVerify(int value, LOTREntityNPC npc) {
		if(GuardsAdvSettingsConfig.allowAmmoRangeModify) {
			byte range = (byte) MathHelper.clamp_int(value, 0, ExtraHiredData.getMax(ExtraHiredData.getBonus(npc), ammoRangeDef));
	    	
	    	if(range != ammoRange) {
	    		ammoRange = range;
	    		NpcReflectionAccess.setAmmoRange(npc, range);
	    		
	    	}
		}
	}
	
	
	
	// onSet -> save checkSight, remove if off
	// onRead -> if exists and true set IgnoreSight
	
	// onSet -> save ammoRange, remove if equals to default
	// onRead  -> if exists set ammorange
	
	// aiRangeMax, ammoRangeMax shouldnt be cached at all because it can change a lot
	
	// onSet -> request ammoRangeDef, aiRangeDef and save if not yet done
	
}
