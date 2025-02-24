package metweaks.command;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;

public class CommandEntityKill extends CommandBase {
	
	// 0=dead 1=damage 2=health0
	public static int deathMethod(Entity entity) {
		if(entity instanceof EntityLivingBase) {
			if(entity instanceof EntityPlayer/*entity instanceof EntityMinecartContainer || */ || (entity instanceof EntityHorse && ((EntityHorse) entity).isChested())) {
				return 2;
			}
			
			
			
			/*boolean has = false;
			try {
				has = Selector.customData.get(entity) != null;
			} catch (Exception e) {}*/
			
			if(entity instanceof EntitySlime/* || (has && entity.getEntityData().hasKey("InfernalMobsMod"))*/) {
				return 0;
			}
			return 2;
		}
		return 0;
	}
	
	public static void killEntity(Entity entity, boolean force, boolean drop) {
		int deathMethod = force ? 0 : drop ? 1 : deathMethod(entity);
		switch(deathMethod) {
			case 0:
				entity.setDead();
				break;
			case 1:
				entity.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
				break;
			case 2:
				((EntityLivingBase) entity).setHealth(0);
				break;
		}
	}

	@Override
	public String getCommandName() {
		return "entitykill";
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List getCommandAliases() {
        return Arrays.asList(new String[] {"ekill"});
    }

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ekill <selector> <force | drop>";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return CommandEntitySelector.getSelectorTabs(sender, args);
	}
	
	

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		
		Object[] data = CommandEntitySelector.selectEntities(0, args, sender);
		@SuppressWarnings("unchecked")
		List<Entity> results = (List<Entity>) data[0];
		int offset = (int) data[1];
		
		// self if not specified
		if(args.length == 0) {
			Entity self = CommandEntitySelector.getSelf(sender);
			
			if(self != null) {
				results.add(self);
			}
			else throw new CommandException(StatCollector.translateToLocal("commands.generic.notAnEntity"));
		}
		else if(offset == 0) {
			//func_152373_a(sender, this, CommandEntitySelector.getSelectorTip()); // we dont want command block console out here
			sender.addChatMessage(new ChatComponentText(CommandEntitySelector.getSelectorTip()));
			throw new CommandException(getCommandUsage(sender));
		}
		
		
		CommandEntityTp.getDistination(results, false);

		boolean force = false;
		boolean drop = false;
		if(args.length > offset) {
			if(args[offset].equalsIgnoreCase("force")) {
				force = true;
			}
			else if(args[offset].equalsIgnoreCase("drop")) {
				drop = true;
			}
			else throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.unknownArg", args[offset], args[offset], "force | drop"));
		}
		
		StringBuilder builder = new StringBuilder();
		Iterator<Entity> it = results.iterator();
		TObjectIntMap<String> group = null;
		
		int VERBOSE = CommandEntitySelector.VERBOSE;
		if(VERBOSE == 1)
			group = new TObjectIntHashMap<>();
		
		boolean color = sender instanceof EntityPlayer;
		if(color) {
			builder.append("\u00a77Killed \u00a76").append(results.size()).append(" \u00a77Entities");
		}
		else {
			builder.append("Killed ").append(results.size()).append(" Entities");
		}
		
		
		if(VERBOSE != 0) {
			builder.append(" {");
		}
		while(it.hasNext()) {
			Entity entity = it.next();
			killEntity(entity, force, drop);
			if(VERBOSE != 0) {
				String eName = EntityList.getEntityString(entity);
				
				if(eName == null) {
					eName = entity.getCommandSenderName();
				}
				
				
				if(VERBOSE == 1) {
					group.adjustOrPutValue(eName, 1, 1);
				}
				else if(VERBOSE == 2) {
					builder.append(eName).append('/').append(entity.dimension).append('[').append((int) entity.posX).append(' ').append((int) entity.posY).append(' ').append((int) entity.posZ).append(']');
					if(it.hasNext())
						builder.append(", ");
				}
			}
		}
		
		if(VERBOSE == 1) {
			Object[] keys = group.keys();
			for(int i = 0; i < keys.length; i++) {
				Object key = keys[i];
				int amount = group.get(key);
				if(amount > 1) {
					if(color)
						builder.append("\u00a7f");
					
					builder.append(amount);
					if(color) {
						builder.append("x\u00a77");
					}
					else {
						builder.append("x");
					}
				}
				else if(color) {
					builder.append("\u00a77");
				}
					
				
				
				
				builder.append(key);
				
				if(i+1 != keys.length)
					builder.append(", ");
			}
		}
		
		
		if(VERBOSE != 0) {
			builder.append('}');
		}
			
		func_152373_a(sender, this, builder.toString());
		
		
	}

}
