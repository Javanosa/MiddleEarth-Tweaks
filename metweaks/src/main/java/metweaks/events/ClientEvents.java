package metweaks.events;

import java.lang.reflect.Field;
import java.util.List;
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
import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.LOTRMod;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.MeTweaksConfig;
import metweaks.block.VerticalSlab;
import metweaks.client.healthbar.RenderHealthBar;
import metweaks.client.Keys;
import metweaks.client.LOTRGuiElements;
import metweaks.client.gui.GuiTOSNotice;
import metweaks.client.gui.GuiTabListOverlay;
import metweaks.client.gui.GuiUnitCountsOverlay;
import metweaks.client.gui.unitoverview.GuiUnitOverview;
import metweaks.core.HooksNetwork;
import metweaks.features.Getters;
import metweaks.features.ItemUseHandler;
import metweaks.features.LOTRDateCache;
import metweaks.features.PlayerDeathLogger;
import metweaks.features.PlayerRidingTracker;
import metweaks.features.ThrowableSpearState;
import metweaks.features.UnitMonitor;
import metweaks.network.GuardsOverviewActionPacket;
import metweaks.network.NetworkHandler;
import metweaks.network.SyncThrowableSpearPacket;
import metweaks.network.SyncUnitTrackingModePacket;
import metweaks.network.SyncVerticalModePacket;
import metweaks.network.SyncedConfig;
import metweaks.proxy.UpdateCheck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemSlab;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import static metweaks.client.healthbar.HealthBarConfig.*;

public class ClientEvents {
	
	
	// states / temp
	private static boolean sendVerticalStateJoin;
	private static int connectedTick = -1;
	
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
		connectedTick = 0;
		//if(MeTweaksConfig.verticalSlabs && VerticalSlab.clientVertical)
			sendVerticalStateJoin = true;
		
		nextCanSeeCacheClear = 0;
		
