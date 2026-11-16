package metweaks.command;

import metweaks.events.ClientEvents;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class ClientCommandTOSNotice extends CommandBase {

	@Override
	public String getCommandName() {
		return "cShowTOSNotice";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		 return true;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		ClientEvents.openTosHintGUI = true;
	}

}
