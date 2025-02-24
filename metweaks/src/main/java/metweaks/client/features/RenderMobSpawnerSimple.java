package metweaks.client.features;

import java.lang.reflect.Method;

import lotr.client.render.tileentity.LOTRTileEntityMobSpawnerRenderer;
import metweaks.MeTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;

public class RenderMobSpawnerSimple extends LOTRTileEntityMobSpawnerRenderer {
	@SuppressWarnings("rawtypes")
	Class neiClientConfig;
	Method neiHidden;
	boolean neiEnabled;
	
	@SuppressWarnings("unchecked")
	public boolean isNEIEnabled() {
		try {
			if(neiHidden == null) {
				neiClientConfig = Class.forName("codechicken.nei.NEIClientConfig");
				neiHidden = neiClientConfig.getMethod("isHidden");
			}
			return !(boolean) neiHidden.invoke(null);
		} catch(Exception e) {
			return false;
		}
	}
	
	int nextNEIcheck;
	
	@Override
	public void renderInvMobSpawner(int i) {
		Minecraft mc = Minecraft.getMinecraft();
		if(MeTweaks.nei && nextNEIcheck < mc.renderViewEntity.ticksExisted) {
			neiEnabled = isNEIEnabled() && mc.currentScreen instanceof GuiContainer;
			nextNEIcheck = mc.renderViewEntity.ticksExisted+1;
			
		}
		
		
		
		if(neiEnabled || mc.currentScreen instanceof GuiContainerCreative)
			return;
			
		super.renderInvMobSpawner(i);
		
	}
}
