package redeq.service;

import com.lidengju.redeq.api.RedeqClient;
import com.lidengju.redeq.model.DelayedJob;
import com.lidengju.redeq.service.AbstractConsumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/5
 */
@Service
@Slf4j
public class RedeqConsumerService implements ApplicationRunner {
    @Autowired
    private RedeqClient redeqClient;

    @Override
    public void run(ApplicationArguments args) {
        List<String> topics = new ArrayList<>();
        topics.add("topic1");
        topics.add("topic2");
        topics.add("topic3");
        topics.add("topic4");
        topics.forEach(x -> redeqClient.subscribe(x, new AbstractConsumeService() {
            @Override
            public boolean onConsume(DelayedJob delayedJob) {
                log.info("Consuming the topic {}", delayedJob.getTopic());
                return delayedJob.getRetry() < 2;
            }
        }));
    }
}
