package metweaks.core;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.projectile.LOTREntityCrossbowBolt;
import lotr.common.entity.projectile.LOTREntityProjectileBase;
import lotr.common.entity.projectile.LOTREntitySpear;
import lotr.common.entity.projectile.LOTREntityThrowingAxe;
import lotr.common.item.LOTRItemSpear;
import lotr.common.item.LOTRWeaponStats;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.features.ThrowableSpearState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeHooks;

public class HooksProjectile {
	
	
	private static Field field_lastDamage = ReflectionHelper.findField(EntityLivingBase.class, "lastDamage", "field_110153_bc");
	private static Field field_ticksInAir;// = ReflectionHelper.findField(LOTREntityProjectileBase.class, "ticksInAir");
	
	public static boolean sendLocationEarly(Entity entity) {
		return entity instanceof EntityArrow || entity instanceof EntityFishHook || (MeTweaks.lotr && entity instanceof LOTREntityProjectileBase)/* || entity instanceof EntityFallingBlock*/;
	}
	
	public static boolean piercingProjectile(LOTREntityProjectileBase projectile) {
		return projectile.worldObj.isRemote ? projectile instanceof LOTREntitySpear || projectile instanceof LOTREntityCrossbowBolt || projectile instanceof LOTREntityThrowingAxe : !projectile.isDead;
	}
	
