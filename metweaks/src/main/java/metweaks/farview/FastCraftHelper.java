package metweaks.farview;

import metweaks.MeTweaks;
import net.minecraft.util.LongHashMap;

public final class FastCraftHelper {
	// Add Field 	private final internaldata Lfastcraft/aU;

	public FastCraftHelper(LongHashMap map) {
		// ALOAD this
		// INVOKESTATIC getInternalData (Lnet/minecraft/util/LongHashMap;)Ljava/lang/Object;
		// PUTFIELD internaldata Lfastcraft/aQ;
	}
	
	public static final LongHashMap getMapInstance() {
		return FastCraftInfo.hasFastcraft && FastCraftInfo.useFastCraft ? null : MeTweaks.hodgepodge ? new FastUtilLongHashMap() : new METLongHashMap();
	}
	
	/*public final int getCapacity() {
		// ALOAD this
		// internaldata.capacity I
		return 0;
	}*/
	
	public final long[] getKeys() {
		// ALOAD this
		// internaldata.keys [J
		return null;
	}
	
	public final Object[] getValues() {
		// ALOAD this
		// internaldata.values [Ljava/lang/Object;
		return null;
	}
	
	public final long getNoEntryValue() {
		// ALOAD this
		// internaldata.noEntryValue J
		return 0L;
	}
}
