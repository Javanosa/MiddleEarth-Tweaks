package metweaks.features;

import java.util.List;
import java.util.Queue;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;

public final class NetworkCalls {
	@SuppressWarnings("rawtypes")
	public static final Queue getReceivedPacketsQueue(final NetworkManager manager) {
		return null;
		// return manager.getReceivedPacketsQueue();
	}
	
	public static final EnumConnectionState getConnectionState(final NetworkManager manager) {
		return null;
		// return manager.getConnectionState();
	}
	
	public static final void setConnectionStateRaw(final NetworkManager manager, final EnumConnectionState state) {
		
		// return manager.setConnectionStateRaw(EnumConnectionState state);
	}
	
	public static final List<NetworkManager> getNetManagers(final NetworkSystem system) {
		return null;
		// return system.getNetManagers();
    }
	
	
	
	
}
