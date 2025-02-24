package metweaks.command;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;

public class CommandEntityTp extends CommandBase {

	@Override
	public String getCommandName() {
		return "entitytp";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getCommandAliases() {
		return Arrays.asList(new String[] { "etp" });
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/etp <selector> <selector teleportTo> or specific [x] [y] [z] or <cursor>";// [dimension]";// [yaw,pitch]  or cursor <cursor>";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return CommandEntitySelector.getSelectorTabs(sender, args);
	}

	public static void tpEntity(Entity entity, double x, double y, double z, float yaw, float pitch, int dim) {
		
		if(entity.ridingEntity != null) {
			tpEntity(entity.ridingEntity, x, y, z, yaw, pitch, dim);
			return;
		}

		
		if(entity instanceof EntityPlayerMP) {
			((EntityLivingBase) entity).setPositionAndUpdate(x, y, z);
		}
		else {
			
			entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
			// setPositionAndRotation
		}
		
		entity.fallDistance = 0;
		//entity.entityCollisionReduction = 1f;
		entity.motionY = 0;
		entity.motionX = 0;
		entity.motionZ = 0;
		entity.onGround = true;
		
		entity.worldObj.updateEntityWithOptionalForce(entity, false);
		
	}

	public static Entity getDistination(List<Entity> list, boolean setDestination) {
		if (list.size() > 0) {
			if (setDestination) {
				// teleport sender to this
				return list.get(0);
			}
		} else
			throw new CommandException(StatCollector.translateToLocal("commands.generic.entity.notFound"));

		return null;
	}

	public static String fancyNumber(double num) {
		String strX = "" + Math.round(num * 100.0) / 100.0;
		if (strX.endsWith(".0"))
			strX = strX.substring(0, strX.length() - 2);
		return strX;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		Object[] data = CommandEntitySelector.selectEntities(0, args, sender);
		
		int offset = (int) data[1];
		boolean teleportSender = offset == 0 || offset == args.length;

		/*
		 * 
		 * if no selector or only one /tp x y z /tp <selector> /tp <selector> x y z /tp
		 * <selector> <selector>
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		double x = 0;
		double y = 0;
		double z = 0;
		int dim = sender.getEntityWorld().provider.dimensionId;
		float yaw = -999;
		float pitch = -999;
		boolean secondSelector = false;
		List<Entity> results = (List<Entity>) data[0];
		Entity destination = null;
		// indicates that the first selector found smth
		if (offset >= 1) {
			destination = getDistination(results, teleportSender);

			if (!teleportSender) {
				Object[] dataTeleportTo = CommandEntitySelector.selectEntities(offset, args, sender);
				int secondOffset = (int) dataTeleportTo[1];
				
				// we found smth!
				if (secondOffset > offset) {
					secondSelector = true;
					destination = getDistination((List<Entity>) dataTeleportTo[0], true);
				}
			}

		}

		// has x y z at after first offset
		if (!secondSelector && args.length >= offset + 3) {
			ChunkCoordinates pos = sender.getPlayerCoordinates();
			// parse relative and absolute coords
			x = func_110666_a(sender, pos.posX + 0.5, args[offset]);
			y = func_110666_a(sender, pos.posY, args[offset + 1]);
			z = func_110666_a(sender, pos.posZ + 0.5, args[offset + 2]);

		} else if (destination != null) {
			x = destination.posX;
			y = destination.posY;
			z = destination.posZ;
			dim = destination.dimension;
			
		} 
		else if(args.length >= offset + 1 && args[offset].equalsIgnoreCase("cursor")) {
			Entity self = CommandEntitySelector.getSelf(sender);
			if(self != null) {
				double distance = Math.max(MinecraftServer.getServer().getConfigurationManager().getEntityViewDistance(), 32);
				
				Vec3 vecOrigin = Vec3.createVectorHelper(self.posX, self.posY + self.getEyeHeight(), self.posZ);
			    
			    
			    float f1 = MathHelper.cos(-self.rotationYaw * 0.017453292F - (float) Math.PI);
	            float f2 = MathHelper.sin(-self.rotationYaw * 0.017453292F - (float) Math.PI);
	            float f3 = -MathHelper.cos(-self.rotationPitch * 0.017453292F);
	            float f4 = MathHelper.sin(-self.rotationPitch * 0.017453292F);
	            Vec3 vecGoal = vecOrigin.addVector((f2 * f3) * distance, f4 * distance, (f1 * f3) * distance);
	            boolean stopfluids = true;
	            boolean checkBlockCollision = false;
	            boolean stopEntityCollision = false;
				MovingObjectPosition raytraceResult = self.worldObj.func_147447_a(vecOrigin, vecGoal, stopfluids, checkBlockCollision, stopEntityCollision);
				
				if(raytraceResult != null) {
					x = raytraceResult.hitVec.xCoord;
					y = raytraceResult.hitVec.yCoord;
					z = raytraceResult.hitVec.zCoord;
					Block block = self.worldObj.getBlock(raytraceResult.blockX, raytraceResult.blockY, raytraceResult.blockZ);
					if (!block.isReplaceable(self.worldObj, raytraceResult.blockX, raytraceResult.blockY, raytraceResult.blockZ)) {
			     		
			        	 switch(raytraceResult.sideHit) {
				     		case 0: --y; break;
				     		//case 1: ++y; break;
				     		case 2: --z; break;
				     		case 3: ++z; break;
				     		case 4: --x; break;
				     		case 5: ++x; break;
				     	}
			         }
				}
				else {
					// take air position
					x = vecGoal.xCoord;
					y = vecGoal.yCoord;
					z = vecGoal.zCoord;
				}
				
				
				dim = self.dimension;
				
				
				
			}
			else throw new CommandException(StatCollector.translateToLocal("commands.generic.notAnEntity"));
			
			
		}
		else {
			
			sender.addChatMessage(new ChatComponentText(CommandEntitySelector.getSelectorTip()));
			throw new CommandException(getCommandUsage(sender));
		}

		

		if (teleportSender) {
			Entity self = CommandEntitySelector.getSelf(sender);
			
			if(self != null) {
				
				
				results.clear();
				results.add(self);
					
			}
			else throw new CommandException(StatCollector.translateToLocal("commands.generic.notAnEntity"));
		}
		
		String strX = fancyNumber(x);
		String strY = fancyNumber(y);
		String strZ = fancyNumber(z);
		
		boolean one = results.size() == 1;
		
		StringBuilder builder = new StringBuilder();
		Iterator<Entity> it = results.iterator();
		TObjectIntMap<String> group = null;
		
		int verbose = one ? 0 : CommandEntitySelector.VERBOSE;
		
		if(verbose == 1)
			group = new TObjectIntHashMap<>();
		
		boolean color = sender instanceof EntityPlayer;
		if(color) {
			builder.append("\u00a77Teleported \u00a76");
			
			if(one) {
				builder.append(results.get(0).getCommandSenderName()).append("\u00a77 to \u00a7f");
			}
			else {
				builder.append(results.size()).append(" \u00a77Entities to \u00a7f");
			}
			
			
			
			
			
		}
		else {
			builder.append("Teleported ");
			
			if(one) {
				builder.append(results.get(0).getCommandSenderName()).append(" to ");
			}
			else {
				builder.append(results.size()).append(" Entities to ");
			}
		}
		
		if(destination != null) {
			builder.append('"');
			builder.append(destination.getCommandSenderName());
			builder.append("\" ");
		}
		
		
		
		builder.append('(').append(strX).append(' ').append(strY).append(' ').append(strZ).append(')');// in ").append(strDim);
		if(yaw != -999) {
			String strYaw = fancyNumber(yaw);
			String strPitch = fancyNumber(pitch);
			builder.append(" with Rotation [").append(strYaw).append(", ").append(strPitch).append(']');
		}
		
		
		
		
		
		if(verbose != 0) {
			if(color) {
				builder.append(" \u00a77{");
			}
			else{
				builder.append(" {");
			}
		}
		
		
		
		while(it.hasNext()) {
			Entity entity = it.next();
			if(entity.dimension == dim)
				tpEntity(entity, x, y, z, yaw, pitch, dim);
			
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

}
