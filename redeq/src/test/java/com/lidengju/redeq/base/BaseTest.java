package com.lidengju.redeq.base;

import com.lidengju.redeq.model.DelayedJob;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
public abstract class BaseTest {
    public static DelayedJob getJob() {
        DelayedJob job = (new DelayedJob.Builder()).withBase("test").build();
        job.setRetry(3);
        job.setDelay(10L);
        return job;
    }


}
