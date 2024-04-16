package metweaks;

public class MeTweaksAPI {
    // add vertical manually here...or tell me to add compartility for your Slab class
	// numberTypes is the amount of types your slab instance supports
	void addVerticalSlab(Block slab, int numberTypes);
	
	// set stateValue null if you just wanna output on / off
    @SideOnly(Side.CLIENT)
    void actionBar(String message, boolean stateOnOff, int ticks, @Nullable String stateValue);
    
    VerticalSlab getVerticalSlab(Block slab, int meta);
    
    void setVerticalMode(EntityPlayer player, boolean enable);
    
    boolean isVerticalMode(EntityPlayer player);
    
    // returns rgba, works with 6 and 8 digit
    int[] parseHexColor(String hex, int[] rgba_defaults, int defaultalpha);
}
