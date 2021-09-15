package com.lidengju.redeq.scheduled;

import com.lidengju.redeq.config.RedeqConfig;
import com.lidengju.redeq.constant.ErrorCodeEnum;
import com.lidengju.redeq.constant.RedeqConstants;
import com.lidengju.redeq.exception.RedeqException;
import com.lidengju.redeq.utils.CommonUtils;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Pulling job from Bucket Queue to Ready Queue every scheduled time.
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 **/
public class PullingTask extends TimerTask {

    private static final Logger log = LoggerFactory.getLogger(PullingTask.class);

    private final RedissonClient redissonClient;

    private final RedeqConfig redeqConfig;

    private final int routeId;

    public PullingTask(RedissonClient redissonClient, RedeqConfig redeqConfig, int routeId) {
        this.redissonClient = redissonClient;
        this.redeqConfig = redeqConfig;
        this.routeId = routeId;
    }

    @Override
    public void run() {
        try {
            pulling();
        } catch (Exception e) {
            log.error("Something wrong happened during pulling job", e);
        }
    }

    public void pulling() {
        log.debug("Start pulling the job from Bucket Queue to Ready Queue...");

        RLock lock = redissonClient.getLock(redeqConfig.getPrefix() + RedeqConstants.PULLING_JOB_LOCK + routeId);
        try {
            boolean lockFlag = lock.tryLock(redeqConfig.getAcquireLockTimeout(), redeqConfig.getExpireLockTimeout(), TimeUnit.SECONDS);
            if (!lockFlag) {
                throw new RedeqException(ErrorCodeEnum.ACQUIRE_LOCK_FAIL);
            }

            /* ---------- 1. Get job list with score less than current timestamp ---------- */
            RScoredSortedSet<Object> bucketQueue = redissonClient.getScoredSortedSet(redeqConfig.getPrefix() + RedeqConstants.ZSET_BUCKET_PRE + routeId);
            long now = CommonUtils.nowInMillis();
            Collection<Object> delayedJobs = bucketQueue.valueRange(0, false, now, true);
            List<String> taskList = delayedJobs.stream().map(String::valueOf).collect(Collectors.toList());
            if (taskList.isEmpty()) {
                return;
            }

            // 1.1 Group the job list with topic name
            Map<String, List<TopicJob>> topicMap = taskList.stream().map(x -> {
                String topic = x.split(RedeqConstants.SEP, 2)[0];
                return new TopicJob(topic, x);
            }).collect(Collectors.groupingBy(TopicJob::getTopic));

            /* ---------- 2. Transfer jobs from Bucket Queue to correlated Ready Queue ---------- */
            for (Map.Entry<String, List<TopicJob>> topic : topicMap.entrySet()) {
                List<String> curTaskList = topic.getValue().stream().map(TopicJob::getTopicId).collect(Collectors.toList());

                RBlockingDeque<String> readyQueue = redissonClient.getBlockingDeque(redeqConfig.getPrefix() + RedeqConstants.READY_QUEUE_PRE + topic.getKey());
                boolean addFlag = readyQueue.addAll(curTaskList);
                if (addFlag) {
                    bucketQueue.removeAllAsync(curTaskList);
                } else {
                    throw new RedeqException(ErrorCodeEnum.REDIS_ERROR);
                }
            }
        } catch (InterruptedException e) {
            log.error("Thread interrupted during pulling!", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Pull job failed!{}", ErrorCodeEnum.PULLING_SCHEDULE_FAILED);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private static class TopicJob {
        private String topic;
        private String topicId;

        public TopicJob(String topic, String topicId) {
            this.topic = topic;
            this.topicId = topicId;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getTopicId() {
            return topicId;
        }

        public void setTopicId(String topicId) {
            this.topicId = topicId;
        }
    }
}
