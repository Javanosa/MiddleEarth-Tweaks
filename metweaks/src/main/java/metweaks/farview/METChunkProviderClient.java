package metweaks.farview;

import lotr.common.LOTRDimension;
import lotr.common.world.biome.variant.LOTRBiomeVariantStorage;
import metweaks.MeTweaks;
import metweaks.farview.METLongHashSet.KeyNode;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class METChunkProviderClient implements IChunkProvider {
	public static final Logger logger = LogManager.getLogger();
    
    public final Chunk emptyChunk;
    
    public LongHashMap chunkMap = FastCraftHelper.getMapInstance();
    private final List<Chunk> tickableChunks = new ArrayList<Chunk>();
    public METLongHashSet unloadable;
    private ChunkIterator chunkIterator;
    
    public final World world;
    
    // settings
    public static boolean farViewEnabled = false;
    public static boolean checkDistance = true;
    public static int distance = 49;
    public static int distanceSq = distance * distance;
    public static int chunkThreshold = 0;
    public static int distanceTarget = distance;
    public static boolean keepLastWorld = true;
    public static boolean keepTileEntities = false;
    public static boolean tickUnloadable = false;
    public static int unloadsPerCycle = 200;
    public static long cycleDelay = 631L;
    public static boolean cacheVariants = true;
    public static boolean forceUnloadEntities = false;
    public static boolean cubicDistance = false; 
    
    // keep after rejoin
    public static LongHashMap lastChunkMap;
    
    // last world data
    public static int lastDimension = -9999;
    public static String lastWorldName;
    
    // player location may be unset if nothing received yet
    public boolean received = false;
    public long nextCheck;
    private int unloaded;
    
    private static Field field_netmanager;
    
    public static String getServerName(Minecraft mc) {
    	
    	NetworkManager netmanager = null;
    	
    	GuiScreen gui = mc.currentScreen;
    	
        if(gui instanceof GuiConnecting) {
        	if(field_netmanager == null) {
        		field_netmanager = ReflectionHelper.findField(GuiConnecting.class, "field_146371_g");
        	}
        	
        	
        		
        	try {
        		netmanager = (NetworkManager) field_netmanager.get(gui);
        	}
        	catch(Exception e) {
        		e.printStackTrace();
        	}
        }
        
        if(netmanager == null) {
    		netmanager = FMLClientHandler.instance().getClientToServerNetworkManager();
    	}
    	
    	
    	if(netmanager != null) {
    		SocketAddress address = netmanager.getSocketAddress();
    		if(address != null && address instanceof InetSocketAddress) {
    			InetSocketAddress inet = (InetSocketAddress) address;
                return inet.getHostString()+'_'+inet.getPort();
    		}
    	}
    	
        // in case of fallback give us a unique random name, dont save to folder in this case either
     	return String.valueOf(System.currentTimeMillis());
     }
    
    
    
    public String getLastWorldName() {
    	Minecraft mc = Minecraft.getMinecraft();
    	if(mc.isSingleplayer()) {
    		return "SP_" + mc.getIntegratedServer().getFolderName();
    	}
    	return "MP_" + getServerName(mc);
    } 
    
    public void setLastWorldData(String worldname) {
    	lastDimension = world.provider.dimensionId;
    	lastChunkMap = chunkMap;
    	lastWorldName = worldname;

    	logger.info("LastWorldData: "+lastChunkMap+" "+lastDimension+" "+lastWorldName);
    	
    	
    	
    }
    
    public boolean checkLastWorldData(World world, String worldname) {
    	return lastDimension == world.provider.dimensionId && worldname.equals(lastWorldName);
    }
    
    public static void resetLastWorldData() {
    	lastChunkMap = null;
    	lastDimension = -9999;
    	lastWorldName = null;
    }
    
    
    
    public void loadLastWorldData() {
    	chunkMap = lastChunkMap;
		unloadable.ensureCapacity(lastChunkMap.getNumHashElements());
		chunkIterator = FastCraftInfo.getChunkIterator(chunkMap);
		chunkIterator.iterateLastWorld(world, unloadable);	
    }
    
    private static Field field_chunkVariantMapClient;
    
    public static void unloadBiomeVariantsForPrevDim() {
    	for(LOTRDimension dim : LOTRDimension.values()) {
    		if(dim.dimensionID == lastDimension) {
    			if(field_chunkVariantMapClient == null) {
    				field_chunkVariantMapClient = ReflectionHelper.findField(LOTRBiomeVariantStorage.class, "chunkVariantMapClient");
    			}
    			
    			try {
    				@SuppressWarnings({ "unchecked", "rawtypes" })
					Map map = ((Map<LOTRDimension, Map>) field_chunkVariantMapClient.get(null)).get(dim);
        			if(map != null)
        				map.clear();
    			}
    			catch(Exception e) {
    				e.printStackTrace();
    			}
    			return;
    		}
    	}
    }

	public METChunkProviderClient(World world) {
		this.emptyChunk = new EmptyChunk(world, 0, 0);
		this.world = world;

		if(farViewEnabled) {
			unloadable = new METLongHashSet();

			if(keepLastWorld) {
				String worldname = getLastWorldName();
				if(checkLastWorldData(world, worldname)) {
					loadLastWorldData();
				}
				else {
					if(MeTweaks.lotr)
						unloadBiomeVariantsForPrevDim();
					setLastWorldData(worldname);
				}

			}


		}

		if(chunkIterator == null)
			chunkIterator = FastCraftInfo.getChunkIterator(chunkMap);
	}

	private void unloadChunk(Chunk chunk, long pos, int x, int z) {


		//Chunk chunk = (Chunk) this.chunkMap.getValueByKey(pos);
		if(chunk != null) {
			if(!chunk.isEmpty()) {
				chunk.onChunkUnload();
				if(MeTweaks.celeritasVers > 0) {
					if(MeTweaks.celeritasVers == 1) {
						CeleritasCallsOld.unloadChunk(world, x, z);
					}
					else {
						CeleritasCalls.unloadChunk(world, x, z);
					}
					
				}
				
			}

			this.chunkMap.remove(pos);
		}
	}

	public void unloadChunk(int x, int z) {

		long pos = ChunkCoordIntPair.chunkXZ2Int(x, z);
		Chunk chunk = (Chunk) chunkMap.getValueByKey(pos);
		tickableChunks.remove(chunk);
		if(farViewEnabled) {
			if(received) {
				if(MeTweaks.celeritasVers == 1) CeleritasCallsOld.preventUnload = true;

				// fix chunks getting removed from unload packets of previous dimension even if fresh chunk map
				this.unloadable.add(pos);

				if(forceUnloadEntities) {
					//Chunk chunk = (Chunk) this.chunkMap.getValueByKey(pos);
					if(chunk != null)  {
					
						for (int i = 0; i < chunk.entityLists.length; ++i) {
							chunk.worldObj.unloadEntities(chunk.entityLists[i]);
							
							chunk.entityLists[i].clear();

						}

					}
				}
			}
		}
		else {
			unloadChunk(chunk, pos, x, z);
		}
	}



	@Override
	public Chunk loadChunk(int x, int z) {
		Chunk chunk = new Chunk(this.world, x, z);
		tickableChunks.add(chunk);
		long pos = ChunkCoordIntPair.chunkXZ2Int(x, z);


		//Bucket entry = this.chunkMap.getEntry(pos);

		if(farViewEnabled && this.unloadable.contains(pos)) {

			this.unloadable.remove(pos);
			Chunk chunkold = (Chunk) this.chunkMap.getValueByKey(pos);
			if(chunkold != null) {
				// copy data from old chunk to new chunk
				chunk.entityLists = chunkold.entityLists;
				chunk.chunkTileEntityMap = chunkold.chunkTileEntityMap;
				chunk.hasEntities = chunkold.hasEntities;

			}

		}

		//entry.value = chunk;

		this.chunkMap.add(pos, chunk);



		//if(farViewEnabled)
		//	this.unloadable.remove(pos);
		MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
		chunk.isChunkLoaded = true;
		received = true;
		if(MeTweaks.celeritasVers >= 2) {
			CeleritasCalls.loadChunk(world, x, z);
		}

		return chunk;
	}

    @Override
    public Chunk provideChunk(int x, int z) {
    	Chunk chunk = (Chunk) this.chunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
    	return chunk == null ? this.emptyChunk : chunk;
    }

	// runs every tick
	@Override
	public boolean unloadQueuedChunks() {
		final long i = System.currentTimeMillis();

		for(final Chunk chunk : tickableChunks) {
			chunk.func_150804_b(System.currentTimeMillis() - i > 5L);
		}

		//chunkIterator.iterateTick(unloadable, i);



		final long current = System.currentTimeMillis();

		if (current - i > 100L) {
			logger.info("Warning: Clientside chunk ticking took {} ms", current - i);
		}

		if(farViewEnabled && checkDistance && received && nextCheck < current) {
			update(false);

			nextCheck = current + cycleDelay;
		}

		return false;
	}
    
    private void markRender(long pos) {
    	int ux = (int) (pos & 0xFFFFFFFF);
		int uz = (int) (pos >>> 32);
		//world.markBlockRangeForRenderUpdate(ux * 16 - 1, 0, uz * 16 - 1, 	ux * 16 + 14, 256, uz * 16 + 14);
		
		world.markBlockRangeForRenderUpdate(ux * 16 + 1, 1, uz * 16 + 1, 	ux * 16 + 14, 254, uz * 16 + 14);
    }
    
    private static boolean tooClose;
    
    private void remove(final boolean clear, final long pos) {
    	final int x = (int) (pos & 0xFFFFFFFF);
    	final int z = (int) (pos >>> 32);
    	
    	if(!clear) { 
    		if(unloaded >= unloadsPerCycle) return;
	    		
    		unloaded++;
    	}
    	this.unloadChunk((Chunk) chunkMap.getValueByKey(pos), pos, x, z);
		unloadable.removeKey(pos);
		if(MeTweaks.lotr && cacheVariants) {
			LOTRBiomeVariantStorage.clearChunkBiomeVariants(world, 
				new ChunkCoordIntPair(x, z));
		}

		if(clear || tooClose) {
			markRender(pos);
		}
    }
    
    
    
    public void update(boolean clear) {
    	
    	
    	final boolean cubic = cubicDistance;
    	int minDistance = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
    	if(cubic) {
    		tooClose = distance < minDistance;
    	}
    	else {
    		minDistance++; // +1 because of center chunk and +1 for compare
    		//tooClose = ((minDistance + 1) * (minDistance + 1)) > (distanceSq * 2);
    		tooClose = distance <= minDistance;
    		
    		
    	}
    	
    	
    	
    	
    	/*if(distance < minDistance) {
    		distanceTarget = Math.max(distanceTarget, distance = minDistance);
	    	distanceSq = minDistance * minDistance;
    	}*/
    	//clear = clear || distance < minDistance;
    	
    	if(!clear && chunkThreshold > 0) {
	    	if(unloadable.size() <= chunkThreshold) {
	    		// increase distance by 1 until desired
	    		if(distance < distanceTarget) {
		    		distance = distanceTarget;
			    	distanceSq = distance * distance;
	    		}
	    		return;
	    	}
	    	// decrease distance by 1 each call, as we removed everything we can in last cycle
	    	if(unloaded < unloadsPerCycle) {
		    	distance = Math.max(distance--, minDistance);
		    	distanceSq = distance * distance;
	    	}
    	}
    	
    	unloaded = 0;
    	
    	EntityPlayer player = Minecraft.getMinecraft().thePlayer;
    	final int centerX = player.chunkCoordX;
    	final int centerZ = player.chunkCoordZ;
    	final int dsq = distanceSq;
    	final int d = distance;
    	
    	final KeyNode[] table = unloadable.hashArray;
    	for(KeyNode item : table) {
    		
        	while(item != null) {
        		final long pos = item.key;
            	final int x = (int) (pos & 0xFFFFFFFF) - centerX;
            	final int z = (int) (pos >>> 32) - centerZ;
        		if((!cubic ? (x * x + z * z > dsq) : (x < -d || x > d || z < -d || z > d)) || clear) {
        			remove(clear, pos);
        		}
        		item = item.nextEntry;
        	}
        }
    }
    
    

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public void populate(IChunkProvider provider, int x, int z) {}

    @Override
    public String makeString() {
    	int total = this.chunkMap.getNumHashElements();
    	int unloadable = farViewEnabled ? this.unloadable.size() : 0;
    	int server = total - unloadable;
        return "ChunkCache: " + total + ", "+unloadable+", "+server;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public List getPossibleCreatures(EnumCreatureType creatureType, int x, int y, int z) {
        return null;
    }

    @Override
    public ChunkPosition func_147416_a(World world, String structure, int x, int y, int z) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return this.chunkMap.getNumHashElements();
    }
    
    @Override
    public boolean chunkExists(int x, int z) {
        return true;
    }

    @Override
    public void recreateStructures(int x, int z) {}
    
    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return true;
    }

    @Override
    public void saveExtraData() {}
    
    
}