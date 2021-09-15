package com.lidengju.redeq.model;

import com.google.gson.Gson;
import com.lidengju.redeq.constant.RedeqConstants;
import com.lidengju.redeq.utils.CommonUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * The processing job entity of ReDeQ.
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 **/
public class DelayedJob implements Serializable {

    /**
     * original jobId, in case the jobId changed
     */
    private final String srcId;
    /**
     * job ID, to uniquely identify every task, used as
     * the key of `Job Pool`, and the value of `Bucket Queue`.
     */
    private String jobId;
    /**
     * route id, calculated by hashCode(srcId) % concurrency
     */
    private int routeId = 0;
    /**
     * message for logging
     */
    private String msg;
    /**
     * the topic of the your application scene for using the bucket Queue.
     */
    private String topic;
    /**
     * the content for consuming.
     */
    private String body;
    /**
     * the retry countdown.
     */
    private int retry = -1;
    /**
     * the delayed time in seconds
     */
    private long delay = -1;
    /**
     * the timestamp for next execution.
     */
    private long nextExecTimestamp = -1;
    /**
     * the timestamp for creation.
     */
    private long createTimestamp;
    /**
     * the timestamp for update.
     */
    private long updateTimestamp;

    public DelayedJob(String jobId, String topic, String body) {
        this.jobId = jobId;
        this.srcId = jobId;
        this.topic = topic;
        this.body = body;
        long now = CommonUtils.nowInMillis();
        this.createTimestamp = now;
        this.updateTimestamp = now;
    }

    public String getSrcId() {
        return srcId;
    }

    /**
     * refresh the JobId with appendix of retry count
     */
    private void refreshJobId() {
        jobId = srcId + "_RETRY" + retry;
    }

    /**
     * invoke this when retry, rename the jobId and reduce retry times
     *
     * @return left retry times
     */
    public int retry() {
        refreshJobId();
        return --retry;
    }

    public String getJobId() {
        return jobId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public Long getNextExecTimestamp() {
        return nextExecTimestamp;
    }

    public void setNextExecTimestamp(Long nextExecTimestamp) {
        this.nextExecTimestamp = nextExecTimestamp;
    }

    public String getTopicId() {
        return topic + RedeqConstants.SEP + jobId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRouteId() {
        return routeId;
    }

    /**
     * don't set routeId manually unless you know exactly what you are doing
     *
     * @param modBy modBy by this number
     */
    public void setRouteId(int modBy) {
        this.routeId = (srcId.hashCode() & Integer.MAX_VALUE) & (modBy-1);
    }

    /**
     * Delayed Job Builder
     */
    public static class Builder {
        private static final Gson gson = new Gson();
        private String jobId;

        private String topic;

        private String body;

        public Builder withBase(String topic, String jobId) {
            if (CommonUtils.isEmpty(topic) || CommonUtils.isEmpty(jobId)) {
                throw new IllegalArgumentException("Topic or jobid shouldn't be null or empty.");
            }
            this.jobId = jobId;
            this.topic = topic;
            return this;
        }

        public Builder withBase(String topic) {
            if (CommonUtils.isEmpty(topic)) {
                throw new IllegalArgumentException("Topic shouldn't be null or empty.");
            }
            // generate a random uuid
            this.jobId = UUID.randomUUID().toString();
            this.topic = topic;
            return this;
        }

        public Builder withBody(Object body) {
            if (body instanceof String) {
                this.body = (String) body;
            } else {
                this.body = gson.toJson(body);
            }

            return this;
        }

        public DelayedJob build() {
            return new DelayedJob(this.jobId, this.topic, this.body);
        }
    }
}
