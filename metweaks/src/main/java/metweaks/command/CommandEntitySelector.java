package metweaks.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import cpw.mods.fml.relauncher.ReflectionHelper;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTREntityNPCRideable;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import metweaks.MeTweaks;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class CommandEntitySelector extends CommandBase {
	
	public static boolean IGNORE_CASE;
	//public static boolean EVEN_COLOR;
	public static int VERBOSE = 1; // 1=amount, 2=amount and grouped types, 3=amount, types with id, name and position
	
	public static class Selector {
		public char operation;
		public String name;
		public boolean negative;
		@SuppressWarnings("rawtypes")
		public Class clazz;
		public boolean superType;
		//public boolean eventnpc;
		public boolean hired;
		public boolean invasionSpawned;
		public boolean tamed;
		public Object player;
		public static final Field customData = ReflectionHelper.findField(Entity.class, "customEntityData");
		public boolean csnSearch;
		/*public boolean itemSearch;
		public int itemMeta;*/
		
		
		@SuppressWarnings("rawtypes")
		public Selector(String token, ICommandSender sender, boolean players) {
			int begin = 0;
			int len = token.length();
			char operation = token.charAt(0);
			
			
			if(operation == '!') {
				this.negative = true;
				begin = 1;
				
				if(len > begin)
					operation = token.charAt(begin);
			}
			
			if(players) {
				
				this.csnSearch = true;
			}
			
			if(operation == '$' || operation == '%' || operation == '*') {
				begin++;
				if(len > begin)
					this.name = token.substring(begin).toLowerCase();
				this.operation = operation;
			}
			else if(players) {
				if(len > begin)
					this.name = token.substring(begin);
			}
			else {
				if(len > begin)
					token = token.substring(begin);
				
					
				
				char searchtype = token.charAt(0);
				
				if(searchtype == '#') {
					switch(token) {
						case "#npc":
							if(MeTweaks.lotr) {
								clazz = LOTREntityNPC.class;
								superType = true;
								return;
							}
							break;
						case "#hired":
							if(MeTweaks.lotr) {
								clazz = LOTREntityNPC.class;
								superType = true;
								hired = true;
								if(sender instanceof EntityPlayer) {
									player = sender;
								}
								return;
							}
							break;
						/*case "#eventnpc":
							clazz = LOTREntityNPC.class;
							superType = true;
							eventnpc = true;
							return;*/
						case "#invasionnpc":
							if(MeTweaks.lotr) {
								clazz = LOTREntityNPC.class;
								superType = true;
								invasionSpawned = true;
								return;
							}
							break;
						case "#alive":
							clazz = EntityLivingBase.class;
							superType = true;
							return;
						case "#tamed":
							// EntityWolf
							// EntityOcelot
							tamed = true;
							if(sender instanceof EntityPlayer) {
								player = sender;
							}
							
							
							
							return;
						default:
							throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.unknownGroup", token));
						
					}
				}
				
				
				
				
				
				if(IGNORE_CASE) {
					name = token.toLowerCase();
				}
				else if(EntityList.stringToClassMapping.containsKey(token)) {
					clazz = (Class) EntityList.stringToClassMapping.get(token);
				}
				else {
					throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.unknown", token));
				}
				
			}
			
			

			
			
			if(len <= begin)
				throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.invalid", token));
				
				
			
		}
		
		public int valid(boolean valid) {
			return valid ? negative ? 2 : 1 : 0;
		}
		
		@SuppressWarnings("unchecked")
		public int apply(Entity entity) {
			if(operation != 0) {
				String entityName = csnSearch ? entity.getCommandSenderName() : EntityList.getEntityString(entity);
				if(entityName != null) {
					entityName = entityName.toLowerCase();
					switch(operation) {
						case '$': // end
							return valid(entityName.endsWith(name));
						case '%': // end
							return valid(entityName.contains(name));
						case '*': // end
							return valid(entityName.startsWith(name));
					}
				}
			}
			else {
				// players commandsendername
				// items class  commandsendername 			itemdamage
				// ignorecase 	name
				// supertype 	clazz
				if(csnSearch) {
					return valid(entity.getCommandSenderName().equalsIgnoreCase(name));
					
				}
				
				if(tamed) {
					boolean found = false;
					if(entity instanceof EntityHorse) {
						EntityHorse horse = (EntityHorse) entity;
						found = horse.isTame() && (!MeTweaks.lotr || !(horse instanceof LOTREntityHorse) || !((LOTREntityHorse) horse).getBelongsToNPC());
						if(found && player != null) {
							String str = horse.func_152119_ch();
							if(str.length() != 0) {
								UUID uuid = UUID.fromString(str);
								found = uuid != null && horse.worldObj.func_152378_a(uuid) == player;
							}
							else found = false;
							
						}
						
					}
					else if(entity instanceof EntityTameable) {
						EntityTameable tameable = (EntityTameable) entity;
						found = tameable.isTamed();
						if(found && player != null) {
							found = tameable.getOwner() == player;
						}
						
					}
					else if(MeTweaks.lotr && entity instanceof LOTREntityNPCRideable) {
						LOTREntityNPCRideable rideable = (LOTREntityNPCRideable) entity;
						found = rideable.isNPCTamed();
						if(found && player != null) {
							found = rideable.getTamingPlayer() == player;
						}
						
					}
					return valid(found);
				}
				/*if(itemSearch) {
					if(entity.getClass() == EntityItem.class && entity.getCommandSenderName().equalsIgnoreCase(name)) {
						EntityItem item = (EntityItem) entity;
						if(itemMeta != Integer.MIN_VALUE) {
							return valid(item.getEntityItem().getItemDamage() == itemMeta);
						}
						return valid(true);
					}
					return 0;
				}*/
				
				boolean valid = IGNORE_CASE && clazz == null ? name.equalsIgnoreCase(EntityList.getEntityString(entity)) : superType ? clazz.isAssignableFrom(entity.getClass()) : entity.getClass() == clazz;
				if(valid) {
					if(invasionSpawned) {
						return valid(((LOTREntityNPC) entity).getInvasionID() != null);
					}
					else if(hired) {
						LOTRHiredNPCInfo info = ((LOTREntityNPC) entity).hiredNPCInfo;
						return valid(info.isActive && (player == null || info.getHiringPlayer() == player));
					}
					/*else if(eventnpc) {
						
						boolean has = false;
						try {
							has = customData.get(entity) != null;
						} catch (Exception e) {}
						
						return valid(has && entity.getEntityData().hasKey("WaveSpawn"));
						
					}*/
				}
				return valid(valid);
			}
			return 0; // neutral
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static Class class_cmdcartlogic;
	
	public static Entity getSelf(ICommandSender sender) {
		if(sender instanceof Entity) {
			return (Entity) sender;
		}
		
		if(class_cmdcartlogic == null) {
			try {
				class_cmdcartlogic = Class.forName("net.minecraft.entity.EntityMinecartCommandBlock$1");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if(sender.getClass() == class_cmdcartlogic) {
			@SuppressWarnings("unchecked")
			List<Entity> carts = sender.getEntityWorld().loadedEntityList;
			for(Entity cart : carts) {
				if(cart.getClass() == EntityMinecartCommandBlock.class) {
					if(sender == ((EntityMinecartCommandBlock) cart).func_145822_e()) {
						return cart;
					}
				}
			}
			
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	// returns [List<Entity, newArgsOffset] 		
	// newArgsOffset == offsetStart indicates no selector
	// returns no selector if no valid @erpas and (first arg len less than 3) or no player found
	
	// where selector recommend @e @a @p @r @s and all online players
	
	// dont throw error if no selector found
	public static Object[] selectEntities(int offsetStart, String[] args, ICommandSender sender) {
		if(args.length > offsetStart && args[offsetStart].length() >= 2) {
			if(args[offsetStart].charAt(0) == '@') {
				boolean players = false;
				int argsindex = offsetStart;
				int limit = -1;
				int sort = -1;
				
				char selectorOperand = args[offsetStart].charAt(1);
				switch(selectorOperand) {
					case 'e':
						players = false;
						break;
					case 'a':
						players = true;
						break;
					case 'p':
						limit = 1;
						sort = 1;
						//args = new String[] {"@a[sort=nearest", "limit=1]"};
						players = true;
						break;
						//return getNearestPlayer();
					case 'r':
						limit = 1;
						sort = 2;
						//args = new String[] {"@a[sort=random", "limit=1]"};
						players = true;
						break;
						//return getRandomPlayer();
					case 's':
						Entity self = getSelf(sender);
						
						if(self != null) {
							List<Entity> selflist = new ArrayList<>();
							selflist.add(self);
							return new Object[] {selflist, argsindex+1};
						}
						else throw new CommandException("@s (@self) needs to be an entity");
						
					default:
						return new Object[] {new ArrayList<>(), offsetStart};
						
				}
				
				
				
				
				double radius = -1;
				
				boolean global = false;
				boolean all = true;
				int dim = Integer.MAX_VALUE;
				
				
				ChunkCoordinates pos = sender.getPlayerCoordinates();
				int x = pos.posX;
				int y = pos.posY;
				int z = pos.posZ;
				
				List<Selector> selectors = new ArrayList<>();
				
				
				if(args[offsetStart].length() >= 3) {
					char bracketBegin = args[offsetStart].charAt(2);
					if(bracketBegin == '[') {
						int end = -1;
						
						for(argsindex = offsetStart; argsindex < args.length; argsindex++) {
							
							
							String token = args[argsindex];
							
							if(argsindex == offsetStart) {
								token = token.substring(3);
							}
							
							
							
							if(end == -1) {
								end = token.indexOf(']');
								if(end != -1) {
									token = token.substring(0, end);
								}
							}
								
							
							
							
							int split = token.indexOf('=');
							if(split != -1) {
								String key = token.substring(0, split);
								String value = token.substring(split+1);
								
								
								
								switch(key) {
									case "r":
										radius = parseDoubleBounded(sender, value, 0, 10000);
										break;
									case "limit":
										limit = parseIntBounded(sender, value, 0, 100000);
										break;
									case "sort":
										if(value.equalsIgnoreCase("nearest")) {
											sort = 1;
										}
										else if(value.equalsIgnoreCase("random")) {
											sort = 2;
										}
										else {
											throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.unknownArgValue", value, token));
										}
										break;
									case "dim":
										if(value.equalsIgnoreCase("all")) {
											global = true;
										}
										else {
											dim = parseInt(sender, value);
										}
										//global = parseBoolean(sender, value);
										break;
									default:
										throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.unknownArg", key, token, "r(radius) | limit | sort"/* | dim(dimension)"*/));
								}
							}
							else {
								
								if(token.length() != 0) {
									String[] tokens = token.split(",");
									
									for(String stoken : tokens) {
										if(stoken.length() == 0) continue;
										
										Selector selector = new Selector(stoken, sender, players);
										if(!selector.negative) {
											all = false;
										}
										//if(!selector.invalid)
											selectors.add(selector);
									}
								}
							}
							
							if(end != -1) {
								break;
							}
						}
						
						if(end == -1) {
							throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.missingCloseBracket", args[Math.max(0, argsindex-1)]));
						}
						
					}
					else {
						throw new CommandException(StatCollector.translateToLocalFormatted("commands.selector.missingOpenBracket", args[Math.max(0, offsetStart-1)]));
					}
				}
				
				
				
				
				
				List<Entity> results = new ArrayList<>();
				
				
				
				
				World[] worlds;
				
				if(global && !players) {
					worlds = MinecraftServer.getServer().worldServers;
					
				}
				else {
					World world = sender.getEntityWorld();
					if(dim != Integer.MAX_VALUE) {
						world = MinecraftServer.getServer().worldServerForDimension(dim);
						if(world == null) {
							throw new CommandException(StatCollector.translateToLocalFormatted("commands.generic.dim.notFound", dim));
						}
					}
					worlds = new World[] {world};
				}	
				
				boolean distanceCheck = (players && radius > 0) || radius > 500;
				
				for(World world : worlds) {
					
					
					List<Entity> entityList;
					if(radius != -1 && !distanceCheck) {
						entityList = world.getEntitiesWithinAABB(/*players ? EntityPlayer.class : */Entity.class, AxisAlignedBB.getBoundingBox(
								x - radius, y - radius, z - radius, 
								x + radius, y + radius, z + radius));
					}
					else if(players) {
						if(global)
							entityList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
						else
							entityList = world.playerEntities;
						
						
					}
					else {
						entityList = world.loadedEntityList;
					}
					
					
					
					
					
					
					for(Entity entity : entityList) {
						if(distanceCheck && entity.getDistanceSq(x, y, z) > radius) continue;
						// fix players being caught on @e
						
						if(/*all && */!players && entity.getClass() == EntityPlayerMP.class) continue;
						
						boolean match = all;
						for(Selector selector : selectors) {
							int result = selector.apply(entity);
							// negative match
							if(result == 2) {
								match = false;
								break;
							}
							
							// match
							if(result == 1)
								match = true;
							
							
							
							
							
						}
						
						
						
						if(match) {
							results.add(entity);
						}
					}
				
				}
				
				// SORT_NEAREST
				if(sort == 1) {
					Collections.sort(results, new Comparator<Entity>() {
		                public int compare(Entity left, Entity right) {
		                    double leftD = left.getDistanceSq(x, y, z);
		                    double rightD = right.getDistanceSq(x, y, z);
		                    return leftD < rightD ? -1 : (leftD > rightD ? 1 : 0);
		                }
		            });
				}
				// SORT_RANDOM
				else if(sort == 2) {
					Collections.shuffle(results);
				}
				
				if(limit != -1) {
					
					
					
					List<Entity> truncated = new ArrayList<>();
					for(int i=0; i < Math.min(limit, results.size()); i++) {
						truncated.add(results.get(i));
					}
					
					results = truncated;
				}
				
				return new Object[] {results, argsindex+1};
				
			}
			else {
				// one player by name
				EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[offsetStart]);
				
				
				if(player != null) {
					List<Entity> playerfound = new ArrayList<>();
					playerfound.add(player);
					return new Object[] {playerfound, offsetStart+1};
				}
			}
		}
		return new Object[] {new ArrayList<>(), offsetStart};
		
	}

	@Override
	public String getCommandName() {
		return "selector";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/selector <optional selector to show>";
	}
	
	public static String getSelectorTip() {
		return "\u00a76Entity Selector: Type /selector for How-To-Use";
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List getSelectorTabs(ICommandSender sender, String[] args) {
		List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		int offset = 5;
		int count = players.size();
		String[] tabs = new String[count + offset];
		for (int i = 0; i < count; i++) {
			tabs[i] = players.get(i).getCommandSenderName();
		}
		
		tabs[count] = "@e";
		tabs[count+1] = "@a";
		tabs[count+2] = "@r";
		tabs[count+3] = "@p";
		tabs[count+4] = "@s";

		

		return getListOfStringsMatchingLastWord(args, tabs);
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return getSelectorTabs(sender, args);
		
	}
	
	public static ChatComponentText makeButton(String display) {
		ChatComponentText node = new ChatComponentText("["+display+"] ");
		node.getChatStyle()
		
		.setColor(EnumChatFormatting.GOLD)
		.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, display.equals("x") ? "/selector" : "/selector page:"+display.toLowerCase()))
		.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(display)));
		
		
		return node;
	}
	
	public static String[] getSelectors() {
		return new String[] {"--- Selectors ---", "@a - players", "@e - entities", "@s - yourself", "@p - nearest player", "@r - random player", "or playername"};
	}
	
	public static String[] getArgs() {
		return new String[] {"--- Arguments ---", "limit = number", "sort = nearest | random", "r (radius) = number"/*, "dim = all | dimensionID"*/};
	}
	
	public static String[] getGroups() {
		return new String[] {"--- Groups ---", "#npc", "#invasionnpc", "#hired", "#alive", "#tamed"};
	}
	
	public static String[] getConditions() {
		return new String[] {"--- Conditions ---", "! = Not", "% = contains name", "* = starts with name", "$ = ends with name"};
	}
	
	public static String[] getEntityTypes() {
		return new String[] {"--- Entity Types ---", "use /summon, /lotr_summon or spawneggs for type name", "Examples: Cow, lotr.MordorOrc"};
	}
	
	/*public static ChatComponentText[] getSettings() {
		
		ChatComponentText node = new ChatComponentText("Verbose LvL: ");
		ChatComponentText node2 = new ChatComponentText("Verbose LvL: ");
		return null;
	}*/
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(args.length == 0) {
			sender.addChatMessage(makeButton("Settings")
				.appendSibling(makeButton("Groups")
				.appendSibling(makeButton("EntityTypes"))));
				
			sender.addChatMessage(makeButton("Conditions")
				.appendSibling(makeButton("Arguments"))
				.appendSibling(makeButton("Selectors"))
			);
			sender.addChatMessage(new ChatComponentText("\u00a77Example: @e[Cow,Sheep limit=4 sort=nearest r=9]"));
			sender.addChatMessage(new ChatComponentText("\u00a7c@e[<names split by comma> limit=<limit> r=<radius>]"));
			return;
		}
		
		
		
		Object[] data = CommandEntitySelector.selectEntities(0, args, sender);
		int offset = (int) data[1];
		
		if(offset > 0) {
			@SuppressWarnings("unchecked")
			List<Entity> results = (List<Entity>) data[0];
			CommandEntityTp.getDistination(results, false);
			
			StringBuilder builder = new StringBuilder();
			Iterator<Entity> it = results.iterator();
			TObjectIntMap<String> group = null;
			
			int VERBOSE = CommandEntitySelector.VERBOSE;
			if(VERBOSE == 1)
				group = new TObjectIntHashMap<>();
			
			boolean color = sender instanceof EntityPlayer;
			if(color) {
				builder.append("\u00a77Found \u00a76").append(results.size()).append(" \u00a77Entities");
			}
			else {
				builder.append("Found ").append(results.size()).append(" Entities");
			}
			
			
			if(VERBOSE != 0) {
				builder.append(" {");
			}
			while(it.hasNext()) {
				Entity entity = it.next();
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
		else if(args.length == 1) {
			String token = args[0];
			int split = token.indexOf(':');
			if(split != -1) {
				String key = token.substring(0, split);
				
				if(key.equalsIgnoreCase("page")) {
					String page  = token.substring(split+1);
					
					String[] out = null;
					
					switch(page) {
						case "selectors":
							out = getSelectors();
							break;
						case "groups":
							out = getGroups();
							break;
						case "entitytypes":
							out = getEntityTypes();
							break;
						case "arguments":
							out = getArgs();
							break;
						case "conditions":
							out = getConditions();
							break;
						case "settings":
							out = new String[] {"--- Settings ---", "", "Comming Soon", ""};
							break;
						default:
							throw new CommandException(getCommandUsage(sender));
					}
					
					sender.addChatMessage(makeButton("x")
							.appendSibling(makeButton("Settings"))
							.appendSibling(makeButton("Groups"))
							.appendSibling(makeButton("EntityTypes")));
							
						sender.addChatMessage(makeButton("Conditions")
							.appendSibling(makeButton("Arguments"))
							.appendSibling(makeButton("Selectors"))
						);
					
					for(int i=0; i < out.length; i++) {
						String str = out[i];
						sender.addChatMessage(new ChatComponentText((i == 0 ? "\u00a7d" : "\u00a77") +str));
					}
				}
			}
		}
		else throw new CommandException(getCommandUsage(sender));
		
		
		
	}
	
	

}
