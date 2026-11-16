package metweaks.command;

import net.minecraft.command.ICommandSender;

public class ClientCommandDebug extends CommandDebugMode {

	@Override
	public String getCommandName() {
		return "cdebugmode";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		 return true;
	}

}
