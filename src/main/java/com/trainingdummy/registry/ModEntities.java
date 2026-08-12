package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.entity.DummyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE, TrainingDummyMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<DummyEntity>> DUMMY =
            ENTITY_TYPES.register("dummy", () -> EntityType.Builder.of(DummyEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(TrainingDummyMod.MODID + ":dummy"));

    private ModEntities() {
    }
}
