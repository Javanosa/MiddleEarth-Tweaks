package metweaks.core;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import cpw.mods.fml.client.FMLClientHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lotr.common.entity.LOTRMountFunctions;
import metweaks.ASMConfig;
import metweaks.MeTweaks;
import metweaks.features.Getters;
import metweaks.features.NetworkCalls;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;

public class HooksNetwork {
	public static int specialDebugMode = 8;
	/* MODES
	
	1 = early packet processing server
	2 = early packet processing client
	3 = priorisize player ticks
	4 = client movement prediction nms (only)
	5 = client movement prediction 
	6 = client movement prediction hybrid (only)
	7 = Disable old packet processing
	8 = Enable newpos patch
	
	*/
	
	/*
	
	- Consistent Player Updates
		- More continuous player updates, can result in 50ms less delay, ensures player updates happen at the right time
		- Serverside
		- (metweaks-ASM.properties -> "consistentPlayerUpdates")
	- The changes should not affect performance negatively, please message me if they do!
	
	*/
	
	public static int processPacketsAmount(boolean client, NetworkManager netManager) {
		if(specialDebugMode >= 7 && (!client || FMLClientHandler.instance().getClientToServerNetworkManager() != null)) {
			return -1;
		}
		return 1000;
		/*INetHandler netHandler = netManager.getNetHandler();
		boolean low = netHandler instanceof INetHandlerPlayServer;
		
		if(!low && netHandler instanceof INetHandlerPlayClient) {
			low = FMLClientHandler.instance().getClientToServerNetworkManager() != null;
		}
		return specialDebugMode >= 7 && low ? -1 : 1000;
		
		INetHandler netHandler = netManager.getNetHandler();
		
		boolean isPlayClient = netHandler instanceof INetHandlerPlayClient;
		if(!isPlayClient || (Minecraft.getMinecraft().getNetHandler() != null)) {
			return 1000;
		}
		return -1; // netManager.isChannelOpen() && specialDebugMode >= 7 ? -1 : 1000;*/
	}
	
	/*public static boolean checkDeltaSquared(int deltaX, int deltaY, int deltaZ) {
		return (deltaX * deltaX) + (deltaY * deltaY) + (deltaZ * deltaZ) >= 1;
	}*/
	
	private static final Logger log = LogManager.getLogger();
	
	private static final boolean isInvalidPlayerPacket(final C03PacketPlayer p) {
		final double x = p.func_149464_c();
		final double y = p.func_149467_d();
		final double z = p.func_149472_e();
		if(Doubles.isFinite(x) && Doubles.isFinite(y) && Doubles.isFinite(z) && Floats.isFinite(p.func_149470_h()) && Floats.isFinite(p.func_149462_g())) {
			return Math.abs(x) > 3.2E7D || Math.abs(y) > 3.2E7D || Math.abs(z) > 3.2E7D || Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
        }
		else {
			return true;
		}
	}
	
	// add at beginning of NetHandlerPlayServer.setPlayerLocation(DDDFF)V
	public static void markLastPosUpdate(NetHandlerPlayServer handler) {
		Getters.lastPositionUpdate(handler, Getters.networkTickCount(handler));
	}

	// add at beginning of NetHandlerPlayServer.onNetworkTick()V
	public static void onNetworkTick(NetHandlerPlayServer handler) {
		if(specialDebugMode < 8) {
			return;
		}
		captureCurrentPosition(handler);
		EntityPlayerMP player = handler.playerEntity;
		// fix 1.7.1ß
		if(player.playerNetServerHandler != null) {
			player.onUpdateEntity();
		}
		player.setPositionAndRotation(Getters.firstGoodX(handler), Getters.firstGoodY(handler), Getters.firstGoodZ(handler), player.rotationYaw, player.rotationPitch);
		Getters.lastMovePacketCounter(handler, Getters.movePacketCounter(handler));


		if(Getters.floating(handler)) {
			int floatingTickCount = Getters.floatingTickCount(handler) + 1;
			Getters.floatingTickCount(handler, floatingTickCount);
			if(floatingTickCount > 80) {
				log.warn(player.getCommandSenderName() + " was kicked for floating too long!");
				handler.kickPlayerFromServer("Flying is not enabled on this server");
				return;
			}
		}
		else {
			Getters.floating(handler, false);
			Getters.floatingTickCount(handler, 0);
		}
	}
	
	public static void captureCurrentPosition(NetHandlerPlayServer handler) {
		EntityPlayerMP player = handler.playerEntity;
		Getters.firstGoodX(handler, player.posX);
		Getters.firstGoodY(handler, player.posY);
		Getters.firstGoodZ(handler, player.posZ);
		Getters.lastPosZ(handler, player.posX);
		Getters.lastPosZ(handler, player.posY);
		Getters.lastPosZ(handler, player.posZ);
    }
		
