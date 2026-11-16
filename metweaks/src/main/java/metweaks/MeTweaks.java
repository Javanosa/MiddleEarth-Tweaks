package metweaks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Level;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.InvalidVersionSpecificationException;
import cpw.mods.fml.common.versioning.VersionRange;
import cpw.mods.fml.relauncher.Side;
import lotr.common.world.biome.variant.LOTRBiomeVariant;
import metweaks.block.MissingMappingCheck;
import metweaks.command.*;
import metweaks.features.LOTREventControl;
import metweaks.guards.customranged.CustomRanged;
import metweaks.physics.ExplosionPhysicsSettings;
import metweaks.proxy.CommonProxy;
import metweaks.proxy.UpdateCheck;
import net.minecraft.server.MinecraftServer;

@Mod(modid = MeTweaks.MODID, name = MeTweaks.MODNAME, version = MeTweaks.VERSION, 
	 guiFactory = "metweaks.client.gui.config.MeTweaksGuiFactory", dependencies = "after:*")
public class MeTweaks {
	public static boolean nei;
	public static boolean lotr;
	public static boolean chisel;
	public static boolean customNpcs;
	public static boolean mpm;
	public static boolean lotrfa;
	public static boolean etfuturum;
	public static boolean astikoor;
	public static boolean hodgepodge;
	public static boolean netherEnderLicious;
	
	
	public static int celeritasVers;
	
	public static final String MODID = "metweaks";
	public static final String MODNAME = "MiddleEarth Tweaks";
	public static final String VERSION = "1.6.22";
	
	@Instance
	public static MeTweaks instance;
	
	@SidedProxy(clientSide = "metweaks.proxy.ClientProxy", serverSide = "metweaks.proxy.CommonProxy")
    public static CommonProxy proxy;
	
	private static VersionRange versionRange;
	
	public static boolean remotePresent;
	
	@NetworkCheckHandler
	public boolean checkNetwork(Map<String, String> remoteVersions, Side side) {
			
		boolean pass = false;
		remotePresent = false;
		
		
		// critical settings enabled?
		boolean unrestricted = MeTweaksConfig.unrestricted();
		if(unrestricted) {
			pass = true;
		}
		
		if(remoteVersions.containsKey(MODID) && versionRange.containsVersion(new DefaultArtifactVersion(remoteVersions.get(MODID)))) {
				pass = true;
				remotePresent = true;
			
		}
		else if(!unrestricted) {
			FMLLog.getLogger().info(MODNAME+ "-Config: Restriction is present");
		}
		
		
		return pass || side == Side.SERVER;
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		nei = Loader.isModLoaded("NotEnoughItems");
		lotr = Loader.isModLoaded("lotr");
		chisel = Loader.isModLoaded("chisel");
		customNpcs = Loader.isModLoaded("customnpcs");
		mpm = Loader.isModLoaded("moreplayermodels");
		lotrfa = Loader.isModLoaded("lotrfa");
		etfuturum = Loader.isModLoaded("etfuturum");
		astikoor = Loader.isModLoaded("astikoor");
		hodgepodge = Loader.isModLoaded("hodgepodge");
		netherEnderLicious = Loader.isModLoaded("netherlicious") || Loader.isModLoaded("enderlicious");
		
		
		if(Loader.isModLoaded("angelica")) {
			ArtifactVersion currentangelica = Loader.instance().getIndexedModList().get("angelica").getProcessedVersion();
			
			// ChunkTracker is now again: Class
			if(new DefaultArtifactVersion("2.2.22").compareTo(currentangelica) <= 0) {
				celeritasVers = 2; // 4 ?
			}
			// ChunkTracker is now: Interface
			else if(new DefaultArtifactVersion("2.1.50").compareTo(currentangelica) <= 0) {
				celeritasVers = 3;
			}
			// package is now: org.embeddedt.embeddium.impl.**;
			// CeleritasCalls.loadChunk/unloadChunk() is called from our own chunkproviderclient as the calls are no longer in doPreChunk()
			else if(new DefaultArtifactVersion("2.0.0").compareTo(currentangelica) <= 0) {
				celeritasVers = 2;
			}
			// package is me.jellysquid.mods.sodium.client.**;
			// ChunkTracker.onChunkStatusRemoved() is prevented during farview as the call is injected in doPreChunk() regardless after our classtransformer.
			else {
				celeritasVers = 1;
			}
			
			 
		}
		else if(Loader.isModLoaded("beddium")) {
			
			celeritasVers = 2;
		}
		
		

		try{
			versionRange = VersionRange.createFromVersionSpec("[1.6.4,1.7)");
        }
        catch(InvalidVersionSpecificationException e) {
            FMLLog.log(Level.WARN, e, "Invalid bounded range %s specified for network mod id %s", versionRange, MODID);
        }
		
		proxy.preInit(event);
		
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
		
		if(MeTweaksConfig.updateCheck)
			UpdateCheck.checkForUpdate();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
		if(MeTweaks.lotr && ASMConfig.lotrEventControl)
			LOTREventControl.initializeDefaults();
		
		if(ASMConfig.preloadClasses)
			PreLoad.preloadClasses(); // before lotr main menu animation
		
		
	}
	
