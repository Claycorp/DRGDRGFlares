package net.doubledoordev.drgflares.entity;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import net.doubledoordev.drgflares.DRGFlaresConfig;
import net.doubledoordev.drgflares.block.BlockRegistry;
import net.doubledoordev.drgflares.block.FakeLightBlock;
import net.doubledoordev.drgflares.block.FakeLightBlockEntity;

public class FlareEntity extends ThrowableProjectile
{
    BlockPos lightBlockPos = null;
    boolean shouldSpawnFakeLights = true;
    BlockPos lastHitBlock = new BlockPos(0, 256, 0);
    public final static EntityDataAccessor<Float> X_PHY_ROT = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.FLOAT);
    public final static EntityDataAccessor<Float> Y_PHY_ROT = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.FLOAT);
    public final static EntityDataAccessor<Float> Z_PHY_ROT = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.FLOAT);
    public final static EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(FlareEntity.class, EntityDataSerializers.INT);
    Vec3 previousPosition = Vec3.ZERO;

    public FlareEntity(EntityType<? extends ThrowableProjectile> entityType, Level level)
    {
        super(entityType, level);
    }

    public FlareEntity(Level level, LivingEntity livingEntity)
    {
        super(EntityRegistry.FLARE_ENTITY.get(), livingEntity, level);
    }

    @Override
    public void tick()
    {
        //Can't get the movement force if there's no previous position to work from or if they are the same.
        if (!previousPosition.equals(Vec3.ZERO) || !previousPosition.equals(position()))
        {
            //Find out what the "force" behind the object is by subtracting the two positions.
            Vec3 movementForce = previousPosition.subtract(position());
            //Only rotate if there's force.
            if (!movementForce.equals(Vec3.ZERO))
            {
                //Set the entity data so the rendering can rotate the object and then this data can be stored for later use to keep objects rotated correctly on reload.
                entityData.set(X_PHY_ROT, (float) (entityData.get(X_PHY_ROT) + random.nextFloat() * movementForce.x));
                entityData.set(Y_PHY_ROT, (float) (entityData.get(Y_PHY_ROT) + random.nextFloat() * movementForce.y));
                entityData.set(Z_PHY_ROT, (float) (entityData.get(Z_PHY_ROT) + random.nextFloat() * movementForce.z));
            }
        }
        previousPosition = position();

        if (this.getY() < level.getMinBuildHeight())
            this.kill();
        if (!level.isClientSide())
        {
            // Enable gravity again if the block is not on the ground.
            if (isNoGravity() && !getDeltaMovement().equals(Vec3.ZERO) || !isOnGround())
            {
                setNoGravity(false);
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

//    @Override
//    protected float getGravity()
//    {
//        return DRGFlaresConfig.GENERALCONFIG.flareGravity.get().floatValue();
//    }

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
                // make sure our fluid doesn't have a block in it already and we have a source.
                if (level.getBlockState(blockPos).getFluidState().isSource())
                    level.setBlockAndUpdate(blockPos, BlockRegistry.FAKE_LIGHT.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
                else
                    level.setBlockAndUpdate(blockPos, BlockRegistry.FAKE_LIGHT.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
                lightBlockPos = blockPos;
            }
        }
    }

    public BlockPos findLightOrSpace(BlockPos entityPos, Block lightBlock)
    {
        BlockState state = level.getBlockState(entityPos);

        if (state.isAir() || state.is(lightBlock) ||
                (state.getFluidState().isSource() && !state.hasProperty(BlockStateProperties.WATERLOGGED)))
        {
            return entityPos;
        }

        // if all else failed, now we search for a spot.
        // Really couldn't think of a better way to do this.

        // Check around the space we are in a + shape for any air, water or light blocks.
        // This check is done first and separate from the extended search as it's most likely to contain a valid space.
        for (Direction facing : Direction.values())
        {
            BlockState stateCardinalRotation = level.getBlockState(entityPos.relative(facing));
            if (stateCardinalRotation.isAir() || stateCardinalRotation.is(lightBlock) ||
                    (stateCardinalRotation.getFluidState().isSource() && !stateCardinalRotation.hasProperty(BlockStateProperties.WATERLOGGED)))
            {
                return entityPos.relative(facing);
            }
        }

        // Now check in a + shape around the fist checked block that is offset by one direction previous to the origin.
        for (Direction firstStep : Direction.values())
            for (Direction secondStep : Direction.values())
            {
                BlockState stateComplexRotation = level.getBlockState(entityPos.relative(firstStep).relative(secondStep));
                if (stateComplexRotation.isAir() || stateComplexRotation.is(lightBlock) ||
                        (stateComplexRotation.getFluidState().isSource() && !stateComplexRotation.hasProperty(BlockStateProperties.WATERLOGGED)))
                {
                    return entityPos.relative(firstStep).relative(secondStep);
                }
            }

        return null;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void addAdditionalSaveData(CompoundTag compoundTag)
    {
        super.addAdditionalSaveData(compoundTag);
        if (lightBlockPos != null)
            NbtUtils.writeBlockPos(lightBlockPos);
        compoundTag.putFloat("xPhyRot", this.entityData.get(X_PHY_ROT));
        compoundTag.putFloat("yPhyRot", this.entityData.get(Y_PHY_ROT));
        compoundTag.putFloat("zPhyRot", this.entityData.get(Z_PHY_ROT));
        compoundTag.putInt("color", this.entityData.get(COLOR));
    }

    @Override
    @ParametersAreNonnullByDefault
    public void readAdditionalSaveData(CompoundTag compoundTag)
    {
        super.readAdditionalSaveData(compoundTag);
        lightBlockPos = NbtUtils.readBlockPos(compoundTag);
        this.entityData.set(X_PHY_ROT, compoundTag.getFloat("xPhyRot"));
        this.entityData.set(Y_PHY_ROT, compoundTag.getFloat("yPhyRot"));
        this.entityData.set(Z_PHY_ROT, compoundTag.getFloat("zPhyRot"));
        this.entityData.set(COLOR, compoundTag.getInt("color"));
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void onHitEntity(EntityHitResult hitResult)
    {
        super.onHitEntity(hitResult);
        Entity entity = hitResult.getEntity();

        if (!this.level.isClientSide && entity instanceof LivingEntity livingEntity)
        {
            if (DRGFlaresConfig.GENERALCONFIG.hitEntityGlows.get())
                livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, DRGFlaresConfig.GENERALCONFIG.entityGlowingTime.get(), 0, false, false));

            this.level.broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @ParametersAreNonnullByDefault
    @Override
    protected void onHitBlock(BlockHitResult hitResult)
    {
        Vec3 vec = this.getDeltaMovement();
        super.onHitBlock(hitResult);

        BlockPos hitPos = hitResult.getBlockPos();

        if (!lastHitBlock.equals(hitPos) && !level.getBlockState(hitPos).getCollisionShape(level, hitPos).isEmpty())
        {
            Direction directionOpposite = hitResult.getDirection().getOpposite();
            double bounceDampeningModifier = DRGFlaresConfig.GENERALCONFIG.bounceModifier.get();

            lastHitBlock = hitPos;

            double vecX = vec.x / bounceDampeningModifier;
            double vecY = vec.y / bounceDampeningModifier;
            double vecZ = vec.z / bounceDampeningModifier;

            switch (directionOpposite)
            {
                case UP, DOWN -> setDeltaMovement(vecX, -vecY, vecZ);
                case EAST, WEST -> setDeltaMovement(-vecX, vecY, vecZ);
                case NORTH, SOUTH -> setDeltaMovement(vecX, vecY, -vecZ);
            }
        }
        else if (lastHitBlock.equals(hitPos))
        {
            setDeltaMovement(Vec3.ZERO);
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