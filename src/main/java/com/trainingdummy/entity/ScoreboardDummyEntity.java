package com.trainingdummy.entity;

import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.network.OpenScoreboardScreenPayload;
import com.trainingdummy.rank.LeaderboardCache;
import com.trainingdummy.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class ScoreboardDummyEntity extends DummyEntity {

    public static final Component DEFAULT_NAME = Component.translatable("entity.trainingdummy.scoreboard_dummy");

    public ScoreboardDummyEntity(EntityType<? extends ScoreboardDummyEntity> type, Level level) {
        super(type, level);
        this.setDisplayMetric(DummyDisplayMetric.PER_HIT);
    }

    @Override
    public void openMenuFor(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        PacketDistributor.sendToPlayer(serverPlayer, new OpenScoreboardScreenPayload(
                this.getId(),
                LeaderboardCache.localEntries(),
                LeaderboardCache.globalEntries(),
                LeaderboardCache.globalAvailable(),
                CommonConfig.MODPACK_DISPLAY_NAME.get()));
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof Player player && player.getMainHandItem().is(net.minecraft.world.item.Items.STICK)) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK,
                    this.getSoundSource(), 1.0F, 1.0F);
            this.spawnAtLocation(level, new ItemStack(ModItems.SCOREBOARD_DUMMY_SPAWNER.get()));
            this.discard();
            return true;
        }
        return super.hurtServer(level, source, amount);
    }
}
