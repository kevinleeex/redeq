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
 * @date 2021/9/4
 **/
public class RedeqTimer {

    private static final Logger log = LoggerFactory.getLogger(RedeqTimer.class);

    private static final ScheduledExecutorService scheduledExecutorService;
    /**
     * scheduler running flag
     */
    private static boolean isRunning = false;

    static {
        scheduledExecutorService = Executors.newScheduledThreadPool(2);
    }

    private RedeqTimer() {

    }

    /**
     * start scheduling thread for transferring delayed job from Bucket Queue to Ready Queue
     */
    public static synchronized void startTransfer(RedissonClient redissonClient, RedeqConfig config) {
        // make sure one instance runs only one pulling scheduler
        if (!isRunning) {
            registerShutdownProcess();
            scheduledExecutorService.scheduleAtFixedRate(new PullingTask(redissonClient, config),
                    0, config.getSchedule(), TimeUnit.SECONDS);
            isRunning = true;
            log.info("[ReDeQ] Job transferring started! Action will be executed every {} seconds.", config.getSchedule());
        }
    }

    private static void registerShutdownProcess() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                scheduledExecutorService.shutdown();
                log.warn("[ReDeQ] Job transferring will shutdown soon!");
                if (!scheduledExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    log.warn("[ReDeQ] Job transferring did not shutdown gracefully within "
                            + "20 seconds. Proceeding will forceful shutdown");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }
}
