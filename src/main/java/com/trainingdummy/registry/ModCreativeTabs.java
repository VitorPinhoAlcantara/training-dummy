package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrainingDummyMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TRAINING_DUMMY_TAB =
            CREATIVE_MODE_TABS.register("training_dummy", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.trainingdummy"))
                    // No withTabsAfter/Before: relative positioning against a shared anchor (e.g.
                    // COMBAT) is exactly what caused a tab-ordering cycle once Artifacts/Relics
                    // (which position their own tabs the same way) were in the mix. Unpositioned
                    // tabs just get appended at the end - no ordering constraint, no cycle risk.
                    .icon(() -> ModItems.DUMMY_SPAWNER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.DUMMY_SPAWNER.get());
                        output.accept(ModItems.LURE_BAIT.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
