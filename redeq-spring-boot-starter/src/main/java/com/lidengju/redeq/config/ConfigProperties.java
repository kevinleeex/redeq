package com.lidengju.redeq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import static com.lidengju.redeq.config.ConfigProperties.PREFIX;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 **/
@ConfigurationProperties(prefix = PREFIX)
@Configuration
public class ConfigProperties {
    public static final String PREFIX = "redeq";

    /**
     * Nested Configuration for App
     */
    @NestedConfigurationProperty
    private App app = new App();
    /**
     * Nested Configuration for Lock
     */
    @NestedConfigurationProperty
    private Lock lock = new Lock();

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public RedeqConfig getConfig() {
        RedeqConfig redeqConfig = new RedeqConfig();
        redeqConfig.setVerbose(this.app.verbose);
        redeqConfig.setPrefix(this.app.prefix);
        redeqConfig.setDelay(this.app.delay);
        redeqConfig.setRetry(this.app.retry);
        redeqConfig.setMaxPool(this.app.maxPool);
        redeqConfig.setMaxTopics(this.app.maxTopics);
        redeqConfig.setMaxSubscribers(this.app.maxSubscribers);
        redeqConfig.setSchedule(this.app.schedule);
        redeqConfig.setPollQueueTimeout(this.app.pollQueueTimeout);
        redeqConfig.setConcurrency(this.app.concurrency);
        redeqConfig.setAcquireLockTimeout(this.lock.acquireLockTimeout);
        redeqConfig.setExpireLockTimeout(this.lock.expireLockTimeout);
        return redeqConfig;
    }

    /**
     * Config properties about App
     */
    public static class App {
        /**
         * prefix of redeq
         */
        private String prefix = "";

        /**
         * timeout of poll, default 5 seconds
         */
        private long pollQueueTimeout = 5L;
        /**
         * retry times with default 3
         */
        private int retry = 3;
        /**
         * delay time in seconds default 60 seconds
         */
        private long delay = 60L;
        /**
         * the maximum size of JobPool and DelayedBucket, default 5,000,000
         */
        private long maxPool = 5000000L;

        /**
         * the maximum size of topical consuming threads
         */
        @Deprecated
        private int maxTopics = 10;
        /**
         * the number of topic subscribers
         */
        private int maxSubscribers = 10;
        /**
         * if display the operation log
         */
        private boolean verbose = false;
        /**
         * schedule period for transferring default 5 seconds, reduce this for real-time improving
         */
        private long schedule = 5L;
        /**
         * the number of threads for topical job transferring and consuming concurrently.
         * set this parameter according to number of your instances, default 1.
         */
        private int concurrency = 1;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public long getPollQueueTimeout() {
            return pollQueueTimeout;
        }

        public void setPollQueueTimeout(long pollQueueTimeout) {
            this.pollQueueTimeout = pollQueueTimeout;
        }

        public int getRetry() {
            return retry;
        }

        public void setRetry(int retry) {
            this.retry = retry;
        }

        public long getDelay() {
            return delay;
        }

        public void setDelay(long delay) {
            this.delay = delay;
        }

        public long getMaxPool() {
            return maxPool;
        }

        public void setMaxPool(long maxPool) {
            this.maxPool = maxPool;
        }

        public boolean isVerbose() {
            return verbose;
        }

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public long getSchedule() {
            return schedule;
        }

        public void setSchedule(long schedule) {
            this.schedule = schedule;
        }

        @Deprecated
        public int getMaxTopics() {
            return maxTopics;
        }

        @Deprecated
        public void setMaxTopics(int maxTopics) {
            this.maxSubscribers = maxTopics;
            this.maxTopics = maxTopics;
        }

        public int getConcurrency() {
            return concurrency;
        }

        public void setConcurrency(int concurrency) {
            this.concurrency = concurrency;
        }

        public int getMaxSubscribers() {
            return maxSubscribers;
        }

        public void setMaxSubscribers(int maxSubscribers) {
            this.maxSubscribers = maxSubscribers;
        }
    }

    /**
     * Config properties about Lock.
     */
    public static class Lock {
        /**
         * timeout of acquiring lock, default 3 seconds
         */
        private long acquireLockTimeout = 3L;
        /**
         * timeout of expiring lock, must be greater than pollQueueTimeout, default 20 seconds
         */
        private long expireLockTimeout = 20L;

        public long getAcquireLockTimeout() {
            return acquireLockTimeout;
        }

        public void setAcquireLockTimeout(long acquireLockTimeout) {
            this.acquireLockTimeout = acquireLockTimeout;
        }

        public long getExpireLockTimeout() {
            return expireLockTimeout;
        }

        public void setExpireLockTimeout(long expireLockTimeout) {
            this.expireLockTimeout = expireLockTimeout;
        }
    }
}
