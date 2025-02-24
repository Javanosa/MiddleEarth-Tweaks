package metweaks.events;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.map.TIntByteMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntByteHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import lotr.client.LOTRSpeechClient;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRMod;
import lotr.common.LOTRReflection;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTREntityNPCRideable;
import lotr.common.entity.npc.LOTREntityWarg;
import lotr.common.entity.npc.LOTRNPCMount;
import lotr.common.fellowship.LOTRFellowshipClient;
import lotr.common.item.LOTRItemMountArmor;
import lotr.common.item.LOTRWeaponStats;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksAPI;
import metweaks.MeTweaksConfig;
import metweaks.block.VerticalSlab;
import metweaks.client.healthbar.RenderHealthBar;
import metweaks.client.Keys;
import metweaks.client.LOTRGuiElements;
import metweaks.client.gui.unitoverview.GuiUnitOverview;
import metweaks.core.Hooks;
import metweaks.features.ItemUseHandler;
import metweaks.features.ModifierHandler;
import metweaks.features.PlayerRidingTracker;
import metweaks.features.UnitMonitor;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.NetworkHandler;
import metweaks.network.SyncUnitTrackingModePacket;
import metweaks.network.SyncVerticalModePacket;
import metweaks.network.SyncedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import static metweaks.client.healthbar.HealthBarConfig.*;

public class ClientEvents {
	
	
	// states / temp
	private static boolean sendVerticalStateJoin;
	private static boolean sendKeyBindTip = true;
	public static int nextCanSeeCacheClear; // private?
	
	private static String translateGuardHornTip;
	private static String translateVerticalSlabTip;
	private static String translateStateOn;
	private static String translateStateOff;
	
	public static String translateSpeed;
	public static String translateJump;
	
	//private static String translateMPS;
	//private static char translateMeters;
	//private static String translateArmor;
	
	
	
