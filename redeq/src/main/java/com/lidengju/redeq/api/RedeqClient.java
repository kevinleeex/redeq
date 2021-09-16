package com.lidengju.redeq.api;

import com.lidengju.redeq.config.RedeqConfig;
import com.lidengju.redeq.model.DelayedJob;
import com.lidengju.redeq.service.AbstractConsumeService;

import java.util.List;

/**
 * The interface of Redeq client.
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 **/
public interface RedeqClient {
    /**
     * Add a delayed job to Job Pool and Bucket Queue
     *
     * @param job - DelayedJob
     * @return 1 if add succeed, 0 if failed
     */
    int add(DelayedJob job);

    /**
     * Add a delayed job to Job Pool and Bucket Queue
     *
     * @param job - DelayedJob
     * @param cover - if cover the job with duplicated jobId
     * @return 1 if add succeed, 0 if failed
     */
    int add(DelayedJob job, boolean cover);

    /**
     * Remove a delayed job from Job Pool and Bucket Queue
     *
     * @param job - DelayedJob
     * @return 1 if remove succeed, 0 if failed
     */
    int remove(DelayedJob job);

    /**
     * Poll a ready task from Ready Queue with specified topics
     *
     * @param topicList - List of topics
     * @return the delayed job
     */
    DelayedJob poll(List<String> topicList);

    /**
     * This method will start a new thread to continuously consume the DelayedJob.
     *
     * @param topics         - subscribed topics for consuming
     * @param consumeService - DelayedJobConsumer service
     */
    void subscribe(List<String> topics, AbstractConsumeService consumeService);

    /**
     * This method will start a new thread to continuously consume the DelayedJob.
     *
     * @param topic          - subscribed topic for consuming
     * @param consumeService - DelayedJobConsumer service
     */
    void subscribe(String topic, AbstractConsumeService consumeService);

    /**
     * Allows to get config loaded.
     *
     * @return RedeqConfig
     */
    RedeqConfig getRedeqConfig();
}
