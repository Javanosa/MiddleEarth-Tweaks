package metweaks.features;

import lotr.common.entity.npc.LOTRBannerBearer;
import lotr.common.entity.npc.LOTREntitySouthronChampion;
import lotr.common.item.LOTRItemBanner.BannerType;
import net.minecraft.world.World;

public class LOTREntitySouthronChampionBannerBearer extends LOTREntitySouthronChampion implements LOTRBannerBearer {

	public LOTREntitySouthronChampionBannerBearer(World world) {
		super(world);
		this.canBannerBearerSpawnRiding = true;
	}

	@Override
	public BannerType getBannerType() {
		return BannerType.NEAR_HARAD;
	}

}