	public ClientEvents() {
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		
	}

	
	
	
	
	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent.Post evt) {
		translateGuardHornTip = null;
		translateVerticalSlabTip = null;
		translateStateOn = null;
		translateStateOff = null;
		
		translateSpeed = null;
		translateJump = null;
		
		//if(MeTweaksConfig.verticalSlabs)
		//	VerticalSlab.generateLangs();
		
		//translateMPS = null;
		//translateMeters = 0;
		//translateArmor = null;
	}
	
	@SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
		
		if(MeTweaksConfig.verticalSlabs && event.itemStack.getItem() instanceof ItemSlab) {
			if(translateVerticalSlabTip == null) translateVerticalSlabTip = EnumChatFormatting.GRAY+StatCollector.translateToLocalFormatted("metweaks.verticalslabs.tooltip", GameSettings.getKeyDisplayString(Keys.toggleVertical.getKeyCode()));
			event.toolTip.add(1, translateVerticalSlabTip); // EnumChatFormatting.GRAY+"Press "+GameSettings.getKeyDisplayString(Keys.toggleVertical.getKeyCode())+" for vertical"
		}
			
		if(MeTweaksConfig.toggleGuardModeHorn && MeTweaks.lotr && event.itemStack.getItem() == LOTRMod.commandHorn) {
			
			int meta = event.itemStack.getItemDamage();
			if(meta == ItemUseHandler.TURN_ON_GUARDMODE || meta == ItemUseHandler.TURN_OFF_GUARDMODE) {
				if(translateGuardHornTip == null) translateGuardHornTip = EnumChatFormatting.GOLD+StatCollector.translateToLocal("gui.guardmodehorn.tip");
				event.toolTip.add(1, translateGuardHornTip); // EnumChatFormatting.GOLD+"[Rightclick + Sneak] for settings"
			}
				
		}
			
	}
	
	@SubscribeEvent
	public void onClientConnect(ClientConnectedToServerEvent event) {
		//if(MeTweaksConfig.verticalSlabs && VerticalSlab.clientVertical)
			sendVerticalStateJoin = true;
		
		nextCanSeeCacheClear = 0;
		
		
		
		
	}
	
	// revert changes from custom server config
	@SubscribeEvent
	public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
		MeTweaksConfig.checkPotionID();
		MeTweaksConfig.mirrorVerticalSlabs = SyncedConfig.prevMirrorVerticalSlabs;
		// reset actionbar
		stateTime = 0;
		
		// this isnt good :(
		if(prevRenderHealth != renderHealth) {
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_HUD, "Enable Health Bar", true).set(renderHealth);
			MeTweaksConfig.config.save(); // we rely on mark dirty?
			
			
			prevRenderHealth = renderHealth;
		}
		
		if(MeTweaks.lotr && MeTweaksConfig.fixMountsRunningAway) {
			PlayerRidingTracker.map.clear();
			
		}
	}
	
	@SubscribeEvent
	public void KeyInputEvent(InputEvent.KeyInputEvent event) {
		
		
		
		boolean f3 = Keyboard.isKeyDown(Keyboard.KEY_F3);
		
		if(MeTweaksConfig.verticalSlabs && Keyboard.isKeyDown(Keys.toggleVertical.getKeyCode()) && !f3) {
			Minecraft mc = Minecraft.getMinecraft();
			
				
				boolean state = VerticalSlab.clientVertical = ! VerticalSlab.clientVertical;
				VerticalSlab.setVerticalMode(mc.thePlayer, state);
				
				
				if(!mc.isSingleplayer()) {
					if(MeTweaksConfig.debug >= 2)
						FMLLog.getLogger().info("send status to server: vertical="+state);
					
					NetworkHandler.networkWrapper.sendToServer(new SyncVerticalModePacket(state));
					
				}
				
				actionBar(StatCollector.translateToLocal("metweaks.verticalslabs.display"), state, 35, null, false); // "Vertical Slabs"
			
		}
		
		if(Keys.toggleHealth.isPressed() && !f3) {
			
			renderHealth = !renderHealth;
			actionBar(StatCollector.translateToLocal("metweaks.healthbar.display"), renderHealth, 30, null, false); // "HealthBar"
		}
		
		if(MeTweaks.lotr && Keys.openUnitOverview.isPressed() && !f3) {
			if(Keyboard.isKeyDown(Keyboard.KEY_F7)/* || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)*/) {
				
				Minecraft mc = Minecraft.getMinecraft();
				int state = ++UnitMonitor.clientUnitTracking;
				if(state > 2) {
					UnitMonitor.clientUnitTracking = state = 0;
				}
				
				UnitMonitor.setUnitTrackingMode(mc.thePlayer, state);
				
				if(!mc.isSingleplayer()) {
					if(MeTweaksConfig.debug >= 2)
						FMLLog.getLogger().info("send status to server: unittracking="+state);
					NetworkHandler.networkWrapper.sendToServer(new SyncUnitTrackingModePacket(state));
				}
				
				actionBar(StatCollector.translateToLocal("metweaks.unittracking.display"), state != 0, 50, 
						state == 2 ? StatCollector.translateToLocal("metweaks.unittracking.near") : 
						state == 1 ? StatCollector.translateToLocal("options.renderDistance.far") : 
						null, 
						false); // "Vertical Slabs"
			}
			else if(ASMConfig.unitOverview) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiUnitOverview());
				if(MeTweaks.remotePresent)
					NetworkHandler.networkWrapper.sendToServer(new GuardsOverviewActionPacket(-1, GuardsOverviewActionPacket.ACTION_SEND_DATA));
			}
		}
	}
	
	private static int stateTime;
	private static boolean stateEnabled;
	private static String stateValue;
	private static String state;
	private static boolean useStateValue;
	
	public static void actionBar(String message, boolean stateOnOff, int time, String preciseState, boolean disableState) {
		state = message;
		stateEnabled = stateOnOff;
		stateTime = time;
		stateValue = preciseState;
		useStateValue = !disableState;
	}
	
	private static Field field_remainingHighlightTicks;
	private static int toolTipTime;
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if(MeTweaks.lotr) {
			GuardEvents.handleClientTick(event);
			
			if(MeTweaksConfig.fixMountsRunningAway) {
				PlayerRidingTracker.processEvent(false);
			}
		}
		
		
		
		if(stateTime > 0 && event.phase == Phase.END) {
			
			if(field_remainingHighlightTicks == null)
				field_remainingHighlightTicks = ReflectionHelper.findField(GuiIngame.class, new String[] {"remainingHighlightTicks", "field_92017_k", "r"});
			try {
				
					toolTipTime = field_remainingHighlightTicks.getInt(Minecraft.getMinecraft().ingameGUI);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			stateTime--;
		}
			 
		
		if(nextCanSeeCacheClear > 0 && event.phase == Phase.END) {
			nextCanSeeCacheClear--;
		}
		
		
		
	}
	
	
	

	@SubscribeEvent
	public void onGameOverlay(RenderGameOverlayEvent.Chat event) {
		
		if(MeTweaksConfig.showTips && sendKeyBindTip) {
			actionBar(StatCollector.translateToLocalFormatted("metweaks.healthbar.tip", GameSettings.getKeyDisplayString(Keys.toggleHealth.getKeyCode())), false, 80, null, true);
			sendKeyBindTip = false;
		}
		
		if(stateTime > 0) {
			
			
			int alpha = (int) ((stateTime - event.partialTicks) * 256.0F / 10.0F);
			
			if (alpha > 255) {
				alpha = 255;
			}
			if (alpha <= 0) {
				return;
			}
			
			int top = event.resolution.getScaledHeight() - 46 + LOTRGuiElements.metweaksMessageOffsetY; // 60
			
			if(toolTipTime > 0) {
				
					
				
				top -= (Math.min(7, toolTipTime - event.partialTicks)) * 2;
				
			}
			
			
			int left = event.resolution.getScaledWidth() / 2;
			
			
			Minecraft mc = Minecraft.getMinecraft();
			
			if(mc.playerController.shouldDrawHUD()) {
				top -= 14;
			}
			
			GL11.glPushMatrix();
			GL11.glTranslatef(left, top, 0);
			
			if(translateStateOn == null) translateStateOn = StatCollector.translateToLocal("options.on");
			if(translateStateOff == null) translateStateOff = StatCollector.translateToLocal("options.off");
			
			
			String value = stateEnabled ? translateStateOn : translateStateOff; // "ON" : "OFF";
			
			
			if(stateValue != null) {
				value = stateValue;
			}
			
			
			int lenState = mc.fontRenderer.getStringWidth(state);
			
			int lenValue = mc.fontRenderer.getStringWidth(value);
			
			int padding = 4;
			int height = 7;
			int total = lenState + padding + (useStateValue ? padding + lenValue : 0);
			
			int totalHalf = total / 2;
			
			
			event.resolution.getScaledWidth();
			Tessellator tessellator = Tessellator.instance;
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
			
			tessellator.startDrawingQuads();
			// state background
			tessellator.setColorRGBA(0, 0, 0, (int) ((60 / 255.0f) * alpha));
			tessellator.addVertex(-totalHalf - padding, -padding, 0);
			tessellator.addVertex(-totalHalf - padding, height + padding, 0);
			tessellator.addVertex(-totalHalf + lenState + padding, height + padding, 0);
			tessellator.addVertex(-totalHalf + lenState + padding, -padding, 0);
			
			// value background
			if(useStateValue) {
				if(stateEnabled)
					tessellator.setColorRGBA(11, 105, 0, alpha);
				else
					tessellator.setColorRGBA(41, 41, 41, alpha);
				
				tessellator.addVertex(-totalHalf + lenState + padding, -padding, 0);
				tessellator.addVertex(-totalHalf + lenState + padding, height + padding, 0);
				tessellator.addVertex(totalHalf + padding, height + padding, 0);
				tessellator.addVertex(totalHalf + padding, -padding, 0);
				
			}
			tessellator.draw();
			
			if(alpha < 10)
				alpha = 10;
			
			int color = 0xffffff + (alpha << 24);
			//int color = 0x00ffffff & -alpha;
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			mc.fontRenderer.drawString(state, -totalHalf, 0, color);
			if(useStateValue)
				mc.fontRenderer.drawString(value, -totalHalf + lenState + padding + padding, 0, color);
			GL11.glPopMatrix();
		}
	}
	
	
	
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if(MeTweaks.lotr)
			GuardEvents.handleRenderWorldLast(event);
		
		if(sendVerticalStateJoin) {
			sendVerticalStateJoin = false;
			if(MeTweaksConfig.verticalSlabs && VerticalSlab.clientVertical) {
				VerticalSlab.setVerticalMode(Minecraft.getMinecraft().thePlayer, VerticalSlab.clientVertical);
				
				if(!Minecraft.getMinecraft().isSingleplayer()) {
					if(MeTweaksConfig.debug >= 2)
						FMLLog.getLogger().info("send status to server join: vertical="+VerticalSlab.clientVertical);
					NetworkHandler.networkWrapper.sendToServer(new SyncVerticalModePacket(VerticalSlab.clientVertical));
				}
			}
			
			if(MeTweaksConfig.unitTracking && UnitMonitor.clientUnitTracking != 0) {
				UnitMonitor.setUnitTrackingMode(Minecraft.getMinecraft().thePlayer, UnitMonitor.clientUnitTracking);
				
				if(!Minecraft.getMinecraft().isSingleplayer()) {
					if(MeTweaksConfig.debug >= 2)
						FMLLog.getLogger().info("send status to server join: unittracking="+UnitMonitor.clientUnitTracking);
					NetworkHandler.networkWrapper.sendToServer(new SyncUnitTrackingModePacket(UnitMonitor.clientUnitTracking));
				}
			}
		}
		

		if(renderHealth && Minecraft.isGuiEnabled()/* && Minecraft.getMinecraft().entityRenderer.debugViewDirection == 0*/) {
			RenderHealthBar.handleRenderWorldLast(event);
		}
		
		
	}
	
	
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.modID.equals(MeTweaks.MODID)) {
			MeTweaksConfig.load(); 
			if(MeTweaksConfig.debug >= 1)
				FMLLog.getLogger().info(MeTweaks.MODNAME + " config changed");
		}
	}
	
}
