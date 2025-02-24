package metweaks.client.healthbar;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import noppes.npcs.client.RenderChatMessages;
import noppes.npcs.entity.EntityNPCInterface;

public class CustomNpcsBridge {
	
	static Field field_messages;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean hasChatMessage(Entity entity) {
		if(entity instanceof EntityNPCInterface) {
			EntityNPCInterface customNpc = (EntityNPCInterface) entity;
			if(customNpc.messages == null) return false;
			
			Set<Long> times = null;
			try {
				if(field_messages == null) {
					field_messages = RenderChatMessages.class.getDeclaredField("messages");
					field_messages.setAccessible(true);
				}
				
				Map map = (Map) field_messages.get(customNpc.messages);
				if(map != null) {
					times = map.keySet();
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return MpmBridge.isTimedout(times);
		}
		return false;
	}
}
