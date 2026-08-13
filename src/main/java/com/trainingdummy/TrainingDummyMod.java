package com.trainingdummy;

import com.mojang.logging.LogUtils;
import com.trainingdummy.config.ClientConfig;
import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.network.DummyCuriosPagePayload;
import com.trainingdummy.network.DummyDamagePayload;
import com.trainingdummy.network.ServerPayloadHandler;
import com.trainingdummy.registry.ModCreativeTabs;
import com.trainingdummy.registry.ModEntities;
import com.trainingdummy.registry.ModIngredientTypes;
import com.trainingdummy.registry.ModItems;
import com.trainingdummy.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@Mod(TrainingDummyMod.MODID)
public class TrainingDummyMod {

    public static final String MODID = "trainingdummy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrainingDummyMod(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModIngredientTypes.INGREDIENT_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerPayloads);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUMMY.get(), DummyEntity.createAttributes().build());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        // .optional() keeps these channels out of the connection handshake's "must match" check,
        // so joining a server that doesn't have this mod at all still works - the mod's features
        // (damage popup, dummy itself) just won't be usable there, instead of the connection
        // being refused outright over a mod-list mismatch.
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(DummyDamagePayload.TYPE, DummyDamagePayload.STREAM_CODEC,
                com.trainingdummy.client.ClientPayloadHandler::handleDummyDamage);
        registrar.playToServer(DummyCuriosPagePayload.TYPE, DummyCuriosPagePayload.STREAM_CODEC,
                ServerPayloadHandler::handleCuriosPage);
    }
}
