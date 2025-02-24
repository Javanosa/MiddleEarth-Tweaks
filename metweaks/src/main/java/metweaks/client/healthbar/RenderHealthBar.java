package metweaks.client.healthbar;

import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import gnu.trove.map.TIntByteMap;
import gnu.trove.map.hash.TIntByteHashMap;
import lotr.client.LOTRSpeechClient;
import lotr.common.LOTRLevelData;
import lotr.common.entity.animal.LOTREntityHorse;
import lotr.common.entity.npc.LOTREntityNPC;
import lotr.common.entity.npc.LOTREntityNPCRideable;
import lotr.common.entity.npc.LOTREntityWarg;
import lotr.common.entity.npc.LOTRNPCMount;
import lotr.common.fellowship.LOTRFellowshipClient;
import lotr.common.item.LOTRItemMountArmor;
import lotr.common.item.LOTRWeaponStats;
import metweaks.MeTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import static metweaks.client.healthbar.HealthBarConfig.*;
import static metweaks.events.ClientEvents.*;

public class RenderHealthBar {
	
	// states
	private static boolean drawingIcon;
	private static boolean drawingHUD;
	
	
	private static Frustrum frustrum;
	public static long lastMouseOverExpire;
	public static int lastMouseOverEntity;
	private static TIntByteMap canSeeIBMap = new TIntByteHashMap();
	
	public static boolean isInRangeToRenderDist(double dist, Entity entity) {
        final double r = entity.boundingBox.getAverageEdgeLength() * 64.0D * entity.renderDistanceWeight;
        return dist < r * r;
    }
	
	private static boolean hasAttributes(EntityLivingBase living) {
		if(living instanceof EntityHorse || (MeTweaks.lotr && living instanceof LOTREntityNPCRideable)) {
			if(!attributesOnlyTamed || (living instanceof EntityHorse && ((EntityHorse) living).isTame()) || (MeTweaks.lotr && living instanceof LOTREntityNPCRideable && ((LOTREntityNPCRideable) living).isNPCTamed())) {
				return true;
			}
		}
		return false;
	}
	
	public static double computeSpeed(double input, boolean mount) {
		if(speedAsMeters && mount) {
			return Math.round(input * 43.1 * 10.0) / 10.0;
		}
		return Math.round(input * 100.0) / 100.0;
	}
	
	// not very efficient
	public static double computeJump(double input) {
		if(speedAsMeters) {
			float factor = (float) input;
			float meters = 0.0F;
			while(factor > 0.0F) {
				meters += factor;
				factor -= 0.082F;
				factor *= 0.98F;
			}
			input = meters;
		}
		return Math.round(input * 10.0) / 10.0;
	}
	
