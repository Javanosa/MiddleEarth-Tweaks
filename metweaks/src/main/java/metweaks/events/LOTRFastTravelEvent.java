package metweaks.events;

import java.util.List;
import java.util.Set;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import lotr.common.world.map.LOTRAbstractWaypoint;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;

// this event will be posted on MinecraftForge.EVENT_BUS
// Listen to LOTRFastTravelEvent.Transport or LOTRFastTravelEvent.Success, not just LOTRFastTravelEvent
public class LOTRFastTravelEvent extends Event {
	 public final EntityPlayer player;
	 
	 // Target waypoint
	 public final LOTRAbstractWaypoint waypoint;
	 
	 public LOTRFastTravelEvent(EntityPlayer player, LOTRAbstractWaypoint waypoint) {
		 this.player = player;
		 this.waypoint = waypoint;
		 
	 }
	
	 @Cancelable
	 public static class Transport extends LOTRFastTravelEvent {
		// If you want to prevent this fastTravel, event.setCanceled(true);
		// Nothing has been done yet
		 
		// Add or remove entities here
		// Ridden mounts that dont have EntityLiving as super class dont travel with you (boats, minecarts)
		//    However you can manually fastTravel them
		// This set does NOT contain the player mount, dont add it, it will be handled seperately. 
		public final Set<EntityLiving> toTransport;
		
		// All entities of superclass EntityLiving in range of 256 blocks (max viewdistance of 16 chunks) around the player
		public final List<EntityLiving> nearbyEntities;
		 
        public Transport(EntityPlayer player, LOTRAbstractWaypoint waypoint, Set<EntityLiving> toTransport, List<EntityLiving> nearbyEntities) {
            super(player, waypoint);
            this.toTransport = toTransport;
            this.nearbyEntities = nearbyEntities;
        }
     }
	 
	 public static class Success extends LOTRFastTravelEvent {
		// After FastTravel succeeded
		// You can use this event to print a success message
		// Or to check/validate things
		 
		public final int x;
		public final int y;
		public final int z;
		 
		public Success(EntityPlayer player, LOTRAbstractWaypoint waypoint, int x, int y, int z) {
			super(player, waypoint);
			this.x = x;
			this.y = y;
			this.z = z;
		}
     }
}

