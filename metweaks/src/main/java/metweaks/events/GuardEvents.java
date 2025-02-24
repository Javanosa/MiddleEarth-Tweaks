package metweaks.events;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.client.gui.LOTRGuiHiredInteract;
import lotr.client.gui.LOTRGuiHiredNPC;
import lotr.client.gui.LOTRGuiHornSelect;
import lotr.client.gui.LOTRGuiNPCInteract;
import lotr.common.entity.npc.LOTREntityNPC;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.client.features.GuardsHomeSetup;
import metweaks.client.features.NpcMarking;
import metweaks.client.gui.GuiHiredTransfer;
import metweaks.client.gui.unitoverview.GuiUnitOverview;
import metweaks.network.GuardmodeHornPacket;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class GuardEvents {
	public static int guardHomeEntityID = -1;
	public static ChunkCoordinates guardHomePos;
	public static int guardHomePosTicks;
	public static long nextInteract;
	
	public static GuiScreen nextToShow;
	public static GuiScreen tempGuiUnitOverview;
	public static long timeout = Long.MAX_VALUE;
	public static boolean emptyPassed;
	public static int markNpcID = -1;
	public static int markNpcTicks;
	
	public GuardEvents() {
		MinecraftForge.EVENT_BUS.register(this);
		
	}
	
	static boolean textureChanged;
	
	private static void changeSelectHornTex(ResourceLocation resource) {
		
		try {
			
			Field field_guiTexture = ReflectionHelper.findField(LOTRGuiHornSelect.class, "guiTexture");
			int modifier = field_guiTexture.getModifiers();
			
		    
		    	Field fieldMod = Field.class.getDeclaredField("modifiers");
			    fieldMod.setAccessible(true);
			    fieldMod.setInt(field_guiTexture, modifier & ~Modifier.FINAL);
		    

			field_guiTexture.set(null, resource);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static Field field_hornselect_ySize;
	
	
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void onInitGui(InitGuiEvent.Pre event) {
		if(MeTweaksConfig.toggleGuardModeHorn && event.gui instanceof LOTRGuiHornSelect) {
			
				int ySize = 113+35;
				int left = (event.gui.width - 176) / 2;
			    int top = (event.gui.height - ySize) / 2;
			    
			    if(field_hornselect_ySize == null)
			    	field_hornselect_ySize = ReflectionHelper.findField(LOTRGuiHornSelect.class, "ySize");
			    
			    try {
			    	field_hornselect_ySize.setInt(event.gui, ySize);
			    }
			    catch (Exception e) {
					e.printStackTrace();
				}
			    
			    event.buttonList.clear();
			    event.gui.initGui();
				event.buttonList.add(new GuiButton(4, left + 40, top + 110, 120, 20, StatCollector.translateToLocal("lotr.gui.hornSelect.guardMode")));
				
				
				if(!textureChanged) {
					textureChanged = true;
					// currently its not changeable again later :(
					changeSelectHornTex(new ResourceLocation(MeTweaks.MODID+":gui/select_guard_horn.png")); // memory leak of texture danger
				}
				event.setCanceled(true);
				
		}
		
		if(ASMConfig.hiredTransfer && event.gui instanceof LOTRGuiHiredInteract) {
			event.buttonList.clear();
		    event.gui.initGui();
			event.buttonList.add(new GuiButton(3, event.gui.width / 2 - 33, event.gui.height / 5 * 3 + 50, 66, 20, StatCollector.translateToLocal("gui.transfer.btn")));
			event.setCanceled(true);
		}
	}
	
	static Field field_theEntity = ReflectionHelper.findField(LOTRGuiNPCInteract.class, "theEntity");
	
	@SubscribeEvent
	public void onActionPerformed(ActionPerformedEvent.Pre event) {
		if(MeTweaksConfig.toggleGuardModeHorn && event.gui instanceof LOTRGuiHornSelect) {
			if(event.button.id == 4) {
				// toggle guardmode horn button
				// send packet to server to init name
				NetworkHandler.networkWrapper.sendToServer(new GuardmodeHornPacket(false, false, 0, 0));
				ClientEvents.actionBar(StatCollector.translateToLocal("gui.guardmodehorn.tip"), false, 80, null, true); // "[Rightclick + Sneak] for settings"
				
			}
		}
		
		if(ASMConfig.hiredTransfer && event.gui instanceof LOTRGuiHiredInteract) {
			if(event.button.id == 3) {
				LOTREntityNPC npc = null;
				try {
					npc = (LOTREntityNPC) field_theEntity.get(event.gui);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if(npc != null) {
					Minecraft.getMinecraft().displayGuiScreen(new GuiHiredTransfer(npc));
					
				}
			}
		}
	}
	
	public static void handleClientTick(ClientTickEvent event) {
		if(event.phase == Phase.END) {
			if(guardHomePosTicks > 0 ) {
				guardHomePosTicks--;
				
				if(guardHomePosTicks <= 0) {
					guardHomePos = null;
				}
			}
			
			if(markNpcTicks > 0) {
				NpcMarking.tick();
			}
			
			if(nextToShow != null) {
				
				GuiScreen show = nextToShow;
				nextToShow = null; // avoid loop on crash
				
				
				// this could lead to issues regarding out of game screen but well
				if(Minecraft.getMinecraft().thePlayer != null)
					Minecraft.getMinecraft().displayGuiScreen(show);
				
				
			}
			
			if(tempGuiUnitOverview != null) {
				
				GuiScreen screen = Minecraft.getMinecraft().currentScreen;
				if(MeTweaksConfig.debug >= 2)
					System.out.println(screen);
				boolean isEmpty = screen == null;
				boolean isPassScreen = screen instanceof LOTRGuiHiredNPC || (!isEmpty && timeout < System.currentTimeMillis());
				if(isEmpty || isPassScreen) {
					
					Minecraft.getMinecraft().displayGuiScreen(tempGuiUnitOverview);
					
					if(!isEmpty && emptyPassed) {
						// reset and ask for data
						if(MeTweaks.remotePresent)
							NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(GuiUnitOverview.unitInvOpenID, GuardsOverviewActionPacket.ACTION_SEND_DATA));
						tempGuiUnitOverview = null;
						emptyPassed = false;
						timeout = Long.MAX_VALUE;
						GuiUnitOverview.unitInvOpenID = -1;
						
					}
					
					if(isEmpty) {
						emptyPassed = true; // first find null
						timeout = 500L+System.currentTimeMillis(); // stop searching after
					}
					
				}
			}
		}
	}
	
	
	
	
	public static void handleRenderWorldLast(RenderWorldLastEvent event) {
		if(markNpcID != -1) {
			NpcMarking.render(event);
		}
		
		if(guardHomePosTicks > 0 && guardHomePos != null) {
			GuardsHomeSetup.render();
		}
	}
	
	@SubscribeEvent
	public void onInteractEvent(PlayerInteractEvent event) {
		if(event.world.isRemote) {
			if(markNpcID != -1) {
				NpcMarking.reset(event);
			}
			// when clicking on an entity it needs to cancel, so air = cancel
			if(guardHomeEntityID != -1) {
				GuardsHomeSetup.handleClick(event);
			}
		}
	}
}
