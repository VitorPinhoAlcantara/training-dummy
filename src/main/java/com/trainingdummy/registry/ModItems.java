package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.item.DummySpawnItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TrainingDummyMod.MODID);

    public static final DeferredItem<Item> DUMMY_SPAWNER = ITEMS.register("dummy_spawner",
            () -> new DummySpawnItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> LURE_BAIT = ITEMS.register("lure_bait",
            () -> new Item(new Item.Properties().stacksTo(16)));

    private ModItems() {
    }
}
