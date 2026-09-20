package com.trainingdummy;

import com.mojang.logging.LogUtils;
import com.trainingdummy.config.ClientConfig;
import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.entity.DummyEntity;
import com.trainingdummy.entity.JackDummyEntity;
import com.trainingdummy.network.DummyClearEffectsPayload;
import com.trainingdummy.network.DummyCuriosPagePayload;
import com.trainingdummy.network.DummyDamagePayload;
import com.trainingdummy.network.DummySetDisplayMetricPayload;
import com.trainingdummy.network.DummySetMaxHealthPayload;
import com.trainingdummy.network.ServerPayloadHandler;
import com.trainingdummy.registry.ModCreativeTabs;
import com.trainingdummy.registry.ModDataComponents;
import com.trainingdummy.registry.ModEntities;
import com.trainingdummy.registry.ModIngredientTypes;
import com.trainingdummy.registry.ModRecipeSerializers;
import com.trainingdummy.registry.ModItems;
import com.trainingdummy.registry.ModMenus;
import com.trainingdummy.registry.ModSounds;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
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


    private static final double WIDENED_ATTRIBUTE_CEILING = 1.0E9;

    public TrainingDummyMod(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModIngredientTypes.INGREDIENT_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerPayloads);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        this.widenVanillaAttributeRanges();
    }


    private void widenVanillaAttributeRanges() {
        widenRange(Attributes.MAX_HEALTH.value());
        widenRange(Attributes.ATTACK_DAMAGE.value());
        widenRange(Attributes.ARMOR.value());
    }

    private static void widenRange(Attribute attribute) {
        if (attribute instanceof RangedAttribute ranged) {
            ranged.maxValue = WIDENED_ATTRIBUTE_CEILING;
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DUMMY.get(), DummyEntity.createAttributes().build());
        event.put(ModEntities.JACK.get(), JackDummyEntity.createAttributes().build());
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {





        PayloadRegistrar registrar = event.registrar("3").optional();
        registrar.playToClient(DummyDamagePayload.TYPE, DummyDamagePayload.STREAM_CODEC,
                com.trainingdummy.client.ClientPayloadHandler::handleDummyDamage);
        registrar.playToServer(DummyCuriosPagePayload.TYPE, DummyCuriosPagePayload.STREAM_CODEC,
                ServerPayloadHandler::handleCuriosPage);
        registrar.playToServer(DummySetMaxHealthPayload.TYPE, DummySetMaxHealthPayload.STREAM_CODEC,
                ServerPayloadHandler::handleSetMaxHealth);
        registrar.playToServer(DummySetDisplayMetricPayload.TYPE, DummySetDisplayMetricPayload.STREAM_CODEC,
                ServerPayloadHandler::handleSetDisplayMetric);
        registrar.playToServer(DummyClearEffectsPayload.TYPE, DummyClearEffectsPayload.STREAM_CODEC,
                ServerPayloadHandler::handleClearEffects);
    }
}
