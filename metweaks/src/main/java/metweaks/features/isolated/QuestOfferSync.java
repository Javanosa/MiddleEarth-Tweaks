package metweaks.features.isolated;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;

public class QuestOfferSync {
	private static Field field_playerPacketCache;
	private static Field field_destroyedItemsNetCache;
	
	public QuestOfferSync() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SuppressWarnings({ "rawtypes" })
	@SubscribeEvent(priority = EventPriority.HIGHEST) // needs to run before lotr starts tracking
	public void onStartTracking(StartTracking event) {
		if(ASMConfig.fixQuestofferSync && MeTweaks.lotr && event.target instanceof LOTREntityNPC) {
			try {
				if(field_playerPacketCache == null) {
					field_playerPacketCache = lotr.common.entity.npc.LOTREntityQuestInfo.class.getDeclaredField("playerPacketCache");
					field_playerPacketCache.setAccessible(true);
				}
				// removes entry for player that determines if quest mark data has been sent
				((Map) field_playerPacketCache.get(((LOTREntityNPC) event.target).questInfo)).remove(event.entityPlayer.getUniqueID());
				
				
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(ASMConfig.entityTrackerPatches) {
			// start fix respawn entities not interactable
			if(field_destroyedItemsNetCache == null) {
				field_destroyedItemsNetCache = ReflectionHelper.findField(EntityPlayerMP.class, "destroyedItemsNetCache", "field_71130_g");
			}
			
			try {
				@SuppressWarnings("unchecked")
				List<Integer> destroyedItemsNetCache = (List<Integer>) field_destroyedItemsNetCache.get(event.entityPlayer);
				destroyedItemsNetCache.remove(Integer.valueOf(event.target.getEntityId()));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
