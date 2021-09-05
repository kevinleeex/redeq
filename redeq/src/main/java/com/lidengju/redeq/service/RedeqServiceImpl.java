package com.lidengju.redeq.service;

import com.lidengju.redeq.config.RedeqConfig;
import com.lidengju.redeq.constant.ErrorCodeEnum;
import com.lidengju.redeq.constant.RedeqConstants;
import com.lidengju.redeq.exception.RedeqException;
import com.lidengju.redeq.model.DelayedJob;
import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ReDeQ job operation service impl.
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 **/
public class RedeqServiceImpl implements RedeqService {

    private static final Logger log = LoggerFactory.getLogger(RedeqServiceImpl.class);

    private final RedissonClient redissonClient;

    private final RedeqConfig redeqConfig;

    public RedeqServiceImpl(RedissonClient redissonClient, RedeqConfig redeqConfig) {
        this.redissonClient = redissonClient;
        this.redeqConfig = redeqConfig;
    }

    /**
     * Add a Job to BucketQueue and JobPool
     *
     * @param job the delayed Job to add
     * @param cover if allow cover the Job by topicId
     */
    @Override
    public int addJob(DelayedJob job, boolean cover) {
        RLock lock = redissonClient.getLock(
                redeqConfig.getPrefix()
                        + RedeqConstants.ADD_JOB_LOCK
                        + job.getTopicId());
        try {
            boolean lockFlag = lock.tryLock(redeqConfig.getAcquireLockTimeout(),
                    redeqConfig.getExpireLockTimeout(),
                    TimeUnit.SECONDS);
            if (!lockFlag) {
                throw new RedeqException(ErrorCodeEnum.ACQUIRE_LOCK_FAIL);
            }
            // check job validation, fill delay, retry and next execution time props
            if (job.getDelay() == null || job.getDelay() < 0) {
                job.setDelay(redeqConfig.getDelay());
            }
            if (job.getRetry() == null || job.getRetry() < 0) {
                job.setRetry(redeqConfig.getRetry());
            }
            if (job.getNextExecTimestamp() == null || job.getNextExecTimestamp() < 0) {
                job.setNextExecTimestamp(job.getCreateTimestamp() + job.getDelay() * 1000);
            }

            // create batch execution
            String topicId = job.getTopicId();
            RMap<String, DelayedJob> jobPool = redissonClient.getMap(redeqConfig.getPrefix() + RedeqConstants.JOB_POOL_KEY_PRE);
            if (jobPool.size() >= redeqConfig.getMaxPool()) {
                throw new RedeqException(ErrorCodeEnum.JOB_POOL_EXCEEDED);
            }
            if (!cover && jobPool.containsKey(job.getTopicId())){
                throw new RedeqException(ErrorCodeEnum.JOB_ALREADY_EXIST);
            }
            /* ---------- 1. Add job to Job Pool ---------- */
            jobPool.put(topicId, job);
            /* ---------- 2. Add job to Bucket Queue(zset) ---------- */
            RScoredSortedSet<String> bucketQueue = redissonClient.getScoredSortedSet(redeqConfig.getPrefix() + RedeqConstants.ZSET_BUCKET_PRE + job.getRouteId());
            bucketQueue.add(job.getNextExecTimestamp(), topicId);
            return 1;
        } catch (InterruptedException e) {
            log.warn("Thread interrupted during adding!", e);
            Thread.currentThread().interrupt();
        } catch (RedeqException e) {
            log.error("Add job failed, msg: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Some wrong during adding a job!", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return 0;
    }

    public int addJob(DelayedJob job){
        return addJob(job, true);
    }

    /**
     * Remove a Job from BucketQueue and JobPool
     *
     * @param job the delayed job to remove
     */
    @Override
    public int removeJob(DelayedJob job) {
        RLock lock = redissonClient.getLock(RedeqConstants.REMOVE_JOB_LOCK + job.getTopicId());

        try {
            boolean lockFlag = lock.tryLock(redeqConfig.getAcquireLockTimeout(),
                    redeqConfig.getExpireLockTimeout(),
                    TimeUnit.SECONDS);
            if (!lockFlag) {
                throw new RedeqException(ErrorCodeEnum.ACQUIRE_LOCK_FAIL);
            }
            // create batch execution
            String topicId = job.getTopicId();
            /* ---------- 1. Remove job from Bucket Queue(zset) ---------- */
            RScoredSortedSet<String> bucketQueue = redissonClient.getScoredSortedSet(redeqConfig.getPrefix() + RedeqConstants.ZSET_BUCKET_PRE + job.getRouteId());
            bucketQueue.remove(topicId);

            /* ---------- 2. Remove job from Job Pool ---------- */
            RMap<String, DelayedJob> jobPool = redissonClient.getMap(redeqConfig.getPrefix() + RedeqConstants.JOB_POOL_KEY_PRE);
            jobPool.remove(topicId);
            return 1;
        } catch (InterruptedException e) {
            log.warn("Thread interrupted during removing!", e);
            Thread.currentThread().interrupt();
        } catch (RedeqException e) {
            log.error("Remove job failed, msg: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Some wrong during removing a job!", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return 0;
    }

    /**
     * Poll a task from ReadyQueue and get related job from JobPool
     *
     * @param topics topic of ready queue
     * @return the first ready task for consuming in specified topics
     */
    @Override
    public DelayedJob pollTask(List<String> topics) {
        long initLockWaitTime = 0L;
        /* ---------- 1. try to get the ready queue consumer lock sequentially ---------- */
        DelayedJob delayedJob;
        Collections.shuffle(topics);
        for (String topic : topics) {
            try {
                delayedJob = tryGetDelayedJob(topic, initLockWaitTime);
            } catch (Exception e) {
                delayedJob = null;
            }
            if (delayedJob != null) {
                return delayedJob;
            }
        }
        /* ---------- 2. if all consumer locks are taken, randomly choose a topic and wait getting lock ---------- */
        try {
            delayedJob = tryGetDelayedJob(topics.get(0), redeqConfig.getAcquireLockTimeout());
        } catch (Exception e) {
            delayedJob = null;
        }
        return delayedJob;
    }

    private DelayedJob tryGetDelayedJob(String topic, long waitTime) {
        RLock lock = redissonClient.getLock(RedeqConstants.CONSUME_TASK_LOCK + topic);
        try {
            boolean lockFlag = lock.tryLock(waitTime,
                    redeqConfig.getExpireLockTimeout(),
                    TimeUnit.SECONDS);
            if (!lockFlag) {
                throw new RedeqException(ErrorCodeEnum.ACQUIRE_LOCK_FAIL);
            }
            /* ---------- 1. Get task topicId from ReadyQueue ---------- */
            RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque(redeqConfig.getPrefix() + RedeqConstants.READY_QUEUE_PRE + topic);
            String topicId = blockingDeque.poll(redeqConfig.getPollQueueTimeout(), TimeUnit.SECONDS);
            if (null == topicId || "".equals(topicId)) {
                return null;
            }
            if (redeqConfig.isVerbose()) {
                log.info("Thread:[{}] polled the Job with TopicId: [{}]", Thread.currentThread().getName(), topicId);
            }
            /* ---------- 2. Get job from JobPool by topicId ---------- */
            RMap<String, DelayedJob> jobPool = redissonClient.getMap(redeqConfig.getPrefix() + RedeqConstants.JOB_POOL_KEY_PRE);
            return jobPool.get(topicId);
        } catch (InterruptedException e) {
            log.warn("Thread interrupted during polling!", e);
            Thread.currentThread().interrupt();
        } catch (RedeqException e) {
            log.debug("Topic: [{}], {}", topic, e.getErrorCode());
        } catch (Exception e) {
            log.error("Poll task failed!", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return null;
    }
}