	// clear method
	// add at beginning of NetHandlerPlayServer.processPlayer(Lnet/minecraft/network/play/client/C03PacketPlayer)V
	public static boolean processPlayer(NetHandlerPlayServer handler, C03PacketPlayer packetIn) {
		if(specialDebugMode < 8) {
			return false;
		}

		if(isInvalidPlayerPacket(packetIn)) {
			handler.kickPlayerFromServer("Illegal position");
			return true;
		}
		EntityPlayerMP player = handler.playerEntity;
		WorldServer worldserver = MinecraftServer.getServer().worldServerForDimension(player.dimension);
		//handler.field_147366_g = true;

		if(!player.playerConqueredTheEnd) {
			int networkTickCount = Getters.networkTickCount(handler);
			if(networkTickCount == 0) {
				captureCurrentPosition(handler);
			}

			boolean hasMoved = Getters.hasMoved(handler);

			if(!hasMoved) {
				double yDifference = packetIn.func_149467_d() - Getters.lastPosY(handler);

				if(packetIn.func_149464_c() == Getters.lastPosX(handler) && yDifference * yDifference < 0.01D && packetIn.func_149472_e() == Getters.lastPosZ(handler)) {
					Getters.hasMoved(handler, hasMoved = true);
				}
			}

			if(hasMoved && !player.isDead) {
				Getters.lastPositionUpdate(handler, networkTickCount);

				if(player.ridingEntity != null) {
					float yaw = player.rotationYaw;
					float pitch = player.rotationPitch;

					boolean hasRotation = packetIn.func_149463_k(); // hasRotation()

					if(hasRotation) {
						yaw = packetIn.func_149462_g(); // getYaw()
						pitch = packetIn.func_149470_h(); // getPitch()
					}

					player.setPositionAndRotation(player.posX, player.posY, player.posZ, yaw, pitch);
					MinecraftServer.getServer().getConfigurationManager().updatePlayerPertinentChunks(player);
				}
				else {
					double oldPosX = player.posX;
					double oldPosY = player.posY;
					double oldPosZ = player.posZ;
					double prevPosY = player.posY;



					double newPosX = player.posX;
					double newPosY = player.posY;
					double newPosZ = player.posZ;

					boolean hasPos = packetIn.func_149466_j(); // hasPos()

					if(hasPos && packetIn.func_149467_d() == -999.0D && packetIn.func_149471_f() == -999.0D) {
						hasPos = false;
					}


					if(hasPos) {
						newPosX = packetIn.func_149464_c();
						newPosY = packetIn.func_149467_d();
						newPosZ = packetIn.func_149472_e();

					}

					float rotationYaw = player.rotationYaw;
					float rotationPitch = player.rotationPitch;

					boolean hasRotation = packetIn.func_149463_k();

					if(hasRotation) {
						rotationYaw = packetIn.func_149462_g();
						rotationPitch = packetIn.func_149470_h();
					}



					double deltaX = newPosX - Getters.firstGoodX(handler);
					double deltaY = newPosY - Getters.firstGoodY(handler);
					double deltaZ = newPosZ - Getters.firstGoodZ(handler);
					double totalMotionSq = player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ;
					double totalPosSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

					if(player.isPlayerSleeping()) {
						if(totalPosSq > 1.0D) {
							handler.setPlayerLocation(player.posX, player.posY, player.posZ, rotationYaw, rotationPitch);
						}
					}
					else{


						double yDifference = packetIn.func_149471_f() - newPosY;
						
						if(hasPos && (yDifference > 1.65D || yDifference < 0.1D)) {
							handler.kickPlayerFromServer("Illegal stance");
							log.warn(player.getCommandSenderName() + " had an illegal stance: " + yDifference);
							return true;
						}

						int movePacketCounter = Getters.movePacketCounter(handler) + 1;
						Getters.movePacketCounter(handler, movePacketCounter);

						int packetsThisTick = movePacketCounter  - Getters.lastMovePacketCounter(handler);

						if(packetsThisTick > 5) {
							log.debug(player.getCommandSenderName() + " is sending move packets too frequently (" + packetsThisTick + " packets since last tick)");
							packetsThisTick = 1;
						}


						float maxMovementSq = 100.0F;

						if(totalPosSq - totalMotionSq > (double) (maxMovementSq * packetsThisTick) && (!MinecraftServer.getServer().isSinglePlayer() || !MinecraftServer.getServer().getServerOwner().equals(player.getCommandSenderName()))) {
							log.warn(player.getCommandSenderName() + " moved too quickly! " + newPosX + "," + newPosY + "," + newPosZ + " (" + deltaX + ", " + deltaY + ", " + deltaZ + ")");

							handler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
							return true;
						}


						boolean collision = worldserver.getCollidingBoundingBoxes(player, player.boundingBox.copy().contract(0.0625D, 0.0625D, 0.0625D)).isEmpty();


						deltaX = newPosX - Getters.lastPosX(handler);
						deltaY = newPosY - Getters.lastPosY(handler);
						deltaZ = newPosZ - Getters.lastPosZ(handler);

						boolean newOnGround = packetIn.func_149465_i();

						if(player.onGround && !newOnGround && deltaY > 0.0D) {
							player.jump();
						}

						player.moveEntity(deltaX, deltaY, deltaZ);
						player.onGround = newOnGround;
						double fallDistance = deltaY;
						deltaX = newPosX - player.posX;
						deltaY = newPosY - player.posY;

						if(deltaY > -0.5D || deltaY < 0.5D) {
							deltaY = 0.0D;
						}

						deltaZ = newPosZ - player.posZ;
						totalPosSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
						boolean movedWrongly = false;

						if(totalPosSq > 0.0625D && !player.isPlayerSleeping() && !player.theItemInWorldManager.isCreative()) {
							movedWrongly = true;
							log.warn(player.getCommandSenderName() + " moved wrongly!");
						}

						player.setPositionAndRotation(newPosX, newPosY, newPosZ, rotationYaw, rotationPitch);
						player.addMovementStat(player.posX - oldPosX, player.posY - oldPosY, player.posZ - oldPosZ);

						if(!player.noClip && !player.isPlayerSleeping()) {
							boolean postCollision = worldserver.getCollidingBoundingBoxes(player, player.boundingBox.copy().contract(0.0625D, 0.0625D, 0.0625D)).isEmpty();

							if(collision && (movedWrongly || !postCollision)) {
								handler.setPlayerLocation(oldPosX, oldPosY, oldPosZ, rotationYaw, rotationPitch);
								return true;
							}
						}

						boolean floating = fallDistance >= -0.03125D;
						floating &= !MinecraftServer.getServer().isFlightAllowed() && !player.capabilities.allowFlying;

						AxisAlignedBB axisalignedbb = player.boundingBox.copy().expand(0.0625D, 0.0625D, 0.0625D).addCoord(0.0D, -0.55D, 0.0D);

						floating &= !worldserver.checkBlockCollision(axisalignedbb);
						Getters.floating(handler, floating);
						player.onGround = newOnGround;
						MinecraftServer.getServer().getConfigurationManager().updatePlayerPertinentChunks(player);
						player.handleFalling(player.posY - prevPosY, newOnGround);
						Getters.lastPosX(handler, player.posX);
						Getters.lastPosY(handler, player.posY);
						Getters.lastPosZ(handler, player.posZ);
					}
				}
			}
			else if(networkTickCount - Getters.lastPositionUpdate(handler) > 20) {
				Getters.lastPositionUpdate(handler, networkTickCount);
				handler.setPlayerLocation(Getters.lastPosX(handler), Getters.lastPosY(handler), Getters.lastPosZ(handler), player.rotationYaw, player.rotationPitch);
			}
		}
		return true;
	}

	
	public static boolean cancelClientMovement(final EntityLivingBase entity) {
    	if(entity.worldObj.isRemote) {
    		if(HooksNetwork.specialDebugMode >= 6 && ASMConfig.smoothEntityMovement > 1) {
    			final int cmTick = Getters.cmTick(entity);
    			if(cmTick > 0) {
    				Getters.cmTick(entity, cmTick - 1);
        			return false;
    			}
    		}
    		
    		if(HooksNetwork.specialDebugMode >= 0 && !(entity instanceof EntityPlayer) && (!MeTweaks.lotr || !LOTRMountFunctions.isPlayerControlledMount(entity))) {
    			computeOnGround(entity);
    			return true;
    		}
    	}
    	
    	return false;
    }
    