	public static void addToBlackList(LOTREntityProjectileBase projectile, Entity target) {
		//projectile.isAirBorne = true; // force velocity sync
		
		if(ASMConfig.alwaysAbsorbPiercingProjectiles) {
			projectile.motionX *= -0.1D;
			projectile.motionY *= -0.1D;
			projectile.motionZ *= -0.1D;
			projectile.rotationYaw += 180F;
			projectile.prevRotationYaw += 180F;
			try {
				if(field_ticksInAir == null)
					field_ticksInAir = ReflectionHelper.findField(LOTREntityProjectileBase.class, "ticksInAir");
				field_ticksInAir.setInt(projectile, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;// false;
		}
		
		if(projectile.worldObj.isRemote) {
			hasMultiTarget_client = true;
			TIntList list = projectileTargetBlacklist_client.get(projectile.getEntityId());
			if(list == null) {
				list = new TIntArrayList();
				list.add(target.getEntityId());
				projectileTargetBlacklist_client.put(projectile.getEntityId(), list);
			}
			
			list.add(target.getEntityId());
		}
		else {
			hasMultiTarget_server = true;
			TIntList list = projectileTargetBlacklist_server.get(projectile.getEntityId());
			if(list == null) {
				list = new TIntArrayList();
				list.add(target.getEntityId());
				projectileTargetBlacklist_server.put(projectile.getEntityId(), list);
			}
			
			list.add(target.getEntityId());
		}
		return;// true;
	}
	
	private static TIntObjectMap<TIntList> projectileTargetBlacklist_server = new TIntObjectHashMap<>();
	private static TIntObjectMap<TIntList> projectileTargetBlacklist_client = new TIntObjectHashMap<>();
	private static boolean hasMultiTarget_server = false;
	private static boolean hasMultiTarget_client = false;
	
	public static void resetBlacklist(LOTREntityProjectileBase projectile) {
		if(hasMultiTarget_client && projectile.worldObj.isRemote) {
			hasMultiTarget_client = false;
			if(projectileTargetBlacklist_client.containsKey(projectile.getEntityId())) {
				projectileTargetBlacklist_client.remove(projectile.getEntityId());
			}
		}
		else if(hasMultiTarget_server) {
			hasMultiTarget_server = false;
			if(projectileTargetBlacklist_server.containsKey(projectile.getEntityId())) {
				projectileTargetBlacklist_server.remove(projectile.getEntityId());
			}
		}
			
		
	}
	
	public static boolean allowProjectileTarget(Entity target, Entity projectile) {
		if(!target.canBeCollidedWith()) {
			return false;
		}
		
		Entity shooter = null;
		if(projectile instanceof EntityArrow) {
			shooter = ((EntityArrow) projectile).shootingEntity;
		}
		else if(MeTweaks.lotr && projectile instanceof LOTREntityProjectileBase) {
			shooter = ((LOTREntityProjectileBase) projectile).shootingEntity;
		}
		
		if(target == shooter) return true; // fix shooter invulnerable by its own projectile and still keep somewhat performance

		if(/*target == shooter || */(shooter != null && target == shooter.ridingEntity && ASMConfig.projectilePatchesReqClient)) {
			return false;
		}
		
		if(ASMConfig.allyProjectilesPassAllies && MeTweaks.lotr && (target instanceof LOTREntityNPC || (target.riddenByEntity instanceof LOTREntityNPC && target instanceof EntityLivingBase)) && target.width < 1F && target.height < 2.5F) {
			
			/*Entity shooter = null;
			if(projectile instanceof EntityArrow) {
				shooter = ((EntityArrow) projectile).shootingEntity;
			}
			else if(projectile instanceof LOTREntityProjectileBase) {
				shooter = ((LOTREntityProjectileBase) projectile).shootingEntity;
			}*/
			
			if(/*target != shooter && */shooter instanceof LOTREntityNPC) {
				
				if(!LOTRMod.canNPCAttackEntity((EntityCreature) shooter, (EntityLivingBase) target, false)) {
					
					return false;
				}
				
			}
		}
		
		if(!ASMConfig.alwaysAbsorbPiercingProjectiles && MeTweaks.lotr) {
			if(hasMultiTarget_client && projectile.worldObj.isRemote) {
				if(projectileTargetBlacklist_client.containsKey(projectile.getEntityId())) {
					TIntList list = projectileTargetBlacklist_client.get(projectile.getEntityId());
					if(list.contains(target.getEntityId())) {
						return false;
					}
				}
			}
			else if(hasMultiTarget_server) {
				if(projectileTargetBlacklist_server.containsKey(projectile.getEntityId())) {
					TIntList list = projectileTargetBlacklist_server.get(projectile.getEntityId());
					if(list.contains(target.getEntityId())) {
						return false;
					}
				}
			}
		}
		
		return true;
		
	}
	
	

	public static boolean attackEntityFromReplace(Entity entity, DamageSource source, float damage) {
		if(!entity.worldObj.isRemote || !(entity instanceof EntityLivingBase)) {
			return entity.attackEntityFrom(source, damage);
		}
		// for client just tell us if we can attack or not (used for spear / throwingaxe absorb movement)
		EntityLivingBase target = (EntityLivingBase) entity;
			
		if(ForgeHooks.onLivingAttack(target, source, damage)) return false;
        
        if(target.isEntityInvulnerable()) {
            return false;
        }
        
        if(target.getHealth() <= 0.0F) {
               return false;
        }
        
         if(target.hurtResistantTime > target.maxHurtResistantTime / 2.0F) {
        	
			try {
				float lastDamage = field_lastDamage.getFloat(target);
				 if(damage <= lastDamage) {
		              return false;
		         }
				 field_lastDamage.setFloat(target, damage);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        else {
            target.hurtResistantTime = target.maxHurtResistantTime;
            //target.hurtTime = target.maxHurtTime = 10;
        }
         
         try {
        	 field_lastDamage.setFloat(target, damage);
         } catch (Exception e) {
        	 e.printStackTrace();
         }
        return true;
    }
	
	public static void handleEntityRemovalQueue(EntityPlayerMP player, List<Integer> entityRemovalQueue) {
		while(!entityRemovalQueue.isEmpty()) {
	        int max = Math.min(entityRemovalQueue.size(), 127); // because packet uses byte to transfer entry count
	        int[] entityIDs = new int[max];
	        
	        Iterator<Integer> iterator = entityRemovalQueue.iterator();
	        int i = 0;
	        while(iterator.hasNext() && i < max) {
	            entityIDs[i++] = iterator.next();
	            iterator.remove();
	        }

	        player.playerNetServerHandler.sendPacket(new S13PacketDestroyEntities(entityIDs));
	        
	    }
	}
	
	@SuppressWarnings("rawtypes")
	public static void forceRemoveEntity(Entity entity, IntHashMap entityHashSet, Set entitySpawnQueue) {
		entity.worldObj.removeEntity(entity);
		entitySpawnQueue.remove(entity);
    	entityHashSet.removeObject(entity.getEntityId());
    	
	}
	
	public static void setTargetFireIfBurning(EntityArrow arrow, MovingObjectPosition mop) {
		if(arrow.isBurning() && !(mop.entityHit instanceof EntityEnderman)) {
			mop.entityHit.setFire(5);
		}
	}
	
	public static boolean isLockedMode(EntityPlayer player) {
		return ThrowableSpearState.modes.contains(player.getUniqueID());
	}
	
	public static boolean cancelGateActivation(ItemStack stack, EntityPlayer player) {
		if(stack != null && stack.getItem() instanceof LOTRItemSpear && isLockedMode(player)) {
			return false; 
		}
		return LOTRWeaponStats.isRangedWeapon(stack);
	}
}
