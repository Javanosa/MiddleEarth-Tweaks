package metweaks.command;

import java.util.ArrayList;
import java.util.List;

import metweaks.physics.ExplosionPhysicsSettings;
import metweaks.physics.HandleExplosion;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class CommandExplosionPhysics extends CommandBase {

	@Override
	public String getCommandName() {
		return "explosionphysics";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/explosionphysics <option> <value>, press TAB to list options";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return MinecraftServer.getServer().isSinglePlayer() || super.canCommandSenderUseCommand(sender);
    }
	
	@Override
	public boolean isUsernameIndex(String[] args, int i) {
		return false;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		if(args.length == 1) {
			return getListOfStringsMatchingLastWord(args, "info", "physicsFactor", "maxPhysicsPerExplosion", "sizeFactor", "holeDepthFactor", "physicsDropChance", 
				"decreaseBigExplosions", "forceBlockDamage", "maxWargPlayerDistance", "particleFactor", "fastMoveImpl", "fastRenderImpl");
		}
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		
		if(args.length >= 1) {
			String option = args[0].toLowerCase();
			String msg = null;
			
			if(args.length >= 2) {
			
				switch(option) {
					case "physicsfactor":
						double factor = parseDoubleBounded(sender, args[1], 0.0, 1.0);
						HandleExplosion.explosionPhysicsFactor = (int) (100 * factor);
						HandleExplosion.updateParticleFactorActual();
						msg = "set physicsFactor = "+factor;
						break;
					case "maxphysicsperexplosion":
						HandleExplosion.maxPhysicsPerExplosion = parseInt(sender, args[1]);
						msg = "set maxphysicsperexplosion = "+HandleExplosion.maxPhysicsPerExplosion;
						break;
					
					case "sizefactor":
						HandleExplosion.explosionSizeFactor = (float) parseDouble(sender, args[1]);
						msg = "set sizeFactor = "+HandleExplosion.explosionSizeFactor;
						break;
					case "holedepthfactor":
						HandleExplosion.holeDepthFactor = (float) parseDouble(sender, args[1]);
						msg = "set holeDepthFactor = "+HandleExplosion.holeDepthFactor;
						break;
					case "physicsdropchance":
						HandleExplosion.fallingblockDropChance = (float) parseDouble(sender, args[1]);
						msg = "set physicsDropChance = "+HandleExplosion.fallingblockDropChance;
						break;
					case "maxwargplayerdistance":
						HandleExplosion.maxWargPlayerDistance = parseDouble(sender, args[1]);
						HandleExplosion.maxWargPlayerDistanceSq = HandleExplosion.maxWargPlayerDistance * HandleExplosion.maxWargPlayerDistance;
						msg = "set maxWargPlayerDistance = "+HandleExplosion.maxWargPlayerDistance;
						break;
					case "particlefactor":
						HandleExplosion.particleFactor = (float) parseDoubleBounded(sender, args[1], 0.0, 1.0);
						HandleExplosion.updateParticleFactorActual();
						msg = "set particleFactor = "+HandleExplosion.particleFactor;
						break;
					
				}
			}
			
			switch(option) {
				case "decreasebigexplosions":
					HandleExplosion.decreaseBigExplosions = !HandleExplosion.decreaseBigExplosions;
					msg = "set decreaseBigExplosions = "+HandleExplosion.decreaseBigExplosions;
					break;
				case "forceblockdamage":
					HandleExplosion.forceBlockDamage = !HandleExplosion.forceBlockDamage;
					msg = "set forceBlockDamage = "+HandleExplosion.forceBlockDamage;
					break;
				case "fastmoveimpl":
					HandleExplosion.fastMoveImpl = !HandleExplosion.fastMoveImpl;
					msg = "set fastMoveImpl = "+HandleExplosion.fastMoveImpl;
					break;
				case "fastrenderimpl":
					ExplosionPhysicsSettings.fastRenderImpl = !ExplosionPhysicsSettings.fastRenderImpl;
					msg = "set fastRenderImpl = "+ExplosionPhysicsSettings.fastRenderImpl;
					break;
				case "info":
					List<String> out = new ArrayList<>();
					out.add("\u00a7e---- ExplosionPhysics World Info ----");
					out.add("\u00a76enabled = \u00a77"+HandleExplosion.explosionPhysics);
					out.add("\u00a76physicsFactor = \u00a77"+HandleExplosion.explosionPhysicsFactor * 0.01);
					out.add("\u00a76maxPhysicsPerExplosion = \u00a77"+(int) HandleExplosion.maxPhysicsPerExplosion);
					out.add("\u00a76sizeFactor = \u00a77"+HandleExplosion.explosionSizeFactor);
					out.add("\u00a76holeDepthFactor = \u00a77"+HandleExplosion.holeDepthFactor);
					out.add("\u00a76physicsDropChance = \u00a77"+HandleExplosion.fallingblockDropChance);
					out.add("\u00a76maxWargPlayerDistance = \u00a77"+HandleExplosion.maxWargPlayerDistance);
					out.add("\u00a76particleFactor = \u00a77"+HandleExplosion.particleFactor);
					out.add("\u00a76decreaseBigExplosions = \u00a77"+HandleExplosion.decreaseBigExplosions);
					out.add("\u00a76forceBlockDamage = \u00a77"+HandleExplosion.forceBlockDamage);
					out.add("\u00a76fastMoveImpl = \u00a77"+HandleExplosion.fastMoveImpl);
					for(String str : out) {
						sender.addChatMessage(new ChatComponentText(str));
					}
					return;
				
			}
			
			
			if(msg != null) {
				ExplosionPhysicsSettings.needsSave = true;
				sender.addChatMessage(new ChatComponentText(msg));
			}
			else {
				throw new WrongUsageException(getCommandUsage(sender));
			}
		}
		else {
			
			HandleExplosion.explosionPhysics = !HandleExplosion.explosionPhysics;
			HandleExplosion.particleFactorActual = HandleExplosion.explosionPhysics ? HandleExplosion.updateParticleFactorActual() : 100;
			ExplosionPhysicsSettings.needsSave = true;
			sender.addChatMessage(new ChatComponentText("Explosion Physics: "+(HandleExplosion.explosionPhysics ? "\u00a7aON" : "\u00a7cOFF")));
		}
	}

}
