package metweaks.features;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;

public class PlayerDeathLogger {
	
	public static void logDeath(EntityLivingBase player) {
		if(!player.worldObj.isRemote || (MinecraftServer.getServer() == null || !MinecraftServer.getServer().isServerRunning())) {
			FMLLog.getLogger().info(player.getCommandSenderName()+" died in "+player.worldObj.provider.getDimensionName()+" at "+MathHelper.floor_double(player.posX)+" "+MathHelper.floor_double(player.posY)+" "+MathHelper.floor_double(player.posZ));
		}
	}
	
	
}
