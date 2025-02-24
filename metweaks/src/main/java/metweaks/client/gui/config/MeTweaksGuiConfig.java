package metweaks.client.gui.config;

import cpw.mods.fml.client.config.GuiConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import net.minecraft.client.gui.GuiScreen;

public class MeTweaksGuiConfig extends GuiConfig{
	public MeTweaksGuiConfig(GuiScreen parent) {
		super(parent, MeTweaksConfig.getElementList(), MeTweaks.MODID, false, false, GuiConfig.getAbridgedConfigPath(MeTweaksConfig.config.toString()));
	}
}
