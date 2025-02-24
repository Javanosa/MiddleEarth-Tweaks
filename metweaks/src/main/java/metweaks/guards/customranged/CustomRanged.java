package metweaks.guards.customranged;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lotr.client.gui.LOTRGuiHiredWarriorInventory;
import lotr.common.LOTRMod;
import lotr.common.enchant.LOTREnchantment;
import lotr.common.enchant.LOTREnchantmentHelper;
import lotr.common.entity.ai.LOTREntityAIRangedAttack;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.item.LOTREntityArrowPoisoned;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTREntityTauredainBlowgunner;
import lotr.common.entity.npc.LOTRInventoryHiredReplacedItems;
import lotr.common.entity.npc.LOTRInventoryNPCItems;
import lotr.common.entity.projectile.LOTREntityCrossbowBolt;
import lotr.common.entity.projectile.LOTREntityDart;
import lotr.common.entity.projectile.LOTREntityFirePot;
import lotr.common.entity.projectile.LOTREntityPebble;
import lotr.common.entity.projectile.LOTREntityPlate;
import lotr.common.entity.projectile.LOTREntitySpear;
import lotr.common.entity.projectile.LOTREntityThrowingAxe;
import lotr.common.entity.projectile.LOTREntityThrownTermite;
import lotr.common.inventory.LOTRContainerHiredWarriorInventory;
import lotr.common.inventory.LOTRSlotHiredReplaceItem;
import lotr.common.item.LOTRItemBlowgun;
import lotr.common.item.LOTRItemBow;
import lotr.common.item.LOTRItemCrossbow;
import lotr.common.item.LOTRItemDart;
import lotr.common.item.LOTRItemPlate;
import lotr.common.item.LOTRItemSpear;
import lotr.common.item.LOTRItemThrowingAxe;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.core.HooksAiRanged;
import metweaks.guards.ExtraHiredData;
import metweaks.guards.GuardsAdvSettingsConfig;
import metweaks.guards.NpcReflectionAccess;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

public class CustomRanged {
	public static IIcon iconRangedWeapon;
	public static boolean skipHeldItemUpdate;

	@SubscribeEvent
	public void preTextureStitch(TextureStitchEvent.Pre event) {
		TextureMap map = event.map;
		// items
		if(map.getTextureType() == 1) {
			iconRangedWeapon = map.registerIcon("metweaks:slotRanged");
		}
	}
	
	public CustomRanged() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	private static Field replacedMeleeWeapons = ReflectionHelper.findField(LOTRInventoryHiredReplacedItems.class, "replacedMeleeWeapons");
	