	@SuppressWarnings("unchecked")
	private static void computeOnGround(final EntityLivingBase entity) {
		double y = -0.08D;
		final double unchanged = y;
		// check collision right below BB
		final List<AxisAlignedBB> boxes = entity.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox.addCoord(0.0D, y, 0.0D));
		for(final AxisAlignedBB box : boxes) {
			y = box.calculateYOffset(entity.boundingBox, y);
		}

		// is on ground
		entity.isCollidedVertically = unchanged != y;
		entity.onGround = unchanged != y;

		// limbs
		entity.prevLimbSwingAmount = entity.limbSwingAmount;
		final double x = entity.posX - entity.prevPosX;
		final double z = entity.posZ - entity.prevPosZ;
		final float progress = Math.min((float) Math.sqrt(x * x + z * z) * 4.0F, 1.0F);
		entity.limbSwingAmount += (progress - entity.limbSwingAmount) * 0.4F;
		entity.limbSwing += entity.limbSwingAmount;
	}
	
	public static void livingUpdateHead(EntityLivingBase entity, int newHeadRotationIncrements, float newRotationYawHead) {
		entity.rotationYawHead = entity.rotationYawHead + MathHelper.wrapAngleTo180_float(newRotationYawHead - entity.rotationYawHead) / newHeadRotationIncrements;
	}
	
	public static boolean canTickHere(World world, Entity entity) {
		return world.isRemote || HooksNetwork.specialDebugMode < 3 || !(entity instanceof EntityPlayer);
	}
	
	public static void tickPlayers(World world) {
		if(HooksNetwork.specialDebugMode < 3) return;
		if(world.isRemote) return;

		Profiler profiler = world.theProfiler;
		IChunkProvider provider = world.getChunkProvider();
		@SuppressWarnings("unchecked")
		List<Entity> players = world.playerEntities;

		profiler.endStartSection("players");



		for(Entity entity : players) {

			if(entity.ridingEntity != null) {
				if(!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity) {
					continue;
				}

				entity.ridingEntity.riddenByEntity = null;
				entity.ridingEntity = null;
			}

			profiler.startSection("tick");

			if(!entity.isDead) {
				try{
					world.updateEntity(entity);
				}
				catch(Throwable throwable) {
					CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking player");
					CrashReportCategory crashreportcategory = crashreport.makeCategory("Player being ticked");
					entity.addEntityCrashInfo(crashreportcategory);
					throw new ReportedException(crashreport);
				}
			}

			profiler.endSection();
			profiler.startSection("remove");

			if(entity.isDead) {
				int x = entity.chunkCoordX;
				int z = entity.chunkCoordZ;

				if(entity.addedToChunk && provider.chunkExists(x, z)) {
					world.getChunkFromChunkCoords(x, z).removeEntity(entity);
				}

				world.loadedEntityList.remove(entity);
				world.onEntityRemoved(entity);
			}

			profiler.endSection();
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public static void processPacketsClient(NetworkManager netManager) {
		if(specialDebugMode < 2) return;
		
		/*NetHandlerPlayClient handler = Minecraft.getMinecraft().getNetHandler();
		
		if(handler == null) return;
		
		
		if(netManager != null && FMLClientHandler.instance().getClientToServerNetworkManager() != null) {
			System.out.println(netManager + " "+FMLClientHandler.instance().getClientToServerNetworkManager());
		}
			*/
		
		
		
		if(netManager == null) {
			netManager = FMLClientHandler.instance().getClientToServerNetworkManager();
		}
		
		if(netManager == null || !netManager.isChannelOpen()) {
			return;
		}

    	Queue<Packet> queue = NetworkCalls.getReceivedPacketsQueue(netManager);
		INetHandler handler = netManager.getNetHandler();
    	
		if(handler != null) {
			while(!queue.isEmpty()) {
				queue.poll().processPacket(handler);
			}
        }
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void processPacketsServer() {
		if(specialDebugMode < 1) return;
		NetworkSystem system = MinecraftServer.getServer().func_147137_ag();

		final List<NetworkManager> netManagers = NetworkCalls.getNetManagers(system);
		synchronized(netManagers) {
			for(final NetworkManager netManager : netManagers) {
				if(!netManager.isChannelOpen()) continue;
				Queue<Packet> queue = NetworkCalls.getReceivedPacketsQueue(netManager);

				EnumConnectionState enumconnectionstate = (EnumConnectionState) netManager
				.channel()
				.attr(NetworkManager.attrKeyConnectionState)
				.get();

				EnumConnectionState connectionState = NetworkCalls.getConnectionState(netManager);
				if(connectionState != enumconnectionstate) {
					if(connectionState != null) {
						netManager.getNetHandler().onConnectionStateTransition(connectionState, enumconnectionstate);
					}

					NetworkCalls.setConnectionStateRaw(netManager, enumconnectionstate);
				}

				INetHandler handler = netManager.getNetHandler();

				if(handler != null) {
					try{
						while(!queue.isEmpty()) {
							queue.poll().processPacket(handler);
						}
					}
					catch(Exception exception) {
						if(netManager.isLocalChannel()) {
							CrashReport crashreport = CrashReport.makeCrashReport(exception, "Processing Packet");
							CrashReportCategory crashreportcategory = crashreport.makeCategory("Processing Packet");
							crashreportcategory.addCrashSectionCallable("Connection", new Callable() {
								public String call() {
									return netManager.toString();
								}
							});
							throw new ReportedException(crashreport);
						}

						log.warn("Failed to handle packet for " + netManager.getSocketAddress(), exception);
						final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");
						netManager.scheduleOutboundPacket(new S40PacketDisconnect(chatcomponenttext), new GenericFutureListener() {
							public void operationComplete(Future future) {
								netManager.closeChannel(chatcomponenttext);
							}
						});
						netManager.disableAutoRead();
					}
				}
			}
		}
	}
}
