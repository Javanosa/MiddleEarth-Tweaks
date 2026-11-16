package metweaks.features.halftroll;

import org.lwjgl.opengl.GL11;

import lotr.client.model.LOTRModelHalfTroll;
import lotr.client.render.entity.LOTRRandomSkins;
import lotr.client.render.entity.LOTRRenderBiped;
import lotr.common.entity.npc.LOTREntityHalfTroll;
import lotr.common.entity.projectile.LOTREntityThrownRock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public class LOTRRenderHalfTrollThrower extends LOTRRenderBiped {
	
	private LOTREntityThrownRock cached_rock;
	
	private static LOTRRandomSkins skins;
	
	public LOTRRenderHalfTrollThrower() {
		super(new LOTRModelHalfTrollThrower(), 0.5F);
		skins = LOTRRandomSkins.loadSkinsList("lotr:mob/halfTroll/halfTroll");
	}
	
	@Override
	protected void func_82421_b() {
		this.field_82423_g = new LOTRModelHalfTroll(1.0F);
		this.field_82425_h = new LOTRModelHalfTroll(0.5F);
	}
	
	@Override
	public ResourceLocation getEntityTexture(Entity entity) {
		LOTREntityHalfTroll halfTroll = (LOTREntityHalfTroll)entity;
		return skins.getRandomSkin(halfTroll);
	}
	
	@Override
	public float getHeldItemYTranslation() {
		return 0.45F;
	}
	
	@Override
	protected void renderEquippedItems(EntityLivingBase entity, float scale) {
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		super.renderEquippedItems(entity, scale);
		GL11.glPushMatrix();
		LOTREntityHalfTrollThrower halftroll = (LOTREntityHalfTrollThrower) entity;
		if(halftroll.isThrowingRocks()) {
			if(mainModel.onGround <= 0.0F) {
				if(cached_rock == null)
					cached_rock = new LOTREntityThrownRock(halftroll.worldObj); 
				
				cached_rock.setWorld(halftroll.worldObj);
				cached_rock.setPosition(halftroll.posX, halftroll.posY, halftroll.posZ);
				// render with scale
				((LOTRModelHalfTroll) mainModel).bipedRightArm.postRender(0.0625F);
				GL11.glTranslatef(0.375F, 0.45F * 1.65F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(0.65F, 0.65F, 0.65F);
				renderManager.renderEntityWithPosYaw(cached_rock, 0.0D, 0.0D, 0.0D, 0.0F, scale);
			} 
		}
		GL11.glPopMatrix();
	}
}
