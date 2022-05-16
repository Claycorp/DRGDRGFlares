package net.doubledoordev.drgflares.entity;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import net.doubledoordev.drgflares.DRGFlaresConfig;
import net.doubledoordev.drgflares.block.BlockRegistry;
import net.doubledoordev.drgflares.block.FakeLightBlock;
import net.doubledoordev.drgflares.block.FakeLightBlockEntity;

public class FlareEntity extends ThrowableEntity
{
    BlockPos lightBlockPos = null;
    boolean shouldSpawnFakeLights = true;
    BlockPos lastHitBlock = new BlockPos(0, 256, 0);
    public final static DataParameter<Float> X_PHY_ROT = EntityDataManager.defineId(FlareEntity.class, DataSerializers.FLOAT);
    public final static DataParameter<Float> Y_PHY_ROT = EntityDataManager.defineId(FlareEntity.class, DataSerializers.FLOAT);
    public final static DataParameter<Float> Z_PHY_ROT = EntityDataManager.defineId(FlareEntity.class, DataSerializers.FLOAT);
    public final static DataParameter<Integer> COLOR = EntityDataManager.defineId(FlareEntity.class, DataSerializers.INT);
    Vector3d previousPosition = Vector3d.ZERO;

    public FlareEntity(EntityType<? extends ThrowableEntity> entityType, World world)
    {
        super(entityType, world);
    }

    public FlareEntity(World world, LivingEntity livingEntity)
    {
        super(EntityRegistry.FLARE_ENTITY.get(), livingEntity, world);
    }

    @Override
    public void tick()
    {
        //Can't get the movement force if there's no previous position to work from or if they are the same.
        if (!previousPosition.equals(Vector3d.ZERO) || !previousPosition.equals(position()))
        {
            //Find out what the "force" behind the object is by subtracting the two positions.
            Vector3d movementForce = previousPosition.subtract(position());
            //Only rotate if there's force.
            if (!movementForce.equals(Vector3d.ZERO))
            {
                //Set the entity data so the rendering can rotate the object and then this data can be stored for later use to keep objects rotated correctly on reload.
                this.entityData.set(X_PHY_ROT, (float) (entityData.get(X_PHY_ROT) + random.nextFloat() * movementForce.x));
                this.entityData.set(Y_PHY_ROT, (float) (entityData.get(Y_PHY_ROT) + random.nextFloat() * movementForce.y));
                this.entityData.set(Z_PHY_ROT, (float) (entityData.get(Z_PHY_ROT) + random.nextFloat() * movementForce.z));
            }
        }
        previousPosition = position();

        //TODO: Replace in 1.17+
        if (getY() <= 0)
            this.kill();
        if (!level.isClientSide())
        {

            if (level.getBlockState(this.getOnPos()).is(Blocks.AIR))
            {
                this.setNoGravity(false);
            }

            // Make sure to call the super, so we actually move unless we plan on rewriting the whole movement lot.
            super.tick();
            // Make sure we have a way to clean up the entities.
            if (tickCount > DRGFlaresConfig.GENERALCONFIG.entityDecayTime.get() + DRGFlaresConfig.GENERALCONFIG.lightDecayTime.get())
                this.kill();

            if (shouldSpawnFakeLights)
            {
                BlockPos entityPos = blockPosition();
                Block lightBlock = BlockRegistry.FAKE_LIGHT.get().defaultBlockState().getBlock();

                // Check if we have a stored pos already and if it's a valid light block.
                if (lightBlockPos != null && level.getBlockState(lightBlockPos).is(lightBlock))
                {
                    // make sure our light block is closer than 3 blocks as entities can slide causing the light block to
                    // become offset from the entity by way too far. Then we can either update the existing one or replace it.
                    if (lightBlockPos.closerThan(entityPos, 1.5))
                        setTEDataOrBlock(lightBlockPos);
                    else setTEDataOrBlock(findLightOrSpace(entityPos, lightBlock));
                }
                else setTEDataOrBlock(findLightOrSpace(entityPos, lightBlock));
            }
        }
    }

    @Override
    protected float getGravity()
    {
        return DRGFlaresConfig.GENERALCONFIG.flareGravity.get().floatValue();
    }

    // You need this because vanilla be stupid and doesn't give a shit.
    @Override
    @Nonnull
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setTEDataOrBlock(BlockPos blockPos)
    {
        // check for null as we start null till the first block is placed.
        if (blockPos != null)
        {
            FakeLightBlockEntity lightTE = (FakeLightBlockEntity) level.getBlockEntity(blockPos);
            if (lightTE != null)
            {
                // make sure to disable the light at the correct time.
                if (DRGFlaresConfig.GENERALCONFIG.lightDecayTime.get() <= tickCount)
                {
                    level.getBlockState(blockPos).setValue(FakeLightBlock.LIT, false);
                    shouldSpawnFakeLights = false;
                }
                else
                    lightTE.setNextCheckIn(DRGFlaresConfig.GENERALCONFIG.noSourceDecayTime.get());
            }
            else
            {
                level.setBlockAndUpdate(blockPos, BlockRegistry.FAKE_LIGHT.get().defaultBlockState());
                lightBlockPos = blockPos;
            }
        }
    }

