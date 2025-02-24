package metweaks.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class CommandEntityEffect extends CommandBase {
	
	public static TObjectIntMap<String> potions;
	
	public static void initPotionCache() {
		if(potions != null) return;
		
		// modern & corrected names
		potions = new TObjectIntHashMap<>();
		potions.put("mining_fatigue", Potion.digSlowdown.id);
		potions.put("haste", Potion.digSpeed.id);
		potions.put("slowness", Potion.moveSlowdown.id);
		potions.put("speed", Potion.moveSpeed.id);
		potions.put("strength", Potion.damageBoost.id);
		potions.put("jump_boost", Potion.jump.id);
		potions.put("instant_damage", Potion.harm.id);
		potions.put("instant_health", Potion.heal.id);
		potions.put("nausea", Potion.confusion.id);
		potions.put("night_vision", Potion.nightVision.id);
		potions.put("fire_resistance", Potion.fireResistance.id);
		potions.put("health_boost", Potion.field_76434_w.id);
		potions.put("water_breathing", Potion.waterBreathing.id);
		
		
		for(Potion potion : Potion.potionTypes) {
			if(potion != null && !potions.containsValue(potion.id)) {
				String name = potion.getName().replace("potion.", "").toLowerCase();
				if(name.length() != 0) {
					potions.put(name, potion.id);
				}
			}
		}
	}

	@Override
	public String getCommandName() {
		return "entityeffect";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getCommandAliases() {
		return Arrays.asList(new String[] { "eeffect" });
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/entityeffect <selector> [effect] [duration] [strength] [ambient] or <clear>";// [dimension]";// [yaw,pitch]  or cursor <cursor>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		initPotionCache();
		List results = new ArrayList<>();
		
		if(args.length == 1) {
			results = CommandEntitySelector.getSelectorTabs(sender, args);
		}
		results.addAll(getListOfStringsMatchingLastWord(args, potions.keys(new String[0])));
		return results;
		
		
		
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		
		Object[] data = CommandEntitySelector.selectEntities(0, args, sender);
		int offset = (int) data[1];
		List<Entity> results = (List<Entity>) data[0];
		
		if(offset == 0) {
			if(sender instanceof EntityLivingBase) {
				results.add((EntityLivingBase) sender);
			}
			else throw new CommandException(StatCollector.translateToLocal("commands.generic.notAnEntity"));
		}
		else {
			CommandEntityTp.getDistination(results, false);
		}
		
		int relativeOffset = args.length - offset;
		if(relativeOffset >= 1) {
			initPotionCache();
			String type = args[offset];
			
			boolean clear = false;
			
			int potionID = 1;
			int duration = 600;
			int amplifier = 0;
			
			boolean ambient = false;
			String display = null;
			
			if(type.equalsIgnoreCase("clear")) {
				clear = true;
			}
			else if(potions.containsKey(type)) {
				potionID = potions.get(type);
				display = type;
			}
			else {
				
				
				try {
					potionID = Integer.parseInt(type);
					int max = Potion.potionTypes.length - 1;
					if(potionID < 0) {
			            throw new NumberInvalidException("commands.generic.num.tooSmall", 0, max);
			        }
			        else if (potionID > max) {
			            throw new NumberInvalidException("commands.generic.num.tooBig", 0, max);
			        }
			        else if(!potions.containsValue(potionID)) {
						throw new CommandException("commands.effect.notFound", potionID);
					}
		        }
		        catch (NumberFormatException ex) {
		        	throw new CommandException(StatCollector.translateToLocalFormatted("commands.entityeffect.notFound", type));
		        }
				
				for(String name : potions.keys(new String[0])) {
					if(potions.get(name) == potionID) {
						display = name;
						break;
					}
				}
				
			}
			
			boolean instant = Potion.potionTypes[potionID].isInstant();
			
			if(instant) duration = 1;
			
			if(relativeOffset >= 2) {
				String length = args[offset+1];
				if(length.equalsIgnoreCase("clear")) {
					duration = 0;
				}
				else {
					duration = parseIntBounded(sender, length, 0, 1000000);
					if(!instant) {
						duration *= 20;
					}
				}
			}
			
			if(relativeOffset >= 3) {
				amplifier = parseIntBounded(sender, args[offset+2], 0, 255);
			}
			
			if(relativeOffset >= 4) {
				ambient = parseBoolean(sender, args[offset+3]);
			}
			
			
			boolean one = results.size() == 1;
			
			StringBuilder builder = new StringBuilder();
			Iterator<Entity> it = results.iterator();
			TObjectIntMap<String> group = null;
			
			int verbose = one ? 0 : CommandEntitySelector.VERBOSE;
			
			if(verbose == 1)
				group = new TObjectIntHashMap<>();
			
			boolean color = sender instanceof EntityPlayer;
			
			// Given Effect 'speed' (x3) for 20s to 200 Entities {...}
			// Given Effect 'speed' (x3) for 20s to Jonosa
			// Took All Effects from 200 Entities {...}
			// Took All Effects from Jonosa
			// Took Effect 'speed' from Jonosa
			// Took Effect 'speed' from 200 Entities {...}
			
			String tint = null;
			
			if(clear || duration == 0) {
				
				if(color) {
					tint = "\u00a7c";
					builder.append(tint);
					
				}
					
				builder.append("Took ");
				
					
			}
			else {
				if(color) {
					tint = "\u00a76";
					builder.append(tint);
					
				}
					
				builder.append("Given ");
				
					
			}
			
			if(clear) {
				builder.append("All Effects from ");
			}
			else{
				builder.append("Effect ").append('\'').append(display).append('\'');
				if(duration == 0) {
					builder.append(" from ");
				}
				else {
					// for 20s to
					if(amplifier != 0) {
						builder.append(" (x").append(amplifier+1);
						
						if(ambient) {
							builder.append(" ambient");
						}
						
						builder.append(')');
					}
					builder.append(" for ").append(duration/20).append('s');
					if(color)
						builder.append(tint);
					builder.append(" to ");
					
				}
			}
			
			if(color)
				builder.append(tint);
			
			if(one) {
				builder.append(results.get(0).getCommandSenderName());
			}
			else if(verbose != 0) {
				int amount = 0;
				for(Entity entity : results) {
					if(entity instanceof EntityLivingBase) amount++;
				}
				
				
				builder.append(amount);
				if(color) {
					builder.append(tint);
					builder.append(" Entities \u00a77{");
				}
				else{
					builder.append(" Entities {");
				}
				
				
				
			}
			
			while(it.hasNext()) {
				Entity entity = it.next();
				if(!(entity instanceof EntityLivingBase)) {
					if(one) throw new CommandException(StatCollector.translateToLocal("commands.entityeffect.requireAlive"));
					continue;
				}
					
				
				EntityLivingBase living = (EntityLivingBase) entity;
				if(clear)  {
					if(one && living.getActivePotionEffects().isEmpty()) {
						throw new CommandException("commands.effect.failure.notActive.all", living.getCommandSenderName());
					}
					living.clearActivePotions();
				}
				else if(duration == 0) {
					
					if(one && !living.isPotionActive(potionID)) {
						throw new CommandException("commands.effect.failure.notActive", display, living.getCommandSenderName());
					}
					living.removePotionEffect(potionID);
				}
				else {
					living.removePotionEffect(potionID);
					living.addPotionEffect(new PotionEffect(potionID, duration, amplifier, ambient));
				}
					
				
				
				
				if(verbose != 0) {
					String eName = EntityList.getEntityString(entity);
					
					if(eName == null) {
						eName = entity.getCommandSenderName();
					}
					
					
					if(verbose == 1) {
						group.adjustOrPutValue(eName, 1, 1);
					}
					else if(verbose == 2) {
						builder.append(eName).append('/').append(entity.dimension).append('[').append((int) entity.posX).append(' ').append((int) entity.posY).append(' ').append((int) entity.posZ).append(']');
						if(it.hasNext())
							builder.append(", ");
					}
				}
			}
			
			if(verbose == 1) {
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
			
			if(verbose != 0) {
				builder.append('}');
			}
				
			func_152373_a(sender, this, builder.toString());
		}
		else {
			
			sender.addChatMessage(new ChatComponentText(CommandEntitySelector.getSelectorTip()));
			throw new CommandException(getCommandUsage(sender));
		}

	}

}
