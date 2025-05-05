package metweaks;

public class MeTweaksAPI {
    // add vertical manually here...or tell me to add compartility for your Slab class
    // subtypes is the amount of types your slab instance supports
    void addVerticalSlab(Block slab, int numberTypes);

    // add vertical manually here...or tell me to add compartility for your Slab class
    // subtypes is the amount of types your slab instance supports
    void addVerticalSlabFalling(Block slab, int subtypes);
	
    // ME-Tweaks Status Message - Can display Messages like Vertical Slabs ON / OFF
    // set stateValue null if you just wanna output on / off
    // set disableState true if you just want to show a message
    // set ticks 0 to clear
    @SideOnly(Side.CLIENT)
    void actionBar(String message, boolean stateOnOff, int ticks, @Nullable String stateValue, boolean disableState);

    // block placement flags like world.setBlock()
    // direction 0: south 1: west 2: north 3: east
    // Please use getVerticalSlab() and getMetaForVertical() when placing multiple blocks
    void setVerticalSlab(World world, int x, int y, int z, Block slab, int subtype, int facing, int flags)

    // meta of the normal slab
    // direction 0: south 1: west 2: north 3: east
    int getMetaForVertical(VerticalSlab vertical, int subtype, int facing)
	
    VerticalSlab getVerticalSlab(Block slab, int meta);
    
    void setVerticalMode(EntityPlayer player, boolean enable);
    
    boolean isVerticalMode(EntityPlayer player);
    
    // returns rgba, works with 6 and 8 digit
    // rgba_defaults if hex is invalid
    // defaultAlpha if hex is missing alpha (only 6 digit)
    int[] parseHexColor(String hex, int[] rgba_defaults, int defaultAlpha);
	
    // since forge one's doesnt properly work
    void renameConfigProperty(Configuration config, String category, String oldName, String newName);

    // returns [List<Entity results, int newArgsOffset] 		
    // newArgsOffset == offsetArgsStart indicates that string isnt a selector
    Object[] selectEntities(int offsetArgsStart, String[] args, ICommandSender sender);

    // duration in ticks, default is 32
    // if you have access to the corresponding food class, please override getMaxEatingDuration() instead.
    void setConsumeDuration(Item food, int duration);

    // updates move range for ranged ai
    void updateMoveRange(LOTREntityNPC npc, float ammoRange);

    // whether an entity will show only a ranged weapon in the unit overview (currently)
    Set<Class<? extends Entity>> getRangedOnlyEntities();
	
    // whether an entity is ranged
    Set<Class<? extends Entity>> getRangedEntities();
}

public class NpcReflectionAccess {
	// Performance Note: All calls in this class use reflection or iteration

	// sets target ai sight check
	void setCheckSight(LOTREntityNPC npc, boolean checkSight);

	// returns target ai sight check
	boolean getCheckSight(LOTREntityNPC npc);

	// set projectile range 
	void setAmmoRange(LOTREntityNPC npc, byte range);

	// returns projectile range
	byte getAmmoRange(LOTREntityNPC npc);

	// returns ranged attack ai if exists
	Object getRangedAttack(LOTREntityNPC npc);
}

public class HiredInfoAccess {
	void setWanderRange(LOTRHiredNPCInfo info, int wanderRange);

	int getWanderRange(LOTRHiredNPCInfo info);

	LOTREntityNPC getNPC(LOTRHiredNPCInfo info);

	// gets extra data of hired, contains defaults for ammoRange/aiRange, and their current ammoRange/checkTargetInSight values
	ExtraHiredData getExt(LOTRHiredNPCInfo info);

	// used the set the initialized extra data, 
	void setExt(LOTRHiredNPCInfo info, ExtraHiredData ext);

	// field-change only
	void setGuardRange(LOTRHiredNPCInfo info, int guardRange);
}

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

