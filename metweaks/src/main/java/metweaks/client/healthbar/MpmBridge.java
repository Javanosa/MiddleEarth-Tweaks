package metweaks.client.healthbar;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import noppes.mpm.client.ChatMessages;

public class MpmBridge {
	
	static Field field_messages;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean hasChatMessage(Entity entity) {
		ChatMessages chat = ChatMessages.getChatMessages(entity.getCommandSenderName());
			
		Set<Long> times = null;
		try {
			if(field_messages == null) {
				field_messages = ChatMessages.class.getDeclaredField("messages");
				field_messages.setAccessible(true);
			}
			
			Map map = (Map) field_messages.get(chat);
			if(map != null) {
				times = map.keySet();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return isTimedout(times);
	}
	
	public static boolean isTimedout(Set<Long> times) {
		if(times != null && !times.isEmpty()) {
			long time = System.currentTimeMillis();
			
			for(long timestamp : times) {
				if(time <= timestamp + 10000L) {
					return true;
				}
			}
		}
		return false;
	}
}
