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
}
