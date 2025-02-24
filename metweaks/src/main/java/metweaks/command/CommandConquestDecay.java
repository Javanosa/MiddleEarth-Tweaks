package metweaks.command;

import java.util.Arrays;
import java.util.List;
import lotr.common.LOTRLevelData;
import metweaks.MeTweaksConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
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
		String decay_str = ""+Math.round(decay * 10.0F) / 10.0F;
		if(decay_str.endsWith(".0"))
			return decay_str.substring(0, decay_str.length() - 2);
		return decay_str;
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
		}
		else {
			func_152373_a(sender, this, StatCollector.translateToLocalFormatted("commands.conqdecay.info", fancyRound(MeTweaksConfig.conquestDecay / 60F)));
		}
	}

}
