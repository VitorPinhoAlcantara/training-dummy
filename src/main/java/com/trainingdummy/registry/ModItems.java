package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.item.DummySpawnItem;
import com.trainingdummy.item.LureBaitItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrainingDummyMod.MODID);

    public static final DeferredItem<Item> DUMMY_SPAWNER = ITEMS.register("dummy_spawner",
            () -> new DummySpawnItem(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> LURE_BAIT = ITEMS.register("lure_bait",
            () -> new LureBaitItem(new Item.Properties().stacksTo(64)));

    private ModItems() {
    }
}
