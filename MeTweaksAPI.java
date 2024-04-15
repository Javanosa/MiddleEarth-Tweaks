package metweaks;

public class MeTweaksAPI {
    @SideOnly(Side.CLIENT)
    void actionBar(String message, boolean stateOnOff, int ticks, @Nullable String stateValue);
    
    VerticalSlab getVerticalSlab(Block slab, int meta);
    
    void setVerticalMode(EntityPlayer player, boolean enable);
    
    boolean isVerticalMode(EntityPlayer player);
    
    // returns rgba, works with 6 and 8 digit
    int[] parseHexColor(String hex, int[] rgba_defaults, int defaultalpha);
}
