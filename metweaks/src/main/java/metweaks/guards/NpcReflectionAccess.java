package metweaks.guards;

import java.lang.reflect.Field;
import java.util.List;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.entity.ai.LOTREntityAINearestAttackableTargetBasic;
import lotr.common.entity.ai.LOTREntityAIRangedAttack;
import lotr.common.entity.npc.LOTREntityCorsair;
import lotr.common.entity.npc.LOTREntityElf;
import lotr.common.entity.npc.LOTREntityHobbitBounder;
import lotr.common.entity.npc.LOTREntityLossarnachAxeman;
import lotr.common.entity.npc.LOTREntityMountainTroll;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTREntityRanger;
import lotr.common.entity.npc.LOTREntitySnowTroll;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;

public class NpcReflectionAccess {
	
	
	private static final String raName = "rangedAttackAI";
	private static final Field elf_rangedAttack = ReflectionHelper.findField(LOTREntityElf.class, raName);
	
	private static final Field hillTroll_rangedAttack = ReflectionHelper.findField(LOTREntityMountainTroll.class, raName);
	private static final Field corsair_rangedAttack = ReflectionHelper.findField(LOTREntityCorsair.class, raName);
	private static final Field snowTroll_rangedAttack = ReflectionHelper.findField(LOTREntitySnowTroll.class, raName);
	private static final Field lossarnach_rangedAttack = ReflectionHelper.findField(LOTREntityLossarnachAxeman.class, raName);
	
	private static final Field attackRange = ReflectionHelper.findField(LOTREntityAIRangedAttack.class, "attackRange");
	private static final Field attackRangeSq = ReflectionHelper.findField(LOTREntityAIRangedAttack.class, "attackRangeSq");
	
	private static final Field shouldCheckSight = ReflectionHelper.findField(EntityAITarget.class, "shouldCheckSight", "field_75297_f", "d");
	
	
	
	public static void setCheckSight(LOTREntityNPC npc, boolean checkSight) {
		@SuppressWarnings("unchecked")
		List<EntityAITaskEntry> tasks = npc.targetTasks.taskEntries;
	     for(EntityAITaskEntry obj : tasks) {
	    	  if(obj.action instanceof LOTREntityAINearestAttackableTargetBasic) {
	    		  try {
	    			  shouldCheckSight.setBoolean(obj.action, checkSight);
				}
	    		catch (Exception e) {
					e.printStackTrace();
				}
	    	  }
	      }
	}
	
	public static boolean getCheckSight(LOTREntityNPC npc) {
		@SuppressWarnings("unchecked")
		List<EntityAITaskEntry> tasks = npc.targetTasks.taskEntries;
	     for(EntityAITaskEntry obj : tasks) {
	    	  if(obj.action instanceof LOTREntityAINearestAttackableTargetBasic) {
	    		  try {
	    			  return shouldCheckSight.getBoolean(obj.action);
					
				}
	    		catch (Exception e) {
					e.printStackTrace();
				}
	    	  }
	     }
		return true;
	}
	
	public static void setAmmoRange(LOTREntityNPC npc, byte range) {
		Object ai = getRangedAttack(npc);
		if(ai != null) {
			try {
				attackRange.setFloat(ai, range);
				attackRangeSq.setFloat(ai, range*range);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static byte getAmmoRange(LOTREntityNPC npc) {
		Object ai = getRangedAttack(npc);
		if(ai != null) {
			try {
				return (byte) attackRange.getFloat(ai);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	// Warning: does not check if instance of LOTREntityAIRangedAttack, trusts the field
	public static Object getRangedAttack(LOTREntityNPC npc) {
		Field field = null;
		Object aiRangedAttack = null;
		if(npc instanceof LOTREntityRanger) {
			aiRangedAttack = ((LOTREntityRanger) npc).rangedAttackAI;
		}
		else if(npc instanceof LOTREntityElf) {
			field = elf_rangedAttack;
		}
		else if(npc instanceof LOTREntityHobbitBounder) {
			aiRangedAttack = ((LOTREntityHobbitBounder) npc).rangedAttackAI;
		}
		else if(npc instanceof LOTREntityMountainTroll) {
			field = hillTroll_rangedAttack;
		}
		else if(npc instanceof LOTREntityCorsair) {
			field = corsair_rangedAttack;
		}
		else if(npc instanceof LOTREntitySnowTroll) {
			field = snowTroll_rangedAttack;
		}
		else if(npc instanceof LOTREntityLossarnachAxeman) {
			field = lossarnach_rangedAttack;
		}
		else {
			@SuppressWarnings("unchecked")
			List<EntityAITaskEntry> tasks = npc.tasks.taskEntries;
		    for(EntityAITaskEntry obj : tasks) {
		    	if(obj.action instanceof LOTREntityAIRangedAttack) {
		    		aiRangedAttack = obj.action;
		    		break;
		    	}
		    }
		}
		
		if(field != null) {
			try {
				aiRangedAttack = field.get(npc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return aiRangedAttack;
	}
	
	 // 0 to 4 difference
	 // LOTREntityCorsair rangedAttackAI
	  // LOTREntityElf rangedAttackAI
	  // LOTREntityHobbitBounder rangedAttackAI
	  // LOTREntityLossarnachAxeman rangedAttackAI
	  // ? LOTREntityMallornEnt rangedAttackAI
	  // LOTREntityMountainTroll rangedAttackAI
	  // LOTREntityRanger rangedAttackAI
	  // LOTREntitySnowTroll rangedAttackAI
}
