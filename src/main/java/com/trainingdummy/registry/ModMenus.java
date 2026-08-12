package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.menu.DummyMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(net.minecraft.core.registries.BuiltInRegistries.MENU, TrainingDummyMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DummyMenu>> DUMMY_MENU =
            MENUS.register("dummy", () -> IMenuTypeExtension.create(DummyMenu::new));

    private ModMenus() {
    }
}
