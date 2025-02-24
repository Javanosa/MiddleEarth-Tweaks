package metweaks.command;

import lotr.common.LOTRMod;
import metweaks.features.ItemUseHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class CommandGuardModeHorn extends CommandBase {
	
	// command can be used by anyone

	@Override
	public String getCommandName() {
		return "guardmodehorn";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return null;
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
	public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayer)) 
			throw new WrongUsageException(StatCollector.translateToLocal("commands.entitystack.requirePlayer"));
		
		EntityPlayer player = (EntityPlayer) sender;
		
		ItemStack stack = player.getHeldItem();		

		if(stack == null || stack.getItem() != LOTRMod.commandHorn) 
			throw new WrongUsageException(StatCollector.translateToLocal("commands.guardmodehorn.requireHorn"));
		
		stack.setItemDamage(ItemUseHandler.TURN_ON_GUARDMODE);
		stack.setStackDisplayName("\u00a7r"+StatCollector.translateToLocal("gui.guardmodehorn.display")+StatCollector.translateToLocal("options.on"));
		sender.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("commands.guardmodehorn.converted")));
	}

}
