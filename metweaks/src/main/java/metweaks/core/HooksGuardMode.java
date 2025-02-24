package metweaks.core;

import java.util.List;

import lotr.client.gui.LOTRGuiButtonOptions;
import lotr.client.gui.LOTRGuiHiredWarrior;
import lotr.client.gui.LOTRGuiSlider;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import metweaks.guards.NpcReflectionAccess;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.guards.ExtraHiredData;
import metweaks.guards.GuardsAdvSettingsConfig;
import metweaks.guards.HiredInfoAccess;
import metweaks.network.HiredAdvInfoPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

public class HooksGuardMode {
	
	public static void setGuardMode(boolean enabled, LOTREntityNPC npc) {
		if(npc.riddenByEntity != null && npc.riddenByEntity instanceof LOTREntityNPC) {
			npc = (LOTREntityNPC) npc.riddenByEntity;
		}
		
		setGuardModeEnter(npc, enabled);
		    
		// process mount
	    if(npc.ridingEntity != null && npc.ridingEntity instanceof LOTREntityNPC) {
	    	setGuardModeEnter((LOTREntityNPC) npc.ridingEntity, enabled);
	    }
	}
	
	public static void setGuardModeEnter(LOTREntityNPC npc, boolean enabled) {
		LOTRHiredNPCInfo info = npc.hiredNPCInfo;
		
		boolean warrior = info.getTask() == Task.WARRIOR && ASMConfig.guardsWanderRange;
		info.guardMode = enabled;
	    if (enabled) {
	      int x = MathHelper.floor_double(npc.posX);
	      int y = MathHelper.floor_double(npc.posY);
	      int z = MathHelper.floor_double(npc.posZ);
	      
	      int distance = warrior ? HiredInfoAccess.getWanderRange(info) : info.getGuardRange();
	      
	      npc.setHomeArea(x, y, z, distance);
	      
	      // set guardrange or maxRange to aiRange, and checksight to false if wanderrange > guardrrange
	      if(warrior && ASMConfig.guardsAdvancedSettings && GuardsAdvSettingsConfig.autoScaleAiRange)
	    	  autoScaleAiRange(info, npc);
	    
	    } else {
	      npc.detachHome();
	      
	      // set default aiRange, checkSight to true
	      if(warrior && ASMConfig.guardsAdvancedSettings && GuardsAdvSettingsConfig.autoScaleAiRange)
	    	  setDefaultAiRange(npc);
	    }
	}
	
	public static void setGuardRangeEnter(LOTREntityNPC npc, int range) {
		LOTRHiredNPCInfo info = npc.hiredNPCInfo;
		
		HiredInfoAccess.setGuardRange(info, range);
	    if (info.guardMode) {
	      
	      int x = MathHelper.floor_double(npc.posX);
	      int y = MathHelper.floor_double(npc.posY);
	      int z = MathHelper.floor_double(npc.posZ);
	      boolean warrior = info.getTask() == Task.WARRIOR && ASMConfig.guardsWanderRange;
	      int distance = warrior ? HiredInfoAccess.getWanderRange(info) : range;
	      npc.setHomeArea(x, y, z, distance);
	      
	      // set guardrange or maxRange to aiRange, and checksight to false if wanderrange > guardrrange
	      if(warrior && ASMConfig.guardsAdvancedSettings && GuardsAdvSettingsConfig.autoScaleAiRange)
	    	  autoScaleAiRange(info, npc);
	    }
	}
	
	public static void setGuardRange(int range, LOTREntityNPC npc) {
		//if(info.guardMode) {
			// get rider
		
			if(npc.riddenByEntity != null && npc.riddenByEntity instanceof LOTREntityNPC) {
				npc = (LOTREntityNPC) npc.riddenByEntity;
			}
			
			setGuardRangeEnter(npc, range);
			    
			// process mount
		    if(npc.ridingEntity != null && npc.ridingEntity instanceof LOTREntityNPC) {
		    	setGuardRangeEnter((LOTREntityNPC) npc.ridingEntity, range);
		    }
		//}
	}
	
