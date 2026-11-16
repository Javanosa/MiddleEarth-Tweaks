package metweaks.command;

import metweaks.core.HooksNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

public class CommandDebugMode extends CommandBase {
	
	@Override
	public String getCommandName() {
		return "debugmode";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length > 0) {
			HooksNetwork.specialDebugMode = parseInt(sender, args[0]);
			sender.addChatMessage(new ChatComponentText("Debug Mode: "+HooksNetwork.specialDebugMode));
		}
		else throw new WrongUsageException("/debugmode <flag>");
	}

}
