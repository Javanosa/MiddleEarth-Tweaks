package metweaks.core;

import java.lang.reflect.Field;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRReflection;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.fac.LOTRFaction;
import metweaks.MeTweaksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.world.World;

public class HooksLOTR {
	public static Field field_horseChest;
	
	public static AnimalChest getHorseInv(EntityHorse horse) {
		try{
			if(field_horseChest == null)
				field_horseChest = ReflectionHelper.findField(EntityHorse.class, new String[] {"horseChest", "field_110296_bG"});
			return (AnimalChest) field_horseChest.get(horse);
		}
		catch(Exception e) {
			LOTRReflection.logFailure(e);
		}
		return null;
	}
	
	public static boolean isTreeAngry(EntityPlayer entityplayer) {
    	if(LOTRLevelData.getData(entityplayer).getAlignment(LOTRFaction.FANGORN) < MeTweaksConfig.fangornTreePenaltyThreshold) {
    		return true;
    	}
		return false;
    }
	
	public static void postTeleportToHiringPlayer(LOTREntityNPC npc) {
		/*
		
		tryTeleportToHiringPlayer(Z)Z
		 
		 
		before every ICONST_1, IRETURN
		
		ALOAD this
		GETFIELD lotr/common/entity/npc/LOTRHiredNPCInfo theEntity Llotr/common/entity/npc/LOTREntityNPC;
		INVOKESTATIC metweaks/PatchesLOTR postTeleportToHiringPlayer (Llotr/common/entity/npc/LOTREntityNPC;)V false
		
		
		
		
		PatchesLOTR.postTeleportToHiringPlayer(this.theEntity);
		return true;
		
		 */
		Entity mount = npc.ridingEntity;
		World world = npc.worldObj;
		if(mount instanceof EntityLiving) {
			// we only teleported mount
			npc.setLocationAndAngles(mount.posX, mount.posY, mount.posZ, npc.rotationYaw, npc.rotationPitch);
			// mark entity tracker for update
			mount.isAirBorne = true;
			// sync chunks
    		world.updateEntityWithOptionalForce(mount, false);
    		
		}
		// mark entity tracker for update
		npc.isAirBorne = true;
		// sync chunks
		world.updateEntityWithOptionalForce(npc, false);
		
		
	}
	
	
}
