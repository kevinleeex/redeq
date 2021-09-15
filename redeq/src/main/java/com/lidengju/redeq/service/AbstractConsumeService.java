package com.lidengju.redeq.service;

import com.lidengju.redeq.config.RedeqConfig;
import com.lidengju.redeq.constant.StatusEnum;
import com.lidengju.redeq.model.DelayedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Consume service interface.
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/4
 **/
public abstract class AbstractConsumeService {

    private static final Logger log = LoggerFactory.getLogger(AbstractConsumeService.class);

    private List<String> topics;

    private RedeqConfig config;

    private volatile StatusEnum status;

    protected AbstractConsumeService() {
        status = StatusEnum.READY;
    }

    public void onSucceed(DelayedJob job) {
        if (config.isVerbose()) {
            log.info("Consume Job in Topic: [{}] with Id: [{}] succeed after retry remains [{}] times!", job.getTopic(), job.getJobId(), job.getRetry());
        }
    }

    public void onFailed(DelayedJob job) {
        if (config.isVerbose()) {
            log.info("Consume Job in Topic: [{}] with Id: [{}] failed after retry exceeds!", job.getTopic(), job.getJobId());
        }
    }

    public void onRetry(DelayedJob job) {
        if (config.isVerbose()) {
            log.info("Consume Job in Topic: [{}] with Id: [{}] need retry! Left {} retry chances.", job.getTopic(), job.getJobId(), job.getRetry());
        }
    }

    /**
     * execute when consuming
     *
     * @param job delayedJob
     * @return consume true or false
     */
    public abstract boolean onConsume(DelayedJob job);

    public final boolean consume(DelayedJob job) {
        return onConsume(job);
    }

    /**
     * get topics related to current consumer service
     *
     * @return topics - List of String
     */
    public final List<String> getTopics() {
        return this.topics;
    }

    public final void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setConfig(RedeqConfig redeqConfig) {
        this.config = redeqConfig;
    }

    public RedeqConfig getConfig() {
        return config;
    }
}
