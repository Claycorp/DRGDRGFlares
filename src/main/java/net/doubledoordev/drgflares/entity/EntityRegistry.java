package net.doubledoordev.drgflares.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static net.doubledoordev.drgflares.DRGFlares.MOD_ID;
import static net.minecraft.entity.EntityClassification.MISC;

public class EntityRegistry
{
    // Entity registry
    public static final DeferredRegister<EntityType<?>> ENTITY_DEFERRED = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    // Flare Entity Type
    public static final RegistryObject<EntityType<FlareEntity>> FLARE_ENTITY = register("flare", EntityType.Builder.<FlareEntity>of(FlareEntity::new, MISC).sized(0.5f, 0.5f).clientTrackingRange(30));

    public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, EntityType.Builder<E> builder)
    {
        return ENTITY_DEFERRED.register(name, () -> {
            final String id = MOD_ID + ":" + name;
            return builder.build(id);
        });
    }
}