	public static void autoScaleAiRange(LOTRHiredNPCInfo info, LOTREntityNPC npc) {
		
		ExtraHiredData ext = ExtraHiredData.load(npc);
    	
    	
    	int guardRange = info.getGuardRange();
    	int wanderRange = HiredInfoAccess.getWanderRange(info);
    	
    	boolean ignore = guardRange < wanderRange;
    	if(ext.ignoreSight != ignore) {
    		ext.setIgnoreSight(ignore, npc);
	    	
    	}
    	
	    if(GuardsAdvSettingsConfig.allowAiRangeModify) {
	    	byte aiRange = (byte) Math.min(Math.max(guardRange, wanderRange), ExtraHiredData.getMax(ExtraHiredData.getBonus(npc), ext.aiRangeDef));
			if(aiRange != ext.aiRange) {
				ext.setAiRange(aiRange, npc);
				
			}
		}
		// aiRangeMax
		// aiRangeDef (need it for aiRangeMax)
		// aiRange (dont need it?)
		
		// npc.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(Math.smaller(Math.bigger(guardRange, wanderRange), aiRangeMax));
		
		// guardRange
		// wanderRange
	}
	
	public static void setDefaultAiRange(LOTREntityNPC npc) {
		// aiRangeDef
		// ignoreSight only to prevent an uneccessary check (optional)
		ExtraHiredData ext = ExtraHiredData.load(npc);
    	
    	if(ext.ignoreSight) {
    		ext.setIgnoreSight(false, npc);
	    	
	    	
    	}
    	
    	if(ext.aiRange != ext.aiRangeDef) {
    		ext.setAiRange(ext.aiRangeDef, npc);
    		
    	}
	}
	
	
	
	
	
