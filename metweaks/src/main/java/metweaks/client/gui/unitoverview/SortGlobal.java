package metweaks.client.gui.unitoverview;

import static metweaks.client.gui.unitoverview.GuiUnitOverview.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import gnu.trove.map.TIntObjectMap;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTRHiredNPCInfo.Task;
import metweaks.client.gui.unitoverview.GuiUnitList.Entry;
import net.minecraft.item.ItemStack;

public class SortGlobal implements Comparator<Entry> {
	
	public static List<Sorter> sorters = new ArrayList<>();
	
	public static class Sorter implements Comparator<Entry> {
		public int columnID;
		
		public Sorter(int id) {
			columnID = id;
		}
		
		@Override
		public int compare(Entry o1, Entry o2) {
			return 0;
		}
	}
	
	public static void onClickColumn(int columnID) {
		
		int indexInList = 0;
		for(int i = 0; i < sorters.size(); i++) {
			Sorter sorter = sorters.get(i);
			if(sorter.columnID == columnID) {
				indexInList = i;
				if(i == 0) return;
				break;
			}
		}
		
		
		Sorter temp = sorters.remove(indexInList);
		sorters.add(0, temp);
		
	}
	
	/*public static int getItemWeight(ItemStack stack) {
		if(LOTRWeaponStats.isMeleeWeapon(stack)) {
			
			// (damage * speed * reach) + knockback
			float damage = (LOTRWeaponStats.getMeleeDamageBonus(stack) * LOTRWeaponStats.getMeleeSpeed(stack) * LOTRWeaponStats.getMeleeReachFactor(stack)) + LOTRWeaponStats.getTotalKnockback(stack);
			
			return (int) damage; 
	    } 
		
		// armor
		// melee
		// ranged
		// stacksize
		// special
		return 0;
	}*/
	
	public static int totalInvWeight(Entry entry) {
		LOTREntityNPC npc = entry.npc;
		// slots
		int numSlots = 0;
		int numUsed = 0;
		//int itemWeight;
		boolean warrior = npc.hiredNPCInfo.getTask() == Task.WARRIOR;
		if(warrior) {
			numSlots += 4;
			for(int i = 1; i < 5; i++) {
				ItemStack stack = npc.getEquipmentInSlot(i);
				if(stack != null) {
					numUsed++;
				}
			}
		}
		
		TIntObjectMap<ItemStack[]> invs = GuiUnitOverview.inventories;
		if(invs != null) {
			ItemStack[] items = invs.get(npc.getEntityId());
			if(items != null) {
				int numToRender = warrior ? /*ASMConfig.unitOverviewHideMeleeForRangedOnly && GuardsOverviewPacket.rangedEntitiesOnly.contains(npc.getClass()) ? 1 : */2 : 4;
				numSlots += numToRender;
				for(int i = 0; i < numToRender; i++) {
					ItemStack stack = items[i];
					if(stack != null) {
						numUsed++;
					}
				}
			}
		}
		else if(warrior) {
			// fallback, render held item of warrior
			ItemStack held = npc.getHeldItem();
			numSlots++;
			if(held != null) {
				numUsed++;
			}
		}
		
		if(warrior) {
			ItemStack bomb = npc.npcItemsInv.getBomb();
			numSlots++;
			if(bomb != null) {
				numUsed++;
			}
		}
		
		return numSlots+numUsed;
	}
	
	public static int getXpLevel(Entry entry) {
		return entry.npc.hiredNPCInfo.getTask() == Task.WARRIOR ? entry.npc.hiredNPCInfo.xpLevel : 0;
	}
	
	static {
		// HEAVY
		sorters.add(new Sorter(columnDistance) {
			public int compare(Entry o1, Entry o2) {
				int result = Integer.compare(o1.blocks, o2.blocks);
				return col_asc[columnDistance] ? -result : result;
			}
		});
		// LIGHT
		sorters.add(new Sorter(columnLvl) {
			public int compare(Entry o1, Entry o2) {
				int result = Integer.compare(getXpLevel(o1), getXpLevel(o2));
				return col_asc[columnLvl] ? result : -result;
			}
		});
		// FINAL
		sorters.add(new Sorter(columnIndex) {
			public int compare(Entry o1, Entry o2) {
				int result = Integer.compare(o1.npc.getEntityId(), o2.npc.getEntityId());
				return col_asc[columnIndex] ? -result : result;
			}
		});
		// LIGHT
		sorters.add(new Sorter(columnArmor) {
			public int compare(Entry o1, Entry o2) {
				 int result = Integer.compare(o1.npc.getTotalArmorValue(), o2.npc.getTotalArmorValue());
				return col_asc[columnArmor] ? result : -result;
			}
		});
		sorters.add(new Sorter(columnType) {
			public int compare(Entry o1, Entry o2) {
				 int result = o1.type.compareToIgnoreCase(o2.type);
				return col_asc[columnType] ? -result : result;
			}
		});
		sorters.add(new Sorter(columnName) {
			public int compare(Entry o1, Entry o2) {
				String name1 = o1.name;
				if(name1 == null) name1 = "";
				String name2 = o2.name;
				if(name2 == null) name2 = "";
				
				int result = name1.compareToIgnoreCase(name2);
				return col_asc[columnName] ? -result : result;
			}
		});
		
		sorters.add(new Sorter(columnCompany) {
			public int compare(Entry o1, Entry o2) {
				String name1 = o1.company;
				if(name1 == null) name1 = "";
				String name2 = o2.company;
				if(name2 == null) name2 = "";
				
				int result = name1.compareToIgnoreCase(name2);
				return col_asc[columnCompany] ? result : -result;
			}
		});
		sorters.add(new Sorter(columnInventory) {
			public int compare(Entry o1, Entry o2) {
				 int result = Integer.compare(totalInvWeight(o1), totalInvWeight(o2));
				return col_asc[columnInventory] ? result : -result;
			}
		});
		
		
		
		
		// onClick column = move sorters by one index and place corresponding sorter at top of list
		// natural order from init
	}

	@Override
	public int compare(Entry top, Entry bottom) {
		//if(!col_asc[columnIndex]) {
		for(Sorter sorter : sorters) {
			int result = sorter.compare(top, bottom);
			if(result == 0) {
				// next, more finetuning in sorting by priority
				continue;
			}
			// else return result
			return result;
		}
		//}
		return 0;
	}

}
