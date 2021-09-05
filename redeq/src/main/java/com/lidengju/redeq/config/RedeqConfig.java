package com.lidengju.redeq.config;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 */
public class RedeqConfig {
    /**
     * prefix of redeq
     */
    private String prefix = "";

    /**
     * timeout of acquiring lock, default 3 seconds
     */
    private long acquireLockTimeout = 3L;
    /**
     * timeout of expiring lock, must be greater than pollQueueTimeout, default 20 seconds
     */
    private long expireLockTimeout = 20L;
    /**
     * timeout of poll with default 5 seconds
     * unit seconds.
     */
    private long pollQueueTimeout = 5L;

    /**
     * retry times with default 3 times
     */
    private int retry = 3;

    /**
     * delay time in seconds default 60 seconds
     */
    private long delay = 60L;

    /**
     * the maximum size of JobPool and BucketQueue, default 5,000,000
     */
    private long maxPool = 5000000L;

    /**
     * the number of topic consuming thread
     */
    private int maxTopics = 10;

    /**
     * if display the operation log
     */
    private boolean verbose = false;

    /**
     * schedule period for transferring default 5 seconds, reduce this for real-time improving
     */
    private long schedule = 5;

    /**
     * the number of threads for topical job transferring and consuming concurrently.
     */
    private int concurrency = 3;

    /**
     * get the expire time, it must be greater than the poll time, in case the lock won't be leased in advance.
     *
     * @return expire lock time
     */
    public long getExpireLockTimeout() {
        return Math.max(expireLockTimeout, pollQueueTimeout + 5);
    }

    public void setExpireLockTimeout(long expireLockTimeout) {
        this.expireLockTimeout = expireLockTimeout;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public long getAcquireLockTimeout() {
        return acquireLockTimeout;
    }

    public void setAcquireLockTimeout(long acquireLockTimeout) {
        this.acquireLockTimeout = acquireLockTimeout;
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

    public int getMaxTopics() {
        return maxTopics;
    }

    public void setMaxTopics(int maxTopics) {
        this.maxTopics = maxTopics;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }
}
