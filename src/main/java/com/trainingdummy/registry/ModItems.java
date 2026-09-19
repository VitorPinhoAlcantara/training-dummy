package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.item.DummySpawnItem;
import com.trainingdummy.item.LureBaitItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrainingDummyMod.MODID);

    public static final DeferredItem<DummySpawnItem> DUMMY_SPAWNER = ITEMS.registerItem("dummy_spawner",
            properties -> new DummySpawnItem(properties.stacksTo(64)));

    public static final DeferredItem<LureBaitItem> LURE_BAIT = ITEMS.registerItem("lure_bait",
            properties -> new LureBaitItem(properties.stacksTo(64)));

    private ModItems() {
    }
}
