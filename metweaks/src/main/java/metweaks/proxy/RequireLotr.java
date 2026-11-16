package metweaks.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.tileentity.LOTRTileEntityMobSpawner;
import metweaks.ASMConfig;
import metweaks.client.features.RenderMobSpawnerSimple;
import metweaks.client.gui.GuiHiredAdvSettings;
import metweaks.features.halftroll.LOTREntityHalfTrollThrower;
import metweaks.features.halftroll.LOTRRenderHalfTrollThrower;
import metweaks.client.gui.GuiGuardmodeHornSettings;
import metweaks.network.HiredAdvInfoPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class RequireLotr {
	
	public static void init() {
		TileEntityRendererDispatcher.instance.mapSpecialRenderers.remove(LOTRTileEntityMobSpawner.class);
		ClientRegistry.bindTileEntitySpecialRenderer(LOTRTileEntityMobSpawner.class, new RenderMobSpawnerSimple());
	}
	
	public static void preInit() {
		if(ASMConfig.halfTrollAdditions) {
			RenderingRegistry.registerEntityRenderingHandler(LOTREntityHalfTrollThrower.class, new LOTRRenderHalfTrollThrower());
		}
	}
	
	public static void openGuiHiredAdvSettings(HiredAdvInfoPacket packet, LOTREntityNPC npc) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiHiredAdvSettings(npc, packet));
	}
	
	public static void openGuiGuardmodeHorn(ItemStack item) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiGuardmodeHornSettings(item));
	}
}
