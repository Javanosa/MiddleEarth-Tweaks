package metweaks.block;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;

public class NetherEnderLiciousSlabs {
	
	private static final Package packageNetherlicious = Package.getPackage("DelirusCrux.Netherlicious.Common.Blocks.Slabs");
	private static final Package packageEnderlicious = Package.getPackage("DelirusCrux.Enderlicious.Common.Blocks.Slabs");

	@SuppressWarnings("unchecked")
	public static int getNumTypes(Block block) {
		Package p = block.getClass().getPackage();
		if((p == packageNetherlicious || p == packageEnderlicious) && p != null) {
			try {
				return ((String[]) ReflectionHelper.<Object, Block>getPrivateValue((Class<? super Block>) block.getClass(), null, "Types")).length;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return 8;
	}
	
}
