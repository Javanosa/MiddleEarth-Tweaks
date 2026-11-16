package metweaks.physics;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public final class RenderPhysicsEntityFast extends Render {
	
	
	
	private final static RenderBlocks renderBlocks = new RenderBlocks();
    private final static FakeWorld fakeWorld = new FakeWorld();
    
    private static final RenderPhysicsEntity stableRender = new RenderPhysicsEntity();
    
    public RenderPhysicsEntityFast() {
        this.shadowSize = ExplosionPhysicsSettings.shadowSize;
    }
    
    @Override
    public final void setRenderManager(final RenderManager manager) {
        super.setRenderManager(manager);
        stableRender.setRenderManager(manager);
    }
    
    
	
	@Override
    protected final ResourceLocation getEntityTexture(final Entity entity) {
        return TextureMap.locationBlocksTexture;
    }
	
	private static IIcon getMissing() {
		return ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missingno");
	}
	
	public static final IIcon getIcon(final Block block, final int side, final int meta) {
	    final IIcon icon = block.getIcon(side, meta);
	    if(icon != null) return icon;
	    return getMissing();
	}
	
	
    public static final void renderStandardBlockWithColorMultiplier(final Block block, final int x, final int y, final int z, final int meta) {
    	block.setBlockBoundsBasedOnState(fakeWorld, x, y, z); // meta call for variative blocks
   		renderBlocks.setRenderBoundsFromBlock(block);
   		
   		   
   		final int color = block.colorMultiplier(fakeWorld, x, y, z);
   		float red = (float) (color >> 16 & 255) / 255.0F;
   		float green = (float) (color >> 8 & 255) / 255.0F;
   		float blue = (float) (color & 255) / 255.0F;

   		
        
        
        
       
        // normals
        final float n1 = 0.5F;
        final float n2 = 1.0F;
        final float n3 = 0.8F;
        final float n4 = 0.6F;
        

        final float ynR = n1 * red;
        final float ynG = n1 * green;
        final float ynB = n1 * blue;
    	
        final float ypR = n2 * red;
        final float ypG = n2 * green;
        final float ypB = n2 * blue;
    	
        final float zR = n3 * red;
        final float zG = n3 * green;
        final float zB = n3 * blue;
    	
        final float xR = n4 * red;
        final float xG = n4 * green;
        final float xB = n4 * blue;
        
        renderBlocks.enableAO = false;
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(block.getMixedBrightnessForBlock(fakeWorld, x, y, z));
        
        tessellator.setColorOpaque_F(ynR, ynG, ynB);
        renderBlocks.renderFaceYNeg(block, -0.5D, -0.5D, -0.5D, getIcon(block, 0, meta));

        tessellator.setColorOpaque_F(ypR, ypG, ypB);
        renderBlocks.renderFaceYPos(block, -0.5D, -0.5D, -0.5D, getIcon(block, 1, meta));

        tessellator.setColorOpaque_F(zR, zG, zB);
        renderBlocks.renderFaceZNeg(block, -0.5D, -0.5D, -0.5D, getIcon(block, 2, meta));

        tessellator.setColorOpaque_F(zR, zG, zB);
        renderBlocks.renderFaceZPos(block, -0.5D, -0.5D, -0.5D, getIcon(block, 3, meta));

        tessellator.setColorOpaque_F(xR, xG, xB);
        renderBlocks.renderFaceXNeg(block, -0.5D, -0.5D, -0.5D, getIcon(block, 4, meta));

        tessellator.setColorOpaque_F(xR, xG, xB);
        renderBlocks.renderFaceXPos(block, -0.5D, -0.5D, -0.5D, getIcon(block, 5, meta));
        tessellator.draw();
       
	}
	
	@Override
	public final void doRender(final Entity entity, final double renderX, final double renderY, final double renderZ, final float entityYaw, final float partialTicks) {
		if(!ExplosionPhysicsSettings.fastRenderImpl) {
			stableRender.doRender(entity, renderX, renderY, renderZ, entityYaw, partialTicks);
			return;
		}
		
		shadowSize = ExplosionPhysicsSettings.shadowSize;
		final PhysicsEntity falling = (PhysicsEntity) entity;
		final World world = falling.worldObj;
		final Block block = falling.func_145805_f(); // getImmitatingBlock()
		final int i = MathHelper.floor_double(falling.posX);
		final int j = MathHelper.floor_double(falling.posY);
		final int k = MathHelper.floor_double(falling.posZ);

        if(!world.getBlock(i, j, k).isOpaqueCube()) {
        	final int meta = falling.field_145814_a;
            GL11.glPushMatrix();
            bindEntityTexture(falling);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glTranslatef((float) renderX, (float) renderY, (float) renderZ);
            
            FakeWorld.fallingBlockBlock = block;
			FakeWorld.fallingBlockMeta = meta;
			FakeWorld.fallingBlockX = i;
			FakeWorld.fallingBlockY = j;
			FakeWorld.fallingBlockZ = k;
			FakeWorld.world = world;
			
			renderBlocks.blockAccess = fakeWorld;
            
            // renderAsNormalBlock
            if(block.renderAsNormalBlock() || block.getRenderType() == 0) {
            	FakeWorld.isFire = false;
            	renderStandardBlockWithColorMultiplier(block, i, j, k, meta);
            }
            else {
            	FakeWorld.isFire = block instanceof BlockFire;
            	final Tessellator tessellator = Tessellator.instance;
    			tessellator.startDrawingQuads();
    			
    			tessellator.setTranslation(-i - 0.5F, -j - 0.5F, -k - 0.5F);
    			renderBlocks.renderAllFaces = true;
    			final GameSettings setting = Minecraft.getMinecraft().gameSettings;
    			final int prevAO = setting.ambientOcclusion;
    			setting.ambientOcclusion = 0; // disable AO
    			renderBlocks.renderBlockByRenderType(block, i, j, k);
    			setting.ambientOcclusion = prevAO;
    			tessellator.setTranslation(0, 0, 0);
    			tessellator.draw();
            }
            
            FakeWorld.reset();
            
            

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
	}
	
	
}
