package metweaks.command;

import java.util.List;

import metweaks.farview.FastCraftInfo;
import metweaks.farview.METChunkProviderClient;
import metweaks.farview.METLongHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.chunk.IChunkProvider;

public class ClientCommandFarView extends CommandBase {
	
	

	@Override
	public String getCommandName() {
		return "cfarview";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/cfarview <distance in chunks>";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		 return true;
	}
	
	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}
	
	// ip address, port, domain if available
	// server brand
	// version
	
	
	
	// dimensionid
	// worldname

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		IChunkProvider provider = Minecraft.getMinecraft().theWorld.getChunkProvider();
		if(!(provider instanceof METChunkProviderClient)) {
			throw new CommandException("FarView module is disabled");
		}
		
		METChunkProviderClient providerMET = (METChunkProviderClient) provider;
		
		if(args.length >= 1) {
			
			String option = args[0];
			
			if(option.equalsIgnoreCase("keep")) {
				boolean enabled = METChunkProviderClient.keepLastWorld;
				// about to turn it off
				// clear unloadable and undo render
				if(enabled) {
					METChunkProviderClient.resetLastWorldData();
				}
				else {
					providerMET.setLastWorldData(providerMET.getLastWorldName());
				}
				
				enabled = METChunkProviderClient.keepLastWorld = !enabled;
				
				sender.addChatMessage(new ChatComponentText("Farview - Keep last World: "+(enabled ? "\u00a7ayes" : "\u00a7cno")));
				return;
			}
			else if(option.equalsIgnoreCase("keepTiles")) {
				boolean enabled = METChunkProviderClient.keepTileEntities;
				enabled = METChunkProviderClient.keepTileEntities = !enabled;
				
				sender.addChatMessage(new ChatComponentText("Farview - Keep TileEntities: "+(enabled ? "\u00a7ayes" : "\u00a7cno")));
				return;
			}
			else if(option.equalsIgnoreCase("tickUnloadable")) {
				boolean enabled = METChunkProviderClient.tickUnloadable;
				enabled = METChunkProviderClient.tickUnloadable = !enabled;
				
				sender.addChatMessage(new ChatComponentText("Farview - Tick Unloadable Chunks: "+(enabled ? "\u00a7ayes" : "\u00a7cno")));
				return;
			}
			else if(option.equalsIgnoreCase("useFastCraft")) {
				boolean enabled = FastCraftInfo.useFastCraft;
				enabled = FastCraftInfo.useFastCraft = !enabled;
				METChunkProviderClient.resetLastWorldData();
				
				sender.addChatMessage(new ChatComponentText("Farview - Use FastCraft: "+(enabled ? "\u00a7ayes" : "\u00a7cno") + " \u00a7r(Needs World Rejoin)"));
				return;
			}
			else if(option.equalsIgnoreCase("forceUnloadEntities")) {
				
				boolean enabled = METChunkProviderClient.forceUnloadEntities;
				enabled = METChunkProviderClient.forceUnloadEntities = !enabled;
				
				
				sender.addChatMessage(new ChatComponentText("Farview - Force Unload Entities: "+(enabled ? "\u00a7ayes" : "\u00a7cno")));
				return;
			}
			else if(option.equalsIgnoreCase("cacheVariants")) {
				
				providerMET.update(true); // remove the biome variants just in case
				boolean enabled = METChunkProviderClient.cacheVariants;
				enabled = METChunkProviderClient.cacheVariants = !enabled;
				
				
				sender.addChatMessage(new ChatComponentText("Farview - Cache LOTR Biome Variants: "+(enabled ? "\u00a7ayes" : "\u00a7cno")));
				return;
			}
			else if(option.equalsIgnoreCase("cubicDistance")) {
				
				
				boolean enabled = METChunkProviderClient.cubicDistance;
				enabled = METChunkProviderClient.cubicDistance = !enabled;
				
				
				sender.addChatMessage(new ChatComponentText("Farview - Cubic Distance: "+(enabled ? "\u00a7ayes" : "\u00a7cno")));
				return;
			}
			else if(option.equalsIgnoreCase("threshold")) {
				if(args.length >= 2) {
					int threshold = parseIntBounded(sender, args[1], 0, 500000);
					METChunkProviderClient.chunkThreshold = threshold;
					
					if(threshold == 0) {
						int view = METChunkProviderClient.distance = METChunkProviderClient.distanceTarget;
						METChunkProviderClient.distanceSq = view * view;
					}
					
					sender.addChatMessage(new ChatComponentText("Farview - Chunk Threshold: "+(threshold == 0 ? "\u00a7cdisabled" : threshold)));
					return;
				}
				throw new WrongUsageException("/cfarview threshold <chunks>");
				
			}
			else if(option.equalsIgnoreCase("delay")) {
				if(args.length >= 2) {
					int delay = parseIntBounded(sender, args[1], 0, 500000);
					METChunkProviderClient.cycleDelay = delay;
					sender.addChatMessage(new ChatComponentText("Farview - Delay: "+delay+"ms"));
					return;
				}
				throw new WrongUsageException("/cfarview delay <milliseconds>");
				
			}
			else if(option.equalsIgnoreCase("unloadsPerCycle")) {
				if(args.length >= 2) {
					int unloadsPerCycle = parseIntBounded(sender, args[1], 0, 500000);
					METChunkProviderClient.unloadsPerCycle = unloadsPerCycle;
					sender.addChatMessage(new ChatComponentText("Farview - Unloads Per Cycle: "+unloadsPerCycle));
					return;
				}
				throw new WrongUsageException("/cfarview unloadsPerCycle <max>");
				
			}
			
			int serverChunks = providerMET.chunkMap.getNumHashElements() - (providerMET.unloadable == null ? 0 : providerMET.unloadable.size());
			int serverViewDistance = (int) Math.ceil((Math.sqrt(serverChunks) - 1) / 2); // guessed server chunk size
			int view = parseIntBounded(sender, option, -1, 500000); // fix int overflow
			if(view <= -1) {
				METChunkProviderClient.checkDistance = false;
				view = 48; // default fallback
			}
			else if(view < serverViewDistance) {
				throw new NumberInvalidException("Distance cannot be below server distance");
			}
			else {
				METChunkProviderClient.checkDistance = true;
				
			}
			
			if(!METChunkProviderClient.cubicDistance) view++;
			
			METChunkProviderClient.distance = view;
			METChunkProviderClient.distanceTarget = view;
			METChunkProviderClient.distanceSq = view * view;
			
			if(!METChunkProviderClient.cubicDistance) view--;
			
			
			String chunks = METChunkProviderClient.checkDistance ? String.valueOf(view) : "Unlimited";
			sender.addChatMessage(new ChatComponentText("Farview - Distance: "+chunks+" Chunks"));
		}
		else {
			boolean enabled = METChunkProviderClient.farViewEnabled;
			// about to turn it off
			// clear unloadable and undo render
			if(enabled) {
				providerMET.update(true);
				if(!METChunkProviderClient.keepLastWorld) {
					METChunkProviderClient.resetLastWorldData();
				}
				
			}
			else {
				providerMET.unloadable = new METLongHashSet();
				if(METChunkProviderClient.keepLastWorld) {
					providerMET.setLastWorldData(providerMET.getLastWorldName());
				}
			}
			
			enabled = METChunkProviderClient.farViewEnabled = !enabled;
			
			String chunks = METChunkProviderClient.checkDistance ? String.valueOf(METChunkProviderClient.distance - (METChunkProviderClient.cubicDistance ? 0 : 1)) : "Unlimited";
			String thresholdStr = METChunkProviderClient.chunkThreshold == 0 ? "" : ". Threshold: " + METChunkProviderClient.chunkThreshold;
			sender.addChatMessage(new ChatComponentText(enabled ? "Farview: \u00a7aenabled. \u00a7rDistance: "+ chunks + " Chunks"+thresholdStr : "Farview: \u00a7cdisabled"));
			
			
			
			
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
    	if(args.length == 1) {
    		return getListOfStringsMatchingLastWord(args, "keep", "-1", "keepTiles", "useFastCraft", "threshold", "tickUnloadable", "delay", "unloadsPerCycle", "cacheVariants", "forceUnloadEntities", "cubicDistance");
    	}
		return null;
    }

}