	public static void onHiredUnitCommand(LOTREntityNPC npc, int action, int value) {
		
		switch(action) {
			case 3:
				HooksGuardMode.setWanderRange(npc, value);
				break;
			case 4:
				
					HiredAdvInfoPacket packet = new HiredAdvInfoPacket(npc);
					NetworkHandler.networkWrapper.sendTo(packet, (EntityPlayerMP) npc.hiredNPCInfo.getHiringPlayer());
					if(MeTweaksConfig.debug >= 2)
						System.out.println("Send HiredAdvInfoPacket");
		
				break;
			case 6:
			case 7:
			case 8:
				if(!ASMConfig.guardsAdvancedSettings) return;
				
				ExtraHiredData ext = ExtraHiredData.load(npc);
				if(action == 6) { // aiRange
					if(GuardsAdvSettingsConfig.allowAiRangeModify) {
						ext.setAiRange((byte) MathHelper.clamp_int(value, 0, ExtraHiredData.getMax(ExtraHiredData.getBonus(npc), ext.aiRangeDef)), npc);
					}
					
				}
				else if(action == 7) { // ammoRange
					 ext.setAmmoRangeVerify(value, npc);
					
				}
				else { // 8 is checkSight
					
					ext.setIgnoreSight(!ext.ignoreSight, npc);
				}
				
				
				if(MeTweaksConfig.debug >= 2) {
					byte bonus = ExtraHiredData.getBonus(npc);
					System.out.println("");
					System.out.println("UnlockLVL: "+GuardsAdvSettingsConfig.aiRangeUnlockLvL+" IgnoreSight: "+ext.ignoreSight+" Bonus: "+bonus);
				    System.out.println("AI C: "+ext.aiRange+" Def:"+ext.aiRangeDef+" Max:"+ExtraHiredData.getMax(bonus, ext.aiRangeDef));
				    System.out.println("Ammo C: "+ext.ammoRange+" Def:"+ext.ammoRangeDef+" Max:"+ExtraHiredData.getMax(bonus, ext.ammoRangeDef));
				    System.out.println("IgnoreSight: "+!NpcReflectionAccess.getCheckSight(npc)+" AmmoRange: "+NpcReflectionAccess.getAmmoRange(npc)+" AiRange: "+npc.getEntityAttribute(SharedMonsterAttributes.followRange).getBaseValue());
				}
			    break;
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void initWarriorScreen(LOTRGuiHiredWarrior gui, GuiTextField squadronNameField, LOTRGuiButtonOptions buttonTeleport, List buttonList) {
		int midX = gui.guiLeft + gui.xSize / 2;
		
		LOTRGuiSlider sliderWanderRange = new LOTRGuiSlider(3, midX - 80, gui.guiTop + 98, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.wanderRange"));
		sliderWanderRange.setMinMaxValues(LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
		sliderWanderRange.setSliderValue(HiredInfoAccess.getWanderRange(gui.theNPC.hiredNPCInfo));
		buttonList.add(sliderWanderRange);
		HiredInfoAccess.setSliderWanderRange(gui, sliderWanderRange);
	      
		if(ASMConfig.guardsAdvancedSettings) {
			LOTRGuiButtonOptions buttonAdvanced = new LOTRGuiButtonOptions(4, midX - 80, gui.guiTop + 122, 160, 20, StatCollector.translateToLocal("lotr.gui.warrior.advanced"));
			buttonList.add(buttonAdvanced);
			
		}
		
		
	      squadronNameField.yPosition = gui.guiTop + 160;
	      buttonTeleport.yPosition = gui.guiTop + 184;
	      
	}
	
	public static void updateWarriorScreen(LOTRGuiHiredWarrior gui) {
		LOTRGuiSlider sliderWanderRange = HiredInfoAccess.getSliderWanderRange(gui);
		 sliderWanderRange.visible = gui.theNPC.hiredNPCInfo.isGuardMode();
	      if (sliderWanderRange.dragging) {
	        int i = sliderWanderRange.getSliderValue();
	        HooksGuardMode.setWanderRange(gui.theNPC, i);
	        gui.sendActionPacket(sliderWanderRange.id, i);
	      } 
	}
	
	
	
	public static void writeToNBT(LOTRHiredNPCInfo info, NBTTagCompound data) {
		
		
		if(info.isActive && info.getTask() == Task.WARRIOR) {
			data.setInteger("WanderRange", HiredInfoAccess.getWanderRange(info));
			if(ASMConfig.guardsAdvancedSettings && HiredInfoAccess.getExt(info) != null/*info.ext != null*/) {
				
				HiredInfoAccess.getExt(info).writeNBT(data);
				
			}
		}
	}
	
	public static void readFromNBT(LOTRHiredNPCInfo info, NBTTagCompound data) {
		// prevent this read for unnecessary load
		if(info.isActive && info.getTask() == Task.WARRIOR) {
			if(data.hasKey("WanderRange"))
				//info.wanderRange = data.getInteger("WanderRange");
				HiredInfoAccess.setWanderRange(info, data.getInteger("WanderRange"));
			
			
			if(ASMConfig.guardsAdvancedSettings && (GuardsAdvSettingsConfig.allowAiRangeModify || GuardsAdvSettingsConfig.allowAmmoRangeModify || GuardsAdvSettingsConfig.allowCheckSightModify)) {
				
				
				ExtraHiredData ext = new ExtraHiredData();
				ext.readNBT(HiredInfoAccess.getNPC(info), data);
				HiredInfoAccess.setExt(info, ext);
				
				
			}
		}
	}
	
	public static boolean isGuardingHired(EntityCreature entity, EntityLivingBase target) {
		int x = MathHelper.floor_double(target.posX);
		int y = MathHelper.floor_double(target.posY);
		int z = MathHelper.floor_double(target.posZ);
		if(MeTweaks.lotr && entity instanceof LOTREntityNPC) {
			LOTREntityNPC npc = (LOTREntityNPC) entity;
			if(npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.guardMode && entity.hasHome()) {
				
				int guardRange = npc.hiredNPCInfo.getGuardRange();
				
				boolean inRange = entity.getHomePosition().getDistanceSquared(x, y, z) < (guardRange * guardRange);
				//System.out.println("InRange: "+inRange);
				return inRange;
			}
		}
		
		return entity.isWithinHomeDistance(x, y, z);
	}
	
	public static void setWanderRangeEnter(LOTREntityNPC npc, int wanderRange) {
		HiredInfoAccess.setWanderRange(npc.hiredNPCInfo, wanderRange);
		npc.setHomeArea(MathHelper.floor_double(npc.posX), MathHelper.floor_double(npc.posY), MathHelper.floor_double(npc.posZ), wanderRange);
		// set guardrange or maxRange to aiRange, and checksight to false if wanderrange > guardrrange
		if(ASMConfig.guardsAdvancedSettings && GuardsAdvSettingsConfig.autoScaleAiRange)
	    	 autoScaleAiRange(npc.hiredNPCInfo, npc);
	}
	
	public static void setWanderRange(LOTREntityNPC npc, int wanderRange) {
		wanderRange = MathHelper.clamp_int(wanderRange, LOTRHiredNPCInfo.GUARD_RANGE_MIN, LOTRHiredNPCInfo.GUARD_RANGE_MAX);
		
		if(npc.riddenByEntity != null && npc.riddenByEntity instanceof LOTREntityNPC) {
			npc = (LOTREntityNPC) npc.riddenByEntity;
		}
		
		setWanderRangeEnter(npc, wanderRange);
		    
		// process mount
	    if(npc.ridingEntity != null && npc.ridingEntity instanceof LOTREntityNPC) {
	    	setWanderRangeEnter((LOTREntityNPC) npc.ridingEntity, wanderRange);
	    }
	}
}
