package metweaks;

public class MeTweaksAPI {
    // add vertical manually here...or tell me to add compartility for your Slab class
    // subtypes is the amount of types your slab instance supports
    void addVerticalSlab(Block slab, int numberTypes);

    // add vertical manually here...or tell me to add compartility for your Slab class
    // subtypes is the amount of types your slab instance supports
    void addVerticalSlabFalling(Block slab, int subtypes);
	
    // set stateValue null if you just wanna output on / off
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
    int[] parseHexColor(String hex, int[] rgba_defaults, int defaultalpha);
}
