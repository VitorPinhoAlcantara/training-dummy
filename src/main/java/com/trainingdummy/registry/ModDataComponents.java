package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.item.DummyStoredData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TrainingDummyMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<DummyStoredData>> DUMMY_DATA =
            DATA_COMPONENTS.registerComponentType("dummy_data", builder -> builder
                    .persistent(DummyStoredData.CODEC)
                    .networkSynchronized(DummyStoredData.STREAM_CODEC));

    private ModDataComponents() {
    }
}
