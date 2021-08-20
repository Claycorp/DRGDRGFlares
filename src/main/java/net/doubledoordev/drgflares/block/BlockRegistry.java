package net.doubledoordev.drgflares.block;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static net.doubledoordev.drgflares.DRGFlares.MOD_ID;


public class BlockRegistry
{
    public static final DeferredRegister<Block> BLOCK_DEFERRED = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);

    // Blocks
    public static final RegistryObject<Block> FAKE_LIGHT = register("fake_light", () -> new FakeLightBlock(AbstractBlock.Properties.of(Material.AIR).noDrops().noOcclusion().noCollission()));

    // Block Entities
    public static final RegistryObject<TileEntityType<FakeLightBlockEntity>> FAKE_LIGHT_BE = register("fake_light", FakeLightBlockEntity::new, FAKE_LIGHT);

    @SuppressWarnings("ConstantConditions")
    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> factory, Supplier<? extends Block> block)
    {
        return TILE_ENTITY_DEFERRED.register(name, () -> TileEntityType.Builder.of(factory, block.get()).build(null));
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier)
    {
        final String actualName = name.toLowerCase(Locale.ROOT);
        return BLOCK_DEFERRED.register(actualName, blockSupplier);
    }
}
