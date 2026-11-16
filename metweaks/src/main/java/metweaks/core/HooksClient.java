package metweaks.core;

import java.util.List;
import java.util.UUID;

import lotr.client.render.entity.LOTRNPCRendering;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.fellowship.LOTRFellowshipClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;

public class HooksClient {
	// preventIfNotBadRelationClient
	public static boolean preventIFNBClient(UUID hiringPlayerUUID, UUID targetHiringPlayerUUID) {
		EntityPlayer clientPlayer = Minecraft.getMinecraft().thePlayer;

		if(clientPlayer != null) {
			UUID uuid = clientPlayer.getUniqueID();
			boolean attackerUUID = hiringPlayerUUID.equals(uuid);

			if(attackerUUID || targetHiringPlayerUUID.equals(uuid)) {

				LOTRPlayerData playerData = LOTRLevelData.getData(clientPlayer);

				if(!playerData.isSiegeActive()) {
					List<LOTRFellowshipClient> fellowships = playerData.getClientFellowships();

					for(LOTRFellowshipClient fs : fellowships) {
						if(fs.getPreventHiredFriendlyFire() && fs.containsPlayer(attackerUUID ? targetHiringPlayerUUID : hiringPlayerUUID)) {
							return true; 
						}
					} 
				}
			}
		}
		return false;
	}

	public static void doRenderEnt(EntityLiving entity, double renderX, double renderY, double renderZ) {
		if(Minecraft.isGuiEnabled() && ((LOTREntityNPC) entity).hiredNPCInfo.getHiringPlayer() == RenderManager.instance.livingPlayer) {
			LOTRNPCRendering.renderHiredIcon(entity, renderX, renderY +1, renderZ);
			LOTRNPCRendering.renderNPCHealthBar(entity, renderX, renderY +1, renderZ);
		}
	}
}	
