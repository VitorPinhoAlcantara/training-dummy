package com.trainingdummy.registry;

import com.trainingdummy.TrainingDummyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TrainingDummyMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> DUMMY_HURT = SOUND_EVENTS.register("entity.dummy.hurt",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(TrainingDummyMod.MODID, "entity.dummy.hurt")));

    private ModSounds() {
    }
}