	private static Set<Object> disabled_structure_cache;
			
	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if(lotr) {
			if(ASMConfig.disableStructures && disabled_structure_cache != null) {
				for(int i = 0; i < 256; i++) {
					LOTRBiomeVariant variant = LOTRBiomeVariant.getVariantForID(i);
					if(!disabled_structure_cache.contains(variant)) {
						variant.disableStructures = false;
					}
				}
			}
			
			if(ASMConfig.guardsEquipRanged) {
				CustomRanged.ddrMap.clear();
			}
		}
		
		if(!MinecraftServer.getServer().isDedicatedServer()) {
			if(lotr && ASMConfig.lotrEventControl) {
				LOTREventControl.setDefaults();
			}
			if(ASMConfig.explosionPhysics) {
				ExplosionPhysicsSettings.setFromDefaults();
			}
			
		}
		
		// otherwise we can add defaults here
		// do not access world/entity data here. Block/item id mapping is already reverted here
	}
	
	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		if(lotr && ASMConfig.lotrEventControl) {
			LOTREventControl.save();
		}
		if(ASMConfig.explosionPhysics) {
			ExplosionPhysicsSettings.save();
		}
		
	}
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		if(lotr) {
			if(ASMConfig.conquestDecay)
				event.registerServerCommand(new CommandConquestDecay());
			if(ASMConfig.conquestClearCommand)
				event.registerServerCommand(new CommandConquestClear());
			if(MeTweaksConfig.toggleGuardModeHorn) // its for server only so its very unlikely that we need an extra option in asmconfig
				event.registerServerCommand(new CommandGuardModeHorn());
			
			if(ASMConfig.lotrEventControl) {
				event.registerServerCommand(new CommandBanditChance());
				event.registerServerCommand(new CommandTravelingTrader());
				event.registerServerCommand(new CommandLotrInvasions());
				
				LOTREventControl.load();
			}
		}
		
		if(ASMConfig.entityKillCommand || ASMConfig.entityTpCommand || ASMConfig.entityEffectCommand) {
			event.registerServerCommand(new CommandEntitySelector());
			
			if(ASMConfig.entityKillCommand)
				event.registerServerCommand(new CommandEntityKill());
			if(ASMConfig.entityTpCommand)
				event.registerServerCommand(new CommandEntityTp());
			if(ASMConfig.entityEffectCommand)
				event.registerServerCommand(new CommandEntityEffect());
		}
		
		if(ASMConfig.entityStackCommand)
			event.registerServerCommand(new CommandEntityStack());
		
		if(ASMConfig.giveManyCommand) {
			event.registerServerCommand(new CommandGiveMany());
		}
		
		if(ASMConfig.explosionPhysics) {
			event.registerServerCommand(new CommandExplosionPhysics());
			ExplosionPhysicsSettings.load();
			
		}
		
		if(ASMConfig.debugModeCommand) {
			event.registerServerCommand(new CommandDebugMode());
		}
		
		
		

			
		
		if(lotr && ASMConfig.disableStructures) {
			disabled_structure_cache = new HashSet<>();
			for(int i = 0; i < 256; i++) {
				LOTRBiomeVariant variant = LOTRBiomeVariant.getVariantForID(i);
				if(variant.disableStructures) {
					disabled_structure_cache.add(variant);
				}
				variant.disableStructures = true;
			}
		}
	}
	
	@EventHandler
	public void onMissingMappings(FMLMissingMappingsEvent event) {
		if(ASMConfig.versionCompat)
			MissingMappingCheck.check(event);
	}
}
