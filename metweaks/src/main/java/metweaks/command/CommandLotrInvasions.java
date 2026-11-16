package metweaks.command;

import java.util.List;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.spawning.LOTRInvasions;
import metweaks.features.LOTREventControl;
import metweaks.features.LOTREventControl.EventData;
import lotr.common.world.spawning.LOTREventSpawner.EventChance;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class CommandLotrInvasions extends CommandBase{
	@Override
	public String getCommandName() {
		return "lotrinvasions";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
        return 2;
    }
	
	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length >= 2) {
			String option = args[0].toLowerCase();
			
			LOTRBiome biome = LOTREventControl.getBiomeWithAlias(args[1]);
			
			if(biome == null)
				throw new CommandException("commands.metweaks.invalidbiome", args[1]);
			
			LOTRInvasions invasion = null;
			EventChance chance = null;
			
			if(args.length >= 3) {
				invasion = LOTRInvasions.forName(args[2]);
				if(invasion == null) {
					throw new CommandException("commands.lotr.invasion.noType", args[2]); 
				}
			}
			
			if(args.length >= 4) {
				chance = LOTREventControl.chanceFor(args[3]);
				
				if(chance == null)
					throw new CommandException("commands.metweaks.invalidchance", args[3]); 
			}
			
			if(option.equalsIgnoreCase("add")) {
				if(args.length < 4) throw new WrongUsageException(getCommandUsage(sender));
	
				int ans = LOTREventControl.addOrSetInvasion(biome, invasion, chance, true);
				
				String msg;
				
				if(ans == 2) {
					msg = "Set Chance for Invasion '"+invasion.codeName()+"' in Biome '"+biome.biomeName+"' to '"+chance.name()+"'";
				}
				else if(ans == 1) {
					msg = "Added Invasion '"+invasion.codeName()+"' to Biome '"+biome.biomeName+"' with Chance '"+chance.name()+"'";
				}
				else {
					msg = "Set Invasion '"+invasion.codeName()+"' for Biome '"+biome.biomeName+"' to default";
				}
				
				LOTREventControl.markDirty();
				func_152373_a(sender, this, CommandTravelingTrader.lineBreakAble(EnumChatFormatting.GREEN, msg));
			}
			else if(option.equalsIgnoreCase("remove")) {
				if(args.length < 3) throw new WrongUsageException(getCommandUsage(sender));
				
				
				
				String msg;
				if(LOTREventControl.removeInvasion(biome, invasion, false, true)) {
					msg = CommandTravelingTrader.lineBreakAble(EnumChatFormatting.GRAY, "Removed Invasion '"+invasion.codeName()+"' from Biome '"+biome.biomeName+"'");
				}
				else {
					msg = EnumChatFormatting.RED + "Couldn't remove Invasion";
				}
				
				LOTREventControl.markDirty();
				func_152373_a(sender, this, msg);
			}
			else if(option.equalsIgnoreCase("list")) {
				CommandTravelingTrader.chat(sender, EnumChatFormatting.BOLD + "" + EnumChatFormatting.GOLD + StatCollector.translateToLocalFormatted("commands.metweaks.lotrinvasions.list", biome.biomeName)); 
				
				EventData data = LOTREventControl.table.get(biome);
				
				for(EventChance chancekey : data.invasionsByChance.keySet()) {
					for(LOTRInvasions invasionitem : data.invasionsByChance.get(chancekey)) {
						CommandTravelingTrader.chat(sender, " " +  EnumChatFormatting.YELLOW + invasionitem.codeName() + " " +EnumChatFormatting.GRAY + chancekey);
					}
				}
			}
			else {
				throw new WrongUsageException(getCommandUsage(sender));
			}
		}
		else {
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/lotrinvasions <add/remove/list> <biome> <invasion> <chance>";
	}
	
	@Override
	public List<?> addTabCompletionOptions(ICommandSender sender, String[] args) {
	    if(args.length == 1)
	    	return getListOfStringsMatchingLastWord(args, "add", "remove", "list"); 
	    if(args.length == 2) {
	    	return getListOfStringsMatchingLastWord(args, LOTREventControl.strBiomes);
	    }
	    if(args.length == 3) {
	        return getListOfStringsMatchingLastWord(args, LOTRInvasions.listInvasionNames());
	    } 
	    if(args.length == 4) {
		    return getListOfStringsMatchingLastWord(args, LOTREventControl.strChances);
		} 
	    return null;
	  }
}
