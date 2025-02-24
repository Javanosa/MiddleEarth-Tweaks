package metweaks.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;

public class NetworkHandler {
	public static final SimpleNetworkWrapper networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MeTweaks.MODID+"_");
	
	public static void init() {
		if(MeTweaksConfig.verticalSlabs) {
			networkWrapper.registerMessage(SyncVerticalModePacket.Handler.class, SyncVerticalModePacket.class, 1, Side.SERVER);
		
		}
		if(MeTweaks.lotr) {
			// should be enough
			if(ASMConfig.guardsWanderRange) { // ASMConfig.guardsAdvancedSettings
				networkWrapper.registerMessage(HiredAdvInfoPacket.Handler.class, HiredAdvInfoPacket.class, 2, Side.CLIENT);
				networkWrapper.registerMessage(HiredAdvInfoPacket.Handler.class, HiredAdvInfoPacket.class, 3, Side.SERVER);
			}
			
			networkWrapper.registerMessage(GuardmodeHornPacket.Handler.class, GuardmodeHornPacket.class, 4, Side.SERVER);
			if(ASMConfig.unitOverview) {
				networkWrapper.registerMessage(GuardsOverviewPacket.Handler.class, GuardsOverviewPacket.class, 5, Side.CLIENT);
				networkWrapper.registerMessage(GuardsOverviewActionPacket.Handler.class, GuardsOverviewActionPacket.class, 6, Side.SERVER);
			}
			
			if(ASMConfig.hiredTransfer) {
				networkWrapper.registerMessage(HiredTransferPacket.Handler.class, HiredTransferPacket.class, 7, Side.CLIENT);
				networkWrapper.registerMessage(HiredTransferPacket.Handler.class, HiredTransferPacket.class, 8, Side.SERVER);
			}
			
			if(MeTweaksConfig.unitTracking) {
				networkWrapper.registerMessage(SyncUnitTrackingModePacket.Handler.class, SyncUnitTrackingModePacket.class, 9, Side.SERVER);
			
			}
			
		}
	}
}