		if(!event.isLocal) {
			MeTweaksConfig.conquestDecay = 3600; // reset 
		}
		
		
	}
	
	// revert changes from custom server config
	@SubscribeEvent
	public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
		connectedTick = -1;
		MeTweaksConfig.checkPotionID();
		MeTweaksConfig.mirrorVerticalSlabs = SyncedConfig.prevMirrorVerticalSlabs;
		// reset actionbar
		stateTime = 0;
		
		boolean saveConf = false;
		
		// this isnt good :(
		if(prevRenderHealth != renderHealth) {
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_HUD, "Enable Health Bar", true).set(renderHealth);
			saveConf = true;
			prevRenderHealth = renderHealth;
		}
		
		if(ThrowableSpearState.prevClientThrowable != ThrowableSpearState.clientThrowable) {
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_MISC, "Throwable Spears", true).set(ThrowableSpearState.clientThrowable);
			saveConf = true;
			ThrowableSpearState.prevClientThrowable = ThrowableSpearState.clientThrowable;
		}
		
		if(MeTweaksConfig.unitTracking && MeTweaks.lotr && UnitMonitor.prevClientUnitTracking != UnitMonitor.clientUnitTracking) {
			MeTweaksConfig.config.get(MeTweaksConfig.CATEGORY_MISC, "Unit Tracking State", 0, "0 = Off, 1 = Far, 2 = Near", 0, 2).set(UnitMonitor.clientUnitTracking);
			saveConf = true;
			UnitMonitor.prevClientUnitTracking = UnitMonitor.clientUnitTracking;
			
		}
		
		if(saveConf) MeTweaksConfig.config.save(); // we rely on mark dirty?
		
		if(MeTweaks.lotr && MeTweaksConfig.fixMountsRunningAway) {
			PlayerRidingTracker.map.clear();
			
		}
		
		if(ASMConfig.logPlayerDeathLocations) {
			logDisconnect();
		}
		
		
	}
	
	public static void logDisconnect() {
		if(MinecraftServer.getServer() != null && MinecraftServer.getServer().isServerRunning()) {
			return;
		}
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if(player == null) return;
		
		FMLLog.getLogger().info(player.getCommandSenderName()+" logget out in "+player.worldObj.provider.getDimensionName()+" at "+MathHelper.floor_double(player.posX)+" "+MathHelper.floor_double(player.posY)+" "+MathHelper.floor_double(player.posZ));
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
		
		if(ASMConfig.throwableSpearToggle && Keyboard.isKeyDown(Keys.toggleThrowableSpear.getKeyCode()) && !f3) {
			Minecraft mc = Minecraft.getMinecraft();
			
				
				boolean state = ThrowableSpearState.clientThrowable = ! ThrowableSpearState.clientThrowable;
				ThrowableSpearState.setThrowableMode(mc.thePlayer, state);
				
				
				if(!mc.isSingleplayer()) {
					if(MeTweaksConfig.debug >= 2)
						FMLLog.getLogger().info("send status to server: throwablespear="+state);
					
					NetworkHandler.networkWrapper.sendToServer(new SyncThrowableSpearPacket(state));
					
				}
				
				actionBar(StatCollector.translateToLocal("metweaks.throwablespear.display"), state, 35, null, false); // "Throwable Spears"
			
		}
		
		if(Keys.toggleHealth.isPressed() && !f3) {
			
			
			
			
			renderHealth = !renderHealth;
			actionBar(StatCollector.translateToLocal("metweaks.healthbar.display"), renderHealth, 30, null, false); // "HealthBar"
		}
		
		if(MeTweaks.lotr && Keys.openUnitOverview.isPressed() && !f3) {
			if(Keyboard.isKeyDown(Keyboard.KEY_F7)) {
				
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
	
	public static boolean openTosHintGUI;
	private static boolean showTOSNotice = true;
	
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if(MeTweaks.lotr) {
			if(ASMConfig.lotrHolidays == 1) {
				LOTRDateCache.updateDates();
			}
			
			GuardEvents.handleClientTick(event);
			
			if(MeTweaksConfig.fixMountsRunningAway) {
				PlayerRidingTracker.processEvent(false);
			}
			
			if(GuiUnitCountsOverlay.unitcountsEnabled && event.phase == Phase.START) {
				GuiUnitCountsOverlay.tick();
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
		
		if(connectedTick != -1 && event.phase == Phase.END && Minecraft.getMinecraft().thePlayer != null) {
			connectedTick++;

			
			// Update Message: TOS or General
			if(connectedTick == 5) { // 0.25 seconds
				
				if(MeTweaksConfig.updateCheck && UpdateCheck.canNotifyClient()) {
					boolean tos = connectedToTOS();
					if(UpdateCheck.hasUpdate(tos)) {
						Minecraft.getMinecraft().thePlayer.addChatMessage(UpdateCheck.notifyUpdate(tos));
					}
				}
			}
			// TOS Info
			if(connectedTick == 400) { // 20s
				if(showTOSNotice && MeTweaksConfig.tosNotice >= 2 && connectedToTOS()) {
					
					
					showTOSNoticeChat();
					showTOSNotice = false;
				}
			}
			
			if(connectedTick >= 400) {
				connectedTick = -1;
			}
		}
		
		if(openTosHintGUI) {
			openTosHintGUI = false;
			Minecraft.getMinecraft().displayGuiScreen(new GuiTOSNotice());
			
		}
		
		if(ASMConfig.logPlayerDeathLocations && event.phase == Phase.END) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if(player != null) {
				if(player.deathTime == 1)
					PlayerDeathLogger.logDeath(player);
			}
			
		}
		
		
	}
	
	private void showTOSNoticeChat() {
		ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cShowTOSNotice");
		HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Click Here"));
		ChatComponentText title = new ChatComponentText("Tip: MiddleEarth Tweaks requires adjusted settings to work properly on TOS ");
		title.getChatStyle().setColor(EnumChatFormatting.LIGHT_PURPLE).setChatClickEvent(click).setChatHoverEvent(hover);;
		
		
		
		
		ChatComponentText btn = new ChatComponentText("[click to resolve]");
		btn.getChatStyle().setColor(EnumChatFormatting.WHITE).setUnderlined(true);
		
		
		/*ChatComponentText msg = new ChatComponentText("MiddleEarth Tweaks requires adjusted settings to work properly on TOS ");
		msg.getChatStyle().setColor(EnumChatFormatting.RED);
        title.appendSibling(msg);*/
        title.appendSibling(btn);
		
		Minecraft.getMinecraft().thePlayer.addChatMessage(title);
	}
	
	private boolean connectedToTOS() {
		ServerData serverData = Minecraft.getMinecraft().func_147104_D();
		if(serverData != null && !Minecraft.getMinecraft().isSingleplayer()) {
			return "lotr.g.akliz.net".equals(serverData.serverIP);
		}
		return false;
	}
	
	
	

	@SubscribeEvent
	public void onGameOverlay(RenderGameOverlayEvent.Chat event) {
		
		if(MeTweaksConfig.showTips && sendKeyBindTip) {
			actionBar(StatCollector.translateToLocalFormatted("metweaks.healthbar.tip", GameSettings.getKeyDisplayString(Keys.toggleHealth.getKeyCode())), false, 80, null, true);
			sendKeyBindTip = false;
		}
		
		if(MeTweaks.lotr && GuiUnitCountsOverlay.unitcountsEnabled)
			GuiUnitCountsOverlay.render(event.resolution);
		
		
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
			
			if(ASMConfig.throwableSpearToggle && !ThrowableSpearState.clientThrowable) {
				ThrowableSpearState.setThrowableMode(Minecraft.getMinecraft().thePlayer, false);
				
				if(!Minecraft.getMinecraft().isSingleplayer()) {
					if(MeTweaksConfig.debug >= 2)
						FMLLog.getLogger().info("send throwablespear status to server on join");
					NetworkHandler.networkWrapper.sendToServer(new SyncThrowableSpearPacket(false));
				}
			}
			
			if(MeTweaksConfig.unitTracking && MeTweaks.lotr && UnitMonitor.clientUnitTracking != 0) {
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
	
	static Field field_enableTicks;
	
	@SubscribeEvent
	public void onInitGui(InitGuiEvent.Pre event) {
		if(ASMConfig.fixGuiGameOver && event.gui instanceof GuiGameOver) {
			if(field_enableTicks == null) {
				field_enableTicks = ReflectionHelper.findField(GuiGameOver.class, "field_146347_a");
			}
			try {
				field_enableTicks.setInt(event.gui, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(MeTweaks.lotr) {
			GuardEvents.handleInitGui(event);
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if(ASMConfig.tabListChanges && event.type == ElementType.PLAYER_LIST) {
			GuiTabListOverlay.instance.renderPlayerlist(event.resolution.getScaledWidth());
			event.setCanceled(true);
		}
		
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Post event) {
		if(ASMConfig.fixOpenLinkGUI && event.gui.getClass() == GuiConfirmOpenLink.class) {
			List<GuiButton> buttons = event.buttonList;
			int left = (event.gui.width / 2) - buttons.size() * 52;
			for(GuiButton btn : buttons) {
				btn.xPosition = left;
				left += 105;
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerAttack(AttackEntityEvent event) {
		if(ASMConfig.smoothEntityMovement > 1 && HooksNetwork.specialDebugMode >= 6 && event.target instanceof EntityLivingBase) {
			Getters.cmTick((EntityLivingBase) event.target, ASMConfig.smoothEntityMovement - 1);
		}
	}
	
}
