package io.kamax.mxisd.hash;

import io.kamax.mxisd.config.HashingConfig;
import io.kamax.mxisd.hash.rotation.HashRotationStrategy;
import io.kamax.mxisd.hash.rotation.NoOpRotationStrategy;
import io.kamax.mxisd.hash.rotation.RotationPerRequests;
import io.kamax.mxisd.hash.storage.EmptyStorage;
import io.kamax.mxisd.hash.storage.HashStorage;
import io.kamax.mxisd.hash.storage.InMemoryHashStorage;
import io.kamax.mxisd.lookup.provider.IThreePidProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HashManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(HashManager.class);

    private HashEngine hashEngine;
    private HashRotationStrategy rotationStrategy;
    private HashStorage hashStorage;
    private HashingConfig config;
    private AtomicBoolean configured = new AtomicBoolean(false);

    public void init(HashingConfig config, List<? extends IThreePidProvider> providers) {
        this.config = config;
        initStorage();
        hashEngine = new HashEngine(providers, getHashStorage(), config);
        initRotationStrategy();
        configured.set(true);
    }

    private void initStorage() {
        if (config.isEnabled()) {
            switch (config.getHashStorageType()) {
                case IN_MEMORY:
                    this.hashStorage = new InMemoryHashStorage();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown storage type: " + config.getHashStorageType());
            }
        } else {
            this.hashStorage = new EmptyStorage();
        }
    }

    private void initRotationStrategy() {
        if (config.isEnabled()) {
            switch (config.getRotationPolicy()) {
                case PER_REQUESTS:
                    this.rotationStrategy = new RotationPerRequests();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown rotation type: " + config.getHashStorageType());
            }
        } else {
            this.rotationStrategy = new NoOpRotationStrategy();
        }

        this.rotationStrategy.register(getHashEngine());
    }

    public HashEngine getHashEngine() {
        return hashEngine;
    }

    public HashRotationStrategy getRotationStrategy() {
        return rotationStrategy;
    }

    public HashStorage getHashStorage() {
        return hashStorage;
    }

    public HashingConfig getConfig() {
        return config;
    }
}