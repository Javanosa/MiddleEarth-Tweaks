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
		fixSluggishHaradAttack,
		fixQuestofferSync,
		foodConsumeDurations,
		npcExplosionProtection,
		improveBlockBreakSpeeds,
		disableTinCopperSilverGen,
		guardsEquipRanged,
		guardsEquipAllowTermites,
		guardsDynamicAttackDelayRanged,
		aiRangedImprovements,
		guardsEquipRemoveBroken,
		randomEnchRangedNpcWeapons,
		hiredTransfer,
		unitOverview, 
		unitOverviewHideMeleeForRangedOnly,
		adventureModePatches,
		fixTeleportToHiringPlayer,
		patchDraughtUse,
		preloadClasses
		
		
		
	;
	
	public static int tauredainPoisonDartChance, 
		guardsLockCategoryRanged, 
		guardsAttackDelayFactorRanged,
		guardsEquipDurabilityMode,
		guardsEquipDurabilityFactor,
		draughtStackEffectsIncrease,
		draughtMaxStacksize,
		stewMaxStacksize,
		passMissingDiscriminator;
	
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
	static List<String> content;
	static boolean needWrite;
	
	public static void init() {
		
		File file = new File("config", MeTweaks.MODID+"-ASM.properties");
		properties = read(file);
		
		long starttime2 = System.currentTimeMillis();
		
		content = new ArrayList<>();
		content.add("# ASM Config - You can disable Patches");
		content.add("# If you encounter any issues please reach out to me");
		
		int version = getInt("configversion", 0, true);
		
		verticalSlabPlacement = get("verticalSlabPlacement", 
			"@Inject ItemSlab.onItemUse(ItemStack EntityPlayer World IIIIFFF)Z", 
			"@Inject ItemSlab.func_150936_a(World IIII EntityPlayer ItemStack)Z",
			"@Replace BlockSlab.setBlockBoundsForItemRender()V"
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
				"@Add getPickBlock(MovingObjectPosition World III EntityPlayer)ItemStack"
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
		
		guardsDynamicAttackDelayRanged = get("guardsAttackDelayDynamicRanged", 
				"Dynamic instead of weapons default delay, for different weapon types than their default");
		
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
