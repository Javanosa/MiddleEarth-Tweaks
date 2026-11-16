package metweaks.physics;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class RenderPhysicsEntity extends Render {
    private final static RenderBlocks renderBlocks = new RenderBlocks();
    private final static FakeWorld fakeWorld = new FakeWorld();
    
    public RenderPhysicsEntity() {
        this.shadowSize = ExplosionPhysicsSettings.shadowSize;
    }
    
    // blockstates needed for slabs, stairs etc.
    // colorizing: first 4 leaves, colored
    // if not renderpass 0:  lotr glass transculency
    // 0, 4, LOTRMod.proxy.getBeamRenderID(), can be rendered as standard block
    
    
    
    // must set the block shape before rendering
    // color: oak, acacia, dark_oak leaves
    // custom renderer: any non quadric cube
	@Override
	public final void doRender(final Entity entity, final double renderX, final double renderY, final double renderZ, final float entityYaw, final float partialTicks) {
		
		shadowSize = ExplosionPhysicsSettings.shadowSize;
		PhysicsEntity falling = (PhysicsEntity) entity;
		World world = falling.worldObj;
		Block block = falling.func_145805_f(); // getImmitatingBlock()
		int i = MathHelper.floor_double(falling.posX);
		int j = MathHelper.floor_double(falling.posY);
		int k = MathHelper.floor_double(falling.posZ);
		if(!world.getBlock(i, j, k).isOpaqueCube()) {
			GL11.glPushMatrix();
			// translate for render, float precision
			GL11.glTranslatef((float) renderX, (float) renderY, (float) renderZ);
			
			bindEntityTexture(entity);
			GL11.glDisable(GL11.GL_LIGHTING);
	
	
			FakeWorld.fallingBlockBlock = block;
			FakeWorld.fallingBlockMeta = falling.field_145814_a;
			FakeWorld.fallingBlockX = i;
			FakeWorld.fallingBlockY = j;
			FakeWorld.fallingBlockZ = k;
			FakeWorld.world = world;
			FakeWorld.isFire = block instanceof BlockFire;
			
	
			renderBlocks.blockAccess = fakeWorld;
	
	
	
			
	
	
	
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.setTranslation(-i - 0.5F, -j - 0.5F, -k - 0.5F);
	
	
			renderBlocks.renderAllFaces = true;
			GameSettings setting = Minecraft.getMinecraft().gameSettings;
			int prevAO = setting.ambientOcclusion;
			setting.ambientOcclusion = 0; // disable AO
	
			renderBlocks.renderBlockByRenderType(block, i, j, k);
	
			setting.ambientOcclusion = prevAO;
			tessellator.setTranslation(0, 0, 0);
			tessellator.draw();
	
			
			FakeWorld.reset();
	
	
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();
	
	
			

		}
	}
	
	

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationBlocksTexture;
    }
}