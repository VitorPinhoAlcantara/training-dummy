package com.trainingdummy.config;

/**
 * Generated at build time by the {@code generateWorkerSecrets} Gradle task from secrets.properties
 * (gitignored, never committed - see secrets.properties.example). This file itself lives under
 * build/ and is regenerated on every build; don't edit it directly.
 */
final class WorkerSecrets {

    static final String DEFAULT_MODPACK_ID = "${worker_default_modpack_id}";
    static final String DEFAULT_MODPACK_API_KEY = "${worker_default_modpack_api_key}";
    static final String DEFAULT_WORKER_BASE_URL = "${worker_default_base_url}";

    private WorkerSecrets() {
    }
}
