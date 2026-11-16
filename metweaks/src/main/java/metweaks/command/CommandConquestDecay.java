package metweaks.command;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import lotr.common.LOTRLevelData;
import metweaks.MeTweaksConfig;
import metweaks.network.NetworkHandler;
import metweaks.network.SyncConquestDecayPacket;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StatCollector;

public class CommandConquestDecay extends CommandBase {

	@Override
	public String getCommandName() {
		return "conquestdecay";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/conquestdecay <minutes | reset>";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1) {
			return Arrays.asList("reset");
		}
		return null;
	}
	
	private static String fancyRound(float decay) {
		DecimalFormat df = new DecimalFormat("0.###");
		return df.format(decay);
	}
	
	public static boolean syncConqDecay() {
		// either not singleplayer or more than one player (lan)
		return !MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getCurrentPlayerCount() > 1;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		
		if(args.length >= 1) {
			float decay = 60; // 1 hour
			if(!args[0].equalsIgnoreCase("reset")) {
				decay = (float) parseDoubleBounded(sender, args[0], 0, 1000000); // max is about 2 years, in minutes
				
			}

			if(decay == 0) {
				// disable
				func_152373_a(sender, this, StatCollector.translateToLocal("commands.conqdecay.disable"));
			}
			else {
				func_152373_a(sender, this, StatCollector.translateToLocalFormatted("commands.conqdecay.set", fancyRound(decay)));
			}
			MeTweaksConfig.conquestDecay = decay * 60;
			LOTRLevelData.markDirty();
			
			if(syncConqDecay())
				NetworkHandler.networkWrapper.sendToAll(new SyncConquestDecayPacket());
		}
		else {
			func_152373_a(sender, this, StatCollector.translateToLocalFormatted("commands.conqdecay.info", fancyRound(MeTweaksConfig.conquestDecay / 60F)));
		}
	}

}