	public static void renderBar(Entity topentity, float ticks, boolean isCursor) {
		
		
		EntityLivingBase living = (EntityLivingBase) topentity;
		// get the lowest living entity
		while(living.ridingEntity != null && living.ridingEntity instanceof EntityLivingBase)
			living = (EntityLivingBase) living.ridingEntity;

		
		
		// offset to calculate for upper / lower entity
		float addY = 0F;
		
		double heightOffset = offsetY;
		// disable for stacked
		boolean textDesc = showDesc && topentity.ridingEntity == null;

		Minecraft mc = Minecraft.getMinecraft();
		boolean hiredOrPlayer = false;
		
		if(MeTweaks.lotr && topentity instanceof LOTREntityNPC) {
			LOTREntityNPC npc = (LOTREntityNPC) topentity;
			
			if(npc.hiredNPCInfo.getHiringPlayerUUID() != null) {
				if(hideOnHireds && mc.thePlayer.getUniqueID().equals(npc.hiredNPCInfo.getHiringPlayerUUID())) {
					return;
				}
				hiredOrPlayer = true;
			}
			
			// server client sync issue!
			if(/*npc.hiredNPCInfo.isActive && */npc.hiredNPCInfo.getHiringPlayer() == mc.thePlayer) {
				if(hideOnHireds) {
					return;
				}
				/*else {
					heightOffset += 0.0;
				}*/
			}
			if(LOTRSpeechClient.getSpeechFor(npc) != null) {
				if(hideOnSpeech) {
					return;
				}
				else {
					
					heightOffset += mc.fontRenderer.FONT_HEIGHT * 0.015 * (3 + mc.fontRenderer.listFormattedStringToWidth(LOTRSpeechClient.getSpeechFor(npc).getSpeech(), 150).size());
				}
			}
		}
		
		if(MeTweaks.customNpcs && CustomNpcsBridge.hasChatMessage(topentity)) {
			return;
		}
		
		if(topentity instanceof EntityPlayer) {
			if(MeTweaks.mpm && MpmBridge.hasChatMessage(topentity)) {
				return;
			}
			
			
			//heightOffset -= 1.72;
			
			if(hideOnPlayers || topentity.isSneaking())
				return;
			if(hideOnFellowshipMembers && MeTweaks.lotr) {
				UUID uuid = ((EntityPlayer) topentity).getUniqueID();
				List<LOTRFellowshipClient> fellowships = LOTRLevelData.getData(mc.thePlayer).getClientFellowships();
			    for(LOTRFellowshipClient fs : fellowships) {
			    	if(fs.containsPlayer(uuid)) {
			    		return;
			    	}
			    }
			}
			
			if(heightOffset < 0.665)
				heightOffset = 0.665;
			
			hiredOrPlayer = true;
		}
		
		if(!isCursor && !hiredOrPlayer && onlyHiredOrPlayer) {
			return;
		}
		
		if(heightOffset < 0.65 && topentity instanceof EntityLiving && (((EntityLiving) topentity).getAlwaysRenderNameTagForRender() || ((EntityLiving) topentity).hasCustomNameTag())) {
			heightOffset = 0.65;
		}
		
		
		
		float x = (float) (topentity.lastTickPosX + (topentity.posX - topentity.lastTickPosX) * ticks - RenderManager.renderPosX);
		float y = (float) (topentity.lastTickPosY + (topentity.posY - topentity.lastTickPosY) * ticks - RenderManager.renderPosY + heightOffset + topentity.height);
		float z = (float) (topentity.lastTickPosZ + (topentity.posZ - topentity.lastTickPosZ) * ticks - RenderManager.renderPosZ);
		
		while(living != null) {
				
				if(living.isInvisible() || ((EntityLivingBase) living).deathTime != 0) {
					break;
				}

				float healthmax = living.getMaxHealth();
				float health = Math.min(healthmax, living.getHealth());
				// prepare because onlyInjured
				boolean isRideable = hasAttributes(living);
				
				if(healthmax <= 0 || (onlyInjured && health == healthmax && !(isCursor || (isRideable && (showSpeed || showJump || showClampSpeed) && !(MeTweaks.lotr && living instanceof LOTRNPCMount && ((LOTRNPCMount) living).getBelongsToNPC()))))) {
					Entity rider = living.riddenByEntity;
					
					if(rider != null && rider instanceof EntityLivingBase) {
						living = (EntityLivingBase) rider;
						continue;
					}
					else return;
					
				}
				
				
				if(!drawingHUD) {
					drawingHUD = true;
					GL11.glNormal3f(0.0F, 1.0F, 0.0F);
					GL11.glDepthMask(false);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glDisable(GL11.GL_LIGHTING); // 
					GL11.glEnable(GL11.GL_BLEND);
					OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
					
					
				}

					
				GL11.glPushMatrix();
				GL11.glTranslatef(x, y, z);
				GL11.glRotatef(-RenderManager.instance.playerViewY, 0F, 1F, 0F);
				GL11.glRotatef(RenderManager.instance.playerViewX, 1F, 0F, 0F);
				float scale = 0.01F;
				GL11.glScalef(-scale, -scale, scale);
				
				if(addY < 0)
			    	GL11.glTranslatef(0, addY, 0);
			    
			    
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				

				float padding = 3;
				int contrastHeight = (int) padding;
				int barHeight = 7+3+3;
				
				
				
				
				// what parts should be prepared for?
				boolean iconSpeed = false;
				boolean iconJump = false;
				boolean iconArmor = false;
				
				
				String jumpstr = null;
				String speedstr = null;
				int armor = 0;
				
				boolean drawnColor = false;
				
				//if(showAttributes) {
					if(attributesAll || isRideable) {//living instanceof EntityHorse || (MeTweaks.lotr && living instanceof LOTREntityNPCRideable)) {
						//if(!attributesOnlyTamed || (living instanceof EntityHorse && ((EntityHorse) living).isTame()) || (MeTweaks.lotr && living instanceof LOTREntityNPCRideable && ((LOTREntityNPCRideable) living).isNPCTamed())) {
							if(showSpeed || showClampSpeed) {
								
								
								IAttributeInstance attr = living.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
								double speed = 0;
								if(showSpeed)
									speed = computeSpeed(attr.getBaseValue(), isRideable);
								
								double clampedspeed = 0;
								if(showClampSpeed)
									clampedspeed = computeSpeed(attr.getAttributeValue(), isRideable);
								
								
								
								if(speed > 0 || clampedspeed > 0) {
									StringBuilder builder = new StringBuilder();
									
									if(showSpeed)
										builder.append(speed);
									if(showClampSpeed) {
										if(showSpeed) {
											if(speed != clampedspeed) {
												if(speedAsMeters && isRideable) {
													if(builder.charAt(builder.length()-1) == '0') {
														builder.setLength(builder.length()-2);
													}
												}
												builder.append(" \u00a77").append(clampedspeed);
											}
											drawnColor = true;
											
										}
										else {
											builder.append(clampedspeed);
										}
									}
									
									if(speedAsMeters && isRideable) {
										if(builder.charAt(builder.length()-1) == '0') {
											
											builder.setLength(builder.length()-2);
										}
										//if(translateMPS == null) translateMPS = StatCollector.translateToLocal("metweaks.healthbar.metersPerSecond");
											
										
										builder.append("m/s");
									}
										
									speedstr = builder.toString();
									
									iconSpeed = true;
								}
							}
							
							if(showJump && living instanceof EntityHorse) {
								double jump = computeJump(((EntityHorse) living).getHorseJumpStrength());
								if(jump > 0) {
									
									StringBuilder builder = new StringBuilder();
									
									builder.append(jump);
									
									if(speedAsMeters) {
										if(builder.charAt(builder.length()-1) == '0') {
											
											builder.setLength(builder.length()-2);
										}
										/*if(translateMeters == 0) {
											String meters = StatCollector.translateToLocal("metweaks.healthbar.meters");
											if(meters.length() != 0) {
												translateMeters = meters.charAt(0);
											}
										}*/
										builder.append('m');
									}
									
									jumpstr = builder.toString();
									iconJump = true;
								}
							}
						//}
			    	}
				//}
				
				if(showArmor) {
					int armoridIndex = 0;
					
					if(MeTweaks.lotr) {
						
					
						if(living instanceof LOTREntityHorse)
							armoridIndex = 27;
						else if(living instanceof LOTREntityWarg)
							armoridIndex = 20;
					}
					
					if(armoridIndex > 0) {
						// LOTRFA's colored mount armor use itemstack datawatcher
						if(MeTweaks.lotrfa && armoridIndex == 27) {
							armor = LOTRFABridge.getHorseArmorValue(living);
						}
						else {
							int id = living.getDataWatcher().getWatchableObjectInt(armoridIndex);
							if(id > 0) {
								Item item = Item.getItemById(id);
								if(item instanceof LOTRItemMountArmor) {
									armor = ((LOTRItemMountArmor) item).getDamageReduceAmount();
								}
							}
						}
						
					}
					else if(MeTweaks.lotr && living instanceof EntityPlayer) {
						armor = LOTRWeaponStats.getTotalArmorValue((EntityPlayer) living);
					}
					else {
						armor = living.getTotalArmorValue();
					}
					
					if(armor > 0) {
						iconArmor = true;
					}
				}
				
				// rework this
				if(iconSpeed || iconJump || iconArmor) {
					if(textDesc && (iconSpeed || iconJump)) {
						contrastHeight = (int) (padding + 7 + padding + padding + 7 + padding);
					}
					else{
						contrastHeight = (int) (padding + 7 + padding + 1);
					}
				}
				
				int halfsize = 70;
				if(healthmax > 25) {
					halfsize += Math.min((int) healthmax - 25, 50);
					
				}
				else if(healthmax < 10 && !iconSpeed && !iconJump) {
					halfsize = 40;
				}
				float fillhealth = ((health / healthmax) * halfsize) * 2 -halfsize;
				
				Tessellator tessellator = Tessellator.instance;
				// Background
				tessellator.startDrawingQuads();
				tessellator.setColorRGBA(color_background[0], color_background[1], color_background[2], color_background[3]);
				tessellator.addVertex(-halfsize - padding, -contrastHeight, 0);
				tessellator.addVertex(-halfsize - padding, barHeight + padding, 0);
				tessellator.addVertex(halfsize + padding, barHeight + padding, 0);
				tessellator.addVertex(halfsize + padding, -contrastHeight, 0);
				
				int Ired = color_injured[0];
				int Igreen = color_injured[1];
				int Iblue = color_injured[2];
				int Ialpha = color_injured[3];
				
				
				
				// Injured
				tessellator.setColorRGBA(Ired, Igreen, Iblue, Ialpha);
				tessellator.addVertex(fillhealth, 0, 0);
				tessellator.addVertex(fillhealth, barHeight, 0);
				tessellator.addVertex(halfsize, barHeight, 0);
				tessellator.addVertex(halfsize, 0, 0);

				// Health Bar
				
				int red = color_health[0];
				int green = color_health[1];
				int blue = color_health[2];
				int alpha = color_health[3];
				
				if(living instanceof EntityPlayer) {
					red = color_player[0];
					green = color_player[1];
					blue = color_player[2];
					alpha = color_player[3];
				}
				
				
				
				if(living.riddenByEntity != null)
					tessellator.setColorRGBA(color_mount[0], color_mount[1], color_mount[2], color_mount[3]);
				else
					tessellator.setColorRGBA(red, green, blue, alpha);
					
				tessellator.addVertex(-halfsize, 0, 0);
				tessellator.addVertex(-halfsize, barHeight, 0);
				tessellator.addVertex(fillhealth, barHeight, 0);
				tessellator.addVertex(fillhealth, 0, 0);
				tessellator.draw();

				GL11.glEnable(GL11.GL_TEXTURE_2D);
				
				
				String strMinHealth = "" + Math.round(health);
				
				
				
				String strMaxHealth = "" + Math.round(healthmax);
				
				
				String strHealth = strMinHealth + " / "+strMaxHealth;
				
				
				
				int offLine0 = 3;
				
				FontRenderer fontrender = mc.fontRenderer;
				
				
				

		        
				fontrender.drawString(strHealth, -fontrender.getStringWidth(strHealth) >> 1, offLine0, 0xFFFFFFFF);
 				
				// render attr
				
				int lenArmor = 0;
				int posJump = 0;
				
				int iconsize = 12;
				int iconbuffer = 0;

				if(showIcons)
				  iconbuffer = 13;

				if(iconArmor) {

				  
				    lenArmor = fontrender.getStringWidth(""+armor);
				}

				int offLine1 = -10;
				

				if(iconSpeed) {
					fontrender.drawString(speedstr, -halfsize + iconbuffer, offLine1, 0xFFFFFFFF);
					
					 
					
				  if(iconJump) {
				    int lenJump = fontrender.getStringWidth(jumpstr);
				    //int lenSpeed = mc.fontRenderer.getStringWidth(speedstr);
				    
				    //int lenSpeed = halfsize + fontrender.getStringWidth(speedstr);
				    //int lenSpeed = fontrender.getStringWidth(speedstr);
				    
				    // half of way between end of speed and begin of armor + centering the jump hud
				    
				    int minPos = iconbuffer + iconbuffer + fontrender.getStringWidth(speedstr);
				    // min + ((max - min) / 2)
				    posJump = -halfsize + minPos + ((((halfsize + halfsize) - (iconbuffer + lenArmor + lenJump)) - minPos) >> 1);
				    //posJump = -halfsize + lenSpeed - lenJump + (halfsize - lenArmor - lenSpeed   +   lenJump + iconbuffer) >> 1;
				  }
				}
				if(iconJump) {
					fontrender.drawString(jumpstr, posJump, offLine1, 0xFFFFFFFF);
				}
				if(iconArmor) {
					fontrender.drawString(""+armor, halfsize - lenArmor, offLine1, 0xFFFFFFFF);
				}

				
				// desc
				if(textDesc) {
				  int offLine2 = -10 - 10 - 3;
				  
				  if(iconSpeed) {
					  if(translateSpeed == null) translateSpeed = "\u00a7o"+StatCollector.translateToLocal("metweaks.healthbar.speed");
					  fontrender.drawString(translateSpeed, -halfsize, offLine2, 0xFFEEEEEE); drawnColor = true; // "\u00a7oSpeed"
				  }
				  if(iconJump) {
					  if(translateJump == null) translateJump = "\u00a7o"+StatCollector.translateToLocal("metweaks.healthbar.jump");
				  
					  fontrender.drawString(translateJump, posJump - iconbuffer, offLine2, 0xFFEEEEEE); drawnColor = true; // "\u00a7oJump"
				  }
				  if(iconArmor && !showIcons) {
					  fontrender.drawString("\u00a7oArmor", halfsize - lenArmor - iconbuffer, offLine2, 0xFFEEEEEE); drawnColor = true;
				  }
				}
				
				// reset color for icons
				  if(drawnColor)
					  GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				
				
				// render icons
				
				
				if(showIcons) {

					mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);
					offLine1 -= 3;
					// speed icon
					
					if(iconSpeed) {
						renderIcon(-halfsize, offLine1, icon_speed.getIconFromDamage(icon_speed_meta), iconsize);
					}

					// jump icon
					if(iconJump) {
						renderIcon(posJump - iconbuffer, offLine1, icon_jump.getIconFromDamage(icon_jump_meta),     iconsize);
					}
					// armor icon
					if(iconArmor) {
						
						int left = (int) halfsize - lenArmor - iconbuffer;
						IIcon icon = icon_armor.getIconFromDamage(icon_armor_meta);
						
						renderIcon(left, offLine1,     icon,     iconsize);
						
						
					}
					
					if(drawingIcon) {
						drawingIcon = false;
						tessellator.draw();
					}
				}
				
				
				GL11.glPopMatrix();
				
				// get upper indexed entity
				Entity rider = living.riddenByEntity;
				
				if(rider != null && rider instanceof EntityLivingBase) {
					living = (EntityLivingBase) rider;
					addY -= contrastHeight + barHeight + padding;
				}
				
				else return;
			}
		
	}
	
	private static void renderIcon(int x, int y, IIcon icon, int size) {
		Tessellator tessellator = Tessellator.instance;
		if(!drawingIcon) {
			tessellator.startDrawingQuads();
			drawingIcon = true;
		}
		
        tessellator.addVertexWithUV(x + 0, y + size, 0, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + size, y + size, 0, icon.getMaxU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + size, y + 0, 0, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(x + 0, y + 0, 0, icon.getMinU(), icon.getMinV());
    }
	
	
	
	public static Entity getMouseOver(Minecraft mc, float tick) {
		
		final double finalDistance = 32;
		double distance = finalDistance;
		MovingObjectPosition result = mc.renderViewEntity.rayTrace(finalDistance, tick);
		
		
		Vec3 positionvec = mc.renderViewEntity.getPosition(tick);
		Vec3 look = mc.renderViewEntity.getLook(tick);
		
		
		
		
		if(result != null) 
			distance = result.hitVec.distanceTo(positionvec);
		
		Vec3 reachvec = positionvec.addVector(look.xCoord * finalDistance, look.yCoord * finalDistance, look.zCoord * finalDistance);
		
		Entity lookedEntity = null;
		@SuppressWarnings("unchecked")
		List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.renderViewEntity, mc.renderViewEntity.boundingBox.addCoord(
				look.xCoord * finalDistance, look.yCoord * finalDistance, look.zCoord * finalDistance).expand(1, 1, 1));
		double minDistance = distance;
		
		// avoid looking at horse, so we can also view through
		Entity mount = mc.renderViewEntity.ridingEntity;

		for(Entity entity : entities) {
			
			if(entity.canBeCollidedWith() && mount != entity) {
				float collisionBorderSize = entity.getCollisionBorderSize();
				AxisAlignedBB hitbox = entity.boundingBox.expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
				MovingObjectPosition interceptPosition = hitbox.calculateIntercept(positionvec, reachvec);

				if(hitbox.isVecInside(positionvec)) {
					if(0 < minDistance || minDistance == 0) {
						lookedEntity = entity;
						minDistance = 0;
					}
				}
				else if(interceptPosition != null) {
					double distanceToEntity = positionvec.distanceTo(interceptPosition.hitVec);
					

					if((distanceToEntity < minDistance || minDistance == 0) && entity.isInRangeToRenderDist(distanceToEntity)) {
						lookedEntity = entity;
						minDistance = distanceToEntity;
					}
				}
			}
		}
		
		if(lookedEntity != null && (minDistance < distance || result == null)) {
			lastMouseOverExpire = System.currentTimeMillis() + 1000;
			lastMouseOverEntity = lookedEntity.getEntityId();
			return lookedEntity;
		}
		
		if(lastMouseOverExpire > System.currentTimeMillis()) {
			Entity entity =  mc.theWorld.getEntityByID(lastMouseOverEntity);
			// fix mount bar showing up for a few seconds
			if(entity != mount) return entity;
		}
		return null;
	}
	
	private static final boolean canSeeEntity(final Entity entity, final Entity camera) {
		int id = entity.getEntityId();
		if(canSeeIBMap.containsKey(id)) {
			return canSeeIBMap.get(id) == 1;
		}
		final byte canSee = (entity.worldObj.func_147447_a(
				Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ),
				Vec3.createVectorHelper(camera.posX, camera.posY + camera.getEyeHeight(), camera.posZ)
				, false, true, false) == null) ? 1 : (byte) 0;
		canSeeIBMap.put(id, canSee);
		
		return canSee == 1;
	}
	
	public static void handleRenderWorldLast(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		
		EntityLivingBase camera = mc.renderViewEntity;
		Entity ignore = null;
		
		
		
        if(showMouseover) {
        	Entity look = getMouseOver(mc, event.partialTicks);
        	if(look != null && look instanceof EntityLivingBase && !blacklist.contains(look.getClass())) {
        		// get the lowest living entity
        		while(look.riddenByEntity != null && look.riddenByEntity instanceof EntityLivingBase)
        			look = (EntityLivingBase) look.riddenByEntity;
        		
        		renderBar(look, event.partialTicks, true);
        		ignore = look;
        	}
        }
        if(showAll) {
        	WorldClient world = mc.theWorld;
        	
        	if(nextCanSeeCacheClear <= 0) {
        		nextCanSeeCacheClear = 4;
        		
        		canSeeIBMap.clear();
        		
        	}
        	
				
			
			
    		double x = RenderManager.renderPosX;
    		double y = RenderManager.renderPosY;
    		double z = RenderManager.renderPosZ;
    		
    		
            double cameraX = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.partialTicks;
            double cameraY = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.partialTicks;
            double cameraZ = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.partialTicks;
            
            if(frustrum == null)
            	frustrum = new Frustrum();
            
            frustrum.setPosition(cameraX, cameraY, cameraZ);
            
            @SuppressWarnings("unchecked")
			List<Entity> lentitylist = world.loadedEntityList;
            
        	for(Entity entity : lentitylist) {
        		
        		if(ignore != entity && entity != camera && entity instanceof EntityLivingBase && entity.riddenByEntity == null) {
        			
    				double distance = entity.getDistanceSq(x, y, z);
    				if(distance < maxDistanceSq && isInRangeToRenderDist(distance, entity)) {
    					if(!blacklist.contains(entity.getClass()) && (entity.ignoreFrustumCheck || frustrum.isBoundingBoxInFrustum(entity.boundingBox)) && canSeeEntity(entity, camera)/*((EntityLivingBase) entity).canEntityBeSeen(camera)*/) {
    						renderBar(entity, event.partialTicks, false);
    					}
    				}
    			}
    		}
        }
        
        if(drawingHUD) {
        	drawingHUD = false;
        	GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			
			
			
        }
	}
}
