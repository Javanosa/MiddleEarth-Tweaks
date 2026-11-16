package metweaks.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;

public class CommandGiveMany extends CommandBase {
	// give command with more player support
	
	@Override
    public String getCommandName() {
        return "give";
    }
	
	@Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

	@Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.give.usage";
    }

	@Override
    public void processCommand(ICommandSender sender, String[] args) {
		
        if(args.length < 2) {
            throw new WrongUsageException("commands.give.usage");
        }
        else {
        	EntityPlayer[] players = PlayerSelector.matchPlayers(sender, args[0]);
        	if(players == null) {
        		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[0]);

                if(player == null) {
                    throw new PlayerNotFoundException();
                }
                
                players = new EntityPlayer[] {player};
        	}
            
            Item item = getItemByText(sender, args[1]);
            int amount = 1;
            int data = 0;

            // amount
            if(args.length >= 3) {
                amount = parseIntBounded(sender, args[2], 1, 64);
            }
            // damage or meta
            if(args.length >= 4) {
                data = parseInt(sender, args[3]);
            }

            ItemStack itemstack = new ItemStack(item, amount, data);

            // nbt
            if(args.length >= 5) {
            				//   combineStringsToMessage
                String message = func_147178_a(sender, args, 4).getUnformattedText();

                try {
                						//  parseFromString
                    NBTBase nbt = JsonToNBT.func_150315_a(message);

                    if(!(nbt instanceof NBTTagCompound)) {
                        func_152373_a(sender, this, "commands.give.tagError", "Not a valid tag");
                        return;
                    }

                    itemstack.setTagCompound((NBTTagCompound) nbt);
                }
                catch (NBTException e) {
                    func_152373_a(sender, this, "commands.give.tagError", e.getMessage());
                    return;
                }
            }
            
            StringBuilder givenPlayers = new StringBuilder();
            
            for(int i = 0; i < players.length; i++) {
            	EntityPlayer player = players[i];
            	EntityItem entityitem = player.dropPlayerItemWithRandomChoice(itemstack, false);
                // pickup instantly
                entityitem.delayBeforeCanPickup = 0;
                String name = player.getCommandSenderName();
                // setOwner()
                entityitem.func_145797_a(name);
                givenPlayers.append(name);
                
                // all before the last one
                if((i+1) < players.length) {
                	givenPlayers.append(", ");
                }
            }

           
            														// 	   getItemHoverChatComponent()
            func_152373_a(sender, this, "commands.give.success", itemstack.func_151000_E(), amount, givenPlayers.toString());
        }
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
    	if(args.length == 1) {
    		return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    	}
    	if(args.length == 2) {
    		// todo: suggest mod names first
    		return getListOfStringsFromIterableMatchingLastWord(args, Item.itemRegistry.getKeys());
    	}
		return null;
    }
}