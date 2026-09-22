package com.trainingdummy.event;

import com.trainingdummy.TrainingDummyMod;
import com.trainingdummy.config.CommonConfig;
import com.trainingdummy.persistence.LeaderboardPersistence;
import com.trainingdummy.rank.HttpLeaderboardService;
import com.trainingdummy.rank.LeaderboardCache;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = TrainingDummyMod.MODID)
public final class ServerLifecycleEvents {

    @SubscribeEvent
    static void onServerStarting(ServerStartingEvent event) {
        String workerBaseUrl = CommonConfig.effectiveWorkerBaseUrl();
        LeaderboardCache.setService(new HttpLeaderboardService(workerBaseUrl, CommonConfig.effectiveWorkerApiKey()));
        TrainingDummyMod.LOGGER.info("Dummy Scoreboard: reporting to worker at {}", workerBaseUrl);

        LeaderboardPersistence.bind(event.getServer());
    }

    @SubscribeEvent
    static void onServerStopping(ServerStoppingEvent event) {
        LeaderboardPersistence.unbind();
    }

    private ServerLifecycleEvents() {
    }
}
