package metweaks.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lotr.common.entity.npc.LOTRTravellingTrader;
import lotr.common.world.biome.LOTRBiome;
import metweaks.features.LOTREventControl;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class CommandTravelingTrader extends CommandBase {

	@Override
	public String getCommandName() {
		return "travellingtrader";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/travellingtrader <add/remove/list> <biome> <name>";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}
	
	public static String lineBreakAble(EnumChatFormatting color, String in) {
		String[] arr = in.split(" ");
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < arr.length; i++) {
			builder.append(color+arr[i]);
			if(i < arr.length - 1) {
				builder.append(' ');
			}
		}
		return builder.toString();
	}
	
	public static void chat(ICommandSender sender, String msg) {
		sender.addChatMessage(new ChatComponentText(msg));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "add", "list", "remove");
		}
		if(args.length == 2) {
			return getListOfStringsMatchingLastWord(args, LOTREventControl.strBiomes);
		}
		else if(args.length == 3) {
			List<String> traders = new ArrayList<>();
			
			Set<Class> set = EntityList.classToStringMapping.keySet();
			for(Class clazz : set) {
				if(LOTRTravellingTrader.class.isAssignableFrom(clazz)) {
					traders.add((String) EntityList.classToStringMapping.get(clazz));
				}
			}
			return getListOfStringsMatchingLastWord(args, traders.toArray(new String[0]));
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length >= 2) {
			
			String option = args[0].toLowerCase();
			
			LOTRBiome biome = LOTREventControl.getBiomeWithAlias(args[1]);

			if(biome == null)
				throw new WrongUsageException("commands.metweaks.invalidbiome", args[1]); 
			
			Class trader = null;
			
			if(args.length >= 3) {
				String name = args[2];
				trader = (Class) EntityList.stringToClassMapping.get(name);
				if(trader == null || !LOTRTravellingTrader.class.isAssignableFrom(trader)) {
					throw new WrongUsageException("commands.metweaks.invalidtrader", name); 
				}
			}
			
			if(option.equalsIgnoreCase("add")) {
				if(args.length < 3) throw new WrongUsageException(getCommandUsage(sender));

				boolean success = LOTREventControl.addTrader(biome, trader, true);
				String translate = success ? "commands.metweaks.travellingtrader.add" :  "commands.metweaks.travellingtrader.addfail";
				
				LOTREventControl.markDirty();
				func_152373_a(sender, this, lineBreakAble(success ? EnumChatFormatting.GREEN : EnumChatFormatting.RED, StatCollector.translateToLocalFormatted(translate, args[2], biome.biomeName)));
			}
			else if(option.equalsIgnoreCase("remove")) {
				if(args.length < 3) throw new WrongUsageException(getCommandUsage(sender));
				
				boolean success = LOTREventControl.removeTrader(biome, trader, true);
				String translate = success ? "commands.metweaks.travellingtrader.remove" : "commands.metweaks.travellingtrader.removefail";

				LOTREventControl.markDirty();
				func_152373_a(sender, this, lineBreakAble(success ? EnumChatFormatting.GRAY : EnumChatFormatting.RED, StatCollector.translateToLocalFormatted(translate, args[2], biome.biomeName)));
			}
			else if(option.equalsIgnoreCase("list")) {
				chat(sender, EnumChatFormatting.BOLD + "" + EnumChatFormatting.GOLD + StatCollector.translateToLocalFormatted("commands.metweaks.travellingtrader.list", biome.biomeName)); 
				
				List<Class> traders = LOTREventControl.table.get(biome).traders;
				for(Class clazz : traders) {
					String name = (String) EntityList.classToStringMapping.get(clazz);
					chat(sender, " " +  EnumChatFormatting.YELLOW + name);
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

}