    public BlockPos findLightOrSpace(BlockPos entityPos, Block lightBlock)
    {

        if (level.getBlockState(entityPos).isAir() || level.getBlockState(entityPos).is(lightBlock))
        {
            return entityPos;
        }

        // if all else failed, now we search for a spot.
        // Really couldn't think of a better way to do this.

        // Check around the space we are in a + shape for any air or light blocks.
        // This check is done first and separate from the extended search as it's most likely to contain a valid space.
        for (Direction facing : Direction.values())
        {
            if (level.getBlockState(entityPos.relative(facing)).isAir() || level.getBlockState(entityPos.relative(facing)).getBlock().is(lightBlock))
            {
                return entityPos.relative(facing);
            }
        }

        // Now check in a + shape around the fist checked block that is offset by one direction previous to the origin.
        for (Direction firstStep : Direction.values())
            for (Direction secondStep : Direction.values())
            {
                if (level.getBlockState(entityPos.relative(firstStep).relative(secondStep)).isAir() || level.getBlockState(entityPos.relative(firstStep).relative(secondStep)).getBlock().is(lightBlock))
                {
                    return entityPos.relative(firstStep).relative(secondStep);
                }
            }

        return null;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addAdditionalSaveData(CompoundNBT compoundNBT)
    {
        super.addAdditionalSaveData(compoundNBT);
        if (lightBlockPos != null)
            NBTUtil.writeBlockPos(lightBlockPos);
        compoundNBT.putFloat("xPhyRot", this.entityData.get(X_PHY_ROT));
        compoundNBT.putFloat("yPhyRot", this.entityData.get(Y_PHY_ROT));
        compoundNBT.putFloat("zPhyRot", this.entityData.get(Z_PHY_ROT));
        compoundNBT.putInt("color", this.entityData.get(COLOR));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void readAdditionalSaveData(CompoundNBT compoundNBT)
    {
        super.readAdditionalSaveData(compoundNBT);
        lightBlockPos = NBTUtil.readBlockPos(compoundNBT);
        this.entityData.set(X_PHY_ROT, compoundNBT.getFloat("xPhyRot"));
        this.entityData.set(Y_PHY_ROT, compoundNBT.getFloat("yPhyRot"));
        this.entityData.set(Z_PHY_ROT, compoundNBT.getFloat("zPhyRot"));
        this.entityData.set(COLOR, compoundNBT.getInt("color"));
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onHitEntity(EntityRayTraceResult entityRayTraceResult)
    {
        super.onHitEntity(entityRayTraceResult);
        Entity entity = entityRayTraceResult.getEntity();

        if (!this.level.isClientSide && entity instanceof LivingEntity)
        {
            LivingEntity livingEntity = (LivingEntity) entity;
            if (DRGFlaresConfig.GENERALCONFIG.hitEntityGlows.get())
                livingEntity.addEffect(new EffectInstance(Effects.GLOWING, DRGFlaresConfig.GENERALCONFIG.entityGlowingTime.get(), 0, false, false));

            this.level.broadcastEntityEvent(this, (byte) 3);
            this.remove();
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void onHitBlock(BlockRayTraceResult rayTraceResult)
    {
        Vector3d vec = this.getDeltaMovement();
        super.onHitBlock(rayTraceResult);

        BlockPos hitPos = rayTraceResult.getBlockPos();

        if (!lastHitBlock.equals(hitPos) && !level.getBlockState(hitPos).getCollisionShape(level, hitPos).isEmpty())
        {
            Direction directionOpposite = rayTraceResult.getDirection().getOpposite();
            double bounceDampeningModifier = DRGFlaresConfig.GENERALCONFIG.bounceModifier.get();

            lastHitBlock = hitPos;

            double vecX = vec.x / bounceDampeningModifier;
            double vecY = vec.y / bounceDampeningModifier;
            double vecZ = vec.z / bounceDampeningModifier;

            switch (directionOpposite)
            {
                case UP:
                case DOWN:
                    setDeltaMovement(vecX, -vecY, vecZ);
                    break;
                case EAST:
                case WEST:
                    setDeltaMovement(-vecX, vecY, vecZ);
                    break;
                case NORTH:
                case SOUTH:
                    setDeltaMovement(vecX, vecY, -vecZ);
            }
        }
        else if (lastHitBlock.equals(hitPos))
        {
            setDeltaMovement(Vector3d.ZERO);
            setNoGravity(true);
        }
    }

    public void setColor(int color)
    {
        this.entityData.set(COLOR, color);
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(X_PHY_ROT, random.nextFloat());
        this.entityData.define(Y_PHY_ROT, random.nextFloat());
        this.entityData.define(Z_PHY_ROT, random.nextFloat());
        this.entityData.define(COLOR, 10907634);
    }

    @Override
    public boolean isAttackable()
    {
        return false;
    }

    //Removes the entity fire.
    @Override
    public boolean fireImmune()
    {
        return true;
    }
}