package metweaks.features;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cpw.mods.fml.common.FMLLog;
import lotr.common.entity.npc.LOTRNPCMount;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.world.World;

public class ModifierHandler {
	static final UUID attrmoduuid = UUID.randomUUID();

	public static final Map<Class<? extends EntityLivingBase>, MountData> mountdata = new HashMap<>();
	
	public static class MountData{
		public final double min;
		public final double max;
		
		public MountData[] subtypes;
		public String name;

		public MountData() {
			this(0, 1);
		}
		
		public MountData(double min, double max) {
			this.min = min;
			this.max = max;
		}
		
		public MountData getSubtype(int type) {
			if(subtypes != null && type < subtypes.length && type >= 0)
				return subtypes[type];
			return null;
		}
	}
	
	public static void modifyAttr(EntityLivingBase living, double min, double max) {
		IAttributeInstance attr = living.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
		// not yet modified
		if(attr.getModifier(attrmoduuid) == null) {
			
			double value = attr.getAttributeValue();
			double add = value > max ? max 		: 		value < min ? min : 0;
			
			
			if(add > 0) {
				AttributeModifier attrmod = new AttributeModifier(attrmoduuid, "generic.movementSpeed", -value+add, 0); // operation add
				attrmod.setSaved(false);
				attr.applyModifier(attrmod);
				
				if(MeTweaksConfig.debug == 3) {
					FMLLog.getLogger().info(living.getClass().getName()+"/"+(living instanceof EntityHorse ? ((EntityHorse) living).getHorseType() : "")+" from "+value+" to "+attr.getAttributeValue()+" Dim"+living.dimension);
				}
			}
		}
	}
	
	public static void checkspeedmods(Entity entity, World world, boolean forceUntamed) {
		if(!world.isRemote && entity instanceof EntityLivingBase) {
			if(mountdata.containsKey(entity.getClass())) {
				if(MeTweaks.lotr && MeTweaksConfig.ignoreNPCmounts) {
					if(entity instanceof LOTRNPCMount && ((LOTRNPCMount) entity).getBelongsToNPC()) {
						return;
					}
				}
				
				if(MeTweaksConfig.useGlobalSpeed) {
					modifyAttr((EntityLivingBase) entity, MeTweaksConfig.globalSpeedMin, MeTweaksConfig.globalSpeedMax);
				}
				else {
					MountData data = mountdata.get(entity.getClass());
					
					if(data.subtypes != null && entity instanceof EntityHorse) {
						
						MountData subdata = data.getSubtype(((EntityHorse) entity).getHorseType() -1);
						if(subdata != null)
							data = subdata;
						
					}
					
					modifyAttr((EntityLivingBase) entity, data.min, data.max);
				}
			}
		}
	}
	
	
	
	
	
	
	
	
}
