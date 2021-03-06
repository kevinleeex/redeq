package com.lidengju.redeq.service;


import com.lidengju.redeq.model.DelayedJob;

import java.util.List;

/**
 * ReDeQ job operation service.
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 **/
public interface RedeqService {
    /**
     * Add a task to BucketQueue and JobPool
     *
     * @param job   Job for adding
     * @param cover allow cover the Job by topicJobId
     * @return if add succeed, 1 - succeed, 0 - failed
     */
    int addJob(DelayedJob job, boolean cover);

    /**
     * Add a task to BucketQueue and JobPool, with cover not allowed
     *
     * @param job Job for adding
     * @return if add succeed, 1 - succeed, 0 - failed
     */
    int addJob(DelayedJob job);

    /**
     * Remove a task from BucketQueue and JobPool
     *
     * @param job Job for removing
     * @return if remove succeed, 1 - succeed, 0 - failed
     */
    int removeJob(DelayedJob job);

    /**
     * Poll a task from ReadyQueue and get related job from JobPool
     *
     * @param topics topic of ready queue
     * @return the first ready task for consuming in specified topics
     */
    DelayedJob pollTask(List<String> topics);
}
