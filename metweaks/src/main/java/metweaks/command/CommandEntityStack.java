package metweaks.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.EntityInteractEvent;

public class CommandEntityStack extends CommandBase {
	
	
	// player uuid, int state
	public static Map<UUID, int[]> entitystack_states;

	@Override
	public String getCommandName() {
		return "entitystack";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/entitystack <ride / disattach>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1)
			return Arrays.asList("ride", "disattach");
		return null;
	}
	
	public static void processInteract(EntityInteractEvent event) {
		if(entitystack_states != null && entitystack_states.containsKey(event.entityPlayer.getUniqueID())) {
			EntityPlayer player = event.entityPlayer;
			UUID uuid = player.getUniqueID();
			
			
			
			int[] state = entitystack_states.get(uuid);
			switch(state[0]) {
				case 0:
					// set mount
					state[0]++;
					state[1] = event.target.getEntityId();
					state[2] = player.dimension;
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN+StatCollector.translateToLocal("commands.entitystack.defmount")));
					
					break;
				case 1:
					
					if(player.dimension != state[2]) {
						// dimension change detected, aborting...
						player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+StatCollector.translateToLocal("commands.entitystack.dimChange")));
						entitystack_states.remove(uuid);
						return;
					}
					Entity rider = player.worldObj.getEntityByID(state[1]);
					
					if(rider == null || rider.isDead) {
						// the entity appears to be missing or unloaded
						player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+StatCollector.translateToLocal("commands.entitystack.missing")));
						entitystack_states.remove(uuid);
						return;
					}
					
					Entity mount = event.target;
					// swap rider and mount
					
						// normal
					if(rider != mount)
						rider.mountEntity(mount);
					//}
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+StatCollector.translateToLocalFormatted("commands.entitystack.success", rider.getCommandSenderName(), rider.getCommandSenderName())));
					
					// set rider, chain
					entitystack_states.remove(uuid);
					break;
				case 2: // unstack
					Entity stacked = event.target;
					// get upper one
					if(stacked.ridingEntity == null && stacked.riddenByEntity != null) {
						stacked = stacked.riddenByEntity;
					}
					
					if(stacked.ridingEntity != null) {
						stacked.mountEntity(null);
						player.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD+StatCollector.translateToLocalFormatted("commands.entitystack.successDismount", stacked.getCommandSenderName())));
						
					}
					else {
						// warn unable to unstack
						player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+StatCollector.translateToLocal("commands.entitystack.failDismount")));
						
						
					}
					entitystack_states.remove(uuid);
					// successfully unstacked
					
					
					break;
			}
			event.setCanceled(true);
		}
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(sender instanceof EntityPlayer) {
			
			if(entitystack_states == null) entitystack_states = new HashMap<>();
			
			EntityPlayer player = (EntityPlayer) sender;
			UUID uuid = player.getUniqueID();
			
			if(entitystack_states.containsKey(uuid)) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED+StatCollector.translateToLocal("commands.entitystack.cancel")));
				entitystack_states.remove(uuid);
				return;
			}
			
			if(args.length >= 1) {
				String arg = args[0];
				
				if(arg.equalsIgnoreCase("ride")) {
					entitystack_states.put(uuid, new int[] {1, player.getEntityId(), player.dimension, 0});
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN+StatCollector.translateToLocal("commands.entitystack.defmount")));
					return;
				}
				else if(arg.equalsIgnoreCase("disattach")) {
					entitystack_states.put(uuid, new int[] {2, Integer.MIN_VALUE, Integer.MIN_VALUE, 0});
					player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+StatCollector.translateToLocal("commands.entitystack.dismount")));
					return;
				}
			}
			
			
			entitystack_states.put(uuid, new int[] {0, Integer.MIN_VALUE, Integer.MIN_VALUE, 0});
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN+StatCollector.translateToLocal("commands.entitystack.defrider")));
		}
		else throw new CommandException(StatCollector.translateToLocal("commands.entitystack.requirePlayer"));
	}

}