	public static void setReplacedMeleeWeapons(LOTRInventoryHiredReplacedItems inv, boolean value) {
		try {
			replacedMeleeWeapons.setBoolean(inv, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean allowCategory(Item item, Category category) {
		int i = ASMConfig.guardsLockCategoryRanged;
		if(i == 0) return true;
		Category current = Category.getCategory(item);
		
		return i == 1 ? current == category : current.ordinal() <= category.ordinal();
	}
	
	public static void updateHeldItem(LOTREntityNPC theNPC) {
		if(!skipHeldItemUpdate && !theNPC.npcItemsInv.getIsEating())
		      theNPC.refreshCurrentAttackMode(); 
		
	}
	
	public static void equipReplacement(LOTRInventoryHiredReplacedItems inv, int i, ItemStack itemstack, boolean replacedMeleeWeapons, LOTREntityNPC theNPC, ItemStack replaced) {
		  boolean origin = ItemStack.areItemStacksEqual(replaced, itemstack);
		  LOTRInventoryNPCItems npcItems = theNPC.npcItemsInv;
		  boolean storedIdleHolder = npcItems.getReplacedIdleItem() != null || npcItems.getReplacedIdleItemMounted() != null;
		  
		  
		  if(i == LOTRInventoryHiredReplacedItems.MELEE) {
			  
			  boolean idleMelee = ItemStack.areItemStacksEqual(npcItems.getMeleeWeapon(), npcItems.getIdleItem());
			  npcItems.setMeleeWeapon(itemstack);
			  
			  if(!replacedMeleeWeapons) {
				  npcItems.setReplacedMeleeWeaponMounted(npcItems.getMeleeWeaponMounted());
				  setReplacedMeleeWeapons(inv, true);
			  } 
			  
			  if(!storedIdleHolder && idleMelee) {
				  npcItems.setReplacedIdleItem(npcItems.getIdleItem());
				  npcItems.setReplacedIdleItemMounted(npcItems.getIdleItemMounted());
			  }
			  
			  npcItems.setMeleeWeaponMounted(itemstack);
			  
			  if(idleMelee) {
				  npcItems.setIdleItem(itemstack);
				  npcItems.setIdleItemMounted(itemstack);
			  } 
			  
			  if(origin) {
				  if(storedIdleHolder && idleMelee) {
					  npcItems.setIdleItem(npcItems.getReplacedIdleItem());
					  npcItems.setIdleItemMounted(npcItems.getReplacedIdleItemMounted());
					  npcItems.setReplacedIdleItem(null);
					  npcItems.setReplacedIdleItemMounted(null);
				  }
				  
				  if(replacedMeleeWeapons) {
					  npcItems.setMeleeWeaponMounted(npcItems.getReplacedMeleeWeaponMounted());
					  npcItems.setReplacedMeleeWeaponMounted(null);
				      setReplacedMeleeWeapons(inv, false);
				  }
			  }
			  
			  updateHeldItem(theNPC);
		  } 
		  else if (i == LOTRInventoryHiredReplacedItems.RANGED) {
			  boolean idleRanged = ItemStack.areItemStacksEqual(npcItems.getRangedWeapon(), npcItems.getIdleItem());
			  
			  npcItems.setRangedWeapon(itemstack);
			  CustomRanged.setupRanged(theNPC, itemstack, ASMConfig.aiRangedImprovements);
			  
			  
			  
			  if(!storedIdleHolder && idleRanged) {
				  npcItems.setReplacedIdleItem(npcItems.getIdleItem());
				  npcItems.setReplacedIdleItemMounted(npcItems.getIdleItemMounted());
				 
			  }
			
			  if (idleRanged) {
				  npcItems.setIdleItem(itemstack);
				  npcItems.setIdleItemMounted(itemstack);
			  }
			  
			  if(origin) {
				  if(storedIdleHolder && idleRanged) {
					  npcItems.setIdleItem(npcItems.getReplacedIdleItem());
					  npcItems.setIdleItemMounted(npcItems.getReplacedIdleItemMounted());
					  npcItems.setReplacedIdleItem(null);
					  npcItems.setReplacedIdleItemMounted(null);
				  }
			  }
			  
			  updateHeldItem(theNPC);
		  } 
		  else if (i == LOTRInventoryHiredReplacedItems.BOMB) {
			  npcItems.setBomb(itemstack);
			  updateHeldItem(theNPC);
		  } 
		  else {
			  theNPC.setCurrentItemOrArmor(4 - i, itemstack);
		  } 
	  }
	
	
	
	@SuppressWarnings("unchecked")
	public static void setupContainer(LOTRContainerHiredWarriorInventory container, LOTREntityNPC npc) {
		
		if(!npc.worldObj.isRemote || (MeTweaks.remotePresent/* && (SyncedConfig.cached == null || !SyncedConfig.cached.received || SyncedConfig.cached.ASM_guardsEquipRanged)*/)) {
			Slot slot = new LOTRSlotHiredReplaceItem(new LOTRSlotRangedWeapon(npc, container.proxyInv, LOTRInventoryHiredReplacedItems.RANGED, 50, 28), npc);
			slot.slotNumber = container.inventorySlots.size();
			container.inventorySlots.add(slot);
			container.inventoryItemStacks.add(null);
		}
	}
	
	/*public static Slot makeSlotRanged(LOTRContainerHiredWarriorInventory container, LOTREntityNPC npc) {
		//System.out.println("anyone?");
		//ReflectionHelper.setPrivateValue(LOTRContainerHiredWarriorInventory.class, container, (int) ReflectionHelper.getPrivateValue(LOTRContainerHiredWarriorInventory.class, container, "npcActiveSlotCount")+ 1, "npcActiveSlotCount");
		
		return new LOTRSlotHiredReplaceItem(new LOTRSlotRangedWeapon(npc, container.proxyInv, LOTRInventoryHiredReplacedItems.RANGED, 50, 28), npc);
	}*/
	
	public static void drawGuiContainerBackgroundLayer(LOTRContainerHiredWarriorInventory containerInv, LOTRGuiHiredWarriorInventory gui, int guiLeft, int guiTop) {
		//Slot slotMelee = containerInv.getSlotFromInventory(containerInv.proxyInv, 4);
	    Slot slotRanged = containerInv.getSlotFromInventory(containerInv.proxyInv, LOTRInventoryHiredReplacedItems.RANGED);
	    if(slotRanged != null)
	    	gui.drawTexturedModalRect(guiLeft + slotRanged.xDisplayPosition - 1, guiTop + slotRanged.yDisplayPosition - 1, 49, 47, 18, 18);
	}
	
	public static float globalPenaltyRanged = Math.max(1F, ASMConfig.guardsAttackDelayFactorRanged * 0.01F);
	
	public static enum Category {
		SLING(20, 40, 12F, false, 		4F), // LOTRMod.sling
		PLATE(15, 25, 12F, true, 		4F),
		BOW(30, 50, 16F, false, 		2F), // instanceof ItemBow, after checking rest for negative
		BLOWGUN(10, 30, 16F, false, 	2F, 1.1F), // LOTRMod.blowgun
		AXE(40, 50, 12F, true, 			1F), // .getClass() == LOTRItemThrowingAxe.class
		CROSSBOW(30, 50, 16F, false, 	2F, 1.1F), // instanceof LOTRItemCrossbow
		SPEAR(50, 80, 16F, true, 		0.25F),
		FIREPOT(20, 30, 16F, true, 		8F, 2.7F), // LOTRMod.rhunFirePot
		TERMITE(60, 100, 16F, true, 	8F); // LOTRMod.termite
		
		
		// stack.getItem() == LOTRMod.termite || stack.getItem() == LOTRMod.rhunFirePot || stack.getItem() == LOTRMod.sling;
		
		
		boolean damageable;
		float durability;
		
		int attackTimeMin;
		int attackTimeMax;
		float attackRange;
		float penalty;
		
		Category(int time, float range, boolean damageable, float durability) {
			this(time, time, range, damageable, durability);
		}
		
		Category(int min, int max, float range, boolean damageable, float durability) {
			this(min, max, range, damageable, durability, 1F);
		}
		
		Category(int min, int max, float range, boolean dmgable, float durabilityFactor, float penaltyFactor) {
			attackTimeMin = min;
			attackTimeMax = max;
			attackRange = range;
			damageable = dmgable;
			float dmg = durabilityFactor * ASMConfig.guardsEquipDurabilityFactor * 0.01F;
			dmg = 1F / dmg;
			if(dmg > 1) {
				// round up to be used directly in damage function
				dmg = Math.round(dmg);
			}
			durability = dmg;
			penalty = penaltyFactor * globalPenaltyRanged;
			
		}
		
		public static Category getCategory(Item item) {
			if(item instanceof LOTRItemCrossbow) {
				return CROSSBOW;
			}
			else if(item == LOTRMod.sling) {
				return SLING;
			}
			else if(item == LOTRMod.tauredainBlowgun) {
				return BLOWGUN;
			}
			else if(item == LOTRMod.termite) {
				return TERMITE;
			}
			else if(item == LOTRMod.rhunFirePot) {
				return FIREPOT;
			}
			else if(item.getClass() == LOTRItemThrowingAxe.class) {
				return AXE;
			}
			else if(item.getClass() == LOTRItemPlate.class && item != LOTRMod.woodPlate) {
				return PLATE;
			}
			else if(item.getClass() == LOTRItemSpear.class && item != LOTRMod.spearStone) {
				return SPEAR;
			}
			else return BOW;
		}
		
		// dont see anything else as bow, used for launching
		public static Category getCategoryStrict(Item item) {
			Category category = getCategory(item);
			return category != BOW || item instanceof ItemBow ? category : null;
		}
		
	}
	
	private static final Field attackTimeMin = ReflectionHelper.findField(LOTREntityAIRangedAttack.class, "attackTimeMin");
	private static final Field attackTimeMax = ReflectionHelper.findField(LOTREntityAIRangedAttack.class, "attackTimeMax");
	private static final Method getPoisonedArrowChance = ReflectionHelper.findMethod(LOTREntityNPC.class, null, new String[] {"getPoisonedArrowChance"});
	
	public static float getPoisonedArrowChance(LOTREntityNPC npc) {
		try {
			return (float) getPoisonedArrowChance.invoke(npc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.0F;
	}
	
	public static void setMinTimeRanged(LOTREntityNPC npc, int min) {
		Object ai = NpcReflectionAccess.getRangedAttack(npc);
		if(ai != null) {
			try {
				attackTimeMin.setInt(ai, min);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static int getMinTimeRanged(LOTREntityNPC npc) {
		Object ai = NpcReflectionAccess.getRangedAttack(npc);
		if(ai != null) {
			try {
				return attackTimeMin.getInt(ai);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public static void setMaxTimeRanged(LOTREntityNPC npc, int max) {
		Object ai = NpcReflectionAccess.getRangedAttack(npc);
		if(ai != null) {
			try {
				attackTimeMax.setInt(ai, max);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static int getMaxTimeRanged(LOTREntityNPC npc) {
		Object ai = NpcReflectionAccess.getRangedAttack(npc);
		if(ai != null) {
			try {
				return attackTimeMax.getInt(ai);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public static long nextGC;
	
	public static class DefaultDataRanged {
		/*public int timeMin;
		public int timeMax;
		
		public float range;*/
		
		public float factor_timeMin;
		public float factor_timeMax;
		public float factor_range;
		
		public Category category;
	}
	
	public static TIntObjectMap<DefaultDataRanged> ddrMap = new TIntObjectHashMap<>();
	
	public static void checkGC() {
		if(nextGC < System.currentTimeMillis()) {
			nextGC = System.currentTimeMillis() + 100000; // 100 seconds
			for(int key : ddrMap.keys()) {
				Entity entity = null;
				for(WorldServer world : MinecraftServer.getServer().worldServers) {
					entity = world.getEntityByID(key);
					if(entity != null) break;
				}
				
				if(entity == null || entity.isDead) {
					ddrMap.remove(key);
				}
			}
			
		}
	}
	
	public static boolean preInit(LOTREntityNPC npc) {
		if(ASMConfig.guardsEquipRanged && npc.hiredReplacedInv.hasReplacedEquipment(LOTRInventoryHiredReplacedItems.RANGED)) {
			return npc.npcItemsInv.getRangedWeapon() != null;
		}
		return false;
	}
	
	public static void initRanged(IRangedAttackMob entity) {
		
		if(entity instanceof LOTREntityNPC) {
			LOTREntityNPC npc = (LOTREntityNPC) entity;
			
			if(preInit(npc)) {
				
				setupRanged(npc, npc.npcItemsInv.getRangedWeapon(), false);
			}
			
		}
	}
	
	public static DefaultDataRanged getOrCreate(LOTREntityNPC npc) {
		
			int id = npc.getEntityId();
			if(ddrMap.containsKey(id)) {
				return ddrMap.get(id);
			}
			else {
				int slot = LOTRInventoryHiredReplacedItems.RANGED;
				ItemStack stack = npc.hiredReplacedInv.hasReplacedEquipment(slot) ? npc.hiredReplacedInv.getStackInSlot(slot) : npc.npcItemsInv.getRangedWeapon();
				
				if(stack != null) {
					checkGC();
					Category category = Category.getCategory(stack.getItem());
					DefaultDataRanged ddr = new DefaultDataRanged();
					int timeMin = getMinTimeRanged(npc);
					int timeMax = getMaxTimeRanged(npc);
					ddr.category = category;
					
					
					float range = ASMConfig.guardsAdvancedSettings && GuardsAdvSettingsConfig.allowAmmoRangeModify ? ExtraHiredData.load(npc).ammoRangePreTemp : NpcReflectionAccess.getAmmoRange(npc);
					
					ddr.factor_timeMin = (float) timeMin / (float) category.attackTimeMin;
					ddr.factor_timeMax = (float) timeMax / (float) category.attackTimeMax;
					ddr.factor_range = (float) range / (float) category.attackRange;
					ddrMap.put(id, ddr);
					return ddr;
				}
			}
			return null;
		
		
		
		
		
		
		// as early as we can access ranged items
		// store def_timemin
		// store def_timemax
		// store def_range
		// store category
		
		
		
		
	}
	

	
	
	public static void setupRanged(LOTREntityNPC npc, ItemStack stack, boolean updateMoveRange) {
		DefaultDataRanged ddr = getOrCreate(npc);
		
		if(ddr != null) {
			Category category = Category.getCategory(stack.getItem());
			// no pentalty if its the default
			boolean same = ddr.category == category;
			
			float penalty = same ? 1 : category.penalty;
			boolean dynamic = same || ASMConfig.guardsDynamicAttackDelayRanged;
			float factor_timeMin = dynamic ? ddr.factor_timeMin : 1;
			float factor_timeMax = dynamic ? ddr.factor_timeMax : 1;
			//System.out.println(ddr.category+" "+category+" "+same+" "+factor_timeMax+" "+penalty+" "+ dynamic);
			setMinTimeRanged(npc, (int) (factor_timeMin * category.attackTimeMin * penalty));
			setMaxTimeRanged(npc, (int) (factor_timeMax * category.attackTimeMax * penalty));
			byte newDefAmmo = (byte) (ddr.factor_range * category.attackRange);
			if(ASMConfig.guardsAdvancedSettings && GuardsAdvSettingsConfig.allowAmmoRangeModify) {
				// handle compartiblity
				ExtraHiredData ext = ExtraHiredData.load(npc);
				int delta = ext.ammoRange - ext.ammoRangeDef;
				
				ext.ammoRangeDef = (byte) newDefAmmo;
				ext.setAmmoRangeVerify(newDefAmmo + delta, npc);
				
				if(updateMoveRange) {
					newDefAmmo = ext.ammoRange;
				}
			}
			else {
				NpcReflectionAccess.setAmmoRange(npc, newDefAmmo);
			}
			
			if(updateMoveRange) {
				updateMoveRange(npc, newDefAmmo);
			}
			
			//System.out.println("DDR: min: "+ddr.timeMin+" max: "+ddr.timeMax+" range: "+ddr.range);
			//System.out.println("	 min: "+ddr.factor_timeMin+" max: "+ddr.factor_timeMax+" range: "+ddr.factor_range);
			//System.out.println("NEW: min: "+getMinTimeRanged(npc)+" max: "+getMaxTimeRanged(npc)+" range: "+NpcReflectionAccess.getAmmoRange(npc));
		}
	}
	
	public static void updateMoveRange(LOTREntityNPC npc, float range) {
		LOTREntityAIRangedAttack ai = (LOTREntityAIRangedAttack) NpcReflectionAccess.getRangedAttack(npc);
		if(ai != null) {
			ai.resetTask(); // could cause npe on target depending on order.
			if(npc.ridingEntity != null && npc.ridingEntity instanceof LOTREntityHorse) {
				@SuppressWarnings("unchecked")
				List<EntityAITaskEntry> tasks = ((LOTREntityHorse) npc.ridingEntity).tasks.taskEntries;
				for(EntityAITaskEntry entry : tasks) {
					if(entry.action instanceof HorseMoveToRiderTargetAiFix) {
						((HorseMoveToRiderTargetAiFix) entry.action).moveRangeSq = HooksAiRanged.getMoveRangeSq(npc, range);
						
						break;
					}
				}
				
			}
		}
	}
	
	public static void spawnByTemplate(Entity entity, LOTREntityNPC npc, EntityLivingBase target, float speed, float acceleration, float gravity, float accuracy) {
		EntityArrow template = new EntityArrow(npc.worldObj, npc, target, speed, accuracy);
		entity.setLocationAndAngles(template.posX, template.posY, template.posZ, template.rotationYaw, template.rotationPitch);
		
	    npc.playSound("random.bow", 1.0F, 1.0F / (npc.getRNG().nextFloat() * 0.4F + 0.8F));
	    setupProjectileVelocity(entity, target, speed, acceleration, gravity, accuracy);
	    npc.worldObj.spawnEntityInWorld(entity);
	}
	
	
	
	
	public static void launchProjectile(IRangedAttackMob entity, EntityLivingBase target, float power) {
		if(entity instanceof LOTREntityNPC) {
			LOTREntityNPC npc = (LOTREntityNPC) entity;
			
			//if(npc.hiredReplacedInv.hasReplacedEquipment(LOTRInventoryHiredReplacedItems.RANGED)) {
				ItemStack stack = npc.npcItemsInv.getRangedWeapon();
				
				if(stack != null) {
					
					
					
					boolean customized = ASMConfig.guardsEquipRanged && npc.hiredReplacedInv.hasReplacedEquipment(LOTRInventoryHiredReplacedItems.RANGED);
					boolean allow = ASMConfig.aiRangedImprovements || customized;
					Item item = stack.getItem();
					Category category = null;
					
					if(allow) {
						category = Category.getCategory(item);
					}
					
					if(customized) {
						DefaultDataRanged ddr = getOrCreate(npc);
						if(ddr == null) {
							// GOTO label skip;
							allow = false;
						}
						else if(!allowCategory(item, ddr.category) || !useDurability(npc, stack, ddr, category)) {
							if(ASMConfig.aiRangedImprovements) {
								category = ddr.category; // use default category if disallowed here
							}
							else {
								// GOTO label skip;
								allow = false;
							}
						}
					}
					
					if(allow) {
						// we only need it for untouched npcs
						if(!customized && ASMConfig.randomEnchRangedNpcWeapons)
							handleRandomEnch(npc, stack);
						
						
						
						float accuracy = (float) npc.getEntityAttribute(LOTREntityNPC.npcRangedAccuracy).getAttributeValue();
						
						World world = npc.worldObj;
						boolean poison;
						float speed;
						//float str;
						switch(category) {
							case FIREPOT:
								spawnByTemplate(new LOTREntityFirePot(world, npc), npc, target, 1.5F, 0.99F, 0.04F, accuracy);
								return;
							case TERMITE:
								if(ASMConfig.guardsEquipAllowTermites) {
									spawnByTemplate(new LOTREntityThrownTermite(world, npc), npc, target, 1F, 0.99F, 0.03F, accuracy);
									return;
								}
								break;
							case SLING:
								spawnByTemplate(new LOTREntityPebble(world, npc).setSling(), npc, target, 1.5F, 0.99F, 0.04F, accuracy);
								return;
							case PLATE:
								spawnByTemplate(new LOTREntityPlate(world, ((LOTRItemPlate) item).plateBlock, npc), npc, target, 1.5F, 0.99F, 0.02F, accuracy);
								
								return;
							case SPEAR:
								LOTREntitySpear spear = new LOTREntitySpear(world, npc, target, stack, 1.07F, accuracy);
								int fireAspect2 = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) + LOTREnchantmentHelper.calcFireAspect(stack);
							    if (fireAspect2 > 0)
							      spear.setFire(100); 
							    for (LOTREnchantment ench : LOTREnchantment.allEnchantments) {
							      if (ench.applyToProjectile() && LOTREnchantmentHelper.hasEnchant(stack, ench))
							        LOTREnchantmentHelper.setProjectileEnchantment(spear, ench); 
							    } 
						        npc.playSound("random.bow", 1.0F, 1.0F / (npc.getRNG().nextFloat() * 0.4F + 1.2F));
						        setupProjectileVelocity(spear, target, 1.6F, 0.99F, 0.05F, accuracy);
								world.spawnEntityInWorld(spear);
								return;
							case AXE:
								LOTREntityThrowingAxe axe = new LOTREntityThrowingAxe(world, npc, target, stack, 1.0F, accuracy);
								int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) + LOTREnchantmentHelper.calcFireAspect(stack);
							    if(fireAspect > 0)
							    	axe.setFire(100); 
							    for(LOTREnchantment ench : LOTREnchantment.allEnchantments) {
							    	if(ench.applyToProjectile() && LOTREnchantmentHelper.hasEnchant(stack, ench))
							    		LOTREnchantmentHelper.setProjectileEnchantment(axe, ench); 
							    }
								
								npc.playSound("random.bow", 1.0F, 1.0F / (npc.getRNG().nextFloat() * 0.4F + 0.8F));
								setupProjectileVelocity(axe, target, 1.5F, 0.99F, 0.05F, accuracy);
								world.spawnEntityInWorld(axe);
							    npc.swingItem();
								return;
							case BLOWGUN:
								
							    speed = LOTRItemBlowgun.getBlowgunLaunchSpeedFactor(stack);
							    LOTREntityDart dart = ((LOTRItemDart) LOTRMod.tauredainDart).createDart(world, npc, target, new ItemStack(npc.getRNG().nextInt(npc instanceof LOTREntityTauredainBlowgunner ? 100 : 200) <= ASMConfig.tauredainPoisonDartChance ? LOTRMod.tauredainDartPoisoned : LOTRMod.tauredainDart), 1, accuracy);
							   
							    LOTRItemBlowgun.applyBlowgunModifiers(dart, stack); 
							    npc.playSound("lotr:item.dart", 1.0F, 1.0F / (npc.getRNG().nextFloat() * 0.4F + 1.2F) + 0.5F);
							    setupProjectileVelocity(dart, target, speed * 1.52F, 0.99F, 0.05F, accuracy);
							    world.spawnEntityInWorld(dart);
								return;
							
							case CROSSBOW:
								
								
							    speed = LOTRItemCrossbow.getCrossbowLaunchSpeedFactor(stack);
							    poison = npc.getRNG().nextFloat() < getPoisonedArrowChance(npc);
							    ItemStack boltitem = new ItemStack(poison ? LOTRMod.crossbowBoltPoisoned : LOTRMod.crossbowBolt);
							    LOTREntityCrossbowBolt bolt = new LOTREntityCrossbowBolt(world, npc, target, boltitem, 1, accuracy);
							    
							    LOTRItemCrossbow.applyCrossbowModifiers(bolt, stack); 
							    npc.playSound("lotr:item.crossbow", 1.0F, 1.0F / (npc.getRNG().nextFloat() * 0.4F + 0.8F));
							    setupProjectileVelocity(bolt, target, speed * 1.52F, 0.99F, 0.05F, accuracy);
							    world.spawnEntityInWorld(bolt);
								return;
							
							
								
							case BOW:
								if(item instanceof ItemBow) {
								   
								    speed = LOTRItemBow.getLaunchSpeedFactor(stack) * 1.5F;
								    
								    poison = npc.getRNG().nextFloat() < getPoisonedArrowChance(npc);
								    EntityArrow arrow = poison ? new LOTREntityArrowPoisoned(world, npc, target, 1, accuracy) : new EntityArrow(world, npc, target, 1, accuracy);
								    
								    LOTRItemBow.applyBowModifiers(arrow, stack); 
								    npc.playSound("random.bow", 1.0F, 1.0F / (npc.getRNG().nextFloat() * 0.4F + 0.8F));
								    
								    setupProjectileVelocity(arrow, target, speed, 0.99F, 0.05F, accuracy);
								    world.spawnEntityInWorld(arrow);
									return;
								}
								break; // we continue with default attack
							
						}
					}
					
					// leave place here so they still work if they have an invalid item.
				//}
			
			}
		}
		
		// default attack
		entity.attackEntityWithRangedAttack(target, power);
	}

	
	public static boolean useDurability(LOTREntityNPC npc, ItemStack rangedweapon, DefaultDataRanged ddr, Category category) {
		// which weapons are affected by durability? (only if not their default)
		// int guardsEquipDurabilityMode = 3; // 0 = none, 1 = any, 2 = any non default, 3 = spears, axes, plates, firepots and termites
		// int guardsEquipDurabilityFactor = 200; // durability multiplier in percent
		// boolean guardsEquipRemoveBroken = true; // whether equip can be removed when broken, def: true
		// percent factor, 0 disable
					// 500 = 5 damage
					// 100 = 1 damage
					// 50 = ~0.5 damage (random)
					// 1 = ~0.01 damage (random)
					
					// 500 = ~ 5x durability (random)
					// 100 = 1x durability
					// 50 = 50% durability
					// 1 = ~0.01 damage (random)
		
		if(ASMConfig.guardsEquipDurabilityMode == 0) return true;
		
		if(ASMConfig.guardsEquipDurabilityMode != 1 && category == ddr.category) {
			return true;
		}
		
		if(ASMConfig.guardsEquipDurabilityMode == 3 && !category.damageable) {
			return true;
		}
		
		LOTRInventoryNPCItems items = npc.npcItemsInv;
		
		
		
		
		// 0.1 every 10 times 1 damage
		// 0.25 every 4 times 1 damage
		// 0.5 every 2 times 1 damage
		// 1.0 every 1 times 1 damage
		// 2.0 every 1 times 2 damage
		// 2.4 every 1 times 2 damage
		// 2.5 every 1 times 3 damage
		
		float damage = category.durability;
		int subtract = (int) damage;
		
		if(damage < 1F) {
			if(HooksAiRanged.rand.nextFloat() > damage) {
				return true;
			}
			subtract = 1;
		}
		
		
		ItemStack modified = rangedweapon.copy();
		
		if(rangedweapon.isItemStackDamageable()) {
			if(!ASMConfig.guardsEquipRemoveBroken && (rangedweapon.getItemDamage()+subtract) > rangedweapon.getMaxDamage()) {
				// itemstack is low durability
				return false;
			}
			modified.damageItem(subtract, npc);
		}
		else {
			modified.stackSize -= subtract;
		}
			
		if(modified.stackSize <= 0) {
			// held item update may change attack mode and cause concurrent modi because we are in an aitask now
			skipHeldItemUpdate = true;
			npc.hiredReplacedInv.onEquipmentChanged(LOTRInventoryHiredReplacedItems.RANGED, null);
			skipHeldItemUpdate = false;
			return true;
		}
		
		
		modifyRangedItem(items, rangedweapon, modified);
	    return true;
	}
	
	static Method tryApplyRandomEnchantsForEntity = ReflectionHelper.findMethod(LOTREnchantmentHelper.class, null, new String[] {"tryApplyRandomEnchantsForEntity"}, ItemStack.class, Random.class);
	
	public static void handleRandomEnch(LOTREntityNPC npc, ItemStack rangedweapon) {
		// WARNING: rangedweapon is expected to not get modified by any other method in the context of launchProjectile!
		// 			Else needs copy first
		//ItemStack modified = rangedweapon;
		boolean applied = false;
		try {
			applied = (boolean) tryApplyRandomEnchantsForEntity.invoke(null, rangedweapon, npc.getRNG());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(applied) {
			modifyRangedItem(npc.npcItemsInv, npc.npcItemsInv.getRangedWeapon(), rangedweapon);
			// NOTE: updatehelditem would not be needed but good for compartibility that listens to that
		}
	}
	
	static final int IDLE_ITEM = 0;
	
	public static void modifyRangedItem(LOTRInventoryNPCItems items, ItemStack rangedweapon, ItemStack modified) {
		if(ItemStack.areItemStacksEqual(rangedweapon, items.getStackInSlot(IDLE_ITEM))) {
		    items.setIdleItem(modified);
		    items.setIdleItemMounted(modified);
	    }
	    
	    items.setRangedWeapon(modified);
	}
	
	// catch arymethic exception / zero
	public static void setupProjectileVelocity(Entity projectile, Entity target, float speed, float acceleration, float gravity, float accuracy) {
		
		double deltaX = target.posX - projectile.posX;
		double deltaZ = target.posZ - projectile.posZ;
		// between half height and eye height
		float halfHeight = target.height * 0.5F;
		float goalHeight = halfHeight + ((target.getEyeHeight() - halfHeight) * 0.5F);
		
		double deltaY = target.posY + goalHeight - projectile.posY;
		
		double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
		
		
		double velocityX = speed * (deltaX / distance);
		double velocityY = speed * (deltaY / distance);
		double velocityZ = speed * (deltaZ / distance);
		
		Random rand = projectile.worldObj.rand;
		
        
        velocityX += rand.nextGaussian() * 0.0075D * accuracy;
        velocityY += rand.nextGaussian() * 0.0075D * accuracy;
        velocityZ += rand.nextGaussian() * 0.0075D * accuracy;
        
		
		double velocityLen = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
		
		int constTime = Math.max(1, (int) Math.ceil(distance / velocityLen));
		int time = constTime;
		
		// faster speeds need less gravity lops
		// slower speeds need more gravity lops
		float speedRetainReverse = 2 - acceleration;
		float factor = 1;
		float halfGravity = gravity / 2.0F;
		while(time > 0) {
			time--;
			velocityY += halfGravity * factor;
			factor *= speedRetainReverse;
		}
		
		
		projectile.motionX = velocityX;
		projectile.motionY = velocityY;
		projectile.motionZ = velocityZ;
	}
    
}
