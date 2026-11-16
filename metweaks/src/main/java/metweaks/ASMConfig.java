package metweaks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASMConfig {
	public static boolean 
		verticalSlabPlacement,
		conquestDecay,
		horseinv,
		allyConquestSpawns,
		allyKillReduceConquest,
		fencegatePlacement,
		trapdoorPlacement,
		barkblocks,
		syncedconfig,
		guardsSyncSettings,
		guardsWanderRange,
		guardsAdvancedSettings,
		lotrHudPositions,
		fangornTreePenaltyThreshold,
		
		fixTurkishLang,
		
		initMountspeeds,
		disableStructures,
		conquestClearCommand,
		entityTpCommand,
		entityKillCommand,
		entityEffectCommand,
		entityStackCommand,
		giveManyCommand,
		fixSluggishHaradAttack,
		fixQuestofferSync,
		foodConsumeDurations,
		npcExplosionProtection,
		improveBlockBreakSpeeds,
		disableTinCopperSilverGen,
		guardsEquipRanged,
		guardsEquipAllowTermites,
		
		aiRangedImprovements,
		guardsEquipRemoveBroken,
		randomEnchRangedNpcWeapons,
		hiredTransfer,
		unitOverview, 
		unitOverviewHideMeleeForRangedOnly,
		adventureModePatches,
		fixTeleportToHiringPlayer,
		patchDraughtUse,
		preloadClasses,
		projectilePatches,
		projectilePatchesReqClient,
		alwaysAbsorbPiercingProjectiles,
		allyProjectilesPassAllies,
		hiredsConquerForTheirFaction,
		halfTrollAdditions,
		entityTrackerPatches,
		fixGuiGameOver,
		versionCompat,
		tabListChanges,
		tabListShowMaxPlayers,
		tabListSort,
		moreEvents,
		projectileFlagsHiredsOnly,
		fixServerJoinFreeze,
		etFuturumCompatibility,
		farView,
		lotrEventControl,
		renderPerformance,
		checkOptifine,
		bannerProtectionPatches,
		permNpcAllowDestroyNonLiving,
		logPlayerDeathLocations,
		logHiredTransfer,
		mountedBannerBearers,
		moreMountedBannerBearers,
		explosionPhysics,
		captainsAttack,
		throwableSpearToggle,
		fellowshipFFPatches,
		fixFireworkLag,
		fixInvisibility,
		hireableEnts,
		hearthFireDoesntSpread,
		interactReachFix, 
		interactReachFeedback,
		unlockEnemyWaypoints,
		fixOpenLinkGUI,
		smoothHeadYawMovement,
		noTCPDelay,
		earlyPacketProcessing,
		prioritizePlayerTicks,
		debugModeCommand,
		experimentalFeatures,
		mutePlayerDetailsApiSpam,
		bannerProtectionVerbose
		
		
		
		
		
		
	;
	
	public static int tauredainPoisonDartChance, 
		guardsLockCategoryRanged, 
		guardsAttackDelayFactorRanged,
		guardsDynamicAttackDelayRanged,
		guardsEquipDurabilityMode,
		guardsEquipDurabilityFactor,
		draughtStackEffectsIncrease,
		draughtMaxStacksize,
		stewMaxStacksize,
		passMissingDiscriminator,
		npcDamageFactorProjectiles,
		npcCriticalProjectileChance,
		revengeCamouflageTargetChance,
		forceFCV,
		lotrHolidays,
		interactReachBuffer,
		interactReachBufferNoSight,
		armorMaintainProtectionPercent,
		entityPosRotationThreshold,
		smoothEntityMovement
		;
	
	public static Map<String, String> read(File file) {
		long starttime = System.currentTimeMillis();
		Map<String, String> properties = new HashMap<>();
		try{
			if(!file.exists()) {
	            file.createNewFile();
	        }
			
			BufferedReader reader = null;
			String line;
			try {
				reader = new BufferedReader(new FileReader(file), 8192);
				
				while((line = reader.readLine()) != null) {
					
					if(line.isEmpty()) continue;
					
					char start = line.charAt(0);
					if(start == '#' || start == ' ') {
						continue;
					}
					
					int index = line.indexOf('=');
					// bigger than 0 so key cannot be empty, hopefully length() is fast
					if(index > 0 && index < line.length()) {
						//				key			value
						properties.put(line.substring(0, index), line.substring(index+1));
					}
					
				}
				reader.close();
			}
			catch (IOException e) {
				if(reader != null)
					reader.close();
				e.printStackTrace();
			}
        }
		catch (IOException e) {
			e.printStackTrace();
		}
		
		long endtime = System.currentTimeMillis();
		System.out.println("ASMConfig read in "+ (endtime - starttime) +"ms");
		return properties;
	}
	
	static Map<String, String> properties;
	public static Map<String, String> propertiesChange;
	static List<String> content;
	static boolean needWrite;
	
	public static void init() {
		
		File file = new File("config", MeTweaks.MODID+"-ASM.properties");
		properties = read(file);
		if(propertiesChange != null) {
			properties.putAll(propertiesChange);
			needWrite = true;
		}
		
		long starttime2 = System.currentTimeMillis();
		
		content = new ArrayList<>();
		content.add("# ASM Config");
		content.add("# Wiki: https://javanosa.github.io/wiki   Discord: https://discord.gg/maHfVhGaah");
		
		int version = getInt("configversion", 0, true);
		
		verticalSlabPlacement = get("verticalSlabPlacement", 
			"@Inject ItemSlab.onItemUse(ItemStack EntityPlayer World IIIIFFF)Z", 
			"@Inject ItemSlab.func_150936_a(World IIII EntityPlayer ItemStack)Z",
			"@Replace BlockSlab.setBlockBoundsForItemRender()V",
			"@Inject ItemCarvableSlab.onItemUse(ItemStack EntityPlayer World IIIIFFF)Z",
			"@Inject CTMRenderer.renderInventoryBlock(Block II RenderBlocks)V"
		);
		
		conquestDecay = get("conquestDecay", 
			"@Inject LOTRLevelData.save()V",
			"@Inject LOTRLevelData.load()V",
			"@Replace LOTRConquestZone.calcTimeStrReduction(J)F"
		);
		
		horseinv = get("horseinv", 
			"@Replace LOTRReflection.getHorseInv(EntityHorse)AnimalChest"
		);
		
		allyConquestSpawns = get("allyConquestSpawns",
			"@Inject LOTRConquestGrid.getEffectiveConquestStrength(World LOTRConquestZone)F",
			"@Inject LOTRBiomeSpawnList$FactionContainer.getConquestEffectIn(World LOTRConquestZone LOTRFaction)LOTRConquestGrid$ConquestEffective"
		);
				
		allyKillReduceConquest = get("allyKillReduceConquest",
			"@Inject LOTRConquestGrid.doRadialConquest(World LOTRConquestZone EntityPlayer LOTRFaction LOTRFaction FF)F"
		);
		
		fencegatePlacement = get("fencegatePlacement",
			"@Replace BlockFenceGate.canPlaceBlockAt(World III)Z"
		);
		
		trapdoorPlacement = get("trapdoorPlacement",
			"@Add BlockTrapdoor.onBlockPlacedBy(World III EntityLivingBase ItemStack)V",
			"@Replace BlockTrapdoor.onBlockPlaced(World IIIIFFFI)I"
		);
		
		barkblocks = get("barkblocks",
				"@Inject BlockRotatedPillar.onBlockPlaced(World IIIIFFFI)I", 
				"for classes: BlockNewLog, BlockOldLog, LOTRBlockWoodBase, LOTRBlockWoodBeam",
				"@Inject getSubBlocks(Item CreativeTabs List)V",
				"@Add getPickBlock(MovingObjectPosition World III EntityPlayer)ItemStack",
				"@Replace LOTRFABlockMultiWoodBeam.getBlockName(I)String"
		);
		
		syncedconfig = get("syncedconfig",
				"@Add FMLHandshakeMessage$ModIdData.syncedconfig",
				"@Inject FMLHandshakeMessage$ModIdData.fromBytes(ByteBuf)V", 
				"@Inject FMLHandshakeMessage$ModIdData.toBytes(ByteBuf)V", 
				"@Inject FMLHandshakeClientState$WAITINGSERVERCOMPLETE.accept(ChannelHandlerContext FMLHandshakeMessage)FMLHandshakeClientState"
		);
		
		fixTurkishLang = get("fixTurkishLang", 
			"Override locale with code TR to US"
		);
		
		initMountspeeds = get("initMountspeeds", 
			"Custom Mountspeeds wont work if disabled"
		);
		
		disableStructures = get("disableStructures", false);
		
		conquestClearCommand = get("conquestClearCommand");
		
		entityKillCommand = get("entityKillCommand");
		
		entityTpCommand = get("entityTpCommand");
		
		entityEffectCommand = get("entityEffectCommand");
		
		entityStackCommand = get("entityStackCommand");
		
		guardsSyncSettings = get("guardsSyncSettings", "sync the settings of both Mount and Rider", "majority of injections are same as guardsWanderRange");
		
		guardsWanderRange = get("guardsWanderRange",
			"@Inject EntityAITarget.isSuitableTarget(EntityLivingBase Z)Z",
			"@Add LOTRGuiHiredWarrior.sliderWanderRange",
			"@Inject LOTRGuiHiredWarrior.initGui()V",
			"@Inject LOTRGuiHiredWarrior.updateScreen()V",
			"@Add LOTRPacketHiredGui.wanderRange",
			"@Inject LOTRPacketHiredGui.fromBytes(ByteBuf)V",
			"@Inject LOTRPacketHiredGui.toBytes(ByteBuf)V",
			"@Inject LOTRPacketHiredUnitCommand$Handler.onMessage(LOTRPacketHiredUnitCommand MessageContext)IMessage",
			"@Add LOTRHiredNPCInfo.wanderRange",
			"@Inject LOTRHiredNPCInfo.<init>",
			"@Add LOTRHiredNPCInfo.readFromNBT(NBTTagCompound)V",
			"@Add LOTRHiredNPCInfo.writeToNBT(NBTTagCompound)V",
			"@Inject LOTRHiredNPCInfo.sendClientPacket(Z)V",
			"@Inject LOTRHiredNPCInfo.receiveClientPacket(LOTRPacketHiredGui)V",
			"@Replace LOTRHiredNPCInfo.setGuardMode(Z)V",
			"@Inject LOTRHiredNPCInfo.setGuardRange(I)V");
		
		
		// guardsextended
		guardsAdvancedSettings = get("guardsAdvancedSettings", 
				"@Add iHiredWarrior.buttonAdvanced", 
				"@Public LOTRHiredNPCInfo.theEntity",
				"@Add LOTRHiredNPCInfo.ext",
				
				"relies on guardsWanderRange");
		
		lotrHudPositions = get("lotrHudPositions",
				"@Inject LOTRTickHandlerClient.onRenderTick(RenderTickEvent)V",
				"@Inject LOTRModelCompass.render(FF)V",
				"@Inject LOTRRenderPortal.doRender(Entity DDDFF)V");
		
		fangornTreePenaltyThreshold = get("fangornTreePenaltyThreshold",
				"@Inject LOTREventHandler.onBlockBreak()");
		
		fixSluggishHaradAttack = get("fixSluggishHaradAttack",
				"@Inject LOTREntityAIAttackOnCollide.continueExecuting()Z");
		
		fixQuestofferSync = get("fixQuestofferSync");
		
		npcExplosionProtection = get("npcExplosionProtection", "Fix player targeted orcbombs causing damage inside bannered areas");
		
		foodConsumeDurations = get("foodConsumeDurations", "berries have a consume speed bonus due to their little saturation increase etc.",
				"@Replace ItemFood.getMaxItemUseDuration(ItemStack)I");
		
		tauredainPoisonDartChance = getInt("tauredainPoisonDartChance", 10, false, "percent 0 to 100, 0 disables the patch", 
				"@Inject LOTREntityTauredainBlowgunner.attackEntityWithRangedAttack(EntityLivingBase F)V");
		
		improveBlockBreakSpeeds = get("improveBlockBreakSpeeds",
				"@Change Superclass for ItemHoe to ItemTool",
				"@Inject ItemHoe.<init>(ToolMaterial)V", 
				"@Change Superclass for ItemShears to ItemTool",
				"@Inject ItemShears.<init>()V");
		
		disableTinCopperSilverGen = get("disableTinCopperSilverGen", false);
		
		guardsEquipAllowTermites = get("guardsEquipAllowTermites");
		
		guardsEquipRanged = get("guardsEquipRanged",
				"@Add LOTREntityAIRangedAttack.hasInit",
				"@Inject LOTREntityAIRangedAttack.updateTask()V",
				"@Inject LOTRContainerHiredWarriorInventory.<init>(InventoryPlayer LOTREntityNPC)V",
				"@Inject LOTRGuiHiredWarriorInventory.drawGuiContainerBackgroundLayer(FII)V",
				"@Inject LOTRInventoryHiredReplacedItems.setReplacedEquipment(I ItemStack Z)V",
				"@Replace LOTRInventoryHiredReplacedItems.equipReplacement(I ItemStack)V");
		
		guardsLockCategoryRanged = getInt("guardsLockCategoryRanged", 0, false, 
				"Allowed Ranged Weapon Categories:	 0 = any, 	1 = same,	2 = same or weaker");
		
		guardsAttackDelayFactorRanged = getInt("guardsAttackDelayFactorRanged", 100, false, 
				"Delay Multiplier for different weapon types than their default in percent");
		
		guardsDynamicAttackDelayRanged = getInt("guardsAttackDelayDynamicRanged", 2, false, 
				//"Dynamic instead of weapons default delay, for different weapon types than their default",
				"For different weapon types than their default",
				"0 = default weapon delay, 1 = dynamic delay, 2 = dynamic but atleast default delay"
				
		);
		
		aiRangedImprovements = get("aiRangedImprovements",
				"@Add LOTREntityAIRangedAttack.hasInit", 
				"@Add LOTREntityAIRangedAttack.decisionWait", 
				"@Add LOTREntityAIRangedAttack.moveRangeSq", 
				"@Inject LOTREntityAIRangedAttack.updateTask()V",
				"@Replace LOTREntityAIRangedAttack.shouldExecute()Z",
				"@Remove LOTREntityAIRangedAttack.continueExecuting()Z");

		guardsEquipRemoveBroken = get("guardsEquipRemoveBroken", "Whether equipment can be removed when broken");
		guardsEquipDurabilityFactor = getInt("guardsEquipDurabilityFactor", 200, false, 
				"Durability multiplier in percent");
		guardsEquipDurabilityMode = getInt("guardsEquipDurabilityMode", 3, false, 
				"which weapons are affected by durability? (only if not default)", 
				"0 = none, 1 = any, 2 = any non default, 3 = spears, axes, plates, firepots and termites");
		// percent factor, 0 disable
							// 500 = 5 damage
							// 100 = 1 damage
							// 50 = ~0.5 damage (random)
							// 1 = ~0.01 damage (random)
							
							// 500 = ~ 5x durability (random)
							// 100 = 1x durability
							// 50 = 50% durability
							// 1 = ~0.01 damage (random)
		
		randomEnchRangedNpcWeapons = get("randomEnchRangedNpcWeapons");
		
		hiredTransfer = get("hiredTransfer");
		
		logHiredTransfer = get("loghiredTransfer");
		
		unitOverview = get("unitOverview");
		
		adventureModePatches = get("adventureModePatches",
				"@Inject EntityPlayer.isCurrentToolAdventureModeExempt(III)Z");
		
		fixTeleportToHiringPlayer = get("fixTeleportToHiringPlayer",
				"@Inject LOTRHiredNPCInfo.tryTeleportToHiringPlayer(Z)Z");
		
		unitOverviewHideMeleeForRangedOnly = get("unitOverviewHideMeleeForRangedOnly");
		
		patchDraughtUse = get("patchDraughtUse",
				"@Inject LOTRItemEntDraught.onItemUse(ItemStack EntityPlayer World IIIIFFF)Z",
				"@Inject LOTRBlockEntJar.onBlockActivated(World III EntityPlayer IFFF)Z",
				"@Inject LOTRBlockMug.onBlockActivated(World III EntityPlayer IFFF)Z");
		
		draughtMaxStacksize = getInt("draughtMaxStacksize", 4, false);
		
		draughtStackEffectsIncrease = getInt("draughtStackEffectsIncrease", 50, false, 
				"@Inject LOTRItemMug.onEaten(ItemStack World EntityPlayer)ItemStack",
				"@Inject LOTRItemEntDraught.onEaten(ItemStack World EntityPlayer)ItemStack",
				"0 = off, percent by which the effects increase when above duration");
		
		stewMaxStacksize = getInt("stewMaxStacksize", 16, false, 
				"0 = off, multiplier for other powerfull stews, not absolute");
		
		passMissingDiscriminator = getInt("passMissingDiscriminator", 1, false, 
				"@Inject FMLIndexedMessageToMessageCodec.decode(ChannelHandlerContext FMLProxyPacket List)V", 
				"0 = off, 1 = output, 2 = no output");
		
		preloadClasses = get("preloadClasses");
		
		projectilePatches = get("projectilePatches", 
			"@Inject EntityTrackerEntry.sendLocationToAllClients(List)V",
			"@Inject EntityArrow.onUpdate()V",
			"@Inject LOTREntityProjectileBase.onUpdate()V",
			"@Inject LOTREntityProjectileBase.readEntityFromNBT(NBTTagCompound)V",
			"@Inject LOTREntityProjectileBase.writeEntityToNBT(NBTTagCompound)V"
		);
		
		projectilePatchesReqClient = get("projectilePatchesReqClient");
		
		alwaysAbsorbPiercingProjectiles = get("alwaysAbsorbPiercingProjectiles", false);
		allyProjectilesPassAllies = get("allyProjectilesPassAllies", "ally npc vs ally npc only");
		
		giveManyCommand = get("giveManyCommand");
		
		hiredsConquerForTheirFaction = get("hiredsConquerForTheirFaction",
				"@Inject LOTRPlayerData.addAlignment(EntityPlayer AlignmentBonus LOTRFaction List DDD)LOTRAlignmentBonusMap",
				"@Inject LOTREventHandler.onLivingDeath(LivingDeathEvent)V");
		
		halfTrollAdditions = get("halfTrollAdditions");
		
		entityTrackerPatches = get("entityTrackerPatches",
			"@Inject EntityPlayerMP.onUpdate()V",
			"@Inject WorldClient.removeAllEntities()V"
		);
		
		fixGuiGameOver = get("fixGuiGameOver");
		
		versionCompat = get("versionCompat", "Turn off if you are not updating metweaks or dont use any metweaks blocks");
		
		tabListChanges = get("tabListChanges");
		
		tabListShowMaxPlayers = get("tabListShowMaxPlayers");
		
		tabListSort = get("tabListSort");
		
		moreEvents = get("moreEvents", "Adds new FastTravel Event and processing");
		
		npcDamageFactorProjectiles = getInt("npcDamageFactorProjectiles", 100, false, "percent, 100 = disable");
		npcCriticalProjectileChance = getInt("npcCriticalProjectileChance", 0, false, "percent, 0 = disable");
		projectileFlagsHiredsOnly = get("projectileFlagsHiredsOnly", false);
		
		fixServerJoinFreeze = get("fixServerJoinFreeze");
		
		etFuturumCompatibility = get("etFuturumCompatibility", false);
		
		farView = get("farView");
		
		lotrEventControl = get("lotrEventControl");
		
		renderPerformance = get("renderPerformance");
		
		checkOptifine = get("checkOptifine");
		
		bannerProtectionPatches = get("bannerProtectionPatches", 
				"Fixes a bunch of banner protection bugs");
		
		permNpcAllowDestroyNonLiving = get("permNpcAllowDestroyNonLiving", false, 
				"Allows NPCs to destroy entities like e.g. ItemFrames through arrows, if they have banner permission");
		
		bannerProtectionVerbose = get("bannerProtectionVerbose", false, 
				"Send Protection Warning when using items like food, ranged-weapons and sword-blocking etc.");
		
		revengeCamouflageTargetChance = getInt("revengeCamouflageTargetChance", 0, false, "percent, 0 = disable");
		
		logPlayerDeathLocations = get("logPlayerDeathLocations");

		forceFCV = getInt("forceFCV", 0, false);
		
		lotrHolidays = getInt("lotrHolidays", 1, false,
				"Better Performance for LOTR Holiday checks (more FPS during weather storms etc.)",
				"0 = Patch disabled, 1 = Patch enabled, 2 = Holidays disabled");
				//"@Replace isNewYearsDay()Z, isAprilFools()Z, isHalloween()Z and isChristmas()Z in LOTRMod Class",
		
		mountedBannerBearers = get("mountedBannerBearers", 
				"Adds Mounted Rohan Banner Bearer");
				//"@Inject LOTREntityRohanBannerBearer.<init>(World)V");
		
		moreMountedBannerBearers = get("moreMountedBannerBearers", false, 
				"Adds Dúnedain, Southron Champion and Golden Easterling Banner Rider");
				//"@Inject LOTREntityRangerNorthBannerBearer.<init>(World)V");
		
		explosionPhysics = get("explosionPhysics"); 
				/*"@Inject Explosion.doExplosionA()V", 
				"@Inject Explosion.doExplosionB(Z)V", 
				"@Inject NetHandlerPlayClient.handleSpawnObject(S0EPacketSpawnObject)V", 
				"@Inject BlockFalling.func_149830_m(World III)V", 
				"@Inject LOTRBlockSlabFalling.tryBlockFall(World III)V", 
				"@Inject WorldServer.newExplosion(Entity DDDFZZ)Explosion");*/
		
		captainsAttack = get("captainsAttack", false, "Makes them attack without being provoked");
		
		throwableSpearToggle = get("throwableSpearToggle", "Throwable Toggle for Spears (G key)");
		
		fellowshipFFPatches = get("fellowshipFFPatches", 
				"Fellowship Friendly Fire Patches",
				"Players can no longer hit other player mounts if pvp is prevented in the shared fellowship",
				"Hired Units with neutral relation can no longer accidently hit each other if the fellowship has unit friendly fire disabled");
		
		fixFireworkLag = get("fixFireworkLag", "Fix extreme client lag caused by Fireworks");
		
		fixInvisibility = get("fixInvisibility", false, "Fix Invisibility and sneaking for the target mechanic of all mobs");
		
		hireableEnts = get("hireableEnts", "Makes Ents hireable with Golden Ent Draughts (20% chance)");
		
		hearthFireDoesntSpread = get("hearthFireDoesntSpread", false);
		
		interactReachFix = get("interactReachFix", "no more \"ghost hits\" by doing the distance check more similar to the clientside");
		
		interactReachFeedback = get("interactReachFeedback", false, 
				"Shows players reach values. Orange = above max, red = above buffer. ",
				"Set debug=2 in metweaks.cfg to see the old lotr value");
		
		interactReachBuffer = getInt("interactReachBuffer", 300, false, 
				"Buffer for latency between server and client. In 1/100 Blocks. Default: 300");
		
		interactReachBufferNoSight = getInt("interactReachBufferNoSight", 100, false, 
				"Used if there are collidable blocks between target and attacker eye pos. In 1/100 Blocks. Default: 100");
		
		unlockEnemyWaypoints = get("unlockEnemyWaypoints", false,
				"Unlock all waypoints regardless of enemy control");
		
		fixOpenLinkGUI = get("fixOpenLinkGUI", 
				"Fix Open Link GUI Button Positons");
		
		armorMaintainProtectionPercent = getInt("armorMaintainProtectionPercent", 0, false, 
				"Specify, to what use percent left low durability armor can still maintain max protection. Allows most armor sets to be maxable. ",
				"Only for max enchanted armor, so that weak armor stays weak. Values can be seen with DebugLVL = 4 in metweaks.cfg",
				"0 = Disable");
		
		smoothHeadYawMovement = get("smoothHeadYawMovement",
				"For Living Entities");
		
		noTCPDelay = get("noTCPDelay", 
				"Greatly reduces ping. Packets are sent right away without being queued");
		
		earlyPacketProcessing = get("earlyPacketProcessing", 
				"50 - 200ms latency improvement for players in singleplayer and multiplayer");
		
		prioritizePlayerTicks = get("prioritizePlayerTicks",
				"Players tick before other entities to avoid delay from other base entity ticks or pathfinding");
		
		entityPosRotationThreshold = getInt("entityPosRotationThreshold", 1, false, 
				"Lower Threshold for sending updated Entity Positions and Rotations. 0 = Disable");
		
		smoothEntityMovement = getInt("smoothEntityMovement", 1, false, 
				"Disable Broken Client Prediction for Living Entity Movement similar to how modern mc versions do",
				"0 = Disable, 1 = Enable, 2+ = Enable Client Prediction On Attack for Number of Ticks");
		
		debugModeCommand = get("debugModeCommand");
		
		experimentalFeatures = get("experimentalFeatures", false);
				//"More continuous player updates, can result in 50ms less delay");
		
		mutePlayerDetailsApiSpam = get("mutePlayerDetailsApiSpam", false);
		
				
		
		long endtime2 = System.currentTimeMillis();
		System.out.println("ASMConfig values in "+ (endtime2 - starttime2) +"ms");
		
		long starttime3 = System.currentTimeMillis();
		
		if(needWrite)
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			for(String line : content) {
				out.write(line);
				out.newLine();
			}
			out.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
		
		properties.clear();
		content.clear();
		long endtime3 = System.currentTimeMillis();
		System.out.println("ASMConfig of version " + version + " saved in "+ (endtime3 - starttime3) +"ms - written="+needWrite);
		
	}
	
	public static int getInt(String key, int value, boolean override, String ...comments) {
		boolean found = properties.containsKey(key);
		// if we make this api or another int used we need to change it!
		int num = value;
		if(found) {
			String data = properties.get(key);
			try{
				
				value = Integer.parseInt(data);
	        }
	        catch(Exception exception) {
	        	needWrite = true;
	        	System.out.println("couldnt parse "+key);
	        }
		}
		else {
			needWrite = true;
			
		}
			
		
		content.add("");
		for(String comment : comments) {
			content.add("# " + comment);
		}
		content.add(key+'='+(override ? num : value));
		
		return value;
	}

	public static boolean get(String key, String ...comments) {
		return get(key, true, comments);
	}
	
	public static boolean get(String key, boolean value, String ...comments) {
		boolean found = properties.containsKey(key);
		
		if(found) {
			String data = properties.get(key);
			try{
				
				value = Boolean.parseBoolean(data);
	        }
	        catch(Exception exception) {
	        	needWrite = true;
	        	System.out.println("couldnt parse "+key);
	        }
		}
		else {
			needWrite = true;
			
		}
			
		
		content.add("");
		for(String comment : comments) {
			content.add("# " + comment);
		}
		content.add(key+'='+value);
		
		return value;
	}
}
