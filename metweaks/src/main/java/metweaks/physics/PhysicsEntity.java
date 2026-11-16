package metweaks.physics;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class PhysicsEntity extends EntityFallingBlock {
    private Block block = Blocks.stone;
    //public int field_145814_a; // metadata
    //public int field_145812_b; // time
    //public boolean field_145813_c; // dropitem
    //public NBTTagCompound field_145810_d;
    private boolean dontPlaceBlock;
    private boolean hurtEntities;
    private int fallHurtMax;
    private float fallHurtAmount;
    
    
    private static final ItemStack dummy_stack = new ItemStack(Blocks.stone, 1, 1);
    
    public Object protectionFilter;
    public float explosionSize;
    private boolean ignoreVelChange;
    
    
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static void replaceVanilla() {
    	Class clazz = PhysicsEntity.class;
    	String name = "FallingSand";
    	int id = 21;
    	EntityList.stringToClassMapping.put(name, clazz);
    	EntityList.classToStringMapping.put(clazz, name);
    	EntityList.IDtoClassMapping.put(id, clazz);
    	Map classToIDMapping = ReflectionHelper.getPrivateValue(EntityList.class, null, "classToIDMapping", "field_75624_e", "f");
    	classToIDMapping.put(clazz, id);
    }

    public PhysicsEntity(World world) {
        super(world);
        this.field_145813_c = true; // dropitem
        this.fallHurtMax = 40;
        this.fallHurtAmount = 2.0F;
    }

    public PhysicsEntity(World world, double x, double y, double z, Block block) {
        this(world, x, y, z, block, 0);
    }

    public PhysicsEntity(World world, double x, double y, double z, Block block, int meta) {
    	super(world);
    	
        this.field_145813_c = true; // dropitem
        this.fallHurtMax = 40;
        this.fallHurtAmount = 2.0F;
        this.block = block;
        this.field_145814_a = meta; // meta
        //this.field_145812_b = 1;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        
    }
    
    

    
    
    private void dropBlock() {
    	if(explosionSize <= 0F) {
    		return;
    	}
    	
    	
    	
    	int i = MathHelper.floor_double(this.posX);
        int j = MathHelper.floor_double(this.posY);
        int k = MathHelper.floor_double(this.posZ);
    	
    	if(block.canDropFromExplosion(null)) {
            block.dropBlockAsItemWithChance(this.worldObj, i, j, k, this.worldObj.getBlockMetadata(i, j, k), HandleExplosion.fallingblockDropChance / explosionSize, 0);
        }
    	
    	
    }
    
    
    
    @SuppressWarnings("unchecked")
	@Override
    public void onUpdate() {
    	
    	
        if (this.block.getMaterial() == Material.air)
        {
            this.setDead();
        }
        else
        {

        	
        	
            
            
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            ++this.field_145812_b;
            
            
            
            this.motionY -= 0.03999999910593033D;
            
            if(HandleExplosion.fastMoveImpl) {
            	altMoveEntity(this.motionX, this.motionY, this.motionZ);
            	 
            }
            else {
            	this.moveEntity(this.motionX, this.motionY, this.motionZ);
            	
            }

            

            if (!this.worldObj.isRemote)
            {
                int i = MathHelper.floor_double(this.posX);
                int j = MathHelper.floor_double(this.posY);
                int k = MathHelper.floor_double(this.posZ);

                if (this.field_145812_b == 1)
                {
                    if (this.worldObj.getBlock(i, j, k) != this.block)
                    {
                        this.setDead();
                        return;
                    }

                    this.worldObj.setBlockToAir(i, j, k);
                }

                if (this.onGround)
                {
                    this.motionX *= 0.699999988079071D;
                    this.motionZ *= 0.699999988079071D;
                    this.motionY *= -0.5D;

                    if (this.worldObj.getBlock(i, j, k) != Blocks.piston_extension)
                    {
                        this.setDead();
                        Item item = Item.getItemFromBlock(this.block);
                        if(item != null) {
                        	dummy_stack.func_150996_a(item);
                        	dummy_stack.setItemDamage(this.field_145814_a);
                        }
                        
                        if (!this.dontPlaceBlock && this.worldObj.canPlaceEntityOnSide(this.block, i, j, k, true, 1, null, dummy_stack) && !BlockFalling.func_149831_e(this.worldObj, i, j - 1, k) && HandleExplosion.canFallingPlaceHere(this, i, j, k) && this.worldObj.setBlock(i, j, k, this.block, this.field_145814_a, 3))
                        {
                        	
                            if (this.block instanceof BlockFalling)
                            {
                                ((BlockFalling) this.block).func_149828_a(this.worldObj, i, j, k, this.field_145814_a);
                                
                            }
                            
                            if(HandleExplosion.canPlaySound(worldObj) && this.block != Blocks.anvil)
                            	worldObj.playSoundEffect(i + 0.5, j + 0.5, k + 0.5, block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1F) / 2F, block.stepSound.getPitch() * 0.8F);
        					

                            if (this.field_145810_d != null && this.block instanceof ITileEntityProvider)
                            {
                                TileEntity tileentity = this.worldObj.getTileEntity(i, j, k);

                                if (tileentity != null)
                                {
                                    NBTTagCompound nbttagcompound = new NBTTagCompound();
                                    tileentity.writeToNBT(nbttagcompound);
                                    Iterator<String> iterator = this.field_145810_d.func_150296_c().iterator();

                                    while (iterator.hasNext())
                                    {
                                        String s = iterator.next();
                                        NBTBase nbtbase = this.field_145810_d.getTag(s);

                                        if (!s.equals("x") && !s.equals("y") && !s.equals("z"))
                                        {
                                            nbttagcompound.setTag(s, nbtbase.copy());
                                        }
                                    }

                                    tileentity.readFromNBT(nbttagcompound);
                                    tileentity.markDirty();
                                }
                            }
                        }
                        else if (this.field_145813_c && !this.dontPlaceBlock)
                        {
                            this.entityDropItem(new ItemStack(this.block, 1, this.block.damageDropped(this.field_145814_a)), 0.0F);
                        }
                        else {
                        	dropBlock();
                        }
                    }
                }
                else if (this.field_145812_b > 100 && !this.worldObj.isRemote && (j < 1 || j > 256) || this.field_145812_b > 600)
                {
                    if (this.field_145813_c)
                    {
                        this.entityDropItem(new ItemStack(this.block, 1, this.block.damageDropped(this.field_145814_a)), 0.0F);
                    }

                    this.setDead();
                }
            }
            
            this.motionY *= 0.9800000190734863D;
            this.motionX *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;
            
           
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    protected void fall(float distance) {
        if (this.hurtEntities)
        {
            int i = MathHelper.ceiling_float_int(distance - 1.0F);

            if (i > 0)
            {
            	List<Entity> entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox);
                boolean isAnvil = this.block == Blocks.anvil;
                DamageSource damagesource = isAnvil ? DamageSource.anvil : DamageSource.fallingBlock;
                
                for(Entity entity : entities) {
                	entity.attackEntityFrom(damagesource, Math.min(MathHelper.floor_float(i * this.fallHurtAmount), this.fallHurtMax));
                }

                if (isAnvil && this.rand.nextFloat() < 0.05F + i * 0.05F) {
                    int anvilDamage = this.field_145814_a >> 2;
                    int rotation = this.field_145814_a & 3;
                    anvilDamage++;

                    if (anvilDamage > 2)
                    {
                        this.dontPlaceBlock = true;
                    }
                    else
                    {
                        this.field_145814_a = rotation | anvilDamage << 2;
                    }
                }
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setByte("Tile", (byte) Block.getIdFromBlock(this.block));
        nbt.setInteger("TileID", Block.getIdFromBlock(this.block));
        nbt.setByte("Data", (byte) this.field_145814_a);
        nbt.setByte("Time", (byte) this.field_145812_b);
        nbt.setBoolean("DropItem", this.field_145813_c);
        nbt.setBoolean("HurtEntities", this.hurtEntities);
        nbt.setFloat("FallHurtAmount", this.fallHurtAmount);
        nbt.setInteger("FallHurtMax", this.fallHurtMax);

        if (this.field_145810_d != null)
        {
            nbt.setTag("TileEntityData", this.field_145810_d);
        }
        
        nbt.setFloat("ExSize", this.explosionSize);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("TileID", 99))
        {
            this.block = Block.getBlockById(nbt.getInteger("TileID"));
        }
        else
        {
            this.block = Block.getBlockById(nbt.getByte("Tile") & 255);
        }

        this.field_145814_a = nbt.getByte("Data") & 255;
        this.field_145812_b = nbt.getByte("Time") & 255;

        if (nbt.hasKey("HurtEntities", 99))
        {
            this.hurtEntities = nbt.getBoolean("HurtEntities");
            this.fallHurtAmount = nbt.getFloat("FallHurtAmount");
            this.fallHurtMax = nbt.getInteger("FallHurtMax");
        }
        else if (this.block == Blocks.anvil)
        {
            this.hurtEntities = true;
        }

        if (nbt.hasKey("DropItem", 99))
        {
            this.field_145813_c = nbt.getBoolean("DropItem");
        }

        if (nbt.hasKey("TileEntityData", 10))
        {
            this.field_145810_d = nbt.getCompoundTag("TileEntityData");
        }

        if (this.block.getMaterial() == Material.air)
        {
            this.block = Blocks.sand;
        }
        
        if (nbt.hasKey("ExSize", 99))
        {
            this.explosionSize = nbt.getFloat("ExSize");
            
        }
        
        
    }

    
    // setDoHurtEntities
    @Override
    public void func_145806_a(boolean hurtEntities) {
        this.hurtEntities = hurtEntities;
    }

    @Override
    public void addEntityCrashInfo(CrashReportCategory crashcategory) {
        super.addEntityCrashInfo(crashcategory);
        crashcategory.addCrashSection("Immitating block ID", Block.getIdFromBlock(this.block));
        crashcategory.addCrashSection("Immitating block data", this.field_145814_a);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double velX, double velY, double velZ) {
    	if(!ignoreVelChange || ticksExisted > 0) {
	        this.motionX = velX;
	        this.motionY = velY;
	        this.motionZ = velZ;
	        ignoreVelChange = true;
        }
    }

    // getImmitatingBlock
    @Override
    public Block func_145805_f() {
        return this.block;
    }
    
    public void setHurtAmountAndMax(float amount, int max) {
    	fallHurtAmount = amount;
    	fallHurtMax = max;
    }
    
    
    public void altMoveEntity(double moveX, double moveY, double moveZ) {
            worldObj.theProfiler.startSection("move");
            ySize *= 0.4F;
            

            if (isInWeb)
            {
                isInWeb = false;
                moveX *= 0.25D;
                moveY *= 0.05000000074505806D;
                moveZ *= 0.25D;
                motionX = 0.0D;
                motionY = 0.0D;
                motionZ = 0.0D;
            }

            final double moveXunchanged = moveX;
            final double moveYunchanged = moveY;
            final double moveZunchanged = moveZ;
            
            final List<AxisAlignedBB> list = FastEntityMovement.getCollidingBoundingBoxes(worldObj, this, boundingBox.addCoord(moveX, moveY, moveZ));

            
            
            
            int i;
            if(moveY != 0.0D) {
	            for(i = 0; i < list.size(); ++i) {
	            	moveY = list.get(i).calculateYOffset(boundingBox, moveY);
	            }
	            
	            boundingBox.offset(0.0D, moveY, 0.0D);
	            
            }

            if(moveX != 0.0D) {

	            for(i = 0; i < list.size(); ++i) {
	                moveX = list.get(i).calculateXOffset(boundingBox, moveX);
	            }
	            
	            if(moveX != 0.0D) {
	            	boundingBox.offset(moveX, 0.0D, 0.0D);
	            }
            }
            
            if(moveZ != 0.0D) {
	            for(i = 0; i < list.size(); ++i) {
	                moveZ = list.get(i).calculateZOffset(boundingBox, moveZ);
	            }
	            
	            if(moveZ != 0.0D) {
	            	boundingBox.offset(0.0D, 0.0D, moveZ);
	            }
            }
            

            

            worldObj.theProfiler.endSection();
            worldObj.theProfiler.startSection("rest");
            posX = (boundingBox.minX + boundingBox.maxX) / 2.0D;
            posY = boundingBox.minY + (double) yOffset - (double) ySize;
            posZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0D;
            isCollidedHorizontally = moveXunchanged != moveX || moveZunchanged != moveZ;
            isCollidedVertically = moveYunchanged != moveY;
            onGround = moveYunchanged != moveY && moveYunchanged < 0.0D;
            isCollided = isCollidedHorizontally || isCollidedVertically;
            updateFallState(moveY, onGround);
            

            if (moveXunchanged != moveX)
            {
                motionX = 0.0D;
            }

            if (moveYunchanged != moveY)
            {
                motionY = 0.0D;
            }

            if (moveZunchanged != moveZ)
            {
                motionZ = 0.0D;
            }

            

            worldObj.theProfiler.endSection();
       }
}