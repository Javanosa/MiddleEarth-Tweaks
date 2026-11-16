package metweaks.farview;

import java.lang.reflect.Field;
import java.util.Iterator;

import cpw.mods.fml.relauncher.ReflectionHelper;
import lotr.common.tileentity.LOTRTileEntityMobSpawner;
import lotr.common.tileentity.LOTRTileEntityWeaponRack;
import metweaks.MeTweaks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class TileEntitySyncer {
	private static Field field_rackEntity;
	private static Field field_spawnedMob;
	
	@SuppressWarnings("unchecked")
	public static void syncTileEntities(Chunk chunk, World world) {
		Iterator<TileEntity> iterator = chunk.chunkTileEntityMap.values().iterator();

        while (iterator.hasNext())
        {
        	TileEntity tile = iterator.next();
        	// update world objects
        	if(tile.hasWorldObj())
        		tile.setWorldObj(world);
        	
        	// clear editing players
        	//if(tile instanceof TileEntitySign) {
        	//	((TileEntitySign) tile).func_145912_a(null);
        	//}
        	
        	if(MeTweaks.lotr) {
        		//if(tile instanceof LOTRTileEntityBarrel) {
        		//	((LOTRTileEntityBarrel) tile).players.clear();
        		//}
        		if(tile instanceof LOTRTileEntityMobSpawner) {
            		if(field_spawnedMob == null) 
        				field_spawnedMob = ReflectionHelper.findField(LOTRTileEntityMobSpawner.class, "spawnedMob");
        			try {
						field_spawnedMob.set(tile, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
        		
        		}
        		//else if(tile instanceof LOTRTileEntitySign) {
        		//	((LOTRTileEntitySign) tile).setEditingPlayer(null);
        		//}
        		else if(tile instanceof LOTRTileEntityWeaponRack) {
        			if(field_rackEntity == null) 
        				field_rackEntity = ReflectionHelper.findField(LOTRTileEntityWeaponRack.class, "rackEntity");
        			try {
						field_rackEntity.set(tile, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
        		}
        	}
        }
	}
}
