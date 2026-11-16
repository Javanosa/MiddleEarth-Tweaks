package metweaks.command;

import java.util.List;
import lotr.common.world.biome.LOTRBiome;
import lotr.common.world.spawning.LOTREventSpawner.EventChance;
import metweaks.features.LOTREventControl;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class CommandBanditChance extends CommandBase {

	@Override
	public String getCommandName() {
		return "banditchance";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/banditchance <biome> <chance>";
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
			return getListOfStringsMatchingLastWord(args, LOTREventControl.strBiomes);
		}
		else if(args.length == 2) {
			return getListOfStringsMatchingLastWord(args, 
				"NEVER", "RARE", "BANDIT_RARE", "UNCOMMON", "BANDIT_UNCOMMON", "BANDIT_COMMON", "COMMON");
		}
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length >= 1) {
			
			LOTRBiome biome = LOTREventControl.getBiomeWithAlias(args[0]);

			if(biome == null)
				throw new WrongUsageException("commands.metweaks.invalidbiome", args[0]); 
	
			if(args.length >= 2) {
				EventChance chance = LOTREventControl.chanceFor(args[1]);
				
				if(chance == null)
					throw new WrongUsageException("commands.metweaks.invalidchance", args[1]); 
				
				LOTREventControl.setBanditChance(biome, chance);
				LOTREventControl.markDirty();
				
				func_152373_a(sender, this, EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("commands.metweaks.banditchance.set", biome.biomeName, chance.name()));
			}
			else {
				func_152373_a(sender, this, EnumChatFormatting.GOLD + StatCollector.translateToLocalFormatted("commands.metweaks.banditchance.get", biome.biomeName, biome.getBanditChance().name()));
			}
		}
		else {
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

}
