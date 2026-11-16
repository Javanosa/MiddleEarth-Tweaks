package metweaks.client.features;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import lotr.client.render.LOTRRenderBlocks;
import metweaks.block.MeTweaksSlab;
import metweaks.block.VerticalSlab;
import metweaks.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;

public class MeTweaksRenderBlocks implements ISimpleBlockRenderingHandler {
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int id, RenderBlocks renderer) {
		if(id == CommonProxy.beamSlabRenderID) {
			renderBeamSlab(block, metadata, renderer, 0, 0, 0, true);
		}
		if(id == CommonProxy.stairsRenderID) {
			renderStairsInv(block, metadata, renderer);
		}
	}
	
	public void renderStairsInv(Block block, int metadata, RenderBlocks renderer) {
		
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
		Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
		for(int i = 0; i < 2; i++) {
			boolean vertical = i == 0;
			if(vertical) { // vertical slab
				// minX, minY, minZ 	, maxX, maxY, maxZ
            	renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5D);
            }
            else { // small bottom box
            	renderer.setRenderBounds(0.0D, 0.0D, 0.5D, 1.0D, 0.5D, 1.0D);
            }

			
            
            tessellator.setNormal(0.0F, -1.0F, 0.0F);
            renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
            
           
            
            
            
            //tessellator.draw();
            //tessellator.startDrawingQuads();
            
            tessellator.setNormal(0.0F, 1.0F, 0.0F);
            renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
            
            //tessellator.draw();
            //tessellator.startDrawingQuads();
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
            //tessellator.draw();
            //tessellator.startDrawingQuads();
            
            // cull away the stair face part that isnt visible anyway
            if(vertical) {
            	renderer.renderMinY = 0.5D;
            }
            tessellator.setNormal(0.0F, 0.0F, 1.0F);
            renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
            // set back to normal
            if(vertical) {
            	renderer.renderMinY = 0.0D;
            }
            
            //tessellator.draw();
            //tessellator.startDrawingQuads();
            tessellator.setNormal(-1.0F, 0.0F, 0.0F);
            renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
            //tessellator.draw();
            //tessellator.startDrawingQuads();
            tessellator.setNormal(1.0F, 0.0F, 0.0F);
            renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
            
            
        }
		tessellator.draw();
 		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		
	}
	
	public void renderBeamSlab(Block block, int metadata, RenderBlocks renderer, int x, int y, int z, boolean inv) {
		MeTweaksSlab slab;
		int metadata2;
		if(block instanceof VerticalSlab) {
			
			VerticalSlab vslab = (VerticalSlab) block;
			slab = (MeTweaksSlab) vslab.slab;
			metadata2 = (metadata & 3) + vslab.offset;
		}
		else {
			metadata2 = metadata & 7;
			slab = (MeTweaksSlab) block;
		}
		
		// remove top or bottom data and add offset
		
		metadata2 += slab.renderMetaOffset;
	    
		// limit rotation to default, 4, 8
        int rot = metadata2 & 12;

        if(rot == 0) {
        	renderer.uvRotateEast = 3;
        	renderer.uvRotateNorth = 3;
        }
        else if (rot == 4) {
        	renderer.uvRotateEast = 1;
        	renderer.uvRotateWest = 2;
        	renderer.uvRotateTop = 2;
        	renderer.uvRotateBottom = 1;
        }
        else if (rot == 8) {
        	renderer.uvRotateSouth = 1;
        	renderer.uvRotateNorth = 2;
        }
        
        

        if(inv) {
			
	        
	        
	        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
	        if(VerticalSlab.clientVertical) {
	        	GL11.glTranslatef(-0.2F, 0.2F, 0F);
	        	block.setBlockBounds(
						// minX, minY, minZ
						0, 0, 0.5F, 
						// maxX, maxY, maxZ
						1, 1, 1);
	        }
	        else {
	        	block.setBlockBoundsForItemRender();
	        }
	        renderer.setRenderBoundsFromBlock(block);
	        
	        	
	        LOTRRenderBlocks.renderStandardInvBlock(renderer, block, renderer.renderMinX, renderer.renderMinY, renderer.renderMinZ, renderer.renderMaxX, renderer.renderMaxY, renderer.renderMaxZ, metadata);
	        if(VerticalSlab.clientVertical)
	        	GL11.glTranslatef(0.2F, -0.2F, 0F);
	        
	        
			
		}
        else {
        	renderer.renderStandardBlock(block, x, y, z);
        }
        
        
        
        // reset rotations
        renderer.uvRotateSouth = 0;
        renderer.uvRotateEast = 0;
        renderer.uvRotateWest = 0;
        renderer.uvRotateNorth = 0;
        renderer.uvRotateTop = 0;
        renderer.uvRotateBottom = 0;
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int id, RenderBlocks renderer) {
		if(id == CommonProxy.beamSlabRenderID) {
		   renderBeamSlab(block, world.getBlockMetadata(x, y, z), renderer, x, y, z, false);
	       return true;
	    }
		if(id == CommonProxy.stairsRenderID) {
			renderer.renderBlockStairs((BlockStairs) block, x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int id) {
		return true;
	}

	@Override
	public int getRenderId() {
		return 0;
	}

}
