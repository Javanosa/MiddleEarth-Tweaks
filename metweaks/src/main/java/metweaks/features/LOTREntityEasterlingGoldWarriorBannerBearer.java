package metweaks.features;

import lotr.common.entity.npc.LOTRBannerBearer;
import lotr.common.entity.npc.LOTREntityEasterlingGoldWarrior;
import lotr.common.item.LOTRItemBanner.BannerType;
import net.minecraft.world.World;

public class LOTREntityEasterlingGoldWarriorBannerBearer extends LOTREntityEasterlingGoldWarrior implements LOTRBannerBearer {

	public LOTREntityEasterlingGoldWarriorBannerBearer(World world) {
		super(world);
		this.canBannerBearerSpawnRiding = true;
	}

	@Override
	public BannerType getBannerType() {
		return BannerType.RHUN;
	}

}
