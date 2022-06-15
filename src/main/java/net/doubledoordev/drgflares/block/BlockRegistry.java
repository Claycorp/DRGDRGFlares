package net.doubledoordev.drgflares.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.doubledoordev.drgflares.DRGFlares.MODID;


public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCK_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);

    // Blocks
    public static final RegistryObject<Block> FAKE_LIGHT = register("fake_light", () -> new FakeLightBlock(BlockBehaviour.Properties.of(Material.AIR).noDrops().noOcclusion().noCollission()));

    @SuppressWarnings("ConstantConditions")
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block)
    {
        return TILE_ENTITY_DEFERRED.register(name, () -> BlockEntityType.Builder.of(factory, block.get()).build(null));
    }    // Block Entities

    public static final RegistryObject<BlockEntityType<FakeLightBlockEntity>> FAKE_LIGHT_BE = register("fake_light", FakeLightBlockEntity::new, FAKE_LIGHT);


    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return BLOCK_DEFERRED.register(actualName, blockSupplier);
    }
}
