package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TrainingDummyMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DUMMY_HURT = SOUND_EVENTS.register("entity.dummy.hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> DANRIQUE_HURT = SOUND_EVENTS.register("entity.dummy.danrique_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.danrique_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MITINHOPLAYER_HURT = SOUND_EVENTS.register("entity.dummy.mitinhoplayer_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.mitinhoplayer_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NOFAXU_HURT = SOUND_EVENTS.register("entity.dummy.nofaxu_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.nofaxu_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> BRUNIMNEETS_HURT = SOUND_EVENTS.register("entity.dummy.brunimneets_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.brunimneets_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MAMAO170_HURT = SOUND_EVENTS.register("entity.dummy.mamao170_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.mamao170_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> JAZARAGAMER_HURT = SOUND_EVENTS.register("entity.dummy.jazaragamer_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.jazaragamer_hurt")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MEIOELFO_HURT = SOUND_EVENTS.register("entity.dummy.meioelfo_hurt",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.meioelfo_hurt")));

    private ModSounds() {
    }
}
