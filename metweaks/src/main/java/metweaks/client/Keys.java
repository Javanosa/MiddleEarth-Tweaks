package metweaks.client;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import metweaks.MeTweaks;
import net.minecraft.client.settings.KeyBinding;

public class Keys {
	public static KeyBinding toggleHealth;
	public static KeyBinding toggleVertical;
	public static KeyBinding openUnitOverview;

	public static void init() {
		toggleHealth = new KeyBinding("key.togglehealth", Keyboard.KEY_H, MeTweaks.MODID);
		toggleVertical = new KeyBinding("key.toggleverticalslabs", Keyboard.KEY_V, MeTweaks.MODID);
		openUnitOverview = new KeyBinding("key.openUnitOverview", Keyboard.KEY_U, MeTweaks.MODID);
		ClientRegistry.registerKeyBinding(toggleHealth);
		ClientRegistry.registerKeyBinding(toggleVertical);
		ClientRegistry.registerKeyBinding(openUnitOverview);
	}
}
