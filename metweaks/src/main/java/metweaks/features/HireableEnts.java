package metweaks.features;

import java.util.Random;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.LOTRAchievement;
import lotr.common.LOTRAchievement.Category;
import lotr.common.LOTRLevelData;
import lotr.common.LOTRMod;
import lotr.common.LOTRPlayerData;
import lotr.common.entity.ai.LOTREntityAIFollowHiringPlayer;
import lotr.common.entity.ai.LOTREntityAIHiredRemainStill;
import lotr.common.entity.npc.LOTREntityEnt;
import lotr.common.entity.npc.LOTREntityTree;
import lotr.common.entity.npc.LOTRHiredNPCInfo;
import lotr.common.entity.npc.LOTRUnitTradeEntry;
import lotr.common.entity.npc.LOTRUnitTradeEntry.PledgeType;
import lotr.common.fac.LOTRAlignmentValues;
import lotr.common.fac.LOTRFaction;
import metweaks.core.Hooks;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public class HireableEnts {
	
	public static LOTRAchievement hireEnt;
	
	public HireableEnts() {
		hireEnt = new LOTRAchievement(Category.FANGORN, 5, new ItemStack(LOTRMod.sapling2, 1, 1), "hireEnt").setRequiresAlly(LOTRFaction.FANGORN);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public static void handle(Entity entity) {
		if(entity instanceof LOTREntityEnt) {
			LOTREntityEnt ent = (LOTREntityEnt) entity;
			ent.tasks.addTask(1, new LOTREntityAIHiredRemainStill(ent));
		    ent.tasks.addTask(3, new LOTREntityAIFollowHiringPlayer(ent));
		}
	}
	
	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event) {
		if(event.action == Action.RIGHT_CLICK_AIR) {
			ItemStack stack = event.entityPlayer.getHeldItem();
			if(stack != null && stack.getItem() == LOTRMod.entDraught && stack.getItemDamage() == 2 && lastHiringTime > System.currentTimeMillis()) { // gold
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onEntityInteract(EntityInteractEvent event) {
		if(event.target instanceof LOTREntityEnt) {
			ItemStack stack = event.entityPlayer.getHeldItem();
			if(stack != null && ((stack.getItem() == LOTRMod.entDraught && stack.getItemDamage() == 2) || stack.getItem() == Items.bowl)) { // gold
				if(attemptToHireEnt(event.entityPlayer, (LOTREntityEnt) event.target, stack)) {
					event.setCanceled(true);
				}
			}
		}
	}
	
	public static void notifyPledgeRequired(EntityPlayer entityplayer, LOTRFaction faction) {
		entityplayer.addChatMessage(new ChatComponentTranslation("chat.lotr.requirePledge", faction.factionName()));
	}
	
	private static long lastHiringTime;
	private static int lastHiringID;
	
	public static boolean attemptToHireEnt(EntityPlayer player, LOTREntityEnt ent, ItemStack stack) {
		if(ent.hiredNPCInfo.isActive || ent.hiredNPCInfo.getHiringPlayerUUID() != null) {
			if(lastHiringID == ent.getEntityId() && lastHiringTime > System.currentTimeMillis())
				return true;
			return false;
		}
		
		if(stack.getItem() != LOTRMod.entDraught) return false;
		
		LOTRPlayerData playerData = LOTRLevelData.getData(player);
		World world = player.worldObj;
		
		if(playerData.getAlignment(LOTRFaction.FANGORN) < 600.0F) {
	    	if(!world.isRemote)
	    		LOTRAlignmentValues.notifyAlignmentNotHighEnough(player, 600.0F, LOTRFaction.FANGORN); 
	        return true;
	    }
		
		if(playerData.getPledgeFaction() != LOTRFaction.FANGORN) {
	    	if(!world.isRemote)
	    		notifyPledgeRequired(player, LOTRFaction.FANGORN); 
	        return true;
	    }
		
		if(world.isRemote) {
			lastHiringTime = System.currentTimeMillis() + 1000L;
			lastHiringID = ent.getEntityId();
		}
		
		
		
		if(!player.capabilities.isCreativeMode) {
			Hooks.consumeItem(stack, player, new ItemStack(Items.bowl));
		}
		
		
		
		
		/*if(false && world.isRemote) {
			int treeType = ent.getTreeType();
		    int blockID = Block.getIdFromBlock(LOTREntityTree.WOOD_BLOCKS[treeType]);
		    int meta = LOTREntityTree.WOOD_META[treeType];
			String particle = "blockdust_"+blockID+"_"+meta;
		    
			double x = ent.boundingBox.minX;
			double y = ent.posY;
			double z = ent.boundingBox.minZ;
			float width = ent.width;
			float height = ent.height;
			
			for(int n = 0; n < 24; n++) {
				double dx = x + (width * rand.nextFloat());
				double dy = y + (height * rand.nextFloat());
				double dz = z + (width * rand.nextFloat());
			    
				world.spawnParticle(particle, dx, dy, dz, 0.0D, 0.0D, 0.0D);
				
				
	        }
		}*/
		
		
		
		
		if(!world.isRemote) {
			
			Random rand = world.rand;
			
			if(rand.nextFloat() <= 0.2F) { // 20% chance
		
				
				
				if(world instanceof WorldServer) {
					int treeType = ent.getTreeType();
				    int blockID = Block.getIdFromBlock(LOTREntityTree.WOOD_BLOCKS[treeType]);
				    int meta = LOTREntityTree.WOOD_META[treeType];
					String particle = "blockdust_"+blockID+"_"+meta;
				    

					float width = ent.width * 0.5F;
					float height = ent.height * 0.5F;
					
					((WorldServer) world).func_147487_a(particle, 
		                	// pos
							ent.posX, ent.posY + height, ent.posZ, 
		                	// amount
			    			100,
			    			// motion
			    			width, height, width, 		
			    			// motion amplifier
			    			0);
					
					// String particle, 
					// double[float] posX, posY, posZ
					// int amount						
						// 0 means one particle at fixed pos with mootion vector and motion amplifier
						// double[float] motionX, motionY, motionZ
						// double[float] motionAmplifier
					
						// bigger than 0 means spawning multiple with variating pos and random motion vector
						// double[float] variationX, variationY, variationZ
						// double[float] motionVariation
					
					
					
					
					
					/*for(int n = 0; n < 24; n++) {
						double dx = x + (width * rand.nextFloat());
						double dy = y + (height * rand.nextFloat());
						double dz = z + (width * rand.nextFloat());
						
						((WorldServer) world).func_147487_a(particle, 
			                	// pos
								dx, dy, dz, 
			                	// amount
				    			0,
				    			// motion
				    			0, 0, 0, 		
				    			// motion amplifier
				    			0);
					}*/
				}
				
				
				
				LOTRUnitTradeEntry tradeEntry = new LOTRUnitTradeEntry(ent.getClass(), 0, 600.0F);
				tradeEntry.setTask(LOTRHiredNPCInfo.Task.WARRIOR);
				tradeEntry.setPledgeType(PledgeType.FACTION);
				ent.hiredNPCInfo.hireUnit(player, false, LOTRFaction.FANGORN, tradeEntry, null, null);
				LOTRLevelData.getData(player).addAchievement(hireEnt);
				SoundType sound = Blocks.leaves.stepSound;
				world.playSoundEffect(ent.posX, ent.posY + 0.5F, ent.posZ, sound.getBreakSound(), (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
			
			
			}
			else {
				
				ent.playSound("random.drink", 0.5F, rand.nextFloat() * 0.1F + 0.9F);
			}
		}
		return true;
	}
	
	
}
