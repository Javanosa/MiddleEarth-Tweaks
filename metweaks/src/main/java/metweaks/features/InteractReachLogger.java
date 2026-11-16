package metweaks.features;

import lotr.common.item.LOTRWeaponStats;
import metweaks.MeTweaksConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class InteractReachLogger {
	
	private static double trim(double val) {
		 return Math.round(val * 100.0) / 100.0;
	 }
	 
	 private static ChatComponentText getComponent(double distance, double reach, double buffer) {
		 ChatComponentText chat = new ChatComponentText(trim(distance)+" ");
		 if(distance > reach) {
			 chat.getChatStyle().setColor(distance > (reach + buffer) ? EnumChatFormatting.RED : EnumChatFormatting.GOLD);
		 }
		 return chat;
	 }
	 
	 public static void verboseInteractReach(EntityPlayerMP player, Entity target, double reach, double distanceSq, double buffer) {
		 double distance = Math.sqrt(distanceSq);
		 ChatComponentText chat = new ChatComponentText("Reach: ");
		 
		 chat.appendSibling( getComponent(distance, reach, buffer) );
		 
		 if(MeTweaksConfig.debug >= 2) {
			 distance = player.getDistanceToEntity(target);
			 chat.appendSibling(
				getComponent(distance, reach, LOTRWeaponStats.getMeleeExtraLookWidth() + target.getCollisionBorderSize())
			 );
		 }
		 
		 chat.appendSibling(new ChatComponentText("Max: "+trim(reach)));
		 player.addChatMessage(chat);
	 }
}
