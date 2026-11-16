package metweaks.core;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRBannerProtection;
import lotr.common.LOTRBannerProtection.IFilter;
import lotr.common.LOTRBannerProtection.Permission;
import lotr.common.block.LOTRBlockEntJar;
import lotr.common.block.LOTRBlockMug;
import lotr.common.block.LOTRBlockPlate;
import lotr.common.block.LOTRBlockWeaponRack;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRMod;
import lotr.common.LOTRPlayerData;
import lotr.common.LOTRReflection;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.fac.LOTRAlignmentValues;
import lotr.common.fac.LOTRFaction;
import lotr.common.fellowship.LOTRFellowship;
import lotr.common.item.LOTRItemMug;
import lotr.common.item.LOTRItemMug.Vessel;
import lotr.common.item.LOTRWeaponStats;
import lotr.common.tileentity.LOTRTileEntityWeaponRack;
import lotr.common.world.map.LOTRAbstractWaypoint;
import metweaks.ASMConfig;
import metweaks.MeTweaksConfig;
import metweaks.events.LOTRFastTravelEvent;
import metweaks.features.InteractReachLogger;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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
	
	public static boolean shouldUnsetTarget(LivingSetAttackTargetEvent event) {
		EntityLivingBase target = event.target;
		if(target == event.entityLiving.getAITarget()
			&& target instanceof LOTREntityNPC 
			&& ((LOTREntityNPC) target).isInvasionSpawned()
			&& target.getRNG().nextInt(100) < ASMConfig.revengeCamouflageTargetChance) {
			return false;
		}
		return true;
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
	
	// this works because everything is sync
	public static LOTRFaction temp_hired_faction;
	
	public static void setTempHiredFaction(DamageSource source) {
		if(MeTweaksConfig.hiredsConquerForTheirFaction) {
			Entity entity = source.getEntity();
			if(entity != null && entity instanceof LOTREntityNPC) {
				temp_hired_faction = ((LOTREntityNPC) entity).getFaction();
				
			}
		}
	}
	
	public static LOTRFaction getConquerFaction(LOTRPlayerData data, LOTRAlignmentValues.AlignmentBonus source) {
		LOTRFaction pledged = data.getPledgeFaction();
		// check != null so they cant conquest if player isnt pledged
		if(pledged != null && MeTweaksConfig.hiredsConquerForTheirFaction && source.killByHiredUnit) {
			
			if(temp_hired_faction != null && temp_hired_faction.isPlayableAlignmentFaction()) {
				
				return temp_hired_faction;
			}
		}
		
		return pledged;
	}
	
	public static boolean onFastTravelTransport(EntityPlayer player, LOTRAbstractWaypoint waypoint, Set<EntityLiving> toTransport, List<EntityLiving> nearbyEntities) {
		LOTRFastTravelEvent.Transport event = new LOTRFastTravelEvent.Transport(player, waypoint, toTransport, nearbyEntities);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
	
	public static void onFastTravelSuccess(EntityPlayer player, LOTRAbstractWaypoint waypoint, int x, int y, int z) {
		LOTRFastTravelEvent.Success event = new LOTRFastTravelEvent.Success(player, waypoint, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
    }
	
	
	public static boolean onAttackEntityFrom(Entity toAttack, DamageSource source, float damage) {
		Entity attacker = source.getEntity();
		boolean player = attacker instanceof EntityPlayer;
		if(player && toAttack instanceof lotr.common.entity.item.LOTREntityBanner) {
			// already handled
			return true;
		}
		// if player has explicit permission
		// if npc then never (optional: if npc has explicit permission)
		// if explosion then never
		// (mind self protecting off banners)
		IFilter filter = null;
		if(player) {
			filter = LOTRBannerProtection.forPlayer((EntityPlayer) attacker);
		}
		else if(attacker instanceof EntityLiving) {
			filter = getFilterForLiving((EntityLiving) attacker);
		}
		else if(source.isExplosion()) {
			filter = LOTRBannerProtection.anyBanner();
		}
		
		if(filter != null) {
			if(LOTRBannerProtection.isProtected(toAttack.worldObj, toAttack, filter, false)) {
				
				return false;
			}
				
		}
		
		return true;
		// net.minecraft.entity.item.EntityItemFrame
		// net.minecraft.entity.EntityHanging
		// lotr.common.entity.item.LOTREntityRugBase
		// lotr.common.entity.item.LOTREntityBanner
		// net.minecraft.entity.item.EntityMinecart
		
	}
	
	
	public static boolean sendSlotsOnPermDenied = true;
	
	public static boolean handleRIGHT_CLICK_ENTITY(World world, Entity entity, IFilter filter, EntityInteractEvent event) {
		ItemStack itemstack = event.entityPlayer.getHeldItem();
		sendSlotsOnPermDenied = false;
		boolean isProtected = LOTRBannerProtection.isProtected(world, entity, filter, ASMConfig.bannerProtectionVerbose || itemstack == null || itemstack.getItemUseAction() == EnumAction.none);
		sendSlotsOnPermDenied = true;
		if(isProtected) {
			event.setCanceled(true);
		}
		return isProtected;
	}
	
	public static boolean handleRIGHT_CLICK_BLOCK(World world, int i, int j, int k, IFilter filter, PlayerInteractEvent event, Block block, ItemStack itemstack, Permission perm) {
		sendSlotsOnPermDenied = false;
		boolean isProtected = LOTRBannerProtection.isProtected(world, i, j, k, filter, ASMConfig.bannerProtectionVerbose || itemstack == null || itemstack.getItemUseAction() == EnumAction.none);
		sendSlotsOnPermDenied = true;

		if(!isProtected && perm == Permission.FOOD) {
			// quick fix to avoid placing blocks when drinking
			if(LOTRBannerProtection.isProtected(world, i, j, k, LOTRBannerProtection.forPlayer(event.entityPlayer), false))
				event.useItem = Result.DENY;
		}

		if(isProtected) {
			event.setCanceled(true);

			boolean sendSlots = false;
			boolean sendBlock = false;

			if(block instanceof net.minecraft.block.BlockDoor) {
				world.markBlockForUpdate(i, j - 1, k);
				world.markBlockForUpdate(i, j + 1, k);
				sendBlock = true;
			}
			else if(block instanceof LOTRBlockPlate) {
				// we are about to empty a plate
				if(LOTRBlockPlate.getFoodItem(world, i, j, k) != null) {
					sendBlock = true;
				}
				sendSlots = true;
			}
			else if(block instanceof LOTRBlockMug) {
				// we are about to empty a mug
				if(LOTRBlockMug.getMugItem(world, i, j, k) != null) {
					sendBlock = true;
				}

				sendSlots = true;
			}
			else if(block instanceof LOTRBlockEntJar) {
				sendBlock = true;
				sendSlots = true;
			}

			// update items
			EntityPlayer entityplayer = event.entityPlayer;
			if(entityplayer instanceof EntityPlayerMP) {
				// send slots if we attempt to fill a weapon rack
				if(block instanceof LOTRBlockWeaponRack) {
					TileEntity tileentity = world.getTileEntity(i, j, k);
					if(tileentity instanceof LOTRTileEntityWeaponRack) {
						LOTRTileEntityWeaponRack rack = (LOTRTileEntityWeaponRack) tileentity;
						if(rack.getWeaponItem() == null) {
							sendSlots = true;
						}
					}
				}
				if(itemstack != null && !entityplayer.isUsingItem() && itemstack.getItem() instanceof LOTRItemMug && canPlaceMug(itemstack, entityplayer, world, i, j, k, event.face)) {
					sendSlots = true;
				}
				
				if(sendSlots) {
					((EntityPlayerMP) entityplayer).sendContainerToPlayer(entityplayer.inventoryContainer);
				}

				if(sendBlock) {
					world.markBlockForUpdate(i, j, k);
				}
			}
		}
		return isProtected;
	}

	public static boolean canPlaceMug(ItemStack itemstack, EntityPlayer entityplayer, World world, int i, int j, int k, int side) {
		Vessel vessel = LOTRItemMug.getVessel(itemstack);
		if(vessel == null || !vessel.canPlace)
			return false; 
		Block mugBlock = vessel.getBlock();
		i += Facing.offsetsXForSide[side];
		j += Facing.offsetsYForSide[side];
		k += Facing.offsetsZForSide[side];
		Block block = world.getBlock(i, j, k);
		if(block != null && !block.isReplaceable(world, i, j, k))
			return false; 
		if(block.getMaterial() == Material.water)
			return false; 
		if(entityplayer.canPlayerEdit(i, j, k, side, itemstack)) {
			if(!mugBlock.canPlaceBlockAt(world, i, j, k))
				return false;
			return true;
		} 
		return false;
	}
	
	public static IFilter getFilterForLiving(EntityLiving entity) {
		if(ASMConfig.permNpcAllowDestroyNonLiving) {
			if(entity instanceof LOTREntityNPC) {
				LOTREntityNPC npc = ((LOTREntityNPC) entity);
				if(npc.hiredNPCInfo.isActive && npc.hiredNPCInfo.getHiringPlayerUUID() != null) {
					EntityPlayer hirer = npc.hiredNPCInfo.getHiringPlayer();
					if(hirer != null) {
						return LOTRBannerProtection.forPlayer(hirer);
					}
				}
			}
	
			return LOTRBannerProtection.forNPC(entity);
		}
		return LOTRBannerProtection.anyBanner();
	}

	public static boolean isFarmLandUnprotected(Entity entity, int x, int y, int z) {
		IFilter filter = null;
	
		if(entity instanceof EntityPlayer) {
			filter = LOTRBannerProtection.forPlayer((EntityPlayer) entity);
		}
		else if(entity instanceof EntityLiving) {
			filter = getFilterForLiving((EntityLiving) entity);
		}
	
		if(filter != null) {
			if(LOTRBannerProtection.isProtected(entity.worldObj, x, y, z, filter, false)) {
				return false;
			}
	
		}
		return true;
	}
	
	
	public static EntityLivingBase getRidingPlayerOptional(EntityLivingBase target) {
		if(target.riddenByEntity instanceof EntityPlayer) {
			return (EntityLivingBase) target.riddenByEntity;
		}
		return target;
	}

	public static boolean preventIfNotBadRelation(UUID hiringPlayerUUID, UUID targetHiringPlayerUUID, EntityCreature attacker, EntityLivingBase target) {
	
		if(hiringPlayerUUID.equals(targetHiringPlayerUUID)) {
			return true;
		}
	
	
		if(attacker.getAttackTarget() != target) {
			if(!attacker.worldObj.isRemote) {
				boolean swap = attacker.worldObj.func_152378_a(hiringPlayerUUID) == null;
	
				LOTRPlayerData playerData = LOTRLevelData.getData(swap ? targetHiringPlayerUUID : hiringPlayerUUID);
	
				if(!playerData.isSiegeActive()) {
					List<LOTRFellowship> fellowships = playerData.getFellowships();
	
					for(LOTRFellowship fs : fellowships) {
						if(fs.getPreventHiredFriendlyFire() && fs.containsPlayer(swap ? hiringPlayerUUID : targetHiringPlayerUUID)) {
							return true; 
						}
					} 
				}
			}
			else{
				return HooksClient.preventIFNBClient(hiringPlayerUUID, targetHiringPlayerUUID);
			}
		}
	
		return false;
	}
	
	public static boolean isHiredNPC(LOTRHiredNPCInfo info) {
		return /*npc.worldObj.isRemote ? */info.getHiringPlayerUUID() != null/* : info.isActive*/;
	}

	public static boolean isHired(LOTREntityNPC npc) {
		return npc.hiredNPCInfo.getHiringPlayerUUID() != null;
	}

	public static float getEntSoundVolume(LOTREntityNPC npc) {
		return npc.hiredNPCInfo.getHiringPlayerUUID() != null ? 1.0F : 1.5F;
	}

	public static float getEntSoundVolumeStep(LOTREntityNPC npc) {
		return npc.hiredNPCInfo.getHiringPlayerUUID() != null ? 0.5F : 0.75F;
	}

	@SuppressWarnings("unchecked")
	public static boolean attackEntityAsEnt(LOTREntityNPC ent, Entity target) {

		float knockbackModifier = 1.5F;
		target.addVelocity(-MathHelper.sin(ent.rotationYaw * 3.1415927F / 180.0F) * knockbackModifier * 0.5F, 0.15D, MathHelper.cos(ent.rotationYaw * 3.1415927F / 180.0F) * knockbackModifier * 0.5F);

		float attackDamage = (float) ent.getEntityAttribute(LOTREntityNPC.npcAttackDamage).getAttributeValue() * 0.9F;
		//float damage = 1.0F + attackDamage * 0.75F;
		List<EntityLivingBase> entities = ent.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, target.boundingBox.expand(2.0D, 0.5D, 2.0D));
		for(EntityLivingBase entity : entities) {
			if(entity != ent && entity != target && ent.getDistanceSqToEntity(entity) < 16.0D && LOTRMod.canNPCAttackEntity(ent, entity, false) ) {

				float strength = Math.min(4.0F, 5.0F - ent.getDistanceToEntity(entity)) * 0.25F;
				entity.addVelocity(-MathHelper.sin(ent.rotationYaw * 3.1415927F / 180.0F) * strength * 0.25F, 0.1D, MathHelper.cos(ent.rotationYaw * 3.1415927F / 180.0F) * strength * 0.25F);
				entity.attackEntityFrom(DamageSource.causeMobDamage(ent), strength * attackDamage);
			}
		}

		return true;
	}

	public static double interactReachBufferD = ASMConfig.interactReachBuffer * 0.01;
	public static double interactReachBufferNoSightD = ASMConfig.interactReachBufferNoSight * 0.01;

	public static boolean inInteractionRange(EntityPlayerMP player, Entity target, double reach) {

		float c = target.getCollisionBorderSize();
		reach -= LOTRWeaponStats.getMeleeExtraLookWidth() + c;


		double distanceSq = closestDistanceToAABB(player.posX, player.posY + 1.62D, player.posZ, target.boundingBox.expand(c, c, c));

		double buffer = interactReachBufferNoSightD >= interactReachBufferD || player.canEntityBeSeen(target) ? interactReachBufferD : interactReachBufferNoSightD;

		if(ASMConfig.interactReachFeedback) {
			InteractReachLogger.verboseInteractReach(player, target, reach, distanceSq, buffer);
		}


		reach += buffer;

		return distanceSq < reach * reach;
	}

	private static double closestDistanceToAABB(double x, double y, double z, AxisAlignedBB box) {
		double dx = 0.0;
		double dy = 0.0;
		double dz = 0.0;

		if(x < box.minX) {
			dx = box.minX - x;
		} 
		else if(x > box.maxX) {
			dx = x - box.maxX;
		}

		if(y < box.minY) {
			dy = box.minY - y;
		}
		else if(y > box.maxY) {
			dy = y - box.maxY;
		}

		if(z < box.minZ) {
			dz = box.minZ - z;
		}
		else if(z > box.maxZ) {
			dz = z - box.maxZ;
		}

		return dx * dx + dy * dy + dz * dz;
	}
}
