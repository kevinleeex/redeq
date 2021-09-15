package com.lidengju.redeq.scheduled;

import com.lidengju.redeq.config.RedeqConfig;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Timer for transferring job from Bucket Queue to Ready Queue
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 **/
public class RedeqTimer {

    private static final Logger log = LoggerFactory.getLogger(RedeqTimer.class);
    private static final RedeqTimer redeqTimer = new RedeqTimer();
    /**
     * scheduler running flag
     */
    private static boolean isRunning = false;
    private ScheduledExecutorService scheduledExecutorService = null;
    private RedissonClient redissonClient;
    private RedeqConfig redeqConfig;

    private RedeqTimer() {

    }

    public static RedeqTimer getTimer() {
        return redeqTimer;
    }

    public void init(RedissonClient redissonClient, RedeqConfig config) {
        this.redissonClient = redissonClient;
        this.redeqConfig = config;
        scheduledExecutorService = Executors.newScheduledThreadPool(config.getConcurrency());
    }

    /**
     * start scheduling thread for transferring delayed job from Bucket Queue to Ready Queue
     */
    public synchronized void startTransfer() {
        if (redissonClient == null || redeqConfig == null) {
            log.error("[ReDeQ] RedeqTimer is not initialized!");
        }
        // make sure one instance runs only concurrency pulling scheduler
        if (!isRunning) {
            registerShutdownProcess();
            for (int i = 0; i < redeqConfig.getConcurrency(); i++) {
                scheduledExecutorService.scheduleAtFixedRate(new PullingTask(redissonClient, redeqConfig, i),
                        0, redeqConfig.getSchedule(), TimeUnit.SECONDS);
            }
            isRunning = true;
            log.info("[ReDeQ] Job transferring started with concurrency of {}! Action will be executed every {} seconds.", redeqConfig.getConcurrency(), redeqConfig.getSchedule());
        }
    }

    private void registerShutdownProcess() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                scheduledExecutorService.shutdown();
                log.warn("[ReDeQ] Job transferring will shutdown soon!");
                if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("[ReDeQ] Job transferring did not shutdown gracefully within "
                            + "20 seconds. Proceeding will forceful shutdown");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
}
