package com.lidengju.redeq.api;

import com.lidengju.redeq.base.BaseTest;
import com.lidengju.redeq.config.RedeqConfig;
import com.lidengju.redeq.model.DelayedJob;
import com.lidengju.redeq.service.AbstractConsumeService;
import com.lidengju.redeq.service.RedeqServiceImpl;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
class RedeqTest extends BaseTest {

    @Mocked
    private RedeqServiceImpl redeqService;
    @Mocked
    private Redisson redissonClient;
    @Mocked
    private ThreadPoolExecutor executorService;
    private Redeq redeq;

    @BeforeEach
    void setUp() {
        RedeqConfig redeqConfig = new RedeqConfig();
        redeqConfig.setMaxSubscribers(1);
        redeq = new Redeq(redissonClient, redeqConfig);
    }

    @Test
    void add_failed() {
        // Record
        new Expectations() {
            {
                redeqService.addJob((DelayedJob) any, true);
                result = 0;
            }
        };
        // Replay
        int real = redeq.add(getJob());

        // Verify
        Assertions.assertEquals(0, real);
    }

    @Test
    void add_succeed() {
        // Record
        new Expectations() {
            {
                redeqService.addJob((DelayedJob) any, true);
                result = 1;
            }
        };
        // Replay
        int real = redeq.add(getJob());

        // Verify
        Assertions.assertEquals(1, real);
    }

    @Test
    void remove_failed() {
        // Record
        new Expectations() {
            {
                redeqService.removeJob((DelayedJob) any);
                result = 0;
            }
        };
        // Replay
        int real = redeq.remove(getJob());

        // Verify
        Assertions.assertEquals(0, real);
    }

    @Test
    void remove_succeed() {
        // Record
        new Expectations() {
            {
                redeqService.removeJob((DelayedJob) any);
                result = 1;
            }
        };
        // Replay
        int real = redeq.remove(getJob());

        // Verify
        Assertions.assertEquals(1, real);
    }

    @Test
    void poll() {
        // Record
        DelayedJob job = getJob();
        new Expectations() {
            {
                redeqService.pollTask((List<String>) any);
                result = job;
            }
        };
        // Replay
        DelayedJob real = redeq.poll("test");

        // Verify
        Assertions.assertSame(job, real);
    }

    @Test
    void subscribe() {
        // Record
        DelayedJob job = getJob();

        // Replay
        redeq.subscribe("test1", new AbstractConsumeService() {
            @Override
            public boolean onConsume(DelayedJob job) {
                return false;
            }
        });
        Assertions.assertEquals(1, Redeq.getTopicCnt().intValue());
        new Verifications() {
            {
                executorService.execute((Runnable) any);
                times = 1;
            }
        };

        redeq.subscribe("test2", new AbstractConsumeService() {
            @Override
            public boolean onConsume(DelayedJob job) {
                return false;
            }
        });
        Assertions.assertEquals(2, Redeq.getTopicCnt().intValue());
        new Verifications() {
            {
                executorService.execute((Runnable) any);
                times = 1;
            }
        };
    }
}