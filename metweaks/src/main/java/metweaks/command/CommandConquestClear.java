package metweaks.command;

import java.util.Arrays;
import java.util.List;

import lotr.common.fac.LOTRFaction;
import lotr.common.world.map.LOTRConquestGrid;
import lotr.common.world.map.LOTRConquestZone;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class CommandConquestClear extends CommandBase {

	@Override
	public String getCommandName() {
		return "conqclear";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/conqclear <faction> <radial> [x] [z]";
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
			List<String> factions = LOTRFaction.getPlayableAlignmentFactionNames();
			return getListOfStringsMatchingLastWord(args, factions.toArray(new String[0]));
		}
		else if(args.length == 2) {
			return Arrays.asList("radial");
		}
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();
		if(!LOTRConquestGrid.conquestEnabled(world))
			throw new WrongUsageException("commands.lotr.conquest.notEnabled");

		if(args.length >= 1) {
			LOTRFaction faction = LOTRFaction.forName(args[0]);
			if(faction == null)
				throw new WrongUsageException("commands.lotr.conquest.noFaction", new Object[] {args[0]}); 
	
			boolean radial = args.length >= 2 && args[1].equals("radial");
			
			
			
			
			
			int index = radial ? 2 : 1;
			int x = sender.getPlayerCoordinates().posX;
			int z = sender.getPlayerCoordinates().posZ;
			
			if(args.length >= index + 2) {
				x = parseInt(sender, args[index]);
				z = parseInt(sender, args[index + 1]);
			}
			LOTRConquestZone conqZone = LOTRConquestGrid.getZoneByWorldCoords(x, z);
			
			if(conqZone.isDummyZone)
				throw new WrongUsageException("commands.lotr.conquest.outOfBounds", new Object[] {x, z});
			
			
			
			
			
	
			String langkey = "commands.metweaks.conqclear";
	
			if(radial) {
				float value = conqZone.getConquestStrength(faction, world);
				//System.out.println("v "+value);
				LOTRConquestGrid.doRadialConquest(world, conqZone, null, null, faction, value, value);
				langkey += ".radial";
			}
			else{
				conqZone.setConquestStrength(faction, 0, world);
			}
	
			func_152373_a(sender, this, StatCollector.translateToLocalFormatted(langkey, new Object[] {faction.factionName(), x, z}));
		}
		else {
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

}
