package com.lidengju.redeq.api;


import com.lidengju.redeq.config.RedeqConfig;
import com.lidengju.redeq.constant.ErrorCodeEnum;
import com.lidengju.redeq.constant.StatusEnum;
import com.lidengju.redeq.exception.RedeqRuntimeException;
import com.lidengju.redeq.model.DelayedJob;
import com.lidengju.redeq.scheduled.RedeqTimer;
import com.lidengju.redeq.service.AbstractConsumeService;
import com.lidengju.redeq.service.RedeqService;
import com.lidengju.redeq.service.RedeqServiceImpl;
import com.lidengju.redeq.utils.CommonUtils;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ReDeQ Client implementation
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 **/
public class Redeq implements RedeqClient {

    private static final Logger log = LoggerFactory.getLogger(Redeq.class);
    private static final AtomicInteger topicCnt = new AtomicInteger(0);
    private final RedeqService redeqService;
    private final RedeqConfig redeqConfig;
    private final ExecutorService executorService;
    private final RedissonClient redissonClient;
    private final RedeqTimer redeqTimer;

    /**
     * constructor for RedeqClient, configured redisson client and Redeq config is required.
     *
     * @param redissonClient configured redisson client
     * @param redeqConfig    Redeq config
     */
    public Redeq(RedissonClient redissonClient, RedeqConfig redeqConfig) {
        this.redeqConfig = redeqConfig;
        this.redissonClient = redissonClient;
        this.executorService = new ThreadPoolExecutor(2,
                redeqConfig.getMaxTopics() * 2,
                redeqConfig.getSchedule(),
                TimeUnit.SECONDS,
                new SynchronousQueue<>(true));
        this.redeqService = new RedeqServiceImpl(redissonClient, redeqConfig);
        this.redeqTimer = RedeqTimer.getTimer();
        this.redeqTimer.init(this.redissonClient, this.redeqConfig);
        registerExecutorShutdownProcess();
    }

    /**
     * @return count of subscribed topic
     */
    public static AtomicInteger getTopicCnt() {
        return topicCnt;
    }

    @Override
    public int add(DelayedJob job) {
        log.info("[ReDeQ Client] add a job[topicId: {}] to JobPool.", job.getTopicId());
        return redeqService.addJob(job);
    }

    @Override
    public int remove(DelayedJob job) {
        log.info("[ReDeQ Client] remove a job[topicId: {}] to JobPool.", job.getTopicId());
        return redeqService.removeJob(job);
    }

    @Override
    public DelayedJob poll(List<String> topicList) {
        if (topicList == null || topicList.isEmpty()) {
            throw new IllegalArgumentException("[ReDeQ Client] topic list should not be null or empty!");
        }
        log.info("[ReDeQ Client] poll a task from ReadyQueue.");

        return redeqService.pollTask(topicList);
    }

    public DelayedJob poll(String topic) {
        List<String> topicList = new ArrayList<>();
        topicList.add(topic);
        return poll(topicList);
    }


    @Override
    public void subscribe(List<String> topics, AbstractConsumeService consumeService) {
        consumeService.setTopics(topics);
        consumeService.setConfig(redeqConfig);
        check(consumeService);

        registerConsumerShutdownProcess(consumeService);

        redeqTimer.startTransfer();
        if (topicCnt.getAndIncrement() < redeqConfig.getMaxTopics()) {
            log.info("[ReDeQ Client] consumer service for topic {} started.", topics);
            executorService.execute(() -> {
                consumeService.setStatus(StatusEnum.RUNNING);
                process(consumeService);
            });
        } else {
            log.error("[ReDeQ Client] {}", ErrorCodeEnum.TOPIC_EXCEEDS);
            log.error("Topic {} will not be consumed", topics);
        }
    }

    @Override
    public void subscribe(String topic, AbstractConsumeService consumeService) {
        List<String> topics = new ArrayList<>();
        topics.add(topic);
        subscribe(topics, consumeService);
    }

    /**
     * check if configuration is all set
     */
    private void check(AbstractConsumeService consumeService) {
        if (consumeService.getTopics() == null || consumeService.getTopics().isEmpty()) {
            throw new RedeqRuntimeException(ErrorCodeEnum.TOPIC_CONFIG_ERROR);
        }
    }

    /**
     * process when a delayed job is consuming.
     *
     * @param consumeService - User defined consumer service.
     */
    private void process(AbstractConsumeService consumeService) {
        Thread.currentThread().setName(consumeService.getTopics().toString());
        while (consumeService.getStatus() == StatusEnum.RUNNING) {
            DelayedJob job = null;
            try {
                // acceptable topics
                List<String> topics = consumeService.getTopics();
                // poll task from ReadyQueue
                job = redeqService.pollTask(topics);
                DelayedJob finalJob = job;
                if (job != null) {
                    FutureTask<Boolean> futureTask = new FutureTask<>(() -> consumeService.consume(finalJob));
                    executorService.execute(futureTask);
                    // if consume succeed, remove the job from JobPool, and execute onSucceed
                    if (Boolean.TRUE.equals(futureTask.get())) {
                        redeqService.removeJob(job);
                        consumeService.onSucceed(job);
                    } else {
                        // execute when retry
                        needRetry(consumeService, job);
                    }
                }
            } catch (InterruptedException e) {
                log.warn("Consume interrupt happened in Thread [{}]", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                consumeService.onFailed(job);
            } catch (Exception e) {
                log.error("Something wrong happened during consuming...", e);
                consumeService.onFailed(job);
            }
        }
    }

    /**
     * Execute this when retry
     * if retry times&lt;0, job will be removed and invoke <code>onFailed</code> method,
     * else update jobId of the job and put the new job into BucketQueue, the old one
     * should be removed if the new is added successfully.
     *
     * @param consumeService - User defined consumer service
     * @param job            - delayed job
     */
    private void needRetry(AbstractConsumeService consumeService, DelayedJob job) {
        // retry count exceeds, then execute onFailed()
        redeqService.removeJob(job);
        if (job.retry() < 0) {
            consumeService.onFailed(job);
            return;
        }
        long nextExecTime = job.getNextExecTimestamp() + job.getDelay() * 1000;
        job.setUpdateTimestamp(CommonUtils.nowInMillis());
        job.setNextExecTimestamp(nextExecTime);
        if (redeqService.addJob(job) < 1) {
            throw new RedeqRuntimeException(ErrorCodeEnum.REDIS_ERROR);
        }
        // consume retry
        consumeService.onRetry(job);
    }

    private void registerExecutorShutdownProcess() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                executorService.shutdown();
                log.warn("[ReDeQ] Job consuming will shutdown soon!");
                if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                    log.warn("[ReDeQ] Job consuming did not shutdown gracefully within "
                            + "20 seconds. Proceeding will forceful shutdown");
                }
                redissonClient.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    private void registerConsumerShutdownProcess(AbstractConsumeService consumeService) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> consumeService.setStatus(StatusEnum.CANCELED)));
    }
}
